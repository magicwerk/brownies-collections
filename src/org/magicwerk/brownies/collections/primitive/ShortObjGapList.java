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

	public ShortObjGapList() {
		super(false, null);
		list = new ShortGapList();
	}

	public ShortObjGapList(int capacity) {
		super(false, null);
		list = new ShortGapList(capacity);
	}

	public ShortObjGapList(Short... elems) {
		super(false, null);
		list = new ShortGapList(toPrimitive(elems));
	}

	public ShortObjGapList(Collection<? extends Short> elems) {
		super(false, null);
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
	public Short get(int index) {
		return list.get(index);
	}

	@Override
	public Short set(int index, Short elem) {
		return list.set(index, elem);
	}

	@Override
	public boolean add(Short elem) {
		return list.add(elem);
	}

	@Override
	public void add(int index, Short elem) {
		list.add(index, elem);
	}

	@Override
	public Short remove(int index) {
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
	public Short getFirst() {
		return list.getFirst();
	}

	@Override
	public Short getLast() {
		return list.getLast();
	}

	@Override
	public void addFirst(Short elem) {
		list.addFirst(elem);
	}

	@Override
	public void addLast(Short elem) {
		list.addLast(elem);
	}

	@Override
	public Short removeFirst() {
		return list.removeFirst();
	}

	@Override
	public Short removeLast() {
		return list.removeLast();
	}

	@Override
	public Short peek() {
		return list.peek();
	}

	@Override
	public Short element() {
		return list.element();
	}

	@Override
	public Short poll() {
		return list.poll();
	}

	@Override
	public Short remove() {
		return list.remove();
	}

	@Override
	public boolean offer(Short elem) {
		return list.offer(elem);
	}

	@Override
	public boolean offerFirst(Short elem) {
		return list.offerFirst(elem);
	}

	@Override
	public boolean offerLast(Short elem) {
		return list.offerLast(elem);
	}

	@Override
	public Short peekFirst() {
		return list.peekFirst();
	}

	@Override
	public Short peekLast() {
		return list.peekLast();
	}

	@Override
	public Short pollFirst() {
		return list.pollFirst();
	}

	@Override
	public Short pollLast() {
		return list.pollLast();
	}

	@Override
	public Short pop() {
		return list.pop();
	}

	@Override
	public void push(Short elem) {
		list.push(elem);
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
	public void remove(int index, int len) {
		list.remove(index, len);
	}

	@Override
	public void init(int len, Short elem) {
		list.init(len, elem);
	}

	@Override
	public void resize(int len, Short elem) {
		list.resize(len, elem);
	}

	@Override
	public void fill(Short elem) {
		list.fill(elem);
	}

	@Override
	public void fill(int index, int len, Short elem) {
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
		if (key == null || key.getClass() != Short.class) {
			throw new IllegalArgumentException("Value is null or has invalid type");
		}
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch((Short) key);
	}

	@Override
	public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
		if (key == null || key.getClass() != Short.class) {
			throw new IllegalArgumentException("Value is null or has invalid type");
		}
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch(index, len, (Short) key);
	}

}
