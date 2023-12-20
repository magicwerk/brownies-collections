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
package org.magicwerk.brownies.collections;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implements a Set based on a Collection.
 * It supports both mutable and immutable sets.
 *
 * @author Thomas Mauch
 */
public class Listables {

	public static class ArrayIterator<E> implements Iterator<E> {

		E[] array;
		int index;

		ArrayIterator(E[] array) {
			this.array = array;
		}

		@Override
		public boolean hasNext() {
			return index < array.length;
		}

		@Override
		public E next() {
			if (index >= array.length) {
				throw new NoSuchElementException();
			}
			return array[index++];
		}

	}

	protected static class IListableArray<E> implements IListable<E> {
		E[] array;

		IListableArray(E[] array) {
			this.array = array;
		}

		@Override
		public int size() {
			return array.length;
		}

		@Override
		public E get(int index) {
			return array[index];
		}
	}

	protected static class IListableList<E> implements IListable<E> {
		List<E> list;

		IListableList(List<E> list) {
			this.list = list;
		}

		@Override
		public int size() {
			return list.size();
		}

		@Override
		public E get(int index) {
			return list.get(index);
		}
	}

}
