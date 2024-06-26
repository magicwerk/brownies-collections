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
 * Class {@link IByteList} is the abstract class which offers all interfaces implemented by ArrayList.
 * It also offers additional methods which are then available in all implementations of {@link GapList} and {@link BigList}.
 *
 * @author Thomas Mauch
 *
 * @param  type of elements stored in the list
 * @see	    java.util.List
 * @see	    java.util.Deque
 * @see	    java.util.ArrayList
 * @see	    java.util.LinkedList
 */
public abstract class IByteList implements IByteListable, Cloneable, Serializable {



    /**
 * Copies the collection values into an array.
 *
 * @param coll   collection of values
 * @return       array containing the collection values
 */
static byte[] toArray(Collection<Byte> coll) {
    Object[] values = coll.toArray();
    byte[] v = new byte[values.length];
    for (int i = 0; i < values.length; i++) {
        v[i] = (Byte) values[i];
    }
    return v;
}

    /**
 * Returns a shallow copy of this list.
 * The new list will contain the same elements as the source list, i.e. the elements themselves are not copied.
 * The capacity of the list will be set to the number of elements, i.e. size and capacity are equal.
 * This returned list will be modifiable, i.e. a read-only list will be copied and be modifiable again.
 *
 * @return a modifiable copy of this list
 */

public abstract IByteList copy() ;

    /**
 * Returns a shallow copy of this list.
 * The new list will contain the same elements as the source list, i.e. the elements themselves are not copied.
 * The capacity of the list will be set to the number of elements, i.e. size and capacity are equal.
 * If the list is read-only, the same list is returned without change.
 * Use {@link #copy} to .
 *
 * @return a modifiable copy of this list
 */

public IByteList clone() {
    try {
        @SuppressWarnings("unchecked")
        IByteList list = (IByteList) super.clone();
        list.doClone(this);
        return list;
    } catch (CloneNotSupportedException e) {
        // This shouldn't happen, since we are Cloneable
        throw new AssertionError(e);
    }
}

    /**
 * Returns a copy this list but without elements.
 * The new list will have the same type as this list.
 *
 * @return  an empty copy of this list
 */

public IByteList crop() {
    return doCreate(0);
}

    /**
 * Returns true if this list is either unmodifiable or immutable, false otherwise.
 */
public abstract boolean isReadOnly() ;

    // Naming as in java.util.Collections#unmodifiableList
public abstract IByteList unmodifiableList() ;

    /**
 * Returns an immutable copy of this list.
 * The returned list is independent from the original list, i.e. changes done later are not seen.
 * Attempts to modify the returned list, whether direct or via its iterator, result in an UnsupportedOperationException.
 * If this list is already immutable, it is returned unchanged.
 * See {@link #unmodifiableList} to get unmodifiable view of a list.
 *
 * @return an immutable copy of the specified list
 */
public abstract IByteList immutableList() ;

    /**
 * Initialize this object after the bitwise copy has been made by Object.clone().
 *
 * @param that	source object
 */
protected abstract void doClone(IByteList that) ;

    
public void clear() {
    doClear();
}

    protected void doClear() {
    doRemoveAll(0, size());
}

    /**
 * Resizes the list so it will afterwards have a size of <code>len</code>.
 * If the list must grow, the specified element <code>elem</code> will be used for filling.
 *
 * @param len  	new size of list
 * @param elem 	element which will be used for extending the list
 * @throws 	 	IndexOutOfBoundsException if the range is invalid
 */
public void resize(int len, byte elem) {
    checkLength(len);
    int size = size();
    if (len < size) {
        doRemoveAll(len, size - len);
    } else {
        doEnsureCapacity(len);
        for (int i = size; i < len; i++) {
            doAdd(i, elem);
        }
    }
    assert size() == len;
}

    // Do not remove - needed for generating primitive classes

abstract public int size() ;

