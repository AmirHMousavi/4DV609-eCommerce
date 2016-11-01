package org.ecommerce.item.impl;

import org.ecommerce.item.api.CreateItemRequest;
import org.ecommerce.item.api.CreateItemResponse;
import org.ecommerce.item.api.Item;
import org.ecommerce.item.api.ItemService;
import org.pcollections.PSequence;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class ItemServiceImpl implements ItemService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemServiceImpl.class);

	private final PersistentEntityRegistry persistentEntities;
	private final CassandraSession db;

	@Inject
	public ItemServiceImpl(PersistentEntityRegistry persistentEntities, CassandraReadSide readSide,
			CassandraSession db) {
		this.persistentEntities = persistentEntities;
		this.db = db;

		persistentEntities.register(ItemEntity.class);
		readSide.register(ItemEventProcessor.class);
	}

	@Override
	public ServiceCall<NotUsed, Item> getItem(String itemId) {
		return (req) -> {
			return persistentEntities.refFor(ItemEntity.class, itemId).ask(GetItem.of()).thenApply(reply -> {
				LOGGER.info(String.format("Looking up item %s", itemId));
				if (reply.getItem().isPresent())
					return reply.getItem().get();
				else
					throw new NotFound(String.format("No item found for id %s", itemId));
			});
		};
	}

	@Override
	public ServiceCall<NotUsed, PSequence<Item>> getAllItems() {
		return (req) -> {
			LOGGER.info("Looking up all items");
			CompletionStage<PSequence<Item>> result = db.selectAll("SELECT userId, itemId, name, price FROM item")
					.thenApply(rows -> {
						List<Item> items = rows.stream().map(
								row -> Item.of(row.getString("userId"),row.getUUID("itemId"), row.getString("name"), row.getDecimal("price")))
								.collect(Collectors.toList());
						return TreePVector.from(items);
					});
			return result;
		};
	}

	@Override
	public ServiceCall<CreateItemRequest, CreateItemResponse> createItem() {
		return request -> {
            LOGGER.info("Creating item: {}", request);
            UUID uuid = UUID.randomUUID();
            return persistentEntities.refFor(ItemEntity.class, uuid.toString())
                    .ask(CreateItem.of(request));
        };
	}

}
