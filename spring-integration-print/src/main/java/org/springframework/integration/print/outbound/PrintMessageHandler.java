/*
 * Copyright 2002-2012 the original author or authors
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package org.springframework.integration.print.outbound;

import java.io.InputStream;
import java.util.Locale;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.Sides;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.Message;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.print.core.PrintServiceExecutor;
import org.springframework.integration.print.support.PrintJobMonitor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Outbound Adapter that supports printing of payloads.
 *
 * @author Gunnar Hillert
 * @since 1.0
 */
public class PrintMessageHandler extends AbstractMessageHandler {

	private static final Log LOG = LogFactory.getLog(PrintMessageHandler.class);

	private final PrintServiceExecutor printServiceExecutor;

	private volatile String printJobName;
	private volatile MediaSizeName mediaSizeName;
	private volatile Sides sides;
	private volatile Copies copies;
	private volatile MediaTray mediaTray;
	private volatile Chromaticity chromaticity;
	private final DocFlavor docFlavor;

	private volatile PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();

	public PrintMessageHandler(PrintServiceExecutor printServiceExecutor, DocFlavor docFlavor) {
		Assert.notNull(docFlavor, "'docFlavor' must not be null.");
		this.docFlavor = docFlavor;
		this.printServiceExecutor = printServiceExecutor;

	}

	public PrintMessageHandler(PrintServiceExecutor printServiceExecutor, String mimeType, String className) {

		Assert.hasText(className, "'className' must be neither null nor empty.");
		Assert.hasText(mimeType, "'mimeType' must be neither null nor empty.");

		this.docFlavor = new DocFlavor(mimeType, className);
		this.printServiceExecutor = printServiceExecutor;

	}

