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

import org.magicwerk.brownies.collections.KeyCollectionImpl.BuilderImpl;
import org.magicwerk.brownies.collections.function.Mapper;
import org.magicwerk.brownies.collections.function.Predicate;
import org.magicwerk.brownies.collections.function.Trigger;


/**
 * Key2List implements a key list with 1 key.
 * This key can be accessed fast.
 * It can provide fast access to its elements like a Set.
 * The elements allowed in the list can be constraint (null/duplicate values).
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @param <E> type of elements stored in the list
 * @param <K> type of key
 */
@SuppressWarnings("serial")
public class Key1List<E,K> extends KeyListImpl<E> {

    /**
     * Builder to construct Key1List instances.
     */
    public static class Builder<E,K> extends BuilderImpl<E> {
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
        Builder(Key1List<E,K> keyList) {
        	this.keyList = keyList;
        	initKeyMapBuilder(1);
        }

        // -- Constraint

        @Override
        public Builder<E,K> withNull(boolean allowNull) {
        	return (Builder<E,K>) super.withNull(allowNull);
        }

        @Override
        public Builder<E,K> withConstraint(Predicate<E> constraint) {
        	return (Builder<E,K>) super.withConstraint(constraint);
        }

        // -- Triggers

        @Override
        public Builder<E,K> withInsertTrigger(Trigger<E> trigger) {
        	return (Builder<E,K>) super.withInsertTrigger(trigger);
        }

        @Override
        public Builder<E,K> withDeleteTrigger(Trigger<E> trigger) {
        	return (Builder<E,K>) super.withDeleteTrigger(trigger);
        }

        //-- Content

        @Override
        public Builder<E,K> withCapacity(int capacity) {
        	return (Builder<E,K>) super.withCapacity(capacity);
        }

        @Override
        public Builder<E,K> withContent(Collection<? extends E> elements) {
        	return (Builder<E,K>) super.withContent(elements);
        }

        @Override
        public Builder<E,K> withContent(E... elements) {
        	return (Builder<E,K>) super.withContent(elements);
        }

        @Override
        public Builder<E,K> withMaxSize(int maxSize) {
        	return (Builder<E,K>) super.withMaxSize(maxSize);
        }

        @Override
        public Builder<E,K> withWindowSize(int maxSize) {
        	return (Builder<E,K>) super.withWindowSize(maxSize);
        }

        //-- Element key

        @Override
        public Builder<E,K> withElemSet() {
        	return (Builder<E,K>) super.withElemSet();
        }

        @Override
        public Builder<E,K> withElemOrderBy(boolean orderBy) {
        	return (Builder<E,K>) super.withElemOrderBy(orderBy);
        }

        @Override
        public Builder<E,K> withElemOrderBy(Class<?> type) {
        	return (Builder<E,K>) super.withElemOrderBy(type);
        }

        @Override
        public Builder<E,K> withElemNull(boolean allowNull) {
        	return (Builder<E,K>) super.withElemNull(allowNull);
        }

        @Override
        public Builder<E,K> withElemDuplicates(boolean allowDuplicates) {
        	return (Builder<E,K>) super.withElemDuplicates(allowDuplicates);
        }

        @Override
        public Builder<E,K> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	return (Builder<E,K>) super.withElemDuplicates(allowDuplicates, allowDuplicatesNull);
        }

        @Override
        public Builder<E,K> withElemSort(boolean sort) {
        	return (Builder<E,K>) super.withElemSort(sort);
        }

        @Override
        public Builder<E,K> withElemSort(Comparator<? super E> comparator) {
        	return (Builder<E,K>) super.withElemSort(comparator);
        }

        @Override
        public Builder<E,K> withElemSort(Comparator<? super E> comparator, boolean sortNullsFirst) {
        	return (Builder<E,K>) super.withElemSort(comparator, sortNullsFirst);
        }

        @Override
        public Builder<E,K> withPrimaryElem() {
        	return (Builder<E,K>) super.withPrimaryElem();
        }

        @Override
        public Builder<E,K> withUniqueElem() {
        	return (Builder<E,K>) super.withUniqueElem();
        }

