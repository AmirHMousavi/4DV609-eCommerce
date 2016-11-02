package org.ecommerce.item.api;

import java.util.Optional;

import com.lightbend.lagom.javadsl.api.deser.MessageSerializer.NegotiatedSerializer;
import com.lightbend.lagom.javadsl.api.deser.SerializationException;
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;

import akka.util.ByteString;

public class BytesSerializer implements NegotiatedSerializer<ByteString, ByteString> {
	private final String charset;

	public BytesSerializer(String charset) {
		this.charset = charset;
	}

	@Override
	public MessageProtocol protocol() {
		return new MessageProtocol(Optional.of("multipart/form-data"), Optional.of(charset), Optional.empty());
	}

	@Override
	public ByteString serialize(ByteString from) throws SerializationException {
//		if (from instanceof String)
//			return (To) ByteString.fromString((String) from, charset);
		return from;
	}
}