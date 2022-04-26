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
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * KeyCollection implements a collection.
 * It can provide fast access to its elements like a Set.
 * The elements allowed in the collection can be constraint (null/duplicate values).
 *
 * @author Thomas Mauch
 *
 * @see Key1List
 * @param <E> type of elements stored in the collection
 */
@SuppressWarnings("serial")
public class KeyCollection<E> extends KeyCollectionImpl<E> {

	/**
	 * Builder to construct KeyCollection instances.
	 */
	public static class Builder<E> extends BuilderImpl<E> {
		/**
		 * Default constructor.
		 */
		public Builder() {
			this(null);
		}

		/**
		 * Private constructor used if extending KeyCollection.
		 *
		 * @param keyColl	key collection
		 */
		Builder(KeyCollection<E> keyColl) {
			this.keyColl = keyColl;
			initKeyMapBuilder(0);
		}

		/**
		 * @return created collection
		 */
		public KeyCollection<E> build() {
			if (keyColl == null) {
				keyColl = new KeyCollection<>();
			}
			build(keyColl, false);
			init(keyColl);
			return (KeyCollection<E>) keyColl;
		}

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
		public Builder<E> withSetBehavior(boolean setBehavior) {
			return (Builder<E>) super.withSetBehavior(setBehavior);
		}

		@Override
		public Builder<E> withElemCount(boolean count) {
			return (Builder<E>) super.withElemCount(count);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that a {@link KeyCollection} always has an element set, so this call is not necessary.
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
		public Builder<E> withElemDuplicates(boolean allowDuplicates) {
			return (Builder<E>) super.withElemDuplicates(allowDuplicates);
		}

		@Override
		public Builder<E> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
			return (Builder<E>) super.withElemDuplicates(allowDuplicates, allowDuplicatesNull);
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
	protected KeyCollection() {
	}

	/**
	 * @return builder to use in extending classes
	 */
	protected Builder<E> getBuilder() {
		return new Builder<>(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public KeyCollection<E> copy() {
		return (KeyCollection<E>) super.copy();
	}

	@SuppressWarnings("unchecked")
	@Override
	public KeyCollection<E> crop() {
		return (KeyCollection<E>) super.crop();
	}

	//-- Element methods

	@Override
	public KeyCollection<E> getAll(E elem) {
		return (KeyCollection<E>) super.getAll(elem);
	}

	@Override
	public KeyCollection<E> removeAll(E elem) {
		return (KeyCollection<E>) super.removeAll(elem);
	}

	@Override
	public E put(E elem) {
		return super.put(elem);
	}

	@Override
	public void invalidate(E elem) {
		super.invalidate(elem);
	}

}
