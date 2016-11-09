package org.ecommerce.user.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.ecommerce.user.api.CreateUserResponse;
import org.ecommerce.user.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import akka.Done;

public class UserEntity extends PersistentEntity<UserCommand, UserEvent, UserState> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserEntity.class);

	@Override
	public Behavior initialBehavior(Optional<UserState> snapshotState) {
		LOGGER.info("Setting up initialBehaviour with snapshotState = {}", snapshotState);
		BehaviorBuilder b = newBehaviorBuilder(
				snapshotState.orElse(UserState.of(Optional.empty(), LocalDateTime.now())));

		// Register command handler
		b.setCommandHandler(CreateUser.class, (cmd, ctx) -> {
			if (state().getUser().isPresent()) {
				ctx.invalidCommand(String.format("User %s is already created, UserId Should Be Unique", entityId()));
				return ctx.done();
			} else {
				User user = User.of(cmd.getCreateUserRequest().getUserId(), cmd.getCreateUserRequest().getPassword(),
						new ArrayList<>());
				final UserCreated userCreated = UserCreated.builder().user(user).build();
				LOGGER.info("Processed CreateUser command into UserCreated event {}", userCreated);
				return ctx.thenPersist(userCreated,
						evt -> ctx.reply(CreateUserResponse.of(userCreated.getUser().getUserId())));
			}
		});

		// Register command handler
		b.setCommandHandler(SetRank.class, (cmd, ctx) -> {
			if (state().getUser().isPresent()) {
				User user=state().getUser().get();
				ArrayList<BigDecimal> ranks = state().getUser().get().getRanks();
				ranks.add(cmd.getTheRank());
				final RankCreated rankCreated = RankCreated.builder().user(user).build();
				LOGGER.info("Processed CreateRank command into RankCreated event {}", rankCreated);
				return ctx.thenPersist(rankCreated,
						evt -> ctx.reply(Done.getInstance()));
			}
			return ctx.done();
		});

		// Register event handler
		b.setEventHandler(UserCreated.class, evt -> {
			LOGGER.info("Processed UserCreated event, updated user state");
			return state().withUser(evt.getUser()).withTimestamp(LocalDateTime.now());
		});
		
		// Register event handler
				b.setEventHandler(RankCreated.class, evt -> {
					LOGGER.info("Processed RankCreated event, updated user state");
					return state().withUser(evt.getUser()).withTimestamp(LocalDateTime.now());
				});

		// Register read-only handler eg a handler that doesn't result in events
		// being created
		b.setReadOnlyCommandHandler(GetUser.class, (cmd, ctx) -> {
			LOGGER.info("Processed GetUser command, returned user");
			ctx.reply(GetUserReply.of(state().getUser()));
		});

		return b.build();
	}
}