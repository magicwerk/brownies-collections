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
package org.magicwerk.brownies.collections.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.primitive.BooleanObjGapList;
import org.magicwerk.brownies.collections.primitive.ByteGapList;
import org.magicwerk.brownies.collections.primitive.ByteObjGapList;
import org.magicwerk.brownies.collections.primitive.CharGapList;
import org.magicwerk.brownies.collections.primitive.CharObjGapList;
import org.magicwerk.brownies.collections.primitive.DoubleObjGapList;
import org.magicwerk.brownies.collections.primitive.FloatObjGapList;
import org.magicwerk.brownies.collections.primitive.GapListPrimitives;
import org.magicwerk.brownies.collections.primitive.IntObjGapList;
import org.magicwerk.brownies.collections.primitive.LongObjGapList;
import org.magicwerk.brownies.collections.primitive.ShortObjGapList;

/**
 * Helper class to create wrapper list objects wrapping primitive GapLists.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class GapLists extends GapListPrimitives {

	/**
	 * Create a GapList wrapping a primitive GapList, e.g. an IntObjGapList wrapping an IntGapList.
	 *
	 * @param type	primitive type for GapList
	 * @return		created wrapping GapList
	 * @throws 		IllegalArgumentException if no primitive type is specified
	 */
	public static IList<?> createWrapperList(Class<?> type) {
		if (type == int.class) {
			return new IntObjGapList();
		} else if (type == long.class) {
			return new LongObjGapList();
		} else if (type == double.class) {
			return new DoubleObjGapList();
		} else if (type == float.class) {
			return new FloatObjGapList();
		} else if (type == boolean.class) {
			return new BooleanObjGapList();
		} else if (type == byte.class) {
			return new ByteObjGapList();
		} else if (type == char.class) {
			return new CharObjGapList();
		} else if (type == short.class) {
			return new ShortObjGapList();
		} else {
			throw new IllegalArgumentException("Primitive type expected: " + type);
		}
	}

	/**
	 * Create a GapList wrapping a primitive GapList, e.g. an IntObjGapList wrapping an IntGapList.
	 *
	 * @param type		primitive type for GapList
	 * @param capacity	initial capacity of created list
	 * @return			created wrapping GapList
	 * @throws 			IllegalArgumentException if no primitive type is specified
	 */
	public static IList<?> createWrapperList(Class<?> type, int capacity) {
		if (type == int.class) {
			return new IntObjGapList(capacity);
		} else if (type == long.class) {
			return new LongObjGapList(capacity);
		} else if (type == double.class) {
			return new DoubleObjGapList(capacity);
		} else if (type == float.class) {
			return new FloatObjGapList(capacity);
		} else if (type == boolean.class) {
			return new BooleanObjGapList(capacity);
		} else if (type == byte.class) {
			return new ByteObjGapList(capacity);
		} else if (type == char.class) {
			return new CharObjGapList(capacity);
		} else if (type == short.class) {
			return new ShortObjGapList(capacity);
		} else {
			throw new IllegalArgumentException("Primitive type expected: " + type);
		}
	}

	/**
	 * Return collector which collects the elements into a GapList.
	 *
	 * @return collector
	 */
	public static <T> Collector<T, ?, IList<T>> toGapList() {
		return new CollectorImpl<>((Supplier<List<T>>) GapList::new, List::add,
				(left, right) -> {
					left.addAll(right);
					return left;
				},
				CollectorImpl.CH_ID);
	}

	private static class CollectorImpl<T, A, R> implements Collector<T, A, R> {
		private final Supplier<A> supplier;
		private final BiConsumer<A, T> accumulator;
		private final BinaryOperator<A> combiner;
		private final Function<A, R> finisher;
		private final Set<Characteristics> characteristics;

		CollectorImpl(Supplier<A> supplier,
				BiConsumer<A, T> accumulator,
				BinaryOperator<A> combiner,
				Function<A, R> finisher,
				Set<Characteristics> characteristics) {
			this.supplier = supplier;
			this.accumulator = accumulator;
			this.combiner = combiner;
			this.finisher = finisher;
			this.characteristics = characteristics;
		}

		CollectorImpl(Supplier<A> supplier,
				BiConsumer<A, T> accumulator,
				BinaryOperator<A> combiner,
				Set<Characteristics> characteristics) {
			this(supplier, accumulator, combiner, castingIdentity(), characteristics);
		}

		static final Set<Collector.Characteristics> CH_ID = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));

		@Override
		public BiConsumer<A, T> accumulator() {
			return accumulator;
		}

		@Override
		public Supplier<A> supplier() {
			return supplier;
		}

		@Override
		public BinaryOperator<A> combiner() {
			return combiner;
		}

		@Override
		public Function<A, R> finisher() {
			return finisher;
		}

		@Override
		public Set<Characteristics> characteristics() {
			return characteristics;
		}

		@SuppressWarnings("unchecked")
		private static <I, R> Function<I, R> castingIdentity() {
			return i -> (R) i;
		}
	}

	/**
	 * Read specified number of bytes from InputStream into ByteGapList.
	 * 
	 * @param istream	input stream (source)
	 * @param list		list (target)
	 * @param len		maximum number of bytes to read
	 * @return			number of bytes read into the buffer, -1 if end of stream has been reached
	 */
	public static int read(InputStream istream, ByteGapList list, int len) throws IOException {
		return GapListPrimitives.read(istream, list, len);
	}

	/**
	 * Read specified number of chars from Reader into CharGapList.
	 * 
	 * @param reader	reader (source)
	 * @param list		list (target)
	 * @param len		maximum number of bytes to read
	 * @return			number of bytes read into the buffer, -1 if end of stream has been reached
	 */
	public static int read(Reader reader, CharGapList list, int len) throws IOException {
		return GapListPrimitives.read(reader, list, len);
	}

	/**
	 * Add specified number of chars from CharSequence into CharGapList.
	 * 
	 * @param str		CharSequence (source)
	 * @param list		list (target)
	 * @param start		start position of characters to add in CharSequence
	 * @param end		end position of characters to add in CharSequence
	 */
	public static void add(CharSequence str, CharGapList list, int start, int end) {
		GapListPrimitives.add(str, list, start, end);
	}

}
