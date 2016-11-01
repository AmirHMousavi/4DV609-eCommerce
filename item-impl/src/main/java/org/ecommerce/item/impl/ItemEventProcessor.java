package org.ecommerce.item.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import akka.Done;

public class ItemEventProcessor extends CassandraReadSideProcessor<ItemEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemEventProcessor.class);

	@Override
	public AggregateEventTag<ItemEvent> aggregateTag() {
		return ItemEventTag.INSTANCE;
	}

	// ******************************************************************************************
	private PreparedStatement writeItem = null; // initialized in prepare
	private PreparedStatement writeOffset = null; // initialized in prepare

	private void setWriteItem(PreparedStatement writeItem) {
		this.writeItem = writeItem;
	}

	private void setWriteOffset(PreparedStatement writeOffset) {
		this.writeOffset = writeOffset;
	}


	// *******************************************************************************************
	/**
	 * Prepare read-side table and statements
	 */
	@Override
	public CompletionStage<Optional<UUID>> prepare(CassandraSession session) {
		return prepareCreateTables(session).thenCompose(a -> prepareWriteItem(session)
				.thenCompose(b -> prepareWriteOffset(session).thenCompose(c -> selectOffset(session))));
	}

	private CompletionStage<Done> prepareCreateTables(CassandraSession session) {
		LOGGER.info("Creating Cassandra tables...");
		return session
				.executeCreateTable("CREATE TABLE IF NOT EXISTS item ("
						+ "itemId uuid, name text, price decimal, PRIMARY KEY (itemId))")
				.thenCompose(a -> session.executeCreateTable("CREATE TABLE IF NOT EXISTS item_offset ("
						+ "partition int, offset timeuuid, PRIMARY KEY (partition))"));
	}

	private CompletionStage<Done> prepareWriteItem(CassandraSession session) {
		LOGGER.info("Inserting into read-side table item...");
		return session.prepare("INSERT INTO item (itemId, name, price) VALUES (?, ?, ?)").thenApply(ps -> {
			setWriteItem(ps);
			return Done.getInstance();
		});
	}

	private CompletionStage<Done> prepareWriteOffset(CassandraSession session) {
		LOGGER.info("Inserting into read-side table item_offset...");
		return session.prepare("INSERT INTO item_offset (partition, offset) VALUES (1, ?)").thenApply(ps -> {
			setWriteOffset(ps);
			return Done.getInstance();
		});
	}

	private CompletionStage<Optional<UUID>> selectOffset(CassandraSession session) {
		LOGGER.info("Looking up item_offset");
		return session.selectOne("SELECT offset FROM item_offset")
				.thenApply(optionalRow -> optionalRow.map(r -> r.getUUID("offset")));
	}
	// **********************************************************************************************
	// ********************************************************************************************
	@Override
	public EventHandlers defineEventHandlers(EventHandlersBuilder builder) {
		LOGGER.info("Setting up read-side event handlers...");
		builder.setEventHandler(ItemCreated.class, this::processItemCreated);
		return builder.build();
	}

	/**
	 * Write a persistent event into the read-side optimized database.
	 */
	private CompletionStage<List<BoundStatement>> processItemCreated(ItemCreated event, UUID offset) {
		BoundStatement bindWriteItem = writeItem.bind();
		bindWriteItem.setString("userId", event.getItem().getUserId());
		bindWriteItem.setUUID("itemId", event.getItem().getItemId());
		bindWriteItem.setString("name", event.getItem().getName());
		bindWriteItem.setDecimal("price", event.getItem().getPrice());
		BoundStatement bindWriteOffset = writeOffset.bind(offset);
		LOGGER.info("Persisted Item {}", event.getItem());
		return completedStatements(Arrays.asList(bindWriteItem, bindWriteOffset));
	}
}
