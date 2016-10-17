package org.ecommerce.user.impl;

import com.lightbend.lagom.serialization.Jsonable;

import java.time.Instant;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public interface UserEvent extends Jsonable, AggregateEvent<UserEvent> {

	@Override
	default AggregateEventTag<UserEvent> aggregateTag() {
		return UserEventTag.INSTANCE;
	}

	@Value.Immutable
	@ImmutableStyle
	@JsonDeserialize (as = UserCreated.class)
	interface AbstractUserCreated extends UserEvent {
		@Value.Parameter
		String getUserId();

		@Value.Parameter
		String getPassword();

		@Value.Default
		default Instant getTimeStamp() {
			return Instant.now();
		}

	}

}
