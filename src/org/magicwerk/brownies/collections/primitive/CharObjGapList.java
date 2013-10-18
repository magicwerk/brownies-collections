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

import org.magicwerk.brownies.collections.primitive.CharGapList;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.helper.NaturalComparator;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class CharObjGapList extends GapList<Character> {

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

	public static CharObjGapList create(int capacity) {
		return new CharObjGapList(capacity);
	}

	public static CharObjGapList create(Character... elems) {
		return new CharObjGapList(elems);
	}

	public static CharObjGapList create(Collection<? extends Character> elems) {
		return new CharObjGapList(elems);
	}

	public CharObjGapList() {
		super(false, null);
		init();
	}

	public void init() {
		list = new CharGapList();
	}

	public CharObjGapList(int capacity) {
		super(false, null);
		init(capacity);
	}

	public void init(int capacity) {
		list = new CharGapList(capacity);
	}

	public CharObjGapList(Character... elems) {
		super(false, null);
		init(elems);
	}

	public void init(Character... elems) {
		list = new CharGapList(toPrimitive(elems));
	}

	public CharObjGapList(Collection<? extends Character> elems) {
		super(false, null);
		init(elems);
	}
	public void init(Collection<? extends Character> elems) {
		list = new CharGapList(toPrimitive(elems));
	}

	@Override
	public Object clone() {
		CharObjGapList list = (CharObjGapList) super.clone();
		list.list = (CharGapList) list.list.clone();
		return list;
	}

	@Override
	public CharObjGapList copy() {
		return (CharObjGapList) clone();
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public int capacity() {
		return list.capacity();
	}

	@Override
	public Character get(int index) {
		return list.get(index);
	}

	@Override
	public Character doGet(int index) {
		return list.doGet(index);
	}

	@Override
	public void doGetAll(Object[] elems, int index, int len) {
		list.doGetAll(toPrimitive((Character[]) elems), index, len);
	}

	@Override
	public boolean doAdd(int index, Character elem) {
		return list.doAdd(index, elem);
	}

	@Override
	public boolean doAddAll(int index, Character[] elem) {
		return list.doAddAll(index, toPrimitive(elem));
	}

	@Override
	public Character doSet(int index, Character elem) {
		return list.doSet(index, elem);
	}

	@Override
	public void doSetAll(int index, Character[] elem) {
		list.doSetAll(index, toPrimitive(elem));
	}

	@Override
	public Character doRemove(int index) {
		return list.doRemove(index);
	}

	@Override
	public void doRemoveAll(int index, int len) {
		list.doRemoveAll(index, len);
	}

	@Override
	public Character doReSet(int index, Character elem) {
		return list.doReSet(index, elem);
	}

	@Override
    public void move(int srcIndex, int dstIndex, int len) {
    	// Use correct default value
    	list.move(srcIndex, dstIndex, len);
    }

	@Override
	public void ensureCapacity(int minCapacity) {
		list.ensureCapacity(minCapacity);
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
	public boolean removeAll(GapList<?> coll) {
		return list.removeAll((Collection<Character>) coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll((Collection<Character>) coll);
	}

	@Override
	public boolean retainAll(GapList<?> coll) {
		return list.retainAll((Collection<Character>) coll);
	}

	@Override
	public Object[] toArray() {
		char[] elems = list.toArray();
		return toWrapper(elems);
	}

	@Override
	public Object[] toArray(int index, int len) {
		char[] elems = list.toArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public <T> T[] toArray(T[] array) {
		int size = list.size();
		if (array.length < size) {
			array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
		}
		for (int i = 0; i < size; i++) {
			array[i] = (T) (Character) list.get(i);
		}
		if (array.length > size) {
			array[size] = null;
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
	public boolean addAll(Character... elems) {
		char[] e = toPrimitive(elems);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, Character... elems) {
		char[] e = toPrimitive(elems);
		return list.addAll(index, e);
	}

	@Override
	public boolean addAll(GapList<? extends Character> list2) {
		char[] e = toPrimitive(list2);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, GapList<? extends Character> list2) {
		char[] e = toPrimitive(list2);
		return list.addAll(index, e);
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
		char[] elems = list.getArray(index, len);
		return GapList.create(toWrapper(elems));
	}

	@Override
	public Character[] getArray(int index, int len) {
		char[] elems = list.getArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public void setAll(int index, GapList<? extends Character> list2) {
		char[] e = toPrimitive(list2);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Collection<? extends Character> coll) {
		char[] e = toPrimitive(coll);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Character... elems) {
		char[] e = toPrimitive(elems);
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
		return list.binarySearch(index, len, (Character) key);
	}

    public GapList<Character> unmodifiableList() {
        return new ImmutableGapList<Character>(this) {
			{
        		CharGapList list = CharObjGapList.this.list;
			}

			@Override
			public int size() {
				return list.size();
			}

			@Override
			public int capacity() {
				return list.capacity();
			}

			@Override
			public Character get(int index) {
				return list.get(index);
			}

			@Override
			public Character doGet(int index) {
				return list.doGet(index);
			}

			@Override
			public void doGetAll(Object[] elems, int index, int len) {
				list.doGetAll(toPrimitive((Character[]) elems), index, len);
			}
        };
    }
}
