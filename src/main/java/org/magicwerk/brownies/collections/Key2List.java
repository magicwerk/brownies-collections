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
 * Key2List implements a key list with 2 keys.
 * These keys can be accessed fast.
 * It can provide fast access to its elements like a Set.
 * The elements allowed in the list can be constraint (null/duplicate values).
 *
 * @author Thomas Mauch
 *
 * @param <E> type of elements stored in the list
 * @param <K1> type of first key
 * @param <K2> type of second key
 */
@SuppressWarnings("serial")
public class Key2List<E, K1, K2> extends KeyListImpl<E> {

	/**
	 * Builder to construct Key2List instances.
	 */
	public static class Builder<E, K1, K2> extends BuilderImpl<E> {
		/**
		 * Default constructor.
		 */
		public Builder() {
			this(null);
		}

		/**
		 * Private constructor used if extending Key2List.
		 *
		 * @param keyList	key list
		 */
		Builder(Key2List<E, K1, K2> keyList) {
			this.keyList = keyList;
			initKeyMapBuilder(2);
		}

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

		@Override
		public Builder<E, K1, K2> withContent(E... elements) {
			return (Builder<E, K1, K2>) super.withContent(elements);
		}

		@Override
		public Builder<E, K1, K2> withMaxSize(int maxSize) {
			return (Builder<E, K1, K2>) super.withMaxSize(maxSize);
		}

		@Override
		public Builder<E, K1, K2> withWindowSize(int maxSize) {
			return (Builder<E, K1, K2>) super.withWindowSize(maxSize);
		}

		@Override
		public Builder<E, K1, K2> withListBig(boolean bigList) {
			return (Builder<E, K1, K2>) super.withListBig(bigList);
		}

		//-- Element key

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
		public Builder<E, K1, K2> withElemDuplicates(boolean allowDuplicates) {
			return (Builder<E, K1, K2>) super.withElemDuplicates(allowDuplicates);
		}

		@Override
		public Builder<E, K1, K2> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
			return (Builder<E, K1, K2>) super.withElemDuplicates(allowDuplicates, allowDuplicatesNull);
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

		@Override
		public Builder<E, K1, K2> withPrimaryElem() {
			return (Builder<E, K1, K2>) super.withPrimaryElem();
		}

		@Override
		public Builder<E, K1, K2> withUniqueElem() {
			return (Builder<E, K1, K2>) super.withUniqueElem();
		}

		// -- Key1

		/**
		 * Add key map.
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
		public Builder<E, K1, K2> withKey1Map(Function<? super E, K1> mapper) {
			return (Builder<E, K1, K2>) super.withKeyMap(1, mapper);
		}

		/**
		 * Specify this key to be a primary key.
		 * This is identical to calling
		 * withKey1Map(mapper), withKey1Null(false), and withKey1Duplicates(false).
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
		public Builder<E, K1, K2> withPrimaryKey1Map(Function<? super E, K1> mapper) {
			return (Builder<E, K1, K2>) super.withPrimaryKeyMap(1, mapper);
		}

		/**
		 * Specify this key to be a unique key.
		 * This is identical to calling
		 * withKey1Map(mapper), withKey1Null(true), and withKey1Duplicates(false, true).
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
		public Builder<E, K1, K2> withUniqueKey1Map(Function<? super E, K1> mapper) {
			return (Builder<E, K1, K2>) super.withUniqueKeyMap(1, mapper);
		}

		@Override
		public Builder<E, K1, K2> withOrderByKey1(boolean orderBy) {
			return (Builder<E, K1, K2>) super.withOrderByKey1(orderBy);
		}

		@Override
		public Builder<E, K1, K2> withOrderByKey1(Class<?> type) {
			return (Builder<E, K1, K2>) super.withOrderByKey1(type);
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
		 * Note that this does not automatically sort the list itself, call a withOrderBy method for this.
		 *
		 * @param comparator    comparator to use for sorting
		 * @return              this (fluent interface)
		 */
		public Builder<E, K1, K2> withKey1Sort(Comparator<? super K1> comparator) {
			return (Builder<E, K1, K2>) super.withKeySort(1, comparator);
		}

