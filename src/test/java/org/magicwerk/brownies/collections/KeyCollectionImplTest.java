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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.magictest.client.Capture;
import org.magicwerk.brownies.collections.TestHelper.Ticket;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.slf4j.Logger;

/**
 * Test of KeyCollectionImpl.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class KeyCollectionImplTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		test();
	}

	static void test() {
		testAsSet();
	}

	static Ticket t1 = new Ticket(1, "extId1", "text1");
	static Ticket t2 = new Ticket(2, "extId2", "text2");
	static Ticket t3 = new Ticket(3, "extId3", "text3");
	static List<Ticket> ts = GapList.create(t1, t2, t3);

	// Access collection as set and modify it
	@Capture
	public static void testAsSet() {
		KeyCollection<Ticket> coll = new KeyCollection.Builder<Ticket>().withOrderByElem(true).withPrimaryElem().build();
		coll.addAll(ts);
		Set<Ticket> set = coll.asSet();
		System.out.println(set);
		set.remove(t2);
		System.out.println(set);
		Iterator<Ticket> iter = set.iterator();
		iter.next();
		iter.remove();
		System.out.println(set);

		//
		KeyCollection<String> coll0 = new KeyCollection.Builder<String>().build();
		coll0.add("abc");
		coll0.add("abc");
		System.out.println(coll0);

		KeyCollection<String> coll1 = new KeyCollection.Builder<String>().withElemDuplicates(false).build();
		coll1.add("abc");
		try {
			coll1.add("abc");
			throw new AssertionError();
		} catch (Exception e) {
		}
		System.out.println(coll1);

		// Use asSet()
		KeyCollection<String> coll2 = new KeyCollection.Builder<String>().withElemDuplicates(false).build();
		Set<String> set2 = coll2.asSet();
		set2.add("abc");
		set2.add("abc");
		System.out.println(set2);

		// Use asSet() with constraint
		Predicate<String> maxLength = new Predicate<String>() {
			@Override
			public boolean test(String elem) {
				return elem.length() < 3;
			}
		};
		KeyCollection<String> coll3 = new KeyCollection.Builder<String>().withConstraint(maxLength).withElemDuplicates(false).build();
		Set<String> set3 = coll3.asSet();
		set3.add("ab");
		set3.add("ab");
		try {
			set3.add("abc");
			throw new AssertionError();
		} catch (Exception e) {
		}
		System.out.println(set3);

		// Use withSetBehavior()
		KeyCollection<String> coll4 = new KeyCollection.Builder<String>().withElemDuplicates(false).withSetBehavior(true).build();
		coll4.add("abc");
		coll4.add("abc");
		coll4.addAll(GapList.create("def", "def"));
		System.out.println(coll4);
	}

	@Capture
	public static void testKeyCollectionImpl() {

		// -- Errors

		// Error: sort already called
		try {
			KeyCollection<Ticket> coll = new KeyCollection.Builder<Ticket>().withElemSet().withElemSort(true).withElemSort(Ticket.Comparator).build();
		} catch (Exception e) {
			System.out.println(e);
		}

		// Error: Only one order by key allowed
		try {
			Key1Collection<Ticket, Integer> coll = new Key1Collection.Builder<Ticket, Integer>().withElemSet().withOrderByElem(true)
					.withKey1Map(Ticket.IdMapper).withOrderByKey1(true).build();
		} catch (Exception e) {
			System.out.println(e);
		}

		// Error: No mapper for key 1 defined
		try {
			Key1Collection<Ticket, String> coll = new Key1Collection.Builder<Ticket, String>().withElemSet().withOrderByElem(true).withKey1Sort(true).build();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

}
