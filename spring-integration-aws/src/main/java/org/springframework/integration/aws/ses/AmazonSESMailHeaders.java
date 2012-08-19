/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.aws.ses;

/**
 * All the pre defined Amazon SES Mail headers
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public interface AmazonSESMailHeaders {

	public static final String FROM_EMAIL_ID 		= 	"fromEmailId";
	public static final String TO_EMAIL_ID 			= 	"toEmailId";
	public static final String BCC_EMAIL_ID			=	"bccEmailId";
	public static final String CC_EMAIL_ID			=	"ccEmailId";
	public static final String REPLYTO_EMAIL_ID		=	"ccEmailId";
	public static final String HTML_FORMAT			=	"htmlFormat";
	public static final String SUBJECT				=	"subject";

}
