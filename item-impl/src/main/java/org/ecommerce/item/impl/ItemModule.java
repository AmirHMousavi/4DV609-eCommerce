package org.ecommerce.item.impl;

import org.ecommerce.item.api.ItemService;
import org.ecommerce.message.api.MessageService;


import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class ItemModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindServices(serviceBinding(
                ItemService.class, ItemServiceImpl.class));
        bindClient(MessageService.class);

    }
}
