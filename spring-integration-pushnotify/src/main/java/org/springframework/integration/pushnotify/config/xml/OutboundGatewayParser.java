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
package org.springframework.integration.pushnotify.config.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.config.xml.AbstractConsumerEndpointParser;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.pushnotify.PushNotifyService;
import org.springframework.integration.pushnotify.gcm.GCMPushNotifyServiceImpl;
import org.springframework.integration.pushnotify.gcm.outbound.GCMOutboundGateway;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * The parser for the outbound-gateway element in int-pushnotify namespace
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class OutboundGatewayParser extends AbstractConsumerEndpointParser {


	private static final Log logger = LogFactory.getLog(OutboundGatewayParser.class);

	public static final String ADAPTER_TYPE = "type";
	private final Map<String, PushNotifyGatewayType> gatewayTypes;

	public OutboundGatewayParser() {
		super();
		gatewayTypes = new HashMap<String, OutboundGatewayParser.PushNotifyGatewayType>();
		//Just add in this map one instance for each type supported
		gatewayTypes.put("android",	new AndroidPushNotifyGatewayType());
	}

	@Override
	protected BeanDefinitionBuilder parseHandler(Element element,
			ParserContext parserContext) {
		String adapterType = element.getAttribute(ADAPTER_TYPE);
		if(!StringUtils.hasText(adapterType)) {
			parserContext
			.getReaderContext()
			.error("Attribute " + ADAPTER_TYPE + " is mandatory and needs to have a non empty value",
					adapterType);
		}

		PushNotifyGatewayType gatewayType = gatewayTypes.get(adapterType.toLowerCase());
		if(gatewayType == null) {
			parserContext
			.getReaderContext()
			.error("Type " + adapterType + " is not currently supported", element);
		}

		//check if all the given attributes are supported by this type, else flag a warning
		List<String> attributes = new ArrayList<String>();
		NamedNodeMap attributeMap = element.getAttributes();
		for(int i = 0 ; i < attributeMap.getLength(); i++) {
			attributes.add(attributeMap.item(i).getNodeName());
		}
		attributes.removeAll(gatewayType.getSupportedAttributes());
		if(attributes.size() > 0) {
			//unsupported attributes present
			StringBuilder message = new StringBuilder().append(attributes.get(0));
			for(int i = 1; i < attributes.size(); i++) {
				message.append(", ").append(attributes.get(i));
			}
			logger.warn("Attributes " + message +
					" are not supported by gateway of type " + gatewayType.getGatewayType());
		}
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(gatewayType.getHandler());
		gatewayType.setupGateway(builder, element, parserContext);
		return builder;
	}

	private abstract class PushNotifyGatewayType {

		private final String gatewayType;
		private final Class<? extends AbstractReplyProducingMessageHandler> handler;
		private final Class<? extends PushNotifyService> service;
		private final List<String> supportedAttributes = new ArrayList<String>();
		private final ExpressionParser parser = new SpelExpressionParser();

		public PushNotifyGatewayType(String gatewayType,
				Class<? extends AbstractReplyProducingMessageHandler> handler,
				Class<? extends PushNotifyService> service, String... supportedAttributes) {
			super();
			this.gatewayType = gatewayType;
			this.handler = handler;
			this.service = service;
			if(supportedAttributes != null && supportedAttributes.length > 0) {
				this.supportedAttributes.addAll(Arrays.asList(supportedAttributes));
			}
		}

		public String getGatewayType() {
			return gatewayType;
		}

		public Class<? extends AbstractReplyProducingMessageHandler> getHandler() {
			return handler;
		}

		public Class<? extends PushNotifyService> getService() {
			return service;
		}

		public List<String> getSupportedAttributes() {
			return new ArrayList<String>(supportedAttributes);
		}

		public abstract void setupGateway(BeanDefinitionBuilder builder, Element element, ParserContext parserContext);

		/**
		 * Helper method that does the below steps
		 *
		 * 1. If both are null, return null
		 * 2. If present, the literalValue and expressionValue are supposed to be
		 * 		mutually exclusive to each other. If not, its an error
		 * 3. If one of them is present, return an appropriate {@link Expression} instance
		 *
		 * @param element
		 * @param literalValueAttribute
		 * @param expressionValueAttribute
		 * @param context
		 *
		 * @return
		 */
		protected Expression getExpression(Element element, String literalValueAttribute,
							String expressionValueAttribute, ParserContext context) {
			Expression expression = null;
			String literalValue = element.getAttribute(literalValueAttribute);
			String expressionValue = element.getAttribute(expressionValueAttribute);
			boolean hasLiteralValue = StringUtils.hasText(literalValue);
			boolean hasExpressionValue = StringUtils.hasText(expressionValue);
			if(hasLiteralValue || hasExpressionValue) {
				if(hasLiteralValue && hasExpressionValue) {
					context.getReaderContext()
					.error(literalValueAttribute + " and " + expressionValueAttribute +
							" are mutually exclusive to each other", element);
				}
				if(hasLiteralValue) {
					expression = new LiteralExpression(literalValue);
				}
				else {
					expression = parser.parseExpression(expressionValue);
				}
			}
			return expression;
		}
	}

	private class AndroidPushNotifyGatewayType extends PushNotifyGatewayType {

		public AndroidPushNotifyGatewayType() {
			super("android",
					GCMOutboundGateway.class,
					GCMPushNotifyServiceImpl.class,
					"id"
					,"request-channel"
					,"reply-channel"
					,"type"
					,"service-ref"
					,"sender-id"
					,"sender-id-ref"
					,"collapse-key"
					,"collapse-key-expression"
					,"time-to-live"
					,"time-to-live-expression"
					,"delay-while-idle"
					,"delay-while-idle-expression"
					,"receiver-id"
					,"receiver-id-expression");
		}

		@Override
		public void setupGateway(BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
			String serviceRef = element.getAttribute("service-ref");
			String senderId = element.getAttribute("sender-id");
			String senderIdRef = element.getAttribute("sender-id-ref");
			boolean hasSenderId = StringUtils.hasText(senderId);
			boolean hasSenderIdRef = StringUtils.hasText(senderIdRef);

			if(StringUtils.hasText(serviceRef)) {
				if(hasSenderId || hasSenderIdRef) {
					parserContext.getReaderContext()
					.error("sender-id and sender-id-ref cannot be defined when service-ref is provided",
							element);
				}
				builder.addConstructorArgReference(serviceRef);
			}
			else {
				if(hasSenderId && hasSenderIdRef) {
					parserContext.getReaderContext()
					.error("sender-id and sender-id-ref are mutually exclusive to each other",
							element);
				}

				if(!(hasSenderId ^ hasSenderIdRef)) {
					parserContext.getReaderContext()
					.error("Atleast one of sender-id and sender-id-ref is required when service-ref is not provided",
							element);
				}

				BeanDefinitionBuilder service = BeanDefinitionBuilder.genericBeanDefinition(getService());
				if(hasSenderId) {
					service.addConstructorArgValue(senderId);
				}
				else {
					service.addConstructorArgReference(senderIdRef);
				}
				builder.addConstructorArgValue(service.getBeanDefinition());
			}

			//Collapse key
			Expression collapseKeyExpr =
				getExpression(element, "collapse-key", "collapse-key-expression", parserContext);
			if(collapseKeyExpr != null) {
				builder.addPropertyValue("collapseKeyExpression", collapseKeyExpr);
			}

			//Time to live
			Expression timeToLiveExpr =
				getExpression(element, "time-to-live", "time-to-live-expression", parserContext);
			if(timeToLiveExpr != null) {
				builder.addPropertyValue("timeToLiveExpression", timeToLiveExpr);
			}

			//Delay while idle expression
			Expression delayWhileIdleExpr =
				getExpression(element, "delay-while-idle", "delay-while-idle-expression", parserContext);
			if(delayWhileIdleExpr != null) {
				builder.addPropertyValue("delayWhileIdleExpression", delayWhileIdleExpr);
			}

			//Receiver id expression
			Expression receiverIdExpr =
				getExpression(element, "receiver-id", "receiver-id-expression", parserContext);
			if(receiverIdExpr != null) {
				builder.addPropertyValue("receiverIdsExpression", receiverIdExpr);
			}
		}
	}

	@Override
	protected String getInputChannelAttributeName() {
		return "request-channel";
	}
}
