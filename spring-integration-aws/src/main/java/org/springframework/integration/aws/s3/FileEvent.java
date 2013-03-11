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

import java.io.File;


/**
 * The interface denoting the file event type and the {@link File} on which the event occurred
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public interface FileEvent {

	/**
	 * The Type of the operation on the {@link File} that occurred
	 * @return The {@link FileOperationType}
	 */
	FileOperationType getFileOperation();

	/**
	 * The File on which the given occurred
	 */
	File getFile();
}
