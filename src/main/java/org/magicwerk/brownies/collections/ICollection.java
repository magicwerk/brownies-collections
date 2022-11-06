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
@SuppressWarnings("serial")
public interface ICollection<E> extends Collection<E> {

	/**
	 * Create a new collection by applying the specified filter to all elements.
	 * Only element which are allowed by the predicate are copied to the new list.
	 *
	 * @param predicate	predicate used for filtering
	 * @return			created list
	 */
	public ICollection<E> filteredList(Predicate<? super E> predicate);

	/**
	 * Create a new list by applying the specified mapping function to all elements.
	 *
	 * @param func	mapping function
	 * @return		created list
	 */
	public <R> IList<R> mappedList(Function<E, R> func);

}
