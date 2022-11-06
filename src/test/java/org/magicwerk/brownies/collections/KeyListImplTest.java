/*
 * Copyright 2013 by Thomas Mauch
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
 * $Id$
 */
package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.TestHelper.ComparableName;
import org.magicwerk.brownies.collections.TestHelper.Name;
import org.magicwerk.brownies.collections.TestHelper.Ticket;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.ThreadTools;
import org.magicwerk.brownies.core.Timer;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.slf4j.Logger;

/**
 * Test of KeyListImpl.
 *
 * @author Thomas Mauch
 */
public class KeyListImplTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		test();
	}

	static void test() {
		//testAsSet();
		//testAddAllWindowSize();
		testTriggers();
		//testSort();
	}

	@Capture
	public static void testSort() {
		// Sorting with natural comparator on null object fails
		try {
			ArrayList<String> al = new ArrayList<>();
			al.add("c");
			al.add(null);
			al.add("a");
			Collections.sort(al);
			CheckTools.error();
		} catch (Exception e) {
		}

		KeyList<Integer> list1 = new KeyList.Builder<Integer>().withOrderByElem(int.class).build();
		doTestSort(list1, null);

		// This list allows null values and the created comparator will therefore also support null values
		// which is not equivalent with the natural comparator
		KeyList<String> list2 = new KeyList.Builder<String>().withOrderByElem(true).build();
		doTestSort(list2, null);

		// This list prohibits null values and the created comparator will therefore be equivalent with the natural comparator
		KeyList<String> list2b = new KeyList.Builder<String>().withOrderByElem(true).withElemNull(false).build();
		doTestSort(list2b, null);

		KeyList<Name> list3 = new KeyList.Builder<Name>().withElemSort(Name.Comparator).withOrderByElem(true).build();
		doTestSort(list3, Name.Comparator);

		KeyList<ComparableName> list4 = new KeyList.Builder<ComparableName>().withOrderByElem(true).withElemNull(false).build();
		doTestSort(list4, null);

		// If a list is not sorte by element but by a key, sort() will always fail
		Key1List<Name, String> list5 = new Key1List.Builder<Name, String>().withPrimaryKey1Map(Name.Mapper).withKey1Sort(true).withOrderByKey1(true).build();
		doTestSort(list5, null);
	}

	static void doTestSort(IList list, Comparator comp) {
		try {
			list.sort(comp);
			System.out.println("sort ok");
		} catch (Exception e) {
			System.out.println("sort fails: " + e.getMessage());
		}
	}

	@Capture
	public static void testTriggers() {
		final boolean[] cancel = new boolean[1];
		Key1List<Name, String> list = new Key1List.Builder<Name, String>().withPrimaryKey1Map(Name.Mapper)
				.withBeforeInsertTrigger(new Consumer<Name>() {
					@Override
					public void accept(Name elem) {
						if (cancel[0]) {
							System.out.println("cancel insert: " + elem);
							cancel[0] = false;
							throw new IllegalArgumentException("cancel insert");
						}
						System.out.println("insert: " + elem);
					}
				})
				.withBeforeDeleteTrigger(new Consumer<Name>() {
					@Override
					public void accept(Name elem) {
						if (cancel[0]) {
							System.out.println("cancel delete: " + elem);
							cancel[0] = false;
							throw new IllegalArgumentException("cancel delete");
						}
						System.out.println("delete: " + elem);
					}
				})
				.build();

		Name n1 = new Name("a1");
		Name n2 = new Name("b2");
		Name n3 = new Name("c3");
		list.add(n1);
		list.add(n2);
		list.add(n3);
		list.remove(n1);
		list.removeByKey1(n2.getName());
		list.removeAllByKey1(n3.getName());

		list.add(n1);
		list.add(n2);
		list.add(n3);
		try {
			cancel[0] = true;
			list.remove(n1);
		} catch (Exception e) {
		}
		try {
			cancel[0] = true;
			list.removeByKey1(n2.getName());
		} catch (Exception e) {
		}
		try {
			cancel[0] = true;
			list.removeAllByKey1(n3.getName());
		} catch (Exception e) {
		}

		System.out.println(list);

		try {
			cancel[0] = true;
			list.clear(); // FIXME
		} catch (Exception e) {
		}
		System.out.println(list);

		list.initArray(n3, n2);

		testTriggerBeforeInsert();
	}

	//

	static void testTriggerBeforeInsert() {
		KeyNameList list = new KeyNameList();
		list.add(new KeyName("a"));
		list.add(new KeyName(null));
		list.add(new KeyName("b"));
		list.add(new KeyName(null));
		list.add(new KeyName("c"));
		System.out.println(list);
	}

	static class KeyName {
		String name;
		String key;

		KeyName(String name) {
			this.name = name;
			this.key = name;
		}

		/** Getter for {@link #key} */
		public String getKey() {
			return key;
		}

		@Override
		public String toString() {
			return key + " (" + name + ")";
		}
	}

	static class KeyNameList extends Key1List<KeyName, String> {
		public KeyNameList() {
			getBuilder().withPrimaryKey1Map(KeyName::getKey).withBeforeInsertTrigger(new Consumer<KeyName>() {
				@Override
				public void accept(KeyName elem) {
					if (elem.getKey() == null) {
						elem.key = getUniqueKey();
					}
				}
			}).build();
		}

		/** Determine unique key */
		String getUniqueKey() {
			String key = null;
			int i = 0;
			while (true) {
				key = "#" + i;
				if (!containsKey1(key)) {
					return key;
				}
				i++;
			}
		}
	}

	//

	static void testPerformanceWindowSize() {
		int num = 1000 * 1000;
		int maxSize = 10;

		Timer t = new Timer();
		GapList<Integer> gl = GapList.create(5);
		for (int i = 0; i < num; i++) {
			if (gl.size() == maxSize) {
				gl.removeFirst();
			}
			gl.addLast(i);
		}
		LOG.info("GapList window: {}", t.elapsedString());

		t = new Timer();
		KeyList<Integer> kl = new KeyList.Builder<Integer>().withWindowSize(5).build();
		for (int i = 0; i < num; i++) {
			kl.addLast(i);
		}
		LOG.info("KeyList window: {}", t.elapsedString());
	}

	static Ticket t1 = new Ticket(1, "extId1", "text1");
	static Ticket t2 = new Ticket(2, "extId2", "text2");
	static Ticket t3 = new Ticket(3, "extId3", "text3");
	static List<Ticket> ts = GapList.create(t2, t3, t1);

	// Access list as set and modify it
	@Capture
	public static void testAsSet() {
		{
			KeyList<Ticket> list = new KeyList.Builder<Ticket>().build();
			list.addAll(ts);
			Set<Ticket> set = list.asSet();
			System.out.println(set);
			set.remove(t2);
			System.out.println(set);
			Iterator<Ticket> iter = set.iterator();
			iter.next();
			iter.remove();
			System.out.println(set);
		}
		{
			KeyList<Integer> list = new KeyList.Builder<Integer>().withOrderByElem(int.class).build();
			list.addAll(Arrays.asList(2, 3, 1));
			Set<Integer> set = list.asSet();
			System.out.println(set);
			set.remove(2);
			System.out.println(set);
			Iterator<Integer> iter = set.iterator();
			iter.next();
			iter.remove();
			System.out.println(set);
		}
		{
			KeyList<Integer> list = new KeyList.Builder<Integer>().withPrimaryElem().build();
			list.add(1);
			try {
				list.add(1);
			} catch (Exception e) {
				System.out.println(e);
			}
			System.out.println(list);

			KeyList<Integer> setList = new KeyList.Builder<Integer>().withPrimaryElem().build();
			Set<Integer> set = setList.asSet();
			set.add(1);
			set.add(1);
			System.out.println(set);
		}
	}

	/**
	 * @return KeyList containing 4 elements with window size of 4
	 */
	static KeyList<Integer> getWindowList() {
		KeyList<Integer> ml = new KeyList.Builder<Integer>().withWindowSize(4).build();
		ml.add(1);
		ml.add(3);
		ml.add(5);
		ml.add(7);
		return ml;
	}

	@Trace(traceClass = "org.magicwerk.brownies.collections.KeyList", traceMethod = "/.*/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testAddMaxSize() {
		KeyList<Integer> ml = new KeyList.Builder<Integer>().withMaxSize(3).build();
		ml.add(1);
		ml.add(3);
		ml.add(5);
		ml.add(7);

		ml.clear();
		ml.addArray(new Integer[] { 1, 3 });
		ml.addArray(new Integer[] { 5, 7 });
		ml.add(9);
	}

	@Trace(traceMethod = "org.magicwerk.brownies.collections.KeyList.add", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testWindowSize() {
		KeyList<String> list = new KeyList.Builder<String>().withWindowSize(2).build();
		list.add("a");
		list.add("ab");
		list.add(2, "abc");
		list.add("abcd");
		System.out.println(list);
	}

	@Trace(traceMethod = "org.magicwerk.brownies.collections.KeyList.add", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testAddWindowSize() {
		getWindowList().add(4, 10);
		getWindowList().add(3, 10);
		getWindowList().add(2, 10);
		getWindowList().add(1, 10);
		getWindowList().add(0, 10);
	}

	@Trace(traceClass = "org.magicwerk.brownies.collections.KeyList", traceMethod = "/addAll|addArray/", parameters = Trace.THIS
			| Trace.ALL_PARAMS, result = Trace.THIS)
	public static void testAddAllWindowSize() {
		getWindowList().addArray(4, new Integer[] { 10, 11 });
		getWindowList().addArray(3, new Integer[] { 10, 11 });
		getWindowList().addArray(2, new Integer[] { 10, 11 });
		getWindowList().addArray(1, new Integer[] { 10, 11 });
		getWindowList().addArray(0, new Integer[] { 10, 11 });

		getWindowList().addAll(0, new ArrayList<Integer>(GapList.create(10, 11)));
	}

	static void testMemory() {
		// 48 MB
		//SetList<Integer> list = new SetList<Integer>();
		//SetList<Integer> list = new SetList.Builder<Integer>().build();
		// 16 MB
		//SetList<Integer> list = new SetList.Builder<Integer>().withKeySort().build();
		// TreeSet: 40 MB
		//SortedSet<Integer> list = new TreeSet<Integer>();
		// HashSet: 44 MB
		//Set<Integer> list = new HashSet<Integer>();
		// GapSet: 16 MB
		//GapSet<Integer> list = GapSet.create();
		// GapSet: 4 MB
		KeyList<Integer> list = new KeyList.Builder<Integer>().withOrderByElem(int.class).build();

		//Performance:
		// HashSet:       0.38, 0.04
		// TreeSet:	      0.53, 0.15
		// GapSet(true):  0.21, 0.80
		// GapSet(false): 0.10, 0.08

		// BloomFilter:  (0.1->0.7 MB: 0.86s/0.81s, , 0.2->0.5 MB, 0.05->0.9MB: 1.3s/1.3s)
		//BloomFilter<Integer> list = new BloomFilter<Integer>(0.05, 1000*1000);
		// BloomFilter: only add()/contains(), remove()/size()/iterator() does not work
		Timer t = new Timer();
		int num = 1000 * 1000;
		for (int i = 0; i < num; i++) {
			list.add(i);
		}
		t.printElapsed();
		t.start();
		for (int i = 0; i < num; i++) {
			list.contains(i);
		}
		t.printElapsed();

		System.out.println("wait");
		ThreadTools.sleep(30 * 1000);
		System.out.println(list.size());
	}

}
