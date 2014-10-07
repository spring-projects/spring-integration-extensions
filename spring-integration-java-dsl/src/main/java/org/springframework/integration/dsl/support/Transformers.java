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
import org.springframework.integration.dsl.support.tuple.Tuple2;
import org.springframework.integration.file.transformer.FileToByteArrayTransformer;
import org.springframework.integration.file.transformer.FileToStringTransformer;
import org.springframework.integration.json.JsonToObjectTransformer;
import org.springframework.integration.json.ObjectToJsonTransformer;
import org.springframework.integration.mail.transformer.MailToStringTransformer;
import org.springframework.integration.support.json.JsonObjectMapper;
import org.springframework.integration.transformer.MapToObjectTransformer;
import org.springframework.integration.transformer.ObjectToMapTransformer;
import org.springframework.integration.transformer.ObjectToStringTransformer;
import org.springframework.integration.transformer.PayloadDeserializingTransformer;
import org.springframework.integration.transformer.PayloadSerializingTransformer;
import org.springframework.integration.transformer.PayloadTypeConvertingTransformer;
import org.springframework.integration.transformer.SyslogToMapTransformer;
import org.springframework.integration.xml.result.ResultFactory;
import org.springframework.integration.xml.source.SourceFactory;
import org.springframework.integration.xml.transformer.MarshallingTransformer;
import org.springframework.integration.xml.transformer.ResultTransformer;
import org.springframework.integration.xml.transformer.SourceCreatingTransformer;
import org.springframework.integration.xml.transformer.UnmarshallingTransformer;
import org.springframework.integration.xml.transformer.XPathTransformer;
import org.springframework.integration.xml.transformer.XsltPayloadTransformer;
import org.springframework.integration.xml.xpath.XPathEvaluationType;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.xml.xpath.NodeMapper;

/**
 * @author Artem Bilan
 */
public abstract class Transformers {

	public static ObjectToStringTransformer objectToString() {
		return objectToString(null);
	}

	public static ObjectToStringTransformer objectToString(String charset) {
		return charset != null ? new ObjectToStringTransformer(charset) : new ObjectToStringTransformer();
	}

	public static ObjectToMapTransformer toMap() {
		return new ObjectToMapTransformer();
	}

	public static ObjectToMapTransformer toMap(boolean shouldFlattenKeys) {
		ObjectToMapTransformer transformer = new ObjectToMapTransformer();
		transformer.setShouldFlattenKeys(shouldFlattenKeys);
		return transformer;
	}

	public static MapToObjectTransformer fromMap(Class<?> targetClass) {
		return new MapToObjectTransformer(targetClass);
	}

	public static MapToObjectTransformer fromMap(String beanName) {
		return new MapToObjectTransformer(beanName);
	}

	public static ObjectToJsonTransformer toJson() {
		return toJson(null, null, null);
	}

	public static ObjectToJsonTransformer toJson(JsonObjectMapper<?, ?> jsonObjectMapper) {
		return toJson(jsonObjectMapper, null, null);
	}

	public static ObjectToJsonTransformer toJson(JsonObjectMapper<?, ?> jsonObjectMapper,
			ObjectToJsonTransformer.ResultType resultType) {
		return toJson(jsonObjectMapper, resultType, null);
	}

	public static ObjectToJsonTransformer toJson(String contentType) {
		return toJson(null, null, contentType);
	}

	public static ObjectToJsonTransformer toJson(JsonObjectMapper<?, ?> jsonObjectMapper, String contentType) {
		return toJson(jsonObjectMapper, null, contentType);
	}

	public static ObjectToJsonTransformer toJson(ObjectToJsonTransformer.ResultType resultType, String contentType) {
		return toJson(null, resultType, contentType);
	}

