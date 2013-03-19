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
package org.springframework.integration.smpp.core;

/**
 * @author Johanes Soetanto
 * @since 1.0
 */
public class UdhUtil {

    /**
     * Get message with UDH to byte[]. This method converts the UDH to byte[] using {@link String#getBytes()} and
     * converts the string using {@link DataCodingSpecification#getMessageInBytes(String, byte)}.
     *
     * @param s string message
     * @param dataCoding data coding
     * @return byte array result
     */
    public static byte[] getMessageWithUdhInBytes(String s, byte dataCoding) {
        final int udhLength = ((byte)s.charAt(0))+1;
        final byte[] udh = s.substring(0, udhLength).getBytes();
        final byte[] content = DataCodingSpecification.getMessageInBytes(s.substring(udhLength), dataCoding);
        final byte[] contentWithUdh = new byte[udhLength + content.length];
        System.arraycopy(udh, 0, contentWithUdh, 0, udhLength);
        System.arraycopy(content, 0, contentWithUdh, udhLength, content.length);
        return contentWithUdh;
    }
}
