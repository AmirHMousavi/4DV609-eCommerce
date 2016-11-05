package org.ecommerce.security;

import java.util.Optional;
import java.util.function.Function;

import com.lightbend.lagom.javadsl.api.security.ServicePrincipal;
import com.lightbend.lagom.javadsl.api.transport.RequestHeader;

public class ClientSecurity {

	/**
	 * Authenticate a client request.
	 */
	public static final Function<RequestHeader, RequestHeader> authenticate(String userId) {
		return request -> {
			Optional<ServicePrincipal> service = request.principal().filter(p -> p instanceof ServicePrincipal)
					.map(p -> (ServicePrincipal) p);

			return request.withPrincipal(UserPrincipal.of(userId, service));
		};
	}

}
