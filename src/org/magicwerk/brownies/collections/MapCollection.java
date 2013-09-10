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
 * $Id: SetList.java 1815 2013-08-09 00:05:35Z origo $
 */
package org.magicwerk.brownies.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.magicwerk.brownies.collections.BagCollection.Builder;
import org.magicwerk.brownies.collections.KeyList.NullMode;
import org.magicwerk.brownies.collections.function.Mapper;
import org.magicwerk.brownies.collections.function.Predicate;
import org.magicwerk.brownies.collections.function.Trigger;
import org.magicwerk.brownies.collections.helper.IdentMapper;


/**
 * Think about SetList as of a Set where you can also access the
 * elements by index. Typically the elements are in the order
 * specified by the list, but you can also let them order
 * automatically like in TreeSet.
 *
 * @author Thomas Mauch
 * @version $Id: SetList.java 1815 2013-08-09 00:05:35Z origo $
 *
 * @see MapList
 * @param <E> type of elements stored in the list
 */
public class MapCollection<E,K> extends KeyCollection<E> {

    /** UID for serialization. */
    private static final long serialVersionUID = 6181488174454611419L;

    /**
     * Builder to construct MapCollection instances.
     * It offers all methods offered by BagCollection.Builder
     * which are implemented as verbatim copy. It cannot inherit
     * from BagCollection.Builder as build() must return an object
     * of type MapCollection instead of BagCollection.
     */
    // TODO: move common methods to BuilderBase?
    public static class Builder<E,K> extends BuilderBase<E> {
        /**
         * Default constructor.
         */
        public Builder() {
        }

        // -- Constraint

        /**
         * Specify element constraint.
         *
         * @param allowNull	true to allow null values
         * @return 			this (fluent interface)
         */
        public Builder<E,K> withNull(boolean allowNull) {
        	this.allowNullElem = allowNull;
        	if (hasElemMapBuilder()) {
        		getElemMapBuilder().allowNull = allowNull;
        	}
        	return this;
        }

        /**
         * Specify element constraint.
         *
         * @param constraint	constraint element must satisfy
         * @return 				this (fluent interface)
         */
        public Builder<E,K> withConstraint(Predicate<E> constraint) {
        	this.constraint = constraint;
        	return this;
        }

        // -- Triggers

        /**
         * Specify insert trigger.
         *
         * @param trigger	insert trigger method
         * @return			this (fluent interface)
         */
        public Builder<E,K> withInsertTrigger(Trigger<E> trigger) {
            this.insertTrigger = trigger;
            return this;
        }

        /**
         * Specify delete trigger.
         *
         * @param trigger	delete trigger method
         * @return			this (fluent interface)
         */
        public Builder<E,K> withDeleteTrigger(Trigger<E> trigger) {
            this.deleteTrigger = trigger;
            return this;
        }

        //-- Content

        /**
         * Specify initial capacity.
         *
         * @param capacity	initial capacity
         * @return			this (fluent interface)
         */
        public Builder<E,K> withCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        /**
         * Specify elements added to the collection upon creation.
         *
         * @param elements	initial elements
         * @return			this (fluent interface)
         */
        public Builder<E,K> withElements(Collection<? extends E> elements) {
            this.collection = elements;
            return this;
        }

        /**
         * Specify elements added to the collection upon creation.
         *
         * @param elements	initial elements
         * @return			this (fluent interface)
         */
        public Builder<E,K> withElements(E... elements) {
            this.array = elements;
            return this;
        }

        //-- Element key

        /**
         * Add element map (with ident mapper).
         *
         * @return	this (fluent interface)
         */
        public Builder<E,K> withElem() {
            return withElem(false);
        }

        /**
         * Add element map (with ident mapper).
         *
         * @param orderBy	true to force the collection to have the order of this map
         * @return			this (fluent interface)
         */
        public Builder<E,K> withElem(boolean orderBy) {
            getElemMapBuilder().orderBy = orderBy;
            return this;
        }

