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
	UUID getId();

	@Value.Parameter
	String getUserId();

	@Value.Parameter
	UUID getItemId();

	@Value.Parameter
	String getMessage();

}
