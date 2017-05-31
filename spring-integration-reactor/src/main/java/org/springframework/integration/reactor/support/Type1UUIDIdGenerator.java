package org.springframework.integration.reactor.support;

import com.eaio.uuid.UUIDGen;
import org.springframework.integration.MessageHeaders;

import java.util.UUID;

/**
 * @author Jon Brisbin
 */
public class Type1UUIDIdGenerator implements MessageHeaders.IdGenerator {
	@Override
	public UUID generateId() {
		return new UUID(System.nanoTime(), UUIDGen.getClockSeqAndNode());
	}
}
