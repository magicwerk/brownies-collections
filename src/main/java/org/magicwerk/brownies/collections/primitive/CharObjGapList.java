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
 * $Id: CharacterObjGapList.java 2200 2014-03-21 10:46:29Z origo $
 */
package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.CharGapList;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.helper.NaturalComparator;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * CharObjGapList implements the List interface and uses an instance
 * of CharGapList for storage. It therefore allows to use the advantages
 * of primitive collections like saved memory and improved execution
 * speed when standard list collections are expected.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * </p>
 *
 * @author Thomas Mauch
 * @version $Id: CharObjGapList.java 2200 2014-03-21 10:46:29Z origo $
 *
 * @see	    org.magicwerk.brownies.collections.GapList
 * @see	    org.magicwerk.brownies.collections.primitive.CharGapList
 */
public class CharObjGapList extends IList<Character> {

	CharGapList list;

	static Character[] toWrapper(char[] elems) {
		Character[] e = new Character[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static char[] toPrimitive(Character[] elems) {
		char[] e = new char[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static char[] toPrimitive(GapList<? extends Character> list2) {
		char[] e = new char[list2.size()];
		for (int i = 0; i < e.length; i++) {
			e[i] = list2.get(i);
		}
		return e;
	}

	static char[] toPrimitive(Collection<? extends Character> list) {
		char[] e = new char[list.size()];
		Iterator<? extends Character> iter = list.iterator();
		for (int i = 0; i < e.length; i++) {
			e[i] = iter.next();
		}
		return e;
	}

	public static CharObjGapList create() {
		return new CharObjGapList();
	}

	public static CharObjGapList create(Character... elems) {
		CharObjGapList list = new CharObjGapList();
		list.init(elems);
		return list;
	}

	public static CharObjGapList create(Collection<? extends Character> elems) {
		return new CharObjGapList(elems);
	}

	public CharObjGapList() {
		init();
	}

	public void init() {
		list = new CharGapList();
	}

	public CharObjGapList(int capacity) {
		list = new CharGapList(capacity);
	}

	public void init(Character... elems) {
		list = CharGapList.create(toPrimitive(elems));
	}

	public CharObjGapList(Collection<? extends Character> elems) {
		init(elems);
	}

	public void init(Collection<? extends Character> elems) {
		list = CharGapList.create(toPrimitive(elems));
	}

	@Override
	protected void doClone(IList<Character> that) {
		list = (CharGapList) ((CharObjGapList)that).list.clone();
	}

	@Override
	protected void doAssign(IList<Character> that) {
		CharObjGapList list = (CharObjGapList) that;
        this.list = list.list;
	}

	@Override
	public boolean isReadOnly() {
		return this instanceof ImmutableCharObjGapList;
	}

	@Override
	public CharObjGapList copy() {
		if (this instanceof ImmutableCharObjGapList) {
			CharObjGapList list = new CharObjGapList();
			list.doClone(this);
			return list;
		} else {
			return (CharObjGapList) super.clone();
		}	
	}

	@Override
	public CharObjGapList clone() {
		if (this instanceof ImmutableCharObjGapList) {
			return this;
		} else {
			return (CharObjGapList) super.clone();
		}	
	}

	@Override
	public Character getDefaultElem() {
		return list.getDefaultElem();
	}

    @Override
    public IList<Character> doCreate(int capacity) {
    	if (capacity == -1) {
    		capacity = GapList.DEFAULT_CAPACITY;
    	}
    	return new CharObjGapList(capacity);
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
	public Character get(int index) {
		return list.get(index);
	}

	@Override
	protected Character doGet(int index) {
		return list.doGet(index);
	}

	@Override
	protected void doGetAll(Object[] elems, int index, int len) {
		for (int i=0; i<len; i++) {
			elems[i] = list.doGet(index+i);
		}
	}

	@Override
	protected boolean doAdd(int index, Character elem) {
		return list.doAdd(index, elem);
	}

	@Override
	protected Character doSet(int index, Character elem) {
		return list.doSet(index, elem);
	}

	@Override
	protected Character doRemove(int index) {
		return list.doRemove(index);
	}

	@Override
	protected void doRemoveAll(int index, int len) {
		list.doRemoveAll(index, len);
	}

	@Override
	protected Character doReSet(int index, Character elem) {
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
		if (elem == null || elem.getClass() != Character.class) {
			return -1;
		}
		return list.indexOf((Character) elem);
	}

	@Override
	public int lastIndexOf(Object elem) {
		if (elem == null || elem.getClass() != Character.class) {
			return -1;
		}
		return list.lastIndexOf((Character) elem);
	}

	@Override
	public boolean remove(Object elem) {
		if (elem == null || elem.getClass() != Character.class) {
			return false;
		}
		int index = list.indexOf((Character) elem);
		if (index == -1) {
			return false;
		}
		list.remove(index);
		return true;
	}

	@Override
	public boolean contains(Object elem) {
		if (elem == null || elem.getClass() != Character.class) {
			return false;
		}
		return list.contains((Character) elem);
	}

	@Override
	public boolean containsAny(Collection<?> coll) {
		return list.containsAny((Collection<Character>) coll);
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
		return list.containsAll((Collection<Character>) coll);
	}

	@Override
	public boolean removeAll(Collection<?> coll) {
		return list.removeAll((Collection<Character>) coll);
	}

	@Override
	public boolean removeAll(IList<?> coll) {
		return list.removeAll((Collection<Character>) coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll((Collection<Character>) coll);
	}

	@Override
	public boolean retainAll(IList<?> coll) {
		return list.retainAll((Collection<Character>) coll);
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
			array[i] = (T) (Character) list.get(i);
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
			array[i] = (T) (Character) list.get(i);
		}
        return array;
	}

	@Override
	public boolean addAll(Collection<? extends Character> coll) {
		return list.addAll((Collection<Character>) coll);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Character> coll) {
		return list.addAll(index, (Collection<Character>) coll);
	}

	@Override
	public boolean addArray(Character... elems) {
		char[] e = toPrimitive(elems);
		return list.addArray(e);
	}

	@Override
	public boolean addArray(int index, Character... elems) {
		char[] e = toPrimitive(elems);
		return list.addArray(index, e);
	}

	@Override
	public boolean addAll(IList<? extends Character> list2) {
		char[] e = toPrimitive(list2);
		return list.addArray(e);
	}

	@Override
	public boolean addAll(int index, IList<? extends Character> list2) {
		char[] e = toPrimitive(list2);
		return list.addArray(index, e);
	}

	@Override
	public boolean removeFirstOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Character.class) {
			return false;
		}
		return list.removeFirstOccurrence((Character) elem);
	}

	@Override
	public boolean removeLastOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Character.class) {
			return false;
		}
		return list.removeLastOccurrence((Character) elem);
	}

	@Override
	public GapList<Character> getAll(int index, int len) {
		char[] elems = list.toArray(char.class, index, len);
		return GapList.create(toWrapper(elems));
	}

	@Override
	public void setAll(int index, IList<? extends Character> list2) {
		char[] e = toPrimitive(list2);
		list.setArray(index, e);
	}

	@Override
	public void setAll(int index, Collection<? extends Character> coll) {
		char[] e = toPrimitive(coll);
		list.setArray(index, e);
	}

	@Override
	public void setArray(int index, Character... elems) {
		char[] e = toPrimitive(elems);
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
		return list.binarySearch(index, len, (Character) key);
	}

    public CharObjGapList unmodifiableList() {
		if (this instanceof ImmutableCharObjGapList) {
			return this;
		} else {
	        return new ImmutableCharObjGapList(this);
	    }
    }

    public CharObjGapList immutableList() {
		if (this instanceof ImmutableCharObjGapList) {
			return this;
		} else {
	        return new ImmutableCharObjGapList(this);
	    }
    }

    /**
     * An immutable version of a GapList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableCharObjGapList extends CharObjGapList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
        protected ImmutableCharObjGapList(CharObjGapList that) {
            super(that);
        }

        @Override
        protected boolean doAdd(int index, Character elem) {
        	error();
        	return false;
        }

        @Override
        protected Character doSet(int index, Character elem) {
        	error();
        	return null;
        }

        @Override
        protected Character doReSet(int index, Character elem) {
        	error();
        	return null;
        }

        @Override
        protected Character doRemove(int index) {
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
