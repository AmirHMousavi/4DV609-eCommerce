# Ranking Service
Ranking service decides how the item is sold and message is replied. This service is defined with service calls: getRanking(), setRanking(), createRanking()

#### Service calls

`getRanking()` - service call is used to return single rating only for which the service call gives the response as Ranking immutable object.

```sh
ServiceCall<NotUsed, Ranking> getRanking(String rankingId)
```
To call the service getRanking(), use the following REST call:

```sh
Service.restCall(Method.GET, "/api/rating/:rankingId", this::getRanking)
```
`setRanking()` - The call does the most of sold. This Service call has CreateRankingRequest parameter as request that contains attributes of the ranking that is going to be created and Done as response for the use case where it is boxed inside another object to signify completion but there is no actual value attached to the completion. By calling this service call we set the Rating of a particular user after an item being sold.
```sh
ServiceCall<CreateRankingRequest, Done> setRanking(String rankingId)
```
To call the service setRanking(), use the following REST call:
```sh
Service.restCall(Method.POST, "/api/rating/set/:rankingId", this::setRanking)
```


`createRanking()` - This service call takes request of CreateRankingRequest type which has messageId itemId and rating as a default value which normally is 0. Within this call we use itemId and messageId to get the item and message object from dependency injection of ItemService and MessageService. While creating ranking we set the isSold attribute in ItemService to Sold. IsSold attribute in itemId will take the rankingId of type UUID.
```sh
ServiceCall<CreateRankingRequest, CreateRankingResponse> createRanking()
```
To call the service createRanking(), use the following REST call:
```sh
Service.restCall(Method.POST, "/api/rating/set/create", this::createRanking)
```
##### Implementation
Folder Structure of Ranking-Impl
```sh

└───org
	└───ecommerce
    	└───ranking
        	└───impl
                	AbstractRankingState.java
                	RankingCommand.java
                	RankingEntity.java
                	RankingEvent.java
                	RankingEventProcessor.java
                	RankingEventTag.java
                	RankingModule.java
                	RankingServiceImpl.java

```
###### RankingServiceImpl.java

Implements each service call defined in the Ranking Service interface.
* `createRanking()` - is command set in RankingEntity. The command execution results in RankingCreated event. The event then notifies RankingEventProcessor to prepare read-side database. While creating ranking we set the isSold attribute in ItemService to Sold.
* `getRanking()` - is a read-only command and replies to the GET request by returning the getRankingReply of Ranking object. The command is handled by ReadOnlyCommandHandler.
* `setRanking()` - this call changes the rate of the item. It sends the rating to the User Service to display the rating average for specific user. This setRanking should be called only once.
