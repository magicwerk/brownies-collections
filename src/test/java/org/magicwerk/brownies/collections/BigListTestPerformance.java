package org.magicwerk.brownies.collections;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.magicwerk.brownies.collections.TestFactories.ArrayListFactory;
import org.magicwerk.brownies.collections.TestFactories.BigListFactory;
import org.magicwerk.brownies.collections.TestFactories.CollectionFactory;
import org.magicwerk.brownies.collections.TestFactories.FastTableFactory;
import org.magicwerk.brownies.collections.TestFactories.GapListFactory;
import org.magicwerk.brownies.collections.TestFactories.IntBigListFactory;
import org.magicwerk.brownies.collections.TestFactories.IntGapListFactory;
import org.magicwerk.brownies.collections.TestFactories.LinkedListFactory;
import org.magicwerk.brownies.collections.TestFactories.TreeListFactory;
import org.magicwerk.brownies.collections.TestRuns.AddAllInitRun;
import org.magicwerk.brownies.collections.TestRuns.AddFirstRunList;
import org.magicwerk.brownies.collections.TestRuns.AddInitRun;
import org.magicwerk.brownies.collections.TestRuns.AddIntRandomRun;
import org.magicwerk.brownies.collections.TestRuns.AddIntWrapperRandomRun;
import org.magicwerk.brownies.collections.TestRuns.AddLastRunList;
import org.magicwerk.brownies.collections.TestRuns.AddMiddleRun;
import org.magicwerk.brownies.collections.TestRuns.AddMultRun;
import org.magicwerk.brownies.collections.TestRuns.AddNearRun;
import org.magicwerk.brownies.collections.TestRuns.AddRandomRun;
import org.magicwerk.brownies.collections.TestRuns.CloneRun;
import org.magicwerk.brownies.collections.TestRuns.FactoryRun;
import org.magicwerk.brownies.collections.TestRuns.GetIntRandomRun;
import org.magicwerk.brownies.collections.TestRuns.GetIntWrapperRandomRun;
import org.magicwerk.brownies.collections.TestRuns.GetIterRun;
import org.magicwerk.brownies.collections.TestRuns.GetRandomRun;
import org.magicwerk.brownies.collections.TestRuns.RemoveFirstRunList;
import org.magicwerk.brownies.collections.TestRuns.RemoveIntRandomRun;
import org.magicwerk.brownies.collections.TestRuns.RemoveIntWrapperRandomRun;
import org.magicwerk.brownies.collections.TestRuns.RemoveLastRunList;
import org.magicwerk.brownies.collections.TestRuns.RemoveMiddleRun;
import org.magicwerk.brownies.collections.TestRuns.RemoveMultRun;
import org.magicwerk.brownies.collections.TestRuns.RemoveNearRun;
import org.magicwerk.brownies.collections.TestRuns.RemoveRandomRun;
import org.magicwerk.brownies.collections.TestRuns.SortRun;
import org.magicwerk.brownies.core.StreamTools;
import org.magicwerk.brownies.core.SystemTools;
import org.magicwerk.brownies.core.Timer;
import org.magicwerk.brownies.core.concurrent.ThreadTools;
import org.magicwerk.brownies.core.exceptions.FileException;
import org.magicwerk.brownies.core.exceptions.FileException.Access;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.core.strings.StringStreamer;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.magicwerk.brownies.tools.runner.JvmRunner;
import org.magicwerk.brownies.tools.runner.Run;
import org.magicwerk.brownies.tools.runner.Runner;
import org.magicwerk.brownies.tools.runner.Runner.RunIterations;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

import javolution.util.FastTable;

/**
 * Test performance of BigList.
 * If the test is run, it must produce similar results as in
 * http://java.dzone.com/articles/biglist-scalable-high
 *
 * @author Thomas Mauch
 */
public class BigListTestPerformance {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		// Run as separate process(es)
		//runJava(args);

