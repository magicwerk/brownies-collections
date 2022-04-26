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
package org.magicwerk.brownies.collections.dev;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.magictest.client.Assert;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.Key1Collection;
import org.magicwerk.brownies.collections.Key1List;
import org.magicwerk.brownies.collections.Key2Collection;
import org.magicwerk.brownies.collections.Key2List;
import org.magicwerk.brownies.collections.KeyCollection;
import org.magicwerk.brownies.collections.KeyList;
import org.magicwerk.brownies.collections.exceptions.KeyException;
import org.magicwerk.brownies.collections.helper.NaturalComparator;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.magicwerk.brownies.collections.primitive.IntObjGapList;
import org.magicwerk.brownies.core.reflect.ReflectEl;
import org.magicwerk.brownies.core.reflect.ReflectTools;

/**
 * Examples for the use of key collections.
 *
 * @author Thomas Mauch
 */
public class RunKeyCollectionsExamples {

	public static void main(String[] args) {
		test();
	}

	static void test() {
		testExamples();

		//testKeyList();
		//testGuavaBiMap();
		//testCommonsCollectionsBidiMap();
		//testBiMap();
		//testBiMap2();
		//testKeyListWithTrigger();
	}

	static void testKeyList() {
		KeyList<Integer> intList1 = new KeyList.Builder<Integer>().build();
		add(intList1, 0);
		add(intList1, 1);
		add(intList1, -1);
		add(intList1, null);
		System.out.println("intList1: " + intList1);

		KeyList<Integer> intList2 = new KeyList.Builder<Integer>().withNull(false).build();
		add(intList2, 0);
		add(intList2, 1);
		add(intList2, -1);
		add(intList2, null);
		System.out.println("intList2: " + intList2);

		KeyList<Integer> intList3 = new KeyList.Builder<Integer>().withNull(false).withConstraint(i -> i >= 0).build();
		add(intList3, 0);
		add(intList3, 1);
		add(intList3, -1);
		add(intList3, null);
		System.out.println("intList3: " + intList3);

		KeyList<Integer> intList4 = new KeyList.Builder<Integer>().withNull(false).withConstraint(i -> i >= 0).withMaxSize(1).build();
		add(intList4, 0);
		add(intList4, 1);
		add(intList4, -1);
		add(intList4, null);
		System.out.println("intList4: " + intList4);

		KeyList<Integer> intList5 = new KeyList.Builder<Integer>().withNull(false).withConstraint(i -> i >= 0).withMaxSize(1).withListBig(true).build();
		add(intList5, 0);
		add(intList5, 1);
		add(intList5, -1);
		add(intList5, null);
		System.out.println("intList5: " + intList5);

		KeyList<Integer> intList6 = new KeyList.Builder<Integer>().withListBig(true).build();
		add(intList6, 0);
		add(intList6, 1);
		add(intList6, -1);
		add(intList6, null);
		System.out.println("intList6: " + intList6);

		KeyList<Integer> intList7 = new KeyList.Builder<Integer>().withListType(int.class).build();
		add(intList7, 0);
		add(intList7, 1);
		add(intList7, -1);
		add(intList7, null);
		System.out.println("intList7: " + intList7);

		KeyList<Integer> intList8 = new KeyList.Builder<Integer>().withListBig(true).withListType(int.class).build();
		add(intList8, 0);
		add(intList8, 1);
		add(intList8, -1);
		add(intList8, null);
		System.out.println("intList8: " + intList8);

		KeyList<Integer> intList9 = new KeyList.Builder<Integer>().withOrderByElem(true).build();
		add(intList9, 0);
		add(intList9, 1);
		add(intList9, -1);
		add(intList9, null);
		System.out.println("intList9: " + intList9);
	}

	static void add(List<Integer> list, Integer elem) {
		try {
			list.add(elem);
		} catch (Exception e) {
		}
	}

