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
import java.util.function.Predicate;

/**
 * KeySet implements a set.
 * The elements allowed in the set can be constraint (null/duplicate values).
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @see Key1Collection
 * @param <E> type of elements stored in the set
 */
@SuppressWarnings("serial")
public class KeySet<E> extends KeyCollection<E> implements Set<E> {

	/**
	 * Builder to construct KeySet instances.
	 */
	public static class Builder<E> extends KeyCollection.Builder<E> {
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
		Builder(KeySet<E> keySet) {
			this.keyColl = keySet;
			initKeyMapBuilder(0);
		}

		/**
		 * @return created collection
		 */
		@Override
		public KeySet<E> build() {
			withSetBehavior(true);
			withElemDuplicates(false);

			if (keyColl == null) {
				keyColl = new KeySet<>();
			}
			build(keyColl, false);
			init(keyColl);
			return (KeySet<E>) keyColl;
		}

		// Builder: Overriden

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that {@link KeySet} only supports set behavior, so an exception is thrown if the argument is false.
		 */
		@Override
		public Builder<E> withSetBehavior(boolean setBehavior) {
			if (!setBehavior) {
				KeyCollectionImpl.errorInvalidSetBehavior();
			}
			return (Builder<E>) super.withSetBehavior(setBehavior);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that {@link KeySet} does not support duplicates, so an exception is thrown if the argument is true.
		 */
		@Override
		public Builder<E> withElemDuplicates(boolean allowDuplicates) {
			if (allowDuplicates) {
				KeyCollectionImpl.errorInvaliDuplicates();
			}
			return (Builder<E>) super.withElemDuplicates(allowDuplicates);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that {@link KeySet} does not support duplicates, so an exception is thrown if any argument is true.
		 */
		@Override
		public Builder<E> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
			if (allowDuplicates || allowDuplicatesNull) {
				KeyCollectionImpl.errorInvaliDuplicates();
			}
			return (Builder<E>) super.withElemDuplicates(allowDuplicates, allowDuplicatesNull);
		}

		// Builder: Verbatim copy of KeyCollection

		// -- Constraint

		@Override
		public Builder<E> withNull(boolean allowNull) {
			return (Builder<E>) super.withNull(allowNull);
		}

		@Override
		public Builder<E> withConstraint(Predicate<E> constraint) {
			return (Builder<E>) super.withConstraint(constraint);
		}

		// -- Triggers

		@Override
		public Builder<E> withBeforeInsertTrigger(Consumer<E> trigger) {
			return (Builder<E>) super.withBeforeInsertTrigger(trigger);
		}

		@Override
		public Builder<E> withAfterInsertTrigger(Consumer<E> trigger) {
			return (Builder<E>) super.withAfterInsertTrigger(trigger);
		}

		@Override
		public Builder<E> withBeforeDeleteTrigger(Consumer<E> trigger) {
			return (Builder<E>) super.withBeforeDeleteTrigger(trigger);
		}

		@Override
		public Builder<E> withAfterDeleteTrigger(Consumer<E> trigger) {
			return (Builder<E>) super.withAfterDeleteTrigger(trigger);
		}

		//-- Content

		@Override
		public Builder<E> withCapacity(int capacity) {
			return (Builder<E>) super.withCapacity(capacity);
		}

		@Override
		public Builder<E> withContent(Collection<? extends E> elements) {
			return (Builder<E>) super.withContent(elements);
		}

		@Override
		public Builder<E> withContent(E... elements) {
			return (Builder<E>) super.withContent(elements);
		}

		@Override
		public Builder<E> withMaxSize(int maxSize) {
			return (Builder<E>) super.withMaxSize(maxSize);
		}

		//-- Element key

		@Override
		public Builder<E> withElemCount(boolean count) {
			return (Builder<E>) super.withElemCount(count);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that a {@link KeySet} always has an element set, so this call is not necessary.
		 */
		@Override
		public Builder<E> withElemSet() {
			return (Builder<E>) super.withElemSet();
		}

		@Override
		public Builder<E> withOrderByElem(boolean orderBy) {
			return (Builder<E>) super.withOrderByElem(orderBy);
		}

		@Override
		public Builder<E> withElemNull(boolean allowNull) {
			return (Builder<E>) super.withElemNull(allowNull);
		}

		@Override
		public Builder<E> withElemSort(boolean sort) {
			return (Builder<E>) super.withElemSort(sort);
		}

		@Override
		public Builder<E> withElemSort(Comparator<? super E> comparator) {
			return (Builder<E>) super.withElemSort(comparator);
		}

		@Override
		public Builder<E> withElemSort(Comparator<? super E> comparator, boolean sortNullsFirst) {
			return (Builder<E>) super.withElemSort(comparator, sortNullsFirst);
		}

		@Override
		public Builder<E> withPrimaryElem() {
			return (Builder<E>) super.withPrimaryElem();
		}

		@Override
		public Builder<E> withUniqueElem() {
			return (Builder<E>) super.withUniqueElem();
		}
	}

	/**
	 * Protected constructor used by builder or derived collections.
	 */
	protected KeySet() {
	}

	/**
	 * @return builder to use in extending classes
	 */
	@Override
	protected Builder<E> getBuilder() {
		return new Builder<>(this);
	}

	@Override
	public KeySet<E> copy() {
		return (KeySet<E>) super.copy();
	}

	@Override
	public KeySet<E> crop() {
		return (KeySet<E>) super.crop();
	}

	@Override
	public KeySet<E> getAll(E elem) {
		return (KeySet<E>) super.getAll(elem);
	}

}
