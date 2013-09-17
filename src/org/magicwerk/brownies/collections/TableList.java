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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.magicwerk.brownies.collections.TableCollection.Builder;
import org.magicwerk.brownies.collections.TableCollectionImpl.BuilderImpl;
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
 * @version $Id$
 *
 * @see Table1List
 * @param <E> type of elements stored in the list
 */
public class TableList<E> extends TableListImpl<E> {

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
        public Builder<E> withElem(boolean orderBy) {
        	return (Builder<E>) super.withElem(orderBy);
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
        public Builder<E> withElemSort() {
        	return (Builder<E>) super.withElemSort();
        }

        @Override
        public Builder<E> withElemSort(Comparator<? super E> comparator) {
        	return (Builder<E>) super.withElemSort(comparator);
        }

        @Override
        public Builder<E> withElemSort(Comparator<? super E> comparator, boolean sortNullsFirst) {
        	return (Builder<E>) super.withElemSort(comparator, sortNullsFirst);
        }

        @Override
        public Builder<E> withElemList() {
        	return (Builder<E>) super.withElemList();
        }

        @Override
        public Builder<E> withElemList(Class<?> type) {
        	return (Builder<E>) super.withElemList(type);
        }


        /**
         * Create collection with specified options.
         *
         * @return created collection
         */
        public TableList<E> build() {
        	if (tableColl == null) {
               	tableColl = new TableCollectionImpl<E>();
        	}
        	build(tableColl);
        	TableList<E> list = new TableList(tableColl);
        	fill(list);
        	return list;
        }
    }

    /**
     * Private constructor used by builder.
     */
    private TableList(TableCollectionImpl tableImpl) {
    	this.tableImpl = tableImpl;
    }

//    /**
//     * Create builder for this class.
//     * Internal use in child classes only.
//     *
//     * @return  builder for this class
//     */
//    protected TableList.Builder<E> getBuilder() {
//        return new TableList.Builder<E>(this);
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

	/**
     * {@inheritDoc}
     */
    @Override
    public TableList<E> copy() {
        TableList<E> copy = new TableList<E>(null);
        copy.initCopy(this);
        return copy;
    }

    /**
     * Returns a copy this list but without elements.
     * The new list will use the same comparator, ordering, etc.
     *
     * @return  an empty copy of this instance
     */
    public TableList<E> crop() {
        TableList<E> crop = new TableList<E>(null);
        crop.initCrop(this);
        return crop;
    }

    // -- Equals / hashCode

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
