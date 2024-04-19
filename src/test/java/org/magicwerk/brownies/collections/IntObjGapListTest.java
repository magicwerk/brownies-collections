package org.magicwerk.brownies.collections;

import java.util.Arrays;

import org.magictest.client.Assert;
import org.magictest.client.Format;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.primitive.IntObjGapList;
import org.magicwerk.brownies.core.RunTools;
import org.magicwerk.brownies.core.strings.StringFormatter;

/**
 * Test of IntObjGapList.
 *
 * @author Thomas Mauch
 */
@Trace(traceClass = "org.magicwerk.brownies.collections.primitive.IntObjGapList")
public class IntObjGapListTest {

	public static void main(String[] args) {
		test();
	}

	static void test() {
		testToArray();
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT, formats = {
			@Format(apply = Trace.THIS | Trace.RESULT, formatter = "formatIntObjGapList") })
	public static void testClone() {
		IntObjGapList list = IntObjGapList.create(1, 2, 3);
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
			@Format(apply = Trace.THIS | Trace.RESULT, formatter = "formatIntObjGapList") })
	public static void testCopy() {
		IntObjGapList list = IntObjGapList.create(1, 2, 3);
		list.ensureCapacity(20);
		list = list.copy();
		list.add(99);
		list = list.copy();

		IList<Integer> ul = list.unmodifiableList();
		RunTools.runThrowing(() -> ul.set(0, 0));
		IList<Integer> ul2 = ul.copy();
		ul2.set(0, 0);
	}

	static String formatIntObjGapList(IntObjGapList list) {
		return String.format("Size: %d, Capacity: %d, Content: %s", list.size(), list.capacity(), list);
	}

	@Trace
	public static void testAdd() {
		IntObjGapList list1 = IntObjGapList.create();
		IList<Integer> list = list1;
		list.add(1);
		list.add(null);
	}

	public static void testToArray() {
		IntObjGapList list = IntObjGapList.create(1, 2, 3);
		Object[] ints = list.toArray();
		StringFormatter.println(ints);

		ints = list.toArray(1, 1);
		StringFormatter.println(ints);
	}

	@Trace(parameters = Trace.ALL_PARAMS | Trace.THIS)
	public static void testEquals() {
		IntObjGapList list1 = IntObjGapList.create(1, 2, 3);
		IntObjGapList list2 = IntObjGapList.create(1, 2, 3);

		list1.equals(list2);

		list2.set(0, 0);
		list1.equals(list2);
	}

	@Trace
	public static void testCreate() {
		IntObjGapList list;
		list = IntObjGapList.create();
		Assert.assertTrue(list.getClass() == IntObjGapList.class);
		list = IntObjGapList.create(5);
		Assert.assertTrue(list.getClass() == IntObjGapList.class);
		list = IntObjGapList.create(1, 2);
		Assert.assertTrue(list.getClass() == IntObjGapList.class);
		list = IntObjGapList.create(Arrays.asList(1, 2, 3));
		Assert.assertTrue(list.getClass() == IntObjGapList.class);
	}

}
