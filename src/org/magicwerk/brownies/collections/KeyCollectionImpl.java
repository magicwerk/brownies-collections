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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.magicwerk.brownies.collections.exceptions.DuplicateKeyException;
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
public class KeyCollectionImpl<E> implements Collection<E> {

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
        Trigger<E> insertTrigger;
        Trigger<E> deleteTrigger;
        // -- keys
    	GapList<KeyMapBuilder<E,Object>> keyMapBuilders = GapList.create();
        // -- content
        Collection<? extends E> collection;
        E[] array;
        int capacity;
        int maxSize;
        boolean movingWindow;
        boolean count;

        // Interface

        /**
         * Specify element constraint.
         *
         * @param allowNull	true to allow null values
         * @return 			this (fluent interface)
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
         * @param constraint	constraint element must satisfy
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
         * @param trigger	insert trigger method
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withInsertTrigger(Trigger<E> trigger) {
            this.insertTrigger = trigger;
            return this;
        }

        /**
         * Specify delete trigger.
         *
         * @param trigger	delete trigger method
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withDeleteTrigger(Trigger<E> trigger) {
            this.deleteTrigger = trigger;
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
        protected BuilderImpl<E> withElements(Collection<? extends E> elements) {
            this.collection = elements;
            return this;
        }

        /**
         * Specify elements added to the collection upon creation.
         *
         * @param elements	initial elements
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withElements(E... elements) {
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
            this.maxSize = maxSize;
            this.movingWindow = false;
            return this;
        }

        /**
         * Specify maximum size of collection.
         * If an attempt is made to add more elements, an exception is thrown.
         *
         * @param maxSize	maximum size
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withWindowSize(int maxSize) {
            this.maxSize = maxSize;
            this.movingWindow = true;
            return this;
        }

        //-- Element key

        /**
         * Specifies that the collection only count the number
         * of occurrences of equal elements, but not the elements
         * themselves.
         *
         * @param count	true to count only number of occurrences
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
        protected BuilderImpl<E> withElem() {
            return withKey(0, IdentMapper.INSTANCE);
        }

        /**
         * Specifies that the collection will have the order of this map.
         * The map must be sorted, if no sort order has been defined,
         * the natural comparator will be used. If the map allows null
         * values, the used comparator will sort them last.
         *
         * @param orderBy	if true the collection will have the order of this map
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
         * @param type	primitive type to use for map
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
         * @param allowNull true to allow null elements, false to disallow
         * @return          this (fluent interfaces)
         */
        protected BuilderImpl<E> withElemNull(boolean allowNull) {
        	return withKeyNull(0, allowNull);
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates   true to allow duplicates
         * @return              	this (fluent interfaces)
         */
        protected BuilderImpl<E> withElemDuplicates(boolean allowDuplicates) {
        	return withElemDuplicates(allowDuplicates, allowDuplicates);
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates		true to allow duplicates
         * @param allowDuplicatesNull	true to allow duplicate null values
         * @return						this (fluent interfaces)
         */
        protected BuilderImpl<E> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	return withKeyDuplicates(0, allowDuplicates, allowDuplicatesNull);
        }

        /**
         * Specify that the collection should be sorted using the natural comparator.
         * If the collection supports null values, they are sorted last.
         *
         * @param sort    true to sorted, false for unsorted
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

        protected BuilderImpl<E> withPrimaryElem() {
        	return withPrimaryKey(0);
        }

        protected BuilderImpl<E> withUniqueElem() {
            return withUniqueKey(0);
        }

        //

        protected BuilderImpl<E> withKey(int keyIndex, Mapper mapper) {
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

        protected BuilderImpl<E> withKeySort(int keyIndex, Comparator<? super E> comparator) {
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

        protected BuilderImpl<E> withKeySort(int keyIndex, Comparator<? super E> comparator, boolean sortNullsFirst) {
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
        // -- The methods for Key and Key1 are exactly the same, except the naming

        /**
         * Add key map.
         *
         * @param mapper	mapper to use
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withKey1(Mapper<E,?> mapper) {
            return withKey(1, mapper);
        }

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
        // only for TableListImpl
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
         * @return        this (fluent interface)
         */
        protected BuilderImpl<E> withKey1Sort(boolean sort) {
        	return withKeySort(1, sort);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (fluent interface)
         */
        protected BuilderImpl<E> withKey1Sort(Comparator<? super E> comparator) {
        	return withKeySort(1, comparator);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param sortNullsFirst   		true if comparator sorts null, false if not
         * @return                      this (fluent interface)
         */
        protected BuilderImpl<E> withKey1Sort(Comparator<? super E> comparator, boolean sortNullsFirst) {
            return withKeySort(1, comparator, sortNullsFirst);
        }

        protected BuilderImpl<E> withPrimaryKey1() {
        	return withPrimaryKey(1);
        }

        protected BuilderImpl<E> withUniqueKey1() {
            return withUniqueKey(1);
        }

        // -- Key2

        /**
         * Add key map.
         *
         * @param mapper	mapper to use
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withKey2(Mapper mapper) {
            return withKey(2, mapper);
        }

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
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (fluent interface)
         */
        protected BuilderImpl<E> withKey2Sort(Comparator<? super E> comparator) {
        	return withKeySort(2, comparator);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator           comparator to use for sorting
         * @param sortNullsFirst	   true to sort nulls first, false for last
         * @return                     this (fluent interface)
         */
        protected BuilderImpl<E> withKey2Sort(Comparator<? super E> comparator, boolean sortNullsFirst) {
        	return withKeySort(2, comparator, sortNullsFirst);
        }

        protected BuilderImpl<E> withPrimaryKey2() {
        	return withPrimaryKey(2);
        }

        protected BuilderImpl<E> withUniqueKey2() {
            return withUniqueKey(2);
        }


        //-- Implementation

        boolean hasElemMapBuilder() {
        	return keyMapBuilders.size() > 0 && keyMapBuilders.get(0) != null;
        }

        KeyMapBuilder<E, Object> getKeyMapBuilder(int index) {
        	int size = keyMapBuilders.size();
        	if (index >= size) {
        		KeyMapBuilder kmb = new KeyMapBuilder();
        		if (index == 1 && size == 0) {
            		keyMapBuilders.add(0, null);
        		}
        		keyMapBuilders.add(index, kmb);
        	}
        	return keyMapBuilders.get(index);
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
                    	keyMap.comparator = new NullComparator(NaturalComparator.INSTANCE, keyMapBuilder.sortNullsFirst);
                	} else {
                    	keyMap.comparator = NaturalComparator.INSTANCE;
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
                	if (keyMapBuilder.comparator != null && keyMapBuilder.comparator != NaturalComparator.INSTANCE) {
                		throw new IllegalArgumentException("Only natural comparator supported for list type");
                	}
                	if (isTrue(keyMapBuilder.allowNull)) {
                		throw new IllegalArgumentException("Null values are not supported for primitive list type");
                	}
                	keyMap.comparator = NaturalComparator.INSTANCE;
        			keyMap.keysList = (GapList<Object>) GapLists.createWrapperList(keyMapBuilder.orderByType);
        		}
        	} else if (keyMap.comparator != null) {
        		keyMap.keysMap = new TreeMap(keyMap.comparator);
        	} else {
        		keyMap.keysMap = new HashMap();
        	}

            return keyMap;
        }

        /**
         * Initialize TableCollection with specified options.
         *
         * @param keyColl collection to initialize
         * @param list    true if a KeyListImpl is built up
         */
        void build(KeyCollectionImpl keyColl, boolean list) {
        	keyColl.allowNullElem = allowNullElem;
            keyColl.constraint = constraint;
            keyColl.insertTrigger = insertTrigger;
            keyColl.deleteTrigger = deleteTrigger;
            keyColl.maxSize = maxSize;
            keyColl.movingWindow = movingWindow;

            int orderByKey = -1;
            int size = keyMapBuilders.size();
            if (size == 0) {
            	if (!list) {
            		withElem();
            		size++;
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

        void init(KeyCollectionImpl keyColl) {
            if (collection != null) {
            	keyColl.addAll(collection);
            } else if (array != null) {
            	keyColl.addAll((Collection<? extends E>) Arrays.asList(array));
            }
        }

        void init(KeyCollectionImpl keyColl, KeyListImpl keyList) {
        	keyList.keyColl = keyColl;
        	keyColl.triggerHandler = keyList;
        	if (keyColl.orderByKey == 0) {
        		keyList.forward = (GapList<E>) keyColl.keyMaps[0].keysList;
                if (collection != null) {
                	keyColl.addAll(collection);
                } else if (array != null) {
                	keyColl.addAll((Collection<? extends E>) Arrays.asList(array));
                }
        	} else {
        		if (collection != null) {
        			keyList.init(collection);
        		} else if (array != null) {
        			keyList.init((Collection<? extends E>) Arrays.asList(array));
        		} else if (capacity != 0) {
        			keyList.init(capacity);
        		} else {
        			keyList.init();
        		}
        	}
        }
   }

    static class KeyMap<E,K> {
	    /** A mapper to extract keys out of element for a MapList. For a SetList, this is always an IdentMapper. */
	    Mapper<E,K> mapper;
	    /** True to allow null keys */
	    boolean allowNull;
	    /** True to allow duplicate values. This also allows duplicate null values, but they are not distinct. */
	    boolean allowDuplicates;
	    boolean allowDuplicatesNull;
	    /** Comparator to use for sorting (if null, elements are not sorted) */
	    Comparator<K> comparator;
	    /**
	     * Key storage if not sorted. The values are single elements or a list of elements.
	     * Note that we cannot use TreeMap as K may not be comparable
	     */
	    Map<K, Object> keysMap;
	    /** Key storage if sorted */
	    GapList<K> keysList;
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
		Iterator<E> iteratorValues(KeyCollectionImpl tableColl) {
	    	if (keysMap != null) {
	    		if (count) {
	    			return (Iterator<E>) new KeyMapCountIter(tableColl, this, keysMap);
	    		} else {
	    			return (Iterator<E>) new KeyMapIter(tableColl, this, keysMap);
	    		}
	    	} else {
	    		// TODO use KeysListIter?
	    		return (Iterator<E>) keysList.unmodifiableList().iterator();
	    	}
	    }

	    static class KeysListIter<E,K> implements Iterator<E> {

	    	KeyCollectionImpl tableColl;
	    	KeyMap<E,K> keyMap;
	    	GapList<E> list;
	    	Iterator<E> iter;
	    	E elem;

			public KeysListIter(KeyCollectionImpl tableColl, KeyMap<E,K> keyMap, GapList<E> list) {
	    		this.tableColl = tableColl;
	    		this.keyMap = keyMap;
				this.list = list;
				this.iter = list.iterator();
			}

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public E next() {
				elem = iter.next();
				return elem;
			}

			@Override
			public void remove() {
				iter.remove();
				tableColl.remove(elem, keyMap);
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
					if (o instanceof GapList) {
						listIter = ((GapList) o).iterator();
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

	    	KeyCollectionImpl tableColl;
	    	KeyMap<E,K> keyMap;
	    	Map<K,Object> map;
	    	Iterator<Entry<K, Object>> mapIter;
	    	E elem;
	    	int count;
	    	boolean hasElem;

	    	public KeyMapCountIter(KeyCollectionImpl tableColl, KeyMap<E,K> keyMap, Map<K,Object> map) {
	    		this.tableColl = tableColl;
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

	    private void add(K key, E elem) {
	    	if (key == null) {
	    		if (!allowNull) {
	    			errorNullKey();
	    		}
	    	}
	        if (keysMap != null) {
	            // Keys not sorted
	        	boolean add = false;
	        	if (!keysMap.containsKey(key)) {
	        		add = true;
	        	} else {
	                if (allowDuplicates ||
	                	(key == null && allowDuplicatesNull)) {
	                	add = true;
	                }
	        	}
	        	if (!add) {
	        		errorDuplicateKey();
	        	}

                // New key
	        	if (count) {
	    			Integer val = (Integer) keysMap.get(key);
	    		    if (val == null) {
    		    		keysMap.put(key, 1);
	    		    } else {
    		    		keysMap.put(key, val+1);
	    		    }
	        	} else {
	    			Object obj = keysMap.get(key);
	    		    if (obj == null) {
	    		    	if (!keysMap.containsKey(key)) {
	    		    		keysMap.put(key, elem);
	    		        } else {
	    		            GapList<E> list = (GapList<E>) new KeyMapList(null, elem);
	    		            keysMap.put(key, list);
	    		    	}
	    		    } else if (obj instanceof GapList) {
	    	            GapList<E> list = (GapList<E>) obj;
	    	            list.add(elem);
	    	        } else {
	    	            GapList<E> list = (GapList<E>) new KeyMapList(obj, elem);
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
	        		errorDuplicateKey();
	        	}
	            keysList.doAdd(addIndex, key);
	       	}
	    }

	    /**
	     * @param key			key of object to remove
	     * @param matchValue	true if value must match to remove entry
	     * @param value			value of object to remove
	     * @return				removed object
	     */
	    Option<E> remove(Object key, boolean matchValue, Object value) {
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
			        if (obj instanceof GapList) {
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
	    		assert(mapper == IdentMapper.INSTANCE);
	    		assert(key == value);
	    		int index = keysList.binarySearch(key, (Comparator<Object>) comparator);
	    		E elem = null;
	    		if (index < 0) {
	    			return Option.EMPTY();
	    		}
    			elem = (E) keysList.remove(index);
	    		return new Option(elem);
	    	}
	    }

		public GapList<Object> getValues(int size) {
			GapList<Object> list = null;
	        if (keysMap != null) {
	        	list = GapList.create(size);
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
        	assert(keysList.size() == size);
			return list;
		}

		GapList<Object> getDistinctKeys() {
	        if (keysMap != null) {
	            GapList<Object> list = new GapList<Object>(keysMap.keySet());
	            return list;
	        } else {
	            K lastKey = null;
	            GapList<Object> list = new GapList<Object>();
	            for (int i=0; i<keysList.size(); i++) {
	                K key = keysList.get(i);
	                boolean add = false;
	                if (list.isEmpty()) {
	                    add = true;
	                } else {
	                    if (key != null) {
	                        add = !key.equals(lastKey);
	                    } else {
	                        add = (lastKey != null);
	                    }
	                }
	                if (add) {
	                    list.add(key);
	                    lastKey = key;
	                }
	            }
	            return list;
	        }
		}
    }

    /**
     * List type used to store multiple elements.
     * We need this distinct type to distinguish it from a normal GapList
     * in a KeyCollection<GapList<String>>.
     */
    static class KeyMapList<E> extends GapList<E> {
    	public KeyMapList(E... elems) {
    		super(elems);
    	}
    }

    //-- KeyCollection --

    /** If true the invariants the GapList are checked for debugging */
    private static final boolean DEBUG_CHECK = true;

    /**
     * Size of collection.
     * The size is cached, as the key maps do not know the size if duplicates are allowed.
     */
    int size;
    int maxSize;
    boolean movingWindow;
    /**
     * Maps for element and all defined keys.
     * Index 0 is reseverd for the elem key using an IdentMapper.
     * If there is no elem key, keyMaps[0] contains null.
     */
    KeyMap<E, Object>[] keyMaps;
    /**
     * Index of key map which defines order
     * (-1 for no order, only possible for TableList).
     * If an order key is defined for a TableList, it must be implemented as list.
     */
    int orderByKey;
	// -- null
    boolean allowNullElem;
    Predicate<E> constraint;
    // -- handlers
    Trigger<E> insertTrigger;
    Trigger<E> deleteTrigger;
    KeyListImpl triggerHandler;

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
    	insertTrigger = that.insertTrigger;
    	deleteTrigger = that.deleteTrigger;
    	orderByKey = that.orderByKey;
    }

    /**
     * Initialize object for crop() operation.
     *
     * @param that source object
     */
    void initCrop(KeyCollectionImpl that) {
    	size = that.size;
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
    	insertTrigger = that.insertTrigger;
    	deleteTrigger = that.deleteTrigger;
    	orderByKey = that.orderByKey;
    }

    /**
     * Private method to check invariant of GapList.
     * It is only used for debugging.
     */
    private void debugCheck() {
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
	    			if (obj instanceof GapList) {
	    				count += ((GapList) obj).size();
	    			} else {
	    				count++;
	    			}
	    		}
    		}
    		assert(count == size());
    	} else if (keyMap.keysList != null) {
    		GapList<?> copy = keyMap.keysList.copy();
    		copy.sort(keyMap.comparator);
    		assert(copy.equals(keyMap.keysList));
    	} else {
    		assert(false);
    	}
    }

    // for TableListImpl

    Object getKey(int keyIndex, E elem) {
    	return keyMaps[keyIndex].getKey(elem);
    }

    boolean isSortedList() {
    	return orderByKey != -1;
    }

    void checkIndex(int loIndex, int hiIndex, E elem) {
   		KeyMap keyMap = keyMaps[orderByKey];
    	Object key = keyMap.getKey(elem);
    	GapList<Object> list = keyMap.keysList;
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

    void addSorted(int index, E elem) {
   		// Check whether index is correct for adding element in a sorted list
    	checkIndex(index-1, index, elem);

    	// Index is correct
   		KeyMap keyMap = keyMaps[orderByKey];
    	Object key = keyMap.getKey(elem);
    	GapList<Object> list = keyMap.keysList;

   		doAdd(elem, keyMap);
    	list.doAdd(index, key);
   		size++;
   }

   	void setSorted(int index, E elem, E oldElem) {
   		// Check whether index is correct for setting element in a sorted list
    	checkIndex(index-1, index+1, elem);

    	// Index is correct
   		KeyMap keyMap = keyMaps[orderByKey];
    	Object key = keyMap.getKey(elem);
    	GapList<Object> list = keyMap.keysList;

    	doRemove(oldElem, keyMap);
    	try {
    		doAdd(elem, keyMap);
    	}
    	catch (RuntimeException e) {
    		doAdd((E) oldElem, keyMap);
    		throw e;
    	}
    	list.doSet(index, key);
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

    Comparator getSortComparator() {
    	if (orderByKey == -1) {
    		return null;
    	} else {
    		return keyMaps[orderByKey].comparator;
    	}
    }

    //

    /**
     * Checks whether element is allowed in collection.
     *
     * @param elem element to check
     * @throws IllegalArgumentException if the element is not allowed
     */
    void checkElemAllowed(E elem) {
    	if (elem == null) {
    		if (!allowNullElem) {
    			errorNullElement();
    		}
    	} else {
    		if (constraint != null) {
    			if (!constraint.allow(elem)) {
        			errorConstraintElement();
    			}
    		}
    	}
    }

    public GapList<E> getElements() {
    	GapList<E> list = GapList.create(size());
    	for (Iterator<E> iter=iterator(); iter.hasNext(); ) {
    		list.add(iter.next());
    	}
    	return list;
    }

    public GapList<Object> getKeyValues(int keyIndex) {
    	return getKeyMap(keyIndex).getValues(size);
    }

    static void errorNullElement() {
		throw new IllegalArgumentException("Constraint violation: null element not allowed");
    }

    static void errorConstraintElement() {
		throw new IllegalArgumentException("Constraint violation: element not allowed");
    }

    static void errorNullKey() {
		throw new IllegalArgumentException("Constraint violation: null key not allowed");
    }

    static void errorDuplicateKey() {
		throw new DuplicateKeyException();
    }

    static void errorInvalidData() {
		throw new IllegalArgumentException("Invalid data: call update() on change of key data");
    }

    static void errorInvalidIndex() {
		throw new IllegalArgumentException("Invalid index for sorted list");
    }

    static void errorMaxSize() {
		throw new IllegalArgumentException("Maximum size reached");
    }

    /**
     * This method is called before a new element is added.
     * If the addition should not happen, an exception can be thrown.
     * Per default, this method calls the registered insert trigger.
     * However the method can also be overwritten when appropriate.
     *
     * @param elem	element to insert
     */
    protected void beforeInsert(E elem) {
        if (triggerHandler != null) {
            triggerHandler.beforeInsert(elem);
        } else if (insertTrigger != null) {
   			insertTrigger.handle(elem);
    	}
    }

    /**
     * This method is called before an existing element is removed.
     * If the deletion should not happen, an exception can be thrown.
     * Per default, this method calls the registered delete trigger.
     * However the method can also be overwritten when appropriate.
     *
     * @param elem	element to insert
     */
    protected void beforeDelete(E elem) {
        if (triggerHandler != null) {
            triggerHandler.beforeDelete(elem);
        } else if (deleteTrigger != null) {
   			deleteTrigger.handle(elem);
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
            if (DEBUG_CHECK) debugCheck();

        }
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

	@Override
	public Object[] toArray() {
		GapList<Object> list = GapList.create(size());
		for (E e : this) {
			list.add(e);
		}
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		GapList<Object> list = GapList.create(size());
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
		       		Option<E> obj = keyMaps[i].remove(key, true, elem);
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
     * Returns a copy of this collection.
     * The new collection will use the same comparator, ordering, etc.
     *
     * @return  an empty copy of this instance
     */
    public KeyCollectionImpl copy() {
    	// Derived classes must implement
    	throw new UnsupportedOperationException();
    }

    /**
     * Returns a copy of this collection.
     * The new collection will use the same comparator, ordering, etc.
     *
     * @return  a copy of this instance
     */
    public KeyCollectionImpl crop() {
    	// Derived classes must implement
    	throw new UnsupportedOperationException();
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
    	IllegalArgumentException error = null;
		int i = 0;
    	if (keyMaps != null) {
    		try {
		    	for (i=0; i<keyMaps.length; i++) {
		    		if (keyMaps[i] != null && keyMaps[i] != ignore) {
		    			Object key = keyMaps[i].getKey(elem);
		    			keyMaps[i].add(key, elem);
		    		}
		    	}
    		}
    		catch (IllegalArgumentException e) {
    			error = e;
    		}
    	}
    	// If an error occurred, roll back changes
    	if (error != null) {
    		for (i--; i>=0; i--) {
    			if (keyMaps[i] != null) {
    				Object key = keyMaps[i].getKey(elem);
    				keyMaps[i].remove(key, true, elem);
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
     * @param key key to look for
     * @return  true if the key exists, otherwise false
     */
    public <K> boolean containsKey(int keyIndex, K key) {
        return getKeyMap(keyIndex).containsKey(key);
    }

    /**
     * Returns list containing all distinct keys.
     *
     * @return list containing all distinct keys
     */
    protected GapList<?> getDistinctKeys(int keyIndex) {
    	return getKeyMap(keyIndex).getDistinctKeys();
    }

    void checkKeyMap(int keyIndex) {
    	if (keyMaps == null || keyIndex >= keyMaps.length || keyIndex < 0) {
    		throw new IllegalArgumentException("Invalid key index: " + keyIndex);
    	}
    }

    KeyMap<E,Object> getKeyMap(int keyIndex) {
    	checkKeyMap(keyIndex);
    	return keyMaps[keyIndex];
    }

    /**
     * Returns value for given key.
     * If there are several values for this key, the first is returned.
     * If the key is not found, null is returned.
     *
     * @param key   key to find
     * @return      value of specified key or null
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
            if (obj instanceof GapList) {
                GapList<E> list = (GapList<E>) obj;
                return list.getFirst();
            } else {
                return (E) keyMap.keysMap.get(key);
            }

        } else {
            // sorted
            int index = SortedLists.binarySearchGet(keyMap.keysList, key, keyMap.comparator);
            if (index >= 0) {
                return (E) keyMap.keysList.doGet(index);
            } else {
                return null;
            }
        }
    }

    /**
     * Returns a list with all elements with the specified key.
     *
     * @param key   key which elements must have
     * @return      list with all keys (null if key is null)
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
                return GapList.EMPTY();
            } else if (obj instanceof GapList) {
                GapList<E> list = (GapList<E>) obj;
                return list.unmodifiableList();
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
                return GapList.EMPTY();
            }
        }
    }

    /**
     * Returns number of elements with specified key.
     *
     * @param key   key which elements must have
     * @return      number of elements with key (-1 if key is null)
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
	            } else if (obj instanceof GapList) {
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

    public void invalidate(E elem) {
        for (int i=0; i<keyMaps.length; i++) {
    		Object key = invalidate(keyMaps[i], elem);
        }
    }

    /**
     * @param keyMap
     * @param elem
     * @return			null if key for keyMap and element is correct, else key which must be added to keymap
     */
    private Object invalidate(KeyMap keyMap, Object elem) {
    	boolean allowDuplicates = keyMap.allowDuplicates;
    	Object key = keyMap.getKey(elem);

    	if (keyMap.keysMap != null) {
    		Iterator<Map.Entry> iter = keyMap.keysMap.entrySet().iterator();
    		while (iter.hasNext()) {
    		    Map.Entry entry = iter.next();
    		    if (GapList.equalsElem(elem, entry.getValue())) {
    		    	if (GapList.equalsElem(key, entry.getKey())) {
    		    		return null;
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
    			if (GapList.equalsElem(elem, keyMap.keysList.doGet(i))) {
    				if (GapList.equalsElem(key, keyMap.keysList.get(i))) {
    					return null;
    				}
    				keyMap.keysList.remove(i);
    		        if (!allowDuplicates) {
    		        	break;
    		        }
    			}
    		}
    	}
    	return key;
    }

    /**
     * Removes element by key.
     * If there are duplicates, only one element is removed.
     *
     * @param keyIndex	index of key map
     * @param key   	key of element to remove
     * @return      	removed element or null if no element has been removed
     */
    protected E removeByKey(int keyIndex, Object key) {
    	checkKeyMap(keyIndex);
    	Option<E> removed = keyMaps[keyIndex].remove(key, false, null);
    	if (removed.hasValue()) {
    		for (int i=0; i<keyMaps.length; i++) {
    			if (i != keyIndex) {
        			if (keyMaps[i] != null) {
        				E value = removed.getValue();
        				Object k = keyMaps[i].getKey(value);
        				keyMaps[i].remove(k, true, value);
        			}
    			}
    		}
    		size--;
    	}
        if (DEBUG_CHECK) debugCheck();
        return removed.getValueOrNull();
    }

    /**
     * Remove element with specified key from key map.
     *
     * @param keyMap		key map
     * @param key			key to remove
     * @param removeElems
     * @return				removed element or null
     */
     /**
     * Removes element by key.
     * If there are duplicates, all elements are removed.
     *
     * @param keyIndex
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
            int num;
            GapList<E> removed = GapList.create();
            if (obj instanceof GapList) {
                removed = (GapList<E>) obj;
                num = removed.size();
            } else {
                num = 1;
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

	// As in AbstractSet
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

	// As in AbstractSet
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

}
