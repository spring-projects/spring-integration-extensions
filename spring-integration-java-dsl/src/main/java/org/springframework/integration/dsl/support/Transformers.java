/*
 * Copyright 2014 the original author or authors.
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

package org.springframework.integration.dsl.support;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.Resource;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.dsl.tuple.Tuple2;
import org.springframework.integration.json.JsonToObjectTransformer;
import org.springframework.integration.json.ObjectToJsonTransformer;
import org.springframework.integration.support.json.JsonObjectMapper;
import org.springframework.integration.transformer.MapToObjectTransformer;
import org.springframework.integration.transformer.ObjectToMapTransformer;
import org.springframework.integration.transformer.ObjectToStringTransformer;
import org.springframework.integration.transformer.PayloadDeserializingTransformer;
import org.springframework.integration.transformer.PayloadSerializingTransformer;
import org.springframework.integration.transformer.PayloadTypeConvertingTransformer;
import org.springframework.integration.transformer.SyslogToMapTransformer;
import org.springframework.integration.transformer.Transformer;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public abstract class Transformers {

	private final static SpelExpressionParser PARSER = new SpelExpressionParser();

	public static Transformer objectToString() {
		return objectToString(null);
	}

	public static Transformer objectToString(String charset) {
		return charset != null ? new ObjectToStringTransformer(charset) : new ObjectToStringTransformer();
	}

	public static Transformer toMap() {
		return new ObjectToMapTransformer();
	}

	public static Transformer toMap(boolean shouldFlattenKeys) {
		ObjectToMapTransformer transformer = new ObjectToMapTransformer();
		transformer.setShouldFlattenKeys(shouldFlattenKeys);
		return transformer;
	}

	public static Transformer fromMap(Class<?> targetClass) {
		return new MapToObjectTransformer(targetClass);
	}

	public static Transformer fromMap(String beanName) {
		return new MapToObjectTransformer(beanName);
	}

	public static Transformer toJson() {
		return toJson(null, null, null);
	}

	public static Transformer toJson(JsonObjectMapper<?, ?> jsonObjectMapper) {
		return toJson(jsonObjectMapper, null, null);
	}

	public static Transformer toJson(JsonObjectMapper<?, ?> jsonObjectMapper,
			ObjectToJsonTransformer.ResultType resultType) {
		return toJson(jsonObjectMapper, resultType, null);
	}

	public static Transformer toJson(String contentType) {
		return toJson(null, null, contentType);
	}

	public static Transformer toJson(JsonObjectMapper<?, ?> jsonObjectMapper, String contentType) {
		return toJson(jsonObjectMapper, null, contentType);
	}

	public static Transformer toJson(ObjectToJsonTransformer.ResultType resultType, String contentType) {
		return toJson(null, resultType, contentType);
	}

	public static Transformer toJson(JsonObjectMapper<?, ?> jsonObjectMapper,
			ObjectToJsonTransformer.ResultType resultType, String contentType) {
		ObjectToJsonTransformer transformer;
		if (jsonObjectMapper != null) {
			if (resultType != null) {
				transformer = new ObjectToJsonTransformer(jsonObjectMapper, resultType);
			}
			else {
				transformer = new ObjectToJsonTransformer(jsonObjectMapper);
			}
		}
		else if (resultType != null) {
			transformer = new ObjectToJsonTransformer(resultType);
		}
		else {
			transformer = new ObjectToJsonTransformer();
		}
		if (contentType != null) {
			transformer.setContentType(contentType);
		}
		return transformer;
	}

	public static Transformer fromJson() {
		return fromJson(null, null);
	}

	public static Transformer fromJson(Class<?> targetClass) {
		return fromJson(targetClass, null);
	}

	public static Transformer fromJson(JsonObjectMapper<?, ?> jsonObjectMapper) {
		return fromJson(null, jsonObjectMapper);
	}

	public static Transformer fromJson(Class<?> targetClass, JsonObjectMapper<?, ?> jsonObjectMapper) {
		return new JsonToObjectTransformer(targetClass, jsonObjectMapper);
	}

	public static Transformer serializer() {
		return serializer(null);
	}

	public static Transformer serializer(Serializer<Object> serializer) {
		PayloadSerializingTransformer transformer = new PayloadSerializingTransformer();
		if (serializer != null) {
			transformer.setSerializer(serializer);
		}
		return transformer;
	}

	public static Transformer deserializer() {
		return deserializer(null);
	}

	public static Transformer deserializer(Deserializer<Object> deserializer) {
		PayloadDeserializingTransformer transformer = new PayloadDeserializingTransformer();
		if (deserializer != null) {
			transformer.setDeserializer(deserializer);
		}
		return transformer;
	}

	public static <T, U> Transformer converter(Converter<T, U> converter) {
		Assert.notNull(converter, "The Converter<?, ?> is required for the PayloadTypeConvertingTransformer");
		PayloadTypeConvertingTransformer<T, U> transformer = new PayloadTypeConvertingTransformer<T, U>();
		transformer.setConverter(converter);
		return transformer;
	}

	public static Transformer syslogToMap() {
		return new SyslogToMapTransformer();
	}

	public static Transformer fromMail() {
		return fromMail(null);
	}

	public static Transformer fromMail(String charset) {
		org.springframework.integration.mail.transformer.MailToStringTransformer transformer =
				new org.springframework.integration.mail.transformer.MailToStringTransformer();
		if (charset != null) {
			transformer.setCharset(charset);
		}
		return transformer;
	}

	public static Transformer fileToString() {
		return fileToString(null);
	}

	public static Transformer fileToString(String charset) {
		org.springframework.integration.file.transformer.FileToStringTransformer transformer =
				new org.springframework.integration.file.transformer.FileToStringTransformer();
		if (charset != null) {
			transformer.setCharset(charset);
		}
		return transformer;
	}

	public static Transformer fileToByteArray() {
		return new org.springframework.integration.file.transformer.FileToByteArrayTransformer();
	}

	public static Transformer marshaller(org.springframework.oxm.Marshaller marshaller) {
		return marshaller(marshaller, null, null, null);
	}

	public static Transformer marshaller(org.springframework.oxm.Marshaller marshaller,
			org.springframework.integration.xml.transformer.ResultTransformer resultTransformer) {
		return marshaller(marshaller, resultTransformer, null);
	}

	public static Transformer marshaller(org.springframework.oxm.Marshaller marshaller,
			org.springframework.integration.xml.result.ResultFactory resultFactory) {
		return marshaller(marshaller, null, resultFactory);
	}

	public static Transformer marshaller(org.springframework.oxm.Marshaller marshaller, boolean extractPayload) {
		return marshaller(marshaller, null, null, extractPayload);
	}

	public static Transformer marshaller(org.springframework.oxm.Marshaller marshaller,
			org.springframework.integration.xml.result.ResultFactory resultFactory,
			boolean extractPayload) {
		return marshaller(marshaller, null, resultFactory, extractPayload);
	}

	public static Transformer marshaller(org.springframework.oxm.Marshaller marshaller,
			org.springframework.integration.xml.transformer.ResultTransformer resultTransformer,
			boolean extractPayload) {
		return marshaller(marshaller, resultTransformer, null, extractPayload);
	}


	public static Transformer marshaller(org.springframework.oxm.Marshaller marshaller,
			org.springframework.integration.xml.transformer.ResultTransformer resultTransformer,
			org.springframework.integration.xml.result.ResultFactory resultFactory) {
		return marshaller(marshaller, resultTransformer, resultFactory, null);
	}

	public static Transformer marshaller(org.springframework.oxm.Marshaller marshaller,
			org.springframework.integration.xml.transformer.ResultTransformer resultTransformer,
			org.springframework.integration.xml.result.ResultFactory resultFactory,
			boolean extractPayload) {
		return marshaller(marshaller, resultTransformer, resultFactory, Boolean.valueOf(extractPayload));
	}

	private static Transformer marshaller(org.springframework.oxm.Marshaller marshaller,
			org.springframework.integration.xml.transformer.ResultTransformer resultTransformer,
			org.springframework.integration.xml.result.ResultFactory resultFactory,
			Boolean extractPayload) {
		try {
			org.springframework.integration.xml.transformer.MarshallingTransformer transformer =
					new org.springframework.integration.xml.transformer.MarshallingTransformer(marshaller, resultTransformer);
			if (resultFactory != null) {
				transformer.setResultFactory(resultFactory);
			}
			if (extractPayload != null) {
				transformer.setExtractPayload(extractPayload);
			}
			return transformer;
		}
		catch (ParserConfigurationException e) {
			throw new IllegalStateException(e);
		}
	}

	public static Transformer unmarshaller(org.springframework.oxm.Unmarshaller unmarshaller) {
		return unmarshaller(unmarshaller, null);
	}

	public static Transformer unmarshaller(org.springframework.oxm.Unmarshaller unmarshaller,
			org.springframework.integration.xml.source.SourceFactory sourceFactory) {
		return unmarshaller(unmarshaller, sourceFactory, false);
	}

	public static Transformer unmarshaller(org.springframework.oxm.Unmarshaller unmarshaller,
			boolean alwaysUseSourceFactory) {
		return unmarshaller(unmarshaller, null, alwaysUseSourceFactory);
	}

	public static Transformer unmarshaller(org.springframework.oxm.Unmarshaller unmarshaller,
			org.springframework.integration.xml.source.SourceFactory sourceFactory,
			boolean alwaysUseSourceFactory) {
		org.springframework.integration.xml.transformer.UnmarshallingTransformer transformer =
				new org.springframework.integration.xml.transformer.UnmarshallingTransformer(unmarshaller);
		if(sourceFactory != null) {
			transformer.setSourceFactory(sourceFactory);
		}
		transformer.setAlwaysUseSourceFactory(alwaysUseSourceFactory);

		return transformer;
	}

	public static Transformer xmlSource() {
		return xmlSource(null);
	}

	public static Transformer xmlSource(org.springframework.integration.xml.source.SourceFactory sourceFactory) {
		if (sourceFactory != null) {
			return new org.springframework.integration.xml.transformer.SourceCreatingTransformer(sourceFactory);
		}
		else {
			return new org.springframework.integration.xml.transformer.SourceCreatingTransformer();
		}
	}

	public static Transformer xpath(String xpathExpression) {
		return xpath(xpathExpression, null, null);
	}

	public static Transformer xpath(String xpathExpression,
			org.springframework.integration.xml.xpath.XPathEvaluationType xpathEvaluationType) {
		return xpath(xpathExpression, xpathEvaluationType, null);
	}

	public static Transformer xpath(String xpathExpression,	org.springframework.xml.xpath.NodeMapper<?> nodeMapper) {
		return xpath(xpathExpression, null, nodeMapper);
	}

	public static Transformer xpath(String xpathExpression,
			org.springframework.integration.xml.xpath.XPathEvaluationType xpathEvaluationType,
			org.springframework.xml.xpath.NodeMapper<?> nodeMapper) {
		org.springframework.integration.xml.transformer.XPathTransformer transformer =
				new org.springframework.integration.xml.transformer.XPathTransformer(xpathExpression);
		if (xpathEvaluationType != null) {
			transformer.setEvaluationType(xpathEvaluationType);
		}
	    if (nodeMapper != null) {
			transformer.setNodeMapper(nodeMapper);
		}
		return transformer;
	}

	public static Transformer xslt(Resource xsltTemplate, Tuple2<String, String>... xslParameterMappings) {
		org.springframework.integration.xml.transformer.XsltPayloadTransformer transformer =
				new org.springframework.integration.xml.transformer.XsltPayloadTransformer(xsltTemplate);
		if (xslParameterMappings != null) {
			Map<String, Expression> params = new HashMap<String, Expression>(xslParameterMappings.length);
			for (Tuple2<String, String> mapping : xslParameterMappings) {
				params.put(mapping.getT1(), PARSER.parseExpression(mapping.getT2()));
			}
			transformer.setXslParameterMappings(params);
		}
		return transformer;
	}

}
