package org.springframework.integration.smpp.config.xml;

import org.jsmpp.bean.BindType;
import org.springframework.integration.smpp.session.ExtendedSmppSession;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Mock smpp session factory for testing.
 * @author Johanes Soetanto
 * @since 2.2
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
