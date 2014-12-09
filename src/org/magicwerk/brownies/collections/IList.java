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

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;

import org.magicwerk.brownies.collections.function.IFunction;
import org.magicwerk.brownies.collections.function.IPredicate;

/**
 * IList is an abstract class which offers all interfaces offered by both ArrayList and LinkedList.
 * It also offers additional methods which are then available in all implementations of GapList and BigList.
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @param <E> type of elements stored in the list
 * @see	    java.util.List
 * @see	    java.util.Deque
 * @see	    java.util.ArrayList
 * @see	    java.util.LinkedList
 */
@SuppressWarnings("serial")
public abstract class IList<E> extends AbstractList<E>
	implements
		// All interfaces of ArrayList
		List<E>, RandomAccess, Cloneable, Serializable,
		// Additional interfaces of LinkedList
		Deque<E> {

	/**
	 * Copies the collection values into an array.
	 *
	 * @param coll   collection of values
	 * @return       array containing the collection values
	 */
	static Object[] toArray(Collection<?> coll) {
    	Object[] values = coll.toArray();
    	// as in ArrayList: toArray() might (incorrectly) not return Object[] (see bug 6260652)
    	if (values.getClass() != Object[].class) {
    		values = Arrays.copyOf(values, values.length, Object[].class);
	    }
    	return values;
	}

    /**
     * Returns a shallow copy of this list instance.
     * (the new list will contain the same elements as the source list, i.e. the elements themselves are not copied).
     * This method is identical to clone() except that the result is casted to an IList.
     *
     * @return a clone of this instance
     * @see #clone
     */
	@SuppressWarnings("unchecked")
    public IList<E> copy() {
	    return (IList<E>) clone();
	}

    /**
     * Returns an unmodifiable view of this list. This method allows
     * modules to provide users with "read-only" access to internal lists.
     * Query operations on the returned list "read through" to the specified
     * list, and attempts to modify the returned list, whether direct or
     * via its iterator, result in an UnsupportedOperationException.
     *
     * @return an unmodifiable view of the specified list
     */
    abstract public IList<E> unmodifiableList();

    /**
     * Returns a shallow copy of this list instance.
     * (The elements themselves are not copied).
     *
     * @return a clone of this list instance
     */
	@SuppressWarnings("unchecked")
    @Override
    public Object clone() {
		try {
			IList<E> list = (IList<E>) super.clone();
			list.doClone(this);
		    return list;
		}
		catch (CloneNotSupportedException e) {
		    // This shouldn't happen, since we are Cloneable
		    throw new AssertionError(e);
		}
    }

	/**
	 * Initialize this object after the bitwise copy has been made
	 * by Object.clone().
	 *
	 * @param that	source object
	 */
	abstract protected void doClone(IList<E> that);

	@Override
	public void clear() {
		doClear();
	}

	protected void doClear() {
		doRemoveAll(0, size());
	}

	@Override
	abstract public int size();

	/**
	 * Returns capacity of this list.
	 * Note that two lists are considered equal even if they have a distinct capacity.
	 * Also the capacity can be changed by operations like clone() etc.
	 *
	 * @return capacity of this list
	 */
	abstract public int capacity();

    @Override
    public E get(int index) {
    	checkIndex(index);
        return doGet(index);
    }

    /**
     * Helper method for getting an element from the list.
     * This is the only method which really gets an element.
     * Override if you need to validity checks before getting.
     *
     * @param index index of element to return
     * @return      the element at the specified position in this list
     */
    abstract protected E doGet(int index);

    /**
     * Helper method for setting an element in the list.
     * This is the only method which really sets an element.
     * Override if you need to validity checks before setting.
     *
     * @param index index where element will be placed
     * @param elem  element to set
     * @return      old element which was at the position
     */
    abstract protected E doSet(int index, E elem);

    @Override
	public E set(int index, E elem) {
    	checkIndex(index);
		return doSet(index, elem);
	}

    /**
     * Sets an element at specified position.
     * This method is used internally if existing elements will be moved etc.
     * Override if you need to validity checks.
     *
     * @param index index where element will be placed
     * @param elem  element to set
     * @return      old element which was at the position
     */
    abstract protected E doReSet(int index, E elem);

    abstract protected E getDefaultElem();

    /**
     * This method is called internally before elements are allocated or freed.
     * Override if you need to validity checks.
     */
    protected void doModify() {
    }

    @Override
	public boolean add(E elem) {
		return doAdd(-1, elem);
	}

	@Override
	public void add(int index, E elem)	{
		checkIndexAdd(index);
		doAdd(index, elem);
	}

	/**
	 * Helper method for adding an element to the list.
	 * This is the only method which really adds an element.
	 * Override if you need to validity checks before adding.
	 *
	 * @param index	index where element should be added
	 *              (-1 means it is up to the implementation to choose the index)
	 * @param elem	element to add
	 * @return      true if element has been added, false otherwise
	 */
	abstract protected boolean doAdd(int index, E elem);

	@Override
	public E remove(int index) {
		checkIndex(index);
		return doRemove(index);
	}

	/**
	 * Helper method to remove an element.
     * This is the only method which really removes an element.
     * Override if you need to validity checks before removing.
	 *
	 * @param index	index of element to remove
	 * @return		removed element
	 */
	abstract protected E doRemove(int index);

    /**
     * Increases the capacity of this <tt>GapList</tt> instance, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity
     */
	// Note: Provide this method to make transition from ArrayList as
	//       smooth as possible
    public void ensureCapacity(int minCapacity) {
		doModify();
		doEnsureCapacity(minCapacity);
    }

    /**
     * Increases the capacity of this list instance, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity
     */
    abstract protected void doEnsureCapacity(int minCapacity);

    /**
     * An application can use this operation to minimize the storage of an instance.
     */
	// Note: Provide this method to make transition from ArrayList as
	//       smooth as possible
	abstract public void trimToSize();

    @Override
    public boolean equals(Object obj) {
    	if (obj == this) {
    		return true;
    	}
    	if (!(obj instanceof List<?>)) {
    		return false;
    	}
    	@SuppressWarnings("unchecked")
		List<E> list = (List<E>) obj;
    	int size = size();
    	if (size != list.size()) {
    		return false;
    	}
    	for (int i=0; i<size; i++) {
    		if (!equalsElem(doGet(i), list.get(i))) {
    			return false;
    		}
    	}
    	return true;
    }

    @Override
    public int hashCode() {
    	int hashCode = 1;
    	int size = size();
    	for (int i=0; i<size; i++) {
    		E elem = doGet(i);
    		hashCode = 31*hashCode + hashCodeElem(elem);
    	}
    	return hashCode;
    }

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		int size = size();
		for (int i=0; i<size; i++) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(doGet(i));
		}
		buf.append("]");
		return buf.toString();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Helper function to check two elements stored in the list for equality.
	 *
	 * @param elem1	first element
	 * @param elem2	second element
	 * @return		true if the elements are equal, otherwise false
	 */
	static boolean equalsElem(Object elem1, Object elem2) {
		if (elem1 == null) {
			if (elem2 == null) {
				return true;
			}
		} else {
			if (elem1.equals(elem2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Helper method to calculate hash code of a element stored in the list.
	 *
	 * @param elem	element
	 * @return		hash code for element
	 */
	static int hashCodeElem(Object elem) {
	    if (elem == null) {
	        return 0;
	    } else {
	        return elem.hashCode();
	    }
	}

	/**
	 * Counts how many times the specified element is contained in the list.
	 *
	 * @param elem	element to count
	 * @return		count how many times the specified element is contained in the list
	 */
	public int getCount(E elem) {
		int count = 0;
		int size = size();
		for (int i=0; i<size; i++) {
			if (equalsElem(doGet(i), elem)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Returns all elements in the list equal to the specified element.
	 *
	 * @param elem	element to look for
	 * @return		all elements in the list equal to the specified element
	 */
    public IList<E> getAll(E elem) {
        IList<E> list = doCreate(-1);
		int size = size();
		for (int i=0; i<size; i++) {
			E e = doGet(i);
			if (equalsElem(e, elem)) {
				list.add(e);
			}
		}
        return list;
    }

	/**
	 * Returns all elements in the list which match the predicate.
	 *
	 * @param predicate	predicate
	 * @return			all elements in the list which match the predicate
	 */
    public IList<E> getWhere(IPredicate<E> predicate) {
        IList<E> list = doCreate(-1);
		int size = size();
		for (int i=0; i<size; i++) {
			E e = doGet(i);
			if (predicate.test(e)) {
				list.add(e);
			}
		}
        return list;
    }

	/**
	 * Removes all elements in the list which match the predicate.
	 *
	 * @param predicate	predicate
	 */
	public void removeWhere(IPredicate<E> predicate) {
	    int size = size();
		for (int i=0; i<size; i++) {
			E e = doGet(i);
			if (predicate.test(e)) {
				doRemove(i);
				size--;
				i--;
			}
		}
	}

	/**
	 * Removes and returns all elements in the list which match the predicate.
	 *
	 * @param predicate	predicate
	 * @return			elements which have been removed from the list
	 */
	public IList<E> extractWhere(IPredicate<E> predicate) {
	    IList<E> list = doCreate(-1);
	    int size = size();
		for (int i=0; i<size; i++) {
			E e = doGet(i);
			if (predicate.test(e)) {
				list.add(e);
				doRemove(i);
				size--;
				i--;
			}
		}
		return list;
	}

	/**
	 * Returns distinct elements in the list.
	 *
	 * @return		distinct elements in the list
	 */
    public Set<E> getDistinct() {
        Set<E> set = new HashSet<E>();
		int size = size();
		for (int i=0; i<size; i++) {
			set.add(doGet(i));
		}
        return set;
    }

    /**
     * Create a new list by applying the specified mapper to all elements.
     *
     * @param mapper	mapper function
     * @return			created list
     */
    public <R> IList<R> mappedList(IFunction<E,R> mapper) {
		int size = size();
    	@SuppressWarnings("unchecked")
		IList<R> mappedList = (IList<R>) doCreate(size);
		for (int i=0; i<size; i++) {
			E e = doGet(i);
			mappedList.add(mapper.apply(e));
    	}
    	return mappedList;
    }

    /**
     * Filter the list using the specified predicate.
     * Only element which are allowed remain in the list, the others are removed
     *
     * @param predicate predicate used for filtering
     */
    public void filter(IPredicate<? super E> predicate) {
    	// It is typically faster to copy the allowed elements in a new list
    	// than to remove the not allowed from the existing one
    	IList<E> list = doCreate(-1);
		int size = size();
		for (int i=0; i<size; i++) {
			E e = doGet(i);
    		if (predicate.test(e)) {
    			list.add(e);
    		}
    	}
    	doAssign(list);
    }

	@Override
	public int indexOf(Object elem) {
		int size = size();
		for (int i=0; i<size; i++) {
			if (equalsElem(doGet(i), elem)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object elem) {
		for (int i=size()-1; i>=0; i--) {
			if (equalsElem(doGet(i), elem)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the index of the first occurrence of the specified element in this list, starting the search at the specified position.
	 * If the element is not found, -1 is returned.
	 *
	 * @param elem			element to search for
	 * @param fromIndex		start index for search
	 * @return				the index of the first occurrence of the specified element in this list that is greater than or equal to fromIndex,
	 * 						or -1 if this list does not contain the element
	 * @see #indexOf(Object)
	 */
	public int indexOf(Object elem, int fromIndex) {
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		int size = size();
		for (int i=fromIndex; i<size; i++) {
			if (equalsElem(doGet(i), elem)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the index of the last occurrence of the specified element in this list, starting the search at the specified position.
	 * If the element is not found, -1 is returned.
	 *
	 * @param elem			element to search for
	 * @param fromIndex		start index for search
	 * @return				the index of the last occurrence of the specified element in this list that is less than or equal to fromIndex,
	 * 						or -1 if this list does not contain the element
	 * @see #lastIndexOf(Object)
	 */
	public int lastIndexOf(Object elem, int fromIndex) {
		int size = size();
		if (fromIndex >= size) {
			fromIndex = size-1;
		}
		for (int i=fromIndex; i>=0; i--) {
			if (equalsElem(doGet(i), elem)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean remove(Object elem) {
		int index = indexOf(elem);
		if (index == -1) {
			return false;
		}
		doRemove(index);
		return true;
	}

	@Override
	public boolean contains(Object elem) {
		return indexOf(elem) != -1;
	}

	/**
	 * Add elements if it is not already contained in the list.
	 *
	 * @param elem	element to add
	 * @return		true if element has been added, false if not
	 */
	public boolean addIfAbsent(E elem) {
		if (contains(elem)) {
			return false;
		}
		return add(elem);
	}

	/**
	 * Returns true if any of the elements of the specified collection is contained in the list.
	 *
	 * @param coll collection with elements to be contained
	 * @return     true if any element is contained, false otherwise
	 */
	public boolean containsAny(Collection<?> coll) {
	    // Note that the signature has been chosen as in List:
	    // - boolean addAll(Collection<? extends E> c);
	    // - boolean containsAll(Collection<?> c);
	    for (Object elem: coll) {
	        if (contains(elem)) {
	            return true;
	        }
	    }
	    return false;
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
	    // Note that this method is already implemented in AbstractCollection.
		// It has been duplicated so the method is also available in the primitive classes.
	    for (Object elem: coll) {
	        if (!contains(elem)) {
	            return false;
	        }
	    }
	    return true;
	}

	@Override
    public boolean removeAll(Collection<?> coll) {
	    // Note that this method is already implemented in AbstractCollection.
		// It has been duplicated so the method is also available in the primitive classes.
	    boolean modified = false;
	    int size = size();
		for (int i=0; i<size; i++) {
			if (coll.contains(doGet(i))) {
				doRemove(i);
				size--;
				i--;
				modified = true;
			}
		}
		return modified;
    }

	/**
	 * Removes all equal elements.
	 *
	 * @param elem	element
	 * @return		removed equal elements (never null)
	 */
    public IList<E> removeAll(E elem) {
	    IList<E> list = doCreate(-1);
	    int size = size();
		for (int i=0; i<size; i++) {
			E e = doGet(i);
			if (equalsElem(elem, e)) {
				list.add(e);
				doRemove(i);
				size--;
				i--;
			}
		}
		return list;
    }

    /**
     * @see #removeAll(Collection)
     */
    public boolean removeAll(IList<?> coll) {
    	// There is a special implementation accepting an IList
    	// so the method is also available in the primitive classes.
	    boolean modified = false;
	    int size = size();
		for (int i=0; i<size; i++) {
			if (coll.contains(doGet(i))) {
				doRemove(i);
				size--;
				i--;
				modified = true;
			}
		}
		return modified;
    }

	@Override
    public boolean retainAll(Collection<?> coll) {
	    // Note that this method is already implemented in AbstractCollection.
		// It has been duplicated so the method is also available in the primitive classes.
	    boolean modified = false;
	    int size = size();
		for (int i=0; i<size; i++) {
			if (!coll.contains(doGet(i))) {
				doRemove(i);
				size--;
				i--;
				modified = true;
			}
		}
		return modified;
    }

    /**
     * @see #retainAll(Collection)
     */
    public boolean retainAll(IList<?> coll) {
    	// There is a special implementation accepting an IList
    	// so the method is also available in the primitive classes.
	    boolean modified = false;
	    int size = size();
		for (int i=0; i<size; i++) {
			if (!coll.contains(doGet(i))) {
				doRemove(i);
				size--;
				i--;
				modified = true;
			}
		}
		return modified;
    }

	@Override
	public Object[] toArray() {
	    int size = size();
		Object[] array = new Object[size];
		doGetAll(array, 0, size);
        return array;
	}

	/**
	 * Returns an array containing the specified elements in this list.
	 *
	 * @param index	index of first element to copy
	 * @param len	number of elements to copy
	 * @return		array the specified elements
	 */
	public Object[] toArray(int index, int len) {
		Object[] array = new Object[len];
		doGetAll(array, index, len);
        return array;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] array) {
	    int size = size();
        if (array.length < size) {
        	array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
        }
        doGetAll(array, 0, size);
        if (array.length > size) {
        	array[size] = null;
        }
        return array;
	}

	/**
	 * Helper method to fill the specified elements in an array.
	 *
	 * @param array	array to store the list elements
	 * @param index	index of first element to copy
	 * @param len	number of elements to copy
	 * @param <T> type of elements stored in the list
	 */
	@SuppressWarnings("unchecked")
	protected <T> void doGetAll(T[] array, int index, int len) {
		for (int i=0; i<len; i++) {
			array[i] = (T) doGet(index+i);
		}
	}

    /**
     * Adds all of the elements in the specified collection into this list.
     * The new elements will appear in the list in the order that they
     * are returned by the specified collection's iterator.
     *
     * @param coll collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
	@Override
	public boolean addAll(Collection<? extends E> coll) {
        // ArrayList.addAll() also first creates an array containing the
        // collection elements. This guarantees that the list's capacity
        // must only be increased once.
        @SuppressWarnings("unchecked")
        E[] array = (E[]) toArray(coll);
        return doAddAll(-1, array);
	}

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (increases their indices).
     * The new elements will appear in the list in the order that they
     * are returned by the specified collection's iterator.
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param coll collection containing elements to be inserted into this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws NullPointerException if the specified collection is null
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> coll) {
        checkIndexAdd(index);

        // ArrayList.addAll() also first creates an array containing the
        // collection elements. This guarantees that the list's capacity
        // must only be increased once.
        @SuppressWarnings("unchecked")
        E[] array = (E[]) toArray(coll);
        return doAddAll(index, array);
    }

    /**
     * Adds all specified elements into this list.
     *
     * @param elems elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     */
	public boolean addAll(E... elems) {
		return doAddAll(-1, elems);
	}

    /**
     * Inserts the specified elements into this list,
     * starting at the specified position.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (increases their indices).
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param elems elements to be inserted into this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public boolean addAll(int index, E... elems) {
        checkIndexAdd(index);

        return doAddAll(index, elems);
    }

    /**
     * Adds all of the elements in the specified list into this list.
     *
     * @param list collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified list is null
     */
    @SuppressWarnings("unchecked")
    public boolean addAll(IList<? extends E> list) {
        return doAddAll(-1, (E[]) list.toArray());
    }

    /**
     * Inserts all of the elements in the specified list into this
     * list, starting at the specified position.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (increases their indices).
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param list list containing elements to be inserted into this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws NullPointerException if the specified collection is null
     */
	@SuppressWarnings("unchecked")
    public boolean addAll(int index, IList<? extends E> list) {
		checkIndexAdd(index);

		return doAddAll(index, (E[]) list.toArray());
	}

    /**
     * Helper method for adding multiple elements to the list.
     * This default implementation calls doAdd() for adding each element.
     *
     * @param index index where element should be added
     *              (-1 is valid for adding at the end)
     * @param array array with elements to add
     * @return      true if elements have been added, false otherwise
     */
	protected boolean doAddAll(int index, E[] array) {
        doEnsureCapacity(size() + array.length);

		if (array.length == 0) {
			return false;
		}
		for (E elem: array) {
			doAdd(index, elem);
			if (index != -1) {
			    index++;
			}
		}
		return true;
	}

	// Iterators

	@Override
	public Iterator<E> iterator() {
		return new Iter(true);
	}

    @Override
	public ListIterator<E> listIterator() {
		return new ListIter(0);
	}

    @Override
	public ListIterator<E> listIterator(int index) {
		return new ListIter(index);
	}

	@Override
	public Iterator<E> descendingIterator() {
		return new Iter(false);
	}

    // Queue operations

    @Override
    public E peek() {
        if (size() == 0) {
            return null;
        }
        return getFirst();
    }

    @Override
    public E element() {
    	// inline version of getFirst():
        if (size() == 0) {
            throw new NoSuchElementException();
        }
    	return doGet(0);
    }

    @Override
    public E poll() {
        if (size() == 0) {
            return null;
        }
    	return doRemove(0);
    }

	@Override
    public E remove() {
		// inline version of removeFirst():
        if (size() == 0) {
            throw new NoSuchElementException();
        }
    	return doRemove(0);
    }

	@Override
    public boolean offer(E elem) {
    	// inline version of add(elem):
    	return doAdd(-1, elem);
    }

    // Deque operations

    @Override
    public E getFirst() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
    	return doGet(0);
    }

    @Override
    public E getLast() {
    	int size = size();
        if (size == 0) {
            throw new NoSuchElementException();
        }
    	return doGet(size-1);
    }

    @Override
    public void addFirst(E elem) {
    	doAdd(0, elem);
    }

    @Override
    public void addLast(E elem) {
    	// inline version of add(elem):
    	doAdd(-1, elem);
    }

    @Override
    public E removeFirst() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
    	return doRemove(0);
    }

    @Override
    public E removeLast() {
    	int size = size();
        if (size == 0) {
            throw new NoSuchElementException();
        }
    	return doRemove(size-1);
    }

	@Override
	public boolean offerFirst(E elem) {
        // inline version of addFirst(elem):
    	doAdd(0, elem);
        return true;
	}

	@Override
	public boolean offerLast(E elem) {
        // inline version of addLast(elem):
    	doAdd(-1, elem);
        return true;
	}

	@Override
	public E peekFirst() {
        if (size() == 0) {
            return null;
        }
        return doGet(0);
	}

	@Override
	public E peekLast() {
		int size = size();
        if (size == 0) {
            return null;
        }
        return doGet(size-1);
	}

	@Override
	public E pollFirst() {
        if (size() == 0) {
            return null;
        }
        return doRemove(0);
	}

	@Override
	public E pollLast() {
		int size = size();
        if (size == 0) {
            return null;
        }
        return doRemove(size-1);
	}

	@Override
	public E pop() {
        // inline version of removeFirst():
        if (size() == 0) {
            throw new NoSuchElementException();
        }
    	return doRemove(0);

	}

	@Override
	public void push(E elem) {
        // inline version of addFirst();
    	doAdd(0, elem);
	}

	@Override
	public boolean removeFirstOccurrence(Object elem) {
		int index = indexOf(elem);
		if (index == -1) {
			return false;
		}
		doRemove(index);
		return true;
	}

	@Override
	public boolean removeLastOccurrence(Object elem) {
		int index = lastIndexOf(elem);
		if (index == -1) {
			return false;
		}
		doRemove(index);
		return true;
	}

    // --- Static bulk methods working with two ILists ---

    /**
     * Moves elements from one list to another.
     *
     * @param src		source list
     * @param srcIndex	index of first element in source list
     * @param dst		destination list
     * @param dstIndex	index of first element in source list
     * @param len		number of elements to move
     * @param <E> 		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public static <E> void move(IList<E> src, int srcIndex, IList<? super E> dst, int dstIndex, int len) {
        if (src == dst) {
            src.move(srcIndex, dstIndex, len);

        } else {
            src.checkRange(srcIndex, len);
            dst.checkRange(dstIndex, len);

            E defaultElem = src.getDefaultElem();
    		for (int i=0; i<len; i++) {
    			E elem = src.doReSet(srcIndex+i, defaultElem);
    			dst.doSet(dstIndex+i, elem);
    		}
        }
    }

    /**
     * Copies elements from one list to another.
     *
     * @param src		source list
     * @param srcIndex	index of first element in source list
     * @param dst		destination list
     * @param dstIndex	index of first element in source list
     * @param len		number of elements to copy
     * @param <E> 		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public static <E> void copy(IList<? extends E> src, int srcIndex, IList<E> dst, int dstIndex, int len) {
        if (src == dst) {
            src.copy(srcIndex, dstIndex, len);

        } else {
            src.checkRange(srcIndex, len);
            dst.checkRange(dstIndex, len);

    		for (int i=0; i<len; i++) {
    			E elem = src.doGet(srcIndex+i);
    			dst.doSet(dstIndex+i, elem);
    		}
    	}
    }

    /**
     * Swaps elements from two lists.
     *
     * @param src		first list
     * @param srcIndex	index of first element in first list
     * @param dst		second list
     * @param dstIndex	index of first element in second list
     * @param len		number of elements to swap
     * @param <E> 		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public static <E> void swap(IList<E> src, int srcIndex, IList<E> dst, int dstIndex, int len) {
        if (src == dst) {
            src.swap(srcIndex, dstIndex, len);

        } else {
        	src.checkRange(srcIndex, len);
        	dst.checkRange(dstIndex, len);

        	if (src != dst) {
        		for (int i=0; i<len; i++) {
            		E swap = src.doGet(srcIndex+i);
            		swap = dst.doSet(dstIndex+i, swap);
            		src.doSet(srcIndex+i, swap);
        		}
        	}
        }
    }


    // --- Bulk methods ---

    // -- Readers --

    /**
     * Create list with specified capacity.
     *
     * @param capacity	initial capacity (use -1 for default capacity)
     * @return			created list
     */
    abstract protected IList<E> doCreate(int capacity);

    /**
     * Assign this list the content of the that list.
     * This is done by bitwise copying so the that list should not be used afterwards.
     *
     * @param that list to copy content from
     */
	abstract protected void doAssign(IList<E> that);


    /**
     * Returns specified range of elements from list.
     *
     * @param index index of first element to retrieve
     * @param len   number of elements to retrieve
     * @return      list containing the specified range of elements
     */
    public IList<E> getAll(int index, int len) {
        checkRange(index, len);

        IList<E> list = doCreate(len);
        for (int i=0; i<len; i++) {
            list.add(doGet(index+i));
        }
        return list;
    }

    /**
     * Removes specified range of elements from list and return them.
     *
     * @param index index of first element to retrieve
     * @param len   number of elements to retrieve
     * @return      list containing the specified range of elements
     */
    public IList<E> extract(int index, int len) {
        checkRange(index, len);

        IList<E> list = doCreate(len);
        for (int i=0; i<len; i++) {
            list.add(doGet(index+i));
        }
        remove(index, len);
        return list;
    }

    /**
     * Returns specified range of elements from list as array.
     *
     * @param index index of first element to retrieve
     * @param len   number of elements to retrieve
     * @return      array containing the specified range of elements
     */
    public E[] getArray(int index, int len) {
        checkRange(index, len);

        @SuppressWarnings("unchecked")
        E[] array = (E[]) new Object[len];
        for (int i=0; i<len; i++) {
            array[i] = doGet(index+i);
        }
        return array;
    }

    // -- Mutators --

    /**
     * Replaces the specified elements.
     *
     * @param index index of first element to set
     * @param list  list with elements to set
     * @throws 		IndexOutOfBoundsException if the range is invalid
     */
    public void setAll(int index, IList<? extends E> list) {
    	// There is a special implementation accepting an IList
    	// so the method is also available in the primitive classes.
	    int size = list.size();
        checkRange(index, size);

        for (int i=0; i<size; i++) {
            doSet(index+i, list.get(i));
        }
    }

    /**
     * Replaces the specified elements.
     *
     * @param index index of first element to set
     * @param coll  collection with elements to set
     */
    public void setAll(int index, Collection<? extends E> coll) {
        checkRange(index, coll.size());

        // In contrary to addAll() there is no need to first create an array
        // containing the collection elements, as the list will not grow.
        int i = 0;
        Iterator<? extends E> iter = coll.iterator();
        while (iter.hasNext()) {
            doSet(index+i, iter.next());
            i++;
        }
    }

    /**
     * Set or add the specified elements.
     *
     * @param index index of first element to set or add
     * @param coll  collection with elements to set or add
     */
    public void merge(int index, Collection<? extends E> coll) {
        checkIndexAdd(index);

        // In contrary to addAll() there is no need to first create an array
        // containing the collection elements, as the list will not grow.
        int size = size();
        Iterator<? extends E> iter = coll.iterator();
        while (iter.hasNext()) {
        	E val = iter.next();
        	if (index < size) {
        		doSet(index, val);
        	} else {
        		doAdd(index, val);
        	}
    		index++;
        }
    }

    /**
     * Replaces the specified elements.
     *
     * @param index index of first element to set
     * @param elems elements to set
     * @throws 		IndexOutOfBoundsException if the range is invalid
     */
    public void setAll(int index, E... elems) {
        checkRange(index, elems.length);

        doSetAll(index, elems);
    }

    /**
     * Replaces the specified elements.
     *
     * @param index index of first element to set
     * @param elems elements to set
     */
    protected void doSetAll(int index, E[] elems) {
        for (int i=0; i<elems.length; i++) {
            doSet(index+i, elems[i]);
        }
    }

	/**
	 * Remove specified range of elements from list.
	 *
	 * @param index	index of first element to remove
	 * @param len	number of elements to remove
     * @throws 		IndexOutOfBoundsException if the range is invalid
	 */
	public void remove(int index, int len) {
    	checkRange(index, len);

    	doRemoveAll(index, len);
	}

    /**
     * Remove specified range of elements from list.
     *
     * @param index index of first element to remove
     * @param len   number of elements to remove
     */
	protected void doRemoveAll(int index, int len) {
		for (int i=index+len-1; i>=index; i--) {
			doRemove(i);
		}
	}

	/**
	 * Initializes the list so it will afterwards have a size of
	 * <code>len</code> and contain only the element <code>elem</code>.
	 * The list will grow or shrink as needed.
	 *
	 * @param len  	length of list
	 * @param elem 	element which the list will contain
     * @throws 		IndexOutOfBoundsException if the range is invalid
	 */
	public void init(int len, E elem) {
	    checkLength(len);

	    int size = size();
        if (len < size) {
            remove(len, size-len);
            fill(0, len, elem);
        } else {
            fill(0, size, elem);
            for (int i=size; i<len; i++) {
                add(elem);
            }
        }
        assert(size() == len);
	}

	/**
     * Resizes the list so it will afterwards have a size of
     * <code>len</code>. If the list must grow, the specified
     * element <code>elem</code> will be used for filling.
     *
     * @param len  	length of list
     * @param elem 	element which will be used for extending the list
     * @throws 	 	IndexOutOfBoundsException if the range is invalid
	 */
	public void resize(int len, E elem) {
	    checkLength(len);

	    int size = size();
        if (len < size) {
            remove(len, size-len);
        } else {
            for (int i=size; i<len; i++) {
                add(elem);
            }
        }
        assert(size() == len);
	}

    /**
     * Fill list.
     *
     * @param elem  element used for filling
     */
    // see java.util.Arrays#fill
    public void fill(E elem) {
    	int size = size();
        for (int i=0; i<size; i++) {
            doSet(i, elem);
        }
    }

    /**
     * Fill specified elements.
     *
     * @param index	index of first element to fill
     * @param len	number of elements to fill
     * @param elem	element used for filling
     * @throws 		IndexOutOfBoundsException if the range is invalid
     */
    // see java.util.Arrays#fill
    public void fill(int index, int len, E elem) {
    	checkRange(index, len);

    	for (int i=0; i<len; i++) {
    		doSet(index+i, elem);
    	}
    }

    /**
     * Copy specified elements.
     * Source and destination ranges may overlap.
     * The size of the list does not change.
     *
     * @param srcIndex	index of first source element to copy
     * @param dstIndex	index of first destination element to copy
     * @param len		number of elements to copy
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public void copy(int srcIndex, int dstIndex, int len) {
    	checkRange(srcIndex, len);
    	checkRange(dstIndex, len);

    	if (srcIndex < dstIndex) {
    		for (int i=len-1; i>=0; i--) {
    			doReSet(dstIndex+i, doGet(srcIndex+i));
    		}
    	} else if (srcIndex > dstIndex) {
    		for (int i=0; i<len; i++) {
    			doReSet(dstIndex+i, doGet(srcIndex+i));
    		}
    	}
    }

    /**
     * Move specified elements.
     * Source and destination ranges may overlap.
     * The elements which are moved away are set to null, so the size of the list does not change.
     *
     * @param srcIndex	index of first source element to move
     * @param dstIndex	index of first destination element to move
     * @param len		number of elements to move
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public void move(int srcIndex, int dstIndex, int len) {
    	checkRange(srcIndex, len);
    	checkRange(dstIndex, len);

    	// Copy elements
    	if (srcIndex < dstIndex) {
    		for (int i=len-1; i>=0; i--) {
    			doReSet(dstIndex+i, doGet(srcIndex+i));
    		}
    	} else if (srcIndex > dstIndex) {
    		for (int i=0; i<len; i++) {
    			doReSet(dstIndex+i, doGet(srcIndex+i));
    		}
    	}

    	// Set elements to null after the move operation
    	if (srcIndex < dstIndex) {
    		int fill = Math.min(len, dstIndex-srcIndex);
    		fill(srcIndex, fill, null);
    	} else if (srcIndex > dstIndex) {
    		int fill = Math.min(len, srcIndex-dstIndex);
    		fill(srcIndex+len-fill, fill, null);
    	}
    }

    /**
     * Drag specified elements.
     * Source and destination ranges may overlap.
     * The size of the list does not change and it contains the same elements as before, but in changed order.
     *
     * @param srcIndex	index of first source element to move
     * @param dstIndex	index of first destination element to move
     * @param len		number of elements to move
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public void drag(int srcIndex, int dstIndex, int len) {
    	checkRange(srcIndex, len);
    	checkRange(dstIndex, len);

    	if (srcIndex < dstIndex) {
    		doRotate(srcIndex, len+(dstIndex-srcIndex), dstIndex-srcIndex);
    	} else if (srcIndex > dstIndex) {
    		doRotate(dstIndex, len+(srcIndex-dstIndex), dstIndex-srcIndex);
    	}
    }

    /**
     * Reverses the order of all elements in the specified list.
     */
    public void reverse() {
    	reverse(0, size());
    }

    /**
     * Reverses the order of the specified elements in the list.
     *
     * @param index	index of first element to reverse
     * @param len	number of elements to reverse
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public void reverse(int index, int len) {
    	checkRange(index, len);

    	int pos1 = index;
		int pos2 = index+len-1;
    	int mid = len / 2;
    	for (int i=0; i<mid; i++) {
    		E swap = doGet(pos1);
    		swap = doReSet(pos2, swap);
    		doReSet(pos1, swap);
    		pos1++;
    		pos2--;
    	}
    }

    /**
     * Swap the specified elements in the list.
     *
     * @param index1	index of first element in first range to swap
     * @param index2	index of first element in second range to swap
     * @param len		number of elements to swap
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public void swap(int index1, int index2, int len) {
    	checkRange(index1, len);
    	checkRange(index2, len);
    	if ((index1 < index2 && index1+len > index2) ||
    		index1 > index2 && index2+len > index1) {
    		throw new IndexOutOfBoundsException("Swap ranges overlap");
    	}

    	for (int i=0; i<len; i++) {
    		E swap = doGet(index1+i);
    		swap = doReSet(index2+i, swap);
    		doReSet(index1+i, swap);
    	}
    }

    /**
     * Rotate specified elements in the list.
     * The distance argument can be positive or negative:
     * If it is positive, the elements are moved towards the end,
     * if negative, the elements are moved toward the beginning,
     * if distance is 0, the list is not changed.
     *
     * @param distance	distance to move the elements
     */
    public void rotate(int distance) {
    	rotate(0, size(), distance);
    }

    /**
     * Rotate specified elements in the list.
     * The distance argument can be positive or negative:
     * If it is positive, the elements are moved towards the end,
     * if negative, the elements are moved toward the beginning,
     * if distance is 0, the list is not changed.
     *
     * @param index		index of first element to rotate
     * @param len		number of elements to rotate
     * @param distance	distance to move the elements
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public void rotate(int index, int len, int distance) {
    	checkRange(index, len);
    	doRotate(index, len, distance);
    }

    /**
     * Internal method to rotate specified elements in the list.
     * The distance argument can be positive or negative:
     * If it is positive, the elements are moved towards the end,
     * if negative, the elements are moved toward the beginning,
     * if distance is 0, the list is not changed.
     *
     * @param index		index of first element to rotate
     * @param len		number of elements to rotate
     * @param distance	distance to move the elements
     */
    protected void doRotate(int index, int len, int distance) {
        distance = distance % len;
        if (distance < 0) {
            distance += len;
        }
        if (distance == 0) {
            return;
        }
        assert(distance >= 0 && distance < len);

        int num = 0;
        for (int start=0; num != len; start++) {
            E elem = doGet(index+start);
            int i = start;
            do {
                i += distance;
                if (i >= len) {
                    i -= len;
                }
                elem = doReSet(index+i, elem);
                num++;
            } while (i != start);
        }
    }

    /**
     * Sort elements in the list using the specified comparator.
     *
     * @param comparator	comparator to use for sorting
     * 						(null means the elements natural ordering should be used)
     *
     * @see Arrays#sort
     */
    public void sort(Comparator<? super E> comparator) {
    	sort(0, size(), comparator);
    }

    /**
     * Sort specified elements in the list using the specified comparator.
     *
     * @param index			index of first element to sort
     * @param len			number of elements to sort
     * @param comparator	comparator to use for sorting
     * 						(null means the elements natural ordering should be used)
     * @throws 				IndexOutOfBoundsException if the range is invalid
     *
     * @see Arrays#sort
     */
    abstract public void sort(int index, int len, Comparator<? super E> comparator);

    /*
    Question:
       Why is the signature of method binarySearch
           public <K> int binarySearch(K key, Comparator<? super K> comparator)
       and not
           public int binarySearch(E key, Comparator<? super E> comparator)
       as you could expect?

    Answer:
       This allows to use the binarySearch method not only with keys of
       the type stored in the GapList, but also with any other type you
       are prepared to handle in you Comparator.
       So if we have a class Name and its comparator as defined in the
       following code snippets, both method calls are possible:

       new GapList<Name>().binarySearch(new Name("a"), new NameComparator());
       new GapList<Name>().binarySearch("a", new NameComparator());

       class Name {
           String name;

           public Name(String name) {
               this.name = name;
           }
           public String getName() {
               return name;
           }
           public String toString() {
               return name;
           }
       }

       static class NameComparator implements Comparator<Object> {
           @Override
           public int compare(Object o1, Object o2) {
               String s1;
               if (o1 instanceof String) {
                   s1 = (String) o1;
               } else {
                   s1 = ((Name) o1).getName();
               }
               String s2;
               if (o2 instanceof String) {
                   s2 = (String) o2;
               } else {
                   s2 = ((Name) o2).getName();
               }
               return s1.compareTo(s2);
           }
        }
    */

    /**
     * Searches the specified range for an object using the binary
     * search algorithm.
     *
     * @param key           the value to be searched for
     * @param comparator    the comparator by which the list is ordered.
     *                      A <tt>null</tt> value indicates that the elements'
     *                      {@linkplain Comparable natural ordering} should be used.
     * @return              index of the search key, if it is contained in the array;
     *                      otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *                      <i>insertion point</i> is defined as the point at which the
     *                      key would be inserted into the array: the index of the first
     *                      element greater than the key, or <tt>a.length</tt> if all
     *                      elements in the array are less than the specified key.  Note
     *                      that this guarantees that the return value will be &gt;= 0 if
     *                      and only if the key is found.
     *
     * @see Arrays#binarySearch
     */
    public <K> int binarySearch(K key, Comparator<? super K> comparator) {
    	return binarySearch(0, size(), key, comparator);
    }

    /**
     * Searches the specified range for an object using the binary
     * search algorithm.
     *
     * @param index         index of first element to search
     * @param len           number of elements to search
     * @param key           the value to be searched for
     * @param comparator    the comparator by which the list is ordered.
     *                      A <tt>null</tt> value indicates that the elements'
     *                      {@linkplain Comparable natural ordering} should be used.
     * @return              index of the search key, if it is contained in the array;
     *                      otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *                      <i>insertion point</i> is defined as the point at which the
     *                      key would be inserted into the array: the index of the first
     *                      element greater than the key, or <tt>a.length</tt> if all
     *                      elements in the array are less than the specified key.  Note
     *                      that this guarantees that the return value will be &gt;= 0 if
     *                      and only if the key is found.
     * @throws 				IndexOutOfBoundsException if the range is invalid
     *
     * @see Arrays#binarySearch
     */
    abstract public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator);

    //--- Arguments check methods

    /**
     * Check that specified index is valid for getting/setting elements.
     *
     * @param index	index to check
     * @throws IndexOutOfBoundsException if index is invalid
     */
    protected void checkIndex(int index) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException("Invalid index: " + index + " (size: " + size() + ")");
		}
	}

    /**
     * Check that specified index is valid for adding elements.
     *
     * @param index	index to check
     * @throws IndexOutOfBoundsException if index is invalid
     */
	protected void checkIndexAdd(int index) {
		if (index < 0 || index > size()) {
			throw new IndexOutOfBoundsException("Invalid index: " + index + " (size: " + size() + ")");
		}
	}

	/**
	 * Check that specified range is valid.
	 *
	 * @param index	start index of range to check
	 * @param len	number of elements  in range to check
     * @throws IndexOutOfBoundsException if index is invalid
	 */
	protected void checkRange(int index, int len) {
		if (index < 0 || len < 0 || index+len > size()) {
			throw new IndexOutOfBoundsException("Invalid range: " + index + "/" + len + " (size: " + size() + ")");
		}
	}

    /**
     * Check that specified length is valid (>= 0).
     *
     * @param length length to check
     * @throws IndexOutOfBoundsException if length is invalid
     */
    protected void checkLength(int length) {
        if (length < 0) {
            throw new IndexOutOfBoundsException("Invalid length: " + length);
        }
    }

    // --- Start class Iter ---

    /**
     * Iterator supports forward and reverse iteration.
     */
    class Iter implements Iterator<E> {
    	/** true if iterator moves forward */
    	boolean forward;
    	/** current index */
    	int index;
    	/** index where element will be removed if remove() is called */
    	int remove;

    	/**
    	 * Constructor.
    	 *
    	 * @param forward	true for forward access
    	 */
    	public Iter(boolean forward) {
    		this.forward = forward;

    		if (forward) {
    			index = 0;
    		} else {
    			index = size()-1;
    		}
    		remove = -1;
    	}

		@Override
		public boolean hasNext() {
			if (forward) {
				return index != size();
			} else {
				return index != -1;
			}
		}

		@Override
		public E next() {
			if (forward) {
				if (index >= size()) {
					throw new NoSuchElementException();
				}
			} else {
				if (index < 0) {
					throw new NoSuchElementException();
				}
			}
			E elem = get(index);
			remove = index;
			if (forward) {
				index++;
			} else {
				index--;
			}
			return elem;
		}

		@Override
		public void remove() {
			if (remove == -1) {
				throw new IllegalStateException("No current element to remove");
			}
			IList.this.remove(remove);
			if (index > remove) {
				index--;
			}
			remove = -1;
		}
    }

    // --- End class Iter ---

    // --- Start class ListIter ---

    /**
     * Iterator supports forward and reverse iteration.
     */
    class ListIter implements ListIterator<E> {
    	/** current index */
    	int index;
    	/** index where element will be removed if remove() is called */
    	int remove;

    	/**
    	 * Constructor.
    	 *
    	 * @param index	start index
    	 */
    	public ListIter(int index) {
    		checkIndexAdd(index);
   			this.index = index;
    		this.remove = -1;
    	}

		@Override
		public boolean hasNext() {
			return index < size();
		}

		@Override
		public boolean hasPrevious() {
			return index > 0;
		}

		@Override
		public E next() {
			if (index >= size()) {
				throw new NoSuchElementException();
			}
			E elem = IList.this.get(index);
			remove = index;
			index++;
			return elem;
		}

		@Override
		public int nextIndex() {
			return index;
		}

		@Override
		public E previous() {
			if (index <= 0) {
				throw new NoSuchElementException();
			}
			index--;
			E elem = IList.this.get(index);
			remove = index;
			return elem;
		}

		@Override
		public int previousIndex() {
			return index-1;
		}

		@Override
		public void remove() {
			if (remove == -1) {
				throw new IllegalStateException("No current element to remove");
			}
			IList.this.remove(remove);
			if (index > remove) {
				index--;
			}
			remove = -1;
		}

		@Override
		public void set(E e) {
			if (remove == -1) {
				throw new IllegalStateException("No current element to set");
			}
			IList.this.set(remove, e);
		}

		@Override
		public void add(E e) {
			IList.this.add(index, e);
			index++;
			remove = -1;
		}
    }

    // --- End class ListIter ---

}
