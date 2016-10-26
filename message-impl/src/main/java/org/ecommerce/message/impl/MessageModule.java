package org.ecommerce.message.impl;

import org.ecommerce.message.api.MessageService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;



public class MessageModule extends AbstractModule implements ServiceGuiceSupport{
	
	@Override
    protected void configure() {
        bindServices(serviceBinding(
                MessageService.class, MessageServiceImpl.class));
    }

}
