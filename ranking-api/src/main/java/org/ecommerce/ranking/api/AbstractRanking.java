package org.ecommerce.ranking.api;

import java.math.BigDecimal;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractRanking {

	@Value.Parameter
	UUID getRankingId();

	@Value.Parameter
	String getUserId();

	@Value.Parameter
	UUID getItemId();

	@Value.Parameter
	BigDecimal getRating();

	@Value.Check
	default void check() {
		Preconditions.checkState(getRating().intValue() <= 5 && getRating().intValue() != 0, "Rating must be between 1 and 5");
	}
}
