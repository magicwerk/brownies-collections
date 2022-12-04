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

/**
 * Key1Collection implements a collection with 1 key.
 * This key can be accessed fast.
 * It can provide fast access to its elements like a Set.
 * The elements allowed in the collection can be constraint (null/duplicate values).
 *
 * @author Thomas Mauch
 *
 * @see Key1List
 * @param <E> type of elements stored in the collection
 * @param <K> type of key
 */
@SuppressWarnings("serial")
public class Key1Collection<E, K> extends KeyCollectionImpl<E> {

	/**
	 * Builder to construct Key1Collection instances.
	 */
	public static class Builder<E, K> extends BuilderImpl<E> {
		/**
		 * Default constructor.
		 */
		public Builder() {
			this(null);
		}

		/**
		 * Private constructor used if extending Key1Collection.
		 *
		 * @param keyColl	key collection
		 */
		Builder(Key1Collection<E, K> keyColl) {
			this.keyColl = keyColl;
			initKeyMapBuilder(1);
		}

		/**
		 * @return created collection
		 */
		@SuppressWarnings("unchecked")
		public Key1Collection<E, K> build() {
			if (keyColl == null) {
				keyColl = new Key1Collection<>();
			}
			build(keyColl, false);
			init(keyColl);
			return (Key1Collection<E, K>) keyColl;
		}

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
		public Builder<E, K> withContent(E... elements) {
			return (Builder<E, K>) super.withContent(elements);
		}

		@Override
		public Builder<E, K> withMaxSize(int maxSize) {
			return (Builder<E, K>) super.withMaxSize(maxSize);
		}

		//-- Element key

		@Override
		public Builder<E, K> withSetBehavior(boolean setBehavior) {
			return (Builder<E, K>) super.withSetBehavior(setBehavior);
		}

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
		public Builder<E, K> withElemDuplicates(boolean allowDuplicates) {
			return (Builder<E, K>) super.withElemDuplicates(allowDuplicates);
		}

		@Override
		public Builder<E, K> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
			return (Builder<E, K>) super.withElemDuplicates(allowDuplicates, allowDuplicatesNull);
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

		@Override
		public Builder<E, K> withPrimaryElem() {
			return (Builder<E, K>) super.withPrimaryElem();
		}

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
		public Builder<E, K> withKey1Map(Function<? super E, K> mapper) {
			return (Builder<E, K>) withKeyMap(1, mapper);
		}

		/**
		 * Specify this key to be a primary key.
		 * This is identical to calling withKey1Map(mapper), withKey1Null(false), and withKey1Duplicates(false).
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
		public Builder<E, K> withPrimaryKey1Map(Function<? super E, K> mapper) {
			return (Builder<E, K>) super.withPrimaryKeyMap(1, mapper);
		}

		/**
		 * Specify this key to be a unique key.
		 * This is identical to calling withKey1Map(mapper), withKey1Null(true), and withKey1Duplicates(false, true).
		 *
		 * @param mapper	mapper to use
		 * @return			this (fluent interface)
		 */
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
		public Builder<E, K> withKey1Sort(Comparator<? super K> comparator, boolean sortNullsFirst) {
			return (Builder<E, K>) super.withKeySort(1, comparator, sortNullsFirst);
		}

	}

	/**
	 * Protected constructor used by builder or derived collections.
	 */
	protected Key1Collection() {
	}

	/**
	 * @return builder to use in extending classes
	 */
	protected Builder<E, K> getBuilder() {
		return new Builder<>(this);
	}

	@Override
	public Key1Collection<E, K> copy() {
		return (Key1Collection<E, K>) super.copy();
	}

	@Override
	public Key1Collection<E, K> crop() {
		return (Key1Collection<E, K>) super.crop();
	}

	//-- Element methods

	@Override
	public Key1Collection<E, K> getAll(E elem) {
		return (Key1Collection<E, K>) super.getAll(elem);
	}

	@Override
	public Key1Collection<E, K> removeAll(E elem) {
		return (Key1Collection<E, K>) super.removeAll(elem);
	}

	@Override
	public E put(E elem) {
		return super.put(elem);
	}

	@Override
	public void invalidate(E elem) {
		super.invalidate(elem);
	}

	//-- Key methods

	/**
	 * Returns mapper for key map.
	 *
	 * @return      	mapper for key map
	 */
	public Function<E, K> getKey1Mapper() {
		return (Function<E, K>) super.getKeyMapper(1);
	}

	/**
	 * Returns a map view to the key map.
	 * The collection can be modified through the map as long as the constraint are not violated.
	 * The collections returned by the methods entrySet(), keySet(), and values() are immutable however.
	 *
	 * @return map view to key map
	 * @throws IllegalArgumentException if the key map cannot be viewed as Map
	 */
	public Map<K, E> asMap1() {
		return new KeyCollectionAsMap<K, E>(this, 1, false);
	}

	/**
	 * Checks whether an element with specified key exists.
	 *
	 * @param key	key
	 * @return		true if element with specified key exists, otherwise false
	 */
	public boolean containsKey1(K key) {
		return super.containsKey(1, key);
	}

	/**
	 * Returns element with specified key.
	 * If there are several elements with the same key, the one added first will be returned.
	 *
	 * @param key	key
	 * @return		element with specified key or null
	 */
	public E getByKey1(K key) {
		return super.getByKey(1, key);
	}

	/**
	 * Returns all elements with specified key.
	 *
	 * @param key	key
	 * @return		all elements with specified key (never null)
	 */
	public Key1Collection<E, K> getAllByKey1(K key) {
		return (Key1Collection<E, K>) super.getAllByKey(1, key);
	}

	/**
	 * Returns the number of elements with specified key.
	 *
	 * @param key	key
	 * @return		number of elements with specified key
	 */
	public int getCountByKey1(K key) {
		return super.getCountByKey(1, key);
	}

	/**
	 * Removes element with specified key.
	 * If there are several elements with the same key, the one added first will be removed.
	 *
	 * @param key	key
	 * @return		element with specified key or null
	 */
	public E removeByKey1(K key) {
		return super.removeByKey(1, key);
	}

	/**
	 * Removes all elements with specified key.
	 *
	 * @param key	key
	 * @return		removed elements with specified key (never null)
	 * 
	 */
	public Key1Collection<E, K> removeAllByKey1(K key) {
		return (Key1Collection<E, K>) super.removeAllByKey(1, key);
	}

	/**
	 * Returns list containing all keys in element order.
	 *
	 * @return 			list containing all keys
	 */
	@SuppressWarnings("unchecked")
	public IList<K> getAllKeys1() {
		return (GapList<K>) super.getAllKeys(1);
	}

	/**
	 * Returns all distinct keys in the same order as in the key map.
	 *
	 * @return		distinct keys
	 */
	@SuppressWarnings("unchecked")
	public Set<K> getDistinctKeys1() {
		return (Set<K>) super.getDistinctKeys(1);
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
	 * @param elem		element
	 * @return			element which has been replaced or null otherwise
	 */
	public E putByKey1(E elem) {
		return super.putByKey(1, elem);
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
		super.invalidateKey(1, oldKey, newKey, elem);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Key1Collection<E, K> filter(Predicate<? super E> filter) {
		return (Key1Collection<E, K>) super.filter(filter);
	}
}
