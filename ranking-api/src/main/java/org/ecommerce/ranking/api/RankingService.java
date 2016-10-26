package org.ecommerce.ranking.api;

import org.ecommerce.ranking.api.Ranking;
import org.ecommerce.ranking.api.CreateRankingRequest;
import org.ecommerce.ranking.api.CreateRankingResponse;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import akka.NotUsed;

public interface RankingService extends Service {

	
	ServiceCall<NotUsed, Ranking> getRanking(String id);


	ServiceCall<CreateRankingRequest, CreateRankingResponse> setRanking();

	
	@Override
	default Descriptor descriptor() {
		return Service.named("RANKING-SERVICE").withCalls(
				Service.restCall(Method.GET, "/api/rating/:id", this::getRanking),
				Service.restCall(Method.POST, "/api/rating", this::setRanking))
				.withAutoAcl(true);
	}
}