        /**
         * Specify whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param allowNull true to allow null elements, false to disallow
         * @return          this (fluent interfaces)
         */
        public Builder<E,K> withElemNull(boolean allowNull) {
        	getElemMapBuilder().allowNull = allowNull;
        	allowNullElem = allowNull;
        	return this;
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates   true to allow duplicates
         * @return              	this (fluent interfaces)
         */
        public Builder<E,K> withElemDuplicates(boolean allowDuplicates) {
        	return withElemDuplicates(allowDuplicates, allowDuplicates);
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates		true to allow duplicates
         * @param allowDuplicatesNull	true to allow duplicate null values
         * @return						this (fluent interfaces)
         */
        public Builder<E,K> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	getElemMapBuilder().allowDuplicates = allowDuplicates;
        	getElemMapBuilder().allowDuplicatesNull = allowDuplicatesNull;
            return this;
        }

        /**
         * Determines that list should be sorted.
         *
         * @param sort    true to sort list, otherwise false
         * @return        this (fluent interface)
         */
        public Builder<E,K> withElemSort(boolean sort) {
        	getElemMapBuilder().sort = sort;
            return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (fluent interface)
         */
        public Builder<E,K> withElemSort(Comparator<? super E> comparator) {
            return withElemSort(comparator, false);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param comparatorSortsNull   true if comparator sorts null, false if not
         * @return                      this (fluent interface)
         */
        public Builder<E,K> withElemSort(Comparator<? super E> comparator, boolean comparatorSortsNull) {
        	getElemMapBuilder().comparator = comparator;
        	getElemMapBuilder().comparatorSortsNull = comparatorSortsNull;
            return this;
        }

        /**
         * Determines whether nulls are sorted first or last.
         *
         * @param nullsFirst    true to sort nulls first, false to sort nulls last
         * @return              this (fluent interface)
         */
        public Builder<E,K> withElemSortNullsFirst(boolean nullsFirst) {
        	getElemMapBuilder().sortNullsFirst = nullsFirst;
            return this;
        }

        /**
         * Specify element type to use.
         *
         * @param type	type to use
         * @return		this (fluent interface)
         */
        public Builder<E,K> withElemType(Class<?> type) {
        	getElemMapBuilder().type = type;
            return this;
        }

        // -- Key

        /**
         * Add key map.
         *
         * @param mapper	mapper to use
         * @return			this (fluent interface)
         */
        public Builder<E,K> withKey(Mapper<E,K> mapper) {
            return withKey(mapper, false);
        }

        /**
         * Add key map.
         *
         * @param mapper	mapper to use
         * @param orderBy	true to force the collection to have the order of this map
         * @return			this (fluent interface)
         */
        public Builder<E,K> withKey(Mapper<E,K> mapper, boolean orderBy) {
            getKeyMapBuilder(0).mapper = (Mapper<E, Object>) mapper;
            getKeyMapBuilder(0).orderBy = orderBy;
            return this;
        }

        /**
         * Specify whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param allowNull true to allow null elements, false to disallow
         * @return          this (fluent interfaces)
         */
        public Builder<E,K> withKeyNull(boolean allowNull) {
        	getKeyMapBuilder(0).allowNull = allowNull;
        	return this;
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates   true to allow duplicates
         * @return              	this (fluent interfaces)
         */
        public Builder<E,K> withKeyDuplicates(boolean allowDuplicates) {
        	return withKeyDuplicates(allowDuplicates, allowDuplicates);
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates		true to allow duplicates
         * @param allowDuplicatesNull	true to allow duplicate null values
         * @return						this (fluent interfaces)
         */
        public Builder<E,K> withKeyDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	getKeyMapBuilder(0).allowDuplicates = allowDuplicates;
        	getKeyMapBuilder(0).allowDuplicatesNull = allowDuplicatesNull;
            return this;
        }

        /**
         * Determines that list should be sorted.
         *
         * @param sort    true to sort list, otherwise false
         * @return        this (fluent interface)
         */
        public Builder<E,K> withKeySort(boolean sort) {
        	getKeyMapBuilder(0).sort = sort;
            return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (fluent interface)
         */
        public Builder<E,K> withKeySort(Comparator<? super E> comparator) {
            return withKeySort(comparator, false);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param comparatorSortsNull   true if comparator sorts null, false if not
         * @return                      this (fluent interface)
         */
        public Builder<E,K> withKeySort(Comparator<? super E> comparator, boolean comparatorSortsNull) {
        	getKeyMapBuilder(0).comparator = comparator;
        	getKeyMapBuilder(0).comparatorSortsNull = comparatorSortsNull;
            return this;
        }

        /**
         * Determines whether nulls are sorted first or last.
         *
         * @param nullsFirst    true to sort nulls first, false to sort nulls last
         * @return              this (fluent interface)
         */
        public Builder<E,K> withKeySortNullsFirst(boolean nullsFirst) {
        	getKeyMapBuilder(0).sortNullsFirst = nullsFirst;
            return this;
        }

        /**
         * Specify element type to use.
         *
         * @param type	type to use
         * @return		this (fluent interface)
         */
        public Builder<E,K> withKeyType(Class<?> type) {
        	getKeyMapBuilder(0).type = type;
            return this;
        }

        /**
         * @return created SetList
         */
        public MapCollection<E,K> build() {
        	if (keyColl == null) {
               	keyColl = new MapCollection<E,K>();
        	}
        	build(keyColl);
        	return (MapCollection<E,K>) keyColl;
        }
    }

    /**
     * Private constructor.
     */
    private MapCollection() {
    }

    //-- Key methods

    public boolean containsKey(K key) {
    	return super.containsKey(1, key);
    }

	public E getByKey(K key) {
		return super.getByKey(1, key);
	}

	public GapList<E> getAllByKey(K key) {
		return super.getAllByKey(1, key);
	}

	public int getCountByKey(K key) {
		return super.getCountByKey(1, key);
	}

	public E removeByKey(K key) {
		return super.removeByKey(1, key);
	}

	public GapList<E> removeAllByKey(K key) {
		return super.removeAllByKey(1, key);
	}

	public GapList<Object> getAllDistinctKeys() {
		return super.getAllDistinctKeys(1);
	}

	public int getCountDistinctKeys() {
		return super.getCountDistinctKeys(1);
	}

    public MapCollection<E,K> copy() {
        MapCollection<E,K> copy = new MapCollection<E,K>();
        copy.initCopy(this);
        return copy;
    }

    public MapCollection<E,K> crop() {
        MapCollection<E,K> copy = new MapCollection<E,K>();
        copy.initCrop(this);
        return copy;
    }

}
