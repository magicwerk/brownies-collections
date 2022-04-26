package org.magicwerk.brownies.collections.sandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.magicwerk.brownies.core.PrintTools;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Test of class ExternalSort.
 *
 * @author Thomas Mauch
 */
public class ExternalSortTest<E> {
	/** Logger */
	private static Logger LOG = (Logger) LoggerFactory.getLogger(ExternalSortTest.class);

	public static void main(String[] args) {
        test();
    }
	
	static void test() {
	    List<Integer> list = Arrays.asList(new Integer[] { 1, 3, 2, 4  } );
	    ExternalSort<Integer> sort = new ExternalSort<Integer>();
	    sort.setChunkSize(2);
	    Iterator<Integer> sorted = sort.sort(list.iterator());
	    List<Integer> sortedList = new ArrayList<Integer>();
	    while (sorted.hasNext()) {
	        sortedList.add(sorted.next());
	    }
	    System.out.println(PrintTools.print(sortedList));
	}
}
