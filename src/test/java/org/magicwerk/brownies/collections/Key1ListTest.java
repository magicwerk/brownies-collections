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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.magictest.client.Assert;
import org.magictest.client.Capture;
import org.magictest.client.Report;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.TestHelper.ComparableName;
import org.magicwerk.brownies.collections.TestHelper.Name;
import org.magicwerk.brownies.collections.TestHelper.Ticket;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.TypeTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.core.strings.StringFormatter;
import org.slf4j.Logger;

/**
 * Test of Key1List.
 *
 * @author Thomas Mauch
 */
public class Key1ListTest {
	/** Logger */
	private static Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		test();
	}

	static void test() {
		//testImmutableList();
		//testInitAll();
		//testContains();
		//testRemoveAllByKey1();
		testMemorySize();
		//testPut();
		//testAsMap();
		//testKeys();
		//testKeys();
		//testKeys();
		//testMove();
	}

	@Trace(traceMethod = "/.*/", parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testKey1List() {
		Key1List<Ticket, Integer> list = new Key1List.Builder<Ticket, Integer>().withKey1Map(Ticket.IdMapper).build();
		list.add(t2);
		list.add(null);
		list.add(t1);
		list.getDistinctKeys1();

		Comparator<Integer> comp = new Comparator<Integer>() {
			final boolean nullsFirst = false;

			@Override
			public int compare(Integer key1, Integer key2) {
				if (key1 != null && key2 != null) {
					return key1.compareTo(key2);
				}
				if (key1 == null) {
					if (key2 == null) {
						return 0;
					} else {
						return nullsFirst ? -1 : 1;
					}
				} else {
					assert (key2 == null);
					return nullsFirst ? 1 : -1;
				}
			}
		};

		list = new Key1List.Builder<Ticket, Integer>().withKey1Map(Ticket.IdMapper).withKey1Sort(comp).build();
		list.add(t2);
		list.add(null);
		list.add(t1);
		list.getDistinctKeys1();

		list = new Key1List.Builder<Ticket, Integer>().withKey1Map(Ticket.IdMapper).withKey1Sort(null).withOrderByKey1(true).build();
		list.add(t2);
		list.add(null);
		list.add(t1);
		list.getDistinctKeys1();
	}

	@Trace(parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testPut() {
		Key1List<Name, String> list;
		list = new Key1List.Builder<Name, String>().withElemSet().withKey1Map(Name.Mapper).build();
		list.put(new Name("a", 1));
		list.put(new Name("b", 2));
		list.put(new Name("a", 3));

		list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).build();
		list.put(new Name("a", 1));
		list.put(new Name("b", 2));
		list.put(new Name("a", 3));
	}

	@Trace(parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testRemoveAllByKey1() {
		Key1List<Name, String> list;

		list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Sort(true).withOrderByKey1(true).withKey1Duplicates(true).build();
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("c", 3));
		list.add(new ComparableName("b", 4));

		IList<Name> l = list.removeAllByKey1("b");
		LOG.info("{}", l);
	}

	@Trace(parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testContains() {
		ComparableName elem = new ComparableName("a", 0);
		Key1List<Name, String> list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Sort(true).withOrderByKey1(true)
				.withKey1Duplicates(true).build();
		list.add(elem);
		list.contains(elem);
	}

	public static void testMove() {
		Key1List<Name, String> list;

		list = new Key1List.Builder<Name, String>().withPrimaryKey1Map(Name.Mapper).build();
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("c", 3));

		//Name e = list.remove(2);
		//list.add(1, e);

		list.drag(2, 1, 1);
	}

	@Trace(parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testInitAll() {
		IList<ComparableName> add = GapList.create(new ComparableName("b", 2), new ComparableName("a", 1));

		Key1List<Name, String> list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Sort(true).withOrderByKey1(true)
				.build();
		// addAll() works if elements to add are not sorted
		list.addAll(add);
		// initAll() works if elements to add are not sorted
		list.initAll(add);
	}

	@Trace(parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testPutByKey1() {
		Key1List<Name, String> list;

		// Sorted, no duplicates
		Report.setDescription("put (sorted, no duplicates)");
		list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Sort(true).withOrderByKey1(true).withKey1Duplicates(false).build();
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("c", 3));

		// Replace
		list.putByKey1(new ComparableName("b", 4));
		// Add
		list.putByKey1(new ComparableName("d", 5));

		// Sorted, duplicates
		Report.setDescription("put (sorted, duplicates)");
		list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Sort(true).withOrderByKey1(true).withKey1Duplicates(true).build();
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("c", 3));

		// Add
		list.putByKey1(new ComparableName("b", 4));
		list.putByKey1(new ComparableName("d", 5));

		// Not sorted, no duplicates
		Report.setDescription("put (not sorted, no duplicates)");
		list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Sort(true).withKey1Duplicates(false).build();
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("c", 3));

		// Replace
		list.putByKey1(new ComparableName("b", 4));
		// Add
		list.putByKey1(new ComparableName("d", 5));

		// Not sorted, duplicates
		Report.setDescription("put (not sorted, duplicates)");
		list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Sort(true).withKey1Duplicates(true).build();
		list.add(new ComparableName("b", 2));
		list.add(new ComparableName("a", 1));
		list.add(new ComparableName("c", 3));

		// Add
		list.putByKey1(new ComparableName("b", 4));
		list.putByKey1(new ComparableName("d", 5));
	}

	static Ticket t1 = new Ticket(1, "extId1", "text1");
	static Ticket t2 = new Ticket(2, "extId2", "text2");
	static Ticket t3 = new Ticket(3, "extId3", "text3");
	static List<Ticket> ts = GapList.create(t1, t2, t3);

	static void testMemorySize() {
		int size = 100 * 1000;

		StringFormatter.println("Size = " + size);

		IList<Ticket> gapList = GapList.create();
		for (int i = 0; i < size; i++) {
			Ticket t = new Ticket(i, null, null);
			gapList.add(t);
		}
		StringFormatter.println("GapList = {}", TypeTools.formatGrouped(ReflectTools.getObjectSize(gapList)));

		{
			List<Ticket> list = new ArrayList<>(gapList);
			StringFormatter.println("ArrayList = {}", TypeTools.formatGrouped(ReflectTools.getObjectSize(list)));
		}
		{
			List<Ticket> list = new LinkedList<>(gapList);
			StringFormatter.println("LinkedList = {}", TypeTools.formatGrouped(ReflectTools.getObjectSize(list)));
		}
		{
			Key1List<Ticket, Integer> key1List = new Key1List.Builder<Ticket, Integer>().withPrimaryKey1Map(Ticket.IdMapper).build();
			key1List.addAll(gapList);
			StringFormatter.println("Key1List = {}", TypeTools.formatGrouped(ReflectTools.getObjectSize(key1List)));
		}
		{
			Map<Integer, Ticket> map = new HashMap<>();
			gapList.forEach(t -> map.put(t.getId(), t));
			StringFormatter.println("HashMap = {}", TypeTools.formatGrouped(ReflectTools.getObjectSize(map)));
		}
		{
			Map<Integer, Ticket> map = new TreeMap<>();
			gapList.forEach(t -> map.put(t.getId(), t));
			StringFormatter.println("TreeMap = {}", TypeTools.formatGrouped(ReflectTools.getObjectSize(map)));
		}
		{
			Map<Integer, Ticket> map = new LinkedHashMap<>();
			gapList.forEach(t -> map.put(t.getId(), t));
			StringFormatter.println("LinkedHashMap = {}", TypeTools.formatGrouped(ReflectTools.getObjectSize(map)));
		}

		//		{
		//			Key1List<Ticket, Integer> key1List = new Key1List.Builder<Ticket, Integer>().withPrimaryKey1Map(Ticket.IdMapper).build();
		//			key1List.addAll(gapList);
		//			Map<Integer, Ticket> key1ListMap = key1List.asMap1();
		//			StringFormatter.println("Key1List = {}", TypeTools.formatGrouped(ReflectTools.getObjectSize(key1ListMap)));
		//		}
		//		{
		//			Key1List<Ticket, Integer> key1List = new Key1List.Builder<Ticket, Integer>().withPrimaryKey1Map(Ticket.IdMapper).build();
		//			Map<Integer, Ticket> key1ListMap = key1List.asMap1();
		//			gapList.forEach(t -> key1ListMap.put(t.getId(), t));
		//			StringFormatter.println("Key1List = {}", TypeTools.formatGrouped(ReflectTools.getObjectSize(key1ListMap)));
		//		}
	}

	@Capture
	public static void testAsMap() {
		Key1List<Ticket, Integer> list = new Key1List.Builder<Ticket, Integer>().withPrimaryKey1Map(Ticket.IdMapper).build();
		list.addAll(ts);
		Map<Integer, Ticket> map = list.asMap1();
		System.out.println(map);
		map.remove(2);
		System.out.println(map);
		Ticket t = map.put(2, t2);
		Assert.assertTrue(t == null);
		System.out.println(map);
		t = map.put(2, t2);
		Assert.assertTrue(t == t2);
		System.out.println(map);
		System.out.println(map.keySet());
		System.out.println(map.values());
		System.out.println(map.entrySet());
	}

	//    public static void testInvalidate() {
	//        {
	//        Table1List<Name, String> list = new Table1List.Builder<Name, String>(mapper).build();
	//        list.add(new ComparableName("c", 0));
	//        ComparableName b1 = new ComparableName("b", 1);
	//        list.add(b1);
	//        list.add(new ComparableName("a", 2));
	//
	//        LOG.info("{}", list);
	//        LOG.info("{}", list.getDistinctKeys());
	//        b1.setName("d");
	//        LOG.info("{}", list);
	//        //LOG.info("{}", list.getDistinctKeys());
	//        list.invalidate(b1);
	//        LOG.info("{}", list.getDistinctKeys());
	//        }
	//        {
	//        Table1List<Name, String> list = new Table1List.Builder<Name, String>().withKey(Name.Mapper).withKeySort(true).build();
	//        list.add(new ComparableName("c", 0));
	//        ComparableName b1 = new ComparableName("b", 1);
	//        list.add(b1);
	//        list.add(new ComparableName("a", 2));
	//
	//        LOG.info("{}", list);
	//        b1.setName("d");
	//        LOG.info("{}", list);
	//        LOG.info("{}", list.getDistinctKeys());
	//        list.invalidate(b1);
	//        LOG.info("{}", list);
	//        LOG.info("{}", list.getDistinctKeys());
	//        }
	//    }

	@Trace(traceMethod = "/.+/", parameters = Trace.THIS | Trace.ALL_PARAMS)
	public static void testKeys() {
		{
			// Unsorted list
			Report.setAutoTrace(false);
			Key1List<Name, String> list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Duplicates(true).build();
			list.add(new ComparableName("c", 0));
			list.add(new ComparableName("b", 1));
			list.add(new ComparableName("a", 2));
			Report.setAutoTrace(true);

			list.getDistinctKeys1();
			list.getByKey1("b");
			list.getAllByKey1("b");
			list.getCountByKey1("b");
			list.containsKey1("b");
			list.indexOfKey1("b");

			Report.setAutoTrace(false);
			list.add(new ComparableName("b", 3));
			Report.setAutoTrace(true);

			list.getDistinctKeys1();
			list.getByKey1("b");
			list.getAllByKey1("b");
			list.getCountByKey1("b");
			list.containsKey1("b");
			list.indexOfKey1("b");

			Report.setAutoTrace(false);
			list.set(0, new ComparableName("b", 3));
			Report.setAutoTrace(true);

			list.indexOfKey1("b");
		}
		{
			// Sorted list
			Report.setAutoTrace(false);
			Key1List<Name, String> list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Duplicates(true).withOrderByKey1(true).build();
			list.add(new ComparableName("c", 0));
			list.add(new ComparableName("b", 1));
			list.add(new ComparableName("a", 2));
			Report.setAutoTrace(true);

			list.getDistinctKeys1();
			list.getByKey1("b");
			list.getAllByKey1("b");
			list.getCountByKey1("b");
			list.containsKey1("b");
			list.indexOfKey1("b");

			Report.setAutoTrace(false);
			list.add(new ComparableName("b", 3));
			Report.setAutoTrace(true);

			list.getDistinctKeys1();
			list.getByKey1("b");
			list.getAllByKey1("b");
			list.getCountByKey1("b");
			list.containsKey1("b");
			list.indexOfKey1("b");

			Report.setAutoTrace(false);
			list.set(0, new ComparableName("b", 3));
			Report.setAutoTrace(true);

			list.indexOfKey1("b");
		}
	}

	@Trace(traceMethod = "/remove.*|getCountByKey/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testRemove() {
		Key1List<Name, String> list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Duplicates(true).build();
		list.add(new ComparableName("c", 0));
		list.add(new ComparableName("b", 1));
		list.add(new ComparableName("a", 2));
		list.set(0, new ComparableName("b", 3));

		list.getCountByKey1("b");
		list.removeByKey1("b");
		list.getCountByKey1("b");
		list.add(0, new ComparableName("b", 3));
		list.removeAllByKey1("b");
		list.getCountByKey1("b");

		list.remove(0);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testClone() {
		Key1List<Name, String> list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Duplicates(true).build();
		list.add(new ComparableName("a", 0));
		list.add(new ComparableName("b", 1));

		Key1List<Name, String> clone = (Key1List<Name, String>) list.clone();
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testClear() {
		Key1List<Name, String> list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Duplicates(true).build();
		list.add(new ComparableName("a", 0));
		list.add(new ComparableName("b", 1));

		list.clear();
		list.removeAllByKey1("a");
	}

	static void testMapListNullElem() {
		Key1List<Name, String> list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Null(true).build();
		list.add(new Name("a"));
		list.add(new Name("b"));
		list.add(new Name("c"));
		list.add(new Name("b"));
		list.add(new Name(null));
		list.add(null);

		System.out.println(list);
		System.out.println(list.getByKey1("b"));
		System.out.println(list.getAllByKey1("b"));

		list.removeByKey1("b");
		System.out.println(list);
	}

	static void testMapListNullElemDuplicates() {
		Key1List<Name, String> list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Null(true).withKey1Duplicates(true).build();
		list.add(new Name("a"));
		list.add(new Name("b"));
		list.add(new Name("c"));
		list.add(new Name("b"));
		list.add(new Name(null));
		list.add(null);

		System.out.println(list);
		System.out.println(list.getByKey1("b"));
		System.out.println(list.getAllByKey1("b"));

		list.removeByKey1("b");
		System.out.println(list);

		System.out.println(list.getByKey1(null));
		System.out.println(list.getAllByKey1(null));
	}

	@Capture
	public static void testImmutableList() {
		Key1List<Name, String> list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).build();
		list.add(new Name("a"));
		list.add(new Name("b"));
		list.add(new Name("c"));
		Key1List<Name, String> list2 = list.unmodifiableList();

		checkThrow(() -> list2.remove(0));
		checkThrow(() -> list2.removeByKey1("b"));
		checkThrow(() -> list2.removeAllByKey1("b"));
		checkThrow(() -> list2.putByKey1(new Name("d")));
		checkThrow(() -> list2.putIfAbsentByKey1(new Name("e")));
		checkThrow(() -> list2.invalidateKey1("a", "a", null));
	}

	static void checkThrow(Runnable runnable) {
		String err = null;
		try {
			runnable.run();
			assert (false);
		} catch (UnsupportedOperationException e) {
			err = e.getMessage();
		}
		CheckTools.check("list is immutable".equals(err));
	}

	static class TicketList extends Key1List<Ticket, Integer> {
		public TicketList() {
			getBuilder().withPrimaryKey1Map(Ticket.IdMapper).build();
		}
	}

	@Trace(traceClass = "org.magicwerk.brownies.collections.Key1ListTest$TicketList", traceMethod = "/.*/", parameters = Trace.ALL_PARAMS
			| Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testExtends() {
		TicketList tc = new TicketList();
		tc.add(t1);
		TicketList tc1 = (TicketList) tc.clone();
		TicketList tc2 = (TicketList) tc.copy();
		TicketList tc3 = (TicketList) tc.crop();
	}

	@Capture
	public static void testBuildFails() {
		try {
			Key1List<Name, String> list = new Key1List.Builder<Name, String>().build();
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}

		try {
			Key1List<Name, String> list = new Key1List.Builder<Name, String>().withKey1Map(Name.Mapper).withKey1Map(Name.Mapper).build();
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}

		try {
			Key1List<Name, String> list = new Key1List.Builder<Name, String>().withOrderByElem(true).withOrderByKey1(true).build();
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

}
