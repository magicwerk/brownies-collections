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
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.magicwerk.brownies.collections.KeyCollectionImpl.BuilderImpl;

/**
 * Key1List implements a key list with 1 key.
 * This key can be accessed fast.
 * It can provide fast access to its elements like a Set.
 * The elements allowed in the list can be constraint (null/duplicate values).
 *
 * @author Thomas Mauch
 *
 * @param <E> type of elements stored in the list
 * @param <K> type of key
 */
public class Key1List<E, K> extends KeyListImpl<E> {

	/**
	 * Builder to construct Key1List instances.
	 */
	public static class Builder<E, K> extends BuilderImpl<E> {
		/**
		 * Default constructor.
		 */
		public Builder() {
			this(null);
		}

		/**
		 * Private constructor used if extending Key1List.
		 *
		 * @param keyList	key list
		 */
		Builder(Key1List<E, K> keyList) {
			this.keyList = keyList;
			initKeyMapBuilder(1);
		}

		// -- Constraint

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withNull(boolean allowNull) {
			return (Builder<E, K>) super.withNull(allowNull);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withConstraint(Predicate<E> constraint) {
			return (Builder<E, K>) super.withConstraint(constraint);
		}

		// -- Triggers

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withBeforeInsertTrigger(Consumer<E> trigger) {
			return (Builder<E, K>) super.withBeforeInsertTrigger(trigger);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withAfterInsertTrigger(Consumer<E> trigger) {
			return (Builder<E, K>) super.withAfterInsertTrigger(trigger);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withBeforeDeleteTrigger(Consumer<E> trigger) {
			return (Builder<E, K>) super.withBeforeDeleteTrigger(trigger);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withAfterDeleteTrigger(Consumer<E> trigger) {
			return (Builder<E, K>) super.withAfterDeleteTrigger(trigger);
		}

		//-- Content

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withCapacity(int capacity) {
			return (Builder<E, K>) super.withCapacity(capacity);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withContent(Collection<? extends E> elements) {
			return (Builder<E, K>) super.withContent(elements);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withContent(E... elements) {
			return (Builder<E, K>) super.withContent(elements);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withMaxSize(int maxSize) {
			return (Builder<E, K>) super.withMaxSize(maxSize);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withWindowSize(int maxSize) {
			return (Builder<E, K>) super.withWindowSize(maxSize);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withListBig(boolean bigList) {
			return (Builder<E, K>) super.withListBig(bigList);
		}

		//-- Element key

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withElemSet() {
			return (Builder<E, K>) super.withElemSet();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withOrderByElem(boolean orderBy) {
			return (Builder<E, K>) super.withOrderByElem(orderBy);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withElemNull(boolean allowNull) {
			return (Builder<E, K>) super.withElemNull(allowNull);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withElemDuplicates(boolean allowDuplicates) {
			return (Builder<E, K>) super.withElemDuplicates(allowDuplicates);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
			return (Builder<E, K>) super.withElemDuplicates(allowDuplicates, allowDuplicatesNull);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withElemSort(boolean sort) {
			return (Builder<E, K>) super.withElemSort(sort);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withElemSort(Comparator<? super E> comparator) {
			return (Builder<E, K>) super.withElemSort(comparator);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withElemSort(Comparator<? super E> comparator, boolean sortNullsFirst) {
			return (Builder<E, K>) super.withElemSort(comparator, sortNullsFirst);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withPrimaryElem() {
			return (Builder<E, K>) super.withPrimaryElem();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withUniqueElem() {
			return (Builder<E, K>) super.withUniqueElem();
		}

		// -- Key1

		/**
		 * Add key map.
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
		@SuppressWarnings("unchecked")
		public Builder<E, K> withKey1Map(Function<? super E, K> mapper) {
			return (Builder<E, K>) super.withKeyMap(1, mapper);
		}

		/**
		 * Specify this key to be a primary key.
		 * This is identical to calling {@code withKey1Map(mapper), withKey1Null(false), and withKey1Duplicates(false)}.
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
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
		@SuppressWarnings("unchecked")
		public Builder<E, K> withUniqueKey1Map(Function<? super E, K> mapper) {
			return (Builder<E, K>) super.withUniqueKeyMap(1, mapper);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withOrderByKey1(boolean orderBy) {
			return (Builder<E, K>) super.withOrderByKey1(orderBy);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withOrderByKey1(Class<?> type) {
			return (Builder<E, K>) super.withOrderByKey1(type);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withKey1Null(boolean allowNull) {
			return (Builder<E, K>) super.withKey1Null(allowNull);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withKey1Duplicates(boolean allowDuplicates) {
			return (Builder<E, K>) super.withKey1Duplicates(allowDuplicates);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withKey1Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
			return (Builder<E, K>) super.withKey1Duplicates(allowDuplicates, allowDuplicatesNull);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Builder<E, K> withKey1Sort(boolean sort) {
			return (Builder<E, K>) super.withKey1Sort(sort);
		}

		/**
		 * Set comparator to use for sorting the key map.
		 * Note that this does not automatically sort the list itself, call a withOrderBy method for this.
		 *
		 * @param comparator    comparator to use for sorting
		 * @return              this (fluent interface)
		 */
		@SuppressWarnings("unchecked")
		public Builder<E, K> withKey1Sort(Comparator<? super K> comparator) {
			return (Builder<E, K>) super.withKeySort(1, comparator);
		}

		/**
		 * Set comparator to use for sorting the key map.
		 * Note that this does not automatically sort the list itself, call a withOrderBy method for this.
		 *
		 * @param comparator            comparator to use for sorting
		 * @param sortNullsFirst   		true if null will be sorted first, false for last
		 * @return                      this (fluent interface)
		 */
		@SuppressWarnings("unchecked")
		public Builder<E, K> withKey1Sort(Comparator<? super K> comparator, boolean sortNullsFirst) {
			return (Builder<E, K>) super.withKeySort(1, comparator, sortNullsFirst);
		}

		/**
		 * @return created list
		 */
		@SuppressWarnings("unchecked")
		public Key1List<E, K> build() {
			if (keyColl == null) {
				keyColl = new KeyCollectionImpl<E>();
			}
			build(keyColl, true);
			if (keyList == null) {
				keyList = new Key1List<E, K>();
			}
			init(keyColl, keyList);
			return (Key1List<E, K>) keyList;
		}
	}

	/**
	 * Protected constructor used by builder or derived collections.
	 */
	protected Key1List() {
	}

	/**
	 * @return builder to use in extending classes
	 */
	protected Builder<E, K> getBuilder() {
		return new Builder<E, K>(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Key1List<E, K> copy() {
		if (this instanceof ReadOnlyKey1List) {
			Key1List<E, K> list = new Key1List<>(false, null);
			list.initCopy(this);
			return list;
		} else {
			return (Key1List<E, K>) super.clone();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Key1List<E, K> clone() {
		if (this instanceof ReadOnlyKey1List) {
			return this;
		} else {
			return (Key1List<E, K>) super.clone();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Key1List<E, K> crop() {
		if (this instanceof ReadOnlyKey1List) {
			Key1List<E, K> list = new Key1List<>(false, null);
			list.initCrop(this);
			return list;
		} else {
			return (Key1List<E, K>) super.crop();
		}
	}

	//-- Element methods

	@Override
	public IList<E> getAll(E elem) {
		return super.getAll(elem);
	}

	@Override
	public int count(E elem) {
		return super.count(elem);
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

	//-- Key methods

	/**
	 * Returns mapper for key map.
	 *
	 * @return mapper for key map
	 */
	@SuppressWarnings("unchecked")
	public Function<E, K> getKey1Mapper() {
		return (Function<E, K>) getKeyMapper(1);
	}

	/**
	 * Returns a map view to the key map.
	 * The collection can be modified through the map as long as the constraint are not violated.
	 * Note however that operations put() and remove() will be slow if the list is not sorted by the key and the element must therefore be searched. 
	 * The collections returned by the methods entrySet(), keySet(), and values() are immutable however.
	 *
	 * @return map view to key map
	 * @throws IllegalArgumentException if the key map cannot be viewed as Map
	 */
	public Map<K, E> asMap1() {
		return new KeyCollectionAsMap<K, E>(this, 1, false);
	}

	/**
	 * Returns index of first element in list with specified key.
	 *
	 * @param key	key
	 * @return		index of first element, -1 if no such element exists
	 */
	public int indexOfKey1(K key) {
		return indexOfKey(1, key);
	}

	/**
	 * Checks whether an element with specified key exists.
	 *
	 * @param key	key
	 * @return		true if element with specified key exists, otherwise false
	 */
	public boolean containsKey1(K key) {
		return containsKey(1, key);
	}

	/**
	 * Returns element with specified key.
	 * If there are several elements with the same key, the one added first will be returned.
	 *
	 * @param key	key
	 * @return		element with specified key or null
	 */
	public E getByKey1(K key) {
		return getByKey(1, key);
	}

	/**
	 * Returns all elements with specified key.
	 *
	 * @param key	key
	 * @return		all elements with specified key (never null)
	 */
	public IList<E> getAllByKey1(K key) {
		return getAllByKey(1, key);
	}

	/**
	 * Returns the number of elements with specified key.
	 *
	 * @param key	key
	 * @return		number of elements with specified key
	 */
	public int getCountByKey1(K key) {
		return getCountByKey(1, key);
	}

	/**
	 * Removes element with specified key.
	 * If there are several elements with the same key, the one added first will be removed.
	 *
	 * @param key	key
	 * @return		element with specified key or null
	 */
	public E removeByKey1(K key) {
		return removeByKey(1, key);
	}

	/**
	 * Removes all elements with specified key.
	 *
	 * @param key	key
	 * @return		removed elements with specified key (never null)
	 */
	public IList<E> removeAllByKey1(K key) {
		return removeAllByKey(1, key);
	}

	/**
	 * Returns list containing all keys in element order.
	 *
	 * @return 			list containing all keys
	 */
	@SuppressWarnings("unchecked")
	public IList<K> getAllKeys1() {
		return (GapList<K>) getAllKeys(1);
	}

	/**
	 * Returns all distinct keys in the same order as in the key map.
	 *
	 * @return		distinct keys
	 */
	@SuppressWarnings("unchecked")
	public Set<K> getDistinctKeys1() {
		return (Set<K>) getDistinctKeys(1);
	}

	/**
	 * Adds element by key.
	 * If there is no such element, the element is added.
	 * If there is such an element, the element is replaced.
	 * So said simply, it is a shortcut for the following code:
	 * <pre>
	 * removeByKey1(elem.getKey1());
	 * add(elem);
	 * </pre>
	 * However the method is atomic in the sense that all or none operations are executed.
	 * So if there is already such an element, but adding the new one fails due to a constraint violation,
	 * the old element remains in the list.
	 *
	 * @param elem	element
	 * @return		element with the same key which has been replaced or null otherwise
	 */
	public E putByKey1(E elem) {
		return putByKey(1, elem, true);
	}

	/**
	 * Adds or replaces element by key.
	 * If there is no such element, the element is added.
	 * If there is such an element, the element is left unchanged.
	 * So said simply, it is a shortcut for the following code:
	 * <pre>
	 * if (!containsKey1(elem.getKey1())) {
	 *   add(elem);
	 * }
	 * </pre>
	 * However the method is atomic in the sense that all or none operations are executed.
	 * So if there is already such an element, but adding the new one fails due to a constraint violation,
	 * the old element remains in the list.
	 *
	 * @param elem	element
	 * @return		element with the same key which has been left unchanged or null otherwise
	 */
	public E putIfAbsentByKey1(E elem) {
		return putByKey(1, elem, false);
	}

	/**
	 * Invalidate key value of element.
	 * You must call an invalidate method if an element's key value has changed after adding it to the collection.
	 *
	 * @param oldKey	old key value
	 * @param newKey	new key value
	 * @param elem		element to invalidate (can be null if there are no duplicates with this key)
	 */
	public void invalidateKey1(K oldKey, K newKey, E elem) {
		invalidateKey(1, oldKey, newKey, elem);
	}

	// --- ImmutableKey1List ---

	@Override
	public boolean isReadOnly() {
		return this instanceof ReadOnlyKey1List;
	}

	@Override
	public Key1List<E, K> unmodifiableList() {
		if (this instanceof ReadOnlyKey1List) {
			return this;
		} else {
			return new ReadOnlyKey1List<E, K>(this);
		}
	}

	@Override
	public Key1List<E, K> immutableList() {
		if (this instanceof ReadOnlyKey1List) {
			return this;
		} else {
			return new ReadOnlyKey1List<E, K>(copy());
		}
	}

	protected Key1List(boolean copy, Key1List<E, K> that) {
		if (copy) {
			doAssign(that);
		}
	}

	/**
	 * A read-only version of {@link Key1List}.
	 * It is used to implement both unmodifiable and immutable lists.
	 * Note that the client cannot change the list, but the content may change if the underlying list is changed.
	 */
	protected static class ReadOnlyKey1List<E, K> extends Key1List<E, K> {

		/** UID for serialization */
		private static final long serialVersionUID = -1352274047348922584L;

		/**
		 * Private constructor used internally.
		 *
		 * @param that  list to create an immutable view of
		 */
		protected ReadOnlyKey1List(Key1List<E, K> that) {
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
