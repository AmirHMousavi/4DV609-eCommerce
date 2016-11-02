package org.ecommerce.item.api;

import java.util.List;

import com.lightbend.lagom.javadsl.api.deser.StrictMessageSerializer;
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;
import com.lightbend.lagom.javadsl.api.transport.NotAcceptable;
import com.lightbend.lagom.javadsl.api.transport.UnsupportedMediaType;

import akka.util.ByteString;

public class ByteStringMessage implements StrictMessageSerializer<ByteString>{
	
	final String charset = "utf-8";

	NegotiatedSerializer<ByteString, ByteString> serializer = new BytesSerializer(charset);
	NegotiatedDeserializer<ByteString, ByteString> deserializer = new BytesDeserializer(charset);
	
	@Override
	public com.lightbend.lagom.javadsl.api.deser.MessageSerializer.NegotiatedSerializer<ByteString, ByteString> serializerForRequest() {
		return serializer;
	}

	@Override
	public com.lightbend.lagom.javadsl.api.deser.MessageSerializer.NegotiatedDeserializer<ByteString, ByteString> deserializer(
			MessageProtocol protocol) throws UnsupportedMediaType {
		return deserializer;
	}

	@Override
	public com.lightbend.lagom.javadsl.api.deser.MessageSerializer.NegotiatedSerializer<ByteString, ByteString> serializerForResponse(
			List<MessageProtocol> acceptedMessageProtocols) throws NotAcceptable {
		return serializer;
	}

}
