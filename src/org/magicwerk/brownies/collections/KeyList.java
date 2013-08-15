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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.magicwerk.brownies.collections.function.Mapper;
import org.magicwerk.brownies.collections.helper.NaturalComparator;
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
 * @version $Id$
 *
 * @see GapList
 * @param <E> type of elements stored in the list
 */
public class KeyList<E> extends GapList<E> {

    /**
     * The Handler interface is used to customize the behavior
     * of onAttach() and onDetach(). These methods cannot be
     * overridden as the concrete instances are created by builders.
     */
    static public interface Trigger<E> {
        /**
         * Handle element.
         *
         * @param elem element to handle
         */
        public void handle(E elem);
    }

    static public interface Predicate<E> {
    	public boolean allow(E elem);
    }

    /**
     * A comparator which can handle null values.
     */
    static class NullComparator<K> implements Comparator<K> {
        private Comparator<K> comparator;
        private boolean nullsFirst;

        public NullComparator(Comparator<K> comparator, boolean nullsFirst) {
            this.comparator = comparator;
            this.nullsFirst = nullsFirst;
        }

        @Override
        public int compare(K key1, K key2) {
            if (key1 != null && key2 != null) {
                return comparator.compare(key1, key2);
            }
            if (key1 == null) {
                if (key2 == null) {
                    return 0;
                } else {
                    return nullsFirst ? -1 : 1;
                }
            } else {
                assert(key2 == null);
                return nullsFirst ? 1 : -1;
            }
        }
    }

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
     * Identity mapper.
     */
    static class IdentMapper<E> implements Mapper<E, E> {

        public static final IdentMapper INSTANCE = new IdentMapper();

        private IdentMapper() {
        }

