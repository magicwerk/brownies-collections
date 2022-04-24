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
import java.util.Map.Entry;

import org.magictest.client.Assert;
import org.magictest.client.Capture;
import org.magicwerk.brownies.collections.KeyCollectionImpl.KeyMap;
import org.magicwerk.brownies.collections.TestHelper.Ticket;
import org.magicwerk.brownies.core.CollectionTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.slf4j.Logger;

/**
 * Show internal structure of key collections.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class KeyCollectionsStructTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		test();
	}

	static void test() {

		testKeyCollection();
		testKeyListImpl();
		testKeySet();

		//		{
		//			Key1List<Ticket, Integer> l = new Key1List.Builder<Ticket, Integer>().withKey1Map(Ticket.IdMapper).withOrderByKey1(true).build();
		//			l.contains(null);
		//		}
		//
		//		{
		//			// Test
		//			KeyList<Ticket> list = new KeyList.Builder<Ticket>().withPrimaryElem().withOrderByElem(true).build();
		//			list.add(t1);
		//			// TODO Trying to add duplicate throws:
		//			//Exception in thread "main" java.lang.IllegalStateException: Invalid index for sorted list
		//			//at org.magicwerk.brownies.collections.KeyCollectionImpl.errorInvalidIndex(KeyCollectionImpl.java:1894)
		//			// It should throw a DuplicateKeyException
		//			// TODO it should be configurable in Builder to have Set behavior, i.e. silently ignoring duplicates
		//			list.add(t1);
		//			printKeyList(list);
		//			LOG.info("{}", list);
		//		}

	}

	// Show that automatic deduction of static parameters does not work with builders

	static class Built<E> {
		static <E> Built<E> create() {
			return null;
		}
	}

	static class Builder<E> {
		static <E> Builder<E> create() {
			return null;
		}

		Built<E> build() {
			return null;
		}
	}

	static void testBuilder() {
		// This does compile
		Built<String> built = Built.create();

		// This does not compile
		//Built<String> built2 = Builder.create().build();
	}

	//

	static Ticket t1 = new Ticket(1, "extId1", "text1");
	static Ticket t2 = new Ticket(2, "extId2", "text2");
	static Ticket t3 = new Ticket(3, "extId3", "text3");
	static List<Ticket> ts = GapList.create(t1, t2, t3);

	@Capture
	public static void testKeyCollection() {

		// -- KeyCollection
		// HashMap
		{
			System.out.println("-- not sorted");
			KeyCollection<Ticket> coll = new KeyCollection.Builder<Ticket>().build();
			coll.add(t1);
			printKeyCollectionImpl(coll);
		}
		// TreeMap
		{
			System.out.println("-- sorted");
			KeyCollection<Ticket> coll = new KeyCollection.Builder<Ticket>().withElemSort(true).build();
			coll.add(t1);
			printKeyCollectionImpl(coll);
		}
		// HashMap
		{
			System.out.println("-- with duplicates ");
			KeyCollection<Ticket> coll = new KeyCollection.Builder<Ticket>().build();
			coll.add(t1);
			coll.add(t1);
			printKeyCollectionImpl(coll);

			Assert.assertTrue(coll.size() == 2);
		}
		// TreeMap
		{
			System.out.println("-- sorted with duplicates");
			KeyCollection<Ticket> coll = new KeyCollection.Builder<Ticket>().withElemSort(true).build();
			coll.add(t1);
			coll.add(t1);
			printKeyCollectionImpl(coll);
		}
		// ElemCount
		{
			System.out.println("-- with elem count");
			KeyCollection<Ticket> coll = new KeyCollection.Builder<Ticket>().withElemCount(true).build();
			coll.add(t1);
			printKeyCollectionImpl(coll);
		}

		// -- Key1Collection
		// Element: null, Key: TreeMap (order)
		{
			Key1Collection<Ticket, Integer> coll = new Key1Collection.Builder<Ticket, Integer>().withKey1Map(Ticket.IdMapper).withKey1Sort(true).build();
			coll.addAll(ts);
			printKeyCollectionImpl(coll);

			// As there is no element set, all methods work over the keys TreeMap.
			// So contains() becomes TreeMap.containsValue()
			Assert.assertTrue(coll.contains(t1));
			Assert.assertTrue(!coll.contains(null));
		}
		// Element: HashMap (order), Key: TreeMap
		{
			Key1Collection<Ticket, Integer> coll = new Key1Collection.Builder<Ticket, Integer>().withElemSet().withKey1Map(Ticket.IdMapper).withKey1Sort(true)
					.build();
			coll.addAll(ts);
			printKeyCollectionImpl(coll);
			// Order of HashMap can change
			//System.out.println(coll);
		}
		// Element: HashMap, Key: TreeMap (order)
		{
			Key1Collection<Ticket, Integer> coll = new Key1Collection.Builder<Ticket, Integer>().withElemSet().withKey1Map(Ticket.IdMapper)
					.withOrderByKey1(true).build();
			coll.addAll(ts);
			printKeyCollectionImpl(coll);
			//System.out.println(coll);
		}
	}

	@Capture
	public static void testKeySet() {

		// -- KeySet
		// HashMap
		{
			System.out.println("-- not sorted");
			KeySet<Ticket> coll = new KeySet.Builder<Ticket>().build();
			coll.add(t1);
			printKeyCollectionImpl(coll);
		}
		// TreeMap
		{
			System.out.println("-- sorted");
			KeySet<Ticket> coll = new KeySet.Builder<Ticket>().withElemSort(true).build();
			coll.add(t1);
			printKeyCollectionImpl(coll);
		}
		// HashMap
		{
			System.out.println("-- no duplicates ");
			KeySet<Ticket> coll = new KeySet.Builder<Ticket>().build();
			coll.add(t1);
			coll.add(t1);
			printKeyCollectionImpl(coll);

			Assert.assertTrue(coll.size() == 1);
		}

		// -- Key1Set
		// Element: null, Key: TreeMap (order)
		{
			Key1Set<Ticket, Integer> coll = new Key1Set.Builder<Ticket, Integer>().withKey1Map(Ticket.IdMapper).withKey1Sort(true).build();
			coll.addAll(ts);
			printKeyCollectionImpl(coll);

			// As there is no element set, all methods work over the keys TreeMap.
			// So contains() becomes TreeMap.containsValue()
			Assert.assertTrue(coll.contains(t1));
			Assert.assertTrue(!coll.contains(null));
		}
		// Element: HashMap (order), Key: TreeMap
		{
			Key1Set<Ticket, Integer> coll = new Key1Set.Builder<Ticket, Integer>().withElemSet().withKey1Map(Ticket.IdMapper).withKey1Sort(true)
					.build();
			coll.addAll(ts);
			printKeyCollectionImpl(coll);
			// Order of HashMap can change
			//System.out.println(coll);
		}
		// Element: HashMap, Key: TreeMap (order)
		{
			Key1Set<Ticket, Integer> coll = new Key1Set.Builder<Ticket, Integer>().withElemSet().withKey1Map(Ticket.IdMapper)
					.withOrderByKey1(true).build();
			coll.addAll(ts);
			printKeyCollectionImpl(coll);
			//System.out.println(coll);
		}
	}

	static IList<KeyListImpl> KeyListImplsInteger = GapList.create(new KeyList.Builder<Integer>().withListType(int.class).build(),
			new KeyList.Builder<Integer>().withListType(int.class).withListBig(true).build(),

			new KeyList.Builder<Integer>().withOrderByElem(true).withListType(int.class).build(),
			new KeyList.Builder<Integer>().withOrderByElem(true).withListType(int.class).withListBig(true).build(),

			new KeyList.Builder<Integer>().withElemSet().withListType(int.class).build(),
			new KeyList.Builder<Integer>().withElemSet().withListType(int.class).withListBig(true).build(),

			new KeyList.Builder<Integer>().withOrderByElem(true).withElemSet().withListType(int.class).build(),
			new KeyList.Builder<Integer>().withOrderByElem(true).withElemSet().withListType(int.class).withListBig(true).build());

	static IList<KeyListImpl> KeyListImplsTicket = GapList.create(new KeyList.Builder<Ticket>().build(),
			new KeyList.Builder<Ticket>().withListBig(true).build(),

			new KeyList.Builder<Ticket>().withOrderByElem(true).build(), new KeyList.Builder<Ticket>().withOrderByElem(true).withListBig(true).build(),

			new KeyList.Builder<Ticket>().withElemSet().build(), new KeyList.Builder<Ticket>().withElemSet().withListBig(true).build(),

			new KeyList.Builder<Ticket>().withOrderByElem(true).withElemSet().build(),
			new KeyList.Builder<Ticket>().withOrderByElem(true).withElemSet().withListBig(true).build(),

			// Key1List
			new Key1List.Builder<Ticket, Integer>().withKey1Map(Ticket.IdMapper).build(),
			new Key1List.Builder<Ticket, Integer>().withKey1Map(Ticket.IdMapper).withKey1Sort(true).build(),
			new Key1List.Builder<Ticket, Integer>().withKey1Map(Ticket.IdMapper).withOrderByKey1(true).build(),
			new Key1List.Builder<Ticket, Integer>().withElemSet().withKey1Map(Ticket.IdMapper).build(),

			// Key2List
			new Key2List.Builder<Ticket, Integer, String>().withElemSet().withKey1Map(Ticket.IdMapper).withKey2Map(Ticket.ExtIdMapper).build(),
			new Key2List.Builder<Ticket, Integer, String>().withElemSet().withKey1Map(Ticket::getId).//withPrimaryKey1().
					withOrderByKey1(int.class).withKey2Map(Ticket::getExtId).//withUniqueKey2().
					withKey2Sort(true).build());

	@Capture
	public static void testKeyListImpl() {

		System.out.println("-- KeyList: list only");
		{
			// GapList
			KeyList<Ticket> list = new KeyList.Builder<Ticket>().build();
			list.add(t1);
			printKeyList(list);
		}
		{
			// BigList
			KeyList<Ticket> list = new KeyList.Builder<Ticket>().withListBig(true).build();
			list.add(t1);
			printKeyList(list);
		}
		{
			// IntObjGapList
			KeyList<Integer> list = new KeyList.Builder<Integer>().withListType(int.class).build();
			list.add(1);
			printKeyList(list);

			KeyList<Integer> list2 = new KeyList.Builder<Integer>().withListType(int.class).withConstraint(i -> i > 0).build();
			list2.add(1);
			// Constraint violation
			//list2.add(-1);
		}
		{
			// IntObjBigList
			KeyList<Integer> list = new KeyList.Builder<Integer>().withListType(int.class).withListBig(true).build();
			list.add(1);
			printKeyList(list);
		}

		System.out.println("-- KeyList: sorted list only");
		{
			// GapList
			KeyList<Ticket> list = new KeyList.Builder<Ticket>().withOrderByElem(true).build();
			list.add(t1);
			printKeyList(list);
		}
		{
			// BigList
			KeyList<Ticket> list = new KeyList.Builder<Ticket>().withOrderByElem(true).withListBig(true).build();
			list.add(t1);
			printKeyList(list);
		}
		{
			// IntObjGapList
			// Use can also use withElemOrderBy(int.class) or even withElemOrderBy(float.class) with the same effect
			KeyList<Integer> list = new KeyList.Builder<Integer>().withOrderByElem(true).withListType(int.class).build();
			list.add(1);
			printKeyList(list);
		}
		{
			// IntObjBigList
			KeyList<Integer> list = new KeyList.Builder<Integer>().withOrderByElem(true).withListType(int.class).withListBig(true).build();
			list.add(1);
			printKeyList(list);
		}

		System.out.println("-- KeyList: list + elem set");
		{
			// GapList
			KeyList<Ticket> list = new KeyList.Builder<Ticket>().withElemSet().build();
			list.add(t1);
			printKeyList(list);
		}
		{
			// BigList
			KeyList<Ticket> list = new KeyList.Builder<Ticket>().withElemSet().withListBig(true).build();
			list.add(t1);
			printKeyList(list);
		}
		{
			// IntObjGapList
			KeyList<Integer> list = new KeyList.Builder<Integer>().withElemSet().withListType(int.class).build();
			list.add(1);
			printKeyList(list);
		}
		{
			// IntObjBigList
			KeyList<Integer> list = new KeyList.Builder<Integer>().withElemSet().withListType(int.class).withListBig(true).build();
			list.add(1);
			printKeyList(list);
		}

		System.out.println("-- KeyList: sorted list + elem set");
		{
			// GapList
			KeyList<Ticket> list = new KeyList.Builder<Ticket>().withOrderByElem(true).withElemSet().build();
			list.add(t1);
			printKeyList(list);
		}
		{
			// BigList
			KeyList<Ticket> list = new KeyList.Builder<Ticket>().withOrderByElem(true).withElemSet().withListBig(true).build();
			list.add(t1);
			printKeyList(list);
		}
		{
			// IntObjGapList
			KeyList<Integer> list = new KeyList.Builder<Integer>().withOrderByElem(true).withElemSet().withListType(int.class).build();
			list.add(1);
			printKeyList(list);
		}
		{
			// IntObjBigList
			KeyList<Integer> list = new KeyList.Builder<Integer>().withOrderByElem(true).withElemSet().withListType(int.class).withListBig(true).build();
			list.add(1);
			printKeyList(list);
		}

		// -- Key1List
		System.out.println("-- Key1List");
		// List with key
		{
			Key1List<Ticket, Integer> list = new Key1List.Builder<Ticket, Integer>().withKey1Map(Ticket.IdMapper).build();
			list.addAll(ts);
			printKeyList(list);
		}
		// List with sorted key
		{
			Key1List<Ticket, Integer> list = new Key1List.Builder<Ticket, Integer>().withKey1Map(Ticket.IdMapper).withKey1Sort(true).build();
			list.addAll(ts);
			printKeyList(list);
		}
		// List sorted by key
		{
			Key1List<Ticket, Integer> list = new Key1List.Builder<Ticket, Integer>().withKey1Map(Ticket.IdMapper).withOrderByKey1(true).build();
			list.addAll(ts);
			printKeyList(list);
		}
		// List with key
		{
			Key1List<Ticket, Integer> list = new Key1List.Builder<Ticket, Integer>().withElemSet().withKey1Map(Ticket.IdMapper).build();
			list.addAll(ts);
			printKeyList(list);
		}

		// -- Key2List
		System.out.println("-- Key2List");
		// List with key
		{
			Key2List<Ticket, Integer, String> list = new Key2List.Builder<Ticket, Integer, String>().withElemSet().withKey1Map(Ticket.IdMapper)
					.withKey2Map(Ticket.ExtIdMapper).build();
			list.addAll(ts);
			printKeyList(list);
		}

		{
			Key2List<Ticket, Integer, String> useItAll = new Key2List.Builder<Ticket, Integer, String>().withElemSet().withPrimaryKey1Map(Ticket::getId)
					.withOrderByKey1(int.class).withUniqueKey2Map(Ticket::getExtId).withKey2Sort(true).build();

			useItAll.addAll(ts);
			//Ticket t4 = new Ticket(4, null, "text4");
			//useItAll.add(t4);
			//Ticket t5 = new Ticket(5, null, "text5");
			//useItAll.add(t5);

			printKeyList(useItAll);
		}

	}

	/**
	 * Print content of KeyListImpl.
	 *
	 * @param kli KeyListImpl
	 */
	static void printKeyList(KeyListImpl<?> kli) {
		IList<?> ilist = kli.list;
		KeyMap<?, ?>[] keyMaps = kli.keyColl.keyMaps;

		System.out.println(formatClassName(kli));
		System.out.println("- IList: " + formatClassName(ilist) + ": " + formatClassName(ilist.getFirst()));
		System.out.println(formatKeyMaps(keyMaps));
		System.out.println();
	}

	/**
	 * Print content of KeyCollectionImpl.
	 *
	 * @param kci KeyCollectionImpl
	 */
	static void printKeyCollectionImpl(KeyCollectionImpl<?> kci) {
		KeyMap<?, ?>[] keyMaps = kci.keyMaps;

		System.out.println(formatClassName(kci));
		System.out.println(formatKeyMaps(keyMaps));
		System.out.println();
	}

	/**
	 * Format content of KeyMaps.
	 *
	 * @param keyMaps KeyMaps
	 */
	static String formatKeyMaps(KeyMap[] keyMaps) {
		if (keyMaps == null) {
			return "- no ElemSet / KeyMaps";
		}
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < keyMaps.length; i++) {
			if (i == 0) {
				buf.append("- Elem Set: ");
			} else {
				buf.append("\n- Key Map " + i + ": ");
			}
			KeyMap keyMap = keyMaps[i];
			if (keyMap == null) {
				buf.append("null");
			} else {
				if (keyMap.keysMap != null) {
					Map map = keyMap.keysMap;
					Entry entry = CollectionTools.getFirstEntry(map);
					String keyStr = (entry != null) ? formatClassName(entry.getKey()) : "undefined";
					String valStr = (entry != null) ? formatClassName(entry.getValue()) : "undefined";
					buf.append(formatClassName(keyMap.keysMap) + ": " + keyStr + " -> " + valStr);
				} else {
					IList list = keyMap.keysList;
					String elemStr = (list != null) ? formatClassName(list.peekFirst()) : "undefined";
					buf.append(formatClassName(keyMap.keysList) + ": " + elemStr);
				}
			}
		}
		return buf.toString();
	}

	static String formatClassName(Object obj) {
		if (obj == null) {
			return "null";
		} else {
			return obj.getClass().getSimpleName();
		}
	}

}
