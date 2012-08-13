/**
 *
 */
package org.springframework.integration.print.support;

import java.util.SortedSet;

import javax.print.PrintService;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.integration.print.core.PrintServiceExecutor;
import org.springframework.util.StringUtils;

/**
 * @author ghillert
 *
 */
public class PrintUtilsTest {

	private static final Logger LOGGER = Logger.getLogger(PrintUtilsTest.class);

	private PrintServiceExecutor printServiceExecutor = new PrintServiceExecutor();

	/**
	 * Test method for {@link org.springframework.integration.print.core.PrintServiceExecutor#getAvailablePrinterServices()}.
	 */
	@Test
	public void testGetAvailablePrinterServices() {
		SortedSet<PrintService> printers = PrintServiceExecutor.getAvailablePrinterServices();
		LOGGER.info("Available Printers: " + StringUtils.collectionToCommaDelimitedString(printers));
	}

	@Test
	public void testGetPrintServiceByName() {
		PrintService printService = printServiceExecutor.getPrintService();
		LOGGER.info("Default Printer found: " + printService);
	}

}
