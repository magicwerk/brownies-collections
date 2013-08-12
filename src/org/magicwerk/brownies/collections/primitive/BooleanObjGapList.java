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

	public BooleanObjGapList() {
		super(false, null);
		list = new BooleanGapList();
	}

	public BooleanObjGapList(int capacity) {
		super(false, null);
		list = new BooleanGapList(capacity);
	}

	public BooleanObjGapList(Boolean... elems) {
		super(false, null);
		list = new BooleanGapList(toPrimitive(elems));
	}

	public BooleanObjGapList(Collection<? extends Boolean> elems) {
		super(false, null);
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
	public Boolean get(int index) {
		return list.get(index);
	}

	@Override
	public Boolean set(int index, Boolean elem) {
		return list.set(index, elem);
	}

	@Override
	public boolean add(Boolean elem) {
		return list.add(elem);
	}

	@Override
	public void add(int index, Boolean elem) {
		list.add(index, elem);
	}

	@Override
	public Boolean remove(int index) {
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
	public Boolean getFirst() {
		return list.getFirst();
	}

	@Override
	public Boolean getLast() {
		return list.getLast();
	}

	@Override
	public void addFirst(Boolean elem) {
		list.addFirst(elem);
	}

	@Override
	public void addLast(Boolean elem) {
		list.addLast(elem);
	}

	@Override
	public Boolean removeFirst() {
		return list.removeFirst();
	}

	@Override
	public Boolean removeLast() {
		return list.removeLast();
	}

	@Override
	public Boolean peek() {
		return list.peek();
	}

	@Override
	public Boolean element() {
		return list.element();
	}

	@Override
	public Boolean poll() {
		return list.poll();
	}

	@Override
	public Boolean remove() {
		return list.remove();
	}

	@Override
	public boolean offer(Boolean elem) {
		return list.offer(elem);
	}

	@Override
	public boolean offerFirst(Boolean elem) {
		return list.offerFirst(elem);
	}

	@Override
	public boolean offerLast(Boolean elem) {
		return list.offerLast(elem);
	}

	@Override
	public Boolean peekFirst() {
		return list.peekFirst();
	}

	@Override
	public Boolean peekLast() {
		return list.peekLast();
	}

	@Override
	public Boolean pollFirst() {
		return list.pollFirst();
	}

	@Override
	public Boolean pollLast() {
		return list.pollLast();
	}

	@Override
	public Boolean pop() {
		return list.pop();
	}

	@Override
	public void push(Boolean elem) {
		list.push(elem);
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
	public void remove(int index, int len) {
		list.remove(index, len);
	}

	@Override
	public void init(int len, Boolean elem) {
		list.init(len, elem);
	}

	@Override
	public void resize(int len, Boolean elem) {
		list.resize(len, elem);
	}

	@Override
	public void fill(Boolean elem) {
		list.fill(elem);
	}

	@Override
	public void fill(int index, int len, Boolean elem) {
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

	

}