    /**
 * Returns capacity of this list.
 * Note that two lists are considered equal even if they have a distinct capacity.
 * Also the capacity can be changed by operations like clone() etc.
 *
 * @return capacity of this list
 */
abstract public int capacity() ;

    
public byte get(int index) {
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
abstract protected byte doGet(int index) ;

    /**
 * Helper method for setting an element in the list.
 * This is the only method which really sets an element.
 * Override if you need to validity checks before setting.
 *
 * @param index index where element will be placed
 * @param elem  element to set
 * @return      old element which was at the position
 */
abstract protected byte doSet(int index, byte elem) ;

    
public byte set(int index, byte elem) {
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
 * @throws IndexOutOfBoundsException if the index is out of range (<tt>index &lt; 0 || index &gt; size()</tt>)
 */
public byte put(int index, byte elem) {
    checkIndexAdd(index);
    if (index < size()) {
        return doSet(index, elem);
    } else {
        doAdd(-1, elem);
        return (byte) 0;
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
abstract protected byte doReSet(int index, byte elem) ;

    abstract protected byte getDefaultElem() ;

    /**
 * This method is called internally before elements are allocated or freed.
 * Override if you need to validity checks.
 */
protected void doModify() {
}

    
public boolean add(byte elem) {
    return doAdd(-1, elem);
}

    /**
 * Inserts the specified element at the specified position in this list.
 * Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their indices).
 *
 * @param index index at which the specified element is to be inserted
 * @param elem element to be inserted
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */

public void add(int index, byte elem) {
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
abstract protected boolean doAdd(int index, byte elem) ;

    
public byte remove(int index) {
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
abstract protected byte doRemove(int index) ;

    // Note: Provide this method to make transition from ArrayList as smooth as possible
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
abstract protected void doEnsureCapacity(int minCapacity) ;

    // Note: Provide this method to make transition from ArrayList as smooth as possible
abstract public void trimToSize() ;

    
public boolean equals(Object obj) {
    if (obj == this) {
        return true;
    }
    if (obj instanceof ByteObjGapList) {
        obj = ((ByteObjGapList) obj).list;
    } else if (obj instanceof ByteObjBigList) {
        obj = ((ByteObjBigList) obj).list;
    }
    if (!(obj instanceof IByteList)) {
        return false;
    }
    @SuppressWarnings("unchecked")
    IByteList list = (IByteList) obj;
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
        byte elem = doGet(i);
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
static boolean equalsElem(byte elem1, byte elem2) {
    return elem1 == elem2;
}

    /**
 * Helper method to calculate hash code of a element stored in the list.
 *
 * @param elem	element
 * @return		hash code for element
 */
static int hashCodeElem(byte elem) {
    return (int) elem;
}

    // See java.util.Collections.frequency(Collection<?>, Object)
public int count(byte elem) {
    int count = 0;
    int size = size();
    for (int i = 0; i < size; i++) {
        if (equalsElem(doGet(i), elem)) {
            count++;
        }
    }
    return count;
}

    
public int countIf(Predicate<Byte> predicate) {
    int count = 0;
    int size = size();
    for (int i = 0; i < size; i++) {
        if (predicate.test(doGet(i))) {
            count++;
        }
    }
    return count;
}

    
public byte getSingle() {
    if (size() != 1) {
        throw new NoSuchElementException();
    }
    return doGet(0);
}

    
public byte getSingleOrNull() {
    int size = size();
    if (size == 0) {
        return (byte) 0;
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
public IByteList getAll(byte elem) {
    IByteList list = doCreate(-1);
    int size = size();
    for (int i = 0; i < size; i++) {
        byte e = doGet(i);
        if (equalsElem(e, elem)) {
            list.add(e);
        }
    }
    return list;
}

    /**
 * Get first element in the list which matches the predicate.
 *
 * @param predicate a predicate which returns {@code true} for elements to be selected
 * @return 			first element matching the predicate, null otherwise
 */

public byte getIf(Predicate<Byte> predicate) {
    int size = size();
    for (int i = 0; i < size; i++) {
        byte e = doGet(i);
        if (predicate.test(e)) {
            return e;
        }
    }
    return (byte) 0;
}

    /**
 * Returns distinct elements in the list.
 *
 * @return		distinct elements in the list
 */
public Set<Byte> getDistinct() {
    Set<Byte> set = new HashSet();
    int size = size();
    for (int i = 0; i < size; i++) {
        set.add(doGet(i));
    }
    return set;
}

    
public <R> IList<R> map(Function<Byte, R> func) {
    int size = size();
    @SuppressWarnings("unchecked")
    IList<R> list = (IList<R>) new GapList<R>(size);
    for (int i = 0; i < size; i++) {
        byte e = doGet(i);
        list.add(func.apply(e));
    }
    return list;
}

    
public <R, C extends Collection<R>> IList<R> flatMap(Function<Byte, C> func) {
    int size = size();
    @SuppressWarnings("unchecked")
    IList<R> list = (IList<R>) new GapList<R>(size);
    for (int i = 0; i < size; i++) {
        byte e = doGet(i);
        list.addAll(func.apply(e));
    }
    return list;
}

    
public <R> IList<R> mapFilter(Function<Byte, R> func, Predicate<R> filter) {
    int size = size();
    @SuppressWarnings("unchecked")
    IList<R> list = (IList<R>) new GapList<R>(size);
    for (int i = 0; i < size; i++) {
        byte e = doGet(i);
        R r = func.apply(e);
        if (filter.test(r)) {
            list.add(r);
        }
    }
    return list;
}

    
public <R> IList<R> filterMap(Predicate<Byte> filter, Function<Byte, R> func) {
    int size = size();
    @SuppressWarnings("unchecked")
    IList<R> list = (IList<R>) new GapList<R>(size);
    for (int i = 0; i < size; i++) {
        byte e = doGet(i);
        if (filter.test(e)) {
            R r = func.apply(e);
            list.add(r);
        }
    }
    return list;
}

    /**
 * Create a new list by applying the specified transforming operator to all elements.
 *
 * @param op	transforming operator
 * @return		created list
 */
public IByteList transformedList(UnaryOperator<Byte> op) {
    int size = size();
    IByteList list = doCreate(size);
    for (int i = 0; i < size; i++) {
        byte e = doGet(i);
        list.add(op.apply(e));
    }
    return list;
}

    /**
 * Change the list by applying the specified operator to all elements.
 *
 * @param op	transforming operator
 */
public void transform(UnaryOperator<Byte> op) {
    int size = size();
    for (int i = 0; i < size; i++) {
        byte e = doGet(i);
        e = op.apply(e);
        doSet(i, e);
    }
}

    /**
 * Create a new list by applying the specified filter to all elements.
 * Only element which are allowed by the predicate are copied to the new list.
 *
 * @param predicate	predicate used for filtering
 * @return			created list
 */

public IByteList filter(Predicate<Byte> predicate) {
    IByteList list = doCreate(-1);
    int size = size();
    for (int i = 0; i < size; i++) {
        byte e = doGet(i);
        if (predicate.test(e)) {
            list.add(e);
        }
    }
    return list;
}

    /**
 * Retains all elements in the list which match the predicate.
 *
 * @param predicate a predicate which returns {@code true} for elements to be retained
 * @return 			{@code true} if the list was changed
 */
public boolean retainIf(Predicate<Byte> predicate) {
    // Design: no allocations needed
    int dst = 0;
    int size = size();
    for (int src = 0; src < size; src++) {
        byte e = doGet(src);
        if (predicate.test(e)) {
            if (dst != src) {
                doSet(dst, e);
            }
            dst++;
        }
    }
    if (dst < size) {
        doRemoveAll(dst, size - dst);
    }
    return dst < size;
}

    /**
 * Removes all elements in the list which match the predicate.
 *
 * @param predicate a predicate which returns {@code true} for elements to be removed
 * @return 			{@code true} if the list was changed
 */

public boolean removeIf(Predicate<Byte> predicate) {
    // Design: no allocations needed
    int dst = 0;
    int size = size();
    for (int src = 0; src < size; src++) {
        byte e = doGet(src);
        if (!predicate.test(e)) {
            if (dst != src) {
                byte e2 = doGet(dst);
                doReSet(src, e2);
                doReSet(dst, e);
            }
            dst++;
        }
    }
    if (dst < size) {
        doRemoveAll(dst, size - dst);
    }
    return dst < size;
}

    /**
 * Removes and returns all elements in the list which match the predicate.
 *
 * @param predicate	predicate
 * @return			elements which have been removed from the list
 */
public IByteList extractIf(Predicate<Byte> predicate) {
    // Design: high performance if all or none elements are removed
    // meaning of removed: ==this: all elements are removed, (byte) 0: none elements are removed, else: some elements are removed
    IByteList removed = this;
    int dst = 0;
    int size = size();
    for (int src = 0; src < size; src++) {
        byte e = doGet(src);
        if (predicate.test(e)) {
            // Remove element
            if (removed == this) {
                // all elements removed so far
            } else {
                if (removed == null) {
                    removed = doCreate(-1);
                }
                removed.add(e);
            }
        } else {
            // Retain element
            if (src == 0) {
                removed = null;
            } else if (removed == this) {
                removed = doCreate(-1);
                for (int i = 0; i < src; i++) {
                    removed.add(doGet(i));
                }
            }
            if (dst != src) {
                doSet(dst, e);
            }
            dst++;
        }
    }
    if (removed == null) {
        removed = doCreate(-1);
    } else if (removed == this) {
        removed = copy();
        doClear();
    } else {
        if (dst < size) {
            doRemoveAll(dst, size - dst);
        }
    }
    return removed;
}

    
public int indexOf(byte elem) {
    return doIndexOf(elem, 0);
}

    /**
 * Returns the index of the first occurrence of the specified element in this list,
 * starting the search at the specified position. If the element is not found, -1 is returned.
 *
 * @param elem			element to search for
 * @param fromIndex		start index for search
 * @return				the index of the first occurrence of the specified element in this list that is greater than or equal to fromIndex,
 * 						or -1 if this list does not contain the element
 * @see #indexOf(Object)
 */
public int indexOf(byte elem, int fromIndex) {
    if (fromIndex < 0) {
        fromIndex = 0;
    }
    return doIndexOf(elem, fromIndex);
}

    /**
 * Returns the index of the first occurrence of the specified element in this list,
 * starting the search at the specified position. If the element is not found, -1 is returned.
 */
protected int doIndexOf(byte elem, int fromIndex) {
    int size = size();
    for (int i = fromIndex; i < size; i++) {
        if (equalsElem(doGet(i), elem)) {
            return i;
        }
    }
    return -1;
}

    /**
 * Returns the index of the first element which matches the specified predicate in this list.
 * If no such element is not found, -1 is returned.
 *
 * @param predicate		predicate used to search for the element
 * @return				the index of the first element which matches the specified predicate,
 * 						or -1 if not found
 * @see #indexOf(Object)
 */
public int indexOfIf(Predicate<Byte> predicate) {
    return doIndexOfIf(predicate, 0);
}

    /**
 * Returns the index of the first element which matches the specified predicate in this list,
 * starting the search at the specified position. If no such element is not found, -1 is returned.
 *
 * @param predicate		predicate used to search for the element
 * @return				the index of the first element which matches the specified predicate,
 * 						or -1 if not found
 * @see #indexOf(Object)
 */
public int indexOfIf(Predicate<Byte> predicate, int fromIndex) {
    if (fromIndex < 0) {
        fromIndex = 0;
    }
    return doIndexOfIf(predicate, fromIndex);
}

    /**
 * Returns the index of the first element which matches the specified predicate in this list,
 * starting the search at the specified position. If no such element is not found, -1 is returned.
 */
protected int doIndexOfIf(Predicate<Byte> predicate, int fromIndex) {
    int size = size();
    for (int i = fromIndex; i < size; i++) {
        if (predicate.test(doGet(i))) {
            return i;
        }
    }
    return -1;
}

    
public int lastIndexOf(byte elem) {
    return doLastIndexOf(elem, size() - 1);
}

    /**
 * Returns the index of the last occurrence of the specified element in this list,
 * starting the search at the specified position. If the element is not found, -1 is returned.
 *
 * @param elem			element to search for
 * @param fromIndex		start index for search
 * @return				the index of the last occurrence of the specified element in this list that is less than or equal to fromIndex,
 * 						or -1 if this list does not contain the element
 * @see #lastIndexOf(Object)
 */
public int lastIndexOf(byte elem, int fromIndex) {
    int size = size();
    if (fromIndex >= size) {
        fromIndex = size - 1;
    }
    return doLastIndexOf(elem, fromIndex);
}

    /**
 * Returns the index of the last occurrence of the specified element in this list,
 * starting the search at the specified position. If the element is not found, -1 is returned.
 */
protected int doLastIndexOf(byte elem, int fromIndex) {
    for (int i = fromIndex; i >= 0; i--) {
        if (equalsElem(doGet(i), elem)) {
            return i;
        }
    }
    return -1;
}

    /**
 * Returns the index of the last element which matches the specified predicate in this list.
 * If no such element is not found, -1 is returned.
 *
 * @param predicate		predicate used to search for the element
 * @return				the index of the last element which matches the specified predicate,
 * 						or -1 if not found
 * @see #lastIndexOf(Object)
 */
public int lastIndexOfIf(Predicate<Byte> predicate) {
    return doLastIndexOfIf(predicate, size() - 1);
}

    /**
 * Returns the index of the last occurrence of the specified element in this list,
 * starting the search at the specified position. If the element is not found, -1 is returned.
 *
 * @param predicate		predicate used to search for the element
 * @param fromIndex		start index for search
 * @return				the index of the last occurrence of the specified element in this list that is less than or equal to fromIndex,
 * 						or -1 if this list does not contain the element
 * @see #lastIndexOf(Object)
 */
public int lastIndexOfIf(Predicate<Byte> predicate, int fromIndex) {
    int size = size();
    if (fromIndex >= size) {
        fromIndex = size - 1;
    }
    return doLastIndexOfIf(predicate, fromIndex);
}

    /**
 * Returns the index of the last occurrence of the specified element in this list,
 * starting the search at the specified position. If the element is not found, -1 is returned.
 */
protected int doLastIndexOfIf(Predicate<Byte> predicate, int fromIndex) {
    for (int i = fromIndex; i >= 0; i--) {
        if (predicate.test(doGet(i))) {
            return i;
        }
    }
    return -1;
}

    
public boolean removeElem(byte elem) {
    int index = indexOf(elem);
    if (index == -1) {
        return false;
    }
    doRemove(index);
    return true;
}

    
public boolean contains(byte elem) {
    return indexOf(elem) != -1;
}

    /**
 * Determines whether the list contains a matching element.
 *
 * @param predicate		predicate used to search element
 * @return				true if the list contains a matching element, false otherwise
 */

public boolean containsIf(Predicate<Byte> predicate) {
    return indexOfIf(predicate) != -1;
}

    // CopyOnWriteArrayList contains methods addIfAbsent() and addAllAbsent()
public boolean addIfAbsent(byte elem) {
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
public boolean containsAny(Collection<Byte> coll) {
    // Note that the signature has been chosen as in List:
    // - boolean addAll(Collection<Byte> c);
    // - boolean containsAll(Collection<Byte> c);
    for (byte elem : coll) {
        if (contains(elem)) {
            return true;
        }
    }
    return false;
}

    
public boolean containsAll(Collection<Byte> coll) {
    // Note that this method is already implemented in AbstractCollection.
    // It has been duplicated so the method is also available in the primitive classes.
    for (byte elem : coll) {
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
public IByteList removeAll(byte elem) {
    IByteList list = doCreate(-1);
    int size = size();
    for (int i = 0; i < size; i++) {
        byte e = doGet(i);
        if (equalsElem(elem, e)) {
            list.add(e);
            doRemove(i);
            size--;
            i--;
        }
    }
    return list;
}

    
public boolean removeAll(Collection<Byte> coll) {
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
public boolean removeAll(IByteList coll) {
    // There is a special implementation accepting an IByteList
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

    
public boolean retainAll(Collection<Byte> coll) {
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
public boolean retainAll(IByteList coll) {
    // There is a special implementation accepting an IByteList
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

    
public byte[] toArray() {
    return toArray(0, size());
}

    
public byte[] toArray(byte[] array) {
    return toArray(array, 0, size());
}

    /**
 * Returns an array containing the elements in this list.
 *
 * @param clazz	class for array elements
 * @return		array containing the specified elements
 */
public byte[] toArray(Class clazz) {
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
public byte[] toArray(int index, int len) {
    byte[] array = new byte[len];
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
public byte[] toArray(byte[] array, int index, int len) {
    if (array.length < len) {
        array = (byte[]) doCreateArray(array.getClass().getComponentType(), len);
    }
    doGetAll(array, index, len);
    if (array.length > len) {
        array[len] = (byte) 0;
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
public byte[] toArray(Class clazz, int index, int len) {
    byte[] array = doCreateArray(clazz, len);
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
protected byte[] doCreateArray(Class clazz, int len) {
    return (byte[]) java.lang.reflect.Array.newInstance(clazz, len);
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
protected void doGetAll(byte[] array, int index, int len) {
    for (int i = 0; i < len; i++) {
        array[i] = doGet(index + i);
    }
}

    /**
 * Helper method for adding multiple elements to the list.
 * This default implementation calls doAdd() for adding each element.
 *
 * @param index index where element should be added (-1 is valid for adding at the end)
 * @param list	list with elements to add
 * @return      true if elements have been added, false otherwise
 */
protected boolean doAddAll(int index, IByteListable list) {
    int listSize = list.size();
    if (listSize == 0) {
        return false;
    }
    int size = size();
    if (size + listSize > capacity()) {
        doEnsureCapacity(size + listSize);
    }
    boolean changed = false;
    int prevSize = size();
    for (int i = 0; i < listSize; i++) {
        byte elem = list.get(i);
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









    /**
 * See {@link Deque#peek}
 */
public byte peek() {
    if (size() == 0) {
        return (byte) 0;
    }
    return getFirst();
}

    /**
 * See {@link Deque#element}
 */
public byte element() {
    // inline version of getFirst():
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doGet(0);
}

    /**
 * See {@link Deque#poll}
 */
public byte poll() {
    if (size() == 0) {
        return (byte) 0;
    }
    return doRemove(0);
}

    /**
 * See {@link Deque#remove}
 */
public byte remove() {
    // inline version of removeFirst():
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doRemove(0);
}

    /**
 * See {@link Deque#offer}
 */
public boolean offer(byte elem) {
    // inline version of add(elem):
    return doAdd(-1, elem);
}

    
public byte getFirst() {
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doGet(0);
}

    
public byte getFirstOrNull() {
    if (size() == 0) {
        return (byte) 0;
    }
    return doGet(0);
}

    /**
 * See {@link Deque#getLast}
 */
public byte getLast() {
    int size = size();
    if (size == 0) {
        throw new NoSuchElementException();
    }
    return doGet(size - 1);
}

    /**
 * Returns the last element stored in the list.
 * If the list is empty, null is returned.
 *
 * @return	last element stored in the list or null if empty
 */
public byte getLastOrNull() {
    int size = size();
    if (size == 0) {
        return (byte) 0;
    }
    return doGet(size - 1);
}

    /**
 * See {@link Deque#addFirst}
 */
public void addFirst(byte elem) {
    doAdd(0, elem);
}

    /**
 * See {@link Deque#addLast}
 */
public void addLast(byte elem) {
    // inline version of add(elem):
    doAdd(-1, elem);
}

    /**
 * See {@link Deque#removeFirst}
 */
public byte removeFirst() {
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doRemove(0);
}

    /**
 * See {@link Deque#removeLast}
 */
public byte removeLast() {
    int size = size();
    if (size == 0) {
        throw new NoSuchElementException();
    }
    return doRemove(size - 1);
}

    /**
 * See {@link Deque#offerFirst}
 */
public boolean offerFirst(byte elem) {
    // inline version of addFirst(elem):
    doAdd(0, elem);
    return true;
}

    /**
 * See {@link Deque#offerLast}
 */
public boolean offerLast(byte elem) {
    // inline version of addLast(elem):
    doAdd(-1, elem);
    return true;
}

    /**
 * See {@link Deque#peekFirst}
 */
public byte peekFirst() {
    if (size() == 0) {
        return (byte) 0;
    }
    return doGet(0);
}

    /**
 * See {@link Deque#peekLast}
 */
public byte peekLast() {
    int size = size();
    if (size == 0) {
        return (byte) 0;
    }
    return doGet(size - 1);
}

    /**
 * See {@link Deque#pollFirst}
 */
public byte pollFirst() {
    if (size() == 0) {
        return (byte) 0;
    }
    return doRemove(0);
}

    /**
 * See {@link Deque#pollLast}
 */
public byte pollLast() {
    int size = size();
    if (size == 0) {
        return (byte) 0;
    }
    return doRemove(size - 1);
}

    /**
 * See {@link Deque#pop}
 */
public byte pop() {
    // inline version of removeFirst():
    if (size() == 0) {
        throw new NoSuchElementException();
    }
    return doRemove(0);
}

    /**
 * See {@link Deque#push}
 */
public void push(byte elem) {
    // inline version of addFirst();
    doAdd(0, elem);
}

    /**
 * See {@link Deque#removeFirstOccurrence}
 */
public boolean removeFirstOccurrence(byte elem) {
    int index = indexOf(elem);
    if (index == -1) {
        return false;
    }
    doRemove(index);
    return true;
}

    /**
 * See {@link Deque#removeLastOccurrence}
 */
public boolean removeLastOccurrence(byte elem) {
    int index = lastIndexOf(elem);
    if (index == -1) {
        return false;
    }
    doRemove(index);
    return true;
}

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
public static void transferCopy(IByteList src, int srcIndex, int srcLen, IByteList dst, int dstIndex, int dstLen) {
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
public static void transferMove(IByteList src, int srcIndex, int srcLen, IByteList dst, int dstIndex, int dstLen) {
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
public static void transferRemove(IByteList src, int srcIndex, int srcLen, IByteList dst, int dstIndex, int dstLen) {
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

    void doTransfer(int transferMode, int srcIndex, int srcLen, IByteList dst, int dstIndex, int dstLen) {
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
    byte defaultElem = getDefaultElem();
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
            byte elem = doReSet(srcIndex + i, defaultElem);
            dst.doSet(dstIndex + i, elem);
        }
    } else {
        // Copy / Remove
        for (int i = 0; i < srcLen; i++) {
            byte elem = doGet(srcIndex + i);
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
public static void transferSwap(IByteList src, int srcIndex, IByteList dst, int dstIndex, int len) {
    if (src == dst) {
        src.swap(srcIndex, dstIndex, len);
    } else {
        src.doTransferSwap(srcIndex, dst, dstIndex, len);
    }
}

    void doTransferSwap(int srcIndex, IByteList dst, int dstIndex, int len) {
    checkRange(srcIndex, len);
    dst.checkRange(dstIndex, len);
    for (int i = 0; i < len; i++) {
        byte swap = doGet(srcIndex + i);
        swap = dst.doSet(dstIndex + i, swap);
        doSet(srcIndex + i, swap);
    }
}

    /**
 * Create list with specified capacity.
 *
 * @param capacity	initial capacity (use -1 for default capacity)
 * @return			created list
 */
abstract protected // -- Readers --
IByteList doCreate(int capacity) ;

    /**
 * Assign this list the content of the that list.
 * This is done by bitwise copying so the that list should not be used afterwards.
 *
 * @param that list to copy content from
 */
abstract protected void doAssign(IByteList that) ;

    /**
 * Returns specified range of elements from list.
 *
 * @param index index of first element to retrieve
 * @param len   number of elements to retrieve
 * @return      list containing the specified range of elements
 */
public IByteList getAll(int index, int len) {
    checkRange(index, len);
    IByteList list = doCreate(len);
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
public IByteList extract(int index, int len) {
    checkRange(index, len);
    IByteList list = doCreate(len);
    for (int i = 0; i < len; i++) {
        list.add(doGet(index + i));
    }
    remove(index, len);
    return list;
}

    /**
 * Retain specified range of elements from list, the other elements are removed.
 *
 * @param index	index of first element to retain
 * @param len	number of elements to retain
 * @throws 		IndexOutOfBoundsException if the range is invalid
 */
public void retain(int index, int len) {
    checkRange(index, len);
    doRemoveAll(index + len, size() - index - len);
    doRemoveAll(0, index);
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
    for (int i = index + len - 1; i >= index; i--) {
        doRemove(i);
    }
}

    /**
 * Adds all of the elements in the specified list into this list.
 *
 * @param list collection containing elements to be added to this list
 * @return <tt>true</tt> if this list changed as a result of the call
 * @throws NullPointerException if the specified list is null
 */
public boolean addAll(IByteList list) {
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
public boolean addAll(int index, IByteList list) {
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

public boolean addAll(Collection<Byte> coll) {
    return doAddAll(-1, asIByteListable(coll));
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

public boolean addAll(int index, Collection<Byte> coll) {
    checkIndexAdd(index);
    return doAddAll(index, asIByteListable(coll));
}

    /**
 * Adds all specified elements into this list.
 *
 * @param elems elements to be added to this list
 * @return <tt>true</tt> if this list changed as a result of the call
 */
@SuppressWarnings("unchecked")
public boolean addArray(byte... elems) {
    return doAddAll(-1, new IByteListableFromArray(elems));
}

    public boolean addArray(byte[] elems, int offset, int length) {
    return doAddAll(-1, new IByteListableFromArray(elems, offset, length));
}

    public boolean addArray(int index, byte[] elems, int offset, int length) {
    return doAddAll(index, new IByteListableFromArray(elems, offset, length));
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
public boolean addArray(int index, @SuppressWarnings("unchecked") byte... elems) {
    checkIndexAdd(index);
    return doAddAll(index, new IByteListableFromArray(elems));
}

    /**
 * Adds element multiple time to list.
 *
 * @param elem element to be added to this list
 * @return <tt>true</tt> if this list changed as a result of the call
 */
public boolean addMult(int len, byte elem) {
    return doAddAll(-1, new IByteListableFromMult(len, elem));
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
public boolean addMult(int index, int len, byte elem) {
    checkIndexAdd(index);
    return doAddAll(index, new IByteListableFromMult(len, elem));
}

    /**
 * Sets the specified elements.
 *
 * @param index index of first element to set
 * @param list  list with elements to set
 */
public void setAll(int index, IByteList list) {
    int listSize = list.size();
    checkRange(index, listSize);
    doReplace(index, listSize, list);
}

    /**
 * Sets the specified elements.
 *
 * @param index index of first element to set
 * @param coll  collection with elements to set
 * @throws 		IndexOutOfBoundsException if the range is invalid
 */
public void setAll(int index, Collection<Byte> coll) {
    int collSize = coll.size();
    checkRange(index, collSize);
    doReplace(index, collSize, asIByteListable(coll));
}

    /**
 * Sets the specified elements.
 *
 * @param index index of first element to set
 * @param elems	array with elements to set
 * @throws 		IndexOutOfBoundsException if the range is invalid
 */
@SuppressWarnings("unchecked")
public void setArray(int index, byte... elems) {
    int arrayLen = elems.length;
    checkRange(index, arrayLen);
    doReplace(index, arrayLen, new IByteListableFromArray(elems));
}

    public void setArray(int index, byte[] elems, int offset, int length) {
    int arrayLen = elems.length;
    checkRange(index, arrayLen);
    doReplace(index, arrayLen, new IByteListableFromArray(elems, offset, length));
}

    /**
 * Sets the element multiple times.
 *
 * @param index index of first element to set
 * @param elem	element to set
 */
public void setMult(int index, int len, byte elem) {
    checkRange(index, len);
    doReplace(index, len, new IByteListableFromMult(len, elem));
}

    /**
 * Set or add the specified elements.
 *
 * @param index index of first element to set or add
 * @param list  list with elements to set or add
 */
public void putAll(int index, IByteList list) {
    doPutAll(index, list);
}

    /**
 * Set or add the specified elements.
 * If the index is smaller than the size of the list, the existing element is replaced.
 * If the index equals the size of the list, the element is added.
 *
 * @param index index of first element to set or add
 * @param coll  collection with elements to set or add
 */
public void putAll(int index, Collection<Byte> coll) {
    doPutAll(index, asIByteListable(coll));
}

    protected void doPutAll(int index, IByteListable list) {
    checkIndexAdd(index);
    checkNonNull(list);
    int len = size() - index;
    if (list.size() < len) {
        len = list.size();
    }
    // Call worker method
    doReplace(index, len, list);
}

    /**
 * Set or add the specified elements.
 * If the index is smaller than the size of the list, the existing element is replaced.
 * If the index equals the size of the list, the element is added.
 *
 * @param index index of first element to set or add
 * @param elems	array with elements to set or add
 */
@SuppressWarnings("unchecked")
public void putArray(int index, byte... elems) {
    doPutAll(index, new IByteListableFromArray(elems));
}

    /**
 * Set or add the specified element multiple times.
 * If the index is smaller than the size of the list, the existing element is replaced.
 * If the index equals the size of the list, the element is added.
 *
 * @param index index of first element to set or add
 * @param len 	element to set or add
 */
public void putMult(int index, int len, byte elem) {
    doPutAll(index, new IByteListableFromMult(len, elem));
}

    /**
 * Initializes the list so it will afterwards only contain the elements of the collection.
 * The list will grow or shrink as needed.
 *
 * @param list 	list with elements
 * @throws 		IndexOutOfBoundsException if the length is invalid
 */
public void initAll(IByteList list) {
    doInitAll(list);
}

    protected void doInitAll(IByteListable list) {
    checkNonNull(list);
    doClear();
    doAddAll(-1, list);
}

    /**
 * Initializes the list so it will afterwards only contain the elements of the collection.
 * The list will grow or shrink as needed.
 *
 * @param coll 	collection with elements
 * @throws 		IndexOutOfBoundsException if the length is invalid
 */
public void initAll(Collection<Byte> coll) {
    doInitAll(asIByteListable(coll));
}

    /**
 * Return correct IByteListable for passed collection.
 */
@SuppressWarnings("unchecked")
protected IByteListable asIByteListable(Collection<Byte> coll) {
    if (coll instanceof IByteListable) {
        return (IByteListable) coll;
    } else if (coll instanceof List) {
        return new IByteListableFromList((List<Byte>) coll);
    } else {
        return new IByteListableFromCollection(coll);
    }
}

    /**
 * Initializes the list so it will afterwards only contain the elements of the array.
 * The list will grow or shrink as needed.
 *
 * @param elems array with elements
 * @throws 		IndexOutOfBoundsException if the length is invalid
 */
@SuppressWarnings("unchecked")
public void initArray(byte... elems) {
    doInitAll(new IByteListableFromArray(elems));
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
public void initMult(int len, byte elem) {
    checkLength(len);
    doInitAll(new IByteListableFromMult(len, elem));
}

    /**
 * Replaces the specified range with new elements.
 * This method is very powerful as it offers the functionality of many other methods
 * which are therefore only offered for convenience: <br>
 * - addAll(index, list): replaceAll(index, 0, list) <br>
 * - setAll(index, list): replaceAll(index, list.size(), list) <br>
 * - putAll(index, list): replaceAll(index, -1, list) <br>
 * - initAll(list)      : replaceAll(0, this.size(), list) <br>
 * - remove(index, list): replaceAll(index, list.size(), null) <br>
 *
 * @param index index of first element to replace, use -1 for the position after the last element (this.size())
 * @param len	number of elements to replace, use -1 for getting behavior of putAll()
 * @param coll  collection with elements which replace the old elements, use null if elements should only be removed
 * @throws 		IndexOutOfBoundsException if the range is invalid
 */
public void replaceAll(int index, int len, Collection<Byte> coll) {
    replace(index, len, asIByteListable(coll));
}

    /**
 * Replaces the specified range with new elements.
 * This method is very powerful as it offers the functionality of many other methods
 * which are therefore only offered for convenience: <br>
 * - addAll(index, list): replaceAll(index, 0, list) <br>
 * - setAll(index, list): replaceAll(index, list.size(), list) <br>
 * - putAll(index, list): replaceAll(index, -1, list) <br>
 * - initAll(list)      : replaceAll(0, this.size(), list) <br>
 * - remove(index, list): replaceAll(index, list.size(), null) <br>
 *
 * @param index index of first element to replace, use -1 for the position after the last element (this.size())
 * @param len	number of elements to replace, use -1 for getting behavior of putAll()
 * @param elems array with elements which replace the old elements, use null if elements should only be removed
 * @throws 		IndexOutOfBoundsException if the range is invalid
 */
@SuppressWarnings("unchecked")
public void replaceArray(int index, int len, byte... elems) {
    replace(index, len, new IByteListableFromArray(elems));
}

    /**
 * Replaces the specified range with new elements.
 * This method is very powerful as it offers the functionality of many other methods
 * which are therefore only offered for convenience: <br>
 * - addAll(index, list): replaceAll(index, 0, list) <br>
 * - setAll(index, list): replaceAll(index, list.size(), list) <br>
 * - putAll(index, list): replaceAll(index, -1, list) <br>
 * - initAll(list)      : replaceAll(0, this.size(), list) <br>
 * - remove(index, list): replaceAll(index, list.size(), null) <br>
 *
 * @param index 	index of first element to replace, use -1 for the position after the last element (this.size())
 * @param len		number of elements to replace, use -1 for getting behavior of putAll()
 * @param numElems  number of time element has to be added
 * @param elem  	element to add
 * @throws 			IndexOutOfBoundsException if the range is invalid
 */
public void replaceMult(int index, int len, int numElems, byte elem) {
    replace(index, len, new IByteListableFromMult(numElems, elem));
}

    /**
 * Replaces the specified range with new elements.
 * This method is very powerful as it offers the functionality of many other methods
 * which are therefore only offered for convenience: <br>
 * - addAll(index, list): replaceAll(index, 0, list) <br>
 * - setAll(index, list): replaceAll(index, list.size(), list) <br>
 * - putAll(index, list): replaceAll(index, -1, list) <br>
 * - initAll(list)      : replaceAll(0, this.size(), list) <br>
 * - remove(index, list): replaceAll(index, list.size(), null) <br>
 *
 * @param index index of first element to replace, use -1 for the position after the last element (this.size())
 * @param len	number of elements to replace, use -1 for getting behavior of putAll()
 * @param list  list with elements which replace the old elements, use null if elements should only be removed
 * @throws 		IndexOutOfBoundsException if the range is invalid
 */
public void replaceAll(int index, int len, IByteList list) {
    replace(index, len, list);
}

    protected void replace(int index, int len, IByteListable list) {
    // Check arguments
    if (index == -1) {
        index = size();
    } else {
        checkIndexAdd(index);
    }
    if (len == -1) {
        len = size() - index;
        if (list.size() < len) {
            len = list.size();
        }
    } else {
        checkRange(index, len);
    }
    // Call worker method
    doReplace(index, len, list);
}

    protected boolean doReplace(int index, int len, IByteListable list) {
    // There is a special implementation accepting an IByteList
    // so the method is also available in the primitive classes.
    assert index >= 0 && index <= size();
    assert len >= 0 && index + len <= size();
    int listLen = list.size();
    if (listLen > len) {
        doEnsureCapacity(size() - len + listLen);
    }
    // Remove elements
    doRemoveAll(index, len);
    // Add elements
    for (int i = 0; i < listLen; i++) {
        if (!doAdd(index + i, list.get(i))) {
            index--;
        }
    }
    return len > 0 || listLen > 0;
}

    // see java.util.Arrays#fill
public /**
 * Fill list.
 *
 * @param elem  element used for filling
 */
void fill(byte elem) {
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
    // Set<Byte> elements to (byte) 0 after the move operation
    if (srcIndex < dstIndex) {
        int fill = Math.min(len, dstIndex - srcIndex);
        setMult(srcIndex, fill, (byte) 0);
    } else if (srcIndex > dstIndex) {
        int fill = Math.min(len, srcIndex - dstIndex);
        setMult(srcIndex + len - fill, fill, (byte) 0);
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
        byte swap = doGet(index1 + i);
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
        byte swap = doGet(pos1);
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
    assert distance >= 0 && distance < len;
    int num = 0;
    for (int start = 0; num != len; start++) {
        byte elem = doGet(index + start);
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
abstract public void sort(int index, int len) ;

    /**
 *  Searches the specified range for an object using the binary* search algorithm.
 *  <p>
 *  Note that the method is defined to work with an arbitrary type {@literal <K>}.
 *  This allows to search directly for a key field in the object without the need to construct an object containing the key:
 *  <pre>{@code
 *  persons.binarySearch("john", new SearchByName());
 *
 *  class SearchByName implements Comparator<Object> {
 * 	 public int compare(Object o1, Object o2) {
 * 	   String s1 = (o1 instanceof String) ? (String) o1 : ((Name) o1).getName();
 * 	   String s2 = (o2 instanceof String) ? (String) o2 : ((Name) o2).getName();
 * 	   return s1.compareTo(s2);
 * 	 }
 *  }
 *  }</pre>
 *
 *  @param key           the value to be searched for
 *  @param comparator    the comparator by which the list is ordered.
 *                       A <tt>null</tt> value indicates that the elements'
 *                       {@linkplain Comparable natural ordering} should be used.
 *  @return              index of the search key, if it is contained in the array;
 *                       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
 *                       <i>insertion point</i> is defined as the point at which the
 *                       key would be inserted into the array: the index of the first
 *                       element greater than the key, or <tt>a.length</tt> if all
 *                       elements in the array are less than the specified key.  Note
 *                       that this guarantees that the return value will be &gt;= 0 if
 *                       and only if the key is found.
 *
 *  @see Arrays#binarySearch
 */
public int binarySearch(byte key) {
    return binarySearch(0, size(), key);
}

    /**
 *  Searches the specified range for an object using the binary search algorithm.
 *  <p>
 *  Note that the method is defined to work with an arbitrary type {@literal <K>}.
 *  This allows to search directly for a key field in the object without the need to construct an object containing the key:
 *  <pre>{@code
 *  persons.binarySearch("john", new SearchByName());
 *
 *  class SearchByName implements Comparator<Object> {
 * 	 public int compare(Object o1, Object o2) {
 * 	   String s1 = (o1 instanceof String) ? (String) o1 : ((Name) o1).getName();
 * 	   String s2 = (o2 instanceof String) ? (String) o2 : ((Name) o2).getName();
 * 	   return s1.compareTo(s2);
 * 	 }
 *  }
 *  }/</pre>
 *
 *  @param index         index of first element to search
 *  @param len           number of elements to search
 *  @param key           the value to be searched for
 *  @param comparator    the comparator by which the list is ordered.
 *                       A <tt>null</tt> value indicates that the elements'
 *                       {@linkplain Comparable natural ordering} should be used.
 *  @return              index of the search key, if it is contained in the array;
 *                       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
 *                       <i>insertion point</i> is defined as the point at which the
 *                       key would be inserted into the array: the index of the first
 *                       element greater than the key, or <tt>a.length</tt> if all
 *                       elements in the array are less than the specified key.  Note
 *                       that this guarantees that the return value will be &gt;= 0 if
 *                       and only if the key is found.
 *  @throws 				IndexOutOfBoundsException if the range is invalid
 *
 *  @see Arrays#binarySearch
 */
abstract public int binarySearch(int index, int len, byte key) ;

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
 * Check that specified length is valid {@literal (>= 0)}.
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
 * Check that both specified lengths are valid {@literal (>= 0)} and equal.
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
}

    /**
 * Check that object is not null.
 *
 * @param obj object to check
 * @throws NullPointerException if object is null
 */
protected void checkNonNull(Object obj) {
    if (obj == null) {
        throw new NullPointerException("Argument may not be (byte) 0");
    }
}





    // --- End class ListIter ---
    /**
     * Wrapper to treat an array as {@link IByteListable}.
     */
    protected static class IByteListableFromArray implements IByteListable {

        byte[] array;

        int offset;

        int length;

        IByteListableFromArray(byte[] array) {
    this.array = array;
    this.offset = 0;
    this.length = array.length;
}

        IByteListableFromArray(byte[] array, int offset, int length) {
    this.array = array;
    this.offset = offset;
    this.length = length;
}

        
public int size() {
    return length;
}

        
public byte get(int index) {
    return array[offset + index];
}
    }

    /**
     * Wrapper to treat a repeated element as {@link IByteListable}.
     */
    protected static class IByteListableFromMult implements IByteListable {

        int len;

        byte elem;

        IByteListableFromMult(int len, byte elem) {
    this.len = len;
    this.elem = elem;
}

        
public int size() {
    return len;
}

        
public byte get(int index) {
    return elem;
}
    }

    /**
     * Wrapper to treat a {@link List} as {@link IByteListable}.
     */
    protected static class IByteListableFromList implements IByteListable {

        List<Byte> list;

        IByteListableFromList(List<Byte> list) {
    this.list = list;
}

        
public int size() {
    return list.size();
}

        
public byte get(int index) {
    return list.get(index);
}
    }

    /**
     * Wrapper to treat a {@link Collection} as {@link IByteListable}.
     * <p>
     * Note that the implementation relies on the fact that the elements are only accessed sequentially.
     */
    protected static class IByteListableFromCollection implements IByteListable {

        Iterator<Byte> iter;

        int size;

        IByteListableFromCollection(Collection<Byte> coll) {
    iter = coll.iterator();
    size = coll.size();
}

        
public int size() {
    return size;
}

        
public byte get(int index) {
    return iter.next();
}
    }
}
interface IByteListable {
		int size();
		byte get(int index);
	}