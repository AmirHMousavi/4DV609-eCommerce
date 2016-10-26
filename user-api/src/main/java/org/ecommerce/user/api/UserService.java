package org.ecommerce.user.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import org.pcollections.PSequence;

public interface UserService extends Service {

	/**
	 * Example: curl
	 * http://localhost:9000/api/items/5e59ff61-214c-461f-9e29-89de0cf88f90
	 */
	ServiceCall<NotUsed, User> getUser(String id, String password);

	/**
	 * Example: curl http://localhost:9000/api/items
	 */
	ServiceCall<NotUsed, PSequence<CreateUserResponse>> getAllUsers();

	/**
	 * Example: curl -v -H "Content-Type: application/json" -X POST -d '{"name":
	 * "Chair", "price": 10.50}' http://localhost:9000/api/items
	 */
	ServiceCall<CreateUserRequest, CreateUserResponse> createUser();

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
				.withCalls(Service.restCall(Method.GET, "/api/users/:id/:password", this::getUser),
						Service.restCall(Method.GET, "/api/users/list", this::getAllUsers),
						Service.restCall(Method.POST, "/api/users", this::createUser))
				.withAutoAcl(true);
	}
}