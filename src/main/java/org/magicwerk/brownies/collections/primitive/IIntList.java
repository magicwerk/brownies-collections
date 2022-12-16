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
 * IIntList is an abstract class which offers all interfaces offered by both ArrayList and LinkedList.
 * It also offers additional methods which are then available in all implementations of GapList and BigList.
 *
 * @author Thomas Mauch
 *
 * @param  type of elements stored in the list
 * @see	    java.util.List
 * @see	    java.util.Deque
 * @see	    java.util.ArrayList
 * @see	    java.util.LinkedList
 */
public abstract class IIntList implements Cloneable, Serializable {

    /**
     * Copies the collection values into an array.
     *
     * @param coll   collection of values
     * @return       array containing the collection values
     */
    static int[] toArray(Collection<Integer> coll) {
        Object[] values = coll.toArray();
        int[] v = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            v[i] = (Integer) values[i];
        }
        return v;
    }

    /**
     * Returns a shallow copy of this list.
     * The new list will contain the same elements as the source list, i.e. the elements themselves are not copied.
     * The capacity of the list will be set to the number of elements, i.e. size and capacity are equal.
     * This returned list will be modifiable, i.e. an unmodifiable list will become modifiable again.
     * This method is identical to clone() except that it returns an object with the exact type.
     *
     * @return a modifiable copy of this list
     */
    @SuppressWarnings("unchecked")
    public IIntList copy() {
        return (IIntList) clone();
    }

    /**
     * Returns a shallow copy of this list.
     * The new list will contain the same elements as the source list, i.e. the elements themselves are not copied.
     * The capacity of the list will be set to the number of elements, i.e. size and capacity are equal.
     * This returned list will be modifiable, i.e. an unmodifiable list will become modifiable again.
     * It is advised to use copy() which is identical except that it returns an object with the exact type.
     *
     * @return a modifiable copy of this list
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            IIntList list = (IIntList) super.clone();
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
    public IIntList crop() {
        return doCreate(0);
    }

    // Naming as in java.util.Collections#unmodifiableList
    abstract public IIntList unmodifiableList();

    /**
     * Initialize this object after the bitwise copy has been made by Object.clone().
     *
     * @param that	source object
     */
    abstract protected void doClone(IIntList that);

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
    public void resize(int len, int elem) {
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
        assert (size() == len);
    }

    // Do not remove - needed for generating primitive classes
    abstract public int size();

    /**
     * Returns capacity of this list.
     * Note that two lists are considered equal even if they have a distinct capacity.
     * Also the capacity can be changed by operations like clone() etc.
     *
     * @return capacity of this list
     */
    abstract public int capacity();

    public int get(int index) {
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
    abstract protected int doGet(int index);

    /**
     * Helper method for setting an element in the list.
     * This is the only method which really sets an element.
     * Override if you need to validity checks before setting.
     *
     * @param index index where element will be placed
     * @param elem  element to set
     * @return      old element which was at the position
     */
    abstract protected int doSet(int index, int elem);

    public int set(int index, int elem) {
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
    public int put(int index, int elem) {
        checkIndexAdd(index);
        if (index < size()) {
            return doSet(index, elem);
        } else {
            doAdd(-1, elem);
            return 0;
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
    abstract protected int doReSet(int index, int elem);

    abstract protected int getDefaultElem();

    /**
     * This method is called internally before elements are allocated or freed.
     * Override if you need to validity checks.
     */
    protected void doModify() {
    }

    public boolean add(int elem) {
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
    public void add(int index, int elem) {
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
    abstract protected boolean doAdd(int index, int elem);

    public int remove(int index) {
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
    abstract protected int doRemove(int index);

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
    abstract protected void doEnsureCapacity(int minCapacity);

    // smooth as possible
    abstract public // Note: Provide this method to make transition from ArrayList as
    void trimToSize();

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof IntObjGapList) {
            obj = ((IntObjGapList) obj).list;
        } else if (obj instanceof IntObjBigList) {
            obj = ((IntObjBigList) obj).list;
        }
        if (!(obj instanceof IIntList)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        IIntList list = (IIntList) obj;
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
            int elem = doGet(i);
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
    static boolean equalsElem(int elem1, int elem2) {
        return elem1 == elem2;
    }

    /**
     * Helper method to calculate hash code of a element stored in the list.
     *
     * @param elem	element
     * @return		hash code for element
     */
    static int hashCodeElem(int elem) {
        return (int) elem;
    }

    /**
     * Counts how many times the specified element is contained in the list.
     *
     * @param elem	element to count
     * @return		count how many times the specified element is contained in the list
     */
    public int count(int elem) {
        int count = 0;
        int size = size();
        for (int i = 0; i < size; i++) {
            if (equalsElem(doGet(i), elem)) {
                count++;
            }
        }
        return count;
    }

    public int countIf(Predicate<Integer> predicate) {
        int count = 0;
        int size = size();
        for (int i = 0; i < size; i++) {
            if (predicate.test(doGet(i))) {
                count++;
            }
        }
        return count;
    }

    public int getSingle() {
        if (size() != 1) {
            throw new NoSuchElementException();
        }
        return doGet(0);
    }

    public int getSingleOrNull() {
        int size = size();
        if (size == 0) {
            return 0;
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
    public IIntList getAll(int elem) {
        IIntList list = doCreate(-1);
        int size = size();
        for (int i = 0; i < size; i++) {
            int e = doGet(i);
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
    public int getIf(Predicate<Integer> predicate) {
        int size = size();
        for (int i = 0; i < size; i++) {
            int e = doGet(i);
            if (predicate.test(e)) {
                return e;
            }
        }
        return 0;
    }

    /**
     * Returns distinct elements in the list.
     *
     * @return		distinct elements in the list
     */
    public Set<Integer> getDistinct() {
        Set<Integer> set = new HashSet();
        int size = size();
        for (int i = 0; i < size; i++) {
            set.add(doGet(i));
        }
        return set;
    }

    public <R> IList<R> map(Function<Integer, R> func) {
        int size = size();
        @SuppressWarnings("unchecked")
        IList<R> list = (IList<R>) new GapList<R>(size);
        for (int i = 0; i < size; i++) {
            int e = doGet(i);
            list.add(func.apply(e));
        }
        return list;
    }

    public <R> IList<R> mapFilter(Function<Integer, R> func, Predicate<R> filter) {
        int size = size();
        @SuppressWarnings("unchecked")
        IList<R> list = (IList<R>) new GapList<R>(size);
        for (int i = 0; i < size; i++) {
            int e = doGet(i);
            R r = func.apply(e);
            if (filter.test(r)) {
                list.add(r);
            }
        }
        return list;
    }

    public <R> IList<R> filterMap(Predicate<Integer> filter, Function<Integer, R> func) {
        int size = size();
        @SuppressWarnings("unchecked")
        IList<R> list = (IList<R>) new GapList<R>(size);
        for (int i = 0; i < size; i++) {
            int e = doGet(i);
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
    public IIntList transformedList(UnaryOperator<Integer> op) {
        int size = size();
        IIntList list = doCreate(size);
        for (int i = 0; i < size; i++) {
            int e = doGet(i);
            list.add(op.apply(e));
        }
        return list;
    }

    /**
     * Change the list by applying the specified operator to all elements.
     *
     * @param op	transforming operator
     */
    public void transform(UnaryOperator<Integer> op) {
        int size = size();
        for (int i = 0; i < size; i++) {
            int e = doGet(i);
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
    public IIntList filter(Predicate<Integer> predicate) {
        IIntList list = doCreate(-1);
        int size = size();
        for (int i = 0; i < size; i++) {
            int e = doGet(i);
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
    public boolean retainIf(Predicate<Integer> predicate) {
        // Design: no allocations needed
        int dst = 0;
        int size = size();
        for (int src = 0; src < size; src++) {
            int e = doGet(src);
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
    public boolean removeIf(Predicate<Integer> predicate) {
        // Design: no allocations needed
        int dst = 0;
        int size = size();
        for (int src = 0; src < size; src++) {
            int e = doGet(src);
            if (!predicate.test(e)) {
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
     * Removes and returns all elements in the list which match the predicate.
     *
     * @param predicate	predicate
     * @return			elements which have been removed from the list
     */
    public IIntList extractIf(Predicate<Integer> predicate) {
        // Design: high performance if all or none elements are removed
        // meaning of removed: ==this: all elements are removed, 0: none elements are removed, else: some elements are removed
        IIntList removed = this;
        int dst = 0;
        int size = size();
        for (int src = 0; src < size; src++) {
            int e = doGet(src);
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

    public int indexOf(int elem) {
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
    public int indexOfIf(Predicate<Integer> predicate) {
        int size = size();
        for (int i = 0; i < size; i++) {
            if (predicate.test(doGet(i))) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(int elem) {
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
    public int indexOf(int elem, int fromIndex) {
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
    public int lastIndexOf(int elem, int fromIndex) {
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

    public boolean removeElem(int elem) {
        int index = indexOf(elem);
        if (index == -1) {
            return false;
        }
        doRemove(index);
        return true;
    }

    public boolean contains(int elem) {
        return indexOf(elem) != -1;
    }

    /**
     * Determines whether the list contains a matching element.
     *
     * @param predicate		predicate used to search element
     * @return				true if the list contains a matching element, false otherwise
     */
    public boolean containsIf(Predicate<Integer> predicate) {
        return indexOfIf(predicate) != -1;
    }

    // CopyOnWriteArrayList contains methods addIfAbsent() and addAllAbsent()
    public boolean addIfAbsent(int elem) {
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
    public boolean containsAny(Collection<Integer> coll) {
        // Note that the signature has been chosen as in List:
        // - boolean addAll(Collection<Integer> c);
        // - boolean containsAll(Collection<Integer> c);
        for (int elem : coll) {
            if (contains(elem)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAll(Collection<Integer> coll) {
        // Note that this method is already implemented in AbstractCollection.
        // It has been duplicated so the method is also available in the primitive classes.
        for (int elem : coll) {
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
    public IIntList removeAll(int elem) {
        IIntList list = doCreate(-1);
        int size = size();
        for (int i = 0; i < size; i++) {
            int e = doGet(i);
            if (equalsElem(elem, e)) {
                list.add(e);
                doRemove(i);
                size--;
                i--;
            }
        }
        return list;
    }

    public boolean removeAll(Collection<Integer> coll) {
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
    public boolean removeAll(IIntList coll) {
        // There is a special implementation accepting an IIntList
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

    public boolean retainAll(Collection<Integer> coll) {
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
    public boolean retainAll(IIntList coll) {
        // There is a special implementation accepting an IIntList
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

    public int[] toArray() {
        return toArray(0, size());
    }

    public int[] toArray(int[] array) {
        return toArray(array, 0, size());
    }

    /**
     * Returns an array containing the elements in this list.
     *
     * @param clazz	class for array elements
     * @return		array containing the specified elements
     */
    public int[] toArray(Class clazz) {
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
    public int[] toArray(int index, int len) {
        int[] array = new int[len];
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
    public int[] toArray(int[] array, int index, int len) {
        if (array.length < len) {
            array = (int[]) doCreateArray(array.getClass().getComponentType(), len);
        }
        doGetAll(array, index, len);
        if (array.length > len) {
            array[len] = 0;
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
    public int[] toArray(Class clazz, int index, int len) {
        int[] array = doCreateArray(clazz, len);
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
    protected int[] doCreateArray(Class clazz, int len) {
        return (int[]) java.lang.reflect.Array.newInstance(clazz, len);
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
    protected void doGetAll(int[] array, int index, int len) {
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
    protected boolean doAddAll(int index, IIntList list) {
        int listSize = list.size();
        doEnsureCapacity(size() + listSize);
        if (listSize == 0) {
            return false;
        }
        boolean changed = false;
        int prevSize = size();
        for (int i = 0; i < listSize; i++) {
            int elem = list.get(i);
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

    public int peek() {
        if (size() == 0) {
            return 0;
        }
        return getFirst();
    }

    public int element() {
        // inline version of getFirst():
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return doGet(0);
    }

    public int poll() {
        if (size() == 0) {
            return 0;
        }
        return doRemove(0);
    }

    public int remove() {
        // inline version of removeFirst():
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return doRemove(0);
    }

    public boolean offer(int elem) {
        // inline version of add(elem):
        return doAdd(-1, elem);
    }

    public int getFirst() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return doGet(0);
    }

    public int getFirstOrNull() {
        if (size() == 0) {
            return 0;
        }
        return doGet(0);
    }

    public int getLast() {
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
    public int getLastOrNull() {
        int size = size();
        if (size == 0) {
            return 0;
        }
        return doGet(size - 1);
    }

    public void addFirst(int elem) {
        doAdd(0, elem);
    }

    public void addLast(int elem) {
        // inline version of add(elem):
        doAdd(-1, elem);
    }

    public int removeFirst() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return doRemove(0);
    }

    public int removeLast() {
        int size = size();
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return doRemove(size - 1);
    }

    public boolean offerFirst(int elem) {
        // inline version of addFirst(elem):
        doAdd(0, elem);
        return true;
    }

    public boolean offerLast(int elem) {
        // inline version of addLast(elem):
        doAdd(-1, elem);
        return true;
    }

    public int peekFirst() {
        if (size() == 0) {
            return 0;
        }
        return doGet(0);
    }

    public int peekLast() {
        int size = size();
        if (size == 0) {
            return 0;
        }
        return doGet(size - 1);
    }

    public int pollFirst() {
        if (size() == 0) {
            return 0;
        }
        return doRemove(0);
    }

    public int pollLast() {
        int size = size();
        if (size == 0) {
            return 0;
        }
        return doRemove(size - 1);
    }

    public int pop() {
        // inline version of removeFirst():
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return doRemove(0);
    }

    public void push(int elem) {
        // inline version of addFirst();
        doAdd(0, elem);
    }

    public boolean removeFirstOccurrence(int elem) {
        int index = indexOf(elem);
        if (index == -1) {
            return false;
        }
        doRemove(index);
        return true;
    }

    public boolean removeLastOccurrence(int elem) {
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
    public static void transferCopy(IIntList src, int srcIndex, int srcLen, IIntList dst, int dstIndex, int dstLen) {
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
    public static void transferMove(IIntList src, int srcIndex, int srcLen, IIntList dst, int dstIndex, int dstLen) {
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
    public static void transferRemove(IIntList src, int srcIndex, int srcLen, IIntList dst, int dstIndex, int dstLen) {
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

    void doTransfer(int transferMode, int srcIndex, int srcLen, IIntList dst, int dstIndex, int dstLen) {
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
        int defaultElem = getDefaultElem();
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
                int elem = doReSet(srcIndex + i, defaultElem);
                dst.doSet(dstIndex + i, elem);
            }
        } else {
            // Copy / Remove
            for (int i = 0; i < srcLen; i++) {
                int elem = doGet(srcIndex + i);
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
    public static void transferSwap(IIntList src, int srcIndex, IIntList dst, int dstIndex, int len) {
        if (src == dst) {
            src.swap(srcIndex, dstIndex, len);
        } else {
            src.doTransferSwap(srcIndex, dst, dstIndex, len);
        }
    }

    void doTransferSwap(int srcIndex, IIntList dst, int dstIndex, int len) {
        checkRange(srcIndex, len);
        dst.checkRange(dstIndex, len);
        for (int i = 0; i < len; i++) {
            int swap = doGet(srcIndex + i);
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
    IIntList doCreate(int capacity);

    /**
     * Assign this list the content of the that list.
     * This is done by bitwise copying so the that list should not be used afterwards.
     *
     * @param that list to copy content from
     */
    abstract protected void doAssign(IIntList that);

    /**
     * Returns specified range of elements from list.
     *
     * @param index index of first element to retrieve
     * @param len   number of elements to retrieve
     * @return      list containing the specified range of elements
     */
    public IIntList getAll(int index, int len) {
        checkRange(index, len);
        IIntList list = doCreate(len);
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
    public IIntList extract(int index, int len) {
        checkRange(index, len);
        IIntList list = doCreate(len);
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
    public boolean addAll(IIntList list) {
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
    public boolean addAll(int index, IIntList list) {
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
    public boolean addAll(Collection<Integer> coll) {
        if (coll instanceof List) {
            return doAddAll(-1, new IReadOnlyIntListFromList((List<Integer>) coll));
        } else {
            return doAddAll(-1, new IReadOnlyIntListFromCollection(coll));
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
    public boolean addAll(int index, Collection<Integer> coll) {
        checkIndexAdd(index);
        if (coll instanceof List) {
            return doAddAll(index, new IReadOnlyIntListFromList((List<Integer>) coll));
        } else {
            return doAddAll(index, new IReadOnlyIntListFromCollection(coll));
        }
    }

    /**
     * Adds all specified elements into this list.
     *
     * @param elems elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     */
    public boolean addArray(int... elems) {
        return doAddAll(-1, new IReadOnlyIntListFromArray(elems));
    }

    public boolean addArray(int[] elems, int offset, int length) {
        return doAddAll(-1, new IReadOnlyIntListFromArray(elems, offset, length));
    }

    public boolean addArray(int index, int[] elems, int offset, int length) {
        return doAddAll(index, new IReadOnlyIntListFromArray(elems, offset, length));
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
    public boolean addArray(int index, int... elems) {
        checkIndexAdd(index);
        return doAddAll(index, new IReadOnlyIntListFromArray(elems));
    }

    /**
     * Adds element multiple time to list.
     *
     * @param elem element to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     */
    public boolean addMult(int len, int elem) {
        return doAddAll(-1, new IReadOnlyIntListFromMult(len, elem));
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
    public boolean addMult(int index, int len, int elem) {
        checkIndexAdd(index);
        return doAddAll(index, new IReadOnlyIntListFromMult(len, elem));
    }

    /**
     * Sets the specified elements.
     *
     * @param index index of first element to set
     * @param list  list with elements to set
     * @throws 		IndexOutOfBoundsException if the range is invalid
     */
    public void setAll(int index, IIntList list) {
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
    public void setAll(int index, Collection<Integer> coll) {
        int collSize = coll.size();
        checkRange(index, collSize);
        if (coll instanceof List) {
            doReplaceAll(index, collSize, new IReadOnlyIntListFromList((List<Integer>) coll));
        } else {
            doReplaceAll(index, collSize, new IReadOnlyIntListFromCollection(coll));
        }
    }

    /**
     * Sets the specified elements.
     *
     * @param index index of first element to set
     * @param elems	array with elements to set
     * @throws 		IndexOutOfBoundsException if the range is invalid
     */
    public void setArray(int index, int... elems) {
        int arrayLen = elems.length;
        checkRange(index, arrayLen);
        doReplaceAll(index, arrayLen, new IReadOnlyIntListFromArray(elems));
    }

    public void setArray(int index, int[] elems, int offset, int length) {
        int arrayLen = elems.length;
        checkRange(index, arrayLen);
        doReplaceAll(index, arrayLen, new IReadOnlyIntListFromArray(elems, offset, length));
    }

    /**
     * Sets the element multiple times.
     *
     * @param index index of first element to set
     * @param elem	element to set
     */
    public void setMult(int index, int len, int elem) {
        checkRange(index, len);
        doReplaceAll(index, len, new IReadOnlyIntListFromMult(len, elem));
    }

    /**
     * Set or add the specified elements.
     *
     * @param index index of first element to set or add
     * @param list  list with elements to set or add
     */
    public void putAll(int index, IIntList list) {
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
    public void putAll(int index, Collection<Integer> coll) {
        if (coll instanceof IIntList) {
            putAll(index, (IIntList) coll);
        } else if (coll instanceof List) {
            putAll(index, new IReadOnlyIntListFromList((List<Integer>) coll));
        } else {
            putAll(index, new IReadOnlyIntListFromCollection(coll));
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
    public void putArray(int index, int... elems) {
        putAll(index, new IReadOnlyIntListFromArray(elems));
    }

    /**
     * Set or add the specified element multiple times.
     * If the index is smaller than the size of the list, the existing element is replaced.
     * If the index equals the size of the list, the element is added.
     *
     * @param index index of first element to set or add
     * @param len 	element to set or add
     */
    public void putMult(int index, int len, int elem) {
        putAll(index, new IReadOnlyIntListFromMult(len, elem));
    }

    /**
     * Initializes the list so it will afterwards only contain the elements of the collection.
     * The list will grow or shrink as needed.
     *
     * @param list 	list with elements
     * @throws 		IndexOutOfBoundsException if the length is invalid
     */
    public void initAll(IIntList list) {
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
    public void initAll(Collection<Integer> coll) {
        if (coll instanceof IIntList) {
            initAll((IIntList) coll);
        } else if (coll instanceof List) {
            initAll(new IReadOnlyIntListFromList((List<Integer>) coll));
        } else {
            initAll(new IReadOnlyIntListFromCollection(coll));
        }
    }

    /**
     * Initializes the list so it will afterwards only contain the elements of the array.
     * The list will grow or shrink as needed.
     *
     * @param elems array with elements
     * @throws 		IndexOutOfBoundsException if the length is invalid
     */
    public void initArray(int... elems) {
        initAll(new IReadOnlyIntListFromArray(elems));
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
    public void initMult(int len, int elem) {
        checkLength(len);
        initAll(new IReadOnlyIntListFromMult(len, elem));
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
    public void replaceAll(int index, int len, Collection<Integer> coll) {
        if (coll instanceof IIntList) {
            replaceAll(index, len, (IIntList) coll);
        } else if (coll instanceof List) {
            replaceAll(index, len, new IReadOnlyIntListFromList((List<Integer>) coll));
        } else {
            replaceAll(index, len, new IReadOnlyIntListFromCollection(coll));
        }
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
    public void replaceArray(int index, int len, int... elems) {
        replaceAll(index, len, new IReadOnlyIntListFromArray(elems));
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
    public void replaceMult(int index, int len, int numElems, int elem) {
        replaceAll(index, len, new IReadOnlyIntListFromMult(numElems, elem));
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
    public void replaceAll(int index, int len, IIntList list) {
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

    protected boolean doReplaceAll(int index, int len, IIntList list) {
        // There is a special implementation accepting an IIntList
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

    // see java.util.Arrays#fill
    public /**
     * Fill list.
     *
     * @param elem  element used for filling
     */
    void fill(int elem) {
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
        // Set<Integer> elements to 0 after the move operation
        if (srcIndex < dstIndex) {
            int fill = Math.min(len, dstIndex - srcIndex);
            setMult(srcIndex, fill, 0);
        } else if (srcIndex > dstIndex) {
            int fill = Math.min(len, srcIndex - dstIndex);
            setMult(srcIndex + len - fill, fill, 0);
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
            int swap = doGet(index1 + i);
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
            int swap = doGet(pos1);
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
            int elem = doGet(index + start);
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
    abstract public void sort(int index, int len);

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
    public int binarySearch(int key) {
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
    abstract public int binarySearch(int index, int len, int key);

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
            throw new NullPointerException("Argument may not be 0");
        }
    }

    // --- End class ListIter ---
    protected static abstract class IReadOnlyIntList extends IIntList {

        public IIntList unmodifiableList() {
            error();
            return null;
        }

        protected void doClone(IIntList that) {
            error();
        }

        public int capacity() {
            error();
            return 0;
        }

        protected int doSet(int index, int elem) {
            error();
            return 0;
        }

        protected int doReSet(int index, int elem) {
            error();
            return 0;
        }

        protected int getDefaultElem() {
            error();
            return 0;
        }

        protected boolean doAdd(int index, int elem) {
            error();
            return false;
        }

        protected int doRemove(int index) {
            error();
            return 0;
        }

        protected void doEnsureCapacity(int minCapacity) {
            error();
        }

        public void trimToSize() {
            error();
        }

        protected IIntList doCreate(int capacity) {
            error();
            return null;
        }

        protected void doAssign(IIntList that) {
            error();
        }

        public void sort(int index, int len) {
            error();
        }

        public int binarySearch(int index, int len, int key) {
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

    protected static class IReadOnlyIntListFromArray extends IReadOnlyIntList {

        int[] array;

        int offset;

        int length;

        IReadOnlyIntListFromArray(int[] array) {
            this.array = array;
            this.offset = 0;
            this.length = array.length;
        }

        IReadOnlyIntListFromArray(int[] array, int offset, int length) {
            this.array = array;
            this.offset = offset;
            this.length = length;
        }

        public int size() {
            return length;
        }

        protected int doGet(int index) {
            return array[offset + index];
        }
    }

    protected static class IReadOnlyIntListFromMult extends IReadOnlyIntList {

        int len;

        int elem;

        IReadOnlyIntListFromMult(int len, int elem) {
            checkLength(len);
            this.len = len;
            this.elem = elem;
        }

        public int size() {
            return len;
        }

        protected int doGet(int index) {
            return elem;
        }
    }

    protected static class IReadOnlyIntListFromCollection extends IReadOnlyIntList {

        int[] array;

        IReadOnlyIntListFromCollection(Collection<Integer> coll) {
            array = toArray(coll);
        }

        public int size() {
            return array.length;
        }

        @SuppressWarnings("unchecked")
        protected int doGet(int index) {
            return array[index];
        }
    }

    protected static class IReadOnlyIntListFromList extends IReadOnlyIntList {

        List<Integer> list2;

        @SuppressWarnings("unchecked")
        IReadOnlyIntListFromList(List<Integer> list) {
            this.list2 = (List) list;
        }

        public int size() {
            return list2.size();
        }

        protected int doGet(int index) {
            return list2.get(index);
        }
    }
}
