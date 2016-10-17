package org.ecommerce.user.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.namedCall;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

import org.pcollections.PSequence;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import akka.NotUsed;


public interface UserService extends Service {

	ServiceCall<NotUsed, User> getUser(String userId, String password);

	ServiceCall<User, NotUsed> createUser();

	ServiceCall<NotUsed, PSequence<User>> getAllUsers();

	@Override
    default Descriptor descriptor() {
		// @formatter:off
	
        return named("userservice").withCalls(
                pathCall("/api/users/:userId/:password", this::getUser),
                namedCall("/api/users", this::createUser),
                pathCall("/api/users/list", this::getAllUsers)
              ).withAutoAcl(true);
            // @formatter:on
        }
}