	static void t() {
		Key1List<Column, String> cols1 = new Key1List.Builder<Column, String>().withKey1Map(Column::getName).withKey1Null(false).withKey1Duplicates(false)
				.build();

		Key1List<Column, String> cols2 = new Key1List.Builder<Column, String>().withPrimaryKey1Map(Column::getName).build();

		cols1.add(new Column("name", ""));
		Column col = cols1.getByKey1("name");

		// Eine Liste, welche nur Strings in Grossbuchstaben akzeptiert
		KeyList<String> constraintList = new KeyList.Builder<String>().withConstraint(s -> s.equals(s.toUpperCase())).build();

		// Eine sortierte Liste
		KeyList<String> sortedList = new KeyList.Builder<String>().withOrderByElem(true).build();

		// Eine SetList, d.h. jedes Element kann nur einmal vorkommen
		KeyList<String> setList = new KeyList.Builder<String>().withPrimaryElem().build();

		// Eine Liste, welche immer nur ein Window der letzten 5 Elemente enth�lt
		KeyList<String> windowList = new KeyList.Builder<String>().withWindowSize(5).build();

		Key1List<Column, String> list1 = new Key1List.Builder<Column, String>().withPrimaryKey1Map(Column::getName).withKey1Sort(true).withOrderByKey1(true)
				.build();

		Key1List<File, String> list2 = new Key1List.Builder<File, String>().withPrimaryKey1Map(File::getName).withOrderByKey1(true).build();

		Key2Collection<Ticket, String, String> coll2 = new Key2Collection.Builder<Ticket, String, String>().withPrimaryKey1Map(Ticket::getId)
				.withUniqueKey2Map(Ticket::getExtId).build();
	}

	static void testExamples() {
		//        testKeyList();
		System.out.println("testKeyListWithNull");
		testKeyListWithNull();
		System.out.println("testKeyListWithConstraint");
		testKeyListWithConstraint();
		System.out.println("testKeyListWithMaxSize");
		testKeyListWithMaxSize();
		System.out.println("testKeyListWithWindowSize");
		testKeyListWithWindowSize();
		System.out.println("testKeyListWithTrigger");
		testKeyListWithTrigger();

		System.out.println("testKeyListWithElemSet");
		testKeyListWithElemSet();
		System.out.println("testKeyListWithElemNull");
		testKeyListWithElemNull();
		System.out.println("testKeyListWithElemDuplicates");
		testKeyListWithElemDuplicates();
		System.out.println("testKeyListWithPrimaryElem");
		testKeyListWithPrimaryElem();
		System.out.println("testKeyListWithUniqueElem");
		testKeyListWithUniqueElem();
		System.out.println("testKeyListWithElemSort");
		testKeyListWithElemSort();
		System.out.println("testKeyListWithElemOrderBy");
		testKeyListWithElemOrderBy();
		System.out.println("testKeyListWithElemOrderByClass");
		testKeyListWithElemOrderByClass();

		testKey1List();
		testKey2List();

		testKeyCollection();
		testKeyCollectionWithElemCount();

		testKey1Collection();
		testKey2Collection();

		testAsSet();
		testAsMap();

		//

	}

	//---- Examples

	//-- GapList

	static void testGapList() {
		// GapList replaces all ArrayList, LinkedList, ArrayDequeue
		List<String> list = GapList.create("a", "b");
	}

	static void testIntObjGapList() {
		// IntObjGapList stores primitive int values instead of Objects,
		// but allows access through the standard List and Dequeue interface
		List<Integer> list = IntObjGapList.create(1, 2);
		Integer value = list.get(0);
	}

	static void testIntGapList() {
		// IntGapList stores primitive int values instead of Objects
		IntGapList list = IntGapList.create(1, 2);
		int value = list.get(0);
	}

	//-- KeyList

	static void testKeyListWithNull() {
		// A list with does not allow null values
		KeyList<String> list = new KeyList.Builder<String>().withNull(false).build();

		//@test:begin
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps") == null);

		list.add("abc");
		try {
			list.add(null);
		} catch (KeyException e) {
			System.out.println(e.getMessage());
		}
		System.out.println(list);
		//@test:end
	}

