package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.magicwerk.brownies.collections.helper.GapLists;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.test.JmhRunner;
import org.magicwerk.brownies.test.JmhRunner.Options;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;
import org.slf4j.Logger;

/**
 * JMH benchmark for iterating/streaming List.
 *
 * @author Thomas Mauch
 */
public class StreamsPerformance {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	static final int NUM_ELEMS = 1000;

	public static void main(String[] args) throws RunnerException {
		run();
	}

	static void run() {
		testPerformanceFlatMapJmhTest();
		//testPerformanceIterationJmh();
	}

	//

	static void testPerformanceFlatMapJmhTest() {
		Options opts = new Options().includeClass(PerformanceFlatMapJmhTest.class);
		//opts.useGcProfiler();
		JmhRunner runner = new JmhRunner();
		//runner.setBuildBrowniesTestAllJar(true);
		runner.runJmh(opts);
	}

	@State(Scope.Benchmark)
	public static class PerformanceFlatMapJmhTest {

		@Param({ "10", "100", "1000" })
		//@Param({ "1", "5", "10", "100", "1000" })
		public static int SIZE;

		@State(Scope.Benchmark)
		public static class ListState {
			IList<Integer> list = IntStream.range(1, SIZE).boxed().collect(GapLists.toGapList());
		}

		//

		@Benchmark
		public Object testFlatMapStream(ListState state) {
			List<Integer> r = state.list.stream().flatMap(e -> map(e).stream()).collect(Collectors.toList());
			return r;
		}

		@Benchmark
		public Object testFlatMapGapList(ListState state) {
			List<Integer> r = flatMap(state.list, e -> map(e));
			return r;
		}

		//

		@Benchmark
		public Object testReduceStream(ListState state) {
			Integer i = state.list.stream().reduce(0, Integer::sum);
			return i;
		}

		@Benchmark
		public Object testReduceGapList(ListState state) {
			Integer i = reduce(state.list, 0, Integer::sum);
			return i;
		}

		//

		@Benchmark
		public Object testFlatMapReduceStream(ListState state) {
			Integer i = state.list.stream().flatMap(e -> map(e).stream()).reduce(0, Integer::sum);
			return i;
		}

		@Benchmark
		public Object testFlatMapReduceGapList(ListState state) {
			Integer i = reduce(flatMap(state.list, e -> map(e)), 0, Integer::sum);
			return i;
		}

		//

		static IList<Integer> map(int n) {
			IList<Integer> l = GapList.create();
			for (int i = 0; i < n; i++) {
				l.add(n * n);
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

		static <T> T reduce(List<T> list, T identity, BinaryOperator<T> accumulator) {
			T result = identity;
			for (T element : list) {
				result = accumulator.apply(result, element);
			}
			return result;
		}

	}

	//

	static void testPerformanceIterationJmh() {
		Options opts = new Options().includeClass(PerformanceIterationJmhTest.class);
		new JmhRunner().runJmh(opts);
	}

	public static class PerformanceIterationJmhTest {

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
