package org.ecommerce.user.impl;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;

import akka.Done;
import akka.persistence.PersistenceIdentity;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

public interface UserCommand extends Jsonable {
	// ******************************************************************
	@Value.Immutable
	@ImmutableStyle
	@JsonDeserialize(as = CreateUser.class)
	public interface AbstractCreateUser extends UserCommand, PersistentEntity.ReplyType<Done> {
		@Value.Parameter
		User getUser();
	}

	// ******************************************************************
	@Value.Immutable(singleton = true, builder = false)
	@ImmutableStyle
	@JsonDeserialize(as = GetUser.class)
	public abstract class AbstractGetUser
			implements UserCommand, PersistentEntity.ReplyType<GetUserReply> {
		protected AbstractGetUser() {

		}
	}

	// ******************************************************************
	@Value.Immutable
	@ImmutableStyle
	@JsonDeserialize(as = GetUserReply.class)
	public interface AbstractGetUserReply extends Jsonable {
		@Value.Parameter
		Optional<User> getUser();
	}

}
