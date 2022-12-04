/*
 * Copyright 2012 by Thomas Mauch
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

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * IList is an abstract class which offers all interfaces offered by both ArrayList and LinkedList.
 * It also offers additional methods which are then available in all implementations of GapList and BigList.
 *
 * @author Thomas Mauch
 *
 * @param <E> type of elements stored in the list
 * @see	    java.util.List
 * @see	    java.util.Deque
 * @see	    java.util.ArrayList
 * @see	    java.util.LinkedList
 */
public interface ICollection<E> extends Collection<E> {

	/**
	 * Returns the first element stored in the collection.
	 * If the collection is empty, a <code>NoSuchElementException</code> is thrown.
	 *
	 * @return	first element stored in the collection
	 */
	default E getFirst() {
		return iterator().next();
	}

	/**
	 * Returns the first element stored in the collection.
	 * If the collection is empty, null is returned.
	 *
	 * @return	first element stored in the collection or null if empty
	 */
	default E getFirstOrNull() {
		Iterator<E> iter = iterator();
		return iter.hasNext() ? iter.next() : null;
	}

	/**
	 * Returns the only element stored in the collection.
	 * If the collection size is not 1, a <code>NoSuchElementException</code> is thrown.
	 *
	 * @return	only element stored in the collection
	 */
	default E getSingle() {
		if (size() != 1) {
			throw new NoSuchElementException();
		}
		return getFirst();
	}

	/**
	 * Returns the only element stored in the collection or null if the collection is empty.
	 * If the collection's size is greater than 1, a <code>NoSuchElementException</code> is thrown.
	 *
	 * @return	only element stored in the collection or null if empty
	 */
	default E getSingleOrNull() {
		int size = size();
		if (size == 0) {
			return null;
		} else if (size == 1) {
			return getFirst();
		} else {
			throw new NoSuchElementException();
		}
	}

	/**
	 * Returns the first element stored in the collection which matches the predicate.
	 * If the collection is empty of no element matches, null is returned.
	 *
	 * @return	first element matching the predicate or null if not founds
	 */
	default E getIf(Predicate<? super E> predicate) {
		for (Iterator<E> iter = iterator(); iter.hasNext();) {
			E e = iter.next();
			if (predicate.test(e)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * Determines whether the list contains a matching element.
	 *
	 * @param predicate		predicate used to search element
	 * @return				true if the list contains a matching element, false otherwise
	 */
	default boolean containsIf(Predicate<? super E> predicate) {
		for (Iterator<E> iter = iterator(); iter.hasNext();) {
			E e = iter.next();
			if (predicate.test(e)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Counts how many elements in the list match the predicate.
	 *
	 * @param predicate a predicate which returns {@code true} for elements to be counted
	 * @return		count how many elements in the list match the predicate
	 */
	default int countIf(Predicate<? super E> predicate) {
		int count = 0;
		for (Iterator<E> iter = iterator(); iter.hasNext();) {
			E e = iter.next();
			if (predicate.test(e)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Create a new collection by applying the specified filter to all elements.
	 * Only element which are allowed by the predicate are copied to the new list.
	 * The returned collection has the same type as the original one.
	 *
	 * @param predicate	predicate used for filtering
	 * @return			created list
	 */
	ICollection<E> filter(Predicate<? super E> predicate);

	/**
	 * Create a new list by applying the specified mapping function to all elements.
	 * The returned list is of type {@link IList}, typically {@link GapList} unless the original type is {@link BigList}.
	 *
	 * @param func	mapping function
	 * @return		created list
	 */
	<R> IList<R> map(Function<E, R> func);

	<R> IList<R> mapFilter(Function<E, R> func, Predicate<R> filter);

	<R> IList<R> filterMap(Predicate<E> filter, Function<E, R> func);

}
