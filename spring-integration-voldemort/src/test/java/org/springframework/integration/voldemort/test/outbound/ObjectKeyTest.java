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
package org.springframework.integration.voldemort.test.outbound;

import junit.framework.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.voldemort.support.VoldemortHeaders;
import org.springframework.integration.voldemort.test.BaseFunctionalTestCase;
import org.springframework.integration.voldemort.test.domain.Car;
import voldemort.client.StoreClient;
import voldemort.versioning.Versioned;

/**
 * Test key of object type.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class ObjectKeyTest extends BaseFunctionalTestCase {
	@Test
	public void testCompositeKey() {
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "ObjectKeyTest-context.xml", getClass() );
		final StoreClient storeClient = context.getBean( "objectStoreClient", StoreClient.class );
		final MessageChannel voldemortOutboundPutChannel = context.getBean( "voldemortOutboundPutChannel", MessageChannel.class );

		// given
		final Car.CarId carId = new Car.CarId( 1 );
		final Car car = new Car( carId, "Ford Mustang" );

		// when
		final Message<Car> message = MessageBuilder.withPayload( car ).setHeader( VoldemortHeaders.KEY, carId ).build();
		voldemortOutboundPutChannel.send( message );

		// then
		final Versioned found = storeClient.get( carId );
		Assert.assertEquals( car, found.getValue() );

		context.close();
	}
}
