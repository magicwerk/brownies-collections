package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.magictest.client.Assert;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test of IntGapList.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
@Trace(traceClass="org.magicwerk.brownies.collections.primitive.IntGapList")
public class IntGapListTest {

	static final Logger LOG = LoggerFactory.getLogger(IntGapListTest.class);

	public static void main(String[] args) {
	    test();
	}

	static void test() {
	    testAddAll();
	}

	public static void testAddAll() {
        List<Integer> add1 = new ArrayList<Integer>();
        add1.add(6);
        add1.add(7);

        IntGapList add2 = new IntGapList();
        add2.add(8);
        add2.add(9);

        IntGapList list = new IntGapList();
	    list.add(1);
	    // ambiguous
	    //list.addArray(2, 3);
	    list.addArray(new int[] { 4, 5 });
	    list.addAll(add1);
	    list.addAll(list.size(), add2);
	}

	@Trace(parameters=Trace.ALL_PARAMS|Trace.THIS)
	public static void testEquals() {
        IntGapList list1 = IntGapList.create(1, 2, 3);
        IntGapList list2 = IntGapList.create(1, 2, 3);

        list1.equals(list2);

        list2.set(0,  0);
        list1.equals(list2);
	}

    @Trace
    public static void testCreate() {
    	IntGapList list;
    	list = IntGapList.create();
    	Assert.assertTrue(list.getClass() == IntGapList.class);
    	list = IntGapList.create(5);
    	Assert.assertTrue(list.getClass() == IntGapList.class);
    	list = IntGapList.create(1, 2);
    	Assert.assertTrue(list.getClass() == IntGapList.class);
    	list = IntGapList.create(Arrays.asList(1, 2, 3));
    	Assert.assertTrue(list.getClass() == IntGapList.class);
    }
    
    @Trace
    public static void testToArray() {
    	IntGapList list;
    	list = IntGapList.create();
    	int[] array = list.toArray();
    	
    	list = new IntGapList(2);
    	array = list.toArray();
    	
        list = IntGapList.create(1, 2, 3);
    	array = list.toArray();
    }

}