		/**
		 * Set comparator to use for sorting the key map.
		 * Note that this does not automatically sort the list itself, call a withOrderBy method for this.
		 *
		 * @param comparator            comparator to use for sorting
		 * @param sortNullsFirst   		true if null will be sorted first, false for last
		 * @return                      this (fluent interface)
		 */
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
		public Builder<E, K1, K2> withKey2Map(Function<? super E, K2> mapper) {
			return (Builder<E, K1, K2>) super.withKeyMap(2, mapper);
		}

		/**
		 * Specify this key to be a primary key.
		 * This is identical to calling
		 * withKey2Map(mapper), withKey2Null(false), and withKey2Duplicates(false).
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
		public Builder<E, K1, K2> withPrimaryKey2Map(Function<? super E, K2> mapper) {
			return (Builder<E, K1, K2>) super.withPrimaryKeyMap(2, mapper);
		}

		/**
		* Specify this key to be a unique key.
		* This is identical to calling
		* withKey2Map(mapper), withKey2Null(true), and withKey2Duplicates(false, true).
		*
		* @param mapper	mapper to use
		* @return			this (fluent interface)
		*/
		public Builder<E, K1, K2> withUniqueKey2Map(Function<? super E, K2> mapper) {
			return (Builder<E, K1, K2>) super.withUniqueKeyMap(2, mapper);
		}

		@Override
		public Builder<E, K1, K2> withOrderByKey2(boolean orderBy) {
			return (Builder<E, K1, K2>) super.withOrderByKey2(orderBy);
		}

		@Override
		public Builder<E, K1, K2> withOrderByKey2(Class<?> type) {
			return (Builder<E, K1, K2>) super.withOrderByKey2(type);
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
		 * Note that this does not automatically sort the list itself, call a withOrderBy method for this.
		 *
		 * @param comparator    comparator to use for sorting
		 * @return              this (fluent interface)
		 */
		public Builder<E, K1, K2> withKey2Sort(Comparator<? super K2> comparator) {
			return (Builder<E, K1, K2>) super.withKeySort(2, comparator);
		}

		/**
		 * Set comparator to use for sorting the key map.
		 * Note that this does not automatically sort the list itself, call a withOrderBy method for this.
		 *
		 * @param comparator            comparator to use for sorting
		 * @param sortNullsFirst   		true if null will be sorted first, false for last
		 * @return                      this (fluent interface)
		 */
		public Builder<E, K1, K2> withKey2Sort(Comparator<? super K2> comparator, boolean sortNullsFirst) {
			return (Builder<E, K1, K2>) super.withKeySort(2, comparator, sortNullsFirst);
		}

		/**
		 * @return created list
		 */
		public Key2List<E, K1, K2> build() {
			if (keyColl == null) {
				keyColl = new KeyCollectionImpl<E>();
			}
			build(keyColl, true);
			if (keyList == null) {
				keyList = new Key2List<E, K1, K2>();
			}
			init(keyColl, keyList);
			return (Key2List<E, K1, K2>) keyList;
		}
	}

	/**
	 * Protected constructor used by builder or derived collections.
	 */
	protected Key2List() {
	}

