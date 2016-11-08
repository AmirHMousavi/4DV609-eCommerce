package org.ecommerce.message.impl;

import org.ecommerce.message.api.Message;
import org.ecommerce.message.api.CreateMessageRequest;
import org.ecommerce.message.api.CreateMessageResponse;

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

public class MessageEventProcessor extends CassandraReadSideProcessor<MessageEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageEventProcessor.class);

	@Override
	public AggregateEventTag<MessageEvent> aggregateTag() {
		return MessageEventTag.INSTANCE;
	}

	private PreparedStatement writeMessage = null;
	private PreparedStatement writeOffset = null;

	private void setWriteMessage(PreparedStatement writeMessage) {
		this.writeMessage = writeMessage;
	}

	private void setWriteOffset(PreparedStatement writeOffset) {
		this.writeOffset = writeOffset;
	}

	/**
	 * Prepare read-side table and statements
	 */
	@Override
	public CompletionStage<Optional<UUID>> prepare(CassandraSession session) {
		return prepareCreateTables(session).thenCompose(a -> prepareWriteMessage(session)
				.thenCompose(b -> prepareWriteOffset(session).thenCompose(c -> selectOffset(session))));
	}

	private CompletionStage<Done> prepareCreateTables(CassandraSession session) {
		LOGGER.info("Creating Cassandra tables...");
		return session
				.executeCreateTable("CREATE TABLE IF NOT EXISTS message ("
						+ "messageId uuid, userId text, itemId uuid, isSold text, message text, timestamp bigint, PRIMARY KEY (messageId, itemId))")
				.thenCompose(a -> session.executeCreateTable("CREATE TABLE IF NOT EXISTS message_offset ("
						+ "partition int, offset timeuuid, PRIMARY KEY (partition))"));
	}

	private CompletionStage<Done> prepareWriteMessage(CassandraSession session) {
		LOGGER.info("Inserting into read-side table message...");
		return session.prepare("INSERT INTO message (messageId, userId, itemId, isSold, message, timestamp) VALUES (?, ?, ?, ?, ?, ?)")
				.thenApply(ps -> {
					setWriteMessage(ps);
					return Done.getInstance();
				});
	}

	private CompletionStage<Done> prepareWriteOffset(CassandraSession session) {
		LOGGER.info("Inserting into read-side table message_offset...");
		return session.prepare("INSERT INTO message_offset (partition, offset) VALUES (1, ?)").thenApply(ps -> {
			setWriteOffset(ps);
			return Done.getInstance();
		});
	}

	private CompletionStage<Optional<UUID>> selectOffset(CassandraSession session) {
		LOGGER.info("Looking up message_offset");
		return session.selectOne("SELECT offset FROM message_offset")
				.thenApply(optionalRow -> optionalRow.map(r -> r.getUUID("offset")));
	}

	/**
	 * Bind the read side persistence to the MessageCreated event.
	 */
	@Override
	public EventHandlers defineEventHandlers(EventHandlersBuilder builder) {
		LOGGER.info("Setting up read-side event handlers...");
		builder.setEventHandler(MessageCreated.class, this::processMessageCreated);
		return builder.build();
	}

	/**
	 * Write a persistent event into the read-side optimized database.
	 */
	private CompletionStage<List<BoundStatement>> processMessageCreated(MessageCreated event, UUID offset) {
		BoundStatement bindWriteMessage = writeMessage.bind();
		bindWriteMessage.setUUID("messageId", event.getMessage().getMessageId());
		bindWriteMessage.setString("userId", event.getMessage().getUserId());
		bindWriteMessage.setUUID("itemId", event.getMessage().getItemId());
		//bindWriteMessage.setString("sellerId", event.getMessage().getSellerId());
		bindWriteMessage.setString("isSold", event.getMessage().getIsSold());
		bindWriteMessage.setString("message", event.getMessage().getMessage());
		bindWriteMessage.setLong("timestamp", event.getMessage().getTimestamp().toEpochMilli());
		BoundStatement bindWriteOffset = writeOffset.bind(offset);
		LOGGER.info("Persisted Message {}", event.getMessage());
		return completedStatements(Arrays.asList(bindWriteMessage, bindWriteOffset));
	}
}
