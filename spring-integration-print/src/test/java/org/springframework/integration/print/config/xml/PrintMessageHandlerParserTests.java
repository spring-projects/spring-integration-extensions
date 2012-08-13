/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.print.config.xml;

import static org.junit.Assert.assertEquals;

import javax.print.DocFlavor;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Sides;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 *
 * @author Gunnar Hillert
 * @since 1.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class PrintMessageHandlerParserTests {

	@Autowired
	private MessageHandler messageHandler;

	@Test
	public void testJpaMessageHandlerParser() throws Exception {


		final DocFlavor docFlavor = TestUtils.getPropertyValue(messageHandler, "docFlavor", DocFlavor.class);
		assertEquals(DocFlavor.STRING.TEXT_PLAIN, docFlavor);

		final Copies copies = TestUtils.getPropertyValue(messageHandler, "copies", Copies.class);
		assertEquals(14, copies.getValue());

		final Sides sides = TestUtils.getPropertyValue(messageHandler, "sides", Sides.class);
		assertEquals(Sides.DUPLEX, sides);

	}

}
