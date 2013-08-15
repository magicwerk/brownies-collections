package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.ShortGapList;
import org.magicwerk.brownies.collections.GapList;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class ShortObjGapList extends GapList<Short> {

	private ShortGapList list;

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

	static short[] toPrimitive(GapList<? extends Short> list2) {
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

	public static ShortObjGapList create() {
		return new ShortObjGapList();
	}

	public static ShortObjGapList create(int capacity) {
		return new ShortObjGapList(capacity);
	}

	public static ShortObjGapList create(Short... elems) {
		return new ShortObjGapList(elems);
	}

	public static ShortObjGapList create(Collection<? extends Short> elems) {
		return new ShortObjGapList(elems);
	}

	public ShortObjGapList(INIT init) {
		super(false, null);
	}

	public ShortObjGapList() {
		super(false, null);
		init();
	}

	public void init() {
		list = new ShortGapList();
	}

	public ShortObjGapList(int capacity) {
		super(false, null);
		init(capacity);
	}

	public void init(int capacity) {
		list = new ShortGapList(capacity);
	}

	public ShortObjGapList(Short... elems) {
		super(false, null);
		init(elems);
	}

	public void init(Short... elems) {
		list = new ShortGapList(toPrimitive(elems));
	}

	public ShortObjGapList(Collection<? extends Short> elems) {
		super(false, null);
		init(elems);
	}
	public void init(Collection<? extends Short> elems) {
		list = new ShortGapList(toPrimitive(elems));
	}

	@Override
	public Object clone() {
		ShortObjGapList list = (ShortObjGapList) super.clone();
		list.list = (ShortGapList) list.list.clone();
		return list;
	}

	@Override
	public ShortObjGapList copy() {
		return (ShortObjGapList) clone();
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
	public Short get(int index) {
		return list.get(index);
	}

	@Override
	public Short doGet(int index) {
		return list.doGet(index);
	}

	@Override
	public void doGetAll(Object[] elems, int index, int len) {
		list.doGetAll(toPrimitive((Short[]) elems), index, len);
	}

	@Override
	public boolean doAdd(int index, Short elem) {
		return list.doAdd(index, elem);
	}

	@Override
	public boolean doAddAll(int index, Short[] elem) {
		return list.doAddAll(index, toPrimitive(elem));
	}

	@Override
	public Short doSet(int index, Short elem) {
		return list.doSet(index, elem);
	}

	@Override
	public void doSetAll(int index, Short[] elem) {
		list.doSetAll(index, toPrimitive(elem));
	}

	@Override
	public Short doRemove(int index) {
		return list.doRemove(index);
	}

	@Override
	public void doRemoveAll(int index, int len) {
		list.doRemoveAll(index, len);
	}

	@Override
	public Short doReSet(int index, Short elem) {
		return list.doReSet(index, elem);
	}

	@Override
	public Short doReSet(int index) {
		return list.doReSet(index);
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
	public boolean removeAll(GapList<?> coll) {
		return list.removeAll((Collection<Short>) coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll((Collection<Short>) coll);
	}

	@Override
	public boolean retainAll(GapList<?> coll) {
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
	public boolean addAll(GapList<? extends Short> list2) {
		short[] e = toPrimitive(list2);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, GapList<? extends Short> list2) {
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
	public GapList<Short> get(int index, int len) {
		short[] elems = list.getArray(index, len);
		return GapList.create(toWrapper(elems));
	}

	@Override
	public Short[] getArray(int index, int len) {
		short[] elems = list.getArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public void setAll(int index, GapList<? extends Short> list2) {
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
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		list.sort(index, len);
	}

	@Override
	public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch(index, len, (Short) key);
	}

    public GapList<Short> unmodifiableList() {
        return new ImmutableGapList<Short>(this) {
			{
        		ShortGapList list = ShortObjGapList.this.list;
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
			public Short get(int index) {
				return list.get(index);
			}

			@Override
			public Short doGet(int index) {
				return list.doGet(index);
			}

			@Override
			public void doGetAll(Object[] elems, int index, int len) {
				list.doGetAll(toPrimitive((Short[]) elems), index, len);
			}
        };
    }
}
