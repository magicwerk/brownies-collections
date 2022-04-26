/*
 * Copyright 2015 by Thomas Mauch
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
package org.magicwerk.brownies.collections;

import java.util.Map.Entry;

/**
 * Read-only implementation of Map.Entry.
 *
 * @author Thomas Mauch
 */
public class ImmutableMapEntry<K, E> implements Entry<K, E> {
	private K key;
	private E value;

	/**
	 * Constructor of an immutable map entry.
	 * 
	 * @param key	key
	 * @param value	value
	 */
	public ImmutableMapEntry(K key, E value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public E getValue() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 * <p><i>
	 * Note that this method fails with an AssertionError because the object is read only.
	 * </i></p>
	 */
	@Override
	public E setValue(E value) {
		throw new AssertionError();
	}

	@Override
	public String toString() {
		return "MapEntry [key=" + key + ", value=" + value + "]";
	}

}