	static void testKeyListWithConstraint() {
		// A list with a user defined constraint
		Predicate<String> uppercasePredicate = new Predicate<String>() {
			@Override
			public boolean test(String elem) {
				return elem.equals(elem.toUpperCase());
			}
		};
		KeyList<String> list = new KeyList.Builder<String>().withConstraint(uppercasePredicate).build();

		// @java8:begin
		//      list = new KeyList.Builder<String>().withConstraint(s -> s.equals(s.toUpperCase())).build();
		// @java8:end
		// @test:begin
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps") == null);

		list.add("ABC");
		list.add(null); // TODO what about null?
		try {
			list.add("abc");
		} catch (KeyException e) {
			System.out.println(e.getMessage());
		}
		System.out.println(list);
		// @test:end
	}

	static void testKeyListWithMaxSize() {
		// A list which can store a maximum of 5 elements
		KeyList<String> list = new KeyList.Builder<String>().withMaxSize(5).build();

		//@test:begin
		list.add("abc");
		try {
			list.add("def");
		} catch (KeyException e) {
			System.out.println(e.getMessage());
		}
		System.out.println(list);
		//@test:end
	}

	static void testKeyListWithWindowSize() {
		// A list which can store a maximum of 5 elements.
		// If an additional element is added, the first element is automatically removed first.
		KeyList<String> list = new KeyList.Builder<String>().withWindowSize(5).build();

		//@test:begin
		list.add("a");
		list.add("ab");
		list.add("abc");
		System.out.println(list);
		//@test:end
	}

	static class Zip {
		int code;
		String city;

		Zip(int code, String city) {
			this.code = code;
			this.city = city;
		}

		int getCode() {
			return code;
		}

		String getCity() {
			return city;
		}
	}

	static void testBiMap() {
		Key2Collection<Zip, Integer, String> zips = new Key2Collection.Builder<Zip, Integer, String>().withKey1Map(Zip::getCode).withKey1Sort(true)
				.withKey2Map(Zip::getCity).withKey2Sort(true).build();

		zips.add(new Zip(1000, "city1000"));
		String city = zips.getByKey1(1000).getCity();
		int code = zips.getByKey2("city1000").getCode();
	}

	static void testKeyListWithTrigger() {
		KeyList<String> list = new KeyList.Builder<String>().withBeforeInsertTrigger(new Consumer<String>() {
			@Override
			public void accept(String elem) {
				System.out.println("insert: " + elem);
			}
		}).withBeforeDeleteTrigger(new Consumer<String>() {
			@Override
			public void accept(String elem) {
				System.out.println("delete: " + elem);
			}
		}).build();

		//@java8:begin
		//      list = new KeyList.Builder<String>().
		//          withInsertTrigger(e -> System.out.println("insert: " + e)).
		//          withDeleteTrigger(e -> System.out.println("delete: " + e)).build();
		//@java8:end
		//@test:begin
		{
			// Unsorted list
			list = new KeyList.Builder<String>().withBeforeInsertTrigger(new Consumer<String>() {
				@Override
				public void accept(String elem) {
					System.out.println("add1: " + elem);
				}
			}).withBeforeDeleteTrigger(new Consumer<String>() {
				@Override
				public void accept(String elem) {
					System.out.println("rem1: " + elem);
				}
			}).build();

			list.add("a");
			list.set(0, "b");
			list.remove(0);
		}
		{
			// Sorted list
			list = new KeyList.Builder<String>().withBeforeInsertTrigger(new Consumer<String>() {
				@Override
				public void accept(String elem) {
					System.out.println("add2: " + elem);
				}
			}).withBeforeDeleteTrigger(new Consumer<String>() {
				@Override
				public void accept(String elem) {
					System.out.println("rem2: " + elem);
				}
			}).withOrderByElem(true).build();

			list.add("a");
			list.set(0, "b");
			list.remove(0);
		}
		//@test:end
	}

	//-- KeyList with element set

