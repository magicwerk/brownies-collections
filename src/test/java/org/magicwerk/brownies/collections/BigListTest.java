package org.magicwerk.brownies.collections;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.magictest.client.Assert;
import org.magictest.client.Test;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.ThreadTools;
import org.magicwerk.brownies.core.Timer;
import org.magicwerk.brownies.core.reflect.ReflectEl;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.core.strings.StringFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test of BigList.
 *
 * @author Thomas Mauch
 * @version $Id: BigListTest.java 2107 2014-01-24 08:08:38Z origo $
 */
public class BigListTest {
	/** Logger */
	private static Logger LOG = LoggerFactory.getLogger(BigListTest.class);

	public static void main(String[] args) {
		test();
	}

	static void test() {
		testReferenceCounting();
		//testReferenceCountingClear();

		//		testCopy();
		//		testAdd();
		//		testAddAll();
		//		testRemoveLast();
		//		testAddLast();
		//		testAddFirst();
		//		testAddAllLast();
		//		testAddAllFirst();
		//		testAddAllMemory();
		//		testTrimToSize();
	}

	@Test
	public static void testCopyOnWrite() {
		BigList<Integer> list = new BigList<Integer>();
		list.addAll(Arrays.asList(1, 2, 3));

		BigList<Integer> copy = list.copy();
		Assert.assertTrue(copy.equals(list));

		copy.set(1, 9);
		Assert.assertTrue(!copy.equals(list));
	}

	static void testTrimToSize() {
		BigList<Integer> list = new BigList<Integer>(5);
		int num = 20;
		for (int i = 0; i < num; i++) {
			list.add(i);
		}
		for (int i = 0; i < num; i += 2) {
			list.remove(num - i - 1);
		}
		System.out.println(ReflectTools.getObjectSize(list));
		BigList<Integer> list2 = list.copy();
		list.trimToSize();
		System.out.println(ReflectTools.getObjectSize(list));
		CheckTools.checkEq(list, list2);
	}

	/**
	 * Test reference counting of BigList.
	 */
	static void testReferenceCountingClear() {
		BigList<Integer> list = BigList.create();
		list.add(0);
		Integer refCount = (Integer) ReflectEl.getByExpr(list, "rootNode.block.refCount.value");
		Assert.assertTrue(refCount == 1);

		// Create a copy of the source list (with reference counting)
		List<Integer> copy = list.copy();
		refCount = (Integer) ReflectEl.getByExpr(list, "rootNode.block.refCount.value");
		Assert.assertTrue(refCount == 2);

		copy.clear();
		refCount = (Integer) ReflectEl.getByExpr(list, "rootNode.block.refCount.value");
		Assert.assertTrue(refCount == 1);
	}

	/**
	 * Test reference counting of BigList.
	 */
	static void testReferenceCounting() {
		List<WeakReference<List>> lists = new GapList<WeakReference<List>>();

		// Create source list which will must not change
		BigList<Integer> list = BigList.create();
		//GapList<Integer> list = GapList.create();
		list.initMult(10 * 1000, -1);

		Timer t = new Timer();
		int num = 1000;
		for (int i = 0; i < num; i++) {
			// Create a copy of the source list (with reference counting)
			List<Integer> copy = list.copy();

			// Modify the copy (so a copy of the block is made)
			copy.set(0, i);

			// Store the copy with a WeakReference so it can be removed by the GC
			lists.add(i, new WeakReference<List>(copy));

			ThreadTools.sleep(10);
			int gone = 0;
			for (int j = 0; j < i; j++) {
				List<Integer> check = lists.get(j).get();
				if (check == null) {
					// BigList stored in WeakReference has been removed by GC
					gone++;
				} else {
					assert (check.get(0) == j);
				}
			}

			// Show reference count of source list
			Integer refCount = (Integer) ReflectEl.getByExpr(list, "rootNode.block.refCount.value");
			System.out.printf("%s: %s gone, refCount %s\n", i, gone, refCount);
			//System.out.printf("%s: %s gone\n", i, gone);
		}
		t.printElapsed();
	}

	static void testCopy() {
		BigList<Integer> l1 = BigList.create();
		l1.initMult(10 * 1000, 0);

		BigList<Integer> l2 = l1.copy();
		BigList<Integer> l3 = new BigList<Integer>(l1);
		BigList<Integer> l4 = l1.copy();
		//l2.set(0, 1);

		StringFormatter.println("{}", l1.get(0));
		//StringFormatter.println("{}", l2.get(0));
	}

