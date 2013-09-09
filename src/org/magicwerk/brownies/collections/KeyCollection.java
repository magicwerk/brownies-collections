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
 * $Id: KeyList.java 1829 2013-08-20 06:45:35Z origo $
 */
package org.magicwerk.brownies.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.magicwerk.brownies.collections.KeyList.Builder;
import org.magicwerk.brownies.collections.KeyList.KeyMap;
import org.magicwerk.brownies.collections.function.Mapper;
import org.magicwerk.brownies.collections.function.Predicate;
import org.magicwerk.brownies.collections.function.Trigger;
import org.magicwerk.brownies.collections.helper.GapLists;
import org.magicwerk.brownies.collections.helper.IdentMapper;
import org.magicwerk.brownies.collections.helper.MapAsSet;
import org.magicwerk.brownies.collections.helper.NaturalComparator;
import org.magicwerk.brownies.collections.helper.NullComparator;
import org.magicwerk.brownies.collections.helper.Option;
import org.magicwerk.brownies.collections.helper.SortedListAsSet;
import org.magicwerk.brownies.collections.helper.SortedLists;
import org.magicwerk.brownies.collections.primitive.BooleanObjGapList;
import org.magicwerk.brownies.collections.primitive.ByteObjGapList;
import org.magicwerk.brownies.collections.primitive.CharObjGapList;
import org.magicwerk.brownies.collections.primitive.DoubleObjGapList;
import org.magicwerk.brownies.collections.primitive.FloatObjGapList;
import org.magicwerk.brownies.collections.primitive.IntObjGapList;
import org.magicwerk.brownies.collections.primitive.LongObjGapList;
import org.magicwerk.brownies.collections.primitive.ShortObjGapList;


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
public class KeyCollection<E> implements Collection<E> {

	/**
     * Data used for builders.
     */
    public static class BuilderBase<E> {

        public static class KeyMapBuilder<E,K> {
        	boolean orderBy;
        	// -- mapper
        	Mapper<E,K> mapper;
            // -- null
            boolean allowNull = true;
            // -- duplicates
            boolean allowDuplicates = true;
            boolean allowNullDuplicates = true;
            // -- sort
            /** True to sort using natural comparator */
            boolean sort;
            /** Comparator to use for sorting */
            Comparator<?> comparator;
            /** The specified comparator can handle null values */
            boolean comparatorSortsNull;
            /** Determine whether null values appear first or last */
            boolean sortNullsFirst;
            /** Primitive class to use for storage */
            Class<?> type;
        }

    	// KeyList to build
    	KeyCollection<E> keyColl;
    	// -- order
        boolean sorted;
    	// -- constraint
        boolean allowNullElem;
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

        KeyMap getKeyMap(KeyMapBuilder keyMapBuilder) {
        	KeyMap<E,Object> keyMap = new KeyMap<E,Object>();
        	keyMap.mapper = (Mapper<E, Object>) keyMapBuilder.mapper;
        	keyMap.allowDuplicates = keyMapBuilder.allowDuplicates;

        	boolean allowNullKey = keyMapBuilder.allowNull;
            if (keyMapBuilder.comparator != null) {
                if (allowNullKey && !keyMapBuilder.comparatorSortsNull) {
                	keyMap.comparator = new NullComparator(keyMapBuilder.comparator, keyMapBuilder.sortNullsFirst);
                } else {
                	keyMap.comparator = (Comparator<Object>) keyMapBuilder.comparator;
                }
            } else if (keyMapBuilder.sort) {
//	            	if (!keyMaps.isEmpty()) {
//	            		throw new IllegalArgumentException("Only first key can be sort key"); // TODO support multiple keys
//	            	}
            	sorted = keyMapBuilder.sort;
            	if (allowNullKey) {
                	keyMap.comparator = new NullComparator(NaturalComparator.INSTANCE, keyMapBuilder.sortNullsFirst);
            	} else {
                	keyMap.comparator = NaturalComparator.INSTANCE;
            	}
            }
            if (keyMap.comparator != null) {
            	// TODO
//	                if (keyMapBuilder.sort && keyMap.mapper == IdentMapper.INSTANCE) {
//	                	// Sorted set: we do not need a separate list for storing
//	                	// keys and elements. We have to handle this case specially later.
//	                	keyMap.sortedKeys = (GapList<Object>) keyList;
//	            	} else {
            		if (keyMapBuilder.type == null) {
	            		keyMap.keysList = new GapList<Object>();
            		} else {
            			keyMap.keysList = (GapList<Object>) GapLists.createWrapperList(keyMapBuilder.type);
            		}
            	//}
            } else {
                // Set is not sorted: maintain a separate HashMap for fast
                // answering contains() calls
                keyMap.keysMap = new HashMap<Object, Object>();
            }

            keyMap.allowNull = keyMapBuilder.allowNull;
            return keyMap;
        }

