/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.magicwerk.brownies.collections.jmh;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.magicwerk.brownies.collections.BigList;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

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
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class MyBenchmark {

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
		List<Integer> result = state.list.getIf((i) -> i % 2 == 0);
	}

	//

	@Benchmark
	public void testGetArrayList(ArrayListState state) {
		int sum = 0;
		for (int i = 0; i < size; i++) {
			sum += state.list.get(i);
		}
	}

	@Benchmark
	public void testGetGapList(GapListState state) {
		int sum = 0;
		for (int i = 0; i < size; i++) {
			sum += state.list.get(i);
		}
	}

	@Benchmark
	public void testGetBigList(BigListState state) {
		int sum = 0;
		for (int i = 0; i < size; i++) {
			sum += state.list.get(i);
		}
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
