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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.magicwerk.brownies.collections.SetList.Builder;
import org.magicwerk.brownies.collections.SetList.IdentMapper;


/**
 * A KeyList add key handling features to GapList.
 * It is the abstract base class for both SetList and MapList.
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @see GapList
 * @param <E> type of elements stored in the list
 * @param <K> type of keys stored used for accessing the list
 */
public abstract class KeyList<E, K> extends GapList<E> {

    /**
     * The Handler interface is used to customize the behavior
     * of onAttach() and onDetach(). These methods cannot be
     * overridden as the concrete instances are created by builders.
     */
    static public interface Handler<E> {
        /**
         * Handle element.
         *
         * @param elem element to handle
         */
        public void handle(E elem);
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

    public static <T> Comparator<T> getNaturalComparator() {
        return new Comparator<T>() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public int compare(Object o1, Object o2) {
                return ((Comparable) o1).compareTo(o2);
            }
        };
    }

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
     * Builder to construct SetList instances.
     */
    public static class Builder<E, K> {
        /** KeyList which is built up */
    	KeyList<E, K> keyList;
    	/** Mapper (IdentMapper for SetList) */
        Mapper<E, K> mapper;
        // -- null
        boolean allowNullElem;
        NullMode nullMode = NullMode.NONE;
        // -- duplicates
        DuplicateMode duplicateMode = DuplicateMode.IGNORE;
        // -- sort
        /** True to sort using natural comparator */
        boolean sort;
        /** Comparator to use for sorting */
        Comparator<? super K> comparator;
        /** The specified comparator can handle null values */
        boolean comparatorSortsNull;
        /** Determine whether null values appear first or last */
        boolean sortNullsFirst;
        // -- content
        int capacity = 10;
        Collection<? extends E> collection;
        E[] array;
        // -- handlers
        Handler<E> attachHandler;
        Handler<E> detachHandler;


        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Internal constructor
         *
         * @param setList   setList to customize
         */
        Builder(KeyList<E, K> keyList) {
            this.keyList = keyList;
        }

        /**
         * Determines whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param nullable  true to allow null elements, false to disallow
         * @return          this (for use in fluent interfaces)
         */
        public Builder<E, K> withNull(boolean nullable) {
            nullMode = nullable ? NullMode.NORMAL : NullMode.NONE;
            return this;
        }

        public Builder<E, K> withNull(NullMode nullMode) {
            this.nullMode = nullMode;
            return this;
        }

