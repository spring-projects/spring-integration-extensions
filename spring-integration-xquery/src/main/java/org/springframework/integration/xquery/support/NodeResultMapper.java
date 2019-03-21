/*
 * Copyright 2002-2019 the original author or authors.
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
package org.springframework.integration.xquery.support;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultSequence;

import org.w3c.dom.Node;

import org.springframework.messaging.MessagingException;

/**
 * The Result mapper implementation that maps the {@link XQResultSequence} to {@link Node}
 *
 * @author Amol Nayak
 * @author Gary Russell
 *
 * @since 1.0
 *
 */
public class NodeResultMapper extends AbstractXQueryResultMapper<Node> {

	@Override
	public List<Node> mapResults(XQResultSequence result) {
		List<Node> results = new ArrayList<Node>();
		try {
			while(result.next()) {
				XQItemType type = result.getItemType();
				if(isNodeType(type)) {
					Node n = result.getNode();
					results.add(n);
				}
			}
		} catch (Exception e) {
			throw new MessagingException("Caught Exception while mapping the result sequence to string",e);
		}
		return results;
	}

}
