package org.ecommerce.ranking.impl;

import org.ecommerce.ranking.api.Ranking;
import org.ecommerce.ranking.api.CreateRankingRequest;
import org.ecommerce.ranking.api.CreateRankingResponse;

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

public class RankingEventProcessor extends CassandraReadSideProcessor<RankingEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RankingEventProcessor.class);

	@Override
	public AggregateEventTag<RankingEvent> aggregateTag() {
		return RankingEventTag.INSTANCE;
	}

	private PreparedStatement writeRanking = null;
	private PreparedStatement writeOffset = null;

	private void setWriteRanking(PreparedStatement writeRanking) {
		this.writeRanking = writeRanking;
	}

	private void setWriteOffset(PreparedStatement writeOffset) {
		this.writeOffset = writeOffset;
	}

	/**
	 * Prepare read-side table and statements
	 */
	@Override
	public CompletionStage<Optional<UUID>> prepare(CassandraSession session) {
		return prepareCreateTables(session).thenCompose(a -> prepareWriteRanking(session)
				.thenCompose(b -> prepareWriteOffset(session).thenCompose(c -> selectOffset(session))));
	}

	private CompletionStage<Done> prepareCreateTables(CassandraSession session) {
		LOGGER.info("Creating Cassandra tables...");
		return session
				.executeCreateTable("CREATE TABLE IF NOT EXISTS ranking ("
						+ "rankingId uuid, messageId uuid, itemId uuid, rating decimal, PRIMARY KEY (rankingId, messageId, itemId))")
				.thenCompose(a -> session.executeCreateTable("CREATE TABLE IF NOT EXISTS ranking_offset ("
						+ "partition int, offset timeuuid, PRIMARY KEY (partition))"));
	}

	private CompletionStage<Done> prepareWriteRanking(CassandraSession session) {
		LOGGER.info("Inserting into read-side table ranking...");
		return session.prepare("INSERT INTO ranking (rankingId, messageId, itemId, rating) VALUES (?, ?, ?, ?)")
			   .thenApply(ps -> {
			setWriteRanking(ps);
			return Done.getInstance();
		});
	}

	private CompletionStage<Done> prepareWriteOffset(CassandraSession session) {
		LOGGER.info("Inserting into read-side table ranking_offset...");
		return session.prepare("INSERT INTO ranking_offset (partition, offset) VALUES (1, ?)").thenApply(ps -> {
			setWriteOffset(ps);
			return Done.getInstance();
		});
	}

	private CompletionStage<Optional<UUID>> selectOffset(CassandraSession session) {
		LOGGER.info("Looking up ranking_offset");
		return session.selectOne("SELECT offset FROM ranking_offset")
				.thenApply(optionalRow -> optionalRow.map(r -> r.getUUID("offset")));
	}

	/**
	 * Bind the read side persistence to the RankingCreated event.
	 */
	@Override
	public EventHandlers defineEventHandlers(EventHandlersBuilder builder) {
		LOGGER.info("Setting up read-side event handlers...");
		builder.setEventHandler(RankingCreated.class, this::processRankingCreated);
		builder.setEventHandler(RankingChanged.class, this::processRankingChanged);
		return builder.build();
	}
	

	/**
	 * Write a persistent event into the read-side optimized database.
	 */
	private CompletionStage<List<BoundStatement>> processRankingCreated(RankingCreated event, UUID offset) {
		BoundStatement bindWriteRanking = writeRanking.bind();
		bindWriteRanking.setUUID("rankingId", event.getRanking().getRankingId());
		bindWriteRanking.setUUID("messageId", event.getRanking().getMessageId());
		bindWriteRanking.setUUID("itemId", event.getRanking().getItemId());
		bindWriteRanking.setDecimal("rating", event.getRanking().getRating());
		BoundStatement bindWriteOffset = writeOffset.bind(offset);
		LOGGER.info("Persisted ranking {}", event.getRanking());
		return completedStatements(Arrays.asList(bindWriteRanking, bindWriteOffset));
	}
	
	
	private CompletionStage<List<BoundStatement>> processRankingChanged(RankingChanged event, UUID offset) {
		BoundStatement bindWriteRanking = writeRanking.bind();
		bindWriteRanking.setUUID("rankingId", event.getRanking().getRankingId());
		bindWriteRanking.setUUID("messageId", event.getRanking().getMessageId());
		bindWriteRanking.setUUID("itemId", event.getRanking().getItemId());
		bindWriteRanking.setDecimal("rating", event.getRanking().getRating());
		BoundStatement bindWriteOffset = writeOffset.bind(offset);
		LOGGER.info("Persisted ranking {}", event.getRanking());
		return completedStatements(Arrays.asList(bindWriteRanking, bindWriteOffset));
	}

}
