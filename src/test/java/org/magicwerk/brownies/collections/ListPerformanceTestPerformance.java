package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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
public class ListPerformanceTestPerformance {

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

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new ListPerformanceTestPerformance().run();
	}

	void run() {
		//test(GetFirstListTest.class);
		test(GetRandomListTest.class);
	}

	void test(Class<?> testClass) {
		Options opts = new Options().includeClass(testClass);
		opts.setWarmupIterations(1);
		opts.setMeasurementIterations(2);
		JmhRunner runner = new JmhRunner();
		runner.runJmh(opts);
	}

	@State(Scope.Benchmark)
	public static abstract class ListState {

		int size = 100;
		int numOps = 50;

		Random r;
		List<Integer>[] lists;
		int[] indexes;
		int pos;

		@Param({ "0", "1", "2", "3" })
		int type;

		@Setup(Level.Iteration)
		public void init() {
			r = new Random(0);

			this.lists = new List[4];
			lists[0] = initList(new ArrayList<>());
			lists[1] = initList(new LinkedList<>());
			lists[2] = initList(GapList.create());
			lists[3] = initList(BigList.create());

			indexes = new int[numOps];
			initIndexes();
			pos = 0;
		}

		List<Integer> initList(List<Integer> list) {
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
			return list;
		}

		abstract void initIndexes();

		List<Integer> list() {
			return lists[type];
		}

		int pos() {
			return indexes[pos++ % numOps];
		}
	}

	public static class GetListTest {

		public Object testGet(ListState state) {
			List<Integer> list = state.list();
			int pos = state.pos();
			list.get(pos);
			return state;
		}
	}

	public static class GetFirstListTest extends GetListTest {

		@State(Scope.Benchmark)
		public static class GetFirstListState extends ListState {
			@Override
			void initIndexes() {
				for (int i = 0; i < numOps; i++) {
					indexes[i] = 0;
				}
			}
		}

		@Benchmark
		public Object testGetFirst(GetFirstListState state) {
			return testGet(state);
		}
	}

	public static class GetRandomListTest extends GetListTest {

		@State(Scope.Benchmark)
		public static class GetRandomListState extends ListState {
			@Override
			void initIndexes() {
				for (int i = 0; i < numOps; i++) {
					indexes[i] = r.nextInt(size);
				}
			}
		}

		@Benchmark
		public Object testGetRandom(GetRandomListState state) {
			return testGet(state);
		}
	}

	public static class GetFirstListTest2 {

		public static class GetFirstListState extends ListState {
			@Override
			void initIndexes() {
				for (int i = 0; i < numOps; i++) {
					indexes[i] = 0;
				}
			}
		}

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
