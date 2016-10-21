package org.ecommerce.user.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;

import org.immutables.value.Value;
import java.math.BigDecimal;
import java.util.UUID;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = User.class)
public interface AbstractUser {

	@Value.Parameter
	String getUserId();

	@Value.Parameter
	String getPassword();

	@Value.Check
	default boolean checkPassword(String password) {
		return getPassword().equals(password);
	}
}