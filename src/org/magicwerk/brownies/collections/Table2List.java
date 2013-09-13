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
import java.util.Map;

import org.magicwerk.brownies.collections.function.Mapper;
import org.magicwerk.brownies.collections.function.Predicate;
import org.magicwerk.brownies.collections.function.Trigger;


/**
 * Think about MapList as of a Map where you can also access the
 * elements by index. Also the key is automatically extracted
 * from the element. Typically the elements are in the order
 * specified by the list, but you can also let them order
 * automatically like in TreeMap.
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @see TableList
 * @param <E> type of elements stored in the list
 * @param <K1> type of first key
 * @param <K2> type of second key
 */
public class Table2List<E,K1,K2> extends TableListImpl<E> {

    /**
     * Builder to construct Map2List instances.
     */
    public static class Builder<E,K1,K2> extends BuilderBase<E> {
        /**
         * Constructor.
         */
        public Builder() {
        }

        /**
         * Internal constructor
         *
         * @param mapList   mapList to customize
         */
        Builder(Table2List<E,K1,K2> mapList) {
            this.keyList = mapList;
        }

        /**
         * Allow null elements.
         *
         * @return this (fluent interface)
         */
        public Builder<E,K1,K2> withNull() {
        	return withNull(true);
        }

        /**
         * Specify whether null elements are allowed.
         *
         * @param allowNullElem	true to allow null elements
         * @return 				this (fluent interface)
         */
        public Builder<E,K1,K2> withNull(boolean allowNullElem) {
        	endKeyMapBuilder();
        	this.allowNullElem = allowNullElem;
        	return this;
        }

