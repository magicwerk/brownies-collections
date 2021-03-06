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
import org.magicwerk.brownies.collections.TestFactories.FastTableFactory;
import org.magicwerk.brownies.collections.TestFactories.GapListFactory;
import org.magicwerk.brownies.collections.TestFactories.LinkedListFactory;
import org.magicwerk.brownies.collections.TestFactories.RandomRandomContentFactory;
import org.magicwerk.brownies.collections.TestFactories.RootishArrayStackFactory;
import org.magicwerk.brownies.collections.TestFactories.TreeListFactory;
import org.magicwerk.brownies.collections.primitive.IIntList;
import org.magicwerk.brownies.core.StringTools;
import org.magicwerk.brownies.core.SystemTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.core.strings.StringFormatter;
import org.magicwerk.brownies.tools.runner.Run;
import org.magicwerk.brownies.tools.runner.Runner;
import org.magicwerk.brownies.tools.runner.Runner.RunResult;
import org.slf4j.Logger;

public class TestRuns {

	static final Logger LOG = LogbackTools.getConsoleLogger();

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

	public Runner getRunner() {
		return runner;
	}

	public void setFactories(CollectionFactory... factories) {
		this.factories = GapList.create(factories);
	}

	// Get

	public void testPerformanceGetFirst(int size, int numOps) {
		testPerformance("Get first", GetFirstRunDeque.class, GetFirstRun.class, size, numOps, 0);
	}

	public void testPerformanceGetLast(int size, int numOps) {
		testPerformance("Get last", GetLastRunDeque.class, GetLastRun.class, size, numOps, 0);
	}

	public void testPerformanceGetMid(int size, int numOps) {
		testPerformance("Get mid", GetMidRun.class, GetMidRun.class, size, numOps, 0);
	}

	public void testPerformanceGetRandom(int size, int numOps) {
		testPerformance("Get random", GetRandomRun.class, GetRandomRun.class, size, numOps, 0);
	}

	public void testPerformanceGetIter1(int size, int numOps) {
		testPerformance("Get iter 1", GetIterRun.class, GetIterRun.class, size, numOps, 1);
	}

	public void testPerformanceGetIter2(int size, int numOps) {
		testPerformance("Get iter 2", GetIterRun.class, GetIterRun.class, size, numOps, 2);
	}

	//

	public void testPerformanceRemoveRange(int size) {
		testPerformance("Remove range", RemoveRangeRunList.class, RemoveRangeRunList.class, RemoveRangeRunIList.class, size, 0, 0);
	}

	public void testPerformanceRemoveAll(int size) {
		testPerformance("RemoveAll", RemoveAllRun.class, RemoveAllRun.class, size, 0, 0);
	}

