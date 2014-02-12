/*
 * Copyright 2014 the original author or authors.
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

package org.springframework.integration.dsl.support;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.aopalliance.aop.Advice;

import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.transaction.TransactionSynchronizationFactory;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.ErrorHandler;

/**
 * @author Artem Bilan
 */
public final class Pollers {

	public static PollerSpec trigger(Trigger trigger) {
		return new PollerSpec(trigger);
	}

	public static PollerSpec fixedRate(long period) {
		return fixedRate(period, null);
	}

	public static PollerSpec fixedRate(long period, TimeUnit timeUnit) {
		return periodicTrigger(period, timeUnit, true);
	}

	public static PollerSpec fixedDelay(long period) {
		return fixedDelay(period, null);
	}

	public static PollerSpec fixedDelay(long period, TimeUnit timeUnit) {
		return periodicTrigger(period, timeUnit, false);
	}

	private static PollerSpec periodicTrigger(long period, TimeUnit timeUnit, boolean fixedRate) {
		PeriodicTrigger periodicTrigger = new PeriodicTrigger(period, timeUnit);
		periodicTrigger.setFixedRate(fixedRate);
		return new PollerSpec(periodicTrigger);
	}

	public static PollerSpec cron(String cronExpression) {
		return cron(cronExpression, TimeZone.getDefault());
	}

	public static PollerSpec cron(String cronExpression, TimeZone timeZone) {
		return new PollerSpec(new CronTrigger(cronExpression, timeZone));
	}

	private Pollers() {
	}

	public static final class PollerSpec {

		private final PollerMetadata pollerMetadata = new PollerMetadata();

		private final List<Advice> adviceChain = new LinkedList<Advice>();

		private PollerSpec(Trigger trigger) {
			this.pollerMetadata.setTrigger(trigger);
		}

		public PollerSpec transactionSynchronizationFactory(TransactionSynchronizationFactory transactionSynchronizationFactory) {
			pollerMetadata.setTransactionSynchronizationFactory(transactionSynchronizationFactory);
			return this;
		}

		public PollerSpec errorHandler(ErrorHandler errorHandler) {
			pollerMetadata.setErrorHandler(errorHandler);
			return this;
		}

		public PollerSpec maxMessagesPerPoll(long maxMessagesPerPoll) {
			pollerMetadata.setMaxMessagesPerPoll(maxMessagesPerPoll);
			return this;
		}

		public PollerSpec receiveTimeout(long receiveTimeout) {
			pollerMetadata.setReceiveTimeout(receiveTimeout);
			return this;
		}

		public PollerSpec advice(Advice... advice) {
			this.adviceChain.addAll(Arrays.asList(advice));
			return this;
		}

		public PollerSpec transactional(PlatformTransactionManager transactionManager) {
			return this.advice(new TransactionInterceptor(transactionManager, new MatchAlwaysTransactionAttributeSource()));
		}

		public PollerSpec taskExecutor(Executor taskExecutor) {
			pollerMetadata.setTaskExecutor(taskExecutor);
			return this;
		}

		public PollerSpec sendTimeout(long sendTimeout) {
			pollerMetadata.setSendTimeout(sendTimeout);
			return this;
		}

		public PollerMetadata get() {
			pollerMetadata.setAdviceChain(this.adviceChain);
			return this.pollerMetadata;
		}



	}

}
