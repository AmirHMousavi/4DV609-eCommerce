package org.ecommerce.message.api;

import java.util.UUID;
import org.immutables.value.Value;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractMessage {

	@Value.Parameter
	UUID getMessageId();

	@Value.Parameter
	String getUserId();

	@Value.Parameter
	UUID getItemId();

	@Value.Parameter
	UUID getIsSold();

	// @Value.Parameter
	// String getSellerId();

	@Value.Parameter
	String getMessage();

}
