package org.ecommerce.user.impl;

import org.ecommerce.user.api.CreateUserResponse;
import org.ecommerce.user.api.CreateUserRequest;
import org.ecommerce.user.api.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as=UserState.class)
public interface AbstractUserState extends Jsonable {

    @Value.Parameter
    Optional<User> getUser();

    @Value.Parameter
    LocalDateTime getTimestamp();
}
