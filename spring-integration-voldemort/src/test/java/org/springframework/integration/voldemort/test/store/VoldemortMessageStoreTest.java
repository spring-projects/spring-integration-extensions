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

import java.io.Serializable;
import java.util.Properties;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.integration.Message;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.history.MessageHistory;
import org.springframework.integration.message.GenericMessage;

/**
 * Voldemort message store tests based on Redis module.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class VoldemortMessageStoreTest extends BaseStoreFunctionalTestCase {
	@Test
	public void testGetNonExistingMessage() {
		final Message<?> message = store.getMessage( UUID.randomUUID() );

		Assert.assertNull( message );
	}

	@Test
	public void testGetMessageCountWhenEmpty() {
		Assert.assertEquals( 0, store.getMessageCount() );
	}

	@Test
	public void testAddStringMessage() {
		final Message<String> stringMessage = new GenericMessage<String>( "Hello Voldemort" );

		final Message<String> storedMessage = store.addMessage( stringMessage );

		Assert.assertNotSame( stringMessage, storedMessage );
		Assert.assertEquals( stringMessage.getPayload(), storedMessage.getPayload() );
	}

	@Test
	public void testAddSerializableObjectMessage() {
		final Address address = new Address( "1600 Pennsylvania Av, Washington, DC" );
		final Person person = new Person( "Barack Obama", address );
		Message<Person> objectMessage = new GenericMessage<Person>( person );

		Message<Person> storedMessage = store.addMessage( objectMessage );

		Assert.assertNotSame( objectMessage, storedMessage );
		Assert.assertEquals( person.getName(), storedMessage.getPayload().getName() );
		Assert.assertEquals( person.getAddress().getAddress(), storedMessage.getPayload().getAddress().getAddress() );
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddNonSerializableObjectMessage() {
		Message<Foo> objectMessage = new GenericMessage<Foo>( new Foo() );
		store.addMessage( objectMessage );
	}

	@Test
	public void testAddAndGetStringMessage() {
		final Message<String> stringMessage = new GenericMessage<String>( "Hello Voldemort" );

		store.addMessage( stringMessage );
		final Message<String> retrievedMessage = (Message<String>) store.getMessage( stringMessage.getHeaders().getId() );

		Assert.assertNotNull( retrievedMessage );
		Assert.assertEquals( stringMessage.getPayload(), retrievedMessage.getPayload() );
	}

	@Test
	public void testAddAndRemoveStringMessage() {
		final Message<String> stringMessage = new GenericMessage<String>( "Hello Voldemort" );

		store.addMessage(stringMessage);
		Message<String> retrievedMessage = (Message<String>) store.removeMessage( stringMessage.getHeaders().getId() );

		Assert.assertNotNull( retrievedMessage );
		Assert.assertEquals( stringMessage.getPayload(), retrievedMessage.getPayload() );
		Assert.assertNull( store.getMessage( stringMessage.getHeaders().getId() ) );
	}

	@Test
	public void testMessageCount() {
		final Message<String> stringMessage1 = new GenericMessage<String>( "Hello Voldemort" );
		final Message<String> stringMessage2 = new GenericMessage<String>( "Hello World" );

		store.addMessage( stringMessage1 );
		Assert.assertEquals( 1, store.getMessageCount() );

		store.addMessage( stringMessage2 );
		Assert.assertEquals( 2, store.getMessageCount() );

		store.removeMessage( stringMessage1.getHeaders().getId() );
		Assert.assertEquals( 1, store.getMessageCount() );
	}

	@Test
	public void testWithMessageHistory() {
		Message<?> message = new GenericMessage<String>( "Hello" );
		final DirectChannel fooChannel = new DirectChannel();
		fooChannel.setBeanName( "fooChannel" );
		final DirectChannel barChannel = new DirectChannel();
		barChannel.setBeanName( "barChannel" );

		message = MessageHistory.write( message, fooChannel );
		message = MessageHistory.write( message, barChannel );
		store.addMessage( message );
		message = store.getMessage( message.getHeaders().getId() );
		MessageHistory messageHistory = MessageHistory.read( message );

		Assert.assertNotNull( messageHistory );
		Assert.assertEquals( 2, messageHistory.size() );

		Properties fooChannelHistory = messageHistory.get( 0 );

		Assert.assertEquals( "fooChannel", fooChannelHistory.get( "name" ) );
		Assert.assertEquals( "channel", fooChannelHistory.get( "type" ) );
	}

	public static class Person implements Serializable {
		private static final long serialVersionUID = 6109955909562732898L;

		private String name;
		private Address address;

		public Person(String name, Address address) {
			this.name = name;
			this.address = address;
		}

		public Address getAddress() {
			return address;
		}

		public void setAddress(Address address) {
			this.address = address;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static class Address implements Serializable {
		private static final long serialVersionUID = 2382619388682259472L;

		private String address;

		public Address(String address) {
			this.address = address;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}
	}

	public static class Foo {
	}
}
