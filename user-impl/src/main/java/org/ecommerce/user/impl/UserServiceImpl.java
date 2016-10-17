package org.ecommerce.user.impl;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.ecommerce.user.api.*;
import org.ecommerce.user.api.UserService;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import akka.NotUsed;


public class UserServiceImpl implements UserService {

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
		return request -> {
			return UserEntityRef(userId).ask(new GetUser()).thenApply(reply -> {
				if (reply.user.isPresent() && reply.user.get().isPasswordOK(password))
					return reply.user.get();
				else
					throw new NotFound("user " + userId + " not found");
			});
		};
	}

	@Override
	public ServiceCall<User, NotUsed> createUser() {
		return request -> {
			return UserEntityRef(request.getUserId()).ask(CreateUser.of(request)).thenApply(ack -> NotUsed.getInstance());
		};
	}

	@Override
	public ServiceCall<NotUsed, PSequence<String>> getAllUsers() {
		return request -> {
			CompletionStage<PSequence<String>> result = db.selectAll("SELECT * FROM user").thenApply(rows -> {
				List<String> users = rows.stream().map(row -> row.getString("userId")).collect(Collectors.toList());
				return TreePVector.from(users);
			});
			return result;
		};
	}

	private Object UserEntityRef(String userId) {
		PersistentEntityRef<UserCommand> ref = persistentEntities.refFor(UserEntity.class, userId);
		return ref;
	}

}
