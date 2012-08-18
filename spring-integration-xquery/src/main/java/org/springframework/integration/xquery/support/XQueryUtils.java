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
package org.springframework.integration.xquery.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.integration.MessagingException;
import org.springframework.util.Assert;

/**
 * The common utility class that is used to perform all common operations
 * amongst XQuery modules. To contain all common operations that would be used
 * by the router, transformer etc along with all the parsers for these components
 *
 * @author Amol Nayak
 *
 * @since 2.2
 *
 */
public class XQueryUtils {

	private static final Log logger = LogFactory.getLog(XQueryUtils.class);



	private XQueryUtils() {
		//prevent instantiation
		throw new AssertionError("Cannot instantiate a utility class");
	}


	/**
	 * Reads the XQuery string from the resource file specified
	 *
	 * @param resource the {@link Resource} instance of the file that contains the XQuery
	 * 			currently only classpath and file resources are supported
	 *
	 * @return the XQuery string from the resource specified
	 */
	public static String readXQueryFromResource(Resource resource) {
		Assert.notNull(resource, "null resource provided");
		Assert.isTrue(resource.exists(), "Provided XQuery resource does not exist");
		Assert.isTrue(resource.isReadable(), "Provided XQuery resource is not readable");
		BufferedReader reader = null;
		try {
			URL url = resource.getURL();
			InputStream inStream = url.openStream();
			reader = new BufferedReader(new InputStreamReader(inStream));
			String line = reader.readLine();
			StringBuilder builder = new StringBuilder();
			while(line != null) {
				builder.append(line).append("\n");
				line = reader.readLine();
			}
			String xQuery = builder.toString();
			reader.close();
			return xQuery;
		} catch (IOException e) {
			throw new MessagingException("Error while reading the xQuery resource", e);
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("Exception while closing reader", e);
				}
			}
		}
	}
}
