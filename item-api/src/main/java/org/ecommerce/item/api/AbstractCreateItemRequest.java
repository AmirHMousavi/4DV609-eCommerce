/**
 * Abstract Interface for Create Item Request
 */
package org.ecommerce.item.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;
import java.math.BigDecimal;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractCreateItemRequest extends Jsonable {
	
	@Value.Parameter
	String getUserId();

    @Value.Parameter
    String getName();

    @Value.Parameter
    BigDecimal getPrice();

    @Value.Check
    default void check() {
        Preconditions.checkState(getPrice().signum() > 0, "Price must be a positive value");
    }
}
