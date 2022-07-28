package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.magicwerk.brownies.collections.helper.GapLists;
import org.magicwerk.brownies.core.CheckTools;
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
		//testListIterator();
		//testPipe();
		//testPerformanceFilterMapJmhTest();
		//testPerformancePipeJmhTest();
		//testPerformanceFlatMapJmhTest();
		testPerformanceHierachyTest();
	}

	//

	// http://insightfullogic.com/2014/May/12/fast-and-megamorphic-what-influences-method-invoca/
	static void testPerformanceHierachyTest() {
		Options opts = new Options().includeClass(PerformanceHierachyTest.class);
		//opts.useGcProfiler();
		//opts.setWarmupIterations(1).setMeasurementIterations(1).setRunTimeSecs(2);
		JmhRunner runner = new JmhRunner();
		//runner.setBuildBrowniesTestAllJar(true);
		runner.runJmh(opts);
	}

	public static class PerformanceHierachyTest {

		static interface Getter {
			int get();
		}

		static class MonoClass implements Getter {

			static Getter of(int count) {
				return new MonoClass(count);
			}

			int count;

			MonoClass(int count) {
				this.count = count;
			}

			@Override
			public int get() {
				return count;
			}
		}

		static class BipolarClass implements Getter {

			static Getter of(int count) {
				if (count == 0) {
					return new MonoClass(count);
				} else {
					return new BipolarClass(count);
				}
			}

			int count;

			BipolarClass(int count) {
				this.count = count;
			}

			@Override
			public int get() {
				return count;
			}
		}

		static class MegaClass implements Getter {

			static Getter of(int count) {
				if (count == 0) {
					return new MonoClass(count);
				} else if (count == 1) {
					return new BipolarClass(count);
				} else {
					return new MegaClass(count);
				}
			}

			int count;

			MegaClass(int count) {
				this.count = count;
			}

			@Override
			public int get() {
				return count;
			}
		}

		static class Getter4 implements Getter {

			static Getter of(int count) {
				if (count == 0) {
					return new MonoClass(count);
				} else if (count == 1) {
					return new BipolarClass(count);
				} else if (count == 2) {
					return new MegaClass(count);
				} else {
					return new Getter4(count);
				}
			}

			int count;

			Getter4(int count) {
				this.count = count;
			}

			@Override
			public int get() {
				return count;
			}
		}

		@State(Scope.Benchmark)
		public static class ListState {
			Getter getter11 = MonoClass.of(0);

			Getter getter21 = BipolarClass.of(0);
			Getter getter22 = BipolarClass.of(1);

			Getter getter31 = MegaClass.of(0);
			Getter getter32 = MegaClass.of(1);
			Getter getter33 = MegaClass.of(2);

			Getter getter41 = Getter4.of(0);
			Getter getter42 = Getter4.of(1);
			Getter getter43 = Getter4.of(2);
			Getter getter44 = Getter4.of(3);

			// monomorphic
			IList<Getter> getters1 = GapList.create(getter11, getter11, getter11, getter11);
			// bimorphics
			IList<Getter> getters2 = GapList.create(getter21, getter22, getter21, getter22);
			// megamorphic
			IList<Getter> getters3 = GapList.create(getter31, getter32, getter33, getter31);
			IList<Getter> getters4 = GapList.create(getter41, getter42, getter43, getter44);
		}

		@Benchmark
		public Object testGetter1(ListState state) {
			return test(state.getters1);
		}

		@Benchmark
		public Object testGetter2(ListState state) {
			return test(state.getters2);
		}

		@Benchmark
		public Object testGetter3(ListState state) {
			return test(state.getters3);
		}

		@Benchmark
		public Object testGetter4(ListState state) {
			return test(state.getters4);
		}

		int test(IList<Getter> getters) {
			int c = 0;
			for (int i = 0; i < getters.size(); i++) {
				c = getters.get(i).get();
			}
			return c;
		}

	}

	//

	static void testPerformancePipeJmhTest() {
		Options opts = new Options().includeClass(PerformancePipeJmhTest.class);
		//opts.useGcProfiler();
		//opts.setWarmupIterations(1).setMeasurementIterations(1).setRunTimeSecs(2);
		JmhRunner runner = new JmhRunner();
		//runner.setBuildBrowniesTestAllJar(true);
		runner.runJmh(opts);
	}

	public static class PerformancePipeJmhTest {

		public static int SIZE;

		@State(Scope.Benchmark)
		public static class ListState {
			IList<Integer> list = IntStream.range(0, SIZE).boxed().collect(GapLists.toGapList());
		}

		//

		@Benchmark
		public Object testListIterator(ListState state) {
			ListIterator<Integer> li = state.list.listIterator();
			while (li.hasNext()) {
				Integer v = li.next();
				li.set(v + 1);
			}
			return state.list;
		}

		@Benchmark
		public Object testPipeWithAdd(ListState state) {
			return pipeWithAdd(state.list, new PipeWithAdd<Integer>() {
				@Override
				public void handle(Integer elem) {
					add(elem + 1);
				}
			});
		}

		@Benchmark
		public Object testPipe(ListState state) {
			return pipe(state.list, (l, e) -> l.add(e + 1));
		}

		@Benchmark
		public Object testStream(ListState state) {
			return state.list.stream().map(e -> e + 1).collect(Collectors.toList());
		}

		@Benchmark
		public Object testPipe2(ListState state) {
			return pipe(pipe(state.list, (l, e) -> l.add(e + 1)), (l, e) -> l.add(e + 1));
		}

		@Benchmark
		public Object testStream2(ListState state) {
			return state.list.stream().map(e -> e + 1).map(e -> e + 1).collect(Collectors.toList());
		}

		@Benchmark
		public Object testPipe3(ListState state) {
			return pipe(pipe(pipe(state.list, (l, e) -> l.add(e + 1)), (l, e) -> l.add(e + 1)), (l, e) -> l.add(e + 1));
		}

		@Benchmark
		public Object testStream3(ListState state) {
			return state.list.stream().map(e -> e + 1).map(e -> e + 1).map(e -> e + 1).collect(Collectors.toList());
		}
	}

	static void testPipe() {
		{
			IList<Integer> list = GapList.create(0, 1, 2);
			LOG.info("{}", list);
			IList<Integer> r = pipeWithAdd(list, new PipeWithAdd<Integer>() {
				@Override
				public void handle(Integer elem) {
					add(elem + 1);
				}
			});
			LOG.info("{}", r);
		}
		{
			IList<Integer> list = GapList.create(0, 1, 2);
			LOG.info("{}", list);
			IList<Integer> r = pipe(list, (l, e) -> l.add(e + 1));
			LOG.info("{}", r);
		}
	}

	static void testListIterator() {
		{
			IList<Integer> list = GapList.create();
			ListIterator<Integer> li = list.listIterator();
			li.add(0);
			LOG.info("{}", list);
		}
		{
			// Prepend and append
			IList<Integer> list = GapList.create(1);
			ListIterator<Integer> li = list.listIterator();
			Integer i = li.next();
			CheckTools.check(i == 1);
			li.previous();
			li.add(0);
			li.next();
			li.add(2);
			LOG.info("{}", list);
		}
	}

	static void testPipe2() {
		{
			IList<Integer> list = GapList.create(0, 1, 2);
			LOG.info("{}", list);
			pipe2(list, new PipeLikeIterator<Integer>() {
				@Override
				public void handle(Integer elem) {
					set(elem + 1);
				}
			});
			LOG.info("{}", list);
		}
		{
			// Prepend and append
			IList<Integer> list = GapList.create(1);
			LOG.info("{}", list);
			pipe2(list, new PipeLikeIterator<Integer>() {

				@Override
				public void handle(Integer elem) {
					prepend(0);
					append(2);
				}
			});
			LOG.info("{}", list);
		}
		{

			IList<Integer> list = GapList.create();
			LOG.info("{}", list);

			pipe2(list, new PipeLikeIterator<Integer>() {
				@Override
				public void handleEnd() {
					if (getList().isEmpty()) {
						append(0);
					}
				}

				@Override
				public void handle(Integer elem) {
				}
			});
			LOG.info("{}", list);
		}

	}

	static <T> IList<T> pipe(IList<T> list, BiConsumer<IList<T>, T> handler) {
		IList<T> result = list.crop();
		int size = list.size();
		for (int i = 0; i < size; i++) {
			T elem = list.get(i);
			handler.accept(result, elem);
		}
		return result;
	}

	static <T> IList<T> pipeWithAdd(IList<T> list, PipeWithAdd<T> pipe) {
		return pipe.pipe(list);
	}

	static class PipeWithAdd<T> {

		IList<T> list;
		IList<T> result;

		public IList<T> getList() {
			return list;
		}

		IList<T> pipe(IList<T> list) {
			this.list = list;
			this.result = list.crop();

			handleStart();
			int size = list.size();
			for (int i = 0; i < size; i++) {
				T elem = list.get(i);
				handle(elem);
			}
			handleEnd();
			return result;
		}

		/**
		 * Method {@link #handle} is called for each element in the list.
		 * 
		 * @param elem element to handle
		 */
		public void handle(T elem) {
		}

		/**
		 * Method {@link #handleStart} is called before iteration of the list starts, i.e. before the first call to {@link #handle}.
		 * If an element should be added at the head, {@link #add} may be called.
		 */
		public void handleStart() {
		}

		/**
		 * Method {@link #handleEnd} is called after iteration of the list has ended, i.e. after the last call to {@link #handle}.
		 * If an element should be added at the tail, {@link #add} may be called.
		 */
		public void handleEnd() {
		}

		/**
		 * Replace element which was passed as argument to the last call of {@link #handle}.
		 * This method is typically called by the client implementation of the overridden {@link #handle} method.
		 * 
		 * @param elem element to set
		 */
		public void add(T elem) {
			result.add(elem);
		}

	}

	static <T> void pipe2(IList<T> list, PipeLikeIterator<T> pipe) {
		pipe.pipe(list);
	}

	static class PipeLikeIterator<T> {

		IList<T> list;
		int index;

		public IList<T> getList() {
			return list;
		}

		void pipe(IList<T> list) {
			this.list = list;
			this.index = -1;

			handleStart();
			if (!list.isEmpty()) {
				index = 0;
				while (index < list.size()) {
					T elem = list.get(index);
					handle(elem);
					index++;
				}
			}
			handleEnd();
		}

		/**
		 * Method {@link #handle} is called for each element in the list.
		 * 
		 * @param elem element to handle
		 */
		public void handle(T elem) {
		}

		/**
		 * Method {@link #handleStart} is called before iteration of the list starts, i.e. before the first call to {@link #handle}.
		 * If an element should be added at the head, {@link #append} may be called (other methods will throw an exception).
		 */
		public void handleStart() {
		}

		/**
		 * Method {@link #handleEnd} is called after iteration of the list has ended, i.e. after the last call to {@link #handle}.
		 * If an element should be added at the tail, {@link #prepend} may be called (other methods will throw an exception).
		 */
		public void handleEnd() {
		}

		/**
		 * Replace element which was passed as argument to the last call of {@link #handle}.
		 * This method is typically called by the client implementation of the overridden {@link #handle} method.
		 * 
		 * @param elem element to set
		 */
		public void set(T elem) {
			list.set(index, elem);
		}

		/**
		 * Remove element which was passed as argument to the last call of {@link #handle}.
		 * This method is typically called by the client implementation of the overridden {@link #handle} method.
		 */
		public void remove() {
			list.remove(index);
			index--;
		}

		/**
		 * Add element after the element which was passed as argument to the last call of {@link #handle}.
		 * This method is typically called by the client implementation of the overridden {@link #handle} method.
		 * 
		 * @param elem element to append
		 */
		public void append(T elem) {
			list.add(index + 1, elem);
			index++;
		}

		/**
		 * Add element before the element which was passed as argument to the last call of {@link #handle}.
		 * This method is typically called by the client implementation of the overridden {@link #handle} method.
		 * 
		 * @param elem element to prepend
		 */
		public void prepend(T elem) {
			list.add(index, elem);
			index++;
		}

	}

	//

	static void testPerformanceFilterMapJmhTest() {
		Options opts = new Options().includeClass(PerformanceFilterMapJmhTest.class);
		//opts.useGcProfiler();
		JmhRunner runner = new JmhRunner();
		//runner.setBuildBrowniesTestAllJar(true);
		runner.runJmh(opts);
	}

	public static class PerformanceFilterMapJmhTest {

		public static int SIZE = 1000;

		@State(Scope.Benchmark)
		public static class ListState {
			IList<Integer> list = IntStream.range(1, SIZE).boxed().collect(GapLists.toGapList());
		}

		//

		@Benchmark
		public Object test1_FilterMapGapList(ListState state) {
			List<Double> r = filterMap(state.list, i -> i % 2 == 0, i -> (double) i);
			return r;
		}

		@Benchmark
		public Object test1_FilterMapStream(ListState state) {
			List<Double> r = state.list.stream().filter(i -> i % 2 == 0).map(i -> (double) i).collect(Collectors.toList());
			return r;
		}

		@Benchmark
		public Object test1_FilterMapArrayList(ListState state) {
			List<Double> r = new ArrayList<>();
			for (int i = 0; i < state.list.size(); i++) {
				int v = state.list.get(i);
				if (v % 2 == 0) {
					r.add((double) v);
				}
			}
			return r;
		}

		//

		@Benchmark
		public Object test2_FilterGapList(ListState state) {
			List<Integer> r = state.list.filteredList(i -> i % 2 == 0);
			return r;
		}

		@Benchmark
		public Object test2_FilterStream(ListState state) {
			List<Integer> r = state.list.stream().filter(i -> i % 2 == 0).collect(Collectors.toList());
			return r;
		}

		@Benchmark
		public Object test2_FilterArrayList(ListState state) {
			List<Integer> r = new ArrayList<>();
			for (int i = 0; i < state.list.size(); i++) {
				int v = state.list.get(i);
				if (v % 2 == 0) {
					r.add(v);
				}
			}
			return r;
		}

		//

		@Benchmark
		public Object test3_MapGapList(ListState state) {
			List<Double> r = state.list.mappedList(i -> (double) i);
			return r;
		}

		@Benchmark
		public Object test3_MapStream(ListState state) {
			List<Double> r = state.list.stream().map(i -> (double) i).collect(Collectors.toList());
			return r;
		}

		@Benchmark
		public Object test3_MapArrayList(ListState state) {
			List<Double> r = new ArrayList<>();
			for (int i = 0; i < state.list.size(); i++) {
				r.add((double) state.list.get(i));
			}
			return r;
		}

		//

		static <T, R> IList<R> filterMap(IList<T> list, Predicate<T> filter, Function<T, R> mapper) {
			IList<R> result = (IList) list.crop();
			for (T elem : list) {
				if (filter.test(elem)) {
					result.add(mapper.apply(elem));
				}
			}
			return result;
		}

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

		static <T, R> IList<R> flatMap(IList<T> list, Function<T, Collection<R>> flatMap) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			IList<R> result = (IList) list.crop();
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
