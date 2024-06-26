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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.magictest.client.Assert;
import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.TestHelper.Ticket;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.objects.Pair;
import org.magicwerk.brownies.core.strings.StringPrinter;
import org.slf4j.Logger;

/**
 * Test of Key2List.
 *
 * @author Thomas Mauch
 */
public class Key2ListTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		test();
	}

	static void test() {
		testRemoveAllByKey1();
		//testAsMap();
		//testInvalidate();
		//testKey2List();
	}

	static class TicketList extends Key2List<Ticket, Integer, String> {
		public TicketList() {
			getBuilder().withPrimaryKey1Map(Ticket.IdMapper).withUniqueKey2Map(Ticket.ExtIdMapper).build();
		}
	}

	@Trace
	public static void testRemoveAllByKey1() {
		Key2List<Pair<String>, String, String> pairs = new Key2List.Builder<Pair<String>, String, String>()
				.withKey1Map(Pair::getFirst).withKey2Map(Pair::getLast).build();
		pairs.add(Pair.of("A", "B1"));
		pairs.add(Pair.of("A", "B2"));
		pairs.add(Pair.of("B1", "B2"));
		check(pairs);

		pairs.removeAllByKey1("A");
		check(pairs);
	}

	static <E> void check(KeyListImpl<E> list) {
		IList<E> elems = GapList.create(list);

		int numKeys = list.keyColl.keyMaps.length;
		for (int i = 1; i < numKeys; i++) {
			IList<E> e1s = GapList.create();
			@SuppressWarnings("unchecked")
			Set<Object> k1s = (Set<Object>) list.getDistinctKeys(i);
			for (Object k1 : k1s) {
				e1s.addAll(list.getAllByKey(i, k1));
			}
			CheckTools.check(e1s.size() == elems.size());
			e1s.removeAll(elems);
			CheckTools.check(e1s.isEmpty());
		}
	}

	@Trace(traceClass = "org.magicwerk.brownies.collections.Key2ListTest$TicketList", traceMethod = "/.*/", parameters = Trace.ALL_PARAMS
			| Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testExtends() {
		TicketList tc = new TicketList();
		Ticket t1 = new Ticket(1, "extId1", "text1");
		tc.add(t1);
		TicketList tc1 = (TicketList) tc.clone();
		TicketList tc2 = (TicketList) tc.copy();
		TicketList tc3 = (TicketList) tc.crop();
	}

	@Trace(traceMethod = "/.*/", parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testInvalidate() {
		{
			Key2List<Ticket, Integer, String> list = new Key2List.Builder<Ticket, Integer, String>().withKey1Map(Ticket.IdMapper).withKey1Duplicates(false)
					.withKey1Sort(true).withKey2Map(Ticket.ExtIdMapper).withKey2Duplicates(true).withKey2Sort(true).build();
			Ticket t1 = new Ticket(1, "extId1", "text1");
			Ticket t2 = new Ticket(2, "extId2", "text2");
			Ticket t3 = new Ticket(3, "extId3x", "text3");
			Ticket t4 = new Ticket(4, "extId3x", "text4");
			list.add(t1);
			list.add(t3);
			list.add(t2);
			list.add(t4);

			t1.id = 11;
			list.getByKey1(11);
			list.invalidate(t1);
			list.getByKey1(11);

			t2.id = 22;
			list.getByKey1(22);
			list.invalidateKey1(2, 22, null);
			list.getByKey1(22);

			t3.extId = "extId3a";
			list.getByKey2("extId3a");
			list.invalidateKey2("extId3x", "extId3a", t3);
			list.getByKey2("extId3a");
		}
		{
			Key2List<Ticket, Integer, String> list = new Key2List.Builder<Ticket, Integer, String>().withKey1Map(Ticket.IdMapper).withKey1Duplicates(false)
					.withKey1Sort(true).withOrderByKey1(true).withKey2Map(Ticket.ExtIdMapper).withKey2Duplicates(true).withKey2Sort(true).build();
			Ticket t = null;
			Ticket t1 = new Ticket(1, "extId1", "text1");
			Ticket t2 = new Ticket(2, "extId2", "text2");
			Ticket t3 = new Ticket(3, "extId3x", "text3");
			Ticket t4 = new Ticket(4, "extId3x", "text4");
			list.add(t1);
			list.add(t3);
			list.add(t2);
			list.add(t4);

			t1.id = 11;
			list.getByKey1(11);
			list.invalidate(t1);
			list.getByKey1(11);

			t2.id = 22;
			list.getByKey1(22);
			list.invalidateKey1(2, 22, null);
			t = list.getByKey1(22);
			Assert.assertTrue(t.getId() == 22);

			t3.extId = "extId3a";
			list.getByKey2("extId3a");
			list.invalidateKey2("extId3x", "extId3a", t3);
			list.getByKey2("extId3a");
		}

	}

	@Trace(traceMethod = "/.+/", parameters = Trace.THIS | Trace.ALL_PARAMS)
	public static void testKey2List() {

		Key2List<Ticket, Integer, String> tickets = new Key2List.Builder<Ticket, Integer, String>().withNull(false).withConstraint(Ticket.Constraint)
				.withPrimaryKey1Map(Ticket.IdMapper).withUniqueKey2Map(Ticket.ExtIdMapper).build();
		Ticket t1 = new Ticket(1, "ExtId1", "Ticket1");
		Ticket t2 = new Ticket(2, null, "Ticket2");
		Ticket t3 = new Ticket(3, "ExtId3", "Ticket3");
		Ticket t4 = new Ticket(4, null, "Ticket4");
		tickets.add(t1);
		tickets.add(t2);
		tickets.add(t3);
		tickets.add(t4);
		LOG.info("Tickets: {}", tickets);

		Ticket t = tickets.getByKey1(1);
		LOG.info("Ticket: {}", t);
		t = tickets.getByKey2("ExtId3");
		LOG.info("Ticket: {}", t);

		// Exceptions
		tickets.add(new Ticket(1, "ExtId", "ERROR"));
		tickets.add(new Ticket(5, "ExtId3", "ERROR"));
		tickets.add(new Ticket(null, "ExtId", "ERROR"));
		tickets.add(null);
		tickets.add(new Ticket(6, "EXT", "ERROR"));

		tickets.removeByKey1(1);
	}

	static String format(List<Ticket> tickets) {
		return StringPrinter.formatArray(tickets.stream().map(t -> t.id));
	}

	@Capture
	public static void testCloneCopyCrop() {
		Key2List<Ticket, Integer, String> tickets1 = new Key2List.Builder<Ticket, Integer, String>().withNull(false).withConstraint(Ticket.Constraint)
				.withPrimaryKey1Map(Ticket.IdMapper).withUniqueKey2Map(Ticket.ExtIdMapper).build();
		doTestCloneCopyCrop(tickets1);

		Key2List<Ticket, Integer, String> tickets2 = new Key2List.Builder<Ticket, Integer, String>().withNull(false).withConstraint(Ticket.Constraint)
				.withOrderByElem(true).withPrimaryKey1Map(Ticket.IdMapper).withUniqueKey2Map(Ticket.ExtIdMapper).build();
		doTestCloneCopyCrop(tickets2);
	}

	static void doTestCloneCopyCrop(Key2List<Ticket, Integer, String> tickets) {
		Ticket t1 = new Ticket(1, "ExtId1", "Ticket1");
		Ticket t2 = new Ticket(2, null, "Ticket2");
		Ticket t3 = new Ticket(3, "ExtId3", "Ticket3");
		Ticket t4 = new Ticket(4, null, "Ticket4");
		Ticket t5 = new Ticket(5, null, "Ticket5");
		tickets.add(t1);
		tickets.add(t2);
		tickets.add(t3);
		tickets.add(t4);
		System.out.println("Orig: " + format(tickets));

		//-- Clone
		Key2List<Ticket, Integer, String> tickets2 = tickets.clone();
		System.out.println("-\nClone: " + format(tickets2));

		// Change original
		tickets.add(t5);
		System.out.println("Orig (changed): " + format(tickets));
		System.out.println("Clone (unchanged): " + format(tickets2));

		// Change clone
		tickets.remove(t5);
		tickets2.add(t5);
		System.out.println("Orig (unchanged): " + format(tickets));
		System.out.println("Clone (changed): " + format(tickets2));

		//        System.out.println(getIdentityHashCode(tickets2));
		//        System.out.println(getIdentityHashCode(getAnyBeanValues(tickets2, "tableColl")));
		//        System.out.println(getIdentityHashCode(getAnyBeanValues(tickets2, "tableColl", "keyMaps[1]", "keysMap")));
		//        System.out.println(getIdentityHashCode(getAnyBeanValues(tickets2, "tableColl", "keyMaps[2]", "keysMap")));
		//        Assert.assertTrue(tickets2.equals(tickets));

		//-- Copy
		Key2List<Ticket, Integer, String> tickets3 = tickets.copy();
		System.out.println("-\nCopy: " + format(tickets3));
		Assert.assertTrue(tickets3.getClass() == tickets.getClass());
		Assert.assertTrue(tickets3.equals(tickets));

		// Change original
		tickets.add(t5);
		System.out.println("Orig (changed): " + format(tickets));
		System.out.println("Copy (unchanged): " + format(tickets3));

		// Change copy
		tickets.remove(t5);
		tickets3.add(t5);
		System.out.println("Orig (unchanged): " + format(tickets));
		System.out.println("Copy (changed): " + format(tickets3));

		//-- Crop
		Key2List<Ticket, Integer, String> tickets4 = tickets.crop();
		Assert.assertTrue(tickets4.getClass() == tickets.getClass());
		System.out.println("-\nCrop: " + format(tickets4));

		// Change original
		tickets.add(t5);
		System.out.println("Orig (changed): " + format(tickets));
		System.out.println("Crop (unchanged): " + format(tickets4));

		// Change crop
		tickets.remove(t5);
		tickets4.add(t5);
		System.out.println("Orig (unchanged): " + format(tickets));
		System.out.println("Crop (changed): " + format(tickets4));
	}

	@Capture
	public static void testAsMap() {
		Ticket t1 = new Ticket(1, "extId1", "text1");
		Ticket t2 = new Ticket(2, "extId2", "text2");
		Ticket t3 = new Ticket(3, "extId3", "text3");
		Ticket t4 = new Ticket(4, "extId4", "text4");

		Key2List<Ticket, Integer, String> init = new Key2List.Builder<Ticket, Integer, String>().withPrimaryKey1Map(Ticket.IdMapper)
				.withPrimaryKey2Map(Ticket.ExtIdMapper).build();

		// asMap1
		{
			Key2List<Ticket, Integer, String> list = init.copy();
			list.add(t1);
			list.add(t3);
			list.add(t2);
			list.add(t4);

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

		// asMap2
		{
			Key2List<Ticket, Integer, String> list = init.copy();
			list.add(t1);
			list.add(t3);
			list.add(t2);
			list.add(t4);

			Map<String, Ticket> map = list.asMap2();
			System.out.println(map);
			map.remove("extId2");
			System.out.println(map);
			Ticket t = map.put("extId2", t2);
			Assert.assertTrue(t == null);
			System.out.println(map);
			t = map.put("extId2", t2);
			Assert.assertTrue(t == t2);
			System.out.println(map);
			System.out.println(map.keySet());
			System.out.println(map.values());
			System.out.println(map.entrySet());
		}
	}

}
