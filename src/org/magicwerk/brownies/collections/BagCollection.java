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

import org.magicwerk.brownies.collections.function.Predicate;
import org.magicwerk.brownies.collections.function.Trigger;


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
public class BagCollection<E> extends KeyCollection<E> {

    /** UID for serialization. */
    private static final long serialVersionUID = 6181488174454611419L;

    /**
     * Builder to construct BagCollection instances.
     */
    public static class Builder<E> extends BuilderBase<E> {
        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Internal constructor.
         *
         * @param keyColl	keyColl to setup
         */
//        Builder(KeyCollection<E> keyColl) {
//        	this.keyColl = keyColl;
//        }

        // -- Constraint

        /**
         * Specify element constraint.
         *
         * @param allowNull	true to allow null values
         * @return 			this (fluent interface)
         */
        public Builder<E> withNull(boolean allowNull) {
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
        public Builder<E> withConstraint(Predicate<E> constraint) {
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
        public Builder<E> withInsertTrigger(Trigger<E> trigger) {
            this.insertTrigger = trigger;
            return this;
        }

        /**
         * Specify delete trigger.
         *
         * @param trigger	delete trigger method
         * @return			this (fluent interface)
         */
        public Builder<E> withDeleteTrigger(Trigger<E> trigger) {
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
        public Builder<E> withCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        /**
         * Specify elements added to the collection upon creation.
         *
         * @param elements	initial elements
         * @return			this (fluent interface)
         */
        public Builder<E> withElements(Collection<? extends E> elements) {
            this.collection = elements;
            return this;
        }

        /**
         * Specify elements added to the collection upon creation.
         *
         * @param elements	initial elements
         * @return			this (fluent interface)
         */
        public Builder<E> withElements(E... elements) {
            this.array = elements;
            return this;
        }

        //-- Element key

        /**
         * Add element map (with ident mapper).
         *
         * @return	this (fluent interface)
         */
        public Builder<E> withElem() {
            return withElem(false);
        }

        /**
         * Add element map (with ident mapper).
         *
         * @param orderBy	true to force the collection to have the order of this map
         * @return			this (fluent interface)
         */
        public Builder<E> withElem(boolean orderBy) {
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
        public Builder<E> withElemNull(boolean allowNull) {
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
        public Builder<E> withElemDuplicates(boolean allowDuplicates) {
        	return withElemDuplicates(allowDuplicates, allowDuplicates);
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates		true to allow duplicates
         * @param allowDuplicatesNull	true to allow duplicate null values
         * @return						this (fluent interfaces)
         */
        public Builder<E> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
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
        public Builder<E> withElemSort(boolean sort) {
        	getElemMapBuilder().sort = sort;
            return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (fluent interface)
         */
        public Builder<E> withElemSort(Comparator<? super E> comparator) {
            return withElemSort(comparator, false);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param comparatorSortsNull   true if comparator sorts null, false if not
         * @return                      this (fluent interface)
         */
        public Builder<E> withElemSort(Comparator<? super E> comparator, boolean comparatorSortsNull) {
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
        public Builder<E> withElemSortNullsFirst(boolean nullsFirst) {
        	getElemMapBuilder().sortNullsFirst = nullsFirst;
            return this;
        }

        /**
         * Specify element type to use.
         *
         * @param type	type to use
         * @return		this (fluent interface)
         */
        public Builder<E> withElemType(Class<?> type) {
        	getElemMapBuilder().type = type;
            return this;
        }

        /**
         * Create collection with specified options.
         *
         * @return created collection
         */
        public BagCollection<E> build() {
        	// Constructs builder if there is none
        	getElemMapBuilder();

        	if (keyColl == null) {
               	keyColl = new BagCollection<E>();
        	}
        	build(keyColl);
        	return (BagCollection<E>) keyColl;
        }
    }

    /**
     * Private constructor used by builder.
     */
    private BagCollection() {
    }

    /**
     * Create builder for this class.
     * Internal use in child classes only.
     *
     * @return  builder for this class
     */
//    protected BagCollection.Builder<E> getBuilder() {
//        return new BagCollection.Builder<E>(this);
//    }

    //-- Element methods

	public E get(E key) {
		return super.getByKey(0, key);
	}

	public GapList<E> getAll(E key) {
		return super.getAllByKey(0, key);
	}

	public int getCount(E key) {
		return super.getCountByKey(0, key);
	}

	public GapList<E> removeAll(E key) {
		return super.removeAllByKey(0, key);
	}

	public GapList<Object> getAllDistinct() {
		return super.getAllDistinctKeys(0);
	}

	public int getCountDistinct() {
		return super.getCountDistinctKeys(0);
	}

	//-- Other methods

    // TODO what about clone()?
    /**
     * Returns a copy of this collection.
     * The new collection will use the same comparator, ordering, etc.
     *
     * @return  an empty copy of this instance
     */
    public BagCollection<E> copy() {
        BagCollection<E> copy = new BagCollection<E>();
        copy.initCopy(this);
        return copy;
    }

    /**
     * Returns a copy of this collection.
     * The new collection will use the same comparator, ordering, etc.
     *
     * @return  a copy of this instance
     */
    public BagCollection<E> crop() {
        BagCollection<E> copy = new BagCollection<E>();
        copy.initCrop(this);
        return copy;
    }

}
