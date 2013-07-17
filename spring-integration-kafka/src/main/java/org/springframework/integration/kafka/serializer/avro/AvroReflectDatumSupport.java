package org.springframework.integration.kafka.serializer.avro;

import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;

/**
 * @author Soby Chacko
 * @since 0.5
 */
public abstract class AvroReflectDatumSupport extends AvroDatumSupport {

	private final Class clazz;
	private final Schema schema;

	protected AvroReflectDatumSupport(Class clazz) {
		super();
		this.clazz = clazz;
		this.schema = ReflectData.get().getSchema(clazz);
	}

	public Class getClazz() {
		return clazz;
	}

	public Schema getSchema() {
		return schema;
	}
}
