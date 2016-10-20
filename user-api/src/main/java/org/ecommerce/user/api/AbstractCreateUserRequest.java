package org.ecommerce.user.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

import java.math.BigDecimal;


@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = CreateUserRequest.class)
public interface AbstractCreateUserRequest extends Jsonable {

    @Value.Parameter
    String getUserId();

    @Value.Parameter
    String getPassword();

    @Value.Check
    default void checkPassword(String password) {
        Preconditions.checkState(getPassword().equals(password), "Password is not correct");
    }
}