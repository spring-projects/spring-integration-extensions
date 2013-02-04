/* Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.smpp.config.xml;

import org.jsmpp.bean.BindType;
import org.springframework.integration.smpp.session.ExtendedSmppSession;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Mock smpp session factory for testing.
 * @author Johanes Soetanto
 * @since 1.0
 */
public class MockSmppSessionFactory {

	public static ExtendedSmppSession getOutSmppSession() {
		ExtendedSmppSession mock = createNiceMock(ExtendedSmppSession.class);
		expect(mock.getBindType()).andReturn(BindType.BIND_TX).anyTimes();
		replay(mock);
		return mock;
	}

	public static ExtendedSmppSession getInSmppSession() {
		ExtendedSmppSession mock = createNiceMock(ExtendedSmppSession.class);
		expect(mock.getBindType()).andReturn(BindType.BIND_RX).anyTimes();
		replay(mock);
		return mock;
	}
}
