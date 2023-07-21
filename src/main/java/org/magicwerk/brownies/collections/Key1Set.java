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
 * Key1Set implements a set.
 * The elements allowed in the set can be constraint (null/duplicate values).
 *
 * @author Thomas Mauch
 *
 * @see Key1Collection
 * @param <E> type of elements stored in the set
 */
@SuppressWarnings("serial")
public class Key1Set<E, K> extends Key1Collection<E, K> implements Set<E> {

	/**
	 * Builder to construct Key1Set instances.
	 */
	public static class Builder<E, K> extends Key1Collection.Builder<E, K> {
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
		Builder(Key1Set<E, K> keySet) {
			this.keyColl = keySet;
			initKeyMapBuilder(0);
		}

		/**
		 * @return created collection
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Key1Set<E, K> build() {
			withSetBehavior(true);
			withElemDuplicates(false);

			if (keyColl == null) {
				keyColl = new Key1Set<>();
			}
			build(keyColl, false);
			init(keyColl);
			return (Key1Set<E, K>) keyColl;
		}

		// Builder: Overriden

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that {@link Key1Set} only supports set behavior, so an exception is thrown if the argument is false.
		 */
		@Override
		public Builder<E, K> withSetBehavior(boolean setBehavior) {
			if (!setBehavior) {
				KeyCollectionImpl.errorInvalidSetBehavior();
			}
			return (Builder<E, K>) super.withSetBehavior(setBehavior);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that {@link Key1Set} does not support duplicates, so an exception is thrown if the argument is true.
		 */
		@Override
		public Builder<E, K> withElemDuplicates(boolean allowDuplicates) {
			if (allowDuplicates) {
				KeyCollectionImpl.errorInvaliDuplicates();
			}
			return (Builder<E, K>) super.withElemDuplicates(allowDuplicates);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that {@link Key1Set} does not support duplicates, so an exception is thrown if any argument is true.
		 */
		@Override
		public Builder<E, K> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
			if (allowDuplicates || allowDuplicatesNull) {
				KeyCollectionImpl.errorInvaliDuplicates();
			}
			return (Builder<E, K>) super.withElemDuplicates(allowDuplicates, allowDuplicatesNull);
		}

		// Builder: Verbatim copy of KeyCollection

		// -- Constraint

		@Override
		public Builder<E, K> withNull(boolean allowNull) {
			return (Builder<E, K>) super.withNull(allowNull);
		}

		@Override
		public Builder<E, K> withConstraint(Predicate<E> constraint) {
			return (Builder<E, K>) super.withConstraint(constraint);
		}

		// -- Triggers

		@Override
		public Builder<E, K> withBeforeInsertTrigger(Consumer<E> trigger) {
			return (Builder<E, K>) super.withBeforeInsertTrigger(trigger);
		}

		@Override
		public Builder<E, K> withAfterInsertTrigger(Consumer<E> trigger) {
			return (Builder<E, K>) super.withAfterInsertTrigger(trigger);
		}

		@Override
		public Builder<E, K> withBeforeDeleteTrigger(Consumer<E> trigger) {
			return (Builder<E, K>) super.withBeforeDeleteTrigger(trigger);
		}

		@Override
		public Builder<E, K> withAfterDeleteTrigger(Consumer<E> trigger) {
			return (Builder<E, K>) super.withAfterDeleteTrigger(trigger);
		}

		//-- Content

		@Override
		public Builder<E, K> withCapacity(int capacity) {
			return (Builder<E, K>) super.withCapacity(capacity);
		}

		@Override
		public Builder<E, K> withContent(Collection<? extends E> elements) {
			return (Builder<E, K>) super.withContent(elements);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Builder<E, K> withContent(E... elements) {
			return (Builder<E, K>) super.withContent(elements);
		}

		@Override
		public Builder<E, K> withMaxSize(int maxSize) {
			return (Builder<E, K>) super.withMaxSize(maxSize);
		}

		//-- Element key

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that {@link Key1Set} does not support duplicates, so an exception is thrown if the argument is true.
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Builder<E, K> withElemCount(boolean count) {
			if (count) {
				KeyCollectionImpl.errorInvalidSetBehavior();
			}
			return (Builder<E, K>) super.withElemCount(count);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that a {@link Key1Set} always has an element set, so this call is not necessary.
		 */
		@Override
		public Builder<E, K> withElemSet() {
			return (Builder<E, K>) super.withElemSet();
		}

		@Override
		public Builder<E, K> withOrderByElem(boolean orderBy) {
			return (Builder<E, K>) super.withOrderByElem(orderBy);
		}

		@Override
		public Builder<E, K> withElemNull(boolean allowNull) {
			return (Builder<E, K>) super.withElemNull(allowNull);
		}

		@Override
		public Builder<E, K> withElemSort(boolean sort) {
			return (Builder<E, K>) super.withElemSort(sort);
		}

		@Override
		public Builder<E, K> withElemSort(Comparator<? super E> comparator) {
			return (Builder<E, K>) super.withElemSort(comparator);
		}

		@Override
		public Builder<E, K> withElemSort(Comparator<? super E> comparator, boolean sortNullsFirst) {
			return (Builder<E, K>) super.withElemSort(comparator, sortNullsFirst);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that a {@link Key1Set} always has an element set, so this call is not necessary.
		 */
		@Override
		public Builder<E, K> withPrimaryElem() {
			return this;
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that a {@link Key1Set} always has an element set, so an exception is thrown.
		 */
		@Override
		public Builder<E, K> withUniqueElem() {
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
		@Override
		@SuppressWarnings("unchecked")
		public Builder<E, K> withKey1Map(Function<? super E, K> mapper) {
			return (Builder<E, K>) withKeyMap(1, mapper);
		}

		/**
		 * Specify this key to be a primary key.
		 * This is identical to calling {@code withKey1Map(mapper), withKey1Null(false), and withKey1Duplicates(false)}.
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Builder<E, K> withPrimaryKey1Map(Function<? super E, K> mapper) {
			return (Builder<E, K>) super.withPrimaryKeyMap(1, mapper);
		}

		/**
		 * Specify this key to be a unique key.
		 * This is identical to calling {@code withKey1Map(mapper), withKey1Null(true), and withKey1Duplicates(false, true)}.
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Builder<E, K> withUniqueKey1Map(Function<? super E, K> mapper) {
			return (Builder<E, K>) super.withUniqueKeyMap(1, mapper);
		}

		@Override
		public Builder<E, K> withOrderByKey1(boolean orderBy) {
			return (Builder<E, K>) super.withOrderByKey1(orderBy);
		}

		@Override
		public Builder<E, K> withKey1Null(boolean allowNull) {
			return (Builder<E, K>) super.withKey1Null(allowNull);
		}

		@Override
		public Builder<E, K> withKey1Duplicates(boolean allowDuplicates) {
			return (Builder<E, K>) super.withKey1Duplicates(allowDuplicates);
		}

		@Override
		public Builder<E, K> withKey1Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
			return (Builder<E, K>) super.withKey1Duplicates(allowDuplicates, allowDuplicatesNull);
		}

		@Override
		public Builder<E, K> withKey1Sort(boolean sort) {
			return (Builder<E, K>) super.withKey1Sort(sort);
		}

		/**
		 * Set comparator to use for sorting the key map.
		 * Note that this does not automatically sort the collection itself, call a withOrderBy method for this.
		 *
		 * @param comparator    comparator to use for sorting
		 * @return              this (fluent interface)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Builder<E, K> withKey1Sort(Comparator<? super K> comparator) {
			return (Builder<E, K>) super.withKeySort(1, comparator);
		}

		/**
		 * Set comparator to use for sorting the key map.
		 * Note that this does not automatically sort the collection itself, call a withOrderBy method for this.
		 *
		 * @param comparator            comparator to use for sorting
		 * @param sortNullsFirst   		true if null will be sorted first, false for last
		 * @return                      this (fluent interface)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Builder<E, K> withKey1Sort(Comparator<? super K> comparator, boolean sortNullsFirst) {
			return (Builder<E, K>) super.withKeySort(1, comparator, sortNullsFirst);
		}

	}

	/**
	 * Protected constructor used by builder or derived collections.
	 */
	protected Key1Set() {
	}

	/**
	 * @return builder to use in extending classes
	 */
	@Override
	protected Builder<E, K> getBuilder() {
		return new Builder<>(this);
	}

	@Override
	public Key1Set<E, K> copy() {
		return (Key1Set<E, K>) super.copy();
	}

	@Override
	public Key1Set<E, K> crop() {
		return (Key1Set<E, K>) super.crop();
	}

	@Override
	public Key1Set<E, K> getAll(E elem) {
		return (Key1Set<E, K>) super.getAll(elem);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Key1Set<E, K> getAllByKey1(K key) {
		return (Key1Set<E, K>) super.getAllByKey(1, key);
	}

	@Override
	public Key1Set<E, K> filter(Predicate<? super E> filter) {
		return (Key1Set<E, K>) super.filter(filter);
	}

}
