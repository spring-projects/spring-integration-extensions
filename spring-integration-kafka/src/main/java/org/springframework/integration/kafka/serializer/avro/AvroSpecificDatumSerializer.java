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
