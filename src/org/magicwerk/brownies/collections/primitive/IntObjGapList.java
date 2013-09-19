package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.helper.NaturalComparator;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class IntObjGapList extends GapList<Integer> {

	IntGapList list;

	static Integer[] toWrapper(int[] elems) {
		Integer[] e = new Integer[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static int[] toPrimitive(Integer[] elems) {
		int[] e = new int[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static int[] toPrimitive(GapList<? extends Integer> list2) {
		int[] e = new int[list2.size()];
		for (int i = 0; i < e.length; i++) {
			e[i] = list2.get(i);
		}
		return e;
	}

	static int[] toPrimitive(Collection<? extends Integer> list) {
		int[] e = new int[list.size()];
		Iterator<? extends Integer> iter = list.iterator();
		for (int i = 0; i < e.length; i++) {
			e[i] = iter.next();
		}
		return e;
	}

	public static IntObjGapList create() {
		return new IntObjGapList();
	}

	public static IntObjGapList create(int capacity) {
		return new IntObjGapList(capacity);
	}

	public static IntObjGapList create(Integer... elems) {
		return new IntObjGapList(elems);
	}

	public static IntObjGapList create(Collection<? extends Integer> elems) {
		return new IntObjGapList(elems);
	}

//	public IntObjGapList(INIT init) {
//		super(false, null);
//	}

	public IntObjGapList() {
		super(false, null);
		init();
	}

	public void init() {
		list = new IntGapList();
	}

	public IntObjGapList(int capacity) {
		super(false, null);
		init(capacity);
	}

	public void init(int capacity) {
		list = new IntGapList(capacity);
	}

	public IntObjGapList(Integer... elems) {
		super(false, null);
		init(elems);
	}

	public void init(Integer... elems) {
		list = new IntGapList(toPrimitive(elems));
	}

	public IntObjGapList(Collection<? extends Integer> elems) {
		super(false, null);
		init(elems);
	}
	public void init(Collection<? extends Integer> elems) {
		list = new IntGapList(toPrimitive(elems));
	}

	@Override
	public Object clone() {
		IntObjGapList list = (IntObjGapList) super.clone();
		list.list = (IntGapList) list.list.clone();
		return list;
	}

	@Override
	public IntObjGapList copy() {
		return (IntObjGapList) clone();
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
	public Integer get(int index) {
		return list.get(index);
	}

	@Override
	public Integer doGet(int index) {
		return list.doGet(index);
	}

	@Override
	public void doGetAll(Object[] elems, int index, int len) {
		list.doGetAll(toPrimitive((Integer[]) elems), index, len);
	}

	@Override
	public boolean doAdd(int index, Integer elem) {
		return list.doAdd(index, elem);
	}

	@Override
	public boolean doAddAll(int index, Integer[] elem) {
		return list.doAddAll(index, toPrimitive(elem));
	}

	@Override
	public Integer doSet(int index, Integer elem) {
		return list.doSet(index, elem);
	}

	@Override
	public void doSetAll(int index, Integer[] elem) {
		list.doSetAll(index, toPrimitive(elem));
	}

	@Override
	public Integer doRemove(int index) {
		return list.doRemove(index);
	}

	@Override
	public void doRemoveAll(int index, int len) {
		list.doRemoveAll(index, len);
	}

	@Override
	public Integer doReSet(int index, Integer elem) {
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
		if (elem == null || elem.getClass() != Integer.class) {
			return -1;
		}
		return list.indexOf((Integer) elem);
	}

	@Override
	public int lastIndexOf(Object elem) {
		if (elem == null || elem.getClass() != Integer.class) {
			return -1;
		}
		return list.lastIndexOf((Integer) elem);
	}

	@Override
	public boolean remove(Object elem) {
		if (elem == null || elem.getClass() != Integer.class) {
			return false;
		}
		int index = list.indexOf((Integer) elem);
		if (index == -1) {
			return false;
		}
		list.remove(index);
		return true;
	}

	@Override
	public boolean contains(Object elem) {
		if (elem == null || elem.getClass() != Integer.class) {
			return false;
		}
		return list.contains((Integer) elem);
	}

	@Override
	public boolean containsAny(Collection<?> coll) {
		return list.containsAny((Collection<Integer>) coll);
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
		return list.containsAll((Collection<Integer>) coll);
	}

	@Override
	public boolean removeAll(Collection<?> coll) {
		return list.removeAll((Collection<Integer>) coll);
	}

	@Override
	public boolean removeAll(GapList<?> coll) {
		return list.removeAll((Collection<Integer>) coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll((Collection<Integer>) coll);
	}

	@Override
	public boolean retainAll(GapList<?> coll) {
		return list.retainAll((Collection<Integer>) coll);
	}

	@Override
	public Object[] toArray() {
		int[] elems = list.toArray();
		return toWrapper(elems);
	}

	@Override
	public Object[] toArray(int index, int len) {
		int[] elems = list.toArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public <T> T[] toArray(T[] array) {
		int size = list.size();
		if (array.length < size) {
			array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
		}
		for (int i = 0; i < size; i++) {
			array[i] = (T) (Integer) list.get(i);
		}
		if (array.length > size) {
			array[size] = null;
		}
		return array;
	}

	@Override
	public boolean addAll(Collection<? extends Integer> coll) {
		return list.addAll((Collection<Integer>) coll);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Integer> coll) {
		return list.addAll(index, (Collection<Integer>) coll);
	}

	@Override
	public boolean addAll(Integer... elems) {
		int[] e = toPrimitive(elems);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, Integer... elems) {
		int[] e = toPrimitive(elems);
		return list.addAll(index, e);
	}

	@Override
	public boolean addAll(GapList<? extends Integer> list2) {
		int[] e = toPrimitive(list2);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, GapList<? extends Integer> list2) {
		int[] e = toPrimitive(list2);
		return list.addAll(index, e);
	}

	@Override
	public boolean removeFirstOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Integer.class) {
			return false;
		}
		return list.removeFirstOccurrence((Integer) elem);
	}

	@Override
	public boolean removeLastOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Integer.class) {
			return false;
		}
		return list.removeLastOccurrence((Integer) elem);
	}

	@Override
	public GapList<Integer> get(int index, int len) {
		int[] elems = list.getArray(index, len);
		return GapList.create(toWrapper(elems));
	}

	@Override
	public Integer[] getArray(int index, int len) {
		int[] elems = list.getArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public void setAll(int index, GapList<? extends Integer> list2) {
		int[] e = toPrimitive(list2);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Collection<? extends Integer> coll) {
		int[] e = toPrimitive(coll);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Integer... elems) {
		int[] e = toPrimitive(elems);
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
		return list.binarySearch(index, len, (Integer) key);
	}

    public GapList<Integer> unmodifiableList() {
        return new ImmutableGapList<Integer>(this) {
			{
        		IntGapList list = IntObjGapList.this.list;
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
			public Integer get(int index) {
				return list.get(index);
			}

			@Override
			public Integer doGet(int index) {
				return list.doGet(index);
			}

			@Override
			public void doGetAll(Object[] elems, int index, int len) {
				list.doGetAll(toPrimitive((Integer[]) elems), index, len);
			}
        };
    }
}
