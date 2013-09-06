/*
 * Copyright 2012 by Thomas Mauch
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
        	newKeyMapBuilder(IdentMapper.INSTANCE);
        }

        /**
         * Internal constructor.
         *
         * @param keyColl	keyColl to setup
         */
        Builder(KeyCollection<E> keyColl) {
        	this.keyColl = keyColl;

        	newKeyMapBuilder(IdentMapper.INSTANCE);
        }

        // -- Constraint

        /**
         * Specify element constraint.
         *
         * @param allowNull	true to allow null values
         * @return 			this (fluent interface)
         */
        public Builder<E> withNull(boolean allowNull) {
        	endKeyMapBuilder();
        	this.allowNullElem = allowNull;
        	return this;
        }

        /**
         * Specify element constraint.
         *
         * @param constraint	constraint element must satisfy
         * @return 				this (fluent interface)
         */
        public Builder<E> withConstraint(Predicate<E> constraint) {
        	endKeyMapBuilder();
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
        	endKeyMapBuilder();
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
        	endKeyMapBuilder();
            this.deleteTrigger = trigger;
            return this;
        }

        //-- Content

        public Builder<E> withCapacity(int capacity) {
        	endKeyMapBuilder();
            this.capacity = capacity;
            return this;
        }

        public Builder<E> withElements(Collection<? extends E> elements) {
        	endKeyMapBuilder();
            this.collection = elements;
            return this;
        }

        public Builder<E> withElements(E... elements) {
        	endKeyMapBuilder();
            this.array = elements;
            return this;
        }

        //-- Keys

        public Builder<E> withElem() {
            return withElem(false);
        }

        public Builder<E> withElem(boolean orderBy) {
            getElemMapBuilder().orderBy = orderBy;
            return this;
        }

        /**
         * Determines whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param nullable  true to allow null elements, false to disallow
         * @return          this (for use in fluent interfaces)
         */
        public Builder<E> withElemNull(boolean allowNull) {
        	getElemMapBuilder().allowNull = allowNull;
        	return this;
        }

        /**
         * Determines whether duplicates are allowed or not.
         *
         * @param duplicates    true to allow duplicates, false to disallow
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E> withElemDuplicates(boolean allowDuplicates) {
        	getElemMapBuilder().allowDuplicates = allowDuplicates;
        	getElemMapBuilder().allowNullDuplicates = allowDuplicates;
            return this;
        }

        public Builder<E> withElemDuplicates(boolean allowDuplicates, boolean allowNullDuplicates) {
        	getElemMapBuilder().allowDuplicates = allowDuplicates;
        	getElemMapBuilder().allowNullDuplicates = allowDuplicates;
            return this;
        }

        /**
         * Determines that list should be sorted.
         *
         * @param sort    true to sort list, otherwise false
         * @return        this (for use in fluent interfaces)
         */
        public Builder<E> withElemSort(boolean sort) {
        	getElemMapBuilder().sort = sort;
            return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E> withElemSort(Comparator<? super E> comparator) {
            return withElemSort(comparator, false);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param comparatorSortsNull   true if comparator sorts null, false if not
         * @return                      this (for use in fluent interfaces)
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
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E> withElemSortNullsFirst(boolean nullsFirst) {
        	getElemMapBuilder().sortNullsFirst = nullsFirst;
            return this;
        }

        public Builder<E> withElemType(Class<?> type) {
        	getElemMapBuilder().type = type;
            return this;
        }

        /**
         * @return created SetList
         */
        public BagCollection<E> build() {
        	if (keyColl == null) {
               	keyColl = new BagCollection<E>();
        	}
        	build(keyColl);
        	return (BagCollection<E>) keyColl;
        }
    }

    private BagCollection() {
    }

	/**
     * {@inheritDoc}
     */
    public BagCollection<E> copy() {
        BagCollection<E> copy = new BagCollection<E>();
        copy.initCopy(this);
        return copy;
    }

    /**
     * Returns a copy this list but without elements.
     * The new list will use the same comparator, ordering, etc.
     *
     * @return  an empty copy of this instance
     */
    public BagCollection<E> crop() {
        BagCollection<E> copy = new BagCollection<E>();
        copy.initCrop(this);
        return copy;
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

    //-- Key methods

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

    @Override
    public boolean equals(Object obj) {
    	// Compare as List
    	if (obj instanceof List<?>) {
    		return super.equals(obj);
    	}

    	// Compare as Set (same functionality as in AbstractSet)
    	if (obj == this) {
    		return true;
    	}
		if (!(obj instanceof Set<?>)) {
		    return false;
		}
		Collection<?> coll = (Collection<?>) obj;
		if (coll.size() != size()) {
			return false;
		} else {
            return containsAll(coll);
		}
    }

    @Override
    public int hashCode() {
    	// Calculate hash code as Set (same functionality as in AbstractSet)
		int hash = 0;
		Iterator<E> iter = iterator();
		while (iter.hasNext()) {
			E obj = iter.next();
	        if (obj != null) {
	        	hash += obj.hashCode();
	        }
	    }
		return hash;
    }

}
