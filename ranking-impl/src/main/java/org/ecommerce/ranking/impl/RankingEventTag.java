package org.ecommerce.ranking.impl;


import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class RankingEventTag {
	
	public static final AggregateEventTag<RankingEvent> INSTANCE =
            AggregateEventTag.of(RankingEvent.class);

}
