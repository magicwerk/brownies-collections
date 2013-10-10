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

import org.magicwerk.brownies.collections.KeyCollectionImpl.BuilderImpl;
import org.magicwerk.brownies.collections.KeyList.Builder;
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
 * @version $Id$
 *
 * @see Key1List
 * @param <E> type of elements stored in the list
 * @param <K> type of key
 */
public class Key1List<E,K> extends KeyListImpl<E> {

    /** UID Key1Listization. */
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

        Builder(Key1List<E,K> keyList) {
        	this.keyList = keyList;
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

        @Override
        public Builder<E,K> withMaxSize(int maxSize) {
        	return (Builder<E,K>) super.withMaxSize(maxSize);
        }

        @Override
        public Builder<E,K> withWindowSize(int maxSize) {
        	return (Builder<E,K>) super.withWindowSize(maxSize);
        }

        //-- Element key

        @Override
        public Builder<E,K> withElem() {
        	return (Builder<E,K>) super.withElem();
        }

        @Override
        public Builder<E,K> withElemOrderBy(boolean orderBy) {
        	return (Builder<E,K>) super.withElemOrderBy(orderBy);
        }

        @Override
        public Builder<E,K> withElemOrderBy(Class<?> type) {
        	return (Builder<E,K>) super.withElemOrderBy(type);
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
        public Builder<E,K> withElemSort(boolean sort) {
        	return (Builder<E,K>) super.withElemSort(sort);
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
        public Builder<E,K> withPrimaryElem() {
        	return (Builder<E,K>) super.withPrimaryElem();
        }

        @Override
        public Builder<E,K> withUniqueElem() {
        	return (Builder<E,K>) super.withUniqueElem();
        }

        // -- Key

        // @Override
        public Builder<E,K> withKey1(Mapper<E,K> mapper) {
        	return (Builder<E,K>) super.withKey1(mapper);
        }

        // @Override
        public Builder<E,K> withKey1OrderBy(boolean orderBy) {
        	return (Builder<E,K>) super.withKey1OrderBy(orderBy);
        }

        @Override
        public Builder<E,K> withKey1OrderBy(Class<?> type) {
        	return (Builder<E,K>) super.withKey1OrderBy(type);
        }

        @Override
        public Builder<E,K> withKey1Null(boolean allowNull) {
        	return (Builder<E,K>) super.withKey1Null(allowNull);
        }

        @Override
        public Builder<E,K> withKey1Duplicates(boolean allowDuplicates) {
        	return (Builder<E,K>) super.withKey1Duplicates(allowDuplicates);
        }

        @Override
        public Builder<E,K> withKey1Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	return (Builder<E,K>) super.withKey1Duplicates(allowDuplicates, allowDuplicatesNull);
        }

        @Override
        public Builder<E,K> withKey1Sort(boolean sort) {
        	return (Builder<E,K>) super.withKey1Sort(sort);
        }

        @Override
        public Builder<E,K> withKey1Sort(Comparator<? super E> comparator) {
        	return (Builder<E,K>) super.withKey1Sort(comparator);
        }

        @Override
        public Builder<E,K> withKey1Sort(Comparator<? super E> comparator, boolean comparatorSortsNull) {
        	return (Builder<E,K>) super.withKey1Sort(comparator, comparatorSortsNull);
        }

        @Override
        public Builder<E,K> withPrimaryKey1() {
        	return (Builder<E,K>) super.withPrimaryKey1();
        }

        @Override
        public Builder<E,K> withUniqueKey1() {
        	return (Builder<E,K>) super.withUniqueKey1();
        }

        /**
         * @return created list
         */
        public Key1List<E,K> build() {
        	if (keyColl == null) {
               	keyColl = new KeyCollectionImpl<E>();
        	}
        	build(keyColl, true);
        	if (keyList == null) {
        		keyList = new Key1List<E,K>();
        	}
        	init(keyColl, keyList);
        	return (Key1List<E, K>) keyList;
        }
    }

    /**
     * Private constructor used by builder.
     */
    protected Key1List() {
    }

    protected Builder<E,K> getBuilder() {
    	return new Builder<E,K>(this);
    }

    @Override
    public Object clone() {
    	return copy();
    }

    @Override
    public Key1List<E,K> copy() {
    	Key1List<E,K> copy = new Key1List<E,K>();
        copy.initCopy(this);
        return copy;
    }

    @Override
    public Key1List<E,K> crop() {
    	Key1List<E,K> copy = new Key1List<E,K>();
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

	public GapList<E> getDistinct() {
		return (GapList<E>) super.getDistinctKeys(0);
	}

    //-- Key methods

    public int indexOfKey(K key) {
    	return super.indexOfKey(1, key);
    }

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

	public GapList<K> getDistinctKeys() {
		return (GapList<K>) super.getDistinctKeys(1);
	}

}
