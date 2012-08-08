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
 * The Result mapper implementation that maps the {@link XQResultSequence} to {@link String}
 *
 * @author Amol Nayak
 *
 * @since 2.2
 *
 */
public class StringResultMapper extends AbstractXQueryResultMapper<String> {

	public List<String> mapResults(XQResultSequence result) {
		List<String> results = new ArrayList<String>();
		try {
			while(result.next()) {
				XQItemType type = result.getItemType();
				String value = convertToString(type, result);
				if(value == null) {
					Number number = convertToNumber(type, result);
					if(number == null) {
						Boolean boolValue = convertToBoolean(type, result);
						if(boolValue == null) {
							if(isNodeType(type)) {
								Node n = result.getNode();
								value = transformNodeToString(n);
							}
						}
						else {
							value = boolValue.toString();
						}
					}
					else {
						value = number.toString();
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