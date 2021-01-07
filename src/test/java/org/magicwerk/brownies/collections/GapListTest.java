package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.function.Predicate;

import org.magictest.client.Assert;
import org.magictest.client.Capture;
import org.magictest.client.Format;
import org.magictest.client.Formatter;
import org.magictest.client.Test;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.PrintTools;
import org.magicwerk.brownies.core.SerializeTools;
import org.magicwerk.brownies.core.strings.StringFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test of org.magicwerk.brownies.collections.GapList.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
@Trace(traceClass = "GapList")
public class GapListTest {

	static final Logger LOG = LoggerFactory.getLogger(GapListTest.class);

	public static final String RELEASE = "0.9.14";

	public static void main(String[] args) {
		test();
	}

	static void test() {
		//testGetAll();
		//testSplit();
		//testRemove();
		//testBug20151113();
		//testRetainAll();
		//testInitMult();
		//testPutAll();
	}

	static void testSplit() {
		GapList<Integer> gl = GapList.create(1, 2, 3, 4);
		List<Integer>[] split2 = split2(gl);
		System.out.println(split2[0] + " - " + split2[1]);
		gl.remove(1);
		System.out.println(split2[0] + " - " + split2[1]);

		//GapList<Integer> gl = GapList.create(1, 2, 3, 4);
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
		GapList<Integer> list = new GapList<Integer>(3);
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
		//Assert.assertTrue(list.equals(GapList.create(9, 9, 9, 9)));
	}

	static void test2() {
		GapList<Integer> list = GapList.create();
		list.addArray(new Integer[] { 0, 1, 2 });
		list.remove(0);

		GapList<Integer> add = GapList.create();
		for (int i = 0; i < 10; i++) {
			add.add(i);
		}

		System.out.println(list.capacity());

		list.addAll(add);
	}

	static int BIGLIST_SIZE = 4;

	static IList<Integer> create(Integer... args) {
		if (args.length == 0) {
			return GapList.create();
			//return IntObjGapList.create();
			//return new BigList(BIGLIST_SIZE);
		} else {
			return GapList.create(args);
			//return IntObjGapList.create(args);
			//IGapList list = new BigList(BIGLIST_SIZE);
			//list.addAll(args);
			//return list;
		}
	}

	static IList<Integer> getGapList(int size) {
		//GapList<Integer> list = new GapList<Integer>(size);
		//IntObjGapList list = new IntObjGapList(size);
		GapList<Integer> list = new GapList<Integer>();
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
	static IList<Integer> getSortedGapList(int size) {
		IList<Integer> list = getGapList(size);
		for (int i = 0; i < size; i++) {
			list.add(i);
		}
		return list;
	}

	static IList<Integer> getUnsortedGapList(int size) {
		IList<Integer> list = getGapList(size);
		for (int i = 0; i < size; i++) {
			list.add(i / 2 * 2 + (1 - i % 2));
		}
		return list;
	}

	static GapList<Name> getNameList() {
		return new GapList<Name>();
	}

	// -- Test GapList

	// Begin test buffer

	@Capture
	public static void testBuffer() {
		GapList<String> list1 = GapList.create("a", "b");
		doTestBuffer(list1);

		GapList<String> list2 = GapList.create("a", "b");
		list2.ensureCapacity(12);
		doTestBuffer(list2);

		GapList<String> list3 = GapList.create("a", "b", "c");
		list3.remove(0);
		doTestBuffer(list3);

		GapList<String> list4 = GapList.create("a", "b", "c");
		list4.remove(0);
		list4.ensureCapacity(12);
		doTestBuffer(list4);
	}

	static void doTestBuffer(GapList<String> list) {
		printBufferInfo(list);
		int index = list.size();
		int len = 2;
		Object[] buf = list.prepareAddBuffer(index, len);
		printBufferInfo(list);
		buf[index] = "x";
		int used = 1;
		list.releaseAddBuffer(index, used);
		printBufferInfo(list);
		StringFormatter.println("");
	}

	static void printBufferInfo(GapList<String> list) {
		StringFormatter.println("{} (size={}, capacity={}, normalized={})", list, list.size(), list.capacity(), list.isNormalized());
	}

	// End test buffer

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, formats = { @Format(apply = Trace.RESULT, formatter = "formatGapList") })
	public static void testCreate() {
		GapList.create();
		GapList.create("a", "b");
		GapList.create(Arrays.asList("a", "b", "c"));
	}

