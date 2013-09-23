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

import org.magicwerk.brownies.collections.Table1Collection.Builder;
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
 * @see Table1List
 * @param <E> type of elements stored in the list
 */
public class TableCollection<E> extends TableCollectionImpl<E> {

    /** UID for serialization. */
    private static final long serialVersionUID = 6181488174454611419L;

    /**
     * Builder to construct TableCollection instances.
     */
    public static class Builder<E> extends BuilderImpl<E> {
        /**
         * Default constructor.
         */
        public Builder() {
        }

        // -- Constraint

        @Override
        public Builder<E> withNull(boolean allowNull) {
        	return (Builder<E>) super.withNull(allowNull);
        }

        @Override
        public Builder<E> withConstraint(Predicate<E> constraint) {
        	return (Builder<E>) super.withConstraint(constraint);
        }

        // -- Triggers

        @Override
        public Builder<E> withInsertTrigger(Trigger<E> trigger) {
        	return (Builder<E>) super.withInsertTrigger(trigger);
        }

        @Override
        public Builder<E> withDeleteTrigger(Trigger<E> trigger) {
        	return (Builder<E>) super.withDeleteTrigger(trigger);
        }

        //-- Content

        @Override
        public Builder<E> withCapacity(int capacity) {
        	return (Builder<E>) super.withCapacity(capacity);
        }

        @Override
        public Builder<E> withElements(Collection<? extends E> elements) {
        	return (Builder<E>) super.withElements(elements);
        }

        @Override
        public Builder<E> withElements(E... elements) {
        	return (Builder<E>) super.withElements(elements);
        }

        //-- Element key

        @Override
        public Builder<E> withElem() {
        	return (Builder<E>) super.withElem();
        }

        @Override
        public Builder<E> withElemOrderBy(boolean orderBy) {
        	return (Builder<E>) super.withElemOrderBy(orderBy);
        }

        @Override
        public Builder<E> withElemNull(boolean allowNull) {
        	return (Builder<E>) super.withElemNull(allowNull);
        }

        @Override
        public Builder<E> withElemDuplicates(boolean allowDuplicates) {
        	return (Builder<E>) super.withElemDuplicates(allowDuplicates);
        }

        @Override
        public Builder<E> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	return (Builder<E>) super.withElemDuplicates(allowDuplicates, allowDuplicatesNull);
        }

        @Override
        public Builder<E> withElemSort(boolean sort) {
        	return (Builder<E>) super.withElemSort(sort);
        }

        @Override
        public Builder<E> withElemSort(Comparator<? super E> comparator) {
        	return (Builder<E>) super.withElemSort(comparator);
        }

        @Override
        public Builder<E> withElemSort(Comparator<? super E> comparator, boolean sortNullsFirst) {
        	return (Builder<E>) super.withElemSort(comparator, sortNullsFirst);
        }

        /**
         * Create collection with specified options.
         *
         * @return created collection
         */
        public TableCollection<E> build() {
        	// Constructs builder if there is none
        	if (tableColl == null) {
               	tableColl = new TableCollection<E>();
        	}
        	build(tableColl, false);
        	fill(tableColl);
        	return (TableCollection<E>) tableColl;
        }
    }

    /**
     * Private constructor used by builder.
     */
    private TableCollection() {
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

	public GapList<E> getDistinct() {
		return (GapList<E>) super.getDistinctKeys(0);
	}

	//-- Other methods

    // TODO what about clone()?
    /**
     * Returns a copy of this collection.
     * The new collection will use the same comparator, ordering, etc.
     *
     * @return  an empty copy of this instance
     */
    public TableCollection<E> copy() {
        TableCollection<E> copy = new TableCollection<E>();
        copy.initCopy(this);
        return copy;
    }

    /**
     * Returns a copy of this collection.
     * The new collection will use the same comparator, ordering, etc.
     *
     * @return  a copy of this instance
     */
    public TableCollection<E> crop() {
        TableCollection<E> copy = new TableCollection<E>();
        copy.initCrop(this);
        return copy;
    }

}
