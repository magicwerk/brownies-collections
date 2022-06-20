package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.magicwerk.brownies.collections.TestFactories.CollectionFactory;
import org.magicwerk.brownies.collections.TestFactories.GapListFactory;
import org.magicwerk.brownies.collections.TestRuns.AddLastRunList;
import org.magicwerk.brownies.core.Timer;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.tools.runner.Run;
import org.magicwerk.brownies.tools.runner.Runner;

import ch.qos.logback.classic.Logger;

/**
 *
 *
 * @author Thomas Mauch
 */
public class KeyCollectionsTestPerformance {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	/**
	 * Run with -XX:+PrintCompilation.
	 */
	public static void main(String[] args) {
		test();
	}

	static void test() {
		testPerfIntersect();

		//		for (int i=0; i<5; i++) {
		//			//testKeyList();
		//			testKeyCollection();
		//		}
	}

	static void testPerfIntersect() {
		int size = 10000;
		int offset1 = 0;
		int offset2 = 100;
		int num = 100;

		List<Integer> list1 = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			list1.add(offset1 + i);
		}
		List<Integer> list2 = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			list2.add(offset2 + i);
		}

		Timer t = new Timer();
		for (int i = 0; i < num; i++) {
			intersect1(list1, list2);
		}
		t.printElapsed();

		t = new Timer();
		for (int i = 0; i < num; i++) {
			intersect2(list1, list2);
		}
		t.printElapsed();
	}

	public static <T> List<T> intersect1(List<T> list1, List<T> list2) {
		List<T> intersect = new ArrayList<T>();
		intersect.addAll(list1);
		intersect.retainAll(list2);
		return intersect;
	}

	public static <T> List<T> intersect2(List<T> list1, List<T> list2) {
		List<T> intersect = new ArrayList<T>();
		KeyList<T> keyList1 = new KeyList.Builder<T>().withElemSet().withElemDuplicates(false).build();
		keyList1.addAll(list1);
		intersect.retainAll(keyList1);
		return intersect;
	}

	static void testKeyList() {
		final int size = 100000;
		final int numGets = 100000;
		Runner runner = new Runner("Add last " + numGets);
		runner.add(new AddLastRunList().setFactory(new GapListFactory()).setSize(size).setNumOps(numGets).setName("GapList"));
		runner.add(new AddLastRunList().setFactory(new KeyListFactory()).setSize(size).setNumOps(numGets).setName("KeyList"));
		runner.run();
		runner.printResults();
	}

	static void testKeyCollection() {
		final int size = 100000;
		final int numGets = 100000;
		Runner runner = new Runner("Add last " + numGets);
		runner.add(new AddLastRunList2("HashSet", new HashSetFactory(), size, numGets));
		runner.add(new AddLastRunList2("KeyCollection", new KeyCollectionFactory(), size, numGets));
		runner.add(new AddLastRunList2("KeyList", new KeyListWithElemSetFactory(), size, numGets));
		runner.run();
		runner.printResults();
	}

	static class AddLastRunList2 extends Run {
		int size;
		int numGets;
		CollectionFactory factory;
		Collection<Object> list;

		AddLastRunList2(String type, CollectionFactory factory, int size, int numGets) {
			super(type, type);

			this.factory = factory;
			this.size = size;
			this.numGets = numGets;
		}

		@Override
		public void beforeRun() {
			list = (Collection<Object>) factory.createSize(size);
		}

		@Override
		public Object run() {
			for (int i = 0; i < numGets; i++) {
				list.add(i);
				//list.add(size+i);
			}
			return list;
		}
	}

	static class KeyListFactory extends CollectionFactory<KeyList> {
		@Override
		public Class<?> getType() {
			return KeyList.class;
		}

		@Override
		public KeyList create(int size) {
			Integer obj = new Integer(0);
			KeyList<Object> list = new KeyList.Builder<Object>().build();
			for (int i = 0; i < size; i++) {
				list.add(obj);
			}
			return list;
		}

		@Override
		public KeyList createSize(int size) {
			Integer obj = new Integer(0);
			KeyList<Object> list = new KeyList.Builder<Object>().withCapacity(size).build();
			for (int i = 0; i < size; i++) {
				list.add(obj);
			}
			return list;
		}

		@Override
		KeyList copy(KeyList that) {
			return that.copy();
		}
	}

	static class KeyListWithElemSetFactory extends CollectionFactory<KeyList> {
		@Override
		public Class<?> getType() {
			return KeyList.class;
		}

		@Override
		public KeyList create(int size) {
			KeyList<Object> list = new KeyList.Builder<Object>().withElemSet().build();
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
			return list;
		}

		@Override
		public KeyList createSize(int size) {
			KeyList<Object> list = new KeyList.Builder<Object>().withElemSet().withCapacity(size).build();
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
			return list;
		}

		@Override
		KeyList copy(KeyList that) {
			return that.copy();
		}
	}

	static class KeyCollectionFactory extends CollectionFactory<KeyCollection> {
		@Override
		public Class<?> getType() {
			return KeyCollection.class;
		}

		@Override
		public KeyCollection create(int size) {
			KeyCollection<Object> list = new KeyCollection.Builder<Object>().build();
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
			return list;
		}

		@Override
		public KeyCollection createSize(int size) {
			KeyCollection<Object> list = new KeyCollection.Builder<Object>().withCapacity(size).build();
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
			return list;
		}

		@Override
		KeyCollection copy(KeyCollection that) {
			return that.copy();
		}
	}

	static class HashSetFactory extends CollectionFactory<HashSet> {
		@Override
		public Class<?> getType() {
			return HashSet.class;
		}

		@Override
		public HashSet create(int size) {
			HashSet<Object> list = new HashSet<Object>();
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
			return list;
		}

		@Override
		public HashSet createSize(int size) {
			HashSet<Object> list = new HashSet<Object>();
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
			return list;
		}

		@Override
		HashSet copy(HashSet that) {
			return new HashSet<Object>(that);
		}
	}

}
