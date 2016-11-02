package org.ecommerce.item.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;

import org.ecommerce.item.api.CreateItemRequest;
import org.ecommerce.item.api.CreateItemResponse;
import org.ecommerce.item.api.Item;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractItemState extends Jsonable {

    @Value.Parameter
    Optional<Item> getItem();

    @Value.Parameter
    LocalDateTime getTimestamp();
}
