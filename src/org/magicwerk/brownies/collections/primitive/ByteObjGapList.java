package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.ByteGapList;
import org.magicwerk.brownies.collections.GapList;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class ByteObjGapList extends GapList<Byte> {

	private ByteGapList list;

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

	public static ByteObjGapList create(int capacity) {
		return new ByteObjGapList(capacity);
	}

	public static ByteObjGapList create(Byte... elems) {
		return new ByteObjGapList(elems);
	}

	public static ByteObjGapList create(Collection<? extends Byte> elems) {
		return new ByteObjGapList(elems);
	}

//	public ByteObjGapList(INIT init) {
//		super(false, null);
//	}

	public ByteObjGapList() {
		super(false, null);
		init();
	}

	public void init() {
		list = new ByteGapList();
	}

	public ByteObjGapList(int capacity) {
		super(false, null);
		init(capacity);
	}

	public void init(int capacity) {
		list = new ByteGapList(capacity);
	}

	public ByteObjGapList(Byte... elems) {
		super(false, null);
		init(elems);
	}

	public void init(Byte... elems) {
		list = new ByteGapList(toPrimitive(elems));
	}

	public ByteObjGapList(Collection<? extends Byte> elems) {
		super(false, null);
		init(elems);
	}
	public void init(Collection<? extends Byte> elems) {
		list = new ByteGapList(toPrimitive(elems));
	}

	@Override
	public Object clone() {
		ByteObjGapList list = (ByteObjGapList) super.clone();
		list.list = (ByteGapList) list.list.clone();
		return list;
	}

	@Override
	public ByteObjGapList copy() {
		return (ByteObjGapList) clone();
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
	public Byte get(int index) {
		return list.get(index);
	}

	@Override
	public Byte doGet(int index) {
		return list.doGet(index);
	}

	@Override
	public void doGetAll(Object[] elems, int index, int len) {
		list.doGetAll(toPrimitive((Byte[]) elems), index, len);
	}

	@Override
	public boolean doAdd(int index, Byte elem) {
		return list.doAdd(index, elem);
	}

	@Override
	public boolean doAddAll(int index, Byte[] elem) {
		return list.doAddAll(index, toPrimitive(elem));
	}

	@Override
	public Byte doSet(int index, Byte elem) {
		return list.doSet(index, elem);
	}

	@Override
	public void doSetAll(int index, Byte[] elem) {
		list.doSetAll(index, toPrimitive(elem));
	}

	@Override
	public Byte doRemove(int index) {
		return list.doRemove(index);
	}

	@Override
	public void doRemoveAll(int index, int len) {
		list.doRemoveAll(index, len);
	}

	@Override
	public Byte doReSet(int index, Byte elem) {
		return list.doReSet(index, elem);
	}

	@Override
	public Byte doReSet(int index) {
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
	public boolean removeAll(GapList<?> coll) {
		return list.removeAll((Collection<Byte>) coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll((Collection<Byte>) coll);
	}

	@Override
	public boolean retainAll(GapList<?> coll) {
		return list.retainAll((Collection<Byte>) coll);
	}

	@Override
	public Object[] toArray() {
		byte[] elems = list.toArray();
		return toWrapper(elems);
	}

	@Override
	public Object[] toArray(int index, int len) {
		byte[] elems = list.toArray(index, len);
		return toWrapper(elems);
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
	public boolean addAll(GapList<? extends Byte> list2) {
		byte[] e = toPrimitive(list2);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, GapList<? extends Byte> list2) {
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
	public GapList<Byte> get(int index, int len) {
		byte[] elems = list.getArray(index, len);
		return GapList.create(toWrapper(elems));
	}

	@Override
	public Byte[] getArray(int index, int len) {
		byte[] elems = list.getArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public void setAll(int index, GapList<? extends Byte> list2) {
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
		return list.binarySearch(index, len, (Byte) key);
	}

    public GapList<Byte> unmodifiableList() {
        return new ImmutableGapList<Byte>(this) {
			{
        		ByteGapList list = ByteObjGapList.this.list;
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
			public Byte get(int index) {
				return list.get(index);
			}

			@Override
			public Byte doGet(int index) {
				return list.doGet(index);
			}

			@Override
			public void doGetAll(Object[] elems, int index, int len) {
				list.doGetAll(toPrimitive((Byte[]) elems), index, len);
			}
        };
    }
}
