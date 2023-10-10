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
import java.util.function.Supplier;

/**
 * ICollectionTools offers default implementations of Collection functionality.
 *
 * @author Thomas Mauch
 * @see	    java.util.Collection
 */
public interface ICollectionTools {

	/**
	 * Returns the first element stored in the collection.
	 * If the collection is empty, a <code>NoSuchElementException</code> is thrown.
	 *
	 * @return	first element stored in the collection
	 */
	public static <E> E getFirst(Collection<E> coll) {
		return coll.iterator().next();
	}

	/**
	 * Returns the first element stored in the collection.
	 * If the collection is empty, null is returned.
	 *
	 * @return	first element stored in the collection or null if empty
	 */
	public static <E> E getFirstOrNull(Collection<E> coll) {
		Iterator<E> iter = coll.iterator();
		return iter.hasNext() ? iter.next() : null;
	}

	/**
	 * Returns the only element stored in the collection.
	 * If the collection size is not 1, a <code>NoSuchElementException</code> is thrown.
	 *
	 * @return	only element stored in the collection
	 */
	public static <E> E getSingle(Collection<E> coll) {
		if (coll.size() != 1) {
			throw new NoSuchElementException();
		}
		return getFirst(coll);
	}

	/**
	 * Returns the only element stored in the collection or null if the collection is empty.
	 * If the collection's size is greater than 1, a <code>NoSuchElementException</code> is thrown.
	 *
	 * @return	only element stored in the collection or null if empty
	 */
	public static <E> E getSingleOrNull(Collection<E> coll) {
		int size = coll.size();
		if (size == 0) {
			return null;
		} else if (size == 1) {
			return getFirst(coll);
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
	public static <E> E getIf(Collection<E> coll, Predicate<? super E> predicate) {
		for (Iterator<E> iter = coll.iterator(); iter.hasNext();) {
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
	public static <E> boolean containsIf(Collection<E> coll, Predicate<? super E> predicate) {
		for (Iterator<E> iter = coll.iterator(); iter.hasNext();) {
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
	public static <E> int countIf(Collection<E> coll, Predicate<? super E> predicate) {
		int count = 0;
		for (Iterator<E> iter = coll.iterator(); iter.hasNext();) {
			E e = iter.next();
			if (predicate.test(e)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Create a new list by applying the specified filter to all elements.
	 *
	 * @param predicate	filter predicate
	 * @return			created list
	 */
	public static <E, C extends Collection<E>> C filter(Collection<E> coll, Predicate<? super E> predicate, Supplier<C> factory) {
		C list = factory.get();
		for (E e : coll) {
			if (predicate.test(e)) {
				list.add(e);
			}
		}
		return list;
	}

	/**
	 * Create a new list by applying the specified mapping function to all elements.
	 *
	 * @param func	mapping function
	 * @return		created list
	 */
	public static <E, R, C extends Collection<R>> C map(Collection<E> coll, Function<E, R> func, Supplier<C> factory) {
		C list = factory.get();
		for (E e : coll) {
			list.add(func.apply(e));
		}
		return list;
	}

	/**
	 * Create a new list by applying the specified mapping function to all elements.
	 *
	 * @param func	mapping function
	 * @return		created list
	 */
	public static <E, R, RC extends Collection<R>, C extends Collection<R>> C flatMap(Collection<E> coll, Function<E, RC> func, Supplier<C> factory) {
		C list = factory.get();
		for (E e : coll) {
			list.addAll(func.apply(e));
		}
		return list;
	}

	/**
	 * Create a new list by applying the specified mapping function to all elements and then filtering it.
	 *
	 * @param func		mapping function
	 * @param filter	filter predicate
	 * @return			created list
	 */
	public static <E, R> IList<R> mapFilter(Collection<E> coll, Function<E, R> func, Predicate<R> filter) {
		return mapFilter(coll, func, filter, GapList::new);
	}

	/**
	 * Create a new collection by applying the specified mapping function to all elements and then filtering it.
	 *
	 * @param func		mapping function
	 * @param filter	filter predicate
	 * @param factory	factory to create collection
	 * @return			created list
	 */
	public static <E, R, C extends Collection<R>> C mapFilter(Collection<E> coll, Function<E, R> func, Predicate<R> filter, Supplier<C> factory) {
		C list = factory.get();
		for (E e : coll) {
			R r = func.apply(e);
			if (filter.test(r)) {
				list.add(r);
			}
		}
		return list;
	}

	/**
	 * Create a new list by applying the specified filter first and then the mapping function to all elements selected.
	 *
	 * @param filter	filter predicate
	 * @param func		mapping function
	 * @return			created list
	 */
	public static <E, R, C extends Collection<R>> IList<R> filterMap(Collection<E> coll, Predicate<E> filter, Function<E, R> func) {
		return filterMap(coll, filter, func, GapList::new);
	}

	/**
	 * Create a new collection by applying the specified filter first and then the mapping function to all elements selected.
	 *
	 * @param filter	filter predicate
	 * @param func		mapping function
	 * @param factory	factory to create collection
	 * @return			created list
	 */
	public static <E, R, C extends Collection<R>> C filterMap(Collection<E> coll, Predicate<E> filter, Function<E, R> func, Supplier<C> factory) {
		C list = factory.get();
		for (E e : coll) {
			if (filter.test(e)) {
				list.add(func.apply(e));
			}
		}
		return list;
	}

}
