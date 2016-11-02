package org.ecommerce.item.api;

import java.util.List;

import com.lightbend.lagom.javadsl.api.deser.StrictMessageSerializer;
import com.lightbend.lagom.javadsl.api.deser.MessageSerializer.NegotiatedDeserializer;
import com.lightbend.lagom.javadsl.api.deser.MessageSerializer.NegotiatedSerializer;
import com.lightbend.lagom.javadsl.api.deser.StreamedMessageSerializer;
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;
import com.lightbend.lagom.javadsl.api.transport.NotAcceptable;
import com.lightbend.lagom.javadsl.api.transport.UnsupportedMediaType;

import akka.stream.javadsl.Source;
import akka.util.ByteString;

public class BytesMessage implements StreamedMessageSerializer<ByteString>{
	
	final String charset = "utf-8";

	NegotiatedSerializer<Source<ByteString,?>, Source<ByteString,?>> serializer = new StreamBytesSerializer(charset);
	NegotiatedDeserializer<Source<ByteString,?>, Source<ByteString,?>> deserializer = new StreamBytesDeserializer(charset);
	
	@Override
	public com.lightbend.lagom.javadsl.api.deser.MessageSerializer.NegotiatedSerializer<Source<ByteString,?>, Source<ByteString,?>> serializerForRequest() {
		return serializer;
	}

	@Override
	public com.lightbend.lagom.javadsl.api.deser.MessageSerializer.NegotiatedDeserializer<Source<ByteString,?>, Source<ByteString,?>> deserializer(
			MessageProtocol protocol) throws UnsupportedMediaType {
		return deserializer;
	}

	@Override
	public com.lightbend.lagom.javadsl.api.deser.MessageSerializer.NegotiatedSerializer<Source<ByteString,?>, Source<ByteString,?>> serializerForResponse(
			List<MessageProtocol> acceptedMessageProtocols) throws NotAcceptable {
		return serializer;
	}

}
