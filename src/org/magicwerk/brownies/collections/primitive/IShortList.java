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
 * $Id: IShortList.java 3834 2018-02-12 20:18:06Z origo $
 */
package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.GapList;


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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * IShortList is an abstract class which offers all interfaces offered by both ArrayList and LinkedList.
 * It also offers additional methods which are then available in all implementations of GapList and BigList.
 *
 * @author Thomas Mauch
 * @version $Id: IShortList.java 3834 2018-02-12 20:18:06Z origo $
 *
 * @param  type of elements stored in the list
 * @see	    java.util.List
 * @see	    java.util.Deque
 * @see	    java.util.ArrayList
 * @see	    java.util.LinkedList
 */
@SuppressWarnings("serial")
public abstract class IShortList implements Cloneable, Serializable {

    /**
	 * Copies the collection values into an array.
	 *
	 * @param coll   collection of values
	 * @return       array containing the collection values
	 */
static short[] toArray(Collection<Short> coll) {
    Object[] values = coll.toArray();
    short[] v = new short[values.length];
    for (int i = 0; i < values.length; i++) {
        v[i] = (Short) values[i];
    }
    return v;
}

    /**
     * Returns a shallow copy of this list instance.
     * (the new list will contain the same elements as the source list, i.e. the elements themselves are not copied).
     * This method is identical to clone() except that the result is casted to an IShortList.
     *
     * @return a clone of this instance
     * @see #clone
     */
@SuppressWarnings("unchecked")
public IShortList copy() {
    return (IShortList) clone();
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
public abstract IShortList unmodifiableList();

    /**
     * Returns a shallow copy of this list instance.
     * (The elements themselves are not copied).
     *
     * @return a clone of this list instance
     */
@SuppressWarnings("unchecked")

public Object clone() {
    try {
        IShortList list = (IShortList) super.clone();
        list.doClone(this);
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
protected abstract void doClone(IShortList that);

    
public void clear() {
    doClear();
}

    protected void doClear() {
    doRemoveAll(0, size());
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
public void resize(int len, short elem) {
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

    
public abstract int size();

    /**
	 * Returns capacity of this list.
	 * Note that two lists are considered equal even if they have a distinct capacity.
	 * Also the capacity can be changed by operations like clone() etc.
	 *
	 * @return capacity of this list
	 */
public abstract int capacity();

    
public short get(int index) {
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
protected abstract short doGet(int index);

    /**
     * Helper method for setting an element in the list.
     * This is the only method which really sets an element.
     * Override if you need to validity checks before setting.
     *
     * @param index index where element will be placed
     * @param elem  element to set
     * @return      old element which was at the position
     */
protected abstract short doSet(int index, short elem);

    
public short set(int index, short elem) {
    checkIndex(index);
    return doSet(index, elem);
}

    /**
     * Sets or adds the element.
     * If the index is smaller than the size of the list, the existing element is replaced.
     * If the index equals the size of the list, the element is added.
     *
     * @param index	index where element will be placed
     * @param elem	element to put
     * @return		old element if an element was replaced, null if the element was added
     */
public short put(int index, short elem) {
    checkIndexAdd(index);
    if (index < size()) {
        return doSet(index, elem);
    } else {
        doAdd(-1, elem);
        return (short) 0;
    }
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
protected abstract short doReSet(int index, short elem);

    protected abstract short getDefaultElem();

    /**
     * This method is called internally before elements are allocated or freed.
     * Override if you need to validity checks.
     */
protected void doModify() {
}

    
public boolean add(short elem) {
    return doAdd(-1, elem);
}

    
public void add(int index, short elem) {
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
protected abstract boolean doAdd(int index, short elem);

    
public short remove(int index) {
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
protected abstract short doRemove(int index);

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
protected abstract void doEnsureCapacity(int minCapacity);

    /**
     * An application can use this operation to minimize the storage of an instance.
     */
// Note: Provide this method to make transition from ArrayList as  
//       smooth as possible  
public abstract void trimToSize();

    
public boolean equals(Object obj) {
    if (obj == null) {
        return true;
    }
    if (obj instanceof ShortObjGapList) {
        obj = ((ShortObjGapList) obj).list;
    } else if (obj instanceof ShortObjBigList) {
        obj = ((ShortObjBigList) obj).list;
    }
    if (!(obj instanceof IShortList)) {
        return false;
    }
    @SuppressWarnings("unchecked") IShortList list = (IShortList) obj;
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
        short elem = doGet(i);
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
	 * Helper function to check two elements stored in the list for equality.
	 *
	 * @param elem1	first element
	 * @param elem2	second element
	 * @return		true if the elements are equal, otherwise false
	 */
static boolean equalsElem(short elem1, short elem2) {
    return elem1 == elem2;
}

    /**
	 * Helper method to calculate hash code of a element stored in the list.
	 *
	 * @param elem	element
	 * @return		hash code for element
	 */
static int hashCodeElem(short elem) {
    return (int) elem;
}

    /**
	 * Counts how many times the specified element is contained in the list.
	 *
	 * @param elem	element to count
	 * @return		count how many times the specified element is contained in the list
	 */
public int getCount(short elem) {
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
	 * Returns the only element stored in the list.
	 * If the list's size is not 1, a <code>NoSuchElementException</code> is thrown.
	 *
	 * @return	only element stored in the list
	 */
public short getSingle() {
    if (size() != 1) {
        throw new NoSuchElementException();
    }
    return doGet(0);
}

    /**
	 * Returns the only element stored in the list or null if the list is empty.
	 * If the list's size is greater than 1, a <code>NoSuchElementException</code> is thrown.
	 *
	 * @return	only element stored in the list
	 */
public short getSingleOrEmpty() {
    int size = size();
    if (size == 0) {
        return (short) 0;
    } else if (size == 1) {
        return doGet(0);
    } else {
        throw new NoSuchElementException();
    }
}

    /**
	 * Returns all elements in the list equal to the specified element.
	 *
	 * @param elem	element to look for
	 * @return		all elements in the list equal to the specified element
	 */
public IShortList getAll(short elem) {
    IShortList list = doCreate(-1);
    int size = size();
    for (int i = 0; i < size; i++) {
        short e = doGet(i);
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
public IShortList getIf(Predicate predicate) {
    IShortList list = doCreate(-1);
    int size = size();
    for (int i = 0; i < size; i++) {
        short e = doGet(i);
        if (predicate.test(e)) {
            list.add(e);
        }
    }
    return list;
}

    /**
	 * Removes all elements in the list which match the predicate.
	 *
     * @param predicate a predicate which returns {@code true} for elements to be removed
     * @return 			{@code true} if any elements were removed
	 */

public boolean removeIf(Predicate<Short> predicate) {
    boolean removed = false;
    int size = size();
    for (int i = 0; i < size; i++) {
        short e = doGet(i);
        if (predicate.test(e)) {
            doRemove(i);
            size--;
            i--;
            removed = true;
        }
    }
    return removed;
}

    /**
	 * Retains all elements in the list which match the predicate.
	 *
     * @param predicate a predicate which returns {@code true} for elements to be retained
     * @return 			{@code true} if any elements were removed
	 */
public boolean retainIf(Predicate<Short> predicate) {
    boolean modified = false;
    int size = size();
    for (int i = 0; i < size; i++) {
        short e = doGet(i);
        if (!predicate.test(e)) {
            doRemove(i);
            size--;
            i--;
            modified = true;
        }
    }
    return modified;
}

    /**
	 * Removes and returns all elements in the list which match the predicate.
	 *
	 * @param predicate	predicate
	 * @return			elements which have been removed from the list
	 */
public IShortList extractIf(Predicate<Short> predicate) {
    IShortList list = doCreate(-1);
    int size = size();
    for (int i = 0; i < size; i++) {
        short e = doGet(i);
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
public Set getDistinct() {
    Set set = new HashSet();
    int size = size();
    for (int i = 0; i < size; i++) {
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
public <R> IList<R> mappedList(Function<Short, R> mapper) {
    int size = size();
    @SuppressWarnings("unchecked") IList<R> mappedList = (IList<R>) new GapList<R>(size);
    for (int i = 0; i < size; i++) {
        short e = doGet(i);
        mappedList.add(mapper.apply(e));
    }
    return mappedList;
}

    public void map(UnaryOperator<Short> operator) {
    int size = size();
    for (int i = 0; i < size; i++) {
        short e = doGet(i);
        e = operator.apply(e);
        doSet(i, e);
    }
}

    /**
     * Filter the list using the specified predicate.
     * Only element which are allowed remain in the list, the others are removed
     *
     * @param predicate predicate used for filtering
     */
public void filter(Predicate<Short> predicate) {
    // It is typically faster to copy the allowed elements in a new list   
    // than to remove the not allowed from the existing one   
    IShortList list = doCreate(-1);
    int size = size();
    for (int i = 0; i < size; i++) {
        short e = doGet(i);
        if (predicate.test(e)) {
            list.add(e);
        }
    }
    doAssign(list);
}

    
public int indexOf(short elem) {
    int size = size();
    for (int i = 0; i < size; i++) {
        if (equalsElem(doGet(i), elem)) {
            return i;
        }
    }
    return -1;
}

    /**
	 * Returns the index of the first element which matches the specified element in this list.
	 *
	 * @param predicate		predicate used to search element
	 * @return				the index of the first element which matches the specified element,
	 * 						or -1 if this list does not contain the element
	 * @see #indexOf(Object)
	 */
public int indexOfIf(Predicate<Short> predicate) {
    int size = size();
    for (int i = 0; i < size; i++) {
        if (predicate.test(doGet(i))) {
            return i;
        }
    }
    return -1;
}

    
public int lastIndexOf(short elem) {
    for (int i = size() - 1; i >= 0; i--) {
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
public int indexOf(short elem, int fromIndex) {
    if (fromIndex < 0) {
        fromIndex = 0;
    }
    int size = size();
    for (int i = fromIndex; i < size; i++) {
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
public int lastIndexOf(short elem, int fromIndex) {
    int size = size();
    if (fromIndex >= size) {
        fromIndex = size - 1;
    }
    for (int i = fromIndex; i >= 0; i--) {
        if (equalsElem(doGet(i), elem)) {
            return i;
        }
    }
    return -1;
}

    
public boolean removeElem(short elem) {
    int index = indexOf(elem);
    if (index == -1) {
        return false;
    }
    doRemove(index);
    return true;
}

    
public boolean contains(short elem) {
    return indexOf(elem) != -1;
}

    /**
	 * 
	 * @param predicate
	 * @return
	 */
public boolean containsIf(Predicate<Short> predicate) {
    return indexOfIf(predicate) != -1;
}

    /**
	 * Add elements if it is not already contained in the list.
	 *
	 * @param elem	element to add
	 * @return		true if element has been added, false if not
	 */
// CopyOnWriteArrayList contains methods addIfAbsent() and addAllAbsent()  
public boolean addIfAbsent(short elem) {
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
public boolean containsAny(Collection<Short> coll) {
    // Note that the signature has been chosen as in List:   
    // - boolean addAll(Collection<Short> c);   
    // - boolean containsAll(Collection<Short> c);   
    for (short elem : coll) {
        if (contains(elem)) {
            return true;
        }
    }
    return false;
}

    
public boolean containsAll(Collection<Short> coll) {
    // Note that this method is already implemented in AbstractCollection.   
    // It has been duplicated so the method is also available in the primitive classes.   
    for (short elem : coll) {
        if (!contains(elem)) {
            return false;
        }
    }
    return true;
}

    /**
	 * Removes all equal elements.
	 *
	 * @param elem	element
	 * @return		removed equal elements (never null)
	 */
public IShortList removeAll(short elem) {
    IShortList list = doCreate(-1);
    int size = size();
    for (int i = 0; i < size; i++) {
        short e = doGet(i);
        if (equalsElem(elem, e)) {
            list.add(e);
            doRemove(i);
            size--;
            i--;
        }
    }
    return list;
}

    
public boolean removeAll(Collection<Short> coll) {
    // Note that this method is already implemented in AbstractCollection.   
    // It has been duplicated so the method is also available in the primitive classes.   
    checkNonNull(coll);
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
     * @see #removeAll(Collection)
     */
public boolean removeAll(IShortList coll) {
    // There is a special implementation accepting an IShortList   
    // so the method is also available in the primitive classes.   
    checkNonNull(coll);
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

    
public boolean retainAll(Collection<Short> coll) {
    // Note that this method is already implemented in AbstractCollection.   
    // It has been duplicated so the method is also available in the primitive classes.   
    checkNonNull(coll);
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
public boolean retainAll(IShortList coll) {
    // There is a special implementation accepting an IShortList   
    // so the method is also available in the primitive classes.   
    checkNonNull(coll);
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

    
public short[] toArray() {
    return toArray(0, size());
}

    
public short[] toArray(short[] array) {
    return toArray(array, 0, size());
}

    /**
	 * Returns an array containing the elements in this list.
	 *
	 * @param clazz	class for array elements
	 * @return		array containing the specified elements
	 */
public short[] toArray(Class clazz) {
    return toArray(clazz, 0, size());
}

    /**
	 * Returns an array containing the specified elements in this list.
	 * @see List#toArray()
	 *
	 * @param index	index of first element to copy
	 * @param len	number of elements to copy
	 * @return		array containing the specified elements
	 */
public short[] toArray(int index, int len) {
    short[] array = new short[len];
    doGetAll(array, index, len);
    return array;
}

    /**
	 * Returns an array containing the specified elements in this list.
	 * @see List#toArray(Object[])
	 *
	 * @param array	the array into which the elements of this list are to be stored, if it is big enough; otherwise, a new array of the same runtime type is allocated for this purpose
	 * @param index	index of first element to copy
	 * @param len	number of elements to copy
	 * @return		array containing the specified elements
	 */
@SuppressWarnings("unchecked")
public short[] toArray(short[] array, int index, int len) {
    if (array.length < len) {
        array = (short[]) doCreateArray(array.getClass().getComponentType(), len);
    }
    doGetAll(array, index, len);
    if (array.length > len) {
        array[len] = (short) 0;
    }
    return array;
}

    /**
	 * Returns an array containing the specified elements in this list.
	 *
	 * @param clazz	class for array elements
	 * @param index	index of first element to copy
	 * @param len	number of elements to copy
	 * @return		array containing the specified elements
	 */
public short[] toArray(Class clazz, int index, int len) {
    short[] array = doCreateArray(clazz, len);
    doGetAll(array, index, len);
    return array;
}

    /**
	 * Create array.
	 *
	 * @param clazz	class for array elements
	 * @param len	array length
	 * @return		created array
	 */
@SuppressWarnings("unchecked")
protected short[] doCreateArray(Class clazz, int len) {
    return (short[]) java.lang.reflect.Array.newInstance(clazz, len);
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
protected void doGetAll(short[] array, int index, int len) {
    for (int i = 0; i < len; i++) {
        array[i] = doGet(index + i);
    }
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
protected boolean doAddAll(int index, IShortList list) {
    int listSize = list.size();
    doEnsureCapacity(size() + listSize);
    if (listSize == 0) {
        return false;
    }
    boolean changed = false;
    int prevSize = size();
    for (int i = 0; i < listSize; i++) {
        short elem = list.get(i);
        if (doAdd(index, elem)) {
            changed = true;
            if (index != -1) {
                if (prevSize != size()) {
                    prevSize = size();
                    index++;
                }
            }
        }
    }
    return changed;
}









    // Queue operations  

public short peek() {
    if (size() == 0) {
        return (short) 0;
    }
    return getFirst();
}

    
public short element() {
    // inline version of getFirst():   
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doGet(0);
}

    
public short poll() {
    if (size() == 0) {
        return (short) 0;
    }
    return doRemove(0);
}

    
public short remove() {
    // inline version of removeFirst():   
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doRemove(0);
}

    
public boolean offer(short elem) {
    // inline version of add(elem):   
    return doAdd(-1, elem);
}

    // Deque operations  

public short getFirst() {
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doGet(0);
}

    
public short getLast() {
    int size = size();
    if (size == 0) {
        throw new NoSuchElementException();
    }
    return doGet(size - 1);
}

    
public void addFirst(short elem) {
    doAdd(0, elem);
}

    
public void addLast(short elem) {
    // inline version of add(elem):   
    doAdd(-1, elem);
}

    
public short removeFirst() {
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doRemove(0);
}

    
public short removeLast() {
    int size = size();
    if (size == 0) {
        throw new NoSuchElementException();
    }
    return doRemove(size - 1);
}

    
public boolean offerFirst(short elem) {
    // inline version of addFirst(elem):   
    doAdd(0, elem);
    return true;
}

    
public boolean offerLast(short elem) {
    // inline version of addLast(elem):   
    doAdd(-1, elem);
    return true;
}

    
public short peekFirst() {
    if (size() == 0) {
        return (short) 0;
    }
    return doGet(0);
}

    
public short peekLast() {
    int size = size();
    if (size == 0) {
        return (short) 0;
    }
    return doGet(size - 1);
}

    
public short pollFirst() {
    if (size() == 0) {
        return (short) 0;
    }
    return doRemove(0);
}

    
public short pollLast() {
    int size = size();
    if (size == 0) {
        return (short) 0;
    }
    return doRemove(size - 1);
}

    
public short pop() {
    // inline version of removeFirst():   
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doRemove(0);
}

    
public void push(short elem) {
    // inline version of addFirst();   
    doAdd(0, elem);
}

    
public boolean removeFirstOccurrence(short elem) {
    int index = indexOf(elem);
    if (index == -1) {
        return false;
    }
    doRemove(index);
    return true;
}

    
public boolean removeLastOccurrence(short elem) {
    int index = lastIndexOf(elem);
    if (index == -1) {
        return false;
    }
    doRemove(index);
    return true;
}

    // --- Static bulk transfer methods working with two IShortLists ---  
/**
     * Copies elements from one list to another.
     * Elements and size of source list do not change.
     * The elements in the specified range in the destination list are removed and
     * the elements specified to be copied are inserted.
     *
     * If source and destination list are identical, the method behaves like {@link #copy(int, int, int)}.
     *
     * @param src		source list
     * @param srcIndex	index of first element in source list
     * @param srcLen	number of elements to copy
     * @param dst		destination list
     * @param dstIndex	index of first element in destination list
     * @param dstLen	number of elements to replace in destination list
     * @param  		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
public static void transferCopy(IShortList src, int srcIndex, int srcLen, IShortList dst, int dstIndex, int dstLen) {
    if (src == dst) {
        src.checkLengths(srcLen, dstLen);
        src.copy(srcIndex, dstIndex, srcLen);
    } else {
        src.doTransfer(TRANSFER_COPY, srcIndex, srcLen, dst, dstIndex, dstLen);
    }
}

    /**
     * Moves elements from one list to another by setting it to null in the source list.
     * Elements in the source range are set to null, but size of source list does not change.
     * The elements in the specified range in the destination list are removed and
     * the elements specified to be moved are inserted.
     *
     * If source and destination list are identical, the method behaves like {@link #move(int, int, int)}.
     *
     * @param src		source list
     * @param srcIndex	index of first element in source list
     * @param srcLen	number of elements to copy
     * @param dst		destination list
     * @param dstIndex	index of first element in destination list
     * @param dstLen	number of elements to replace in destination list
     * @param  		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
public static void transferMove(IShortList src, int srcIndex, int srcLen, IShortList dst, int dstIndex, int dstLen) {
    if (src == dst) {
        src.checkLengths(srcLen, dstLen);
        src.move(srcIndex, dstIndex, srcLen);
    } else {
        src.doTransfer(TRANSFER_MOVE, srcIndex, srcLen, dst, dstIndex, dstLen);
    }
}

    /**
     * Moves elements from one list to another by removing it from the source list.
     * So the size of source list will change.
     * The elements in the specified range in the destination list are removed and
     * the elements specified to be moved are inserted.
     *
     * If source and destination list are identical, the method behaves like {@link #drag(int, int, int)}.
     *
     * @param src		source list
     * @param srcIndex	index of first element in source list
     * @param srcLen	number of elements to copy
     * @param dst		destination list
     * @param dstIndex	index of first element in destination list
     * @param dstLen	number of elements to replace in destination list
     * @param  		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
public static void transferRemove(IShortList src, int srcIndex, int srcLen, IShortList dst, int dstIndex, int dstLen) {
    if (src == dst) {
        src.checkLengths(srcLen, dstLen);
        src.drag(srcIndex, dstIndex, srcLen);
    } else {
        src.doTransfer(TRANSFER_REMOVE, srcIndex, srcLen, dst, dstIndex, dstLen);
    }
}

    private static final int TRANSFER_COPY = 0;

    private static final int TRANSFER_MOVE = 1;

    private static final int TRANSFER_REMOVE = 2;

    void doTransfer(int transferMode, int srcIndex, int srcLen, IShortList dst, int dstIndex, int dstLen) {
    // Prepare arguments   
    if (srcLen == -1) {
        srcLen = size() - srcIndex;
    }
    checkRange(srcIndex, srcLen);
    if (dstIndex == -1) {
        dstIndex = dst.size();
    } else {
        dst.checkIndexAdd(dstIndex);
    }
    if (dstLen == -1) {
        dstLen = dst.size() - dstIndex;
    } else {
        dst.checkLength(dstLen);
    }
    short defaultElem = getDefaultElem();
    if (dstLen > srcLen) {
        // Remove elements from destination because the source range is smaller than the destination range   
        dst.remove(dstIndex, dstLen - srcLen);
    } else if (srcLen > dstLen) {
        // Add elements to destination because the source range is larger than the destination range   
        dst.addMult(dstIndex, srcLen - dstLen, defaultElem);
    }
    // Overwrite the range starting at dstIndex with length srcIndex in dst   
    if (transferMode == TRANSFER_MOVE) {
        // Move   
        for (int i = 0; i < srcLen; i++) {
            short elem = doReSet(srcIndex + i, defaultElem);
            dst.doSet(dstIndex + i, elem);
        }
    } else {
        // Copy / Remove   
        for (int i = 0; i < srcLen; i++) {
            short elem = doGet(srcIndex + i);
            dst.doSet(dstIndex + i, elem);
        }
        if (transferMode == TRANSFER_REMOVE) {
            // Remove   
            remove(srcIndex, srcLen);
        }
    }
}

    /**
     * Swaps elements from two lists.
     * The size of both source and destination list do not change.
     *
     * If source and destination list are identical, the method behaves like {@link #swap(int, int, int)}.
     *
     * @param src		first list
     * @param srcIndex	index of first element in first list
     * @param dst		second list
     * @param dstIndex	index of first element in second list
     * @param len		number of elements to swap
     * @param  		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
public static void transferSwap(IShortList src, int srcIndex, IShortList dst, int dstIndex, int len) {
    if (src == dst) {
        src.swap(srcIndex, dstIndex, len);
    } else {
        src.doTransferSwap(srcIndex, dst, dstIndex, len);
    }
}

    void doTransferSwap(int srcIndex, IShortList dst, int dstIndex, int len) {
    checkRange(srcIndex, len);
    dst.checkRange(dstIndex, len);
    for (int i = 0; i < len; i++) {
        short swap = doGet(srcIndex + i);
        swap = dst.doSet(dstIndex + i, swap);
        doSet(srcIndex + i, swap);
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
protected abstract IShortList doCreate(int capacity);

    /**
     * Assign this list the content of the that list.
     * This is done by bitwise copying so the that list should not be used afterwards.
     *
     * @param that list to copy content from
     */
protected abstract void doAssign(IShortList that);

    /**
     * Returns specified range of elements from list.
     *
     * @param index index of first element to retrieve
     * @param len   number of elements to retrieve
     * @return      list containing the specified range of elements
     */
public IShortList getAll(int index, int len) {
    checkRange(index, len);
    IShortList list = doCreate(len);
    for (int i = 0; i < len; i++) {
        list.add(doGet(index + i));
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
public IShortList extract(int index, int len) {
    checkRange(index, len);
    IShortList list = doCreate(len);
    for (int i = 0; i < len; i++) {
        list.add(doGet(index + i));
    }
    remove(index, len);
    return list;
}

    // -- Mutators --  
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
    for (int i = index + len - 1; i >= index; i--) {
        doRemove(i);
    }
}

    // -- addAll()  
/**
     * Adds all of the elements in the specified list into this list.
     *
     * @param list collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified list is null
     */
public boolean addAll(IShortList list) {
    return doAddAll(-1, list);
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
public boolean addAll(int index, IShortList list) {
    checkIndexAdd(index);
    return doAddAll(index, list);
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

public boolean addAll(Collection<Short> coll) {
    if (coll instanceof List) {
        return doAddAll(-1, new IReadOnlyShortListFromList((List<Short>) coll));
    } else {
        return doAddAll(-1, new IReadOnlyShortListFromCollection(coll));
    }
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

public boolean addAll(int index, Collection<Short> coll) {
    checkIndexAdd(index);
    if (coll instanceof List) {
        return doAddAll(index, new IReadOnlyShortListFromList((List<Short>) coll));
    } else {
        return doAddAll(index, new IReadOnlyShortListFromCollection(coll));
    }
}

    /**
     * Adds all specified elements into this list.
     *
     * @param elems elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     */
public boolean addArray(short... elems) {
    return doAddAll(-1, new IReadOnlyShortListFromArray(elems));
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
public boolean addArray(int index, short... elems) {
    checkIndexAdd(index);
    return doAddAll(index, new IReadOnlyShortListFromArray(elems));
}

    /**
     * Adds element multiple time to list.
     *
     * @param elem element to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     */
public boolean addMult(int len, short elem) {
    return doAddAll(-1, new IReadOnlyShortListFromMult(len, elem));
}

    /**
     * Inserts element multiple time to list, starting at the specified position.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (increases their indices).
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param elem element to be inserted into this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException if the index is invalid
     */
public boolean addMult(int index, int len, short elem) {
    checkIndexAdd(index);
    return doAddAll(index, new IReadOnlyShortListFromMult(len, elem));
}

    // -- setAll()  
/**
     * Sets the specified elements.
     *
     * @param index index of first element to set
     * @param list  list with elements to set
     * @throws 		IndexOutOfBoundsException if the range is invalid
     */
public void setAll(int index, IShortList list) {
    int listSize = list.size();
    checkRange(index, listSize);
    doReplaceAll(index, listSize, list);
}

    /**
     * Sets the specified elements.
     *
     * @param index index of first element to set
     * @param coll  collection with elements to set
     */
public void setAll(int index, Collection<Short> coll) {
    int collSize = coll.size();
    checkRange(index, collSize);
    if (coll instanceof List) {
        doReplaceAll(index, collSize, new IReadOnlyShortListFromList((List<Short>) coll));
    } else {
        doReplaceAll(index, collSize, new IReadOnlyShortListFromCollection(coll));
    }
}

    /**
     * Sets the specified elements.
     *
     * @param index 	index of first element to set
     * @param elemes	array with elements to set
     * @throws 			IndexOutOfBoundsException if the range is invalid
     */
public void setArray(int index, short... elems) {
    int arrayLen = elems.length;
    checkRange(index, arrayLen);
    doReplaceAll(index, arrayLen, new IReadOnlyShortListFromArray(elems));
}

    /**
     * Sets the element multiple times.
     *
     * @param index index of first element to set
     * @param elem	element to set
     */
public void setMult(int index, int len, short elem) {
    checkRange(index, len);
    doReplaceAll(index, len, new IReadOnlyShortListFromMult(len, elem));
}

    // -- putAll()  
/**
     * Set or add the specified elements.
     *
     * @param index index of first element to set or add
     * @param list  list with elements to set or add
     */
public void putAll(int index, IShortList list) {
    checkIndexAdd(index);
    checkNonNull(list);
    int len = size() - index;
    if (list != null) {
        if (list.size() < len) {
            len = list.size();
        }
    }
    // Call worker method   
    doReplaceAll(index, len, list);
}

    /**
     * Set or add the specified elements.
     * If the index is smaller than the size of the list, the existing element is replaced.
     * If the index equals the size of the list, the element is added.
     *
     * @param index index of first element to set or add
     * @param coll  collection with elements to set or add
     */
public void putAll(int index, Collection<Short> coll) {
    if (coll instanceof IShortList) {
        putAll(index, (IShortList) coll);
    } else if (coll instanceof List) {
        putAll(index, new IReadOnlyShortListFromList((List<Short>) coll));
    } else {
        putAll(index, new IReadOnlyShortListFromCollection(coll));
    }
}

    /**
     * Set or add the specified elements.
     * If the index is smaller than the size of the list, the existing element is replaced.
     * If the index equals the size of the list, the element is added.
     *
     * @param index index of first element to set or add
     * @param elems	array with elements to set or add
     */
public void putArray(int index, short... elems) {
    putAll(index, new IReadOnlyShortListFromArray(elems));
}

    /**
     * Set or add the specified element multiple times.
     * If the index is smaller than the size of the list, the existing element is replaced.
     * If the index equals the size of the list, the element is added.
     *
     * @param index index of first element to set or add
     * @param len 	element to set or add
     */
public void putMult(int index, int len, short elem) {
    putAll(index, new IReadOnlyShortListFromMult(len, elem));
}

    // -- initAll()  
/**
	 * Initializes the list so it will afterwards only contain the elements of the collection.
	 * The list will grow or shrink as needed.
	 *
	 * @param list 	list with elements
     * @throws 		IndexOutOfBoundsException if the length is invalid
	 */
public void initAll(IShortList list) {
    checkNonNull(list);
    doReplaceAll(0, size(), list);
}

    /**
	 * Initializes the list so it will afterwards only contain the elements of the collection.
	 * The list will grow or shrink as needed.
	 *
	 * @param coll 	collection with elements
     * @throws 		IndexOutOfBoundsException if the length is invalid
	 */
public void initAll(Collection<Short> coll) {
    if (coll instanceof IShortList) {
        initAll((IShortList) coll);
    } else if (coll instanceof List) {
        initAll(new IReadOnlyShortListFromList((List<Short>) coll));
    } else {
        initAll(new IReadOnlyShortListFromCollection(coll));
    }
}

    /**
	 * Initializes the list so it will afterwards only contain the elements of the array.
	 * The list will grow or shrink as needed.
	 *
	 * @param elems array with elements
     * @throws 		IndexOutOfBoundsException if the length is invalid
	 */
public void initArray(short... elems) {
    initAll(new IReadOnlyShortListFromArray(elems));
}

    /**
	 * Initializes the list so it will afterwards have a size of
	 * <code>len</code> and contain only the element <code>elem</code>.
	 * The list will grow or shrink as needed.
	 *
	 * @param len  	length of list
	 * @param elem 	element which the list will contain
     * @throws 		IndexOutOfBoundsException if the length is invalid
	 */
public void initMult(int len, short elem) {
    checkLength(len);
    initAll(new IReadOnlyShortListFromMult(len, elem));
}

    // -- replaceAll()  
/**
     * Replaces the specified range with new elements.
     * This method is very powerful as it offers the functionality of many other methods
     * which are therefore only offered for convenience: <br/>
     * - addAll(index, list) -> replaceAll(index, 0, list) <br/>
     * - setAll(index, list) -> replaceAll(index, list.size(), list) <br/>
     * - putAll(index, list) -> replaceAll(index, -1, list) <br/>
     * - initAll(list)       -> replaceAll(0, this.size(), list) <br/>
     * - remove(index, list) -> replaceAll(index, list.size(), null) <br/>
     *
     * @param index index of first element to replace, use -1 for the position after the last element (this.size())
     * @param len	number of elements to replace, use -1 for getting behavior of putAll()
     * @param coll  collection with elements which replace the old elements, use null if elements should only be removed
     * @throws 		IndexOutOfBoundsException if the range is invalid
     */
public void replaceAll(int index, int len, Collection<Short> coll) {
    if (coll instanceof IShortList) {
        replaceAll(index, len, (IShortList) coll);
    } else if (coll instanceof List) {
        replaceAll(index, len, new IReadOnlyShortListFromList((List<Short>) coll));
    } else {
        replaceAll(index, len, new IReadOnlyShortListFromCollection(coll));
    }
}

    /**
     * Replaces the specified range with new elements.
     * This method is very powerful as it offers the functionality of many other methods
     * which are therefore only offered for convenience: <br/>
     * - addAll(index, list) -> replaceAll(index, 0, list) <br/>
     * - setAll(index, list) -> replaceAll(index, list.size(), list) <br/>
     * - putAll(index, list) -> replaceAll(index, -1, list) <br/>
     * - initAll(list)       -> replaceAll(0, this.size(), list) <br/>
     * - remove(index, list) -> replaceAll(index, list.size(), null) <br/>
     *
     * @param index index of first element to replace, use -1 for the position after the last element (this.size())
     * @param len	number of elements to replace, use -1 for getting behavior of putAll()
     * @param elems array with elements which replace the old elements, use null if elements should only be removed
     * @throws 		IndexOutOfBoundsException if the range is invalid
     */
public void replaceArray(int index, int len, short... elems) {
    replaceAll(index, len, new IReadOnlyShortListFromArray(elems));
}

    /**
     * Replaces the specified range with new elements.
     * This method is very powerful as it offers the functionality of many other methods
     * which are therefore only offered for convenience: <br/>
     * - addAll(index, list) -> replaceAll(index, 0, list) <br/>
     * - setAll(index, list) -> replaceAll(index, list.size(), list) <br/>
     * - putAll(index, list) -> replaceAll(index, -1, list) <br/>
     * - initAll(list)       -> replaceAll(0, this.size(), list) <br/>
     * - remove(index, list) -> replaceAll(index, list.size(), null) <br/>
     *
     * @param index 	index of first element to replace, use -1 for the position after the last element (this.size())
     * @param len		number of elements to replace, use -1 for getting behavior of putAll()
     * @param numElems  number of time element has to be added
     * @param elem  	element to add
     * @throws 			IndexOutOfBoundsException if the range is invalid
     */
public void replaceMult(int index, int len, int numElems, short elem) {
    replaceAll(index, len, new IReadOnlyShortListFromMult(numElems, elem));
}

    /**
     * Replaces the specified range with new elements.
     * This method is very powerful as it offers the functionality of many other methods
     * which are therefore only offered for convenience: <br/>
     * - addAll(index, list) -> replaceAll(index, 0, list) <br/>
     * - setAll(index, list) -> replaceAll(index, list.size(), list) <br/>
     * - putAll(index, list) -> replaceAll(index, -1, list) <br/>
     * - initAll(list)       -> replaceAll(0, this.size(), list) <br/>
     * - remove(index, list) -> replaceAll(index, list.size(), null) <br/>
     *
     * @param index index of first element to replace, use -1 for the position after the last element (this.size())
     * @param len	number of elements to replace, use -1 for getting behavior of putAll()
     * @param list  list with elements which replace the old elements, use null if elements should only be removed
     * @throws 		IndexOutOfBoundsException if the range is invalid
     */
public void replaceAll(int index, int len, IShortList list) {
    // Check arguments   
    if (index == -1) {
        index = size();
    } else {
        checkIndexAdd(index);
    }
    if (len == -1) {
        len = size() - index;
        if (list != null) {
            if (list.size() < len) {
                len = list.size();
            }
        }
    } else {
        checkRange(index, len);
    }
    // Call worker method   
    doReplaceAll(index, len, list);
}

    // -- doReplaceAll()  
protected boolean doReplaceAll(int index, int len, IShortList list) {
    // There is a special implementation accepting an IShortList   
    // so the method is also available in the primitive classes.   
    assert (index >= 0 && index <= size());
    assert (len >= 0 && index + len <= size());
    int srcLen = 0;
    if (list != null) {
        srcLen = list.size();
    }
    doEnsureCapacity(size() - len + srcLen);
    // Remove elements   
    doRemoveAll(index, len);
    // Add elements   
    for (int i = 0; i < srcLen; i++) {
        if (!doAdd(index + i, list.doGet(i))) {
            index--;
        }
    }
    return len > 0 || srcLen > 0;
}

    //  
/**
     * Fill list.
     *
     * @param elem  element used for filling
     */
// see java.util.Arrays#fill  
public void fill(short elem) {
    int size = size();
    for (int i = 0; i < size; i++) {
        doSet(i, elem);
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
        for (int i = len - 1; i >= 0; i--) {
            doReSet(dstIndex + i, doGet(srcIndex + i));
        }
    } else if (srcIndex > dstIndex) {
        for (int i = 0; i < len; i++) {
            doReSet(dstIndex + i, doGet(srcIndex + i));
        }
    }
    // Set elements to (short) 0 after the move operation    
    if (srcIndex < dstIndex) {
        int fill = Math.min(len, dstIndex - srcIndex);
        setMult(srcIndex, fill, (short) 0);
    } else if (srcIndex > dstIndex) {
        int fill = Math.min(len, srcIndex - dstIndex);
        setMult(srcIndex + len - fill, fill, (short) 0);
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
        doRotate(srcIndex, len + (dstIndex - srcIndex), dstIndex - srcIndex);
    } else if (srcIndex > dstIndex) {
        doRotate(dstIndex, len + (srcIndex - dstIndex), dstIndex - srcIndex);
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
    if ((index1 < index2 && index1 + len > index2) || index1 > index2 && index2 + len > index1) {
        throw new IndexOutOfBoundsException("Swap ranges overlap");
    }
    for (int i = 0; i < len; i++) {
        short swap = doGet(index1 + i);
        swap = doReSet(index2 + i, swap);
        doReSet(index1 + i, swap);
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
    int pos2 = index + len - 1;
    int mid = len / 2;
    for (int i = 0; i < mid; i++) {
        short swap = doGet(pos1);
        swap = doReSet(pos2, swap);
        doReSet(pos1, swap);
        pos1++;
        pos2--;
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
    assert (distance >= 0 && distance < len);
    int num = 0;
    for (int start = 0; num != len; start++) {
        short elem = doGet(index + start);
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
     * @throws 				IndexOutOfBoundsException if the range is invalid
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
public int binarySearch(short key) {
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
     * @throws 				IndexOutOfBoundsException if the range is invalid
     *
     * @see Arrays#binarySearch
     */
public abstract int binarySearch(int index, int len, short key);

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
     * @param len length to check
     * @throws IndexOutOfBoundsException if length is invalid
     */
protected void checkLength(int len) {
    if (len < 0) {
        throw new IndexOutOfBoundsException("Invalid length: " + len);
    }
}

    /**
     * Check that both specified lengths are valid (>= 0) and equal.
     *
     * @param len1 length to check
     * @param len2 length to check
     * @throws IndexOutOfBoundsException if lengths are invalid
     */
protected void checkLengths(int len1, int len2) {
    if (len1 != len2) {
        throw new IndexOutOfBoundsException("Invalid lengths: " + len1 + ", " + len2);
    }
    if (len1 < 0) {
        throw new IndexOutOfBoundsException("Invalid length: " + len1);
    }
    if (len2 < 0) {
        throw new IndexOutOfBoundsException("Invalid length: " + len2);
    }
}

    /**
     * Check that object is not null.
     *
     * @param obj object to check
     * @throws NullPointerException if object is null
     */
protected void checkNonNull(Object obj) {
    if (obj == null) {
        throw new NullPointerException("Argument may not be (short) 0");
    }
}





    // --- End class ListIter ---  
    protected abstract static class IReadOnlyShortList extends IShortList {

        
public IShortList unmodifiableList() {
    error();
    return null;
}

        
protected void doClone(IShortList that) {
    error();
}

        
public int capacity() {
    error();
    return 0;
}

        
protected short doSet(int index, short elem) {
    error();
    return (short) 0;
}

        
protected short doReSet(int index, short elem) {
    error();
    return (short) 0;
}

        
protected short getDefaultElem() {
    error();
    return (short) 0;
}

        
protected boolean doAdd(int index, short elem) {
    error();
    return false;
}

        
protected short doRemove(int index) {
    error();
    return (short) 0;
}

        
protected void doEnsureCapacity(int minCapacity) {
    error();
}

        
public void trimToSize() {
    error();
}

        
protected IShortList doCreate(int capacity) {
    error();
    return null;
}

        
protected void doAssign(IShortList that) {
    error();
}

        
public void sort(int index, int len) {
    error();
}

        
public int binarySearch(int index, int len, short key) {
    error();
    return 0;
}

        /**
         * Throw exception if an attempt is made to change an immutable list.
         */
private void error() {
    throw new UnsupportedOperationException("list is read-only");
}
    }

    protected static class IReadOnlyShortListFromArray extends IReadOnlyShortList {

        short[] array;

        IReadOnlyShortListFromArray(short[] array){
    this.array = array;
}

        
public int size() {
    return array.length;
}

        
protected short doGet(int index) {
    return array[index];
}
    }

    protected static class IReadOnlyShortListFromMult extends IReadOnlyShortList {

        int len;

        short elem;

        IReadOnlyShortListFromMult(int len, short elem){
    checkLength(len);
    this.len = len;
    this.elem = elem;
}

        
public int size() {
    return len;
}

        
protected short doGet(int index) {
    return elem;
}
    }

    protected static class IReadOnlyShortListFromCollection extends IReadOnlyShortList {

        short[] array;

        IReadOnlyShortListFromCollection(Collection<Short> coll){
    array = toArray(coll);
}

        
public int size() {
    return array.length;
}

        @SuppressWarnings("unchecked")

protected short doGet(int index) {
    return array[index];
}
    }

    protected static class IReadOnlyShortListFromList extends IReadOnlyShortList {

        List<Short> list2;

        @SuppressWarnings("unchecked")
IReadOnlyShortListFromList(List<Short> list){
    this.list2 = (List) list;
}

        
public int size() {
    return list2.size();
}

        
protected short doGet(int index) {
    return list2.get(index);
}
    }
}
