package org.ecommerce.message.impl;

import static org.ecommerce.security.ServerSecurity.authenticated;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ecommerce.message.api.CreateMessageRequest;
import org.ecommerce.message.api.CreateMessageResponse;
import org.ecommerce.message.api.Message;
import org.ecommerce.message.api.MessageService;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import akka.NotUsed;

public class MessageServiceImpl implements MessageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

	private final PersistentEntityRegistry persistentEntities;
	private final CassandraSession db;

	@Inject
	public MessageServiceImpl(PersistentEntityRegistry persistentEntities, CassandraReadSide readSide,
			CassandraSession db) {
		this.persistentEntities = persistentEntities;
		this.db = db;

		persistentEntities.register(MessageEntity.class);
		readSide.register(MessageEventProcessor.class);

	}

	@Override
	public ServiceCall<NotUsed, Message> getMessage(String messageId) {
		return (req) -> {
			return persistentEntities.refFor(MessageEntity.class, messageId).ask(GetMessage.of()).thenApply(reply -> {
				LOGGER.info(String.format("Message received", messageId));
				if (reply.getMessage().isPresent())
					return reply.getMessage().get();
				else
					throw new NotFound(String.format("Message was not received", messageId));
			});
		};
	}

	@Override
	public ServiceCall<CreateMessageRequest, CreateMessageResponse> sendMessage() {
		return authenticated(userId -> request -> {
			LOGGER.info("Sending a message: ", request);
			UUID uuid = UUID.randomUUID();
			return persistentEntities.refFor(MessageEntity.class, uuid.toString()).ask(CreateMessage.of(request));
		});
	}

	@Override
	public ServiceCall<NotUsed, PSequence<Message>> getAllMessages() {
		return (req) -> {
			LOGGER.info("Looking up all messages");
			CompletionStage<PSequence<Message>> res = db.selectAll("SELECT * FROM message").thenApply(rows -> {
				List<Message> messages = rows.stream()
						.map(row -> Message.of(row.getUUID("messageId"), row.getString("userId"), row.getUUID("itemId"),
								row.getString("isSold"),
								/* row.getString("sellerId") */ row.getString("message"),
								Instant.ofEpochMilli(row.getLong("timestamp"))))
						.collect(Collectors.toList());
				return TreePVector.from(messages);
			});
			return res;
		};
	}

	@Override
	public ServiceCall<NotUsed, PSequence<Message>> getAllMessagesByItemId(String itemId) {
		return (req) -> {
			LOGGER.info("Looking up all messages");
			UUID uuid = UUID.fromString(itemId);
			CompletionStage<PSequence<Message>> result = db
					.selectAll("SELECT * FROM message WHERE itemId = ?  ALLOW FILTERING", uuid).thenApply(rows -> {
						List<Message> messages = rows.stream()
								.map(row -> Message.of(row.getUUID("messageId"), row.getString("userId"),
										row.getUUID("itemId"), row.getString("isSold"), row.getString("message"),
										Instant.ofEpochMilli(row.getLong("timestamp"))))
								.collect(Collectors.toList());
						return TreePVector.from(messages);
					});
			return result;
		};
	}

	@Override
	public ServiceCall<NotUsed, String> getIsSold(String itemId) {
		final String invalidSold = "-1";
		return (req) -> {
			LOGGER.info("Looking up all messages");
			UUID uuid = UUID.fromString(itemId);
			CompletionStage<String> result = db
					.selectAll("SELECT * FROM message WHERE itemId = ? ALLOW FILTERING", uuid).thenApply(rows -> {
						List<Message> messages = rows.stream()
								.map(row -> Message.of(row.getUUID("messageId"), row.getString("userId"),
										row.getUUID("itemId"), row.getString("isSold"), row.getString("message"),
										Instant.ofEpochMilli(row.getLong("timestamp"))))
								.collect(Collectors.toList());
						for (Message message : messages) {
							String soldId = message.getIsSold();
							if (soldId.equals(invalidSold))
								return soldId;
						}
						return invalidSold;
					});
			return result;
		};
	}

	@Override
	public ServiceCall<NotUsed, PSequence<Message>> getAllMessagesByUserId(String userId) {
		return (req) -> {
			LOGGER.info("Looking up all messages");
			CompletionStage<PSequence<Message>> theResult = db
					.selectAll("SELECT * FROM message WHERE userId = ? ALLOW FILTERING", userId).thenApply(rows -> {
						List<Message> messages = rows.stream()
								.map(row -> Message.of(row.getUUID("messageId"), row.getString("userId"),
										row.getUUID("itemId"), row.getString("isSold"), row.getString("message"),
										Instant.ofEpochMilli(row.getLong("timestamp"))))
								.collect(Collectors.toList());
						return TreePVector.from(messages);
					});
			return theResult;
		};
	}

	@Override
	public ServiceCall<String, String> setSold(String id) {
		return request -> {
			LOGGER.info("Selling message: {}", request);
			return persistentEntities.refFor(MessageEntity.class, id).ask(SetMessageSold.of(request));
		};
	}
}
