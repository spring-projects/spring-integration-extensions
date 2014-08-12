/*
 * Copyright 2014 the original author or authors.
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

package org.springframework.integration.dsl.file;

import java.util.Comparator;

/**
 * @author Artem Bilan
 */
public abstract class File {

	public static FileInboundChannelAdapterSpec inboundAdapter(java.io.File directory) {
		return inboundAdapter(directory, null);
	}

	public static FileInboundChannelAdapterSpec inboundAdapter(java.io.File directory,
			Comparator<java.io.File> receptionOrderComparator) {
		return new FileInboundChannelAdapterSpec(receptionOrderComparator).directory(directory);
	}

	public static FileWritingMessageHandlerSpec outboundAdapter(java.io.File destinationDirectory) {
		return new FileWritingMessageHandlerSpec(destinationDirectory).expectReply(false);
	}

	public static FileWritingMessageHandlerSpec outboundAdapter(String directoryExpression) {
		return new FileWritingMessageHandlerSpec(directoryExpression).expectReply(false);
	}

	public static FileWritingMessageHandlerSpec outboundGateway(java.io.File destinationDirectory) {
		return new FileWritingMessageHandlerSpec(destinationDirectory).expectReply(true);
	}

	public static FileWritingMessageHandlerSpec outboundGateway(String directoryExpression) {
		return new FileWritingMessageHandlerSpec(directoryExpression).expectReply(true);
	}

	public static TailAdapterSpec tailAdapter(java.io.File file) {
		return new TailAdapterSpec().file(file);
	}

}
