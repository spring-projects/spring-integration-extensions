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
package org.springframework.integration.xquery.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.xml.DefaultXmlPayloadConverter;
import org.springframework.integration.xml.XmlPayloadConverter;
import org.springframework.integration.xquery.support.AbstractXQueryResultMapper;
import org.springframework.integration.xquery.support.BooleanResultMapper;
import org.springframework.integration.xquery.support.NodeResultMapper;
import org.springframework.integration.xquery.support.NumberResultMapper;
import org.springframework.integration.xquery.support.StringResultMapper;
import org.springframework.integration.xquery.support.XQueryParameter;
import org.springframework.integration.xquery.support.XQueryResultMapper;
import org.springframework.integration.xquery.support.XQueryUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

/**
 * The common logic for performing the common xquery operations would reside in this
 * implementation.
 * The class would be instantiated with the xquery to be executed and the parameters
 * that would be used. It would return the result of the execution with the
 * {@link Message} instance passed to one of its execute methods.
 * Currently doesn't support advanced mapping techniques of mapping the resulting Node(s)
 * to a custom object type
 *
 * @author Amol Nayak
 *
 * @since 2.2
 *
 */
public class XQueryExecutor implements InitializingBean,BeanClassLoaderAware {


	private static final String SAXON_XQ_DATASOURCE_CLASS = "net.sf.saxon.xqj.SaxonXQDataSource";

	private final Log logger = LogFactory.getLog(XQueryExecutor.class);

	/**
	 * The payload converter
	 */
	private XmlPayloadConverter converter = new DefaultXmlPayloadConverter();

	 @SuppressWarnings("rawtypes")
	private volatile  Map resultMappers;

	private volatile boolean formatOutput;

	/**
	 * The xQuery that this Executor will execute
	 */
	private String xQuery;

	private XQDataSource xqDataSource;

	private Map<String, XQueryParameter> xQueryParameterMap;

	//maintained internally and used for setting the values
	private List<String> xQueryParameters;

	//The resource to the XQuery's .xq file
	private Resource xQueryFileResource;

	private String userName;

	private String password;

	private ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

	//TODO: Can we have a static xml resource which will be used always to execute the
	//given XQuery as against the one sent in the payload. The default is to use the one in the
	//payload unless one for static xml is provided

	@Override
	public void afterPropertiesSet() {
		if(resultMappers == null) {
			resultMappers = new HashMap<Class<?>,XQueryResultMapper<?>>();
		}
		addDefaultMappers();

		if(xqDataSource == null) {
			xqDataSource = discoverXQDataSource();//default
		}
		if(xQuery == null) {
			//perhaps resource specified
			Assert.notNull(xQueryFileResource, "One of XQuery or the XQuery resource is mandatory");
			xQuery = XQueryUtils.readXQueryFromResource(xQueryFileResource);
		}

		try {
			XQConnection conn = getConnection();
			XQPreparedExpression expression = conn.prepareExpression(xQuery);
			QName[] extParameters = expression.getAllExternalVariables();
			if(extParameters != null && extParameters.length > 0) {
				xQueryParameters = new ArrayList<String>();
				for(QName qName:extParameters) {
					xQueryParameters.add(qName.getLocalPart());
				}
			}
			expression.close();
			conn.close();
		} catch (XQException e) {
			throw new MessagingException("Caught Exception while opening a connection to the datasource", e);
		}

		if(xQueryParameters != null) {
			if (xQueryParameterMap == null) {
				throw new MessagingException("Expecting " + xQueryParameters.size() + " parameters in the xquery, " +
						"but none provided to the router");
			}

			//now check if all the parameter needed are present in the map
			List<String> missingParameters = new ArrayList<String>();
			for(String xQueryParameter:xQueryParameters) {
				if(!xQueryParameterMap.containsKey(xQueryParameter)) {
					missingParameters.add(xQueryParameter);
				}
			}

			if(missingParameters.size() > 0) {
				StringBuilder builder = new StringBuilder();
				builder.append("[").append("$").append(missingParameters.get(0));
				if(missingParameters.size() > 1) {
					for(int i = 1;i < missingParameters.size();i++) {
						builder.append(", ").append("$").append(missingParameters.get(i));
					}
				}
				builder.append("]");
				throw new MessagingException("Missing parameter(s) " + builder.toString());
			}
		}


	}



	public void setBeanClassLoader(ClassLoader classLoader) {
		Assert.notNull(classLoader, "Non null class loader instance expected");
		this.classLoader = classLoader;

	}


