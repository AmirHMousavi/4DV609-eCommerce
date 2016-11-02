package org.ecommerce.item.api;

import java.math.BigDecimal;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractCreateItemRequest extends Jsonable {
	
	@Value.Parameter
    String getUserId();

    @Value.Parameter
    String getName();
    
    @Value.Parameter
    String getDescription();
    
    @Value.Parameter
    String getPhoto();

    @Value.Parameter
    BigDecimal getPrice();

    @Value.Check
    default void check() {
        Preconditions.checkState(getPrice().signum() > 0, "Price must be a positive value");
    }
}
