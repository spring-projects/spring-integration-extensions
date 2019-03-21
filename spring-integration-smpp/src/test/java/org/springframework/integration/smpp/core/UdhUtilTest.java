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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

/**
 * @author Johanes Soetanto
 * @since 1.0
 */
public class UdhUtilTest {

    short ref = (short)new Random().nextInt(Short.MAX_VALUE);
    byte[] udh;
    byte total = 2;
    byte seqNum = 1;

    @Before
    public void setUp() throws Exception {
        udh = new byte[6];
        udh[0] = 0x05;
        udh[1] = 0x00;
        udh[2] = 3;
        udh[3] = (byte)(ref & 0x7F);
        udh[4] = total;
        udh[5] = seqNum;
    }

    @Test
    public void testGetMessageWithUdhInBytes() throws Exception {
        String message = "This is the message";
        byte dataCoding = 0;
        String messageWithUdh = new String(udh).concat(message);
        byte[] result = UdhUtil.getMessageWithUdhInBytes(messageWithUdh, dataCoding);
        Assert.assertEquals(udh[0], result[0]);
        Assert.assertEquals(udh[1], result[1]);
        Assert.assertEquals(udh[2], result[2]);
        Assert.assertEquals(udh[3], result[3]);
        Assert.assertEquals(udh[4], result[4]);
        Assert.assertEquals(udh[5], result[5]);
        byte[] content = new byte[result.length-6];
        System.arraycopy(result, 6, content, 0, content.length);
        String decoded = new String(content, "UTF-8");
        Assert.assertEquals(message, decoded);
    }

    @Test
    public void testGetMessageWithUdhInBytes_dataCoding8() throws Exception {
        String message = "這將是一個長期的短信";
        byte dataCoding = 8;
        String messageWithUdh = new String(udh).concat(message);
        byte[] result = UdhUtil.getMessageWithUdhInBytes(messageWithUdh, dataCoding);
        Assert.assertEquals(udh[0], result[0]);
        Assert.assertEquals(udh[1], result[1]);
        Assert.assertEquals(udh[2], result[2]);
        Assert.assertEquals(udh[3], result[3]);
        Assert.assertEquals(udh[4], result[4]);
        Assert.assertEquals(udh[5], result[5]);
        byte[] content = new byte[result.length-6];
        System.arraycopy(result, 6, content, 0, content.length);
        String decoded = new String(content, "UTF-16");
        Assert.assertEquals(message, decoded);
    }
}
