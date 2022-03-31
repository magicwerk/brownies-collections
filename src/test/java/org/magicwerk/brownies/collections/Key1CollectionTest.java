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

import org.magictest.client.Assert;
import org.magictest.client.Capture;
import org.magictest.client.Report;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.TestHelper.ComparableName;
import org.magicwerk.brownies.collections.TestHelper.Name;
import org.magicwerk.brownies.collections.TestHelper.Ticket;
import org.magicwerk.brownies.collections.helper.IdentMapper;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.slf4j.Logger;

/**
 * Test of Key1Collection.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class Key1CollectionTest {

	@SuppressWarnings("unused")
	private static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		test();
	}

	static void test() {
		testKey1Collection();
		//testPutByKey1();
		//testAsMap();
		//testDuplicates();
	}

	@Trace(traceMethod = "/.*/", parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testKey1Collection() {
		Key1Collection<Ticket, Integer> coll = new Key1Collection.Builder<Ticket, Integer>().withKey1Map(Ticket.IdMapper).build();
		Ticket t1 = new Ticket(1, null, "text1");
		Ticket t2 = new Ticket(2, null, "text2");
		coll.add(t1);
		coll.add(t2);
		coll.add(null);

		coll.getByKey1(1);
		coll.getByKey1(null);
		coll.getByKey1(9);

		coll.contains(t1);
		coll.contains("x");

		Ticket t1b = new Ticket(1, null, "text1b");
		coll.add(t1b);
		coll.contains(t1);
		coll.contains(t1b);
		coll.contains("x");

		coll.getAllKeys1();
		coll.getDistinctKeys1();

		GapList<Ticket> list = coll.toList();
		Object[] arr0 = coll.toArray();
		Ticket[] arr1 = coll.toArray(new Ticket[] {});
		CheckTools.check(list.equals(GapList.create(arr0)));
		CheckTools.check(list.equals(GapList.create(arr1)));
	}

	static Ticket t1 = new Ticket(1, "extId1", "text1");
	static Ticket t2 = new Ticket(2, "extId2", "text2");
	static Ticket t3 = new Ticket(3, "extId3", "text3");
	static List<Ticket> ts = GapList.create(t1, t2, t3);

	// Access collection as map and modify it
	@Capture
	public static void testAsMap() {
		Key1Collection<Ticket, Integer> coll = new Key1Collection.Builder<Ticket, Integer>().withPrimaryKey1Map(Ticket.IdMapper).build();
		coll.addAll(ts);
		Map<Integer, Ticket> map = coll.asMap1();
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

	@Trace(traceMethod = "/.*/", parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testPut() {
		Key1Collection<Name, String> coll;
		coll = new Key1Collection.Builder<Name, String>().withElemSet().withKey1Map(Name.Mapper).build();
		coll.put(new Name("a", 1));
		coll.put(new Name("b", 2));
		coll.put(new Name("a", 3));

		coll = new Key1Collection.Builder<Name, String>().withKey1Map(Name.Mapper).build();
		coll.put(new Name("a", 1));
		coll.put(new Name("b", 2));
		coll.put(new Name("a", 3));
	}

	@Trace(parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testPutByKey1() {
		Key1Collection<ComparableName, String> coll;

		// Sorted, no duplicates
		Report.setDescription("put (sorted, no duplicates)");
		coll = new Key1Collection.Builder<ComparableName, String>().withKey1Map(Name.Mapper).withOrderByKey1(true).withElemDuplicates(false).build();
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("c", 3));

		// Replace
		coll.putByKey1(new ComparableName("b", 4));
		// Add
		coll.putByKey1(new ComparableName("d", 5));

		// Sorted, duplicates
		Report.setDescription("put (sorted, duplicates)");
		coll = new Key1Collection.Builder<ComparableName, String>().withKey1Map(Name.Mapper).withOrderByKey1(true).withElemDuplicates(true).build();
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("c", 3));

		// Replace
		coll.putByKey1(new ComparableName("b", 4));
		// Add
		coll.putByKey1(new ComparableName("d", 5));

		// Not sorted, no duplicates
		Report.setDescription("put (not sorted, no duplicates)");
		coll = new Key1Collection.Builder<ComparableName, String>().withKey1Map(Name.Mapper).withElemDuplicates(false).build();
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("c", 3));

		// Replace
		coll.putByKey1(new ComparableName("b", 4));
		// Add
		coll.putByKey1(new ComparableName("d", 5));

		// Not sorted, duplicates
		Report.setDescription("put (not sorted, duplicates)");
		coll = new Key1Collection.Builder<ComparableName, String>().withKey1Map(Name.Mapper).withElemDuplicates(true).build();
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("c", 3));

		// Replace
		coll.putByKey1(new ComparableName("b", 4));
		// Add
		coll.putByKey1(new ComparableName("d", 5));

		// Check rollback
		coll = new Key1Collection.Builder<ComparableName, String>().withKey1Map(Name.Mapper).withElemDuplicates(true).withConstraint(e -> e.value != 99)
				.build();

		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("c", 3));

		// Replace fails
		coll.putByKey1(new ComparableName("b", 99));
		// Add
		coll.putByKey1(new ComparableName("d", 5));
		// Remove
		System.out.println(coll.size() + " " + coll);
	}

	@Trace(traceMethod = "/.*/", parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testDuplicates() {
		// Check that duplicates are added at the end and removed from the head
		Key1Collection<ComparableName, ComparableName> coll = new Key1Collection.Builder<ComparableName, ComparableName>().withKey1Map(IdentMapper.INSTANCE)
				.withKey1Sort(true).withKey1Duplicates(true).build();
		coll.add(new ComparableName("d", 4));
		coll.add(new ComparableName("c", 2));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("c", 3));

		coll.getByKey1(new ComparableName("c", -1));

		coll.remove(new ComparableName("c", -1));
		coll.remove(new ComparableName("c", -1));
	}

	static class TicketCollection extends Key1Collection<Ticket, Integer> {
		public TicketCollection() {
			getBuilder().withPrimaryKey1Map(Ticket.IdMapper).build();
		}
	}

	@Trace(traceClass = "org.magicwerk.brownies.collections.Key1CollectionTest$TicketCollection", traceMethod = "/.*/", parameters = Trace.ALL_PARAMS
			| Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testExtends() {
		TicketCollection tc = new TicketCollection();
		tc.add(t1);
		TicketCollection tc1 = (TicketCollection) tc.clone();
		TicketCollection tc2 = (TicketCollection) tc.copy();
		TicketCollection tc3 = (TicketCollection) tc.crop();
	}

}
