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
 * Sample object with composite key persisted in Voldemort database.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
public class Car implements Serializable {
	private static final long serialVersionUID = -891019943116582242L;

	private CarId id;
	private String model;

	public Car(CarId id, String model) {
		this.id = id;
		this.model = model;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( ! ( o instanceof Car ) ) return false;

		Car car = (Car) o;

		if ( id != null ? !id.equals( car.id ) : car.id != null ) return false;
		if ( model != null ? !model.equals( car.model ) : car.model != null ) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + ( model != null ? model.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		return "Car(id = " + id + ", model = " + model + ")";
	}

	public CarId getId() {
		return id;
	}

	public void setId(CarId id) {
		this.id = id;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	/**
	 * Car identifier wrapper.
	 *
	 * @author Lukasz Antoniak
	 * @since 1.0
	 */
	public static class CarId implements Serializable {
		private static final long serialVersionUID = -5586075844887213095L;

		private Integer id;

		public CarId(Integer id) {
			this.id = id;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) return true;
			if ( ! ( o instanceof CarId ) ) return false;

			CarId carId = (CarId) o;

			if ( id != null ? !id.equals( carId.id ) : carId.id != null ) return false;

			return true;
		}

		@Override
		public int hashCode() {
			return id != null ? id.hashCode() : 0;
		}

		@Override
		public String toString() {
			return "CarId(id = " + id + ")";
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}
	}
}
