package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.Random;

import org.magictest.client.Assert;
import org.magicwerk.brownies.collections.primitive.IIntList;
import org.magicwerk.brownies.collections.primitive.IntBigList;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.magicwerk.brownies.collections.primitive.IntObjBigList;
import org.magicwerk.brownies.collections.primitive.IntObjGapList;
import org.magicwerk.brownies.core.logback.LogbackTools;

import ch.qos.logback.classic.Logger;


/**
 * Test lists by comparing the output with an instance of ArrayList.
 * The following classes are tested: GapList, IntGapList, IntObjGapList, BigList, IntBigList, IntObjBigList.
 *
 * @author Thomas Mauch
 */
public class ListCompareTest {

	/**
	 * Class storing instances of ArrayList, GapList, IntGapList, IntObjGapList, BigList, IntBigList, IntObjBigList
	 * and keeping the synchronized by applying methods to all lists at the same time and comparing the outcome.
	 */
	static class Lists {
		ArrayList<Integer> arrayList = new ArrayList<Integer>();

		GapList<Integer> gapList = new GapList<Integer>();
		IntGapList intGapList = new IntGapList();
		IntObjGapList intObjGapList = new IntObjGapList();

		// Use a small blockSize for tests so block operation like split or merge occur often
		int blockSize = 10;
		BigList<Integer> bigList = new BigList<Integer>(blockSize);
		IntBigList intBigList = new IntBigList(blockSize);
		IntObjBigList intObjBigList = new IntObjBigList(blockSize);


		int size() {
			return arrayList.size();
		}

		void add(int index, int val) {
			arrayList.add(index, val);

			gapList.add(index, val);
			intGapList.add(index, val);
			intObjGapList.add(index, val);

			bigList.add(index, val);
			intBigList.add(index, val);
			intObjBigList.add(index, val);
		}

		void addAll(int index, int len) {
			ArrayList<Integer> add1 = new ArrayList<Integer>(len);
			for (int i=0; i<len; i++) {
				add1.add(i);
			}
			int[] add2 = new int[len];
			for (int i=0; i<len; i++) {
				add2[i] = i;
			}

			arrayList.addAll(index, add1);

			gapList.addAll(index, add1);
			intGapList.addArray(index, add2);
			intObjGapList.addAll(index, add1);

			bigList.addAll(index, add1);
			intBigList.addArray(index, add2);
			intObjBigList.addAll(index, add1);
		}

		void remove(int index) {
			arrayList.remove(index);

			gapList.remove(index);
			intGapList.remove(index);
			intObjGapList.remove(index);

			bigList.remove(index);
			intBigList.remove(index);
			intObjBigList.remove(index);
		}

		void removeAll(int index, int len) {
			arrayList.subList(index, index+len).clear();;

			gapList.remove(index, len);
			intGapList.remove(index, len);
			intObjGapList.remove(index, len);

			bigList.remove(index, len);
			intBigList.remove(index, len);
			intObjBigList.remove(index, len);
		}

		void checkEquals() {
			checkEquals(gapList);
			checkEquals(intGapList);
			checkEquals(intObjGapList);

			checkEquals(bigList);
			checkEquals(intBigList);
			checkEquals(intObjBigList);
		}

		void checkEquals(IList<Integer> list) {
			Assert.assertTrue(arrayList.size() == list.size());
			for (int i=0; i<arrayList.size(); i++) {
				int val1 = arrayList.get(i);
				int val2 = (Integer) list.get(i);
				if (val1 != val2) {
					System.out.println("Expected: " + arrayList);
					System.out.println("Found:    " + list);
					Assert.assertTrue(val1 == val2);
				}
			}
		}

		void checkEquals(IIntList list) {
			Assert.assertTrue(arrayList.size() == list.size());
			for (int i=0; i<arrayList.size(); i++) {
				int val1 = arrayList.get(i);
				int val2 = list.get(i);
				if (val1 != val2) {
					System.out.println("Expected: " + arrayList);
					System.out.println("Found:    " + list);
					Assert.assertTrue(val1 == val2);
				}
			}
		}
	}

	/** Logger */
	static final Logger LOG = LogbackTools.getConsoleLogger();

	static int size = 1000;
	static int maxSize = 10*1000;
	static int numOps = 100*1000;

	Lists lists;
	Random rnd;

	public static void main(String[] args) {
		test();
	}

	static void test() {
	    new ListCompareTest().doTest();
	}

	void doTest() {
		lists = new Lists();

		for (int i=0; i<size; i++) {
			int val = rnd(i+1);
			lists.add(val, val);
		}

		for (int i=0; i<numOps; i++) {
			modify();
		}

	}

	/**
	 * Returns random number.
	 *
	 * @param max	exclusive end of value range
	 * @return		random number in range >=0 and <max, 0 if max is 0.
	 */
	int rnd(int max) {
		if (rnd == null) {
			rnd = new Random(1);
		}
		if (max == 0) {
			return 0;
		}
		return rnd.nextInt(max);
	}

	//int count;

	/**
	 * Apply modification to lists.
	 */
	void modify() {
		//System.out.println(count);
		//if (count == 7) {
		//	System.out.println();
		//}
		//count++;

		int r = rnd(4);
		switch (r) {
		case 0:
			if (lists.size() < maxSize) {
				add();
			} else {
				remove();
			}
			break;
		case 1:
			remove();
			break;
		case 2:
			if (lists.size() < maxSize) {
				addAll();
			} else {
				removeAll();
			}
			break;
		case 3:
			removeAll();
			break;
		default:
			assert(false);
		}
		lists.checkEquals();
	}

	void add() {
		int r = rnd(lists.size()+1);
		lists.add(r, r);
	}

	void remove() {
		int size = lists.size();
		if (size == 0) {
			return;
		}
		int r = rnd(size);
		lists.remove(r);
	}

	void addAll() {
		int size = lists.size();
		int r;
		int r2 = rnd(10);
		if (r2 == 0) {
			r = 0;
		} else if (r2 == 1) {
			r = size;
		} else {
			r = rnd(size+1);
		}
		int l = 3*rnd(lists.blockSize)+1;
		lists.addAll(r, l);
	}

	void removeAll() {
		int size = lists.size();
		if (size == 0) {
			return;
		}

		int r0;
		int r1;
		int r2 = rnd(10);
		if (r2 == 0) {
			r0 = 0;
			r1 = rnd(size);
		} else if (r2 == 1) {
			r0 = rnd(size);
			r1 = size;
		} else if (r2 == 2) {
			r0 = 0;
			r1 = size;
		} else {
			r0 = rnd(size);
			r1 = rnd(size);
		}

		int p0 = Math.min(r0, r1);
		int p1 = Math.max(r0, r1);
		lists.removeAll(p0, p1-p0);
	}

}

