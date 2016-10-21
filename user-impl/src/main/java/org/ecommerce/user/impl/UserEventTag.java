package org.ecommerce.user.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

import org.ecommerce.user.api.CreateUserResponse;
import org.ecommerce.user.api.CreateUserRequest;
import org.ecommerce.user.api.User;

public class UserEventTag {
    public static final AggregateEventTag<UserEvent> INSTANCE =
            AggregateEventTag.of(UserEvent.class);
}
