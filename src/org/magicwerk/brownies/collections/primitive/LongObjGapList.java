package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.LongGapList;
import org.magicwerk.brownies.collections.GapList;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class LongObjGapList extends GapList<Long> {

	private LongGapList list;

	static Long[] toWrapper(long[] elems) {
		Long[] e = new Long[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static long[] toPrimitive(Long[] elems) {
		long[] e = new long[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static long[] toPrimitive(GapList<? extends Long> list2) {
		long[] e = new long[list2.size()];
		for (int i = 0; i < e.length; i++) {
			e[i] = list2.get(i);
		}
		return e;
	}

	static long[] toPrimitive(Collection<? extends Long> list) {
		long[] e = new long[list.size()];
		Iterator<? extends Long> iter = list.iterator();
		for (int i = 0; i < e.length; i++) {
			e[i] = iter.next();
		}
		return e;
	}

	public static LongObjGapList create() {
		return new LongObjGapList();
	}

	public static LongObjGapList create(int capacity) {
		return new LongObjGapList(capacity);
	}

	public static LongObjGapList create(Long... elems) {
		return new LongObjGapList(elems);
	}

	public static LongObjGapList create(Collection<? extends Long> elems) {
		return new LongObjGapList(elems);
	}

	public LongObjGapList() {
		super(false, null);
		list = new LongGapList();
	}

	public LongObjGapList(int capacity) {
		super(false, null);
		list = new LongGapList(capacity);
	}

	public LongObjGapList(Long... elems) {
		super(false, null);
		list = new LongGapList(toPrimitive(elems));
	}

	public LongObjGapList(Collection<? extends Long> elems) {
		super(false, null);
		list = new LongGapList(toPrimitive(elems));
	}

	@Override
	public Object clone() {
		LongObjGapList list = (LongObjGapList) super.clone();
		list.list = (LongGapList) list.list.clone();
		return list;
	}

	@Override
	public LongObjGapList copy() {
		return (LongObjGapList) clone();
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
	public Long get(int index) {
		return list.get(index);
	}

	@Override
	public Long set(int index, Long elem) {
		return list.set(index, elem);
	}

	@Override
	public boolean add(Long elem) {
		return list.add(elem);
	}

	@Override
	public void add(int index, Long elem) {
		list.add(index, elem);
	}

	@Override
	public Long remove(int index) {
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
		if (elem == null || elem.getClass() != Long.class) {
			return -1;
		}
		return list.indexOf((Long) elem);
	}

	@Override
	public int lastIndexOf(Object elem) {
		if (elem == null || elem.getClass() != Long.class) {
			return -1;
		}
		return list.lastIndexOf((Long) elem);
	}

	@Override
	public boolean remove(Object elem) {
		if (elem == null || elem.getClass() != Long.class) {
			return false;
		}
		int index = list.indexOf((Long) elem);
		if (index == -1) {
			return false;
		}
		list.remove(index);
		return true;
	}

	@Override
	public boolean contains(Object elem) {
		if (elem == null || elem.getClass() != Long.class) {
			return false;
		}
		return list.contains((Long) elem);
	}

	@Override
	public boolean containsAny(Collection<?> coll) {
		return list.containsAny((Collection<Long>) coll);
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
		return list.containsAll((Collection<Long>) coll);
	}

	@Override
	public boolean removeAll(Collection<?> coll) {
		return list.removeAll((Collection<Long>) coll);
	}

	@Override
	public boolean removeAll(GapList<?> coll) {
		return list.removeAll((Collection<Long>) coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll((Collection<Long>) coll);
	}

	@Override
	public boolean retainAll(GapList<?> coll) {
		return list.retainAll((Collection<Long>) coll);
	}

	@Override
	public Object[] toArray() {
		long[] elems = list.toArray();
		return toWrapper(elems);
	}

	@Override
	public Object[] toArray(int index, int len) {
		long[] elems = list.toArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public <T> T[] toArray(T[] array) {
		int size = list.size();
		if (array.length < size) {
			array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
		}
		for (int i = 0; i < size; i++) {
			array[i] = (T) (Long) list.get(i);
		}
		if (array.length > size) {
			array[size] = null;
		}
		return array;
	}

	@Override
	public boolean addAll(Collection<? extends Long> coll) {
		return list.addAll((Collection<Long>) coll);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Long> coll) {
		return list.addAll(index, (Collection<Long>) coll);
	}

	@Override
	public boolean addAll(Long... elems) {
		long[] e = toPrimitive(elems);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, Long... elems) {
		long[] e = toPrimitive(elems);
		return list.addAll(index, e);
	}

	@Override
	public boolean addAll(GapList<? extends Long> list2) {
		long[] e = toPrimitive(list2);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, GapList<? extends Long> list2) {
		long[] e = toPrimitive(list2);
		return list.addAll(index, e);
	}

	@Override
	public Long getFirst() {
		return list.getFirst();
	}

	@Override
	public Long getLast() {
		return list.getLast();
	}

	@Override
	public void addFirst(Long elem) {
		list.addFirst(elem);
	}

	@Override
	public void addLast(Long elem) {
		list.addLast(elem);
	}

	@Override
	public Long removeFirst() {
		return list.removeFirst();
	}

	@Override
	public Long removeLast() {
		return list.removeLast();
	}

	@Override
	public Long peek() {
		return list.peek();
	}

	@Override
	public Long element() {
		return list.element();
	}

	@Override
	public Long poll() {
		return list.poll();
	}

	@Override
	public Long remove() {
		return list.remove();
	}

	@Override
	public boolean offer(Long elem) {
		return list.offer(elem);
	}

	@Override
	public boolean offerFirst(Long elem) {
		return list.offerFirst(elem);
	}

	@Override
	public boolean offerLast(Long elem) {
		return list.offerLast(elem);
	}

	@Override
	public Long peekFirst() {
		return list.peekFirst();
	}

	@Override
	public Long peekLast() {
		return list.peekLast();
	}

	@Override
	public Long pollFirst() {
		return list.pollFirst();
	}

	@Override
	public Long pollLast() {
		return list.pollLast();
	}

	@Override
	public Long pop() {
		return list.pop();
	}

	@Override
	public void push(Long elem) {
		list.push(elem);
	}

	@Override
	public boolean removeFirstOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Long.class) {
			return false;
		}
		return list.removeFirstOccurrence((Long) elem);
	}

	@Override
	public boolean removeLastOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Long.class) {
			return false;
		}
		return list.removeLastOccurrence((Long) elem);
	}

	@Override
	public GapList<Long> get(int index, int len) {
		long[] elems = list.getArray(index, len);
		return GapList.create(toWrapper(elems));
	}

	@Override
	public Long[] getArray(int index, int len) {
		long[] elems = list.getArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public void setAll(int index, GapList<? extends Long> list2) {
		long[] e = toPrimitive(list2);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Collection<? extends Long> coll) {
		long[] e = toPrimitive(coll);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Long... elems) {
		long[] e = toPrimitive(elems);
		list.setAll(index, e);
	}

	@Override
	public void remove(int index, int len) {
		list.remove(index, len);
	}

	@Override
	public void init(int len, Long elem) {
		list.init(len, elem);
	}

	@Override
	public void resize(int len, Long elem) {
		list.resize(len, elem);
	}

	@Override
	public void fill(Long elem) {
		list.fill(elem);
	}

	@Override
	public void fill(int index, int len, Long elem) {
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
		if (key == null || key.getClass() != Long.class) {
			throw new IllegalArgumentException("Value is null or has invalid type");
		}
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch((Long) key);
	}

	@Override
	public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
		if (key == null || key.getClass() != Long.class) {
			throw new IllegalArgumentException("Value is null or has invalid type");
		}
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch(index, len, (Long) key);
	}

}
