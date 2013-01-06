/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.aws.core;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * Load the AWS credentials from the .properties file
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class PropertiesAWSCredentials extends BasicAWSCredentials implements InitializingBean {


	public static final String DEFAULT_AWS_ACCESS_KEY_PROPERTY = "accessKey";
	public static final String DEFAULT_AWS_SECRET_KEY_PROPERTY = "secretKey";

	private String accessKeyProperty = DEFAULT_AWS_ACCESS_KEY_PROPERTY;
	private String secretKeyProperty = DEFAULT_AWS_SECRET_KEY_PROPERTY;
	private String propertyFileName;

	/**.
	 * Constructor accepting the properties file
	 * @param propertyFileName
	 */
	public PropertiesAWSCredentials(String propertyFileName) {
		super();
		this.propertyFileName = propertyFileName;
	}

	/**.
	 * Gets the property name which holds the
	 * @return
	 */
	public String getAccessKeyProperty() {
		return accessKeyProperty;
	}

	/**.
	 * Sets the name of the property that will be used as the key in the properties
	 * file to hold the AWS access key
	 * @param accessKeyProperty
	 */
	public void setAccessKeyProperty(String accessKeyProperty) {
		this.accessKeyProperty = accessKeyProperty;
	}

	/**.
	 * Gets the property name which holds the
	 * @return
	 */
	public String getSecretKeyProperty() {
		return secretKeyProperty;
	}

	/**.
	 * Sets the name of the property that will be used as the key in the properties
	 * file to hold the AWS secret key
	 * @param accessKeyProperty
	 */
	public void setSecretKeyProperty(String secretKeyProperty) {
		this.secretKeyProperty = secretKeyProperty;
	}

	/**.
	 * Get the name of the property file that will hold the AWS credentials
	 * @return
	 */
	public String getPropertyFileName() {
		return propertyFileName;
	}

	/**.
	 * Sets the name of the file that will hold the AW credentials
	 * @param propertyFileName
	 */
	public void setPropertyFileName(String propertyFileName) {
		this.propertyFileName = propertyFileName;
	}

	/**.
	 * Load the properties file and the keys
	 */
	public void afterPropertiesSet() throws Exception {
		if (!StringUtils.hasText(propertyFileName))
			throw new InvalidAWSCredentialsException("Mandatory property propertyFileName expected");

		if(!StringUtils.hasText(accessKeyProperty))
			throw new InvalidAWSCredentialsException("accessKeyValue has to be non empty and non null");

		if(!StringUtils.hasText(secretKeyProperty))
			throw new InvalidAWSCredentialsException("secretKeyValue has to be non empty and non null");

		loadProperties();

	}

	/**.
	 * The private method that loads the properties from the .properties file and sets the access keys
	 */
	private void loadProperties() {
		Resource resource;
		if(propertyFileName.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
			resource = new ClassPathResource(propertyFileName.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length()), ClassUtils.getDefaultClassLoader());
		} else {
			resource = new ClassPathResource(propertyFileName, ClassUtils.getDefaultClassLoader());
		}
		if(!resource.exists())
			throw new InvalidAWSCredentialsException("Unable to find resource \"" + propertyFileName + "\" in classpath");

		Properties props = new Properties();
		try {
			props.load(resource.getInputStream());
		} catch (IOException e) {
			throw new InvalidAWSCredentialsException("Unable to load properties from  \"" + propertyFileName + "\" in classpath");
		}

		setAccessKey((String)props.get(accessKeyProperty));
		setSecretKey((String)props.get(secretKeyProperty));


	}
}
