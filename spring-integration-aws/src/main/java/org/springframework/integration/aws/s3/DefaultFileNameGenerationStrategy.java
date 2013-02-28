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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.Message;
import org.springframework.integration.util.AbstractExpressionEvaluator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The Default file name generation strategy. The strategy does the below steps for file name
 * generation
 * 1. The expression provided for generation of the file name, it is evaluated and
 * if a value is obtained, it is used. By default it used the value present in the
 * file_name header.
 * 2. Else, if the provided payload is of type {@link File}, then the name of the file is used.
 * if the file name ends with the temporary file suffix, the suffix is removed and the
 * remainder of the file is used as the name.
 * 3. If none of the above two are provided, the file name is the <Message Id>.ext
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class DefaultFileNameGenerationStrategy extends AbstractExpressionEvaluator implements
		FileNameGenerationStrategy {

	private final Log logger = LogFactory.getLog(DefaultFileNameGenerationStrategy.class);

	private volatile String temporarySuffix = ".writing";

	private volatile String fileNameExpression = "headers['" + AmazonS3MessageHeaders.FILE_NAME + "']" ;

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.s3.FileNameGenerationStrategy#generateFileName(org.springframework.integration.Message)
	 */
	public String generateFileName(Message<?> message) {
		String generatedFileName;
		try {
			String fileName = evaluateExpression(fileNameExpression, message,String.class);
			if(StringUtils.hasText(fileName)) {
				return fileName;
			}
		} catch (Exception e) {
			//Some exception while evaluating using expression, continue to the file Name
			//Ignore
			logger.warn("Exception while evaluating the expression '"
								+ fileNameExpression + "' on the message", e);
		}
		Object payload = message.getPayload();
		if(payload instanceof File) {
			String fileName = ((File)payload).getName();
			if(fileName.endsWith(temporarySuffix)) {
				//chop off the temp suffix
				generatedFileName =  fileName.substring(0, fileName.indexOf(temporarySuffix));
			}
			else {
				generatedFileName =  fileName;
			}
		}
		else {
			//use the default name generated
			generatedFileName = message.getHeaders().getId() + ".ext";
		}
		if(logger.isInfoEnabled()) {
			logger.info("Generated file name is " + generatedFileName);
		}

		return generatedFileName;

	}

	public void setTemporarySuffix(String temporarySuffix) {
		Assert.hasText(temporarySuffix, "Temporary directory suffix should be non null, non empty");
		this.temporarySuffix = temporarySuffix;
	}

	public void setFileNameExpression(String fileNameExpression) {
		Assert.hasText(fileNameExpression, "Remote name generation expression should be non null, non empty string");
		this.fileNameExpression = fileNameExpression;
	}
}
