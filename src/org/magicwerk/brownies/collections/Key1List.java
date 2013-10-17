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
        }

        /**
         * Private constructor used if extending Key1List.
         *
         * @param keyList	key list
         */
        Builder(Key1List<E,K> keyList) {
        	this.keyList = keyList;
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
        public Builder<E,K> withElements(Collection<? extends E> elements) {
        	return (Builder<E,K>) super.withElements(elements);
        }

        @Override
        public Builder<E,K> withElements(E... elements) {
        	return (Builder<E,K>) super.withElements(elements);
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
        public Builder<E,K> withElem() {
        	return (Builder<E,K>) super.withElem();
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

        // -- Key

        // @Override
        public Builder<E,K> withKey1(Mapper<E,K> mapper) {
        	return (Builder<E,K>) super.withKey1(mapper);
        }

        // @Override
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

        @Override
        public Builder<E,K> withKey1Sort(Comparator<? super E> comparator) {
        	return (Builder<E,K>) super.withKey1Sort(comparator);
        }

        @Override
        public Builder<E,K> withKey1Sort(Comparator<? super E> comparator, boolean comparatorSortsNull) {
        	return (Builder<E,K>) super.withKey1Sort(comparator, comparatorSortsNull);
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
     * Private constructor used by builder.
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
    public Object clone() {
    	return copy();
    }

    @Override
    public Key1List<E,K> copy() {
    	Key1List<E,K> copy = new Key1List<E,K>();
        copy.initCopy(this);
        return copy;
    }

    @Override
    public Key1List<E,K> crop() {
    	Key1List<E,K> copy = new Key1List<E,K>();
        copy.initCrop(this);
        return copy;
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
	 * The returned list is immutable.
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
	 * The returned set is immutable.
	 *
	 * @return		distinct keys
	 */
	@SuppressWarnings("unchecked")
	public Set<K> getDistinctKeys1() {
		return (Set<K>) super.getDistinctKeys(1);
	}

}
