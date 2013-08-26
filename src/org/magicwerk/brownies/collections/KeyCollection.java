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

import org.magicwerk.brownies.collections.KeyList.Builder;
import org.magicwerk.brownies.collections.function.Mapper;
import org.magicwerk.brownies.collections.function.Predicate;
import org.magicwerk.brownies.collections.function.Trigger;
import org.magicwerk.brownies.collections.helper.NaturalComparator;
import org.magicwerk.brownies.collections.helper.NullComparator;
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
 * A KeyList add key handling features to GapList.
 * It is the abstract base class for both SetList and MapList.
 *
 * @author Thomas Mauch
 * @version $Id: KeyList.java 1829 2013-08-20 06:45:35Z origo $
 *
 * @see GapList
 * @param <E> type of elements stored in the list
 */
public class KeyCollection<E> implements Collection<E> {


    // FIXME: constraint check - silent vs. error
    /**
     * Mode to control handling of duplicate values:
     * REPLACE, IGNORE, ERROR, ALLOW.
     */
    public enum DuplicateMode {
        /** Duplicates on add or set are ignored, i.e. the old value remains in place (default mode) */
        IGNORE,
        /** Duplicates on add or set will replace the old value */
        REPLACE,
        /** Duplicates on add or set will be rejected by throwing an exception */
        ERROR,
        /** Duplicates are allowed */
        ALLOW
    }

    /**
     * Mode to control handling of duplicate values:
     * NONE, NORMAL, MULTIPLE.
     */
    public enum NullMode {
        /** Null values are not allowed. */
        NONE,
        /**
         * Null values are treated like normal values, so if duplicates
         * are allowed there can be multiple
         */
        NORMAL,
        /** Duplicates on add or set will be rejected by throwing an exception */
        MULTIPLE
    }

    /**
     * Data used for builders.
     */
    public static class BuilderBase<E> {

        public static class KeyMapBuilder<E,K> {
        	// -- mapper
        	Mapper<E,K> mapper;
            // -- null
            NullMode nullMode = NullMode.NONE;
            // -- duplicates
            DuplicateMode duplicateMode = DuplicateMode.IGNORE;
            // -- sort
            /** True to sort using natural comparator */
            boolean sort;
            /** Comparator to use for sorting */
            Comparator<?> comparator;
            /** The specified comparator can handle null values */
            boolean comparatorSortsNull;
            /** Determine whether null values appear first or last */
            boolean sortNullsFirst;
            /** Primitive class to use for sorting */
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
        KeyMapBuilder keyMapBuilder;
    	GapList<KeyMap<E,Object>> keyMaps = GapList.create();
        // -- content
        Collection<? extends E> collection;
        E[] array;

        //-- Implementation

        KeyMapBuilder<E, Object> getKeyMapBuilder() {
        	if (keyMapBuilder == null) {
        		throw new IllegalArgumentException("Call withKey() to define a new key first");
        	}
        	return keyMapBuilder;
        }

        void newKeyMapBuilder(Mapper<E,Object> mapper) {
        	assert(keyMapBuilder == null);
        	keyMapBuilder = new KeyMapBuilder<E,Object>();
        	keyMapBuilder.mapper = mapper;
        }

        void endKeyMapBuilder() {
        	if (keyMapBuilder != null) {
            	KeyMap<E,Object> keyMap = new KeyMap<E,Object>();
            	keyMap.mapper = (Mapper<E, Object>) keyMapBuilder.mapper;
            	keyMap.duplicateMode = keyMapBuilder.duplicateMode;

            	boolean allowNullKey = (keyMapBuilder.nullMode != NullMode.NONE);
                if (keyMapBuilder.comparator != null) {
	                if (allowNullKey && !keyMapBuilder.comparatorSortsNull) {
	                	keyMap.comparator = new NullComparator(keyMapBuilder.comparator, keyMapBuilder.sortNullsFirst);
	                } else {
	                	keyMap.comparator = (Comparator<Object>) keyMapBuilder.comparator;
	                }
                } else if (keyMapBuilder.sort) {
	            	if (!keyMaps.isEmpty()) {
	            		throw new IllegalArgumentException("Only first key can be sort key"); // TODO support multiple keys
	            	}
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
		            		keyMap.sortedKeys = new GapList<Object>();
	            		} else {
	            			keyMap.sortedKeys = (GapList<Object>) createWrapperList(keyMapBuilder.type);
	            		}
	            	//}
	            } else {
	                // Set is not sorted: maintain a separate HashMap for fast
	                // answering contains() calls
	                keyMap.unsortedKeys = new HashMap<Object, Object>();
	            }

                keyMap.allowNullKeys = keyMapBuilder.nullMode;
                keyMaps.add(keyMap);

        		keyMapBuilder = null;
        	}
        }

