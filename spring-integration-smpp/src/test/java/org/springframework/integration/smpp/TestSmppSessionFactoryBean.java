/* Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.smpp;

import java.lang.reflect.Field;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.Session;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.smpp.session.DelegatingMessageReceiverListener;
import org.springframework.integration.smpp.session.ExtendedSmppSession;
import org.springframework.integration.smpp.session.ExtendedSmppSessionAdaptingDelegate;
import org.springframework.integration.smpp.session.SmppSessionFactoryBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ReflectionUtils;

/**
 * Simple test, more of the SMPP API than anything, at the moment.
 * <p/>
 * Demonstrates that the {@link org.springframework.integration.smpp.session.SmppSessionFactoryBean} works, too.
 *
 * @author Josh Long
 * @since 1.0
 */
@ContextConfiguration("classpath:TestSmppSessionFactoryBean-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class TestSmppSessionFactoryBean {

	private Log logger = LogFactory.getLog(getClass());

	@Autowired
	@Qualifier("session")
	private ExtendedSmppSessionAdaptingDelegate smppSession;

	@Value("${smpp.host}")
	private String host;

	@Value("#{smppPort}")
	private int port;

	@Value("${smpp.systemId}")
	private String systemId;

	@Value("${smpp.password}")
	private String password;

	@Test
	public void testSmppSessionFactory() throws Throwable {

		SmppSessionFactoryBean smppSessionFactoryBean = new SmppSessionFactoryBean();
		smppSessionFactoryBean.setSystemId(this.systemId);
		smppSessionFactoryBean.setPort(this.port);
		smppSessionFactoryBean.setPassword(this.password);
		smppSessionFactoryBean.setHost(this.host);
		smppSessionFactoryBean.afterPropertiesSet();

		ExtendedSmppSession extendedSmppSession = smppSessionFactoryBean.getObject();
		Assert.assertTrue(extendedSmppSession instanceof ExtendedSmppSessionAdaptingDelegate);

		ExtendedSmppSessionAdaptingDelegate es = (ExtendedSmppSessionAdaptingDelegate) extendedSmppSession;
		Assert.assertNotNull("the factoried object should not be null", extendedSmppSession);
		es.addMessageReceiverListener(new MessageReceiverListener() {
			public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
				logger.debug("in onAcceptDeliverSm");
			}

			public void onAcceptAlertNotification(AlertNotification alertNotification) {
				logger.debug("in onAcceptAlertNotification");
			}

			public DataSmResult onAcceptDataSm(DataSm dataSm, Session source) throws ProcessRequestException {
				logger.debug("in onAcceptDataSm");
				return null;
			}
		});
		Assert.assertEquals(extendedSmppSession.getClass(), ExtendedSmppSessionAdaptingDelegate.class);
		Assert.assertNotNull(es.getTargetClientSession());
		Assert.assertTrue(es.getTargetClientSession() != null);
		final SMPPSession s = es.getTargetClientSession();

		ReflectionUtils.doWithFields(ExtendedSmppSessionAdaptingDelegate.class, new ReflectionUtils.FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				if (field.getName().equalsIgnoreCase("messageReceiverListener")) {
					field.setAccessible(true);
					MessageReceiverListener messageReceiverListener = (MessageReceiverListener) field.get(s);
					Assert.assertNotNull(messageReceiverListener);
					Assert.assertTrue(messageReceiverListener instanceof DelegatingMessageReceiverListener);
					final DelegatingMessageReceiverListener delegatingMessageReceiverListener = (DelegatingMessageReceiverListener) messageReceiverListener;
					ReflectionUtils.doWithFields(DelegatingMessageReceiverListener.class, new ReflectionUtils.FieldCallback() {
						public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
							if (field.getName().equals("messageReceiverListenerSet")) {
								field.setAccessible(true);
								@SuppressWarnings("unchecked")
								Set<MessageReceiverListener> l = (Set<MessageReceiverListener>) field.get(delegatingMessageReceiverListener);
								Assert.assertEquals(l.size(), 1);
							}
						}
					});
				}
			}
		});
	}

	@Test
	public void testWhetherTheBeansAlreadyStarted() throws Throwable {

		Assert.assertNotNull("session shouldn't be null", this.smppSession);

		Assert.assertTrue("the " + ExtendedSmppSession.class.getName() + " should be started if the " +
				"container supports Lifecycle, otherwise, it must be manually #start'd",
				(smppSession).isRunning());

		BindType bindType = smppSession.getBindType();

		Assert.assertNotNull("the bind type should not be null", bindType);
	}
}