        /**
         * Determines whether duplicates are allowed or not.
         *
         * @param duplicates    true to allow duplicates, false to disallow
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E, K> withDuplicates(DuplicateMode duplicateMode) {
            this.duplicateMode = duplicateMode;
            return this;
        }

        public Builder<E, K> withAttachHandler(Handler<E> handler) {
            this.attachHandler = handler;
            return this;
        }

        public Builder<E, K> withDetachHandler(Handler<E> handler) {
            this.detachHandler = handler;
            return this;
        }

        public Builder<E, K> withCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder<E, K> withElements(Collection<? extends E> elements) {
            this.collection = elements;
            return this;
        }

        public Builder<E, K> withElements(E... elements) {
            this.array = elements;
            return this;
        }

        /**
         * Determines whether list should be sorted or not.
         *
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E, K> withSort() {
            return withSort(true);
        }

        /**
         * Determines that list should be sorted.
         *
         * @param sort    true to sort list, otherwise false
         * @return        this (for use in fluent interfaces)
         */
        public Builder<E, K> withSort(boolean sort) {
            this.sort = sort;
            return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E, K> withComparator(Comparator<? super K> comparator) {
            this.comparator = comparator;
            return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param comparatorSortsNull   true if comparator sorts null, false if not
         * @return                      this (for use in fluent interfaces)
         */
        public Builder<E, K> withComparator(Comparator<? super K> comparator, boolean comparatorSortsNull) {
            this.comparator = comparator;
            this.comparatorSortsNull = comparatorSortsNull;
            return this;
        }

        /**
         * Determines that nulls are sorted first.
         *
         * @return  this (for use in fluent interfaces)
         */
        public Builder<E, K> withNullsFirst() {
            return withNullsFirst(true);
        }

        /**
         * Determines whether nulls are sorted first or last.
         *
         * @param nullsFirst    true to sort nulls first, false to sort nulls last
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E, K> withNullsFirst(boolean nullsFirst) {
            this.sortNullsFirst = nullsFirst;
            return this;
        }

        /**
         * Build SetList with specified options.
         *
         * @return created SetList
         */
        KeyList<E, K> doBuild() {
        	if (this instanceof SetList.Builder) {
        		if (keyList == null) {
        			keyList = (KeyList<E, K>) new SetList<E>(false);
        		}
        		mapper = new IdentMapper();
        	} else if (this instanceof MapList.Builder) {
        		if (keyList == null) {
        			keyList = new MapList<E, K>(false);
        		}
        		assert(mapper != null);
        	}

        	keyList.mapper = mapper;
            keyList.duplicateMode = duplicateMode;
            keyList.attachHandler = attachHandler;
            keyList.detachHandler = detachHandler;

            if (comparator != null) {
            	keyList.comparator = (Comparator<K>) comparator;
            } else if (sort) {
                keyList.comparator = getNaturalComparator();
            }

            if (keyList.comparator != null) {
                if (!comparatorSortsNull) {
                    keyList.comparator = new NullComparator(keyList.comparator, sortNullsFirst);
                }
                if (this instanceof SetList.Builder) {
                	// Sorted set: we do not need a separate list for storing
                	// keys and elements. We have to handle this case specially later.
                	keyList.sortedKeys = (GapList<K>) keyList;
            	} else if (this instanceof MapList.Builder) {
            		keyList.sortedKeys = new GapList<K>();
            	} else {
            		assert(false);
            	}
            } else {
                // Set is not sorted: maintain a separate HashMap for fast
                // answering contains() calls
                keyList.keys = new HashMap<K, Object>();
            }

       		keyList.allowNullElem = (nullMode == NullMode.NORMAL || nullMode == NullMode.MULTIPLE);
       		keyList.allowNullKeys = nullMode;

            keyList.init(capacity);
            if (collection != null) {
            	keyList.addAll(collection);
            } else if (array != null) {
            	keyList.addAll(array.clone());
            }
            return keyList;
        }
    }

    /** A mapper to extract keys out of element for a MapList. For a SetList, this is always an IdentMapper. */
    Mapper<E, K> mapper;
    /** True to allow null elements. A null element will always generate a null key. */
    boolean allowNullElem;
    /** True to allow null keys */
    NullMode allowNullKeys;
    /** True to allow duplicate values. This also allows duplicate null values, but they are not distinct. */
    DuplicateMode duplicateMode = DuplicateMode.IGNORE;
    /** Comparator to use for sorting (if null, elements are not sorted) */
    Comparator<K> comparator;
    /** Determine whether null values are listed before or after values (only if sorted) */
    boolean nullsFirst;
    /** Key storage if not sorted (note that we cannot use TreeMap as K may not be comparable) */
    HashMap<K, Object> keys;
    /** Key storage if sorted */
    GapList<K> sortedKeys;
    /** Handler method which is called if an element is attached to the list */
    Handler<E> attachHandler;
    /** Handler method which is called if an element is detached from the list */
    Handler<E> detachHandler;


    /** If true the invariants the GapList are checked for debugging */
    private static final boolean DEBUG_CHECK = true;

    /**
     * Private method to check invariant of GapList.
     * It is only used for debugging.
     */
    private void debugCheck() {
    	if (keys != null) {
    		assert(keys.size() <= size());  // keys() can contain lists
    		int count = 0;
    		for (Object obj: keys.values()) {
    			if (obj instanceof GapList) {
    				count += ((GapList) obj).size();
    			} else {
    				count++;
    			}
    		}
    		assert(count == size());
    	} else if (sortedKeys != null) {
    		assert(sortedKeys.size() == size());
    		GapList<K> copy = sortedKeys.copy();
    		copy.sort(comparator);
    		assert(copy.equals(sortedKeys));
    	} else {
    		assert(false);
    	}
    }

    protected Mapper<E, K> getMapper() {
        return mapper;
    }

    public Comparator<K> getComparator() {
        return comparator;
    }

    /**
     * Internal initialization.
     */
    KeyList() {
    	super(false, null);
    }


    /**
     * Internal initialization for copy() or crop() operation,
     * i.e. no element storage is allocated and initialized.
     *
     * @param that source object
     */
	KeyList(KeyList<E, K> that) {
	    super(false, that);

	    mapper = that.mapper;
	    allowNullElem = that.allowNullElem;
	    allowNullKeys = that.allowNullKeys;
	    duplicateMode = that.duplicateMode;
	    comparator = that.comparator;
	    nullsFirst = that.nullsFirst;
	}

	/**
	 * Initialize object for crop() operation.
	 *
	 * @param that source object
	 */
	@SuppressWarnings("unchecked")
    void initCrop(KeyList<E, K> that) {
	    // GapList
	    init(10);

	    // KeyList
	    if (that.keys != null) {
	        keys = new HashMap<K, Object>();
	    } else if (that.sortedKeys != that) {
	        sortedKeys = new GapList<K>();
	    } else {
	        assert(false);
	    }
	}

    /**
     * Initialize object for copy() operation.
     *
     * @param that source object
     */
    void initCopy(KeyList<E, K> that) {
        // GapList
        init(toArray(that));

        // KeyList
        if (that.keys != null) {
            keys = new HashMap<K, Object>(that.keys);
        } else if (that.sortedKeys != that) {
            sortedKeys = that.sortedKeys.copy();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        KeyList<E, K> clone = (KeyList<E, K>) super.clone();

        // KeyList
        if (keys != null) {
            clone.keys = new HashMap<K, Object>(keys);
        } else if (sortedKeys != this) {
            clone.sortedKeys = sortedKeys.copy();
        }
        return clone;
    }

    @Override
    public void clear() {
        if (keys != null) {
            keys.clear();
        } else if (sortedKeys != this) {
            sortedKeys.clear();
        }
        super.clear();
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
    public E put(E elem) {
        K key = getKey(elem);
        int index = indexOfKey(key);
        if (index != -1) {
            return doSet(index, elem);
        } else {
            doAdd(-1, elem);
            return null;
        }
    }

	/**
	 * Produce key out of specified element.
	 *
	 * @param elem element
	 * @return     key of specified element
	 * @throws IllegalArgumentException if a null key is produced and null keys are not allowed
	 */
	K getKey(E elem) {
	    K key;
	    if (elem == null) {
	        if (!allowNullElem) {
	            throw new IllegalArgumentException("Null element not allowed");
	        }
	        key = null;
	    } else {
	        key = mapper.getKey(elem);
	    }
        if (key == null && allowNullKeys == NullMode.NONE) {
            throw new IllegalArgumentException("Null key not allowed");
        }
        return key;
	}

	private void doAdd(E elem, K key) {
		Object obj = keys.get(key);
	    if (obj == null) {
	    	if (!keys.containsKey(key)) {
	    		keys.put(key, elem);
	        } else {
	            GapList<E> list = (GapList<E>) GapList.create(null, elem);
	            keys.put(key, list);
	    	}
	    } else if (obj instanceof GapList) {
            GapList<E> list = (GapList<E>) obj;
            list.add(elem);
        } else {
            GapList<E> list = (GapList<E>) GapList.create(obj, elem);
            keys.put(key, list);
        }
	}

    private E doRemove(Object obj, K key) {
        assert(obj != null);

        E elem = null;
        if (obj instanceof GapList) {
            GapList<E> list = (GapList<E>) obj;
            list.removeFirst();
            if (list.isEmpty()) {
                keys.remove(key);
            }
        } else {
            elem = (E) keys.remove(key);
        }
        return elem;
    }

    @Override
    protected boolean doAdd(int index, E elem) {
        return doAdd(index, elem, true);
    }

    /**
     * @param index
     * @param elem
     * @param attach	true to call the attach handler, false to skip this call
     * @return
     */
    private boolean doAdd(int index, E elem, boolean attach) {
        K key = getKey(elem);
        if (keys != null) {
            // Keys not sorted
            if (!keys.containsKey(key)) {
                // New key
                if (attach) {
                    onAttach(elem);
                }
                doAdd(elem, key);
                super.doAdd(index, elem);
                if (DEBUG_CHECK) debugCheck();
                return true;

            } else {
                // Key exists already
                if (duplicateMode == DuplicateMode.ALLOW ||
                		(key == null && allowNullKeys == NullMode.MULTIPLE)) {
                	// Add duplicate
                    if (attach) {
                        onAttach(elem);
                    }
                    doAdd(elem, key);
                    super.doAdd(index, elem);
                    if (DEBUG_CHECK) debugCheck();
                    return true;

                } else {
                	// Handle duplicate without adding
                    if (index != -1) {
                        throw new IllegalArgumentException("Duplicate key not allowed: " + key);
                    }
                    if (duplicateMode == DuplicateMode.REPLACE) {
                        if (attach) {
                            onAttach(elem);
                        }
                        Object oldElem = keys.put(key, elem);
                        index = indexOf(oldElem);
                        super.doSet(index, elem);
                        if (DEBUG_CHECK) debugCheck();
                        return true;

                    } else if (duplicateMode == DuplicateMode.IGNORE) {
                        return false;
                    } else if (duplicateMode == DuplicateMode.ERROR) {
                        throw new IllegalArgumentException("Duplicate key not allowed: " + key);
                    } else {
                        throw new AssertionError();
                    }
                }
            }

        } else {
            // Sorted keys
            int addIndex = SortedLists.binarySearchAdd(sortedKeys, key, comparator);
            if (addIndex < 0) {
                // New key
                if (attach) {
                    onAttach(elem);
                }
                addIndex = -addIndex - 1;
                if (index == -1) {
                	index = addIndex;
                } else if (index != addIndex) {
                	throw new IllegalArgumentException("Invalid index for sorted order");
                }
                if (sortedKeys != this) {
                    sortedKeys.doAdd(index, key);
                }
                super.doAdd(index, elem);
                if (DEBUG_CHECK) debugCheck();
                return true;

            } else {
                // Key exists already
            	if (index == -1) {
                    if (duplicateMode == DuplicateMode.ALLOW ||
                    		(key == null && allowNullKeys == NullMode.MULTIPLE)) {
                    	index = addIndex;
                    } else if (duplicateMode == DuplicateMode.REPLACE) {
                        if (attach) {
                            onAttach(elem);
                        }
                        int getIndex = SortedLists.binarySearchGet(sortedKeys, key, comparator);
                        if (sortedKeys != this) {
                            sortedKeys.set(getIndex, key);
                        }
                        super.doSet(getIndex, elem);
                        if (DEBUG_CHECK) debugCheck();
                        return true;

                    } else if (duplicateMode == DuplicateMode.IGNORE) {
                        return false;
                    } else if (duplicateMode == DuplicateMode.ERROR) {
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
                			if (comparator.compare(key, sortedKeys.get(lowerIndex)) != 0) {
                				break;
                			}
                			lowerIndex--;
                		}
                		if (index < lowerIndex+1) {
                        	throw new IllegalArgumentException("Invalid index for sorted order");
                		}
                	}
                }
                if (attach) {
                    onAttach(elem);
                }
                if (sortedKeys != this) {
                    sortedKeys.doAdd(index, key);
                }
                super.doAdd(index, elem);
                if (DEBUG_CHECK) debugCheck();
                return true;
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
        if (attachHandler != null) {
            attachHandler.handle(elem);
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
        if (detachHandler != null) {
            detachHandler.handle(elem);
        }
    }

    @Override
    protected E doSet(int index, E elem) {
        K key = getKey(elem);
        E oldElem = doGet(index);
        if (keys != null) {
            // Keys not sorted
            K oldKey = mapper.getKey(oldElem);
            if (!keys.containsKey(key)) {
                // New key
                onDetach(oldElem);
                onAttach(elem);
                keys.remove(oldKey);
                keys.put(key, elem);
                super.doSet(index, elem);
                if (DEBUG_CHECK) debugCheck();
                return oldElem;

            } else {
                // Key exists already
                if (GapList.equalsElem(oldKey, key)) {
                    // Same index
                    onDetach(oldElem);
                    onAttach(elem);
                    keys.put(key, elem);
                    super.doSet(index, elem);
                    if (DEBUG_CHECK) debugCheck();
                    return oldElem;

                } else {
                    // Different index
                    if (duplicateMode == DuplicateMode.ALLOW ||
                    		(key == null && allowNullKeys == NullMode.MULTIPLE)) {
                        onDetach(oldElem);
                        onAttach(elem);
                        doAdd(elem, key);
                        keys.remove(oldKey);
                        super.doSet(index, elem);
                        if (DEBUG_CHECK) debugCheck();
                        return oldElem;

                    } else {
                        throw new IllegalArgumentException("Duplicate key not allowed: " + key);
                    }
                }
            }
        } else {
            // Sorted keys
            int setIndex = SortedLists.binarySearchAdd(sortedKeys, key, comparator);
            if (setIndex < 0) {
                // New key
                setIndex = -setIndex - 1;
                if (setIndex != index) {
                	throw new IllegalArgumentException("Invalid index for sorted order");
                }
                onDetach(oldElem);
                onAttach(elem);
                if (sortedKeys != this) {
                    sortedKeys.set(setIndex, key);
                }
                super.doSet(setIndex, elem);
                if (DEBUG_CHECK) debugCheck();
                return oldElem;

            } else {
                // Key exists already
            	setIndex--;
                if (setIndex != index) {
                    if (duplicateMode == DuplicateMode.ALLOW ||
                    		(key == null && allowNullKeys == NullMode.MULTIPLE)) {
                    	if (index == setIndex+1) {
                    		// ok
                    	} else if (index > setIndex) {
                        	throw new IllegalArgumentException("Invalid index for sorted order");
                    	} else {
                    		int lowerIndex = setIndex-1;
                    		while (lowerIndex >= 0) {
                    			if (comparator.compare(key, sortedKeys.get(lowerIndex)) != 0) {
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
                onDetach(oldElem);
                onAttach(elem);
                if (sortedKeys != this) {
                    sortedKeys.set(index, key);
                }
                super.doSet(index, elem);
                if (DEBUG_CHECK) debugCheck();
                return oldElem;
            }
        }
    }

    @Override
    protected E doRemove(int index) {
        E elem = get(index);
        onDetach(elem);
        K key = getKey(elem);
        return doRemove(index, elem, key);
    }

    /**
     * Private method to remove element.
     *
     * @param index index of element to remove
     * @param elem  element to remove
     * @param key   key of element to remove
     * @return      removed element
     */
    @SuppressWarnings("unchecked")
    private E doRemove(int index, E elem, K key) {
        if (keys != null) {
            // not sorted
            Object obj = keys.get(key);
            if (obj == null) {
                throw new IllegalArgumentException("Key missmatch: " + key);
            }
            if (obj instanceof GapList) {
                GapList<E> list = (GapList<E>) obj;
                if (!list.remove(elem)) {
                    throw new IllegalArgumentException("Key missmatch: " + key);
                }
                if (list.isEmpty()) {
                    keys.remove(key);
                }
            } else {
                keys.remove(key);
            }
        } else {
            // sorted
            Object removedKey = null;
            if (sortedKeys == this) {
                removedKey = super.doRemove(index);
            } else {
                removedKey = sortedKeys.remove(index);
            }
            if (!GapList.equalsElem(key, removedKey)) {
                throw new IllegalArgumentException("Key missmatch: " + key);
            }
            if (sortedKeys == this) {
                if (DEBUG_CHECK) debugCheck();
                return (E) removedKey;
            }
        }
        E oldElem = super.doRemove(index);
        if (DEBUG_CHECK) debugCheck();
        return oldElem;
    }

    /**
     * Checks whether the specified key exists in this list.
     *
     * @param key key to look for
     * @return  true if the key exists, otherwise false
     */
    public boolean containsKey(K key) {
        return indexOfKey(key) != -1;
    }

    /**
     * Find given key and return its index.
     *
     * @param key   key to find
     * @return      index of key or -1 if not found
     */
    public int indexOfKey(K key) {
        if (key == null) {
            if (allowNullKeys == NullMode.NONE) {
                return -1;
            }
        }

        if (keys != null) {
            // not sorted
            Object elem = keys.get(key);
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
                    K oldKey = mapper.getKey(doGet(i));
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
            int index = SortedLists.binarySearchGet(sortedKeys, key, comparator);
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
    public int getCountDistinctKeys() {
    	if (keys != null) {
    		return keys.size();
    	} else {
    		return getDistinctKeys().size();
    	}
    }

    /**
     * Returns list containing all distinct keys.
     *
     * @return list containing all distinct keys
     */
    public GapList<K> getDistinctKeys() {
        if (keys != null) {
            GapList<K> list = new GapList<K>();
            Set<K> ks = new HashSet<K>(keys.keySet());
            for (int i=0; i<size(); i++) {
                K k = getKey(get(i));
                if (ks.remove(k)) {
                    list.add(k);
                    if (ks.isEmpty()) {
                        break;
                    }
                }
            }
            assert(list.size() == keys.size());
            return list;
        } else {
            K lastKey = null;
            GapList<K> list = new GapList<K>();
            for (int i=0; i<sortedKeys.size(); i++) {
                K key = sortedKeys.get(i);
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

    /**
     * Returns value for given key.
     * If there are several values for this key, the first is returned.
     * If the key is not found, null is returned.
     *
     * @param key   key to find
     * @return      value of specified key or null
     */
    @SuppressWarnings("unchecked")
    public E getByKey(K key) {
        // Handle null key if not allowed to prevent NPE
        if (key == null) {
            if (allowNullKeys == NullMode.NONE) {
                return null;
            }
        }

        if (keys != null) {
            // not sorted
            Object obj = keys.get(key);
            if (obj instanceof GapList) {
                GapList<E> list = (GapList<E>) obj;
                return list.getFirst();
            } else {
                return (E) keys.get(key);
            }

        } else {
            // sorted
            int index = SortedLists.binarySearchGet(sortedKeys, key, comparator);
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
    public GapList<E> getAllByKey(K key) {
        // Handle null key if not allowed to prevent NPE
        if (key == null) {
            if (allowNullKeys == NullMode.NONE) {
                return GapList.EMPTY();
            }
        }

        if (keys != null) {
            // not sorted
            Object obj = keys.get(key);
            if (obj == null) {
                return GapList.EMPTY();
            } else if (obj instanceof GapList) {
                GapList<E> list = (GapList<E>) obj;
                return list.unmodifiableList();
            } else {
                return (GapList<E>) GapList.create(keys.get(key));
            }

        } else {
            // sorted
            int index = SortedLists.binarySearchGet(sortedKeys, key, comparator);
            if (index >= 0) {
                GapList<E> list = new GapList<E>();
                while (true) {
                    list.add(doGet(index));
                    index++;
                    if (index == size()) {
                        break;
                    }
                    if (!GapList.equalsElem(sortedKeys.get(index), key)) {
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
    @SuppressWarnings("unchecked")
    public int getCountByKey(K key) {
        // Handle null key if not allowed to prevent NPE
        if (key == null) {
            if (allowNullKeys == NullMode.NONE) {
                return 0;
            }
        }

        if (keys != null) {
            // not sorted
            Object obj = keys.get(key);
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
            int index = SortedLists.binarySearchGet(sortedKeys, key, comparator);
            if (index >= 0) {
                int count = 0;
                while (true) {
                    count++;
                    index++;
                    if (index == size()) {
                        break;
                    }
                    if (!GapList.equalsElem(sortedKeys.get(index), key)) {
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
     * The keys of the elements must not change as long as the elements are stored in the list.
     * If a key changes nevertheless, you must call invalidate() to update the list's internal information.
     *
     * @param elem  element whose key has changed
     */
    @SuppressWarnings("unchecked")
    public void invalidate(E elem) {
        K newKey = getKey(elem);
        if (keys != null) {
            // Keys not sorted
            K oldKey = null;
            for (Map.Entry<K, Object> entry: keys.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof GapList) {
                    GapList<E> list = (GapList<E>) value;
                    int index = list.indexOf(elem);
                    if (index != -1) {
                        oldKey = entry.getKey();
                        if (!GapList.equalsElem(oldKey, newKey)) {
                            if (containsKey(newKey)) {
                                if (duplicateMode != DuplicateMode.ALLOW) {
                                    throw new IllegalArgumentException("Duplicate key not allowed: " + newKey);
                                }
                            }
                            doRemove(value, oldKey);
                            doAdd(elem, newKey);
                            if (DEBUG_CHECK) debugCheck();
                            return;
                        }
                        break;
                    }
                } else {
                    if (value == elem) {
                        oldKey = entry.getKey();
                        if (!GapList.equalsElem(oldKey, newKey)) {
                            if (containsKey(newKey)) {
                                if (duplicateMode != DuplicateMode.ALLOW) {
                                    throw new IllegalArgumentException("Duplicate key not allowed: " + newKey);
                                }
                            }
                            doRemove(value, oldKey);
                            doAdd(elem, newKey);
                            if (DEBUG_CHECK) debugCheck();
                            return;
                        }
                        break;
                    }
                }
            }
            throw new IllegalArgumentException("Key missmatch: " + newKey);

        } else {
            // Sorted keys
            int i;
            int size = size();
            for (i=0; i<size; i++) {
                if (doGet(i) == elem) {
                    break;
                }
            }
            if (i == size) {
                throw new IllegalArgumentException("Key missmatch: " + newKey);
            }
            K oldKey = sortedKeys.get(i);
            if (!GapList.equalsElem(oldKey, newKey)) {
                if (containsKey(newKey)) {
                    if (duplicateMode != DuplicateMode.ALLOW) {
                        throw new IllegalArgumentException("Duplicate key not allowed: " + newKey);
                    }
                }
                doRemove(i, elem, oldKey);
                doAdd(-1, elem, false);
            }
        }
    }

    /**
     * Removes element by key.
     * If there are duplicates, only one element is removed.
     *
     * @param key   key of element to remove
     * @return      removed element or null if no element has been removed
     */
    public E removeByKey(K key) {
//        // Handle null key if not allowed to prevent NPE
//        if (key == null) {
//            if (allowNullKeys == NullMode.NONE) {
//                return null;
//            }
//        }

        if (keys != null) {
            // not sorted
        	if (!keys.containsKey(key)) {
        		return null;
        	}
            Object obj = keys.get(key);
            E elem = doRemove(obj, key);

            // Faster than remove(elem) (equals not needed)
            int i;
            for (i=0; i<size(); i++) {
                if (elem != null) {
                    if (doGet(i) == elem) {
                        break;
                    }
                } else {
                    K oldKey = mapper.getKey(doGet(i));
                    if (GapList.equalsElem(oldKey, key)) {
                        break;
                    }
                }
            }
            if (i == size()) {
                throw new IllegalArgumentException("Key missmatch: " + key);
            }
            elem = super.doRemove(i);
            if (DEBUG_CHECK) debugCheck();
            return elem;

        } else {
            // sorted
            int index = SortedLists.binarySearchGet(sortedKeys, key, comparator);
            if (index >= 0) {
                if (sortedKeys != this) {
                    sortedKeys.remove(index);
                }
                E elem = super.doRemove(index);
                if (DEBUG_CHECK) debugCheck();
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
    public boolean removeAllByKey(K key) {
//      // Handle null key if not allowed to prevent NPE
//      if (key == null) {
//          if (allowNullKeys == NullMode.NONE) {
//              return false;
//          }
//      }

        if (keys != null) {
            // not sorted
        	if (!keys.containsKey(key)) {
        		return false;
        	}
            Object obj = keys.remove(key);
            int num;
            if (obj instanceof GapList) {
                GapList<E> list = (GapList<E>) obj;
                num = list.size();
            } else {
                num = 1;
            }
            for (int i=0; i<size(); i++) {
                K oldKey = mapper.getKey(doGet(i));
                if (GapList.equalsElem(oldKey, key)) {
                    super.doRemove(i);
                    num--;
                    if (num == 0) {
                        break;
                    }
                    i--;
                }
            }
            if (DEBUG_CHECK) debugCheck();
            return true;

        } else {
            // sorted
            int index = SortedLists.binarySearchGet(sortedKeys, key, comparator);
            if (index < 0) {
                return false;
            }
            int start = index;
            while (true) {
                index++;
                if (index == size()) {
                    break;
                }
                if (!GapList.equalsElem(sortedKeys.get(index), key)) {
                    break;
                }
            }
            sortedKeys.remove(start, index-start);
            if (sortedKeys != this) {
                while (index > start) {
                    index--;
                    super.doRemove(index);
                }
            }
            if (DEBUG_CHECK) debugCheck();
            return true;
        }
    }

    @Override
    public void sort(int index, int len, Comparator<? super E> comparator) {
    	checkRange(index, len);

    	if (sortedKeys != null) {
    		// already sorted
    		return;
    	}

    	super.sort(index, len, comparator);
    }

}