	public void testPerformanceRetainAll(int size) {
		testPerformance("RetainAll", RetainAllRun.class, RetainAllRun.class, size, 0, 0);
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

	public void testPerformanceAddNear(int size, int numOps, int step) {
		testPerformance("Add near {2}", AddNearRun.class, AddNearRun.class, size, numOps, step);
	}

	public void testPerformanceAddIter(int size, int numOps, int sel) {
		testPerformance("Add iter {2}", AddIterRun.class, AddIterRun.class, size, numOps, sel);
	}

	public void testPerformanceRemoveNear(int size, int numOps, int step) {
		testPerformance("Remove near {2}", RemoveNearRun.class, RemoveNearRun.class, size, numOps, step);
	}

	public void testPerformanceRemoveIter(int size, int numOps, int step) {
		testPerformance("Remove iter {2}", RemoveIterRun.class, RemoveIterRun.class, size, numOps, step);
	}

	void testPerformance(String desc, Class<?> dequeRun, Class<?> listRun, int size, int numOps, int step) {
		testPerformance(desc, dequeRun, listRun, listRun, size, numOps, step);
	}

	void testPerformance(String desc, Class<?> dequeRun, Class<?> listRun, Class<?> ilistRun, int size, int numOps, int step) {
		if (factories == null) {
			factories = GapList.create();
			for (Class<?> factoryClass : listFactories) {
				CollectionFactory factory = (CollectionFactory) ReflectTools.create(factoryClass);
				factories.add(factory);
			}
		}
		String str = StringFormatter.format(desc, size, numOps, step);
		runner.setName(str);
		for (CollectionFactory factory : factories) {
			String name = factory.getClass().getSimpleName();
			name = StringTools.removeTail(name, "Factory");

			Class<?> listClass = factory.getType();
			Class<?> runClass = selectRunClass(listClass, dequeRun, listRun, ilistRun);
			FactoryRun run = (FactoryRun) ReflectTools.create(runClass);

			run.setName(name);
			run.setTags(name);
			run.setFactory(factory);
			run.setSize(size);
			run.setNumOps(numOps);
			run.setStep(step);
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

	Class<?> selectRunClass(Class<?> listClass, Class<?> dequeRun, Class<?> listRun, Class<?> ilistRun) {
		if (IList.class.isAssignableFrom(listClass)) {
			return ilistRun;
		} else if (List.class.isAssignableFrom(listClass)) {
			return listRun;
		} else if (Deque.class.isAssignableFrom(listClass)) {
			return dequeRun;
		} else {
			throw new AssertionError();
		}
	}

	public static abstract class FactoryRun extends Run {
		protected CollectionFactory factory;
		/** size of collection */
		int size;
		/** number of operations to execute on collection */
		int numOps;
		/** specify how near two subsequent operations will be in the collection */
		int step;
		/** number of operations which will executed locally on the same index */
		int localOps = 1;

		@Override
		public String getName() {
			String name = super.getName();
			if (name != null) {
				return name;
			}
			return factory.getName();
		}

		/** Setter for {@link #factory} */
		public FactoryRun setFactory(CollectionFactory factory) {
			this.factory = factory;
			return this;
		}

		/** Setter for {@link #size} */
		public FactoryRun setSize(int size) {
			this.size = size;
			return this;
		}

		/** Setter for {@link #numOps} */
		public FactoryRun setNumOps(int numOps) {
			this.numOps = numOps;
			return this;
		}

		/** Setter for {@link #step} */
		public FactoryRun setStep(int step) {
			this.step = step;
			return this;
		}

		/** Setter for {@link #localOps} */
		public FactoryRun setLocalOps(int localOps) {
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
		static int getNearPos(Random random, int pos, int size, double near) {
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
		public Object run() {
			List<Object> copy = (List<Object>) factory.copy(list);
			int pos = (size - localOps) / 2;
			for (int i = 0; i < localOps; i++) {
				copy.set(pos + i, null);
			}
			return copy;
		}
	}

	// --- Get ---

	static abstract class GetBaseRun extends FactoryRun {
		int indexes[];
		Random r = new Random(0);

		List<Object> list;
		Deque<Object> deque;

		@Override
		public void beforeAll() {
			indexes = new int[numOps];
			initIndexes();
			LOG.info("{}", indexes);

			Object obj = factory.createSize(size);
			if (obj instanceof List) {
				list = (List<Object>) obj;
			} else if (obj instanceof Deque) {
				deque = (Deque<Object>) obj;
			} else {
				throw new AssertionError();
			}
		}

		void initIndexes() {
		}

		@Override
		public Object run() {
			for (int i = 0; i < numOps; i++) {
				list.get(indexes[i]);
			}
			return list;
		}
	}

	static class GetFirstRun extends GetBaseRun {
		@Override
		void initIndexes() {
			for (int i = 0; i < numOps; i++) {
				indexes[i] = 0;
			}
		}
	}

	static class GetFirstRunDeque extends GetFirstRun {
		@Override
		public Object run() {
			for (int i = 0; i < numOps; i++) {
				deque.getFirst(); // Deque only know getFirst()
			}
			return deque;
		}
	}

	static class GetLastRun extends GetBaseRun {
		@Override
		void initIndexes() {
			for (int i = 0; i < numOps; i++) {
				indexes[i] = size - 1;
			}
		}
	}

	static class GetLastRunDeque extends GetLastRun {
		@Override
		public Object run() {
			for (int i = 0; i < numOps; i++) {
				deque.getLast(); // Deque only know getLast()
			}
			return deque;
		}
	}

	public static class GetMidRun extends GetBaseRun {
		@Override
		void initIndexes() {
			int pos = size / 2;
			for (int i = 0; i < numOps; i++) {
				indexes[i] = pos;
			}
		}
	}

	public static class GetRandomRun extends GetBaseRun {
		@Override
		void initIndexes() {
			for (int i = 0; i < numOps; i++) {
				indexes[i] = r.nextInt(size);
			}
		}
	}

	public static class GetIterRun extends GetBaseRun {
		@Override
		void initIndexes() {
			int start = (size / 2) - (numOps * step / 2);
			for (int i = 0; i < numOps; i++) {
				indexes[i] = start + i * step;
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
		public Object run() {
			Random r = new Random();
			r.setSeed(0);
			for (int i = 0; i < numOps; i++) {
				int pos = r.nextInt(list.size());
				int val = list.get(pos);
			}
			return list;
		}
	}

	public static class GetIntRandomRun extends FactoryRun {
		IIntList list;

		@Override
		public void beforeAll() {
			list = (IIntList) factory.createSize(size);
		}

		@Override
		public Object run() {
			Random r = new Random();
			r.setSeed(0);
			for (int i = 0; i < numOps; i++) {
				int pos = r.nextInt(list.size());
				int val = list.get(pos);
			}
			return list;
		}
	}

	//

	public static class SortRun extends FactoryRun {
		List<Object> list;

		@SuppressWarnings("unchecked")
		@Override
		public void beforeRun() {
			list = (List<Object>) factory.create(size, true, new RandomRandomContentFactory());
		}

		@Override
		public Object run() {
			list.sort(null);
			return list;
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
		public Object run() {
			Object o = new Object();
			for (int i = 0; i < numOps; i++) {
				list.add(o);
			}
			return list;
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
		public Object run() {
			list.addAll(add);
			return list;
		}
	}

	static class AddMiddleRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public Object run() {
			Random r = new Random();
			int pos = size / 2;
			Object o = new Object();
			for (int i = 0; i < numOps; i++) {
				r.nextInt(1);
				list.add(pos, o);
			}
			return list;
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
		public Object run() {
			int pos = size / 2;
			list.addAll(pos, add);
			return list;
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
		public Object run() {
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
			return list;
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
		public Object run() {
			Object o = new Object();
			for (int i = 0; i < numOps; i++) {
				deque.addLast(o);
			}
			return deque;
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
		public Object run() {
			Object o = new Object();
			for (int i = 0; i < numOps; i++) {
				list.add(0, o);
			}
			return list;
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
		public Object run() {
			Object o = new Object();
			for (int i = 0; i < numOps; i++) {
				deque.addFirst(o);
			}
			return deque;
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
		public Object run() {
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
			return list;
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
		public Object run() {
			Random r = getRandom();
			for (int i = 0; i < numOps; i++) {
				int pos = (i == 0) ? 0 : r.nextInt(list.size());
				list.add(pos, 1000 + i);
			}
			return list;
		}
	}

	public static class AddIntRandomRun extends FactoryRun {
		IIntList list;

		@SuppressWarnings("unchecked")
		@Override
		public void beforeRun() {
			list = (IIntList) factory.createSize(size);
		}

		@Override
		public Object run() {
			Random r = getRandom();
			for (int i = 0; i < numOps; i++) {
				int pos = (i == 0) ? 0 : r.nextInt(list.size());
				list.add(pos, 1000 + i);
			}
			return list;
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
		public Object run() {
			for (int i = 0; i < numOps; i++) {
				list.remove(list.size() - 1); // no removeLast
			}
			return list;
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
		public Object run() {
			for (int i = 0; i < numOps; i++) {
				deque.removeLast();
			}
			return deque;
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
		public Object run() {
			for (int i = 0; i < numOps; i++) {
				list.remove(list.size() - 1); // no removeLast
			}
			return list;
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
		public Object run() {
			for (int i = 0; i < numOps; i++) {
				deque.removeLast();
			}
			return deque;
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
		public Object run() {
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
			return list;
		}
	}

	public static class RemoveIntWrapperRandomRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public Object run() {
			Random r = getRandom();
			for (int i = 0; i < numOps; i++) {
				int size = list.size();
				int pos = r.nextInt(size);
				list.remove(pos);
			}
			return list;
		}
	}

	public static class RemoveIntRandomRun extends FactoryRun {
		IIntList list;

		@Override
		public void beforeRun() {
			list = (IIntList) factory.createSize(size);
		}

		@Override
		public Object run() {
			Random r = getRandom();
			for (int i = 0; i < numOps; i++) {
				int size = list.size();
				int pos = r.nextInt(size);
				list.remove(pos);
			}
			return list;
		}
	}

	public static class RemoveMiddleRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public Object run() {
			int pos = (size - numOps) / 2;
			for (int i = 0; i < numOps; i++) {
				list.remove(pos);
			}
			return list;
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
		public Object run() {
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
			return list;
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
		public Object run() {
			Random r = getRandom();
			Integer elem = 0;
			int pos = list.size() / 2;
			for (int i = 0; i < numOps; i++) {
				pos = getNearPos(r, pos, list.size(), 0); // near
				for (int n = 0; n < localOps; n++) {
					list.add(pos + n, elem);
				}
			}
			return list;
		}
	}

	public static class AddIterRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public Object run() {
			int cnt = 0;
			for (int i = 0; i < list.size(); i++) {
				Object o = list.get(i);
				int sel = 0; // near
				if (cnt % sel == 0) {
					for (int j = 0; j < numOps / (size / sel); j++) {
						list.add(i + 1, o);
						i++;
					}
				}
				cnt++;
			}
			return list;
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
		public Object run() {
			Random r = getRandom();
			int pos = list.size() / 2;
			for (int i = 0; i < numOps; i++) {
				pos = getNearPos(r, pos, list.size() - localOps, 0); // near
				for (int j = 0; j < localOps; j++) {
					list.remove(pos);
				}
			}
			return list;
		}

	}

	static class RemoveIterRun extends FactoryRun {
		List<Object> list;

		@Override
		public void beforeRun() {
			list = (List<Object>) factory.createSize(size);
		}

		@Override
		public Object run() {
			int sel = 0; // near

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
			return list;
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
		public Object run() {
			list.removeAll(remove);
			return list;
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
		public Object run() {
			list.retainAll(remove);
			return list;
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
		public Object run() {
			int len = size / 2;
			int index = len / 2;
			list.subList(index, index + len).clear();
			return list;
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
		public Object run() {
			int len = size / 2;
			int index = len / 2;
			list.remove(index, len);
			return list;
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
		public Object run() {
			List<Integer> result = list.stream().filter((i) -> i % 2 == 0).collect(Collectors.toList());
			return list;
		}
	}

	public static class FilterIListRun extends FactoryRun {
		IList<Integer> list;

		@Override
		public void beforeAll() {
			list = (IList<Integer>) factory.createSize(size);
		}

		@Override
		public Object run() {
			List<Integer> result = list.filteredList((i) -> i % 2 == 0);
			return list;
		}
	}

}
