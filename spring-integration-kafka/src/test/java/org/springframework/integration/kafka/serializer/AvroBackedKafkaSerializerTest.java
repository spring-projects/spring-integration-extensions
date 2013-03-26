package org.springframework.integration.kafka.serializer;

import junit.framework.Assert;
import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;
import org.junit.Test;
import org.springframework.integration.kafka.serializer.avro.AvroBackedKafkaDecoder;
import org.springframework.integration.kafka.serializer.avro.AvroBackedKafkaEncoder;

/**
 * @author Soby Chacko
 * @since 1.0
 */
public class AvroBackedKafkaSerializerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testDecodePlainSchema() {

        String schemaDescription = " {    \n"
                       + " \"testData1\": \"FacebookUser\", \n"
                       + " \"type\": \"record\",\n" + " \"fields\": [\n"
                       + "   {\"testData1\": \"testData1\", \"type\": \"string\"},\n"
                       + "   {\"testData1\": \"testData2\", \"type\": \"int\"},\n"
                       + "   {\"testData1\": \"num_photos\", \"type\": \"int\"},\n"
                       + "   {\"testData1\": \"numGroups\", \"type\": \"int\"} ]\n" + "}";


        //Schema schema = Schema.parse(schemaDescription);
        //Schema schema = ReflectData.get().getSchema(TestObject.class);

        AvroBackedKafkaEncoder avroBackedKafkaEncoder = new AvroBackedKafkaEncoder(TestObject.class);
        TestObject testObject = new TestObject();
        testObject.setTestData1("\"Test Data1\"");
        testObject.setTestData2(1);
        final byte[] data = avroBackedKafkaEncoder.toBytes(testObject);

        AvroBackedKafkaDecoder avroBackedKafkaDecoder = new AvroBackedKafkaDecoder(TestObject.class);
        final TestObject decodedFbu = (TestObject) avroBackedKafkaDecoder.fromBytes(data);

        Assert.assertEquals(testObject.getTestData1(), decodedFbu.getTestData1());
        Assert.assertEquals(testObject.getTestData2(), decodedFbu.getTestData2());

    }

    @SuppressWarnings("unchecked")
        @Test
        public void anotherTest() {
            Schema schema = ReflectData.get().getSchema(java.lang.String.class);
            AvroBackedKafkaEncoder avroBackedKafkaEncoder = new AvroBackedKafkaEncoder(java.lang.String.class);
            String s = "Testing Avro";
            final byte[] data = avroBackedKafkaEncoder.toBytes(s);

            AvroBackedKafkaDecoder avroBackedKafkaDecoder = new AvroBackedKafkaDecoder(java.lang.String.class);
            final String decodedS = (String) avroBackedKafkaDecoder.fromBytes(data);

            Assert.assertEquals(s, decodedS);
        }
}
