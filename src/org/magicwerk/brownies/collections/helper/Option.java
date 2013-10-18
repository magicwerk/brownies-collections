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

/**
 * The Option class allows to distinguish between a null value and no value.
 *
 * @param <T>	element type
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class Option<T> {
    private static final Option EMPTY = new Option();

    /**
     * @return unmodifiable empty instance
     */
    @SuppressWarnings("unchecked")
    public static <EE> Option<EE> EMPTY() {
        return EMPTY;
    }

	private boolean hasValue;
	private T value;


	/**
	 * Prevent construction (use EMPTY)
	 */
	private Option() {
	}

	public Option(T value) {
		this.hasValue = true;
		this.value = value;
	}

	public boolean hasValue() {
		return hasValue;
	}

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
	public String toString() {
		return "Option [hasValue=" + hasValue + ", value=" + value + "]";
	}

}
