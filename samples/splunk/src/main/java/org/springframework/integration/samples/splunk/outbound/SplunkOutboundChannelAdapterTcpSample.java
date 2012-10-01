/*
 * Copyright 2011-2012 the original author or authors.
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
package org.springframework.integration.samples.splunk.outbound;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.splunk.entity.SplunkData;
import org.springframework.integration.support.MessageBuilder;


/**
 * @author Jarred Li
 * @since 1.0
 *
 */
public class SplunkOutboundChannelAdapterTcpSample {

	public static void main(String args[]) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"SplunkOutboundChannelAdapterTcpTests-context.xml", SplunkOutboundChannelAdapterTcpSample.class);
		ctx.start();

		SubscribableChannel channel = ctx.getBean("outputToSplunk", SubscribableChannel.class);

		SplunkData data = new SplunkData("spring", "spring:example");
		data.setCommonDesc("description");

		Message<SplunkData> msg = MessageBuilder.withPayload(data).build();
		channel.send(msg);


	}
}
