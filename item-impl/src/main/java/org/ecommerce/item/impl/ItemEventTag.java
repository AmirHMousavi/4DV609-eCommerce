package org.ecommerce.item.impl;

import org.ecommerce.item.api.CreateItemRequest;
import org.ecommerce.item.api.CreateItemResponse;
import org.ecommerce.item.api.Item;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class ItemEventTag {
    public static final AggregateEventTag<ItemEvent> INSTANCE =
            AggregateEventTag.of(ItemEvent.class);
}
