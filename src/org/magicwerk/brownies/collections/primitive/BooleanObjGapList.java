package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.BooleanGapList;
import org.magicwerk.brownies.collections.GapList;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class BooleanObjGapList extends GapList<Boolean> {

	private BooleanGapList list;

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

	static boolean[] toPrimitive(GapList<? extends Boolean> list2) {
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

	public static BooleanObjGapList create() {
		return new BooleanObjGapList();
	}

	public static BooleanObjGapList create(int capacity) {
		return new BooleanObjGapList(capacity);
	}

	public static BooleanObjGapList create(Boolean... elems) {
		return new BooleanObjGapList(elems);
	}

	public static BooleanObjGapList create(Collection<? extends Boolean> elems) {
		return new BooleanObjGapList(elems);
	}

//	public BooleanObjGapList(INIT init) {
//		super(false, null);
//	}

	public BooleanObjGapList() {
		super(false, null);
		init();
	}

	public void init() {
		list = new BooleanGapList();
	}

	public BooleanObjGapList(int capacity) {
		super(false, null);
		init(capacity);
	}

	public void init(int capacity) {
		list = new BooleanGapList(capacity);
	}

	public BooleanObjGapList(Boolean... elems) {
		super(false, null);
		init(elems);
	}

	public void init(Boolean... elems) {
		list = new BooleanGapList(toPrimitive(elems));
	}

	public BooleanObjGapList(Collection<? extends Boolean> elems) {
		super(false, null);
		init(elems);
	}
	public void init(Collection<? extends Boolean> elems) {
		list = new BooleanGapList(toPrimitive(elems));
	}

	@Override
	public Object clone() {
		BooleanObjGapList list = (BooleanObjGapList) super.clone();
		list.list = (BooleanGapList) list.list.clone();
		return list;
	}

	@Override
	public BooleanObjGapList copy() {
		return (BooleanObjGapList) clone();
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
	public Boolean get(int index) {
		return list.get(index);
	}

	@Override
	public Boolean doGet(int index) {
		return list.doGet(index);
	}

	@Override
	public void doGetAll(Object[] elems, int index, int len) {
		list.doGetAll(toPrimitive((Boolean[]) elems), index, len);
	}

	@Override
	public boolean doAdd(int index, Boolean elem) {
		return list.doAdd(index, elem);
	}

	@Override
	public boolean doAddAll(int index, Boolean[] elem) {
		return list.doAddAll(index, toPrimitive(elem));
	}

	@Override
	public Boolean doSet(int index, Boolean elem) {
		return list.doSet(index, elem);
	}

	@Override
	public void doSetAll(int index, Boolean[] elem) {
		list.doSetAll(index, toPrimitive(elem));
	}

	@Override
	public Boolean doRemove(int index) {
		return list.doRemove(index);
	}

	@Override
	public void doRemoveAll(int index, int len) {
		list.doRemoveAll(index, len);
	}

	@Override
	public Boolean doReSet(int index, Boolean elem) {
		return list.doReSet(index, elem);
	}

	@Override
	public Boolean doReSet(int index) {
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
	public boolean removeAll(GapList<?> coll) {
		return list.removeAll((Collection<Boolean>) coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll((Collection<Boolean>) coll);
	}

	@Override
	public boolean retainAll(GapList<?> coll) {
		return list.retainAll((Collection<Boolean>) coll);
	}

	@Override
	public Object[] toArray() {
		boolean[] elems = list.toArray();
		return toWrapper(elems);
	}

	@Override
	public Object[] toArray(int index, int len) {
		boolean[] elems = list.toArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public <T> T[] toArray(T[] array) {
		int size = list.size();
		if (array.length < size) {
			array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
		}
		for (int i = 0; i < size; i++) {
			array[i] = (T) (Boolean) list.get(i);
		}
		if (array.length > size) {
			array[size] = null;
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
	public boolean addAll(Boolean... elems) {
		boolean[] e = toPrimitive(elems);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, Boolean... elems) {
		boolean[] e = toPrimitive(elems);
		return list.addAll(index, e);
	}

	@Override
	public boolean addAll(GapList<? extends Boolean> list2) {
		boolean[] e = toPrimitive(list2);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, GapList<? extends Boolean> list2) {
		boolean[] e = toPrimitive(list2);
		return list.addAll(index, e);
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
	public GapList<Boolean> get(int index, int len) {
		boolean[] elems = list.getArray(index, len);
		return GapList.create(toWrapper(elems));
	}

	@Override
	public Boolean[] getArray(int index, int len) {
		boolean[] elems = list.getArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public void setAll(int index, GapList<? extends Boolean> list2) {
		boolean[] e = toPrimitive(list2);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Collection<? extends Boolean> coll) {
		boolean[] e = toPrimitive(coll);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Boolean... elems) {
		boolean[] e = toPrimitive(elems);
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
		return list.binarySearch(index, len, (Boolean) key);
	}

    public GapList<Boolean> unmodifiableList() {
        return new ImmutableGapList<Boolean>(this) {
			{
        		BooleanGapList list = BooleanObjGapList.this.list;
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
			public Boolean get(int index) {
				return list.get(index);
			}

			@Override
			public Boolean doGet(int index) {
				return list.doGet(index);
			}

			@Override
			public void doGetAll(Object[] elems, int index, int len) {
				list.doGetAll(toPrimitive((Boolean[]) elems), index, len);
			}
        };
    }
}
