package org.magicwerk.brownies.collections;

import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.helper.primitive.IntBinarySearch;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test of BinarySearch.
 *
 * @author Thomas Mauch
 * @version $Id: BigListTest.java 2107 2014-01-24 08:08:38Z origo $
 */
@Trace(traceClass="org.magicwerk.brownies.collections.helper.primitive.IntBinarySearch")
public class BinarySearchTest {
	/** Logger */
	private static Logger LOG = LoggerFactory.getLogger(BinarySearchTest.class);

	public static void main(String[] args) {
		test();
	}

	static void test() {
	}

	@Trace
	public static void testBinarySearch() {
		IntGapList list = IntGapList.create(1, 3, 5, 5, 5, 5, 5, 7, 9);
		int size = list.size();
		IntBinarySearch.binarySearch(list, 3, 0, size);
		IntBinarySearch.binarySearch(list, 5, 0, size);
		IntBinarySearch.binarySearch(list, 0, 0, size);
	}
}
