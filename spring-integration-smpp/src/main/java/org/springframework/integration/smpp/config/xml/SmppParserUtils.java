/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.smpp.config.xml;

import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Contains various utility methods for parsing Smpp Adapter
 * specific namespace elements as well as for the generation of the the
 * respective {@link BeanDefinition}s.
 *
 * @author Johanes Soetanto
 * @since 1.0
 *
 */
public final class SmppParserUtils {

	/** Prevent instantiation. */
	private SmppParserUtils() {
		throw new AssertionError();
	}

	static void setSession(Element e, String sessionAttribute, String sessionChild, String propName,
						ParserContext context, BeanDefinitionBuilder builder) {
		final String sessionRef = e.getAttribute(sessionAttribute);
		final Element sessionElement = DomUtils.getChildElementByTagName(e, sessionChild);
		if(StringUtils.hasText(sessionRef)) {
			if(sessionElement != null) {
				context.getReaderContext().error("Child element '"+sessionChild+"' is not allowed when attribute '"
						+sessionAttribute+"' has been specified", context.extractSource(sessionElement));
			}
			builder.addPropertyReference(propName, sessionRef);
		}
		else if(sessionElement != null) {
			final String ref = sessionElement.getAttribute("ref");
			//
			BeanComponentDefinition innerBean = IntegrationNamespaceUtils
					.parseInnerHandlerDefinition(sessionElement, context);
			if (StringUtils.hasText(ref)) {
				builder.addPropertyReference(propName, ref);
			}
			else if (innerBean != null) {
				builder.addPropertyValue(propName, innerBean);
			}
		}
		else {
			context.getReaderContext().error("Child element '"+sessionChild+"' or '"+sessionAttribute+"' attribute "
					+ "need to be specified", context.extractSource(e));
		}
	}

	static void setTon(Element e, String tonAttribute, String propName, BeanDefinitionBuilder builder) {
		final String ton = e.getAttribute(tonAttribute);
		if (StringUtils.hasText(ton)) {
			final RootBeanDefinition tonDef = new RootBeanDefinition(TypeOfNumber.class);
			tonDef.setUniqueFactoryMethodName("valueOf");
			tonDef.getConstructorArgumentValues().addGenericArgumentValue(getByteTon(ton));
			builder.addPropertyValue(propName, tonDef);
		}
	}

	static void setNpi(Element e, String npiAttribute, String propName, BeanDefinitionBuilder builder) {
		final String npi = e.getAttribute(npiAttribute);
		if (StringUtils.hasText(npi)) {
			final RootBeanDefinition npiDef = new RootBeanDefinition(NumberingPlanIndicator.class);
			npiDef.setUniqueFactoryMethodName("valueOf");
			npiDef.getConstructorArgumentValues().addGenericArgumentValue(getByteNpi(npi));
			builder.addPropertyValue(propName, npiDef);
		}
	}

	static void setBindType(Element e, String bindAttribute, String propName, BeanDefinitionBuilder builder) {
		final String bt = e.getAttribute(bindAttribute);
		if (StringUtils.hasText(bt)) {
			final RootBeanDefinition bindTypeDef = new RootBeanDefinition(BindType.class);
			bindTypeDef.setUniqueFactoryMethodName("valueOf");
			bindTypeDef.getConstructorArgumentValues().addGenericArgumentValue(getByteBind(bt));
			builder.addPropertyValue(propName, bindTypeDef);
		}
	}

	private static byte getByteTon(String t) {
		if ("ABBREVIATED".equals(t)) {
			return SMPPConstant.TON_ABBREVIATED;
		}
		if ("ALPHANUMERIC".equals(t)) {
			return SMPPConstant.TON_ALPHANUMERIC;
		}
		if ("SUBSCRIBER_NUMBER".equals(t)) {
			return SMPPConstant.TON_SUBSCRIBER_NUMBER;
		}
		if ("NETWORK_SPECIFIC".equals(t)) {
			return SMPPConstant.TON_NETWORK_SPECIFIC;
		}
		if ("NATIONAL".equals(t)) {
			return SMPPConstant.TON_NATIONAL;
		}
		if ("INTERNATIONAL".equals(t)) {
			return SMPPConstant.TON_INTERNATIONAL;
		}
		return SMPPConstant.TON_UNKNOWN;
	}

	private static byte getByteNpi(String n) {
		if ("WAP".equals(n)) {
			return SMPPConstant.NPI_WAP;
		}
		if ("INTERNET".equals(n)) {
			return SMPPConstant.NPI_INTERNET;
		}
		if ("ERMES".equals(n)) {
			return SMPPConstant.NPI_ERMES;
		}
		if ("PRIVATE".equals(n)) {
			return SMPPConstant.NPI_PRIVATE;
		}
		if ("NATIONAL".equals(n)) {
			return SMPPConstant.NPI_NATIONAL;
		}
		if ("LAND_MOBILE".equals(n)) {
			return SMPPConstant.NPI_LAND_MOBILE;
		}
		if ("TELEX".equals(n)) {
			return SMPPConstant.NPI_TELEX;
		}
		if ("DATA".equals(n)) {
			return SMPPConstant.NPI_DATA;
		}
		if ("ISDN".equals(n)) {
			return SMPPConstant.NPI_ISDN;
		}
		return SMPPConstant.NPI_UNKNOWN;
	}

	private static byte getByteBind(String b) {
		if ("BIND_RX".equals(b)) {
			return SMPPConstant.CID_BIND_RECEIVER;
		}
		if ("BIND_TX".equals(b)) {
			return SMPPConstant.CID_BIND_TRANSMITTER;
		}
		return SMPPConstant.CID_BIND_TRANSCEIVER;
	}

}
