/*
 * Copyright 2012 by Thomas Mauch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: MaxList.java 977 2012-03-11 20:45:48Z thmauch $
 */
package org.magicwerk.brownies.collections.sandbox;

import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.tools.runner.Run;
import org.magicwerk.brownies.tools.runner.Runner;
import org.slf4j.Logger;

/**
 * Test of GapSet.
 *
 * @author Thomas Mauch
 * @version $Id: MaxList.java 977 2012-03-11 20:45:48Z thmauch $
 */
public class GapSetTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		//testPerformance2();
		testSortedList1();
		//test();
	}

	static void test() {
	}

	static void testPerformance2() {
		final int size = 100 * 1000;
		new Runner().run(new Run() {
			GapList<Integer> list;

			@Override
			public void beforeRun() {
				list = GapList.create();
				for (int i = 0; i < size; i++) {
					list.add(i);
				}
			}

			@Override
			public void run() {
				list.contains(-1);
			}
		});
		new Runner().run(new Run() {
			GapList<Integer> list;

			@Override
			public void beforeRun() {
				list = GapList.create();
				for (int i = 0; i < size; i++) {
					list.add(i);
				}
			}

			@Override
			public void run() {
				list.remove(size / 2);
				list.remove(size / 4);
			}
		});
	}

	static void testSortedList1() {
		final int size = 10 * 1000;
		new Runner().run(new Run() {
			@Override
			public void run() {
				Random rnd = new Random(123);
				GapList<Integer> list = GapList.create();
				for (int i = 0; i < size; i++) {
					int val = rnd.nextInt(size);
					int index = list.binarySearch(val, null);
					if (index < 0) {
						index = -index - 1;
					}
					list.add(index, val);
				}
			}
		});
		new Runner().run(new Run() {
			@Override
			public void run() {
				Random rnd = new Random(123);
				GapList<Integer> list = GapList.create();
				for (int i = 0; i < size; i++) {
					int val = rnd.nextInt(size);
					list.add(val);
					list.sort(null);
				}
			}
		});
		new Runner().run(new Run() {
			@Override
			public void run() {
				Random rnd = new Random(123);
				GapList<Integer> list = GapList.create();
				for (int i = 0; i < size; i++) {
					int val = rnd.nextInt(size);
					list.add(val);
					if (i % 10 == 0) {
						list.sort(null);
					}
				}
			}
		});
		new Runner().run(new Run() {
			@Override
			public void run() {
				Random rnd = new Random(123);
				GapList<Integer> copy = GapList.create();
				TreeSet<Integer> list = new TreeSet<Integer>();
				for (int i = 0; i < size; i++) {
					int val = rnd.nextInt(size);
					list.add(val);
					if (i % 4 == 0) {
						copy.initAll(GapList.create(list));
					}
				}
			}
		});
	}

	static void testPerformance() {
		int size = 1000 * 1000;
		//testPerformanceAddSorted(size);
		size = 100 * 1000;
		//testPerformanceAddRandom(size);
		testPerformanceContains(1000 * 1000, 1000 * 1000);
	}

	static void testPerformanceAddSorted(final int size) {
		Runner runner = new Runner("Add sorted");
		runner.add(new Run("GapSet (Integer)") {
			GapSet<Integer> set = GapSet.create();

			@Override
			public void run() {
				for (int i = 0; i < size; i++) {
					set.add(i);
				}
			}
		});
		runner.add(new Run("GapSet (int)") {
			GapSet<Integer> set = GapSet.create(int.class);

			@Override
			public void run() {
				for (int i = 0; i < size; i++) {
					set.add(i);
				}
			}
		});
		runner.add(new Run("HashSet") {
			HashSet<Integer> set = new HashSet<Integer>();

			@Override
			public void run() {
				for (int i = 0; i < size; i++) {
					set.add(i);
				}
			}
		});
		runner.add(new Run("TreeSet") {
			TreeSet<Integer> set = new TreeSet<Integer>();

			@Override
			public void run() {
				for (int i = 0; i < size; i++) {
					set.add(i);
				}
			}
		});
		runner.run();
		runner.printResults();
	}

	static void testPerformanceAddRandom(final int size) {
		Runner runner = new Runner("Add random");
		runner.add(new Run("GapSet (Integer)") {
			GapSet<Integer> set = GapSet.create();
			Random random = new Random(123);

			@Override
			public void run() {
				for (int i = 0; i < size; i++) {
					set.add(random.nextInt(size));
				}
			}
		});
		runner.add(new Run("GapSet (int)") {
			GapSet<Integer> set = GapSet.create(int.class);
			Random random = new Random(123);

			@Override
			public void run() {
				for (int i = 0; i < size; i++) {
					set.add(random.nextInt(size));
				}
			}
		});
		runner.add(new Run("HashSet") {
			HashSet<Integer> set = new HashSet<Integer>();
			Random random = new Random(123);

			@Override
			public void run() {
				for (int i = 0; i < size; i++) {
					set.add(random.nextInt(size));
				}
			}
		});
		runner.add(new Run("TreeSet") {
			TreeSet<Integer> set = new TreeSet<Integer>();
			Random random = new Random(123);

			@Override
			public void run() {
				for (int i = 0; i < size; i++) {
					set.add(random.nextInt(size));
				}
			}
		});
		runner.run();
		runner.printResults();
	}

	static void testPerformanceContains(final int size, final int numContains) {
		Runner runner = new Runner("Contains");
		runner.add(new Run("GapSet (Integer)") {
			GapSet<Integer> set = GapSet.create();
			{
				for (int i = 0; i < size; i++) {
					set.add(i);
				}
			}
			Random random = new Random(123);

			@Override
			public void run() {
				for (int i = 0; i < numContains; i++) {
					set.contains(random.nextInt(size));
				}
			}
		});
		runner.add(new Run("GapSet (int)") {
			GapSet<Integer> set = GapSet.create(int.class);
			{
				for (int i = 0; i < size; i++) {
					set.add(i);
				}
			}
			Random random = new Random(123);

			@Override
			public void run() {
				for (int i = 0; i < numContains; i++) {
					set.contains(random.nextInt(size));
				}
			}
		});
		runner.add(new Run("HashSet") {
			HashSet<Integer> set = new HashSet<Integer>();
			{
				for (int i = 0; i < size; i++) {
					set.add(i);
				}
			}
			Random random = new Random(123);

			@Override
			public void run() {
				for (int i = 0; i < numContains; i++) {
					set.contains(random.nextInt(size));
				}
			}
		});
		runner.add(new Run("TreeSet") {
			TreeSet<Integer> set = new TreeSet<Integer>();
			{
				for (int i = 0; i < size; i++) {
					set.add(i);
				}
			}
			Random random = new Random(123);

			@Override
			public void run() {
				for (int i = 0; i < numContains; i++) {
					set.contains(random.nextInt(size));
				}
			}
		});
		runner.run();
		runner.printResults();
	}

	/**
	 * Returns list with specified size containing elements 0, 1, 2, ...
	 *
	 * @param size number of elements in list
	 * @return     created list
	 */
	static GapSet<Integer> getGapSet(int size) {
		GapSet<Integer> set = GapSet.create();
		for (int i = 0; i < size; i++) {
			set.add(2 * i);
		}
		return set;
	}

	static TreeSet<Integer> getTreeSet(int size) {
		TreeSet<Integer> set = new TreeSet<Integer>();
		for (int i = 0; i < size; i++) {
			set.add(2 * i);
		}
		return set;
	}

	static HashSet<Integer> getHashSet(int size) {
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < size; i++) {
			set.add(2 * i);
		}
		return set;
	}

	@Trace
	public void testContains() {
		//GapSet<Integer> set = getGapSet(3);
		Set<Integer> set = getGapSet(3);
		//		Set<Integer> set = getTreeSet(3);
		//		Set<Integer> set = getHashSet(3);
		set.contains(2);
		set.contains(9);
		set.contains(null); // NPE (NPE in java.util.TreeSet, false in java.util.HashSet)
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS)
	public void testIsEmpty() {
		Set<Integer> set = getGapSet(3);
		set.isEmpty();
		set.clear();
		set.isEmpty();
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public void testClear() {
		Set<Integer> set = getGapSet(3);
		set.clear();
	}

	@Trace
	public void testSubSet() {
		NavigableSet<Integer> set = getGapSet(4);
		NavigableSet<Integer> treeSet = getTreeSet(4);

		NavigableSet<Integer> subSet = set.subSet(2, true, 6, true);
		//NavigableSet<Integer> treeSubSet = treeSet.subSet(2, true, 6, true);
		//Assert.assertTrue(subSet.equals(treeSubSet));

		subSet = set.subSet(1, true, 6, true);
		//treeSubSet = treeSet.subSet(1, true, 6, true);
		//Assert.assertTrue(subSet.equals(treeSubSet));
	}

	//	@Trace
	//	public void testSubSet() {
	//		Object param0 = null;
	//		Object param1 = null;
	//		GapSet obj = new GapSet();
	//		obj.subSet(param0, param1);
	//	}

	/*
		@Trace
		public void testEMPTY() {
			GapSet.EMPTY();
		}
	
		@Trace
		public void testCreate() {
			GapSet.create();
		}
	
		@Trace
		public void testCreate() {
			int param0 = 0;
			GapSet.create(param0);
		}
	
		@Trace
		public void testCreate() {
			java.util.Collection param0 = null;
			GapSet.create(param0);
		}
	
		@Trace
		public void testCreate() {
			Object[] param0 = null;
			GapSet.create(param0);
		}
	
		@Trace
		public void testCreate() {
			Class param0 = null;
			GapSet.create(param0);
		}
	
		@Trace
		public void testCreate() {
			Class param0 = null;
			int param1 = 0;
			GapSet.create(param0, param1);
		}
	
		@Trace
		public void testCreate() {
			Class param0 = null;
			java.util.Collection param1 = null;
			GapSet.create(param0, param1);
		}
	
		@Trace
		public void testCreate() {
			Class param0 = null;
			Object[] param1 = null;
			GapSet.create(param0, param1);
		}
	
		@Trace
		public void testCreate() {
			java.util.Comparator param0 = null;
			GapSet.create(param0);
		}
	
		@Trace
		public void testCreate() {
			java.util.Comparator param0 = null;
			int param1 = 0;
			GapSet.create(param0, param1);
		}
	
		@Trace
		public void testCreate() {
			java.util.Comparator param0 = null;
			java.util.Collection param1 = null;
			GapSet.create(param0, param1);
		}
	
		@Trace
		public void testCreate() {
			java.util.Comparator param0 = null;
			Object[] param1 = null;
			GapSet.create(param0, param1);
		}
	
		@Trace
		public void testCreateWrapperList() {
			Class param0 = null;
			GapSet.createWrapperList(param0);
		}
	
		@Trace
		public void testInit() {
			GapSet obj = new GapSet();
			obj.init();
		}
	
		@Trace
		public void testInit() {
			int param0 = 0;
			GapSet obj = new GapSet();
			obj.init(param0);
		}
	
		@Trace
		public void testInit() {
			java.util.Collection param0 = null;
			GapSet obj = new GapSet();
			obj.init(param0);
		}
	
		@Trace
		public void testInit() {
			Object[] param0 = null;
			GapSet obj = new GapSet();
			obj.init(param0);
		}
	
		@Trace
		public void testUnmodifiableList() {
			GapSet obj = new GapSet();
			obj.unmodifiableList();
		}
	
		@Trace
		public void testUnmodifiableSet() {
			GapSet obj = new GapSet();
			obj.unmodifiableSet();
		}
	
		@Trace
		public void testCompare() {
			Object param0 = null;
			Object param1 = null;
			GapSet obj = new GapSet();
			obj.compare(param0, param1);
		}
	
		@Trace
		public void testAdd() {
			Object param0 = null;
			GapSet obj = new GapSet();
			obj.add(param0);
		}
	
		@Trace
		public void testAddAll() {
			java.util.Collection param0 = null;
			GapSet obj = new GapSet();
			obj.addAll(param0);
		}
	
		@Trace
		public void testContainsAll() {
			java.util.Collection param0 = null;
			GapSet obj = new GapSet();
			obj.containsAll(param0);
		}
	
		@Trace
		public void testIterator() {
			GapSet obj = new GapSet();
			obj.iterator();
		}
	
		@Trace
		public void testRemove() {
			Object param0 = null;
			GapSet obj = new GapSet();
			obj.remove(param0);
		}
	
		@Trace
		public void testRemoveAll() {
			java.util.Collection param0 = null;
			GapSet obj = new GapSet();
			obj.removeAll(param0);
		}
	
		@Trace
		public void testRetainAll() {
			java.util.Collection param0 = null;
			GapSet obj = new GapSet();
			obj.retainAll(param0);
		}
	
		@Trace
		public void testSize() {
			GapSet obj = new GapSet();
			obj.size();
		}
	
		@Trace
		public void testToArray() {
			GapSet obj = new GapSet();
			obj.toArray();
		}
	
		@Trace
		public void testToArray() {
			Object[] param0 = null;
			GapSet obj = new GapSet();
			obj.toArray(param0);
		}
	
		@Trace
		public void testComparator() {
			GapSet obj = new GapSet();
			obj.comparator();
		}
	
		@Trace
		public void testHeadSet() {
			Object param0 = null;
			GapSet obj = new GapSet();
			obj.headSet(param0);
		}
	
		@Trace
		public void testTailSet() {
			Object param0 = null;
			GapSet obj = new GapSet();
			obj.tailSet(param0);
		}
	
		@Trace
		public void testFirst() {
			GapSet obj = new GapSet();
			obj.first();
		}
	
		@Trace
		public void testLast() {
			GapSet obj = new GapSet();
			obj.last();
		}
	
		@Trace
		public void testLower() {
			Object param0 = null;
			GapSet obj = new GapSet();
			obj.lower(param0);
		}
	
		@Trace
		public void testFloor() {
			Object param0 = null;
			GapSet obj = new GapSet();
			obj.floor(param0);
		}
	
		@Trace
		public void testCeiling() {
			Object param0 = null;
			GapSet obj = new GapSet();
			obj.ceiling(param0);
		}
	
		@Trace
		public void testHigher() {
			Object param0 = null;
			GapSet obj = new GapSet();
			obj.higher(param0);
		}
	
		@Trace
		public void testPollFirst() {
			GapSet obj = new GapSet();
			obj.pollFirst();
		}
	
		@Trace
		public void testPollLast() {
			GapSet obj = new GapSet();
			obj.pollLast();
		}
	
		@Trace
		public void testDescendingSet() {
			GapSet obj = new GapSet();
			obj.descendingSet();
		}
	
		@Trace
		public void testDescendingIterator() {
			GapSet obj = new GapSet();
			obj.descendingIterator();
		}
	
		@Trace
		public void testHeadSet() {
			Object param0 = null;
			boolean param1 = null;
			GapSet obj = new GapSet();
			obj.headSet(param0, param1);
		}
	
		@Trace
		public void testTailSet() {
			Object param0 = null;
			boolean param1 = null;
			GapSet obj = new GapSet();
			obj.tailSet(param0, param1);
		}
	*/
}
