# Security Service
 One common cross cutting concern is authentication. This package provides, authentication and authorization using javax.security.auth
>the general idea here is that; if Service A wants to call Service B, Service A has to have all authentication details handy, put them in header of the request it wants to send to Service B and then send the request. So how does Service A acquire this information? Service A can be behind an API gateway that asks the user for username and password, sends request to Auth Service (Service C) and gets back authentication details. Every time that user request comes in through API gateway and API gateway fires of additional requests for other services, this information is in header thus accessible by the service.

Therefore here when a user logs in, for every service call that the user requests, the userinfo (userId which is a unique) is included in the header of the service calls.
then the authenticated() methods returns the requested service, if user allowed and returns ForbiddenException if user is not allowed for that serviceCall. 
### service
* server security
```sh
<Request, Response> ServerServiceCall<Request, Response> authenticated(
            Function<String, ? extends ServerServiceCall<Request, Response>> serviceCall)
```
* return type 
```sh
serviceCall.apply(((UserPrincipal) principal).getUserId());
```
* principal: 
user principal represents the abstract notion of a principal, which can be used to represent any entity, such as an individual, a corporation, and a login id.
```sh
UserPrincipal implements Principal
private final String userId;
```
* getName() returns the name of the pricipal (which should be defined as "User_Id" in the header)
```sh
public String getName() {return userId;}
```
* SecurityHeaderFilter.This is used to transform transport (transform request and response) and protocol headers according to various strategies for protocol and version negotiation, as well as authentication.
```sh
SecurityHeaderFilter implements HeaderFilter
```
in order to authenticate, other services description should be with header filter
```sh
Service.named("servicename")
                .withCalls(...).withHeaderFilter(SecurityHeaderFilter.INSTANCE) 
```
In build.sbt services should depend on security service
```sh
lazy val serviceApi = project("service-api")
       .dependsOn(security)
```








