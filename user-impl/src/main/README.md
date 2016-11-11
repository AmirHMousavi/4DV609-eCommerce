# User Service
 the descriptor of user service defines services with calls; getUser(), getAllUsers() and createUser(). these are method calls that return serviceCalls. 
### service calls
```sh
to login();
Method.GET, "/api/users/login/:userId/:password"
to getAllUsers();
Method.GET, "/api/users/list"
to createUser();
Method.POST, "/api/users"
to setRank();
Method.POST, "/api/users/setrank/:userId"
to getAverageRank();
Method.GET, "/api/users/avgrank/:userId"
```
Each serviceCall can take two parameters as request and response and an identifier to provide routing information.
The Request parameter is the type of the incoming request message, and the Response parameter is the type of the outgoing response message.
```sh
ServiceCall<NotUsed, User> getUser(String id, String password)
```
* `NotUsed` parameter: This type is used in generic type signatures wherever the actual value is of no importance. It is a combination of Scala's `Unit` and Java's `Void`, which both have different issues when used from the other language. An example use-case is the materialized value of an Akka Stream for cases where no result shall be returned from materialization.
* `User` parameter: the response of this serviceCall is User object model which contains a String as userId and a String as password. the userId and password also used as path identifiers for logining in.
* this service call contains two identifiers, userId and password
```sh
ServiceCall<NotUsed, PSequence<userId>> getAllUsers();
```
* `PSequence` the response of this service call is an immutable, persistent indexed collection of all userId's.
```sh
ServiceCall<CreateUserRequest, CreateUserResponse> createUser();
```
* `CreateUserRequest` is an immutable, nonNullByDefault object which contains UserId and password of the user that is going to be registred.
* `CreateUserResponse` is an immutable, nonNullByDefault object which contains the userId of the just registred user.
```sh
ServiceCall<BigDecimal, Done> setRank(String userId);
```
* `BigDecimal` is an Immutable, arbitrary-precision signed decimal number passed from rankingService
* `Done` is a typically used together with Future to signal completion
```sh
ServiceCall<NotUsed, Double> getAvarageRank(String userId);
```

### implementation
At system startup, Lagom registers all PersistentEntity classes in PersistentEntityRegistry. The entities are automatically distributed across the nodes in the cluster of the service. Each entity runs only at one place, and messages can be sent to the entity without requiring the sender to know the location of the entity. An entity is kept alive, holding its current state in memory, as long as it is used. When it has not been used for a while it will automatically be passivated to free up resources.
When an entity is started it replays the stored events to restore the current state. Later, PersistentEntityRef can be retrieved with PersistentEntityRegistry#refFor in order to send to a PersistentEntity.

In brief, every command to a persistentEntity is crreating an event, the event get processed and becomes available in readside. the event process also results in state change of the entity.

##### Folder Structure
```
└───org
    └───ecommerce
        └───user
            └───impl
                    AbstractUserState.java
                    UserCommand.java
                    UserEntity.java
                    UserEvent.java
                    UserEventProcessor.java
                    UserEventTag.java
                    UserModule.java
                    UserServiceImpl.java
```
##### UserServiceImpl.java
this class is providing an implementation of the service descriptor interface, implementing each call specified by that descriptor.
* `CreateUser` when we create a user, a "CreateUser" command is beeing sent. the command get processed in UserEntity class, by setting commandHandler. command process will result in UserCreated event. when the event get processed by setting eventHandler, the created entity becomes available in readside. UserEventProcessor, is our access to readside, which in UserService is a "user" table in cassandra.
* `getUser` is a read-only command and replies to the GET request by returning the getUserReply of User object type. the command is handled by setting ReadOnlyCommandHandler. if the entity exists in state, it reurns the entity and the password is checked.
* `getAllUsers` is a direct query from readside. An instance of the cassandraSession is created in the UserServiceImpl and a completionStage with a collection of userId's are returned regarding the query `selectAll("SELECT userId FROM user")`.
* `setRank` with the userId path parameter we get access to the user entity and retrive its ranking collection and push the new rank to the collection.
* `getAvarageRank` we retrive the collection and iterate over it and calculate the average of the collection as avrage rank of user.
