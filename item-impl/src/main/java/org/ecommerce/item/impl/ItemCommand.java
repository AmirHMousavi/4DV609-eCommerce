package org.ecommerce.item.impl;

import com.lightbend.lagom.serialization.Jsonable;
import org.ecommerce.item.api.CreateItemRequest;
import org.ecommerce.item.api.CreateItemResponse;
import org.ecommerce.item.api.Item;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import org.immutables.value.Value;

import java.util.Optional;

public interface ItemCommand extends Jsonable {
	
    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    public interface AbstractCreateItem extends ItemCommand, CompressedJsonable, PersistentEntity.ReplyType<CreateItemResponse> {

        @Value.Parameter
        CreateItemRequest getCreateItemRequest();
    }
    
    @Value.Immutable(singleton = true)
    @ImmutableStyle
    @JsonDeserialize
    public interface AbstractGetItem extends ItemCommand, CompressedJsonable, PersistentEntity.ReplyType<GetItemReply> {

    }

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    public interface AbstractGetItemReply extends Jsonable {

        @Value.Parameter
        Optional<Item> getItem();
    }

}