        // -- Key1

        /**
         * Add key map.
         *
         * @param mapper	mapper to use
         * @return			this (fluent interface)
         */
        public Builder<E,K> withKey1Map(Mapper<? super E,K> mapper) {
        	return (Builder<E,K>) super.withKeyMap(1, mapper);
        }

        @Override
        public Builder<E,K> withKey1OrderBy(boolean orderBy) {
        	return (Builder<E,K>) super.withKey1OrderBy(orderBy);
        }

        @Override
        public Builder<E,K> withKey1OrderBy(Class<?> type) {
        	return (Builder<E,K>) super.withKey1OrderBy(type);
        }

        @Override
        public Builder<E,K> withKey1Null(boolean allowNull) {
        	return (Builder<E,K>) super.withKey1Null(allowNull);
        }

        @Override
        public Builder<E,K> withKey1Duplicates(boolean allowDuplicates) {
        	return (Builder<E,K>) super.withKey1Duplicates(allowDuplicates);
        }

        @Override
        public Builder<E,K> withKey1Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	return (Builder<E,K>) super.withKey1Duplicates(allowDuplicates, allowDuplicatesNull);
        }

        @Override
        public Builder<E,K> withKey1Sort(boolean sort) {
        	return (Builder<E,K>) super.withKey1Sort(sort);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (fluent interface)
         */
        public Builder<E,K> withKey1Sort(Comparator<? super K> comparator) {
        	return (Builder<E,K>) super.withKeySort(1, comparator);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param sortNullsFirst   		true if null will be sorted first, false for last
         * @return                      this (fluent interface)
         */
        public Builder<E,K> withKey1Sort(Comparator<? super K> comparator, boolean sortNullsFirst) {
        	return (Builder<E,K>) super.withKeySort(1, comparator, sortNullsFirst);
        }

        @Override
        public Builder<E,K> withPrimaryKey1() {
        	return (Builder<E,K>) super.withPrimaryKey1();
        }

        @Override
        public Builder<E,K> withUniqueKey1() {
        	return (Builder<E,K>) super.withUniqueKey1();
        }

        /**
         * @return created list
         */
        @SuppressWarnings("unchecked")
		public Key1List<E,K> build() {
        	if (keyColl == null) {
               	keyColl = new KeyCollectionImpl<E>();
        	}
        	build(keyColl, true);
        	if (keyList == null) {
        		keyList = new Key1List<E,K>();
        	}
        	init(keyColl, keyList);
        	return (Key1List<E,K>) keyList;
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
    protected Builder<E,K> getBuilder() {
    	return new Builder<E,K>(this);
    }

    @Override
    public Key1List<E,K> copy() {
    	return (Key1List<E,K>) super.copy();
    }

    @Override
    public Key1List<E,K> crop() {
    	return (Key1List<E,K>) super.crop();
    }

    //-- Element methods

	@Override
	public GapList<E> getAll(E elem) {
		return super.getAll(elem);
	}

	@Override
	public int getCount(E elem) {
		return super.getCount(elem);
	}

	@Override
	public GapList<E> removeAll(E elem) {
		return super.removeAll(elem);
	}

	@Override
	public Set<E> getDistinct() {
		return (Set<E>) super.getDistinct();
	}

	@Override
	public E put(E elem) {
		return super.put(elem);
	}

    //-- Key methods

    /**
     * Returns index of first element in list with specified key.
     *
     * @param key	key
     * @return		index of first element, -1 if no such element exists
     */
    public int indexOfKey1(K key) {
    	return super.indexOfKey(1, key);
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
	public GapList<E> getAllByKey1(K key) {
		return super.getAllByKey(1, key);
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
	 */
	public GapList<E> removeAllByKey1(K key) {
		return super.removeAllByKey(1, key);
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
	 * Adds or replaces element with specified key.
	 * If there is no element with specified key, the element is added.
	 * If there is an element with specified key and no duplicates
	 * are allowed, the existing element is replaced.
	 * If duplicates are allowed, the element is added.
	 *
	 * @param elem	element
	 * @return		element which has been replaced or null otherwise
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

}
