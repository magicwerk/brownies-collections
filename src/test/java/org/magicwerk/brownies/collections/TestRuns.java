package org.magicwerk.brownies.collections;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.magicwerk.brownies.collections.PerformanceReport.RunInfo;
import org.magicwerk.brownies.collections.TestFactories.ArrayListFactory;
import org.magicwerk.brownies.collections.TestFactories.BigListFactory;
import org.magicwerk.brownies.collections.TestFactories.CircularArrayListFactory;
import org.magicwerk.brownies.collections.TestFactories.CollectionFactory;
import org.magicwerk.brownies.collections.TestFactories.DualArrayDequeFactory;
import org.magicwerk.brownies.collections.TestFactories.DualRootishArrayDequeFactory;
import org.magicwerk.brownies.collections.TestFactories.Factory;
import org.magicwerk.brownies.collections.TestFactories.FastTableFactory;
import org.magicwerk.brownies.collections.TestFactories.GapListFactory;
import org.magicwerk.brownies.collections.TestFactories.LinkedListFactory;
import org.magicwerk.brownies.collections.TestFactories.PrimitiveFactory;
import org.magicwerk.brownies.collections.TestFactories.RandomRandomContentFactory;
import org.magicwerk.brownies.collections.TestFactories.RootishArrayStackFactory;
import org.magicwerk.brownies.collections.TestFactories.TreeListFactory;
import org.magicwerk.brownies.collections.primitive.IIntList;
import org.magicwerk.brownies.core.StringTools;
import org.magicwerk.brownies.core.SystemTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.core.strings.StringFormatter;
import org.magicwerk.brownies.tools.runner.Run;
import org.magicwerk.brownies.tools.runner.Runner;
import org.magicwerk.brownies.tools.runner.Runner.RunResult;

public class TestRuns {

	public static final String GAPLIST = "GapList";
	public static final String BIGLIST = "BigList";
	public static final String ARRAYLIST = "ArrayList";
	public static final String LINKEDLIST = "LinkedList";
	public static final String CIRCULARARRAYLIST = "CircularArrayList";
	public static final String ROOTISHARRAYSTACK = "RootishArrayStack";
	public static final String TREELIST = "TreeList";
	public static final String FASTTABLE = "FastTable";
	public static final String HUGELIST = "HugeList";
	public static final String TLIST = "TList";

	static Class<?> listFactories[] = new Class<?>[] {
			GapListFactory.class,
			ArrayListFactory.class,
			CircularArrayListFactory.class,
			BigListFactory.class,
			TreeListFactory.class,
			LinkedListFactory.class,
			RootishArrayStackFactory.class,
			DualRootishArrayDequeFactory.class,
			DualArrayDequeFactory.class,
			FastTableFactory.class
	};

	Runner runner;
	List<CollectionFactory> factories;

	public TestRuns(Runner runner) {
		this.runner = runner;
	}

	public void setFactories(CollectionFactory... factories) {
		this.factories = GapList.create(factories);
	}

	public void testPerformanceRemoveRange(int size) {
		testPerformance("Remove range", RemoveRangeRunList.class, RemoveRangeRunList.class, RemoveRangeRunIList.class, size, 0, 0);
	}

	public void testPerformanceRemoveAll(int size) {
		testPerformance("RemoveAll", RemoveAllRun.class, RemoveAllRun.class, size, 0, 0);
	}

	public void testPerformanceRetainAll(int size) {
		testPerformance("RetainAll", RetainAllRun.class, RetainAllRun.class, size, 0, 0);
	}

	public void testPerformanceGetLast(int size, int numOps) {
		testPerformance("Get last", GetLastRunDeque.class, GetLastRunList.class, size, numOps, 0);
	}

	public void testPerformanceGetFirst(int size, int numOps) {
		testPerformance("Get first", GetFirstRunDeque.class, GetFirstRunList.class, size, numOps, 0);
	}

