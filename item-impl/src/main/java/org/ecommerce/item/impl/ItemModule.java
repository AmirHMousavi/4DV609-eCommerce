package org.ecommerce.item.impl;

import org.ecommerce.user.api.UserService;

import org.ecommerce.item.api.CreateItemRequest;
import org.ecommerce.item.api.CreateItemResponse;
import org.ecommerce.item.api.Item;
import org.ecommerce.item.api.ItemService;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class ItemModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindServices(serviceBinding(
                ItemService.class, ItemServiceImpl.class));
        bindClient(UserService.class);
    }
}
