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
package org.springframework.integration.aws.common;

import java.io.IOException;
import java.util.Properties;

/**
 * Class extended by some of the test cases which are not spring based to get access to the properties
 * and other common functionality
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public abstract class BaseTestCase {

	protected static String propsLocation = "testprops.properties";
	protected static Properties props;
	static {
		props = new Properties();
		try {
			props.load(ClassLoader.getSystemClassLoader().getResourceAsStream(propsLocation));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected static String getProperty(String key) {
		if(props != null)
			return props.getProperty(key);
		else
			return null;
	}

}
