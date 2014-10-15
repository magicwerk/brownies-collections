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

import org.magicwerk.brownies.collections.primitive.ByteGapList;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.helper.NaturalComparator;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * ByteObjGapList implements the List interface and uses an instance
 * of ByteGapList for storage. It therefore allows to use the advantages
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
 * @see	    org.magicwerk.brownies.collections.primitive.ByteGapList
 */
public class ByteObjGapList extends IList<Byte> {

	ByteGapList list;

	static Byte[] toWrapper(byte[] elems) {
		Byte[] e = new Byte[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static byte[] toPrimitive(Byte[] elems) {
		byte[] e = new byte[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static byte[] toPrimitive(GapList<? extends Byte> list2) {
		byte[] e = new byte[list2.size()];
		for (int i = 0; i < e.length; i++) {
			e[i] = list2.get(i);
		}
		return e;
	}

	static byte[] toPrimitive(Collection<? extends Byte> list) {
		byte[] e = new byte[list.size()];
		Iterator<? extends Byte> iter = list.iterator();
		for (int i = 0; i < e.length; i++) {
			e[i] = iter.next();
		}
		return e;
	}

	public static ByteObjGapList create() {
		return new ByteObjGapList();
	}

	public static ByteObjGapList create(Byte... elems) {
		ByteObjGapList list = new ByteObjGapList();
		list.init(elems);
		return list;
	}

	public static ByteObjGapList create(Collection<? extends Byte> elems) {
		return new ByteObjGapList(elems);
	}

	public ByteObjGapList() {
		init();
	}

	public void init() {
		list = new ByteGapList();
	}

	public ByteObjGapList(int capacity) {
		list = new ByteGapList(capacity);
	}

	public void init(Byte... elems) {
		list = ByteGapList.create(toPrimitive(elems));
	}

	public ByteObjGapList(Collection<? extends Byte> elems) {
		init(elems);
	}

	public void init(Collection<? extends Byte> elems) {
		list = ByteGapList.create(toPrimitive(elems));
	}

	@Override
	protected void doClone(IList<Byte> that) {
		list = (ByteGapList) ((ByteObjGapList)that).list.clone();
	}

	@Override
	protected void doAssign(IList<Byte> that) {
		ByteObjGapList list = (ByteObjGapList) that;
        this.list = list.list;
	}

	@Override
	public ByteObjGapList copy() {
		return (ByteObjGapList) clone();
	}

	@Override
	public Byte getDefaultElem() {
		return list.getDefaultElem();
	}

    @Override
    public IList<Byte> doCreate(int capacity) {
    	if (capacity == -1) {
    		capacity = GapList.DEFAULT_CAPACITY;
    	}
    	return new ByteObjGapList(capacity);
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
	public Byte get(int index) {
		return list.get(index);
	}

	@Override
	protected Byte doGet(int index) {
		return list.doGet(index);
	}

	@Override
	protected void doGetAll(Object[] elems, int index, int len) {
		list.doGetAll(toPrimitive((Byte[]) elems), index, len);
	}

	@Override
	protected boolean doAdd(int index, Byte elem) {
		return list.doAdd(index, elem);
	}

	@Override
	protected boolean doAddAll(int index, Byte[] elem) {
		return list.doAddAll(index, toPrimitive(elem));
	}

	@Override
	protected Byte doSet(int index, Byte elem) {
		return list.doSet(index, elem);
	}

	@Override
	protected void doSetAll(int index, Byte[] elem) {
		list.doSetAll(index, toPrimitive(elem));
	}

	@Override
	protected Byte doRemove(int index) {
		return list.doRemove(index);
	}

	@Override
	protected void doRemoveAll(int index, int len) {
		list.doRemoveAll(index, len);
	}

	@Override
	protected Byte doReSet(int index, Byte elem) {
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
		if (elem == null || elem.getClass() != Byte.class) {
			return -1;
		}
		return list.indexOf((Byte) elem);
	}

	@Override
	public int lastIndexOf(Object elem) {
		if (elem == null || elem.getClass() != Byte.class) {
			return -1;
		}
		return list.lastIndexOf((Byte) elem);
	}

	@Override
	public boolean remove(Object elem) {
		if (elem == null || elem.getClass() != Byte.class) {
			return false;
		}
		int index = list.indexOf((Byte) elem);
		if (index == -1) {
			return false;
		}
		list.remove(index);
		return true;
	}

	@Override
	public boolean contains(Object elem) {
		if (elem == null || elem.getClass() != Byte.class) {
			return false;
		}
		return list.contains((Byte) elem);
	}

	@Override
	public boolean containsAny(Collection<?> coll) {
		return list.containsAny((Collection<Byte>) coll);
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
		return list.containsAll((Collection<Byte>) coll);
	}

	@Override
	public boolean removeAll(Collection<?> coll) {
		return list.removeAll((Collection<Byte>) coll);
	}

	@Override
	public boolean removeAll(IList<?> coll) {
		return list.removeAll((Collection<Byte>) coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll((Collection<Byte>) coll);
	}

	@Override
	public boolean retainAll(IList<?> coll) {
		return list.retainAll((Collection<Byte>) coll);
	}

	@Override
	public Object[] toArray() {
		return toArray(0, size());
	}

	@Override
	public Object[] toArray(int index, int len) {
		Object[] elems = new Object[len];
		for (int i=0; i<len; i++) {
			elems[i] = list.get(i);
		}
		return elems;
	}

	@Override
	public <T> T[] toArray(T[] array) {
		int size = list.size();
		if (array.length < size) {
			array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
		}
		for (int i = 0; i < size; i++) {
			array[i] = (T) (Byte) list.get(i);
		}
		if (array.length > size) {
			array[size] = null;
		}
		return array;
	}

	@Override
	public boolean addAll(Collection<? extends Byte> coll) {
		return list.addAll((Collection<Byte>) coll);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Byte> coll) {
		return list.addAll(index, (Collection<Byte>) coll);
	}

	@Override
	public boolean addAll(Byte... elems) {
		byte[] e = toPrimitive(elems);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, Byte... elems) {
		byte[] e = toPrimitive(elems);
		return list.addAll(index, e);
	}

	@Override
	public boolean addAll(IList<? extends Byte> list2) {
		byte[] e = toPrimitive(list2);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, IList<? extends Byte> list2) {
		byte[] e = toPrimitive(list2);
		return list.addAll(index, e);
	}

	@Override
	public boolean removeFirstOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Byte.class) {
			return false;
		}
		return list.removeFirstOccurrence((Byte) elem);
	}

	@Override
	public boolean removeLastOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Byte.class) {
			return false;
		}
		return list.removeLastOccurrence((Byte) elem);
	}

	@Override
	public GapList<Byte> getAll(int index, int len) {
		byte[] elems = list.getArray(index, len);
		return GapList.create(toWrapper(elems));
	}

	@Override
	public Byte[] getArray(int index, int len) {
		byte[] elems = list.getArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public void setAll(int index, IList<? extends Byte> list2) {
		byte[] e = toPrimitive(list2);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Collection<? extends Byte> coll) {
		byte[] e = toPrimitive(coll);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Byte... elems) {
		byte[] e = toPrimitive(elems);
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
		return list.binarySearch(index, len, (Byte) key);
	}

    public ByteObjGapList unmodifiableList() {
        return new ImmutableByteObjGapList(this);
    }

    /**
     * An immutable version of a GapList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableByteObjGapList extends ByteObjGapList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
        protected ImmutableByteObjGapList(ByteObjGapList that) {
            super(that);
        }

        @Override
        protected boolean doAdd(int index, Byte elem) {
        	error();
        	return false;
        }

        @Override
        protected boolean doAddAll(int index, Byte[] elems) {
        	error();
        	return false;
        }

        @Override
        protected Byte doSet(int index, Byte elem) {
        	error();
        	return null;
        }

        @Override
        protected void doSetAll(int index, Byte[] elems) {
        	error();
        }

        @Override
        protected Byte doReSet(int index, Byte elem) {
        	error();
        	return null;
        }

        @Override
        protected Byte doRemove(int index) {
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
