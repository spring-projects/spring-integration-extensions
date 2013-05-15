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

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.MapAssert.entry;

import java.util.HashMap;
import java.util.Map;

import junitparams.JUnitParamsRunner;
import static junitparams.JUnitParamsRunner.$;
import junitparams.Parameters;

import org.jgroups.Message;
import org.jgroups.Message.Flag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.integration.MessageHeaders;

@RunWith(JUnitParamsRunner.class)
public class DefaultJGroupsFlagHeaderMapperTest {

	@Test
	@Parameters(method = "headers")
	public void test_to_headers_mapping(Flag flag, String value) {
		DefaultJGroupsHeaderMapper headerMapper = new DefaultJGroupsHeaderMapper();

		Message source = new Message(null, null, null);

		source.setFlag(flag);

		Map<String, Object> headers = headerMapper.toHeaders(source);

		assertThat(headers).includes(entry(value, true));
	}

	@Test
	@Parameters(method = "headers")
	public void test_from_headers_mapping(Flag flag, String value) {
		DefaultJGroupsHeaderMapper headerMapper = new DefaultJGroupsHeaderMapper();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put(value, true);
		MessageHeaders headers = new MessageHeaders(map);

		Message target = new Message();
		headerMapper.fromHeaders(headers, target);

		assertThat(target.isFlagSet(flag));
	}

	public Object[] headers() {
		return $(
				$(Flag.OOB, "OOB"), 
				$(Flag.DONT_BUNDLE, "DONT_BUNDLE"),
				$(Flag.NO_FC, "NO_FC"), 
				$(Flag.NO_RELAY, "NO_RELAY"),
				$(Flag.NO_TOTAL_ORDER, "NO_TOTAL_ORDER"), 
				$(Flag.RSVP, "RSVP"),
				$(Flag.SCOPED, "SCOPED")
			);
	}

}
