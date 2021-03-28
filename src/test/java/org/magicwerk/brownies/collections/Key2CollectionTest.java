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

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

import org.magictest.client.Assert;
import org.magictest.client.Capture;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.PrintTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.slf4j.Logger;

/**
 * Test of Key2Collection.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class Key2CollectionTest extends TestHelper {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		test();
	}

	static void test() {
		//testKey2Collection();
		testTicketCollection();
	}

	@SuppressWarnings("serial")
	static class TicketCollection extends Key2Collection<Ticket, Integer, String> {
		public TicketCollection() {
			getBuilder().withKey1Map(Ticket.IdMapper).withKey1Duplicates(false).withKey1Sort(true).withKey2Map(Ticket.ExtIdMapper).withKey2Duplicates(true)
					.withKey2Sort(true).build();
		}
	}

	static void testTicketCollection() {
		TicketCollection tc = new TicketCollection();
		Ticket t1 = new Ticket(1, "extId1", "text1");
		tc.add(t1);

		tc.getByKey1(1);
		tc.getByKey2("extId1");

		System.out.println(tc);
	}

	@Trace(traceMethod = "/.*/", parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testInvalidate() {
		Key2Collection<Ticket, Integer, String> coll = new Key2Collection.Builder<Ticket, Integer, String>().withKey1Map(Ticket.IdMapper)
				.withKey1Duplicates(false).withKey1Sort(true).withKey2Map(Ticket.ExtIdMapper).withKey2Duplicates(true).withKey2Sort(true).build();
		Ticket t1 = new Ticket(1, "extId1", "text1");
		Ticket t2 = new Ticket(2, "extId2", "text2");
		Ticket t3 = new Ticket(3, "extId3x", "text3");
		Ticket t4 = new Ticket(4, "extId3x", "text4");
		coll.add(t1);
		coll.add(t2);
		coll.add(t3);
		coll.add(t4);

		t1.id = 11;
		coll.getByKey1(11);
		coll.invalidate(t1);
		coll.getByKey1(11);

		t2.id = 22;
		coll.getByKey1(22);
		coll.invalidateKey1(2, 22, null);
		coll.getByKey1(22);

		t3.extId = "extId3a";
		coll.getByKey2("extId3a");
		coll.invalidateKey2("extId3x", "extId3a", t3);
		coll.getByKey2("extId3a");
	}

	@Trace(traceMethod = "/.*/", parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testKey2Collection() {
		Key2Collection<Ticket, Integer, String> coll = new Key2Collection.Builder<Ticket, Integer, String>().withKey1Map(Ticket.IdMapper).withKey1Null(false)
				.withKey1Duplicates(false).withKey2Map(Ticket.ExtIdMapper).withKey2Duplicates(false, true).build();
		Ticket t1 = new Ticket(1, "extId1", "text1");
		Ticket t2 = new Ticket(2, null, "text2");
		Ticket t3 = new Ticket(3, null, "text3");
		coll.add(t1);
		coll.add(t2);
		coll.add(t3);

		coll.getByKey1(1);
		coll.getByKey1(null);
		coll.getByKey1(9);

		coll.getByKey2("extId1");
		coll.getByKey2(null);
		coll.getByKey2("x");

		coll.contains(t1);
		coll.contains(null);
		coll.contains("x");

		// Errors on add
		// elem
		coll.add(null);
		// key1
		coll.add(new Ticket(1, null, null));
		// key2
		coll.add(new Ticket(4, "extId1", null));

		// OK
		coll.remove(null);
		coll.removeByKey1(9);

		coll.remove(t3);
		coll.removeByKey1(2);
		coll.removeByKey2("extId1");
	}

	@Capture
	public static void testIterator() {
		Ticket t1 = new Ticket(1, null, "text1");
		Ticket t2 = new Ticket(2, null, "text2");
		Ticket t3 = new Ticket(1, null, "text3");
		Ticket t4 = new Ticket(4, null, "text4");
		Key2Collection<Ticket, Integer, String> coll1 = new Key2Collection.Builder<Ticket, Integer, String>().withElemSet().withOrderByElem(true)
				.withKey1Map(Ticket.IdMapper).withKey2Map(Ticket.ExtIdMapper).build();
		coll1.add(t1);
		coll1.add(t2);
		coll1.add(t3);
		coll1.add(t4);
		System.out.println(PrintTools.print(coll1));
		Iterator<Ticket> iter1 = coll1.iterator();
		iter1.next();
		//iter1.remove();

		Key2Collection<Ticket, Integer, String> coll2 = new Key2Collection.Builder<Ticket, Integer, String>().withKey1Map(Ticket.IdMapper)
				.withKey2Map(Ticket.ExtIdMapper).build();
		coll2.add(t1);
		coll2.add(t2);
		coll2.add(t3);
		coll2.add(t4);
		System.out.println(PrintTools.print(coll2));
		Iterator<Ticket> iter2 = coll2.iterator();
		iter2.next();
		//iter2.remove();
	}

	static String format(Collection<Ticket> tickets) {
		StringBuilder buf = new StringBuilder();
		buf.append("[ ");
		boolean first = true;
		for (Ticket ticket : tickets) {
			if (!first) {
				buf.append(", ");
			} else {
				first = false;
			}
			buf.append(ticket.id);
		}
		buf.append(" ]");
		return buf.toString();
	}

	@Capture
	public static void testCloneCopyCrop() {
		Key2Collection<Ticket, Integer, String> tickets = new Key2Collection.Builder<Ticket, Integer, String>().withNull(false)
				.withConstraint(Ticket.Constraint).withPrimaryKey1Map(Ticket.IdMapper).withOrderByKey1(true).withUniqueKey2Map(Ticket.ExtIdMapper).build();
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
		//        System.out.println(getIdentityHashCode(tickets));
		//        System.out.println(getIdentityHashCode(getAnyBeanValues(tickets, "tableColl")));
		//        System.out.println(getIdentityHashCode(getAnyBeanValues(tickets, "tableColl", "keyMaps[1]", "keysMap")));
		//        System.out.println(getIdentityHashCode(getAnyBeanValues(tickets, "tableColl", "keyMaps[2]", "keysMap")));

		//-- Clone
		Key2Collection<Ticket, String, String> tickets2 = (Key2Collection<Ticket, String, String>) tickets.clone();
		System.out.println("-\nClone: " + format(tickets2));
		//        System.out.println(getIdentityHashCode(tickets2));
		//        System.out.println(getIdentityHashCode(getAnyBeanValues(tickets2, "tableColl")));
		//        System.out.println(getIdentityHashCode(getAnyBeanValues(tickets2, "tableColl", "keyMaps[1]", "keysMap")));
		//        System.out.println(getIdentityHashCode(getAnyBeanValues(tickets2, "tableColl", "keyMaps[2]", "keysMap")));
		//        Assert.assertTrue(tickets2.equals(tickets));

		// Change original
		tickets.add(t5);
		System.out.println("Orig (changed): " + format(tickets));
		System.out.println("Clone (unchanged): " + format(tickets2));

		// Change clone
		tickets.remove(t5);
		tickets2.add(t5);
		System.out.println("Orig (unchanged): " + format(tickets));
		System.out.println("Clone (changed): " + format(tickets2));

		//-- Copy
		Key2Collection<Ticket, Integer, String> tickets3 = tickets.copy();
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
		Key2Collection<Ticket, Integer, String> tickets4 = tickets.crop();
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

	static class BiEntry {
		public Integer e1;
		public String e2;

		public BiEntry(Integer e1, String e2) {
			this.e1 = e1;
			this.e2 = e2;
		}
	}

	@Capture
	public static void testBiMap() {
		Function<BiEntry, Integer> mapper1 = new Function<BiEntry, Integer>() {
			@Override
			public Integer apply(BiEntry e) {
				return e.e1;
			}
		};
		Function<BiEntry, String> mapper2 = new Function<BiEntry, String>() {
			@Override
			public String apply(BiEntry e) {
				return e.e2;
			}
		};
		Key2Collection<BiEntry, Integer, String> biMap = new Key2Collection.Builder<BiEntry, Integer, String>().withKey1Map(mapper1).withKey1Sort(true)
				.withKey2Map(mapper2).withKey2Sort(true).build();

		biMap.add(new BiEntry(2, "b"));
		biMap.add(new BiEntry(1, "c"));
		biMap.add(new BiEntry(3, "a"));

		System.out.println(biMap.getDistinctKeys1());
		System.out.println(biMap.getDistinctKeys2());

		System.out.println(biMap.getByKey1(1).e2);
		System.out.println(biMap.getByKey2("b").e1);
	}

}
