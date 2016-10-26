package org.ecommerce.ranking.impl;

import org.ecommerce.ranking.api.Ranking;
import org.ecommerce.ranking.api.CreateRankingRequest;
import org.ecommerce.ranking.api.CreateRankingResponse;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;

public interface RankingCommand extends Jsonable {

	@Value.Immutable
	@ImmutableStyle
	@JsonDeserialize
	public interface AbstractCreateRanking
			extends RankingCommand, CompressedJsonable, PersistentEntity.ReplyType<CreateRankingResponse> {

		@Value.Parameter
		CreateRankingRequest getCreateRankingRequest();
	}

	@Value.Immutable(singleton = true)
	@ImmutableStyle
	@JsonDeserialize
	public interface AbstractGetRanking
			extends RankingCommand, CompressedJsonable, PersistentEntity.ReplyType<GetRankingReply> {

	}

	@Value.Immutable
	@ImmutableStyle
	@JsonDeserialize
	public interface AbstractGetRankingReply extends Jsonable {

		@Value.Parameter
		Optional<Ranking> getRanking();
	}
}