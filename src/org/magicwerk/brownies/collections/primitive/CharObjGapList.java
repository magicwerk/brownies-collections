package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.CharGapList;
import org.magicwerk.brownies.collections.GapList;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class CharObjGapList extends GapList<Character> {

	private CharGapList list;

	static Character[] toWrapper(char[] elems) {
		Character[] e = new Character[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static char[] toPrimitive(Character[] elems) {
		char[] e = new char[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static char[] toPrimitive(GapList<? extends Character> list2) {
		char[] e = new char[list2.size()];
		for (int i = 0; i < e.length; i++) {
			e[i] = list2.get(i);
		}
		return e;
	}

	static char[] toPrimitive(Collection<? extends Character> list) {
		char[] e = new char[list.size()];
		Iterator<? extends Character> iter = list.iterator();
		for (int i = 0; i < e.length; i++) {
			e[i] = iter.next();
		}
		return e;
	}

	public static CharObjGapList create() {
		return new CharObjGapList();
	}

	public static CharObjGapList create(int capacity) {
		return new CharObjGapList(capacity);
	}

	public static CharObjGapList create(Character... elems) {
		return new CharObjGapList(elems);
	}

	public static CharObjGapList create(Collection<? extends Character> elems) {
		return new CharObjGapList(elems);
	}

	public CharObjGapList() {
		super(false, null);
		list = new CharGapList();
	}

	public CharObjGapList(int capacity) {
		super(false, null);
		list = new CharGapList(capacity);
	}

	public CharObjGapList(Character... elems) {
		super(false, null);
		list = new CharGapList(toPrimitive(elems));
	}

	public CharObjGapList(Collection<? extends Character> elems) {
		super(false, null);
		list = new CharGapList(toPrimitive(elems));
	}

	@Override
	public Object clone() {
		CharObjGapList list = (CharObjGapList) super.clone();
		list.list = (CharGapList) list.list.clone();
		return list;
	}

	@Override
	public CharObjGapList copy() {
		return (CharObjGapList) clone();
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
	public Character get(int index) {
		return list.get(index);
	}

	@Override
	public Character set(int index, Character elem) {
		return list.set(index, elem);
	}

	@Override
	public boolean add(Character elem) {
		return list.add(elem);
	}

	@Override
	public void add(int index, Character elem) {
		list.add(index, elem);
	}

	@Override
	public Character remove(int index) {
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
		if (elem == null || elem.getClass() != Character.class) {
			return -1;
		}
		return list.indexOf((Character) elem);
	}

	@Override
	public int lastIndexOf(Object elem) {
		if (elem == null || elem.getClass() != Character.class) {
			return -1;
		}
		return list.lastIndexOf((Character) elem);
	}

	@Override
	public boolean remove(Object elem) {
		if (elem == null || elem.getClass() != Character.class) {
			return false;
		}
		int index = list.indexOf((Character) elem);
		if (index == -1) {
			return false;
		}
		list.remove(index);
		return true;
	}

	@Override
	public boolean contains(Object elem) {
		if (elem == null || elem.getClass() != Character.class) {
			return false;
		}
		return list.contains((Character) elem);
	}

	@Override
	public boolean containsAny(Collection<?> coll) {
		return list.containsAny((Collection<Character>) coll);
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
		return list.containsAll((Collection<Character>) coll);
	}

	@Override
	public boolean removeAll(Collection<?> coll) {
		return list.removeAll((Collection<Character>) coll);
	}

	@Override
	public boolean removeAll(GapList<?> coll) {
		return list.removeAll((Collection<Character>) coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll((Collection<Character>) coll);
	}

	@Override
	public boolean retainAll(GapList<?> coll) {
		return list.retainAll((Collection<Character>) coll);
	}

	@Override
	public Object[] toArray() {
		char[] elems = list.toArray();
		return toWrapper(elems);
	}

	@Override
	public Object[] toArray(int index, int len) {
		char[] elems = list.toArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public <T> T[] toArray(T[] array) {
		int size = list.size();
		if (array.length < size) {
			array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
		}
		for (int i = 0; i < size; i++) {
			array[i] = (T) (Character) list.get(i);
		}
		if (array.length > size) {
			array[size] = null;
		}
		return array;
	}

	@Override
	public boolean addAll(Collection<? extends Character> coll) {
		return list.addAll((Collection<Character>) coll);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Character> coll) {
		return list.addAll(index, (Collection<Character>) coll);
	}

	@Override
	public boolean addAll(Character... elems) {
		char[] e = toPrimitive(elems);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, Character... elems) {
		char[] e = toPrimitive(elems);
		return list.addAll(index, e);
	}

	@Override
	public boolean addAll(GapList<? extends Character> list2) {
		char[] e = toPrimitive(list2);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, GapList<? extends Character> list2) {
		char[] e = toPrimitive(list2);
		return list.addAll(index, e);
	}

	@Override
	public Character getFirst() {
		return list.getFirst();
	}

	@Override
	public Character getLast() {
		return list.getLast();
	}

	@Override
	public void addFirst(Character elem) {
		list.addFirst(elem);
	}

	@Override
	public void addLast(Character elem) {
		list.addLast(elem);
	}

	@Override
	public Character removeFirst() {
		return list.removeFirst();
	}

	@Override
	public Character removeLast() {
		return list.removeLast();
	}

	@Override
	public Character peek() {
		return list.peek();
	}

	@Override
	public Character element() {
		return list.element();
	}

	@Override
	public Character poll() {
		return list.poll();
	}

	@Override
	public Character remove() {
		return list.remove();
	}

	@Override
	public boolean offer(Character elem) {
		return list.offer(elem);
	}

	@Override
	public boolean offerFirst(Character elem) {
		return list.offerFirst(elem);
	}

	@Override
	public boolean offerLast(Character elem) {
		return list.offerLast(elem);
	}

	@Override
	public Character peekFirst() {
		return list.peekFirst();
	}

	@Override
	public Character peekLast() {
		return list.peekLast();
	}

	@Override
	public Character pollFirst() {
		return list.pollFirst();
	}

	@Override
	public Character pollLast() {
		return list.pollLast();
	}

	@Override
	public Character pop() {
		return list.pop();
	}

	@Override
	public void push(Character elem) {
		list.push(elem);
	}

	@Override
	public boolean removeFirstOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Character.class) {
			return false;
		}
		return list.removeFirstOccurrence((Character) elem);
	}

	@Override
	public boolean removeLastOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Character.class) {
			return false;
		}
		return list.removeLastOccurrence((Character) elem);
	}

	@Override
	public GapList<Character> get(int index, int len) {
		char[] elems = list.getArray(index, len);
		return GapList.create(toWrapper(elems));
	}

	@Override
	public Character[] getArray(int index, int len) {
		char[] elems = list.getArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public void setAll(int index, GapList<? extends Character> list2) {
		char[] e = toPrimitive(list2);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Collection<? extends Character> coll) {
		char[] e = toPrimitive(coll);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Character... elems) {
		char[] e = toPrimitive(elems);
		list.setAll(index, e);
	}

	@Override
	public void remove(int index, int len) {
		list.remove(index, len);
	}

	@Override
	public void init(int len, Character elem) {
		list.init(len, elem);
	}

	@Override
	public void resize(int len, Character elem) {
		list.resize(len, elem);
	}

	@Override
	public void fill(Character elem) {
		list.fill(elem);
	}

	@Override
	public void fill(int index, int len, Character elem) {
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
		if (key == null || key.getClass() != Character.class) {
			throw new IllegalArgumentException("Value is null or has invalid type");
		}
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch((Character) key);
	}

	@Override
	public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
		if (key == null || key.getClass() != Character.class) {
			throw new IllegalArgumentException("Value is null or has invalid type");
		}
		if (comparator != null) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch(index, len, (Character) key);
	}

}
