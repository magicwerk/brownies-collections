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

import org.magicwerk.brownies.collections.primitive.FloatGapList;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.helper.NaturalComparator;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class FloatObjGapList extends GapList<Float> {

	FloatGapList list;

	static Float[] toWrapper(float[] elems) {
		Float[] e = new Float[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static float[] toPrimitive(Float[] elems) {
		float[] e = new float[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static float[] toPrimitive(GapList<? extends Float> list2) {
		float[] e = new float[list2.size()];
		for (int i = 0; i < e.length; i++) {
			e[i] = list2.get(i);
		}
		return e;
	}

	static float[] toPrimitive(Collection<? extends Float> list) {
		float[] e = new float[list.size()];
		Iterator<? extends Float> iter = list.iterator();
		for (int i = 0; i < e.length; i++) {
			e[i] = iter.next();
		}
		return e;
	}

	public static FloatObjGapList create() {
		return new FloatObjGapList();
	}

	public static FloatObjGapList create(int capacity) {
		return new FloatObjGapList(capacity);
	}

	public static FloatObjGapList create(Float... elems) {
		return new FloatObjGapList(elems);
	}

	public static FloatObjGapList create(Collection<? extends Float> elems) {
		return new FloatObjGapList(elems);
	}

	public FloatObjGapList() {
		super(false, null);
		init();
	}

	public void init() {
		list = new FloatGapList();
	}

	public FloatObjGapList(int capacity) {
		super(false, null);
		init(capacity);
	}

	public void init(int capacity) {
		list = new FloatGapList(capacity);
	}

	public FloatObjGapList(Float... elems) {
		super(false, null);
		init(elems);
	}

	public void init(Float... elems) {
		list = new FloatGapList(toPrimitive(elems));
	}

	public FloatObjGapList(Collection<? extends Float> elems) {
		super(false, null);
		init(elems);
	}
	public void init(Collection<? extends Float> elems) {
		list = new FloatGapList(toPrimitive(elems));
	}

	@Override
	public Object clone() {
		FloatObjGapList list = (FloatObjGapList) super.clone();
		list.list = (FloatGapList) list.list.clone();
		return list;
	}

	@Override
	public FloatObjGapList copy() {
		return (FloatObjGapList) clone();
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
	public Float get(int index) {
		return list.get(index);
	}

	@Override
	public Float doGet(int index) {
		return list.doGet(index);
	}

	@Override
	public void doGetAll(Object[] elems, int index, int len) {
		list.doGetAll(toPrimitive((Float[]) elems), index, len);
	}

	@Override
	public boolean doAdd(int index, Float elem) {
		return list.doAdd(index, elem);
	}

	@Override
	public boolean doAddAll(int index, Float[] elem) {
		return list.doAddAll(index, toPrimitive(elem));
	}

	@Override
	public Float doSet(int index, Float elem) {
		return list.doSet(index, elem);
	}

	@Override
	public void doSetAll(int index, Float[] elem) {
		list.doSetAll(index, toPrimitive(elem));
	}

	@Override
	public Float doRemove(int index) {
		return list.doRemove(index);
	}

	@Override
	public void doRemoveAll(int index, int len) {
		list.doRemoveAll(index, len);
	}

	@Override
	public Float doReSet(int index, Float elem) {
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
		if (elem == null || elem.getClass() != Float.class) {
			return -1;
		}
		return list.indexOf((Float) elem);
	}

	@Override
	public int lastIndexOf(Object elem) {
		if (elem == null || elem.getClass() != Float.class) {
			return -1;
		}
		return list.lastIndexOf((Float) elem);
	}

	@Override
	public boolean remove(Object elem) {
		if (elem == null || elem.getClass() != Float.class) {
			return false;
		}
		int index = list.indexOf((Float) elem);
		if (index == -1) {
			return false;
		}
		list.remove(index);
		return true;
	}

	@Override
	public boolean contains(Object elem) {
		if (elem == null || elem.getClass() != Float.class) {
			return false;
		}
		return list.contains((Float) elem);
	}

	@Override
	public boolean containsAny(Collection<?> coll) {
		return list.containsAny((Collection<Float>) coll);
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
		return list.containsAll((Collection<Float>) coll);
	}

	@Override
	public boolean removeAll(Collection<?> coll) {
		return list.removeAll((Collection<Float>) coll);
	}

	@Override
	public boolean removeAll(GapList<?> coll) {
		return list.removeAll((Collection<Float>) coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll((Collection<Float>) coll);
	}

	@Override
	public boolean retainAll(GapList<?> coll) {
		return list.retainAll((Collection<Float>) coll);
	}

	@Override
	public Object[] toArray() {
		float[] elems = list.toArray();
		return toWrapper(elems);
	}

	@Override
	public Object[] toArray(int index, int len) {
		float[] elems = list.toArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public <T> T[] toArray(T[] array) {
		int size = list.size();
		if (array.length < size) {
			array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
		}
		for (int i = 0; i < size; i++) {
			array[i] = (T) (Float) list.get(i);
		}
		if (array.length > size) {
			array[size] = null;
		}
		return array;
	}

	@Override
	public boolean addAll(Collection<? extends Float> coll) {
		return list.addAll((Collection<Float>) coll);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Float> coll) {
		return list.addAll(index, (Collection<Float>) coll);
	}

	@Override
	public boolean addAll(Float... elems) {
		float[] e = toPrimitive(elems);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, Float... elems) {
		float[] e = toPrimitive(elems);
		return list.addAll(index, e);
	}

	@Override
	public boolean addAll(GapList<? extends Float> list2) {
		float[] e = toPrimitive(list2);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, GapList<? extends Float> list2) {
		float[] e = toPrimitive(list2);
		return list.addAll(index, e);
	}

	@Override
	public boolean removeFirstOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Float.class) {
			return false;
		}
		return list.removeFirstOccurrence((Float) elem);
	}

	@Override
	public boolean removeLastOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Float.class) {
			return false;
		}
		return list.removeLastOccurrence((Float) elem);
	}

	@Override
	public GapList<Float> getAll(int index, int len) {
		float[] elems = list.getArray(index, len);
		return GapList.create(toWrapper(elems));
	}

	@Override
	public Float[] getArray(int index, int len) {
		float[] elems = list.getArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public void setAll(int index, GapList<? extends Float> list2) {
		float[] e = toPrimitive(list2);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Collection<? extends Float> coll) {
		float[] e = toPrimitive(coll);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Float... elems) {
		float[] e = toPrimitive(elems);
		list.setAll(index, e);
	}

	@Override
	public void sort(int index, int len, Comparator comparator) {
		if (comparator != null && comparator != NaturalComparator.INSTANCE) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		list.sort(index, len);
	}

	@Override
	public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
		if (comparator != null && comparator != NaturalComparator.INSTANCE) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch(index, len, (Float) key);
	}

    public GapList<Float> unmodifiableList() {
        return new ImmutableGapList<Float>(this) {
			{
        		FloatGapList list = FloatObjGapList.this.list;
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
			public Float get(int index) {
				return list.get(index);
			}

			@Override
			public Float doGet(int index) {
				return list.doGet(index);
			}

			@Override
			public void doGetAll(Object[] elems, int index, int len) {
				list.doGetAll(toPrimitive((Float[]) elems), index, len);
			}
        };
    }
}
