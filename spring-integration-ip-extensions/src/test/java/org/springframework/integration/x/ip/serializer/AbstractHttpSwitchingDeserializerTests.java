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
package org.springframework.integration.x.ip.serializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.springframework.integration.x.ip.serializer.AbstractHttpSwitchingDeserializer.BasicState;

/**
 * @author Gary Russell
 * @since 3.0
 *
 */
public class AbstractHttpSwitchingDeserializerTests {

	private final AbstractHttpSwitchingDeserializer deserializer = new AbstractHttpSwitchingDeserializer() {
		@Override
		public DataFrame deserialize(InputStream inputStream) throws IOException {
			return checkStreaming(inputStream).get(0);
		}
	};

	@Test
	public void testPathNoQuery() throws Exception {

		String simplePath = "GET /foo HTTP/1.1\r\n\r\n";
		InputStream stream = new ByteArrayInputStream(simplePath.getBytes());

		DataFrame frame = deserializer.deserialize(stream);
		assertEquals(DataFrame.TYPE_HEADERS, frame.getType());

		BasicState state = deserializer.getState(stream);
		assertNotNull(state);
		assertEquals("/foo", state.getPath());
		assertNull(state.getQueryString());
	}

	@Test
	public void testPathAndQuery() throws Exception {

		String simplePath = "GET /foo?bar HTTP/1.1\r\n\r\n";
		InputStream stream = new ByteArrayInputStream(simplePath.getBytes());

		DataFrame frame = deserializer.deserialize(stream);
		assertEquals(DataFrame.TYPE_HEADERS, frame.getType());

		BasicState state = deserializer.getState(stream);
		assertNotNull(state);
		assertEquals("/foo", state.getPath());
		assertEquals("bar", state.getQueryString());
	}

	@Test
	public void testPathEmptyQuery() throws Exception {

		String simplePath = "GET /foo? HTTP/1.1\r\n\r\n";
		InputStream stream = new ByteArrayInputStream(simplePath.getBytes());

		DataFrame frame = deserializer.deserialize(stream);
		assertEquals(DataFrame.TYPE_HEADERS, frame.getType());

		BasicState state = deserializer.getState(stream);
		assertNotNull(state);
		assertEquals("/foo", state.getPath());
		assertEquals("", state.getQueryString());
	}

}
