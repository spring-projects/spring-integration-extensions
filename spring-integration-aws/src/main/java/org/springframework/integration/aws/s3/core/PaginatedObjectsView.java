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
package org.springframework.integration.aws.s3.core;

import java.util.List;

/**
 * Returns the Paginated view of the objects in Amazon S3 for the queries bucket
 * See {@link AmazonS3Operations} for more details on various operations on S3
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public interface PaginatedObjectsView {

	/**
	 * Gets the Paginated List of Object names
	 * @return: A {@link List} of paginated object names
	 */
	List<S3ObjectSummary> getObjectSummary();

	/**
	 * Invoke this method to know if more pages of results is present
	 * @return true if more results are present in the {@link List} of objects returned
	 */
	boolean hasMoreResults();

	/**
	 * Contains the merker that can be used to get the next listing of objects from the
	 * S3. Contains a null value if the listing is complete, the hasMoreResults
	 * method will return true if this marker contains a non null value.
	 * @return
	 */
	String getNextMarker();

}
