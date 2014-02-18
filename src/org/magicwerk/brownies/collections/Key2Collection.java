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

import org.magicwerk.brownies.collections.Key1List.Builder;
import org.magicwerk.brownies.collections.function.Mapper;
import org.magicwerk.brownies.collections.function.Predicate;
import org.magicwerk.brownies.collections.function.Trigger;


/**
 * Key2Collection implements a collection with 2 keys.
 * These keys can be accessed fast.
 * It can provide fast access to its elements like a Set.
 * The elements allowed in the list can be constraint (null/duplicate values).
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @see Key1List
 * @param <E> type of elements stored in the list
 * @param <K1> type of first key
 * @param <K2> type of second key
 */
@SuppressWarnings("serial")
public class Key2Collection<E,K1,K2> extends KeyCollectionImpl<E> {

    /**
     * Builder to construct Key2Collection instances.
     */
    public static class Builder<E,K1,K2> extends BuilderImpl<E> {
        /**
         * Default constructor.
         */
        public Builder() {
        	this(null);
        }

        /**
         * Private constructor used if extending Key2Collection.
         *
         * @param keyColl	key collection
         */
        Builder(Key2Collection<E,K1,K2> keyColl) {
        	this.keyColl = keyColl;
        	initKeyMapBuilder(2);
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
        public Builder<E,K1,K2> withContent(Collection<? extends E> elements) {
        	return (Builder<E,K1,K2>) super.withContent(elements);
        }

        @Override
        public Builder<E,K1,K2> withContent(E... elements) {
        	return (Builder<E,K1,K2>) super.withContent(elements);
        }

        @Override
        public Builder<E,K1,K2> withMaxSize(int maxSize) {
        	return (Builder<E,K1,K2>) super.withMaxSize(maxSize);
        }

        //-- Element key

        @Override
        public Builder<E,K1,K2> withElemSet() {
        	return (Builder<E,K1,K2>) super.withElemSet();
        }

        @Override
        public Builder<E,K1,K2> withElemOrderBy(boolean orderBy) {
        	return (Builder<E,K1,K2>) super.withElemOrderBy(orderBy);
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
        public Builder<E,K1,K2> withKey1Map(Mapper<? super E,K1> mapper) {
        	return (Builder<E,K1,K2>) super.withKeyMap(1, mapper);
        }

        @Override
        public Builder<E,K1,K2> withKey1OrderBy(boolean orderBy) {
        	return (Builder<E,K1,K2>) super.withKey1OrderBy(orderBy);
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
        public Builder<E,K1,K2> withKey2Map(Mapper<? super E,K2> mapper) {
        	return (Builder<E,K1,K2>) super.withKeyMap(2, mapper);
        }

        @Override
        public Builder<E,K1,K2> withKey2OrderBy(boolean orderBy) {
        	return (Builder<E,K1,K2>) super.withKey2OrderBy(orderBy);
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
        	return (Builder<E,K1,K2>) super.withKeySort(2, comparator);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param sortNullsFirst   		true if null will be sorted first, false for last
         * @return                      this (fluent interface)
         */
        public Builder<E,K1,K2> withKey2Sort(Comparator<? super K2> comparator, boolean sortNullsFirst) {
        	return (Builder<E,K1,K2>) super.withKeySort(2, comparator, sortNullsFirst);
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
         * @return created collection
         */
        @SuppressWarnings("unchecked")
		public Key2Collection<E,K1,K2> build() {
        	if (keyColl == null) {
               	keyColl = new Key2Collection<E,K1,K2>();
        	}
        	build(keyColl, false);
        	init(keyColl);
        	return (Key2Collection<E,K1,K2>) keyColl;
        }
    }

    /**
     * Protected constructor used by builder or derived collections.
     */
    protected Key2Collection() {
    }

    /**
     * @return builder to use in extending classes
     */
    protected Builder<E,K1,K2> getBuilder() {
    	return new Builder<E,K1,K2>(this);
    }

    @Override
    public Key2Collection<E,K1,K2> copy() {
        return (Key2Collection<E,K1,K2>) super.copy();
    }

    @Override
    public Key2Collection<E,K1,K2> crop() {
        return (Key2Collection<E,K1,K2>) super.crop();
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

	@Override
	public void invalidate(E elem) {
		super.invalidate(elem);
	}

    //-- Key1 methods

    /**
     * Returns a map view to the key map.
     * The collection can be modified through the map as long
     * as the constraint are not violated.
     * The collections returned by the methods entrySet(), keySet(), and
     * values() are immutable however.
     *
     * @return map view to key map
     */
    public Map<K1,E> asMap1() {
    	return new KeyCollectionAsMap<E,K1>(this, 1, false);
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
	 * Returns all elements with specified key.
	 * The returned list is immutable.
	 *
	 * @param key	key
	 * @return		all elements with specified key (never null)
	 */
	public E getByKey1(K1 key) {
		return super.getByKey(1, key);
	}

	/**
	 * Returns all elements with specified key.
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

    /**
     * Invalidate key value of element.
     * You must call an invalidate method if an element's key value has changed after adding it to the collection.
     *
     * @param oldKey	old key value
     * @param newKey	new key value
     * @param elem		element to invalidate (can be null if there are no duplicates with this key)
     */
    public void invalidateKey1(K1 oldKey, K1 newKey, E elem) {
    	super.invalidateKey(1, oldKey, newKey, elem);
    }

    //-- Key2 methods

    /**
     * Returns a map view to the key map.
     * The collection can be modified through the map as long
     * as the constraint are not violated.
     * The collections returned by the methods entrySet(), keySet(), and
     * values() are immutable however.
     *
     * @return map view to key map
     */
    public Map<K2,E> asMap2() {
    	return new KeyCollectionAsMap<E,K2>(this, 2, false);
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

    /**
     * Invalidate key value of element.
     * You must call an invalidate method if an element's key value has changed after adding it to the collection.
     *
     * @param oldKey	old key value
     * @param newKey	new key value
     * @param elem		element to invalidate (can be null if there are no duplicates with this key)
     */
    public void invalidateKey2(K2 oldKey, K2 newKey, E elem) {
    	super.invalidateKey(2, oldKey, newKey, elem);
    }

}
