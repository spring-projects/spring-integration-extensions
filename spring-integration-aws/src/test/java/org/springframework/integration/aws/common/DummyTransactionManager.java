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
package org.springframework.integration.aws.common;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * Dummy transaction manager used for the test
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class DummyTransactionManager extends AbstractPlatformTransactionManager{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	@Override
	protected void doBegin(Object arg0, TransactionDefinition arg1)
			throws TransactionException {
		//NOP

	}


	@Override
	protected void doCommit(DefaultTransactionStatus arg0)
			throws TransactionException {
		//NOP

	}


	@Override
	protected Object doGetTransaction() throws TransactionException {
		return new DefaultTransactionDefinition();
	}


	@Override
	protected void doRollback(DefaultTransactionStatus status)
			throws TransactionException {
		//NOP

	}

}
