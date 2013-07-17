package org.springframework.integration.kafka.serializer;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.integration.kafka.serializer.avro.AvroSpecificDatumBackedKafkaDecoder;
import org.springframework.integration.kafka.serializer.avro.AvroSpecificDatumBackedKafkaEncoder;
import org.springframework.integration.kafka.test.utils.User;

/**
 * @author Soby Chacko
 * @since 0.5
 */
public class AvroSpecificDatumBackedKafkaSerializerTest {

	@Test
	@SuppressWarnings("unchecked")
	public void testEncodeDecodeFromSpecificDatumSchema() {
		final AvroSpecificDatumBackedKafkaEncoder avroBackedKafkaEncoder = new AvroSpecificDatumBackedKafkaEncoder(User.class);

		final User user = new User("First", "Last");

		final byte[] data = avroBackedKafkaEncoder.toBytes(user);

		final AvroSpecificDatumBackedKafkaDecoder avroSpecificDatumBackedKafkaDecoder = new AvroSpecificDatumBackedKafkaDecoder(User.class);
		final User decodedUser = (User) avroSpecificDatumBackedKafkaDecoder.fromBytes(data);

		Assert.assertEquals(user.getFirstName(), decodedUser.getFirstName().toString());
		Assert.assertEquals(user.getLastName(), decodedUser.getLastName().toString());
	}
}
