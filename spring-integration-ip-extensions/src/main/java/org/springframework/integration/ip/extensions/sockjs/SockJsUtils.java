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
package org.springframework.integration.ip.extensions.sockjs;

import java.security.MessageDigest;

import org.apache.commons.codec.binary.Base64;

/**
 * @author Gary Russell
 * @since 2.2
 *
 */
public class SockJsUtils {

	public static String generateWebSocketAccept(String key) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		String toDigest = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		byte[] acceptStringBytes  = md.digest(toDigest.getBytes());
		acceptStringBytes = Base64.encodeBase64(acceptStringBytes);
		String acceptString = new String(acceptStringBytes);
		return acceptString;
	}

	public static void main(String[] args) throws Exception {
		generateWebSocketAccept("wSE2F/mejbdJ59sbarKyUA==");
	}
}
