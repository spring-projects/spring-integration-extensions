/**
 * Copyright 2013 Jaroslaw Palka<jaroslaw.palka@symentis.pl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.jgroups;

import java.util.HashMap;
import java.util.Map;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.Message.Flag;
import org.springframework.integration.MessageHeaders;

/**
 * 
 * @author Jaroslaw Palka <jaroslaw.palka@symentis.pl>
 * @since 1.0.0
 *
 */
public class DefaultJGroupsHeaderMapper implements JGroupsHeaderMapper {

	private static final String SCOPED_HEADER = "SCOPED";
	private static final String RSVP_HEADER = "RSVP";
	private static final String NO_TOTAL_ORDER_HEADER = "NO_TOTAL_ORDER";
	private static final String NO_RELIABILITY_HEADER = "NO_RELIABILITY";
	private static final String NO_RELAY_HEADER = "NO_RELAY";
	private static final String NO_FC_HEADER = "NO_FC";
	private static final String DONT_BUNDLE_HEADER = "DONT_BUNDLE";
	private static final String OOB_HEADER = "OOB";

	public void fromHeaders(MessageHeaders headers, Message target) {

		if (getHeaderValueAsBoolean(headers, OOB_HEADER)) {
			target.setFlag(Flag.OOB);
		}

		if (getHeaderValueAsBoolean(headers, DONT_BUNDLE_HEADER)) {
			target.setFlag(Flag.DONT_BUNDLE);
		}

		if (getHeaderValueAsBoolean(headers, NO_FC_HEADER)) {
			target.setFlag(Flag.NO_FC);
		}

		if (getHeaderValueAsBoolean(headers, NO_RELAY_HEADER)) {
			target.setFlag(Flag.NO_RELAY);
		}

		if (getHeaderValueAsBoolean(headers, NO_RELIABILITY_HEADER)) {
			target.setFlag(Flag.NO_RELIABILITY);
		}

		if (getHeaderValueAsBoolean(headers, NO_TOTAL_ORDER_HEADER)) {
			target.setFlag(Flag.OOB);
		}

		if (getHeaderValueAsBoolean(headers, RSVP_HEADER)) {
			target.setFlag(Flag.RSVP);
		}

		if (getHeaderValueAsBoolean(headers, SCOPED_HEADER)) {
			target.setFlag(Flag.SCOPED);
		}

	}

	public Map<String, Object> toHeaders(Message source) {

		HashMap<String, Object> map = new HashMap<String, Object>();

		Address src = source.getSrc();
		if (src != null) {
			map.put("src", src);
		}

		Address dest = source.getDest();
		if (dest != null) {
			map.put("dest", dest);
		}

		map.put(OOB_HEADER, source.isFlagSet(Flag.OOB));
		map.put(DONT_BUNDLE_HEADER, source.isFlagSet(Flag.DONT_BUNDLE));
		map.put(NO_FC_HEADER, source.isFlagSet(Flag.NO_FC));
		map.put(NO_RELAY_HEADER, source.isFlagSet(Flag.NO_RELAY));
		map.put(NO_RELIABILITY_HEADER, source.isFlagSet(Flag.NO_RELIABILITY));
		map.put(NO_TOTAL_ORDER_HEADER, source.isFlagSet(Flag.NO_TOTAL_ORDER));
		map.put(RSVP_HEADER, source.isFlagSet(Flag.RSVP));
		map.put(SCOPED_HEADER, source.isFlagSet(Flag.SCOPED));

		return map;
	}

	private static Boolean getHeaderValueAsBoolean(MessageHeaders headers, String headerName) {
		Boolean headerValue = headers.get(headerName, Boolean.class);
		if (headerValue == null) {
			headerValue = false;
		}
		return headerValue;
	}
}
