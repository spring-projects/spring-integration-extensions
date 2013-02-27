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


import org.springframework.util.StringUtils;

/**
 * The abstract file name filter that first filters out the file if it is
 * not eligible for filtering based on the name.
 * For e.g, if a particular folder on S3 is to be synchronized with the
 * local file system, then the name of the key is initially accepted
 * only if it corresponds to an object under that folder or sub folder on S3.
 * All other keys are ignored
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public abstract class AbstractFileNameFilter implements FileNameFilter {

	private volatile String folderName;

	private volatile boolean acceptSubFolders;

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.s3.FileNameFilter#accept(java.lang.String)
	 */
	public boolean accept(String fileName) {
		if(!StringUtils.hasText(fileName))
			return false;

		if(StringUtils.hasText(folderName)) {
			if(fileName.startsWith(folderName)) {
				//This file is in the folder or in a child folder or the given folder
				String relativePath = fileName.substring(folderName.length());
				if(relativePath.length() == 0 || (!acceptSubFolders && relativePath.indexOf("/") != -1)) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		else {
			//Its the folder entry within the bucket
			if(!acceptSubFolders && fileName.indexOf("/") != -1) {
				return false;
			}
		}
		if(fileName.contains("/")) {
			return isFileNameAccepted(fileName.substring(fileName.lastIndexOf("/") + 1));
		}
		else {
			return isFileNameAccepted(fileName);
		}

	}


	/**
	 * Gets the folder whose file are to be accepted, this path is relative to the
	 * bucket.
	 * @return
	 */
	public String getFolderName() {
		return folderName;
	}

	/**
	 * Sets the base folder name under which which the files will be accepted.
	 *
	 * @param folderName
	 */
	public void setFolderName(String folderName) {
		if(StringUtils.hasText(folderName)) {
			folderName = folderName.trim();
			if(folderName.equals("/")) {
				folderName = null;
			}
			else {
				if(!folderName.endsWith("/")) {
					folderName = folderName + "/";
				}

				if(folderName.startsWith("/")) {
					folderName = folderName.substring(1);
				}
			}
			this.folderName = folderName;
		}
		else {
			this.folderName = null;
		}

	}


	/**
	 * Checks the flag if the sub folders are to be accepted or not.
	 *
	 * @return
	 */
	public boolean isAcceptSubFolders() {
		return acceptSubFolders;
	}


	/**
	 * Sets if the sub folders of the folder set in {@link #folderName}
	 * are to be accepted or not.
	 *
	 * @param acceptSubFolders
	 */
	public void setAcceptSubFolders(boolean acceptSubFolders) {
		this.acceptSubFolders = acceptSubFolders;
	}


	public abstract boolean isFileNameAccepted(String fileName);

}
