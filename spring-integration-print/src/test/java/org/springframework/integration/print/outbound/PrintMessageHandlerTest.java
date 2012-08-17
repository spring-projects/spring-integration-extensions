package org.springframework.integration.print.outbound;

import java.io.InputStream;

import javax.print.DocFlavor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.integration.print.core.PrintServiceExecutor;
import org.springframework.integration.support.MessageBuilder;

public class PrintMessageHandlerTest {

	private static final Log LOG = LogFactory.getLog(PrintMessageHandler.class);

	@Test
	@Ignore
	public void testPdfPrint() {

		PrintServiceExecutor printServiceExecutor = new PrintServiceExecutor();
		PrintMessageHandler handler = new PrintMessageHandler(printServiceExecutor, DocFlavor.INPUT_STREAM.AUTOSENSE);

		InputStream is = PrintMessageHandlerTest.class.getResourceAsStream("Spring Integration Print Testing.pdf");
		handler.setCopies(2);
		handler.setPrintJobName("My Spring Integration Print test.");
		handler.afterPropertiesSet();
		handler.handleMessage(MessageBuilder.withPayload(is).build());

	}

	@Test
	@Ignore
	public void testShowSupportedAttributes() {
		final PrintServiceExecutor printServiceExecutor = new PrintServiceExecutor();
		final String info = printServiceExecutor.getPrinterInfo();
		LOG.info(info);
	}
}
