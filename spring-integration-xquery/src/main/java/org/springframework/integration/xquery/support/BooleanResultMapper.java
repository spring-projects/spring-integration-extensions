/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.xquery.support;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultSequence;

import org.springframework.integration.MessagingException;
import org.w3c.dom.Node;

/**
 * The Result mapper implementation that maps the {@link XQResultSequence} to {@link Boolean}
 *
 * @author Amol Nayak
 *
 * @since 2.2
 *
 */
public class BooleanResultMapper extends AbstractXQueryResultMapper<Boolean> {

	public List<Boolean> mapResults(XQResultSequence result) {
		List<Boolean> results = new ArrayList<Boolean>();
		try {
			//check for boolean or string type and convert it accordingly, if a node then get it's text
			//content and convert to boolean
			while(result.next()) {
				XQItemType type = result.getItemType();
				Boolean value = convertToBoolean(type, result);
				if(value == null && isNodeType(type)) {
					Node n = result.getNode();
					value = Boolean.valueOf(transformNodeToString(n));
				}
				results.add(value);
			}
		} catch (Exception e) {
			throw new MessagingException("Caught Exception while mapping the result sequence to string",e);
		}
		return results;
	}

}
