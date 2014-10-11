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
 * $Id$
 */
package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.LongGapList;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.helper.NaturalComparator;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * LongObjGapList implements the List interface and uses an instance
 * of LongGapList for storage. It therefore allows to use the advantages
 * of primitive collections like saved memory and improved execution
 * speed when standard list collections are expected.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * </p>
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @see	    org.magicwerk.brownies.collections.GapList
 * @see	    org.magicwerk.brownies.collections.primitive.LongGapList
 */
public class LongObjGapList extends IList<Long> {

	LongGapList list;

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

	static long[] toPrimitive(GapList<? extends Long> list2) {
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

	public static LongObjGapList create() {
		return new LongObjGapList();
	}

	public static LongObjGapList create(int capacity) {
		return new LongObjGapList(capacity);
	}

	public static LongObjGapList create(Long... elems) {
		return new LongObjGapList(elems);
	}

	public static LongObjGapList create(Collection<? extends Long> elems) {
		return new LongObjGapList(elems);
	}

	public LongObjGapList() {
		init();
	}

	public void init() {
		list = new LongGapList();
	}

	public LongObjGapList(int capacity) {
		list = new LongGapList(capacity);
	}

	public LongObjGapList(Long... elems) {
		init(elems);
	}

	public void init(Long... elems) {
		list = LongGapList.create(toPrimitive(elems));
	}

	public LongObjGapList(Collection<? extends Long> elems) {
		init(elems);
	}

	public void init(Collection<? extends Long> elems) {
		list = LongGapList.create(toPrimitive(elems));
	}

	@Override
	protected void doClone(IList<Long> that) {
		list = (LongGapList) ((LongObjGapList)that).list.clone();
	}

	@Override
	protected void doAssign(IList<Long> that) {
		LongObjGapList list = (LongObjGapList) that;
        this.list = list.list;
	}

	@Override
	public LongObjGapList copy() {
		return (LongObjGapList) clone();
	}

	@Override
	public Long getDefaultElem() {
		return list.getDefaultElem();
	}

    @Override
    public IList<Long> doCreate(int capacity) {
    	if (capacity == -1) {
    		capacity = GapList.DEFAULT_CAPACITY;
    	}
    	return new LongObjGapList(capacity);
    }

	@Override
	public int size() {
		return list.size();
	}

	/**
	 * Returns capacity of this GapList.
	 * Note that two GapLists are considered equal even if they have a distinct capacity.
	 * Also the capacity can be changed by operations like clone() etc.
	 *
	 * @return capacity of this GapList
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
		list.doGetAll(toPrimitive((Long[]) elems), index, len);
	}

	@Override
	protected boolean doAdd(int index, Long elem) {
		return list.doAdd(index, elem);
	}

	@Override
	protected boolean doAddAll(int index, Long[] elem) {
		return list.doAddAll(index, toPrimitive(elem));
	}

	@Override
	protected Long doSet(int index, Long elem) {
		return list.doSet(index, elem);
	}

	@Override
	protected void doSetAll(int index, Long[] elem) {
		list.doSetAll(index, toPrimitive(elem));
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
		return list.equals(obj);
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

	@Override
	public Object[] toArray() {
		long[] elems = list.toArray();
		return toWrapper(elems);
	}

	@Override
	public Object[] toArray(int index, int len) {
		long[] elems = list.toArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public <T> T[] toArray(T[] array) {
		int size = list.size();
		if (array.length < size) {
			array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
		}
		for (int i = 0; i < size; i++) {
			array[i] = (T) (Long) list.get(i);
		}
		if (array.length > size) {
			array[size] = null;
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
	public boolean addAll(Long... elems) {
		long[] e = toPrimitive(elems);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, Long... elems) {
		long[] e = toPrimitive(elems);
		return list.addAll(index, e);
	}

	@Override
	public boolean addAll(IList<? extends Long> list2) {
		long[] e = toPrimitive(list2);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, IList<? extends Long> list2) {
		long[] e = toPrimitive(list2);
		return list.addAll(index, e);
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
	public GapList<Long> getAll(int index, int len) {
		long[] elems = list.getArray(index, len);
		return GapList.create(toWrapper(elems));
	}

	@Override
	public Long[] getArray(int index, int len) {
		long[] elems = list.getArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public void setAll(int index, IList<? extends Long> list2) {
		long[] e = toPrimitive(list2);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Collection<? extends Long> coll) {
		long[] e = toPrimitive(coll);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Long... elems) {
		long[] e = toPrimitive(elems);
		list.setAll(index, e);
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

    public LongObjGapList unmodifiableList() {
        return new ImmutableLongObjGapList(this);
    }

    /**
     * An immutable version of a GapList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableLongObjGapList extends LongObjGapList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
        protected ImmutableLongObjGapList(LongObjGapList that) {
            super(that);
        }

        @Override
        protected boolean doAdd(int index, Long elem) {
        	error();
        	return false;
        }

        @Override
        protected boolean doAddAll(int index, Long[] elems) {
        	error();
        	return false;
        }

        @Override
        protected Long doSet(int index, Long elem) {
        	error();
        	return null;
        }

        @Override
        protected void doSetAll(int index, Long[] elems) {
        	error();
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
