package org.springframework.integration.kafka.serializer.avro;

import kafka.serializer.Decoder;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Soby Chacko
 * @since 0.5
 */
public class AvroSpecificDatumBackedKafkaDecoder<T> extends AvroSpecificDatumSupport implements Decoder<T> {

	private static final Log LOG = LogFactory.getLog(AvroSpecificDatumBackedKafkaDecoder.class);

	private final DatumReader<T> reader;

	public AvroSpecificDatumBackedKafkaDecoder(final Schema schema) {
		super(schema);
		this.reader = new SpecificDatumReader<T>(schema);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T fromBytes(final byte[] bytes) {
		return (T) fromBytes(bytes, reader);
	}
}