        @Override
        public E getKey(E v) {
            return v;
        }
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
    	KeyList<E> keyList;
    	// -- null
        boolean allowNullElem;
        boolean sorted;
        Predicate<E> constraint;
        // -- keys
        KeyMapBuilder keyMapBuilder;
    	GapList<KeyMap<E,Object>> keyMaps = GapList.create();
        // -- content
        int capacity = -1;
        Collection<? extends E> collection;
        E[] array;
        // -- handlers
        Trigger<E> insertTrigger;
        Trigger<E> deleteTrigger;

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
	                if (keyMapBuilder.sort && keyMap.mapper == IdentMapper.INSTANCE) {
	                	// Sorted set: we do not need a separate list for storing
	                	// keys and elements. We have to handle this case specially later.
	                	keyMap.sortedKeys = (GapList<Object>) keyList;
	            	} else {
	            		if (keyMapBuilder.type == null) {
		            		keyMap.sortedKeys = new GapList<Object>();
	            		} else {
	            			keyMap.sortedKeys = (GapList<Object>) createWrapperList(keyMapBuilder.type);
	            		}
	            	}
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
         * @param keyList keyList to initialize
         */
        void build(KeyList<E> keyList) {
        	endKeyMapBuilder();

       		keyList.allowNullElem = allowNullElem;
       		keyList.sorted = sorted;
       		keyList.constraint = constraint;
            keyList.insertTrigger = insertTrigger;
            keyList.deleteTrigger = deleteTrigger;
            keyList.keyMaps = keyMaps.toArray(new KeyMap[keyMaps.size()]);

            if (capacity == -1) {
                if (collection != null) {
                	capacity = collection.size();
                } else if (array != null) {
                	capacity = array.length;
                } else {
                	capacity = 10;
                }
            }
            keyList.init(new Object[capacity], 0);
            if (collection != null) {
            	keyList.addAll(collection);
            } else if (array != null) {
            	keyList.addAll(array.clone());
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
        Builder(KeyList<E> keyList) {
        	this.keyList = keyList;
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

        public Builder<E> withCapacity(int capacity) {
        	endKeyMapBuilder();
            this.capacity = capacity;
            return this;
        }

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

        public Builder<E> withSetKey() {
        	return withKey(new IdentMapper());
        }

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
        public KeyList<E> build() {
        	if (keyList == null) {
               	keyList = new KeyList<E>(false);
        	}
        	build(keyList);
        	return (KeyList<E>) keyList;
        }
    }

    /** True to allow null elements. A null element will always generate a null key. */
    boolean allowNullElem;
    /** True if list is sorted by first key */
    boolean sorted;
    /** Evaluation of the predicate must be successful for every element added to the list */
    Predicate<E> constraint;
    /** Handler method which is called if an element is attached to the list */
    Trigger<E> insertTrigger;
    /** Handler method which is called if an element is detached from the list */
    Trigger<E> deleteTrigger;

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
    }
    /**
     * There can be 0, 1, or several keys.
     * If there are no keys, all key methods will fail. This can be used, if a constraint list is needed.
     * If there are keys, only the first key can be sorted.
     */
    KeyMap<E, Object>[] keyMaps;

    /** If true the invariants the GapList are checked for debugging */
    private static final boolean DEBUG_CHECK = true;

    /**
     * Private method to check invariant of GapList.
     * It is only used for debugging.
     */
    private void debugCheck() {
    	for (KeyMap<E,?> keyMap: keyMaps) {
    		debugCheck(keyMap);
    	}
    }

    private void debugCheck(KeyMap keyMap) {
    	if (keyMap.unsortedKeys != null) {
    		assert(keyMap.unsortedKeys.size() <= size());  // keys() can contain lists
    		int count = 0;
    		for (Object obj: keyMap.unsortedKeys.values()) {
    			if (obj instanceof GapList) {
    				count += ((GapList) obj).size();
    			} else {
    				count++;
    			}
    		}
    		assert(count == size());
    	} else if (keyMap.sortedKeys != null) {
    		assert(keyMap.sortedKeys.size() == size());
    		GapList<?> copy = keyMap.sortedKeys.copy();
    		copy.sort(keyMap.comparator);
    		assert(copy.equals(keyMap.sortedKeys));
    	} else {
    		assert(false);
    	}
    }

    /**
     * Internal initialization.
     *
     * @param ignore ignored parameter for unique method signature
     */
    KeyList(boolean ignore) {
    	super(false, null);
    }

    /**
     * Internal initialization for copy() or crop() operation,
     * i.e. no element storage is allocated and initialized.
     *
     * @param that source object
     */
	KeyList(KeyList<E> that) {
	    super(false, that);

	    allowNullElem = that.allowNullElem;
	    sorted = that.sorted;
	    constraint = that.constraint;
	    insertTrigger = that.insertTrigger;
	    deleteTrigger = that.deleteTrigger;
	    keyMaps = that.keyMaps;
	}

	/**
	 * Initialize object for crop() operation.
	 *
	 * @param that source object
	 */
	@SuppressWarnings("unchecked")
    void initCrop(KeyList<E> that) {
	    // GapList
	    init(new Object[10], 0);

	    // KeyList
	    keyMaps = that.keyMaps.clone();
	    for (KeyMap<E,?> keyMap: keyMaps) {
	    	init(keyMap);
	    }
	}

    /**
     * Initialize object for copy() operation.
     *
     * @param that source object
     */
    void initCopy(KeyList<E> that) {
        // GapList
        init(that.toArray(), that.size());

        // KeyList
	    keyMaps = that.keyMaps.clone();
	    for (KeyMap<E,Object> keyMap: keyMaps) {
	    	clone(keyMap);
	    }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        KeyList<E> clone = (KeyList<E>) super.clone();

        for (KeyMap<E,Object> keyMap: keyMaps) {
        	clone(keyMap);
        }
        return clone;
    }

    private <K> void init(KeyMap<E,K> keyMap) {
        if (keyMap.unsortedKeys != null) {
        	keyMap.unsortedKeys = new HashMap<K, Object>();
        } else {
        	// Note that the check (keyMap.sortedKeys != this) does not work here
        	// as cloned KeyMap points to the old this pointer
        	if (keyMap.sortedKeys.getClass() == GapList.class) {
        		keyMap.sortedKeys = new GapList<K>();
        	} else {
	        	keyMap.sortedKeys = (GapList<K>) this;
        	}
        }
    }

    private <K> void clone(KeyMap<E,K> keyMap) {
        if (keyMap.unsortedKeys != null) {
        	keyMap.unsortedKeys = new HashMap<K, Object>(keyMap.unsortedKeys);
        } else {
        	// Note that the check (keyMap.sortedKeys != this) does not work here
        	// as cloned KeyMap points to the old this pointer
        	if (keyMap.sortedKeys.getClass() == GapList.class) {
	        	keyMap.sortedKeys = keyMap.sortedKeys.copy();
	        } else {
	        	keyMap.sortedKeys = (GapList<K>) this;
	        }
        }
    }

    @Override
    public void clear() {
    	for (KeyMap<E,Object> keyMap: keyMaps) {
    		clear(keyMap);
    	}
        super.clear();
    }

    private void clear(KeyMap<E,?> keyMap) {
        if (keyMap.unsortedKeys != null) {
        	keyMap.unsortedKeys.clear();
        } else if (keyMap.sortedKeys != this) {
        	keyMap.sortedKeys.clear();
        }
    }

    /**
     * {@inheritDoc}
     * <p><i>
     * Note that the behavior of the operation regarding duplicate values is controlled by the duplicate mode.
     * </i></p>
     * @see DuplicateMode
     */
    // This method is only overwritten to change the Javadoc comment.
    @Override
    public boolean add(E elem) {
        return super.add(elem);
    }

    /**
     * {@inheritDoc}
     * <p><i>
     * Note that the behavior of the operation regarding duplicate values is controlled by the duplicate mode.
     * </i></p>
     * @see DuplicateMode
     */
    // This method is only overwritten to change the Javadoc comment.
    @Override
    public void add(int index, E elem)  {
        super.add(index, elem);
    }

    /**
     * {@inheritDoc}
     * <p><i>
     * Note that the behavior of the operation regarding duplicate values is controlled by the duplicate mode.
     * </i></p>
     * @see DuplicateMode
     */
    // This method is only overwritten to change the Javadoc comment.
    @Override
    public E set(int index, E elem) {
        return super.set(index, elem);
    }

    /**
     * Puts an element into the list.
     * The behavior is similar to Map.put():
     * If no element with this key exists, the element is added.
     * If there is already such an element, it is replaced.
     * This method does not support duplicates.
     *
     * @param elem  element to add
     * @return      element which has been replaced or null if the element has been added
     */
    // TODO behavior with several keys?
//    public E put(E elem) {
//        K key = getKey(elem);
//        int index = indexOfKey(key);
//        if (index != -1) {
//            return doSet(index, elem);
//        } else {
//            doAdd(-1, elem);
//            return null;
//        }
//    }

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
	        if (!allowNullElem) {
	            throw new IllegalArgumentException("Null element not allowed");
	        }
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
    protected boolean doAdd(int index, E elem) {
    	checkElemAllowed(elem);

    	IllegalArgumentException error = null;
    	int addIndex = 0;
		int i = 0;
    	if (keyMaps != null) {
    		try {
		    	for (i=0; i<keyMaps.length; i++) {
		    		int idx = doAddKey(keyMaps[i], index, elem);
		    		if (idx == Integer.MIN_VALUE) {
		    			addIndex = idx;
		    			break;
		    		}
		    		if (i == 0) {
		    			addIndex = idx;
		    		}
		    	}
    		}
    		catch (IllegalArgumentException e) {
    			addIndex = Integer.MIN_VALUE;
    			error = e;
    		}
    	}
    	if (addIndex == Integer.MIN_VALUE) {
    		for (i--; i>=0; i--) {
    			Object key = keyMaps[i].mapper.getKey(elem);
    			removeByKey(keyMaps[i], key, false);
    		}
    		if (error != null) {
    			throw error;
    		} else {
    			return false;
    		}
    	}
        super.doAdd(addIndex, elem);
        onAttach(elem);
        if (DEBUG_CHECK) debugCheck();
        return true;
    }

    /**
     * Add element.
     *
     * @param index		index of element to add (may be -1)
     * @param elem		element to add
     * @return			index where element should be added (-1 is valid), otherwise Integer.MIN_VALUE
     */
    private <K> int doAddKey(KeyMap<E,K> keyMap, int index, E elem) {
        K key = getKey(keyMap, elem);
        if (keyMap.unsortedKeys != null) {
            // Keys not sorted
            if (!keyMap.unsortedKeys.containsKey(key)) {
                // New key
                doAddUnsorted(keyMap, elem, key);
                return index;

            } else {
                // Key exists already
                if (keyMap.duplicateMode == DuplicateMode.ALLOW ||
                		(key == null && keyMap.allowNullKeys == NullMode.MULTIPLE)) {
                	// Add duplicate
                    doAddUnsorted(keyMap, elem, key);
                    return index;

                } else {
                	// Handle duplicate without adding
                    if (index != -1) {
                        throw new IllegalArgumentException("Duplicate key not allowed: " + key);
                    }
                    if (keyMap.duplicateMode == DuplicateMode.REPLACE) {
                        Object oldElem = keyMap.unsortedKeys.put(key, elem);
                        index = indexOf(oldElem);
                        super.doSet(index, elem);
                        return Integer.MIN_VALUE;	// TODO what should be returned? false -> attach is not called

                    } else if (keyMap.duplicateMode == DuplicateMode.IGNORE) {
                        return Integer.MIN_VALUE;
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
                if (index == -1) {
                	index = addIndex;
                } else if (index != addIndex) {
                	throw new IllegalArgumentException("Invalid index for sorted order");
                }
                if (keyMap.sortedKeys != this) {
                	keyMap.sortedKeys.doAdd(index, key);
                }
                return addIndex;

            } else {
                // Key exists already
            	if (index == -1) {
                    if (keyMap.duplicateMode == DuplicateMode.ALLOW ||
                    		(key == null && keyMap.allowNullKeys == NullMode.MULTIPLE)) {
                    	index = addIndex;
                    } else if (keyMap.duplicateMode == DuplicateMode.REPLACE) {
                        int getIndex = SortedLists.binarySearchGet(keyMap.sortedKeys, key, keyMap.comparator);
                        if (keyMap.sortedKeys != this) {
                        	keyMap.sortedKeys.set(getIndex, key);
                        }
                        super.doSet(getIndex, elem);
                        return Integer.MIN_VALUE;	// TODO what should be returned? false -> attach is not called

                    } else if (keyMap.duplicateMode == DuplicateMode.IGNORE) {
                        return Integer.MIN_VALUE;
                    } else if (keyMap.duplicateMode == DuplicateMode.ERROR) {
                        throw new IllegalArgumentException("Duplicate key not allowed: " + key);
                    } else {
                        throw new AssertionError();
                    }
            	}
                if (addIndex != index) {
                	if (index == addIndex-1) {
                		// ok
                	} else if (index > addIndex) {
                    	throw new IllegalArgumentException("Invalid index for sorted order");
                	} else {
                		int lowerIndex = addIndex-1;
                		while (lowerIndex >= 0) {
                			if (keyMap.comparator.compare(key, keyMap.sortedKeys.get(lowerIndex)) != 0) {
                				break;
                			}
                			lowerIndex--;
                		}
                		if (index < lowerIndex+1) {
                        	throw new IllegalArgumentException("Invalid index for sorted order");
                		}
                	}
                }
                if (keyMap.sortedKeys != this) {
                	keyMap.sortedKeys.doAdd(index, key);
                }
                return index;
            }
        }
    }

    /**
     * The method onAttach() is called before a new element is added to or set in the list.
     * For a set() operation, onDetach() is first called for the old element and then
     * onAttach() is called for the new element.
     *
     * @param elem element which is stored in list
     */
    protected void onAttach(E elem) {
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
    protected void onDetach(E elem) {
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
    protected E doSet(int index, E elem) {
    	checkElemAllowed(elem);

        E oldElem = super.doSet(index, elem);
        if (keyMaps != null) {
        	int i = 0;
        	try {
        		for (i=0; i<keyMaps.length; i++) {
        			doSet(keyMaps[i], index, elem, oldElem);
        		}
        	}
        	catch (IllegalArgumentException e) {
        		// Rollback changes
        		for (i--; i>=0; i--) {
        			doSet(keyMaps[i], index, oldElem, elem);
        		}
        		super.doSet(index, oldElem);
        		throw e;
        	}
        }
        if (DEBUG_CHECK) debugCheck();
        onDetach(oldElem);
        onAttach(elem);
        return oldElem;
    }

    private <K> E doSet(KeyMap<E,K> keyMap, int index, E elem, E oldElem) {
        K key = getKey(keyMap, elem);
        if (keyMap.unsortedKeys != null) {
            // Keys not sorted
            K oldKey = keyMap.mapper.getKey(oldElem);
            if (!keyMap.unsortedKeys.containsKey(key)) {
                // New key
                keyMap.unsortedKeys.remove(oldKey);
                keyMap.unsortedKeys.put(key, elem);
                return oldElem;

            } else {
                // Key exists already
                if (GapList.equalsElem(oldKey, key)) {
                    // Same index
                	keyMap.unsortedKeys.put(key, elem);
                    return oldElem;

                } else {
                    // Different index
                    if (keyMap.duplicateMode == DuplicateMode.ALLOW ||
                    		(key == null && keyMap.allowNullKeys == NullMode.MULTIPLE)) {
                        doAddUnsorted(keyMap, elem, key);
                        keyMap.unsortedKeys.remove(oldKey);
                        return oldElem;

                    } else {
                        throw new IllegalArgumentException("Duplicate key not allowed: " + key);
                    }
                }
            }
        } else {
            // Sorted keys
            int setIndex = SortedLists.binarySearchAdd(keyMap.sortedKeys, key, keyMap.comparator);
            if (setIndex < 0) {
                // New key
                setIndex = -setIndex - 1;
                if (setIndex != index) {
                	throw new IllegalArgumentException("Invalid index for sorted order");
                }
                if (keyMap.sortedKeys != this) {
                	keyMap.sortedKeys.set(setIndex, key);
                }
                return oldElem;

            } else {
                // Key exists already
            	setIndex--;
                if (setIndex != index) {
                    if (keyMap.duplicateMode == DuplicateMode.ALLOW ||
                    		(key == null && keyMap.allowNullKeys == NullMode.MULTIPLE)) {
                    	if (index == setIndex+1) {
                    		// ok
                    	} else if (index > setIndex) {
                        	throw new IllegalArgumentException("Invalid index for sorted order");
                    	} else {
                    		int lowerIndex = setIndex-1;
                    		while (lowerIndex >= 0) {
                    			if (keyMap.comparator.compare(key, keyMap.sortedKeys.get(lowerIndex)) != 0) {
                    				break;
                    			}
                    			lowerIndex--;
                    		}
                    		if (index < lowerIndex) {
                            	throw new IllegalArgumentException("Invalid index for sorted order");
                    		}
                    	}
                    } else {
                    	throw new IllegalArgumentException("Invalid index for sorted order");
                    }
                }
                if (keyMap.sortedKeys != this) {
                	keyMap.sortedKeys.set(index, key);
                }
                return oldElem;
            }
        }
    }

    @Override
    protected E doRemove(int index) {
        E elem = super.doRemove(index);
        onDetach(elem);
        for (int i=0; i<keyMaps.length; i++) {
        	if (i > 0 || keyMaps[i].sortedKeys != this) {
        		Object key = keyMaps[i].mapper.getKey(elem);
        		removeByKey(keyMaps[i], key, false);
        	}
        }
        return elem;
    }

    @Override
    public int indexOf(Object elem) {
        if (keyMaps != null) {
            if (keyMaps[0].sortedKeys == this) {
                // sorted set
                assert(keyMaps[0].mapper == IdentMapper.INSTANCE);
                return indexOfKey(keyMaps[0], elem);
            }
        }
        return super.indexOf(elem);
    }
    /**
     * Checks whether the specified key exists in this list.
     *
     * @param key key to look for
     * @return  true if the key exists, otherwise false
     */
    public <K> boolean containsKey(int keyIndex, K key) {
        return indexOfKey(keyIndex, key) != -1;
    }

    /**
     * Find given key and return its index.
     *
     * @param key   key to find
     * @return      index of key or -1 if not found
     */
    public <K> int indexOfKey(int keyIndex, K key) {
    	return indexOfKey(getKeyMap(keyIndex), key);
    }

    private <K> int indexOfKey(KeyMap<E,K> keyMap, K key) {
        if (key == null) {
            if (keyMap.allowNullKeys == NullMode.NONE) {
                return -1;
            }
        }

        if (keyMap.unsortedKeys != null) {
            // not sorted
            Object elem = keyMap.unsortedKeys.get(key);
            if (elem == null) {
                return -1;
            }

            boolean equal = !(elem instanceof GapList);
            int i;
            for (i=0; i<size(); i++) {
                if (equal) {
                    if (doGet(i) == elem) {
                        break;
                    }
                } else {
                    K oldKey = keyMap.mapper.getKey(doGet(i));
                    if (GapList.equalsElem(oldKey, key)) {
                        break;
                    }
                }
            }
            if (i == size()) {
                throw new IllegalArgumentException("Key missmatch: " + key);
            }
            return i;

        } else {
            // sorted
            int index = SortedLists.binarySearchGet(keyMap.sortedKeys, key, keyMap.comparator);
            if (index >= 0) {
                return index;
            } else {
                return -1;
            }
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
            GapList<K> list = new GapList<K>();
            Set<K> ks = new HashSet<K>(keyMap.unsortedKeys.keySet());
            for (int i=0; i<size(); i++) {
                K k = getKey(keyMap, doGet(i));
                if (ks.remove(k)) {
                    list.add(k);
                    if (ks.isEmpty()) {
                        break;
                    }
                }
            }
            if (list.size() != keyMap.unsortedKeys.size()) {
            	throw new IllegalArgumentException("Invalid data (key has changed without invalidate)");
            }
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
                return doGet(index);
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
    	return getAllByKey(getKeyMap(keyIndex), key);
    }

    private <K> GapList<E> getAllByKey(KeyMap<E,K> keyMap, K key) {
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
                    list.add(doGet(index));
                    index++;
                    if (index == size()) {
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
                    if (index == size()) {
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
    	int index = indexOf(elem);
    	if (index == -1) {
    		throw new IllegalArgumentException("Element not found: " + elem);
    	}
    	invalidate(index);
    }

    public void invalidate(int index) {
    	E elem = doGet(index);
        for (int i=0; i<keyMaps.length; i++) {
    		Object key = invalidate(keyMaps[i], elem);
    		if (key != null) {
    			if (i == 0 && keyMaps[i].sortedKeys != null && keyMaps[i].sortedKeys != this) {
    				// First key is sorted
    				int idx = super.indexOf(elem);
    				super.doRemove(idx);
    				idx = doAddKey(keyMaps[i], -1, elem);
    				super.doAdd(idx, elem);
    			} else {
    				// Not first or not sorted key
    				doAddKey(keyMaps[i], -1, elem);
    			}
    		}
        }
        if (DEBUG_CHECK) debugCheck();
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
    		    if (equalsElem(elem, entry.getValue())) {
    		    	if (equalsElem(key, entry.getKey())) {
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
    		for (int i=0; i<size(); i++) {
    			if (equalsElem(elem, doGet(i))) {
    				if (equalsElem(key, keyMap.sortedKeys.get(i))) {
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
     * The keys of the elements must not change as long as the elements are stored in the list.
     * If a key changes nevertheless, you must call invalidate() to update the list's internal information.
     *
     * @param elem  element whose key has changed
     */
    @SuppressWarnings("unchecked")
//    public void invalidate(E elem) {
//        K newKey = getKey(elem);
//        if (unsortedKeys != null) {
//            // Keys not sorted
//            K oldKey = null;
//            for (Map.Entry<K, Object> entry: unsortedKeys.entrySet()) {
//                Object value = entry.getValue();
//                if (value instanceof GapList) {
//                    GapList<E> list = (GapList<E>) value;
//                    int index = list.indexOf(elem);
//                    if (index != -1) {
//                        oldKey = entry.getKey();
//                        if (!GapList.equalsElem(oldKey, newKey)) {
//                            if (containsKey(newKey)) {
//                                if (duplicateMode != DuplicateMode.ALLOW) {
//                                    throw new IllegalArgumentException("Duplicate key not allowed: " + newKey);
//                                }
//                            }
//                            doRemove(value, oldKey);
//                            doAdd(elem, newKey);
//                            if (DEBUG_CHECK) debugCheck();
//                            return;
//                        }
//                        break;
//                    }
//                } else {
//                    if (value == elem) {
//                        oldKey = entry.getKey();
//                        if (!GapList.equalsElem(oldKey, newKey)) {
//                            if (containsKey(newKey)) {
//                                if (duplicateMode != DuplicateMode.ALLOW) {
//                                    throw new IllegalArgumentException("Duplicate key not allowed: " + newKey);
//                                }
//                            }
//                            doRemove(value, oldKey);
//                            doAdd(elem, newKey);
//                            if (DEBUG_CHECK) debugCheck();
//                            return;
//                        }
//                        break;
//                    }
//                }
//            }
//            throw new IllegalArgumentException("Key missmatch: " + newKey);
//
//        } else {
//            // Sorted keys
//            int i;
//            int size = size();
//            for (i=0; i<size; i++) {
//                if (doGet(i) == elem) {
//                    break;
//                }
//            }
//            if (i == size) {
//                throw new IllegalArgumentException("Key missmatch: " + newKey);
//            }
//            K oldKey = sortedKeys.get(i);
//            if (!GapList.equalsElem(oldKey, newKey)) {
//                if (containsKey(newKey)) {
//                    if (duplicateMode != DuplicateMode.ALLOW) {
//                        throw new IllegalArgumentException("Duplicate key not allowed: " + newKey);
//                    }
//                }
//                doRemove(i, elem, oldKey);
//                doAdd(-1, elem, false);
//            }
//        }
//    }

    /**
     * Removes element by key.
     * If there are duplicates, only one element is removed.
     *
     * @param key   key of element to remove
     * @return      removed element or null if no element has been removed
     */
    public E removeByKey(int keyIndex, Object key) {
    	checkKeyMap(keyIndex);
    	E removed = removeByKey(keyMaps[keyIndex], key, true);
    	if (removed != null) {
    		for (int i=0; i<keyMaps.length; i++) {
    			if (i != keyIndex) {
    				Object k = keyMaps[i].mapper.getKey(removed);
    				removeByKey(keyMaps[i], k, false);
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
    private <K> E removeByKey(KeyMap<E,K> keyMap, K key, boolean removeElems) {
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

            // Faster than remove(elem) (equals not needed)
            if (!removeElems) {
            	elem = null;
            } else {
	            int i;
	            for (i=0; i<size(); i++) {
	                if (elem != null) {
	                    if (doGet(i) == elem) {
	                        break;
	                    }
	                } else {
	                    K oldKey = keyMap.mapper.getKey(doGet(i));
	                    if (GapList.equalsElem(oldKey, key)) {
	                        break;
	                    }
	                }
	            }
	            if (i == size()) {
	                throw new IllegalArgumentException("Key missmatch: " + key);
	            }
	            E elem2 = super.doRemove(i);
	            assert(elem2 == elem);
            }
            return elem;

        } else {
            // sorted
        	assert(removeElems);

            int index = SortedLists.binarySearchGet(keyMap.sortedKeys, key, keyMap.comparator);
            if (index >= 0) {
                if (keyMap.sortedKeys != this) {
                	keyMap.sortedKeys.remove(index);
                }
                E elem = super.doRemove(index);
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
     * @param key   key of element to remove
     * @return      true if elements have been removed, false otherwise
     */
    @SuppressWarnings("unchecked")
    public GapList<E> removeAllByKey(int keyIndex, Object key) {
    	checkKeyMap(keyIndex);
    	GapList<E> removeds = removeAllByKey(keyMaps[keyIndex], key, true);
    	for (E removed: removeds) {
    		for (int i=0; i<keyMaps.length; i++) {
    			if (i != keyIndex) {
    				Object k = keyMaps[i].mapper.getKey(removed);
    				removeAllByKey(keyMaps[i], k, false);
    			}
    		}
    	}
        if (DEBUG_CHECK) debugCheck();
        return removeds;
    }

    private <K> GapList<E> removeAllByKey(KeyMap<E,K> keyMap, K key, boolean removeElems) {
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
                if (removeElems) {
                	removed = GapList.create((E) obj);
                }
            }
            if (removeElems) {
	            // We have to iterate through the list until we have found
	            // all elements with specified key
	            for (int i=0; i<size(); i++) {
	                K oldKey = keyMap.mapper.getKey(doGet(i));
	                if (GapList.equalsElem(oldKey, key)) {
	                    super.doRemove(i);
	                    num--;
	                    if (num == 0) {
	                        break;
	                    }
	                    i--;
	                }
	            }
            }
            return removed;

        } else {
            // sorted
        	assert(removeElems);

        	int index = SortedLists.binarySearchGet(keyMap.sortedKeys, key, keyMap.comparator);
            if (index < 0) {
                return GapList.EMPTY();
            }
            int start = index;
            while (true) {
                index++;
                if (index == size()) {
                    break;
                }
                if (!GapList.equalsElem(keyMap.sortedKeys.get(index), key)) {
                    break;
                }
            }
            GapList<E> removed = super.get(start, index-start);
            keyMap.sortedKeys.remove(start, index-start);
            if (keyMap.sortedKeys != this) {
                while (index > start) {
                    index--;
                    super.doRemove(index);
                }
            }
            return removed;
        }
    }

    @Override
    public void sort(int index, int len, Comparator<? super E> comparator) {
    	checkRange(index, len);

    	if (keyMaps != null && keyMaps[0].comparator != null) {
    		if (keyMaps[0].comparator == comparator) {
    			return;
    		}
    		throw new IllegalArgumentException("Differen comparator specified for sorted list");
    	}

    	super.sort(index, len, comparator);
    	// FIXME adapt keys
    }

}
