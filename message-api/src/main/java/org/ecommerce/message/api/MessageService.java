package org.ecommerce.message.api;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import akka.NotUsed;

public interface MessageService extends Service {

	
	ServiceCall<NotUsed, Message> getMessage(String id);


	ServiceCall<CreateMessageRequest, CreateMessageResponse> sendMessage();

	
	@Override
	default Descriptor descriptor() {
		return Service.named("MESSAGE-SERVICE").withCalls(
				Service.restCall(Method.GET, "/api/message/:id", this::getMessage),
				Service.restCall(Method.POST, "/api/message", this::sendMessage))
				.withAutoAcl(true);
	}
}
