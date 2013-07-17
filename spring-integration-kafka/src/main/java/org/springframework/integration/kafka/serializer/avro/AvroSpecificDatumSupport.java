package org.springframework.integration.kafka.serializer.avro;

import org.apache.avro.Schema;

/**
 * @author Soby Chacko
 * @since 0.5
 */
public abstract class AvroSpecificDatumSupport extends AvroDatumSupport {
	private final Schema schema;

	protected AvroSpecificDatumSupport(final Schema schema) {
		super();
		this.schema = schema;
	}

	public Schema getSchema() {
		return schema;
	}
}
