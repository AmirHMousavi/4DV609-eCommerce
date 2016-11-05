package org.ecommerce.message.api;

import java.util.UUID;
import org.immutables.value.Value;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractCreateMessageResponse extends Jsonable {

	@Value.Parameter
	UUID getMessageId();

}
