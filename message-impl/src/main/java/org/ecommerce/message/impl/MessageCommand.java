package org.ecommerce.message.impl;

import org.ecommerce.message.api.Message;
import org.ecommerce.message.api.CreateMessageRequest;
import org.ecommerce.message.api.CreateMessageResponse;
import java.util.Optional;
import org.immutables.value.Value;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;

import akka.Done;

public interface MessageCommand extends Jsonable {

	@Value.Immutable
	@ImmutableStyle
	@JsonDeserialize
	public interface AbstractCreateMessage
			extends MessageCommand, CompressedJsonable, PersistentEntity.ReplyType<CreateMessageResponse> {

		@Value.Parameter
	    CreateMessageRequest getCreateMessageRequest();
	}
	
	@Value.Immutable
	@ImmutableStyle
	@JsonDeserialize
	public interface AbstractSetMessageSold
			extends MessageCommand, CompressedJsonable, PersistentEntity.ReplyType<String> {

		@Value.Parameter
	    String getIsSold();
	}
	

	@Value.Immutable(singleton = true)
	@ImmutableStyle
	@JsonDeserialize
	public interface AbstractGetMessage
			extends MessageCommand, CompressedJsonable, PersistentEntity.ReplyType<GetMessageReply> {

	}

	@Value.Immutable
	@ImmutableStyle
	@JsonDeserialize
	public interface AbstractGetMessageReply extends Jsonable {

		@Value.Parameter
		Optional<Message> getMessage();
	}
}