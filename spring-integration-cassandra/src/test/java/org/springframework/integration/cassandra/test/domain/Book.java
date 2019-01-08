/*
 * Copyright 2015-2019 the original author or authors.
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

package org.springframework.integration.cassandra.test.domain;

import java.util.Date;
import java.util.Objects;

import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;


/**
 * Test POJO
 *
 * @author David Webb
 * @author Artem Bilan
 */
@Table("book")
public class Book {

	@PrimaryKey
	private String isbn;

	private String title;

	@Indexed
	private String author;

	private int pages;

	private Date saleDate;

	private boolean isInStock;

	/**
	 * @return Returns the isbn.
	 */
	public String getIsbn() {
		return this.isbn;
	}

	/**
	 * @return Returns the saleDate.
	 */
	public Date getSaleDate() {
		return this.saleDate;
	}

	/**
	 * @param saleDate The saleDate to set.
	 */
	public void setSaleDate(Date saleDate) {
		this.saleDate = saleDate;
	}

	/**
	 * @return Returns the isInStock.
	 */
	public boolean isInStock() {
		return this.isInStock;
	}

	/**
	 * @param isInStock The isInStock to set.
	 */
	public void setInStock(boolean isInStock) {
		this.isInStock = isInStock;
	}

	/**
	 * @param isbn The isbn to set.
	 */
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return Returns the author.
	 */
	public String getAuthor() {
		return this.author;
	}

	/**
	 * @param author The author to set.
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return Returns the pages.
	 */
	public int getPages() {
		return this.pages;
	}

	/**
	 * @param pages The pages to set.
	 */
	public void setPages(int pages) {
		this.pages = pages;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ("isbn -> " + this.isbn) + "\n" + "tile -> " + this.title + "\n" + "author -> " + this.author
				+ "\n" + "pages -> " + this.pages + "\n";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Book book = (Book) o;
		return this.pages == book.pages &&
				this.isInStock == book.isInStock &&
				Objects.equals(this.isbn, book.isbn) &&
				Objects.equals(this.title, book.title) &&
				Objects.equals(this.author, book.author) &&
				Objects.equals(this.saleDate, book.saleDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.isbn, this.title, this.author, this.pages, this.saleDate, this.isInStock);
	}

}
