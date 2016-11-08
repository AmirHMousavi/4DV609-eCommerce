package org.ecommerce.item.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;
import java.math.BigDecimal;
import java.util.UUID;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractItem {

	@Value.Parameter
    UUID getId();
	
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
    
    @Value.Parameter
    String getIsSold();
    

    @Value.Check
    default void check() {
        Preconditions.checkState(getPrice().signum() > 0, "Price must be a positive value");
    }
}
