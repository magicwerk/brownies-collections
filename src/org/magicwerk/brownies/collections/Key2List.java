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

import org.magicwerk.brownies.collections.Key1List.Builder;
import org.magicwerk.brownies.collections.KeyCollectionImpl.BuilderImpl;
import org.magicwerk.brownies.collections.function.Mapper;
import org.magicwerk.brownies.collections.function.Predicate;
import org.magicwerk.brownies.collections.function.Trigger;


/**
 * Key2List implements a key list with 2 keys.
 * These keys can be accessed fast.
 * It can provide fast access to its elements like a Set.
 * The elements allowed in the list can be constraint (null/duplicate values).
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @param <E> type of elements stored in the list
 * @param <K1> type of first key
 * @param <K2> type of second key
 */
@SuppressWarnings("serial")
public class Key2List<E,K1,K2> extends KeyListImpl<E> {

    /**
     * Builder to construct Key2List instances.
     */
    public static class Builder<E,K1,K2> extends BuilderImpl<E> {
        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Private constructor used if extending Key2List.
         *
         * @param keyList	key list
         */
        Builder(Key2List<E,K1,K2> keyList) {
        	this.keyList = keyList;
        }

        // -- Constraint

        @Override
        public Builder<E,K1,K2> withNull(boolean allowNull) {
        	return (Builder<E,K1,K2>) super.withNull(allowNull);
        }

        @Override
        public Builder<E,K1,K2> withConstraint(Predicate<E> constraint) {
        	return (Builder<E,K1,K2>) super.withConstraint(constraint);
        }

        // -- Triggers

        @Override
        public Builder<E,K1,K2> withInsertTrigger(Trigger<E> trigger) {
        	return (Builder<E,K1,K2>) super.withInsertTrigger(trigger);
        }

        @Override
        public Builder<E,K1,K2> withDeleteTrigger(Trigger<E> trigger) {
        	return (Builder<E,K1,K2>) super.withDeleteTrigger(trigger);
        }

        //-- Content

        @Override
        public Builder<E,K1,K2> withCapacity(int capacity) {
        	return (Builder<E,K1,K2>) super.withCapacity(capacity);
        }

        @Override
        public Builder<E,K1,K2> withElements(Collection<? extends E> elements) {
        	return (Builder<E,K1,K2>) super.withElements(elements);
        }

        @Override
        public Builder<E,K1,K2> withElements(E... elements) {
        	return (Builder<E,K1,K2>) super.withElements(elements);
        }

        @Override
        public Builder<E,K1,K2> withMaxSize(int maxSize) {
        	return (Builder<E,K1,K2>) super.withMaxSize(maxSize);
        }

        @Override
        public Builder<E,K1,K2> withWindowSize(int maxSize) {
        	return (Builder<E,K1,K2>) super.withWindowSize(maxSize);
        }

        //-- Element key

        @Override
        public Builder<E,K1,K2> withElem() {
        	return (Builder<E,K1,K2>) super.withElem();
        }

        @Override
        public Builder<E,K1,K2> withElemOrderBy(boolean orderBy) {
        	return (Builder<E,K1,K2>) super.withElemOrderBy(orderBy);
        }

        @Override
        public Builder<E,K1,K2> withElemOrderBy(Class<?> type) {
        	return (Builder<E,K1,K2>) super.withElemOrderBy(type);
        }

        @Override
        public Builder<E,K1,K2> withElemNull(boolean allowNull) {
        	return (Builder<E,K1,K2>) super.withElemNull(allowNull);
        }

        @Override
        public Builder<E,K1,K2> withElemDuplicates(boolean allowDuplicates) {
        	return (Builder<E,K1,K2>) super.withElemDuplicates(allowDuplicates);
        }

        @Override
        public Builder<E,K1,K2> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	return (Builder<E,K1,K2>) super.withElemDuplicates(allowDuplicates, allowDuplicatesNull);
        }

        @Override
        public Builder<E,K1,K2> withElemSort(boolean sort) {
        	return (Builder<E,K1,K2>) super.withElemSort(sort);
        }

        @Override
        public Builder<E,K1,K2> withElemSort(Comparator<? super E> comparator) {
        	return (Builder<E,K1,K2>) super.withElemSort(comparator);
        }

        @Override
        public Builder<E,K1,K2> withElemSort(Comparator<? super E> comparator, boolean sortNullsFirst) {
        	return (Builder<E,K1,K2>) super.withElemSort(comparator, sortNullsFirst);
        }

        @Override
        public Builder<E,K1,K2> withPrimaryElem() {
        	return (Builder<E,K1,K2>) super.withPrimaryElem();
        }

        @Override
        public Builder<E,K1,K2> withUniqueElem() {
        	return (Builder<E,K1,K2>) super.withUniqueElem();
        }

        // -- Key1

