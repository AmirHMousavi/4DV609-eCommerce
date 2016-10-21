package org.ecommerce.user.impl;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ecommerce.user.api.CreateUserRequest;
import org.ecommerce.user.api.CreateUserResponse;
import org.ecommerce.user.api.User;
import org.ecommerce.user.api.UserService;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import akka.NotUsed;

public class UserServiceImpl implements UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	private final PersistentEntityRegistry persistentEntities;
	private final CassandraSession db;

	@Inject
	public UserServiceImpl(PersistentEntityRegistry persistentEntities, CassandraReadSide readSide,
			CassandraSession db) {
		this.persistentEntities = persistentEntities;
		this.db = db;

		persistentEntities.register(UserEntity.class);
		readSide.register(UserEventProcessor.class);
	}

	@Override
	public ServiceCall<NotUsed, User> getUser(String userId, String password) {
		return (req) -> {
			return persistentEntities.refFor(UserEntity.class, userId).ask(GetUser.of()).thenApply(reply -> {
				LOGGER.info(String.format("Looking up user %s", userId));
				if (reply.getUser().isPresent() && reply.getUser().get().checkPassword(password))
					return reply.getUser().get();
				else
					throw new NotFound(String.format("User %s , User-ID or Password is wrong", userId));
			});
		};
	}

	@Override
	public ServiceCall<NotUsed, PSequence<CreateUserResponse>> getAllUsers() {
		return (req) -> {
			LOGGER.info("Looking up all users");
			CompletionStage<PSequence<CreateUserResponse>> result = db.selectAll("SELECT userId FROM user")
					.thenApply(rows -> {
						List<CreateUserResponse> users = rows.stream()
								.map(row -> CreateUserResponse.of(row.getString("userId")))
								.collect(Collectors.toList());
						return TreePVector.from(users);
					});
			return result;
		};
	}

	@Override
	public ServiceCall<CreateUserRequest, CreateUserResponse> createUser() {
		return request -> {
			LOGGER.info("Creating user: {}", request);
			return persistentEntities.refFor(UserEntity.class, request.getUserId()).ask(CreateUser.of(request));
		};
	}
}