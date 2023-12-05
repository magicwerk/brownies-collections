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
 * The NaturalComparator will compare object using their natural order.
 *
 * @param <T>	element type
 */
public class NaturalComparator<T> extends SingletonComparator<T> {

	/**
	 * Singleton instance.
	 */
	@SuppressWarnings("rawtypes")
	private static final NaturalComparator INSTANCE = new NaturalComparator();

	/**
	 * Returns singleton instance.
	 */
	@SuppressWarnings("unchecked")
	public static <T> NaturalComparator<T> INSTANCE() {
		return INSTANCE;
	}

	/**
	 * Returns singleton instance.
	 */
	@SuppressWarnings("unchecked")
	public static <T> NaturalComparator<T> INSTANCE(Class<T> c) {
		return INSTANCE;
	}

	/**
	 * Prevent construction.
	 */
	private NaturalComparator() {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int compare(T o1, T o2) {
		return ((Comparable) o1).compareTo(o2);
	}
}
