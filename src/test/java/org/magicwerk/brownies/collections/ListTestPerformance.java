package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.magicwerk.brownies.core.FuncTools;
import org.magicwerk.brownies.core.FuncTools.MapMode;
import org.magicwerk.brownies.core.SystemTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.test.JmhRunner;
import org.magicwerk.brownies.test.JmhRunner.Options;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

/**
 * Show usage of {@link JmhRunner}.
 * 
 * @author Thomas Mauch
 */
public class ListTestPerformance {

	// Execution model of JHM

	// run
	// - test 1
	// - - fork 1 (new JVM with new PID)
	// - - - call @Setup(Level.Trial)
	// - - - - call @Setup(Level.Iteration)
	// - - - - - warmup iteration 0
	// - - - - -  call @Setup(Level.Invocation)
	// - - - - - - call @Benchmark method
	// - - - - -  call @TearDown(Level.Invocation)
	// - - - - call @TearDown(Level.Iteration)
	// - - - - call @Setup(Level.Iteration)
	// - - - - - warmup iteration 1
	// - - - - call @TearDown(Level.Iteration)
	// - - - - measurement iterations
	// - - - call @TearDown(Level.Trial)
	// - - fork 2 (new JVM wit new PID)
	// - - - warmup iterations
	// - - - measurement iterations
	// - test 2
	// ...

	// fork: controls how many test processes are spawned (real processes with own PID, not threads)
	// 0: All benchmarks are executed as part of the main process, should not be used
	// N: For each test, N forks are created which run the test iterations

