package org.ecommerce.item.impl;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import org.ecommerce.item.api.CreateItemRequest;
import org.ecommerce.item.api.CreateItemResponse;
import org.ecommerce.item.api.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class ItemEntity extends PersistentEntity<ItemCommand, ItemEvent, ItemState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemEntity.class);

    @Override
    public Behavior initialBehavior(Optional<ItemState> snapshotState) {
        LOGGER.info("Setting up initialBehaviour with snapshotState = {}", snapshotState);
        BehaviorBuilder b = newBehaviorBuilder(snapshotState.orElse(
                ItemState.of(Optional.empty(), LocalDateTime.now()))
        );

        // Register command handler
        b.setCommandHandler(CreateItem.class, (cmd, ctx) -> {
            if (state().getItem().isPresent()) {
                ctx.invalidCommand(String.format("Item %s is already created", entityId()));
                return ctx.done();
            } else {
            	CreateItemRequest req = cmd.getCreateItemRequest();
            	String photoName = req.getPhoto();
                Item item = Item.of(UUID.fromString(entityId()),
                		req.getUserId(), req.getName(), req.getDescription(), req.getPhoto(),
                        req.getPrice(),"-1");
                final ItemCreated itemCreated = ItemCreated.builder().item(item).build();
                LOGGER.info("Processed CreateItem command into ItemCreated event {}", itemCreated);
                return ctx.thenPersist(itemCreated, evt ->
                        ctx.reply(CreateItemResponse.of(itemCreated.getItem().getId())));
            }
        });

        // Register event handler
        b.setEventHandler(ItemCreated.class, evt -> {
                    LOGGER.info("Processed ItemCreated event, updated item state");
                    
                    return state().withItem(evt.getItem())
                            .withTimestamp(LocalDateTime.now());
                }
        );

        // Register read-only handler eg a handler that doesn't result in events being created
        b.setReadOnlyCommandHandler(GetItem.class,
                (cmd, ctx) -> {
                    LOGGER.info("Processed GetItem command, returned item");
                    ctx.reply(GetItemReply.of(state().getItem()));
                }
        );
        
        b.setCommandHandler(SetItemSold.class, (cmd, ctx) -> {
        	String req = cmd.getIsSold();
        	Item item = state().getItem().get().withIsSold(req);
        	final ItemSold itemSold = ItemSold.builder().item(item).build();
        	LOGGER.info("Processed SetItemSold command into ItemSold event {}", itemSold);
            return ctx.thenPersist(itemSold, evt ->
                    ctx.reply("sold"));
        	
        });
        
     // Register event handler for item sold
        b.setEventHandler(ItemSold.class, evt -> {
                    LOGGER.info("Processed ItemSold event, updated item state");                    
                    return state().withItem(evt.getItem())
                            .withTimestamp(LocalDateTime.now());
                }
        );


        return b.build();
    }
}
