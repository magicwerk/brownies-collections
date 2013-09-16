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

import org.magicwerk.brownies.collections.function.Mapper;
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
 * @param <K> type of key
 */
public class Table1Collection<E,K> extends TableCollectionImpl<E> {

    /** UID for serialization. */
    private static final long serialVersionUID = 6181488174454611419L;

    /**
     * Builder to construct MapCollection instances.
     */
    public static class Builder<E,K> extends BuilderImpl<E> {
        /**
         * Default constructor.
         */
        public Builder() {
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

        //-- Element key

        @Override
        public Builder<E,K> withElem() {
        	return (Builder<E,K>) super.withElem();
        }

        @Override
        public Builder<E,K> withElem(boolean orderBy) {
        	return (Builder<E,K>) super.withElem(orderBy);
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
        public Builder<E,K> withElemSort() {
        	return (Builder<E,K>) super.withElemSort();
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
        public Builder<E,K> withElemList() {
        	return (Builder<E,K>) super.withElemList();
        }

        @Override
        public Builder<E,K> withElemList(Class<?> type) {
        	return (Builder<E,K>) super.withElemList(type);
        }

        // -- Key

        // @Override
        public Builder<E,K> withKey(Mapper<E,K> mapper) {
        	return (Builder<E,K>) super.withKey(mapper);
        }

        // @Override
        public Builder<E,K> withKey(Mapper<E,K> mapper, boolean orderBy) {
        	return (Builder<E,K>) super.withKey(mapper, orderBy);
        }

        @Override
        public Builder<E,K> withKeyNull(boolean allowNull) {
        	return (Builder<E,K>) super.withKeyNull(allowNull);
        }

        @Override
        public Builder<E,K> withKeyDuplicates(boolean allowDuplicates) {
        	return (Builder<E,K>) super.withKeyDuplicates(allowDuplicates);
        }

        @Override
        public Builder<E,K> withKeyDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	return (Builder<E,K>) super.withKeyDuplicates(allowDuplicates, allowDuplicatesNull);
        }

        @Override
        public Builder<E,K> withKeySort() {
        	return (Builder<E,K>) super.withKeySort();
        }

        @Override
        public Builder<E,K> withKeySort(Comparator<? super E> comparator) {
        	return (Builder<E,K>) super.withKeySort(comparator);
        }

        @Override
        public Builder<E,K> withKeySort(Comparator<? super E> comparator, boolean sortNullsFirst) {
        	return (Builder<E,K>) super.withKeySort(comparator, sortNullsFirst);
        }

        @Override
        public Builder<E,K> withKeyList() {
        	return (Builder<E,K>) super.withKeyList();
        }

        @Override
        public Builder<E,K> withKeyList(Class<?> type) {
        	return (Builder<E,K>) super.withKeyList(type);
        }

        /**
         * @return created SetList
         */
        public Table1Collection<E,K> build() {
        	if (tableColl == null) {
               	tableColl = new Table1Collection<E,K>();
        	}
        	build(tableColl);
        	return (Table1Collection<E,K>) tableColl;
        }
    }

    /**
     * Private constructor.
     */
    private Table1Collection() {
    }

    public Table1Collection<E,K> copy() {
        Table1Collection<E,K> copy = new Table1Collection<E,K>();
        copy.initCopy(this);
        return copy;
    }

    public Table1Collection<E,K> crop() {
        Table1Collection<E,K> copy = new Table1Collection<E,K>();
        copy.initCrop(this);
        return copy;
    }

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

	public GapList<E> getAllDistinct() {
		return (GapList<E>) super.getAllDistinctKeys(0);
	}

	public int getCountDistinct() {
		return super.getCountDistinctKeys(0);
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

	public GapList<K> getAllDistinctKeys() {
		return (GapList<K>) super.getAllDistinctKeys(1);
	}

	public int getCountDistinctKeys() {
		return super.getCountDistinctKeys(1);
	}

}
