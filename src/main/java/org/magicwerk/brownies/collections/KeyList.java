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

import org.magicwerk.brownies.collections.KeyCollectionImpl.BuilderImpl;

/**
 * KeyList implements a list.
 * It can provide fast access to its elements like a Set.
 * The elements allowed in the list can be constraint (null/duplicate values).
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @param <E> type of elements stored in the list
 */
@SuppressWarnings("serial")
public class KeyList<E> extends KeyListImpl<E> {

	/**
	 * Builder to construct KeyList instances.
	 */
	public static class Builder<E> extends BuilderImpl<E> {
		/**
		 * Default constructor.
		 */
		public Builder() {
			this(null);
		}

		/**
		 * Private constructor used if extending KeyList.
		 *
		 * @param keyList	key list
		 */
		Builder(KeyList<E> keyList) {
			this.keyList = keyList;
			initKeyMapBuilder(0);
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

		@Override
		public Builder<E> withWindowSize(int maxSize) {
			return (Builder<E>) super.withWindowSize(maxSize);
		}

		@Override
		public Builder<E> withListBig(boolean bigList) {
			return (Builder<E>) super.withListBig(bigList);
		}

		//-- Element key

		@Override
		public Builder<E> withListType(Class<?> type) {
			return (Builder<E>) super.withListType(type);
		}

		@Override
		public Builder<E> withElemSet() {
			return (Builder<E>) super.withElemSet();
		}

		@Override
		public Builder<E> withOrderByElem(boolean orderBy) {
			return (Builder<E>) super.withOrderByElem(orderBy);
		}

		@Override
		public Builder<E> withOrderByElem(Class<?> type) {
			return (Builder<E>) super.withOrderByElem(type);
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

		/**
		 * Create collection with specified options.
		 *
		 * @return created collection
		 */
		public KeyList<E> build() {
			if (keyColl == null) {
				keyColl = new KeyCollectionImpl<E>();
			}
			build(keyColl, true);
			if (keyList == null) {
				keyList = new KeyList<E>();
			}
			init(keyColl, keyList);
			return (KeyList<E>) keyList;
		}
	}

	/**
	 * Protected constructor used by builder or derived collections.
	 */
	protected KeyList() {
	}

	/**
	 * @return builder to use in extending classes
	 */
	protected Builder<E> getBuilder() {
		return new Builder<E>(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public KeyList<E> copy() {
		return (KeyList<E>) clone();
	}

	@Override
	public Object clone() {
		if (this instanceof ImmutableKeyList) {
			KeyList<E> list = new KeyList<>(false, null);
			list.initCopy(this);
			return list;
		} else {
			return super.clone();
		}
	}

	@Override
	public KeyList<E> crop() {
		if (this instanceof ImmutableKeyList) {
			KeyList<E> list = new KeyList<>(false, null);
			list.initCrop(this);
			return list;
		} else {
			return (KeyList<E>) super.crop();
		}
	}

	//-- Element methods

	@Override
	public IList<E> getAll(E elem) {
		return super.getAll(elem);
	}

	@Override
	public int getCount(E elem) {
		return super.getCount(elem);
	}

	@Override
	public IList<E> removeAll(E elem) {
		return super.removeAll(elem);
	}

	@Override
	public Set<E> getDistinct() {
		return super.getDistinct();
	}

	@Override
	public E put(E elem) {
		return super.put(elem);
	}

	// --- ImmutableKey1List ---

	@Override
	public KeyList<E> unmodifiableList() {
		if (this instanceof ImmutableKeyList) {
			return this;
		} else {
			return new ImmutableKeyList<E>(this);
		}
	}

	protected KeyList(boolean copy, KeyList<E> that) {
		if (copy) {
			doAssign(that);
		}
	}

	/**
	 * An immutable version of a Key1List.
	 * Note that the client cannot change the list, but the content may change if the underlying list is changed.
	 */
	protected static class ImmutableKeyList<E> extends KeyList<E> {

		/** UID for serialization */
		private static final long serialVersionUID = -1352274047348922584L;

		/**
		 * Private constructor used internally.
		 *
		 * @param that  list to create an immutable view of
		 */
		protected ImmutableKeyList(KeyList<E> that) {
			super(true, that);
		}

		@Override
		protected void doEnsureCapacity(int capacity) {
			error();
		}

		@Override
		protected boolean doAdd(int index, E elem) {
			error();
			return false;
		}

		@Override
		protected E doSet(int index, E elem) {
			error();
			return null;
		}

		@Override
		protected E doReSet(int index, E elem) {
			error();
			return null;
		}

		@Override
		protected E doRemove(int index) {
			error();
			return null;
		}

		@Override
		protected void doRemoveAll(int index, int len) {
			error();
		}

		@Override
		protected void doClear() {
			error();
		}

		@Override
		protected void doModify() {
			error();
		}

		@Override
		protected E removeByKey(int keyIndex, Object key) {
			error();
			return null;
		}

		@Override
		protected IList<E> removeAllByKey(int keyIndex, Object key) {
			error();
			return null;
		}

		@Override
		protected E putByKey(int keyIndex, E elem, boolean replace) {
			error();
			return null;
		}

		@Override
		protected void invalidateKey(int keyIndex, Object oldKey, Object newKey, E elem) {
			error();
		}

		/**
		 * Throw exception if an attempt is made to change an immutable list.
		 */
		private void error() {
			throw new UnsupportedOperationException("list is immutable");
		}
	}

}
