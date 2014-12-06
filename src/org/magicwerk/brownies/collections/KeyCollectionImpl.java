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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.magicwerk.brownies.collections.exceptions.DuplicateKeyException;
import org.magicwerk.brownies.collections.exceptions.KeyException;
import org.magicwerk.brownies.collections.function.Mapper;
import org.magicwerk.brownies.collections.function.Predicate;
import org.magicwerk.brownies.collections.function.Trigger;
import org.magicwerk.brownies.collections.helper.GapLists;
import org.magicwerk.brownies.collections.helper.IdentMapper;
import org.magicwerk.brownies.collections.helper.NaturalComparator;
import org.magicwerk.brownies.collections.helper.NullComparator;
import org.magicwerk.brownies.collections.helper.Option;
import org.magicwerk.brownies.collections.helper.SortedLists;


/**
 * Add:
 * - validation fails: null / constraint
 * - duplicate not allowed (mode replace)
 *
 * Triggers:
 * - triggers are called after the add/remove operation has finished
 * - if an exception is thrown in the trigger, the change already made to the collection is not undone
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @see GapList
 * @param <E> type of elements stored in the list
 */
public class KeyCollectionImpl<E> implements Collection<E>, Serializable, Cloneable {

	/**
     * Implementation of builder.
     */
    public static class BuilderImpl<E> {

        public static class KeyMapBuilder<E,K> {
        	/** True order collection by this key map */
        	Boolean orderBy;
            // -- sorted list
            /** Primitive class to use for list storage */
            Class<?> orderByType;
        	// -- mapper
        	Mapper<E,K> mapper;
            // -- null
            Boolean allowNull;
            // -- duplicates
            Boolean allowDuplicates;
            boolean allowDuplicatesNull;
            // -- sorted list
            /** True to sort using natural comparator */
            Boolean sort;
            /** Comparator to use for sorting */
            Comparator<?> comparator;
            /** The specified comparator can handle null values */
            boolean comparatorSortsNull;
            /** Determine whether null values appear first or last */
            boolean sortNullsFirst;
        }

    	// KeyList to build
    	KeyCollectionImpl keyColl;
    	KeyListImpl keyList;
    	// -- constraint
        boolean allowNullElem = true;
        Predicate<E> constraint;
        // -- triggers
        Trigger<E> beforeInsertTrigger;
        Trigger<E> afterInsertTrigger;
        Trigger<E> beforeDeleteTrigger;
        Trigger<E> afterDeleteTrigger;
        // -- keys
    	GapList<KeyMapBuilder<E,Object>> keyMapBuilders = GapList.create();
        // -- content
        Collection<? extends E> collection;
        E[] array;
        int capacity;
        int maxSize;
        Boolean movingWindow;
        /** True to count only number of occurrences of equal elements */
        boolean count;

        // Interface

        /**
         * Specifies whether null elements are allowed or not.
         * A null element will have a null key.
         * This method has the same effect as {@link #withElemNull}.
         *
         * @param allowNull true to allow null elements (default), false to disallow
         * @return          this (fluent interfaces)
         */
        protected BuilderImpl<E> withNull(boolean allowNull) {
        	this.allowNullElem = allowNull;
        	if (hasElemMapBuilder()) {
        		getKeyMapBuilder(0).allowNull = allowNull;
        	}
        	return this;
        }

        /**
         * Specify element constraint.
         *
         * @param constraint	constraint element must satisfy, null for none (default)
         * @return 				this (fluent interface)
         */
        protected BuilderImpl<E> withConstraint(Predicate<E> constraint) {
        	this.constraint = constraint;
        	return this;
        }

        // -- Triggers

        /**
         * Specify insert trigger.
         *
         * @param trigger	insert trigger method, null for none (default)
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withBeforeInsertTrigger(Trigger<E> trigger) {
            this.beforeInsertTrigger = trigger;
            return this;
        }

        /**
         * Specify insert trigger.
         *
         * @param trigger	insert trigger method, null for none (default)
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withAfterInsertTrigger(Trigger<E> trigger) {
            this.afterInsertTrigger = trigger;
            return this;
        }

        /**
         * Specify delete trigger.
         *
         * @param trigger	delete trigger method, null for none (default)
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withBeforeDeleteTrigger(Trigger<E> trigger) {
            this.beforeDeleteTrigger = trigger;
            return this;
        }
        /**
         * Specify delete trigger.
         *
         * @param trigger	delete trigger method, null for none (default)
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withAfterDeleteTrigger(Trigger<E> trigger) {
            this.afterDeleteTrigger = trigger;
            return this;
        }

        //-- Content

        /**
         * Specify initial capacity.
         *
         * @param capacity	initial capacity
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        /**
         * Specify elements added to the collection upon creation.
         *
         * @param elements	initial elements
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withContent(Collection<? extends E> elements) {
            this.collection = elements;
            return this;
        }

        /**
         * Specify elements added to the collection upon creation.
         *
         * @param elements	initial elements
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withContent(E... elements) {
            this.array = elements;
            return this;
        }

        /**
         * Specify maximum size of collection.
         * If an attempt is made to add more elements, an exception is thrown.
         *
         * @param maxSize	maximum size
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withMaxSize(int maxSize) {
        	if (movingWindow != null) {
        		throw new IllegalArgumentException("maximum or window size alreay set");
        	}
            this.maxSize = maxSize;
            this.movingWindow = false;
            return this;
        }

        /**
         * Specify maximum window size of collection.
         * If an attempt is made to add and additional element, the first element is removed.
         *
         * @param maxSize	maximum window size
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withWindowSize(int maxSize) {
        	if (movingWindow != null) {
        		throw new IllegalArgumentException("maximum or window size alreay set");
        	}
            this.maxSize = maxSize;
            this.movingWindow = true;
            return this;
        }

        //-- Element key

        /**
         * Specifies that the collection only counts the number of occurrences
         * of equal elements, but does not store the elements themselves.
         *
         * @param count	true to count only number of occurrences (default is false)
         * @return		this (fluent interface)
         */
        protected BuilderImpl<E> withElemCount(boolean count) {
        	this.count = count;
        	return this;
        }

        /**
         * Add element map (with ident mapper).
         *
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withElemSet() {
            return withKeyMap(0, IdentMapper.INSTANCE);
        }

        /**
         * Specifies that the collection will have the order of this map.
         * The map must be sorted, if no sort order has been defined,
         * the natural comparator will be used. If the map allows null
         * values, the used comparator will sort them last.
         *
         * @param orderBy	if true the collection will have the order of this map
         * 					(default is false, only one map can have this option set)
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withElemOrderBy(boolean orderBy) {
            return withKeyOrderBy(0, orderBy);
        }

        /**
         * Specifies that the list will have the order of this map.
         * The map will store values of the primitive type specified like <code>int</code>.
         * The map will be sorted using the natural comparator and no null values are allowed.
         *
         * @param type	primitive type to use for map (only one map can have the order by option set)
         * @return		this (fluent interface)
         */
        // only for KeyList
        protected BuilderImpl<E> withElemOrderBy(Class<?> type) {
            return withKeyOrderBy(0, type);
        }

