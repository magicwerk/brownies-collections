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

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Key2Set implements a set.
 * The elements allowed in the set can be constraint (null/duplicate values).
 *
 * @author Thomas Mauch
 *
 * @see Key2Collection
 * @param <E> type of elements stored in the set
 */
public class Key2Set<E, K1, K2> extends Key2Collection<E, K1, K2> implements Set<E> {

	/**
	 * Builder to construct Key2Set instances.
	 */
	public static class Builder<E, K1, K2> extends Key2Collection.Builder<E, K1, K2> {
		/**
		 * Default constructor.
		 */
		public Builder() {
			this(null);
		}

		/**
		 * Private constructor used if extending KeySet.
		 *
		 * @param keySet	key set
		 */
		Builder(Key2Set<E, K1, K2> keySet) {
			this.keyColl = keySet;
			initKeyMapBuilder(0);
		}

		/**
		 * @return created collection
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Key2Set<E, K1, K2> build() {
			withSetBehavior(true);
			withElemDuplicates(false);

			if (keyColl == null) {
				keyColl = new Key2Set<>();
			}
			build(keyColl, false);
			init(keyColl);
			return (Key2Set<E, K1, K2>) keyColl;
		}

		// Builder: Overriden

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that {@link Key2Set} only supports set behavior, so an exception is thrown if the argument is false.
		 */
		@Override
		public Builder<E, K1, K2> withSetBehavior(boolean setBehavior) {
			if (!setBehavior) {
				KeyCollectionImpl.errorInvalidSetBehavior();
			}
			return (Builder<E, K1, K2>) super.withSetBehavior(setBehavior);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that {@link Key2Set} does not support duplicates, so an exception is thrown if the argument is true.
		 */
		@Override
		public Builder<E, K1, K2> withElemDuplicates(boolean allowDuplicates) {
			if (allowDuplicates) {
				KeyCollectionImpl.errorInvaliDuplicates();
			}
			return (Builder<E, K1, K2>) super.withElemDuplicates(allowDuplicates);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that {@link Key2Set} does not support duplicates, so an exception is thrown if any argument is true.
		 */
		@Override
		public Builder<E, K1, K2> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
			if (allowDuplicates || allowDuplicatesNull) {
				KeyCollectionImpl.errorInvaliDuplicates();
			}
			return (Builder<E, K1, K2>) super.withElemDuplicates(allowDuplicates, allowDuplicatesNull);
		}

		// Builder: Verbatim copy of Key2Collection

		// -- Constraint

		@Override
		public Builder<E, K1, K2> withNull(boolean allowNull) {
			return (Builder<E, K1, K2>) super.withNull(allowNull);
		}

		@Override
		public Builder<E, K1, K2> withConstraint(Predicate<E> constraint) {
			return (Builder<E, K1, K2>) super.withConstraint(constraint);
		}

		// -- Triggers

		@Override
		public Builder<E, K1, K2> withBeforeInsertTrigger(Consumer<E> trigger) {
			return (Builder<E, K1, K2>) super.withBeforeInsertTrigger(trigger);
		}

		@Override
		public Builder<E, K1, K2> withAfterInsertTrigger(Consumer<E> trigger) {
			return (Builder<E, K1, K2>) super.withAfterInsertTrigger(trigger);
		}

		@Override
		public Builder<E, K1, K2> withBeforeDeleteTrigger(Consumer<E> trigger) {
			return (Builder<E, K1, K2>) super.withBeforeDeleteTrigger(trigger);
		}

		@Override
		public Builder<E, K1, K2> withAfterDeleteTrigger(Consumer<E> trigger) {
			return (Builder<E, K1, K2>) super.withAfterDeleteTrigger(trigger);
		}

		//-- Content

		@Override
		public Builder<E, K1, K2> withCapacity(int capacity) {
			return (Builder<E, K1, K2>) super.withCapacity(capacity);
		}

		@Override
		public Builder<E, K1, K2> withContent(Collection<? extends E> elements) {
			return (Builder<E, K1, K2>) super.withContent(elements);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K1, K2> withContent(E... elements) {
			return (Builder<E, K1, K2>) super.withContent(elements);
		}

		@Override
		public Builder<E, K1, K2> withMaxSize(int maxSize) {
			return (Builder<E, K1, K2>) super.withMaxSize(maxSize);
		}

		//-- Element key

		@SuppressWarnings("unchecked")
		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that {@link Key2Set} does not support duplicates, so an exception is thrown if the argument is true.
		 */
		@Override
		public Builder<E, K1, K2> withElemCount(boolean count) {
			if (count) {
				KeyCollectionImpl.errorInvalidSetBehavior();
			}
			return (Builder<E, K1, K2>) super.withElemCount(count);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that a {@link Key2Set} always has an element set, so this call is not necessary.
		 */
		@Override
		public Builder<E, K1, K2> withElemSet() {
			return (Builder<E, K1, K2>) super.withElemSet();
		}

		@Override
		public Builder<E, K1, K2> withOrderByElem(boolean orderBy) {
			return (Builder<E, K1, K2>) super.withOrderByElem(orderBy);
		}

		@Override
		public Builder<E, K1, K2> withElemNull(boolean allowNull) {
			return (Builder<E, K1, K2>) super.withElemNull(allowNull);
		}

		@Override
		public Builder<E, K1, K2> withElemSort(boolean sort) {
			return (Builder<E, K1, K2>) super.withElemSort(sort);
		}

		@Override
		public Builder<E, K1, K2> withElemSort(Comparator<? super E> comparator) {
			return (Builder<E, K1, K2>) super.withElemSort(comparator);
		}

		@Override
		public Builder<E, K1, K2> withElemSort(Comparator<? super E> comparator, boolean sortNullsFirst) {
			return (Builder<E, K1, K2>) super.withElemSort(comparator, sortNullsFirst);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that a {@link Key2Set} always has an element set, so this call is not necessary.
		 */
		@Override
		public Builder<E, K1, K2> withPrimaryElem() {
			return this;
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that a {@link Key2Set} always has an element set, so an exception is thrown.
		 */
		@Override
		public Builder<E, K1, K2> withUniqueElem() {
			KeyCollectionImpl.errorInvalidSetBehavior();
			return this;
		}

		// -- Key1

		/**
		 * Add key map.
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K1, K2> withKey1Map(Function<? super E, K1> mapper) {
			return (Builder<E, K1, K2>) super.withKeyMap(1, mapper);
		}

		/**
		 * Specify this key to be a primary key.
		 * This is identical to calling {@code withKey1Map(mapper), withKey1Null(false), and withKey1Duplicates(false)}.
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K1, K2> withPrimaryKey1Map(Function<? super E, K1> mapper) {
			return (Builder<E, K1, K2>) super.withPrimaryKeyMap(1, mapper);
		}

		/**
		 * Specify this key to be a unique key.
		 * This is identical to calling {@code withKey1Map(mapper), withKey1Null(true), and withKey1Duplicates(false, true)}.
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K1, K2> withUniqueKey1Map(Function<? super E, K1> mapper) {
			return (Builder<E, K1, K2>) super.withUniqueKeyMap(1, mapper);
		}

		@Override
		public Builder<E, K1, K2> withOrderByKey1(boolean orderBy) {
			return (Builder<E, K1, K2>) super.withOrderByKey1(orderBy);
		}

		@Override
		public Builder<E, K1, K2> withKey1Null(boolean allowNull) {
			return (Builder<E, K1, K2>) super.withKey1Null(allowNull);
		}

		@Override
		public Builder<E, K1, K2> withKey1Duplicates(boolean allowDuplicates) {
			return (Builder<E, K1, K2>) super.withKey1Duplicates(allowDuplicates);
		}

		@Override
		public Builder<E, K1, K2> withKey1Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
			return (Builder<E, K1, K2>) super.withKey1Duplicates(allowDuplicates, allowDuplicatesNull);
		}

		@Override
		public Builder<E, K1, K2> withKey1Sort(boolean sort) {
			return (Builder<E, K1, K2>) super.withKey1Sort(sort);
		}

		/**
		 * Set comparator to use for sorting the key map.
		 * Note that this does not automatically sort the list collection, call a withOrderBy method for this.
		 *
		 * @param comparator    comparator to use for sorting
		 * @return              this (fluent interface)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K1, K2> withKey1Sort(Comparator<? super K1> comparator) {
			return (Builder<E, K1, K2>) super.withKeySort(1, comparator);
		}

		/**
		 * Set comparator to use for sorting the key map.
		 * Note that this does not automatically sort the list collection, call a withOrderBy method for this.
		 *
		 * @param comparator            comparator to use for sorting
		 * @param sortNullsFirst   		true if null will be sorted first, false for last
		 * @return                      this (fluent interface)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K1, K2> withKey1Sort(Comparator<? super K1> comparator, boolean sortNullsFirst) {
			return (Builder<E, K1, K2>) super.withKeySort(1, comparator, sortNullsFirst);
		}

		// -- Key2

		/**
		 * Add key map.
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K1, K2> withKey2Map(Function<? super E, K2> mapper) {
			return (Builder<E, K1, K2>) super.withKeyMap(2, mapper);
		}

		/**
		 * Specify this key to be a primary key.
		 * This is identical to calling {@code withKey2Map(mapper), withKey2Null(false), and withKey2Duplicates(false)}.
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K1, K2> withPrimaryKey2Map(Function<? super E, K2> mapper) {
			return (Builder<E, K1, K2>) super.withPrimaryKeyMap(2, mapper);
		}

		/**
		 * Specify this key to be a unique key.
		 * This is identical to calling {@code withKey2Map(mapper), withKey2Null(true), and withKey2Duplicates(false, true)}.
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K1, K2> withUniqueKey2Map(Function<? super E, K2> mapper) {
			return (Builder<E, K1, K2>) super.withUniqueKeyMap(2, mapper);
		}

		@Override
		public Builder<E, K1, K2> withOrderByKey2(boolean orderBy) {
			return (Builder<E, K1, K2>) super.withOrderByKey2(orderBy);
		}

		@Override
		public Builder<E, K1, K2> withKey2Null(boolean allowNull) {
			return (Builder<E, K1, K2>) super.withKey2Null(allowNull);
		}

		@Override
		public Builder<E, K1, K2> withKey2Duplicates(boolean allowDuplicates) {
			return (Builder<E, K1, K2>) super.withKey2Duplicates(allowDuplicates);
		}

		@Override
		public Builder<E, K1, K2> withKey2Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
			return (Builder<E, K1, K2>) super.withKey2Duplicates(allowDuplicates, allowDuplicatesNull);
		}

		@Override
		public Builder<E, K1, K2> withKey2Sort(boolean sort) {
			return (Builder<E, K1, K2>) super.withKey2Sort(sort);
		}

		/**
		 * Set comparator to use for sorting the key map.
		 * Note that this does not automatically sort the collection itself, call a withOrderBy method for this.
		 *
		 * @param comparator    comparator to use for sorting
		 * @return              this (fluent interface)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K1, K2> withKey2Sort(Comparator<? super K2> comparator) {
			return (Builder<E, K1, K2>) super.withKeySort(2, comparator);
		}

		/**
		 * Set comparator to use for sorting the key map.
		 * Note that this does not automatically sort the collection itself, call a withOrderBy method for this.
		 *
		 * @param comparator            comparator to use for sorting
		 * @param sortNullsFirst   		true if null will be sorted first, false for last
		 * @return                      this (fluent interface)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K1, K2> withKey2Sort(Comparator<? super K2> comparator, boolean sortNullsFirst) {
			return (Builder<E, K1, K2>) super.withKeySort(2, comparator, sortNullsFirst);
		}

	}

	/**
	 * Protected constructor used by builder or derived collections.
	 */
	protected Key2Set() {
	}

	/**
	 * @return builder to use in extending classes
	 */
	@Override
	protected Builder<E, K1, K2> getBuilder() {
		return new Builder<>(this);
	}

	@Override
	public Key2Set<E, K1, K2> copy() {
		return (Key2Set<E, K1, K2>) super.copy();
	}

	@Override
	public Key2Set<E, K1, K2> crop() {
		return (Key2Set<E, K1, K2>) super.crop();
	}

	@Override
	public Key2Set<E, K1, K2> getAll(E elem) {
		return (Key2Set<E, K1, K2>) super.getAll(elem);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Key2Set<E, K1, K2> getAllByKey1(K1 key) {
		return (Key2Set<E, K1, K2>) super.getAllByKey(1, key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Key2Set<E, K1, K2> getAllByKey2(K2 key) {
		return (Key2Set<E, K1, K2>) super.getAllByKey(2, key);
	}

	@Override
	public Key2Set<E, K1, K2> filter(Predicate<? super E> filter) {
		return (Key2Set<E, K1, K2>) super.filter(filter);
	}

}
