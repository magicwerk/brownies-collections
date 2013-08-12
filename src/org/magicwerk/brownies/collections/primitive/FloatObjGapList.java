package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.FloatGapList;
import org.magicwerk.brownies.collections.GapList;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class FloatObjGapList extends GapList<Float> {

	private FloatGapList list;

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
		list = new FloatGapList();
	}

	public FloatObjGapList(int capacity) {
		super(false, null);
		list = new FloatGapList(capacity);
	}

	public FloatObjGapList(Float... elems) {
		super(false, null);
		list = new FloatGapList(toPrimitive(elems));
	}

	public FloatObjGapList(Collection<? extends Float> elems) {
		super(false, null);
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
	public void clear() {
		list.clear();
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
	public Float set(int index, Float elem) {
		return list.set(index, elem);
	}

	@Override
	public boolean add(Float elem) {
		return list.add(elem);
	}

	@Override
	public void add(int index, Float elem) {
		list.add(index, elem);
	}

	@Override
	public Float remove(int index) {
		return list.remove(index);
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
	public Float getFirst() {
		return list.getFirst();
	}

	@Override
	public Float getLast() {
		return list.getLast();
	}

	@Override
	public void addFirst(Float elem) {
		list.addFirst(elem);
	}

	@Override
	public void addLast(Float elem) {
		list.addLast(elem);
	}

	@Override
	public Float removeFirst() {
		return list.removeFirst();
	}

	@Override
	public Float removeLast() {
		return list.removeLast();
	}

	@Override
	public Float peek() {
		return list.peek();
	}

	@Override
	public Float element() {
		return list.element();
	}

	@Override
	public Float poll() {
		return list.poll();
	}

	@Override
	public Float remove() {
		return list.remove();
	}

	@Override
	public boolean offer(Float elem) {
		return list.offer(elem);
	}

	@Override
	public boolean offerFirst(Float elem) {
		return list.offerFirst(elem);
	}

	@Override
	public boolean offerLast(Float elem) {
		return list.offerLast(elem);
	}

	@Override
	public Float peekFirst() {
		return list.peekFirst();
	}

	@Override
	public Float peekLast() {
		return list.peekLast();
	}

	@Override
	public Float pollFirst() {
		return list.pollFirst();
	}

	@Override
	public Float pollLast() {
		return list.pollLast();
	}

	@Override
	public Float pop() {
		return list.pop();
	}

	@Override
	public void push(Float elem) {
		list.push(elem);
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
	public GapList<Float> get(int index, int len) {
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
	public void remove(int index, int len) {
		list.remove(index, len);
	}

	@Override
	public void init(int len, Float elem) {
		list.init(len, elem);
	}

	@Override
	public void resize(int len, Float elem) {
		list.resize(len, elem);
	}

	@Override
	public void fill(Float elem) {
		list.fill(elem);
	}

	@Override
	public void fill(int index, int len, Float elem) {
		list.fill(index, len, elem);
	}

	@Override
	public void copy(int srcIndex, int dstIndex, int len) {
		list.copy(srcIndex, dstIndex, len);
	}

	@Override
	public void move(int srcIndex, int dstIndex, int len) {
		list.move(srcIndex, dstIndex, len);
	}

	@Override
	public void reverse() {
		list.reverse();
	}

	@Override
	public void reverse(int index, int len) {
		list.reverse(index, len);
	}

	@Override
	public void swap(int index1, int index2, int len) {
		list.swap(index1, index2, len);
	}

	@Override
	public void rotate(int distance) {
		list.rotate(distance);
	}

	@Override
	public void rotate(int index, int len, int distance) {
		list.rotate(index, len, distance);
	}

	@Override
	public void sort(Comparator comparator) {
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		list.sort();
	}

	@Override
	public void sort(int index, int len, Comparator comparator) {
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		list.sort(index, len);
	}

	@Override
	public <K> int binarySearch(K key, Comparator<? super K> comparator) {
		if (key == null || key.getClass() != Float.class) {
			throw new IllegalArgumentException("Value is null or has invalid type");
		}
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch((Float) key);
	}

	@Override
	public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
		if (key == null || key.getClass() != Float.class) {
			throw new IllegalArgumentException("Value is null or has invalid type");
		}
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch(index, len, (Float) key);
	}

}
