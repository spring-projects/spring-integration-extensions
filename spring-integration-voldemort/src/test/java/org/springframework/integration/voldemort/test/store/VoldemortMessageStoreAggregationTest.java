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
package org.springframework.integration.voldemort.test.store;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.voldemort.test.BaseFunctionalTestCase;

/**
 * Voldemort message store tests based on Redis module.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class VoldemortMessageStoreAggregationTest extends BaseFunctionalTestCase {
	@Override
	protected File getStoreConfiguration() {
		return new File( "src/test/resources/org/springframework/integration/voldemort/test/store/stores.xml" );
	}

	@Test
	public void testAggregatorWithShutdown() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "VoldemortMessageStoreAggregationTest-context.xml", getClass() );
		MessageChannel input = context.getBean( "inputChannel", MessageChannel.class );
		QueueChannel output = context.getBean( "outputChannel", QueueChannel.class );

		Message<?> message1 = MessageBuilder.withPayload( "1" )
											.setSequenceNumber( 1 ).setSequenceSize( 3 ).setCorrelationId( 1 ).build();
		Message<?> message2 = MessageBuilder.withPayload( "2" )
											.setSequenceNumber( 2 ).setSequenceSize( 3 ).setCorrelationId( 1 ).build();
		input.send( message1 );
		Assert.assertNull( output.receive( 1000 ) );
		input.send( message2 );
		Assert.assertNull( output.receive( 1000 ) );

		context.close();

		context = new ClassPathXmlApplicationContext( "VoldemortMessageStoreAggregationTest-context.xml", getClass() );
		input = context.getBean( "inputChannel", MessageChannel.class );
		output = context.getBean( "outputChannel", QueueChannel.class );

		Message<?> message3 = MessageBuilder.withPayload( "3" )
											.setSequenceNumber( 3 ).setSequenceSize( 3 ).setCorrelationId( 1 ).build();
		input.send( message3 );
		Assert.assertNotNull( output.receive( 1000 ) );

		context.close();
	}
}
