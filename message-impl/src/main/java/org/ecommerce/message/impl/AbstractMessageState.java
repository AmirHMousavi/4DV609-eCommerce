package org.ecommerce.message.impl;

import org.ecommerce.message.api.Message;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractMessageState extends Jsonable {

	@Value.Parameter
	Optional<Message> getMessage();

	@Value.Parameter
	LocalDateTime getTimestamp();

}
