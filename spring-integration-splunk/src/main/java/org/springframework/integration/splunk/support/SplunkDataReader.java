/*
 * Copyright 2011-2014 the original author or authors.
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

package org.springframework.integration.splunk.support;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.splunk.Args;
import com.splunk.Job;
import com.splunk.ResultsReader;
import com.splunk.ResultsReaderXml;
import com.splunk.SavedSearch;
import com.splunk.SavedSearchCollection;
import com.splunk.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.splunk.core.DataReader;
import org.springframework.integration.splunk.core.ServiceFactory;
import org.springframework.integration.splunk.event.SplunkEvent;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Data reader to search data from Splunk.
 *
 * There are 5 ways to search data provided by Splunk SDK: saved search, blocking search,
 * non blocking search, realtime search, export search.
 *
 * Splunk search also supports time range search with earliestTime and latestTime.
 * For the first time start, initEarliestTime is used as earliestTime.
 * If user does not specify earliestTime and latestTime, latestTime is "now"
 * earliestTime is the time that last polling is run.
 *
 * @author  Jarred Li
 * @author Olivier Lamy
 * @since 1.0
 *
 */
public class SplunkDataReader implements DataReader, InitializingBean {

	private static final String DATE_FORMAT = "MM/dd/yy HH:mm:ss:SSS";

	private static final String SPLUNK_TIME_FORMAT = "%m/%d/%y %H:%M:%S:%3N";

	private static final Log logger = LogFactory.getLog(SplunkDataReader.class);

	private SearchMode mode;

	private int count = 0;

	private String fieldList;

	private String search;

	private String earliestTime;

	private String latestTime;

	private String savedSearch;

	private String owner;

	private String app;

	private String initEarliestTime;

	private transient Calendar lastSuccessfulReadTime;

	private final ServiceFactory serviceFactory;

	public SplunkDataReader(ServiceFactory serviceFactory) {
		this.serviceFactory = serviceFactory;
	}

	public void setSearch(String searchStr) {
		Assert.hasText(searchStr, "search must be neither null nor empty");
		this.search = searchStr;
	}

	public void setEarliestTime(String earliestTime) {
		this.earliestTime = earliestTime;
	}

	public void setLatestTime(String latestTime) {
		this.latestTime = latestTime;
	}

	public void setSavedSearch(String savedSearch) {
		this.savedSearch = savedSearch;
	}