	/**
	 * The method that finds an implementation in the classpath for some well known
	 * providers. This will kick into action only if an {@link XQDataSource} is not
	 * explicitly provided by the user by invoking {@link XQueryExecutor#setXQDataSource(XQDataSource)}
	 * The method currently looks for Saxon's implementation only
	 *
	 * @return the instantiated {@link XQDataSource}
	 */
	private XQDataSource discoverXQDataSource() {
		Object dataSource = null;
		try {
			if(ClassUtils.isPresent(SAXON_XQ_DATASOURCE_CLASS, classLoader)) {
				dataSource = Class.forName(SAXON_XQ_DATASOURCE_CLASS).newInstance();
			}
			//For now its just Saxon we will discover, we can add other implementations here later
		} catch (Exception e) {
			throw new MessagingException("Unable to discover/instantiate an XQDataSource, " +
					"see nested exception for details", e);
		}
		Assert.notNull(dataSource, "No XQDataSource provided nor any known implementation discovered in the classpath");
		logger.info("Using \"" + dataSource.getClass() + "\" as the XQDataSource implementation");
		return (XQDataSource)dataSource;
	}

	/**
	 * Gets the connection, uses userName and password if one is set
	 * @return
	 * @throws XQException
	 */
	private XQConnection getConnection() throws XQException {
		XQConnection conn;
		if(StringUtils.hasText(userName)) {
			conn = xqDataSource.getConnection(userName,password);
		}
		else {
			conn = xqDataSource.getConnection();
		}
		return conn;
	}

	/**
	 * There are some default mappers defined, those will be added if the user has not provided
	 * some implementations for them
	 */
	@SuppressWarnings("unchecked")
	private void addDefaultMappers() {
		if(!resultMappers.containsKey(String.class)) {
			StringResultMapper mapper = new StringResultMapper();
			mapper.setFormatOutput(formatOutput);
			resultMappers.put(String.class, mapper);
		}

		if(!resultMappers.containsKey(Boolean.class)) {
			BooleanResultMapper mapper = new BooleanResultMapper();
			mapper.setFormatOutput(formatOutput);
			resultMappers.put(Boolean.class, mapper);
		}

		if(!resultMappers.containsKey(Number.class)) {
			NumberResultMapper mapper = new NumberResultMapper();
			mapper.setFormatOutput(formatOutput);
			resultMappers.put(Number.class, mapper);
		}

		if(!resultMappers.containsKey(Node.class)) {
			NodeResultMapper mapper = new NodeResultMapper();
			mapper.setFormatOutput(formatOutput);
			resultMappers.put(Node.class, mapper);
		}
	}

	/**
	 * Executes the XQuery for result and produce the result as a {@link List} of {@link String}
	 * @param message the source message that would be used to derive the values of the parameters
	 *
	 * @return The {@link List} of results
	 */
	public List<String> executeForString(Message<?> message) {
		return execute(message,String.class);
	}

	/**
	 * Executes the XQuery for result and produce the result as a {@link List} of {@link Boolean}
	 * @param message the source message that would be used to derive the values of the parameters
	 *
	 * @return The {@link List} of results
	 */
	public List<Boolean> executeForBoolean(Message<?> message) {
		return execute(message,Boolean.class);
	}


	/**
	 * Executes the XQuery for result and produce the result as a {@link List} of {@link Number}
	 * @param message the source message that would be used to derive the values of the parameters
	 *
	 * @return The {@link List} of results
	 */
	public List<Number> executeForNumber(Message<?> message) {
		return execute(message,Number.class);
	}

	/**
	 * Executes the XQuery for result and produce the result as a {@link List} of {@link Node}
	 * @param message the source message that would be used to derive the values of the parameters
	 *
	 * @return The {@link List} of results
	 */
	public List<Node> executeForNode(Message<?> message) {
		return execute(message,Node.class);
	}

	/**
	 * Execute the given XQuery and returns a {@link List} of the provided type
	 * @param <T>
	 * @param message
	 * @param returnType
	 * @return
	 */
	@SuppressWarnings({ "unchecked"})
	public <T> List<T> execute(Message<?> message,Class<T> returnType) {
		Assert.notNull(message,"Non null message expected");
		Assert.notNull(returnType,"Non null type expected");
		Assert.isTrue(resultMappers.containsKey(returnType),"No Result mapper found for the type " + returnType.getName());
		return execute(message, (XQueryResultMapper<T>)resultMappers.get(returnType));
	}

