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

/**
 * @author Gary Russell
 * @since 3.0
 *
 */
public class DataFrame {

	public static final int TYPE_INVALID = 0;

	public static final int TYPE_HEADERS = 1;

	public static final int TYPE_DATA = 4;

	public static final int TYPE_DATA_BINARY = 260;

	protected final int type;

	protected final String payload;

	protected final byte[] binary;

	public DataFrame(int type, String payload) {
		this(type, payload, null);
	}

	public DataFrame(int type, byte[] binary) {
		this(type, null, binary);
	}

	public DataFrame(int type, String payload, byte[] binary) {
		this.type = type;
		this.payload = payload;
		this.binary = binary;
	}

	public int getType() {
		return this.type;
	}

	public String getPayload() {
		return this.payload;
	}

	public byte[] getBinary() {
		return binary;
	}

}