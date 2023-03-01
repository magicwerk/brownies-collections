// ---
// --- DO NOT EDIT
// --- AUTOMATICALLY GENERATED FILE
// ---
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
 * $Id: LongObjBigList.java 2200 2014-03-21 10:46:29Z origo $
 */
package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.LongBigList;
import org.magicwerk.brownies.collections.BigList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.helper.NaturalComparator;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * LongObjBigList implements the List interface and uses an instance
 * of LongBigList for storage. It therefore allows to use the advantages
 * of primitive collections like saved memory and improved execution
 * speed when standard list collections are expected.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * </p>
 *
 * @author Thomas Mauch
 * @version $Id: LongObjBigList.java 2200 2014-03-21 10:46:29Z origo $
 *
 * @see	    org.magicwerk.brownies.collections.BigList
 * @see	    org.magicwerk.brownies.collections.primitive.LongBigList
 */
public class LongObjBigList extends IList<Long> {

    LongBigList list;

    static Long[] toWrapper(long[] elems) {
        Long[] e = new Long[elems.length];
        for (int i = 0; i < e.length; i++) {
            e[i] = elems[i];
        }
        return e;
    }

    static long[] toPrimitive(Long[] elems) {
        long[] e = new long[elems.length];
        for (int i = 0; i < e.length; i++) {
            e[i] = elems[i];
        }
        return e;
    }

    static long[] toPrimitive(BigList<? extends Long> list2) {
        long[] e = new long[list2.size()];
        for (int i = 0; i < e.length; i++) {
            e[i] = list2.get(i);
        }
        return e;
    }

    static long[] toPrimitive(Collection<? extends Long> list) {
        long[] e = new long[list.size()];
        Iterator<? extends Long> iter = list.iterator();
        for (int i = 0; i < e.length; i++) {
            e[i] = iter.next();
        }
        return e;
    }

    public static LongObjBigList create() {
        return new LongObjBigList();
    }

    public static LongObjBigList create(Long... elems) {
        LongObjBigList list = new LongObjBigList();
        list.init(elems);
        return list;
    }

    public static LongObjBigList create(Collection<? extends Long> elems) {
        return new LongObjBigList(elems);
    }

    public LongObjBigList() {
        init();
    }

    public void init() {
        list = new LongBigList();
    }

    public LongObjBigList(int capacity) {
        list = new LongBigList(capacity);
    }

    public void init(Long... elems) {
        list = LongBigList.create(toPrimitive(elems));
    }

    public LongObjBigList(Collection<? extends Long> elems) {
        init(elems);
    }

    public void init(Collection<? extends Long> elems) {
        list = LongBigList.create(toPrimitive(elems));
    }

    @Override
    protected void doClone(IList<Long> that) {
        list = (LongBigList) ((LongObjBigList) that).list.clone();
    }

    @Override
    protected void doAssign(IList<Long> that) {
        LongObjBigList list = (LongObjBigList) that;
        this.list = list.list;
    }

    @Override
    public LongObjBigList copy() {
        return (LongObjBigList) clone();
    }

    @Override
    public Long getDefaultElem() {
        return list.getDefaultElem();
    }

    @Override
    public IList<Long> doCreate(int capacity) {
        return new LongObjBigList();
    }

    @Override
    public int size() {
        return list.size();
    }

    /**
     * Returns capacity of this BigList.
     * Note that two BigLists are considered equal even if they have a distinct capacity.
     * Also the capacity can be changed by operations like clone() etc.
     *
     * @return capacity of this BigList
     */
    public int capacity() {
        return list.capacity();
    }

    @Override
    public Long get(int index) {
        return list.get(index);
    }

    @Override
    protected Long doGet(int index) {
        return list.doGet(index);
    }

    @Override
    protected void doGetAll(Object[] elems, int index, int len) {
        for (int i = 0; i < len; i++) {
            elems[i] = list.doGet(index + i);
        }
    }

    @Override
    protected boolean doAdd(int index, Long elem) {
        return list.doAdd(index, elem);
    }

    @Override
    protected Long doSet(int index, Long elem) {
        return list.doSet(index, elem);
    }

    @Override
    protected Long doRemove(int index) {
        return list.doRemove(index);
    }

    @Override
    protected void doRemoveAll(int index, int len) {
        list.doRemoveAll(index, len);
    }

    @Override
    protected Long doReSet(int index, Long elem) {
        return list.doReSet(index, elem);
    }

    @Override
    protected void doRelease(int index) {
        list.doRelease(index);
    }

    @Override
    public void move(int srcIndex, int dstIndex, int len) {
        // Use correct default value
        list.move(srcIndex, dstIndex, len);
    }

    @Override
    protected void doEnsureCapacity(int minCapacity) {
        list.doEnsureCapacity(minCapacity);
    }

