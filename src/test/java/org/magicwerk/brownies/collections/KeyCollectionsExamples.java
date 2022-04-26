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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.magicwerk.brownies.collections.helper.NaturalComparator;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.magicwerk.brownies.collections.primitive.IntObjGapList;

/**
 * Examples for the use of key collections.
 *
 * @author Thomas Mauch
 */
public class KeyCollectionsExamples {

	public static void main(String[] args) {
		test();
	}

	static void test() {
		testKeySet();

		//testExamples();
	}

	static void testExamples() {
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

		testKeySet();
		testKey1Set();
		testKey2Set();

		testAsSet();
		testAsMap();
	}

	//---- Examples

	//-- GapList

	static void testGapList() {
		// GapList replaces all ArrayList, LinkedList, ArrayDeque
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
	}

	static void testKeyListWithConstraint() {
		// A list with a user defined constraint
		KeyList<String> list = new KeyList.Builder<String>().withConstraint(s -> s.equals(s.toUpperCase())).build();
	}

	static void testKeyListWithMaxSize() {
		// A list which can store a maximum of 5 elements
		KeyList<String> list = new KeyList.Builder<String>().withMaxSize(5).build();
	}

	static void testKeyListWithWindowSize() {
		// A list which can store a maximum of 5 elements.
		// If an additional element is added, the first element is automatically removed first.
		KeyList<String> list = new KeyList.Builder<String>().withWindowSize(5).build();
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
		KeyList<String> list = new KeyList.Builder<String>().withBeforeInsertTrigger(e -> System.out.println("before insert: " + e))
				.withAfterDeleteTrigger(e -> System.out.println("after delete: " + e)).build();
	}

	//-- KeyList with element set

	static void testKeyListWithElemSet() {
		// List with set for fast access to elements (all values allowed)
		KeyList<String> list = new KeyList.Builder<String>().withElemSet().build();
	}

	static void testKeyListWithElemNull() {
		// List with set for fast access to elements (no null values allowed)
		KeyList<String> list = new KeyList.Builder<String>().withElemNull(false).build();
	}

	static void testKeyListWithElemDuplicates() {
		// List with set for fast access to elements (no duplicate values allowed)
		KeyList<String> list = new KeyList.Builder<String>().withElemDuplicates(false).build();
	}

	static void testKeyListWithPrimaryElem() {
		// List with set for fast access to elements (no duplicate or null values allowed)
		KeyList<String> list = new KeyList.Builder<String>().withPrimaryElem().build();
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
		list = new KeyList.Builder<String>().withElemSort((s1, s2) -> (s1.toLowerCase().compareTo(s2.toLowerCase())), false).build();
	}

	static void testKeyListWithElemOrderBy() {
		// List with set for fast access to elements.
		// The element set and the list are sorted by the natural comparator.
		KeyList<String> list = new KeyList.Builder<String>().withOrderByElem(true).build();

		// Sort element set and list by specified comparator, nulls last
		Comparator<String> comparator = NaturalComparator.INSTANCE(String.class).reversed();
		list = new KeyList.Builder<String>().withElemSort(comparator, false).withOrderByElem(true).build();
	}

	static void testKeyListWithElemOrderByClass() {
		// List with set for fast access to elements.
		// The set is realized as sorted list of primitive values.
		KeyList<Integer> list = new KeyList.Builder<Integer>().withOrderByElem(int.class).build();
	}

	// Key1List

	static void testKey1List() {
		// Use Key1List to implement a list of column names with a defined
		// order and unique names which can be accessed fast
		Key1List<Column, String> list = new Key1List.Builder<Column, String>().withPrimaryKey1Map(Column::getName).build();
	}

	// Key2List

	static void testKey2List() {
		// Use Key2List to maintain a list of tickets.
		// Each ticket has unique ID (primary key) and optional but also unique external ID.
		Key2List<Ticket, String, String> list = new Key2List.Builder<Ticket, String, String>().withConstraint(t -> t.getText() != null)
				.withPrimaryKey1Map(Ticket::getId)
				.withUniqueKey2Map(Ticket::getExtId).build();
	}

	// KeyCollection

	static void testKeyCollection() {
		// A KeyCollection implements java.util.Collection
		KeyCollection<String> coll1 = new KeyCollection.Builder<String>().build();
		// A KeyCollection will always have element set automatically added, so these two declarations are equal
		KeyCollection<String> coll2 = new KeyCollection.Builder<String>().withElemSet().build();
	}

