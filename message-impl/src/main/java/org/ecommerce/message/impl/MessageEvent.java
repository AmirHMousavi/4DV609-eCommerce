package org.ecommerce.message.impl;

import org.ecommerce.message.api.Message;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

import java.time.Instant;

public interface MessageEvent extends Jsonable, AggregateEvent<MessageEvent> {

	@Value.Immutable
	@ImmutableStyle
	@JsonDeserialize
	interface AbstractMessageCreated extends MessageEvent {
		@Override
		default AggregateEventTag<MessageEvent> aggregateTag() {
			return MessageEventTag.INSTANCE;
		}

		@Value.Parameter
		Message getMessage();

		@Value.Default
		default Instant getTimestamp() {
			return Instant.now();
		}
	}

	@Value.Immutable
	@ImmutableStyle
	@JsonDeserialize
	interface AbstractMessageSold extends MessageEvent {
		@Override
		default AggregateEventTag<MessageEvent> aggregateTag() {
			return MessageEventTag.INSTANCE;
		}

		@Value.Parameter
		Message getMessage();

		@Value.Default
		default Instant getTimestamp() {
			return Instant.now();
		}
	}

}
