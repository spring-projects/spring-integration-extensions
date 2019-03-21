/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.smpp.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * This is specification for data coding based on SMPP API and Java charset.
 *
 * @author Johanes Soetanto
 * @since 1.0
 */
public class DataCodingSpecification {

    private static final Logger log = LoggerFactory.getLogger(DataCodingSpecification.class);
    public static final String US_ASCII = "US-ASCII";
    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String ISO_8859_5 = "ISO-8859-5";
    public static final String ISO_8859_8 = "ISO-8859-8";
    public static final String UTF_16 = "UTF-16";
    public static final String UTF_8 = "UTF-8";
    public static final String EUC_KR = "EUC-KR";
    public static final String EUC_JP = "EUC-JP";

    /**
     * Get maximum characters for data coding. Returns
     * <ul>
     *     <li>160 for data coding 0/1</li>
     *     <li>70 for data coding 5/8/10/13/14</li>
     *     <li>140 for others</li>
     * </ul>
     *
     * @param dataCoding data coding
     * @return maximum characters can be used for the text with specified data coding
     */
    // reference https://www.cisco.com/en/US/docs/voice_ip_comm/connection/7x/administration/guide/7xcucsag200.pdf
    public static int getMaxCharacters(byte dataCoding) {
        switch (dataCoding) {
            case 0:case 1: return 160; // these are 7bit, return full length
            // JP and KR are suppose to use multi-byte character. This is probably needed to be tested once we get more
            // people using those charset so they can maximize their allowed number of characters in single message.
            // For now assume it is the same one as double byte characters
            case 5:case 10:case 13: // just to be safe for japanese
            case 8:
            case 14: // just to be safe for korean
                return 70;
            default: return 140;
        }
    }

    /**
     * Get charset name based on data coding. Returns:
     * <ul>
     *     <li>US-ASCII for data coding 1</li>
     *     <li>ISO-8859-1 for data coding 3</li>
     *     <li>ISO-8859-5 for data coding 6</li>
     *     <li>ISO-8859-8 for data coding 7</li>
     *     <li>UTF-16 for data coding 8</li>
     *     <li>EUC-KR for data coding 14</li>
     *     <li>EUC-JP for data coding 5/10/13</li>
     *     <li>UTF-8 for others</li>
     * </ul>
     *
     * @param dataCoding data coding
     * @return charset name related to the data coding
     */
    public static String getCharsetName(byte dataCoding) {
        switch (dataCoding) {
            case 1: return US_ASCII;
            case 3: return ISO_8859_1;
            case 6: return ISO_8859_5;
            case 7: return ISO_8859_8;
            case 8: return UTF_16;
            case 14: return EUC_KR;
            case 5: case 10: case 13: return EUC_JP;
            case 2: case 4: // since both 2 and 4 is unspecified binary, use UTF-8 encoding
            case 0: // since dataCoding 0 is gsm 7bit, it is quite safe to use UTF-8
            default: return UTF_8;
        }
    }

    /**
     * Get message in bytes. This will use {@link #getCharsetName(byte)} to get the message in bytes.
     * @param message short message
     * @param dataCoding data coding
     * @return message in bytes based on the data coding
     */
    public static byte[] getMessageInBytes(String message, byte dataCoding) {
        final String charsetName = getCharsetName(dataCoding);
        if (!charsetName.equals(UTF_8)) {
            try {
                return message.getBytes(charsetName);
            }
            catch (UnsupportedEncodingException e) {
                log.warn("Fail to encode message using charset '{}'", charsetName);
            }
        }
        return message.getBytes();
    }
}
