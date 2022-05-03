/*
 * Copyright 2013 by Thomas Mauch
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
 *
 * $Id$
 */
package org.magicwerk.brownies.collections.helper;

import java.util.Optional;

/**
 * Class {@link Option} stores a single value which may also be null.
 * This is different from {@link Optional} which does not support null values.
 *
 * @param <T>	element type
 *
 * @author Thomas Mauch
 */
public class Option<T> {
	@SuppressWarnings("rawtypes")
	private static final Option EMPTY = new Option();

	/**
	 * @return unmodifiable empty instance
	 */
	@SuppressWarnings("unchecked")
	public static <EE> Option<EE> empty() {
		return EMPTY;
	}

	public static <T> Option<T> of(T value) {
		return new Option<>(value);
	}

	private boolean hasValue;
	private T value;

	/**
	 * Prevent construction (use EMPTY)
	 */
	private Option() {
	}

	/**
	 * Construct option with specified value.
	 *
	 * @param value		value
	 */
	public Option(T value) {
		this.hasValue = true;
		this.value = value;
	}

	/**
	 * Returns true if a value is stored, false otherwise
	 */
	public boolean hasValue() {
		return hasValue;
	}

	/**
	 * Returns stored value of null if no value is stored.
	 * It is therefore not possible to distinguish between a null value stored or no value stored.
	 */
	public T getValueOrNull() {
		return value;
	}

	/**
	 * Returns stored value.
	 * If no value is stored, an exception is thrown.
	 *
	 * @return stored value
	 * @throws IllegalArgumentException if no value is stored
	 */
	public T getValue() {
		if (!hasValue) {
			throw new IllegalArgumentException("No value stored");
		}
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		Option other = (Option) obj;
		if (hasValue != other.hasValue)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (hasValue ? 1231 : 1237);
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "Option [hasValue=" + hasValue + ", value=" + value + "]";
	}

}
