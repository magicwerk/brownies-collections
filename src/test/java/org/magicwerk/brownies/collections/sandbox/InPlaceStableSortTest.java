package org.magicwerk.brownies.collections.sandbox;

import java.util.Random;

import org.magicwerk.brownies.runner.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class InPlaceStableSortTest {
	/** Logger */
	private static Logger LOG = LoggerFactory.getLogger(InPlaceStableSortTest.class);

	public static void main(String[] args) {
		test();
	}

	static Random random = new Random(1);

	static void test() {
		testPerformanceSort(100 * 1000);

		testPerformanceSort(500 * 1000);

		testPerformanceSort(1000 * 1000);

		testPerformanceSort(2 * 1000 * 1000);

		testPerformanceSort(4 * 1000 * 1000);

		// GapList OOME
		testPerformanceSort(6 * 1000 * 1000);

		testPerformanceSort(8 * 1000 * 1000);

		testPerformanceSort(10 * 1000 * 1000);

		testPerformanceSort(12 * 1000 * 1000);
	}

	static void testPerformanceSort(final int size) {
		final int numIter = 1;

		Runner runner = new Runner("Sort " + size);
		//		runner.add(new Run("GapList") {
		//			GapList<Value> list;
		//			public void initEach() {
		//				list = new GapList<Value>(size);
		//				for (int i=0; i<size; i++) {
		//					int val = random.nextInt(size);
		//					list.add(new Value(val, i));
		//				}
		//			};
		//			public void run() {
		//				for (int i=0; i<numIter; i++) {
		//					list.sort(null);
		//				}
		//			}
		//		});
		//		runner.add(new Run("InPlace") {
		//			GapList<Value> list;
		//			InPlaceStableSort<Value> sorter;
		//			public void initEach() {
		//				list = new GapList<Value>(size);
		//				for (int i=0; i<size; i++) {
		//					int val = random.nextInt(size);
		//					list.add(new Value(val, i));
		//				}
		//				sorter = new InPlaceStableSort<Value>(list, null);
		//			};
		//			public void run() {
		//				for (int i=0; i<numIter; i++) {
		//					sorter.sort();
		//				}
		//			}
		//		});
		//		runner.add(new Run("BigList InPlace") {
		//			BigList<Value> list;
		//			InternalSort<Value> sorter;
		//			public void beforeRun() {
		//				list = new BigList<Value>(size);
		//				for (int i=0; i<size; i++) {
		//					int val = random.nextInt(size);
		//					list.add(new Value(val, i));
		//				}
		//				sorter = new InternalSort<Value>(list, null);
		//			};
		//			public void run() {
		//				for (int i=0; i<numIter; i++) {
		//					sorter.sort();
		//				}
		//			}
		//		});
		runner.run();
		runner.printResults();

	}

}

class Value implements Comparable<Value> {
	int value;
	int origIdx;

	public Value(int value, int origIdx) {
		this.value = value;
		this.origIdx = origIdx;
	}

	@Override
	public int compareTo(Value o) {
		if (value == o.value) {
			return origIdx - o.origIdx;
		} else {
			return value - o.value;
		}
	}

	@Override
	public String toString() {
		return value + " (" + origIdx + ")";
	}

}
