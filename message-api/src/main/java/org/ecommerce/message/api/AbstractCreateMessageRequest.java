package org.ecommerce.message.api;

import java.time.Instant;
import java.util.UUID;
import org.immutables.value.Value;
import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractCreateMessageRequest extends Jsonable {

	@Value.Parameter
	String getUserId();

	@Value.Parameter
	UUID getItemId();

	@Value.Parameter
	String getIsSold();

	// @Value.Parameter
	// String getSellerId();

	@Value.Parameter
	String getMessage();
	
	@Value.Parameter
	Instant getTimestamp();

}
