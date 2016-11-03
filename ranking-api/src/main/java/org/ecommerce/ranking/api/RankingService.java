package org.ecommerce.ranking.api;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import akka.Done;
import akka.NotUsed;

public interface RankingService extends Service {

	
	ServiceCall<NotUsed, Ranking> getRanking(String rankingId);
	

	ServiceCall<CreateRankingRequest, Done> setRanking(String rankingId);
	
	
	ServiceCall<CreateRankingRequest, CreateRankingResponse> createRanking();

	
	@Override
	default Descriptor descriptor() {
		return Service.named("RANKING-SERVICE").withCalls(
						Service.restCall(Method.GET, "/api/rating/:rankingId", this::getRanking),
						Service.restCall(Method.POST, "/api/rating/set/create", this::createRanking),
						Service.restCall(Method.POST, "/api/rating/set/:rankingId", this::setRanking))
				.withAutoAcl(true);
	}
}