	static void testKeyCollectionWithElemCount() {
		// A KeyCollection which behaves like a multi set, i.e. it does not store duplicate elements,
		// but only its number of occurrences.
		KeyCollection<String> coll = new KeyCollection.Builder<String>().withElemCount(true).build();
	}

	// Key1Collection

	static void testKey1Collection() {
		// Use a Key1Collection store tag elements with a name as primary key.
		Key1Collection<Tag, String> coll = new Key1Collection.Builder<Tag, String>().withPrimaryKey1Map(Tag::getName).build();
	}

	// Key2Collection

	static void testKey2Collection() {
		// Use a Key2Collection to construct a bidirectional map where both key sets are sorted.
		Key2Collection<ZipCity, Integer, String> zipCities = new Key2Collection.Builder<ZipCity, Integer, String>().withPrimaryKey1Map(ZipCity::getZip)
				.withKey1Sort(true).withKey2Map(ZipCity::getCity)
				.withKey2Null(false).withKey2Sort(true).build();
		zipCities.add(new ZipCity(4000, "Basel"));
		zipCities.add(new ZipCity(5000, "Aarau"));
		zipCities.add(new ZipCity(5001, "Aarau"));
		zipCities.add(new ZipCity(6000, "Luzern"));

		Set<Integer> allZips = zipCities.asMap1().keySet();
		Set<String> allCities = zipCities.asMap2().keySet();

		String city = zipCities.getByKey1(5000).getCity();
		List<Integer> zips = GapList.create(zipCities.getAllByKey2("Aarau")).mappedList(zipCities.getKey1Mapper());
	}

	// KeySet

	static void testKeySet() {
		// A KeySet implements java.util.Set
		KeySet<String> set = new KeySet.Builder<String>().build();
	}

	// Key1Set

	static void testKey1Set() {
		// Use a Key1Set store tag elements with a name as primary key.
		Key1Set<Tag, String> coll = new Key1Set.Builder<Tag, String>().withPrimaryKey1Map(Tag::getName).build();
	}

	// Key2Set

	static void testKey2Set() {
		// Use a Key2Set to construct a bidirectional map where both key sets are sorted.
		Key2Collection<ZipCity, Integer, String> zipCities = new Key2Set.Builder<ZipCity, Integer, String>().withPrimaryKey1Map(ZipCity::getZip)
				.withKey1Sort(true).withKey2Map(ZipCity::getCity)
				.withKey2Null(false).withKey2Sort(true).build();
		zipCities.add(new ZipCity(4000, "Basel"));
		zipCities.add(new ZipCity(5000, "Aarau"));
		zipCities.add(new ZipCity(5001, "Aarau"));
		zipCities.add(new ZipCity(6000, "Luzern"));

		Set<Integer> allZips = zipCities.asMap1().keySet();
		Set<String> allCities = zipCities.asMap2().keySet();

		String city = zipCities.getByKey1(5000).getCity();
		List<Integer> zips = GapList.create(zipCities.getAllByKey2("Aarau")).mappedList(zipCities.getKey1Mapper());
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
		Key1Collection<Tag, String> coll = new Key1Collection.Builder<Tag, String>().withKey1Map(Tag::getName).build();
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

	// NOT USED

	class ZipCityMap extends Key2Collection<ZipCity, Integer, String> {

		public ZipCityMap() {
			Function<ZipCity, Integer> zipMapper = new Function<ZipCity, Integer>() {
				@Override
				public Integer apply(ZipCity entry) {
					return entry.getZip();
				}
			};
			Function<ZipCity, String> cityMapper = new Function<ZipCity, String>() {
				@Override
				public String apply(ZipCity entry) {
					return entry.getCity();
				}
			};
			getBuilder().withPrimaryKey1Map(zipMapper).withKey1Sort(true).withKey2Map(cityMapper).withKey2Null(false).withKey2Sort(true).build();
		}

		public void add(int zip, String city) {
			add(new ZipCity(zip, city));
		}

		public String getCity(int zip) {
			ZipCity entry = getByKey1(zip);
			return entry != null ? entry.getCity() : null;
		}

		public Integer getZip(String city) {
			ZipCity entry = getByKey2(city);
			return entry != null ? entry.getZip() : null;
		}

		public IntGapList getZips(String city) {
			Key2Collection<ZipCity, Integer, String> entries = getAllByKey2(city);
			IntGapList zips = IntGapList.create(entries.size());
			for (ZipCity entry : entries) {
				zips.add(entry.getZip());
			}
			return zips;
		}

		public Set<Integer> getZips() {
			return asMap1().keySet();
		}

		public Set<String> getCities() {
			return asMap2().keySet();
		}
	}

}
