package org.ecommerce.user.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import org.pcollections.PSequence;

public interface UserService extends Service {

	/**
	 * Example: curl http://localhost:9000/api/users/user1/1234
	 */
	ServiceCall<NotUsed, User> getUser(String id, String password);

	/**
	 * Example: curl http://localhost:9000/api/users/list
	 */
	ServiceCall<NotUsed, PSequence<CreateUserResponse>> getAllUsers();

	/**
	 * Example: curl -v -H "Content-Type: application/json" -X POST -d
	 * '{"userId": "user1", "password": "1234"}' http://localhost:9000/api/users
	 */
	ServiceCall<CreateUserRequest, CreateUserResponse> createUser();

	ServiceCall<NotUsed, Done> logOutUser(String id);

	/**
	 * Other useful URLs:
	 *
	 * http://localhost:8000/services - Lists the available services
	 * http://localhost:{SERVICE_PORT}/_status/circuit-breaker/current -
	 * Snapshot of current circuit breaker status
	 * http://localhost:{SERVICE_PORT}/_status/circuit-breaker/stream - Stream
	 * of circuit breaker status
	 */

	@Override
	default Descriptor descriptor() {
		return Service.named("userservice")
				.withCalls(Service.restCall(Method.GET, "/api/users/login/:id/:password", this::getUser),
						Service.restCall(Method.GET, "/api/users/list", this::getAllUsers),
						Service.restCall(Method.POST, "/api/users", this::createUser),
						Service.restCall(Method.GET, "/api/users/logout/:id", this::logOutUser))
				.withAutoAcl(true);
	}
}