	public void testPerformanceGetRandom(int size, int numOps) {
		testPerformance("Get random", GetRandomRun.class, GetRandomRun.class, size, numOps, 0);
	}

	public void testPerformanceAddLast(int size, int numOps) {
		testPerformance("Add last", AddLastRunDeque.class, AddLastRunList.class, size, numOps, 0);
	}

	public void testPerformanceAddFirst(int size, int numOps) {
		testPerformance("Add first", AddFirstRunDeque.class, AddFirstRunList.class, size, numOps, 0);
	}

	public void testPerformanceAddMiddle(int size, int numOps) {
		testPerformance("Add middle", AddMiddleRun.class, AddMiddleRun.class, size, numOps, 0);
	}

	public void testPerformanceAddRandom(int size, int numOps) {
		testPerformance("Add random", AddRandomRun.class, AddRandomRun.class, size, numOps, 0);
	}

	public void testPerformanceRemoveLast(int size, int numOps) {
		testPerformance("Remove last", RemoveLastRunDeque.class, RemoveLastRunList.class, size, numOps, 0);
	}

	public void testPerformanceRemoveFirst(int size, int numOps) {
		testPerformance("Remove first", RemoveFirstRunDeque.class, RemoveFirstRunList.class, size, numOps, 0);
	}

	public void testPerformanceRemoveRandom(int size, int numOps) {
		testPerformance("Remove random", RemoveRandomRun.class, RemoveRandomRun.class, size, numOps, 0);
	}

	public void testPerformanceAddNear(int size, int numOps, double near) {
		testPerformance("Add near {2}", AddNearRun.class, AddNearRun.class, size, numOps, near);
	}

	public void testPerformanceAddIter(int size, int numOps, int sel) {
		testPerformance("Add iter {2}", AddIterRun.class, AddIterRun.class, size, numOps, sel);
	}

	public void testPerformanceRemoveNear(int size, int numOps, double near) {
		testPerformance("Remove near {2}", RemoveNearRun.class, RemoveNearRun.class, size, numOps, near);
	}

	public void testPerformanceRemoveIter(int size, int numOps, double near) {
		testPerformance("Remove iter {2}", RemoveIterRun.class, RemoveIterRun.class, size, numOps, near);
	}

	void testPerformance(String desc, Class<?> dequeRun, Class<?> listRun, int size, int numOps, double near) {
		testPerformance(desc, dequeRun, listRun, listRun, size, numOps, near);
	}

	void testPerformance(String desc, Class<?> dequeRun, Class<?> listRun, Class<?> ilistRun, int size, int numOps, double near) {
		if (factories == null) {
			factories = GapList.create();
			for (Class<?> factoryClass : listFactories) {
				CollectionFactory factory = (CollectionFactory) ReflectTools.create(factoryClass);
				factories.add(factory);
			}
		}
		String str = StringFormatter.format(desc, size, numOps, near);
		runner.setName(str);
		for (CollectionFactory factory : factories) {
			Class<?> listClass = factory.getType();
			FactoryRun run = null;
			String name = factory.getClass().getSimpleName();
			name = StringTools.removeTail(name, "Factory");
			if (IList.class.isAssignableFrom(listClass)) {
				run = (FactoryRun) ReflectTools.create(ilistRun);
			} else if (List.class.isAssignableFrom(listClass)) {
				run = (FactoryRun) ReflectTools.create(listRun);
			} else if (Deque.class.isAssignableFrom(listClass)) {
				run = (FactoryRun) ReflectTools.create(dequeRun);
			} else {
				throw new AssertionError();
			}
			run.setName(name);
			run.setTags(name);
			run.setFactory(factory);
			run.setSize(size);
			run.setNumOps(numOps);
			run.setNear(near);
			runner.add(run);
		}
		runner.run();
		runner.printResults();

		String RELEASE = "0.9.16"; // FIXME retrieve automatically

		// Add result to report
		PerformanceReport report = new PerformanceReport();
		report.load();
		List<RunResult> results = runner.getResults();
		for (RunResult result : results) {
			String javaVersion = SystemTools.getJavaVersion();
			if (SystemTools.Is64bit) {
				javaVersion += "_x64";
			}
			RunInfo runInfo = new RunInfo().setJava(javaVersion).setVmArgs(SystemTools.getJvmArgsString()).setRelease(RELEASE)
					.setRun(runner.getName()).setType(result.getName()).setTime(result.getAvgTime());
			report.addRun(runInfo);
		}
		report.save();
	}

