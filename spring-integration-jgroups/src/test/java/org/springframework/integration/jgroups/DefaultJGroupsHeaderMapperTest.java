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

import java.net.Inet4Address;
import java.util.Map;

import org.jgroups.Message;
import org.jgroups.stack.IpAddress;
import org.junit.Test;

/**
 * 
 * @author Jaroslaw Palka <jaroslaw.palka@symentis.pl>
 * @since 1.0.0
 *
 */
public class DefaultJGroupsHeaderMapperTest {

	@Test
	public void should_map_to_src_address() throws Exception {
		DefaultJGroupsHeaderMapper headerMapper = new DefaultJGroupsHeaderMapper();

		IpAddress src = new IpAddress(Inet4Address.getByName("192.168.0.1"),
				6666);

		Message source = new Message(null, src, null);

		Map<String, Object> headers = headerMapper.toHeaders(source);

		assertThat(headers).includes(entry("src", src));
	}

	@Test
	public void should_map_to_dest_address() throws Exception {
		DefaultJGroupsHeaderMapper headerMapper = new DefaultJGroupsHeaderMapper();

		IpAddress dest = new IpAddress(Inet4Address.getByName("192.168.0.1"),
				6666);

		Message source = new Message(dest, null, null);

		Map<String, Object> headers = headerMapper.toHeaders(source);

		assertThat(headers).includes(entry("dest", dest));
	}


}
