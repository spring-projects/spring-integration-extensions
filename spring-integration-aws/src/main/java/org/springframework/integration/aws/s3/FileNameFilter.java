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
package org.springframework.integration.aws.s3;

/**
 * The strategy interface used to filter out file names based on some predetermined criteria
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public interface FileNameFilter {

	/**
	 * Determines whether to accept the file with the given name or not
	 * @param fileName
	 * @return true if the file with the given name can be accepted, else false
	 */
	boolean accept(String fileName);
}
