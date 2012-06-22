/*
 * Copyright 2002-2012 the original author or authors
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package org.springframework.integration.print.outbound;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Sides;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.Message;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.util.Assert;

/**
 * Simple adapter to support printing of payloads.
 *
 * @author Gunnar Hillert
 * @since 2.2
 */
public class PrintMessageHandler extends AbstractMessageHandler {

	private static final Log LOG = LogFactory.getLog(PrintMessageHandler.class);

	private volatile Sides sides   = Sides.ONE_SIDED;
	private volatile Copies copies = new Copies(1);

	private final DocFlavor docFlavor;

	public PrintMessageHandler(String mimeType, String className) {

		Assert.hasText(className, "'className' must be neither null nor empty.");
		Assert.hasText(mimeType, "'mimeType' must be neither null nor empty.");

		this.docFlavor = new DocFlavor(mimeType, className);

	}

	public PrintMessageHandler() {
		this.docFlavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
	}

	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {

		final PrintService printService = PrintServiceLookup.lookupDefaultPrintService();

		Assert.notNull(printService, "Did not find a 'printService'.");

		if (LOG.isInfoEnabled()) {
			LOG.info("Printing to default printer: " + printService.getName());
		}

		final Doc doc = new SimpleDoc(((String) message.getPayload()).getBytes(), docFlavor, null);

		final PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();

		attributes.add(copies);
		attributes.add(sides);

		final DocPrintJob job = printService.createPrintJob();
		job.print(doc, attributes);

	}

	public void setSides(Sides sides) {
		Assert.notNull(sides, "'sides' must not be null");
		this.sides = sides;
	}

	public void setCopies(int numberOfCopies) {
		Assert.isTrue(numberOfCopies > 0, "'copies' must be greater than 0");
		this.copies = new Copies(numberOfCopies);
	}

}