	public static ObjectToJsonTransformer toJson(JsonObjectMapper<?, ?> jsonObjectMapper,
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

	public static JsonToObjectTransformer fromJson() {
		return fromJson(null, null);
	}

	public static JsonToObjectTransformer fromJson(Class<?> targetClass) {
		return fromJson(targetClass, null);
	}

	public static JsonToObjectTransformer fromJson(JsonObjectMapper<?, ?> jsonObjectMapper) {
		return fromJson(null, jsonObjectMapper);
	}

	public static JsonToObjectTransformer fromJson(Class<?> targetClass, JsonObjectMapper<?, ?> jsonObjectMapper) {
		return new JsonToObjectTransformer(targetClass, jsonObjectMapper);
	}

	public static PayloadSerializingTransformer serializer() {
		return serializer(null);
	}

	public static PayloadSerializingTransformer serializer(Serializer<Object> serializer) {
		PayloadSerializingTransformer transformer = new PayloadSerializingTransformer();
		if (serializer != null) {
			transformer.setSerializer(serializer);
		}
		return transformer;
	}

	public static PayloadDeserializingTransformer deserializer() {
		return deserializer(null);
	}

	public static PayloadDeserializingTransformer deserializer(Deserializer<Object> deserializer) {
		PayloadDeserializingTransformer transformer = new PayloadDeserializingTransformer();
		if (deserializer != null) {
			transformer.setDeserializer(deserializer);
		}
		return transformer;
	}

	public static <T, U> PayloadTypeConvertingTransformer<T, U> converter(Converter<T, U> converter) {
		Assert.notNull(converter, "The Converter<?, ?> is required for the PayloadTypeConvertingTransformer");
		PayloadTypeConvertingTransformer<T, U> transformer = new PayloadTypeConvertingTransformer<T, U>();
		transformer.setConverter(converter);
		return transformer;
	}

	public static SyslogToMapTransformer syslogToMap() {
		return new SyslogToMapTransformer();
	}

	public static MailToStringTransformer fromMail() {
		return fromMail(null);
	}

	public static MailToStringTransformer fromMail(String charset) {
		MailToStringTransformer transformer = new MailToStringTransformer();
		if (charset != null) {
			transformer.setCharset(charset);
		}
		return transformer;
	}

	public static FileToStringTransformer fileToString() {
		return fileToString(null);
	}

	public static FileToStringTransformer fileToString(String charset) {
		FileToStringTransformer transformer = new FileToStringTransformer();
		if (charset != null) {
			transformer.setCharset(charset);
		}
		return transformer;
	}

	public static FileToByteArrayTransformer fileToByteArray() {
		return new FileToByteArrayTransformer();
	}

	public static MarshallingTransformer marshaller(Marshaller marshaller) {
		return marshaller(marshaller, null, null, null);
	}

	public static MarshallingTransformer marshaller(Marshaller marshaller, ResultTransformer resultTransformer) {
		return marshaller(marshaller, resultTransformer, null);
	}

	public static MarshallingTransformer marshaller(Marshaller marshaller, ResultFactory resultFactory) {
		return marshaller(marshaller, null, resultFactory);
	}

	public static MarshallingTransformer marshaller(Marshaller marshaller, boolean extractPayload) {
		return marshaller(marshaller, null, null, extractPayload);
	}

	public static MarshallingTransformer marshaller(Marshaller marshaller,ResultFactory resultFactory,
			boolean extractPayload) {
		return marshaller(marshaller, null, resultFactory, extractPayload);
	}

	public static MarshallingTransformer marshaller(Marshaller marshaller, ResultTransformer resultTransformer,
			boolean extractPayload) {
		return marshaller(marshaller, resultTransformer, null, extractPayload);
	}


	public static MarshallingTransformer marshaller(Marshaller marshaller, ResultTransformer resultTransformer,
			ResultFactory resultFactory) {
		return marshaller(marshaller, resultTransformer, resultFactory, null);
	}

	public static MarshallingTransformer marshaller(Marshaller marshaller, ResultTransformer resultTransformer,
			ResultFactory resultFactory, boolean extractPayload) {
		return marshaller(marshaller, resultTransformer, resultFactory, Boolean.valueOf(extractPayload));
	}

	private static MarshallingTransformer marshaller(Marshaller marshaller, ResultTransformer resultTransformer,
			ResultFactory resultFactory, Boolean extractPayload) {
		try {
			MarshallingTransformer transformer = new MarshallingTransformer(marshaller, resultTransformer);
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

	public static UnmarshallingTransformer unmarshaller(Unmarshaller unmarshaller) {
		return unmarshaller(unmarshaller, null);
	}

	public static UnmarshallingTransformer unmarshaller(Unmarshaller unmarshaller, SourceFactory sourceFactory) {
		return unmarshaller(unmarshaller, sourceFactory, false);
	}

	public static UnmarshallingTransformer unmarshaller(Unmarshaller unmarshaller,
			boolean alwaysUseSourceFactory) {
		return unmarshaller(unmarshaller, null, alwaysUseSourceFactory);
	}

	public static UnmarshallingTransformer unmarshaller(Unmarshaller unmarshaller, SourceFactory sourceFactory,
			boolean alwaysUseSourceFactory) {
		UnmarshallingTransformer transformer = new UnmarshallingTransformer(unmarshaller);
		if(sourceFactory != null) {
			transformer.setSourceFactory(sourceFactory);
		}
		transformer.setAlwaysUseSourceFactory(alwaysUseSourceFactory);

		return transformer;
	}

	public static SourceCreatingTransformer xmlSource() {
		return xmlSource(null);
	}

	public static SourceCreatingTransformer xmlSource(SourceFactory sourceFactory) {
		return sourceFactory != null ? new SourceCreatingTransformer(sourceFactory) : new SourceCreatingTransformer();
	}

	public static XPathTransformer xpath(String xpathExpression) {
		return xpath(xpathExpression, null, null);
	}

	public static XPathTransformer xpath(String xpathExpression, XPathEvaluationType xpathEvaluationType) {
		return xpath(xpathExpression, xpathEvaluationType, null);
	}

	public static XPathTransformer xpath(String xpathExpression, NodeMapper<?> nodeMapper) {
		return xpath(xpathExpression, null, nodeMapper);
	}

	public static XPathTransformer xpath(String xpathExpression, XPathEvaluationType xpathEvaluationType,
			NodeMapper<?> nodeMapper) {
		XPathTransformer transformer = new XPathTransformer(xpathExpression);
		if (xpathEvaluationType != null) {
			transformer.setEvaluationType(xpathEvaluationType);
		}
	    if (nodeMapper != null) {
			transformer.setNodeMapper(nodeMapper);
		}
		return transformer;
	}

	@SuppressWarnings("unchecked")
	public static XsltPayloadTransformer xslt(Resource xsltTemplate,
			Tuple2<String, Expression>... xslParameterMappings) {
		XsltPayloadTransformer transformer = new XsltPayloadTransformer(xsltTemplate);
		if (xslParameterMappings != null) {
			Map<String, Expression> params = new HashMap<String, Expression>(xslParameterMappings.length);
			for (Tuple2<String, Expression> mapping : xslParameterMappings) {
				params.put(mapping.getT1(), mapping.getT2());
			}
			transformer.setXslParameterMappings(params);
		}
		return transformer;
	}

}
