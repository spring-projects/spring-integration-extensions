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

import static javax.xml.xquery.XQItemType.XQBASETYPE_BOOLEAN;
import static javax.xml.xquery.XQItemType.XQBASETYPE_DECIMAL;
import static javax.xml.xquery.XQItemType.XQBASETYPE_DOUBLE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_FLOAT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_INT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_LONG;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NEGATIVE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NONNEGATIVE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NONPOSITIVE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_POSITIVE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_SHORT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_STRING;
import static javax.xml.xquery.XQItemType.XQBASETYPE_UNSIGNED_INT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_UNSIGNED_LONG;
import static javax.xml.xquery.XQItemType.XQBASETYPE_UNSIGNED_SHORT;
import static javax.xml.xquery.XQItemType.XQITEMKIND_ATTRIBUTE;
import static javax.xml.xquery.XQItemType.XQITEMKIND_COMMENT;
import static javax.xml.xquery.XQItemType.XQITEMKIND_DOCUMENT;
import static javax.xml.xquery.XQItemType.XQITEMKIND_DOCUMENT_ELEMENT;
import static javax.xml.xquery.XQItemType.XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT;
import static javax.xml.xquery.XQItemType.XQITEMKIND_ELEMENT;
import static javax.xml.xquery.XQItemType.XQITEMKIND_NODE;
import static javax.xml.xquery.XQItemType.XQITEMKIND_PI;
import static javax.xml.xquery.XQItemType.XQITEMKIND_SCHEMA_ATTRIBUTE;
import static javax.xml.xquery.XQItemType.XQITEMKIND_SCHEMA_ELEMENT;
import static javax.xml.xquery.XQItemType.XQITEMKIND_TEXT;

import java.io.StringWriter;
import java.math.BigInteger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultSequence;

import org.springframework.integration.xquery.support.XQueryResultMapper;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * The absract base class for {@link XQueryResultMapper} implementations
 * @author Amol Nayak
 * @since 2.2
 *
 */
public abstract class AbstractXQueryResultMapper<T> implements XQueryResultMapper<T> {

	protected volatile boolean formatOutput;
	/**
	 * The getBaseType method throws an exception if the item kind is of some specific types
	 * This method will be used to check if the getBaseType method can be invoked or not
	 * on the given element
	 *
	 *
	 * @param type
	 * @return
	 */
	protected boolean shouldSkipBaseType(XQItemType type) {
		return isNodeType(type);
	}

	/**
	 * The method checks if the given type is a numeric field and returns an appropriate implementation
	 * of the {@link Number}
	 *
	 * @param type
	 * @param result
	 * @return the appropriate {@link Number} implementation or null if cannot be converted to number
	 */
	protected Number convertToNumber(XQItemType type,XQResultSequence result) throws XQException {
		boolean skipBaseTypes = shouldSkipBaseType(type);
		Number value = null;
		if(!skipBaseTypes) {
			int baseType = type.getBaseType();
			if(baseType == XQBASETYPE_DOUBLE) {
				value = Double.valueOf(result.getDouble());

			}
			else if(baseType == XQBASETYPE_FLOAT) {
				value = Float.valueOf(result.getFloat());
			}
			else if(baseType == XQBASETYPE_DECIMAL) {
				value = Double.valueOf(result.getAtomicValue());
				//TODO: Should DECIMAL be BigDecimal?
			}

			else if(baseType == XQBASETYPE_INT
					|| baseType == XQBASETYPE_NEGATIVE_INTEGER
					|| baseType == XQBASETYPE_POSITIVE_INTEGER
					|| baseType == XQBASETYPE_NONNEGATIVE_INTEGER
					|| baseType == XQBASETYPE_NONPOSITIVE_INTEGER
					|| baseType == XQBASETYPE_UNSIGNED_INT) {
				value = Integer.valueOf(result.getInt());
			}
			else if(baseType == XQBASETYPE_INTEGER) {
				value = BigInteger.valueOf(result.getInt());
			}
			else if(baseType == XQBASETYPE_LONG
					|| baseType == XQBASETYPE_UNSIGNED_LONG) {
				value = Long.valueOf(result.getLong());
			}
			else if(baseType == XQBASETYPE_SHORT
					|| baseType == XQBASETYPE_UNSIGNED_SHORT) {
				value = Short.valueOf(result.getShort());
			}
			else if(baseType == XQBASETYPE_STRING) {
				String strValue = result.getAtomicValue();
				value = convertStringToNumber(strValue);
			}
		}
		else if(XQITEMKIND_TEXT == type.getItemKind()) {
			String textContent = result.getNode().getTextContent();
			value = convertStringToNumber(textContent);
		}
		else if(XQITEMKIND_ATTRIBUTE == type.getItemKind()) {
			String textContent = ((Attr)result.getNode()).getValue();
			value = convertStringToNumber(textContent);
		}

		return value;
	}

