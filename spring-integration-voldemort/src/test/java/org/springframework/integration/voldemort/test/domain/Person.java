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
package org.springframework.integration.voldemort.test.domain;

import java.io.Serializable;

/**
 * Sample object persisted in Voldemort database.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
public class Person implements Serializable {
	private static final long serialVersionUID = -9092199331950213292L;

	private String id;
	private String firstName;
	private String lastName;

	public Person(String id, String firstName, String lastName) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( !( o instanceof Person ) ) return false;

		Person person = (Person) o;

		if ( id != null ? !id.equals( person.id ) : person.id != null ) return false;
		if ( firstName != null ? !firstName.equals( person.firstName ) : person.firstName != null ) return false;
		if ( lastName != null ? !lastName.equals( person.lastName ) : person.lastName != null ) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + ( firstName != null ? firstName.hashCode() : 0 );
		result = 31 * result + ( lastName != null ? lastName.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		return "Person(id = " + id + ", firstName = " + firstName + ", lastName = " + lastName + ")";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