	//

	public static abstract class FactoryRun extends FactoryRun2 {
		protected CollectionFactory factory;

		@Override
		public FactoryRun setFactory(Factory factory) {
			super.setFactory(factory);
			this.factory = (CollectionFactory) factory;
			return this;
		}

	}

	public static abstract class PrimitiveFactoryRun extends FactoryRun2 {
		protected PrimitiveFactory factory;

		@Override
		public PrimitiveFactoryRun setFactory(Factory factory) {
			super.setFactory(factory);
			this.factory = (PrimitiveFactory) factory;
			return this;
		}

	}

	public static abstract class FactoryRun2 extends Run {
		protected Factory factory2;
		/** size of collection */
		int size;
		/** number of operations to execute on collection */
		int numOps;
		/** specify how near two subsequent operations will be in the collection */
		double near;
		/** number of operations which will executed locally on the same index */
		int localOps = 0;

		@Override
		public String getName() {
			String name = super.getName();
			if (name != null) {
				return name;
			}
			return factory2.getName();
		}

		public FactoryRun2 setFactory(Factory factory) {
			this.factory2 = factory;
			return this;
		}

		public FactoryRun2 setSize(int size) {
			this.size = size;
			return this;
		}

		public FactoryRun2 setNumOps(int numOps) {
			this.numOps = numOps;
			return this;
		}

		public FactoryRun2 setNear(double near) {
			this.near = near;
			return this;
		}

		public FactoryRun2 setLocalOps(int localOps) {
			this.localOps = localOps;
			return this;
		}

		/**
		 * Generated next random index.
		 * The index is generated based on the previous index and the specified near factor.
		 * The factor specifies the range the index can have prevIndex-(size*near) < index < prevIndex+(size*near),
		 * so the smaller the near factor the nearer the indexes will be.
		 *
		 * @param random	Random instance used to generate random numbers
		 * @param size		max size of indexes, so for the returned index 0 <= index < size
		 * @param pos		previous index
		 * @param near		factor to control how near the generated index will be to the previous one
		 * @return
		 */
		static int getPos(Random random, int pos, int size, double near) {
			int range = (int) (size * near);
			int min = pos - range / 2;
			int max;
			if (min <= 0) {
				min = 0;
				max = range;
			} else {
				max = min + range;
				if (max > size) {
					max = size;
					min = max - range;
				}
			}
			if (min == max) {
				pos = min;
			} else {
				pos = min + random.nextInt(max - min);
			}
			return pos;
		}

		static Random getRandom() {
			Random r = new Random();
			r.setSeed(0);
			return r;
		}
	}

	//

