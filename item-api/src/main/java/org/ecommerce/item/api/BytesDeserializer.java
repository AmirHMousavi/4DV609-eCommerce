package org.ecommerce.item.api;

import com.lightbend.lagom.javadsl.api.deser.DeserializationException;
import com.lightbend.lagom.javadsl.api.deser.MessageSerializer.NegotiatedDeserializer;

import akka.http.javadsl.model.FormData;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;

public class BytesDeserializer implements NegotiatedDeserializer<ByteString, ByteString> {

	private final String charset;

	public BytesDeserializer(String charset) {
		this.charset = charset;
	}

	@Override
	public ByteString deserialize(ByteString bytes) throws DeserializationException {
		return bytes;
//		return ((To) bytes.decodeString(charset));
	}
}
