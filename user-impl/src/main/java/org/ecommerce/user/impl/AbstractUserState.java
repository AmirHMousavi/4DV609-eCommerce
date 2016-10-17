package org.ecommerce.user.impl;

import org.ecommerce.user.api.User;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;


@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = UserState.class)
public interface AbstractUserState extends Jsonable {
	
	@Value.Parameter
	Optional<User> getUser();


}
