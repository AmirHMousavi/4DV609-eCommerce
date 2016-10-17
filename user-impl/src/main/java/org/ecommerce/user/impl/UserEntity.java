package org.ecommerce.user.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

public class UserEntity extends PersistentEntity<UserCommand, UserEvent, UserState> {

	@Override
	public Behavior initialBehavior(Optional<UserState> snapshotState) {
		BehaviorBuilder b = newBehaviorBuilder(
				snapshotState.orElse(UserState.of(Optional.empty())));

		// Register command handler
		b.setCommandHandler(CreateUser.class, (cmd, ctx) -> {
			if (state().getUser.isPresent()) {
				ctx.invalidCommand("user " + entityId() + " Already Exists, UserId should be unique!");
				return ctx.done();
			} else {
				User user=cmd.getUser();
				UserCreated userCreated=UserCreated.of(user.getUserId(), user.getPassword());
				return ctx.thenPersist(userCreated, evt -> ctx.reply(Done.getInstance()));
			}
		});
		// Register Event Handler
		b.setEventHandler(UserCreated.class, evt -> 
			state().withUser(User.builder().userId(evt.getUserId)).password(evt.getPassword()).build());

		// Register read-only handler, a handler that doesn't result in events
		// being created
		b.setReadOnlyCommandHandler(GetUser.class, (cmd, ctx) -> {
			ctx.reply(GetUserReply.of(state().getUser()));
		});

		return b.build();
	}

}
