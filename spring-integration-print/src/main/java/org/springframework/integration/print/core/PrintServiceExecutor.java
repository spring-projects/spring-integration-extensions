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
package org.springframework.integration.print.core;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.print.support.AttributeComparator;
import org.springframework.integration.print.support.DocFlavorComparator;
import org.springframework.integration.print.support.PrintServiceComparator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Gunnar Hillert
 * @since 1.0
 *
 */
public final class PrintServiceExecutor {

	private final Log logger = LogFactory.getLog(PrintServiceExecutor.class);

	private PrintService printService;

	public PrintServiceExecutor() {
		this("");
	}

	public PrintServiceExecutor(PrintService printService) {
		this.printService = printService;
	}

	public PrintServiceExecutor(String printerName) {

		if (!StringUtils.hasText(printerName)) {
			this.printService = PrintServiceLookup.lookupDefaultPrintService();
			Assert.notNull(this.printService, "Did not find a the default print service.");
		}
		else {
			PrintService matchingPrintService = null;

			for (PrintService printService : PrintServiceExecutor.getAvailablePrinterServices()) {
				if (printerName.equalsIgnoreCase(printService.getName())) {
					matchingPrintService = printService;
					break;
				}
			}

			this.printService = matchingPrintService;

			Assert.notNull(this.printService, String.format("Did not find the" +
					"print service for printer '%s'.", printerName));
		}

		if (logger.isInfoEnabled()) {
			logger.info("Setting up print service for printer '" + this.printService.getName() + "'.");
		}
		else if (logger.isDebugEnabled()) {
			logger.debug(this.getPrinterInfo());
		}

	}

	public SortedSet<DocFlavor> getDocFlavors() {
		final SortedSet<DocFlavor> docFlavors = new TreeSet<DocFlavor>(new DocFlavorComparator());
		docFlavors.addAll(Arrays.asList(this.printService.getSupportedDocFlavors()));
		return docFlavors;
	}

	public String getPrinterInfo() {
		StringBuilder sb = new StringBuilder();

		final SortedSet<DocFlavor> docFlavors = this.getDocFlavors();
		final AttributeSet attributes = this.printService.getAttributes();

		sb.append("\nPrinter Information\n");
		sb.append("==========================================================\n");
		sb.append("Name: " + this.printService.getName() + "\n");
		sb.append("Supported DocFlavors:\n");

		for (DocFlavor docFlavor : docFlavors) {
			sb.append("  " + docFlavor + "\n");
		}

		@SuppressWarnings("unchecked")
		Class<? extends Attribute>[] categories = (Class<? extends Attribute>[]) this.printService
				.getSupportedAttributeCategories();

		sb.append("Supported Categories:\n");
		for (Class<?> clazz : categories) {
			sb.append("  " + clazz.getName() + "\n");
		}

		final SortedSet<Attribute> supportedAttributes = new TreeSet<Attribute>(new AttributeComparator());

		for (Class<? extends Attribute> clazz : categories) {
			for (DocFlavor docFlavor : docFlavors) {

				Object value = this.printService.getSupportedAttributeValues(clazz, docFlavor, attributes);

				if (value != null) {
					if (value instanceof Attribute) {
						supportedAttributes.add((Attribute) value);
					}
					else if (value instanceof Attribute[]) {
						supportedAttributes.addAll(Arrays.asList((Attribute[]) value));
					}
				}
			}
		}

		sb.append("Supported Attributes:\n");

		for (Attribute attribute : supportedAttributes) {
			sb.append("  " + attribute + "(" + attribute.getCategory().getSimpleName() + ")\n");
		}

		return sb.toString();
	}

	public static SortedSet<PrintService> getAvailablePrinterServices() {
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
		printServices[0].getName();

		SortedSet<PrintService> printServicesList = new TreeSet<PrintService>(new PrintServiceComparator());
		printServicesList.addAll(Arrays.asList(printServices));

		return printServicesList;
	}

	public SortedSet<DocFlavor> getDocFlavorsSupportingAttribute(Attribute attribute) {

		final SortedSet<DocFlavor> docFlavors = this.getDocFlavors();
		final AttributeSet attributes = this.printService.getAttributes();

		final SortedSet<DocFlavor> matchedDocFlavors = new TreeSet<DocFlavor>(new DocFlavorComparator());

		for (DocFlavor docFlavor : docFlavors) {

			final Object value = this.printService.getSupportedAttributeValues(attribute.getCategory(), docFlavor, attributes);

			if (value != null) {
				matchedDocFlavors.add(docFlavor);
			}

		}

		return matchedDocFlavors;
	}

	public PrintService getPrintService() {
		return printService;
	}

}