	/**Helper method that will be used to convert a String value to Number
	 * @param value
	 * @param strValue
	 * @return
	 */
	private Number convertStringToNumber(String strValue) {
		Number value = null;
		try {
			if(StringUtils.hasText(strValue)) {
				if(strValue.indexOf(".") > 0) {
					value = Double.valueOf(strValue);
				}
				else {
					value = Long.valueOf(strValue);
				}
			}
		} catch(NumberFormatException ne) {
			value = null;
		}
		return value;
	}

	/**
	 * Gets the given text content as String if the type is a string base type
	 * @param type
	 * @param result
	 * @return
	 */
	protected String convertToString(XQItemType type,XQResultSequence result) throws XQException {
		boolean skipBaseTypes = shouldSkipBaseType(type);
		String value = null;
		if(!skipBaseTypes) {
			int baseType = type.getBaseType();
			if(baseType == XQBASETYPE_STRING) {
				value = result.getAtomicValue();
			}
		}
		else if(XQITEMKIND_TEXT == type.getItemKind()) {
			value = result.getNode().getTextContent();
		}
		else if(XQITEMKIND_ATTRIBUTE == type.getItemKind()) {
			value = ((Attr)result.getNode()).getValue();
		}
		return value;
	}

	/**
	 * Checks the data type of the string and converts to boolean if applicable.
	 * The types have to be either boolean or string. For a string value,
	 * the returned value is same as Boolean.valueOf(strValue)
	 *
	 * @param type
	 * @param result
	 * @return
	 */
	protected Boolean convertToBoolean(XQItemType type,XQResultSequence result) throws XQException {
		boolean skipBaseTypes = shouldSkipBaseType(type);
		Boolean value = null;
		if(!skipBaseTypes) {
			if(type.getBaseType() == XQBASETYPE_BOOLEAN) {
				value = Boolean.valueOf(result.getBoolean());
			}
			else if(type.getBaseType() == XQBASETYPE_STRING) {
				value = Boolean.valueOf(result.getAtomicValue());
			}
		}
		else if(XQITEMKIND_TEXT == type.getItemKind()) {
			String textContent = result.getNode().getTextContent();
			value = Boolean.valueOf(textContent);
		}
		else if(XQITEMKIND_ATTRIBUTE == type.getItemKind()) {
			String textContent = ((Attr)result.getNode()).getValue();
			value = Boolean.valueOf(textContent);
		}
		return value;
	}

	/**
	 * Returns true if the given type if for a valid node type
	 * @param type
	 * @return
	 */
	protected boolean isNodeType(XQItemType type) {
		int itemKind = type.getItemKind();
		return (
				itemKind == XQITEMKIND_ATTRIBUTE
				||
				itemKind == XQITEMKIND_COMMENT
				||
				itemKind == XQITEMKIND_DOCUMENT
				||
				itemKind == XQITEMKIND_DOCUMENT_ELEMENT
				||
				itemKind == XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT
				||
				itemKind == XQITEMKIND_ELEMENT
				||
				itemKind == XQITEMKIND_NODE
				||
				itemKind == XQITEMKIND_PI
				||
				itemKind == XQITEMKIND_SCHEMA_ATTRIBUTE
				||
				itemKind == XQITEMKIND_SCHEMA_ELEMENT
				||
				itemKind == XQITEMKIND_TEXT
		);
	}

	/**Transforms the given {@link Node} to a String
	 * @param n
	 * @return
	 * @throws TransformerConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	protected String transformNodeToString(Node n)
			throws TransformerConfigurationException,
			TransformerFactoryConfigurationError, TransformerException {
		String value;
		StringWriter writer = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		if(formatOutput)
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform( new DOMSource(n), new StreamResult(writer));
		value = writer.toString();
		return value;
	}

	/**
	 * If the output result is an xml, the value of this parameter will determine
	 * if the output xml is to be formatted or not. By default, the output will
	 * not be formatted
	 *
	 * @param formatOutput
	 */
	public void setFormatOutput(boolean formatOutput) {
		this.formatOutput = formatOutput;
	}

}
