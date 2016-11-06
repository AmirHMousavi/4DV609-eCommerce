package org.ecommerce.item.api;

import java.io.File;
import java.util.concurrent.CompletionStage;

import org.ecommerce.security.SecurityHeaderFilter;
import org.pcollections.PSequence;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;


import akka.Done;
import akka.NotUsed;
import akka.http.javadsl.model.HttpRequest;


import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.RequestHeader;

import play.libs.streams.Accumulator;
import akka.stream.javadsl.*;
import akka.util.ByteString;




public interface ItemService extends Service {

    /**
     * Example: curl http://localhost:9000/api/items/5e59ff61-214c-461f-9e29-89de0cf88f90
     */
    ServiceCall<NotUsed, Item> getItem(String id);
    
    /**
     * Example: curl http://localhost:9000/api/items
     */
    ServiceCall<NotUsed, PSequence<Item>> getAllItems();
    ServiceCall<NotUsed, PSequence<Item>> getAllItemsBy(String userId);

    /**
     * Example:
     * curl -v -H "Content-Type: application/json" -X POST -d
     * '{"name": "Chair", "price": 10.50}' http://localhost:9000/api/items
     */
    ServiceCall<CreateItemRequest, CreateItemResponse> createItem();
    
    ServiceCall<ByteString, String> createImage(String id);
    ServiceCall<NotUsed, ByteString> getImage(String id);
    
    ServiceCall<Source<ByteString,?>, String> uploadImage(String id);
    
//    ServiceCall<String, Accumulator<ByteString, Done>> createImage();
    //ServiceCall<String, String> createImage();
    
//    PlayServiceCall createImage();
    
    
    
   

    /**
     * Other useful URLs:
     *
     * http://localhost:8000/services                                  - Lists the available services
     * http://localhost:{SERVICE_PORT}/_status/circuit-breaker/current - Snapshot of current circuit breaker status
     * http://localhost:{SERVICE_PORT}/_status/circuit-breaker/stream  - Stream of circuit breaker status
     */

    @Override
    default Descriptor descriptor() {
    	
        return Service.named("itemservice").withCalls(
                Service.restCall(Method.GET,  "/api/items/:id", this::getItem),
                Service.restCall(Method.GET,  "/api/items", this::getAllItems),
                Service.restCall(Method.GET,  "/api/items/all/:id", this::getAllItemsBy),
                Service.restCall(Method.POST, "/api/items", this::createItem),
                Service.restCall(Method.POST, "/api/items/upload/:id", this::uploadImage),
                Service.restCall(Method.POST, "/api/items/image/:id", this::createImage)
                .withRequestSerializer(
                		new ByteStringMessage()),
                Service.restCall(Method.GET, "/api/items/image/:id", this::getImage)
                .withResponseSerializer(
                		new ByteStringMessage())
        ).withAutoAcl(true).withHeaderFilter(SecurityHeaderFilter.INSTANCE);
//                .withSerializerFactory(new MultipartFormDataSerializerFactory());
    }
}
