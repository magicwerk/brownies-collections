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

import org.magicwerk.brownies.collections.TableCollectionImpl.BuilderImpl;
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
 * @see Table2List
 * @param <E> type of elements stored in the list
 * @param <K> type of key
 */
public class Table2List<E,K1,K2> extends TableListImpl<E> {

    /** UID for serialization. */
    private static final long serialVersionUID = 6181488174454611419L;

    /**
     * Builder to construct MapCollection instances.
     */
    public static class Builder<E,K1,K2> extends BuilderImpl<E> {
        /**
         * Default constructor.
         */
        public Builder() {
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
        public Builder<E,K1,K2> withElements(Collection<? extends E> elements) {
        	return (Builder<E,K1,K2>) super.withElements(elements);
        }

        @Override
        public Builder<E,K1,K2> withElements(E... elements) {
        	return (Builder<E,K1,K2>) super.withElements(elements);
        }

        //-- Element key

        @Override
        public Builder<E,K1,K2> withElem() {
        	return (Builder<E,K1,K2>) super.withElem();
        }

        @Override
        public Builder<E,K1,K2> withElemOrderBy(boolean orderBy) {
        	return (Builder<E,K1,K2>) super.withElemOrderBy(orderBy);
        }

        @Override
        public Builder<E,K1,K2> withElemOrderBy(Class<?> type) {
        	return (Builder<E,K1,K2>) super.withElemOrderBy(type);
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

        // -- Key 1

        // @Override
        public Builder<E,K1,K2> withKey1(Mapper<E,K1> mapper) {
        	return (Builder<E,K1,K2>) super.withKey(mapper);
        }

        // @Override
        public Builder<E,K1,K2> withKey1OrderBy(boolean orderBy) {
        	return (Builder<E,K1,K2>) super.withKey1OrderBy(orderBy);
        }

        @Override
        public Builder<E,K1,K2> withKey1OrderBy(Class<?> type) {
        	return (Builder<E,K1,K2>) super.withKey1OrderBy(type);
        }

        @Override
        public Builder<E,K1,K2> withKey1Null(boolean allowNull) {
        	return (Builder<E,K1,K2>) super.withKeyNull(allowNull);
        }

        @Override
        public Builder<E,K1,K2> withKey1Duplicates(boolean allowDuplicates) {
        	return (Builder<E,K1,K2>) super.withKeyDuplicates(allowDuplicates);
        }

        @Override
        public Builder<E,K1,K2> withKey1Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	return (Builder<E,K1,K2>) super.withKeyDuplicates(allowDuplicates, allowDuplicatesNull);
        }

        @Override
        public Builder<E,K1,K2> withKey1Sort(boolean sort) {
        	return (Builder<E,K1,K2>) super.withKeySort(sort);
        }

        @Override
        public Builder<E,K1,K2> withKey1Sort(Comparator<? super E> comparator) {
        	return (Builder<E,K1,K2>) super.withKeySort(comparator);
        }

        @Override
        public Builder<E,K1,K2> withKey1Sort(Comparator<? super E> comparator, boolean sortNullsFirst) {
        	return (Builder<E,K1,K2>) super.withKeySort(comparator, sortNullsFirst);
        }

        // -- Key 2

        // @Override
        public Builder<E,K1,K2> withKey2(Mapper<E,K1> mapper) {
        	return (Builder<E,K1,K2>) super.withKey(mapper);
        }

        // @Override
        public Builder<E,K1,K2> withKey2OrderBy(boolean orderBy) {
        	return (Builder<E,K1,K2>) super.withKey2OrderBy(orderBy);
        }

        @Override
        public Builder<E,K1,K2> withKey2OrderBy(Class<?> type) {
        	return (Builder<E,K1,K2>) super.withKey2OrderBy(type);
        }

        @Override
        public Builder<E,K1,K2> withKey2Null(boolean allowNull) {
        	return (Builder<E,K1,K2>) super.withKeyNull(allowNull);
        }

        @Override
        public Builder<E,K1,K2> withKey2Duplicates(boolean allowDuplicates) {
        	return (Builder<E,K1,K2>) super.withKeyDuplicates(allowDuplicates);
        }

        @Override
        public Builder<E,K1,K2> withKey2Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	return (Builder<E,K1,K2>) super.withKeyDuplicates(allowDuplicates, allowDuplicatesNull);
        }

        @Override
        public Builder<E,K1,K2> withKey2Sort(boolean sort) {
        	return (Builder<E,K1,K2>) super.withKeySort(sort);
        }

        @Override
        public Builder<E,K1,K2> withKey2Sort(Comparator<? super E> comparator) {
        	return (Builder<E,K1,K2>) super.withKeySort(comparator);
        }

        @Override
        public Builder<E,K1,K2> withKey2Sort(Comparator<? super E> comparator, boolean sortNullsFirst) {
        	return (Builder<E,K1,K2>) super.withKeySort(comparator, sortNullsFirst);
        }

        /**
         * @return created SetList
         */
        public Table2List<E,K1,K2> build() {
        	if (tableColl == null) {
               	tableColl = new TableCollectionImpl<E>();
        	}
        	build(tableColl, true);
        	Table2List<E,K1,K2> list = new Table2List();
        	fill(tableColl, list);
        	return list;
        }
    }

    /**
     * Private constructor used by builder.
     */
    private Table2List() {
    }

    public Table2List<E,K1,K2> copy() {
    	Table2List<E,K1,K2> copy = new Table2List<E,K1,K2>();
        copy.initCopy(this);
        return copy;
    }

    public Table2List<E,K1,K2> crop() {
    	Table2List<E,K1,K2> copy = new Table2List<E,K1,K2>();
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

    //-- Key 1 methods

    public boolean containsKey1(K1 key) {
    	return super.containsKey(1, key);
    }

	public E getByKey1(K1 key) {
		return super.getByKey(1, key);
	}

	public GapList<E> getAllByKey1(K1 key) {
		return super.getAllByKey(1, key);
	}

	public int getCountByKey1(K1 key) {
		return super.getCountByKey(1, key);
	}

	public E removeByKey1(K1 key) {
		return super.removeByKey(1, key);
	}

	public GapList<E> removeAllByKey1(K1 key) {
		return super.removeAllByKey(1, key);
	}

	public GapList<K1> getDistinctKeys1() {
		return (GapList<K1>) super.getDistinctKeys(1);
	}

    //-- Key 2 methods

    public boolean containsKey2(K2 key) {
    	return super.containsKey(2, key);
    }

	public E getByKey2(K2 key) {
		return super.getByKey(2, key);
	}

	public GapList<E> getAllByKey2(K2 key) {
		return super.getAllByKey(2, key);
	}

	public int getCountByKey2(K2 key) {
		return super.getCountByKey(2, key);
	}

	public E removeByKey2(K2 key) {
		return super.removeByKey(2, key);
	}

	public GapList<E> removeAllByKey2(K2 key) {
		return super.removeAllByKey(2, key);
	}

	public GapList<K2> getDistinctKeys2() {
		return (GapList<K2>) super.getDistinctKeys(2);
	}

}
