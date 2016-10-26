package org.ecommerce.ranking.api;

import java.math.BigDecimal;
import java.util.UUID;
import org.immutables.value.Value;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;

import scala.reflect.internal.Trees.Try;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractRanking {

	@Value.Parameter
	UUID getId();

	@Value.Parameter
	String getUserId();

	@Value.Parameter
	UUID getItemId();

	@Value.Parameter
	BigDecimal getRating();

	@Value.Check
	default void check() {
		Preconditions.checkState(getRating().signum() <=5 ,"Rating must be between 1 to 5");
	}
}
