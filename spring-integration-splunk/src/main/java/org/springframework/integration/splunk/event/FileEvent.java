/*
 * Copyright 2002-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.splunk.event;

/**
 * @author David Turanski
 *
 */
@SuppressWarnings("serial")
public class FileEvent extends SplunkEvent {
	// ----------------------------------
	// File management
	// ----------------------------------

	/**
	 * The time the file (the object of the event) was accessed.
	 */
	public static String FILE_ACCESS_TIME = "file_access_time";
	/**
	 * The time the file (the object of the event) was created.
	 */
	public static String FILE_CREATE_TIME = "file_create_time";
	/**
	 * A cryptographic identifier assigned to the file object affected by the
	 * event.
	 */
	public static String FILE_HASH = "file_hash";
	/**
	 * The time the file (the object of the event) was altered.
	 */
	public static String FILE_MODIFY_TIME = "file_modify_time";
	/**
	 * The name of the file that is the object of the event (without location
	 * information related to local file or directory structure).
	 */
	public static String FILE_NAME = "file_name";
	/**
	 * The location of the file that is the object of the event, in terms of
	 * local file and directory structure.
	 */
	public static String FILE_PATH = "file_path";
	/**
	 * Access controls associated with the file affected by the event.
	 */
	public static String FILE_PERMISSION = "file_permission";
	/**
	 * The size of the file that is the object of the event. Indicate whether
	 * Bytes, KB, MB, GB.
	 */
	public static String FILE_SIZE = "file_size";

	public void setFileAccessTime(long fileAccessTime) {
		addPair(FILE_ACCESS_TIME, fileAccessTime);
	}

	public void setFileCreateTime(long fileCreateTime) {
		addPair(FILE_CREATE_TIME, fileCreateTime);
	}

	public void setFileHash(String fileHash) {
		addPair(FILE_HASH, fileHash);
	}

	public void setFileModifyTime(long fileModifyTime) {
		addPair(FILE_MODIFY_TIME, fileModifyTime);
	}

	public void setFileName(String fileName) {
		addPair(FILE_NAME, fileName);
	}

	public void setFilePath(String filePath) {
		addPair(FILE_PATH, filePath);
	}

	public void setFilePermission(String filePermission) {
		addPair(FILE_PERMISSION, filePermission);
	}

	public void setFileSize(long fileSize) {
		addPair(FILE_SIZE, fileSize);
	}
}