        /**
         * Specify element constraint.
         *
         * @param constraint	constraint element must satisfy
         * @return 				this (fluent interface)
         */
        public Builder<E,K1,K2> withConstraint(Predicate<E> constraint) {
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
        public Builder<E,K1,K2> withInsertTrigger(Trigger<E> trigger) {
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
        public Builder<E,K1,K2> withDeleteTrigger(Trigger<E> trigger) {
        	endKeyMapBuilder();
            this.deleteTrigger = trigger;
            return this;
        }

        //-- Capacity / Elements

        public Builder<E,K1,K2> withCapacity(int capacity) {
        	endKeyMapBuilder();
            this.capacity = capacity;
            return this;
        }

        public Builder<E,K1,K2> withElements(Collection<? extends E> elements) {
        	endKeyMapBuilder();
            this.collection = elements;
            return this;
        }

        public Builder<E,K1,K2> withElements(E... elements) {
        	endKeyMapBuilder();
            this.array = elements;
            return this;
        }

        //-- Keys

        public Builder<E,K1,K2> withKey(Mapper<E,K1> mapper) {
        	endKeyMapBuilder();
        	newKeyMapBuilder((Mapper<E, Object>) mapper);
        	return this;
        }

        /**
         * Determines whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param nullable  true to allow null elements, false to disallow
         * @return          this (for use in fluent interfaces)
         */
        public Builder<E,K1,K2> withKeyNull(boolean nullable) {
            return withKeyNull(nullable ? NullMode.NORMAL : NullMode.NONE);
        }

        public Builder<E,K1,K2> withKeyNull(NullMode nullMode) {
        	getKeyMapBuilder().nullMode = nullMode;
            return this;
        }

        /**
         * Determines whether duplicates are allowed or not.
         *
         * @param duplicateMode duplicate mode
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E,K1,K2> withKeyDuplicates(DuplicateMode duplicateMode) {
        	getKeyMapBuilder().duplicateMode = duplicateMode;
            return this;
        }

        /**
         * Determines whether list should be sorted or not.
         *
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E,K1,K2> withKeySort() {
            return withKeySort(true);
        }

        /**
         * Determines that list should be sorted.
         *
         * @param sort    true to sort list, otherwise false
         * @return        this (for use in fluent interfaces)
         */
        public Builder<E,K1,K2> withKeySort(boolean sort) {
        	getKeyMapBuilder().sort = sort;
            return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E,K1,K2> withKeyComparator(Comparator<?> comparator) {
            return withKeyComparator(comparator, false);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param comparatorSortsNull   true if comparator sorts null, false if not
         * @return                      this (for use in fluent interfaces)
         */
        public Builder<E,K1,K2> withKeyComparator(Comparator<?> comparator, boolean comparatorSortsNull) {
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
        public Builder<E,K1,K2> withKeyNullsFirst() {
            return withKeyNullsFirst(true);
        }

        /**
         * Determines whether nulls are sorted first or last.
         *
         * @param nullsFirst    true to sort nulls first, false to sort nulls last
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E,K1,K2> withKeyNullsFirst(boolean nullsFirst) {
        	getKeyMapBuilder().sortNullsFirst = nullsFirst;
            return this;
        }

        /**
         * @return create Map2List
         */
        public Table2List<E,K1,K2> build() {
        	if (keyList == null) {
               	keyList = new Table2List<E,K1,K2>(false);
        	}
        	build(keyList);
        	return (Table2List<E,K1,K2>) keyList;
        }
    }

    /** UID for serialization. */
    private static final long serialVersionUID = -927503847084195522L;


    /**
     * Default constructor.
     * Internal use in builder and child classes only.
     *
     * @param ignore ignored parameter for unique method signature
     */
    protected Table2List(boolean ignore) {
    	super(ignore);
    }

    /**
     * Copy constructor.
     * Internal use in copy() and crop() only.
     *
     * @param that source list
     */
    Table2List(Table2List<E,K1,K2> that) {
        super(that);
    }

    /**
     * Create builder for this class.
     * Internal use in child classes only.
     *
     * @return builder for this class
     */
    protected Builder<E,K1,K2> getBuilder() {
        return new Table2List.Builder<E,K1,K2>();
    }

    //-- Key1 methods

    public Mapper<E,K1> getMapper1() {
    	return (Mapper<E,K1>) keyMaps[0].mapper;
    }

    public K1 getKey1(E elem) {
    	return getMapper1().getKey(elem);
    }

    public boolean containsKey1(K1 key) {
    	return super.containsKey(0, key);
    }

    public int indexOfKey1(K1 key) {
    	return super.indexOfKey(0, key);
    }

	public int getCountDistinctKeys1() {
		return super.getCountDistinctKeys(0);
	}

	public GapList<K1> getDistinctKeys1() {
		return (GapList<K1>) super.getDistinctKeys(0);
	}

	public E getByKey1(K1 key) {
		return super.getByKey(0, key);
	}

	public GapList<E> getAllByKey1(K1 key) {
		return super.getAllByKey(0, key);
	}

	public int getCountByKey1(K1 key) {
		return super.getCountByKey(0, key);
	}

	public E removeByKey1(K1 key) {
		return super.removeByKey(0, key);
	}

	public GapList<E> removeAllByKey1(K1 key) {
		return super.removeAllByKey(0, key);
	}

    //-- Key2 methods

    public Mapper<E,K2> getMapper2() {
    	return (Mapper<E,K2>) keyMaps[1].mapper;
    }

    public K2 getKey2(E elem) {
    	return getMapper2().getKey(elem);
    }

    public boolean containsKey2(K2 key) {
    	return super.containsKey(1, key);
    }

    public int indexOfKey2(K2 key) {
    	return super.indexOfKey(1, key);
    }

	public int getCountDistinctKeys2() {
		return super.getCountDistinctKeys(1);
	}

	public GapList<K1> getDistinctKeys2() {
		return (GapList<K1>) super.getDistinctKeys(1);
	}

	public E getByKey2(K2 key) {
		return super.getByKey(1, key);
	}

	public GapList<E> getAllByKey2(K2 key) {
		return super.getAllByKey(1, key);
	}

	public int getCountByKey2(K2 key) {
		return super.getCountByKey(1, key);
	}

	public E removeByKey2(K2 key) {
		return super.removeByKey(1, key);
	}

	public GapList<E> removeAllByKey2(K2 key) {
		return super.removeAllByKey(1, key);
	}

	//	public E put(E elem) {
//		K key = getMapper().getKey(elem);
//		int index = indexOfKey(0, key);
//		E oldElem;
//		if (index == -1) {
//			oldElem = null;
//			add(elem);
//		} else {
//			oldElem = get(index);
//			set(index, elem);
//		}
//		return oldElem;
//	}

    @Override
    public Table2List<E,K1,K2> copy() {
        Table2List<E,K1,K2> copy = new Table2List<E,K1,K2>(this);
        copy.initCopy(this);
        return copy;
    }

    /**
     * Returns a copy this list but without elements.
     * The new list will use the same comparator, ordering, etc.
     *
     * @return  an empty copy of this instance
     */
    public Table2List<E,K1,K2> crop() {
        Table2List<E,K1,K2> copy = new Table2List<E,K1,K2>(this);
        copy.initCrop(this);
        return copy;
    }

    /**
     * Returns a view on the MapList as Map.
     * Note that a class cannot implement both List and Map.
     * Reason is the conflicting definition of the remove() method:  <br/>
     * - Map:  V remove(Object key) <br/>
     * - List: boolean remove(Object obj) <br/>
     *
     * @return view of this MapList as Map
     */
    public Map<K1,E> asMap1() {
    	return Table1List.asMap(this, 0);
    }

    public Map<K1,E> asMap2() {
    	return Table1List.asMap(this, 1);
    }
}
