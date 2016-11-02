package org.ecommerce.item.api;

import java.util.Optional;

import com.lightbend.lagom.javadsl.api.deser.SerializationException;
import com.lightbend.lagom.javadsl.api.deser.MessageSerializer.NegotiatedSerializer;
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;

import akka.stream.javadsl.Source;
import akka.util.ByteString;

public class StreamBytesSerializer implements NegotiatedSerializer<Source<ByteString,?>, Source<ByteString,?>> {
	private final String charset;

	public StreamBytesSerializer(String charset) {
		this.charset = charset;
	}

	@Override
	public MessageProtocol protocol() {
		return new MessageProtocol(Optional.of("multipart/form-data"), Optional.of(charset), Optional.empty());
	}

	@Override
	public Source<ByteString,?> serialize(Source<ByteString,?> bytes) throws SerializationException {
		return bytes;
	}
}