	// http://hg.openjdk.java.net/code-tools/jmh/file/3c8d4f23d112/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_11_Loops.java
	// http://hg.openjdk.java.net/code-tools/jmh/file/3c8d4f23d112/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_26_BatchSize.java
	// http://hg.openjdk.java.net/code-tools/jmh/file/3c8d4f23d112/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_27_Params.java
	//
	// https://shipilev.net/blog/2014/nanotrusting-nanotime/

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new ListTestPerformance().run();
	}

	void run() {
		testGet();
	}

	void testGet() {
		//test(GetFirstListTest.class);
		//test(GetLastListTest.class);
		//test(GetMidListTest.class);
		//test(GetIterListTest.class);
		//test(ListTest.class);
		//test(GetRandomListTest2.class);
		testList();
	}

	void testList() {
		Options opts = new Options();
		opts.includeMethod(ListTest.class, "testGet");
		//opts.includeMethod(ListTest.class, "testAdd");
		//opts.includeMethod(ListTest.class, "testRemove");

		int numIter = ListState.numIter;
		opts.setJvmArgs(GapList.create("-Xmx6g", "-Xms6g", "-XX:+UseG1GC"));
		opts.setWarmupIterations(numIter);
		opts.setMeasurementIterations(numIter);
		opts.setRunTimeMillis(ListState.runTimeMillis);
		opts.setResultFile("output/ListTestPerformance.json");
		JmhRunner runner = new JmhRunner();
		runner.runJmh(opts);
	}

	@State(Scope.Benchmark)
	public abstract static class ListState {

		static final int numIter = 5;

		static final int runTimeMillis = 50;

		/** 
		 * Max number of invocations which may occur during an iteration.
		 * This number of must be set in relation to the run time of the benchmark.
		 */
		static final int maxAccess = 5_000_000;

		final int step = 1;

		@Param({ "ArrayList", "LinkedList", "GapList", "BigList" })
		String type;

		@Param({ "Random" })
		//@Param({ "First", "Last", "Mid", "Iter", "Random" })
		String op;

		@Param({ "100" })
		//@Param({ "100", "10000", "1000000" })
		int size;

		Random r;
		int numBatches;
		int batchSize;
		int realNumBatches;
		int realBatchSize;
		int realNumIter;
		List<Integer> sourceList;
		List<Integer>[] lists;
		int numLists = 1;
		int[] indexes;
		int basePos;
		int iter;
		int pos;

		@Setup(Level.Trial)
		public void setupTrial() {
			// This setup is called before a trial (a sequence of all warmup / measurement iterations)
			LOG.info("### setupTrial.start pid={}, type={}, op={}, size={}", SystemTools.getProcessId(), type, op, size);

			batchSize = size / 10;
			numBatches = maxAccess / batchSize;
			realNumIter = 2 * numIter; // warmup and measurement iterations

			if (needsBatches()) {
				realNumBatches = realNumIter * numBatches;
				realBatchSize = batchSize;
			} else {
				realNumBatches = 1;
				realBatchSize = Integer.MAX_VALUE;
			}

			sourceList = initList(type, size);
			lists = new List[realNumBatches];
			for (int i = 0; i < realNumBatches; i++) {
				lists[i] = initList(type);
				lists[i].addAll(sourceList);
			}

			r = new Random(0);
			initIndexes(op, batchSize);
			iter = 0;

			LOG.info("### setupTrial.end");
		}

		@Setup(Level.Iteration)
		public void setupIteration() {
			// This setup is called before each warmup / measurement iteration
			LOG.info("setupIteration.start");

			basePos = iter * batchSize;
			iter++;
			pos = 0;

			LOG.info("setupIteration.end");
		}

		List<Integer> initList(String type, int size) {
			List<Integer> list = initList(type);
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
			return list;
		}

		List<Integer> initList(String type) {
			List<Integer> list = FuncTools.map(type, MapMode.ERROR,
					"ArrayList", new ArrayList<>(), "LinkedList", new LinkedList<>(), "GapList", GapList.create(), "BigList", BigList.create());
			return list;
		}

		abstract boolean needsBatches();

		abstract void initIndexes(String op, int batchSize);

		int pos() {
			return indexes[pos++ % batchSize];
		}

		List<Integer> list() {
			return lists[pos / realBatchSize];
		}
	}

	@State(Scope.Benchmark)
	public static class GetListState extends ListState {
		@Override
		boolean needsBatches() {
			return true;
		}

		@Override
		void initIndexes(String op, int batchSize) {
			indexes = new int[batchSize];
			if ("First".equals(op)) {
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = 0;
				}
			} else if ("Last".equals(op)) {
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = size - 1;
				}
			} else if ("Mid".equals(op)) {
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = size / 2;
				}
			} else if ("Iter".equals(op)) {
				int start = (size / 2) - (batchSize * step / 2);
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = start + i * step;
				}
			} else if ("Random".equals(op)) {
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = r.nextInt(size);
				}
			} else {
				throw new AssertionError();
			}
		}
	}

	@State(Scope.Benchmark)
	public static class AddListState extends ListState {
		@Override
		boolean needsBatches() {
			return true;
		}

		@Override
		void initIndexes(String op, int batchSize) {
			indexes = new int[batchSize];
			if ("First".equals(op)) {
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = 0;
				}
			} else if ("Last".equals(op)) {
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = size - 1 + i;
				}
			} else if ("Mid".equals(op)) {
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = size / 2;
				}
			} else if ("Iter".equals(op)) {
				int start = (size / 2) - (batchSize * step / 2);
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = start + i * step;
				}
			} else if ("Random".equals(op)) {
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = r.nextInt(size + i);
				}
			} else {
				throw new AssertionError();
			}
		}
	}

	@State(Scope.Benchmark)
	public static class RemoveListState extends ListState {
		@Override
		boolean needsBatches() {
			return true;
		}

		@Override
		void initIndexes(String op, int batchSize) {
			indexes = new int[batchSize];
			if ("First".equals(op)) {
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = 0;
				}
			} else if ("Last".equals(op)) {
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = size - 1 - i;
				}
			} else if ("Mid".equals(op)) {
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = size / 2;
				}
			} else if ("Iter".equals(op)) {
				int start = (size / 2) - (batchSize * step / 2);
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = start + i * step;
				}
			} else if ("Random".equals(op)) {
				for (int i = 0; i < batchSize; i++) {
					indexes[i] = r.nextInt(size - i);
				}
			} else {
				throw new AssertionError();
			}
		}
	}

	// Get

	public static class ListTest {

		@Benchmark
		public Object testGet(GetListState state) {
			List<Integer> list = state.list();
			int pos = state.pos();
			list.get(pos);
			return state;
		}

		@Benchmark
		public Object testAdd(AddListState state) {
			List<Integer> list = state.list();
			int pos = state.pos();
			list.add(pos, pos);
			return state;
		}

		@Benchmark
		public Object testRemove(RemoveListState state) {
			List<Integer> list = state.list();
			int pos = state.pos();
			list.remove(pos);
			return state;
		}
	}

	// Add

	//
	//	public static class AddFirstListTest extends AddListTest {
	//
	//		@State(Scope.Benchmark)
	//		public static class AddFirstListState extends ListState {
	//			@Override
	//			void initIndexes() {
	//			}
	//		}
	//
	//		@Benchmark
	//		public Object testAddFirst(AddFirstListState state) {
	//			return testAdd(state);
	//		}
	//	}

	//	public static class GetFirstListTest extends GetListTest {
	//
	//		@State(Scope.Benchmark)
	//		public static class GetFirstListState extends ListState {
	//			@Override
	//			void initIndexes() {
	//				for (int i = 0; i < numOps; i++) {
	//					indexes[i] = 0;
	//				}
	//			}
	//		}
	//
	//		@Benchmark
	//		public Object testGetFirst(GetFirstListState state) {
	//			return testGet(state);
	//		}
	//	}
	//
	//	public static class GetLastListTest extends GetListTest {
	//
	//		@State(Scope.Benchmark)
	//		public static class GetLastListState extends ListState {
	//			@Override
	//			void initIndexes() {
	//				for (int i = 0; i < numOps; i++) {
	//					indexes[i] = size - 1;
	//				}
	//			}
	//		}
	//
	//		@Benchmark
	//		public Object testGetLast(GetLastListState state) {
	//			return testGet(state);
	//		}
	//	}
	//
	//	public static class GetMidListTest extends GetListTest {
	//
	//		@State(Scope.Benchmark)
	//		public static class GetMidListState extends ListState {
	//			@Override
	//			void initIndexes() {
	//				for (int i = 0; i < numOps; i++) {
	//					indexes[i] = size / 2;
	//				}
	//			}
	//		}
	//
	//		@Benchmark
	//		public Object testGetLast(GetMidListState state) {
	//			return testGet(state);
	//		}
	//	}
	//
	//	public static class GetIterListTest extends GetListTest {
	//
	//		@State(Scope.Benchmark)
	//		public static class GetIterListState extends ListState {
	//			@Override
	//			void initIndexes() {
	//				int start = (size / 2) - (numOps * step / 2);
	//				for (int i = 0; i < numOps; i++) {
	//					indexes[i] = start + i * step;
	//				}
	//			}
	//		}
	//
	//		@Benchmark
	//		public Object testGetIter(GetIterListState state) {
	//			return testGet(state);
	//		}
	//	}
	//
	//	public static class GetRandomListTest extends GetListTest {
	//
	//		@State(Scope.Benchmark)
	//		public static class GetRandomListState extends ListState {
	//			@Override
	//			void initIndexes() {
	//				for (int i = 0; i < numOps; i++) {
	//					indexes[i] = r.nextInt(size);
	//				}
	//			}
	//		}
	//
	//		@Benchmark
	//		public Object testGetRandom(GetRandomListState state) {
	//			return testGet(state);
	//		}
	//	}

	//

	public static class GetRandomListTest2 {

		@State(Scope.Benchmark)
		public static abstract class ListState {

			int size = 100;
			int numOps = 50;
			int step = 1;

			Random r;
			List<Integer> list;
			int[] indexes;
			int pos;

			@Setup(Level.Iteration)
			public void init() {
				r = new Random(0);
				list = initList();
				indexes = new int[numOps];
				initIndexes();
				pos = 0;
			}

			abstract List<Integer> initList();

			List<Integer> initList(List<Integer> list) {
				for (int i = 0; i < size; i++) {
					list.add(i);
				}
				return list;
			}

			void initIndexes() {
				for (int i = 0; i < numOps; i++) {
					indexes[i] = r.nextInt(size);
				}
			}

			int pos() {
				return indexes[pos++ % numOps];
			}
		}

		@State(Scope.Benchmark)
		public static class GetRandomArrayListState extends ListState {
			@Override
			List<Integer> initList() {
				return initList(new ArrayList<>());
			}
		}

		@State(Scope.Benchmark)
		public static class GetRandomLinkedListState extends ListState {
			@Override
			List<Integer> initList() {
				return initList(new LinkedList<>());
			}
		}

		@State(Scope.Benchmark)
		public static class GetRandomGapListState extends ListState {
			@Override
			List<Integer> initList() {
				return initList(GapList.create());
			}
		}

		@State(Scope.Benchmark)
		public static class GetRandomBigListState extends ListState {
			@Override
			List<Integer> initList() {
				return initList(BigList.create());
			}
		}

		Object testGet(ListState state) {
			List<Integer> list = state.list;
			int pos = state.pos();
			list.get(pos);
			return state;
		}

		@Benchmark
		public Object testGetRandomArrayList(GetRandomArrayListState state) {
			return testGet(state);
			//			List<Integer> list = state.list;
			//			int pos = state.pos();
			//			list.get(pos);
			//			return state;
		}

		@Benchmark
		public Object testGetRandomLinkedList(GetRandomLinkedListState state) {
			return testGet(state);
			//			List<Integer> list = state.list;
			//			int pos = state.pos();
			//			list.get(pos);
			//			return state;
		}

		@Benchmark
		public Object testGetRandomGapList(GetRandomGapListState state) {
			return testGet(state);
			//			List<Integer> list = state.list;
			//			int pos = state.pos();
			//			list.get(pos);
			//			return state;
		}

		@Benchmark
		public Object testGetRandomBigList(GetRandomBigListState state) {
			return testGet(state);
			//			List<Integer> list = state.list;
			//			int pos = state.pos();
			//			list.get(pos);
			//			return state;
		}
	}

	public static class GetFirstListTest2 {

		//		public static class GetFirstListState extends ListState {
		//			@Override
		//			void initIndexes() {
		//				for (int i = 0; i < numOps; i++) {
		//					indexes[i] = 0;
		//				}
		//			}
		//		}

		//		@State(Scope.Benchmark)
		//		public static class GapListState extends GetFirstListState {
		//			@Setup(Level.Iteration)
		//			public void setup() {
		//				init(GapList.create());
		//			}
		//		}
		//
		//		@State(Scope.Benchmark)
		//		public static class ArrayListState extends GetFirstListState {
		//			@Setup(Level.Iteration)
		//			public void setup() {
		//				init(new ArrayList<>());
		//			}
		//		}
		//
		//		@Benchmark
		//		public Object testGapList(GapListState state) {
		//			int pos = state.pos();
		//			state.list.get(pos);
		//			return state;
		//		}
		//
		//		@Benchmark
		//		public Object testArrayList(ArrayListState state) {
		//			int pos = state.pos();
		//			state.list.get(pos);
		//			return state;
		//		}
	}

}
