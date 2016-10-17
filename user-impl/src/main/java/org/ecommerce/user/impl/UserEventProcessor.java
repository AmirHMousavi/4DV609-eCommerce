package org.ecommerce.user.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import akka.Done;

public class UserEventProcessor extends CassandraReadSideProcessor<UserEvent> {

	private PreparedStatement writeUsers = null; // initialized in prepare
	private PreparedStatement writeOffset = null; // initialized in prepare
	// *********************************************************

	private void setWriteUsers(PreparedStatement writeUsers) {
		this.writeUsers = writeUsers;
	}

	private void setWriteOffset(PreparedStatement writeOffset) {
		this.writeOffset = writeOffset;
	}

	// ***********************************************************
	@Override
	public AggregateEventTag<UserEvent> aggregateTag() {
		return UserEventTag.INSTANCE;
	}


	// ************************************************************
	@Override
	public CompletionStage<Optional<UUID>> prepare(CassandraSession session) {
		// @formatter:off
		return prepareCreateTables(session).thenCompose(a -> prepareWriteUsers(session)
				.thenCompose(b -> prepareWriteOffset(session).thenCompose(c -> selectOffset(session))));
		// @formatter:on
	}

	private CompletionStage<Done> prepareCreateTables(CassandraSession session) {

		return session
				.executeCreateTable(
						"CREATE TABLE IF NOT EXISTS user (" + "userId text, password text, " + "PRIMARY KEY (userId))")
				.thenCompose(a -> session.executeCreateTable("CREATE TABLE IF NOT EXISTS user_offset ("
						+ "partition int, offset timeuuid, " + "PRIMARY KEY (partition))"));
	}

	private CompletionStage<Done> prepareWriteUsers(CassandraSession session) {
		return session.prepare("INSERT INTO user (userId, password) VALUES (?, ?)").thenApply(ps -> {
			setWriteUsers(ps);
			return Done.getInstance();
		});
	}

	private CompletionStage<Done> prepareWriteOffset(CassandraSession session) {
		return session.prepare("INSERT INTO user_offset (partition, offset) VALUES (1, ?)").thenApply(ps -> {
			setWriteOffset(ps);
			return Done.getInstance();
		});
	}

	private CompletionStage<Optional<UUID>> selectOffset(CassandraSession session) {
		return session.selectOne("SELECT offset FROM user_offset WHERE partition=1")
				.thenApply(optionalRow -> optionalRow.map(r -> r.getUUID("offset")));
	}
	// ********************************************************
	@Override
	public EventHandlers defineEventHandlers(EventHandlersBuilder builder) {
		builder.setEventHandler(UserCreated.class, this::processUserCreated);
		return builder.build();
	}
	private CompletionStage<List<BoundStatement>> processUserCreated(UserCreated event, UUID offset) {
		BoundStatement bindWriteUsers = writeUsers.bind();
		bindWriteUsers.setString("userId", event.userId);
		bindWriteUsers.setString("password", event.password);
		BoundStatement bindWriteOffset = writeOffset.bind(offset);
		return completedStatements(Arrays.asList(bindWriteUsers, bindWriteOffset));
	}

}
