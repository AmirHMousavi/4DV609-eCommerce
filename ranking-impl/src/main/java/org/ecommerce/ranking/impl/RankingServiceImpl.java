package org.ecommerce.ranking.impl;

import static org.ecommerce.security.ServerSecurity.authenticated;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.ecommerce.ranking.api.CreateRankingRequest;
import org.ecommerce.ranking.api.CreateRankingResponse;
import org.ecommerce.ranking.api.Ranking;
import org.ecommerce.ranking.api.RankingService;
import org.ecommerce.item.api.ItemService;
import org.ecommerce.item.api.Item;
import org.ecommerce.message.api.Message;
import org.ecommerce.message.api.MessageService;
import org.ecommerce.user.api.User;
import org.ecommerce.user.api.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import akka.Done;
import akka.NotUsed;

public class RankingServiceImpl implements RankingService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RankingServiceImpl.class);

	private final PersistentEntityRegistry persistentEntities;
	private final CassandraSession db;
	MessageService messageService;
	ItemService itemService;
	UserService userService;

	@Inject
	public RankingServiceImpl(ItemService itemService, MessageService messageService, UserService userService,
			PersistentEntityRegistry persistentEntities, CassandraReadSide readSide, CassandraSession db) {
		this.persistentEntities = persistentEntities;
		this.db = db;
		this.messageService = messageService;
		this.itemService = itemService;
		this.userService = userService;

		persistentEntities.register(RankingEntity.class);
		readSide.register(RankingEventProcessor.class);
	}

	@Override
	public ServiceCall<NotUsed, Ranking> getRanking(String rankingId) {
		return (req) -> {
			return persistentEntities.refFor(RankingEntity.class, rankingId).ask(GetRanking.of()).thenApply(reply -> {
				LOGGER.info(String.format("The seller was rated", rankingId));
				if (reply.getRanking().isPresent())
					return reply.getRanking().get();
				else
					throw new NotFound(String.format("The rating was not received", rankingId));
			});
		};
	}

	@Override
	public ServiceCall<CreateRankingRequest, Done> setRanking(String rankingId) {
		return authenticated(userId -> request -> {
			LOGGER.info("Rating a seller: ", request);
			// UUID uuid = UUID.fromString(rankingId);
			// BigDecimal bigdecimal =
			// BigDecimal.valueOf(Long.parseLong(request));
			return persistentEntities.refFor(RankingEntity.class, rankingId).ask(ChangeRanking.of(request.getRating()));
		});
	}

	@Override
	public ServiceCall<CreateRankingRequest, CreateRankingResponse> createRanking() {
		return authenticated(userId -> request -> {
			LOGGER.info("Rating a seller: ", request);
			UUID uuid = UUID.randomUUID();
			CompletionStage<String> msg = itemService.setSold(request.getItemId().toString()).invoke(uuid.toString());
			String isSold = msg.toCompletableFuture().join();

			msg = messageService.setSold(request.getMessageId().toString()).invoke(uuid.toString());
			isSold = msg.toCompletableFuture().join();
			
			return persistentEntities.refFor(RankingEntity.class, uuid.toString()).ask(CreateRanking.of(request));
		});
	}
}
