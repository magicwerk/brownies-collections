package org.magicwerk.brownies.collections;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.RandomAccess;
import java.util.stream.Collectors;

import org.magicwerk.brownies.collections.TestFactories.ArrayListFactory;
import org.magicwerk.brownies.collections.TestFactories.GapListFactory;
import org.magicwerk.brownies.collections.TestRuns.FilterIListRun;
import org.magicwerk.brownies.collections.TestRuns.FilterLambdaRun;
import org.magicwerk.brownies.collections.ext.TList;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.magicwerk.brownies.core.ArrayTools;
import org.magicwerk.brownies.core.SystemTools;
import org.magicwerk.brownies.core.Timer;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.stat.NumberStat;
import org.magicwerk.brownies.core.stat.StatValues.StoreValues;
import org.magicwerk.brownies.test.JmhRunner;
import org.magicwerk.brownies.test.JmhRunner.Options;
import org.magicwerk.brownies.tools.runner.JvmRunner;
import org.magicwerk.brownies.tools.runner.Runner;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Test performance of GapList.
 *
 * @author Thomas Mauch
 */
public class GapListTestPerformance {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		//run(args);

		testPerformanceJmh();
		//testPerformanceFilterJmh();
		//testPerfFilter();
		//testPerfRemoveRangeAll();
		//testPerfRemoveRetainAll();
		//testPerfCalls();
		//testPerfExtend();
	}

	//

	static void testPerformanceJmh() {
		Options opts = new Options().includeClass(PerformanceJmhTest.class);
		JmhRunner runner = new JmhRunner();
		runner.runJmh(opts);
	}

	public static class PerformanceJmhTest {
		/**
		 * Run benchmark:
		 * java -jar target\benchmarks.jar -wi 5 -i 5 -f 1
		 *
		 * Benchmark                 Mode  Cnt         Score         Error  Units
		 * MyBenchmark.testMethod1  thrpt    5   4970699.315 �  122071.873  ops/s
		 * MyBenchmark.testMethod2  thrpt    5  13985808.890 � 3974616.240  ops/s
		 *
		 * Benchmark                          Mode  Cnt     Score     Error  Units
		 * MyBenchmark.testAddHeadArrayList  thrpt    5     0.947 �  0.054  ops/s
		 * MyBenchmark.testAddHeadBigList    thrpt    5   110.173 �104.469  ops/s
		 * MyBenchmark.testAddHeadGapList    thrpt    5   677.559 � 70.342  ops/s
		 *
		 * MyBenchmark.testAddTailArrayList  thrpt    5  1037.228 � 75.359  ops/s
		 * MyBenchmark.testAddTailBigList    thrpt    5   108.340 �105.254  ops/s
		 * MyBenchmark.testAddTailGapList    thrpt    5   557.762 � 55.324  ops/s
		 *
		 * MyBenchmark.testGetArrayList      thrpt    5  3283.230 �190.602  ops/s
		 * MyBenchmark.testGetBigList        thrpt    5   864.140 � 34.903  ops/s
		 * MyBenchmark.testGetGapList        thrpt    5  2272.831 �129.432  ops/s
		 *
		 * Size    1: Factor 4.0
		 * Size   10: Factor 3.0
		 * Size  100: Factor 2.0
		 * Size 1000: Factor 1.5
		 */

		@State(Scope.Benchmark)
		public static class BenchmarkState {
			final int num = 1000;
			volatile IList<Integer> list = GapList.create();

			public BenchmarkState() {
				for (int i = 0; i < num; i++) {
					list.add(i);
				}
			}
		}

		static int size = 100 * 1000;

		@State(Scope.Benchmark)
		public static class ArrayListState {
			volatile List<Integer> list = new ArrayList<>(size);

			public ArrayListState() {
				for (int i = 0; i < size; i++) {
					list.add(i);
				}
			}
		}

		@State(Scope.Benchmark)
		public static class GapListState {
			volatile IList<Integer> list = GapList.create(size);

			public GapListState() {
				for (int i = 0; i < size; i++) {
					list.add(i);
				}
			}
		}

		@State(Scope.Benchmark)
		public static class BigListState {
			volatile IList<Integer> list = BigList.create();

			public BigListState() {
				for (int i = 0; i < size; i++) {
					list.add(i);
				}
			}
		}

		@Benchmark
		public void testMethod1(BenchmarkState state) {
			List<Integer> result = state.list.stream().filter((i) -> i % 2 == 0).collect(Collectors.toList());
		}

		@Benchmark
		public void testMethod2(BenchmarkState state) {
			List<Integer> result = state.list.filteredList((i) -> i % 2 == 0);
		}

		//

		@Benchmark
		public int testGetArrayList(ArrayListState state) {
			int sum = 0;
			for (int i = 0; i < size; i++) {
				sum += state.list.get(i);
			}
			return sum;
		}

		@Benchmark
		public int testGetGapList(GapListState state) {
			int sum = 0;
			for (int i = 0; i < size; i++) {
				sum += state.list.get(i);
			}
			return sum;
		}

		@Benchmark
		public int testGetBigList(BigListState state) {
			int sum = 0;
			for (int i = 0; i < size; i++) {
				sum += state.list.get(i);
			}
			return sum;
		}

		@Benchmark
		public void testAddTailArrayList(ArrayListState state) {
			List<Integer> list = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
		}

		@Benchmark
		public void testAddTailGapList() {
			IList<Integer> list = GapList.create();
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
		}

		@Benchmark
		public void testAddTailBigList() {
			IList<Integer> list = BigList.create();
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
		}

		@Benchmark
		public void testAddHeadArrayList(ArrayListState state) {
			List<Integer> list = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				list.add(0, i);
			}
		}

		@Benchmark
		public void testAddHeadGapList() {
			IList<Integer> list = GapList.create();
			for (int i = 0; i < size; i++) {
				list.add(0, i);
			}
		}

		@Benchmark
		public void testAddHeadBigList() {
			IList<Integer> list = BigList.create();
			for (int i = 0; i < size; i++) {
				list.add(0, i);
			}
		}

	}

	//

	static void testPerformanceFilterJmh() {
		Options opts = new Options().includeClass(PerformanceFilterJmhTest.class);
		JmhRunner runner = new JmhRunner();
		runner.runJmh(opts);
	}

	public static class PerformanceFilterJmhTest {

		//static int LIST_SIZE = 10; // filter factor 3, map factor 2
		static int LIST_SIZE = 1000; // factor 1.5
		//static int LIST_SIZE = 1_000_000; // factor 1.05

		@State(Scope.Thread)
		public static class ListState {
			IList<Integer> list;
			{
				int num = LIST_SIZE;
				list = new GapList<>(num);
				for (int i = 0; i < num; i++) {
					list.add(i);
				}
			}
		}

		@Benchmark
		public List<Integer> testFilteredList(ListState state) {
			return state.list.filteredList((i) -> i % 2 == 0);
		}

		@Benchmark
		public List<Integer> testFilteredStream(ListState state) {
			return state.list.stream().filter((i) -> i % 2 == 0).collect(Collectors.toList());
		}

		@Benchmark
		public List<Integer> testMappedList(ListState state) {
			return state.list.mappedList(i -> i + 1);
		}

		@Benchmark
		public List<Integer> testMappedStream(ListState state) {
			return state.list.stream().map(i -> i + 1).collect(Collectors.toList());
		}

	}

	//

	static void testPerfFilter() {
		Runner runner = new Runner();
		//runner.setInclude(TestRuns.GAPLIST);
		runner.add(new FilterLambdaRun().setSize(10).setNumOps(100).setFactory(new GapListFactory()).setName("Lambda"));
		runner.add(new FilterIListRun().setSize(10).setNumOps(100).setFactory(new GapListFactory()).setName("IList"));
		runner.run();
		runner.printResults();
	}

	static void testPerfRemoveRangeAll() {
		int size = 10 * 1000;

		Runner runner = new Runner();
		runner.setInclude(TestRuns.GAPLIST, TestRuns.ARRAYLIST);
		new TestRuns(runner).testPerformanceRemoveRange(size);
	}

	static void testPerfRemoveRetainAll() {
		int size = 10 * 1000;

		Runner runner = new Runner();
		runner.setInclude(TestRuns.GAPLIST, TestRuns.ARRAYLIST);
		new TestRuns(runner).testPerformanceRemoveAll(size);

		runner = new Runner();
		runner.setInclude(TestRuns.GAPLIST, TestRuns.ARRAYLIST);
		new TestRuns(runner).testPerformanceRetainAll(size);
	}

	static void testPerfRemoveRetainAll2() {
		// TODO ArrayList is twice as fast as GapList
		final int size = 5 * 1000;
		ArrayList<Integer> al = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			al.add(i);
		}
		GapList<Integer> kl = GapList.create();
		for (int i = 0; i < size; i++) {
			kl.add(i);
		}

		List<Integer> objs = GapList.create();
		for (int i = 0; i < size; i++) {
			if (i % 10 == 0) {
				objs.add(i);
			}
		}

		for (int n = 0; n < 3; n++) {
			final int num = 1 * 1000;
			List<List> lists = GapList.create(al, kl);
			for (List list : lists) {
				Timer t = new Timer();
				for (int i = 0; i < num; i++) {
					t.stop();
					List copy = null;
					if (list instanceof ArrayList) {
						copy = (List) ((ArrayList) list).clone();
					} else {
						copy = (List) ((GapList) list).clone();
					}
					t.start();
					list.removeAll(objs);
				}
				t.printElapsed();
			}
		}

	}

	static void testPerfExtend() {
		doTestPerfExtend(false);
		doTestPerfExtend(true);
	}

	/**
	 * Analyze performance of adding elemnts at the tail to a GapList to
	 * see how extending the array influences the result.
	 */
	static void doTestPerfExtend(boolean print) {
		final int num = 10 * 1000 * 1000;
		IntGapList capacities = IntGapList.create();
		NumberStat<Long> times = new NumberStat<>(StoreValues.ALL_VALUES);
		GapList<Integer> list = GapList.create();
		GapList<Integer> list2 = GapList.create();
		Timer t = new Timer();
		int capacity = list.capacity();
		capacities.add(capacity);
		for (int i = 0; i < num; i++) {
			t.restart();
			list.add(i);
			list2.add(i);
			long nanos = t.stop();
			times.add(nanos);

			if (list.capacity() != capacity) {
				capacity = list.capacity();
				capacities.add(capacity);
			}
		}
		if (print) {
			System.out.println(capacities);
			System.out.println("Count: " + times.count());
			System.out.println("Min: " + times.min());
			System.out.println("Max: " + times.max());
			System.out.println("Avg: " + times.avg());
			System.out.println("Median: " + times.median());
			List<Long> values = times.values();
			for (int i = 1; i < capacities.size(); i++) {
				int c = capacities.get(i);
				if (c + 1 >= values.size()) {
					break;
				}
				System.out.println(c - 1 + ": " + values.get(c - 1));
				System.out.println(c + ": " + values.get(c));
				System.out.println(c + 1 + ": " + values.get(c + 1));
			}
		}
	}

	static void run(String[] args) {
		// Run directly
		//doRun();

		// Run as separate Java process
		runJava(args);
	}

	static void test() {
		int numAdd = 1000 * 1000;
		List<Object> adds = new GapList(numAdd);
		for (int i = 0; i < numAdd; i++) {
			adds.add(null);
		}

		int num = 1000;
		Timer t = new Timer();
		for (int i = 0; i < num; i++) {
			GapList<Object> list = new GapList(0);
			list.init(adds);
		}
		t.printElapsed();

		t = new Timer();
		for (int i = 0; i < num; i++) {
			GapList<Object> list = new GapList(0);
			list.addAll(adds);
		}
		t.printElapsed();
	}

	static void runJava(String[] args) {
		String RELEASE = "0.9.16"; // FIXME retrieve automatically

		JvmRunner runner = new JvmRunner();
		runner.setRunnable((a) -> doRun());
		runner.setStart(() -> {
			PerformanceReport report = new PerformanceReport();
			report.load();
			report.deleteRelease(RELEASE);
			report.save();
		});

		String java6 = "C:\\Java\\JDK\\jdk1.6.0_45\\bin\\java.exe";
		String java7 = "C:\\Java\\JDK\\jdk1.7.0_40\\bin\\java.exe";
		String java8 = "C:\\dev\\Java\\JDK\\jdk1.8.0_25\\bin\\java.exe";
		String java8_64 = "C:\\dev\\Java\\JDK\\jdk1.8.0_25-x64\\bin\\java.exe";

		//String[] jvmArgs = new String[] { "-Xms512m", "-Xmx512m" };
		String[] jvmArgs = new String[] { "-Xms1024m", "-Xmx1024m" };

		//tester.addJavaArgsRun(java6, jvmArgs);
		//tester.addJavaArgsRun(java7, jvmArgs);
		runner.addJavaArgsRun(java8, jvmArgs);
		runner.addJavaArgsRun(java8_64, jvmArgs);

		LogbackTools.setAllLevels(Level.INFO);
		LogbackTools.setLogLevel("org.magicwerk.brownies.test.runner.JvmTester", Level.DEBUG);
		runner.run(args);

		//			        tester.addJvmArgsRun("-Xmx64m");	//  5'089'000
		//			        tester.addJvmArgsRun("-Xmx128m");	// 11'451'000
		//			        tester.addJvmArgsRun("-Xmx256m");	// 25'764'000
		//			tester.addJvmArgsRun("-Xmx512m");	// 38'647'000
		//			        tester.addJvmArgsRun("-Xmx1024m");	// 86'956'000
		//			        tester.addJvmArgsRun("-Xmx1536m");	// 130'435'000

		//tester.addJvmArgsRun();
	}

	//--- Memory ---

	static void doRun() {
		//TimerTools.sleep(10*1000);

		LogbackTools.setConsoleOut();
		//LogbackTools.setConsoleLevel(Level.DEBUG);

		// Check that assertions are disabled
		//assert(false);

		//		testArrayList();

		// Use -XX:+PrintCompilation -verbose:gc
		//showVmArgs(); //-Xms512m -Xmx512m

		boolean orig = true;
		if (orig) {
			int size = 100 * 1000;
			int numOps = 50 * 1000;

			// Chart 1: Get
			newRun().testPerformanceGetLast(size, numOps);
			newRun().testPerformanceGetFirst(size, numOps);
			newRun().testPerformanceGetRandom(size, numOps);

			// Chart 2: Add
			size = 10 * 1000;
			numOps = 10 * 1000;
			newRun().testPerformanceAddLast(size, numOps);
			newRun().testPerformanceAddFirst(size, numOps);
			newRun().testPerformanceAddRandom(size, numOps);

			// Chart 3: Add near iter
			newRun().testPerformanceAddNear(size, numOps, 0.1);
			newRun().testPerformanceAddNear(size, numOps, 0.01);
			newRun().testPerformanceAddIter(size, numOps, 2);
		}

		boolean chart = false;
		if (chart) {
			int size = 10 * 1000;
			int numOps = 10 * 1000;

			// Chart 1: Get
			newRun().testPerformanceGetLast(size, numOps);
			newRun().testPerformanceGetFirst(size, numOps);
			newRun().testPerformanceGetRandom(size, numOps);

			// Chart 2: Add
			newRun().testPerformanceAddLast(size, numOps);
			newRun().testPerformanceAddFirst(size, numOps);
			newRun().testPerformanceAddMiddle(size, numOps);

			// Chart 3: Add near iter
			//newRun().testPerformanceAddRandom(size, numOps);
			newRun().testPerformanceAddNear(size, numOps, 1.0);
			newRun().testPerformanceAddNear(size, numOps, 0.1);
			newRun().testPerformanceAddNear(size, numOps, 0.01);
		}

		//		newRun().testPerformanceAddIter(size, numOps, 2);

		// Other benchmarks
		// Remove
		//		testPerformanceRemoveLast(size, numOps);
		//		testPerformanceRemoveFirst(size, numOps);
		//		testPerformanceRemoveRandom(size, numOps);

		//		testPerformanceAddIter(size, numOps, 100);
		//		testPerformanceAddIter(size, numOps, 1000);
		//
		//		testPerformanceAddRandom(size, numOps);
		//		testPerformanceAddNear(size, numOps, 1);
		//		testPerformanceAddNear(size, numOps, 0.001);
		//
		//		testPerformanceRemoveRandom(size, numOps);
		//		testPerformanceRemoveNear(size, numOps, 1);
		//		testPerformanceRemoveNear(size, numOps, 0.1);
		//		testPerformanceRemoveNear(size, numOps, 0.01);
		//		testPerformanceRemoveNear(size, numOps, 0.001);
		//
		//		testPerformanceRemoveIter(size, 2);
		//		testPerformanceRemoveIter(size, 5);
		//		testPerformanceRemoveIter(size, 10);

		//		testPerformanceAddRandomNear(numAdds, 20);
		//		testPerformanceAddRandomNear(numAdds, 10);
		//
		//		// Remove
		//		int numRemoves = 100*1000;
		//		testPerformanceRemoveRandomNear(numRemoves, 50);
		//		testPerformanceRemoveRandomNear(numRemoves, 20);
		//		testPerformanceRemoveRandomNear(numRemoves, 10);

		//		testPerformanceAddRandomNear(numAdds, 100);
		//		testPerformanceAddRandomNear(numAdds, 20);
		//		testPerformanceAddRandomNear(numAdds, 10);
		//		testPerformanceAddRandomNear(numAdds, 5);
		//
		//		testPerformanceIterAdd(100*1000, 10);
		//		testPerformanceIterAdd(100*1000, 100);
		//		testPerformanceIterAdd(100*1000, 1000);
		//
		//		testPerformanceRemoveLastRange(100*1000, 1000);
		//		testPerformanceRemoveFirstRange(100*1000, 1000);
		//
		//		testPerformanceIterRemove(10*1000, 5);
		//		testPerformanceIterRemove(10*1000, 10);
		//		testPerformanceIterRemove(10*1000, 50);
		//		testPerformanceIterRemove(10*1000, 100);
		//
		//		//testPerformanceMaxList();
		//
		//		//TimerTools.start();
		//		//allocArray(85*1024*1024);			//  85m, 0.375 s
		//		//allocArrayList(36*1024*1024);		//  36m, 1.359 s
		//		//allocArrayListSize(85*1024*1024);	//  85m, 1.641 s
		//		//allocLinkedList(20*1024*1024);	//  20m, 2.406 s
		//		//allocGapList(32*1024*1024);		// 32m, 1.452 s
		//
		//		//allocBigGapList(120*1024*1024);	// 120m, 4.524 s
		//		//allocFastTable(120*1024*1024);		// 120m, 2.527 s
		//
		//		//allocBigGapList(16*1024*1024);	// 16m 0.9s
		//		//allocFastTable(16*1024*1024);		// 120m, 0.3s
		//		//TimerTools.stopPrint();

		//		int size=1000*1000;
		//		while (size <= 100*1000*1000) {
		//			final int s = size;
		//			Log.info("Size {}", s);
		//			run("BigList.addLast", new Test() {
		//				public void run() {
		//					addLast(new BigList<Object>(), s);
		//				}
		//			});
		//			run("FastTable.addLast", new Test() {
		//				public void run() {
		//					addLast(new FastTable<Object>(), s);
		//				}
		//			});
		//			size *= 2;
		//		}
		//testMemory("allocFastTable");
		//testMemory("allocBigList");
		//		testMemory("allocArray");
		//		testMemory("allocArrayList");
		//		testMemory("allocArrayListSize");
		//		testMemory("allocGapList");
		//		testMemory("allocGapListSize");
	}

	public static void showVmArgs() {
		List<String> lst = SystemTools.getJvmArgs();
		for (int i = 0; i < lst.size(); i++) {
			System.out.println(lst.get(i));
		}
	}

	//--- Performance ---

	static TestRuns newRun() {
		Runner runner = new Runner();
		runner.setInclude(TestRuns.GAPLIST, TestRuns.ARRAYLIST, TestRuns.LINKEDLIST);
		//		runner.setInclude(TREELIST);
		//		runner.setInclude(LINKEDLIST);
		//		runner.setInclude(CIRCULARARRAYLIST, GAPLIST);
		//		runner.setInclude(GAPLIST, "RootishArrayStack", "DualRootishArrayDeque", "DualArrayDeque");
		//		runner.setInclude(GAPLIST, CIRCULARARRAYLIST, TREELIST);
		//		//runner.setExclude(BIGLIST);
		//		runner.setInclude(GAPLIST, CIRCULARARRAYLIST);
		return new TestRuns(runner);
	}

	//
	/*
		static void testPerformanceRemoveLastRange(final int size, final int numRem) {
			Runner runner = newRunner("Remove last range " + size + "/" + numRem);
			runner.add(new Run("GapList", GAPLIST) {
				GapList<Object> list;
	
				public void initEach() {
					list = GapListFactory.allocGapListSize(size);
				}
	
				public void run() {
					while (list.size() > numRem) {
						list.remove(list.size() - numRem, numRem);
					}
				}
			});
			runner.add(new Run("ArrayList", ARRAYLIST) {
				ArrayList<Object> list;
	
				public void initEach() {
					list = ArrayListFactory.allocArrayListSize(size);
				}
	
				public void run() {
					while (list.size() > numRem) {
						for (int i = 0; i < numRem; i++) {
							list.remove(list.size() - i - 1);
						}
					}
				}
			});
			runner.add(new Run("ArrayList subList", ARRAYLIST) {
				ArrayList<Object> list;
	
				public void initEach() {
					list = ArrayListFactory.allocArrayListSize(size);
				}
	
				public void run() {
					while (list.size() > numRem) {
						list.subList(list.size() - numRem, list.size()).clear();
					}
				}
			});
			runner.add(new Run("LinkedList", LINKEDLIST) {
				LinkedList<Object> list;
	
				public void initEach() {
					list = LinkedListFactory.allocLinkedList(size);
				}
	
				public void run() {
					while (list.size() > numRem) {
						for (int i = 0; i < numRem; i++) {
							list.removeLast();
						}
					}
				}
			});
			runner.add(new Run("LinkedList subList", LINKEDLIST) {
				LinkedList<Object> list;
	
				public void initEach() {
					list = LinkedListFactory.allocLinkedList(size);
				}
	
				public void run() {
					while (list.size() > numRem) {
						list.subList(list.size() - numRem, list.size()).clear();
					}
				}
			});
			runner.run();
			runner.printResults();
		}
	
		static void testPerformanceRemoveFirstRange(final int size, final int numRem) {
			Runner runner = newRunner("Remove first range " + size + "/" + numRem);
			runner.add(new Run("GapList", GAPLIST) {
				GapList<Object> list;
	
				public void initEach() {
					list = GapListFactory.allocGapListSize(size);
				}
	
				public void run() {
					while (list.size() > numRem) {
						list.remove(0, numRem);
					}
				}
			});
			runner.add(new Run("ArrayList single", ARRAYLIST) {
				ArrayList<Object> list;
	
				public void initEach() {
					list = ArrayListFactory.allocArrayListSize(size);
				}
	
				public void run() {
					while (list.size() > numRem) {
						for (int i = 0; i < numRem; i++) {
							list.remove(0);
						}
					}
				}
			});
			runner.add(new Run("ArrayList subList", ARRAYLIST) {
				ArrayList<Object> list;
	
				public void initEach() {
					list = ArrayListFactory.allocArrayListSize(size);
				}
	
				public void run() {
					while (list.size() > numRem) {
						list.subList(0, numRem).clear();
					}
				}
			});
			runner.add(new Run("LinkedList", LINKEDLIST) {
				LinkedList<Object> list;
	
				public void initEach() {
					list = LinkedListFactory.allocLinkedList(size);
				}
	
				public void run() {
					while (list.size() > numRem) {
						for (int i = 0; i < numRem; i++) {
							list.removeFirst();
						}
					}
				}
			});
			runner.add(new Run("LinkedList subList", LINKEDLIST) {
				LinkedList<Object> list;
	
				public void initEach() {
					list = LinkedListFactory.allocLinkedList(size);
				}
	
				public void run() {
					while (list.size() > numRem) {
						list.subList(0, numRem).clear();
					}
				}
			});
			runner.run();
			runner.printResults();
		}
	*/
	static void testArrayList() {
		testArrayListRemoveRange();
		//		subList.add("a");
		//		subList.addAll(Arrays.asList(new String[] { "b", "c" }));
		//		System.out.println(list.size());
	}

	// Removing a range through subList() is efficiently done
	// with a single System.arraycopy() call.
	static void testArrayListRemoveRange() {
		ArrayList<Object> list;
		List<Object> subList;

		list = ArrayListFactory.allocArrayListSize(1000);
		subList = list.subList(0, 100);
		subList.clear();

		list = ArrayListFactory.allocArrayListSize(1000);
		subList = list.subList(900, 1000);
		subList.clear();
	}

	static Object[] allocArray(int size) {
		Object[] l = new Object[size];
		Object obj = new Object();
		for (int i = 0; i < size; i++) {
			l[i] = obj;
		}
		return l;
	}

	static GapList<Object> allocColl(int size, boolean set) {
		Object obj = new Object();
		GapList<Object> l = new GapList<Object>();
		for (int i = 0; i < size; i++) {
			if (set) {
				obj = new Integer(i);
			}
			l.add(i, obj);
		}
		return l;
	}

	static void get(List<Object> l) {
		int num = l.size();
		for (int i = 0; i < num; i++) {
			Object o = l.get(i);
		}
	}

	static void get2(List<Object> list) {
		int l = 0;
		int r = list.size() - 1;
		while (l <= r) {
			Object ol = list.get(l);
			if (l < r) {
				Object or = list.get(r);
			}
			l++;
			r--;
		}
	}

	static void addFirst(List<Object> l, int num) {
		Object obj = new Object();
		for (int i = 0; i < num; i++) {
			l.add(0, obj);
		}
	}

	static void addLast(List<Object> l, int num) {
		Object obj = new Object();
		int index = l.size();
		for (int i = 0; i < num; i++) {
			l.add(index, obj);
			index++;
		}
	}

	static void addFirstLast(List<Object> l, int num) {
		Object obj = new Object();
		int index = l.size();
		for (int i = 0; i < num; i++) {
			if (i % 2 == 0) {
				l.add(0, obj);
			} else {
				l.add(index, obj);
			}
			index++;
		}
	}

	static void addFirstRemoveLast(List<Object> l, int num) {
		Object obj = new Object();
		for (int i = 0; i < num; i++) {
			l.add(0, obj);
			l.remove(l.size() - 1);
		}
	}

	static void addLastRemoveFirst(List<Object> l, int num) {
		Object obj = new Object();
		for (int i = 0; i < num; i++) {
			obj = new Double(10 + i);//TODO
			l.add(l.size(), obj);
			l.remove(0);
		}
	}

	static void addFirst(Deque<Object> l, int num) {
		Object obj = new Object();
		for (int i = 0; i < num; i++) {
			l.addFirst(obj);
		}
	}

	static void addLast(Deque<Object> l, int num) {
		Object obj = new Object();
		for (int i = 0; i < num; i++) {
			l.addLast(obj);
		}
	}

	static void addFirstLast(Deque<Object> l, int num) {
		Object obj = new Object();
		for (int i = 0; i < num; i++) {
			if (i % 2 == 0) {
				l.addFirst(obj);
			} else {
				l.addLast(obj);
			}
		}
	}

	static void addFirstRemoveLast(Deque<Object> l, int num) {
		Object obj = new Object();
		for (int i = 0; i < num; i++) {
			l.addFirst(obj);
			l.removeLast();
		}
	}

	static void addLastRemoveFirst(Deque<Object> l, int num) {
		Object obj = new Object();
		for (int i = 0; i < num; i++) {
			l.addLast(obj);
			l.removeFirst();
		}
	}

	//	static FastTable<Object> allocFastTable(int size) {
	//		Integer obj = new Integer(0);
	//		FastTable<Object> l = new FastTable<Object>();
	//		for (int i=0; i<size; i++) {
	//			l.add(i, obj);
	//		}
	//		return l;
	//	}

	static TList allocTList(int size) {
		Integer obj = new Integer(0);
		TList l = new TList();
		l.setMode(TList.NORMAL);
		for (int i = 0; i < size; i++) {
			l.add(i, obj);
		}
		return l;
	}

	static interface IMutableObject {
		public void setObject(Object o);

		public Object getObject();
	}

	static class MutableObject implements IMutableObject {
		private Object object;

		public MutableObject(Object object) {
			this.object = object;
		}

		@Override
		public void setObject(Object object) {
			this.object = object;
		}

		@Override
		public Object getObject() {
			return this.object;
		}

	}

	//	static HugeArrayList<IMutableObject> allocHugeList(int size) {
	//		Object obj = new Object();
	//		HugeArrayList<IMutableObject> hugeList = new HugeArrayBuilder<IMutableObject>() {}.create();
	//		for (int i=0; i<size; i++) {
	//			hugeList.add(i, new MutableObject(obj));
	//		}
	//		return hugeList;
	//	}

	static class Name {
		private String name;

		public Name(String name) {
			this.name = name;
		}
	}

	static void runBharat() {
		int num = 100 * 90000;
		GapList gapList = new GapList();
		//gapList.ensureCapacity(9000);
		//ComplexObject obj=null;
		Object obj = null;
		long before = System.currentTimeMillis();
		//System.out.println(System.currentTimeMillis());
		for (int i = 0; i < num; i++) {
			//obj = new ComplexObject(i, "String1", new Double(i), new ArrayList(), null);
			obj = new Name("String1");
			//gapList.addLast(obj);
			gapList.add(obj);
		}

		long afterAdd = System.currentTimeMillis();
		System.out.println("Difference Gap List after add" + (afterAdd - before));
		for (int i = 0; i < num; i++) {
			gapList.getLast();
		}

		long after = System.currentTimeMillis();
		System.out.println("Difference Gap List after remove " + (after - afterAdd));

		System.out.println("----------------------------------------------------------");

		List arrayList = new ArrayList();
		before = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {
			//	double j=Math.random();
			//obj = new ComplexObject(i, "String1", new Double(i), new ArrayList(), null);
			obj = new Name("String1");
			arrayList.add(obj);
		}
		afterAdd = System.currentTimeMillis();
		System.out.println("Difference Gap List after add" + (afterAdd - before));

		for (int i = 0; i < num; i++) {
			arrayList.get(i);
		}
		//
		after = System.currentTimeMillis();
		System.out.println("Difference Array List aftre remove" + (after - afterAdd));
	}

	static void testPerfCalls() {
		Integer[] i10 = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		List<Integer> lf10 = asFixedList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
		List<Integer> lr10 = asRepeatList(10, 5);

		final int NUM = 1000 * 1000;

		Timer t;

		// Repeat List
		t = new Timer();
		for (int i = 0; i < NUM; i++) {
			sum1(10, 5);
		}
		t.printElapsed();

		t = new Timer();
		for (int i = 0; i < NUM; i++) {
			sum3(asRepeatList(10, 5));
		}
		t.printElapsed();

		t = new Timer();
		for (int i = 0; i < NUM; i++) {
			sum4(asRepeatList(10, 5));
		}
		t.printElapsed();

		// Fixed List
		t = new Timer();
		for (int i = 0; i < NUM; i++) {
			sum2(i10);
		}
		t.printElapsed();

		t = new Timer();
		for (int i = 0; i < NUM; i++) {
			sum3(asFixedList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
		}
		t.printElapsed();

		t = new Timer();
		for (int i = 0; i < NUM; i++) {
			sum4(asFixedList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
		}
		t.printElapsed();
	}

	static int sum1(int num, Integer elem) {
		int sum = 0;
		for (int i = 0; i < num; i++) {
			sum += elem;
		}
		return sum;
	}

	static int sum2(Integer[] elems) {
		int sum = 0;
		for (Integer elem : elems) {
			sum += elem;
		}
		return sum;
	}

	static int sum3(List<Integer> elems) {
		int sum = 0;
		for (Integer elem : elems) {
			sum += elem;
		}
		return sum;
	}

	static int sum4(List<Integer> elems) {
		Object[] a = elems.toArray();
		int sum = 0;
		for (int i = 0; i < a.length; i++) {
			sum += (Integer) a[i];
		}
		return sum;
	}

	public static <T> List<T> asFixedList(T... a) {
		return new FixedArrayList<T>(a);
	}

	public static <T> List<T> asRepeatList(int num, T elem) {
		return new RepeatArrayList<T>(num, elem);
	}

	static class FixedArrayList<E> extends AbstractList<E> implements RandomAccess, java.io.Serializable {
		private static final long serialVersionUID = -2764017481108945198L;
		private final E[] a;

		FixedArrayList(E[] array) {
			if (array == null)
				throw new NullPointerException();
			a = array;
		}

		@Override
		public int size() {
			return a.length;
		}

		@Override
		public Object[] toArray() {
			return a.clone();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			int size = size();
			if (a.length < size) {
				return Arrays.copyOf(this.a, size, (Class<? extends T[]>) a.getClass());
			}
			System.arraycopy(this.a, 0, a, 0, size);
			if (a.length > size) {
				a[size] = null;
			}
			return a;
		}

		@Override
		public E get(int index) {
			return a[index];
		}

		@Override
		public E set(int index, E element) {
			E oldValue = a[index];
			a[index] = element;
			return oldValue;
		}

		@Override
		public int indexOf(Object o) {
			if (o == null) {
				for (int i = 0; i < a.length; i++) {
					if (a[i] == null) {
						return i;
					}
				}
			} else {
				for (int i = 0; i < a.length; i++) {
					if (o.equals(a[i])) {
						return i;
					}
				}
			}
			return -1;
		}

		@Override
		public boolean contains(Object o) {
			return indexOf(o) != -1;
		}
	}

	static class RepeatArrayList<E> extends AbstractList<E> implements RandomAccess, java.io.Serializable {
		private static final long serialVersionUID = -2764017481108945198L;
		private final int num;
		private final E elem;

		RepeatArrayList(int num, E elem) {
			this.num = num;
			this.elem = elem;
		}

		@Override
		public int size() {
			return num;
		}

		@Override
		public Object[] toArray() {
			Object[] array = new Object[num];
			Arrays.fill(array, elem);
			return array;
		}

		@Override
		public <T> T[] toArray(T[] a) {
			if (a.length >= num) {
				Arrays.fill(a, 0, num, elem);
				if (a.length > num) {
					a[num] = null;
				}
				return a;
			}
			Object[] array = ArrayTools.create(ArrayTools.getComponentType(a), num);
			Arrays.fill(array, elem);
			return (T[]) array;
		}

		@Override
		public E get(int index) {
			return elem;
		}

		@Override
		public E set(int index, E element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int indexOf(Object o) {
			if (o == elem) {
				return 0;
			}
			return -1;
		}

		@Override
		public boolean contains(Object o) {
			return indexOf(o) != -1;
		}
	}

}