	/**
	 * The method that executes the actual XQuery and uses the provided mapper
	 * to get the result that is returned.
	 * @param <T>
	 * @param message
	 * @param mapper
	 * @return
	 */
	public <T> List<T> execute(Message<?> message,XQueryResultMapper<T> mapper) {
		Node node = converter.convertToNode(message.getPayload());

		if(node == null) {
			return null;
		}

		XQConnection connection = null;
		XQPreparedExpression expression = null;
		try {
			connection = getConnection();
			expression = connection.prepareExpression(xQuery);
			expression.bindNode(XQConstants.CONTEXT_ITEM, node, null);

			//bind the parameter values
			if(xQueryParameters != null && xQueryParameters.size() > 0) {
				//bind them one by one
				for(String parameter:xQueryParameters) {
					XQueryParameter xQueryParam = xQueryParameterMap.get(parameter);
					//TODO: Check what possible values can be supported to be set here
					//Accordingly do we need to set the third parameter for XQItemType
					expression.bindObject(new QName(xQueryParam.getParameterName()),
													xQueryParam.evaluate(message), null);
				}
			}

			XQResultSequence result = expression.executeQuery();
			return mapper.mapResults(result);

		} catch (XQException e) {
			throw new MessagingException("Caught Exception while opening a connection to the datasource", e);
		} finally {
			try {
				if(expression != null) {
					expression.close();
				}
				if(connection != null) {
					connection.close();
				}
			} catch (XQException e) {
				logger.error("Caught Exception while closing the XQ expression.connection", e);
			}
		}
	}

	/**
	 * Sets all the result mappers to be used by this executor.
	 * @param <T>
	 * @param mappers
	 */
	@SuppressWarnings("rawtypes")
	public <T> void setResultMappers(Map<Class<T>, XQueryResultMapper<T>> mappers) {
		Assert.notNull(mappers);
		this.resultMappers = new HashMap<Class<T>, XQueryResultMapper<T>>(mappers);
		//not iterate through them and set the format
		for(Object mapper:resultMappers.values()) {
			if(mapper instanceof AbstractXQueryResultMapper) {
				((AbstractXQueryResultMapper)mapper).setFormatOutput(formatOutput);
			}
		}
	}

	/**
	 *Set the {@link XmlPayloadConverter} that would be used to convert the payload
	 * into the XML {@link Node}
	 *
	 * @param converter
	 */
	public void setConverter(XmlPayloadConverter converter) {
		Assert.notNull(converter, "Provide a non null instance of XmlPayloadConverter");
		this.converter = converter;
	}

	/**
	 * The XQuery string that would be evaluated to determine the channel names
	 * @param xQuery
	 */
	public void setXQuery(String xQuery) {
		Assert.isTrue(xQueryFileResource == null, "Only one of XQuery resource file or XQuery may be specified");
		Assert.notNull(xQuery, "Provide a non null XQuery");
		this.xQuery = xQuery;
	}

	/**
	 * Sets the XQuery's .xq file as the resource. The contents of this file will be read as xQuery
	 *
	 * @param xQueryFileResource
	 */
	public void setXQueryFileResource(Resource xQueryFileResource) {
		Assert.isTrue(xQuery == null,"Only one of XQuery resource file or XQuery may be specified");
		this.xQueryFileResource = xQueryFileResource;
	}

	/**
	 * Sets the {@link XQDataSource}
	 * @param xqDataSource
	 */
	public void setXQDataSource(XQDataSource xqDataSource) {
		Assert.notNull(xqDataSource, "Provide a non null instance of the XQDatasource");
		this.xqDataSource = xqDataSource;
	}


	/**
	 * Sets the parameter map where the parameter name is the key and the
	 * {@link XQueryParameter} instance is the value
	 *
	 * @param xQueryParameterMap
	 */
	public void setXQueryParameterMap(
			Map<String, XQueryParameter> xQueryParameterMap) {
		this.xQueryParameterMap = xQueryParameterMap;
	}

	/**
	 * Convenience method to add a {@link XQueryParameter} to the map
	 * @param param
	 */
	public void addXQueryParameter(XQueryParameter param) {
		if(xQueryParameterMap == null) {
			xQueryParameterMap = new HashMap<String, XQueryParameter>();
		}
		xQueryParameterMap.put(param.getParameterName(), param);
	}

	/**
	 * Convenience method that would be used to set the parameters in the parameter
	 * map. Any non conflicting parameters would be retained
	 * @param params
	 */
	public void setXQueryParameters(List<XQueryParameter> params) {
		if(params != null && params.size() > 0) {
			if(xQueryParameterMap == null) {
				xQueryParameterMap = new HashMap<String, XQueryParameter>();
			}
			for(XQueryParameter param:params) {
				xQueryParameterMap.put(param.getParameterName(), param);
			}
		}
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



	//TODO: Support date, dateTime data types


	/**
	 * Sets the user name to be used while obtaining the connection.
	 *
	 */
	public void setUserName(String userName) {
		Assert.hasText(userName, "Null or empty string provided as user name");
		this.userName = userName;
	}

	/**
	 * Sets the password to be used for obtaining the connection
	 * The value if ignored if the userName is null
	 *
	 * @param password
	 */
	public void setPassword(String password) {
		Assert.notNull(password, "Null password provided");
		//Can password be empty string?
		this.password = password;
	}
}
