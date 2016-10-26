package org.ecommerce.message.impl;

import org.ecommerce.message.api.Message;
import org.ecommerce.message.api.CreateMessageRequest;
import org.ecommerce.message.api.CreateMessageResponse;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import com.lightbend.lagom.javadsl.api.transport.NotFound;

import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import akka.NotUsed;

import org.ecommerce.message.api.AbstractMessage;
import org.ecommerce.message.api.MessageService;

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

	}

	@Override
	public ServiceCall<NotUsed, Message> getMessage(String id) {
		return (req) -> {
			return persistentEntities.refFor(MessageEntity.class, id).ask(GetMessage.of()).thenApply(reply -> {
				LOGGER.info(String.format("Message received", id));
				if (reply.getMessage().isPresent())
					return reply.getMessage().get();
				else
					throw new NotFound(String.format("Message was not received", id));
			});
		};
	}

	@Override
	public ServiceCall<CreateMessageRequest, CreateMessageResponse> sendMessage() {
		return request -> {
			LOGGER.info("Sending a message: ", request);
			UUID uuid = UUID.randomUUID();
			return persistentEntities.refFor(MessageEntity.class, uuid.toString()).ask(CreateMessage.of(request));
		};
	}
}
