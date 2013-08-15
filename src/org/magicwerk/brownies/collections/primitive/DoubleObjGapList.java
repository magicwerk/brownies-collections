package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.DoubleGapList;
import org.magicwerk.brownies.collections.GapList;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class DoubleObjGapList extends GapList<Double> {

	private DoubleGapList list;

	static Double[] toWrapper(double[] elems) {
		Double[] e = new Double[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static double[] toPrimitive(Double[] elems) {
		double[] e = new double[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static double[] toPrimitive(GapList<? extends Double> list2) {
		double[] e = new double[list2.size()];
		for (int i = 0; i < e.length; i++) {
			e[i] = list2.get(i);
		}
		return e;
	}

	static double[] toPrimitive(Collection<? extends Double> list) {
		double[] e = new double[list.size()];
		Iterator<? extends Double> iter = list.iterator();
		for (int i = 0; i < e.length; i++) {
			e[i] = iter.next();
		}
		return e;
	}

	public static DoubleObjGapList create() {
		return new DoubleObjGapList();
	}

	public static DoubleObjGapList create(int capacity) {
		return new DoubleObjGapList(capacity);
	}

	public static DoubleObjGapList create(Double... elems) {
		return new DoubleObjGapList(elems);
	}

	public static DoubleObjGapList create(Collection<? extends Double> elems) {
		return new DoubleObjGapList(elems);
	}

	public DoubleObjGapList(INIT init) {
		super(false, null);
	}

	public DoubleObjGapList() {
		super(false, null);
		init();
	}

	public void init() {
		list = new DoubleGapList();
	}

	public DoubleObjGapList(int capacity) {
		super(false, null);
		init(capacity);
	}

	public void init(int capacity) {
		list = new DoubleGapList(capacity);
	}

	public DoubleObjGapList(Double... elems) {
		super(false, null);
		init(elems);
	}

	public void init(Double... elems) {
		list = new DoubleGapList(toPrimitive(elems));
	}

	public DoubleObjGapList(Collection<? extends Double> elems) {
		super(false, null);
		init(elems);
	}
	public void init(Collection<? extends Double> elems) {
		list = new DoubleGapList(toPrimitive(elems));
	}

	@Override
	public Object clone() {
		DoubleObjGapList list = (DoubleObjGapList) super.clone();
		list.list = (DoubleGapList) list.list.clone();
		return list;
	}

	@Override
	public DoubleObjGapList copy() {
		return (DoubleObjGapList) clone();
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
	public Double get(int index) {
		return list.get(index);
	}

	@Override
	public Double doGet(int index) {
		return list.doGet(index);
	}

	@Override
	public void doGetAll(Object[] elems, int index, int len) {
		list.doGetAll(toPrimitive((Double[]) elems), index, len);
	}

	@Override
	public boolean doAdd(int index, Double elem) {
		return list.doAdd(index, elem);
	}

	@Override
	public boolean doAddAll(int index, Double[] elem) {
		return list.doAddAll(index, toPrimitive(elem));
	}

	@Override
	public Double doSet(int index, Double elem) {
		return list.doSet(index, elem);
	}

	@Override
	public void doSetAll(int index, Double[] elem) {
		list.doSetAll(index, toPrimitive(elem));
	}

	@Override
	public Double doRemove(int index) {
		return list.doRemove(index);
	}

	@Override
	public void doRemoveAll(int index, int len) {
		list.doRemoveAll(index, len);
	}

	@Override
	public Double doReSet(int index, Double elem) {
		return list.doReSet(index, elem);
	}

	@Override
	public Double doReSet(int index) {
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
		if (elem == null || elem.getClass() != Double.class) {
			return -1;
		}
		return list.indexOf((Double) elem);
	}

	@Override
	public int lastIndexOf(Object elem) {
		if (elem == null || elem.getClass() != Double.class) {
			return -1;
		}
		return list.lastIndexOf((Double) elem);
	}

	@Override
	public boolean remove(Object elem) {
		if (elem == null || elem.getClass() != Double.class) {
			return false;
		}
		int index = list.indexOf((Double) elem);
		if (index == -1) {
			return false;
		}
		list.remove(index);
		return true;
	}

	@Override
	public boolean contains(Object elem) {
		if (elem == null || elem.getClass() != Double.class) {
			return false;
		}
		return list.contains((Double) elem);
	}

	@Override
	public boolean containsAny(Collection<?> coll) {
		return list.containsAny((Collection<Double>) coll);
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
		return list.containsAll((Collection<Double>) coll);
	}

	@Override
	public boolean removeAll(Collection<?> coll) {
		return list.removeAll((Collection<Double>) coll);
	}

	@Override
	public boolean removeAll(GapList<?> coll) {
		return list.removeAll((Collection<Double>) coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll((Collection<Double>) coll);
	}

	@Override
	public boolean retainAll(GapList<?> coll) {
		return list.retainAll((Collection<Double>) coll);
	}

	@Override
	public Object[] toArray() {
		double[] elems = list.toArray();
		return toWrapper(elems);
	}

	@Override
	public Object[] toArray(int index, int len) {
		double[] elems = list.toArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public <T> T[] toArray(T[] array) {
		int size = list.size();
		if (array.length < size) {
			array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
		}
		for (int i = 0; i < size; i++) {
			array[i] = (T) (Double) list.get(i);
		}
		if (array.length > size) {
			array[size] = null;
		}
		return array;
	}

	@Override
	public boolean addAll(Collection<? extends Double> coll) {
		return list.addAll((Collection<Double>) coll);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Double> coll) {
		return list.addAll(index, (Collection<Double>) coll);
	}

	@Override
	public boolean addAll(Double... elems) {
		double[] e = toPrimitive(elems);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, Double... elems) {
		double[] e = toPrimitive(elems);
		return list.addAll(index, e);
	}

	@Override
	public boolean addAll(GapList<? extends Double> list2) {
		double[] e = toPrimitive(list2);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, GapList<? extends Double> list2) {
		double[] e = toPrimitive(list2);
		return list.addAll(index, e);
	}

	@Override
	public boolean removeFirstOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Double.class) {
			return false;
		}
		return list.removeFirstOccurrence((Double) elem);
	}

	@Override
	public boolean removeLastOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Double.class) {
			return false;
		}
		return list.removeLastOccurrence((Double) elem);
	}

	@Override
	public GapList<Double> get(int index, int len) {
		double[] elems = list.getArray(index, len);
		return GapList.create(toWrapper(elems));
	}

	@Override
	public Double[] getArray(int index, int len) {
		double[] elems = list.getArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public void setAll(int index, GapList<? extends Double> list2) {
		double[] e = toPrimitive(list2);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Collection<? extends Double> coll) {
		double[] e = toPrimitive(coll);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Double... elems) {
		double[] e = toPrimitive(elems);
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
		return list.binarySearch(index, len, (Double) key);
	}

    public GapList<Double> unmodifiableList() {
        return new ImmutableGapList<Double>(this) {
			{
        		DoubleGapList list = DoubleObjGapList.this.list;
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
			public Double get(int index) {
				return list.get(index);
			}

			@Override
			public Double doGet(int index) {
				return list.doGet(index);
			}

			@Override
			public void doGetAll(Object[] elems, int index, int len) {
				list.doGetAll(toPrimitive((Double[]) elems), index, len);
			}
        };
    }
}
