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
 * $Id: GapList.java 2098 2014-01-13 00:37:08Z origo $
 */
package org.magicwerk.brownies.collections.primitive;

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

/**
 * GapList combines the strengths of both ArrayList and LinkedList.
 * It is implemented to offer both efficient random access to elements
 * by index (as ArrayList does) and at the same time efficient adding
 * and removing elements to and from beginning and end (as LinkedList does).
 * It also exploits the locality of reference often seen in applications
 * to further improve performance, e.g. for iterating over the list.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * </p>
 *
 * @author Thomas Mauch
 * @version $Id: GapList.java 2098 2014-01-13 00:37:08Z origo $
 *
 * @param <E> type of elements stored in the list
 * @see	    java.util.List
 * @see	    java.util.ArrayList
 * @see	    java.util.LinkedList
 */
public abstract class IDoubleGapList<E> implements Cloneable, Serializable {

    /**
	 * Copies the collection values into an array.
	 *
	 * @param coll   collection of values
	 * @return       array containing the collection values
	 */
static double[] toArray(Collection<Double> coll) {
    Object[] values = coll.toArray();
    double[] v = new double[values.length];
    for (int i = 0; i < values.length; i++) {
        v[i] = (Double) values[i];
    }
    return v;
}

    /**
     * Returns a shallow copy of this <tt>GapList</tt> instance
     * (the new list will contain the same elements as the source list, i.e. the elements themselves are not copied).
     * This method is identical to clone() except that the result is casted to GapList.
     *
     * @return a clone of this instance
     * @see #clone
     */
@SuppressWarnings("unchecked")
public IDoubleGapList copy() {
    return (IDoubleGapList) clone();
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
public abstract IDoubleGapList unmodifiableList();

    /**
     * Returns a shallow copy of this <tt>GapList</tt> instance
     * (The elements themselves are not copied).
     * The capacity of the list will be set to the number of elements,
     * so after calling clone(), size and capacity are equal.
     *
     * @return a clone of this <tt>GapList</tt> instance
     */
@SuppressWarnings("unchecked")

public Object clone() {
    try {
        IDoubleGapList list = (IDoubleGapList) super.clone();
        list.initClone(this);
        return list;
    } catch (CloneNotSupportedException e) {
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
protected abstract void initClone(IDoubleGapList that);

    
public void clear() {
    doRemoveAll(0, size());
}

    
public abstract int size();

    
public double get(int index) {
    checkIndex(index);
    return doGet(index);
}

    /**
     * Helper method for getting an element from the GapList.
     * This is the only method which really gets an element.
     * Override if you need to validity checks before getting.
     *
     * @param index index of element to return
     * @return      the element at the specified position in this list
     */
protected abstract double doGet(int index);

    /**
     * Helper method for setting an element in the GapList.
     * This is the only method which really sets an element.
     * Override if you need to validity checks before setting.
     *
     * @param index index where element will be placed
     * @param elem  element to set
     * @return      old element which was at the position
     */
protected abstract double doSet(int index, double elem);

    
public double set(int index, double elem) {
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
protected abstract double doReSet(int index, double elem);

    protected abstract double getDefaultElem();

    /**
     * This method is called internally before elements are allocated or freed.
     * Override if you need to validity checks.
     */
protected void doModify() {
}

    
public boolean add(double elem) {
    return doAdd(-1, elem);
}

    
public void add(int index, double elem) {
    checkIndexAdd(index);
    doAdd(index, elem);
}

    /**
	 * Helper method for adding an element to the GapList.
	 * This is the only method which really adds an element.
	 * Override if you need to validity checks before adding.
	 *
	 * @param index	index where element should be added
	 *              (-1 means it is up to the implementation to choose the index)
	 * @param elem	element to add
	 * @return      true if element has been added (GapList.add() will always return true)
	 */
protected abstract boolean doAdd(int index, double elem);

    
public double remove(int index) {
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
protected abstract double doRemove(int index);

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
     * Increases the capacity of this <tt>GapList</tt> instance, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity
     */
protected abstract void doEnsureCapacity(int minCapacity);

    /**
     * Trims the capacity of this <tt>GapList</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>GapList</tt> instance.
     */
// Note: Provide this method to make transition from ArrayList as  
//       smooth as possible  
public abstract void trimToSize();

    
public boolean equals(Object obj) {
    if (obj == this) {
        return true;
    }
    if (obj instanceof DoubleObjGapList) {
        obj = ((DoubleObjGapList) obj).list;
    }
    if (!(obj instanceof DoubleGapList)) {
        return false;
    }
    @SuppressWarnings("unchecked") DoubleGapList list = (DoubleGapList) obj;
    int size = size();
    if (size != list.size()) {
        return false;
    }
    for (int i = 0; i < size; i++) {
        if (!equalsElem(doGet(i), list.get(i))) {
            return false;
        }
    }
    return true;
}

    
public int hashCode() {
    int hashCode = 1;
    int size = size();
    for (int i = 0; i < size; i++) {
        double elem = doGet(i);
        hashCode = 31 * hashCode + hashCodeElem(elem);
    }
    return hashCode;
}

    
public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("[");
    int size = size();
    for (int i = 0; i < size; i++) {
        if (i > 0) {
            buf.append(", ");
        }
        buf.append(doGet(i));
    }
    buf.append("]");
    return buf.toString();
}

    
public boolean isEmpty() {
    return size() == 0;
}

    /**
	 * Helper function to check two elements stored in the GapList
	 * for equality.
	 *
	 * @param elem1	first element
	 * @param elem2	second element
	 * @return		true if the elements are equal, otherwise false
	 */
static boolean equalsElem(double elem1, double elem2) {
    return elem1 == elem2;
}

    /**
	 * Helper method to calculate hash code of a element stored in
	 * the GapList.
	 *
	 * @param elem	element
	 * @return		hash code for element
	 */
static int hashCodeElem(double elem) {
    return (int) elem;
}

    /**
	 * Counts how many times the specified element is contained in the list.
	 *
	 * @param elem	element to count
	 * @return		count how many times the specified element is contained in the list
	 */
public int getCount(double elem) {
    int count = 0;
    int size = size();
    for (int i = 0; i < size; i++) {
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
public IDoubleGapList getAll(double elem) {
    IDoubleGapList list = doCreate(-1);
    int size = size();
    for (int i = 0; i < size; i++) {
        double e = doGet(i);
        if (equalsElem(e, elem)) {
            list.add(e);
        }
    }
    return list;
}

    /**
	 * Returns distinct elements in the list.
	 *
	 * @return		distinct elements in the list
	 */
public Set getDistinct() {
    Set set = new HashSet();
    int size = size();
    for (int i = 0; i < size; i++) {
        set.add(doGet(i));
    }
    return set;
}

    
public int indexOf(double elem) {
    int size = size();
    for (int i = 0; i < size; i++) {
        if (equalsElem(doGet(i), elem)) {
            return i;
        }
    }
    return -1;
}

    
public int lastIndexOf(double elem) {
    for (int i = size() - 1; i >= 0; i--) {
        if (equalsElem(doGet(i), elem)) {
            return i;
        }
    }
    return -1;
}

    
public boolean removeElem(double elem) {
    int index = indexOf(elem);
    if (index == -1) {
        return false;
    }
    doRemove(index);
    return true;
}

    
public boolean contains(double elem) {
    return indexOf(elem) != -1;
}

    /**
	 * Returns true if any of the elements of the specified collection is contained in the list.
	 *
	 * @param coll collection with elements to be contained
	 * @return     true if any element is contained, false otherwise
	 */
public boolean containsAny(Collection<Double> coll) {
    // Note that the signature has been chosen as in List:   
    // - boolean addAll(Collection<Double> c);   
    // - boolean containsAll(Collection<Double> c);   
    for (double elem : coll) {
        if (contains(elem)) {
            return true;
        }
    }
    return false;
}

    
public boolean containsAll(Collection<Double> coll) {
    // Note that this method is already implemented in AbstractCollection.   
    // It has been duplicated so the method is also available in the primitive classes.   
    for (double elem : coll) {
        if (!contains(elem)) {
            return false;
        }
    }
    return true;
}

    
public boolean removeAll(Collection<Double> coll) {
    // Note that this method is already implemented in AbstractCollection.   
    // It has been duplicated so the method is also available in the primitive classes.   
    boolean modified = false;
    int size = size();
    for (int i = 0; i < size; i++) {
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
public IDoubleGapList removeAll(double elem) {
    IDoubleGapList list = doCreate(-1);
    int size = size();
    for (int i = 0; i < size; i++) {
        double e = doGet(i);
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
public boolean removeAll(IDoubleGapList<?> coll) {
    // There is a special implementation accepting a GapList   
    // so the method is also available in the primitive classes.   
    boolean modified = false;
    int size = size();
    for (int i = 0; i < size; i++) {
        if (coll.contains(doGet(i))) {
            doRemove(i);
            size--;
            i--;
            modified = true;
        }
    }
    return modified;
}

    
public boolean retainAll(Collection<Double> coll) {
    // Note that this method is already implemented in AbstractCollection.   
    // It has been duplicated so the method is also available in the primitive classes.   
    boolean modified = false;
    int size = size();
    for (int i = 0; i < size; i++) {
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
public boolean retainAll(IDoubleGapList<?> coll) {
    // There is a special implementation accepting a GapList   
    // so the method is also available in the primitive classes.   
    boolean modified = false;
    int size = size();
    for (int i = 0; i < size; i++) {
        if (!coll.contains(doGet(i))) {
            doRemove(i);
            size--;
            i--;
            modified = true;
        }
    }
    return modified;
}

    
public double[] toArray() {
    int size = size();
    double[] array = new double[size];
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
public double[] toArray(int index, int len) {
    double[] array = new double[len];
    doGetAll(array, index, len);
    return array;
}

    @SuppressWarnings("unchecked")

public double[] toArray(double[] array) {
    int size = size();
    if (array.length < size) {
        array = (double[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
    }
    doGetAll(array, 0, size);
    if (array.length > size) {
        array[size] = 0;
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
protected void doGetAll(double[] array, int index, int len) {
    for (int i = 0; i < len; i++) {
        array[i] = doGet(index + i);
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

public boolean addAll(Collection<Double> coll) {
    // ArrayList.addAll() also first creates an array containing the   
    // collection elements. This guarantees that the list's capacity   
    // must only be increased once.   
    @SuppressWarnings("unchecked") double[] array = (double[]) toArray(coll);
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

public boolean addAll(int index, Collection<Double> coll) {
    checkIndexAdd(index);
    // ArrayList.addAll() also first creates an array containing the   
    // collection elements. This guarantees that the list's capacity   
    // must only be increased once.   
    @SuppressWarnings("unchecked") double[] array = (double[]) toArray(coll);
    return doAddAll(index, array);
}

    /**
     * Adds all specified elements into this list.
     *
     * @param elems elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     */
public boolean addAll(double... elems) {
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
public boolean addAll(int index, double... elems) {
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
public boolean addAll(IDoubleGapList list) {
    return doAddAll(-1, (double[]) list.toArray());
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
public boolean addAll(int index, IDoubleGapList list) {
    checkIndexAdd(index);
    return doAddAll(index, (double[]) list.toArray());
}

    /**
     * Helper method for adding multiple elements to the GapList.
     * It still calls doAdd() for adding each element.
     *
     * @param index index where element should be added
     *              (-1 is valid for adding at the end)
     * @param array array with elements to add
     * @return      true if elements have been added, false otherwise
     */
protected boolean doAddAll(int index, double[] array) {
    doEnsureCapacity(size() + array.length);
    if (array.length == 0) {
        return false;
    }
    for (double elem : array) {
        doAdd(index, elem);
        if (index != -1) {
            index++;
        }
    }
    return true;
}









    // Queue operations  

public double peek() {
    if (size() == 0) {
        return 0;
    }
    return getFirst();
}

    
public double element() {
    // inline version of getFirst():   
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doGet(0);
}

    
public double poll() {
    if (size() == 0) {
        return 0;
    }
    return doRemove(0);
}

    
public double remove() {
    // inline version of removeFirst():   
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doRemove(0);
}

    
public boolean offer(double elem) {
    // inline version of add(elem):   
    return doAdd(-1, elem);
}

    // Deque operations  

public double getFirst() {
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doGet(0);
}

    
public double getLast() {
    int size = size();
    if (size == 0) {
        throw new NoSuchElementException();
    }
    return doGet(size - 1);
}

    
public void addFirst(double elem) {
    doAdd(0, elem);
}

    
public void addLast(double elem) {
    // inline version of add(elem):   
    doAdd(-1, elem);
}

    
public double removeFirst() {
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doRemove(0);
}

    
public double removeLast() {
    int size = size();
    if (size == 0) {
        throw new NoSuchElementException();
    }
    return doRemove(size - 1);
}

    
public boolean offerFirst(double elem) {
    // inline version of addFirst(elem):   
    doAdd(0, elem);
    return true;
}

    
public boolean offerLast(double elem) {
    // inline version of addLast(elem):   
    doAdd(-1, elem);
    return true;
}

    
public double peekFirst() {
    if (size() == 0) {
        return 0;
    }
    return doGet(0);
}

    
public double peekLast() {
    int size = size();
    if (size == 0) {
        return 0;
    }
    return doGet(size - 1);
}

    
public double pollFirst() {
    if (size() == 0) {
        return 0;
    }
    return doRemove(0);
}

    
public double pollLast() {
    int size = size();
    if (size == 0) {
        return 0;
    }
    return doRemove(size - 1);
}

    
public double pop() {
    // inline version of removeFirst():   
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doRemove(0);
}

    
public void push(double elem) {
    // inline version of addFirst();   
    doAdd(0, elem);
}

    
public boolean removeFirstOccurrence(double elem) {
    int index = indexOf(elem);
    if (index == -1) {
        return false;
    }
    doRemove(index);
    return true;
}

    
public boolean removeLastOccurrence(double elem) {
    int index = lastIndexOf(elem);
    if (index == -1) {
        return false;
    }
    doRemove(index);
    return true;
}

    // --- Static bulk methods working with two GapLists ---  
/**
     * Moves elements from one GapList to another.
     *
     * @param src		source list
     * @param srcIndex	index of first element in source list
     * @param dst		destination list
     * @param dstIndex	index of first element in source list
     * @param len		number of elements to move
     * @param <E> 		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
public static void move(IDoubleGapList src, int srcIndex, IDoubleGapList<Double> dst, int dstIndex, int len) {
    if (src == dst) {
        src.move(srcIndex, dstIndex, len);
    } else {
        src.checkRange(srcIndex, len);
        dst.checkRange(dstIndex, len);
        double defaultElem = src.getDefaultElem();
        for (int i = 0; i < len; i++) {
            double elem = src.doReSet(srcIndex + i, defaultElem);
            dst.doSet(dstIndex + i, elem);
        }
    }
}

    /**
     * Copies elements from one GapList to another.
     *
     * @param src		source list
     * @param srcIndex	index of first element in source list
     * @param dst		destination list
     * @param dstIndex	index of first element in source list
     * @param len		number of elements to copy
     * @param <E> 		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
public static void copy(IDoubleGapList src, int srcIndex, IDoubleGapList dst, int dstIndex, int len) {
    if (src == dst) {
        src.copy(srcIndex, dstIndex, len);
    } else {
        src.checkRange(srcIndex, len);
        dst.checkRange(dstIndex, len);
        for (int i = 0; i < len; i++) {
            double elem = src.doGet(srcIndex + i);
            dst.doSet(dstIndex + i, elem);
        }
    }
}

    /**
     * Swaps elements from two GapLists.
     *
     * @param src		first list
     * @param srcIndex	index of first element in first list
     * @param dst		second list
     * @param dstIndex	index of first element in second list
     * @param len		number of elements to swap
     * @param <E> 		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
public static void swap(IDoubleGapList src, int srcIndex, IDoubleGapList dst, int dstIndex, int len) {
    if (src == dst) {
        src.swap(srcIndex, dstIndex, len);
    } else {
        src.checkRange(srcIndex, len);
        dst.checkRange(dstIndex, len);
        if (src != dst) {
            for (int i = 0; i < len; i++) {
                double swap = src.doGet(srcIndex + i);
                swap = dst.doSet(dstIndex + i, swap);
                src.doSet(srcIndex + i, swap);
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
public abstract IDoubleGapList doCreate(int capacity);

    /**
     * Returns specified range of elements from list.
     *
     * @param index index of first element to retrieve
     * @param len   number of elements to retrieve
     * @return      GapList containing the specified range of elements from list
     */
public IDoubleGapList getAll(int index, int len) {
    checkRange(index, len);
    IDoubleGapList list = doCreate(len);
    for (int i = 0; i < len; i++) {
        list.add(doGet(index + i));
    }
    return list;
}

    /**
     * Returns specified range of elements from list.
     *
     * @param index index of first element to retrieve
     * @param len   number of elements to retrieve
     * @return      GapList containing the specified range of elements from list
     */
public double[] getArray(int index, int len) {
    checkRange(index, len);
    @SuppressWarnings("unchecked") double[] array = (double[]) new double[len];
    for (int i = 0; i < len; i++) {
        array[i] = doGet(index + i);
    }
    return array;
}

    // -- Mutators --  
/**
     * Replaces the specified elements.
     *
     * @param index index of first element to set
     * @param list  list with elements to set
     */
public void setAll(int index, IDoubleGapList list) {
    // There is a special implementation accepting a GapList   
    // so the method is also available in the primitive classes.   
    int size = list.size();
    checkRange(index, size);
    for (int i = 0; i < size; i++) {
        doSet(index + i, list.get(i));
    }
}

    /**
     * Replaces the specified elements.
     *
     * @param index index of first element to set
     * @param coll  collection with elements to set
     */
public void setAll(int index, Collection<Double> coll) {
    checkRange(index, coll.size());
    // In contrary to addAll() there is no need to first create an array   
    // containing the collection elements, as the list will not grow.   
    int i = 0;
    Iterator<Double> iter = coll.iterator();
    while (iter.hasNext()) {
        doSet(index + i, iter.next());
        i++;
    }
}

    /**
     * Replaces the specified elements.
     *
     * @param index index of first element to set
     * @param elems elements to set
     */
public void setAll(int index, double... elems) {
    checkRange(index, elems.length);
    doSetAll(index, elems);
}

    /**
     * Replaces the specified elements.
     *
     * @param index index of first element to set
     * @param elems elements to set
     */
protected void doSetAll(int index, double[] elems) {
    for (int i = 0; i < elems.length; i++) {
        doSet(index + i, elems[i]);
    }
}

    /**
	 * Remove specified range of elements from list.
	 *
	 * @param index	index of first element to remove
	 * @param len	number of elements to remove
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
    for (int i = index + len - 1; i >= index; i--) {
        doRemove(i);
    }
}

    /**
	 * Initializes the list so it will afterwards have a size of
	 * <code>len</code> and contain only the element <code>elem</code>.
	 * The list will grow or shrink as needed.
	 *
	 * @param len  length of list
	 * @param elem element which the list will contain
	 */
public void init(int len, double elem) {
    checkLength(len);
    int size = size();
    if (len < size) {
        remove(len, size - len);
        fill(0, len, elem);
    } else {
        fill(0, size, elem);
        for (int i = size; i < len; i++) {
            add(elem);
        }
    }
    assert (size() == len);
}

    /**
     * Resizes the list so it will afterwards have a size of
     * <code>len</code>. If the list must grow, the specified
     * element <code>elem</code> will be used for filling.
     *
     * @param len  length of list
     * @param elem element which will be used for extending the list
	 */
public void resize(int len, double elem) {
    checkLength(len);
    int size = size();
    if (len < size) {
        remove(len, size - len);
    } else {
        for (int i = size; i < len; i++) {
            add(elem);
        }
    }
    assert (size() == len);
}

    /**
     * Fill list.
     *
     * @param elem  element used for filling
     */
// see java.util.Arrays#fill  
public void fill(double elem) {
    int size = size();
    for (int i = 0; i < size; i++) {
        doSet(i, elem);
    }
}

    /**
     * Fill specified elements.
     *
     * @param index	index of first element to fill
     * @param len	number of elements to fill
     * @param elem	element used for filling
     */
// see java.util.Arrays#fill  
public void fill(int index, int len, double elem) {
    checkRange(index, len);
    for (int i = 0; i < len; i++) {
        doSet(index + i, elem);
    }
}

    /**
     * Copy specified elements.
     * Source and destination ranges may overlap.
     *
     * @param srcIndex	index of first source element to copy
     * @param dstIndex	index of first destination element to copy
     * @param len		number of elements to copy
     */
public void copy(int srcIndex, int dstIndex, int len) {
    checkRange(srcIndex, len);
    checkRange(dstIndex, len);
    if (srcIndex < dstIndex) {
        for (int i = len - 1; i >= 0; i--) {
            doReSet(dstIndex + i, doGet(srcIndex + i));
        }
    } else if (srcIndex > dstIndex) {
        for (int i = 0; i < len; i++) {
            doReSet(dstIndex + i, doGet(srcIndex + i));
        }
    }
}

    /**
     * Move specified elements.
     * The elements which are moved away are set to null.
     * Source and destination ranges may overlap.
     *
     * @param srcIndex	index of first source element to move
     * @param dstIndex	index of first destination element to move
     * @param len		number of elements to move
     */
public void move(int srcIndex, int dstIndex, int len) {
    checkRange(srcIndex, len);
    checkRange(dstIndex, len);
    // Copy elements    
    if (srcIndex < dstIndex) {
        for (int i = len - 1; i >= 0; i--) {
            doReSet(dstIndex + i, doGet(srcIndex + i));
        }
    } else if (srcIndex > dstIndex) {
        for (int i = 0; i < len; i++) {
            doReSet(dstIndex + i, doGet(srcIndex + i));
        }
    }
    // Set elements to 0 after the move operation    
    if (srcIndex < dstIndex) {
        int fill = Math.min(len, dstIndex - srcIndex);
        fill(srcIndex, fill, 0);
    } else if (srcIndex > dstIndex) {
        int fill = Math.min(len, srcIndex - dstIndex);
        fill(srcIndex + len - fill, fill, 0);
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
     */
public void reverse(int index, int len) {
    checkRange(index, len);
    int pos1 = index;
    int pos2 = index + len - 1;
    int mid = len / 2;
    for (int i = 0; i < mid; i++) {
        double swap = doGet(pos1);
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
     * @throws 			IndexOutOfBoundsException if the ranges overlap
     */
public void swap(int index1, int index2, int len) {
    checkRange(index1, len);
    checkRange(index2, len);
    if ((index1 < index2 && index1 + len > index2) || index1 > index2 && index2 + len > index1) {
        throw new IllegalArgumentException("Swap ranges overlap");
    }
    for (int i = 0; i < len; i++) {
        double swap = doGet(index1 + i);
        swap = doReSet(index2 + i, swap);
        doReSet(index1 + i, swap);
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
     */
public void rotate(int index, int len, int distance) {
    checkRange(index, len);
    int size = size();
    distance = distance % size;
    if (distance < 0) {
        distance += size;
    }
    if (distance == 0) {
        return;
    }
    int num = 0;
    for (int start = 0; num != size; start++) {
        double elem = doGet(index + start);
        int i = start;
        do {
            i += distance;
            if (i >= len) {
                i -= len;
            }
            elem = doReSet(index + i, elem);
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
public void sort() {
    sort(0, size());
}

    /**
     * Sort specified elements in the list using the specified comparator.
     *
     * @param index			index of first element to sort
     * @param len			number of elements to sort
     * @param comparator	comparator to use for sorting
     * 						(null means the elements natural ordering should be used)
     *
     * @see Arrays#sort
     */
public abstract void sort(int index, int len);

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
       new GapList<Name>().binarySearch("a), new NameComparator());

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
public int binarySearch(double key) {
    return binarySearch(0, size(), key);
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
     *
     * @see Arrays#binarySearch
     */
public abstract int binarySearch(int index, int len, double key);

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
    if (index < 0 || len < 0 || index + len > size()) {
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




}
