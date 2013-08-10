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
		for (int i=0; i<e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static double[] toPrimitive(Double[] elems) {
		double[] e = new double[elems.length];
		for (int i=0; i<e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static double[] toPrimitive(GapList<? extends Double> list2) {
		double[] e = new double[list2.size()];
		for (int i=0; i<e.length; i++) {
			e[i] = list2.get(i);
		}
		return e;
	}

	static double[] toPrimitive(Collection<? extends Double> list2) {
		double[] e = new double[list2.size()];
		Iterator<? extends Double> iter=list2.iterator();
		for (int i=0; i<e.length; i++) {
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

	public DoubleObjGapList() {
		super(false, null);
		list = new DoubleGapList();
	}

	public DoubleObjGapList(int capacity) {
		super(false, null);
		list = new DoubleGapList(capacity);
	}

	public DoubleObjGapList(Double... elems) {
		super(false, null);
		list = new DoubleGapList(toPrimitive(elems));
	}

	public DoubleObjGapList(Collection<? extends Double> elems) {
		super(false, null);
		list = new DoubleGapList(toPrimitive(elems));
	}

	@Override
	public Object clone() {
		DoubleObjGapList list = (DoubleObjGapList) super.clone();
		list.list = (DoubleGapList) list.list.clone();
	    return list;
	}

@Override
public   DoubleObjGapList copy() {
	return  (DoubleObjGapList) clone();
}

@Override
public   void clear() {
	list.clear();
}

@Override
public   int size() {
	return list.size();
}

@Override
public   int capacity() {
	return list.capacity();
}

@Override
public   Double get(int index) {
	return list.get(index);
}

@Override
public   Double set(int index, Double elem) {
	return list.set(index, elem);
}

@Override
public   boolean add(Double elem) {
	return list.add(elem);
}

@Override
public   void add(int index, Double elem) {
	list.add(index, elem);
}

@Override
public   Double remove(int index) {
	return list.remove(index);
}

@Override
public   void ensureCapacity(int minCapacity) {
	list.ensureCapacity(minCapacity);
}

@Override
public   void trimToSize() {
	list.trimToSize();
}

@Override
public   boolean equals(Object obj) {
	return list.equals(obj);
}

@Override
public   int hashCode() {
	return list.hashCode();
}

@Override
public   String toString() {
	return list.toString();
}

@Override
public   boolean isEmpty() {
	return list.isEmpty();
}

@Override
public   int indexOf(Object elem) {
	if (elem == null || elem.getClass() != Double.class) {
		return -1;
	}
	return list.indexOf((Double) elem);
}

@Override
public   int lastIndexOf(Object elem) {
	if (elem == null || elem.getClass() != Double.class) {
		return -1;
	}
	return list.lastIndexOf((Double) elem);
}

@Override
public   boolean remove(Object elem) {
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
public   boolean contains(Object elem) {
	if (elem == null || elem.getClass() != Double.class) {
		return false;
	}
	return list.contains((Double) elem);
}

@Override
public   boolean containsAny(Collection<?> coll) {
	return list.containsAny((Collection<Double>)coll);
}

@Override
public   boolean containsAll(Collection<?> coll) {
	return list.containsAll((Collection<Double>)coll);
}

@Override
public   boolean removeAll(Collection<?> coll) {
	return list.removeAll((Collection<Double>)coll);
}

@Override
public   boolean removeAll(GapList<?> coll) {
	return list.removeAll((Collection<Double>)coll);
}

@Override
public   boolean retainAll(Collection<?> coll) {
	return list.retainAll((Collection<Double>)coll);
}

@Override
public   boolean retainAll(GapList<?> coll) {
	return list.retainAll((Collection<Double>)coll);
}

@Override
public   Object[] toArray() {
	double[] elems = list.toArray();
	return toWrapper(elems);
}

@Override
public   Object[] toArray(int index, int len) {
	double[] elems = list.toArray(index, len);
	return toWrapper(elems);
}

@Override
public  <T> T[] toArray(T[] array) {
	int size = list.size();
    if (array.length < size) {
    	array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
    }
    for (int i=0; i<size; i++) {
    	array[i] = (T)(Double) list.get(i);
    }
    if (array.length > size) {
    	array[size] = null;
    }
    return array;
}

@Override
public   boolean addAll(Collection<? extends Double> coll) {
	return list.addAll((Collection<Double>)coll);
}

@Override
public   boolean addAll(int index, Collection<? extends Double> coll) {
	return list.addAll(index, (Collection<Double>)coll);
}

@Override
public   boolean addAll(Double... elems) {
	double[] e = toPrimitive(elems);
	return list.addAll(e);
}

@Override
public   boolean addAll(int index, Double... elems) {
	double[] e = toPrimitive(elems);
	return list.addAll(index, e);
}

@Override
public   boolean addAll(GapList<? extends Double> list2) {
	double[] e = toPrimitive(list2);
	return list.addAll(e);
}

@Override
public   boolean addAll(int index, GapList<? extends Double> list2) {
	double[] e = toPrimitive(list2);
	return list.addAll(index, e);
}

@Override
public   Double getFirst() {
	return list.getFirst();
}

@Override
public   Double getLast() {
	return list.getLast();
}

@Override
public   void addFirst(Double elem) {
	list.addFirst(elem);
}

@Override
public   void addLast(Double elem) {
	list.addLast(elem);
}

@Override
public   Double removeFirst() {
	return list.removeFirst();
}

@Override
public   Double removeLast() {
	return list.removeLast();
}

@Override
public   Double peek() {
	return list.peek();
}

@Override
public   Double element() {
	return list.element();
}

@Override
public   Double poll() {
	return list.poll();
}

@Override
public   Double remove() {
	return list.remove();
}

@Override
public   boolean offer(Double elem) {
	return list.offer(elem);
}

@Override
public   boolean offerFirst(Double elem) {
	return list.offerFirst(elem);
}

@Override
public   boolean offerLast(Double elem) {
	return list.offerLast(elem);
}

@Override
public   Double peekFirst() {
	return list.peekFirst();
}

@Override
public   Double peekLast() {
	return list.peekLast();
}

@Override
public   Double pollFirst() {
	return list.pollFirst();
}

@Override
public   Double pollLast() {
	return list.pollLast();
}

@Override
public   Double pop() {
	return list.pop();
}

@Override
public   void push(Double elem) {
	list.push(elem);
}

@Override
public   boolean removeFirstOccurrence(Object elem) {
	if (elem == null || elem.getClass() != Double.class) {
		return false;
	}
	return list.removeFirstOccurrence((Double) elem);
}

@Override
public   boolean removeLastOccurrence(Object elem) {
	if (elem == null || elem.getClass() != Double.class) {
		return false;
	}
	return list.removeLastOccurrence((Double) elem);
}

@Override
public   GapList<Double> get(int index, int len) {
	double[] elems = list.getArray(index, len);
	return GapList.create(toWrapper(elems));
}

@Override
public   Double[] getArray(int index, int len) {
	double[] elems = list.getArray(index, len);
	return toWrapper(elems);
}

@Override
public   void setAll(int index, GapList<? extends Double> list2) {
	double[] e = toPrimitive(list2);
	list.setAll(index, e);
}

@Override
public   void setAll(int index, Collection<? extends Double> coll) {
	double[] e = toPrimitive(coll);
	list.setAll(index, e);
}

@Override
public   void setAll(int index, Double... elems) {
	double[] e = toPrimitive(elems);
	list.setAll(index, e);
}

@Override
public   void remove(int index, int len) {
	list.remove(index, len);
}

@Override
public   void init(int len, Double elem) {
	list.init(len, elem);
}

@Override
public   void resize(int len, Double elem) {
	list.resize(len, elem);
}

@Override
public   void fill(Double elem) {
	list.fill(elem);
}

@Override
public   void fill(int index, int len, Double elem) {
	list.fill(index, len, elem);
}

@Override
public   void copy(int srcIndex, int dstIndex, int len) {
	 list.copy(srcIndex, dstIndex, len);
}

@Override
public   void move(int srcIndex, int dstIndex, int len) {
	list.move(srcIndex, dstIndex, len);
}

@Override
public   void reverse() {
	list.reverse();
}

@Override
public   void reverse(int index, int len) {
	list.reverse(index, len);
}

@Override
public   void swap(int index1, int index2, int len) {
	list.swap(index1, index2, len);
}

@Override
public   void rotate(int distance) {
	list.rotate(distance);
}

@Override
public   void rotate(int index, int len, int distance) {
	list.rotate(index, len, distance);
}

@Override
public   void sort(Comparator comparator) {
	if (comparator != null) {
		throw new IllegalArgumentException("Only natural comparator (null) allowed");
	}
	list.sort();
}

@Override
public   void sort(int index, int len, Comparator comparator) {
	if (comparator != null) {
		throw new IllegalArgumentException("Only natural comparator (null) allowed");
	}
	list.sort(index, len);
}

@Override
public  <K> int binarySearch(K key, Comparator<? super K> comparator) {
	if (key == null || key.getClass() != Double.class) {
		throw new IllegalArgumentException("Value is null or has invalid type");
	}
	if (comparator != null) {
		throw new IllegalArgumentException("Only natural comparator (null) allowed");
	}
	return list.binarySearch((Double) key);
}

@Override
public  <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
	if (key == null || key.getClass() != Double.class) {
		throw new IllegalArgumentException("Value is null or has invalid type");
	}
	if (comparator != null) {
		throw new IllegalArgumentException("Only natural comparator (null) allowed");
	}
	return list.binarySearch(index, len, (Double) key);
}


}
