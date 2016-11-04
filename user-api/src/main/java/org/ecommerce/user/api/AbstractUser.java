package org.ecommerce.user.api;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = User.class)
public interface AbstractUser {

	@Value.Parameter
	String getUserId();

	@Value.Parameter
	String getPassword();
	
	@Value.Parameter
	boolean getAuthStatus();

	@Value.Check
	default boolean checkPassword(String password) {
		return getPassword().equals(password);
	}
}