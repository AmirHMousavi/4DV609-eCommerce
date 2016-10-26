package org.ecommerce.ranking.impl;

import org.ecommerce.ranking.api.RankingService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;



public class RankingModule extends AbstractModule implements ServiceGuiceSupport{
	
	@Override
    protected void configure() {
        bindServices(serviceBinding(
                RankingService.class, RankingServiceImpl.class));
    }

}
