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
 * $Id: KeyList.java 1829 2013-08-20 06:45:35Z origo $
 */
package org.magicwerk.brownies.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

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
 * @version $Id: KeyList.java 1829 2013-08-20 06:45:35Z origo $
 *
 * @see GapList
 * @param <E> type of elements stored in the list
 */
public class TableCollectionImpl<E> implements Collection<E> {

	/**
     * Implementation of builder.
     */
    public static class BuilderImpl<E> {

        public static class KeyMapBuilder<E,K> {
        	boolean orderBy;
        	// -- mapper
        	Mapper<E,K> mapper;
            // -- null
            boolean allowNull = true;
            // -- duplicates
            boolean allowDuplicates = true;
            boolean allowDuplicatesNull = true;
            // -- sorted list
            /** True to sort using natural comparator */
            boolean sort;
            /** Comparator to use for sorting */
            Comparator<?> comparator;
            /** The specified comparator can handle null values */
            boolean comparatorSortsNull;
            /** Determine whether null values appear first or last */
            boolean sortNullsFirst;
            // -- sorted list
            boolean list;
            /** Primitive class to use for storage */
            Class<?> listType;
        }

    	// KeyList to build
    	TableCollectionImpl<E> tableColl;
    	// -- order
        boolean sorted;
    	// -- constraint
        boolean allowNullElem = true;
        Predicate<E> constraint;
        // -- triggers
        Trigger<E> insertTrigger;
        Trigger<E> deleteTrigger;
        // -- keys
        KeyMapBuilder elemMapBuilder;
    	GapList<KeyMapBuilder<E,Object>> keyMapBuilders = GapList.create();
        // -- content
        Collection<? extends E> collection;
        E[] array;
        int capacity;

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
        		getElemMapBuilder().allowNull = allowNull;
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

        //-- Element key

        /**
         * Add element map (with ident mapper).
         *
         * @return	this (fluent interface)
         */
        protected BuilderImpl<E> withElem() {
            return withElem(false);
        }

        /**
         * Add element map (with ident mapper).
         *
         * @param orderBy	true to force the collection to have the order of this map
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withElem(boolean orderBy) {
            getElemMapBuilder().orderBy = orderBy;
            return this;
        }

        /**
         * Specify whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param allowNull true to allow null elements, false to disallow
         * @return          this (fluent interfaces)
         */
        protected BuilderImpl<E> withElemNull(boolean allowNull) {
        	getElemMapBuilder().allowNull = allowNull;
        	allowNullElem = allowNull;
        	return this;
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
        	getElemMapBuilder().allowDuplicates = allowDuplicates;
        	getElemMapBuilder().allowDuplicatesNull = allowDuplicatesNull;
            return this;
        }