        /**
         * Create a GapList wrapping a primitive GapList, e.g. IntObjGapList.
         *
         * @param type	primitive type for GapList
         * @return		create wrapping GapList
         * @throws 		IllegalArgumentException if no primitive type is specified
         */
        static GapList<?> createWrapperList(Class<?> type) {
        	if (type == int.class) {
        		return new IntObjGapList();
        	} else if (type == long.class) {
            	return new LongObjGapList();
        	} else if (type == double.class) {
            	return new DoubleObjGapList();
        	} else if (type == float.class) {
            	return new FloatObjGapList();
        	} else if (type == boolean.class) {
            	return new BooleanObjGapList();
        	} else if (type == byte.class) {
            	return new ByteObjGapList();
        	} else if (type == char.class) {
            	return new CharObjGapList();
        	} else if (type == short.class) {
            	return new ShortObjGapList();
        	} else {
        		throw new IllegalArgumentException("Primitive type expected: " + type);
        	}
        }

        /**
         * Initialize KeyList with specified options.
         *
         * @param keyColl keyList to initialize
         */
        void build(KeyCollection<E> keyColl) {
        	endKeyMapBuilder();

            keyColl.keyMaps = keyMaps.toArray(new KeyMap[keyMaps.size()]);

            if (collection != null) {
            	keyColl.addAll(collection);
            } else if (array != null) {
            	keyColl.addAll((Collection<? extends E>) Arrays.asList(collection));
            }
        }
    }

    /**
     * Builder to construct KeyList instances.
     */
    public static class Builder<E> extends BuilderBase<E> {
        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Internal constructor.
         *
         * @param keyList	keyList to setup
         */
        Builder(KeyCollection<E> keyList) {
        	this.keyColl = keyList;
        }

        /**
         * Allow null elements.
         *
         * @return this (fluent interface)
         */
        public Builder<E> withNull() {
        	return withNull(true);
        }

