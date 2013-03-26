package org.springframework.integration.kafka.serializer.avro;

import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Soby Chacko
 */
public class AvroSerializer<T> {

    public T deserialize(byte[] bytes, Schema schema) throws IOException {
        final Decoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
        final DatumReader<T> reader = new ReflectDatumReader<T>(schema);
        return reader.read(null, decoder);
    }

    public byte[] serialize(T input, Schema schema) throws IOException {

        final DatumWriter<T> writer = new ReflectDatumWriter<T>(schema);
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final Encoder encoder = EncoderFactory.get().binaryEncoder(stream, null);
        writer.write(input, encoder);
        encoder.flush();
        return stream.toByteArray();
    }
}
