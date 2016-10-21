package org.ecommerce.user.impl;

import java.util.Optional;

import org.ecommerce.user.api.CreateUserResponse;
import org.ecommerce.user.api.CreateUserRequest;
import org.ecommerce.user.api.User;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;

public interface UserCommand extends Jsonable {

	@Value.Immutable
	@ImmutableStyle
	@JsonDeserialize(as = CreateUser.class)
	public interface AbstractCreateUser
			extends UserCommand, CompressedJsonable, PersistentEntity.ReplyType<CreateUserResponse> {

		@Value.Parameter
		CreateUserRequest getCreateUserRequest();
	}

	@Value.Immutable(singleton = true)
	@ImmutableStyle
	@JsonDeserialize(as = GetUser.class)
	public interface AbstractGetUser extends UserCommand, CompressedJsonable, PersistentEntity.ReplyType<GetUserReply> {

	}

	@Value.Immutable
	@ImmutableStyle
	@JsonDeserialize(as = GetUserReply.class)
	public interface AbstractGetUserReply extends Jsonable {

		@Value.Parameter
		Optional<User> getUser();
	}
}
