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
 * $Id$
 */
package org.magicwerk.brownies.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.magicwerk.brownies.collections.KeyList.DuplicateMode;
import org.magicwerk.brownies.collections.KeyList.Handler;
import org.magicwerk.brownies.collections.KeyList.NullMode;
import org.magicwerk.brownies.collections.MapList.Builder;


/**
 * Think about SetList as of a Set where you can also access the
 * elements by index. Typically the elements are in the order
 * specified by the list, but you can also let them order
 * automatically like in TreeSet.
 * SetList offers the put() method which is missing in java.util.Set.
 *
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @see MapList
 * @param <E> type of elements stored in the list
 */
public class SetList<E> extends KeyList<E, E> implements Set<E> {

    /** UID for serialization. */
    private static final long serialVersionUID = 6181488174454611419L;

    /**
     * Identity mapper.
     */
    static class IdentMapper<E> implements Mapper<E, E> {
        @Override
        public E getKey(E v) {
            return v;
        }
    }

    /**
     * Builder to construct SetList instances.
     */
    public static class Builder<E> extends KeyList.Builder<E, E> {

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Internal constructor
         *
         * @param setList   setList to customize
         */
        Builder(SetList<E> setList) {
            this.keyList = setList;
        }

        /**
         * Build SetList with specified options.
         *
         * @return created SetList
         */
        public SetList<E> build() {
        	return (SetList<E>) doBuild();
        }

        // --- Methods overridden to change return type

		@Override
		public Builder<E> withCapacity(int capacity) {
			return (Builder<E>) super.withCapacity(capacity);
		}

		@Override
		public Builder<E> withDuplicates(DuplicateMode mode) {
			return (Builder<E>) super.withDuplicates(mode);
		}

		@Override
		public Builder<E> withElements(Collection<? extends E> elements) {
			return (Builder<E>) super.withElements(elements);
		}

		@Override
		public Builder<E> withElements(E... elements) {
			return (Builder<E>) super.withElements(elements);
		}

		@Override
		public Builder<E> withNull(boolean allowNull) {
			return (Builder<E>) super.withNull(allowNull);
		}

		@Override
		public Builder<E> withNull(NullMode nullMode) {
			return (Builder<E>) super.withNull(nullMode);
		}

		@Override
		public Builder<E> withNullsFirst() {
			return (Builder<E>) super.withNullsFirst();
		}

		@Override
		public Builder<E> withNullsFirst(boolean nullsFirst) {
			return (Builder<E>) super.withNullsFirst(nullsFirst);
		}

		@Override
		public Builder<E> withSort() {
			return (Builder<E>) super.withSort();
		}

		@Override
		public Builder<E> withComparator(Comparator<? super E> comparator) {
			return (Builder<E>) super.withComparator(comparator);
		}
    }

    /**
     * Copy constructor.
     * Internal use in copy() and crop() only.
     *
     * @param that  source list
     */
    SetList(SetList<E> that) {
        super(that);
    }

    /**
     * Default constructor.
     * Internal use in builder and child classes only.
     */
    protected SetList(boolean ignore) {
        super();
    }

    /**
     * Create builder for this class.
     * Internal use in child classes only.
     *
     * @return  builder for this class
     */
    protected Builder<E> getBuilder() {
        return new Builder<E>(this);
    }

    public static <E> SetList.Builder<E> builder() {
        return new SetList.Builder<E>();
    }

    // SetList constructors

    public SetList() {
    	getBuilder().build();
    }

    public SetList(int capacity) {
    	getBuilder().withCapacity(capacity).build();
    }

    public SetList(Collection<? extends E> elements) {
    	getBuilder().withElements(elements).build();
    }

    public SetList(E... elements) {
    	getBuilder().withElements(elements).build();

    }

    // Create SetList

    public static <E> SetList<E> create() {
    	return new Builder<E>().build();
    }

    public static <E> SetList<E> create(int capacity) {
    	return new Builder<E>().withCapacity(capacity).build();
    }

    public static <E> SetList<E> create(Collection<? extends E> elements) {
    	return new Builder<E>().withElements(elements).build();
    }

    public static <E> SetList<E> create(E... elements) {
    	return new Builder<E>().withElements(elements).build();

    }

    // Create HashSet

    public static <E> SetList<E> createHashSet() {
    	return new Builder<E>().withNull(NullMode.NORMAL).build();
    }

    public SetList<E> createHashSet(int capacity) {
    	return new Builder<E>().withNull(NullMode.NORMAL).withCapacity(capacity).build();
    }

    public SetList<E> createHashSet(Collection<? extends E> elements) {
    	return new Builder<E>().withNull(NullMode.NORMAL).withElements(elements).build();
    }

    public SetList<E> createHashSet(E... elements) {
    	return new Builder<E>().withNull(NullMode.NORMAL).withElements(elements).build();
    }

    // Create TreeSet

    public SetList<E> createTreeSet() {
    	return new Builder<E>().withSort().withNull(NullMode.NORMAL).build();
    }

    public SetList<E> createTreeSet(int capacity) {
    	return new Builder<E>().withSort().withNull(NullMode.NORMAL).withCapacity(capacity).build();
    }

    public SetList<E> createTreeSet(Collection<? extends E> elements) {
    	return new Builder<E>().withSort().withNull(NullMode.NORMAL).withElements(elements).build();
    }

    public SetList<E> createTreeSet(E... elements) {
    	return new Builder<E>().withSort().withNull(NullMode.NORMAL).withElements(elements).build();
    }

    // TreeSet with comparator

    public SetList<E> createTreeSet(Comparator<? super E> comparator) {
    	return new Builder<E>().withComparator(comparator).withNull(NullMode.NORMAL).build();
    }

    public SetList<E> createTreeSet(Comparator<? super E> comparator, int capacity) {
    	return new Builder<E>().withComparator(comparator).withNull(NullMode.NORMAL).withCapacity(capacity).build();
    }

    public SetList<E> createTreeSet(Comparator<? super E> comparator, Collection<? extends E> elements) {
    	return new Builder<E>().withComparator(comparator).withNull(NullMode.NORMAL).withElements(elements).build();
    }

    public SetList<E> createTreeSet(Comparator<? super E> comparator, E... elements) {
    	return new Builder<E>().withComparator(comparator).withNull(NullMode.NORMAL).withElements(elements).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SetList<E> copy() {
        SetList<E> copy = new SetList<E>(this);
        copy.initCopy(this);
        return copy;
    }

    /**
     * Returns a copy this list but without elements.
     * The new list will use the same comparator, ordering, etc.
     *
     * @return  an empty copy of this instance
     */
    public SetList<E> crop() {
        SetList<E> copy = new SetList<E>(this);
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
