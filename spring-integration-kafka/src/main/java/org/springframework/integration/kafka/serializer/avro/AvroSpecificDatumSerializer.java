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

import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: chackos
 * Date: 3/24/13
 * Time: 7:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class AvroSpecificDatumSerializer<T> {

    public T deserialize(byte[] bytes, Schema schema) throws IOException {
            final Decoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
            final DatumReader<T> reader = new SpecificDatumReader<T>(schema);
            return reader.read(null, decoder);
        }

        public byte[] serialize(T input, Schema schema) throws IOException {

            final DatumWriter<T> writer = new SpecificDatumWriter<T>(schema);
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final org.apache.avro.io.Encoder encoder = EncoderFactory.get().binaryEncoder(stream, null);
            writer.write(input, encoder);
            encoder.flush();
            return stream.toByteArray();
        }
}
