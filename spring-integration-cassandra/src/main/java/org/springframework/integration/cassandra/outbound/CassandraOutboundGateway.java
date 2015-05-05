/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.cassandra.outbound;

import java.util.List;

import org.springframework.cassandra.core.WriteOptions;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.WriteListener;
import org.springframework.integration.cassandra.support.CassandraOutboundGatewayType;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * @author Soby Chacko
 */
public class CassandraOutboundGateway<T> extends AbstractReplyProducingMessageHandler {

	private final CassandraOperations cassandraTemplate;

	private CassandraOutboundGatewayType gatewayType = CassandraOutboundGatewayType.INSERTING;

	private WriteListener<T> writeListener;

	private boolean producesReply = true;

	private boolean async = false;

	/**
	 * Various options that can be used for Cassandra writes.
	 */
	private WriteOptions writeOptions;

	public CassandraOutboundGateway(CassandraOperations cassandraOperations) {
		Assert.notNull(cassandraOperations, "'cassandraOperations' must not be null.");
		this.cassandraTemplate = cassandraOperations;
	}

	public void setGatewayType(CassandraOutboundGatewayType gatewayType) {
		this.gatewayType = gatewayType;
	}

	public void setWriteListener(WriteListener<T> writeListener) {
		this.writeListener = writeListener;
	}

	public void setWriteOptions(WriteOptions writeOptions) {
		this.writeOptions = writeOptions;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	@Override
	public String getComponentType() {
		return "cassandra:outbound-gateway";
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object handleRequestMessage(Message<?> requestMessage) {
		Object payload = requestMessage.getPayload();
		final Object result;

		switch (this.gatewayType) {
			case INSERTING:
				if (async) {
					if (payload instanceof List) {
						result = this.cassandraTemplate.insertAsynchronously((List<T>) payload,
								this.writeListener, this.writeOptions);
					}
					else {
						result = this.cassandraTemplate.insertAsynchronously((T) payload,
								this.writeListener, this.writeOptions);
					}
				}
				else {
					if (payload instanceof List) {
						result = this.cassandraTemplate.insert((List<T>) payload, this.writeOptions);
					}
					else {
						result = this.cassandraTemplate.insert(payload, this.writeOptions);
					}
				}
				break;
			case UPDATING:
				if (async) {
					if (payload instanceof List) {
						result = this.cassandraTemplate.updateAsynchronously((List<T>) payload,
								this.writeListener, this.writeOptions);
					}
					else {
						result = this.cassandraTemplate.updateAsynchronously((T) payload,
								this.writeListener, this.writeOptions);
					}
				}
				else {
					if (payload instanceof List) {
						result = this.cassandraTemplate.update((List<T>) payload, this.writeOptions);
					}
					else {
						result = this.cassandraTemplate.update(payload, this.writeOptions);
					}
				}
				break;
			case DELETING:
				result = null;
				break;
			default:
				result = null;

		}

		if (result == null || !this.producesReply) {
			return null;
		}

		return result;
	}

}
