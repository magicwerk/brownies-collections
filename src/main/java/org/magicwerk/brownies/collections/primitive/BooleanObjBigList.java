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
 * $Id: BooleanObjBigList.java 2200 2014-03-21 10:46:29Z origo $
 */
package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.BooleanBigList;
import org.magicwerk.brownies.collections.BigList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.helper.NaturalComparator;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * BooleanObjBigList implements the List interface and uses an instance
 * of BooleanBigList for storage. It therefore allows to use the advantages
 * of primitive collections like saved memory and improved execution
 * speed when standard list collections are expected.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * </p>
 *
 * @author Thomas Mauch
 * @version $Id: BooleanObjBigList.java 2200 2014-03-21 10:46:29Z origo $
 *
 * @see	    org.magicwerk.brownies.collections.BigList
 * @see	    org.magicwerk.brownies.collections.primitive.BooleanBigList
 */
public class BooleanObjBigList extends IList<Boolean> {

	BooleanBigList list;

	static Boolean[] toWrapper(boolean[] elems) {
		Boolean[] e = new Boolean[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static boolean[] toPrimitive(Boolean[] elems) {
		boolean[] e = new boolean[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static boolean[] toPrimitive(BigList<? extends Boolean> list2) {
		boolean[] e = new boolean[list2.size()];
		for (int i = 0; i < e.length; i++) {
			e[i] = list2.get(i);
		}
		return e;
	}

	static boolean[] toPrimitive(Collection<? extends Boolean> list) {
		boolean[] e = new boolean[list.size()];
		Iterator<? extends Boolean> iter = list.iterator();
		for (int i = 0; i < e.length; i++) {
			e[i] = iter.next();
		}
		return e;
	}

	public static BooleanObjBigList create() {
		return new BooleanObjBigList();
	}

	public static BooleanObjBigList create(Boolean... elems) {
		BooleanObjBigList list = new BooleanObjBigList();
		list.init(elems);
		return list;
	}

	public static BooleanObjBigList create(Collection<? extends Boolean> elems) {
		return new BooleanObjBigList(elems);
	}

	public BooleanObjBigList() {
		init();
	}

	public void init() {
		list = new BooleanBigList();
	}

	public BooleanObjBigList(int capacity) {
		list = new BooleanBigList(capacity);
	}

	public void init(Boolean... elems) {
		list = BooleanBigList.create(toPrimitive(elems));
	}

	public BooleanObjBigList(Collection<? extends Boolean> elems) {
		init(elems);
	}

	public void init(Collection<? extends Boolean> elems) {
		list = BooleanBigList.create(toPrimitive(elems));
	}

	@Override
	protected void doClone(IList<Boolean> that) {
		list = (BooleanBigList) ((BooleanObjBigList)that).list.clone();
	}

	@Override
	protected void doAssign(IList<Boolean> that) {
		BooleanObjBigList list = (BooleanObjBigList) that;
        this.list = list.list;
	}

	@Override
	public boolean isReadOnly() {
		return this instanceof ImmutableBooleanObjBigList;
	}

	@Override
	public BooleanObjBigList copy() {
		if (this instanceof ImmutableBooleanObjBigList) {
			BooleanObjBigList list = new BooleanObjBigList();
			list.doClone(this);
			return list;
		} else {
			return (BooleanObjBigList) super.clone();
		}	
	}

	@Override
	public BooleanObjBigList clone() {
		if (this instanceof ImmutableBooleanObjBigList) {
			return this;
		} else {
			return (BooleanObjBigList) super.clone();
		}	
	}

	@Override
	public Boolean getDefaultElem() {
		return list.getDefaultElem();
	}

    @Override
    public IList<Boolean> doCreate(int capacity) {
    	return new BooleanObjBigList();
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
	public Boolean get(int index) {
		return list.get(index);
	}

	@Override
	protected Boolean doGet(int index) {
		return list.doGet(index);
	}

	@Override
	protected void doGetAll(Object[] elems, int index, int len) {
		for (int i=0; i<len; i++) {
			elems[i] = list.doGet(index+i);
		}
	}

	@Override
	protected boolean doAdd(int index, Boolean elem) {
		return list.doAdd(index, elem);
	}

	@Override
	protected Boolean doSet(int index, Boolean elem) {
		return list.doSet(index, elem);
	}

	@Override
	protected Boolean doRemove(int index) {
		return list.doRemove(index);
	}

	@Override
	protected void doRemoveAll(int index, int len) {
		list.doRemoveAll(index, len);
	}

	@Override
	protected Boolean doReSet(int index, Boolean elem) {
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
		if (elem == null || elem.getClass() != Boolean.class) {
			return -1;
		}
		return list.indexOf((Boolean) elem);
	}

	@Override
	public int lastIndexOf(Object elem) {
		if (elem == null || elem.getClass() != Boolean.class) {
			return -1;
		}
		return list.lastIndexOf((Boolean) elem);
	}

	@Override
	public boolean remove(Object elem) {
		if (elem == null || elem.getClass() != Boolean.class) {
			return false;
		}
		int index = list.indexOf((Boolean) elem);
		if (index == -1) {
			return false;
		}
		list.remove(index);
		return true;
	}

	@Override
	public boolean contains(Object elem) {
		if (elem == null || elem.getClass() != Boolean.class) {
			return false;
		}
		return list.contains((Boolean) elem);
	}

	@Override
	public boolean containsAny(Collection<?> coll) {
		return list.containsAny((Collection<Boolean>) coll);
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
		return list.containsAll((Collection<Boolean>) coll);
	}

	@Override
	public boolean removeAll(Collection<?> coll) {
		return list.removeAll((Collection<Boolean>) coll);
	}

	@Override
	public boolean removeAll(IList<?> coll) {
		return list.removeAll((Collection<Boolean>) coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll((Collection<Boolean>) coll);
	}

	@Override
	public boolean retainAll(IList<?> coll) {
		return list.retainAll((Collection<Boolean>) coll);
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
		for (int i=0; i<len; i++) {
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
		for (int i=0; i<len; i++) {
			array[i] = (T) (Boolean) list.get(i);
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
		for (int i=0; i<len; i++) {
			array[i] = (T) (Boolean) list.get(i);
		}
        return array;
	}

	@Override
	public boolean addAll(Collection<? extends Boolean> coll) {
		return list.addAll((Collection<Boolean>) coll);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Boolean> coll) {
		return list.addAll(index, (Collection<Boolean>) coll);
	}

	@Override
	public boolean addArray(Boolean... elems) {
		boolean[] e = toPrimitive(elems);
		return list.addArray(e);
	}

	@Override
	public boolean addArray(int index, Boolean... elems) {
		boolean[] e = toPrimitive(elems);
		return list.addArray(index, e);
	}

	@Override
	public boolean addAll(IList<? extends Boolean> list2) {
		boolean[] e = toPrimitive(list2);
		return list.addArray(e);
	}

	@Override
	public boolean addAll(int index, IList<? extends Boolean> list2) {
		boolean[] e = toPrimitive(list2);
		return list.addArray(index, e);
	}

	@Override
	public boolean removeFirstOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Boolean.class) {
			return false;
		}
		return list.removeFirstOccurrence((Boolean) elem);
	}

	@Override
	public boolean removeLastOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Boolean.class) {
			return false;
		}
		return list.removeLastOccurrence((Boolean) elem);
	}

	@Override
	public BigList<Boolean> getAll(int index, int len) {
		boolean[] elems = list.toArray(boolean.class, index, len);
		return BigList.create(toWrapper(elems));
	}

	@Override
	public void setAll(int index, IList<? extends Boolean> list2) {
		boolean[] e = toPrimitive(list2);
		list.setArray(index, e);
	}

	@Override
	public void setAll(int index, Collection<? extends Boolean> coll) {
		boolean[] e = toPrimitive(coll);
		list.setArray(index, e);
	}

	@Override
	public void setArray(int index, Boolean... elems) {
		boolean[] e = toPrimitive(elems);
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
		return list.binarySearch(index, len, (Boolean) key);
	}

    public BooleanObjBigList unmodifiableList() {
		if (this instanceof ImmutableBooleanObjBigList) {
			return this;
		} else {
	        return new ImmutableBooleanObjBigList(this);
	    }
    }

    public BooleanObjBigList immutableList() {
		if (this instanceof ImmutableBooleanObjBigList) {
			return this;
		} else {
	        return new ImmutableBooleanObjBigList(this);
	    }
    }

    /**
     * An immutable version of a BigList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableBooleanObjBigList extends BooleanObjBigList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
        protected ImmutableBooleanObjBigList(BooleanObjBigList that) {
            super(that);
        }

        @Override
        protected boolean doAdd(int index, Boolean elem) {
        	error();
        	return false;
        }

        @Override
        protected Boolean doSet(int index, Boolean elem) {
        	error();
        	return null;
        }

        @Override
        protected Boolean doReSet(int index, Boolean elem) {
        	error();
        	return null;
        }

        @Override
        protected Boolean doRemove(int index) {
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
