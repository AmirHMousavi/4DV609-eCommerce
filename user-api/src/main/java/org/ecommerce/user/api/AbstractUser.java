package org.ecommerce.user.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;
import java.math.BigDecimal;
import java.util.UUID;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = User.class)
public interface AbstractUser {

	@Value.Parameter
	UUID getUUID();

	@Value.Parameter
	String getUserId();

	@Value.Parameter
	String getPassword();

	@Value.Check
	default void checkPassword(String password) {
		Preconditions.checkState(getPassword().equals(password), "Password is not correct");
	}
}