package org.ecommerce.user.impl;

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

public class UserEventProcessor extends CassandraReadSideProcessor<UserEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEventProcessor.class);

    @Override
    public AggregateEventTag<UserEvent> aggregateTag() {
        return UserEventTag.INSTANCE;
    }

    private PreparedStatement writeUser = null; // initialized in prepare
    private PreparedStatement writeOffset = null; // initialized in prepare

    private void setWriteUser(PreparedStatement writeUser) {
        this.writeUser = writeUser;
    }

    private void setWriteOffset(PreparedStatement writeOffset) {
        this.writeOffset = writeOffset;
    }

    /**
     * Prepare read-side table and statements
     */
    @Override
    public CompletionStage<Optional<UUID>> prepare(CassandraSession session) {
        return
                prepareCreateTables(session).thenCompose(a ->
                        prepareWriteUser(session).thenCompose(b ->
                                prepareWriteOffset(session).thenCompose(c ->
                                        selectOffset(session))));
    }

    private CompletionStage<Done> prepareCreateTables(CassandraSession session) {
        LOGGER.info("Creating Cassandra tables...");
        return session.executeCreateTable(
                "CREATE TABLE IF NOT EXISTS user ("
                        + "userId text, password text, PRIMARY KEY (userId))")
                .thenCompose(a -> session.executeCreateTable(
                        "CREATE TABLE IF NOT EXISTS user_offset ("
                                + "partition int, offset timeuuid, PRIMARY KEY (partition))"));
    }

    private CompletionStage<Done> prepareWriteUser(CassandraSession session) {
        LOGGER.info("Inserting into read-side table user...");
        return session.prepare("INSERT INTO user (userId, password) VALUES (?, ?)").thenApply(ps -> {
            setWriteUser(ps);
            return Done.getInstance();
        });
    }

    private CompletionStage<Done> prepareWriteOffset(CassandraSession session) {
        LOGGER.info("Inserting into read-side table user_offset...");
        return session.prepare("INSERT INTO user_offset (partition, offset) VALUES (1, ?)").thenApply(ps -> {
            setWriteOffset(ps);
            return Done.getInstance();
        });
    }

    private CompletionStage<Optional<UUID>> selectOffset(CassandraSession session) {
        LOGGER.info("Looking up user_offset");
        return session.selectOne("SELECT offset FROM user_offset")
                .thenApply(
                        optionalRow -> optionalRow.map(r -> r.getUUID("offset")));
    }

    /**
     * Bind the read side persistence to the UserCreated event.
     */
    @Override
    public EventHandlers defineEventHandlers(EventHandlersBuilder builder) {
        LOGGER.info("Setting up read-side event handlers...");
        builder.setEventHandler(UserCreated.class, this::processUserCreated);
        return builder.build();
    }

    /**
     * Write a persistent event into the read-side optimized database.
     */
    private CompletionStage<List<BoundStatement>> processUserCreated(UserCreated event, UUID offset) {
        BoundStatement bindWriteUser = writeUser.bind();
        bindWriteUser.setString("userId", event.getUser().getUserId());
        bindWriteUser.setString("password", event.getUser().getPassword());
        BoundStatement bindWriteOffset = writeOffset.bind(offset);
        LOGGER.info("Persisted User {}", event.getUser());
        return completedStatements(Arrays.asList(bindWriteUser, bindWriteOffset));
    }}