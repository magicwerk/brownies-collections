package org.magicwerk.brownies.collections;

import java.util.Arrays;

import org.magictest.client.Assert;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.primitive.IntObjGapList;
import org.magicwerk.brownies.core.strings.StringFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test of IntGapList.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
@Trace(traceClass="org.magicwerk.brownies.collections.primitive.IntObjGapList")
public class IntObjGapListTest {
	/** Logger */
	private static Logger LOG = LoggerFactory.getLogger(IntObjGapListTest.class);

	public static void main(String[] args) {
	    test();
	}

	static void test() {
		testToArray();
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

	@Trace(parameters=Trace.ALL_PARAMS|Trace.THIS)
	public static void testEquals() {
		IntObjGapList list1 = IntObjGapList.create(1, 2, 3);
		IntObjGapList list2 = IntObjGapList.create(1, 2, 3);

        list1.equals(list2);

        list2.set(0,  0);
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
