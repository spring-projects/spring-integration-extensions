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
package org.springframework.integration.pushnotify.gcm;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.integration.pushnotify.AbstractPushNotifyService;
import org.springframework.integration.pushnotify.PushNotifyService;
import org.springframework.integration.pushnotify.PushResponse;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The implementation of the push messaging for Android hand held devices
 * that used Google's Cloud Messaging (GCM) to push messages.
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class GCMPushNotifyServiceImpl extends AbstractPushNotifyService
			implements PushNotifyService {

	private static final Log logger = LogFactory.getLog(GCMPushNotifyServiceImpl.class);

	/**
	 * The parameter used to carry the registration id of the device to which the notification
	 * is to be sent
	 */
	public static final String REGISTRATION_ID = "registration_id";

	/**
	 * The parameter used to carry the registration ids of the device to which the notification
	 * is to be sent
	 */
	public static final String REGISTRATION_IDS = "registration_ids";

	/**
	 * The collapse key parameter of the request
	 */
	public static final String COLLAPSE_KEY = "collapse_key";

	/**
	 * The prefix to the parameter for sending the data in case the request
	 * is sent out as plain text
	 */
	public static final String DATA_KEY_PREFIX = "data.";

	/**
	 * The key used in the JSON request to send the data to the GCM service
	 */
	public static final String DATA_KEY = "data";

	/**
	 * The key used to specify if the messages should be held and not pushed if
	 * the device is idle.
	 */
	public static final String DELAY_WHILE_IDLE = "delay_while_idle";

	/**
	 * The parameter that gives the time the message will live and will not be discarded
	 * before it is delivered to the device.
	 */
	public static final String TIME_TO_LIVE = "time_to_live";

	/**
	 * The key in the JSON response that indicates that an error has occurred while sending the message
	 */
	public static final String ERROR = "error";

	/**
	 * The key in the JSON response giving the message id of the successfully sent message
	 */
	public static final String MESSAGE_ID = "message_id";

	/**
	 *The key in the JSON response giving the canonnical id of the device to which the message was posted
	 *may be present only if the message_id attribute is present
	 */
	public static final String CANONICAL_MESSAGES = "canonical_ids";

	/**
	 * The key giving the number of failed messages in the request
	 */
	public static final String FAILURE_MESSAGES = "failure";

	/**
	 * The key giving the number of successful messages in the request
	 */
	public static final String SUCCESS_MESSAGES = "success";

	/**
	 * The key giving a unique id to the multi cast request, this is returned by the GCM server
	 */
	public static final String MULTICAST_ID = "multicast_id";


	/**
	 * The key present in the pain text response indicating the request is successful, the value
	 * of this key is the message id
	 */
	public static final String RESPONSE_TOKEN_ID = "id";


	/**
	 * The content type if the request is of type JSON.
	 */
	public static final String JSON_TYPE = "application/json";

	/**
	 * The content type if the request is of type plain text.
	 */
	public static final String PLAIN_TEXT_TYPE = "application/x-www-form-urlencoded;charset=UTF-8";

	/**
	 * The Web service for the GCM services
	 */
	public static final String GCM_SERVICE_URL = "https://android.googleapis.com/gcm/send";


	private static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * The Sender id that would be used while making request to the GCM services
	 */
	private final String senderId;



	public GCMPushNotifyServiceImpl(String senderId) {
		Assert.notNull(senderId, "senderId is 'null'");
		this.senderId = senderId;
	}


	/**
	 * Gets the content type of the content to be posted to the servers based in whether the type is
	 * JSON or plain text, the value is application/json or application/x-www-form-urlencoded;charset=UTF-8
	 * for JSON and plain text content respectively
	 *
	 * @return
	 */
	private String getContentType(boolean sendAsJson) {
		return sendAsJson? JSON_TYPE : PLAIN_TEXT_TYPE;
	}


	/* (non-Javadoc)
	 * @see org.springframework.integration.pushnotify.AbstractPushNotifyService#doPush(java.util.Map, java.lang.String[])
	 */
	@Override
	protected PushResponse doPush(Map<String, Object> message, Map<String, String> attributes, String... receiverIds) {

		Map<String, String> copy = null;
		if(attributes != null) {
			copy = new HashMap<String, String>(attributes);
		}

		try {
			if(receiverIds.length > 1) {
				return sendAsJson(message, copy, receiverIds);
			}
			else {
				return sendAsPlainText(message, copy, receiverIds[0]);
			}
		} catch (IOException e) {
			// TODO handle and throw a GCM Messaging exception
			return null;
		}
	}

	/**
	 * The private method used to request to the GCM using plain text request type
	 *
	 * @param message
	 * @param attributes
	 * @param receiverId
	 */
	private PushResponse sendAsPlainText(Map<String, Object> message, Map<String, String> attributes, String receiverId)
				throws IOException{

		String requestString = getPlainTextMessageBody(message, attributes, receiverId);
		if(logger.isDebugEnabled()) {
			logger.debug("Request String is " + requestString);
		}
		String contentType = getContentType(false);
		return postMessage(contentType, requestString, receiverId);
	}

	/**
	 * Posts the given message to to the GCM service
	 *
	 * @param contentType
	 * @param requestMessage
	 * @param receiverIds
	 *
	 * @return
	 */
	private GCMPushResponse postMessage(String contentType, String requestMessage, String... receiverIds) throws IOException {
		URL url = new URL(GCM_SERVICE_URL);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setDoOutput(true);
		byte[] bytes = requestMessage.getBytes();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", contentType);
		connection.setRequestProperty("Authorization", "key=" + senderId);
		connection.setFixedLengthStreamingMode(bytes.length);
		OutputStream out = connection.getOutputStream();
		out.write(bytes);
		out.flush();
		//Now Get the response
		if(JSON_TYPE.equals(contentType)) {
			return handleJSONResponse(connection, receiverIds);
		}
		else {
			return handlePlainTextResponse(connection, receiverIds);
		}

	}

	/**
	 * Parses the response from the GCM server for a request made with JSON message
	 * and constructs the instance of {@link GCMPushResponse}
	 *
	 * @param connection
	 * @param receiverIds
	 *
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private GCMPushResponse handleJSONResponse(HttpURLConnection connection, String... receiverIds)
			throws IOException {
		final int respCode = connection.getResponseCode();
		final Long multicastId;
		final Integer successfulMessages;
		final Integer failedMessages;
		final Integer canonicalMessages;
		final Map<String, String> messageIds;
		final Map<String, String> canonicalIds;
		final Map<String, String> errorCodes;
		if(respCode == 200) {	//successful
			messageIds = new HashMap<String, String>();
			canonicalIds = new HashMap<String, String>();
			errorCodes = new HashMap<String, String>();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			String line = reader.readLine();
			if(logger.isDebugEnabled()) {
				logger.debug("JSON Response from the GCM server is " + line);
			}
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> responseMap = mapper.readValue(new ByteArrayInputStream(line.getBytes()), Map.class);

			multicastId = (Long)responseMap.get(MULTICAST_ID);
			successfulMessages = (Integer)responseMap.get(SUCCESS_MESSAGES);
			failedMessages = (Integer)responseMap.get(FAILURE_MESSAGES);
			canonicalMessages = (Integer)responseMap.get(CANONICAL_MESSAGES);


			List<Map<String, String>> results  = (List<Map<String,String>>)responseMap.get("results");
			int index = 0;
			for(Map<String, String> result:results) {
				String currentRecId = receiverIds[index++];
				String messageId = result.get(MESSAGE_ID);
				if(StringUtils.hasText(messageId)) {
					messageIds.put(currentRecId, messageId);
					String canonicalId = result.get(REGISTRATION_ID);
					if(StringUtils.hasText(canonicalId)) {
						canonicalIds.put(currentRecId, canonicalId);
					}
				}
				else {
					//it has to be error
					errorCodes.put(currentRecId, result.get(ERROR));
				}
			}
		}
		else if(respCode == 503) {
			return null;
		}
		else {
			multicastId = 0L;
			successfulMessages = 0;
			failedMessages = 0;
			canonicalMessages = 0;
			messageIds = Collections.EMPTY_MAP;
			canonicalIds = Collections.EMPTY_MAP;
			errorCodes = Collections.EMPTY_MAP;
		}

		return new GCMPushResponse() {

			@Override
			public boolean isMulticast() {
				return true;
			}

			@Override
			public int getSuccessfulMessages() {
				return successfulMessages;
			}

			@Override
			public Map<String, String> getSentMessageIds() {
				return messageIds;
			}

			@Override
			public int getResponseCode() {
				return respCode;
			}

			@Override
			public int getNumberOfCanonicalIds() {
				return canonicalMessages;
			}

			@Override
			public int getFailedMessages() {
				return failedMessages;
			}

			@Override
			public Map<String, String> getErrorCodes() {
				return errorCodes;
			}

			@Override
			public Map<String, String> getCanonicalIds() {
				return canonicalIds;
			}

			@Override
			public long getMulticastId() {
				return multicastId;
			}
		};

	}


	/**
	 * Parses the response from the GCM server and constructs the instance of {@link GCMPushResponse}
	 *
	 * @param connection
	 * @param receiverIds
	 *
	 * @return
	 * @throws IOException
	 */
	private GCMPushResponse handlePlainTextResponse(HttpURLConnection connection, final String... receiverIds)
			throws IOException {
		final int respCode = connection.getResponseCode();
		final String errorCode;
		final String sentMessageId;
		final String canonicalId;
		if(respCode == 200) {	//successful
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			String line = reader.readLine();
			if(logger.isDebugEnabled()) {
				logger.debug("First line of response is " + line);
			}
			//Should be for the message id or Error
			String[] splits = line.split("=");
			String token = splits[0];
			if(RESPONSE_TOKEN_ID.equals(token)) {
				sentMessageId = splits[1];
				errorCode = null;
			}
			else {
				sentMessageId = null;
				errorCode = splits[1];
			}

			//check for canonical id
			line = reader.readLine();
			if(logger.isDebugEnabled()) {
				logger.debug("Second line of response is " + line);
			}
			if(line != null) {
				//canonical id present
				splits = line.split("=");
				canonicalId = splits[1];
			}
			else {
				canonicalId = null;
			}
		}
		else if(respCode == 503) {
			//for temp unavailable,
			//TODO, handle this one to retry using expo backoff if needed
			return null;
		}
		else {
			//Error, could be 401 or 500
			errorCode = null;
			sentMessageId = null;
			canonicalId = null;
		}


		return new GCMPushResponse() {

			@Override
			public boolean isMulticast() {
				return false;
			}

			@Override
			public int getResponseCode() {
				return respCode;
			}


			@Override
			@SuppressWarnings("unchecked")
			public Map<String, String> getSentMessageIds() {
				if(sentMessageId != null) {
					return Collections.singletonMap(receiverIds[0], sentMessageId);
				}
				else {
					return Collections.EMPTY_MAP;
				}

			}

			@Override
			@SuppressWarnings("unchecked")
			public Map<String, String> getErrorCodes() {
				if(errorCode != null) {
					return Collections.singletonMap(receiverIds[0], errorCode);
				}
				else {
					return Collections.EMPTY_MAP;
				}

			}

			@Override
			@SuppressWarnings("unchecked")
			public Map<String, String> getCanonicalIds() {
				if(canonicalId != null) {
					return Collections.singletonMap(receiverIds[0], canonicalId);
				}
				else {
					return Collections.EMPTY_MAP;
				}
			}

			@Override
			public int getSuccessfulMessages() {
				return sentMessageId != null? 1 : 0;
			}

			@Override
			public int getFailedMessages() {
				return errorCode != null || respCode == 400 || respCode == 401 || respCode == 500? 1 : 0;
			}

			@Override
			public int getNumberOfCanonicalIds() {
				return canonicalId != null? 1 : 0;
			}

			@Override
			public long getMulticastId() {
				return 0;
			}
		};
	}

	/**
	 * Gets the message body that will be posted to the GCM server as a plain text message
	 *
	 * @param message
	 * @param attributes
	 * @param receiverId
	 *
	 * @return
	 */
	private String getPlainTextMessageBody(Map<String, Object> message, Map<String, String> copy, String receiverId)
			throws UnsupportedEncodingException {
		Assert.notNull(receiverId);
		StringBuilder builder = new StringBuilder();
		//Add the registration ID parameter
		builder.append(REGISTRATION_ID).append("=").append(URLEncoder.encode(receiverId, DEFAULT_ENCODING));

		for(Entry<String, Object> entry:message.entrySet()) {
			builder.append("&")
			.append(DATA_KEY_PREFIX)
			.append(entry.getKey())
			.append("=")
			.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
			//We using toString, not conversion service
		}

		if(copy != null && !copy.isEmpty()) {
			//Check if the collapse key is present in the message
			if(copy.containsKey(COLLAPSE_KEY)) {
				builder.append("&").append(COLLAPSE_KEY)
					.append("=")
					.append(URLEncoder.encode(copy.get(COLLAPSE_KEY), DEFAULT_ENCODING));
				copy.remove(COLLAPSE_KEY);
			}

			//Check if the delay if idle key is present
			if(copy.containsKey(DELAY_WHILE_IDLE)) {
				boolean delayWhileIdleFlag = getDelayWhileIdleFlag(copy);
				builder.append("&").append(DELAY_WHILE_IDLE)
				.append("=")
				.append(delayWhileIdleFlag);
				copy.remove(DELAY_WHILE_IDLE);
			}

			//Check if the time to live key is present
			if(copy.containsKey(TIME_TO_LIVE)) {
				long timeToLive = getTimeToLive(copy);
				builder.append("&").append(TIME_TO_LIVE)
				.append("=")
				.append(timeToLive);
				copy.remove(TIME_TO_LIVE);
			}

			if(copy.size() > 0) {
				if(logger.isWarnEnabled()) {
					String warnMessage = String.format("Attributes contain more %d elements which are not recognised as valid" +
							" attributes by GCM, are those intended to be a part of message? Attributes are" +
							" %s", copy.size(), copy);
					logger.warn(warnMessage);
				}
			}
		}

		String requestString = builder.toString();
		return requestString;
	}


	/**
	 * Gets the delay_while_idle flag's value to be sent out with the request to the GCM
	 * @param copy
	 * @return
	 */
	private boolean getDelayWhileIdleFlag(Map<String, String> copy) {
		boolean delayWhileIdleFlag = false;
		String delayWhileIdle = copy.get(DELAY_WHILE_IDLE);
		if(StringUtils.hasText(delayWhileIdle)
				&& ("1".equals(delayWhileIdle.trim())
						||
					Boolean.valueOf(delayWhileIdle)
						)) {
			delayWhileIdleFlag = true;
		}
		return delayWhileIdleFlag;
	}


	/**
	 * Gets the the time_to_live attribute to be sent in the request
	 * @param copy
	 * @return
	 */
	private long getTimeToLive(Map<String, String> copy) {
		String timeToLoveString = copy.get(TIME_TO_LIVE);
		//Will throw a NumberFormatException if the value is not numeric
		long timeToLive = Long.parseLong(timeToLoveString);
		return timeToLive;
	}

	/**
	 * Private method that constructs the JSON request message using {@link #getJsonMessageBody(Map, Map, String...)}
	 * and then posts the content to to GCM service
	 *
	 * @param message
	 * @param attributes
	 * @param receiverIds
	 * @return
	 * @throws IOException
	 */
	private PushResponse sendAsJson(Map<String, Object> message, Map<String, String> attributes,
			String... receiverIds) throws IOException {
		String requestMessage = getJsonMessageBody(message, attributes, receiverIds);
		if(logger.isDebugEnabled()) {
			logger.debug("Request message is " + requestMessage);
		}
		String contentType = getContentType(true);
		return postMessage(contentType, requestMessage, receiverIds);
	}

	/**
	 * Private helper method that constructs the JSON request message
	 *
	 * @param message
	 * @param attributes
	 * @param receiverIds
	 * @return
	 * @throws IOException
	 */
	private String getJsonMessageBody(Map<String, Object> message, Map<String, String> attributes,
			String... receiverIds) throws IOException {
		String stringMessage = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonFactory factory = new JsonFactory();
		JsonGenerator generator = factory.createJsonGenerator(os);
		//start writing the document
		generator.writeStartObject();

		//Start writing registration ids array
		generator.writeArrayFieldStart(REGISTRATION_IDS);
		for(String receiverId:receiverIds) {
			generator.writeString(receiverId);
		}
		//end registration id array
		generator.writeEndArray();

		//Start the data node
		generator.writeObjectFieldStart(DATA_KEY);
		for(Entry<String, Object> dataKey:message.entrySet()) {
			generator.writeStringField(dataKey.getKey(), dataKey.getValue().toString());
			//Again, we are using toString and not conversion service
		}
		//End for data node
		generator.writeEndObject();

		//Check the collapse key
		if(attributes.containsKey(COLLAPSE_KEY)) {
			generator.writeStringField(COLLAPSE_KEY, attributes.get(COLLAPSE_KEY));
			attributes.remove(COLLAPSE_KEY);
		}

		//Check if delay while idle attribute is present
		if(attributes.containsKey(DELAY_WHILE_IDLE)) {
			boolean delayWhileIdleFlag = getDelayWhileIdleFlag(attributes);
			generator.writeBooleanField(DELAY_WHILE_IDLE, delayWhileIdleFlag);
		}

		//Check if the time to live key is present
		if(attributes.containsKey(TIME_TO_LIVE)) {
			long timeToLive = getTimeToLive(attributes);
			generator.writeNumberField(TIME_TO_LIVE, timeToLive);
		}

		//End the document
		generator.writeEndObject();
		generator.close();
		stringMessage = new String(os.toByteArray());
		return stringMessage;
	}
}