	static void testRemoveLast() {
		//for (int s=2; s<20; s++) {
		BigList<Integer> list = new BigList<Integer>(10);
		for (int i = 0; i < 20; i++) {
			list.add(i);
		}
		System.out.println("bytes = " + ReflectTools.getObjectSize(list));
		for (int i = 0; i < 8; i++) {
			list.remove(1);
		}
		for (int i = 0; i < 8; i++) {
			list.removeLast();
		}
		System.out.println("bytes = " + ReflectTools.getObjectSize(list));
		//}
	}

	static void testAddLast() {
		for (int s = 2; s < 20; s++) {
			BigList<Integer> list = new BigList<Integer>(s);
			for (int i = 0; i < 1000; i++) {
				System.out.printf("add %d \n", i);
				list.add(i);
			}
		}
	}

	static void testAddFirst() {
		for (int s = 2; s < 20; s++) {
			BigList<Integer> list = new BigList<Integer>(s);
			for (int i = 0; i < 1000; i++) {
				System.out.printf("add %d \n", i);
				list.add(0, i);
			}
		}
	}

	static void testAddAllLast() {
		for (int s = 2; s < 20; s++) {
			BigList<Integer> list = new BigList<Integer>(s);
			for (int i = 0; i < 1000; i++) {
				System.out.printf("addAll %d \n", i);
				GapList l = GapList.create(i, i);
				list.addAll(l);
			}
		}
	}

	static void testAddAllFirst() {
		for (int s = 2; s < 20; s++) {
			BigList<Integer> list = new BigList<Integer>(s);
			for (int i = 0; i < 1000; i++) {
				System.out.printf("addAll %d \n", i);
				GapList l = GapList.create(i, i);
				list.addAll(0, l);
			}
		}
	}

	static void testAddAllMemory() {
		//for (int s=2; s<20; s++) {
		Integer add = 0;
		{
			GapList<Integer> list = new GapList<Integer>();
			for (int i = 0; i < 501; i++) {
				//System.out.printf("add %d \n", i);
				GapList l = GapList.create(add, add);
				list.addAll(l);
			}
			System.out.println("size = " + list.size());
			System.out.println("bytes = " + ReflectTools.getObjectSize(list));
		}

		BigList<Integer> list = new BigList<Integer>(1000);
		for (int i = 0; i < 1001; i++) {
			//System.out.printf("add %d \n", i);
			GapList l = GapList.create(add, add);
			list.addAll(l);
		}
		System.out.println("size = " + list.size());
		System.out.println("bytes = " + ReflectTools.getObjectSize(list));

		//}
	}

	static void testAddAll() {
		for (int s = 2; s < 20; s++) {
			BigList<Integer> list = new BigList<Integer>(s);
			Random random = new Random(0);

			int n = 6;
			for (int i = 0; i < n; i++) {
				list.initMult(n, 0);
				for (int j = 0; j < n; j++) {
					list.set(j, j);
				}
			}

			for (int i = 0; i < n; i++) {
				int pos = random.nextInt(list.size() + 1);
				int len = random.nextInt(list.size() + 1);
				IList<Integer> sublist = list.getAll(0, len);
				System.out.printf("addAll %d %d %d %d %d \n", s, n, i, pos, len);
				//		List<Integer> newList = list.copy();// TODO
				BigList<Integer> newList = BigList.create();
				for (Integer e : list) {
					newList.add(e);
				}
				//newList.get(5);
				newList.addAll(pos, sublist);
				newList.clear();
			}

		}
	}

	static void testAdd() {
		//		for (int s=2; s<20; s++) {
		for (int s = 6; s < 20; s++) {
			BigList<Integer> list = new BigList<Integer>(s);

			int n = 200;
			Random random = new Random(0);
			for (int i = 0; i < n; i++) {
				int p = random.nextInt(i + 1);
				System.out.printf("add %d %d %d %d \n", s, n, i, p);
				list.add(p, i);
				//System.out.println(list);
			}
			for (int i = 0; i < n; i++) {
				int p = 0;
				if (list.size() > 1) {
					p = random.nextInt(list.size() - 1);
				}
				System.out.printf("remove %d %d %d %d \n", s, n, i, p);
				list.remove(p);
			}
			for (int i = 0; i < n; i++) {
				list.initMult(n, 0);
				for (int j = 0; j < n; j++) {
					list.set(j, j);
				}

				int p0 = 0;
				if (list.size() > 1) {
					p0 = random.nextInt(list.size() - 1);
				}
				int p1 = 0;
				if (list.size() > 1) {
					p1 = random.nextInt(list.size() - 1);
				}
				if (p0 > p1) {
					int p = p0;
					p0 = p1;
					p1 = p;
				}
				System.out.printf("removeAll %d %d %d %d %d \n", s, n, i, p0, p1);
				list.remove(p0, p1 - p0);
			}
		}
	}

}