	static void testKeyListWithElemSet() {
		// List with set for fast access to elements (all values allowed)
		KeyList<String> list = new KeyList.Builder<String>().withElemSet().build();

		//@test:begin
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0]") != null);
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0].keysMap").getClass() == HashMap.class);

		list.add("a");
		list.add(null);
		list.add("ab");
		list.add("ab");
		list.add("abc");
		System.out.println(list);

		// Fast set operations
		System.out.println(list.contains("a"));
		System.out.println(list.getCount("a"));
		// The returned set is unordered
		System.out.println(list.getDistinct());
		//@test:end
	}

	static void testKeyListWithElemNull() {
		// List with set for fast access to elements (no null values allowed)
		KeyList<String> list = new KeyList.Builder<String>().withElemNull(false).build();

		//@test:begin
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0]") != null);
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0].keysMap").getClass() == HashMap.class);
		//@test:end
	}

	static void testKeyListWithElemDuplicates() {
		// List with set for fast access to elements (no duplicate values allowed)
		KeyList<String> list = new KeyList.Builder<String>().withElemDuplicates(false).build();

		//@test:begin
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0]") != null);
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0].keysMap").getClass() == HashMap.class);

		list.add("abc");
		try {
			list.add("abc");
		} catch (KeyException e) {
			System.out.println(e.getMessage());
		}
		System.out.println(list);
		//@test:end
	}

	static void testKeyListWithPrimaryElem() {
		// List with set for fast access to elements (no duplicate or null values allowed)
		KeyList<String> list = new KeyList.Builder<String>().withPrimaryElem().build();

		//@test:begin
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0]") != null);
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0].keysMap").getClass() == HashMap.class);

		list.add("abc");
		list.add("def");

		try {
			list.add(null);
		} catch (KeyException e) {
			System.out.println(e.getMessage());
		}
		try {
			list.add("abc");
		} catch (KeyException e) {
			System.out.println(e.getMessage());
		}
		System.out.println(list);
		//@test:end
	}

	static void testKeyListWithUniqueElem() {
		// List with set for fast access to elements (no duplicate values allowed except null)
		KeyList<String> list = new KeyList.Builder<String>().withUniqueElem().build();
	}

	static void testKeyListWithElemSort() {
		// List with set for fast access to elements.
		// The element set, but not the list is sorted by the natural comparator.
		KeyList<String> list = new KeyList.Builder<String>().withElemSort(true).build();

		// Sort set by user defined comparator, null values are sorted last.
		Comparator<String> cmp = new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return s1.toLowerCase().compareTo(s2.toLowerCase());
			}
		};
		list = new KeyList.Builder<String>().withElemSort(cmp, false).build();

		//@java8:begin
		//      list = new KeyList.Builder<String>().
		//          withElemSort( (s1,s2) -> ( s1.toLowerCase().compareTo(s2.toLowerCase()) ), false).build();
		//@java8:end
		//@test:begin
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0]") != null);
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0].keysMap").getClass() == TreeMap.class);

		list.add("abc");
		list.add(null);
		list.add("ab");
		list.add("ab");
		list.add("a");
		System.out.println(list);

		// Fast set operations
		System.out.println(list.contains("a"));
		System.out.println(list.getCount("a"));
		// The returned set is ordered by natural comparator
		System.out.println(list.getDistinct());
		//@test:end
	}

	static void testKeyListWithElemOrderBy() {
		// List with set for fast access to elements.
		// The element set and the list are sorted by the natural comparator.
		KeyList<String> list = new KeyList.Builder<String>().withOrderByElem(true).build();

		//@test:begin
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0]") != null);
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0].keysList").getClass() == GapList.class);
		Assert.assertTrue(ReflectEl.getByExpr(list, "forward") != null);
		Assert.assertTrue(ReflectEl.getByExpr(list, "forward").getClass() == GapList.class);

		list.add("ab");
		list.add("abc");
		list.add(null);
		list.add("ab");
		list.add("a");
		System.out.println(list);
		//@test:end

		// Sort element set and list by specified comparator, nulls last
		Comparator<String> comparator = NaturalComparator.INSTANCE(String.class).reversed();
		list = new KeyList.Builder<String>().withElemSort(comparator, false).withOrderByElem(true).build();

		//@test:begin
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0]") != null);
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0].keysList").getClass() == GapList.class);
		Assert.assertTrue(ReflectEl.getByExpr(list, "forward") != null);
		Assert.assertTrue(ReflectEl.getByExpr(list, "forward").getClass() == GapList.class);

		list.add("ab");
		list.add("abc");
		list.add(null);
		list.add("ab");
		list.add("a");
		System.out.println(list);
		//@test:end
	}

	static void testKeyListWithElemOrderByClass() {
		// List with set for fast access to elements.
		// The set is realized as sorted list of primitive values.
		KeyList<Integer> list = new KeyList.Builder<Integer>().withOrderByElem(int.class).build();

		//@test:begin
		list = new KeyList.Builder<Integer>().withOrderByElem(int.class).build();
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0]") != null);
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0].keysList").getClass() == IntObjGapList.class);
		Assert.assertTrue(ReflectEl.getByExpr(list, "forward") != null);
		Assert.assertTrue(ReflectEl.getByExpr(list, "forward").getClass() == IntObjGapList.class);

		list.add(2);
		list.add(1);
		System.out.println(list);
		//@test:end
	}

	// Key1List

	static void testKey1List() {
		// Use Key1List to implement a list of column names with a defined
		// order and unique names which can be accessed fast
		Function<Column, String> nameMapper = new Function<Column, String>() {
			@Override
			public String apply(Column column) {
				return column.getName();
			}
		};
		Key1List<Column, String> list = new Key1List.Builder<Column, String>().withPrimaryKey1Map(nameMapper).build();

		// @java8:begin
		//      list = new Key1List.Builder<Column,String>().withKey1Map(Column::getName).withPrimaryKey1().build();
		// @java8:end
		// @test:begin
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0]") == null);
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[1].keysMap").getClass() == HashMap.class);

		list.add(new Column("id", "string"));
		list.add(new Column("value", "int"));
		list.add(null); //null elements are not allowed
		list.add(new Column(null, null)); // null keys are not allowed
		list.add(new Column("id", "int")); // duplicate keys are not allowed

		Column col1 = list.getByKey1("value");
		List<Column> cols = list.getAllByKey1("value");
		Column removed = list.removeByKey1("value");
		List<Column> removeds = list.removeAllByKey1("value");
		int count = list.getCountByKey1("value");
		Set<String> keys = list.getDistinctKeys1();

		list.add(new Column("col", "string"));
		try {
			list.add(null);
		} catch (KeyException e) {
			System.out.println(e.getMessage());
		}
		try {
			list.add(new Column(null, "string"));
		} catch (KeyException e) {
			System.out.println(e.getMessage());
		}
		try {
			list.add(new Column("col", "string"));
		} catch (KeyException e) {
			System.out.println(e.getMessage());
		}
		list.add(new Column("col2", "string"));

		list.getByKey1("col");
		System.out.println(list);
		// @test:end
	}

	// Key2List

	static void testKey2List() {
		// Use Key2List to maintain a list of tickets.
		// Each ticket has unique ID (primary key) and optional but also unique external ID.
		Predicate<Ticket> ticketConstraint = new Predicate<Ticket>() {
			@Override
			public boolean test(Ticket ticket) {
				return ticket.getText() != null;
			}
		};
		Function<Ticket, String> idMapper = new Function<Ticket, String>() {
			@Override
			public String apply(Ticket ticket) {
				return ticket.getId();
			}
		};
		Function<Ticket, String> extIdMapper = new Function<Ticket, String>() {
			@Override
			public String apply(Ticket ticket) {
				return ticket.getExtId();
			}
		};
		Key2List<Ticket, String, String> list = new Key2List.Builder<Ticket, String, String>().withConstraint(ticketConstraint).withPrimaryKey1Map(idMapper)
				.withUniqueKey2Map(extIdMapper).build();

		// @java8:begin
		//        list = new Key2List.Builder<Ticket,String,String>().
		//                withConstraint(t -> t.getText()!=null).
		//                withKey1Map(Ticket::getId).withPrimaryKey1().
		//                withKey2Map(Ticket::getExtId).withUniqueKey2().
		//                build();
		// @java8:end
		// @test:begin
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[0]") == null);
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[1].keysMap").getClass() == HashMap.class);
		Assert.assertTrue(ReflectEl.getByExpr(list, "keyColl.keyMaps[2].keysMap").getClass() == HashMap.class);

		list.add(new Ticket("id1", "extId1", "text1"));
		list.add(new Ticket("id2", null, "text2"));
		try {
			list.add(new Ticket("id3", "extId3", null));
		} catch (KeyException e) {
			System.out.println(e.getMessage());
		}
		try {
			list.add(new Ticket("id1", "extId4", "text4"));
		} catch (KeyException e) {
			System.out.println(e.getMessage());
		}
		try {
			list.add(new Ticket("id5", "extId1", "text5"));
		} catch (KeyException e) {
			System.out.println(e.getMessage());
		}
		try {
			list.add(new Ticket(null, "extId6", "text6"));
		} catch (KeyException e) {
			System.out.println(e.getMessage());
		}
		try {
			list.add(null);
		} catch (KeyException e) {
			System.out.println(e.getMessage());
		}

		list.getByKey1("id1");
		list.getByKey2("extId1");
		System.out.println(list);
		// @test:end
	}

	// KeyCollection

	static void testKeyCollection() {
		// A KeyCollection will always have element set automatically added
		KeyCollection<String> coll = new KeyCollection.Builder<String>().build();
		// So these two declarations are equal
		coll = new KeyCollection.Builder<String>().withElemSet().build();

		//@test:begin
		{
			KeyCollection<String> coll1 = new KeyCollection.Builder<String>().build();
			coll1.add("abc");
			coll1.getDistinct();
			Assert.assertTrue(ReflectEl.getByExpr(coll1, "keyMaps[0]") != null);
			Assert.assertTrue(ReflectEl.getByExpr(coll1, "keyMaps[0].keysMap").getClass() == HashMap.class);
		}
		{
			KeyCollection<String> coll2 = new KeyCollection.Builder<String>().withElemSet().build();
			coll2.add("abc");
			coll2.getDistinct();
			Assert.assertTrue(ReflectEl.getByExpr(coll2, "keyMaps[0]") != null);
			Assert.assertTrue(ReflectEl.getByExpr(coll2, "keyMaps[0].keysMap").getClass() == HashMap.class);
		}
		//@test:end
	}

	static void testKeyCollectionWithElemCount() {
		// A KeyCollection which behaves like a multi set, i.e. it does not store duplicate elements,
		// but only its number of occurrences.
		KeyCollection<String> coll = new KeyCollection.Builder<String>().withElemCount(true).build();

		//@test:begin
		Assert.assertTrue(ReflectEl.getByExpr(coll, "keyMaps[0]") != null);
		Assert.assertTrue(ReflectEl.getByExpr(coll, "keyMaps[0].keysMap").getClass() == HashMap.class);

		coll.add("a");
		coll.add("b");
		coll.add("a");
		coll.add("c");
		int size = ReflectTools.getObjectSize(coll);
		System.out.println(coll);

		KeyCollection<String> coll2 = coll.copy();
		coll2.add("a");
		int size2 = ReflectTools.getObjectSize(coll2);
		//Assert.assertTrue(size == size2);
		//@test:end
	}

	// Key1Collection

	static void testKey1Collection() {
		// Use a Key1Collection store tag elements with a name as primary key.
		Function<Tag, String> nameMapper = new Function<Tag, String>() {
			@Override
			public String apply(Tag tag) {
				return tag.getName();
			}
		};
		Key1Collection<Tag, String> coll = new Key1Collection.Builder<Tag, String>().withPrimaryKey1Map(nameMapper).build();

		//@java8:begin
		//      coll = new Key1Collection.Builder<Tag,String>().withKey1Map(Tag::getName).withPrimaryKey1().build();
		//@java8:end
	}

	// Key2Collection

	static void testKey2Collection() {
		// Use a Key2Collection to construct a bidirectional map
		// where both key sets are sorted.

		Function<ZipCity, Integer> mapper1 = new Function<ZipCity, Integer>() {
			@Override
			public Integer apply(ZipCity entry) {
				return entry.getZip();
			}
		};

		Key2Collection<ZipCity, Integer, String> zipCities = new Key2Collection.Builder<ZipCity, Integer, String>().withPrimaryKey1Map(mapper1)
				.withKey1Sort(true).withKey2Map(new Function<ZipCity, String>() {
					@Override
					public String apply(ZipCity entry) {
						return entry.getCity();
					}
				}).withKey2Null(false).withKey2Sort(true).build();

		//@java8:begin
		//        zipCities = new Key2Collection.Builder<ZipCity,Integer,String>().
		//                withKey1Map(ZipCity::getZip).
		//                withPrimaryKey1().withKey1Sort(true).
		//                withKey2Map(ZipCity::getCity).
		//                withKey2Null(false).withKey2Sort(true).build();
		//@java8:end
		zipCities.add(new ZipCity(4000, "Basel"));
		zipCities.add(new ZipCity(5000, "Aarau"));
		zipCities.add(new ZipCity(5001, "Aarau"));
		zipCities.add(new ZipCity(6000, "Luzern"));

		Set<Integer> allZips = zipCities.asMap1().keySet();
		Set<String> allCities = zipCities.asMap2().keySet();

		String city = zipCities.getByKey1(5000).getCity();
		// TODO do we need mappedCollection
		List<Integer> zips = GapList.create(zipCities.getAllByKey2("Aarau")).mappedList(zipCities.getKey1Mapper());

		//@test:begin
		System.out.println(allZips);
		System.out.println(allCities);
		System.out.println(city);
		System.out.println(zips);
		//@test:end
	}

	// AsSet / AsMap

	static void testAsSet() {
		KeyList<String> list = new KeyList.Builder<String>().build();
		// Returns a modifiable set view of the collection.
		// Note that this method does not check whether the collection really is really a set
		// as defined by the Set interface. It makes only sure that the add() method will return
		// false instead of throwing a DuplicateKeyException.
		Set<String> set = list.asSet();
	}

	static void testAsMap() {
		Function<Tag, String> nameMapper = new Function<Tag, String>() {
			@Override
			public String apply(Tag tag) {
				return tag.getName();
			}
		};
		Key1Collection<Tag, String> coll = new Key1Collection.Builder<Tag, String>().withKey1Map(nameMapper).build();

		//@java8:begin
		//      coll = new Key1Collection.Builder<Tag,String>().withKey1Map(Tag::getName).build();
		//@java8:end
		// Returns a modifiable map view of the collection.
		Map<String, Tag> map = coll.asMap1();
	}

	// Helper classes

	static class Tag {
		String name;

		public Tag(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	static class Column {
		String name;
		String type;

		public Column(String name, String type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}
	}

	static class ZipCity {
		int zip;
		String city;

		public ZipCity(int zip, String city) {
			this.zip = zip;
			this.city = city;
		}

		public int getZip() {
			return zip;
		}

		public String getCity() {
			return city;
		}

		@Override
		public String toString() {
			return "BiEntry [zip=" + zip + ", city=" + city + "]";
		}
	}

	static class Ticket {
		String id;
		String extId;
		String text;

		public Ticket(String id, String extId, String text) {
			this.id = id;
			this.extId = extId;
			this.text = text;
		}

		public String getId() {
			return id;
		}

		public String getExtId() {
			return extId;
		}

		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			return "Ticket [id=" + id + ", extId=" + extId + ", text=" + text + "]";
		}
	}

}
