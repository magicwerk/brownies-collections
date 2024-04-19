// ---
// --- DO NOT EDIT
// --- AUTOMATICALLY GENERATED FILE
// ---

package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.magictest.client.Assert;
import org.magictest.client.Capture;
import org.magictest.client.Format;
import org.magictest.client.Formatter;
import org.magictest.client.Test;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.helper.GapLists;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.ObjectTools;
import org.magicwerk.brownies.core.RunTools;
import org.magicwerk.brownies.core.function.Predicates;
import org.magicwerk.brownies.core.function.Predicates.NamedPredicate;
import org.magicwerk.brownies.core.print.PrintTools;
import org.magicwerk.brownies.core.serialize.SerializeTools;
import org.magicwerk.brownies.core.strings.StringFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test of class {@link BigList}.
 *
 * @author Thomas Mauch
 */
@Trace(traceClass = "BigList")
public class BigListGapListTest {

	static final Logger LOG = LoggerFactory.getLogger(BigListGapListTest.class);

	public static void main(String[] args) {
		test();
	}

	static void test() {
		//testPeekPoll();
		//testExtractIf();
		//testRemoveIf();
		//testStream();
		//testParallelStream();
		//testRetain();
		//testGetAll();
		//testSplit();
		//testRemove();
		//testBug20151113();
		//testRetainAll();
		//testInitMult();
		//testPutAll();
		//testReplaceAll();
		testClone();
	}

	static void testSplit() {
		BigList<Integer> gl = BigList.create(1, 2, 3, 4);
		List<Integer>[] split2 = split2(gl);
		System.out.println(split2[0] + " - " + split2[1]);
		gl.remove(1);
		System.out.println(split2[0] + " - " + split2[1]);

		//BigList<Integer> gl = BigList.create(1, 2, 3, 4);
		List<Integer>[] split = split1(gl);
		System.out.println(split[0] + " - " + split[1]);
		gl.remove(1);
		System.out.println(split[0] + " - " + split[1]);

		ArrayList<Integer> al = new ArrayList<>();
		al.addAll(Arrays.asList(1, 2, 3, 4));
		List<Integer>[] split1 = split1(al);
		System.out.println(split1[0] + " - " + split1[1]);
		al.remove(1);
		System.out.println(split1[0] + " - " + split1[1]);
	}

	static <T> List<T>[] split1(List<T> list) {
		int size = list.size();
		int mid = size / 2;
		List<T> leftList = list.subList(0, mid);
		List<T> rightList = list.subList(mid, size);
		return (List<T>[]) new List<?>[] { leftList, rightList };
	}

	static <T> IList<T>[] split2(IList<T> list) {
		int size = list.size();
		int mid = size / 2;
		IList<T> leftList = list.getAll(0, mid);
		IList<T> rightList = list.getAll(mid, size - mid);
		return (IList<T>[]) new IList<?>[] { leftList, rightList };
	}

	@Test
	public static void testBug20151113() {
		BigList<Integer> list = new BigList<Integer>(3);
		list.add(1);
		list.add(2);
		list.remove(0);
		list.remove(0);
		//		int state0 = list.debugState();
		list.ensureCapacity(10);
		//		int state1 = list.debugState();
		list.addMult(4, 9);
		//list.add(9);
		//		System.out.println(state0 + " - " + state1);
		System.out.println(list);
		//Assert.assertTrue(list.equals(BigList.create(9, 9, 9, 9)));
	}

	static void test2() {
		BigList<Integer> list = BigList.create();
		list.addArray(new Integer[] { 0, 1, 2 });
		list.remove(0);

		BigList<Integer> add = BigList.create();
		for (int i = 0; i < 10; i++) {
			add.add(i);
		}

		System.out.println(list.capacity());

		list.addAll(add);
	}

	static int BIGLIST_SIZE = 4;

	static IList<Integer> create(Integer... args) {
		if (args.length == 0) {
			return BigList.create();
			//return IntObjBigList.create();
			//return new BigList(BIGLIST_SIZE);
		} else {
			return BigList.create(args);
			//return IntObjBigList.create(args);
			//IBigList list = new BigList(BIGLIST_SIZE);
			//list.addAll(args);
			//return list;
		}
	}

