package org.ecommerce.user.impl;

import org.ecommerce.user.api.UserService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

import org.ecommerce.user.api.CreateUserResponse;
import org.ecommerce.user.api.CreateUserRequest;
import org.ecommerce.user.api.User;

public class UserModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindServices(serviceBinding(
                UserService.class, UserServiceImpl.class));
    }
}
