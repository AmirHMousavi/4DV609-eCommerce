package org.ecommerce.user.impl;

import org.ecommerce.user.api.User;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

import java.time.Instant;

public interface UserEvent extends Jsonable, AggregateEvent<UserEvent> {

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractUserCreated extends UserEvent {
        @Override
        default AggregateEventTag<UserEvent> aggregateTag() {
            return UserEventTag.INSTANCE;
        }

        @Value.Parameter
        User getUser();

        @Value.Default
        default Instant getTimestamp() {
            return Instant.now();
        }
    }
}