		// Run in this process for debugging
		testPerformanceAddAllJmh();
		//doRun();
		//testSort();
		//testStandard();
	}

	//

	static void testPerformanceAddAllJmh() {
		Options opts = new Options().includeClass(PerformanceAddAllJmhTest.class);
		opts.setRunTimeMillis(500);
		JmhRunner runner = new JmhRunner();
		runner.runJmh(opts);
	}

	public static class PerformanceAddAllJmhTest {

		static int initSize = 10_000;
		static int addSize = 10_000;

		@State(Scope.Benchmark)
		public static class MyState {

			IList<Integer> addAllList;
			IList<Integer> addRepeatedList;
			IList<Integer> addList;

			@Setup(Level.Iteration)
			public void setup() {
				LOG.info("setup");
				addAllList = getSortedBigList(initSize);
				addRepeatedList = getSortedBigList(initSize);
				addList = getSortedBigList(addSize);
			}
		}

		@Benchmark
		public void testAddAllLast(MyState state) {
			state.addAllList.addAll(state.addList);
		}

		@Benchmark
		public void testAddRepeatedLast(MyState state) {
			for (int i = 0; i < state.addList.size(); i++) {
				state.addRepeatedList.add(state.addList.get(i));
			}
		}

		@Benchmark
		public void testAddAllFirst(MyState state) {
			state.addAllList.addAll(0, state.addList);
		}

		@Benchmark
		public void testAddRepeatedFirst(MyState state) {
			for (int i = 0; i < state.addList.size(); i++) {
				state.addRepeatedList.add(i, state.addList.get(i));
			}
		}

		@Benchmark
		public void testAddAllMid(MyState state) {
			int index = state.addAllList.size() / 2;
			state.addAllList.addAll(index, state.addList);
		}

		@Benchmark
		public void testAddRepeatedMid(MyState state) {
			int index = state.addAllList.size() / 2;
			for (int i = 0; i < state.addList.size(); i++) {
				state.addRepeatedList.add(index + i, state.addList.get(i));
			}
		}

	}

	static void testAddAllPerformance() {
		int initSize = 100_000;
		int addSize = 10_000;
		IList<Integer> add = getSortedBigList(addSize);

		{
			IList<Integer> list = getSortedBigList(initSize);
			new Runner("addAll").run(() -> {
				list.addAll(add);
			});
		}
		{
			IList<Integer> list = getSortedBigList(initSize);
			new Runner("repeated add").run(() -> {
				for (int i = 0; i < add.size(); i++) {
					list.add(add.get(i));
				}
			});
		}
	}

	static IList<Integer> getSortedBigList(int size) {
		IList<Integer> list = new BigList<>();
		for (int i = 0; i < size; i++) {
			list.add(i);
		}
		return list;
	}

	//

	static void runJava(String[] args) {
		JvmRunner runner = new JvmRunner();
		runner.setRunnable((a) -> doRun());

		String java8 = "C:\\dev\\Java\\JDK\\jdk1.8.0_241\\bin\\java.exe";
		String java11 = "C:\\dev\\Java\\JDK\\jdk-11.0.6\\bin\\java.exe";
		String java17 = "C:\\dev\\Java\\JDK\\jdk-17.0.2\\bin\\java.exe";

		String[] jvmArgs = new String[] { "-Xms1024m", "-Xmx1024m",
				//		String[] jvmArgs = new String[] { "-Xms256m", "-Xmx256m",
				"-XX:+UseG1GC",
				//				"-XX:+PrintGC",
				//				"-XX:+PrintGCDetails",
				//				"-XX:+PrintGCTimeStamps"
		};

		//tester.addJavaArgsRun(java6, jvmArgs);
		//tester.addJavaArgsRun(java7, jvmArgs);
		runner.addJavaArgsRun(java8, jvmArgs);
		runner.addJavaArgsRun(java11, jvmArgs);
		runner.addJavaArgsRun(java17, jvmArgs);
		//tester.addJavaArgsRun(java8_64, jvmArgs);

		//LogbackTools.setAllLevels(Level.INFO);
		//LogbackTools.setAllLevels(Level.DEBUG);
		//LogbackTools.setLogLevel("org.magicwerk.brownies.test.runner.JvmTester", Level.DEBUG);
		runner.run(args);

		//			        tester.addJvmArgsRun("-Xmx64m");	//  5'089'000
		//			        tester.addJvmArgsRun("-Xmx128m");	// 11'451'000
		//			        tester.addJvmArgsRun("-Xmx256m");	// 25'764'000
		//			tester.addJvmArgsRun("-Xmx512m");	// 38'647'000
		//			        tester.addJvmArgsRun("-Xmx1024m");	// 86'956'000
		//			        tester.addJvmArgsRun("-Xmx1536m");	// 130'435'000

		//tester.addJvmArgsRun();
	}

	static int BigListTestSize = 1 * 1000 * 1000;
	static int GapListTestSize = 10 * 1000;
	static int size = BigListTestSize;

	static int BigListBlockSize = 1000;

	static CollectionFactory[] factories = new CollectionFactory[] { new ArrayListFactory(), new LinkedListFactory(), new GapListFactory(),
			new BigListFactory(BigListBlockSize), new FastTableFactory(), new TreeListFactory() };

	static void doRun() {
		testStandard();
		//doTestBigList();
	}

	static void doTestBigList() {
		testGetRandom();
		testAddRandom();
		testRemoveRandom();

		testGetIter();
		testAddNear();
		testRemoveNear();

		testAddMult();
		testRemoveMult();
		testClone();
	}

	static void runSingle() {
		Runner runner = new Runner("Single run");
		runner.add(new AddLastRunList().setAll(true).setSize(size).setNumOps(1).setLocalOps(5000).setFactory(new BigListFactory()));
		//		runner.add(new AddMiddleRun().setSize(1000*1000).setNumOps(1).setFactory(new FastTableFactory()));
		RunIterations initIters = new RunIterations();
		initIters.maxIters = 0;
		runner.setInitIterations(initIters);
		RunIterations runIters = new RunIterations();
		runIters.maxIters = 1;
		runner.setRunIterations(runIters);
		runner.run();
		runner.printResults();
	}

	static void doRunPrimitive() {
		LOG.info("Run with {}", SystemTools.getJvmArgsString());

		testGetIntRandomGap();
		testGetIntRandomBig();
		testAddIntRandomGap();
		testAddIntRandomBig();
		testRemoveIntRandomGap();
		testRemoveIntRandomBig();
	}

	static void testCompareGapList() {
		for (int size : new int[] { 1000, 5000, 10 * 1000, 50 * 1000, 100 * 1000, 500 * 1000, 1000 * 1000 }) {

			System.out.println("Size= " + size);

			for (int loc = 1; loc <= 3; loc++) {
				Timer t;

				t = new Timer();
				test(new ArrayList(), size, loc);
				t.printElapsed("ArrayList");

				t = new Timer();
				test(GapList.create(), size, loc);
				t.printElapsed("GapList  ");

				t = new Timer();
				test(BigList.create(), size, loc);
				t.printElapsed("BigList  ");

				ThreadTools.sleep(250);
			}
		}
	}

	static void test(List list, int size, int loc) {
		int num = size / loc;
		for (int n = 0; n < num; n++) {
			int s = n * 5;
			for (int i = 0; i < loc; i++) {
				list.add(0, i);
			}
			for (int i = 0; i < loc; i++) {
				list.add(s / 4 * 1, i);
			}
			for (int i = 0; i < loc; i++) {
				list.add(s / 4 * 2, i);
			}
			for (int i = 0; i < loc; i++) {
				list.add(s / 4 * 3, i);
			}
			for (int i = 0; i < loc; i++) {
				list.add(list.size() - 1, i);
			}
		}

	}

	static void testStandard() {
		//TimerTools.sleep(10*1000);

		LogbackTools.setConsoleOut();
		//LogbackTools.setConsoleLevel(Level.DEBUG);

		// Check that assertions are disabled
		//assert(false);

		//		testArrayList();

		// Use -XX:+PrintCompilation -verbose:gc
		//showVmArgs(); //-Xms512m -Xmx512m

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
		//newRun().testPerformanceAddNear(size, numOps, 0.1);
		//newRun().testPerformanceAddNear(size, numOps, 0.01);
		//newRun().testPerformanceAddIter(size, numOps, 2);
	}

	static TestRuns newRun() {
		Runner runner = new Runner();
		//		runner.setInclude(TREELIST);
		//		runner.setInclude(LINKEDLIST);
		//		runner.setInclude(CIRCULARARRAYLIST, GAPLIST);
		//		runner.setInclude(GAPLIST, "RootishArrayStack", "DualRootishArrayDeque", "DualArrayDeque");
		//		runner.setInclude(GAPLIST, CIRCULARARRAYLIST, TREELIST);
		//		//runner.setExclude(BIGLIST);
		//		runner.setInclude(GAPLIST, CIRCULARARRAYLIST);
		TestRuns runs = new TestRuns(runner);
		//		runs.setFactories(new BigListFactory(10000));
		//runs.setFactories(new GapListFactory(), new BigListFactory(10000), new BigListFactory(50000), new FastTableFactory());
		runs.setFactories(new GapListFactory(), new ArrayListFactory(), new LinkedListFactory());
		return runs;
	}

	static void testSort() {
		Runner runner = new Runner("Sort");
		int size = 10 * 1000;
		runner.add(new SortRun().setFactory(new ArrayListFactory()).setSize(size));
		runner.add(new SortRun().setFactory(new GapListFactory()).setSize(size));
		runner.add(new SortRun().setFactory(new BigListFactory(1000)).setSize(size));
		runner.add(new SortRun().setFactory(new BigListFactory(2000)).setSize(size));
		runner.add(new SortRun().setFactory(new BigListFactory(5000)).setSize(size));
		runner.setDurations(500);
		runner.run();
		runner.printResults();
	}

	static void testFastTable2() {
		Runner runner = new Runner("Benchmark");
		//CollectionFactory factory = new FastTableFactory();
		CollectionFactory factory = new BigListFactory(10000);
		runner.add(new AddLastRunList().setFactory(factory).setSize(100000).setNumOps(1000).setName("AddLast"));
		runner.add(new AddFirstRunList().setFactory(factory).setSize(100000).setNumOps(1000).setName("AddFirst"));
		runner.add(new AddRandomRun().setFactory(factory).setSize(100000).setNumOps(1000).setName("BigList  10'000"));
		//runner.add(new AddNearRun().setFactory(factory).setSize(100000).setNumOps(1000).setNear(0).setName(""));
		//runner.add(new AddNearRun().setFactory(factory).setSize(100000).setNumOps(1000).setNear(0.01).setName("FastTable"));
		//runner.add(new AddNearRun().setFactory(factory).setSize(100000).setNumOps(1000).setNear(0.1).setName(""));
		runner.add(new RemoveLastRunList().setFactory(factory).setSize(100000).setNumOps(1000).setName("Remove"));
		runner.add(new RemoveFirstRunList().setFactory(factory).setSize(100000).setNumOps(1000).setName("BigList 100'000"));
		runner.add(new RemoveRandomRun().setFactory(factory).setSize(100000).setNumOps(1000).setName("FastTable"));
		//runner.add(new RemoveNearRun().setFactory(factory).setSize(100000).setNumOps(1000).setNear(0).setName("FastTable"));
		//runner.add(new RemoveNearRun().setFactory(factory).setSize(100000).setNumOps(1000).setNear(0.01).setName("FastTable"));
		//runner.add(new RemoveNearRun().setFactory(factory).setSize(100000).setNumOps(1000).setNear(0.1).setName("FastTable"));
		runner.setDurations(500);
		runner.run();
		runner.printResults();
	}

	static void testFastTable() {
		FastTable<Integer> table = new FastTable<Integer>();
		int i;
		for (i = 0; i < 10000; i++) {
			int n = i / 2;
			table.add(n, i);
		}
		System.out.println("---");
		table.add(5000, 999);
		//		for (i=0; i<600; i++) {
		//			int n = i/2;
		//			table.add(n, i);
		//		}
	}

	static void testFastTable3() {
		// DOES NOT SHRINK
		//ArrayList<Integer> table = new ArrayList<Integer>(10000);
		//BigList<Integer> table = new BigList<Integer>(10000);
		FastTable<Integer> table = new FastTable<Integer>();
		int num = 1000 * 1000;
		Random r = new Random(1);
		Timer t = new Timer();
		for (int i = 0; i < num; i++) {
			int pos = r.nextInt(table.size() + 1);
			table.add(pos, i);
		}
		t.printElapsed();
		t.start();
		for (int i = 0; i < num; i++) {
			int pos = table.size() / 2;
			//pos = 0;
			pos = r.nextInt(table.size());
			table.remove(pos);
		}
		t.printElapsed();
		System.out.println(table.size());
		System.out.println(ReflectTools.getObjectSize(table));
	}

	// Primitive

	static void testGetIntRandomGap() {
		int size = 1000;
		int numOps = 1000;
		Runner runner = new Runner("Get random Gap");
		runner.useAvg = false;
		runner.add(new GetIntWrapperRandomRun().setSize(size).setNumOps(numOps).setFactory(new GapListFactory()));
		runner.add(new GetIntRandomRun().setSize(size).setNumOps(numOps).setFactory(new IntGapListFactory()));
		runner.run();
		runner.printResults();
	}

	static void testGetIntRandomBig() {
		int size = 1000;
		int numOps = 1000;
		Runner runner = new Runner("Get random Big");
		runner.useAvg = false;
		runner.add(new GetIntWrapperRandomRun().setSize(size).setNumOps(numOps).setFactory(new BigListFactory()));
		runner.add(new GetIntRandomRun().setSize(size).setNumOps(numOps).setFactory(new IntBigListFactory()));
		runner.run();
		runner.printResults();
	}

	static void testAddIntRandomGap() {
		int size = 10 * 1000;
		int numOps = 1000;
		Runner runner = new Runner("Add random Gap");
		runner.useAvg = false;
		runner.add(new AddIntWrapperRandomRun().setSize(size).setNumOps(numOps).setFactory(new GapListFactory()));
		runner.add(new AddIntRandomRun().setSize(size).setNumOps(numOps).setFactory(new IntGapListFactory()));
		runner.run();
		runner.printResults();
	}

	static void testAddIntRandomBig() {
		int size = 10 * 1000;
		int numOps = 1000;
		Runner runner = new Runner("Add random Big");
		runner.useAvg = false;
		runner.add(new AddIntWrapperRandomRun().setSize(size).setNumOps(numOps).setFactory(new BigListFactory()));
		runner.add(new AddIntRandomRun().setSize(size).setNumOps(numOps).setFactory(new IntBigListFactory()));
		runner.run();
		runner.printResults();
	}

	static void testRemoveIntRandomGap() {
		int size = 1000;
		int numOps = 1000;
		Runner runner = new Runner("Remove random Gap");
		runner.useAvg = false;
		runner.add(new RemoveIntWrapperRandomRun().setSize(size).setNumOps(numOps).setFactory(new GapListFactory()));
		runner.add(new RemoveIntRandomRun().setSize(size).setNumOps(numOps).setFactory(new IntGapListFactory()));
		runner.run();
		runner.printResults();
	}

	static void testRemoveIntRandomBig() {
		int size = 10 * 1000;
		int numOps = 1000;
		Runner runner = new Runner("Remove random Big");
		runner.useAvg = false;
		runner.add(new RemoveIntWrapperRandomRun().setSize(size).setNumOps(numOps).setFactory(new BigListFactory()));
		runner.add(new RemoveIntRandomRun().setSize(size).setNumOps(numOps).setFactory(new IntBigListFactory()));
		runner.run();
		runner.printResults();
	}

	//

	static void testGetRandom() {
		int numOps = 10000;
		Runner runner = new Runner("Get random");
		for (CollectionFactory factory : factories) {
			runner.add(new GetRandomRun().setSize(size).setNumOps(numOps).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testGetIter() {
		int numOps = 10000;
		Runner runner = new Runner("Get iter");
		for (CollectionFactory factory : factories) {
			runner.add(new GetIterRun().setSize(size).setNumOps(numOps).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	//	static void testGetNear() {
	//		int numOps = 10000;
	//		int localOps = 1;
	//		int step = 0;
	//		Runner runner = new Runner("Get random near");
	//		for (CollectionFactory factory : factories) {
	//			runner.add(new GetNearRun().setSize(size).setNumOps(numOps).setLocalOps(localOps).setStep(step).setFactory(factory));
	//		}
	//		runner.run();
	//		runner.printResults();
	//	}

	static void testAddNear() {
		int numOps = 1000;
		int localOps = 10;
		int step = 0;
		Runner runner = new Runner("Add near ");
		for (CollectionFactory factory : factories) {
			runner.add(new AddNearRun().setSize(size).setNumOps(numOps).setStep(step).setLocalOps(localOps).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testRemoveNear() {
		int numOps = 1000;
		int localOps = 10;
		int step = 0;
		Runner runner = new Runner("Remove near ");
		for (CollectionFactory factory : factories) {
			runner.add(new RemoveNearRun().setSize(size).setNumOps(numOps).setStep(step).setLocalOps(localOps).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	//

	static void testAddMult() {
		int numOps = size / 10;
		Runner runner = new Runner("Add mult");
		for (CollectionFactory factory : factories) {
			runner.add(new AddMultRun().setSize(size).setNumOps(numOps).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testRemoveMult() {
		int numOps = size / 10;
		Runner runner = new Runner("Remove mult");
		for (CollectionFactory factory : factories) {
			runner.add(new RemoveMultRun().setSize(size).setNumOps(numOps).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testClone() {
		Runner runner = new Runner("Copy " + size);
		for (CollectionFactory factory : factories) {
			runner.add(new CloneRun().setSize(size).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testCloneModify() {
		int localOps = size / 10;
		Runner runner = new Runner("Copy and modify " + size);
		for (CollectionFactory factory : factories) {
			runner.add(new CloneRun().setSize(size).setLocalOps(localOps).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	//

	static void testAddAllInit() {
		Runner runner = new Runner("Add all init " + size);
		for (CollectionFactory factory : factories) {
			runner.add(new AddAllInitRun().setSize(size).setNumOps(size).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testAddInit() {
		Runner runner = new Runner("Add init " + size);
		for (CollectionFactory factory : factories) {
			runner.add(new AddInitRun().setSize(size).setNumOps(size).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testAddFirst() {
		Runner runner = new Runner("Add first");
		for (CollectionFactory factory : factories) {
			runner.add(new AddFirstRunList().setSize(size).setNumOps(1000).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testAddMiddle() {
		Runner runner = new Runner("Add middle");
		for (CollectionFactory factory : factories) {
			runner.add(new AddMiddleRun().setSize(size).setNumOps(1000).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testAddLast() {
		Runner runner = new Runner("Add last");
		for (CollectionFactory factory : factories) {
			runner.add(new AddLastRunList().setSize(size).setNumOps(1000).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testAddRandom() {
		Runner runner = new Runner("Add random");
		for (CollectionFactory factory : factories) {
			runner.add(new AddRandomRun().setSize(size).setNumOps(1000).setFactory(factory));
			//runner.add(new AddRandomRun().setSize(size).setNumOps(1000).setLocalOps(5).setFactory(factory));
		}
		runner.setRunIterations(new RunIterations(2000, 5000));
		runner.setInitIterations(new RunIterations(2000, 5000));
		runner.run();
		runner.printResults();
	}

	static void testAddAllRandom() {
		int numOps = 10;
		int localOps = 5000;
		Runner runner = new Runner("AddAll random");
		for (CollectionFactory factory : factories) {
			runner.add(new AddRandomRun().setAll(false).setSize(size).setNumOps(numOps).setLocalOps(localOps).setFactory(factory));
			runner.add(new AddRandomRun().setAll(true).setSize(size).setNumOps(numOps).setLocalOps(localOps).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testAddAllLast() {
		int numOps = 10;
		int localOps = 5000;
		Runner runner = new Runner("AddAll last");
		for (CollectionFactory factory : factories) {
			//runner.add(new AddLastRunList().setAll(false).setSize(size).setNumOps(numOps).setLocalOps(localOps).setFactory(factory));
			runner.add(new AddLastRunList().setAll(true).setSize(size).setNumOps(numOps).setLocalOps(localOps).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testRemoveAllRandom() {
		int numOps = 10;
		int localOps = 5000;
		Runner runner = new Runner("RemoveAll random");
		for (CollectionFactory factory : factories) {
			runner.add(new RemoveRandomRun().setAll(false).setSize(size).setNumOps(numOps).setLocalOps(localOps).setFactory(factory));
			runner.add(new RemoveRandomRun().setAll(true).setSize(size).setNumOps(numOps).setLocalOps(localOps).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testRemoveRandom() {
		Runner runner = new Runner("Remove random");
		for (CollectionFactory factory : factories) {
			runner.add(new RemoveRandomRun().setSize(size).setNumOps(1000).setStep(0).setLocalOps(1).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testRemoveLast() {
		Runner runner = new Runner("Remove last");
		for (CollectionFactory factory : factories) {
			runner.add(new RemoveLastRunList().setSize(size).setNumOps(1000).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testRemoveMiddle() {
		Runner runner = new Runner("Remove middle");
		for (CollectionFactory factory : factories) {
			runner.add(new RemoveMiddleRun().setSize(size).setNumOps(1000).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testRemoveFirst() {
		Runner runner = new Runner("Remove first");
		for (CollectionFactory factory : factories) {
			runner.add(new RemoveFirstRunList().setSize(size).setNumOps(1 * 1000).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	static void testBenchmark() {
		int size = 100 * 1000;
		Runner runner = new Runner("Benchmark");
		for (CollectionFactory factory : factories) {
			if (factory instanceof LinkedListFactory) {
				continue;
			}
			runner.add(new BenchmarkRun().setSize(size).setFactory(factory));
		}
		runner.run();
		runner.printResults();
	}

	public static class BenchmarkRun extends FactoryRun {

		List<Integer> init;

		@Override
		public void beforeAll() {
			init = (List<Integer>) factory.createSize(0);
			for (int i = 0; i < size; i++) {
				init.add(i);
			}
		}

		@Override
		public Object run() {
			Random random = new Random(5);

			List<Integer> list = (List<Integer>) factory.copy(init);

			int near = 100;
			int idx = random.nextInt(list.size());
			for (int i = 0; i < numOps; i++) {
				int op = random.nextInt(4);
				idx = idx + random.nextInt(2 * near + 1) - near;
				if (idx < 0) {
					idx = 0;
				} else if (idx >= list.size()) {
					idx = list.size() - 1;
				}
				int val = list.get(idx);
				switch (op) {
				case 0: // no-op
					break;
				case 1: // set
					list.set(idx, val);
					break;
				case 2: // add
					list.add(idx, val);
					break;
				case 3: // remove
					list.remove(idx);
					break;
				default:
					throw new AssertionError();
				}
			}
			return list;
		}
	}

	static void testBigList() {
		testBigList(1 * 1000);
		testBigList(5 * 1000);
		testBigList(10 * 1000);
		testBigList(50 * 1000);
		testBigList(100 * 1000);
	}

	static void testBigList(int blockSize) {
		testBigListAdd(blockSize);
		testBigListGet(blockSize);
	}

	static void testBigListAdd(int blockSize) {
		int size = 500 * 1000;
		int numGets = 100000;
		Runner runner = new Runner("Add random (blockSize = " + blockSize + ")");
		runner.setInitIterations(new RunIterations(1 * 1000));
		runner.setRunIterations(new RunIterations(1 * 1000, 2 * 1000));
		numGets = 1000;
		runner.add(new AddRandomRun().setFactory(new GapListFactory()).setSize(size).setNumOps(numGets).setName("GapList"));
		runner.add(new AddRandomRun().setFactory(new BigListFactory()).setSize(size).setNumOps(numGets).setName("BigList"));
		runner.run();
		runner.printResults();
	}

	static void testBigListGet(int blockSize) {
		int size = 500 * 1000;
		int numGets = 100000;
		Runner runner = new Runner("Get random (blockSize = " + blockSize + ")");
		runner.setInitIterations(new RunIterations(1 * 1000));
		runner.setRunIterations(new RunIterations(1 * 1000, 2 * 1000));
		numGets = 1000 * 1000;
		runner.add(new GetRandomRun().setFactory(new GapListFactory()).setSize(size).setNumOps(numGets).setName("GapList"));
		runner.add(new GetRandomRun().setFactory(new BigListFactory()).setSize(size).setNumOps(numGets).setName("BigList"));
		runner.run();
		runner.printResults();
	}

	public static void showVmArgs() {
		List<String> lst = SystemTools.getJvmArgs();
		for (int i = 0; i < lst.size(); i++) {
			System.out.println(lst.get(i));
		}
	}

	static void runMemory() {
		rnd = new Random(0);

		// addLast 8.4 / 8.4
		List<Integer> list = new GapList<Integer>();
		//List<Integer> list = new BigList<Integer>();

		//runAddLast(list)

		// 1 Mio: 214s / 35s
		// 2 Mio:      / 70s
		//		runAddRandom(list);

		// 1 Mio:  6.7s / 12.8s
		// 2 Mio: 17.0s / 32.6s
		for (int i = 0; i < num; i++) {
			list.add(i);
		}
		runGetRandom(list);
	}

	static Random rnd;
	static int num = 2 * 1000 * 1000;

	static void runAddLast(List<Integer> list) {
		Timer t = new Timer();
		for (int i = 0; i < num; i++) {
			list.add(i);
		}
		t.printElapsed();
	}

	static void runAddRandom(List<Integer> list) {
		Timer t = new Timer();
		for (int i = 0; i < num; i++) {
			if (i % 10000 == 0) {
				System.out.println(i);
			}
			int idx = rnd.nextInt(i + 1);
			list.add(idx, i);
		}
		t.printElapsed();
	}

	static void runGetRandom(List<Integer> list) {
		Timer t = new Timer();
		int size = list.size();
		for (int i = 0; i < 100 * num; i++) {
			int idx = rnd.nextInt(size);
			list.get(idx);
		}
		t.printElapsed();
	}

	static void testTypes() {
		testBigList(60 * 1000 * 1000);
		//		testFastTable(60*1000*1000);
	}

	//	static void testFastTable(int size) {
	//		FastTable<?> list = allocFastTable(size);
	//		FastTable<?> copy = new FastTable(list);
	//		//copy.sort(null);
	//	}

	/**
	 * Tests System.arraycopy().
	 * To copy same amount of data, the method needs 5s on a 32 bit system, but only 3s a 64 bit system.
	 */
	static void testArrayCopy() {
		Runner runner = new Runner("");
		runner.add(new Run() {
			final int size = 1000 * 1000;
			int[] array = new int[size];

			@Override
			public Object run() {
				for (int i = 0; i < 1000; i++) {
					System.arraycopy(array, 0, array, size / 2, size / 2);
				}
				return array;
			}
		});
		runner.run();
		runner.printResults();
	}

	static void runContest() {
		//-XX:+HeapDumpOnOutOfMemoryError
		showVmArgs();
		internalSort();
		//externalSort();
	}

	static void externalSort() {
		Timer t = new Timer();
		// 33.4 s
		//externalSort("rec_5_000_000.txt");
		// 26.4 s
		externalSort2("rec_5_000_000.txt");
		t.printElapsed("externalSort");
	}

	static void internalSort() {
		Timer t = new Timer();
		internalSort2();
		t.printElapsed("internalSort");
	}

	static void internalSort2() {

		//createTestFile("rec_100_000.txt", 100*1000);
		//createTestFile("rec_1_000_000.txt", 1000*1000);	// 5 s, 7 MB
		//createTestFile("rec_5_000_000.txt", 5*1000*1000); // 25 s, 37 MB
		//createTestFile("rec_8_000_000.txt", 8*1000*1000); // 25 s, 37 MB
		//createTestFile("rec_10_000_000.txt", 10*1000*1000); // 50 s, 80 MB

		// ArrayList:
		//  5'000'000:  3.10 s
		//  8'000'000: 10.00 s
		// 10'000'000:  OOME
		// LinkedList:
		//  5'000'000:  4.60 s
		//  8'000'000:  OOME
		// BigList:
		//  5'000'000:  3.30 s
		//  8'000'000:  6.90 s
		// 10'000'000:
		Timer t = new Timer();
		//ArrayList<String> list = new ArrayList<String>();
		//LinkedList<String> list = new LinkedList<String>();
		BigList<String> list = new BigList<String>();
		StringStreamer reader = new StringStreamer().addSourceFile("rec_5_000_000.txt");
		while (true) {
			String line = reader.readLine(false);
			if (line == null) {
				break;
			}
			if (line.length() > 0) {
				list.add(line);
			}
		}
		reader.close();
		t.printElapsed("file read");
		System.out.println(list.size());

		// ArrayList:
		// 5'000'000: 15.3 s
		// 8'000'000: OOME
		// LinkedList:
		// 5'000'000: 15.3 s
		// BigList:
		//  5'000'000:   17 s (100 s)
		//  8'000'000:  171 s
		// 10'000'000:
		t.start();
		//Collections.sort(list);
		list.sort(null);
		t.printElapsed("sorted");

		//		// ArrayList:
		//		// 5'000'000: 0.05 s
		//		// BigList:
		//		//  8'000'000:   0.3 s s (100 s)
		//		t.start();
		//		//List copy = new BigList<String>(list);
		//		List copy = new ArrayList<String>(list);
		//		copy.remove(0);
		//		t.printElapsed("copied and removed");
	}

	static void externalSort(String file) {
		Timer t = new Timer();
		int chunkSize = 1000 * 1000;
		int totalSize = 0;
		int chunk = 0;
		ArrayList<String> list = new ArrayList<String>();
		//LinkedList<String> list = new LinkedList<String>();
		//BigList<String> list = new BigList<String>();
		StringStreamer reader = new StringStreamer().addSourceFile(file);
		while (true) {
			String line = reader.readLine(false);
			if (line == null) {
				break;
			}
			if (line.length() > 0) {
				list.add(line);
				totalSize++;
				if (list.size() == chunkSize) {
					Collections.sort(list);
					writeTempFile(file + "." + chunk, list);
					list.clear();
					chunk++;
				}
			}
		}
		if (list.size() > 0) {
			Collections.sort(list);
			writeTempFile(file + "." + chunk, list);
			list.clear();
			chunk++;
		}
		reader.close();
		t.printElapsed("prepared " + list.size());
		t.start();

		StringStreamer[] readers = new StringStreamer[chunk];
		String[] values = new String[chunk];
		for (int i = 0; i < chunk; i++) {
			String path = file + "." + i;
			readers[i] = new StringStreamer().addSourceFile(path);
			values[i] = readers[i].readLine(false);
		}

		list = new ArrayList<String>(totalSize);
		while (true) {
			//System.out.println(PrintTools.print(values));
			int minIdx = -1;
			String minStr = null;
			for (int i = 0; i < chunk; i++) {
				if (values[i] != null) {
					if (minStr == null) {
						minStr = values[i];
						minIdx = i;
					} else if (values[i].compareTo(minStr) < 0) {
						minStr = values[i];
						minIdx = i;
					}
				}
			}
			if (minIdx == -1) {
				break;
			}
			list.add(minStr);
			values[minIdx] = readers[minIdx].readLine(false);
		}
		t.printElapsed("done " + list.size());
	}

	static void writeTempFile(String path, List<String> list) {
		LOG.info("writeTempFile {}", path);
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(path));
			for (int i = 0; i < list.size(); i++) {
				out.write(list.get(i) + "\n");
			}
		} catch (IOException e) {
			throw new FileException(path, Access.WRITE, e);
		} finally {
			StreamTools.close(out);
		}
	}

	static void externalSort2(String file) {
		Timer t = new Timer();
		int chunkSize = 1000 * 1000;
		int totalSize = 0;
		int chunk = 0;
		ArrayList<String> list = new ArrayList<String>();
		//LinkedList<String> list = new LinkedList<String>();
		//BigList<String> list = new BigList<String>();
		StringStreamer reader = new StringStreamer().addSourceFile(file);
		while (true) {
			String line = reader.readLine(false);
			if (line == null) {
				break;
			}
			if (line.length() > 0) {
				list.add(line);
				totalSize++;
				if (list.size() == chunkSize) {
					Collections.sort(list);
					writeTempFile2(file + "." + chunk, list);
					list.clear();
					chunk++;
				}
			}
		}
		if (list.size() > 0) {
			Collections.sort(list);
			writeTempFile2(file + "." + chunk, list);
			list.clear();
			chunk++;
		}
		reader.close();
		t.printElapsed("prepared " + list.size());
		t.start();

		try {
			ObjectInputStream[] readers = new ObjectInputStream[chunk];
			String[] values = new String[chunk];
			for (int i = 0; i < chunk; i++) {
				String path = file + "." + i;
				FileInputStream fis = new FileInputStream(path);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ObjectInputStream is = new ObjectInputStream(bis);
				readers[i] = is;
				values[i] = (String) is.readObject();
			}

			list = new ArrayList<String>(totalSize);
			while (true) {
				//System.out.println(PrintTools.print(values));
				int minIdx = -1;
				String minStr = null;
				for (int i = 0; i < chunk; i++) {
					if (values[i] != null) {
						if (minStr == null) {
							minStr = values[i];
							minIdx = i;
						} else if (values[i].compareTo(minStr) < 0) {
							minStr = values[i];
							minIdx = i;
						}
					}
				}
				if (minIdx == -1) {
					break;
				}
				list.add(minStr);
				values[minIdx] = readers[minIdx].readLine();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		t.printElapsed("done " + list.size());
	}

	static void writeTempFile2(String path, List<String> list) {
		LOG.info("writeTempFile {}", path);
		BufferedWriter out = null;
		try {
			FileOutputStream fos = new FileOutputStream(path);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ObjectOutputStream os = new ObjectOutputStream(bos);
			for (int i = 0; i < list.size(); i++) {
				os.writeObject(list.get(i));
			}
			os.close();
		} catch (IOException e) {
			throw new FileException(path, Access.WRITE, e);
		} finally {
			StreamTools.close(out);
		}
	}

	static void createTestFile(String path, int numLines) {
		Random r = new Random();
		r.setSeed(0);
		Timer t = new Timer();
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(path));
			for (int i = 0; i < numLines; i++) {
				int num = r.nextInt(numLines);
				out.write(String.format("%d\n", num));
			}
		} catch (IOException e) {
			throw new FileException(path, Access.WRITE, e);
		} finally {
			StreamTools.close(out);
		}
		t.printElapsed(String.format("Creating file %s with %d lines", path, numLines));
	}

}

class TestList<T> {
	private List<T>[] lists;

	public TestList(List<T>... lists) {
		this.lists = lists;
	}

	public int size() {
		return lists[0].size();
	}

	public void add(int index, T elem) {
		for (List<T> list : lists) {
			list.add(index, elem);
		}
		for (int i = 1; i < lists.length; i++) {
			assert (lists[0].equals(lists[i]));
		}
	}

	public T remove(int index) {
		T result = lists[0].remove(index);
		for (int i = 1; i < lists.length; i++) {
			T result2 = lists[i].remove(index);
			assert (result == result2);
		}
		for (int i = 1; i < lists.length; i++) {
			assert (lists[0].equals(lists[i]));
		}
		return result;
	}
}