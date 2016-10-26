package org.ecommerce.ranking.impl;

import org.ecommerce.ranking.api.Ranking;
import org.ecommerce.ranking.api.CreateRankingRequest;
import org.ecommerce.ranking.api.CreateRankingResponse;
import org.ecommerce.ranking.api.AbstractRanking;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

public class RankingEntity extends PersistentEntity<RankingCommand, RankingEvent, RankingState> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RankingEntity.class);

	@Override
    public Behavior initialBehavior(Optional<RankingState> snapshotState) {
        LOGGER.info("Setting up initialBehaviour with snapshotState = {}", snapshotState);
        BehaviorBuilder b = newBehaviorBuilder(snapshotState.orElse(
                RankingState.of(Optional.empty(), LocalDateTime.now()))
        );

	// Register command handler
	 b.setCommandHandler(CreateRanking.class,(cmd,ctx)->

	{
		if (state().getRanking().isPresent()) {
			ctx.invalidCommand(String.format("The seller is Rated", entityId()));
			return ctx.done();
		} else {

			CreateRankingRequest req = cmd.getCreateRankingRequest();

			Ranking ranking = Ranking.of(UUID.fromString(entityId()), req.getUserId(), req.getItemId(), 
					req.getRating());
			final RankingCreated rankingCreated = RankingCreated.builder().ranking(ranking).build();
			LOGGER.info("Processed CreateRanking command into RankingCreated event {}", rankingCreated);
			return ctx.thenPersist(rankingCreated,
					evt -> ctx.reply(CreateRankingResponse.of(rankingCreated.getRanking().getId())));
		}
	});

	// Register event handler
	// Register event handler
     b.setEventHandler(RankingCreated.class, evt -> {
                 LOGGER.info("Processed RankingCreated event, updated rating state");
                 return state().withRanking(evt.getRanking())
                         .withTimestamp(LocalDateTime.now());
             }
     );

	// Register read-only handler eg a handler that doesn't result in events
	// being created
     // Register read-only handler eg a handler that doesn't result in events being created
     b.setReadOnlyCommandHandler(GetRanking.class,
             (cmd, ctx) -> {
                 LOGGER.info("Processed GetRanking command, returned rating");
                 ctx.reply(GetRankingReply.of(state().getRanking()));
             }
     );

     return b.build();
 }
}