	@Trace(traceMethod = "GapList", parameters = Trace.THIS | Trace.ALL_PARAMS, formats = { @Format(apply = Trace.RESULT, formatter = "formatGapList") })
	public static void testNew() {
		new GapList();
		new GapList(5);
		new GapList(Arrays.asList("a", "b", "c"));
	}

	@Trace(traceMethod = "/.*/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testTrimToSize() {
		GapList<Integer> list = (GapList<Integer>) create();
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

	// -- Test IGapList

	@Capture
	public static void testBinarySearch() {
		GapList<Name> nameList = getNameList();
		add(nameList, new Name("b"));
		add(nameList, new Name("a"));
		add(nameList, new Name("c"));
		add(nameList, new Name("b"));
		System.out.println(nameList);

		System.out.println(get(nameList, "b"));
		System.out.println(get(nameList, "x"));
	}

	static boolean add(GapList<Name> nameList, Name nameObj) {
		int index = nameList.binarySearch(nameObj, nameComparator);
		if (index >= 0) {
			return false;
		}
		index = -index - 1;
		nameList.add(index, nameObj);
		return true;
	}

	static Name get(GapList<Name> nameList, String nameStr) {
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
			@Format(apply = Trace.THIS | Trace.RESULT, formatter = "formatGapList") })
	public static void testClone() {
		IList<Integer> list = getSortedGapList(7);
		list.ensureCapacity(20);
		list = (IList<Integer>) list.clone();
		list.add(99);
		list = (IList<Integer>) list.clone();
	}

