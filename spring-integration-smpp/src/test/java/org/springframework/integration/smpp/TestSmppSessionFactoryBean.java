package org.springframework.integration.smpp;

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
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.smpp.session.DelegatingMessageReceiverListener;
import org.springframework.integration.smpp.session.ExtendedSmppSession;
import org.springframework.integration.smpp.session.ExtendedSmppSessionAdaptingDelegate;
import org.springframework.integration.smpp.session.SmppSessionFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Simple test, more of the SMPP API than anything, at the moment.
 * <p/>
 * Demonstrates that the {@link org.springframework.integration.smpp.session.SmppSessionFactoryBean} works, too.
 *
 * @author Josh Long
 * @since 2.1
 */
@ContextConfiguration("classpath:TestSmppSessionFactoryBean-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSmppSessionFactoryBean {

	private Log logger = LogFactory.getLog(getClass());

	@Autowired
	@Qualifier("session")
	private ExtendedSmppSessionAdaptingDelegate smppSession;

	private AbsoluteTimeFormatter timeFormatter = new AbsoluteTimeFormatter();

	private String host = "127.0.0.1";

	private int port = 2775;

	private String systemId = "smppclient1";

	private String password = "password";

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
