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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.magictest.client.Assert;
import org.magictest.client.Capture;
import org.magictest.client.Format;
import org.magictest.client.Report;
import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.TestHelper.ComparableName;
import org.magicwerk.brownies.collections.TestHelper.Name;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.slf4j.Logger;

/**
 * Test of KeyCollection.
 *
 * @author Thomas Mauch
 */
public class KeyCollectionTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		test();
	}

	static void test() {
		//testIterator();
		//testPut();
		testElemCount();
	}

	@Capture
	public static void testElemCount() {
		KeyCollection<ComparableName> coll;

		coll = new KeyCollection.Builder<ComparableName>().withElemCount(false).withElemSort(true).build();
		coll.add(new ComparableName("d", 0));
		coll.add(new ComparableName("d", 1));
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("aa", 3));
		LOG.info("withElemCount(false): {}", coll);

		coll = new KeyCollection.Builder<ComparableName>().withElemCount(true).withElemSort(false).build();
		coll.add(new ComparableName("d", 0));
		coll.add(new ComparableName("d", 1));
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("aa", 3));
		LOG.info("withElemCount(true): {}", coll);

		Set<ComparableName> elems = coll.getDistinct();
		Map<ComparableName, Integer> countedElems = new TreeMap<>();
		for (ComparableName elem : elems) {
			int count = coll.getCount(elem);
			countedElems.put(elem, count);
		}
		LOG.info("countedElems: {}", countedElems);

		// Error: invalidate not supported
		//coll.invalidate(null);

		coll.remove(new ComparableName("d", 1));
		LOG.info("removed(d): {}", coll);
		coll.remove(new ComparableName("d", 1));
		LOG.info("removed(d): {}", coll);
		coll.remove(new ComparableName("d", 1));
		LOG.info("removed(d): {}", coll);
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

	//    static void testAddAll() {
	//        List<String> l1 = Arrays.asList("a", "b");
	//        List<String> l2 = Arrays.asList("c", "d");
	//
	//        BagCollection<String> sl1 = BagCollection.create();
	//        sl1.addAll(l1);
	//
	//        BagCollection<String> sl2 = BagCollection.create(l1);
	//    }

	static String formatBagCollection(KeyCollection<?> coll) {
		return String.format("Size: %d, Content: %s", coll.size(), coll);
	}

	//traceMethod="org.magicwerk.brownies.collections.BagCollection$Builder#build"
	@Trace(traceClass = "org.magicwerk.brownies.collections.BagCollection$Builder", traceMethod = "build", formats = {
			@Format(apply = Trace.RESULT, formatter = "formatBagCollection") })
	public static void testNew() {
		new KeyCollection.Builder<String>().build();
		new KeyCollection.Builder<String>().withCapacity(5).build();
		new KeyCollection.Builder<String>().withContent("a", "b").build();
		new KeyCollection.Builder<String>().withContent(Arrays.asList("a", "b", "c")).build();
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSet() {
		KeyCollection<Name> coll = new KeyCollection.Builder<Name>().withElemNull(true).withElemDuplicates(false).build();
		coll.add(new Name("a", 0));
		coll.add(new Name("b", 1));
		// Only one null value allowed (existing is preserved)
		coll.add(new Name("a", 2));
		// Only one null value allowed
		coll.add(null);
		coll.add(null);
		LOG.info("BagCollection: {}", coll);

		Set<Name> set = new HashSet<Name>();
		set.add(new Name("a", 0));
		set.add(new Name("b", 1));
		set.add(new Name("a", 2));
		// Only one null value allowed
		set.add(null);
		set.add(null);
		LOG.info("HashSet: {}", set);

		Assert.assertTrue(coll.equals(set));
	}

	@Trace(parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS | Trace.RESULT)
	public static void testPut() {
		KeyCollection<ComparableName> coll;

		// Sorted, no duplicates
		Report.setDescription("put (sorted, no duplicates)");
		coll = new KeyCollection.Builder<ComparableName>().withElemSort(true).withOrderByElem(true).withElemDuplicates(false).build();
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("c", 3));

		// Replace
		coll.put(new ComparableName("b", 4));
		// Add
		coll.put(new ComparableName("d", 5));

		// Sorted, duplicates
		Report.setDescription("put (sorted, duplicates)");
		coll = new KeyCollection.Builder<ComparableName>().withElemSort(true).withOrderByElem(true).withElemDuplicates(true).build();
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("c", 3));

		// Replace
		coll.put(new ComparableName("b", 4));
		// Add
		coll.put(new ComparableName("d", 5));

		// Not sorted, no duplicates
		Report.setDescription("put (not sorted, no duplicates)");
		coll = new KeyCollection.Builder<ComparableName>().withElemSort(false).withElemDuplicates(false).build();
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("c", 3));

		// Replace
		coll.put(new ComparableName("b", 4));
		// Add
		coll.put(new ComparableName("d", 5));

		// Not sorted, duplicates
		Report.setDescription("put (not sorted, duplicates)");
		coll = new KeyCollection.Builder<ComparableName>().withElemSort(false).withElemDuplicates(true).build();
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("c", 3));

		// Replace
		coll.put(new ComparableName("b", 4));
		// Add
		coll.put(new ComparableName("d", 5));

		// Check rollback
		coll = new KeyCollection.Builder<ComparableName>().withElemSort(false).withElemDuplicates(true).withConstraint(e -> e.value != 99).build();

		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("c", 3));

		// Replace fails
		coll.put(new ComparableName("b", 99));
		// Add
		coll.put(new ComparableName("d", 5));
		// Remove
		System.out.println(coll.size() + " " + coll);
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSetWithNoNulls() {
		KeyCollection<Name> coll = new KeyCollection.Builder<Name>().withElemDuplicates(false).build();
		coll.add(new Name("a", 0));
		coll.add(new Name("b", 1));
		coll.add(new Name("a", 2));
		coll.add(null);
		LOG.info("BagCollection: {}", coll);
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSortedCollection() {
		// For a sorted set, the entries must be comparable
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().withElemNull(true).withElemSort(true).build();
		coll.add(new ComparableName("c", 0));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("b", 3));
		coll.add(null);
		LOG.info("BagCollection: {}", coll);
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSortedSet() {
		// For a sorted set, the entries must be comparable
		// Add
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().withElemDuplicates(false).withElemSort(true).build();
		coll.add(new ComparableName("c", 0));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("b", 3));
		// Null elements are per default not allowed
		coll.add(null);
		LOG.info("BagCollection: {}", coll);

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

		//Assert.assertTrue(coll.equals(set));
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSortedCollectionByComparable() {
		// For a sorted set, the entries must be comparable
		KeyCollection<Name> coll = new KeyCollection.Builder<Name>().withElemSort(Name.Comparator).build();
		coll.add(new Name("c", 0));
		coll.add(new Name("a", 1));
		coll.add(new Name("b", 2));
		coll.add(new Name("b", 3));
		// Null elements are not allowed
		coll.add(null);
		LOG.info("BagCollection: {}", coll);
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSortedCollectionWithNull() {
		// For a sorted set, the entries must be comparable
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().withElemNull(true).withElemDuplicates(true).withElemSort(true).build();
		coll.add(new ComparableName("c", 0));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("b", 3));
		coll.add(null);
		LOG.info("BagCollection: {}", coll);
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSortedSetWithDuplicates() {
		// For a sorted set, the entries must be comparable
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().withElemSort(true).withElemDuplicates(true).build();
		coll.add(new ComparableName("c", 0));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("b", 3));
		// Null elements are per default not allowed
		coll.add(null);
		LOG.info("BagCollection: {}", coll);

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

		//Assert.assertTrue(coll.equals(alist));
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSortedSetWithNulls() {
		// For a sorted set, the entries must be comparable
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().withElemSort(true).withElemNull(true).build();
		coll.add(new ComparableName("c", 0));
		coll.add(new ComparableName("a", 1));
		coll.add(null);
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("b", 3));
		coll.add(null);
		LOG.info("BagCollection: {}", coll);
	}

	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsSortedSetWithNullsFirst() {
		// For a sorted set, the entries must be comparable
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().withElemSort(null, true).withElemNull(true).build();
		coll.add(new ComparableName("c", 0));
		coll.add(new ComparableName("a", 1));
		coll.add(null);
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("b", 3));
		coll.add(null);
		LOG.info("BagCollection: {}", coll);
	}

	// The columns in a table must have a unique key, null is not allowed.
	// If an attempt is made to insert a duplicate, an error should be raised.
	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsPrimaryKey() {
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().withElemNull(false).withElemDuplicates(false).build();
		coll.add(new ComparableName("c", 0));
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("b", 2));
		// Error: duplicates are not allowed
		coll.add(new ComparableName("b", 3));
		// Null elements are not allowed
		coll.add(null);
		LOG.info("BagCollection: {}", coll);
	}

	// The columns in a table must have a unique key, null is not allowed.
	// If an attempt is made to insert a duplicate, an error should be raised.
	@Trace(traceMethod = "add", result = Trace.THIS)
	public static void testAsUniqueKey() {
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().withElemNull(true).withElemDuplicates(false, true).build();
		coll.add(new ComparableName("c", 0));
		coll.add(null);
		coll.add(new ComparableName("a", 1));
		coll.add(new ComparableName("b", 2));
		// Error: duplicates are not allowed
		coll.add(new ComparableName("b", 3));
		coll.add(null);
		LOG.info("BagCollection: {}", coll);
	}

	//

	//    public static void testBagCollectionSorted2() {
	//        BagCollection<Name> coll = new BagCollection.Builder<Name>().withElemSort(true).build();
	//        //java.lang.ClassCastException: org.magicwerk.brownies.collections.KeyListTest$Name cannot be cast to java.lang.Comparable
	//        //coll.add(new Name("d"));
	//        coll.add(new ComparableName("d"));
	//        coll.add(new ComparableName("d"));
	//        coll.add(new ComparableName("b"));
	//        coll.add(new ComparableName("a"));
	//        System.out.println(coll);
	//        coll.set(1, new ComparableName("ax"));
	//        System.out.println(coll);
	//        coll.remove(1);
	//        System.out.println(coll);
	//    }

	static void testBagCollectionCopy() {
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().build();
		coll.add(new ComparableName("a"));
		coll.add(new ComparableName("b"));
		coll.add(new ComparableName("c"));
		System.out.println(coll);

		KeyCollection<ComparableName> copy = coll.copy();
		System.out.println(copy);
		KeyCollection<ComparableName> crop = coll.crop();
		System.out.println(crop);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testRemove() {
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().build();
		coll.add(new ComparableName("a", 0));
		coll.add(new ComparableName("b", 1));

		coll.remove(new ComparableName("a", 2));
		coll.remove(null);
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testClone() {
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().build();
		coll.add(new ComparableName("d", 0));
		coll.add(new ComparableName("d", 1));
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 3));
		coll.clone();

		coll = new KeyCollection.Builder<ComparableName>().withElemSort(true).build();
		coll.add(new ComparableName("d", 0));
		coll.add(new ComparableName("d", 1));
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 3));
		coll.clone();
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testIterator() {
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().build();
		coll.iterator();
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testCopy() {
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().build();
		coll.add(new ComparableName("d", 0));
		coll.add(new ComparableName("d", 1));
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 3));
		coll.copy();

		coll = new KeyCollection.Builder<ComparableName>().withElemSort(true).build();
		coll.add(new ComparableName("d", 0));
		coll.add(new ComparableName("d", 1));
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 3));
		coll.copy();
	}

	@Trace(parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testCrop() {
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().build();
		coll.add(new ComparableName("d", 0));
		coll.add(new ComparableName("d", 1));
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 3));
		coll.crop();

		coll = new KeyCollection.Builder<ComparableName>().withElemSort(true).build();
		coll.add(new ComparableName("d", 0));
		coll.add(new ComparableName("d", 1));
		coll.add(new ComparableName("b", 2));
		coll.add(new ComparableName("a", 3));
		coll.crop();
	}

	@Trace(traceMethod = "/.+/", parameters = Trace.THIS | Trace.ALL_PARAMS, result = Trace.THIS | Trace.RESULT)
	public static void testKey() {
		ComparableName a = new ComparableName("a");
		ComparableName d = new ComparableName("d");
		ComparableName x = new ComparableName("x");
		ComparableName n = null;
		KeyCollection<ComparableName> coll;

		coll = getCollection();
		coll.getAll(d);
		coll.getCount(d);
		coll.getDistinct();

		coll.getAll(x);
		coll.getCount(x);
		coll.remove(x);
		coll.removeAll(x);

		coll.getAll(n);
		coll.getCount(n);
		coll.remove(n);
		coll.removeAll(n);

		coll.remove(a);
		coll.removeAll(d);

		coll.remove(a);
		coll.removeAll(d);

		coll.add(a);
		coll.removeAll(a);

		Report.printStep("-- Sort --");
		coll = getCollectionSort();
		coll.getAll(d);
		coll.getCount(d);
		coll.getDistinct();

		coll.getAll(x);
		coll.getCount(x);
		coll.remove(x);
		coll.removeAll(x);

		coll.getAll(n);
		coll.getCount(n);
		coll.remove(n);
		coll.removeAll(n);

		coll.remove(a);
		coll.removeAll(d);

		coll.add(a);
		coll.removeAll(a);

		Report.printStep("-- Null --");
		coll = getCollectionNull();
		coll.getAll(n);
		coll.getCount(n);

		coll.remove(n);
		coll.removeAll(n);

		Report.printStep("-- NullSort --");
		coll = getCollectionNullSort();
		coll.getAll(n);
		coll.getCount(n);

		coll.remove(n);
		coll.removeAll(n);
	}

	static KeyCollection<ComparableName> getCollection() {
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().withElemDuplicates(true).build();
		coll.add(new ComparableName("d"));
		coll.add(new ComparableName("b"));
		coll.add(new ComparableName("a"));
		coll.add(new ComparableName("d", 1));
		return coll;
	}

	static KeyCollection<ComparableName> getCollectionSort() {
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().withElemDuplicates(true).withElemSort(true).build();
		coll.add(new ComparableName("d"));
		coll.add(new ComparableName("b"));
		coll.add(new ComparableName("a"));
		coll.add(new ComparableName("d", 1));
		return coll;
	}

	static KeyCollection<ComparableName> getCollectionNull() {
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().withElemNull(true).withElemDuplicates(true).build();
		coll.add(new ComparableName("d"));
		coll.add(null);
		coll.add(new ComparableName("b"));
		coll.add(new ComparableName("a"));
		coll.add(null);
		coll.add(new ComparableName("d", 1));
		return coll;
	}

	static KeyCollection<ComparableName> getCollectionNullSort() {
		KeyCollection<ComparableName> coll = new KeyCollection.Builder<ComparableName>().withElemNull(true).withElemDuplicates(true).withElemSort(true).build();
		coll.add(new ComparableName("d"));
		coll.add(null);
		coll.add(new ComparableName("b"));
		coll.add(new ComparableName("a"));
		coll.add(null);
		coll.add(new ComparableName("d"));
		return coll;
	}

}