        /**
         * Add key map.
         *
         * @param mapper	mapper to use
         * @return			this (fluent interface)
         */
        public Builder<E,K1,K2> withKey1(Mapper<E,K1> mapper) {
        	return (Builder<E,K1,K2>) super.withKey(1, mapper);
        }

        @Override
        public Builder<E,K1,K2> withKey1OrderBy(boolean orderBy) {
        	return (Builder<E,K1,K2>) super.withKey1OrderBy(orderBy);
        }

        @Override
        public Builder<E,K1,K2> withKey1OrderBy(Class<?> type) {
        	return (Builder<E,K1,K2>) super.withKey1OrderBy(type);
        }

        @Override
        public Builder<E,K1,K2> withKey1Null(boolean allowNull) {
        	return (Builder<E,K1,K2>) super.withKey1Null(allowNull);
        }

        @Override
        public Builder<E,K1,K2> withKey1Duplicates(boolean allowDuplicates) {
        	return (Builder<E,K1,K2>) super.withKey1Duplicates(allowDuplicates);
        }

        @Override
        public Builder<E,K1,K2> withKey1Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	return (Builder<E,K1,K2>) super.withKey1Duplicates(allowDuplicates, allowDuplicatesNull);
        }

        @Override
        public Builder<E,K1,K2> withKey1Sort(boolean sort) {
        	return (Builder<E,K1,K2>) super.withKey1Sort(sort);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (fluent interface)
         */
        public Builder<E,K1,K2> withKey1Sort(Comparator<? super K1> comparator) {
        	return (Builder<E,K1,K2>) super.withKeySort(1, comparator);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param sortNullsFirst   		true if null will be sorted first, false for last
         * @return                      this (fluent interface)
         */
        public Builder<E,K1,K2> withKey1Sort(Comparator<? super K1> comparator, boolean sortNullsFirst) {
        	return (Builder<E,K1,K2>) super.withKeySort(1, comparator, sortNullsFirst);
        }
        @Override
        public Builder<E,K1,K2> withPrimaryKey1() {
        	return (Builder<E,K1,K2>) super.withPrimaryKey1();
        }

        @Override
        public Builder<E,K1,K2> withUniqueKey1() {
        	return (Builder<E,K1,K2>) super.withUniqueKey1();
        }

        // -- Key2

        /**
         * Add key map.
         *
         * @param mapper	mapper to use
         * @return			this (fluent interface)
         */
        public Builder<E,K1,K2> withKey2(Mapper<E,K1> mapper) {
        	return (Builder<E,K1,K2>) super.withKey(2, mapper);
        }

        @Override
        public Builder<E,K1,K2> withKey2OrderBy(boolean orderBy) {
        	return (Builder<E,K1,K2>) super.withKey2OrderBy(orderBy);
        }

        @Override
        public Builder<E,K1,K2> withKey2OrderBy(Class<?> type) {
        	return (Builder<E,K1,K2>) super.withKey2OrderBy(type);
        }

        @Override
        public Builder<E,K1,K2> withKey2Null(boolean allowNull) {
        	return (Builder<E,K1,K2>) super.withKey2Null(allowNull);
        }

        @Override
        public Builder<E,K1,K2> withKey2Duplicates(boolean allowDuplicates) {
        	return (Builder<E,K1,K2>) super.withKey2Duplicates(allowDuplicates);
        }

        @Override
        public Builder<E,K1,K2> withKey2Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	return (Builder<E,K1,K2>) super.withKey2Duplicates(allowDuplicates, allowDuplicatesNull);
        }

        @Override
        public Builder<E,K1,K2> withKey2Sort(boolean sort) {
        	return (Builder<E,K1,K2>) super.withKey2Sort(sort);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (fluent interface)
         */
        public Builder<E,K1,K2> withKey2Sort(Comparator<? super K2> comparator) {
        	return (Builder<E,K1,K2>) super.withKeySort(1, comparator);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param sortNullsFirst   		true if null will be sorted first, false for last
         * @return                      this (fluent interface)
         */
        public Builder<E,K1,K2> withKey2Sort(Comparator<? super K2> comparator, boolean sortNullsFirst) {
        	return (Builder<E,K1,K2>) super.withKeySort(1, comparator, sortNullsFirst);
        }
        @Override
        public Builder<E,K1,K2> withPrimaryKey2() {
        	return (Builder<E,K1,K2>) super.withPrimaryKey2();
        }

        @Override
        public Builder<E,K1,K2> withUniqueKey2() {
        	return (Builder<E,K1,K2>) super.withUniqueKey2();
        }

        /**
         * @return created list
         */
        public Key2List<E,K1,K2> build() {
        	if (keyColl == null) {
               	keyColl = new KeyCollectionImpl<E>();
        	}
        	build(keyColl, true);
        	Key2List<E,K1,K2> list = new Key2List<E,K1,K2>();
        	init(keyColl, list);
        	return list;
        }
    }

    /**
     * Private constructor used by builder.
     */
    private Key2List() {
    }

    /**
     * @return builder to use in extending classes
     */
    protected Builder<E,K1,K2> getBuilder() {
    	return new Builder<E,K1,K2>(this);
    }

    @Override
    public Object clone() {
    	return copy();
    }

    @Override
    public Key2List<E,K1,K2> copy() {
    	Key2List<E,K1,K2> copy = new Key2List<E,K1,K2>();
        copy.initCopy(this);
        return copy;
    }

    @Override
    public Key2List<E,K1,K2> crop() {
    	Key2List<E,K1,K2> copy = new Key2List<E,K1,K2>();
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
		return super.getDistinct();
	}

	@Override
	public E put(E elem) {
		return super.put(elem);
	}

    //-- Key1 methods

    /**
     * Returns index of first element in list with specified key.
     *
     * @param key	key
     * @return		index of first element, -1 if no such element exists
     */
    public int indexOfKey1(K1 key) {
    	return super.indexOfKey(1, key);
    }

	/**
	 * Checks whether an element with specified key exists.
	 *
	 * @param key	key
	 * @return		true if element with specified key exists, otherwise false
	 */
    public boolean containsKey1(K1 key) {
    	return super.containsKey(1, key);
    }

	/**
	 * Returns element with specified key.
	 * If there are several elements with the same key, the one added first will be returned.
	 *
	 * @param key	key
	 * @return		element with specified key or null
	 */
	public E getByKey1(K1 key) {
		return super.getByKey(1, key);
	}

	/**
	 * Returns all elements with specified key.
	 * The returned list is immutable.
	 *
	 * @param key	key
	 * @return		all elements with specified key (never null)
	 */
	public GapList<E> getAllByKey1(K1 key) {
		return super.getAllByKey(1, key);
	}

	/**
	 * Returns the number of elements with specified key.
	 *
	 * @param key	key
	 * @return		number of elements with specified key
	 */
	public int getCountByKey1(K1 key) {
		return super.getCountByKey(1, key);
	}

	/**
	 * Removes element with specified key.
	 * If there are several elements with the same key, the one added first will be removed.
	 *
	 * @param key	key
	 * @return		element with specified key or null
	 */
	public E removeByKey1(K1 key) {
		return super.removeByKey(1, key);
	}

	/**
	 * Removes all elements with specified key.
	 *
	 * @param key	key
	 * @return		removed elements with specified key (never null)
	 */
	public GapList<E> removeAllByKey1(K1 key) {
		return super.removeAllByKey(1, key);
	}

	/**
	 * Returns all distinct keys in the same order as in the key map.
	 * The returned set is immutable.
	 *
	 * @return		distinct keys
	 */
	@SuppressWarnings("unchecked")
	public Set<K1> getDistinctKeys1() {
		return (Set<K1>) super.getDistinctKeys(1);
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

    //-- Key2 methods

    /**
     * Returns index of first element in list with specified key.
     *
     * @param key	key
     * @return		index of first element, -1 if no such element exists
     */
    public int indexOfKey2(K2 key) {
    	return super.indexOfKey(2, key);
    }

	/**
	 * Checks whether an element with specified key exists.
	 *
	 * @param key	key
	 * @return		true if element with specified key exists, otherwise false
	 */
    public boolean containsKey2(K2 key) {
    	return super.containsKey(2, key);
    }

	/**
	 * Returns element with specified key.
	 * If there are several elements with the same key, the one added first will be returned.
	 *
	 * @param key	key
	 * @return		element with specified key or null
	 */
	public E getByKey2(K2 key) {
		return super.getByKey(2, key);
	}

	/**
	 * Returns all elements with specified key.
	 * The returned list is immutable.
	 *
	 * @param key	key
	 * @return		all elements with specified key (never null)
	 */
	public GapList<E> getAllByKey2(K2 key) {
		return super.getAllByKey(2, key);
	}

	/**
	 * Returns the number of elements with specified key.
	 *
	 * @param key	key
	 * @return		number of elements with specified key
	 */
	public int getCountByKey2(K2 key) {
		return super.getCountByKey(2, key);
	}

	/**
	 * Removes element with specified key.
	 * If there are several elements with the same key, the one added first will be removed.
	 *
	 * @param key	key
	 * @return		element with specified key or null
	 */
	public E removeByKey2(K2 key) {
		return super.removeByKey(2, key);
	}

	/**
	 * Removes all elements with specified key.
	 *
	 * @param key	key
	 * @return		removed elements with specified key (never null)
	 */
	public GapList<E> removeAllByKey2(K2 key) {
		return super.removeAllByKey(2, key);
	}

	/**
	 * Returns all distinct keys in the same order as in the key map.
	 * The returned set is immutable.
	 *
	 * @return		distinct keys
	 */
	@SuppressWarnings("unchecked")
	public Set<K2> getDistinctKeys2() {
		return (Set<K2>) super.getDistinctKeys(2);
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
    public E putByKey2(E elem) {
		return super.putByKey(2, elem);
	}

}
