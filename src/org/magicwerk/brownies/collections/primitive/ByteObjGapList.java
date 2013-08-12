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

	public ByteObjGapList() {
		super(false, null);
		list = new ByteGapList();
	}

	public ByteObjGapList(int capacity) {
		super(false, null);
		list = new ByteGapList(capacity);
	}

	public ByteObjGapList(Byte... elems) {
		super(false, null);
		list = new ByteGapList(toPrimitive(elems));
	}

	public ByteObjGapList(Collection<? extends Byte> elems) {
		super(false, null);
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
	public Byte get(int index) {
		return list.get(index);
	}

	@Override
	public Byte set(int index, Byte elem) {
		return list.set(index, elem);
	}

	@Override
	public boolean add(Byte elem) {
		return list.add(elem);
	}

	@Override
	public void add(int index, Byte elem) {
		list.add(index, elem);
	}

	@Override
	public Byte remove(int index) {
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
	public Byte getFirst() {
		return list.getFirst();
	}

	@Override
	public Byte getLast() {
		return list.getLast();
	}

	@Override
	public void addFirst(Byte elem) {
		list.addFirst(elem);
	}

	@Override
	public void addLast(Byte elem) {
		list.addLast(elem);
	}

	@Override
	public Byte removeFirst() {
		return list.removeFirst();
	}

	@Override
	public Byte removeLast() {
		return list.removeLast();
	}

	@Override
	public Byte peek() {
		return list.peek();
	}

	@Override
	public Byte element() {
		return list.element();
	}

	@Override
	public Byte poll() {
		return list.poll();
	}

	@Override
	public Byte remove() {
		return list.remove();
	}

	@Override
	public boolean offer(Byte elem) {
		return list.offer(elem);
	}

	@Override
	public boolean offerFirst(Byte elem) {
		return list.offerFirst(elem);
	}

	@Override
	public boolean offerLast(Byte elem) {
		return list.offerLast(elem);
	}

	@Override
	public Byte peekFirst() {
		return list.peekFirst();
	}

	@Override
	public Byte peekLast() {
		return list.peekLast();
	}

	@Override
	public Byte pollFirst() {
		return list.pollFirst();
	}

	@Override
	public Byte pollLast() {
		return list.pollLast();
	}

	@Override
	public Byte pop() {
		return list.pop();
	}

	@Override
	public void push(Byte elem) {
		list.push(elem);
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
	public void remove(int index, int len) {
		list.remove(index, len);
	}

	@Override
	public void init(int len, Byte elem) {
		list.init(len, elem);
	}

	@Override
	public void resize(int len, Byte elem) {
		list.resize(len, elem);
	}

	@Override
	public void fill(Byte elem) {
		list.fill(elem);
	}

	@Override
	public void fill(int index, int len, Byte elem) {
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
		if (key == null || key.getClass() != Byte.class) {
			throw new IllegalArgumentException("Value is null or has invalid type");
		}
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch((Byte) key);
	}

	@Override
	public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
		if (key == null || key.getClass() != Byte.class) {
			throw new IllegalArgumentException("Value is null or has invalid type");
		}
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch(index, len, (Byte) key);
	}

}
