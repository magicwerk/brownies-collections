package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.magicwerk.brownies.collections.helper.GapLists;
import org.magicwerk.brownies.core.Timer;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.test.JmhRunner;
import org.magicwerk.brownies.test.JmhRunner.Options;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;
import org.slf4j.Logger;

/**
 * JMH benchmark for iterating/streaming List.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class StreamsPerformance {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	static final int NUM_ELEMS = 1000;

	public static void main(String[] args) throws RunnerException {
		run();
	}

	static void run() {
		testPerformanceIterationJmh();
		//testFlatMap();
	}

	//	void runJhm() {
	//		int secs = 5;
	//		Options opt = new OptionsBuilder().//
	//				include("StreamsBenchmark.testList")
	//				//include("StreamsBenchmark.testError")
	//				//include("StreamsBenchmark.testCollection")
	//				//.mode(Mode.AverageTime)
	//				.forks(1).warmupIterations(2).warmupTime(TimeValue.seconds(secs)).//
	//				measurementIterations(2).measurementTime(TimeValue.seconds(secs)).//
	//				build();
	//		//new Runner(opt).run();
	//	}

	//

	static void testFlatMap() {
		for (int i = 0; i < 10; i++) {
			testFlatMap1(10, 100_000);
			testFlatMap2(10, 100_000);
		}
	}

	static void testFlatMap1(int size, int num) {
		IList<Integer> is = IntStream.range(1, size).boxed().collect(GapLists.toGapList());
		IList<String> sss = GapList.create();
		Timer t = new Timer();
		for (int i = 0; i < num; i++) {
			IList<String> ss = flatMap(is, e -> map(e));
			sss.addAll(ss);
		}
		LOG.info("{}", t.elapsedString());
		//LOG.info("{} -> {}", is, ss);
	}

	static void testFlatMap2(int size, int num) {
		IList<Integer> is = IntStream.range(1, size).boxed().collect(GapLists.toGapList());
		IList<String> sss = GapList.create();
		Timer t = new Timer();
		for (int i = 0; i < num; i++) {
			List<String> ss = is.stream().flatMap(e -> map(e).stream()).collect(Collectors.toList());
			sss.addAll(ss);
		}
		LOG.info("{}", t.elapsedString());
		//LOG.info("{} -> {}", is, ss);
	}

	static IList<String> map(int n) {
		IList<String> l = GapList.create();
		for (int i = 0; i < n; i++) {
			l.add("" + n + n);
		}
		return l;
	}

	static <T, R> IList<R> flatMap(List<T> list, Function<T, Collection<R>> flatMap) {
		IList<R> result = GapList.create();
		for (T elem : list) {
			result.addAll(flatMap.apply(elem));
		}
		return result;
	}

	static IList<String> listFiles(String dir) {
		return GapList.create(dir + "1", dir + "2");
	}

	//

	static void testPerformanceIterationJmh() {
		Options opts = new Options().includeClass(PerformanceStreamJmhTest.class);
		new JmhRunner().runJmh(opts);
	}

	public static class PerformanceStreamJmhTest {

		// State is shared across all invocations of same benchmark
		@State(Scope.Benchmark)
		public static class ListState {
			List<Integer> list = new ArrayList<>();

			public ListState() {
				for (int i = 0; i < NUM_ELEMS; i++) {
					list.add(i);
				}
			}
		}

		@State(Scope.Benchmark)
		public static class CollectionState {
			Collection<Integer> list = new HashSet<>();

			public CollectionState() {
				for (int i = 0; i < NUM_ELEMS; i++) {
					list.add(i);
				}
			}
		}

		// -- Collection

		// Stream: base line
		@Benchmark
		public int testCollectionStream(CollectionState state) {
			int count = (int) state.list.stream().filter(n -> n % 2 == 0).count();
			return count;
		}

		@Benchmark
		public int testCollectionStreamParallel(CollectionState state) {
			int count = (int) state.list.parallelStream().filter(n -> n % 2 == 0).count();
			return count;
		}

		// For-loop: faster than stream
		@Benchmark
		public int testCollectionIterate(CollectionState state) {
			int count = 0;
			for (int n : state.list) {
				if (n % 2 == 0) {
					count++;
				}
			}
			return count;
		}

		//

		@Benchmark
		public int testErrorCollectionStream(CollectionState state) {
			int count = (int) state.list.stream().filter(n -> {
				if (true) {
					throw new IllegalArgumentException();
				}
				return true;
			}).count();
			return count;

		}

		@Benchmark
		public int testErrorCollectionIterate(CollectionState state) {
			int count = 0;
			for (int n : state.list) {
				if (true) {
					throw new IllegalArgumentException();
				}
			}
			return count;
		}

		// -- List

		// For-loop: quite slow, as an iterator is involved
		@Benchmark
		public int testListIterate(ListState state) {
			int count = 0;
			for (int n : state.list) {
				if (n % 2 == 0) {
					count++;
				}
			}
			return count;
		}

		// Stream: Faster than the for-loop, uses array index
		@Benchmark
		public int testListStream(ListState state) {
			int count = (int) state.list.stream().filter(n -> n % 2 == 0).count();
			return count;
		}

		// Faster than stream, no iterator, but size determined in each loop
		@Benchmark
		public int testListIndexed(ListState state) {
			int count = 0;
			for (int i = 0; i < state.list.size(); i++) {
				int n = state.list.get(i);
				if (n % 2 == 0) {
					count++;
				}
			}
			return count;
		}

		// Fastest, size just determined once
		@Benchmark
		public int test2ListIndexedSized(ListState state) {
			int count = 0;
			int size = state.list.size();
			for (int i = 0; i < size; i++) {
				int n = state.list.get(i);
				if (n % 2 == 0) {
					count++;
				}
			}
			return count;
		}

		//

		// Sequential search is faster until 1'000 elements

		@Benchmark
		public boolean testMatch(ListState state) {
			return state.list.stream().anyMatch(n -> n == NUM_ELEMS - 1);
		}

		@Benchmark
		public boolean testMatchParallel(ListState state) {
			return state.list.parallelStream().anyMatch(n -> n == NUM_ELEMS - 1);
		}

	}
}
