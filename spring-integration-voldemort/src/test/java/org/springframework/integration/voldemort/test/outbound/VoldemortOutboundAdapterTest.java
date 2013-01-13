/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.voldemort.test.outbound;

import junit.framework.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessageDeliveryException;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.voldemort.support.PersistMode;
import org.springframework.integration.voldemort.support.VoldemortHeaders;
import org.springframework.integration.voldemort.test.BaseFunctionalTestCase;
import org.springframework.integration.voldemort.test.domain.Person;
import voldemort.client.StoreClient;
import voldemort.versioning.Versioned;

/**
 * Voldemort basic outbound adapter tests.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class VoldemortOutboundAdapterTest extends BaseFunctionalTestCase {
	@Test
	public void testPutObject() {
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "VoldemortOutboundAdapterTest-context.xml", getClass() );
		final StoreClient storeClient = context.getBean( "storeClient", StoreClient.class );
		final MessageChannel voldemortOutboundPutChannel = context.getBean( "voldemortOutboundPutChannel", MessageChannel.class );

		// given
		final Person lukasz = new Person( "1", "Lukasz", "Antoniak" );

		// when
		final Message<Person> message = MessageBuilder.withPayload( lukasz ).build();
		voldemortOutboundPutChannel.send( message );

		// then
		final Versioned found = storeClient.get( lukasz.getId() );
		Assert.assertEquals( lukasz, found.getValue() );

		context.close();
	}

	@Test
	public void testDeleteObject() {
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "VoldemortOutboundAdapterTest-context.xml", getClass() );
		final StoreClient storeClient = context.getBean( "storeClient", StoreClient.class );
		final MessageChannel voldemortOutboundDeleteChannel = context.getBean( "voldemortOutboundDeleteChannel", MessageChannel.class );

		// given
		final Person lukasz = new Person( "1", "Lukasz", "Antoniak" );
		storeClient.put( lukasz.getId(), lukasz );

		// when
		final Message<Person> message = MessageBuilder.withPayload( lukasz ).build();
		voldemortOutboundDeleteChannel.send( message );

		// then
		final Versioned found = storeClient.get( lukasz.getId() );
		Assert.assertNull( found );

		context.close();
	}

	@Test
	public void testOverridePersistMode() {
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "VoldemortOutboundAdapterTest-context.xml", getClass() );
		final StoreClient storeClient = context.getBean( "storeClient", StoreClient.class );
		final MessageChannel voldemortOutboundDeleteChannel = context.getBean( "voldemortOutboundDeleteChannel", MessageChannel.class );

		// given
		final Person lukasz = new Person( "1", "Lukasz", "Antoniak" );

		// when
		// Overriding output adapter's persist mode.
		final Message<Person> message = MessageBuilder.withPayload( lukasz )
				.setHeader( VoldemortHeaders.PERSIST_MODE, PersistMode.PUT ).build();
		voldemortOutboundDeleteChannel.send( message );

		// then
		final Versioned found = storeClient.get( lukasz.getId() );
		Assert.assertEquals( lukasz, found.getValue() );

		context.close();
	}

	@Test
	public void testStoppedAdapter() {
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "VoldemortOutboundAdapterTest-context.xml", getClass() );
		final MessageChannel voldemortStoppedChannel = context.getBean( "voldemortStoppedChannel", MessageChannel.class );

		// given
		final Person lukasz = new Person( "1", "Lukasz", "Antoniak" );

		// when
		final Message<Person> message = MessageBuilder.withPayload( lukasz ).build();
		try {
			voldemortStoppedChannel.send( message );
		}
		catch ( MessageDeliveryException e ) {
			return;
		}
		finally {
			context.close();
		}

		Assert.fail();
	}

	@Test
	public void testOrder() {
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "VoldemortOutboundAdapterTest-context.xml", getClass() );
		final StoreClient storeClient = context.getBean( "storeClient", StoreClient.class );
		final MessageUpdatingServiceActivator messageUpdater = context.getBean( "messageUpdater", MessageUpdatingServiceActivator.class );
		final MessageChannel voldemortOrderChannel = context.getBean( "voldemortOrderChannel", MessageChannel.class );

		// given
		final Person lukasz = new Person( "lukasz", "Lukasz", "Antoniak" );
		final Person copy = new Person( "lukasz", "Lukasz", "Antoniak" );

		// when
		final Message<Person> message = MessageBuilder.withPayload( lukasz ).build();
		voldemortOrderChannel.send( message );

		// then
		messageUpdater.updatePerson( copy );
		final Versioned found = storeClient.get( lukasz.getId() );
		Assert.assertEquals( copy, found.getValue() );

		context.close();
	}
}
