package org.ecommerce.user.api;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = CreateUserResponse.class)
public interface AbstractCreateUserResponse extends Jsonable {

	@Value.Parameter
	String getUserId();
}