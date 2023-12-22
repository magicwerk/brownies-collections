/*
 * Copyright 2023 by Thomas Mauch
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
package org.magicwerk.brownies.collections;

import java.util.List;

/**
 * Class {@link Listables} contains implementations of the {@link IListable} interface for {@link List}s and arrays.
 *
 * @author Thomas Mauch
 */
public class Listables {

	/**
	 * Class {@link IListableArray} implements the {@link IListable} interface for arrays.
	 */
	public static class IListableArray<E> implements IListable<E> {
		E[] array;

		public IListableArray(E[] array) {
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

	/**
	 * Class {@link IListableList} implements the {@link IListable} interface for {@link List}s.
	 */
	public static class IListableList<E> implements IListable<E> {
		List<E> list;

		public IListableList(List<E> list) {
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