	static IList<Integer> getBigList(int size) {
		//BigList<Integer> list = new BigList<Integer>(size);
		//IntObjBigList list = new IntObjBigList(size);
		BigList<Integer> list = new BigList<Integer>();
		list.ensureCapacity(size);
		return list;
	}

	static Integer getDefaultElem() {
		return null;
		//return 0;
	}

	/**
	 * Returns list with specified size containing elements 0, 1, 2, ...
	 *
	 * @param size number of elements in list
	 * @return     created list
	 */
	static IList<Integer> getSortedBigList(int size) {
		IList<Integer> list = getBigList(size);
		for (int i = 0; i < size; i++) {
			list.add(i);
		}
		return list;
	}

	static IList<Integer> getUnsortedBigList(int size) {
		IList<Integer> list = getBigList(size);
		for (int i = 0; i < size; i++) {
			list.add(i / 2 * 2 + (1 - i % 2));
		}
		return list;
	}

	static BigList<Name> getNameList() {
		return new BigList<Name>();
	}

	// -- Test BigList

	// 

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, formats = { @Format(apply = Trace.RESULT, formatter = "formatBigList") })
	public static void testCreate() {
		BigList.create();
		BigList.create("a", "b");
		BigList.create(Arrays.asList("a", "b", "c"));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Trace(traceMethod = "BigList", parameters = Trace.THIS | Trace.ALL_PARAMS, formats = { @Format(apply = Trace.RESULT, formatter = "formatBigList") })
	public static void testNew() {
		new BigList();
		new BigList(5);
		new BigList(Arrays.asList("a", "b", "c"));
	}

	@Trace(traceMethod = "/.*/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testTrimToSize() {
		BigList<Integer> list = (BigList<Integer>) create();
		list.capacity();
		list.trimToSize();
		list.capacity();
		list.add(1);
		list.capacity();
		list.add(1);
		list.capacity();
		list.add(1);
		list.capacity();
		list.trimToSize();
		list.capacity();
	}

	// -- Test IList

	@Capture
	public static void testBinarySearch() {
		BigList<Name> nameList = getNameList();
		add(nameList, new Name("b"));
		add(nameList, new Name("a"));
		add(nameList, new Name("c"));
		add(nameList, new Name("b"));
		System.out.println(nameList);

		System.out.println(get(nameList, "b"));
		System.out.println(get(nameList, "x"));
	}

	static boolean add(BigList<Name> nameList, Name nameObj) {
		int index = nameList.binarySearch(nameObj, nameComparator);
		if (index >= 0) {
			return false;
		}
		index = -index - 1;
		nameList.add(index, nameObj);
		return true;
	}

	static Name get(BigList<Name> nameList, String nameStr) {
		int index = nameList.binarySearch(nameStr, nameComparator2);
		if (index >= 0) {
			return nameList.get(index);
		}
		return null;
	}

	static class Name {
		String name;

		public Name(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	static NameComparator nameComparator = new NameComparator();

	static class NameComparator implements Comparator<Name> {
		@Override
		public int compare(Name n1, Name n2) {
			return n1.getName().compareTo(n2.getName());
		}
	}

	static NameComparator2 nameComparator2 = new NameComparator2();

	static class NameComparator2 implements Comparator<Object> {
		@Override
		public int compare(Object o1, Object o2) {
			String s1 = (o1 instanceof String) ? (String) o1 : ((Name) o1).getName();
			String s2 = (o2 instanceof String) ? (String) o2 : ((Name) o2).getName();
			return s1.compareTo(s2);
		}
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT, formats = {
			@Format(apply = Trace.THIS | Trace.RESULT, formatter = "formatBigList") })
	public static void testClone() {
		IList<Integer> list = getSortedBigList(7);
		list.ensureCapacity(20);
		list = list.clone();
		list.add(99);
		list = list.clone();

		IList<Integer> ul = list.unmodifiableList();
		RunTools.runThrowing(() -> ul.set(0, 0));
		IList<Integer> ul2 = ul.clone();
		RunTools.runThrowing(() -> ul2.set(0, 0));
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT, formats = {
			@Format(apply = Trace.THIS | Trace.RESULT, formatter = "formatBigList") })
	public static void testCopy() {
		IList<Integer> list = getSortedBigList(7);
		list.ensureCapacity(20);
		list = list.copy();
		list.add(99);
		list = list.copy();

		IList<Integer> ul = list.unmodifiableList();
		RunTools.runThrowing(() -> ul.set(0, 0));
		IList<Integer> ul2 = ul.copy();
		ul2.set(0, 0);
	}

	static String formatBigList(IList<?> list) {
		if (list instanceof BigList) {
			BigList<?> gapList = (BigList<?>) list;
			return String.format("Size: %d, Capacity: %d, Content: %s", list.size(), gapList.capacity(), list);
		} else {
			return String.format("Size: %d, Content: %s", list.size(), list);
		}
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testReverse() {
		getSortedBigList(7).reverse();
		getSortedBigList(7).reverse(1, 5);
		getSortedBigList(7).reverse(4, 0);

		// Errors
		getSortedBigList(7).reverse(-1, 2);
		getSortedBigList(7).reverse(1, -1);
		getSortedBigList(7).reverse(1, 7);
	}

	@Formatter
	public static String format(Comparator<?> comparator) {
		if (comparator == null) {
			return "null";
		} else {
			return comparator.getClass().getSimpleName();
		}
	}

	@Trace(traceMethod = "/.*/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testPeekPoll() {
		// Empty list
		IList<Integer> list = create();
		list.peek();
		list.peekFirst();
		list.peekLast();
		list.poll();
		list.pollFirst();
		list.pollLast();
		list.getFirst();
		list.getLast();
		list.removeFirst();
		list.removeLast();
		list.element();
		list.pop();
		list.push(1);
		list.addFirst(0);
		list.addLast(2);
		list.offerFirst(1);
		list.offerLast(3);
		list.offer(4);
		list.removeFirstOccurrence(1);
		list.removeLastOccurrence(2);

		// Non empty list
		list = getSortedBigList(5);
		list.peek();
		list.peekFirst();
		list.peekLast();
		list.poll();
		list.pollFirst();
		list.pollLast();
		list.getFirst();
		list.getLast();
		list.removeFirst();
		list.removeLast();
		list.element();
		list.pop();
		list.push(1);
		list.addFirst(0);
		list.addLast(2);
		list.offerFirst(1);
		list.offerLast(3);
		list.offer(4);
		list.removeFirstOccurrence(1);
		list.removeLastOccurrence(2);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testSort() {
		getUnsortedBigList(7).sort(null);
		getUnsortedBigList(7).sort(Collections.reverseOrder());

		getUnsortedBigList(7).sort(1, 5, null);
		getUnsortedBigList(7).sort(1, 5, Collections.reverseOrder());
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT, formats = { @Format(apply = Trace.PARAM0, printFormat = "i -> i%%2==0") })
	public static void testGetIf() {
		getSortedBigList(4).getIf(i -> i % 2 == 0);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testFilter() {
		getSortedBigList(4).filter(new NamedPredicate<>("i%2==0", i -> i % 2 == 0));
		getSortedBigList(4).filter(new NamedPredicate<>("i%2==1", i -> i % 2 == 1));
		getSortedBigList(4).filter(Predicates.allow());
		getSortedBigList(4).filter(Predicates.deny());
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testRetainIf() {
		getSortedBigList(4).retainIf(new NamedPredicate<>("i%2==0", i -> i % 2 == 0));
		getSortedBigList(4).retainIf(new NamedPredicate<>("i%2==1", i -> i % 2 == 1));
		getSortedBigList(4).retainIf(Predicates.allow());
		getSortedBigList(4).retainIf(Predicates.deny());
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testRemoveIf() {
		getSortedBigList(4).removeIf(new NamedPredicate<>("i%2==0", i -> i % 2 == 0));
		getSortedBigList(4).removeIf(new NamedPredicate<>("i%2==1", i -> i % 2 == 1));
		getSortedBigList(4).removeIf(Predicates.allow());
		getSortedBigList(4).removeIf(Predicates.deny());
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testExtractIf() {
		getSortedBigList(4).extractIf(new NamedPredicate<>("i%2==0", i -> i % 2 == 0));
		getSortedBigList(4).extractIf(new NamedPredicate<>("i%2==1", i -> i % 2 == 1));
		getSortedBigList(4).extractIf(Predicates.allow());
		getSortedBigList(4).extractIf(Predicates.deny());
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testExtract() {
		getSortedBigList(5).extract(1, 3);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testRotate() {
		getSortedBigList(4).rotate(1);

		getSortedBigList(4).rotate(0, 4, 0);
		getSortedBigList(4).rotate(0, 4, 1);
		getSortedBigList(4).rotate(0, 4, 2);
		getSortedBigList(4).rotate(0, 4, 3);
		getSortedBigList(4).rotate(0, 4, 4);
		getSortedBigList(4).rotate(0, 4, -1);
		getSortedBigList(4).rotate(0, 4, -2);
		getSortedBigList(4).rotate(0, 4, -3);
		getSortedBigList(4).rotate(0, 4, -4);

		getSortedBigList(7).rotate(0, 5, 3);
	}

	@SuppressWarnings("rawtypes")
	@Capture
	public static void testSerialization() {
		IList list1 = getSortedBigList(7);
		System.out.println(list1);
		byte[] data = SerializeTools.toBinary(list1);
		IList list2 = (IList) SerializeTools.fromBinary(data);
		assert (list1.equals(list2));
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testSwap() {
		getSortedBigList(7).swap(0, 4, 3);
		getSortedBigList(7).swap(4, 0, 3);

		// Errors
		getSortedBigList(7).swap(0, 4, 4);
		getSortedBigList(7).swap(4, 0, 4);
		getSortedBigList(7).swap(0, 2, 3);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.PARAM0 | Trace.PARAM2)
	public static void testTransferSwap() {
		IList<Integer> l1 = getSortedBigList(7);
		IList<Integer> l2 = getSortedBigList(7);
		for (int i = 0; i < l2.size(); i++) {
			l2.set(i, l2.get(i) + 10);
		}

		BigList.transferSwap(l1, 0, l2, 0, 3);

		// transferSwap on one list
		l1 = getSortedBigList(7);
		BigList.transferSwap(l1, 0, l1, 3, 3);
	}

	@Capture
	public static void testMappedList() {
		IList<Integer> l1 = getSortedBigList(7);
		IList<String> l2 = l1.map(i -> "(" + i + ")");
		System.out.println(l2);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS)
	public static void testGetDistinct() {
		IList<Integer> l1 = getSortedBigList(7);
		l1.add(0);
		l1.add(6);
		l1.getDistinct();
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS)
	public static void testCount() {
		IList<Integer> l1 = getSortedBigList(7);
		l1.add(0);
		l1.add(6);
		l1.count(0);
		l1.count(1);
		l1.count(9);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS)
	public static void testCountIf() {
		IList<Integer> l1 = getSortedBigList(7);
		l1.countIf(i -> i == 1);
		l1.countIf(i -> i % 2 == 0);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.PARAM0 | Trace.PARAM3)
	public static void testTransferMove() {
		IList<Integer> l1 = getSortedBigList(7);
		IList<Integer> l2 = getSortedBigList(7);
		for (int i = 0; i < l2.size(); i++) {
			l2.set(i, l2.get(i) + 10);
		}

		BigList.transferMove(l1, 0, 3, l2, 0, 3);

		// transferMove on one list
		l1 = getSortedBigList(7);
		BigList.transferMove(l1, 0, 3, l1, 3, 3);

		// Error (lenghts not equal)
		BigList.transferMove(l1, 0, 3, l1, 3, 2);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.PARAM0 | Trace.PARAM3)
	public static void testTransferRemove() {
		IList<Integer> l1 = getSortedBigList(7);
		IList<Integer> l2 = getSortedBigList(7);
		for (int i = 0; i < l2.size(); i++) {
			l2.set(i, l2.get(i) + 10);
		}

		BigList.transferRemove(l1, 0, 3, l2, 0, 3);

		// transferCopy on one list
		l1 = getSortedBigList(7);
		BigList.transferRemove(l1, 0, 3, l1, 3, 3);

		// Error (lenghts not equal)
		BigList.transferRemove(l1, 0, 3, l1, 3, 2);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.PARAM0 | Trace.PARAM3)
	public static void testTransferCopy() {
		IList<Integer> l1 = getSortedBigList(7);
		IList<Integer> l2 = getSortedBigList(7);
		for (int i = 0; i < l2.size(); i++) {
			l2.set(i, l2.get(i) + 10);
		}

		BigList.transferCopy(l1, 0, 3, l2, 0, 3);

		// transferCopy on one list
		l1 = getSortedBigList(7);
		BigList.transferCopy(l1, 0, 3, l1, 3, 3);

		// Error (lenghts not equal)
		BigList.transferCopy(l1, 0, 3, l1, 3, 2);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testClear() {
		getSortedBigList(0).clear();
		getSortedBigList(7).clear();
	}

	@Trace(traceMethod = "copy", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testCopy2() {
		getSortedBigList(7).copy(0, 4, 3);
		getSortedBigList(7).copy(0, 3, 4);
		getSortedBigList(7).copy(3, 0, 4);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testMove() {
		getSortedBigList(7).move(0, 4, 3);
		getSortedBigList(7).move(0, 3, 4);
		getSortedBigList(7).move(3, 0, 4);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testDrag() {
		for (int i = 0; i < 8; i++) {
			getSortedBigList(8).drag(3, i, 2);
		}
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testFill() {
		getSortedBigList(7).fill(9);
	}

	@Trace(traceMethod = "remove", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testRemoveRange() {
		getSortedBigList(7).remove(1, 5);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testRetain() {
		getSortedBigList(7).retain(1, 3);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS)
	public static void testGetAll() {
		getSortedBigList(7).getAll(1, 5);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS)
	public static void testGet() {
		getSortedBigList(7).get(1);

		getSortedBigList(7).get(-1);
		getSortedBigList(7).get(7);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS)
	public static void testHashCode() {
		IList<Integer> list = getSortedBigList(7);
		list.hashCode();

		list.set(3, getDefaultElem());
		list.hashCode();
	}

	@Trace(traceMethod = "/initAll|initArray|initMult/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testInitAll() {
		getSortedBigList(5).initAll(Arrays.asList());
		getSortedBigList(5).initAll(Arrays.asList(1));
		getSortedBigList(5).initAll(Arrays.asList(1, 2));

		getSortedBigList(5).initArray();
		getSortedBigList(5).initArray(1);
		getSortedBigList(5).initArray(1, 2);

		getSortedBigList(5).initMult(4, 9);
		getSortedBigList(5).initMult(5, 9);
		getSortedBigList(5).initMult(6, 9);
		getSortedBigList(5).initMult(0, 9);

		// Error
		getSortedBigList(5).initAll(null);
		getSortedBigList(5).initMult(-1, 9);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testResize() {
		getSortedBigList(5).resize(4, 9);
		getSortedBigList(5).resize(5, 9);
		getSortedBigList(5).resize(6, 9);
		getSortedBigList(5).resize(0, 9);
		getSortedBigList(5).resize(-1, 9);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testRemoveAll() {
		getSortedBigList(7).removeAll(create(0, 2, 4, 6, 8));
		getSortedBigList(7).removeAll(create(8));

		// NullPointerException
		getSortedBigList(0).removeAll((List<?>) null);
		getSortedBigList(7).removeAll((List<?>) null);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testRetainAll() {
		getSortedBigList(7).retainAll(create(0, 2, 4, 6, 8));
		getSortedBigList(7).retainAll(create(0, 1, 2, 3, 4, 5, 6, 7, 8));

		// NullPointerException
		getSortedBigList(0).retainAll(null);
		getSortedBigList(7).retainAll(null);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testRemove() {
		getSortedBigList(7).remove(1);
		getSortedBigList(7).remove(1, 3);

		// IndexOutOfBoundsException
		getSortedBigList(7).remove(-1);
		getSortedBigList(7).remove(7);
		getSortedBigList(7).remove(5, 3);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testPut() {
		getSortedBigList(5).put(4, 8);
		getSortedBigList(5).put(5, 9);

		getSortedBigList(5).put(-1, 9);
		getSortedBigList(5).put(7, 9);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testSet() {
		getSortedBigList(7).set(1, 9);

		getSortedBigList(7).set(-1, 9);
		getSortedBigList(7).set(7, 9);
	}

	@Trace(traceMethod = "/setAll|setArray|setMult/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testSetAll() {
		Integer[] array = new Integer[] { -1, -2, -3 };
		IList<Integer> list = create(-1, -2, -3);
		Collection<Integer> coll = new ArrayList<Integer>(list);

		getSortedBigList(7).setAll(1, list);
		getSortedBigList(7).setAll(1, coll);

		getSortedBigList(7).setArray(1, array);
		getSortedBigList(7).setArray(1, -1, -2, -3);

		getSortedBigList(7).setMult(1, 5, 9);

		// Error
		getSortedBigList(7).setArray(5, -1, -2, -3);
		getSortedBigList(7).setAll(1, null);
	}

	@Trace(traceMethod = "/putAll|putArray|putMult/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testPutAll() {
		IList<Integer> list = create(8, 9);

		getSortedBigList(4).putAll(0, list);
		getSortedBigList(4).putAll(1, list);
		getSortedBigList(4).putAll(2, list);
		getSortedBigList(4).putAll(3, list);
		getSortedBigList(4).putAll(4, list);

		getSortedBigList(4).putArray(2, 6, 7, 8, 9);

		getSortedBigList(4).putMult(2, 4, 9);

		// Error
		getSortedBigList(4).putAll(5, list);
		getSortedBigList(4).putAll(0, null);
	}

	@Trace(traceMethod = "/replaceAll|replaceArray|replaceMult/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testReplaceAll() {
		IList<Integer> list = create(8, 9);

		getSortedBigList(4).replaceAll(1, 1, list);
		getSortedBigList(4).replaceAll(1, 2, list);
		getSortedBigList(4).replaceAll(1, 3, list);

		getSortedBigList(4).replaceArray(1, 1, 8, 9);
		getSortedBigList(4).replaceArray(1, 2, 8, 9);
		getSortedBigList(4).replaceArray(1, 3, 8, 9);

		getSortedBigList(4).replaceMult(1, 1, 2, 9);
		getSortedBigList(4).replaceMult(1, 2, 2, 9);
		getSortedBigList(4).replaceMult(1, 3, 2, 9);

		checkEquivalence(getSortedBigList(4), l -> l.replaceAll(-1, 0, list), l -> l.addAll(list));
		checkEquivalence(getSortedBigList(4), l -> l.replaceAll(2, 0, list), l -> l.addAll(2, list));
	}

	static void checkEquivalence(IList<Integer> list, Consumer<IList<Integer>> c1, Consumer<IList<Integer>> c2) {
		IList<Integer> l1 = list.copy();
		c1.accept(l1);

		IList<Integer> l2 = list.copy();
		c2.accept(l2);

		CheckTools.check(ObjectTools.equals(l1, l2));
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testAdd() {
		getSortedBigList(7).add(0, 9);
		getSortedBigList(7).add(1, 9);
		getSortedBigList(7).add(7, 9);

		getSortedBigList(7).add(-1, 9);
		getSortedBigList(7).add(8, 9);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testAddIfAbsent() {
		getSortedBigList(5).addIfAbsent(2);
		getSortedBigList(5).addIfAbsent(9);
	}

	@Trace(traceMethod = "/addAll|addArray|addMult/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testAddAll() {
		Integer[] array = new Integer[] { -1, -2, -3 };
		IList<Integer> list = create(-1, -2, -3);
		Collection<Integer> coll = new ArrayList<Integer>(list);

		getSortedBigList(7).addAll(1, list);
		getSortedBigList(7).addAll(1, coll);

		getSortedBigList(7).addArray(1, array);

		getSortedBigList(7).addMult(1, 2, 9);

		// Error
		getSortedBigList(7).addAll(-1, list);
		getSortedBigList(7).addAll(8, list);
		getSortedBigList(7).addAll(-1, new ArrayList<Integer>());
		getSortedBigList(7).addAll(1, null);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testContainsIf() {
		getSortedBigList(7).containsIf(e -> e == 1);
		getSortedBigList(7).containsIf(e -> e == 8);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testContains() {
		getSortedBigList(7).contains(1);
		getSortedBigList(7).contains(8);
		getSortedBigList(7).contains(null);
		getSortedBigList(7).contains("abc");

		IList<Integer> list = getSortedBigList(7);
		Integer defElem = getDefaultElem();
		list.add(defElem);
		list.contains(defElem);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testIndexOf() {
		getSortedBigList(7).indexOf(1);
		getSortedBigList(7).indexOf(8);
		getSortedBigList(7).indexOf(null);
		getSortedBigList(7).indexOf("abc");

		IList<Integer> list = getSortedBigList(7);
		Integer defElem = getDefaultElem();
		list.add(defElem);
		list.indexOf(defElem);

		// Check how String handles indexOf / lastIndexOf
		String str = "abcde";
		Assert.assertTrue(str.indexOf("c", -1) == 2);
		Assert.assertTrue(str.indexOf("c", 2) == 2);
		Assert.assertTrue(str.indexOf("c", 10) == -1);

		getSortedBigList(7).indexOf(2, -1);
		getSortedBigList(7).indexOf(2, 2);
		getSortedBigList(7).indexOf(2, 10);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testIndexOfIf() {
		getSortedBigList(7).indexOfIf(n -> n == 3);
		getSortedBigList(7).indexOfIf(n -> n == 3, 1);
		getSortedBigList(7).indexOfIf(n -> n == 3, 5);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testLastIndexOf() {
		getSortedBigList(7).lastIndexOf(1);
		getSortedBigList(7).lastIndexOf(8);
		getSortedBigList(7).lastIndexOf(null);
		getSortedBigList(7).lastIndexOf("abc");

		// Check how String handles indexOf / lastIndexOf
		String str = "abcde";
		Assert.assertTrue(str.lastIndexOf("c", -1) == -1);
		Assert.assertTrue(str.lastIndexOf("c", 10) == 2);
		Assert.assertTrue(str.lastIndexOf("c", 2) == 2);

		getSortedBigList(7).lastIndexOf(2, -1);
		getSortedBigList(7).lastIndexOf(2, 2);
		getSortedBigList(7).lastIndexOf(2, 10);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testLastIndexOfIf() {
		getSortedBigList(7).lastIndexOfIf(n -> n == 3);
		getSortedBigList(7).lastIndexOfIf(n -> n == 3, 5);
		getSortedBigList(7).lastIndexOfIf(n -> n == 3, 1);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testContainsAny() {
		getSortedBigList(7).containsAny(create(1, 2));
		getSortedBigList(7).containsAny(create(1, 8));
		getSortedBigList(7).containsAny(create(8, 1));
		getSortedBigList(7).containsAny(create(8, 8));
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testContainsAll() {
		getSortedBigList(7).containsAll(create(1, 2));
		getSortedBigList(7).containsAll(create(1, 8));
		getSortedBigList(7).containsAll(create(8, 1));
		getSortedBigList(7).containsAll(create(8, 8));
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testToArray() {
		Object[] oa = getSortedBigList(7).toArray();
		getSortedBigList(7).toArray(0, 7);
		getSortedBigList(7).toArray(0, 2);
		getSortedBigList(7).toArray(5, 2);
		getSortedBigList(7).toArray(2, 3);

		Integer[] ia = getSortedBigList(7).toArray(new Integer[] {});
	}

	@Trace(traceMethod = "/.+/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testUnmodifiableList() {
		IList<Integer> list = getSortedBigList(7).unmodifiableList();
		list.get(0);

		list.set(0, 9);
		list.add(9);
		list.remove(0);
		list.ensureCapacity(10);
		list.clear();

		// A unmodifiable list is returned unchanged by unmodifiableList()
		IList<Integer> list2 = list.unmodifiableList();
		Assert.assertTrue(list2 == list);

		// If an unmodifiable list is copied, it can be changed
		IList<Integer> list3 = list.copy();
		list3.add(1);
	}

	@Trace(traceMethod = "/.+/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testAsDeque() {
		IList<Integer> list = getSortedBigList(7);
		Deque<Integer> deque = list.asDeque();
		CheckTools.check(deque.getFirst() == list.getFirst());
		CheckTools.check(deque.getLast() == list.getLast());
	}

	// Iterator

	@Capture
	public static void testIterator() {
		{
			// Iterate over all elements and show them
			IList<Integer> gapList = getSortedBigList(7);
			Iterator<Integer> iter = gapList.iterator();
			List<Integer> list = new ArrayList<Integer>();
			while (iter.hasNext()) {
				Integer i = iter.next();
				list.add(i);
			}
			System.out.println(PrintTools.print(gapList) + " (" + PrintTools.print(list) + ")");
		}
		{
			// Iterate over all elements and remove every second
			IList<Integer> gapList = getSortedBigList(7);
			Iterator<Integer> iter = gapList.iterator();
			while (iter.hasNext()) {
				Integer i = iter.next();
				if (i % 2 == 0) {
					iter.remove();
				}
			}
			System.out.println(PrintTools.print(gapList));
		}
		{
			// Iterate over all elements and remove all
			IList<Integer> gapList = getSortedBigList(7);
			Iterator<Integer> iter = gapList.iterator();
			while (iter.hasNext()) {
				Integer i = iter.next();
				iter.remove();
			}
			System.out.println(PrintTools.print(gapList));
		}
	}

	@Capture
	public static void testDescendingIterator() {
		{
			// Iterate over all elements and show them
			IList<Integer> gapList = getSortedBigList(7);
			Iterator<Integer> iter = gapList.descendingIterator();
			List<Integer> list = new ArrayList<Integer>();
			while (iter.hasNext()) {
				Integer i = iter.next();
				list.add(i);
			}
			System.out.println(PrintTools.print(gapList) + " (" + PrintTools.print(list) + ")");
		}
		{
			// Iterate over all elements and remove every second
			IList<Integer> gapList = getSortedBigList(7);
			Iterator<Integer> iter = gapList.descendingIterator();
			while (iter.hasNext()) {
				Integer i = iter.next();
				if (i % 2 == 0) {
					iter.remove();
				}
			}
			System.out.println(PrintTools.print(gapList));
		}
		{
			// Iterate over all elements and remove all
			IList<Integer> gapList = getSortedBigList(7);
			Iterator<Integer> iter = gapList.descendingIterator();
			while (iter.hasNext()) {
				Integer i = iter.next();
				iter.remove();
			}
			System.out.println(PrintTools.print(gapList));
		}
	}

	@Test
	public static void testListIterator2() {
		List<Integer> list = getSortedBigList(0);
		// ArrayList also throws an exception
		//list = new ArrayList<Integer>(list);
		ListIterator<Integer> iter = list.listIterator();
		iter.add(1);
		iter.previous();
		iter.add(2);
		try {
			iter.remove();
			Assert.fail();
		} catch (IllegalStateException e) {
		}
	}

	@Capture
	public static void testListIterator() {
		{
			// Iterate over all elements and show them
			IList<Integer> gapList = getSortedBigList(7);
			Iterator<Integer> iter = gapList.listIterator();
			List<Integer> list = new ArrayList<Integer>();
			while (iter.hasNext()) {
				Integer i = iter.next();
				list.add(i);
			}
			System.out.println(PrintTools.print(gapList) + " (" + PrintTools.print(list) + ")");
		}
		{
			// Iterate over all elements and remove every second
			IList<Integer> gapList = getSortedBigList(7);
			Iterator<Integer> iter = gapList.listIterator();
			while (iter.hasNext()) {
				Integer i = iter.next();
				if (i % 2 == 0) {
					iter.remove();
				}
			}
			System.out.println(PrintTools.print(gapList));
		}
		{
			// Iterate over all elements and remove all
			IList<Integer> gapList = getSortedBigList(7);
			Iterator<Integer> iter = gapList.listIterator();
			while (iter.hasNext()) {
				Integer i = iter.next();
				iter.remove();
			}
			System.out.println(PrintTools.print(gapList));
		}
		{
			// Iterate over all elements starting at index 2
			IList<Integer> gapList = getSortedBigList(7);
			ListIterator<Integer> iter = gapList.listIterator(2);
			List<Integer> list = new ArrayList<Integer>();
			while (iter.hasNext()) {
				Integer i = iter.next();
				list.add(i);
			}
			while (iter.hasPrevious()) {
				Integer i = iter.previous();
				list.add(i);
			}
			System.out.println(PrintTools.print(gapList) + " (" + PrintTools.print(list) + ")");
		}
	}

	// Stream

	@Capture
	public static void testStream() {
		IList<Integer> l = getSortedBigList(10_000);

		List<Integer> list = l.stream().collect(Collectors.toList());
		BigList<Integer> list2 = l.stream().collect(GapLists.toBigList());

		CheckTools.check(list.equals(l));
		CheckTools.check(list2.equals(l));
	}

	@Capture
	public static void testParallelStream() {
		IList<Integer> l = getSortedBigList(10_000);

		List<Integer> list = l.parallelStream().collect(Collectors.toList());
		BigList<Integer> list2 = l.parallelStream().collect(GapLists.toBigList());

		CheckTools.check(list.equals(l));
		CheckTools.check(list2.equals(l));
	}

}
