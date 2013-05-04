/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.kafka.serializer.avro;

import kafka.serializer.Encoder;
import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;

import java.io.IOException;

/**
 * @author Soby Chacko
 */
public class AvroBackedKafkaEncoder<T> implements Encoder<T> {

    private final Class clazz;

    public AvroBackedKafkaEncoder(final Class clazz) {
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    public byte[] toBytes(T source) {
        final Schema schema = ReflectData.get().getSchema(clazz);
        final AvroSerializer avroSerializer = new AvroSerializer();

        try {
            return avroSerializer.serialize(source, schema);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }
}
