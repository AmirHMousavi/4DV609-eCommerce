package org.ecommerce.user.impl;

import org.ecommerce.user.api.CreateUserResponse;
import org.ecommerce.user.api.CreateUserRequest;
import org.ecommerce.user.api.User;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;


public class UserEventTag {
    public static final AggregateEventTag<UserEvent> INSTANCE =
            AggregateEventTag.of(UserEvent.class);
}
