/*
 * Copyright 2013 by Thomas Mauch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */
package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.magictest.client.Assert;
import org.magictest.client.Test;
import org.magictest.client.Trace;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.test.JmhRunner;
import org.magicwerk.brownies.test.JmhRunner.Options;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;

/**
 * Test of KeySet.
 *
 * @author Thomas Mauch
 */
public class KeySetTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		test();
	}

	static void test() {
		//testEquals();
		testAddPerformanceJmh();
	}

	KeySet<Integer> getKeySet(int num) {
		KeySet<Integer> set = new KeySet.Builder<Integer>().build();
		for (int i = 0; i < num; i++) {
			set.add(i);
		}
		return set;
	}

	// ICollection

	@Trace
	public void testGetFirst() {
		getKeySet(0).getFirst();
		getKeySet(1).getFirst();
		getKeySet(2).getFirst();
	}

	@Trace
	public void testGetFirstOrNull() {
		getKeySet(0).getFirstOrNull();
		getKeySet(1).getFirstOrNull();
		getKeySet(2).getFirstOrNull();
	}

	@Trace
	public void testGetSingle() {
		getKeySet(0).getSingle();
		getKeySet(1).getSingle();
		getKeySet(2).getSingle();
	}

	@Trace
	public void testGetSingleOrNull() {
		getKeySet(0).getSingleOrNull();
		getKeySet(1).getSingleOrNull();
		getKeySet(2).getSingleOrNull();
	}

	Predicate<Integer> filter = i -> i % 2 == 0;
	Function<Integer, String> map = i -> "(" + i + ")";

	@Trace
	public void testContainsIf() {
		getKeySet(0).containsIf(filter);
		getKeySet(1).containsIf(filter);
		getKeySet(2).containsIf(filter);
	}

	@Trace
	public void testCountIf() {
		getKeySet(0).countIf(filter);
		getKeySet(1).countIf(filter);
		getKeySet(2).countIf(filter);
		getKeySet(3).countIf(filter);
		getKeySet(4).countIf(filter);
	}

	@Trace
	public void testGetIf() {
		getKeySet(0).getIf(filter);
		getKeySet(1).getIf(filter);
		getKeySet(2).getIf(filter);
		getKeySet(3).getIf(filter);
		getKeySet(4).getIf(filter);
	}

	@Trace
	public void testFilteredList() {
		getKeySet(0).filteredList(filter);
		getKeySet(1).filteredList(filter);
		getKeySet(2).filteredList(filter);
		getKeySet(3).filteredList(filter);
		getKeySet(4).filteredList(filter);
	}

	@Trace
	public void testMappedList() {
		getKeySet(0).mappedList(map);
		getKeySet(1).mappedList(map);
		getKeySet(2).mappedList(map);
		getKeySet(3).mappedList(map);
		getKeySet(4).mappedList(map);
	}

	//

	@Test
	public static void testEquals() {
		// Empty set and list are not equal
		{
			Set<String> set = new HashSet<>();
			List<String> list = new ArrayList<>(set);
			Assert.assertTrue(!set.equals(list));
		}
		{
			KeySet<String> set = new KeySet.Builder<String>().build();
			List<String> list = new ArrayList<>(set);
			Assert.assertTrue(!set.equals(list));
		}
	}

	static void testAddPerformanceJmh() {
		//		Options opts = new Options().includeClass(PerformanceJmhTest.class);
		Options opts = new Options().includeClass(AddPerformanceJmhTest.class);
		opts.setWarmupIterations(3).setMeasurementIterations(2);
		opts.setRunTimeMillis(500);
		JmhRunner runner = new JmhRunner();
		runner.runJmh(opts);
	}

	public static class AddPerformanceJmhTest {

		@State(Scope.Benchmark)
		public static class BenchmarkState {
			static int SIZE = 1000;

			Set<Integer> keySet;
			Set<Integer> hashSet;
			int count;

			@Setup(Level.Iteration)
			public void setup() {
				keySet = new KeySet.Builder<Integer>().build();
				for (int i = 0; i < SIZE; i++) {
					keySet.add(i);
				}
				hashSet = new HashSet<Integer>();
				for (int i = 0; i < SIZE; i++) {
					hashSet.add(i);
				}
				count = SIZE;
			}

		}

		@Benchmark
		public int testKeySetAdd(BenchmarkState state) {
			state.keySet.add(state.count);
			state.count++;
			return state.count;
		}

		@Benchmark
		public int testKeySetAddCheck(BenchmarkState state) {
			if (!state.keySet.contains(state.count)) {
				state.keySet.add(state.count);
			}
			state.count++;
			return state.count;
		}

		@Benchmark
		public int testKeySetAddDuplicateSetBehavior(BenchmarkState state) {
			state.keySet.add(0);
			state.count++;
			return state.count;
		}

		@Benchmark
		public int testKeySetAddDuplicateCheck(BenchmarkState state) {
			if (!state.keySet.contains(0)) {
				state.keySet.add(0);
			}
			state.count++;
			return state.count;
		}

		@Benchmark
		public int testHashSetAdd(BenchmarkState state) {
			state.hashSet.add(state.count);
			state.count++;
			return state.count;
		}

		@Benchmark
		public int testHashSetAddDuplicate(BenchmarkState state) {
			state.hashSet.add(0);
			state.count++;
			return state.count;
		}

	}

}
