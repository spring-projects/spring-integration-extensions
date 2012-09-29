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
package org.springframework.integration.splunk.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.integration.splunk.core.Connection;
import org.springframework.integration.splunk.core.ConnectionFactory;

/**
 * Pooling ConnectionFactory to pool <code>Connection</code> with Apache Commons Pool.
 *
 * @author Jarred Li
 * @since 1.0
 *
 */
public class PoolingConnectionFactory<T> implements ConnectionFactory<T>, DisposableBean {

	private final Log log = LogFactory.getLog(this.getClass());

	private final ConnectionFactory<T> connectionFactory;

	private ObjectPool<Connection<T>> pool;

	public PoolingConnectionFactory(ConnectionFactory<T> f) {
		this.connectionFactory = f;
		this.pool = new GenericObjectPool<Connection<T>>(new ConnectionPoolableObjectFactory());
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.splunk.core.ServiceFactory#getService()
	 */
	public Connection<T> getConnection() throws Exception {
		return new PooledConnection(this.pool.borrowObject());
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() throws Exception {
		pool.clear();
		pool.close();
	}

	class ConnectionPoolableObjectFactory extends BasePoolableObjectFactory<Connection<T>> {

		/* (non-Javadoc)
		 * @see org.apache.commons.pool.BasePoolableObjectFactory#makeObject()
		 */
		@Override
		public Connection<T> makeObject() throws Exception {
			return connectionFactory.getConnection();
		}

		@Override
		public void destroyObject(Connection<T> obj) throws Exception {
			obj.close();
		}

		/**
		 * Whether the object is valid or not.
		 *
		 * @param obj object to be validated
		 * @return <tt>true</tt>
		 */
		public boolean validateObject(Connection<T> obj) {
			return obj.isOpen();
		}

		/**
		 *  activate the object
		 *
		 *  @param obj ignored
		 */
		public void activateObject(Connection<T> obj) throws Exception {
			obj.isOpen();
		}


	}

	class PooledConnection implements Connection<T> {

		private Connection<T> connection;

		public PooledConnection(Connection<T> con) {
			this.connection = con;
		}

		/* (non-Javadoc)l
		 * @see org.springframework.integration.splunk.core.IService#close()
		 */
		public void close() {
			try {
				pool.returnObject(connection);
			} catch (Exception e) {
				log.warn("failed to return pooled object", e);
			}
		}

		/* (non-Javadoc)
		 * @see org.springframework.integration.splunk.core.IService#isOpen()
		 */
		public boolean isOpen() {
			return connection.isOpen();
		}

		/* (non-Javadoc)
		 * @see org.springframework.integration.splunk.core.IService#getService()
		 */
		public T getTarget() {
			return connection.getTarget();
		}

	}

}
