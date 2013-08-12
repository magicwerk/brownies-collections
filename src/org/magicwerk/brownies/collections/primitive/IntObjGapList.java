package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.magicwerk.brownies.collections.GapList;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class IntObjGapList extends GapList<Integer> {

	private IntGapList list;

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

	public IntObjGapList() {
		super(false, null);
		list = new IntGapList();
	}

	public IntObjGapList(int capacity) {
		super(false, null);
		list = new IntGapList(capacity);
	}

	public IntObjGapList(Integer... elems) {
		super(false, null);
		list = new IntGapList(toPrimitive(elems));
	}

	public IntObjGapList(Collection<? extends Integer> elems) {
		super(false, null);
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
	public Integer get(int index) {
		return list.get(index);
	}

	@Override
	public Integer set(int index, Integer elem) {
		return list.set(index, elem);
	}

	@Override
	public boolean add(Integer elem) {
		return list.add(elem);
	}

	@Override
	public void add(int index, Integer elem) {
		list.add(index, elem);
	}

	@Override
	public Integer remove(int index) {
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
	public Integer getFirst() {
		return list.getFirst();
	}

	@Override
	public Integer getLast() {
		return list.getLast();
	}

	@Override
	public void addFirst(Integer elem) {
		list.addFirst(elem);
	}

	@Override
	public void addLast(Integer elem) {
		list.addLast(elem);
	}

	@Override
	public Integer removeFirst() {
		return list.removeFirst();
	}

	@Override
	public Integer removeLast() {
		return list.removeLast();
	}

	@Override
	public Integer peek() {
		return list.peek();
	}

	@Override
	public Integer element() {
		return list.element();
	}

	@Override
	public Integer poll() {
		return list.poll();
	}

	@Override
	public Integer remove() {
		return list.remove();
	}

	@Override
	public boolean offer(Integer elem) {
		return list.offer(elem);
	}

	@Override
	public boolean offerFirst(Integer elem) {
		return list.offerFirst(elem);
	}

	@Override
	public boolean offerLast(Integer elem) {
		return list.offerLast(elem);
	}

	@Override
	public Integer peekFirst() {
		return list.peekFirst();
	}

	@Override
	public Integer peekLast() {
		return list.peekLast();
	}

	@Override
	public Integer pollFirst() {
		return list.pollFirst();
	}

	@Override
	public Integer pollLast() {
		return list.pollLast();
	}

	@Override
	public Integer pop() {
		return list.pop();
	}

	@Override
	public void push(Integer elem) {
		list.push(elem);
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
	public void remove(int index, int len) {
		list.remove(index, len);
	}

	@Override
	public void init(int len, Integer elem) {
		list.init(len, elem);
	}

	@Override
	public void resize(int len, Integer elem) {
		list.resize(len, elem);
	}

	@Override
	public void fill(Integer elem) {
		list.fill(elem);
	}

	@Override
	public void fill(int index, int len, Integer elem) {
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
		if (key == null || key.getClass() != Integer.class) {
			throw new IllegalArgumentException("Value is null or has invalid type");
		}
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch((Integer) key);
	}

	@Override
	public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
		if (key == null || key.getClass() != Integer.class) {
			throw new IllegalArgumentException("Value is null or has invalid type");
		}
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch(index, len, (Integer) key);
	}

}
