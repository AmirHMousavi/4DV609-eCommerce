package org.ecommerce.ranking.impl;

import org.ecommerce.ranking.api.Ranking;
import org.ecommerce.ranking.api.CreateRankingRequest;
import org.ecommerce.ranking.api.CreateRankingResponse;
import java.time.Instant;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;

public interface RankingEvent extends Jsonable, AggregateEvent<RankingEvent> {

	@Value.Immutable
	@ImmutableStyle
	@JsonDeserialize
	interface AbstractRankingCreated extends RankingEvent {
		@Override
		default AggregateEventTag<RankingEvent> aggregateTag() {
			return RankingEventTag.INSTANCE;
		}

		@Value.Parameter
		Ranking getRanking();

		@Value.Default
		default Instant getTimestamp() {
			return Instant.now();
		}
	}
}
