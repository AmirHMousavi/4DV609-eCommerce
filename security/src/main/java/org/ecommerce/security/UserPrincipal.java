package org.ecommerce.security;

import java.security.Principal;
import java.util.Optional;

import javax.security.auth.Subject;

import com.lightbend.lagom.javadsl.api.security.ServicePrincipal;

public class UserPrincipal implements Principal {

    private final String userId;

    private UserPrincipal(String userId) {
        this.userId = userId;
    }

    @Override
    public String getName() {
        return userId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean implies(Subject subject) {
        return false;
    }

    public static UserPrincipal of(String userId, Optional<ServicePrincipal> service) {
        if (service.isPresent()) {
            return new UserServicePrincipal(userId, service.get());
        } else {
            return new UserPrincipal(userId);
        }
    }

    private static class UserServicePrincipal extends UserPrincipal implements ServicePrincipal {
        private final ServicePrincipal service;

        public UserServicePrincipal(String userId, ServicePrincipal service) {
            super(userId);
            this.service = service;
        }

        @Override
        public String serviceName() {
            return service.serviceName();
        }

        @Override
        public boolean authenticated() {
            return service.authenticated();
        }
    }

}
