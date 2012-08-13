package org.springframework.integration.print.support;

import javax.print.DocPrintJob;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PrintJobMonitor {

	private static final Log LOG = LogFactory.getLog(PrintJobMonitor.class);

	boolean done = false;

	public PrintJobMonitor(DocPrintJob job) {

		job.addPrintJobListener(new PrintJobAdapter() {

			@Override
			public void printJobCanceled(PrintJobEvent arg0) {
				if (LOG.isWarnEnabled()) {
					LOG.warn("PrintJob Job canceled.");
				}
				allDone();
			}

			@Override
			public void printJobCompleted(PrintJobEvent printJobEvent) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("PrintJob Job completed.");
				}
				allDone();
			}

			public void printJobFailed(PrintJobEvent pje) {
				allDone();
			}

			public void printJobNoMoreEvents(PrintJobEvent pje) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("PrintJob Job completed (No More Events).");
				}
				allDone();
			}

			void allDone() {
				synchronized (PrintJobMonitor.this) {
					done = true;
					PrintJobMonitor.this.notify();
				}
			}

		});
	}

	public synchronized void waitForDone() {
		try {
			while (!done) {
				wait();
			}
		} catch (InterruptedException e) {
		}
	}

}
