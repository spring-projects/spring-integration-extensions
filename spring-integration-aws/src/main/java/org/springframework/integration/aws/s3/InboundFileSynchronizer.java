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
 * The strategy interface for synchronizion the remote file system with local directory
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public interface InboundFileSynchronizer {

	/**
	 * The operation synchronizes the remote S3 file system with the local directory
	 * It retrieved new files and updates existing ones with the latest content from S3
	 * Please note that this method will NOT delete any additional files present on the
	 * local filesystem
	 * @param localDirectory The local directory that needs to be synchronized with the Remote S3 bucket
	 * @param bucketName The name of the bucket whose contents are to be synchronized
	 * @param remoteFolder The folder name in S3 whose contents are to be synchronized
	 *                     use / if the contents of the bucket starting from the root
	 *                     are to be synchronized. This operation will only synchronize
	 *                     the files resent in the given remote folder and will ignore
	 *                     all the folders and sub folder in it.
	 */
	void synchronizeToLocalDirectory(File localDirectory,String bucketName,String remoteFolder);

	/**
	 * Sets the max number of Files to synchronize at a time. This is the max number of
	 * Object information retrieved at a time from S3 and synchronized with the Local file system
	 * before the next set of information is retrieved from S3. Please note if the file name
	 * pattern is set the total number of files from a batch fetched can be anywhere between
	 * 0 to the max number of files fetched per batch
	 * @param batchSize
	 */
	void setSynchronizingBatchSize(int batchSize);

	/**
	 * Sets the file name regex that will be used to match the key value from S3
	 * to find a match.
	 * @param fileNameRegex
	 */
	void setFileNamePattern(String fileNameRegex);

	/**
	 * Sets the simple file name wildcard to match to match the file e.g., it can be
	 * set to *.txt to accept all .txt files
	 */
	void setFileWildcard(String wildcardString);

	/**
	 * Set the value to true to enable the objects to be accepted even if they
	 * are present in the sub folder of the remote folder passed to
	 * {@link #synchronizeToLocalDirectory(File, String, String)}
	 *
	 * @param acceptSubFolders
	 */
	void setAcceptSubFolders(boolean acceptSubFolders);
}
