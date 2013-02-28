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
package org.springframework.integration.aws.s3.config.xml;

import org.springframework.integration.aws.s3.core.AmazonS3Object;
import org.springframework.integration.aws.s3.core.AmazonS3Operations;
import org.springframework.integration.aws.s3.core.PaginatedObjectsView;

/**
 * The dummy {@link AmazonS3Operations} for tests
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AmazonS3DummyOperations implements AmazonS3Operations {

	@Override
	public PaginatedObjectsView listObjects(String bucketName,
			String folder, String nextMarker, int pageSize) {
		return null;
	}

	@Override
	public void putObject(String bucketName, String folder,
			String objectName, AmazonS3Object s3Object) {
	}

	@Override
	public AmazonS3Object getObject(String bucketName, String folder,
			String objectName) {
		return null;
	}

	@Override
	public boolean removeObject(String bucketName, String folder,
			String objectName) {
		return false;
	}

}