	static String formatGapList(IList<?> list) {
		if (list instanceof GapList) {
			GapList<?> gapList = (GapList<?>) list;
			return String.format("Size: %d, Capacity: %d, Content: %s", list.size(), gapList.capacity(), list);
		} else {
			return String.format("Size: %d, Content: %s", list.size(), list);
		}
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testReverse() {
		getSortedGapList(7).reverse();
		getSortedGapList(7).reverse(1, 5);
		getSortedGapList(7).reverse(4, 0);

		// Errors
		getSortedGapList(7).reverse(-1, 2);
		getSortedGapList(7).reverse(1, -1);
		getSortedGapList(7).reverse(1, 7);
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

		// Non empty list
		list = getSortedGapList(5);
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
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testSort() {
		getUnsortedGapList(7).sort(null);
		getUnsortedGapList(7).sort(Collections.reverseOrder());

		getUnsortedGapList(7).sort(1, 5, null);
		getUnsortedGapList(7).sort(1, 5, Collections.reverseOrder());
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT, formats = { @Format(apply = Trace.PARAM0, printFormat = "i -> i%%2==0") })
	public static void testGetIf() {
		getSortedGapList(4).getIf(i -> i % 2 == 0);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT, formats = { @Format(apply = Trace.PARAM0, printFormat = "i -> i%%2==0") })
	public static void testFilteredList() {
		getSortedGapList(4).filteredList(i -> i % 2 == 0);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT, formats = {
			@Format(apply = Trace.PARAM0, printFormat = "i -> i%%2==0") })
	public static void testRetainIf() {
		getSortedGapList(4).retainIf(i -> i % 2 == 0);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT, formats = {
			@Format(apply = Trace.PARAM0, printFormat = "i -> i%%2==0") })
	public static void testRemoveIf() {
		getSortedGapList(4).removeIf(i -> i % 2 == 0);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT, formats = {
			@Format(apply = Trace.PARAM0, printFormat = "i -> i%%2==0") })
	public static void testExtractIf() {
		getSortedGapList(4).extractIf(i -> i % 2 == 0);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testExtract() {
		getSortedGapList(5).extract(1, 3);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testRotate() {
		getSortedGapList(4).rotate(1);

		getSortedGapList(4).rotate(0, 4, 0);
		getSortedGapList(4).rotate(0, 4, 1);
		getSortedGapList(4).rotate(0, 4, 2);
		getSortedGapList(4).rotate(0, 4, 3);
		getSortedGapList(4).rotate(0, 4, 4);
		getSortedGapList(4).rotate(0, 4, -1);
		getSortedGapList(4).rotate(0, 4, -2);
		getSortedGapList(4).rotate(0, 4, -3);
		getSortedGapList(4).rotate(0, 4, -4);

		getSortedGapList(7).rotate(0, 5, 3);
	}

	@Capture
	public static void testSerialization() {
		IList list1 = getSortedGapList(7);
		System.out.println(list1);
		byte[] data = SerializeTools.toBinary(list1);
		IList list2 = (IList) SerializeTools.fromBinary(data);
		assert (list1.equals(list2));
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testSwap() {
		getSortedGapList(7).swap(0, 4, 3);
		getSortedGapList(7).swap(4, 0, 3);

		// Errors
		getSortedGapList(7).swap(0, 4, 4);
		getSortedGapList(7).swap(4, 0, 4);
		getSortedGapList(7).swap(0, 2, 3);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.PARAM0 | Trace.PARAM2)
	public static void testTransferSwap() {
		IList<Integer> l1 = getSortedGapList(7);
		IList<Integer> l2 = getSortedGapList(7);
		for (int i = 0; i < l2.size(); i++) {
			l2.set(i, l2.get(i) + 10);
		}

		GapList.transferSwap(l1, 0, l2, 0, 3);

		// transferSwap on one list
		l1 = getSortedGapList(7);
		GapList.transferSwap(l1, 0, l1, 3, 3);
	}

	@Capture
	public static void testMappedList() {
		IList<Integer> l1 = getSortedGapList(7);
		IList<String> l2 = l1.mappedList(new Function<Integer, String>() {
			@Override
			public String apply(Integer v) {
				return "(" + v + ")";
			}
		});
		System.out.println(l2);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS)
	public static void testGetDistinct() {
		IList<Integer> l1 = getSortedGapList(7);
		l1.add(0);
		l1.add(6);
		l1.getDistinct();
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS)
	public static void testGetCount() {
		IList<Integer> l1 = getSortedGapList(7);
		l1.add(0);
		l1.add(6);
		l1.getCount(0);
		l1.getCount(1);
		l1.getCount(9);
	}

	@Capture
	public static void testFilter() {
		IList<Integer> l1 = getSortedGapList(7);
		l1.filter(new Predicate<Integer>() {
			@Override
			public boolean test(Integer elem) {
				return elem % 2 == 0;
			}
		});
		System.out.println(l1);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.PARAM0 | Trace.PARAM3)
	public static void testTransferMove() {
		IList<Integer> l1 = getSortedGapList(7);
		IList<Integer> l2 = getSortedGapList(7);
		for (int i = 0; i < l2.size(); i++) {
			l2.set(i, l2.get(i) + 10);
		}

		GapList.transferMove(l1, 0, 3, l2, 0, 3);

		// transferMove on one list
		l1 = getSortedGapList(7);
		GapList.transferMove(l1, 0, 3, l1, 3, 3);

		// Error (lenghts not equal)
		GapList.transferMove(l1, 0, 3, l1, 3, 2);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.PARAM0 | Trace.PARAM3)
	public static void testTransferRemove() {
		IList<Integer> l1 = getSortedGapList(7);
		IList<Integer> l2 = getSortedGapList(7);
		for (int i = 0; i < l2.size(); i++) {
			l2.set(i, l2.get(i) + 10);
		}

		GapList.transferRemove(l1, 0, 3, l2, 0, 3);

		// transferCopy on one list
		l1 = getSortedGapList(7);
		GapList.transferRemove(l1, 0, 3, l1, 3, 3);

		// Error (lenghts not equal)
		GapList.transferRemove(l1, 0, 3, l1, 3, 2);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.PARAM0 | Trace.PARAM3)
	public static void testTransferCopy() {
		IList<Integer> l1 = getSortedGapList(7);
		IList<Integer> l2 = getSortedGapList(7);
		for (int i = 0; i < l2.size(); i++) {
			l2.set(i, l2.get(i) + 10);
		}

		GapList.transferCopy(l1, 0, 3, l2, 0, 3);

		// transferCopy on one list
		l1 = getSortedGapList(7);
		GapList.transferCopy(l1, 0, 3, l1, 3, 3);

		// Error (lenghts not equal)
		GapList.transferCopy(l1, 0, 3, l1, 3, 2);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testClear() {
		getSortedGapList(0).clear();
		getSortedGapList(7).clear();
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testCopy() {
		getSortedGapList(7).copy(0, 4, 3);
		getSortedGapList(7).copy(0, 3, 4);
		getSortedGapList(7).copy(3, 0, 4);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testMove() {
		getSortedGapList(7).move(0, 4, 3);
		getSortedGapList(7).move(0, 3, 4);
		getSortedGapList(7).move(3, 0, 4);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testDrag() {
		for (int i = 0; i < 8; i++) {
			getSortedGapList(8).drag(3, i, 2);
		}
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testFill() {
		getSortedGapList(7).fill(9);
	}

	@Trace(traceMethod = "remove", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testRemoveRange() {
		getSortedGapList(7).remove(1, 5);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS)
	public static void testGetAll() {
		getSortedGapList(7).getAll(1, 5);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS)
	public static void testGet() {
		getSortedGapList(7).get(1);

		getSortedGapList(7).get(-1);
		getSortedGapList(7).get(7);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS)
	public static void testHashCode() {
		IList<Integer> list = getSortedGapList(7);
		list.hashCode();

		list.set(3, getDefaultElem());
		list.hashCode();
	}

	@Trace(traceMethod = "/initAll|initArray|initMult/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testInitAll() {
		getSortedGapList(5).initAll(Arrays.asList());
		getSortedGapList(5).initAll(Arrays.asList(1));
		getSortedGapList(5).initAll(Arrays.asList(1, 2));

		getSortedGapList(5).initArray();
		getSortedGapList(5).initArray(1);
		getSortedGapList(5).initArray(1, 2);

		getSortedGapList(5).initMult(4, 9);
		getSortedGapList(5).initMult(5, 9);
		getSortedGapList(5).initMult(6, 9);
		getSortedGapList(5).initMult(0, 9);

		// Error
		getSortedGapList(5).initAll(null);
		getSortedGapList(5).initMult(-1, 9);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testResize() {
		getSortedGapList(5).resize(4, 9);
		getSortedGapList(5).resize(5, 9);
		getSortedGapList(5).resize(6, 9);
		getSortedGapList(5).resize(0, 9);
		getSortedGapList(5).resize(-1, 9);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testRemoveAll() {
		getSortedGapList(7).removeAll(create(0, 2, 4, 6, 8));
		getSortedGapList(7).removeAll(create(8));

		// NullPointerException
		getSortedGapList(0).removeAll((List<?>) null);
		getSortedGapList(7).removeAll((List<?>) null);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testRetainAll() {
		getSortedGapList(7).retainAll(create(0, 2, 4, 6, 8));
		getSortedGapList(7).retainAll(create(0, 1, 2, 3, 4, 5, 6, 7, 8));

		// NullPointerException
		getSortedGapList(0).retainAll(null);
		getSortedGapList(7).retainAll(null);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testRemove() {
		getSortedGapList(7).remove(1);
		getSortedGapList(7).remove(1, 3);

		// IndexOutOfBoundsException
		getSortedGapList(7).remove(-1);
		getSortedGapList(7).remove(7);
		getSortedGapList(7).remove(5, 3);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testPut() {
		getSortedGapList(5).put(4, 8);
		getSortedGapList(5).put(5, 9);

		getSortedGapList(5).put(-1, 9);
		getSortedGapList(5).put(7, 9);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testSet() {
		getSortedGapList(7).set(1, 9);

		getSortedGapList(7).set(-1, 9);
		getSortedGapList(7).set(7, 9);
	}

	@Trace(traceMethod = "/setAll|setArray|setMult/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testSetAll() {
		Integer[] array = new Integer[] { -1, -2, -3 };
		IList<Integer> list = create(-1, -2, -3);
		Collection<Integer> coll = new ArrayList<Integer>(list);

		getSortedGapList(7).setAll(1, list);
		getSortedGapList(7).setAll(1, coll);

		getSortedGapList(7).setArray(1, array);
		getSortedGapList(7).setArray(1, -1, -2, -3);

		getSortedGapList(7).setMult(1, 5, 9);

		// Error
		getSortedGapList(7).setArray(5, -1, -2, -3);
		getSortedGapList(7).setAll(1, null);
	}

	@Trace(traceMethod = "/putAll|putArray|putMult/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testPutAll() {
		IList<Integer> list = create(8, 9);

		getSortedGapList(4).putAll(0, list);
		getSortedGapList(4).putAll(1, list);
		getSortedGapList(4).putAll(2, list);
		getSortedGapList(4).putAll(3, list);
		getSortedGapList(4).putAll(4, list);

		getSortedGapList(4).putArray(2, 6, 7, 8, 9);

		getSortedGapList(4).putMult(2, 4, 9);

		// Error
		getSortedGapList(4).putAll(5, list);
		getSortedGapList(4).putAll(0, null);
	}

	@Trace(traceMethod = "/replaceAll|replaceArray|replaceMult/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testReplaceAll() {
		IList<Integer> list = create(8, 9);

		getSortedGapList(4).replaceAll(1, 1, list);
		getSortedGapList(4).replaceAll(1, 2, list);
		getSortedGapList(4).replaceAll(1, 3, list);

		getSortedGapList(4).replaceArray(1, 1, 8, 9);
		getSortedGapList(4).replaceArray(1, 2, 8, 9);
		getSortedGapList(4).replaceArray(1, 3, 8, 9);

		getSortedGapList(4).replaceMult(1, 1, 2, 9);
		getSortedGapList(4).replaceMult(1, 2, 2, 9);
		getSortedGapList(4).replaceMult(1, 3, 2, 9);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testAdd() {
		getSortedGapList(7).add(0, 9);
		getSortedGapList(7).add(1, 9);
		getSortedGapList(7).add(7, 9);

		getSortedGapList(7).add(-1, 9);
		getSortedGapList(7).add(8, 9);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testAddIfAbsent() {
		getSortedGapList(5).addIfAbsent(2);
		getSortedGapList(5).addIfAbsent(9);
	}

	@Trace(traceMethod = "/addAll|addArray|addMult/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testAddAll() {
		Integer[] array = new Integer[] { -1, -2, -3 };
		IList<Integer> list = create(-1, -2, -3);
		Collection<Integer> coll = new ArrayList<Integer>(list);

		getSortedGapList(7).addAll(1, list);
		getSortedGapList(7).addAll(1, coll);

		getSortedGapList(7).addArray(1, array);

		getSortedGapList(7).addMult(1, 2, 9);

		// Error
		getSortedGapList(7).addAll(-1, list);
		getSortedGapList(7).addAll(8, list);
		getSortedGapList(7).addAll(-1, new ArrayList<Integer>());
		getSortedGapList(7).addAll(1, null);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testContainsIf() {
		getSortedGapList(7).containsIf(e -> e == 1);
		getSortedGapList(7).containsIf(e -> e == 8);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testContains() {
		getSortedGapList(7).contains(1);
		getSortedGapList(7).contains(8);
		getSortedGapList(7).contains(null);
		getSortedGapList(7).contains("abc");

		IList<Integer> list = getSortedGapList(7);
		Integer defElem = getDefaultElem();
		list.add(defElem);
		list.contains(defElem);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testIndexOf() {
		getSortedGapList(7).indexOf(1);
		getSortedGapList(7).indexOf(8);
		getSortedGapList(7).indexOf(null);
		getSortedGapList(7).indexOf("abc");

		IList<Integer> list = getSortedGapList(7);
		Integer defElem = getDefaultElem();
		list.add(defElem);
		list.indexOf(defElem);

		// Check how String handles indexOf / lastIndexOf
		String str = "abcde";
		Assert.assertTrue(str.indexOf("c", -1) == 2);
		Assert.assertTrue(str.indexOf("c", 2) == 2);
		Assert.assertTrue(str.indexOf("c", 10) == -1);

		getSortedGapList(7).indexOf(2, -1);
		getSortedGapList(7).indexOf(2, 2);
		getSortedGapList(7).indexOf(2, 10);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testLastIndexOf() {
		getSortedGapList(7).lastIndexOf(1);
		getSortedGapList(7).lastIndexOf(8);
		getSortedGapList(7).lastIndexOf(null);
		getSortedGapList(7).lastIndexOf("abc");

		// Check how String handles indexOf / lastIndexOf
		String str = "abcde";
		Assert.assertTrue(str.lastIndexOf("c", -1) == -1);
		Assert.assertTrue(str.lastIndexOf("c", 10) == 2);
		Assert.assertTrue(str.lastIndexOf("c", 2) == 2);

		getSortedGapList(7).lastIndexOf(2, -1);
		getSortedGapList(7).lastIndexOf(2, 2);
		getSortedGapList(7).lastIndexOf(2, 10);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testContainsAny() {
		getSortedGapList(7).containsAny(create(1, 2));
		getSortedGapList(7).containsAny(create(1, 8));
		getSortedGapList(7).containsAny(create(8, 1));
		getSortedGapList(7).containsAny(create(8, 8));
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testContainsAll() {
		getSortedGapList(7).containsAll(create(1, 2));
		getSortedGapList(7).containsAll(create(1, 8));
		getSortedGapList(7).containsAll(create(8, 1));
		getSortedGapList(7).containsAll(create(8, 8));
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testToArray() {
		Object[] oa = getSortedGapList(7).toArray();
		getSortedGapList(7).toArray(0, 7);
		getSortedGapList(7).toArray(0, 2);
		getSortedGapList(7).toArray(5, 2);
		getSortedGapList(7).toArray(2, 3);

		Integer[] ia = getSortedGapList(7).toArray(new Integer[] {});
	}

	@Trace(traceMethod = "/.+/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.RESULT)
	public static void testUnmodifiableList() {
		IList<Integer> list = getSortedGapList(7).unmodifiableList();
		list.get(0);

		list.set(0, 9);
		list.add(9);
		list.remove(0);
		list.ensureCapacity(10);
		list.clear();
	}

	@Capture
	public static void testIterator() {
		{
			// Iterate over all elements and show them
			IList<Integer> gapList = getSortedGapList(7);
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
			IList<Integer> gapList = getSortedGapList(7);
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
			IList<Integer> gapList = getSortedGapList(7);
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
			IList<Integer> gapList = getSortedGapList(7);
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
			IList<Integer> gapList = getSortedGapList(7);
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
			IList<Integer> gapList = getSortedGapList(7);
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
		List<Integer> list = getSortedGapList(0);
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
			IList<Integer> gapList = getSortedGapList(7);
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
			IList<Integer> gapList = getSortedGapList(7);
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
			IList<Integer> gapList = getSortedGapList(7);
			Iterator<Integer> iter = gapList.listIterator();
			while (iter.hasNext()) {
				Integer i = iter.next();
				iter.remove();
			}
			System.out.println(PrintTools.print(gapList));
		}
		{
			// Iterate over all elements starting at index 2
			IList<Integer> gapList = getSortedGapList(7);
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

}
