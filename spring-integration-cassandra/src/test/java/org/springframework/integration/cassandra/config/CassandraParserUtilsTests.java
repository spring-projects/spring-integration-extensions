/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.cassandra.config;

import org.junit.Assert;
import org.junit.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.integration.cassandra.config.xml.CassandraParserUtils;

/**
 * @author Filippo Balicchia
 */

public class CassandraParserUtilsTests {

	@Test
	public void mutuallyExclusiveCase1() {
		String query = "";
		BeanDefinition statementExpressionDef = null;
		String ingestQuery = "";
		Assert.assertTrue(CassandraParserUtils.areMutuallyExclusive(query, statementExpressionDef, ingestQuery));
	}

	@Test
	public void mutuallyExclusiveCase2() {
		String query = "";
		BeanDefinition statementExpressionDef = null;
		String ingestQuery = "insert into book (isbn, title, author, pages, saleDate, isInStock) values (?, ?, ?, ?, ?, ?)";
		Assert.assertTrue(CassandraParserUtils.areMutuallyExclusive(query, statementExpressionDef, ingestQuery));
	}

	@Test
	public void mutuallyExclusiveCase3() {
		String query = "";
		BeanDefinition statementExpressionDef = new RootBeanDefinition();
		String ingestQuery = "";
		Assert.assertTrue(CassandraParserUtils.areMutuallyExclusive(query, statementExpressionDef, ingestQuery));
	}

	@Test
	public void mutuallyExclusiveCase4() {
		String query = "";
		BeanDefinition statementExpressionDef = new RootBeanDefinition();
		String ingestQuery = "insert into book (isbn, title, author, pages, saleDate, isInStock) values (?, ?, ?, ?, ?, ?)";
		Assert.assertFalse(CassandraParserUtils.areMutuallyExclusive(query, statementExpressionDef, ingestQuery));
	}

	@Test
	public void mutuallyExclusiveCase5() {
		String query = "SELECT * FROM book limit :size";
		BeanDefinition statementExpressionDef = new RootBeanDefinition();
		String ingestQuery = "";
		Assert.assertFalse(CassandraParserUtils.areMutuallyExclusive(query, statementExpressionDef, ingestQuery));
	}

	@Test
	public void mutuallyExclusiveCase6() {
		String query = "SELECT * FROM book limit :size";
		BeanDefinition statementExpressionDef = new RootBeanDefinition();
		String ingestQuery = "insert into book (isbn, title, author, pages, saleDate, isInStock) values (?, ?, ?, ?, ?, ?)";
		Assert.assertFalse(CassandraParserUtils.areMutuallyExclusive(query, statementExpressionDef, ingestQuery));
	}

	@Test
	public void mutuallyExclusiveCase7() {
		String query = "SELECT * FROM book limit :size";
		BeanDefinition statementExpressionDef = new RootBeanDefinition();
		String ingestQuery = "";
		Assert.assertFalse(CassandraParserUtils.areMutuallyExclusive(query, statementExpressionDef, ingestQuery));
	}

	@Test
	public void mutuallyExclusiveCase8() {
		String query = "SELECT * FROM book limit :size";
		BeanDefinition statementExpressionDef = new RootBeanDefinition();
		String ingestQuery = "insert into book (isbn, title, author, pages, saleDate, isInStock) values (?, ?, ?, ?, ?, ?)";
		Assert.assertFalse(CassandraParserUtils.areMutuallyExclusive(query, statementExpressionDef, ingestQuery));
	}

}
