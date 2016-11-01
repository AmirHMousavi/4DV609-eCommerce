package org.ecommerce.item.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class ItemEventTag {
	public static final AggregateEventTag<ItemEvent> INSTANCE =
            AggregateEventTag.of(ItemEvent.class);
}
