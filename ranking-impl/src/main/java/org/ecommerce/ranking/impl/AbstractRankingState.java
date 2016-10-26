package org.ecommerce.ranking.impl;

import org.ecommerce.ranking.api.Ranking;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractRankingState extends Jsonable {

	@Value.Parameter
	Optional<Ranking> getRanking();

	@Value.Parameter
	LocalDateTime getTimestamp();

}
