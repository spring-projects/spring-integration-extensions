/*
 * Copyright 2002-2014 the original author or authors.
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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Interface for performing File Operations on local file system.
 * It supports registering an {@link FileEventHandler} instances that
 * notifies the operations on the file those were performed
 *
 * @author Amol Nayak
 * @author Rob Harrop
 *
 * @since 0.5
 *
 */
public interface InboundLocalFileOperations {

	/**
	 * Registers an individual event handler.
	 * @param handler
	 */
	void addEventListener(FileEventHandler handler);

	/**
	 * Registers a {@link List} of {@link FileEventHandler} instances
	 * @param handlers
	 */
	void setEventListeners(List<FileEventHandler> handlers);

	/**
	 * The temporary file suffix that will be used when the file is being written to the filesystem
	 * @param prefix
	 */
	void setTemporaryFileSuffix(String prefix);


	/**
	 * Sets the flag to true if directories given are to be created if not present
	 *
	 * @param createDirectoriesIfRequired
	 */
	void setCreateDirectoriesIfRequired(boolean createDirectoriesIfRequired);

	/**
	 * The method will write to the file with the specified name in the specified directory
	 * from the given {@link InputStream}. Upon completion of the writing the appropriate
	 * {@link FileEventHandler} instance(s) will be notified with the {@link FileOperationType}
	 * <i>WRITE</i> and {@link File} instance for the created file.
	 * @param directory
	 * @param fileName
	 * @param in
	 */
	void writeToFile(File directory,String fileName,InputStream in) throws IOException ;
}