	public static class CloneRun extends FactoryRun {
		List<Object> list;
		boolean all;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			List<Object> copy = (List<Object>) factory.copy(list);
			int pos = (size - localOps) / 2;
			for (int i = 0; i < localOps; i++) {
				copy.set(pos + i, null);
			}
		}
	}

	// --- Get ---

	static abstract class GetLastRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeAll() {
			list = (List<Object>) factory.createSize(size);
		}

		public String exitOnce() {
			return "Memory: " + ReflectTools.getObjectSize(list);
		}
	}

	static class GetLastRunList extends GetLastRun {
		@Override
		public void run() {
			// Use random so values can be compared with GetRandomRun
			Random r = new Random(0);
			for (int i = 0; i < numOps; i++) {
				list.get(list.size() - 1);
				r.nextInt(1);
			}
		}
	}

	static class GetLastRunDeque extends GetLastRun {
		Deque<Object> deque;

		@Override
		public void beforeAll() {
			super.beforeAll();
			deque = (Deque<Object>) list;
		}

		@Override
		public void run() {
			// Use random so values can be compared with GetRandomRun
			Random r = new Random(0);
			for (int i = 0; i < numOps; i++) {
				r.nextInt(1);
				deque.getLast();
			}
		}
	}

	static abstract class GetFirstRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeAll() {
			list = (List<Object>) factory.createSize(size);
		}
	}

	static class GetFirstRunList extends GetFirstRun {
		@Override
		public void run() {
			// Use random so values can be compared with GetRandomRun
			Random r = new Random(0);
			for (int i = 0; i < numOps; i++) {
				r.nextInt(1);
				list.get(0);
			}
		}
	}

	static class GetFirstRunDeque extends GetFirstRun {
		Deque<Object> deque;

		@Override
		public void beforeAll() {
			super.beforeAll();
			deque = (Deque<Object>) list;
		}

		@Override
		public void run() {
			// Use random so values can be compared with GetRandomRun
			Random r = new Random(0);
			for (int i = 0; i < numOps; i++) {
				r.nextInt(1);
				deque.getFirst();
			}
		}
	}

	public static class GetIntWrapperRandomRun extends FactoryRun {
		List<Integer> list;

		@SuppressWarnings("unchecked")
		@Override
		public void beforeAll() {
			list = (List<Integer>) factory.createSize(size);
		}

		@Override
		public void run() {
			Random r = new Random();
			r.setSeed(0);
			for (int i = 0; i < numOps; i++) {
				int pos = r.nextInt(list.size());
				int val = list.get(pos);
			}
		}
	}

	public static class GetIntRandomRun extends PrimitiveFactoryRun {
		IIntList list;

		@Override
		public void beforeAll() {
			list = (IIntList) factory.createSize(size);
		}

		@Override
		public void run() {
			Random r = new Random();
			r.setSeed(0);
			for (int i = 0; i < numOps; i++) {
				int pos = r.nextInt(list.size());
				int val = list.get(pos);
			}
		}
	}

	public static class GetRandomRun extends FactoryRun {
		List<Object> list;

		@SuppressWarnings("unchecked")
		@Override
		public void beforeAll() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			Random r = new Random(0);
			int size = list.size();
			for (int i = 0; i < numOps; i++) {
				int pos = r.nextInt(size);
				list.get(pos);
			}
		}
	}

	public static class SortRun extends FactoryRun {
		List<Object> list;

		@SuppressWarnings("unchecked")
		@Override
		public void beforeRun() {
			list = (List<Object>) factory.create(size, true, new RandomRandomContentFactory());
		}

		@Override
		public void run() {
			list.sort(null);
		}
	}

	public static class GetIterRun extends FactoryRun {
		List<Object> list;

		@SuppressWarnings("unchecked")
		@Override
		public void beforeAll() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			Random r = new Random(0);
			for (int i = 0; i < numOps; i++) {
				int pos = r.nextInt(size);
				list.get(i);
			}
		}
	}

	public static class GetNearRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeAll() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			Random r = new Random(0);
			int size = list.size();
			//int pos = size / 2;
			for (int i = 0; i < numOps; i++) {
				int pos = r.nextInt(size);
				//pos = RemoveNearRun.getPos(r, size, pos, near);
				//System.out.println(pos);
				for (int j = 0; j < localOps; j++) {
					list.get(pos);
				}
			}
		}

		@Override
		public String afterAll() {
			int size = ReflectTools.getObjectSize(list);
			list = null;
			return "Size " + size;
		}

	}

	// --- Add ---

	static class AddInitRun extends FactoryRun {
		Collection<Object> list;

		@Override
		public void beforeRun() {
			list = (Collection<Object>) factory.create(16);
		}

		@Override
		public void run() {
			Object o = new Object();
			for (int i = 0; i < numOps; i++) {
				list.add(o);
			}
		}
	}

	static class AddAllInitRun extends FactoryRun {
		Collection<Object> list;
		Collection<Object> add;

		@Override
		public void beforeAll() {
			add = (Collection<Object>) factory.create(numOps);
		}

		@Override
		public void beforeRun() {
			list = (Collection<Object>) factory.create(16);
		}

		@Override
		public void run() {
			list.addAll(add);
		}
	}

	static class AddMiddleRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			Random r = new Random();
			int pos = size / 2;
			Object o = new Object();
			for (int i = 0; i < numOps; i++) {
				r.nextInt(1);
				list.add(pos, o);
			}
		}
	}

	static class AddMultRun extends FactoryRun {
		List<Object> list;
		List<Object> add;

		@SuppressWarnings("unchecked")
		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
			add = (List<Object>) factory.createSize(numOps);
		}

		@Override
		public void run() {
			int pos = size / 2;
			list.addAll(pos, add);
		}
	}

	static abstract class AddLastRun extends FactoryRun {
		Collection<Object> list;

		@Override
		public void beforeRun() {
			list = (Collection<Object>) factory.createSize(size);
		}
	}

	public static class AddLastRunList extends AddLastRun {
		List<Object> list;
		boolean all;

		public FactoryRun setAll(boolean all) {
			this.all = all;
			return this;
		}

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			Random r = getRandom();
			Object elem = new Object();
			GapList<Object> elems = new GapList(localOps);
			elems.init(localOps, elem);
			for (int i = 0; i < numOps; i++) {
				int pos = (i == 0) ? 0 : r.nextInt(list.size());
				if (all) {
					list.addAll(elems);
				} else {
					for (int j = 0; j < localOps; j++) {
						list.add(elem);
					}
				}
			}
		}
	}

	public static class AddLastRunDeque extends AddLastRun {
		Deque<Object> deque;

		@Override
		public void beforeRun() {
			super.beforeRun();
			deque = (Deque<Object>) list;
		}

		@Override
		public void run() {
			Object o = new Object();
			for (int i = 0; i < numOps; i++) {
				deque.addLast(o);
			}
		}
	}

	static abstract class AddFirstRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}
	}

	public static class AddFirstRunList extends AddFirstRun {
		@Override
		public void run() {
			Object o = new Object();
			for (int i = 0; i < numOps; i++) {
				list.add(0, o);
			}
		}
	}

	public static class AddFirstRunDeque extends AddFirstRun {
		Deque<Object> deque;

		@Override
		public void beforeRun() {
			super.beforeRun();
			deque = (Deque<Object>) list;
		}

		@Override
		public void run() {
			Object o = new Object();
			for (int i = 0; i < numOps; i++) {
				deque.addFirst(o);
			}
		}
	}

	public static class AddRandomRun extends FactoryRun {
		//-- Configuration
		boolean all;
		//-- State
		List<Object> list;
		Integer elem;
		GapList<Integer> elems;

		public FactoryRun setAll(boolean all) {
			this.all = all;

			elem = 0;
			GapList<Integer> elems = GapList.create(localOps);
			elems.init(localOps, 0);
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			Random r = getRandom();
			for (int i = 0; i < numOps; i++) {
				int pos = (i == 0) ? 0 : r.nextInt(list.size());
				if (all) {
					list.addAll(pos, elems);
				} else {
					for (int j = 0; j < localOps; j++) {
						list.add(pos, elem);
					}
				}
			}
		}
	}

	public static class AddIntWrapperRandomRun extends FactoryRun {
		List<Object> list;

		@SuppressWarnings("unchecked")
		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			Random r = getRandom();
			for (int i = 0; i < numOps; i++) {
				int pos = (i == 0) ? 0 : r.nextInt(list.size());
				list.add(pos, 1000 + i);
			}
		}
	}

	public static class AddIntRandomRun extends PrimitiveFactoryRun {
		IIntList list;

		@SuppressWarnings("unchecked")
		@Override
		public void beforeRun() {
			list = (IIntList) factory.createSize(size);
		}

		@Override
		public void run() {
			Random r = getRandom();
			for (int i = 0; i < numOps; i++) {
				int pos = (i == 0) ? 0 : r.nextInt(list.size());
				list.add(pos, 1000 + i);
			}
		}
	}

	// --- Remove ---

	static abstract class RemoveLastRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}
	}

	public static class RemoveLastRunList extends RemoveLastRun {
		@Override
		public void run() {
			for (int i = 0; i < numOps; i++) {
				list.remove(list.size() - 1); // no removeLast
			}
		}
	}

	public static class RemoveLastRunDeque extends RemoveLastRun {
		Deque<Object> deque;

		@Override
		public void beforeRun() {
			super.beforeRun();
			deque = (Deque<Object>) list;
		}

		@Override
		public void run() {
			for (int i = 0; i < numOps; i++) {
				deque.removeLast();
			}
		}
	}

	static abstract class RemoveFirstRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}
	}

	public static class RemoveFirstRunList extends RemoveFirstRun {
		@Override
		public void run() {
			for (int i = 0; i < numOps; i++) {
				list.remove(list.size() - 1); // no removeLast
			}
		}
	}

	public static class RemoveFirstRunDeque extends RemoveFirstRun {
		Deque<Object> deque;

		@Override
		public void beforeRun() {
			super.beforeRun();
			deque = (Deque<Object>) list;
		}

		@Override
		public void run() {
			for (int i = 0; i < numOps; i++) {
				deque.removeLast();
			}
		}
	}

	public static class RemoveRandomRun extends FactoryRun {
		List<Object> list;
		boolean all;

		public FactoryRun setAll(boolean all) {
			this.all = all;
			return this;
		}

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			IList ilist = null;
			if (all) {
				if (list instanceof IList) {
					ilist = (IList) list;
				}
			}
			Random r = getRandom();
			for (int i = 0; i < numOps; i++) {
				int size = list.size();
				int pos = r.nextInt(size);
				if (localOps == 1) {
					list.remove(pos);
				} else {
					pos = Math.min(pos, size - localOps);
					int ops = localOps;
					if (pos < 0) {
						ops += pos;
						pos = 0;
					}
					if (ilist != null) {
						ilist.remove(pos, ops);
					} else {
						for (int j = 0; j < ops; j++) {
							list.remove(pos);
						}
					}
				}
			}
		}
	}

	public static class RemoveIntWrapperRandomRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			Random r = getRandom();
			for (int i = 0; i < numOps; i++) {
				int size = list.size();
				int pos = r.nextInt(size);
				list.remove(pos);
			}
		}
	}

	public static class RemoveIntRandomRun extends PrimitiveFactoryRun {
		IIntList list;

		@Override
		public void beforeRun() {
			list = (IIntList) factory.createSize(size);
		}

		@Override
		public void run() {
			Random r = getRandom();
			for (int i = 0; i < numOps; i++) {
				int size = list.size();
				int pos = r.nextInt(size);
				list.remove(pos);
			}
		}
	}

	public static class RemoveMiddleRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			int pos = (size - numOps) / 2;
			for (int i = 0; i < numOps; i++) {
				list.remove(pos);
			}
		}
	}

	public static class RemoveMultRun extends FactoryRun {
		List<Object> list;
		IList<Object> ilist;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
			if (list instanceof IList) {
				ilist = (IList) list;
			}
		}

		@Override
		public void run() {
			int pos = (size - numOps) / 2;
			if (ilist != null) {
				ilist.remove(pos, numOps);
			} else {
				list.subList(pos, pos + numOps).clear();
				//
				//for (int i = 0; i < numOps; i++) {
				//	list.remove(pos);
				//}
			}
		}
	}

	//

	public static class AddNearRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			Random r = getRandom();
			Integer elem = 0;
			int pos = list.size() / 2;
			for (int i = 0; i < numOps; i++) {
				pos = getPos(r, pos, list.size(), near);
				for (int n = 0; n < localOps; n++) {
					list.add(pos + n, elem);
				}
			}
		}
	}

	public static class AddIterRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			int cnt = 0;
			for (int i = 0; i < list.size(); i++) {
				Object o = list.get(i);
				int sel = (int) near;
				if (cnt % sel == 0) {
					for (int j = 0; j < numOps / (size / sel); j++) {
						list.add(i + 1, o);
						i++;
					}
				}
				cnt++;
			}
		}
	}

	//

	public static class RemoveNearRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			Random r = getRandom();
			int pos = list.size() / 2;
			for (int i = 0; i < numOps; i++) {
				pos = getPos(r, pos, list.size() - localOps, near);
				for (int j = 0; j < localOps; j++) {
					list.remove(pos);
				}
			}
		}

	}

	static class RemoveIterRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public void run() {
			int sel = (int) near;

			int cnt = 0;
			int num = size;
			for (int i = 0; i < num; i++) {
				Object o = list.get(i);
				if (cnt % sel == 0) {
					list.remove(i);
					i--;
					num--;
				}
				cnt++;
			}
		}
	}

	public static class RemoveAllRun extends FactoryRun {
		int step = 5;
		List<Integer> list;
		List<Integer> remove;

		@Override
		public void beforeAll() {
			remove = (List<Integer>) factory.createSize(size / step);
			for (int i = 0; i < size; i++) {
				if (i % step == 0) {
					remove.add(i);
				}
			}
		}

		@Override
		public void beforeRun() {
			list = (List<Integer>) factory.createSize(size);
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
		}

		@Override
		public void run() {
			list.removeAll(remove);
		}
	}

	public static class RetainAllRun extends FactoryRun {
		int step = 5;
		List<Integer> list;
		List<Integer> remove;

		@Override
		public void beforeAll() {
			remove = (List<Integer>) factory.createSize(size / step);
			for (int i = 0; i < size; i++) {
				if (i % step == 0) {
					remove.add(i);
				}
			}
		}

		@Override
		public void beforeRun() {
			list = (List<Integer>) factory.createSize(size);
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
		}

		@Override
		public void run() {
			list.retainAll(remove);
		}
	}

	// Remove range

	public static class RemoveRangeRunList extends FactoryRun {
		List<Integer> list;

		@Override
		public void beforeRun() {
			list = (List<Integer>) factory.createSize(size);
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
		}

		@Override
		public void run() {
			int len = size / 2;
			int index = len / 2;
			list.subList(index, index + len).clear();
		}
	}

	public static class RemoveRangeRunIList extends FactoryRun {
		IList<Integer> list;

		@Override
		public void beforeRun() {
			list = (IList<Integer>) factory.createSize(size);
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
		}

		@Override
		public void run() {
			int len = size / 2;
			int index = len / 2;
			list.remove(index, len);
		}
	}

	// Filter

	public static class FilterLambdaRun extends FactoryRun {
		List<Integer> list;

		@Override
		public void beforeAll() {
			list = (List<Integer>) factory.createSize(size);
		}

		@Override
		public void run() {
			List<Integer> result = list.stream().filter((i) -> i % 2 == 0).collect(Collectors.toList());
		}
	}

	public static class FilterIListRun extends FactoryRun {
		IList<Integer> list;

		@Override
		public void beforeAll() {
			list = (IList<Integer>) factory.createSize(size);
		}

		@Override
		public void run() {
			List<Integer> result = list.filteredList((i) -> i % 2 == 0);
		}
	}

}