        /**
         * Determines that list should be sorted.
         *
         * @return        this (fluent interface)
         */
        protected BuilderImpl<E> withElemSort() {
        	getElemMapBuilder().sort = true;
            return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (fluent interface)
         */
        protected BuilderImpl<E> withElemSort(Comparator<? super E> comparator) {
        	getElemMapBuilder().sort = true;
        	getElemMapBuilder().comparator = comparator;
        	getElemMapBuilder().comparatorSortsNull = true;
        	return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator           comparator to use for sorting
         * @param sortNullsFirst	   true to sort null values first, false for last
         * @return                     this (fluent interface)
         */
        protected BuilderImpl<E> withElemSort(Comparator<? super E> comparator, boolean sortNullsFirst) {
        	getElemMapBuilder().sort = true;
        	getElemMapBuilder().comparator = comparator;
        	getElemMapBuilder().comparatorSortsNull = false;
        	getElemMapBuilder().sortNullsFirst = sortNullsFirst;
            return this;
        }

        /**
         * Specify element type to use.
         *
         * @return		this (fluent interface)
         */
        protected BuilderImpl<E> withElemList() {
        	getElemMapBuilder().list = true;
            return this;
        }

        /**
         * Specify element type to use.
         *
         * @param type	type to use
         * @return		this (fluent interface)
         */
        protected BuilderImpl<E> withElemList(Class<?> type) {
        	getElemMapBuilder().list = true;
        	getElemMapBuilder().listType = type;
            return this;
        }

        // -- Key

        /**
         * Add key map.
         *
         * @param mapper	mapper to use
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withKey(Mapper mapper) {
            return withKey(mapper, false);
        }

        /**
         * Add key map.
         *
         * @param mapper	mapper to use
         * @param orderBy	true to force the collection to have the order of this map
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withKey(Mapper mapper, boolean orderBy) {
            getKeyMapBuilder(0).mapper = (Mapper<E, Object>) mapper;
            getKeyMapBuilder(0).orderBy = orderBy;
            return this;
        }

        /**
         * Specify whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param allowNull true to allow null elements, false to disallow
         * @return          this (fluent interfaces)
         */
        protected BuilderImpl<E> withKeyNull(boolean allowNull) {
        	getKeyMapBuilder(0).allowNull = allowNull;
        	return this;
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates   true to allow duplicates
         * @return              	this (fluent interfaces)
         */
        protected BuilderImpl<E> withKeyDuplicates(boolean allowDuplicates) {
        	return withKeyDuplicates(allowDuplicates, allowDuplicates);
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates		true to allow duplicates
         * @param allowDuplicatesNull	true to allow duplicate null values
         * @return						this (fluent interfaces)
         */
        protected BuilderImpl<E> withKeyDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	getKeyMapBuilder(0).allowDuplicates = allowDuplicates;
        	getKeyMapBuilder(0).allowDuplicatesNull = allowDuplicatesNull;
            return this;
        }

        /**
         * Determines that list should be sorted.
         *
         * @return        this (fluent interface)
         */
        protected BuilderImpl<E> withKeySort() {
        	getKeyMapBuilder(0).sort = true;
            return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (fluent interface)
         */
        protected BuilderImpl<E> withKeySort(Comparator<? super E> comparator) {
            return withKeySort(comparator, false);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator        comparator to use for sorting
         * @param sortNullsFirst   	true if comparator sorts null, false if not
         * @return                  this (fluent interface)
         */
        protected BuilderImpl<E> withKeySort(Comparator<? super E> comparator, boolean sortNullsFirst) {
        	getKeyMapBuilder(0).comparator = comparator;
        	getKeyMapBuilder(0).comparatorSortsNull = false;
        	getKeyMapBuilder(0).sortNullsFirst = sortNullsFirst;
            return this;
        }

        /**
         * Specify element type to use.
         *
         * @return		this (fluent interface)
         */
        protected BuilderImpl<E> withKeyList() {
        	getKeyMapBuilder(0).list = true;
            return this;
        }

        /**
         * Specify element type to use.
         *
         * @param type	type to use
         * @return		this (fluent interface)
         */
        protected BuilderImpl<E> withKeyList(Class<?> type) {
        	getKeyMapBuilder(0).list = true;
        	getKeyMapBuilder(0).listType = type;
            return this;
        }

        // -- Key1

        /**
         * Add key map.
         *
         * @param mapper	mapper to use
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withKey1(Mapper mapper) {
            return withKey1(mapper, false);
        }

        /**
         * Add key map.
         *
         * @param mapper	mapper to use
         * @param orderBy	true to force the collection to have the order of this map
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withKey1(Mapper mapper, boolean orderBy) {
            getKeyMapBuilder(0).mapper = (Mapper<E, Object>) mapper;
            getKeyMapBuilder(0).orderBy = orderBy;
            return this;
        }

        /**
         * Specify whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param allowNull true to allow null elements, false to disallow
         * @return          this (fluent interfaces)
         */
        protected BuilderImpl<E> withKey1Null(boolean allowNull) {
        	getKeyMapBuilder(0).allowNull = allowNull;
        	return this;
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates   true to allow duplicates
         * @return              	this (fluent interfaces)
         */
        protected BuilderImpl<E> withKey1Duplicates(boolean allowDuplicates) {
        	return withKey1Duplicates(allowDuplicates, allowDuplicates);
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates		true to allow duplicates
         * @param allowDuplicatesNull	true to allow duplicate null values
         * @return						this (fluent interfaces)
         */
        protected BuilderImpl<E> withKey1Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	getKeyMapBuilder(0).allowDuplicates = allowDuplicates;
        	getKeyMapBuilder(0).allowDuplicatesNull = allowDuplicatesNull;
            return this;
        }

        /**
         * Determines that list should be sorted.
         *
         * @return        this (fluent interface)
         */
        protected BuilderImpl<E> withKey1Sort() {
        	getKeyMapBuilder(0).sort = true;
            return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (fluent interface)
         */
        protected BuilderImpl<E> withKey1Sort(Comparator<? super E> comparator) {
        	getKeyMapBuilder(0).sort = true;
        	getKeyMapBuilder(0).comparator = comparator;
        	getKeyMapBuilder(0).comparatorSortsNull = true;
        	return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param sortNullsFirst   		true if comparator sorts null, false if not
         * @return                      this (fluent interface)
         */
        protected BuilderImpl<E> withKey1Sort(Comparator<? super E> comparator, boolean sortNullsFirst) {
        	getKeyMapBuilder(0).comparator = comparator;
        	getKeyMapBuilder(0).comparatorSortsNull = false;
        	getKeyMapBuilder(0).sortNullsFirst = sortNullsFirst;
            return this;
        }

        /**
         * Specify element type to use.
         *
         * @return		this (fluent interface)
         */
        protected BuilderImpl<E> withKey1List() {
        	getKeyMapBuilder(0).list = true;
            return this;
        }

        /**
         * Specify element type to use.
         *
         * @param type	type to use
         * @return		this (fluent interface)
         */
        protected BuilderImpl<E> withKey1List(Class<?> type) {
        	getKeyMapBuilder(0).list = true;
        	getKeyMapBuilder(0).listType = type;
            return this;
        }

        // -- Key2

        /**
         * Add key map.
         *
         * @param mapper	mapper to use
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withKey2(Mapper mapper) {
            return withKey2(mapper, false);
        }

        /**
         * Add key map.
         *
         * @param mapper	mapper to use
         * @param orderBy	true to force the collection to have the order of this map
         * @return			this (fluent interface)
         */
        protected BuilderImpl<E> withKey2(Mapper mapper, boolean orderBy) {
            getKeyMapBuilder(1).mapper = (Mapper<E, Object>) mapper;
            getKeyMapBuilder(1).orderBy = orderBy;
            return this;
        }

        /**
         * Specify whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param allowNull true to allow null elements, false to disallow
         * @return          this (fluent interfaces)
         */
        protected BuilderImpl<E> withKey2Null(boolean allowNull) {
        	getKeyMapBuilder(1).allowNull = allowNull;
        	return this;
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates   true to allow duplicates
         * @return              	this (fluent interfaces)
         */
        protected BuilderImpl<E> withKey2Duplicates(boolean allowDuplicates) {
        	return withKey2Duplicates(allowDuplicates, allowDuplicates);
        }

        /**
         * Specify whether duplicates are allowed or not.
         *
         * @param allowDuplicates		true to allow duplicates
         * @param allowDuplicatesNull	true to allow duplicate null values
         * @return						this (fluent interfaces)
         */
        protected BuilderImpl<E> withKey2Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	getKeyMapBuilder(1).allowDuplicates = allowDuplicates;
        	getKeyMapBuilder(1).allowDuplicatesNull = allowDuplicatesNull;
            return this;
        }

        /**
         * Determines that list should be sorted.
         *
         * @return        this (fluent interface)
         */
        protected BuilderImpl<E> withKey2Sort() {
        	getKeyMapBuilder(1).sort = true;
            return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (fluent interface)
         */
        protected BuilderImpl<E> withKey2Sort(Comparator<? super E> comparator) {
        	getKeyMapBuilder(1).sort = true;
        	getKeyMapBuilder(1).comparator = comparator;
        	getKeyMapBuilder(1).comparatorSortsNull = true;
        	return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param comparatorSortsNull   true if comparator sorts null, false if not
         * @return                      this (fluent interface)
         */
        protected BuilderImpl<E> withKey2Sort(Comparator<? super E> comparator, boolean sortNullsFirst) {
        	getKeyMapBuilder(1).sort = true;
        	getKeyMapBuilder(1).comparator = comparator;
        	getKeyMapBuilder(1).comparatorSortsNull = false;
        	getKeyMapBuilder(1).sortNullsFirst = sortNullsFirst;
            return this;
        }

        /**
         * Specify element type to use.
         *
         * @return		this (fluent interface)
         */
        protected BuilderImpl<E> withKey2List() {
        	getKeyMapBuilder(1).list = true;
            return this;
        }

        /**
         * Specify element type to use.
         *
         * @param type	type to use
         * @return		this (fluent interface)
         */
        protected BuilderImpl<E> withKey2List(Class<?> type) {
        	getKeyMapBuilder(1).list = true;
        	getKeyMapBuilder(1).listType = type;
            return this;
        }


        //-- Implementation

        boolean hasElemMapBuilder() {
        	return elemMapBuilder != null;
        }

        KeyMapBuilder<E, Object> getElemMapBuilder() {
        	if (elemMapBuilder == null) {
        		elemMapBuilder = new KeyMapBuilder();
        		elemMapBuilder.mapper = IdentMapper.INSTANCE;
        		elemMapBuilder.allowNull = allowNullElem;
        	}
        	return elemMapBuilder;
        }

        KeyMapBuilder<E, Object> getKeyMapBuilder(int index) {
        	if (index >= keyMapBuilders.size()) {
        		keyMapBuilders.add(index, new KeyMapBuilder());
        	}
        	return keyMapBuilders.get(index);
        }

        KeyMap buildKeyMap(KeyMapBuilder keyMapBuilder) {
        	KeyMap<E,Object> keyMap = new KeyMap<E,Object>();
        	keyMap.mapper = (Mapper<E, Object>) keyMapBuilder.mapper;
            keyMap.allowNull = keyMapBuilder.allowNull;
        	keyMap.allowDuplicates = keyMapBuilder.allowDuplicates;
        	keyMap.allowDuplicatesNull = keyMapBuilder.allowDuplicatesNull;

        	boolean allowNullKey = keyMapBuilder.allowNull;
        	if (keyMapBuilder.sort || keyMapBuilder.list) {
                if (keyMapBuilder.comparator == null) {
                	if (allowNullKey) {
                    	keyMap.comparator = new NullComparator(NaturalComparator.INSTANCE, keyMapBuilder.sortNullsFirst);
                	} else {
                    	keyMap.comparator = NaturalComparator.INSTANCE;
                	}
                } else {
                    if (!keyMapBuilder.comparatorSortsNull && allowNullKey) {
                    	keyMap.comparator = new NullComparator(keyMapBuilder.comparator, keyMapBuilder.sortNullsFirst);
                    } else {
                    	keyMap.comparator = (Comparator<Object>) keyMapBuilder.comparator;
                    }
                }
        	}

        	if (keyMapBuilder.list) {
        		if (keyMapBuilder.listType == null) {
            		keyMap.keysList = new GapList<Object>();
        		} else {
                	if (keyMap.comparator != NaturalComparator.INSTANCE) {
                		throw new IllegalArgumentException("Only natural comparator supported for list type");
                	}
        			keyMap.keysList = (GapList<Object>) GapLists.createWrapperList(keyMapBuilder.listType);
        		}
        	} else if (keyMapBuilder.sort) {
        		keyMap.keysMap = new TreeMap(keyMap.comparator);
        	} else {
        		keyMap.keysMap = new HashMap();
        	}

            return keyMap;
        }

        /**
         * Initialize TableCollection with specified options.
         *
         * @param tableColl collection to initialize
         */
        void build(TableCollectionImpl<E> tableColl) {
        	tableColl.allowNullElem = allowNullElem;
            tableColl.constraint = constraint;
            tableColl.insertTrigger = insertTrigger;
            tableColl.deleteTrigger = deleteTrigger;

            if (elemMapBuilder != null || keyMapBuilders.size() > 0) {
                int orderByKey = -1;
	            tableColl.keyMaps = new KeyMap[keyMapBuilders.size()+1];
	            if (elemMapBuilder != null) {
	            	tableColl.keyMaps[0] = buildKeyMap(elemMapBuilder);
	            	if (elemMapBuilder.orderBy) {
	            		orderByKey = 0;
	            	}
	            }
	            for (int i=0; i<keyMapBuilders.size(); i++) {
	            	KeyMapBuilder kmb = keyMapBuilders.get(i);
	            	if (kmb == null) {
	            		throw new IllegalArgumentException("Key " + i + " is not defined");
	            	}
	            	if (kmb.orderBy) {
	            		if (orderByKey != -1) {
	                		throw new IllegalArgumentException("Only one order by key allowed");
	            		}
	            		orderByKey = i+1;
	            	}
	            	tableColl.keyMaps[i+1] = buildKeyMap(kmb);
	            }
	            if (orderByKey == -1) {
	            	if (tableColl.keyMaps[0] != null) {
	            		orderByKey = 0;
	            	} else {
	            		assert(tableColl.keyMaps[1] != null);
	            		orderByKey = 1;
	            	}
	            }
	            tableColl.orderByKey = orderByKey;
            }
        }

        void fill(TableCollectionImpl<E> tableColl) {
            if (collection != null) {
            	tableColl.addAll(collection);
            } else if (array != null) {
            	tableColl.addAll((Collection<? extends E>) Arrays.asList(array));
            }
        }

        void fill(TableListImpl<E> tableList) {
            if (collection != null) {
            	tableList.init(collection);
            } else if (array != null) {
            	tableList.init((Collection<? extends E>) Arrays.asList(array));
            } else if (capacity != 0) {
        		tableList.init(capacity);
        	} else {
        		tableList.init();
        	}
        }
   }

    static class KeyMap<E, K> {
	    /** A mapper to extract keys out of element for a MapList. For a SetList, this is always an IdentMapper. */
	    Mapper<E, K> mapper;
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

	    KeyMap() {
	    }

	    KeyMap copy() {
	    	KeyMap copy = new KeyMap();
	    	copy.mapper = mapper;
	    	copy.allowNull = allowNull;
	    	copy.allowDuplicates = allowDuplicates;
	    	copy.allowDuplicatesNull = allowDuplicatesNull;
	    	copy.comparator = comparator;
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
	    	if (keysMap != null) {
    			return keysMap.containsValue(value);
	    	} else {
	    		return keysList.binarySearch(value, (Comparator<Object>) comparator) >= 0;
	    	}
	    }

	    @SuppressWarnings("unchecked")
		Iterator<E> iteratorValues() {
	    	if (keysMap != null) {
	    		return (Iterator<E>) new KeyMapIter(keysMap);
	    	} else {
	    		return (Iterator<E>) keysList.unmodifiableList().iterator();
	    	}
	    }

	    static class KeyMapIter<E,K> implements Iterator<E> {

	    	TableCollectionImpl<E> coll;
	    	Iterator<Object> iter0;
	    	Iterator<E> iter1;
	    	boolean hasElem = false;
	    	E elem;

	    	public KeyMapIter(Map<K,Object> coll) {
	    		this.iter0 = coll.values().iterator();
	    	}

			@Override
			public boolean hasNext() {
				boolean hasNext = false;
				if (iter1 != null) {
					hasNext = iter1.hasNext();
				}
				if (!hasNext) {
					hasNext = iter0.hasNext();
				}
				return hasNext;
			}

			@Override
			public E next() {
				boolean hasNext = false;
				E elem = null;
				if (iter1 != null) {
					if (iter1.hasNext()) {
						hasNext = true;
						elem = iter1.next();
					} else {
						iter1 = null;
					}
				}
				if (!hasNext) {
					if (iter0.hasNext()) {
						Object o = iter0.next();
						if (o instanceof GapList) {
							iter1 = ((GapList) o).iterator();
							elem = iter1.next();
						} else {
							elem = (E) o;
						}
					} else {
						iter1 = null;
					}
				}
				return elem;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
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
	     * @param key		key of object to remove
	     * @param value		value of object to remove
	     * @return			removed object
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
     */
    int size;
    /**
     * Index 0 is reseverd for the elem key using an IdentMapper. If there is no
     * elem key, keyMaps[0] contains null.
     */
    KeyMap<E, Object>[] keyMaps;
    /** Index of key map which defines order (-1 for no order) */
    int orderByKey;
	// -- null
    boolean allowNullElem;
    Predicate<E> constraint;
    // -- handlers
    Trigger<E> insertTrigger;
    Trigger<E> deleteTrigger;

    TableCollectionImpl() {
    }

    /**
     * Initialize object for copy() operation.
     *
     * @param that source object
     */
    void initCopy(TableCollectionImpl<E> that) {
    	size = that.size;
    	keyMaps = new KeyMap[that.keyMaps.length];
    	for (int i=0; i<keyMaps.length; i++) {
    		if (that.keyMaps[i] != null) {
    			keyMaps[i] = that.keyMaps[i].copy();
    		}
    	}
    	allowNullElem = that.allowNullElem;
    	constraint = that.constraint;
    	insertTrigger = that.insertTrigger;
    	deleteTrigger = that.deleteTrigger;
    }

    /**
     * Initialize object for crop() operation.
     *
     * @param that source object
     */
    void initCrop(TableCollectionImpl<E> that) {
    	size = that.size;
    	keyMaps = new KeyMap[that.keyMaps.length];
    	for (int i=0; i<keyMaps.length; i++) {
    		if (that.keyMaps[i] != null) {
    			keyMaps[i] = that.keyMaps[i].crop();
    		}
    	}
    	allowNullElem = that.allowNullElem;
    	constraint = that.constraint;
    	insertTrigger = that.insertTrigger;
    	deleteTrigger = that.deleteTrigger;
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
    		for (Object obj: keyMap.keysMap.values()) {
    			if (obj instanceof GapList) {
    				count += ((GapList) obj).size();
    			} else {
    				count++;
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

    boolean isSortedList() {
    	return false;
    }

    int binarySearchSorted(Object elem) {
    	assert(isSortedList());

    	int index = keyMaps[0].keysList.binarySearch(elem, keyMaps[0].comparator);
    	return (index < 0) ? -1 : index;
    }

    int indexOfSorted(Object elem) {
    	int index = binarySearchSorted(elem);
    	return (index < 0) ? -1 : index;
    }

    Object getKey(int keyIndex, E elem) {
    	return keyMaps[keyIndex].getKey(elem);
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
     * Checks whether element is allowed.
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

//    public Set<E> getSet() {
//    	if (setMap != null) {
//    		boolean immutable = (keyMaps != null);
//    		if (setMap.keysMap != null) {
//    			return new MapAsSet(setMap.keysMap, immutable);
//    		} else {
//    			return new SortedListAsSet(setMap.keysList, setMap.comparator, immutable);
//    		}
//    	}
//    	return null;
//    }
//
//    public Map getMap(int keyIndex) {
//    	return getKeyMap(keyIndex).keysMap;
//    }

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
		throw new IllegalArgumentException("Constraint violation: duplicate key not allowed");
    }

    static void errorInvalidData() {
		throw new IllegalArgumentException("Invalid data: call update() on change of key data");
    }

    @Override
    public boolean add(E elem) {
    	checkElemAllowed(elem);
    	doAdd(elem);
    	size++;
        if (DEBUG_CHECK) debugCheck();

    	if (insertTrigger != null) {
   			insertTrigger.handle(elem);
    	}
    	return true;
    }

	@Override
	public boolean remove(Object elem) {
		return remove(elem, false);
	}

	boolean remove(Object elem, boolean iterator) {
        boolean removed = doRemove(elem, iterator);
        if (removed) {
        	size--;
            if (DEBUG_CHECK) debugCheck();

        	if (deleteTrigger != null) {
        		deleteTrigger.handle((E) elem);
        	}
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
		return keyMaps[orderByKey].iteratorValues();
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

	boolean doRemove(Object elem, boolean iterator) {
        E removed = null;
        boolean first = true;
        int start = (iterator ? 1 : 0);
        for (int i=start; i<keyMaps.length; i++) {
        	if (keyMaps[i] != null) {
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
        return true;
	}

    @Override
    public void clear() {
    	for (KeyMap<E,Object> keyMap: keyMaps) {
    		doClear(keyMap);
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

    void doAdd(E elem) {
    	IllegalArgumentException error = null;
		int i = 0;
    	if (keyMaps != null) {
    		try {
		    	for (i=0; i<keyMaps.length; i++) {
		    		if (keyMaps[i] != null) {
		    			Object key = keyMaps[i].getKey(elem);
		    			keyMaps[i].add(key, elem);
		    		}
		    	}
    		}
    		catch (IllegalArgumentException e) {
    			error = e;
    		}
    	}
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
     * Add element.
     *
     * @param elem		element to add
     * @return			index where element should be added (-1 is valid), otherwise Integer.MIN_VALUE
     */
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
            Object obj = keyMap.keysMap.get(key);
            if (obj == null) {
                return 0;
            } else if (obj instanceof GapList) {
                GapList<E> list = (GapList<E>) obj;
                return list.size();
            } else {
                return 1;
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
    			if (i != keyIndex) {
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
            GapList<E> removed = (GapList<E>) keyMap.keysList.get(start, index-start);
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
