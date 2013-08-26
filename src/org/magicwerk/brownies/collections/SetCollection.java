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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.magicwerk.brownies.collections.KeyCollection.BuilderBase;
import org.magicwerk.brownies.collections.KeyCollection.DuplicateMode;
import org.magicwerk.brownies.collections.KeyCollection.NullMode;
import org.magicwerk.brownies.collections.KeyCollection.BuilderBase.KeyMapBuilder;
import org.magicwerk.brownies.collections.MapList.Builder;
import org.magicwerk.brownies.collections.function.Predicate;
import org.magicwerk.brownies.collections.function.Trigger;
import org.magicwerk.brownies.collections.helper.IdentMapper;


/**
 * Think about SetList as of a Set where you can also access the
 * elements by index. Typically the elements are in the order
 * specified by the list, but you can also let them order
 * automatically like in TreeSet.
 *
 *
 * @author Thomas Mauch
 * @version $Id: SetList.java 1815 2013-08-09 00:05:35Z origo $
 *
 * @see MapList
 * @param <E> type of elements stored in the list
 */
public class SetCollection<E> extends KeyCollection<E> {

    /** UID for serialization. */
    private static final long serialVersionUID = 6181488174454611419L;

    /**
     * Builder to construct SetList instances.
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

        //-- Capacity / Elements

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

        /**
         * Determines whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param nullable  true to allow null elements, false to disallow
         * @return          this (for use in fluent interfaces)
         */
        public Builder<E> withKeyNull(boolean nullable) {
            return withKeyNull(nullable ? NullMode.NORMAL : NullMode.NONE);
        }

        public Builder<E> withKeyNull(NullMode nullMode) {
        	getKeyMapBuilder().nullMode = nullMode;
        	allowNullElem = (nullMode == NullMode.NONE ? false : true);
            return this;
        }

        /**
         * Determines whether duplicates are allowed or not.
         *
         * @param duplicates    true to allow duplicates, false to disallow
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E> withKeyDuplicates(DuplicateMode duplicateMode) {
        	getKeyMapBuilder().duplicateMode = duplicateMode;
            return this;
        }

        /**
         * Determines whether list should be sorted or not.
         *
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E> withKeySort() {
            return withKeySort(true);
        }

        /**
         * Determines that list should be sorted.
         *
         * @param sort    true to sort list, otherwise false
         * @return        this (for use in fluent interfaces)
         */
        public Builder<E> withKeySort(boolean sort) {
        	getKeyMapBuilder().sort = sort;
            return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E> withKeyComparator(Comparator<? super E> comparator) {
            return withKeyComparator(comparator, false);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param comparatorSortsNull   true if comparator sorts null, false if not
         * @return                      this (for use in fluent interfaces)
         */
        public Builder<E> withKeyComparator(Comparator<? super E> comparator, boolean comparatorSortsNull) {
        	KeyMapBuilder<E, Object> kmb = getKeyMapBuilder();
        	kmb.comparator = comparator;
            kmb.comparatorSortsNull = comparatorSortsNull;
            return this;
        }

        /**
         * Determines that nulls are sorted first.
         *
         * @return  this (for use in fluent interfaces)
         */
        public Builder<E> withKeyNullsFirst() {
            return withKeyNullsFirst(true);
        }

        /**
         * Determines whether nulls are sorted first or last.
         *
         * @param nullsFirst    true to sort nulls first, false to sort nulls last
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E> withKeyNullsFirst(boolean nullsFirst) {
        	getKeyMapBuilder().sortNullsFirst = nullsFirst;
            return this;
        }

        /**
         * @return created SetList
         */
        public SetCollection<E> build() {
        	if (keyColl == null) {
               	keyColl = new SetCollection<E>(false);
        	}
        	build(keyColl);
        	return (SetCollection<E>) keyColl;
        }
    }

    /**
     * Copy constructor.
     * Internal use in copy() and crop() only.
     *
     * @param that  source list
     */
    SetCollection(SetCollection<E> that) {
        super(that);
    }

    /**
     * Default constructor.
     * Internal use in builder and child classes only.
     *
     * @param ignore ignored parameter for unique method signature
     */
    protected SetCollection(boolean ignore) {
        super(ignore);
    }

    /**
     * Create builder for this class.
     * Internal use in child classes only.
     *
     * @return  builder for this class
     */
    protected SetCollection.Builder<E> getBuilder() {
        return new SetCollection.Builder<E>(this);
    }

    // SetList constructors

    public SetCollection() {
        super(false);
        getBuilder().build();
    }

    public SetCollection(int capacity) {
        super(false);
    	getBuilder().withCapacity(capacity).build();
    }

    public SetCollection(Collection<? extends E> elements) {
        super(false);
    	getBuilder().withElements(elements).build();
    }

    public SetCollection(E... elements) {
        super(false);
    	getBuilder().withElements(elements).build();
    }

    // Create SetList

    public static <E> SetCollection<E> create() {
    	return new Builder<E>().build();
    }

    public static <E> SetCollection<E> create(int capacity) {
    	return new Builder<E>().withCapacity(capacity).build();
    }

    public static <E> SetCollection<E> create(Collection<? extends E> elements) {
    	return new Builder<E>().withElements(elements).build();
    }

    public static <E> SetCollection<E> create(E... elements) {
    	return new Builder<E>().withElements(elements).build();

    }

    //-- Key methods

    public boolean containsKey(E key) {
    	return super.containsKey(0, key);
    }

    public int indexOfKey(E key) {
    	return super.indexOfKey(0, key);
    }

	public int getCountDistinctKeys() {
		return super.getCountDistinctKeys(0);
	}

	public GapList<Object> getDistinctKeys() {
		return super.getDistinctKeys(0);
	}

	public E getByKey(E key) {
		return super.getByKey(0, key);
	}

	public GapList<E> getAllByKey(E key) {
		return super.getAllByKey(0, key);
	}

	public int getCountByKey(E key) {
		return super.getCountByKey(0, key);
	}

	public E removeByKey(E key) {
		return super.removeByKey(0, key);
	}

	public GapList<E> removeAllByKey(E key) {
		return super.removeAllByKey(0, key);
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public SetCollection<E> copy() {
        SetCollection<E> copy = new SetCollection<E>(this);
        copy.initCopy(this);
        return copy;
    }

    /**
     * Returns a copy this list but without elements.
     * The new list will use the same comparator, ordering, etc.
     *
     * @return  an empty copy of this instance
     */
    public SetCollection<E> crop() {
        SetCollection<E> copy = new SetCollection<E>(this);
        copy.initCrop(this);
        return copy;
    }

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
