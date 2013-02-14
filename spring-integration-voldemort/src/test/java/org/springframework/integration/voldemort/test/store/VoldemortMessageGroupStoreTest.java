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

import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.integration.Message;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.history.MessageHistory;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.SimpleMessageGroup;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.voldemort.store.VoldemortMessageStore;

/**
 * Voldemort message store tests based on Redis module.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class VoldemortMessageGroupStoreTest extends BaseStoreFunctionalTestCase {
	@Test
	public void testNonExistingEmptyMessageGroup() {
		final MessageGroup messageGroup = store.getMessageGroup( 1 );

		Assert.assertNotNull( messageGroup );
		Assert.assertTrue( messageGroup instanceof SimpleMessageGroup );
		Assert.assertEquals( 0, messageGroup.size() );
	}

	@Test
	public void testUpdatedDateChangesWithEachAddedMessage() throws InterruptedException {
		MessageGroup messageGroup = store.getMessageGroup( 1 );
		Message<?> message = new GenericMessage<String>( "Hello" );
		messageGroup = store.addMessageToGroup( 1, message );
		Assert.assertEquals( 1, messageGroup.size() );

		long createdTimestamp = messageGroup.getTimestamp();
		long updatedTimestamp = messageGroup.getLastModified();
		Assert.assertEquals( createdTimestamp, updatedTimestamp );
		Thread.sleep( 1000 );

		message = new GenericMessage<String>( "Hello" );
		messageGroup = store.addMessageToGroup( 1, message );
		createdTimestamp = messageGroup.getTimestamp();
		updatedTimestamp = messageGroup.getLastModified();
		Assert.assertTrue( updatedTimestamp > createdTimestamp );

		// use another message store instance
		VoldemortMessageStore newStore = createNewStoreClient();

		messageGroup = newStore.getMessageGroup( 1 );
		Assert.assertEquals( 2, messageGroup.size() );
	}

	@Test
	public void testMessageGroupAddOperation() {
		MessageGroup messageGroup = store.getMessageGroup( 1 );
		Message<?> message = new GenericMessage<String>( "Hello" );
		messageGroup = store.addMessageToGroup( 1, message );
		Assert.assertEquals( 1, messageGroup.size() );

		// use another message store instance
		VoldemortMessageStore newStore = createNewStoreClient();

		messageGroup = newStore.getMessageGroup( 1 );
		Assert.assertEquals( 1, messageGroup.size() );
	}

	@Test
	public void testMessageGroupWithAddedMessageUUIDGroupIdAndUUIDHeader() {
		final Object id = UUID.randomUUID();
		MessageGroup messageGroup = store.getMessageGroup( id );
		final UUID uuidA = UUID.randomUUID();
		Message<?> messageA = MessageBuilder.withPayload( "A" ).setHeader( "foo", uuidA ).build();
		final UUID uuidB = UUID.randomUUID();
		Message<?> messageB = MessageBuilder.withPayload( "B" ).setHeader( "foo", uuidB ).build();
		store.addMessageToGroup( id, messageA );
		messageGroup = store.addMessageToGroup( id, messageB );
		Assert.assertEquals( 2, messageGroup.size() );
		Message<?> retrievedMessage = store.getMessage( messageA.getHeaders().getId() );
		Assert.assertNotNull( retrievedMessage );
		Assert.assertEquals( retrievedMessage.getHeaders().getId(), messageA.getHeaders().getId() );
		Object fooHeader = retrievedMessage.getHeaders().get( "foo" );
		Assert.assertTrue( fooHeader instanceof UUID );
		Assert.assertEquals( uuidA, fooHeader );
	}

	@Test
	public void testCountMessagesInGroup() {
		Message<?> messageA = new GenericMessage<String>( "A" );
		Message<?> messageB = new GenericMessage<String>( "B" );
		store.addMessageToGroup( 1, messageA );
		store.addMessageToGroup( 1, messageB );
		Assert.assertEquals( 2, store.messageGroupSize( 1 ) );
	}

	@Test
	public void testRemoveMessageGroup() {
		MessageGroup messageGroup = store.getMessageGroup( 1 );
		Message<?> message = new GenericMessage<String>( "Hello" );
		messageGroup = store.addMessageToGroup( messageGroup.getGroupId(), message );
		Assert.assertEquals( 1, messageGroup.size() );

		store.removeMessageGroup( 1 );

		MessageGroup messageGroupA = store.getMessageGroup( 1 );
		Assert.assertNotSame( messageGroup, messageGroupA );
		Assert.assertEquals( 0, messageGroupA.getMessages().size() );
		Assert.assertEquals( 0, messageGroupA.size() );

		// use another message store instance
		VoldemortMessageStore newStore = createNewStoreClient();

		messageGroup = newStore.getMessageGroup( 1 );

		Assert.assertEquals( 0, messageGroup.getMessages().size() );
		Assert.assertEquals( 0, messageGroup.size() );
	}

	@Test
	public void testCompleteMessageGroup() {
		MessageGroup messageGroup = store.getMessageGroup( 1 );
		Message<?> message = new GenericMessage<String>( "Hello" );
		messageGroup = store.addMessageToGroup( messageGroup.getGroupId(), message );
		store.completeGroup( messageGroup.getGroupId() );
		messageGroup = store.getMessageGroup( 1 );
		Assert.assertTrue( messageGroup.isComplete() );
	}

	@Test
	public void testLastReleasedSequenceNumber() {
		MessageGroup messageGroup = store.getMessageGroup( 1 );
		Message<?> message = new GenericMessage<String>( "Hello" );
		messageGroup = store.addMessageToGroup( messageGroup.getGroupId(), message );
		store.setLastReleasedSequenceNumberForGroup( messageGroup.getGroupId(), 5 );
		messageGroup = store.getMessageGroup( 1 );
		Assert.assertEquals( 5, messageGroup.getLastReleasedMessageSequenceNumber() );
	}

	@Test
	public void testRemoveMessageFromTheGroup() {
		MessageGroup messageGroup = store.getMessageGroup( 1 );
		Message<?> message = new GenericMessage<String>( "2" );
		store.addMessageToGroup( messageGroup.getGroupId(), new GenericMessage<String>( "1" ) );
		store.addMessageToGroup( messageGroup.getGroupId(), message );
		messageGroup = store.addMessageToGroup( messageGroup.getGroupId(), new GenericMessage<String>( "3" ) );
		Assert.assertEquals( 3, messageGroup.size() );

		messageGroup = store.removeMessageFromGroup( 1, message );
		Assert.assertEquals( 2, messageGroup.size() );

		// use another message store instance
		VoldemortMessageStore newStore = createNewStoreClient();

		messageGroup = newStore.getMessageGroup( 1 );
		Assert.assertEquals( 2, messageGroup.size() );
	}

	@Test
	public void testWithMessageHistory() {
		MessageGroup messageGroup = store.getMessageGroup( 1 );
		Message<?> message = new GenericMessage<String>( "Hello" );
		DirectChannel fooChannel = new DirectChannel();
		fooChannel.setBeanName( "fooChannel" );
		DirectChannel barChannel = new DirectChannel();
		barChannel.setBeanName( "barChannel" );

		message = MessageHistory.write( message, fooChannel );
		message = MessageHistory.write( message, barChannel );
		store.addMessageToGroup( 1, message );
		message = store.getMessageGroup( 1 ).getMessages().iterator().next();
		MessageHistory messageHistory = MessageHistory.read( message );

		Assert.assertNotNull( messageHistory );
		Assert.assertEquals( 2, messageHistory.size() );

		Properties fooChannelHistory = messageHistory.get( 0 );

		Assert.assertEquals( "fooChannel", fooChannelHistory.get( "name" ) );
		Assert.assertEquals( "channel", fooChannelHistory.get( "type" ) );
	}

	@Test
	public void testRemoveNonExistingMessageFromGroup() {
		MessageGroup messageGroup = store.getMessageGroup( 1 );
		store.addMessageToGroup( messageGroup.getGroupId(), new GenericMessage<String>( "1" ) );
		store.removeMessageFromGroup( messageGroup.getGroupId(), new GenericMessage<String>( "2" ) );
	}

	@Test
	public void testRemoveNonExistingMessageFromNonExistingGroup() {
		store.removeMessageFromGroup( 1, new GenericMessage<String>( "2" ) );
	}

	@Test
	public void testMultipleInstancesOfGroupStore() {
		VoldemortMessageStore store1 = createNewStoreClient();
		VoldemortMessageStore store2 = createNewStoreClient();

		Message<?> message = new GenericMessage<String>( "1" );
		store1.addMessageToGroup( 1, message );
		MessageGroup messageGroup = store2.addMessageToGroup( 1, new GenericMessage<String>( "2" ) );
		Assert.assertEquals( 2, messageGroup.getMessages().size() );

		VoldemortMessageStore store3 = createNewStoreClient();
		messageGroup = store3.removeMessageFromGroup( 1, message );
		Assert.assertEquals( 1, messageGroup.getMessages().size() );
	}

	@Test
	public void testIteratorOfMessageGroups() {
		VoldemortMessageStore store1 = createNewStoreClient();
		VoldemortMessageStore store2 = createNewStoreClient();

		store1.addMessageToGroup( 1, new GenericMessage<String>( "1" ) );
		store2.addMessageToGroup( 2, new GenericMessage<String>( "2" ) );
		store1.addMessageToGroup( 3, new GenericMessage<String>( "3" ) );
		store2.addMessageToGroup( 3, new GenericMessage<String>( "3A" ) );

		Iterator<MessageGroup> messageGroups = store1.iterator();
		int counter = 0;
		while ( messageGroups.hasNext() ) {
			final MessageGroup group = messageGroups.next();
			final String groupId = (String) group.getGroupId();
			if ( "1".equals( groupId ) ) {
				Assert.assertEquals( 1, group.getMessages().size() );
			}
			else if ( "2".equals( groupId ) ) {
				Assert.assertEquals( 1, group.getMessages().size() );
			}
			else if ( "3".equals( groupId ) ) {
				Assert.assertEquals( 2, group.getMessages().size() );
			}
			++counter;
		}
		Assert.assertEquals( 3, counter );

		store2.removeMessageGroup( 3 );

		messageGroups = store1.iterator();
		counter = 0;
		while ( messageGroups.hasNext() ) {
			messageGroups.next();
			++counter;
		}
		Assert.assertEquals( 2, counter );
	}

	@Test
	public void testConcurrentModifications() throws InterruptedException {
		final VoldemortMessageStore store1 = createNewStoreClient();
		final VoldemortMessageStore store2 = createNewStoreClient();

		final ExecutorService executor = Executors.newCachedThreadPool();
		final Counter errorCounter = new Counter();
		final Random randomGenerator = new Random();
		for ( int i = 0; i < 10; ++i ) {
			executor.execute( new Runnable() {
				public void run() {
					try {
						final Message<?> message = new GenericMessage<Object>( UUID.randomUUID() );
						MessageGroup group = store1.addMessageToGroup( 1, message );

						Thread.sleep( randomGenerator.nextInt( 100 ) );

						group = store2.removeMessageFromGroup( 1, message );
					}
					catch ( Exception e ) {
						errorCounter.increment();
					}
				}
			});
		}
		executor.shutdown();
		executor.awaitTermination( 10, TimeUnit.SECONDS );

		Assert.assertEquals( 0, errorCounter.getValue() );
		Assert.assertEquals( 0, store1.getMessageCount() );
	}

	private static class Counter {
		private int value = 0;

		public synchronized int increment() {
			return ++value;
		}

		public int getValue() {
			return value;
		}
	}
}
