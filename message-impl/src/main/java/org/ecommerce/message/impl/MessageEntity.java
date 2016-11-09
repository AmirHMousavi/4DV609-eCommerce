package org.ecommerce.message.impl;

import org.ecommerce.message.api.Message;
import org.ecommerce.message.api.CreateMessageResponse;
import org.ecommerce.message.api.CreateMessageRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

public class MessageEntity extends PersistentEntity<MessageCommand, MessageEvent, MessageState> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageEntity.class);

	@Override
	public Behavior initialBehavior(Optional<MessageState> snapshotState) {
		LOGGER.info("Setting up initialBehaviour with snapshotState = {}", snapshotState);
		BehaviorBuilder b = newBehaviorBuilder(
				snapshotState.orElse(MessageState.of(Optional.empty(), LocalDateTime.now())));

		// Register command handler
		b.setCommandHandler(CreateMessage.class, (cmd, ctx) ->

		{
			if (state().getMessage().isPresent()) {
				ctx.invalidCommand(String.format("Message is already created/sent", entityId()));
				return ctx.done();
			} else {

				CreateMessageRequest req = cmd.getCreateMessageRequest();

				Message message = Message.of(UUID.fromString(entityId()), req.getUserId(), req.getItemId(),
						req.getIsSold(), req.getMessage(), req.getTimestamp());
				// req.getSellerId(),);
				final MessageCreated messageCreated = MessageCreated.builder().message(message).build();
				LOGGER.info("Processed CreateMessage command into MessageCreated event {}", messageCreated);
				return ctx.thenPersist(messageCreated,
						evt -> ctx.reply(CreateMessageResponse.of(messageCreated.getMessage().getMessageId())));
			}
		});

		// Register event handler
		// Register event handler
		b.setEventHandler(MessageCreated.class, evt -> {
			LOGGER.info("Processed MessageCreated event, updated message state");
			return state().withMessage(evt.getMessage()).withTimestamp(LocalDateTime.now());
		});

		// Register read-only handler eg a handler that doesn't result in events
		// being created
		// Register read-only handler eg a handler that doesn't result in events
		// being created
		b.setReadOnlyCommandHandler(GetMessage.class, (cmd, ctx) -> {
			LOGGER.info("Processed GetMessage command, returned message");
			ctx.reply(GetMessageReply.of(state().getMessage()));

		});
		
		 b.setCommandHandler(SetMessageSold.class, (cmd, ctx) -> {
	        	String req = cmd.getIsSold();
	        	Message message = state().getMessage().get().withIsSold(req);
	        	final MessageSold messageSold = MessageSold.builder().message(message).build();
	        	LOGGER.info("Processed SetMessageSold command into MessageSold event {}", messageSold);
	            return ctx.thenPersist(messageSold, evt ->
	                    ctx.reply("sold"));
	        	
	        });
	        
	     // Register event handler for item sold
	        b.setEventHandler(MessageSold.class, evt -> {
	                    LOGGER.info("Processed MessageSold event, updated message state");                    
	                    return state().withMessage(evt.getMessage())
	                            .withTimestamp(LocalDateTime.now());
	                }
	        );
	
		return b.build();
	}
}
