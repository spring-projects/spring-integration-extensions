/* Copyright 2002-2013 the original author or authors.
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
import org.junit.Test;

import java.nio.charset.Charset;

/**
 * @author Johanes Soetanto
 * @since 1.0
 */
public class DataCodingSpecificationTest {

    @Test
    public void testGetMaxCharacters() throws Exception {
        Assert.assertEquals(160, DataCodingSpecification.getMaxCharacters((byte)0));
        Assert.assertEquals(160, DataCodingSpecification.getMaxCharacters((byte)1));
        Assert.assertEquals(140, DataCodingSpecification.getMaxCharacters((byte)2));
        Assert.assertEquals(140, DataCodingSpecification.getMaxCharacters((byte)3));
        Assert.assertEquals(140, DataCodingSpecification.getMaxCharacters((byte)4));
        Assert.assertEquals(70, DataCodingSpecification.getMaxCharacters((byte)5));
        Assert.assertEquals(140, DataCodingSpecification.getMaxCharacters((byte)6));
        Assert.assertEquals(140, DataCodingSpecification.getMaxCharacters((byte)7));
        Assert.assertEquals(70, DataCodingSpecification.getMaxCharacters((byte)8));
        Assert.assertEquals(70, DataCodingSpecification.getMaxCharacters((byte)10));
        Assert.assertEquals(70, DataCodingSpecification.getMaxCharacters((byte)13));
        Assert.assertEquals(70, DataCodingSpecification.getMaxCharacters((byte)14));
    }

    private void assertEqualsAndSupported(String expected, String charsetName) {
        Assert.assertEquals(expected, charsetName);
        Assert.assertTrue(Charset.isSupported(charsetName));
    }

    @Test
    public void testGetCharsetName() throws Exception {
        assertEqualsAndSupported("UTF-8", DataCodingSpecification.getCharsetName((byte)0));
        assertEqualsAndSupported("US-ASCII", DataCodingSpecification.getCharsetName((byte)1));
        assertEqualsAndSupported("UTF-8", DataCodingSpecification.getCharsetName((byte)2));
        assertEqualsAndSupported("ISO-8859-1", DataCodingSpecification.getCharsetName((byte)3));
        assertEqualsAndSupported("UTF-8", DataCodingSpecification.getCharsetName((byte)4));
        assertEqualsAndSupported("EUC-JP", DataCodingSpecification.getCharsetName((byte)5));
        assertEqualsAndSupported("ISO-8859-5", DataCodingSpecification.getCharsetName((byte)6));
        assertEqualsAndSupported("ISO-8859-8", DataCodingSpecification.getCharsetName((byte)7));
        assertEqualsAndSupported("UTF-16", DataCodingSpecification.getCharsetName((byte)8));
        assertEqualsAndSupported("EUC-JP", DataCodingSpecification.getCharsetName((byte)10));
        assertEqualsAndSupported("EUC-JP", DataCodingSpecification.getCharsetName((byte)13));
        assertEqualsAndSupported("EUC-KR", DataCodingSpecification.getCharsetName((byte)14));
    }
}