	/**
	 * @return builder to use in extending classes
	 */
	protected Builder<E, K1, K2> getBuilder() {
		return new Builder<E, K1, K2>(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Key2List<E, K1, K2> copy() {
		return (Key2List<E, K1, K2>) clone();
	}

	@Override
	public Object clone() {
		if (this instanceof ImmutableKey2List) {
			Key2List<E, K1, K2> list = new Key2List<>(false, null);
			list.initCopy(this);
			return list;
		} else {
			return super.clone();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Key2List<E, K1, K2> crop() {
		if (this instanceof ImmutableKey2List) {
			Key2List<E, K1, K2> list = new Key2List<>(false, null);
			list.initCrop(this);
			return list;
		} else {
			return (Key2List<E, K1, K2>) super.crop();
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

	//-- Key1 methods

	/**
	 * Returns mapper for key map.
	 *
	 * @return mapper for key map
	 */
	public Function<E, K1> getKey1Mapper() {
		return (Function<E, K1>) getKeyMapper(1);
	}

	/**
	 * Returns a map view to the key map.
	 * The collection can be modified through the map as long as the constraint are not violated.
	 * The collections returned by the methods entrySet(), keySet(), and values() are immutable however.
	 *
	 * @return map view to key map
	 * @throws IllegalArgumentException if the key map cannot be viewed as Map
	 */
	public Map<K1, E> asMap1() {
		return new KeyCollectionAsMap<K1, E>(this, 1, false);
	}

	/**
	 * Returns index of first element in list with specified key.
	 *
	 * @param key	key
	 * @return		index of first element, -1 if no such element exists
	 */
	public int indexOfKey1(K1 key) {
		return indexOfKey(1, key);
	}

	/**
	 * Checks whether an element with specified key exists.
	 *
	 * @param key	key
	 * @return		true if element with specified key exists, otherwise false
	 */
	public boolean containsKey1(K1 key) {
		return containsKey(1, key);
	}

	/**
	 * Returns element with specified key.
	 * If there are several elements with the same key, the one added first will be returned.
	 *
	 * @param key	key
	 * @return		element with specified key or null
	 */
	public E getByKey1(K1 key) {
		return getByKey(1, key);
	}

	/**
	 * Returns all elements with specified key.
	 *
	 * @param key	key
	 * @return		all elements with specified key (never null)
	 */
	public IList<E> getAllByKey1(K1 key) {
		return getAllByKey(1, key);
	}

	/**
	 * Returns the number of elements with specified key.
	 *
	 * @param key	key
	 * @return		number of elements with specified key
	 */
	public int getCountByKey1(K1 key) {
		return getCountByKey(1, key);
	}

	/**
	 * Removes element with specified key.
	 * If there are several elements with the same key, the one added first will be removed.
	 *
	 * @param key	key
	 * @return		element with specified key or null
	 */
	public E removeByKey1(K1 key) {
		return removeByKey(1, key);
	}

	/**
	 * Removes all elements with specified key.
	 *
	 * @param key	key
	 * @return		removed elements with specified key (never null)
	 */
	public IList<E> removeAllByKey1(K1 key) {
		return removeAllByKey(1, key);
	}

	/**
	 * Returns list containing all keys in element order.
	 *
	 * @return 			list containing all keys
	 */
	@SuppressWarnings("unchecked")
	public IList<K1> getAllKeys1() {
		return (IList<K1>) getAllKeys(1);
	}

	/**
	 * Returns all distinct keys in the same order as in the key map.
	 *
	 * @return		distinct keys
	 */
	@SuppressWarnings("unchecked")
	public Set<K1> getDistinctKeys1() {
		return (Set<K1>) getDistinctKeys(1);
	}

	/**
	 * Adds or replaces element by key.
	 * If there is no such element, the element is added.
	 * If there is such an element, the element is replaced.
	 * So said simply, it is a shortcut for the following code:
	 * <pre>
	 * if (containsKey1(elem)) {
	 *   removeByKey1(elem);
	 * }
	 * add(elem);
	 * </pre>
	 * However the method is atomic in the sense that all or none operations are executed.
	 * So if there is already such an element, but adding the new one fails due to a constraint violation,
	 * the old element remains in the list.
	 *
	 * @param elem	element
	 * @return		element which has been replaced or null otherwise
	 */
	public E putByKey1(E elem) {
		return putByKey(1, elem, true);
	}

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
	public void invalidateKey1(K1 oldKey, K1 newKey, E elem) {
		invalidateKey(1, oldKey, newKey, elem);
	}

	//-- Key2 methods

	/**
	 * Returns mapper for key map.
	 *
	 * @return mapper for key map
	 */
	public Function<E, K2> getKey2Mapper() {
		return (Function<E, K2>) getKeyMapper(2);
	}

	/**
	 * Returns a map view to the key map.
	 * The collection can be modified through the map as long as the constraint are not violated.
	 * The collections returned by the methods entrySet(), keySet(), and values() are immutable however.
	 *
	 * @return map view to key map
	 * @throws IllegalArgumentException if the key map cannot be viewed as Map
	 */
	public Map<K2, E> asMap2() {
		return new KeyCollectionAsMap<K2, E>(this.keyColl, 2, false);
	}

	/**
	 * Returns index of first element in list with specified key.
	 *
	 * @param key	key
	 * @return		index of first element, -1 if no such element exists
	 */
	public int indexOfKey2(K2 key) {
		return indexOfKey(2, key);
	}

	/**
	 * Checks whether an element with specified key exists.
	 *
	 * @param key	key
	 * @return		true if element with specified key exists, otherwise false
	 */
	public boolean containsKey2(K2 key) {
		return containsKey(2, key);
	}

	/**
	 * Returns element with specified key.
	 * If there are several elements with the same key, the one added first will be returned.
	 *
	 * @param key	key
	 * @return		element with specified key or null
	 */
	public E getByKey2(K2 key) {
		return getByKey(2, key);
	}

	/**
	 * Returns all elements with specified key.
	 *
	 * @param key	key
	 * @return		all elements with specified key (never null)
	 */
	public IList<E> getAllByKey2(K2 key) {
		return getAllByKey(2, key);
	}

	/**
	 * Returns the number of elements with specified key.
	 *
	 * @param key	key
	 * @return		number of elements with specified key
	 */
	public int getCountByKey2(K2 key) {
		return getCountByKey(2, key);
	}

	/**
	 * Removes element with specified key.
	 * If there are several elements with the same key, the one added first will be removed.
	 *
	 * @param key	key
	 * @return		element with specified key or null
	 */
	public E removeByKey2(K2 key) {
		return removeByKey(2, key);
	}

	/**
	 * Removes all elements with specified key.
	 *
	 * @param key	key
	 * @return		removed elements with specified key (never null)
	 */
	public IList<E> removeAllByKey2(K2 key) {
		return removeAllByKey(2, key);
	}

	/**
	 * Returns list containing all keys in element order.
	 *
	 * @return 			list containing all keys
	 */
	@SuppressWarnings("unchecked")
	public IList<K2> getAllKeys2() {
		return (IList<K2>) getAllKeys(2);
	}

	/**
	 * Returns all distinct keys in the same order as in the key map.
	 *
	 * @return		distinct keys
	 */
	@SuppressWarnings("unchecked")
	public Set<K2> getDistinctKeys2() {
		return (Set<K2>) getDistinctKeys(2);
	}

	/**
	 * Adds or replaces element by key.
	 * If there is no such element, the element is added.
	 * If there is such an element, the element is replaced.
	 * So said simply, it is a shortcut for the following code:
	 * <pre>
	 * if (containsKey2(elem)) {
	 *   removeByKey2(elem);
	 * }
	 * add(elem);
	 * </pre>
	 * However the method is atomic in the sense that all or none operations are executed.
	 * So if there is already such an element, but adding the new one fails due to a constraint violation,
	 * the old element remains in the list.
	 *
	 * @param elem	element
	 * @return		element which has been replaced or null otherwise
	 */
	public E putByKey2(E elem) {
		return putByKey(2, elem, true);
	}

	public E putIfAbsentByKey2(E elem) {
		return putByKey(2, elem, false);
	}

	/**
	 * Invalidate key value of element.
	 * You must call an invalidate method if an element's key value has changed after adding it to the collection.
	 *
	 * @param oldKey	old key value
	 * @param newKey	new key value
	 * @param elem		element to invalidate (can be null if there are no duplicates with this key)
	 */
	public void invalidateKey2(K2 oldKey, K2 newKey, E elem) {
		invalidateKey(2, oldKey, newKey, elem);
	}

	// --- ImmutableKey2List ---

	@Override
	public Key2List<E, K1, K2> unmodifiableList() {
		if (this instanceof ImmutableKey2List) {
			return this;
		} else {
			return new ImmutableKey2List<E, K1, K2>(this);
		}
	}

	protected Key2List(boolean copy, Key2List<E, K1, K2> that) {
		if (copy) {
			doAssign(that);
		}
	}

	/**
	 * An immutable version of a Key1List.
	 * Note that the client cannot change the list, but the content may change if the underlying list is changed.
	 */
	protected static class ImmutableKey2List<E, K1, K2> extends Key2List<E, K1, K2> {

		/** UID for serialization */
		private static final long serialVersionUID = -1352274047348922584L;

		/**
		 * Private constructor used internally.
		 *
		 * @param that  list to create an immutable view of
		 */
		protected ImmutableKey2List(Key2List<E, K1, K2> that) {
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
