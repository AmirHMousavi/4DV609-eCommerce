package org.ecommerce.security;

import java.security.Principal;
import java.util.function.Function;

import com.lightbend.lagom.javadsl.api.transport.Forbidden;
import com.lightbend.lagom.javadsl.server.HeaderServiceCall;
import com.lightbend.lagom.javadsl.server.ServerServiceCall;

public class ServerSecurity {
	
    public static <Request, Response> ServerServiceCall<Request, Response> authenticated(
            Function<String, ? extends ServerServiceCall<Request, Response>> serviceCall) {
        return HeaderServiceCall.compose(requestHeader -> {
            if (requestHeader.principal().isPresent()) {
                Principal principal = requestHeader.principal().get();
                if (principal instanceof UserPrincipal) {
                    return serviceCall.apply(((UserPrincipal) principal).getUserId());
                }
            }

            return req -> {
                throw new Forbidden("User not authenticated");
            };
        });
    }

}
