package org.ecommerce.message.impl;


import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class MessageEventTag {
	
	public static final AggregateEventTag<MessageEvent> INSTANCE =
            AggregateEventTag.of(MessageEvent.class);

}