        /**
         * Specifies whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param allowNull true to allow null elements, false to disallow (default is true)
         * @return          this (fluent interfaces)
         */
        protected BuilderImpl<E> withElemNull(boolean allowNull) {
        	return withKeyNull(0, allowNull);
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates   true to allow duplicates (default is true)
         * @return              	this (fluent interfaces)
         */
        protected BuilderImpl<E> withElemDuplicates(boolean allowDuplicates) {
        	return withElemDuplicates(allowDuplicates, allowDuplicates);
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates		true to allow duplicates (default is true)
         * @param allowDuplicatesNull	true to allow duplicate null values (default is true)
         * @return						this (fluent interfaces)
         */
        protected BuilderImpl<E> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	return withKeyDuplicates(0, allowDuplicates, allowDuplicatesNull);
        }

        /**
         * Specify that the collection should be sorted using the natural comparator.
         * If the collection supports null values, they are sorted last.
         *
         * @param sort    true to sorted, false for unsorted (default is false)
         * @return        this (fluent interface)
         */
        protected BuilderImpl<E> withElemSort(boolean sort) {
        	return withKeySort(0, sort);
        }

        /**
         * Set comparator to use for sorting.
         * If the collection allows null values, the comparator must be able to compare null values.
         * If the comparator does not support null values, use withElemSort(Comparator, boolean) to
         * explicitly specify how null values should be sorted.
         *
         * @param comparator    comparator to use for sorting (null for natural comparator)
         * @return              this (fluent interface)
         */
        protected BuilderImpl<E> withElemSort(Comparator<? super E> comparator) {
        	return withKeySort(0, comparator);
        }

        /**
         * Set comparator to use for sorting.
         * This method should be used if the collection can contain null values, but the comparator
         * is not able to handle them. The parameter sortNullsFirst determine how the null values
         * should be sorted.
         *
         * @param comparator           comparator to use for sorting
         * @param sortNullsFirst	   true to sort null values first, false for last
         * @return                     this (fluent interface)
         */
        protected BuilderImpl<E> withElemSort(Comparator<? super E> comparator, boolean sortNullsFirst) {
        	return withKeySort(0, comparator, sortNullsFirst);
        }

        /**
         * Specify the element to be a primary key.
         * This is identical to calling
         * withElemNull(false) and withElemDuplicates(false).
         *
         * @return	this (fluent interface)
         */
        protected BuilderImpl<E> withPrimaryElem() {
        	return withPrimaryKey(0);
        }

        /**
         * Specify the element to be a unique key.
         * This is identical to calling
         * withElemNull(true) and withElemDuplicates(false, true).
         *
         * @return	this (fluent interface)
         */
        protected BuilderImpl<E> withUniqueElem() {
            return withUniqueKey(0);
        }

        //

        protected BuilderImpl<E> withKeyMap(int keyIndex, Mapper mapper) {
        	if (mapper == null) {
        		throw new IllegalArgumentException("Mapper may not be null");
        	}
        	KeyMapBuilder kmb = getKeyMapBuilder(keyIndex);
        	if (kmb.mapper != null) {
        		throw new IllegalArgumentException("Mapper already set");
        	}
        	kmb.mapper = mapper;
            return this;
        }

        protected BuilderImpl<E> withKeyOrderBy(int keyIndex, boolean orderBy) {
        	KeyMapBuilder kmb = getKeyMapBuilder(keyIndex);
        	if (kmb.orderBy != null) {
        		throw new IllegalArgumentException("Order by already set");
        	}
        	kmb.orderBy = orderBy;
        	kmb.orderByType = null;
            return this;
        }

        protected BuilderImpl<E> withKeyOrderBy(int keyIndex, Class<?> type) {
        	if (type == null) {
        		throw new IllegalArgumentException("Order by type may not be null");
        	}
        	KeyMapBuilder kmb = getKeyMapBuilder(keyIndex);
        	if (kmb.orderBy != null) {
        		throw new IllegalArgumentException("Order by already set");
        	}
        	kmb.orderBy = true;
        	kmb.orderByType = type;
            return this;
        }

        protected BuilderImpl<E> withKeyNull(int keyIndex, boolean allowNull) {
        	KeyMapBuilder kmb = getKeyMapBuilder(keyIndex);
        	if (kmb.allowNull != null) {
        		throw new IllegalArgumentException("AllowNull already set");
        	}
        	kmb.allowNull = allowNull;
        	if (keyIndex == 0) {
        		allowNullElem = allowNull;
        	}
        	return this;
        }

        protected BuilderImpl<E> withKeyDuplicates(int keyIndex, boolean allowDuplicates, boolean allowDuplicatesNull) {
        	KeyMapBuilder kmb = getKeyMapBuilder(keyIndex);
        	if (kmb.allowDuplicates != null) {
        		throw new IllegalArgumentException("AllowDuplicates already set");
        	}
        	kmb.allowDuplicates = allowDuplicates;
        	kmb.allowDuplicatesNull = allowDuplicatesNull;
            return this;
        }

        protected BuilderImpl<E> withKeySort(int keyIndex, boolean sort) {
        	KeyMapBuilder kmb = getKeyMapBuilder(keyIndex);
        	if (kmb.sort != null) {
        		throw new IllegalArgumentException("Sort already set");
        	}
        	kmb.sort = sort;
        	kmb.comparator = null;
        	kmb.comparatorSortsNull = false;
        	kmb.sortNullsFirst = false;
            return this;
        }

        protected BuilderImpl<E> withKeySort(int keyIndex, Comparator<?> comparator) {
        	KeyMapBuilder kmb = getKeyMapBuilder(keyIndex);
        	if (kmb.sort != null) {
        		throw new IllegalArgumentException("Sort already set");
        	}
        	kmb.sort = true;
        	kmb.comparator = comparator;
        	kmb.comparatorSortsNull = true;
        	kmb.sortNullsFirst = false;
        	return this;
        }

        protected BuilderImpl<E> withKeySort(int keyIndex, Comparator<?> comparator, boolean sortNullsFirst) {
        	KeyMapBuilder kmb = getKeyMapBuilder(keyIndex);
        	if (kmb.sort != null) {
        		throw new IllegalArgumentException("Sort already set");
        	}
        	kmb.sort = true;
        	kmb.comparator = comparator;
        	kmb.comparatorSortsNull = false;
        	kmb.sortNullsFirst = sortNullsFirst;
            return this;
        }

        protected BuilderImpl<E> withPrimaryKey(int keyIndex) {
        	withKeyNull(keyIndex, false);
        	withKeyDuplicates(keyIndex, false, false);
            return this;
        }

        protected BuilderImpl<E> withUniqueKey(int keyIndex) {
        	withKeyNull(keyIndex, true);
        	withKeyDuplicates(keyIndex, false, true);
            return this;
        }

        // -- Key1

        /**
         * Add key map.
         *
         * @param orderBy	true to force the collection to have the order of this map
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withKey1OrderBy(boolean orderBy) {
            return withKeyOrderBy(1, orderBy);
        }

        /**
         * Specify element type to use.
         *
         * @param type	type to use
         * @return		this (fluent interface)
         */
        protected BuilderImpl<E> withKey1OrderBy(Class<?> type) {
            return withKeyOrderBy(1, type);
        }

        /**
         * Specify whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param allowNull true to allow null elements, false to disallow
         * @return          this (fluent interfaces)
         */
        protected BuilderImpl<E> withKey1Null(boolean allowNull) {
        	return withKeyNull(1, allowNull);
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates   true to allow duplicates
         * @return              	this (fluent interfaces)
         */
        protected BuilderImpl<E> withKey1Duplicates(boolean allowDuplicates) {
            return withKeyDuplicates(1, allowDuplicates, allowDuplicates);
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates		true to allow duplicates
         * @param allowDuplicatesNull	true to allow duplicate null values
         * @return						this (fluent interfaces)
         */
        protected BuilderImpl<E> withKey1Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
            return withKeyDuplicates(1, allowDuplicates, allowDuplicatesNull);
        }

        /**
         * Determines that list should be sorted.
         *
         * @param sort	  ture to sort list
         * @return        this (fluent interface)
         */
        protected BuilderImpl<E> withKey1Sort(boolean sort) {
        	return withKeySort(1, sort);
        }

        /**
         * Specify this key to be a primary key.
         * This is identical to calling
         * withKey1Null(false) and withKey1Duplicates(false).
         *
         * @return	this (fluent interface)
         */
        protected BuilderImpl<E> withPrimaryKey1() {
        	return withPrimaryKey(1);
        }

        /**
         * Specify this key to be a unique key.
         * This is identical to calling
         * withKey1Null(true) and withKey1Duplicates(false, true).
         *
         * @return	this (fluent interface)
         */
        protected BuilderImpl<E> withUniqueKey1() {
            return withUniqueKey(1);
        }

        // -- Key2

        /**
         * Add key map.
         *
         * @param orderBy	true to force the collection to have the order of this map
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withKey2OrderBy(boolean orderBy) {
            return withKeyOrderBy(2, orderBy);
        }

        /**
         * Specify element type to use.
         *
         * @param type	type to use
         * @return		this (fluent interface)
         */
        protected BuilderImpl<E> withKey2OrderBy(Class<?> type) {
            return withKeyOrderBy(2, type);
        }

        /**
         * Specify whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param allowNull true to allow null elements, false to disallow
         * @return          this (fluent interfaces)
         */
        protected BuilderImpl<E> withKey2Null(boolean allowNull) {
        	return withKeyNull(2, allowNull);
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates   true to allow duplicates
         * @return              	this (fluent interfaces)
         */
        protected BuilderImpl<E> withKey2Duplicates(boolean allowDuplicates) {
        	return withKeyDuplicates(2, allowDuplicates, allowDuplicates);
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates		true to allow duplicates
         * @param allowDuplicatesNull	true to allow duplicate null values
         * @return						this (fluent interfaces)
         */
        protected BuilderImpl<E> withKey2Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
            return withKeyDuplicates(2, allowDuplicates, allowDuplicatesNull);
        }

        /**
         * Determines that list should be sorted.
         *
         * @param sort    true to sort keys
         * @return        this (fluent interface)
         */
        protected BuilderImpl<E> withKey2Sort(boolean sort) {
        	return withKeySort(2, sort);
        }

        /**
         * Specify this key to be a primary key.
         * This is identical to calling
         * withKey2Null(false) and withKey2Duplicates(false).
         *
         * @return	this (fluent interface)
         */
        protected BuilderImpl<E> withPrimaryKey2() {
        	return withPrimaryKey(2);
        }

        /**
         * Specify this key to be a unique key.
         * This is identical to calling
         * withKey1Null(true) and withKey1Duplicates(false, true).
         *
         * @return	this (fluent interface)
         */
        protected BuilderImpl<E> withUniqueKey2() {
            return withUniqueKey(2);
        }


        //-- Implementation

        /**
         * @param numKeys	number of keys
         */
        void initKeyMapBuilder(int numKeys) {
        	assert(numKeys >= 0);
        	// add 1 for elem key
        	keyMapBuilders.init(numKeys+1, null);
        }

        boolean hasElemMapBuilder() {
        	return keyMapBuilders.size() > 0 && keyMapBuilders.get(0) != null;
        }

        KeyMapBuilder<E, Object> getKeyMapBuilder(int index) {
        	int size = keyMapBuilders.size();
    		for (int i=size; i<=index; i++) {
    			keyMapBuilders.add(i, null);
    		}
        	KeyMapBuilder kmb = keyMapBuilders.get(index);
        	if (kmb == null) {
        		kmb = new KeyMapBuilder();
        		keyMapBuilders.set(index, kmb);
        	}
        	return kmb;
        }

        boolean isTrue(Boolean b) {
        	return b != null && b;
        }

        boolean isFalse(Boolean b) {
        	return !(b != null && !b);
        }

        KeyMap buildKeyMap(KeyMapBuilder keyMapBuilder, boolean list) {
        	KeyMap<E,Object> keyMap = new KeyMap<E,Object>();
        	keyMap.mapper = (Mapper<E, Object>) keyMapBuilder.mapper;
            keyMap.allowNull = isFalse(keyMapBuilder.allowNull);
        	keyMap.allowDuplicates = isFalse(keyMapBuilder.allowDuplicates);
        	keyMap.allowDuplicatesNull = isFalse(keyMapBuilder.allowDuplicatesNull);

        	if (isTrue(keyMapBuilder.sort) || isTrue(keyMapBuilder.orderBy)) {
                if (keyMapBuilder.comparator == null) {
                	if (keyMap.allowNull) {
                    	keyMap.comparator = new NullComparator(NaturalComparator.INSTANCE(), keyMapBuilder.sortNullsFirst);
                	} else {
                    	keyMap.comparator = NaturalComparator.INSTANCE();
                	}
                } else {
                    if (!keyMapBuilder.comparatorSortsNull && keyMap.allowNull) {
                    	keyMap.comparator = new NullComparator(keyMapBuilder.comparator, keyMapBuilder.sortNullsFirst);
                    } else {
                    	keyMap.comparator = (Comparator<Object>) keyMapBuilder.comparator;
                    }
                }
        	}

        	if (list && isTrue(keyMapBuilder.orderBy)) {
        		if (keyMapBuilder.orderByType == null) {
            		keyMap.keysList = new GapList<Object>();
        		} else {
                	if (keyMapBuilder.comparator != null && keyMapBuilder.comparator != NaturalComparator.INSTANCE()) {
                		throw new IllegalArgumentException("Only natural comparator supported for list type");
                	}
                	if (isTrue(keyMapBuilder.allowNull)) {
                		throw new IllegalArgumentException("Null values are not supported for primitive list type");
                	}
                	keyMap.comparator = NaturalComparator.INSTANCE();
        			keyMap.keysList = (IList<Object>) GapLists.createWrapperList(keyMapBuilder.orderByType);
        		}
        	} else if (keyMap.comparator != null) {
        		keyMap.keysMap = new TreeMap(keyMap.comparator);
        	} else {
        		keyMap.keysMap = new HashMap();
        	}

            return keyMap;
        }

        /**
         * Initialize KeyCollectionImpl.
         *
         * @param keyColl collection to initialize
         * @param list    true if a KeyListImpl is built up, false for KeyCollectionImpl
         */
        void build(KeyCollectionImpl keyColl, boolean list) {
        	keyColl.allowNullElem = allowNullElem;
            keyColl.constraint = constraint;
            keyColl.beforeInsertTrigger = beforeInsertTrigger;
            keyColl.afterInsertTrigger = afterInsertTrigger;
            keyColl.beforeDeleteTrigger = beforeDeleteTrigger;
            keyColl.afterDeleteTrigger = afterDeleteTrigger;
            keyColl.maxSize = maxSize;
            keyColl.movingWindow = isTrue(movingWindow);

            int orderByKey = -1;
            int size = keyMapBuilders.size();
            if (size == 1 && keyMapBuilders.get(0) == null) {
            	if (!list) {
            		withElemSet();
            	} else {
            		size = 0;
            	}
            }
            if (size > 0) {
	            keyColl.keyMaps = new KeyMap[size];
	            for (int i=0; i<size; i++) {
	            	KeyMapBuilder kmb = keyMapBuilders.get(i);
	            	if (kmb == null) {
	            		if (i != 0) {
	            			throw new IllegalArgumentException("Key " + i + " is not defined");
	            		}
	            	} else {
	            		if (isTrue(kmb.orderBy)) {
	            			if (orderByKey != -1) {
	            				throw new IllegalArgumentException("Only one order by key allowed");
	            			}
	            			orderByKey = i;
	            		}
	                	if (kmb.mapper == null) {
	                		if (i == 0) {
	                			kmb.mapper = IdentMapper.INSTANCE;
	                		} else {
		            			throw new IllegalArgumentException("No mapper for key " + i + " defined");
	                		}
	                	}
		            	keyColl.keyMaps[i] = buildKeyMap(kmb, list);
		            	if (i == 0) {
		            		keyColl.keyMaps[i].count = count;
		            	}
	            	}
	            }
            }

            // TableCollectionImpl must have a defined order,
            // TableListImpl will use the list order
            if (orderByKey == -1 && !list) {
	            if (keyColl.keyMaps != null) {
	            	if (keyColl.keyMaps[0] != null) {
	            		orderByKey = 0;
	            	} else {
	            		assert(keyColl.keyMaps[1] != null);
	            		orderByKey = 1;
	            	}
            	}
            }
            keyColl.orderByKey = orderByKey;
        }

        /**
         * This method is called if a KeyCollection, Key1Collection, Key2Collection is initialized.
         *
         * @param keyColl
         * @param keyList
         */
        void init(KeyCollectionImpl keyColl) {
            if (collection != null) {
            	keyColl.addAll(collection);
            } else if (array != null) {
            	keyColl.addAll((Collection<? extends E>) Arrays.asList(array));
            }
        }

        /**
         * This method is called if a KeyList, Key1List, Key2List is initialized.
         *
         * @param keyColl
         * @param keyList
         */
        void init(KeyCollectionImpl keyColl, KeyListImpl keyList) {
        	keyList.keyColl = keyColl;
        	keyColl.keyList = keyList;
        	if (keyColl.orderByKey == 0) {
        		keyList.forward = (IList<E>) keyColl.keyMaps[0].keysList;
                if (collection != null) {
                	keyColl.addAll(collection);
                } else if (array != null) {
                	keyColl.addAll((Collection<? extends E>) Arrays.asList(array));
                }
        	} else {
        		keyList.forward = new GapList<E>(); // TODO
        		if (collection != null) {
        			keyList.ensureCapacity(capacity);
        			keyList.addAll(collection);
        		} else if (array != null) {
        			keyList.ensureCapacity(capacity);
        			keyList.addAll(array);
        		} else if (capacity != 0) {
        			keyList.ensureCapacity(capacity);
        		}
        	}
        }
    }

    static class KeyMap<E,K> implements Serializable {
	    /** A mapper to extract keys out of element. For the element itself , this is always an IdentMapper. */
	    Mapper<E,K> mapper;
	    /** True to allow null keys */
	    boolean allowNull;
	    /** True to allow duplicate values if they are not null */
	    boolean allowDuplicates;
	    /** True to allow duplicate null values */
	    boolean allowDuplicatesNull;
	    /** Comparator to use for sorting (if null, elements are not sorted) */
	    Comparator<K> comparator;
	    /**
	     * Key storage if not sorted. The values are single elements or a list of elements.
	     * Note that we cannot use TreeMap as K may not be comparable.
	     * One of keysMap or keysList is used.
	     */
	    Map<K, Object> keysMap;
	    /** Key storage if this is a sorted KeyListImpl, otherwise null */
	    IList<K> keysList;
	    /** True to count only number of occurrences of equal elements */
	    boolean count;

	    KeyMap() {
	    }

	    KeyMap copy() {
	    	KeyMap copy = new KeyMap();
	    	copy.mapper = mapper;
	    	copy.allowNull = allowNull;
	    	copy.allowDuplicates = allowDuplicates;
	    	copy.allowDuplicatesNull = allowDuplicatesNull;
	    	copy.comparator = comparator;
	    	copy.count = count;
	    	if (keysMap != null) {
	    		if (keysMap instanceof HashMap) {
	    			copy.keysMap = (Map) ((HashMap) keysMap).clone();
	    		} else {
	    			copy.keysMap = (Map) ((TreeMap) keysMap).clone();
	    		}

	    		// The map cannot only contain simple value, but also instances of KeyMapList
	    		// for duplicates which must be also be cloned
	    		for (Object obj: copy.keysMap.entrySet()) {
	    			Map.Entry entry = (Map.Entry) obj;
	    			Object val = entry.getValue();
	    			if (val instanceof KeyMapList) {
	    				val = new KeyMapList((KeyMapList) val);
	    				entry.setValue(val);
	    			}
	    		}
	    	} else {
	    		copy.keysList = keysList.copy();
	    	}
	    	return copy;
	    }

	    KeyMap crop() {
	    	KeyMap copy = new KeyMap();
	    	copy.mapper = mapper;
	    	copy.allowNull = allowNull;
	    	copy.allowDuplicates = allowDuplicates;
	    	copy.allowDuplicatesNull = allowDuplicatesNull;
	    	copy.comparator = comparator;
	    	copy.count = count;
	    	if (keysMap != null) {
	    		if (keysMap instanceof HashMap) {
	    			copy.keysMap = new HashMap();
	    		} else {
	    			copy.keysMap = new TreeMap();
	    		}
	    	} else {
	    		copy.keysList = new GapList();
	    	}
	    	return copy;
	    }

	    K getKey(E elem) {
	    	if (elem == null) {
	            return null;
	    	}
	    	return mapper.getKey(elem);
	    }

	    boolean containsKey(Object key) {
	        if (key == null) {
	            if (!allowNull) {
	                return false;
	            }
	        }
	    	if (keysMap != null) {
    			return keysMap.containsKey(key);
	    	} else {
	    		return keysList.binarySearch(key, (Comparator<Object>) comparator) >= 0;
	    	}
	    }

	    boolean containsValue(Object value) {
	    	assert(count == false);
	    	if (keysMap != null) {
    			return keysMap.containsValue(value);
	    	} else {
	    		return keysList.binarySearch(value, (Comparator<Object>) comparator) >= 0;
	    	}
	    }

	    @SuppressWarnings("unchecked")
		Iterator<E> iteratorValues(KeyCollectionImpl keyColl) {
	    	assert(keysMap != null);
    		if (count) {
    			return (Iterator<E>) new KeyMapCountIter(keyColl, this, keysMap);
    		} else {
    			return (Iterator<E>) new KeyMapIter(keyColl, this, keysMap);
    		}
	    }

	    static class KeyMapIter<E,K> implements Iterator<E> {

	    	KeyCollectionImpl tableColl;
	    	KeyMap<E,K> keyMap;
	    	Iterator<Object> mapIter;
	    	Iterator<E> listIter;
	    	boolean hasElem;
	    	E elem;

	    	public KeyMapIter(KeyCollectionImpl tableColl, KeyMap<E,K> keyMap, Map<K,Object> map) {
	    		this.tableColl = tableColl;
	    		this.keyMap = keyMap;
	    		this.mapIter = map.values().iterator();
	    	}

			@Override
			public boolean hasNext() {
				boolean hasNext = false;
				if (listIter != null) {
					hasNext = listIter.hasNext();
				}
				if (!hasNext) {
					hasNext = mapIter.hasNext();
				}
				return hasNext;
			}

			@Override
			public E next() {
				// Reset hasElem so it is false if a call to Iterator.next()
				// fails with NoSuchElementException
				hasElem = false;

				boolean hasNext = false;
				elem = null;
				if (listIter != null) {
					if (listIter.hasNext()) {
						hasNext = true;
						elem = listIter.next();
					} else {
						listIter = null;
					}
				}
				if (!hasNext) {
					// This call can fail with NoSuchElementException
					Object o = mapIter.next();
					if (o instanceof KeyMapList) {
						listIter = ((KeyMapList) o).iterator();
						elem = listIter.next();
					} else {
						elem = (E) o;
					}
				}
				hasElem = true;
				return elem;
			}

			@Override
			public void remove() {
				if (!hasElem) {
					throw new IllegalStateException("No current element to remove");
				}
				hasElem = false;

				if (listIter != null) {
					listIter.remove();
				} else {
					mapIter.remove();
				}
				tableColl.remove(elem, keyMap);
			}
	    }

	    static class KeyMapCountIter<E,K> implements Iterator<E> {

	    	KeyCollectionImpl keyColl;
	    	KeyMap<E,K> keyMap;
	    	Map<K,Object> map;
	    	Iterator<Entry<K, Object>> mapIter;
	    	E elem;
	    	int count;
	    	boolean hasElem;

	    	public KeyMapCountIter(KeyCollectionImpl keyColl, KeyMap<E,K> keyMap, Map<K,Object> map) {
	    		this.keyColl = keyColl;
	    		this.keyMap = keyMap;
	    		this.map = map;
	    		this.mapIter = map.entrySet().iterator();
	    	}

			@Override
			public boolean hasNext() {
				boolean hasNext = false;
				if (count > 0) {
					hasNext = true;
				}
				if (!hasNext) {
					hasNext = mapIter.hasNext();
				}
				return hasNext;
			}

			@Override
			public E next() {
				// Reset hasElem so it is false if a call to Iterator.next()
				// fails with NoSuchElementException
				hasElem = false;

				if (count > 0) {
					count--;
				} else {
					// This call can fail with NoSuchElementException
					Entry<K, Object> o = mapIter.next();
					elem = (E) o.getKey();
					count = (Integer) o.getValue();
					count--;
				}

				hasElem = true;
				return elem;
			}

			@Override
			public void remove() {
				if (!hasElem) {
					throw new IllegalStateException("No current element to remove");
				}
				hasElem = false;

				Integer val = (Integer) map.get(elem);
				if (val == 1) {
					mapIter.remove();
				} else {
					map.put((K) elem, val-1);
				}
			}
	    }

	    /**
	     * Add element to key map.
	     *
	     * @param key	key of element
	     * @param elem	element
	     */
	    void add(K key, E elem) {
	    	if (key == null) {
	    		if (!allowNull) {
	    			errorNullKey();
	    		}
	    	}
	        if (keysMap != null) {
	            // Keys not sorted
	        	Object newElem = (count ? 1 : elem);
	        	int oldSize = keysMap.size();
	        	Object oldElem = keysMap.put(key, newElem);
	        	boolean hasOldElem;
	        	if (oldElem != null) {
	        		hasOldElem = true;
	        	} else {
	        		if (key == null) {
	        			hasOldElem = (keysMap.size() == oldSize);
	        		} else {
	        			hasOldElem = false;
	        		}
	        	}

	        	if (!hasOldElem) {
	        		// There was no old element, so it was correct to just add the new one

	        	} else {
	        		if (!(allowDuplicates || (key == null && allowDuplicatesNull))) {
	        			// Revert change and raise error
	        			keysMap.put(key, oldElem);
		        		errorDuplicateKey(key);
	        		}

		        	if (count) {
		    		    if (oldElem != null) {
			    			Integer val = (Integer) oldElem;
	    		    		keysMap.put(key, val+1);
		    		    }
		        	} else {
		        		GapList<E> list;
		        		if (oldElem instanceof KeyMapList) {
		        			list = (GapList<E>) oldElem;
		        			list.add(elem);
		        		} else {
		    	            list = (GapList<E>) new KeyMapList();
		    	            list.addAll((E) oldElem, elem);
		        		}
	    	            keysMap.put(key, list);
		        	}
	        	}

	        } else {
	            // Sorted keys
	        	int addIndex = 0;
	        	if (!keysList.isEmpty()) {
	        		if (comparator.compare(key, keysList.getLast()) > 0) {
	        			addIndex = -keysList.size() - 1;
	        		} else if (comparator.compare(key, keysList.getFirst()) < 0) {
	        			addIndex = -1;
	        		}
	        	}
	        	if (addIndex == 0) {
	        		addIndex = SortedLists.binarySearchAdd(keysList, key, comparator);
	        	}
	        	boolean add = false;
	            if (addIndex < 0) {
	                // New key
	                addIndex = -addIndex - 1;
	        		add = true;
	        	} else {
	        		// Existing key
	                if (allowDuplicates ||
	                	(key == null && allowDuplicatesNull)) {
	                	add = true;
	                }
	        	}
	        	if (!add) {
	        		errorDuplicateKey(key);
	        	}
	            keysList.doAdd(addIndex, key);
	       	}
	    }

	    /**
	     * Remove element from key map.
	     *
	     * @param key			key of object to remove
	     * @param matchValue	true if value must match to remove entry
	     * @param value			value of object to remove
	     * @param keyColl		key collection which stores object
	     * @return				removed object
	     */
	    Option<E> remove(Object key, boolean matchValue, Object value, KeyCollectionImpl keyColl) {
	    	// If list cannot contain null, handle null explicitly to prevent NPE
	    	if (key == null) {
	    		if (!allowNull) {
	    			return Option.EMPTY();
	    		}
	    	}

	    	if (keysMap != null) {
	        	if (!keysMap.containsKey(key)) {
	        		return Option.EMPTY();
	        	}
	        	if (count) {
	        		assert(!matchValue || key == value);
		            Integer val = (Integer) keysMap.get(key);
		            if (val == 1) {
		            	keysMap.remove(key);
		            } else {
		            	keysMap.put((K) key, val-1);
		            }
			        return new Option(key);
	        	} else {
		        	E elem = null;
		            Object obj = keysMap.get(key);
			        if (obj instanceof KeyMapList) {
			            GapList<E> list = (GapList<E>) obj;
			            if (matchValue) {
			            	if (!list.remove(value)) {
			            		return Option.EMPTY();
			            	} else {
			            		elem = (E) value;
			            	}
			            } else {
			            	elem = list.removeFirst();
			            }
			            if (list.isEmpty()) {
			                keysMap.remove(key);
			            }
			        } else {
			            elem = (E) keysMap.remove(key);
			        }
			        return new Option(elem);
	        	}
	    	} else {
	    		int index = keysList.binarySearch(key, (Comparator<Object>) comparator);
	    		E elem = null;
	    		if (index < 0) {
	    			return Option.EMPTY();
	    		}
	    		elem = (E) keyColl.keyList.doGet(index);
    			keysList.remove(index);
	    		return new Option(elem);
	    	}
	    }

		public GapList<Object> getValues(int capacity) {
			GapList<Object> list = null;
	        if (keysMap != null) {
	        	list = new GapList(capacity);
	        	for (Object obj: keysMap.values()) {
			        if (obj instanceof KeyMapList) {
			        	list.addAll((GapList) obj);
			        } else {
			        	list.add(obj);
			        }
	        	}
	        } else {
	        	list = (GapList<Object>) keysList.unmodifiableList();
	        }
        	assert(keysList.size() == capacity);
			return list;
		}

		Set<K> getDistinctKeys() {
	        if (keysMap != null) {
	        	Set<K> set = keysMap.keySet();
	        	if (comparator != null) {
	        		TreeSet treeSet = new TreeSet(comparator);
	        		treeSet.addAll(set);
	        		return treeSet;
	        	} else {
	        		return new HashSet(set);
	        	}
	        } else {
	            K lastKey = null;
	            TreeSet<K> set = new TreeSet<K>(comparator);
	            for (int i=0; i<keysList.size(); i++) {
	                K key = keysList.get(i);
	                boolean add = false;
	                if (set.isEmpty()) {
	                    add = true;
	                } else {
	                    if (key != null) {
	                        add = !key.equals(lastKey);
	                    } else {
	                        add = (lastKey != null);
	                    }
	                }
	                if (add) {
	                    set.add(key);
	                    lastKey = key;
	                }
	            }
	            return set;
	        }
		}
    }

    /**
     * List type used to store multiple elements.
     * We need this distinct type to distinguish it from a normal GapList
     * in a KeyCollection&lt;GapList&lt;String&gt;&gt;.
     */
    static class KeyMapList<E> extends GapList<E> {
    	public KeyMapList() {
    		super();
    	}
    	public KeyMapList(KeyMapList that) {
    		super(that);
    	}
    }

    //-- KeyCollection --

    /** If true the invariants the GapList are checked for debugging */
    private static final boolean DEBUG_CHECK = true; // TODO

    /**
     * Size of collection.
     * The size is cached, as the key maps do not know the size if duplicates are allowed.
     */
    int size;
    /** Maximum absolute or windows size, 0 if this list has no size restriction */
    int maxSize;
    /** If maxSize is >0, this boolean indicates whether the size is for window (true) or absolute (false) */
    boolean movingWindow;
    /**
     * Maps for element and all defined keys.
     * keyMaps may be null for a KeyListImpl without keys.
     * Index 0 is reserved for the elem key using an IdentMapper.
     * If there is no elem key, keyMaps[0] contains null.
     */
    KeyMap<E, Object>[] keyMaps;
    /**
     * Index of key map which defines order
     * (-1 for no order, only possible for KeyList).
     * If an order key is defined for a KeyList, it must be implemented as KeyMap.keysList.
     */
    int orderByKey;
	/**
	 * True to allow null elements, false to reject them.
	 */
    boolean allowNullElem;
    /**
     * All elements in the list must fulfill this predicate, if null, all elements are allowed
     */
    Predicate<E> constraint;
    // -- handlers
    Trigger<E> beforeInsertTrigger;
    Trigger<E> afterInsertTrigger;
    Trigger<E> beforeDeleteTrigger;
    Trigger<E> afterDeleteTrigger;
    /**
     * Back pointer to KeyListImpl if this object is used to implement a KeyList, Key1List, Key2List.
     * Otherwise null if it is part of a KeyCollection, Key1Collection, Key2Collection.
     */
    KeyListImpl keyList;

    /**
     * Private constructor.
     */
    KeyCollectionImpl() {
    }

    /**
     * Initialize object for copy() operation.
     *
     * @param that source object
     */
    void initCopy(KeyCollectionImpl that) {
    	size = that.size;
    	if (that.keyMaps != null) {
	    	keyMaps = new KeyMap[that.keyMaps.length];
	    	for (int i=0; i<keyMaps.length; i++) {
	    		if (that.keyMaps[i] != null) {
	    			keyMaps[i] = that.keyMaps[i].copy();
	    		}
	    	}
    	}
    	allowNullElem = that.allowNullElem;
    	constraint = that.constraint;
    	beforeInsertTrigger = that.beforeInsertTrigger;
    	afterInsertTrigger = that.afterInsertTrigger;
    	beforeDeleteTrigger = that.beforeDeleteTrigger;
    	afterDeleteTrigger = that.afterDeleteTrigger;
    	orderByKey = that.orderByKey;
    }

    /**
     * Initialize object for crop() operation.
     *
     * @param that source object
     */
    void initCrop(KeyCollectionImpl that) {
    	size = 0;
    	if (that.keyMaps != null) {
	    	keyMaps = new KeyMap[that.keyMaps.length];
	    	for (int i=0; i<keyMaps.length; i++) {
	    		if (that.keyMaps[i] != null) {
	    			keyMaps[i] = that.keyMaps[i].crop();
	    		}
	    	}
    	}
    	allowNullElem = that.allowNullElem;
    	constraint = that.constraint;
    	beforeInsertTrigger = that.beforeInsertTrigger;
    	afterInsertTrigger = that.afterInsertTrigger;
    	beforeDeleteTrigger = that.beforeDeleteTrigger;
    	afterDeleteTrigger = that.afterDeleteTrigger;
    	orderByKey = that.orderByKey;
    }

    /**
     * Private method to check invariant of KeyCollectionImpl.
     * It is only used for debugging.
     */
    void debugCheck() {
    	if (keyMaps != null) {
    		for (KeyMap<E,?> keyMap: keyMaps) {
    			if (keyMap != null) {
    				doDebugCheck(keyMap);
    			}
    		}
    	}
    }

    private void doDebugCheck(KeyMap keyMap) {
    	if (keyMap.keysMap != null) {
    		int count = 0;
    		if (keyMap.count) {
	    		for (Object val: keyMap.keysMap.values()) {
    				count += ((Integer) val);
	    		}
    		} else {
	    		for (Object obj: keyMap.keysMap.values()) {
	    			if (obj instanceof KeyMapList) {
	    				count += ((KeyMapList) obj).size();
	    			} else {
	    				count++;
	    			}
	    		}
    		}
    		assert(count == size());
    	} else if (keyMap.keysList != null) {
    		assert(keyMap.keysList.size() == size());
    		IList<?> copy = keyMap.keysList.copy();
    		copy.sort(keyMap.comparator);
    		assert(copy.equals(keyMap.keysList));
    	} else {
    		assert(false);
    	}
    }

    // for KeyListImpl

    Object getKey(int keyIndex, E elem) {
    	return keyMaps[keyIndex].getKey(elem);
    }

    boolean isSortedList() {
    	return orderByKey != -1;
    }

	boolean hasElemSet() {
		return keyMaps != null && keyMaps[0] != null;
	}

    void checkIndex(int loIndex, int hiIndex, E elem) {
   		KeyMap keyMap = keyMaps[orderByKey];
    	Object key = keyMap.getKey(elem);
    	IList<Object> list = keyMap.keysList;
    	Comparator<Object> comp = keyMap.comparator;
    	if (loIndex >= 0) {
    		int cmp = comp.compare(list.doGet(loIndex), key);
    		if (cmp == 0) {
    			if (elem != null) {
    				if (!keyMap.allowDuplicates) {
    					cmp = 1;
    				}
    			} else {
    				if (!keyMap.allowDuplicatesNull) {
    					cmp = 1;
    				}
    			}
    		}
    		if (cmp > 0) {
    			errorInvalidIndex();
    		}
    	}
    	if (hiIndex < list.size()) {
    		int cmp = comp.compare(key, list.doGet(hiIndex));
    		if (cmp == 0) {
    			if (elem != null) {
    				if (!keyMap.allowDuplicates) {
    					cmp = 1;
    				}
    			} else {
    				if (!keyMap.allowDuplicatesNull) {
    					cmp = 1;
    				}
    			}
    		}
			if (cmp > 0) {
    			errorInvalidIndex();
    		}
    	}
    }

    // Called from KeyListImpl.doAdd
    void addSorted(int index, E elem) {
    	// Check whether index is correct for adding element in a sorted list
    	checkIndex(index-1, index, elem);

    	beforeInsert(elem);

    	// Index is correct
   		KeyMap keyMap = keyMaps[orderByKey];
    	Object key = keyMap.getKey(elem);
    	IList<Object> list = keyMap.keysList;

   		doAdd(elem, keyMap);
    	list.doAdd(index, key);
   		size++;

   		afterInsert(elem);
   }

    // Called from KeyListImpl.doAdd
    void addUnsorted(E elem) {
    	beforeInsert(elem);
    	doAdd(elem, null);
    	size++;
    	afterInsert(elem);
    }

    // Called from KeyListImpl.doSet
   	void setSorted(int index, E elem, E oldElem) {
   		// Check whether index is correct for setting element in a sorted list
    	checkIndex(index-1, index+1, elem);

    	// Index is correct
   		KeyMap keyMap = keyMaps[orderByKey];
    	Object key = keyMap.getKey(elem);
    	IList<Object> list = keyMap.keysList;

    	beforeDelete(oldElem);
    	beforeInsert(elem);
    	doRemove(oldElem, keyMap);
    	try {
    		doAdd(elem, keyMap);
    	}
    	catch (RuntimeException e) {
    		doAdd((E) oldElem, keyMap);
    		throw e;
    	}
    	list.doSet(index, key);
    	afterDelete(elem);
    	afterInsert(elem);
    }

    int binarySearchSorted(E elem) {
    	KeyMap keyMap = keyMaps[orderByKey];
    	Object key = keyMap.getKey(elem);
    	int index = keyMap.keysList.binarySearch(key, keyMap.comparator);
    	if (index >= 0) {
    		index++;
    		while (index < keyMap.keysList.size()) {
    			if (keyMap.comparator.compare(keyMap.keysList.get(index), key) != 0) {
    				break;
    			}
    			index++;
    		}
    	}
    	return index;
    }

    int indexOfSorted(E elem) {
    	KeyMap keyMap = keyMaps[orderByKey];
    	Object key = keyMap.getKey(elem);
    	int index = keyMap.keysList.binarySearch(key, keyMap.comparator);
    	return (index < 0) ? -1 : index;
    }

    /**
     * Returns comparator used for sorting.
     * If there is no sorting because the elements are stored in a HashMap
     * or because the order is determined by the list order, null is returned.
     *
     * @return comparator used for sorting, null if there is no sorting
     */
    Comparator getSortComparator() {
    	if (orderByKey == -1) {
    		return null;
    	} else {
    		return keyMaps[orderByKey].comparator;
    	}
    }

    //

    /**
     * Checks whether element is allowed in collection (null/constraint).
     * The constraint is also checked if the element is null.
     *
     * @param elem element to check
     * @throws IllegalArgumentException if the element is not allowed
     */
    void checkElemAllowed(E elem) {
    	if (elem == null) {
    		if (!allowNullElem) {
    			errorNullElement();
    		}
    	}
    	if (constraint != null) {
    		if (!constraint.allow(elem)) {
       			errorConstraintElement();
    		}
    	}
    }

    static void errorNullElement() {
		throw new KeyException("Constraint violation: null element not allowed");
    }

    static void errorConstraintElement() {
		throw new KeyException("Constraint violation: element not allowed");
    }

    static void errorNullKey() {
		throw new KeyException("Constraint violation: null key not allowed");
    }

    static void errorDuplicateKey(Object key) {
		throw new DuplicateKeyException(key);
    }

    static void errorInvalidData() {
		throw new KeyException("Invalid data: call update() on change of key data");
    }

    static void errorInvalidIndex() {
		throw new KeyException("Invalid index for sorted list");
    }

    static void errorMaxSize() {
		throw new KeyException("Maximum size reached");
    }

    /**
     * This method is called before a new element is added.
     * If the addition should not happen, an exception can be thrown.
     *
     * @param elem	element to insert
     */
    private void beforeInsert(E elem) {
        if (beforeInsertTrigger != null) {
        	beforeInsertTrigger.handle(elem);
    	}
    }

    /**
     * This method is called after a new element has been added.
     *
     * @param elem	element which has been inserted
     */
    private void afterInsert(E elem) {
        if (afterInsertTrigger != null) {
        	afterInsertTrigger.handle(elem);
    	}
    }

    /**
     * This method is called before an existing element is removed.
     * If the deletion should not happen, an exception can be thrown.
     *
     * @param elem	element to delete
     */
    private void beforeDelete(E elem) {
        if (beforeDeleteTrigger != null) {
        	beforeDeleteTrigger.handle(elem);
    	}
    }

    /**
     * This method is called after an existing element has been removed.
     *
     * @param elem	element which has been deleted
     */
    private void afterDelete(E elem) {
        if (afterDeleteTrigger != null) {
   			afterDeleteTrigger.handle(elem);
    	}
    }

    @Override
    public boolean add(E elem) {
        // This method is also used by addAll()
    	beforeInsert(elem);
    	checkElemAllowed(elem);
    	if (maxSize != 0 && size >= maxSize) {
    		errorMaxSize();
    	}
    	doAdd(elem, null);
    	size++;
        if (DEBUG_CHECK) debugCheck();
        afterInsert(elem);
    	return true;
    }

	@Override
	public boolean remove(Object elem) {
		return remove(elem, null);
	}

    /**
     * Remove element.
     *
     * @param elem		element to remove
     * @param ignore	KeyMap to ignore (null to add element to all maps)
     * @return			true if element has been removed
     */
	boolean remove(Object elem, KeyMap ignore) {
	    beforeDelete((E) elem);
        boolean removed = doRemove(elem, ignore);
        if (removed) {
        	size--;
        }
        if (DEBUG_CHECK) debugCheck();
        afterDelete((E) elem);
        return removed;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean contains(Object o) {
		if (keyMaps[0] != null) {
			return keyMaps[0].containsKey(o);
		} else {
			return keyMaps[1].containsValue(o);
		}
	}

	@Override
	public Iterator<E> iterator() {
		return keyMaps[orderByKey].iteratorValues(this);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean added = false;
		for (E e: c) {
			if (add(e)) {
				added = true;
			}
		}
		return added;
	}

    public GapList<Object> getKeyValues(int keyIndex) {
    	return getKeyMap(keyIndex).getValues(size);
    }

    /**
     * Returns all elements contained in this collection as list.
     *
     * @return all elements contained in this collection as list
     */
    public GapList<E> toList() {
    	GapList<E> list = new GapList(size());
		for (E e : this) {
			list.add(e);
		}
    	return list;
    }

	@Override
	public Object[] toArray() {
		GapList<Object> list = new GapList(size());
		for (E e : this) {
			list.add(e);
		}
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		GapList<Object> list = new GapList(size());
		for (E e : this) {
			list.add(e);
		}
		return list.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
        for (Object e: c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
    	boolean changed = false;
    	if (c.size() < size()) {
    		for (Iterator<?> i = c.iterator(); i.hasNext(); ) {
    			if (remove(i.next())) {
    				changed = true;
    			}
    		}
    	} else {
    		for (Iterator<?> i = iterator(); i.hasNext(); ) {
    			if (c.contains(i.next())) {
    				i.remove();
    				changed = true;
    			}
    		}
    	}
        return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
    	boolean changed = false;
		for (Iterator<?> i = iterator(); i.hasNext(); ) {
			if (!c.contains(i.next())) {
				i.remove();
				changed = true;
			}
		}
        return changed;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		boolean first = true;
		for (Iterator<E> iter=iterator(); iter.hasNext(); ) {
			if (!first) {
				buf.append(", ");
			} else {
				first = false;
			}
			buf.append(iter.next());
		}
		buf.append("]");
		return buf.toString();
	}

    /**
     * Remove element.
     *
     * @param elem		element to remove
     * @param ignore	KeyMap to ignore (null to add element to all maps)
     * @return			true if element has been removed
     */
	boolean doRemove(Object elem, KeyMap ignore) {
        E removed = null;
        boolean first = true;
        if (keyMaps != null) {
	        for (int i=0; i<keyMaps.length; i++) {
	        	if (keyMaps[i] != null && keyMaps[i] != ignore) {
		       		Object key = keyMaps[i].getKey((E) elem);
		       		Option<E> obj = keyMaps[i].remove(key, true, elem, this);
		       		if (first) {
		       			if (!obj.hasValue()) {
		       				return false;
		       			} else {
		       				removed = obj.getValue();
		       			}
		       			first = false;
		       		} else {
		       			if (!obj.hasValue() || obj.getValue() != removed) {
		       				errorInvalidData();
		       			}
		       		}
	        	}
	        }
        }
        return true;
	}

    /**
     * Returns a copy of this collection with all its elements.
     * The new collection will use the same comparator, ordering, etc.
     *
     * @return  a copy of this collection
     */
    public KeyCollectionImpl copy() {
		try {
			KeyCollectionImpl copy = (KeyCollectionImpl) super.clone();
			copy.initCopy(this);
	        if (DEBUG_CHECK) copy.debugCheck();
			return copy;
		}
		catch (CloneNotSupportedException e) {
		    // This shouldn't happen, since we are Cloneable
		    throw new AssertionError(e);
		}
    }

    /**
     * Returns a copy of this collection but without elements.
     * The new collection will use the same comparator, ordering, etc.
     *
     * @return  an empty copy of this collection
     */
    public KeyCollectionImpl crop() {
    	try {
	    	KeyCollectionImpl copy = (KeyCollectionImpl) super.clone();
	        copy.initCrop(this);
	        if (DEBUG_CHECK) copy.debugCheck();
	        return copy;
		}
		catch (CloneNotSupportedException e) {
		    // This shouldn't happen, since we are Cloneable
		    throw new AssertionError(e);
		}
    }

    @Override
    protected Object clone() {
    	return copy();
    }

    /**
	 * Initialize this object after the bitwise copy has been made
	 * by Object.clone().
	 *
	 * @param that	source object
	 */
	protected void initClone(Object that) {
	}

    /**
     * Returns a set view of the collection.
     * Note that this method does not check whether the collection really
     * is really a set as defined by the Set interface. It makes only sure
     * that the add() method will return false instead of throwing a
     * DuplicateKeyException.
     *
     * @return set view
     */
    public Set<E> asSet() {
    	return new KeyCollectionAsSet(this, false);
    }

    @Override
    public void clear() {
    	if (keyMaps != null) {
	    	for (KeyMap<E,Object> keyMap: keyMaps) {
	    		if (keyMap != null) {
	    			doClear(keyMap);
	    		}
	    	}
    	}
    	size = 0;
    }

    private void doClear(KeyMap<E,?> keyMap) {
        if (keyMap.keysMap != null) {
        	keyMap.keysMap.clear();
        } else {
        	keyMap.keysList.clear();
        }
    }

	/**
	 * Produce key out of specified element.
	 *
	 * @param elem element
	 * @return     key of specified element
	 * @throws IllegalArgumentException if a null key is produced and null keys are not allowed
	 */
	private <K> K getKey(KeyMap<E,K> keyMap, E elem) {
	    K key = keyMap.getKey(elem); // TODO do we need both getKey() methods?
        if (key == null && !keyMap.allowNull) {
            throw new IllegalArgumentException("Null key not allowed");
        }
        return key;
	}

    /**
     * Add element.
     *
     * @param elem		element to add
     * @param ignore	KeyMap to ignore (null to add element to all maps)
     */
    void doAdd(E elem, KeyMap ignore) {
    	if (keyMaps == null) {
    		return;
    	}
    	RuntimeException error = null;
		int i = 0;
		try {
	    	for (i=0; i<keyMaps.length; i++) {
	    		if (keyMaps[i] != null && keyMaps[i] != ignore) {
	    			Object key = keyMaps[i].getKey(elem);
	    			keyMaps[i].add(key, elem);
	    		}
	    	}
		}
		catch (RuntimeException e) {
			error = e;
		}

    	// If an error occurred, roll back changes
    	if (error != null) {
    		for (i--; i>=0; i--) {
    			if (keyMaps[i] != null) {
    				Object key = keyMaps[i].getKey(elem);
    				keyMaps[i].remove(key, true, elem, this);
    			}
    		}
    		if (error != null) {
    	        if (DEBUG_CHECK) debugCheck();
    			throw error;
    		}
    	}
    }

    /**
     * Checks whether the specified key exists in this list.
     *
     * @param keyIndex  key index
     * @param key 		key to look for
     * @return  		true if the key exists, otherwise false
     */
    protected <K> boolean containsKey(int keyIndex, K key) {
        return getKeyMap(keyIndex).containsKey(key);
    }

    /**
     * Returns list containing all distinct keys.
     *
     * @param keyIndex	key index
     * @return 			list containing all distinct keys
     */
    protected Set<?> getDistinctKeys(int keyIndex) {
    	return getKeyMap(keyIndex).getDistinctKeys();
    }

    void checkKeyMap(int keyIndex) {
    	if (keyMaps == null || keyIndex >= keyMaps.length || keyIndex < 0 || keyMaps[keyIndex] == null) {
    		throw new IllegalArgumentException("Invalid key index: " + keyIndex);
    	}
    }

    KeyMap<E,Object> getKeyMap(int keyIndex) {
    	checkKeyMap(keyIndex);
    	return keyMaps[keyIndex];
    }

    /**
     * Returns mapper for specified key map.
     *
     * @param keyIndex 	key index
     * @return      	mapper for specified key map
     */
    protected Mapper<E,Object> getKeyMapper(int keyIndex) {
    	return getKeyMap(keyIndex).mapper;

    }

    /**
     * Returns value for specified key.
     * If there are several values for this key, the first is returned.
     * If the key is not found, null is returned.
     *
     * @param keyIndex 	key index
     * @param key   	key to find
     * @return      	value of specified key or null
     */
    protected E getByKey(int keyIndex, Object key) {
    	return getByKey(getKeyMap(keyIndex), key);
    }

    private <K> E getByKey(KeyMap<E,K> keyMap, K key) {
        // Handle null key if not allowed to prevent NPE
        if (key == null) {
            if (!keyMap.allowNull) {
                return null;
            }
        }

        if (keyMap.keysMap != null) {
            // not sorted
            Object obj = keyMap.keysMap.get(key);
            if (obj instanceof KeyMapList) {
                GapList<E> list = (GapList<E>) obj;
                return list.getFirst();
            } else {
                return (E) obj;
            }

        } else {
            // sorted
            int index = SortedLists.binarySearchGet(keyMap.keysList, key, keyMap.comparator);
            if (index >= 0) {
           		return (E) keyList.doGet(index);
            } else {
                return null;
            }
        }
    }

    /**
     * Returns a list with all elements with the specified key.
     *
     * @param keyIndex	key index
     * @param key   	key which elements must have
     * @return      	list with all keys (null if key is null)
     */
    protected GapList<E> getAllByKey(int keyIndex, Object key) {
    	return doGetAllByKey(getKeyMap(keyIndex), key);
    }

    private <K> GapList<E> doGetAllByKey(KeyMap<E,K> keyMap, K key) {
        // Handle null key if not allowed to prevent NPE
        if (key == null) {
            if (!keyMap.allowNull) {
                return GapList.EMPTY();
            }
        }

        if (keyMap.keysMap != null) {
            // not sorted
            Object obj = keyMap.keysMap.get(key);
            if (obj == null) {
                return GapList.create();
            } else if (obj instanceof KeyMapList) {
                GapList<E> list = (GapList<E>) obj;
                return list.copy();
            } else {
                return (GapList<E>) GapList.create(keyMap.keysMap.get(key));
            }

        } else {
            // sorted
            int index = SortedLists.binarySearchGet(keyMap.keysList, key, keyMap.comparator);
            if (index >= 0) {
                GapList<E> list = new GapList<E>();
                while (true) {
                    list.add((E) keyMap.keysList.doGet(index));
                    index++;
                    if (index == keyMap.keysList.size()) {
                        break;
                    }
                    if (!GapList.equalsElem(keyMap.keysList.get(index), key)) {
                        break;
                    }
                }
                return list;
            } else {
                return GapList.create();
            }
        }
    }

    /**
     * Returns number of elements with specified key.
     *
     * @param keyIndex 	key index
     * @param key   	key which elements must have
     * @return      	number of elements with key (-1 if key is null)
     */
    protected int getCountByKey(int keyIndex, Object key) {
    	return getCountByKey(getKeyMap(keyIndex), key);
    }

    private <K> int getCountByKey(KeyMap<E,K> keyMap, K key) {
        // Handle null key if not allowed to prevent NPE
        if (key == null) {
            if (!keyMap.allowNull) {
                return 0;
            }
        }

        if (keyMap.keysMap != null) {
            // not sorted
        	if (keyMap.count) {
	            Integer val = (Integer) keyMap.keysMap.get(key);
	            if (val == null) {
	                return 0;
	            } else {
	            	return val;
	            }
        	} else {
	            Object obj = keyMap.keysMap.get(key);
	            if (obj == null) {
	                return 0;
	            } else if (obj instanceof KeyMapList) {
	                GapList<E> list = (GapList<E>) obj;
	                return list.size();
	            } else {
	                return 1;
	            }
        	}
        } else {
            // sorted
            int index = SortedLists.binarySearchGet(keyMap.keysList, key, keyMap.comparator);
            if (index >= 0) {
                int count = 0;
                while (true) {
                    count++;
                    index++;
                    if (index == keyMap.keysList.size()) {
                        break;
                    }
                    if (!GapList.equalsElem(keyMap.keysList.get(index), key)) {
                        break;
                    }
                }
                return count;
            } else {
                return 0;
            }
        }
    }

    /**
     * Invalidate element, i.e. all keys of the element are extracted
     * again and stored in the key maps. Old key values are removed
     * if needed.
     * You must call an invalidate method if an element's key value has changed after adding it to the collection.
     *
     * @param elem element to invalidate
     */
    protected void invalidate(E elem) {
    	if (keyMaps != null) {
    		for (int i=0; i<keyMaps.length; i++) {
    			if (keyMaps[i] != null) {
    				Option<Object> key = invalidate(keyMaps[i], elem);
    				if (key.hasValue()) {
    					keyMaps[i].add(key.getValue(), elem);
    				}
    			}
    		}
        }
        if (DEBUG_CHECK) debugCheck();
    }

    /**
     * Invalidate key value of element.
     * You must call an invalidate method if an element's key value has changed after adding it to the collection.
     *
     * @param keyIndex	key index
     * @param oldKey	old key value
     * @param newKey	new key value
     * @param elem		element to invalidate (can be null if there are no duplicates with this key)
     */
    protected void invalidateKey(int keyIndex, Object oldKey, Object newKey, E elem) {
    	doInvalidateKey(keyIndex, oldKey, newKey, elem);
    }

    E doInvalidateKey(int keyIndex, Object oldKey, Object newKey, E elem) {
    	KeyMap keyMap = getKeyMap(keyIndex);
    	Option<Object> removed;
    	if (elem == null) {
    		removed = keyMap.remove(oldKey, false, null, this);
    	} else {
    		removed = keyMap.remove(oldKey, true, elem, this);
    	}
    	if (!removed.hasValue()) {
    		errorInvalidData();
    	}
    	keyMap.add(newKey, removed.getValue());
        if (DEBUG_CHECK) debugCheck();
    	return (E) removed.getValue();
    }

    /**
     * @param keyMap	key map
     * @param elem		elem to invalidate
     * @return			null if key for keyMap and element is correct, else key which must be added to keymap
     */
    private Option<Object> invalidate(KeyMap keyMap, Object elem) {
    	boolean allowDuplicates = keyMap.allowDuplicates;
    	Object key = keyMap.getKey(elem);

    	if (keyMap.keysMap != null) {
    		Iterator<Map.Entry> iter = keyMap.keysMap.entrySet().iterator();
    		while (iter.hasNext()) {
    		    Map.Entry entry = iter.next();
    		    if (GapList.equalsElem(elem, entry.getValue())) {
    		    	if (GapList.equalsElem(key, entry.getKey())) {
    		    		return Option.EMPTY();
    		    	}
    		        iter.remove();
    		        if (!allowDuplicates) {
    		        	break;
    		        }
    		    }
    		}
    	} else {
    		assert(keyMap.keysList != null);
    		for (int i=0; i<keyMap.keysList.size(); i++) {
    			if (GapList.equalsElem(elem, keyList.doGet(i))) {
    				if (GapList.equalsElem(key, keyMap.keysList.get(i))) {
    					return Option.EMPTY();
    				}
    				keyMap.keysList.remove(i);
    		        if (!allowDuplicates) {
    		        	break;
    		        }
    			}
    		}
    	}
    	return new Option(key);
    }

    /**
     * Removes element by key.
     * If there are duplicates, only one element is removed.
     *
     * @param keyIndex	index of key map
     * @param key   	key of element to remove
     * @return      	removed element or null if no element has been removed
     */
    protected Option<E> doRemoveByKey(int keyIndex, Object key) {
    	checkKeyMap(keyIndex);
    	Option<E> removed = keyMaps[keyIndex].remove(key, false, null, this);
    	if (removed.hasValue()) {
    		E elem = removed.getValue();
    		try {
    			beforeDelete(elem);
    		}
    		catch (RuntimeException e) {
    			keyMaps[keyIndex].add(key, elem);
    			throw e;
    		}
    		for (int i=0; i<keyMaps.length; i++) {
    			if (i != keyIndex) {
        			if (keyMaps[i] != null) {
        				E value = removed.getValue();
        				Object k = keyMaps[i].getKey(value);
        				keyMaps[i].remove(k, true, value, this);
        			}
    			}
    		}
    		size--;
    		afterDelete(elem);
    	}
        if (DEBUG_CHECK) debugCheck();
        return removed;
    }

    protected E removeByKey(int keyIndex, Object key) {
    	return doRemoveByKey(keyIndex, key).getValueOrNull();
    }

    /**
     * Puts element in collection using specified key map.
     *
     * @param keyIndex	key index
     * @param elem		element
     * @return			element which has been replaced, null otherwise
     */
    protected E putByKey(int keyIndex, E elem) {
    	checkKeyMap(keyIndex);
		Object k = keyMaps[keyIndex].getKey(elem);
		boolean add = false;
    	if (!containsKey(keyIndex, k)) {
    		add = true;
    	} else {
    		if (elem != null) {
    			add = keyMaps[keyIndex].allowDuplicates;
    		} else {
    			add = keyMaps[keyIndex].allowDuplicatesNull;
    		}
    	}

    	Option<E> oldElem = new Option(null);
    	if (!add) {
    		oldElem = doRemoveByKey(keyIndex, elem);
    	}
    	try {
    		add(elem);
    	}
       	catch (RuntimeException e) {
       		add(oldElem.getValue());
       		throw e;
       	}
    	return oldElem.getValue();
    }

    /**
     * Removes element by key.
     * If there are duplicates, all elements are removed.
     *
     * @param keyIndex	key index
     * @param key   	key of element to remove
     * @return      	true if elements have been removed, false otherwise
     */
    @SuppressWarnings("unchecked")
    protected GapList<E> removeAllByKey(int keyIndex, Object key) {
    	checkKeyMap(keyIndex);
    	GapList<E> removeds = doRemoveAllByKey(keyMaps[keyIndex], key);
    	for (E removed: removeds) {
    		for (int i=0; i<keyMaps.length; i++) {
    			if (i != keyIndex && keyMaps[i] != null) {
    				Object k = keyMaps[i].getKey(removed);
    				doRemoveAllByKey(keyMaps[i], k);
    			}
    		}
    		size--;
    	}
        if (DEBUG_CHECK) debugCheck();
        return removeds;
    }

    /**
     * Removes element by key.
     * If there are duplicates, all elements are removed.
     *
     * @param keyMap	key map
     * @param key   	key of element to remove
     * @return      	list with all removed elements
     */
    private <K> GapList<E> doRemoveAllByKey(KeyMap<E,K> keyMap, K key) {
    	// If list cannot contain null, handle null explicitly to prevent NPE
    	if (key == null) {
    		if (!keyMap.allowNull) {
    			return GapList.EMPTY();
    		}
    	}
        if (keyMap.keysMap != null) {
            // not sorted
        	if (!keyMap.keysMap.containsKey(key)) {
        		return GapList.EMPTY();
        	}
            Object obj = keyMap.keysMap.remove(key);
            GapList<E> removed;
            if (obj instanceof KeyMapList) {
                removed = GapList.create((GapList<E>) obj);
            } else {
            	removed = GapList.create((E) obj);
            }
            return removed;

        } else {
            // sorted
        	int index = SortedLists.binarySearchGet(keyMap.keysList, key, keyMap.comparator);
            if (index < 0) {
                return GapList.EMPTY();
            }
            int start = index;
            while (true) {
                index++;
                if (index == keyMap.keysList.size()) {
                    break;
                }
                if (!GapList.equalsElem(keyMap.keysList.get(index), key)) {
                    break;
                }
            }
            GapList<E> removed = (GapList<E>) keyMap.keysList.getAll(start, index-start);
            keyMap.keysList.remove(start, index-start);
            return removed;
        }
    }

	// As in AbstractCollection
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Collection)) {
			return false;
		}
		Collection c = (Collection) o;
		if (c.size() != size()) {
			return false;
		}
		try {
			return containsAll(c);
		} catch (ClassCastException unused) {
			return false;
		} catch (NullPointerException unused) {
			return false;
		}
	}

	// As in AbstractCollection
	@Override
	public int hashCode() {
		int h = 0;
		Iterator<E> i = iterator();
		while (i.hasNext()) {
			E obj = i.next();
			if (obj != null) {
				h += obj.hashCode();
			}
		}
		return h;
	}

    //-- Element methods

	/**
	 * Returns all equal elements.
	 *
	 * @param elem	element
	 * @return		all equal elements (never null)
	 */
	protected GapList<E> getAll(E elem) {
		return getAllByKey(0, elem);
	}

	/**
	 * Returns the number of equal elements.
	 *
	 * @param elem	element
	 * @return		number of equal elements
	 */
	protected int getCount(E elem) {
		return getCountByKey(0, elem);
	}

	/**
	 * Removes all equal elements.
	 *
	 * @param elem	element
	 * @return		removed equal elements (never null)
	 */
	protected GapList<E> removeAll(E elem) {
		return removeAllByKey(0, elem);
	}

	/**
	 * Returns all distinct elements in the same order as in the collection.
	 *
	 * @return		distinct elements
	 */
	protected Set<E> getDistinct() {
		return (Set<E>) getDistinctKeys(0);
	}

	/**
	 * Adds or replaces element.
	 * If there is no such element, the element is added.
	 * If there is such an element and no duplicates
	 * are allowed, the existing element is replaced.
	 * If duplicates are allowed, the element is added.
	 *
	 * @param elem	element
	 * @return		element which has been replaced or null otherwise
	 */
	protected E put(E elem) {
		return putByKey(0, elem);
	}

    //-- Key methods
	// The key methods can not be defined here.
	// Due to the generic type parameters, the methods cannot be overridden.

}
