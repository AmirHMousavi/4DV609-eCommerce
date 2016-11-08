package org.ecommerce.item.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;

import org.ecommerce.item.api.CreateItemRequest;
import org.ecommerce.item.api.CreateItemResponse;
import org.ecommerce.item.api.Item;
import org.immutables.value.Value;

import java.time.Instant;

public interface ItemEvent extends Jsonable, AggregateEvent<ItemEvent> {
	
	@Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractItemCreated extends ItemEvent {
    	
        @Override
        default AggregateEventTag<ItemEvent> aggregateTag() {
            return ItemEventTag.INSTANCE;
        }

        @Value.Parameter
        Item getItem();

        @Value.Default
        default Instant getTimestamp() {
            return Instant.now();
        }
    }
	
	@Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractItemSold extends ItemEvent {
    	
        @Override
        default AggregateEventTag<ItemEvent> aggregateTag() {
            return ItemEventTag.INSTANCE;
        }

        @Value.Parameter
        Item getItem();

        @Value.Default
        default Instant getTimestamp() {
            return Instant.now();
        }
    }
}
