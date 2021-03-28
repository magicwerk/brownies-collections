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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.magictest.client.Assert;
import org.magictest.client.Capture;
import org.magictest.client.Report;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.TestHelper.ComparableName;
import org.magicwerk.brownies.collections.TestHelper.Name;
import org.magicwerk.brownies.collections.TestHelper.Ticket;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.Timer;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.slf4j.Logger;

/**
 * Test class of KeyList.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class KeyListTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		test();
	}

	static void test() {
		testClone();
		//testAdd();
		//testRemove();
		//testReorder();

		//testPerformance();
	}

	//	static void testPerformanceSortedList() {
	//	    InstrumentConfig ic = InstrumentConfig.DETAIL;
	//	    TraceConfig tc = new TraceConfig(TraceMode.SELF);
	//	    PrintConfig pc = new PrintConfig();
	//	    pc.setPrintTime(true);
	//
	//		MethodConfig mc = new MethodConfig(ic, tc, pc);
	//		MagicTraceConfig magicTraceConfig = MagicTraceConfig.getEmpty();
	//		magicTraceConfig.addTraceConfig("**.*", mc);
	//		MagicTraceConfig.setInstance(magicTraceConfig);
	//
	//		new ClassInstrumentator().instrumentClass("org.magicwerk.brownies.collections.KeyList");
	//
	//		doTestPerformanceSortedList();
	//
	//	    MagicTrace.getProfileData().printInfo();
	//	}

	static void test2() {
		KeyList<Integer> list = new KeyList.Builder<Integer>().withOrderByElem(true).withListType(int.class).build();
		list.add(0);
		list.add(1);
		list.setAll(1, GapList.create(2));
	}

	static void doTestPerformanceSortedList() {
		// 10'000 -  2.6 s
		// 20'000 - 10.9 s
		Timer t = new Timer();
		KeyList<Integer> list = new KeyList.Builder<Integer>().withElemSort(true).build();
		int num = 50 * 1000;
		for (int i = 0; i < num; i++) {
			//System.out.println(i);
			list.add(i);
		}
		t.printElapsed();
	}

	static String formatTableList(KeyList<?> list) {
		return String.format("Size: %d, Capacity: %d, Content: %s", list.size(), list.capacity(), list);
	}

	public static void testPerformance() {
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		final int size = 1000 * 1000;
		for (int i = 0; i < size; i++) {
			arrayList.add(i);
		}

		KeyList<Integer> keyList = new KeyList.Builder<Integer>().withElemSet().build();
		for (int i = 0; i < size; i++) {
			keyList.add(i);
		}
		Integer remove = size / 2;
		List<Integer> removes = GapList.create(91, 92, 93, 94, 95);

		final int num = 10 * 1000;
		for (List<Integer> list : new List[] { arrayList, keyList }) {
			Timer t = new Timer();
			for (int i = 0; i < num; i++) {
				list.contains(remove);
				//list.remove(remove);
				//list.removeAll(removes);
				//list.retainAll(removes);
			}
			t.printElapsed();
		}
	}

	//@Capture
	public static void testReorder() {
		KeyList<String> list = new KeyList.Builder<String>().withPrimaryElem().build();
		list.addArray("a", "b", "c", "d", "e", "f");
		List<String> strs = list.getAll(1, 4);

		// ok
		KeyList<String> copy = list.copy();
		copy.reverse(1, 4);

		// ok
		copy = list.copy();
		copy.setAll(1, strs);

		// fails
		copy = list.copy();
		try {
			for (int i = 0; i < 4; i++) {
				copy.set(1 + i, strs.get(i));
			}
			CheckTools.error();
		} catch (Exception e) {
		}

		copy = list.copy();
		try {
			copy.setArray(1, "x", "a", "y");
		} catch (Exception e) {
		}
		System.out.println(copy);
	}

	static class TicketList extends KeyList<Ticket> {
		public TicketList() {
			getBuilder().withPrimaryElem().build();
		}
	}

	@Trace(traceClass = "org.magicwerk.brownies.collections.KeyListTest$TicketList", traceMethod = "/.*/", parameters = Trace.ALL_PARAMS
			| Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testExtends() {
		TicketList tc = new TicketList();
		Ticket t1 = new Ticket(1, "extId1", "text1");
		tc.add(t1);
		TicketList tc1 = (TicketList) tc.clone();
		TicketList tc2 = (TicketList) tc.copy();
		TicketList tc3 = (TicketList) tc.crop();
	}

	@Capture
	public static void testBuildFails() {
		try {
			KeyList<String> list = new KeyList.Builder<String>().withMaxSize(2).withWindowSize(2).build();
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	@Trace(traceMethod = "/add|set/", result = Trace.THIS)
	public static void testAsSet() {
		// Add
		KeyList<Name> list = new KeyList.Builder<Name>().withElemNull(true).withElemDuplicates(false).build();
		list.add(new Name("a", 0));
		list.add(new Name("b", 1));
		// No duplicate value allowed (existing is preserved)
		list.add(new Name("a", 2));
		// Only one null value allowed
		list.add(null);
		list.add(null);
		LOG.info("TableList: {}", list);

		// Set
		list.set(0, new Name("a", 2));
		try {
			list.set(1, new Name("a", 3));
		} catch (Exception e) {
		}
		list.set(1, new Name("c", 4));
		list.set(1, new Name("b", 5));

		Set<Name> set = new HashSet<Name>();
		set.add(new Name("a", 0));
		set.add(new Name("b", 1));
		set.add(new Name("a", 2));
		// Only one null value allowed
		set.add(null);
		set.add(null);
		LOG.info("HashSet: {}", set);

		// Add
		KeyList<Name> list2 = new KeyList.Builder<Name>().withElemDuplicates(false).build();
		list2.addIf(new Name("a", 0));
		list2.addIf(new Name("b", 1));
		// No duplicate value allowed (existing is preserved)
		list2.addIf(new Name("a", 2));
		// Only one null value allowed
		list2.addIf(null);
		list2.addIf(null);
		LOG.info("SetList: {}", list2);
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSetWithNoNulls() {
		KeyList<Name> list = new KeyList.Builder<Name>().withElemNull(false).withElemDuplicates(false).build();
		list.add(new Name("a", 0));
		list.add(new Name("b", 1));
		list.add(new Name("a", 2));
		list.add(null);
		LOG.info("TableList: {}", list);
	}

	@Trace(traceMethod = "/.*/", parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testDuplicates() {
		// Check that duplicates are added at the end and removed from the head
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().withElemSort(true).withOrderByElem(true).withElemDuplicates(true).build();
		list.add(new ComparableName("c", 2));
		list.add(new ComparableName("d", 4));
		list.add(new ComparableName("c", 3));
		list.add(new ComparableName("a", 1));

		list.getAll(new ComparableName("c", -1));

		list.remove(new ComparableName("c", -1));
		list.remove(new ComparableName("c", -1));
	}

	@Trace(traceMethod = "/contains|getCount|getAll|getDistinct|removeAll/", parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testElemSet() {
		KeyList<String> list = new KeyList.Builder<String>().build();

		list.add("abc");
		list.add(null);
		list.add("def");
		list.add("abc");

		System.out.println(list.contains("abc"));
		System.out.println(list.getCount("abc"));
		System.out.println(list.getAll("abc"));
		System.out.println(list.getDistinct());
		list.removeAll("abc");
	}

	@Trace(parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testPut() {
		KeyList<ComparableName> list;

		// Sorted, no duplicates
		Report.setDescription("put (sorted, no duplicates)");
		list = new KeyList.Builder<ComparableName>().withElemSort(true).withOrderByElem(true).withElemDuplicates(false).build();
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("c", 3));

		// Replace
		list.put(new ComparableName("b", 4));
		// Add
		list.put(new ComparableName("d", 5));

		// Sorted, duplicates
		Report.setDescription("put (sorted, duplicates)");
		list = new KeyList.Builder<ComparableName>().withElemSort(true).withOrderByElem(true).withElemDuplicates(true).build();
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("c", 3));

		// Add
		list.put(new ComparableName("b", 4));
		list.put(new ComparableName("d", 5));

		// Not sorted, no duplicates
		Report.setDescription("put (not sorted, no duplicates)");
		list = new KeyList.Builder<ComparableName>().withElemSort(true).withElemDuplicates(false).build();
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("c", 3));

		// Replace
		list.put(new ComparableName("b", 4));
		// Add
		list.put(new ComparableName("d", 5));

		// Not sorted, duplicates
		Report.setDescription("put (not sorted, duplicates)");
		list = new KeyList.Builder<ComparableName>().withElemSort(true).withElemDuplicates(true).build();
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("c", 3));

		// Add
		list.put(new ComparableName("b", 4));
		list.put(new ComparableName("d", 5));
	}

	@Trace(parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS)
	public static void testSet() {
		KeyList<ComparableName> list;

		// Sorted, no duplicates
		list = new KeyList.Builder<ComparableName>().withElemSort(true).withOrderByElem(true).withElemDuplicates(false).build();
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("c", 3));

		// OK
		list.set(1, new ComparableName("b", 4));

		// Error
		list.set(0, new ComparableName("b", 4));
		list.set(2, new ComparableName("b", 4));
		list.set(3, new ComparableName("b", 4));

		list.set(3, new ComparableName("c", 5));

		list.set(-1, new ComparableName("x", 9));
		list.set(9, new ComparableName("x", 9));

		// OK
		list.set(1, new ComparableName("aa", 0));

		// Sorted, duplicates
		list = new KeyList.Builder<ComparableName>().withElemSort(true).withOrderByElem(true).withElemDuplicates(true).build();
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("c", 3));
		list.add(new ComparableName("c", 4));
		list.add(new ComparableName("d", 5));
		list.add(new ComparableName("e", 6));

		// Error
		list.set(0, new ComparableName("c", 7));
		list.set(5, new ComparableName("c", 8));

		// OK
		list.set(1, new ComparableName("c", 7));
		list.set(2, new ComparableName("c", 8));
		list.set(3, new ComparableName("c", 9));
		list.set(4, new ComparableName("c", 10));
	}

	@Trace(parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS)
	public static void testAdd() {
		KeyList<ComparableName> list;

		// Sorted, no duplicates
		list = new KeyList.Builder<ComparableName>().withElemSort(true).withOrderByElem(true).withElemDuplicates(false).build();
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("c", 3));

		// Error
		list.add(2, new ComparableName("b", 4));
		list.add(2, new ComparableName("d", 4));

		// OK
		list.add(3, new ComparableName("d", 4));

		// Sorted, duplicates
		list = new KeyList.Builder<ComparableName>().withElemSort(true).withOrderByElem(true).withElemDuplicates(true).build();
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("c", 3));

		list.add(2, new ComparableName("b", 4));
		list.add(1, new ComparableName("b", 5));

		// Error
		list.add(0, new ComparableName("b", 6));
		list.add(2, new ComparableName("x", 9));
	}

	@Trace(traceMethod = "/add|set/", result = Trace.THIS)
	public static void testAsSortedSet() {
		// For a sorted set, the entries must be comparable
		// Add
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().withElemSort(true).withOrderByElem(true).withElemNull(false)
				.withElemDuplicates(false).build();
		list.add(new ComparableName("c", 0));
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("b", 3));
		// Null elements are per default not allowed
		list.add(null);
		LOG.info("TableList: {}", list);
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSortedList() {
		// For a sorted set, the entries must be comparable
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().withElemSort(true).withOrderByElem(true).withElemNull(false)
				.withElemDuplicates(true).build();
		list.add(new ComparableName("c", 0));
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("b", 3));
		// Null elements are not allowed
		list.add(null);
		LOG.info("TableList: {}", list);
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSortedListByComparable() {
		// For a sorted set, the entries must be comparable
		KeyList<Name> list = new KeyList.Builder<Name>().withElemSort(Name.Comparator).withOrderByElem(true).withElemNull(false).build();
		list.add(new Name("c", 0));
		list.add(new Name("a", 1));
		list.add(new Name("b", 2));
		list.add(new Name("b", 3));
		// Null elements are not allowed
		list.add(null);
		LOG.info("TableList: {}", list);
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSortedListWithNull() {
		// For a sorted set, the entries must be comparable
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().withElemNull(true).withElemDuplicates(true).withElemSort(true).build();
		list.add(new ComparableName("c", 0));
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("b", 3));
		list.add(null);
		LOG.info("TableList: {}", list);
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSortedSetWithDuplicates() {
		// For a sorted set, the entries must be comparable
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().withElemSort(true).withOrderByElem(true).withElemNull(false)
				.withElemDuplicates(true).build();
		list.add(new ComparableName("c", 0));
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("b", 3));
		// Null elements are per default not allowed
		list.add(null);
		LOG.info("TableList: {}", list);

		Report.setAutoTrace(false);
		List<ComparableName> alist = new ArrayList<ComparableName>();
		alist.add(new ComparableName("c", 0));
		alist.add(new ComparableName("a", 1));
		alist.add(new ComparableName("b", 2));
		alist.add(new ComparableName("b", 3));
		try {
			// Without null-aware comparator, null values are not allowed
			ArrayList alist2 = new ArrayList(alist);
			alist2.add(null);
			Collections.sort(alist2);
			throw new AssertionError();
		} catch (Exception e) {
		}
		Collections.sort(alist);
		LOG.info("ArrayList: {}", alist);

		//Assert.assertTrue(list.equals(alist));
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSortedSetWithNulls() {
		// For a sorted set, the entries must be comparable
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().withOrderByElem(true).build();
		list.add(new ComparableName("c", 0));
		list.add(new ComparableName("a", 1));
		list.add(null);
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("b", 3));
		list.add(null);
		LOG.info("TableList: {}", list);
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSortedSetWithNullsFirst() {
		// For a sorted set, the entries must be comparable
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().withElemSort(null, true).withOrderByElem(true).build();
		list.add(new ComparableName("c", 0));
		list.add(new ComparableName("a", 1));
		list.add(null);
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("b", 3));
		list.add(null);
		LOG.info("TableList: {}", list);
	}

	// The columns in a table must have a unique key, null is not allowed.
	// If an attempt is made to insert a duplicate, an error should be raised.
	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsPrimaryKey() {
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().withPrimaryElem().build();
		list.add(new ComparableName("c", 0));
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("b", 2));
		// Error: duplicates are not allowed
		list.add(new ComparableName("b", 3));
		// Null elements are not allowed
		list.add(null);
		LOG.info("TableList: {}", list);
	}

	// The columns in a table must have a unique key, null is not allowed.
	// If an attempt is made to insert a duplicate, an error should be raised.
	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsUniqueKey() {
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().withUniqueElem().build();
		list.add(new ComparableName("c", 0));
		list.add(null);
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("b", 2));
		// Error: duplicates are not allowed
		list.add(new ComparableName("b", 3));
		list.add(null);
		LOG.info("TableList: {}", list);
	}

	//

	public static void testTableListSorted2() {
		KeyList<Name> list = new KeyList.Builder<Name>().withElemSort(true).build();
		//java.lang.ClassCastException: org.magicwerk.brownies.collections.KeyListTest$Name cannot be cast to java.lang.Comparable
		//list.add(new Name("d"));
		list.add(new ComparableName("d"));
		list.add(new ComparableName("d"));
		list.add(new ComparableName("b"));
		list.add(new ComparableName("a"));
		System.out.println(list);
		list.set(1, new ComparableName("ax"));
		System.out.println(list);
		list.remove(1);
		System.out.println(list);
	}

	static void testTableListCopy() {
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().build();
		list.add(new ComparableName("a"));
		list.add(new ComparableName("b"));
		list.add(new ComparableName("c"));
		System.out.println(list);

		KeyList<ComparableName> copy = list.copy();
		System.out.println(copy);
		KeyList<ComparableName> crop = list.crop();
		System.out.println(crop);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testRemove() {
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().build();
		list.add(new ComparableName("a", 0));
		list.add(new ComparableName("b", 1));

		list.remove(1);
		list.remove(0);

		list.add(new ComparableName("a", 0));
		list.add(new ComparableName("b", 1));
		list.add(new ComparableName("c", 2));
		list.add(new ComparableName("d", 3));

		list.remove(1, 2);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testClear() {
		{
			KeyList<Integer> list = new KeyList.Builder<Integer>().withOrderByElem(int.class).build();
			list.add(1);
			list.add(2);
			list.clear();
			Assert.assertTrue(list.size() == 0);
		}
		{
			KeyList<Integer> list = new KeyList.Builder<Integer>().build();
			list.add(1);
			list.add(2);
			list.clear();
			Assert.assertTrue(list.size() == 0);
		}
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testClone() {
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().build();
		list.add(new ComparableName("d", 0));
		list.add(new ComparableName("d", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 3));
		list.clone();

		list = new KeyList.Builder<ComparableName>().withOrderByElem(true).build();
		list.add(new ComparableName("d", 0));
		list.add(new ComparableName("d", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 3));
		list.clone();

		KeyList<ComparableName> list2 = ReflectTools.cloneDeep(list);
		Assert.assertTrue(list2.equals(list));
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testCopy() {
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().build();
		list.add(new ComparableName("d", 0));
		list.add(new ComparableName("d", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 3));
		list.copy();

		list = new KeyList.Builder<ComparableName>().withOrderByElem(true).build();
		list.add(new ComparableName("d", 0));
		list.add(new ComparableName("d", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 3));
		list.copy();
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testCrop() {
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().build();
		list.add(new ComparableName("d", 0));
		list.add(new ComparableName("d", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 3));
		list.crop();

		list = new KeyList.Builder<ComparableName>().withOrderByElem(true).build();
		list.add(new ComparableName("d", 0));
		list.add(new ComparableName("d", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 3));
		list.crop();
	}

	@Trace(traceMethod = "/.+/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testKey() {
		ComparableName a = new ComparableName("a");
		ComparableName d = new ComparableName("d");
		ComparableName x = new ComparableName("x");
		ComparableName n = null;
		KeyList<ComparableName> list;

		list = getList();
		list.getAll(d);
		list.getCount(d);
		list.getDistinct();

		list.getAll(x);
		list.getCount(x);
		list.remove(x);
		list.removeAll(x);

		list.getAll(n);
		list.getCount(n);
		list.remove(n);
		list.removeAll(n);

		list.remove(a);
		list.removeAll(d);

		list.remove(a);
		list.removeAll(d);

		list = getListSort();
		list.getAll(d);
		list.getCount(d);
		list.getDistinct();

		list.getAll(x);
		list.getCount(x);
		list.remove(x);
		list.removeAll(x);

		list.getAll(n);
		list.getCount(n);
		list.remove(n);
		list.removeAll(n);

		list.remove(a);
		list.removeAll(d);

		// Null
		list = getListNull();
		list.getAll(n);
		list.getCount(n);

		list.remove(n);
		list.removeAll(n);

		list = getListNullSort();
		list.getAll(n);
		list.getCount(n);

		list.remove(n);
		list.removeAll(n);
	}

	static KeyList<ComparableName> getList() {
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().withElemDuplicates(true).build();
		list.add(new ComparableName("d"));
		list.add(new ComparableName("b"));
		list.add(new ComparableName("a"));
		list.add(new ComparableName("d", 1));
		return list;
	}

	static KeyList<ComparableName> getListSort() {
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().withElemDuplicates(true).withOrderByElem(true).build();
		list.add(new ComparableName("d"));
		list.add(new ComparableName("b"));
		list.add(new ComparableName("a"));
		list.add(new ComparableName("d", 1));
		return list;
	}

	static KeyList<ComparableName> getListNull() {
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().withElemNull(true).withElemDuplicates(true).build();
		list.add(new ComparableName("d"));
		list.add(null);
		list.add(new ComparableName("b"));
		list.add(new ComparableName("a"));
		list.add(null);
		list.add(new ComparableName("d", 1));
		return list;
	}

	static KeyList<ComparableName> getListNullSort() {
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().withElemNull(true).withElemDuplicates(true).withOrderByElem(true).build();
		list.add(new ComparableName("d"));
		list.add(null);
		list.add(new ComparableName("b"));
		list.add(new ComparableName("a"));
		list.add(null);
		list.add(new ComparableName("d"));
		return list;
	}

	//--

	static void testSortedList3() {
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().build();
		list.add(null);
		list.add(new ComparableName("b"));
		list.add(new ComparableName("a"));

		list.set(1, new ComparableName("c"));

		System.out.println(list);
	}

	static void testSortedList4() {
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().build();
		list.add(new ComparableName("d"));
		list.add(new ComparableName("b"));
		list.add(new ComparableName("a"));
		list.add(new ComparableName("a"));

		System.out.println(list);
	}

	static void testSortedList2() {
		KeyList<ComparableName> list = new KeyList.Builder<ComparableName>().withElemSort(null, true).build();
		list.add(null);
		list.add(new ComparableName("b"));
		list.add(new ComparableName("a"));

		System.out.println(list);
	}

}
