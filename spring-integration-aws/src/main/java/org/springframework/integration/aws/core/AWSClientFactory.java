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

import com.amazonaws.AmazonWebServiceClient;

/**
 * The factory interface that would be used to get the implementation of the appropriate
 * instance of {@link AmazonWebServiceClient}
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public interface AWSClientFactory<T extends AmazonWebServiceClient> {

	/**
	 * Returns the instance of the {@link AmazonWebServiceClient} with the apropriate endpoint value
	 * set based on the provided url value
	 *
	 * @param url The url of the service
	 * @return The appropriate {@link AmazonWebServiceClient} for the provided endpoint URL
	 */
	T getClient(String url);
}