        /**
         * Specify whether null elements are allowed.
         *
         * @param allowNullElem	true to allow null elements
         * @return 				this (fluent interface)
         */
        public Builder<E> withNull(boolean allowNullElem) {
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
        public Builder<E> withConstraint(Predicate<E> constraint) {
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
        public Builder<E> withInsertTrigger(Trigger<E> trigger) {
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
        public Builder<E> withDeleteTrigger(Trigger<E> trigger) {
        	endKeyMapBuilder();
            this.deleteTrigger = trigger;
            return this;
        }

        //-- Capacity / Elements

        public Builder<E> withElements(Collection<? extends E> elements) {
        	endKeyMapBuilder();
            this.collection = elements;
            return this;
        }

        public Builder<E> withElements(E... elements) {
        	endKeyMapBuilder();
            this.array = elements;
            return this;
        }

        //-- Keys

        public Builder<E> withKey(Mapper<E, Object> mapper) {
        	endKeyMapBuilder();
        	newKeyMapBuilder(mapper);
        	return this;
        }

        /**
         * Determines whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param nullable  true to allow null elements, false to disallow
         * @return          this (for use in fluent interfaces)
         */
        public Builder<E> withKeyNull(boolean nullable) {
            return withKeyNull(nullable ? NullMode.NORMAL : NullMode.NONE);
        }

        public Builder<E> withKeyNull(NullMode nullMode) {
        	getKeyMapBuilder().nullMode = nullMode;
            return this;
        }

        /**
         * Determines whether duplicates are allowed or not.
         *
         * @param duplicates    true to allow duplicates, false to disallow
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E> withKeyDuplicates(DuplicateMode duplicateMode) {
        	getKeyMapBuilder().duplicateMode = duplicateMode;
            return this;
        }

        /**
         * Determines that list should be sorted.
         *
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E> withKeySort() {
            return withKeySort(true);
        }

        /**
         * Determines whether list should be sorted or not.
         *
         * @param sort    true to sort list, otherwise false
         * @return        this (for use in fluent interfaces)
         */
        public Builder<E> withKeySort(boolean sort) {
        	getKeyMapBuilder().sort = sort;
            return this;
        }

        /**
         * Set primitive type to use for sorting.
         *
         * @param type    primitive type to use for key
         * @return        this (for use in fluent interfaces)
         */
        public Builder<E> withKeyType(Class<?> type) {
        	getKeyMapBuilder().type = type;
        	return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E> withKeyComparator(Comparator<? super Object> comparator) {
            return withKeyComparator(comparator, false);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param comparatorSortsNull   true if comparator sorts null, false if not
         * @return                      this (for use in fluent interfaces)
         */
        public Builder<E> withKeyComparator(Comparator<? super Object> comparator, boolean comparatorSortsNull) {
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
        public Builder<E> withKeyNullsFirst() {
            return withKeyNullsFirst(true);
        }

        /**
         * Determines whether nulls are sorted first or last.
         *
         * @param nullsFirst    true to sort nulls first, false to sort nulls last
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E> withKeyNullsFirst(boolean nullsFirst) {
        	getKeyMapBuilder().sortNullsFirst = nullsFirst;
            return this;
        }

        /**
         * @return
         */
        public KeyCollection<E> build() {
        	if (keyColl == null) {
               	keyColl = new KeyCollection<E>();
        	}
        	build(keyColl);
        	return (KeyCollection<E>) keyColl;
        }
    }

    static class KeyMap<E, K> {
	    /** A mapper to extract keys out of element for a MapList. For a SetList, this is always an IdentMapper. */
	    Mapper<E, K> mapper;
	    /** True to allow null keys */
	    NullMode allowNullKeys;
	    /** True to allow duplicate values. This also allows duplicate null values, but they are not distinct. */
	    DuplicateMode duplicateMode = DuplicateMode.IGNORE;
	    /** Comparator to use for sorting (if null, elements are not sorted) */
	    Comparator<K> comparator;
	    /**
	     * Key storage if not sorted. The values are single elements or a list of elements.
	     * Note that we cannot use TreeMap as K may not be comparable
	     */
	    HashMap<K, Object> unsortedKeys;
	    /** Key storage if sorted */
	    GapList<K> sortedKeys;

	    KeyMap() {
	    }

	    KeyMap(KeyMap that) {
	    	mapper = that.mapper;
	    	allowNullKeys = that.allowNullKeys;
	    	duplicateMode = that.duplicateMode;
	    	comparator = that.comparator;
	    }
    }
    /**
     * There can be 0, 1, or several keys.
     * If there are no keys, all key methods will fail. This can be used, if a constraint list is needed.
     * If there are keys, only the first key can be sorted.
     */
    KeyMap<E, Object>[] keyMaps;
	// -- null
    boolean allowNullElem;
    Predicate<E> constraint;
    // -- handlers
    Trigger<E> insertTrigger;
    Trigger<E> deleteTrigger;

    /** If true the invariants the GapList are checked for debugging */
    private static final boolean DEBUG_CHECK = true;

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
    	if (keyMap.unsortedKeys != null) {
    		int count = 0;
    		for (Object obj: keyMap.unsortedKeys.values()) {
    			if (obj instanceof GapList) {
    				count += ((GapList) obj).size();
    			} else {
    				count++;
    			}
    		}
    		assert(count == keyMap.unsortedKeys.size());
    	} else if (keyMap.sortedKeys != null) {
    		GapList<?> copy = keyMap.sortedKeys.copy();
    		copy.sort(keyMap.comparator);
    		assert(copy.equals(keyMap.sortedKeys));
    	} else {
    		assert(false);
    	}
    }

    public static <E> KeyCollection<E> create() {
        return new KeyCollection<E>();
    }

	public static <E> KeyCollection<E> create(Collection<? extends E> elements) {
		return new KeyCollection<E>(elements);
	}

	/**
	 * Create new collection and add elements.
	 *
	 * @param elems 	elements to add
	 * @return 			created array list
	 * @param <E> 		type of elements stored in the list
	 */
	public static <E> KeyCollection<E> create(E... elements) {
		return new KeyCollection<E>(elements);
	}

	/**
	 * Default constructor.
	 */
	public KeyCollection() {
	}

	/**
	 * Copy constructor.
	 *
	 * @param that	source object to copy
	 */
	public KeyCollection(Collection<? extends E> that) {
		addAll(that);
	}

	/**
	 * Copy constructor.
	 *
	 * @param that	source object to copy
	 */
	public KeyCollection(E... that) {
		addAll(Arrays.asList(that));
	}

    /**
     * Internal initialization for copy() or crop() operation,
     * i.e. no element storage is allocated and initialized.
     *
     * @param that source object
     */
//	KeyCollection(KeyCollection<E> that) {
//	    keyMaps = that.keyMaps;
//	}

    /**
     * The method onAttach() is called before a new element is added to or set in the list.
     * For a set() operation, onDetach() is first called for the old element and then
     * onAttach() is called for the new element.
     *
     * @param elem element which is stored in list
     */
    protected void onInsert(E elem) {
        if (insertTrigger != null) {
            insertTrigger.handle(elem);
        }
    }

    /**
     * The method onDetach() is called before an element is removed from or overwritten in the list.
     * For a set() operation, onDetach() is first called for the old element and then
     * onAttach() is called for the new element.
     *
     * @param elem element which is detached from the list
     */
    protected void onDelete(E elem) {
        if (deleteTrigger != null) {
            deleteTrigger.handle(elem);
        }
    }

    void checkElemAllowed(E elem) {
    	if (elem == null) {
    		if (!allowNullElem) {
    			throw new IllegalArgumentException("Constraint violation: null element not allowed");
    		}
    	} else {
    		if (constraint != null) {
    			if (!constraint.allow(elem)) {
        			throw new IllegalArgumentException("Constraint violation: element not allowed");
    			}
    		}
    	}
    }

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return size() == 0;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Initialize object for crop() operation.
	 *
	 * @param that source object
	 */
	@SuppressWarnings("unchecked")
    void initCrop(KeyCollection<E> that) {
	    keyMaps = init(that.keyMaps);
	}

    /**
     * Initialize object for copy() operation.
     *
     * @param that source object
     */
    void initCopy(KeyCollection<E> that) {
 	    keyMaps = copy(that.keyMaps);
    }

    private <K> KeyMap[] init(KeyMap<E,K>[] keyMaps) {
    	KeyMap<E,K>[] copy = new KeyMap[keyMaps.length];
    	for (int i=0; i<keyMaps.length; i++) {
    		copy[i] = init(keyMaps[i]);
    	}
    	return copy;
    }

    private <K> KeyMap[] copy(KeyMap<E,K>[] keyMaps) {
    	KeyMap<E,K>[] copy = new KeyMap[keyMaps.length];
    	for (int i=0; i<keyMaps.length; i++) {
    		copy[i] = copy(keyMaps[i]);
    	}
    	return copy;
    }

    private <K> KeyMap<E,K> init(KeyMap<E,K> keyMap) {
    	KeyMap<E,K> copy = new KeyMap(keyMap);
        if (keyMap.unsortedKeys != null) {
        	copy.unsortedKeys = new HashMap<K, Object>();
        } else {
        	// Note that the check (keyMap.sortedKeys != this) does not work here
        	// as cloned KeyMap points to the old this pointer
        	// TODO
        	//if (keyMap.sortedKeys.getClass() == GapList.class) {
        		copy.sortedKeys = new GapList<K>();
        	//} else {
//	        	copy.sortedKeys = (GapList<K>) this;
        	//}
        }
        return copy;
    }

    private <K> KeyMap<E,K> copy(KeyMap<E,K> keyMap) {
    	KeyMap<E,K> copy = new KeyMap(keyMap);
        if (keyMap.unsortedKeys != null) {
        	copy.unsortedKeys = new HashMap<K, Object>(keyMap.unsortedKeys);
        } else {
        	// Note that the check (keyMap.sortedKeys != this) does not work here
        	// as cloned KeyMap points to the old this pointer
        	// TODO
        	//if (keyMap.sortedKeys.getClass() == GapList.class) {
	        	copy.sortedKeys = keyMap.sortedKeys.copy();
	        //} else {
//	        	copy.sortedKeys = (GapList<K>) this;
	        //}
        }
        return copy;
    }

    @Override
    public void clear() {
    	for (KeyMap<E,Object> keyMap: keyMaps) {
    		doClear(keyMap);
    	}
    }

    private void doClear(KeyMap<E,?> keyMap) {
        if (keyMap.unsortedKeys != null) {
        	keyMap.unsortedKeys.clear();
        } else {
        	keyMap.sortedKeys.clear();
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
        if (key == null && keyMap.allowNullKeys == NullMode.NONE) {
            throw new IllegalArgumentException("Null key not allowed");
        }
        return key;
	}

	private <K> void doAddUnsorted(KeyMap<E,K> keyMap, E elem, K key) {
		Object obj = keyMap.unsortedKeys.get(key);
	    if (obj == null) {
	    	if (!keyMap.unsortedKeys.containsKey(key)) {
	    		keyMap.unsortedKeys.put(key, elem);
	        } else {
	            GapList<E> list = (GapList<E>) GapList.create(null, elem);
	            keyMap.unsortedKeys.put(key, list);
	    	}
	    } else if (obj instanceof GapList) {
            GapList<E> list = (GapList<E>) obj;
            list.add(elem);
        } else {
            GapList<E> list = (GapList<E>) GapList.create(obj, elem);
            keyMap.unsortedKeys.put(key, list);
        }
	}

    private <K> E doRemoveUnsorted(KeyMap<E,K> keyMap, K key, Object obj) {
        assert(obj != null);

        E elem = null;
        if (obj instanceof GapList) {
            GapList<E> list = (GapList<E>) obj;
            elem = list.removeFirst();
            if (list.isEmpty()) {
                keyMap.unsortedKeys.remove(key);
            }
        } else {
            elem = (E) keyMap.unsortedKeys.remove(key);
        }
        return elem;
    }

    @Override
    public boolean add(E elem) {
    	IllegalArgumentException error = null;
    	boolean add = true;
		int i = 0;
    	if (keyMaps != null) {
    		try {
		    	for (i=0; i<keyMaps.length; i++) {
		    		add = doAddKey(keyMaps[i], elem);
		    		if (!add) {
		    			break;
		    		}
		    	}
    		}
    		catch (IllegalArgumentException e) {
    			add = false;
    			error = e;
    		}
    	}
    	if (!add) {
    		for (i--; i>=0; i--) {
    			Object key = keyMaps[i].mapper.getKey(elem);
    			doRemoveByKey(keyMaps[i], key);
    		}
    		if (error != null) {
    			throw error;
    		} else {
    			return false;
    		}
    	}
        if (DEBUG_CHECK) debugCheck();
        return true;
    }

    /**
     * Add element.
     *
     * @param elem		element to add
     * @return			index where element should be added (-1 is valid), otherwise Integer.MIN_VALUE
     */
    private <K> boolean doAddKey(KeyMap<E,K> keyMap, E elem) {
        K key = getKey(keyMap, elem);
        if (keyMap.unsortedKeys != null) {
            // Keys not sorted
            if (!keyMap.unsortedKeys.containsKey(key)) {
                // New key
                doAddUnsorted(keyMap, elem, key);
                return true;

            } else {
                // Key exists already
                if (keyMap.duplicateMode == DuplicateMode.ALLOW ||
                		(key == null && keyMap.allowNullKeys == NullMode.MULTIPLE)) {
                	// Add duplicate
                    doAddUnsorted(keyMap, elem, key);
                    return true;

                } else {
                	// Handle duplicate without adding
                    if (keyMap.duplicateMode == DuplicateMode.REPLACE) {
                        Object oldElem = keyMap.unsortedKeys.put(key, elem);
                        return false;	// TODO what should be returned? false -> attach is not called

                    } else if (keyMap.duplicateMode == DuplicateMode.IGNORE) {
                        return false;
                    } else if (keyMap.duplicateMode == DuplicateMode.ERROR) {
                        throw new IllegalArgumentException("Duplicate key not allowed: " + key);
                    } else {
                        throw new AssertionError();
                    }
                }
            }

        } else {
            // Sorted keys
        	int addIndex = 0;
        	if (!keyMap.sortedKeys.isEmpty()) {
        		if (keyMap.comparator.compare(key, keyMap.sortedKeys.getLast()) > 0) {
        			addIndex = -keyMap.sortedKeys.size() - 1;
        		} else if (keyMap.comparator.compare(key, keyMap.sortedKeys.getFirst()) < 0) {
        			addIndex = -1;
        		}
        	}
        	if (addIndex == 0) {
        		addIndex = SortedLists.binarySearchAdd(keyMap.sortedKeys, key, keyMap.comparator);
        	}
            if (addIndex < 0) {
                // New key
                addIndex = -addIndex - 1;
                keyMap.sortedKeys.doAdd(addIndex, key);
                return true;

            } else {
                // Key exists already
                if (keyMap.duplicateMode == DuplicateMode.ALLOW ||
                		(key == null && keyMap.allowNullKeys == NullMode.MULTIPLE)) {
                	;
                } else if (keyMap.duplicateMode == DuplicateMode.REPLACE) {
                    int getIndex = SortedLists.binarySearchGet(keyMap.sortedKeys, key, keyMap.comparator);
                   	keyMap.sortedKeys.set(getIndex, key);
                    return false;	// TODO what should be returned? false -> attach is not called

                } else if (keyMap.duplicateMode == DuplicateMode.IGNORE) {
                    return false;
                } else if (keyMap.duplicateMode == DuplicateMode.ERROR) {
                    throw new IllegalArgumentException("Duplicate key not allowed: " + key);
                } else {
                    throw new AssertionError();
                }
               	keyMap.sortedKeys.doAdd(addIndex, key);
                return true;
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
        return doContainsKey(getKeyMap(keyIndex), key);
    }

    private <K> boolean doContainsKey(KeyMap<E,K> keyMap, K key) {
        if (key == null) {
            if (keyMap.allowNullKeys == NullMode.NONE) {
                return false;
            }
        }

        if (keyMap.unsortedKeys != null) {
            // not sorted
           return keyMap.unsortedKeys.containsKey(key);
        } else {
            // sorted
            int index = SortedLists.binarySearchGet(keyMap.sortedKeys, key, keyMap.comparator);
            return (index >= 0);
        }
    }

    /**
     * Returns count of distinct keys.
     *
     * @return count of distinct keys
     */
    public int getCountDistinctKeys(int keyIndex) {
    	return getCountDistinctKeysCount(getKeyMap(keyIndex));
    }

    private <K> int getCountDistinctKeysCount(KeyMap<E,K> keyMap) {
    	if (keyMap.unsortedKeys != null) {
    		return keyMap.unsortedKeys.size();
    	} else {
    		return getDistinctKeys(keyMap).size();
    	}
    }

    /**
     * Returns list containing all distinct keys.
     *
     * @return list containing all distinct keys
     */
    public GapList<Object> getDistinctKeys(int keyIndex) {
    	return getDistinctKeys(getKeyMap(keyIndex));
    }

    private <K> GapList<K> getDistinctKeys(KeyMap<E,K> keyMap) {
        if (keyMap.unsortedKeys != null) {
            GapList<K> list = new GapList<K>(keyMap.unsortedKeys.keySet());
            return list;
        } else {
            K lastKey = null;
            GapList<K> list = new GapList<K>();
            for (int i=0; i<keyMap.sortedKeys.size(); i++) {
                K key = keyMap.sortedKeys.get(i);
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
    public E getByKey(int keyIndex, Object key) {
    	return getByKey(getKeyMap(keyIndex), key);
    }

    private <K> E getByKey(KeyMap<E,K> keyMap, K key) {
        // Handle null key if not allowed to prevent NPE
        if (key == null) {
            if (keyMap.allowNullKeys == NullMode.NONE) {
                return null;
            }
        }

        if (keyMap.unsortedKeys != null) {
            // not sorted
            Object obj = keyMap.unsortedKeys.get(key);
            if (obj instanceof GapList) {
                GapList<E> list = (GapList<E>) obj;
                return list.getFirst();
            } else {
                return (E) keyMap.unsortedKeys.get(key);
            }

        } else {
            // sorted
            int index = SortedLists.binarySearchGet(keyMap.sortedKeys, key, keyMap.comparator);
            if (index >= 0) {
                return (E) keyMap.sortedKeys.doGet(index);
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
    @SuppressWarnings("unchecked")
    public GapList<E> getAllByKey(int keyIndex, Object key) {
    	return doGetAllByKey(getKeyMap(keyIndex), key);
    }

    private <K> GapList<E> doGetAllByKey(KeyMap<E,K> keyMap, K key) {
        // Handle null key if not allowed to prevent NPE
        if (key == null) {
            if (keyMap.allowNullKeys == NullMode.NONE) {
                return GapList.EMPTY();
            }
        }

        if (keyMap.unsortedKeys != null) {
            // not sorted
            Object obj = keyMap.unsortedKeys.get(key);
            if (obj == null) {
                return GapList.EMPTY();
            } else if (obj instanceof GapList) {
                GapList<E> list = (GapList<E>) obj;
                return list.unmodifiableList();
            } else {
                return (GapList<E>) GapList.create(keyMap.unsortedKeys.get(key));
            }

        } else {
            // sorted
            int index = SortedLists.binarySearchGet(keyMap.sortedKeys, key, keyMap.comparator);
            if (index >= 0) {
                GapList<E> list = new GapList<E>();
                while (true) {
                    list.add((E) keyMap.sortedKeys.doGet(index));
                    index++;
                    if (index == keyMap.sortedKeys.size()) {
                        break;
                    }
                    if (!GapList.equalsElem(keyMap.sortedKeys.get(index), key)) {
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
    public int getCountByKey(int keyIndex, Object key) {
    	return getCountByKey(getKeyMap(keyIndex), key);
    }

    private <K> int getCountByKey(KeyMap<E,K> keyMap, K key) {
        // Handle null key if not allowed to prevent NPE
        if (key == null) {
            if (keyMap.allowNullKeys == NullMode.NONE) {
                return 0;
            }
        }

        if (keyMap.unsortedKeys != null) {
            // not sorted
            Object obj = keyMap.unsortedKeys.get(key);
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
            int index = SortedLists.binarySearchGet(keyMap.sortedKeys, key, keyMap.comparator);
            if (index >= 0) {
                int count = 0;
                while (true) {
                    count++;
                    index++;
                    if (index == keyMap.sortedKeys.size()) {
                        break;
                    }
                    if (!GapList.equalsElem(keyMap.sortedKeys.get(index), key)) {
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
    	boolean allowDuplicates = (keyMap.duplicateMode == DuplicateMode.ALLOW);
    	Object key = keyMap.mapper.getKey(elem);

    	if (keyMap.unsortedKeys != null) {
    		Iterator<Map.Entry> iter = keyMap.unsortedKeys.entrySet().iterator();
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
    		assert(keyMap.sortedKeys != null);
    		for (int i=0; i<keyMap.sortedKeys.size(); i++) {
    			if (GapList.equalsElem(elem, keyMap.sortedKeys.doGet(i))) {
    				if (GapList.equalsElem(key, keyMap.sortedKeys.get(i))) {
    					return null;
    				}
    				keyMap.sortedKeys.remove(i);
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
    public E removeByKey(int keyIndex, Object key) {
    	checkKeyMap(keyIndex);
    	E removed = doRemoveByKey(keyMaps[keyIndex], key);
    	if (removed != null) {
    		for (int i=0; i<keyMaps.length; i++) {
    			if (i != keyIndex) {
    				Object k = keyMaps[i].mapper.getKey(removed);
    				doRemoveByKey(keyMaps[i], k);
    			}
    		}
    	}
        if (DEBUG_CHECK) debugCheck();
        return removed;
    }

    /**
     * Remove element with specified key from key map.
     *
     * @param keyMap		key map
     * @param key			key to remove
     * @param removeElems
     * @return				removed element or null
     */
    private <K> E doRemoveByKey(KeyMap<E,K> keyMap, K key) {
    	// If list cannot contain null, handle null explicitly to prevent NPE
    	if (key == null) {
    		if (keyMap.allowNullKeys == NullMode.NONE) {
    			return null;
    		}
    	}
        if (keyMap.unsortedKeys != null) {
            // not sorted
        	if (!keyMap.unsortedKeys.containsKey(key)) {
        		return null;
        	}
            Object obj = keyMap.unsortedKeys.get(key);
            E elem = doRemoveUnsorted(keyMap, key, obj);
            return elem;

        } else {
            // sorted
            int index = SortedLists.binarySearchGet(keyMap.sortedKeys, key, keyMap.comparator);
            if (index >= 0) {
               	E elem = (E) keyMap.sortedKeys.remove(index);
                return elem;
            } else {
                return null;
            }
        }
    }

    /**
     * Removes element by key.
     * If there are duplicates, all elements are removed.
     *
     * @param keyIndex
     * @param key   	key of element to remove
     * @return      	true if elements have been removed, false otherwise
     */
    @SuppressWarnings("unchecked")
    public GapList<E> removeAllByKey(int keyIndex, Object key) {
    	checkKeyMap(keyIndex);
    	GapList<E> removeds = doRemoveAllByKey(keyMaps[keyIndex], key);
    	for (E removed: removeds) {
    		for (int i=0; i<keyMaps.length; i++) {
    			if (i != keyIndex) {
    				Object k = keyMaps[i].mapper.getKey(removed);
    				doRemoveAllByKey(keyMaps[i], k);
    			}
    		}
    	}
        if (DEBUG_CHECK) debugCheck();
        return removeds;
    }

    private <K> GapList<E> doRemoveAllByKey(KeyMap<E,K> keyMap, K key) {
    	// If list cannot contain null, handle null explicitly to prevent NPE
    	if (key == null) {
    		if (keyMap.allowNullKeys == NullMode.NONE) {
    			return GapList.EMPTY();
    		}
    	}
        if (keyMap.unsortedKeys != null) {
            // not sorted
        	if (!keyMap.unsortedKeys.containsKey(key)) {
        		return GapList.EMPTY();
        	}
            Object obj = keyMap.unsortedKeys.remove(key);
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
        	int index = SortedLists.binarySearchGet(keyMap.sortedKeys, key, keyMap.comparator);
            if (index < 0) {
                return GapList.EMPTY();
            }
            int start = index;
            while (true) {
                index++;
                if (index == keyMap.sortedKeys.size()) {
                    break;
                }
                if (!GapList.equalsElem(keyMap.sortedKeys.get(index), key)) {
                    break;
                }
            }
            GapList<E> removed = (GapList<E>) keyMap.sortedKeys.get(start, index-start);
            keyMap.sortedKeys.remove(start, index-start);
            return removed;
        }
    }

}