    @Override
    public void trimToSize() {
        list.trimToSize();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && getClass() == obj.getClass()) {
            return list.equals(obj);
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public int indexOf(Object elem) {
        if (elem == null || elem.getClass() != Long.class) {
            return -1;
        }
        return list.indexOf((Long) elem);
    }

    @Override
    public int lastIndexOf(Object elem) {
        if (elem == null || elem.getClass() != Long.class) {
            return -1;
        }
        return list.lastIndexOf((Long) elem);
    }

    @Override
    public boolean remove(Object elem) {
        if (elem == null || elem.getClass() != Long.class) {
            return false;
        }
        int index = list.indexOf((Long) elem);
        if (index == -1) {
            return false;
        }
        list.remove(index);
        return true;
    }

    @Override
    public boolean contains(Object elem) {
        if (elem == null || elem.getClass() != Long.class) {
            return false;
        }
        return list.contains((Long) elem);
    }

    @Override
    public boolean containsAny(Collection<?> coll) {
        return list.containsAny((Collection<Long>) coll);
    }

    @Override
    public boolean containsAll(Collection<?> coll) {
        return list.containsAll((Collection<Long>) coll);
    }

    @Override
    public boolean removeAll(Collection<?> coll) {
        return list.removeAll((Collection<Long>) coll);
    }

    @Override
    public boolean removeAll(IList<?> coll) {
        return list.removeAll((Collection<Long>) coll);
    }

    @Override
    public boolean retainAll(Collection<?> coll) {
        return list.retainAll((Collection<Long>) coll);
    }

    @Override
    public boolean retainAll(IList<?> coll) {
        return list.retainAll((Collection<Long>) coll);
    }

    /**
     * Returns an array containing the specified elements in this list.
     * @see List#toArray()
     *
     * @param index	index of first element to copy
     * @param len	number of elements to copy
     * @return		array containing the specified elements
     */
    public Object[] toArray(int index, int len) {
        Object[] array = new Object[len];
        for (int i = 0; i < len; i++) {
            array[i] = list.get(i);
        }
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
    public <T> T[] toArray(T[] array, int index, int len) {
        if (array.length < len) {
            array = (T[]) doCreateArray(array.getClass().getComponentType(), len);
        }
        for (int i = 0; i < len; i++) {
            array[i] = (T) (Long) list.get(i);
        }
        if (array.length > len) {
            array[len] = null;
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
    public <T> T[] toArray(Class<T> clazz, int index, int len) {
        T[] array = doCreateArray(clazz, len);
        for (int i = 0; i < len; i++) {
            array[i] = (T) (Long) list.get(i);
        }
        return array;
    }

    @Override
    public boolean addAll(Collection<? extends Long> coll) {
        return list.addAll((Collection<Long>) coll);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Long> coll) {
        return list.addAll(index, (Collection<Long>) coll);
    }

    @Override
    public boolean addArray(Long... elems) {
        long[] e = toPrimitive(elems);
        return list.addArray(e);
    }

    @Override
    public boolean addArray(int index, Long... elems) {
        long[] e = toPrimitive(elems);
        return list.addArray(index, e);
    }

    @Override
    public boolean addAll(IList<? extends Long> list2) {
        long[] e = toPrimitive(list2);
        return list.addArray(e);
    }

    @Override
    public boolean addAll(int index, IList<? extends Long> list2) {
        long[] e = toPrimitive(list2);
        return list.addArray(index, e);
    }

    @Override
    public boolean removeFirstOccurrence(Object elem) {
        if (elem == null || elem.getClass() != Long.class) {
            return false;
        }
        return list.removeFirstOccurrence((Long) elem);
    }

    @Override
    public boolean removeLastOccurrence(Object elem) {
        if (elem == null || elem.getClass() != Long.class) {
            return false;
        }
        return list.removeLastOccurrence((Long) elem);
    }

    @Override
    public BigList<Long> getAll(int index, int len) {
        long[] elems = list.toArray(long.class, index, len);
        return BigList.create(toWrapper(elems));
    }

    @Override
    public void setAll(int index, IList<? extends Long> list2) {
        long[] e = toPrimitive(list2);
        list.setArray(index, e);
    }

    @Override
    public void setAll(int index, Collection<? extends Long> coll) {
        long[] e = toPrimitive(coll);
        list.setArray(index, e);
    }

    @Override
    public void setArray(int index, Long... elems) {
        long[] e = toPrimitive(elems);
        list.setArray(index, e);
    }

    @Override
    public void sort(int index, int len, Comparator comparator) {
        if (comparator != null && comparator != NaturalComparator.INSTANCE()) {
            throw new IllegalArgumentException("Only natural comparator (null) allowed");
        }
        list.sort(index, len);
    }

    @Override
    public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
        if (comparator != null && comparator != NaturalComparator.INSTANCE()) {
            throw new IllegalArgumentException("Only natural comparator (null) allowed");
        }
        return list.binarySearch(index, len, (Long) key);
    }

    public LongObjBigList unmodifiableList() {
        return new ImmutableLongObjBigList(this);
    }

    /**
     * An immutable version of a BigList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableLongObjBigList extends LongObjBigList {

        /**
         * UID for serialization
         */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
        protected ImmutableLongObjBigList(LongObjBigList that) {
            super(that);
        }

        @Override
        protected boolean doAdd(int index, Long elem) {
            error();
            return false;
        }

        @Override
        protected Long doSet(int index, Long elem) {
            error();
            return null;
        }

        @Override
        protected Long doReSet(int index, Long elem) {
            error();
            return null;
        }

        @Override
        protected Long doRemove(int index) {
            error();
            return null;
        }

        @Override
        protected void doRemoveAll(int index, int len) {
            error();
        }

        @Override
        protected void doModify() {
            error();
        }

        /**
         * Throw exception if an attempt is made to change an immutable list.
         */
        private void error() {
            throw new UnsupportedOperationException("list is immutable");
        }
    }
}
