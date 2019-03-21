/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.xquery;

import javax.xml.transform.Source;

import org.springframework.integration.xml.XmlPayloadConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The Dummy Xml payload converter class that would be used for parser tests
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class DummyXmlPayloadConverter implements XmlPayloadConverter {

	/* (non-Javadoc)
	 * @see org.springframework.integration.xml.XmlPayloadConverter#convertToDocument(java.lang.Object)
	 */
	public Document convertToDocument(Object object) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.xml.XmlPayloadConverter#convertToNode(java.lang.Object)
	 */
	public Node convertToNode(Object object) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.xml.XmlPayloadConverter#convertToSource(java.lang.Object)
	 */
	public Source convertToSource(Object object) {
		return null;
	}

}
