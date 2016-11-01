package org.ecommerce.item.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import org.pcollections.PSequence;


public interface ItemService extends Service {
	
    ServiceCall<NotUsed, Item> getItem(String itemId);

    ServiceCall<NotUsed, PSequence<Item>> getAllItems();

    ServiceCall<CreateItemRequest, CreateItemResponse> createItem();

    @Override
    default Descriptor descriptor() {
        return Service.named("itemservice").withCalls(
                Service.restCall(Method.GET,  "/api/items/:itemId", this::getItem),
                Service.restCall(Method.GET,  "/api/items", this::getAllItems),
                Service.restCall(Method.POST, "/api/items", this::createItem)
        ).withAutoAcl(true);
    }

}
