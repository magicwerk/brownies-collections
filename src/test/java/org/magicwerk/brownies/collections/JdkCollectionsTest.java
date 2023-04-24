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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualTreeBidiMap;
import org.magicwerk.brownies.collections.TestHelper.ComparableName;
import org.magicwerk.brownies.collections.TestHelper.Name;
import org.magicwerk.brownies.collections.helper.NaturalComparator;
import org.magicwerk.brownies.collections.helper.NullComparator;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.print.PrintTools;
import org.slf4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Test functionality of JDK collections.
 *
 * @author Thomas Mauch
 */
public class JdkCollectionsTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		test();
	}

	static void test() {
		//testList();
		//testDeque();
		//testHashSet();
		//testHashMap();

		//testInvalidateMap();
		testInvalidateSet();
	}

	static class HashedObj {
		String str;

		HashedObj(String s) {
			this.str = s;
		}

		@Override
		public int hashCode() {
			return str.hashCode();
		}

		public boolean equals(String s) {
			return s.equals(str);
		}
	}

	static void testInvalidateSet() {
		// If the hashCode of an element stored in set changes, you cannot remove it from the set anymore:
		// even if you have an object reference which you can find by iterating, methods contains() or
		// remove() will not work and return false. Explicitly copy the set's elements to a new set working.
		String sa = "a";
		String sb = "b";
		HashedObj a = new HashedObj(sa);
		HashedObj b = new HashedObj(sb);

		HashSet<HashedObj> set = new HashSet<>();
		set.add(a);
		set.add(b);

		a.str = "aa";
		System.out.println(a);
		System.out.println(set.contains(a));
		HashedObj found = null;
		for (HashedObj ho : set) {
			if (ho.str.equals("aa")) {
				found = ho;
			}
		}
		System.out.println(found);
		System.out.println(set.size());

		// Explicit remove does not work
		boolean removed = set.remove(found);
		System.out.println(removed);
		System.out.println(set.size());

		// Remove with iterator does not work
		Iterator<HashedObj> iter = set.iterator();
		while (iter.hasNext()) {
			HashedObj ho = iter.next();
			if (ho.str.equals("aa")) {
				iter.remove();
			}
		}
		System.out.println(set.size());

		// Make a copy of set by removing found element
		HashSet<HashedObj> set2 = new HashSet<>();
		for (HashedObj ho : set) {
			if (ho.str.equals("aa")) {
				continue;
			}
			set2.add(ho);
		}
		System.out.println(set2.size());
	}

	static void testInvalidateMap() {
		String sa = "a";
		String sb = "b";
		HashedObj a = new HashedObj(sa);
		HashedObj b = new HashedObj(sb);

		HashMap<String, HashedObj> map = new HashMap<>();
		map.put(a.str, a);
		map.put(b.str, b);

		System.out.println(map.get(sa));

		String sa2 = "ab".substring(0, 1);
		a.str = "aa";
		System.out.println(map.get(sa));
		System.out.println(map.get(sa2));
		System.out.println(map.get("aa"));
	}

	static void testDeque() {
		//Deque<String> deque = new LinkedList<String>();
		Deque<String> deque = new ArrayDeque<String>();

		try {
			deque.getFirst();
		} catch (Exception e) {
			System.out.println(e);
		}
		try {
			deque.getLast();
		} catch (Exception e) {
			System.out.println(e);
		}
		try {
			deque.removeFirst();
		} catch (Exception e) {
			System.out.println(e);
		}
		try {
			deque.removeLast();
		} catch (Exception e) {
			System.out.println(e);
		}

		deque.add("a");
		deque.add("b");
		deque.add("c");

		deque.add(null);
		deque.addFirst(null);
		deque.addLast(null);

		deque.offer(null);
		deque.offerFirst(null);
		deque.offerLast(null);

	}

	static void testList() {
		List<String> list = new ArrayList<String>();

		// throws NullPointerException
		//list.addAll(null);

		list.add("a");
		list.add("b");
		list.add("c");
		list.add("a");
		list.add("b");
		list.add("c");

		// Removes first occurence
		//list.remove("a");

		// Removes all occurences
		list.removeAll(Arrays.asList("a"));

		// Efficiently remove a range of elements out of an ArrayList
		list.subList(1, 3).clear();

		LOG.info("List: {}", list);
	}

	// -- java.util.Set
	// - HashSet allows null values (only one, treated like ordinary value)
	// - TreeSet allows null values if comparator can handle them (only one, treated like ordinary value)

	static void testJavaUtilSet() {
		Name n1 = new Name("a");
		Name n2 = new Name("b");
		ComparableName cn1 = new ComparableName("a");
		ComparableName cn2 = new ComparableName("b");

		HashSet<Name> hashSet = new HashSet<Name>();
		hashSet.add(n1);
		hashSet.add(n1);
		hashSet.add(n2);
		hashSet.add(null);
		hashSet.add(null);
		LOG.info("HashSet: {}", hashSet);

		// TreeSet can be declared with any object, but a
		// ClassCastException will result if the entries are not comparable
		//TreeSet<Name> treeSet = new TreeSet<Name>();
		TreeSet<ComparableName> treeSet = new TreeSet<ComparableName>();
		treeSet.add(cn1);
		treeSet.add(cn2);
		// TreeSet does not allow null keys
		//treeSet.add(null);
		LOG.info("TreeSet: {}", treeSet);

		Comparator<Object> cmp = new NullComparator<Object>(NaturalComparator.INSTANCE(), false);
		TreeSet<ComparableName> treeSet2 = new TreeSet<ComparableName>(cmp);
		treeSet2.add(cn1);
		treeSet2.add(cn2);
		treeSet2.add(null);
		LOG.info("TreeSet: {}", treeSet2);
	}

	static void testJavaUtilMap() {
		Name n1 = new Name("a");
		Name n2 = new Name("b");
		ComparableName cn1 = new ComparableName("a");
		ComparableName cn2 = new ComparableName("b");

		HashMap<Name, Name> hashMap = new HashMap<Name, Name>();
		hashMap.put(n1, n1);
		hashMap.put(n2, null);
		hashMap.put(null, null);
		LOG.info("HashMap: {}", hashMap);

		// TreeMap can be declared with any object, but a
		// ClassCastException will result if the entries are not comparable
		//TreeMap<Name, Name> treeMap = new TreeMap<Name, Name>();
		TreeMap<ComparableName, ComparableName> treeMap = new TreeMap<ComparableName, ComparableName>();
		treeMap.put(cn1, cn1);
		treeMap.put(cn2, null);
		// TreeMap does not allow null keys
		//treeMap.put(null, null);
		LOG.info("TreeMap: {}", treeMap);
	}

	static void testHashSet() {
		HashSet<String> set = new HashSet<String>();
		set.add("a");
		set.add("a");
		set.add("c");
		set.add("b");
		set.add(null);
		set.add(null);
		System.out.println(PrintTools.print(set));
	}

	static void testHashMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("a", "aa");
		map.put("c", "cc");
		map.put("b", "bb");
		map.put(null, "nothing");
		// The methods keySet(), values(), entrySet() all return the entries in the same order
		System.out.println(PrintTools.print(map.keySet()));
		System.out.println(PrintTools.print(map.values()));
		System.out.println(PrintTools.print(map.entrySet()));
	}

	static void testTreeSet() {
		{
			Set<ComparableName> set = new TreeSet<ComparableName>();
			set.add(new ComparableName("c", 0));
			set.add(new ComparableName("a", 1));
			set.add(new ComparableName("b", 2));
			set.add(new ComparableName("b", 3));
			// Without null-aware comparator, null values are not allowed
			try {
				set.add(null);
				throw new AssertionError();
			} catch (Exception e) {
			}
			LOG.info("TreeSet: {}", set);
		}

		TreeSet<String> set = new TreeSet<String>();
		set.add("a");
		set.add("a");
		set.add("c");
		set.add("b");
		// java.lang.NullPointerException
		// set.add(null);
		System.out.println(PrintTools.print(set));

		Comparator<String> comparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (o1 != null && o2 != null) {
					return o1.compareTo(o2);
				} else if (o1 == null) {
					if (o2 == null) {
						return 0;
					} else {
						return -1;
					}
				} else {
					return 1;
				}
			}
		};
		set = new TreeSet<String>(comparator);
		set.add("a");
		set.add("a");
		set.add("c");
		set.add("b");
		set.add(null);
		System.out.println(PrintTools.print(set));

		TreeSet<ComparableName> set2 = new TreeSet<ComparableName>();
		ComparableName n = new ComparableName("a", 2);
		set2.add(new ComparableName("a", 1));
		set2.add(n);
		set2.add(new ComparableName("c", 3));
		set2.add(new ComparableName("b", 4));
		System.out.println(PrintTools.print(set2));

		// Set does not offer a put method:
		// - functionality without return value
		//        set2.remove(n);
		//        set2.add(n);

		// - functionality with return value
		ComparableName old = set2.floor(n);
		if (old == null || !old.equals(n)) {
			old = null;
		}
		if (old != null) {
			set2.remove(old);
		}
		set2.add(n);
		System.out.println(old);
		System.out.println(PrintTools.print(set2));
	}

	//-- Other collection libraries

	static void testGuavaBiMap() {
		BiMap<String, String> zipToCity = HashBiMap.create();
		zipToCity.put("4000", "Basel");
		zipToCity.put("5000", "Aarau");
		// Duplicate values are not allowed
		//zipToCity.put("5001", "Aarau");

		BiMap<String, String> cityToZip = zipToCity.inverse();

	}

	static void testCommonsCollectionsBidiMap() {
		DualTreeBidiMap<String, String> zipToCity = new DualTreeBidiMap();
		zipToCity.put("4000", "Basel");
		zipToCity.put("5000", "Aarau");
		// Duplicate values are not allowed
		zipToCity.put("5001", "Aarau");
		System.out.println(zipToCity);

		BidiMap<String, String> cityToZip = zipToCity.inverseBidiMap();
		System.out.println(cityToZip);
		System.out.println(cityToZip.getKey("Aarau"));
	}

}