	public PrintMessageHandler(PrintServiceExecutor printServiceExecutor) {
		this.docFlavor = DocFlavor.STRING.TEXT_PLAIN;
		this.printServiceExecutor = printServiceExecutor;
	}

	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Printing using printer '" + this.printServiceExecutor.getPrintService().getName() + "'.");
		}

	DocAttributeSet das = new HashDocAttributeSet();
		das.add(Chromaticity.MONOCHROME);

		Object payload = message.getPayload();

		final Doc doc = new SimpleDoc(message.getPayload(), docFlavor, das);

		final DocPrintJob job = this.printServiceExecutor.getPrintService().createPrintJob();

		PrintJobMonitor printJobMonitor = new PrintJobMonitor(job);

		job.print(doc, this.printRequestAttributeSet);

		printJobMonitor.waitForDone();

		if (payload instanceof InputStream) {
			((InputStream) payload).close();
		}

	}

	@Override
	protected void onInit() throws Exception {

		if (this.copies != null) {
			//Assert.isTrue(printService.isAttributeValueSupported(this.copies, this.docFlavor, this.printService.getAttributes()),
			//	"Attribute '" + this.copies.getName() + "' with value '" + this.copies.getValue() + "' is not supported.");
			this.printRequestAttributeSet.add(this.copies);
		}

		if (this.chromaticity != null) {
			Assert.isTrue(this.printServiceExecutor.getPrintService().isAttributeValueSupported(this.chromaticity, this.docFlavor, this.printServiceExecutor.getPrintService().getAttributes()),
				"Attribute '" + this.chromaticity.getName() + "' with value '" + this.chromaticity.getValue() + "' is not supported.");
			this.printRequestAttributeSet.add(this.chromaticity);
		}

		if (this.mediaSizeName != null) {
			Assert.isTrue(this.printServiceExecutor.getPrintService().isAttributeValueSupported(this.mediaSizeName, null, null),
				"Attribute '" + this.mediaSizeName.getName()
				+ "' with value '" + this.mediaSizeName.getValue() + "' is not supported.");
			this.printRequestAttributeSet.add(this.mediaSizeName);
		}

		if (this.mediaTray != null) {
			Assert.isTrue(this.printServiceExecutor.getPrintService().isAttributeValueSupported(this.mediaTray, this.docFlavor, this.printServiceExecutor.getPrintService().getAttributes()),
				"Attribute value '" + this.mediaTray + "' is not supported.");
			this.printRequestAttributeSet.add(this.mediaTray);
		}

		if (this.sides!=null) {
			Assert.isTrue(this.printServiceExecutor.getPrintService().isAttributeValueSupported(this.sides, this.docFlavor, this.printServiceExecutor.getPrintService().getAttributes()),
				"Attribute value '" + this.sides + "' is not supported.");
			this.printRequestAttributeSet.add(this.sides);
		}

		if (StringUtils.hasText(this.printJobName)) {
			this.printRequestAttributeSet.add(new JobName(this.printJobName, Locale.getDefault()));
		}

		super.onInit();

	}

	/**
	 * Specifies how the pages are are printed to the selected medium,
	 * e.g. duplex or one-sided. Under the covers, this property will add the
	 * provided {@link Sides} instance to the respective {@link PrintRequestAttributeSet}.
	 *
	 * This property is not used, if not specified (No explicit default value).
	 *
	 * @param sides Must not be null
	 */
	public void setSides(Sides sides) {
		Assert.notNull(sides, "'sides' must not be null.");
		this.sides = sides;
	}

	/**
	 * Let's you specify an integer value that indicates how many time each
	 * print page shall be printed. Under the covers, this property will add a
	 *
	 * {@link Copies} instance to the respective {@link PrintRequestAttributeSet}.
	 * This property is not used, if not specified (No explicit default value).
	 *
	 * @param numberOfCopies Must be greater than 0
	 */
	public void setCopies(int numberOfCopies) {
		Assert.isTrue(numberOfCopies > 0, "'copies' must be greater than 0.");
		this.copies = new Copies(numberOfCopies);
	}

	/**
	 * Specifies the media tray/bin for the print job. This property can be used
	 * instead of providing the {@link #mediaSizeName} property. Under the covers,
	 * this property will add the provided {@link MediaTray} instance to the
	 * respective {@link PrintRequestAttributeSet}. This property is not used,
	 * if not specified (No explicit default value).
	 *
	 * @param mediaTray Must not be null
	 */
	public void setMediaTray(MediaTray mediaTray) {
		Assert.notNull(mediaTray, "'mediaTray' must not be null.");
		this.mediaTray = mediaTray;
	}

	/**
	 *  Specifies whether you want to do monochrome or color printing. Under the
	 *  covers, this property will add the provided {@link Chromaticity} instance
	 *  to the respective {@link PrintRequestAttributeSet}. This property is not
	 *  used, if not specified (No explicit default value).
	 *
	 *  @param chromaticity Must not be null.
	 */
	public void setChromaticity(Chromaticity chromaticity) {
		Assert.notNull(chromaticity, "'chromaticity' must not be null.");
		this.chromaticity = chromaticity;
	}

	/**
	 * Sets the {@link MediaSizeName}. Under the covers, this property will add
	 * the provided {@link MediaSizeName} to the respective {@link PrintRequestAttributeSet}.
	 * This property is not used, if not specified (No explicit default value).
	 *
	 * @param mediaSizeName Must not be null.
	 */
	public void setMediaSizeName(MediaSizeName mediaSizeName) {
		Assert.notNull(mediaSizeName, "'mediaSizeName' must not be null.");
		this.mediaSizeName = mediaSizeName;
	}

	/**
	 * Optional property that let's you specify the name of the print job as it
	 * is added to the print queue. Under the covers, this property will create
	 * a {@link JobName} instance and add it to the respective {@link PrintRequestAttributeSet}.
	 * This property is not used, if not specified (No explicit default value).
	 *
	 * @param printJobName Must neither be null nor empty.
	 */
	public void setPrintJobName(String printJobName) {
		Assert.hasText(printJobName, "'mediaSizeName' must neither be null nor empty.");
		this.printJobName = printJobName;
	}

}