         /**
         * Initialize KeyCollection with specified options.
         *
         * @param keyColl collection to initialize
         */
        void build(KeyCollection<E> keyColl) {
        	keyColl.allowNullElem = allowNullElem;
            keyColl.constraint = constraint;
            keyColl.insertTrigger = insertTrigger;
            keyColl.deleteTrigger = deleteTrigger;

            keyColl.keyMaps = new KeyMap[keyMapBuilders.size()+1];
            if (elemMapBuilder != null) {
            	keyColl.keyMaps[0] = getKeyMap(elemMapBuilder);
            }
            for (int i=0; i<keyMapBuilders.size(); i++) {
            	KeyMapBuilder kmb = keyMapBuilders.get(i);
            	if (kmb == null) {
            		throw new IllegalArgumentException("Key " + i + " is not defined");
            	}
            	keyColl.keyMaps[i+1] = getKeyMap(kmb);
            }

            if (collection != null) {
            	keyColl.addAll(collection);
            } else if (array != null) {
            	keyColl.addAll((Collection<? extends E>) Arrays.asList(array));
            }
        }
    }

//    /**
//     * Builder to construct KeyList instances.
//     */
//    public static class Builder<E> extends BuilderBase<E> {
//        /**
//         * Default constructor.
//         */
//        public Builder() {
//        }
//
//        /**
//         * Internal constructor.
//         *
//         * @param keyList	keyList to setup
//         */
//        Builder(KeyCollection<E> keyList) {
//        	this.keyColl = keyList;
//        }
//
//        /**
//         * Allow null elements.
//         *
//         * @return this (fluent interface)
//         */
//        public Builder<E> withNull() {
//        	return withNull(true);
//        }
//
//        /**
//         * Specify whether null elements are allowed.
//         *
//         * @param allowNullElem	true to allow null elements
//         * @return 				this (fluent interface)
//         */
//        public Builder<E> withNull(boolean allowNullElem) {
//        	endKeyMapBuilder();
//        	this.allowNullElem = allowNullElem;
//        	return this;
//        }
//
//        /**
//         * Specify element constraint.
//         *
//         * @param constraint	constraint element must satisfy
//         * @return 				this (fluent interface)
//         */
//        public Builder<E> withConstraint(Predicate<E> constraint) {
//        	endKeyMapBuilder();
//        	this.constraint = constraint;
//        	return this;
//        }
//
//        /**
//         * Specify insert trigger.
//         *
//         * @param trigger	insert trigger method
//         * @return			this (fluent interface)
//         */
//        public Builder<E> withInsertTrigger(Trigger<E> trigger) {
//        	endKeyMapBuilder();
//            this.insertTrigger = trigger;
//            return this;
//        }
//
//        /**
//         * Specify delete trigger.
//         *
//         * @param trigger	delete trigger method
//         * @return			this (fluent interface)
//         */
//        public Builder<E> withDeleteTrigger(Trigger<E> trigger) {
//        	endKeyMapBuilder();
//            this.deleteTrigger = trigger;
//            return this;
//        }
//
//        //-- Capacity / Elements
//
//        public Builder<E> withElements(Collection<? extends E> elements) {
//        	endKeyMapBuilder();
//            this.collection = elements;
//            return this;
//        }
//
//        public Builder<E> withElements(E... elements) {
//        	endKeyMapBuilder();
//            this.array = elements;
//            return this;
//        }
//
//        //-- Keys
//
//        public Builder<E> withSetKey() {
//        	if (keyMapBuilder != null || !keyMaps.isEmpty()) {
//        		throw new IllegalArgumentException("Set key must be first");
//        	}
//        	newKeyMapBuilder(IdentMapper.INSTANCE);
//        	withKeyNull();
//        	withKeyDuplicates(true);
//        	return this;
//        }
//
//        public Builder<E> withKey(Mapper<E, Object> mapper) {
//        	endKeyMapBuilder();
//        	newKeyMapBuilder(mapper);
//        	return this;
//        }
//
//        public Builder<E> withKeyNull() {
//            return withKeyNull(true);
//        }
//
//        /**
//         * Determines whether null elements are allowed or not.
//         * A null element will have a null key.
//         *
//         * @param nullable  true to allow null elements, false to disallow
//         * @return          this (for use in fluent interfaces)
//         */
//        public Builder<E> withKeyNull(boolean nullable) {
//            return withKeyNull(nullable ? NullMode.NORMAL : NullMode.NONE);
//        }
//
//        public Builder<E> withKeyNull(NullMode nullMode) {
//        	getKeyMapBuilder().nullMode = nullMode;
//            return this;
//        }
//
//        public Builder<E> withKeyDuplicates() {
//        	return withKeyDuplicates(true);
//        }
//
//        /**
//         * Determines whether duplicates are allowed or not.
//         *
//         * @param duplicates    true to allow duplicates, false to disallow
//         * @return              this (for use in fluent interfaces)
//         */
//        public Builder<E> withKeyDuplicates(boolean duplicates) {
//        	getKeyMapBuilder().allowDuplicates = duplicates;
//            return this;
//        }
//
//        /**
//         * Determines that list should be sorted.
//         *
//         * @return              this (for use in fluent interfaces)
//         */
//        public Builder<E> withKeySort() {
//            return withKeySort(true);
//        }
//
//        /**
//         * Determines whether list should be sorted or not.
//         *
//         * @param sort    true to sort list, otherwise false
//         * @return        this (for use in fluent interfaces)
//         */
//        public Builder<E> withKeySort(boolean sort) {
//        	getKeyMapBuilder().sort = sort;
//            return this;
//        }
//
//        /**
//         * Set primitive type to use for sorting.
//         *
//         * @param type    primitive type to use for key
//         * @return        this (for use in fluent interfaces)
//         */
//        public Builder<E> withKeyType(Class<?> type) {
//        	getKeyMapBuilder().type = type;
//        	return this;
//        }
//
//        /**
//         * Set comparator to use for sorting.
//         *
//         * @param comparator    comparator to use for sorting
//         * @return              this (for use in fluent interfaces)
//         */
//        public Builder<E> withKeyComparator(Comparator<? super Object> comparator) {
//            return withKeyComparator(comparator, false);
//        }
//
//        /**
//         * Set comparator to use for sorting.
//         *
//         * @param comparator            comparator to use for sorting
//         * @param comparatorSortsNull   true if comparator sorts null, false if not
//         * @return                      this (for use in fluent interfaces)
//         */
//        public Builder<E> withKeyComparator(Comparator<? super Object> comparator, boolean comparatorSortsNull) {
//        	KeyMapBuilder<E, Object> kmb = getKeyMapBuilder();
//        	kmb.comparator = comparator;
//            kmb.comparatorSortsNull = comparatorSortsNull;
//            return this;
//        }
//
//        /**
//         * Determines that nulls are sorted first.
//         *
//         * @return  this (for use in fluent interfaces)
//         */
//        public Builder<E> withKeyNullsFirst() {
//            return withKeyNullsFirst(true);
//        }
//
//        /**
//         * Determines whether nulls are sorted first or last.
//         *
//         * @param nullsFirst    true to sort nulls first, false to sort nulls last
//         * @return              this (for use in fluent interfaces)
//         */
//        public Builder<E> withKeyNullsFirst(boolean nullsFirst) {
//        	getKeyMapBuilder().sortNullsFirst = nullsFirst;
//            return this;
//        }
//
//        /**
//         * @return
//         */
//        public KeyCollection<E> build() {
//        	if (keyColl == null) {
//               	keyColl = new KeyCollection<E>();
//        	}
//        	// If no keys have been defined, create a set key so the elements can be stored somewhere
//        	if (keyMapBuilder == null && keyMaps.isEmpty()) {
//        		withSetKey();
//        	}
//        	build(keyColl);
//        	return (KeyCollection<E>) keyColl;
//        }
//    }

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
	    		copy.keysList = keysList.copy();
	    	}
	    	return copy;
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

	    @SuppressWarnings("unchecked")
		Iterator<E> iterator() {
	    	if (keysMap != null) {
	    		return (Iterator<E>) keysMap.values().iterator();
	    	} else {
	    		return (Iterator<E>) keysList.iterator();
	    	}
	    }

	    static class KeyMapIter<E> implements Iterator<E> {

	    	KeyCollection<E> coll;
	    	Iterator<E> iter;
	    	boolean hasElem = false;
	    	E elem;

	    	public KeyMapIter(KeyCollection<E> coll, Iterator<E> iter) {
	    		this.coll = coll;
	    		this.iter = iter;
	    	}

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public E next() {
				hasElem = false;
				elem = iter.next();
				hasElem = true;
				return elem;
			}

			@Override
			public void remove() {
				if (hasElem) {
					iter.remove();
					coll.remove(elem, true);
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
	     * @param key
	     * @return		removed object
	     */
	    Option<E> removeKey(Object key) {
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
		            elem = list.removeFirst();
		            if (list.isEmpty()) {
		                keysMap.remove(key);
		            }
		        } else {
		            elem = (E) keysMap.remove(key);
		        }
		        return new Option(elem);
	    	} else {
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
	// -- null
    boolean allowNullElem;
    Predicate<E> constraint;
    // -- handlers
    Trigger<E> insertTrigger;
    Trigger<E> deleteTrigger;

    KeyCollection() {
    }

    /**
     * Initialize object for copy() operation.
     *
     * @param that source object
     */
    void initCopy(KeyCollection<E> that) {
    	size = that.size;
    	keyMaps = new KeyMap[that.keyMaps.length];
    	for (int i=0; i<keyMaps.length; i++) {
    		keyMaps[i] = that.keyMaps[i].copy();
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
    void initCrop(KeyCollection<E> that) {
    	size = that.size;
    	keyMaps = new KeyMap[that.keyMaps.length];
    	for (int i=0; i<keyMaps.length; i++) {
    		keyMaps[i] = that.keyMaps[i].crop();
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
    	for (KeyMap<E,?> keyMap: keyMaps) {
    		doDebugCheck(keyMap);
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

    /**
     * Default constructor.
     * Internal use in builder and child classes only.
     *
     * @param ignore ignored parameter for unique method signature
     */
//    protected KeyCollection(boolean ignore) {
//        super(ignore);
//    }

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
		return getKeyMap(0).containsKey(o);
	}

	@Override
	public Iterator<E> iterator() {
		return getKeyMap(0).iterator();
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
        int i = (iterator ? 1 : 0);
        for (; i<keyMaps.length; i++) {
       		Object key = keyMaps[i].mapper.getKey((E) elem);
       		Option<E> obj = keyMaps[i].removeKey(key);
       		if (i == 0) {
       			if (!obj.hasValue()) {
       				return false;
       			} else {
       				removed = obj.getValue();
       			}
       		} else {
       			removed = (E) obj.getValue();
       			if (obj != removed) {
       				errorInvalidData();
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
	    K key;
	    if (elem == null) {
	        key = null;
	    } else {
	        key = keyMap.mapper.getKey(elem);
	    }
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
	    			Object key = keyMaps[i].mapper.getKey(elem);
		    		keyMaps[i].add(key, elem);
		    	}
    		}
    		catch (IllegalArgumentException e) {
    			error = e;
    		}
    	}
    	if (error != null) {
    		for (i--; i>=0; i--) {
    			Object key = keyMaps[i].mapper.getKey(elem);
    			keyMaps[i].removeKey(key);
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
     * Returns count of distinct keys.
     *
     * @return count of distinct keys
     */
    protected int getCountDistinctKeys(int keyIndex) {
    	return getCountDistinctKeys(getKeyMap(keyIndex));
    }

    private <K> int getCountDistinctKeys(KeyMap<E,K> keyMap) {
    	if (keyMap.keysMap != null) {
    		return keyMap.keysMap.size();
    	} else {
    		return getAllDistinctKeys(keyMap).size();
    	}
    }

    /**
     * Returns list containing all distinct keys.
     *
     * @return list containing all distinct keys
     */
    protected GapList<Object> getAllDistinctKeys(int keyIndex) {
    	return getAllDistinctKeys(getKeyMap(keyIndex));
    }

    private <K> GapList<K> getAllDistinctKeys(KeyMap<E,K> keyMap) {
        if (keyMap.keysMap != null) {
            GapList<K> list = new GapList<K>(keyMap.keysMap.keySet());
            return list;
        } else {
            K lastKey = null;
            GapList<K> list = new GapList<K>();
            for (int i=0; i<keyMap.keysList.size(); i++) {
                K key = keyMap.keysList.get(i);
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
    	Object key = keyMap.mapper.getKey(elem);

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
     * @param key   key of element to remove
     * @return      removed element or null if no element has been removed
     */
    protected E removeByKey(int keyIndex, Object key) {
    	checkKeyMap(keyIndex);
    	Option<E> removed = keyMaps[keyIndex].removeKey(key);
    	if (removed.hasValue()) {
    		for (int i=0; i<keyMaps.length; i++) {
    			if (i != keyIndex) {
    				Object k = keyMaps[i].mapper.getKey(removed.getValue());
    				keyMaps[i].removeKey(k);
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
    				Object k = keyMaps[i].mapper.getKey(removed);
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

}