	public void setMode(SearchMode mode) {
		Assert.notNull(mode, "mode must be set");
		this.mode = mode;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setFieldList(String fieldList) {
		this.fieldList = fieldList;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public void setInitEarliestTime(String initEarliestTime) {
		Assert.notNull(initEarliestTime, "initial earliest time can not be null");
		this.initEarliestTime = initEarliestTime;
	}

	public SearchMode getMode() {
		return this.mode;
	}

	public int getCount() {
		return this.count;
	}

	public String getFieldList() {
		return this.fieldList;
	}

	public String getSearch() {
		return this.search;
	}

	public String getEarliestTime() {
		return this.earliestTime;
	}

	public String getLatestTime() {
		return this.latestTime;
	}

	public String getSavedSearch() {
		return this.savedSearch;
	}

	public String getOwner() {
		return this.owner;
	}

	public String getInitEarliestTime() {
		return this.initEarliestTime;
	}

	public String getApp() {
		return this.app;
	}

	public List<SplunkEvent> read() throws Exception {
		logger.debug("mode:" + this.mode);
		switch (this.mode) {
		case SAVEDSEARCH: {
			return savedSearch();
		}
		case BLOCKING: {
			return blockingSearch();
		}
		case NORMAL: {
			return nonBlockingSearch();
		}
		case EXPORT: {
			return exportSearch();
		}
		case REALTIME: {
			throw new UnsupportedOperationException("The 'real-time' search isn't supported " +
					"because of the infinite Splunk Job nature.");
//			return realtimeSearch();
		}
		}
		return null;
	}

	/**
	 * Get the earliestTime of range search.
	 *
	 * @param startTime the time where search start
	 * @param realtime if this is realtime search
	 *
	 * @return The time of last successful read if not realtime;
	 *         Time difference between last successful read and start time;
	 */
	private String calculateEarliestTime(Calendar startTime, boolean realtime) {
		String result = null;
		if (realtime) {
			result = calculateEarliestTimeForRealTime(startTime);
		}
		DateFormat df = new SimpleDateFormat(DATE_FORMAT);
		result = df.format(this.lastSuccessfulReadTime.getTime());
		return result;
	}

	/**
	 * get earliest time for realtime search
	 *
	 * @param startTime
	 * @return
	 */
	private String calculateEarliestTimeForRealTime(Calendar startTime) {
		String result = null;
		long diff = startTime.getTimeInMillis() - this.lastSuccessfulReadTime.getTimeInMillis();
		result = "-" + diff / 1000 + "s";
		return result;
	}

	private void populateArgs(Args queryArgs, Calendar startTime, boolean realtime) {
		String earliestTime = getEarliestTime(startTime, realtime);
		if (StringUtils.hasText(earliestTime)) {
			queryArgs.put("earliest_time", earliestTime);
		}

		String latestTime = getLatestTime(startTime, realtime);
		if (StringUtils.hasText(latestTime)) {
			queryArgs.put("latest_time", latestTime);
		}

		queryArgs.put("time_format", SPLUNK_TIME_FORMAT);

		if (StringUtils.hasText(this.fieldList)) {
			queryArgs.put("field_list", this.fieldList);
		}
	}

	private String getLatestTime(Calendar startTime, boolean realtime) {
		String lTime = null;
		if (StringUtils.hasText(this.latestTime)) {
			lTime = this.latestTime;
		} else {
			if (realtime) {
				lTime = "rt";
			} else {
				DateFormat df = new SimpleDateFormat(DATE_FORMAT);
				lTime = df.format(startTime.getTime());
			}
		}
		return lTime;
	}

	private String getEarliestTime(Calendar startTime, boolean realtime) {
		String eTime = null;

		if (this.lastSuccessfulReadTime == null) {
			eTime = this.initEarliestTime;
		} else {
			if (StringUtils.hasText(earliestTime)) {
				eTime = this.earliestTime;
			} else {
				String calculatedEarliestTime = calculateEarliestTime(startTime, realtime);
				if (calculatedEarliestTime != null) {
					if (realtime) {
						eTime = "rt" + calculatedEarliestTime;
					} else {
						eTime = calculatedEarliestTime;
					}
				}
			}
		}
		return eTime;
	}

	private List<SplunkEvent> runQuery(Args queryArgs) throws Exception {
		Service service = this.serviceFactory.getService();
		Job job = service.getJobs().create(this.search, queryArgs);
		while (!job.isDone()) {
			Thread.sleep(2000);
		}
		return extractData(job);

	}

	private List<SplunkEvent> blockingSearch() throws Exception {
		logger.debug("block search start");

		Args queryArgs = new Args();
		queryArgs.put("exec_mode", "blocking");
		Calendar startTime = Calendar.getInstance();
		populateArgs(queryArgs, startTime, false);
		List<SplunkEvent> data = runQuery(queryArgs);
		this.lastSuccessfulReadTime = startTime;
		return data;
	}

	private List<SplunkEvent> nonBlockingSearch() throws Exception {
		logger.debug("non block search start");

		Args queryArgs = new Args();
		queryArgs.put("exec_mode", "normal");
		Calendar startTime = Calendar.getInstance();
		populateArgs(queryArgs, startTime, false);

		List<SplunkEvent> data = runQuery(queryArgs);
		this.lastSuccessfulReadTime = startTime;
		return data;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private List<SplunkEvent> realtimeSearch() throws Exception {
		logger.debug("realtime search start");

		Args queryArgs = new Args();
		queryArgs.put("search_mode", "realtime");
		Calendar startTime = Calendar.getInstance();
		populateArgs(queryArgs, startTime, true);

		List<SplunkEvent> data = runQuery(queryArgs);
		this.lastSuccessfulReadTime = startTime;
		return data;
	}

	/**
	 * @throws Exception
	 *
	 */
	private List<SplunkEvent> exportSearch() throws Exception {
		logger.debug("export start");
		List<SplunkEvent> result = new ArrayList<SplunkEvent>();
		HashMap<String, String> data;
		SplunkEvent splunkData;

		Args queryArgs = new Args();
		Calendar startTime = Calendar.getInstance();
		populateArgs(queryArgs, startTime, false);
		queryArgs.put("output_mode", "xml");

		Service service = this.serviceFactory.getService();
		InputStream os = service.export(this.search, queryArgs);
		ResultsReaderXml resultsReader = new ResultsReaderXml(os);
		while ((data = resultsReader.getNextEvent()) != null) {
			splunkData = new SplunkEvent(data);
			result.add(splunkData);
		}
		return result;
	}

	private List<SplunkEvent> savedSearch() throws Exception {
		logger.debug("saved search start");

		Args queryArgs = new Args();
		queryArgs.put("app", "search");
		if (this.owner != null && this.owner.length() > 0) {
			queryArgs.put("owner", this.owner);
		}
		if (this.app != null && this.app.length() > 0) {
			queryArgs.put("app", this.app);
		}

		Calendar startTime = Calendar.getInstance();

			SavedSearch search = null;
			Job job = null;
			String latestTime = getLatestTime(startTime, false);
			String earliestTime = getEarliestTime(startTime, false);

			Service service = this.serviceFactory.getService();
			SavedSearchCollection savedSearches = service.getSavedSearches(queryArgs);
			for (SavedSearch s : savedSearches.values()) {
				if (s.getName().equals(this.savedSearch)) {
					search = s;
				}
			}
			if (search != null) {
				Map<String, String> args = new HashMap<String, String>();
				args.put("force_dispatch", "true");
				args.put("dispatch.earliest_time", earliestTime);
				args.put("dispatch.latest_time", latestTime);
				job = search.dispatch(args);
			}
			while (!job.isDone()) {
				Thread.sleep(2000);
			}
			List<SplunkEvent> data = extractData(job);
			this.lastSuccessfulReadTime = startTime;
			return data;

	}

	private List<SplunkEvent> extractData(Job job) throws Exception {
		List<SplunkEvent> result = new ArrayList<SplunkEvent>();
		HashMap<String, String> data;
		SplunkEvent splunkData;
		ResultsReader resultsReader;
		int total = job.getResultCount();

		if (this.count == 0 || total < this.count) {
			InputStream stream = null;
			Args outputArgs = new Args();
			outputArgs.put("count", this.count);
			outputArgs.put("output_mode", "xml");
			stream = job.getResults(outputArgs);

			resultsReader = new ResultsReaderXml(stream);
			while ((data = resultsReader.getNextEvent()) != null) {
				splunkData = new SplunkEvent(data);
				result.add(splunkData);
			}
		} else {
			int offset = 0;
			while (offset < total) {
				InputStream stream = null;
				Args outputArgs = new Args();
				outputArgs.put("output_mode", "xml");
				outputArgs.put("count", this.count);
				outputArgs.put("offset", offset);
				stream = job.getResults(outputArgs);
				resultsReader = new ResultsReaderXml(stream);
				while ((data = resultsReader.getNextEvent()) != null) {
					splunkData = new SplunkEvent(data);
					result.add(splunkData);
				}
				offset += this.count;
			}
		}
		return result;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.initEarliestTime, "initial earliest time can not be null");
	}

}
