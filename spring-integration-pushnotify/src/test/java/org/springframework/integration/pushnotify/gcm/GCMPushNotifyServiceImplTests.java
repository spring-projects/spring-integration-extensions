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
package org.springframework.integration.pushnotify.gcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * The test class for {@link GCMPushNotifyServiceImpl}
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class GCMPushNotifyServiceImplTests {


	private static String receiverId;
	private static String senderId;

	@BeforeClass
	public static void setup() throws IOException {
		ClassPathResource res = new ClassPathResource("services.properties");
		Properties props = new Properties();
		props.load(res.getInputStream());
		senderId = props.getProperty("senderId");
		receiverId = props.getProperty("receiverId");
	}

	/**
	 * Should get {@link IllegalArgumentException} on passing null message
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullMessage() throws IOException {
		GCMPushNotifyService service = new GCMPushNotifyServiceImpl("1");
		service.push(null, null, "1");
	}

	/**
	 * Should get {@link IllegalArgumentException} on passing null message
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullReceiverId() throws IOException  {
		GCMPushNotifyService service = new GCMPushNotifyServiceImpl("1");
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("Data", "Some Data");
		data.put("Data2", "Some Data 2");
		service.push(data, null, (String)null);
	}

	/**
	 * Doesn't use attributes, the message contents should successfully be pushed to the GCM service
	 */
	@Test
	public void withNullAttributes() throws IOException  {
		GCMPushNotifyService service = new GCMPushNotifyServiceImpl(senderId);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("Data", "Some Data");
		data.put("Data2", "Some Data 2");
		GCMPushResponse response = (GCMPushResponse)service.push(data, null, receiverId);
		assertNotNull(response);
		assertEquals(200, response.getResponseCode());
		assertNotNull(response.getSentMessageIds().get(receiverId));
		assertEquals(1, response.getSuccessfulMessages());
		assertEquals(0, response.getFailedMessages());
	}

	/**
	 * Sends with invalid sender, should get 401 error
	 */
	@Test
	public void withInvalidSenderId() throws IOException  {
		GCMPushNotifyService service = new GCMPushNotifyServiceImpl("123");
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("Data", "Some Data");
		data.put("Data2", "Some Data 2");
		GCMPushResponse response = (GCMPushResponse)service.push(data, null, receiverId);
		assertNotNull(response);
		assertEquals(401, response.getResponseCode());
		assertEquals(0, response.getSuccessfulMessages());
		assertEquals(1, response.getFailedMessages());
	}

	/**
	 * Sends request with an invalid device id
	 */
	@Test
	public void withInvalidDeviceId() throws IOException {
		GCMPushNotifyService service = new GCMPushNotifyServiceImpl(senderId);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("Data", "Some Data");
		data.put("Data2", "Some Data 2");
		GCMPushResponse response = (GCMPushResponse)service.push(data, null, "123");
		assertNotNull(response);
		assertEquals(200, response.getResponseCode());
		assertNull(response.getSentMessageIds().get("123"));
		assertEquals("InvalidRegistration", response.getErrorCodes().get("123"));
		assertEquals(0, response.getSuccessfulMessages());
		assertEquals(1, response.getFailedMessages());
	}

	/**
	 * Sends request with an invalid key, the key name begins with google.
	 */
	@Test
	public void withInvalidKey() throws IOException {
		GCMPushNotifyService service = new GCMPushNotifyServiceImpl(senderId);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("google.Data", "Some Data");
		GCMPushResponse response = (GCMPushResponse)service.push(data, null, receiverId);
		assertNotNull(response);
		assertEquals(200, response.getResponseCode());
		assertNull(response.getSentMessageIds().get(receiverId));
		assertEquals("InvalidDataKey", response.getErrorCodes().get(receiverId));
		assertEquals(0, response.getSuccessfulMessages());
		assertEquals(1, response.getFailedMessages());
	}


	/**
	 * Publishes with some additional unrecognized attribute, the message is posted successfully with
	 * a warning msg printed to console
	 */
	@Test
	public void publishWithAdditionalAttributes() throws IOException {
		GCMPushNotifyService service = new GCMPushNotifyServiceImpl(senderId);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("Data", "Some Data");
		data.put("Data2", "Some Data 2");
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("ExtraAttr", "Attr1");
		GCMPushResponse response = (GCMPushResponse)service.push(data, attributes, receiverId);
		assertNotNull(response);
		assertEquals(200, response.getResponseCode());
		assertNotNull(response.getSentMessageIds().get(receiverId));
		assertEquals(1, response.getSuccessfulMessages());
		assertEquals(0, response.getFailedMessages());
	}

	/**
	 *
	 */
	@Test
	public void publishToMultipleInvalidSenders() throws IOException {
		GCMPushNotifyService service = new GCMPushNotifyServiceImpl(senderId);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("Data", "Some Data");
		data.put("Data2", "Some Data 2");
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put(GCMPushNotifyService.TIME_TO_LIVE, "1000");
		attributes.put(GCMPushNotifyService.COLLAPSE_KEY, "Update Available");
		attributes.put(GCMPushNotifyService.DELAY_WHILE_IDLE, "1");
		GCMPushResponse response = (GCMPushResponse)service.push(data, attributes, receiverId, "123");
		assertNotNull(response);
		assertEquals(1, response.getSuccessfulMessages());
		assertEquals(1, response.getFailedMessages());
		assertNotNull(response.getSentMessageIds().get(receiverId));
		assertEquals("InvalidRegistration", response.getErrorCodes().get("123"));
	}
}
