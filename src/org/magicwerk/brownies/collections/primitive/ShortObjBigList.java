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
 * $Id: ShortObjBigList.java 2200 2014-03-21 10:46:29Z origo $
 */
package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.ShortBigList;
import org.magicwerk.brownies.collections.BigList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.helper.NaturalComparator;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * ShortObjBigList implements the List interface and uses an instance
 * of ShortBigList for storage. It therefore allows to use the advantages
 * of primitive collections like saved memory and improved execution
 * speed when standard list collections are expected.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * </p>
 *
 * @author Thomas Mauch
 * @version $Id: ShortObjBigList.java 2200 2014-03-21 10:46:29Z origo $
 *
 * @see	    org.magicwerk.brownies.collections.BigList
 * @see	    org.magicwerk.brownies.collections.primitive.ShortBigList
 */
public class ShortObjBigList extends IList<Short> {

	ShortBigList list;

	static Short[] toWrapper(short[] elems) {
		Short[] e = new Short[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static short[] toPrimitive(Short[] elems) {
		short[] e = new short[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static short[] toPrimitive(BigList<? extends Short> list2) {
		short[] e = new short[list2.size()];
		for (int i = 0; i < e.length; i++) {
			e[i] = list2.get(i);
		}
		return e;
	}

	static short[] toPrimitive(Collection<? extends Short> list) {
		short[] e = new short[list.size()];
		Iterator<? extends Short> iter = list.iterator();
		for (int i = 0; i < e.length; i++) {
			e[i] = iter.next();
		}
		return e;
	}

	public static ShortObjBigList create() {
		return new ShortObjBigList();
	}

	public static ShortObjBigList create(Short... elems) {
		ShortObjBigList list = new ShortObjBigList();
		list.init(elems);
		return list;
	}

	public static ShortObjBigList create(Collection<? extends Short> elems) {
		return new ShortObjBigList(elems);
	}

	public ShortObjBigList() {
		init();
	}

	public void init() {
		list = new ShortBigList();
	}

	public ShortObjBigList(int capacity) {
		list = new ShortBigList(capacity);
	}

	public void init(Short... elems) {
		list = ShortBigList.create(toPrimitive(elems));
	}

	public ShortObjBigList(Collection<? extends Short> elems) {
		init(elems);
	}

	public void init(Collection<? extends Short> elems) {
		list = ShortBigList.create(toPrimitive(elems));
	}

	@Override
	protected void doClone(IList<Short> that) {
		list = (ShortBigList) ((ShortObjBigList)that).list.clone();
	}

	@Override
	protected void doAssign(IList<Short> that) {
		ShortObjBigList list = (ShortObjBigList) that;
        this.list = list.list;
	}

	@Override
	public ShortObjBigList copy() {
		return (ShortObjBigList) clone();
	}

	@Override
	public Short getDefaultElem() {
		return list.getDefaultElem();
	}

    @Override
    public IList<Short> doCreate(int capacity) {
    	return new ShortObjBigList();
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
	public Short get(int index) {
		return list.get(index);
	}

	@Override
	protected Short doGet(int index) {
		return list.doGet(index);
	}

	@Override
	protected void doGetAll(Object[] elems, int index, int len) {
		list.doGetAll(toPrimitive((Short[]) elems), index, len);
	}

	@Override
	protected boolean doAdd(int index, Short elem) {
		return list.doAdd(index, elem);
	}

	@Override
	protected boolean doAddAll(int index, Short[] elem) {
		return list.doAddAll(index, toPrimitive(elem));
	}

	@Override
	protected Short doSet(int index, Short elem) {
		return list.doSet(index, elem);
	}

	@Override
	protected void doSetAll(int index, Short[] elem) {
		list.doSetAll(index, toPrimitive(elem));
	}

	@Override
	protected Short doRemove(int index) {
		return list.doRemove(index);
	}

	@Override
	protected void doRemoveAll(int index, int len) {
		list.doRemoveAll(index, len);
	}

	@Override
	protected Short doReSet(int index, Short elem) {
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
		if (elem == null || elem.getClass() != Short.class) {
			return -1;
		}
		return list.indexOf((Short) elem);
	}

	@Override
	public int lastIndexOf(Object elem) {
		if (elem == null || elem.getClass() != Short.class) {
			return -1;
		}
		return list.lastIndexOf((Short) elem);
	}

	@Override
	public boolean remove(Object elem) {
		if (elem == null || elem.getClass() != Short.class) {
			return false;
		}
		int index = list.indexOf((Short) elem);
		if (index == -1) {
			return false;
		}
		list.remove(index);
		return true;
	}

	@Override
	public boolean contains(Object elem) {
		if (elem == null || elem.getClass() != Short.class) {
			return false;
		}
		return list.contains((Short) elem);
	}

	@Override
	public boolean containsAny(Collection<?> coll) {
		return list.containsAny((Collection<Short>) coll);
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
		return list.containsAll((Collection<Short>) coll);
	}

	@Override
	public boolean removeAll(Collection<?> coll) {
		return list.removeAll((Collection<Short>) coll);
	}

	@Override
	public boolean removeAll(IList<?> coll) {
		return list.removeAll((Collection<Short>) coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll((Collection<Short>) coll);
	}

	@Override
	public boolean retainAll(IList<?> coll) {
		return list.retainAll((Collection<Short>) coll);
	}

	@Override
	public Object[] toArray() {
		short[] elems = list.toArray();
		return toWrapper(elems);
	}

	@Override
	public Object[] toArray(int index, int len) {
		short[] elems = list.toArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public <T> T[] toArray(T[] array) {
		int size = list.size();
		if (array.length < size) {
			array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
		}
		for (int i = 0; i < size; i++) {
			array[i] = (T) (Short) list.get(i);
		}
		if (array.length > size) {
			array[size] = null;
		}
		return array;
	}

	@Override
	public boolean addAll(Collection<? extends Short> coll) {
		return list.addAll((Collection<Short>) coll);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Short> coll) {
		return list.addAll(index, (Collection<Short>) coll);
	}

	@Override
	public boolean addAll(Short... elems) {
		short[] e = toPrimitive(elems);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, Short... elems) {
		short[] e = toPrimitive(elems);
		return list.addAll(index, e);
	}

	@Override
	public boolean addAll(IList<? extends Short> list2) {
		short[] e = toPrimitive(list2);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, IList<? extends Short> list2) {
		short[] e = toPrimitive(list2);
		return list.addAll(index, e);
	}

	@Override
	public boolean removeFirstOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Short.class) {
			return false;
		}
		return list.removeFirstOccurrence((Short) elem);
	}

	@Override
	public boolean removeLastOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Short.class) {
			return false;
		}
		return list.removeLastOccurrence((Short) elem);
	}

	@Override
	public BigList<Short> getAll(int index, int len) {
		short[] elems = list.getArray(index, len);
		return BigList.create(toWrapper(elems));
	}

	@Override
	public Short[] getArray(int index, int len) {
		short[] elems = list.getArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public void setAll(int index, IList<? extends Short> list2) {
		short[] e = toPrimitive(list2);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Collection<? extends Short> coll) {
		short[] e = toPrimitive(coll);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Short... elems) {
		short[] e = toPrimitive(elems);
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
		return list.binarySearch(index, len, (Short) key);
	}

    public ShortObjBigList unmodifiableList() {
        return new ImmutableShortObjBigList(this);
    }

    /**
     * An immutable version of a BigList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableShortObjBigList extends ShortObjBigList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
        protected ImmutableShortObjBigList(ShortObjBigList that) {
            super(that);
        }

        @Override
        protected boolean doAdd(int index, Short elem) {
        	error();
        	return false;
        }

        @Override
        protected boolean doAddAll(int index, Short[] elems) {
        	error();
        	return false;
        }

        @Override
        protected Short doSet(int index, Short elem) {
        	error();
        	return null;
        }

        @Override
        protected void doSetAll(int index, Short[] elems) {
        	error();
        }

        @Override
        protected Short doReSet(int index, Short elem) {
        	error();
        	return null;
        }

        @Override
        protected Short doRemove(int index) {
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