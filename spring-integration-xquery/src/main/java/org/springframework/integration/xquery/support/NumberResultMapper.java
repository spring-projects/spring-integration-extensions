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
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

/**
 * The Result mapper implementation that maps the {@link XQResultSequence} to {@link Number}
 *
 * @author Amol Nayak
 *
 * @since 2.2
 *
 */
public class NumberResultMapper extends AbstractXQueryResultMapper<Number> {

	public List<Number> mapResults(XQResultSequence result) {
		List<Number> results = new ArrayList<Number>();
		try {
			while(result.next()) {

				XQItemType type = result.getItemType();
				Number value = convertToNumber(type, result);
				if(value == null && isNodeType(type)) {
					Node n = result.getNode();
					String strValue = transformNodeToString(n);
					if(StringUtils.hasText(strValue)) {
						if(strValue.indexOf('.') > 0) {
							value = Double.valueOf(strValue);
						}
						else {
							value = Long.valueOf(strValue);
						}
					}
				}
				results.add(value);
			}
		} catch (Exception e) {
			throw new MessagingException("Caught Exception while mapping the result sequence to string",e);
		}
		return results;
	}

}