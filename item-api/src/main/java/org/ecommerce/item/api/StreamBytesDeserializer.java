package org.ecommerce.item.api;

import com.lightbend.lagom.javadsl.api.deser.DeserializationException;
import com.lightbend.lagom.javadsl.api.deser.MessageSerializer.NegotiatedDeserializer;

import akka.stream.javadsl.Source;
import akka.util.ByteString;

public class StreamBytesDeserializer implements NegotiatedDeserializer<Source<ByteString,?>,Source<ByteString,?>> {

	private final String charset;

	public StreamBytesDeserializer(String charset) {
		this.charset = charset;
	}

	@Override
	public Source<ByteString,?> deserialize(Source<ByteString,?> bytes) throws DeserializationException {
		return bytes;
	}
}
