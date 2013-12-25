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
import java.util.Set;

import org.magicwerk.brownies.collections.KeyCollectionImpl.KeyMap;
import org.magicwerk.brownies.collections.helper.Option;



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
@SuppressWarnings("static-access")
public class KeyListImpl<E> extends GapList<E> {

    /**
     * Key collection used for key storage.
     */
    KeyCollectionImpl<E> keyColl;
    /**
     * If forward is not null, the pointed to list stores the data for this instance
     * of KeyListImpl. The inherited GapList will not be used and be empty.
     */
    GapList<E> forward;

    /** If true the invariants the GapList are checked for debugging */
    private static final boolean DEBUG_CHECK = false;


    /*
     * GapList offers static create() methods which are public.
     * As TableListImpl extends GapList, it also inherits these methods.
     * As they cannot be hidden, the will throw an UnsupportedOperationException.
     * A cleaner solution would be that TableListImpl does not extend GapList.
     */

    /**
     * Do not use. Use Builder instead.
     */
    public static <E> GapList<E> create() {
        throw new UnsupportedOperationException();
    }

    /**
     * Do not use. Use Builder instead.
     */
    public static <E> GapList<E> create(int capacity) {
        throw new UnsupportedOperationException();
    }

    /**
     * Do not use. Use Builder instead.
     */
    public static <E> GapList<E> create(Collection<? extends E> coll) {
        throw new UnsupportedOperationException();
    }

    /**
     * Do not use. Use Builder instead.
     */
    public static <E> GapList<E> create(E... elems) {
        throw new UnsupportedOperationException();
    }

    @Override
	void init(Object[] values, int size) {
    	assert(forward == null);
        super.init(values, size);
	}

    /**
     * Private method to check invariant of GapList.
     * It is only used for debugging.
     */
    private void debugCheck() {
    	keyColl.debugCheck();
    	if (forward != null) {
    		assert(super.size() == 0);
    	} else {
    		assert(super.size() == keyColl.size());
    	}
    }

    /**
     * Constructor.
     */
    KeyListImpl() {
    	super(false, null);
    }

	@Override
    public Object clone() {
    	return copy();
    }

    @Override
    public KeyListImpl copy() {
    	KeyListImpl copy = (KeyListImpl) super.clone();
        copy.initCopy(this);
        return copy;
    }

    /**
     * Returns a copy this list but without elements.
     * The new list will use the same comparator, ordering, etc.
     *
     * @return  an empty copy of this list
     */
    public KeyListImpl crop() {
    	KeyListImpl crop = (KeyListImpl) super.clone();
        crop.initCrop(this);
        return crop;
    }
	/**
	 * Initialize object for crop() operation.
	 *
	 * @param that source object
	 */
    void initCrop(KeyListImpl<E> that) {
	    // TableCollection
	    keyColl = new KeyCollectionImpl<E>();
	    keyColl.initCrop(that.keyColl);
	    if (keyColl.keyList != null) {
	    	keyColl.keyList = this;
	    }

	    // GapList
	    if (that.forward != null) {
	    	assert(that.forward == that.keyColl.keyMaps[0].keysList);
	    	forward = (GapList<E>) keyColl.keyMaps[0].keysList;
	    } else {
	    	super.init();
	    }

	    if (DEBUG_CHECK) debugCheck();
	}

	/**
     * Initialize object for copy() operation.
     *
     * @param that source object
     */
    void initCopy(KeyListImpl<E> that) {
	    // TableCollection
	    keyColl = new KeyCollectionImpl<E>();
	    keyColl.initCopy(that.keyColl);
	    if (keyColl.keyList != null) {
	    	keyColl.keyList = this;
	    }

        // GapList
	    if (that.forward != null) {
	    	assert(that.forward == that.keyColl.keyMaps[0].keysList);
	    	forward = (GapList<E>) keyColl.keyMaps[0].keysList;
	    } else {
	    	super.initClone(that);
	    }

        if (DEBUG_CHECK) debugCheck();
    }

    @Override
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
    	return new CollectionAsSet<E>(this, false);
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
        if (keyColl.insertTrigger != null) {
            keyColl.insertTrigger.handle(elem);
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
        if (keyColl.deleteTrigger != null) {
            keyColl.deleteTrigger.handle(elem);
        }
    }

    //-- Read

    @Override
    public int capacity() {
    	if (forward != null) {
    		return forward.capacity();
    	} else {
    		return super.capacity();
    	}
    }

    @Override
    public int size() {
    	if (forward != null) {
    		return forward.size();
    	} else {
    		return super.size();
    	}
    }

    @Override
    public E get(int index) {
    	if (forward != null) {
    		return forward.get(index);
    	} else {
    		return super.get(index);
    	}
    }

    @Override
    protected E doGet(int index) {
    	if (forward != null) {
    		return forward.doGet(index);
    	} else {
    		return super.doGet(index);
    	}
    }

    @Override
	protected <T> void doGetAll(T[] array, int index, int len) {
    	if (forward != null) {
    		forward.doGetAll(array, index, len);
    	} else {
    		super.doGetAll(array, index, len);
    	}
    }

    //--

    /**
     * {@inheritDoc}
     * <p><i>
     * Note that the behavior of the operation depends on the defined constraints.
     * </i></p>
     */
    // This method is only overwritten to change the Javadoc comment.
    @Override
    public boolean add(E elem) {
        return super.add(elem);
    }

    /**
     * {@inheritDoc}
     * <p><i>
     * Note that the behavior of the operation depends on the defined constraints.
     * </i></p>
     */
    // This method is only overwritten to change the Javadoc comment.
    @Override
    public void add(int index, E elem)  {
        super.add(index, elem);
    }

    /**
     * {@inheritDoc}
     * <p><i>
     * Note that the behavior of the operation depends on the defined constraints.
     * </i></p>
     */
    // This method is only overwritten to change the Javadoc comment.
    @Override
    public E set(int index, E elem) {
        return super.set(index, elem);
    }

    @Override
    public void clear() {
    	keyColl.clear();
    	if (forward == null) {
    		super.clear();
    	}
    }

    @Override
    public void ensureCapacity(int minCapacity) {
        // Make sure that we never allocate more slots than needed.
        // The add methods make sure that we never use to many slots.
    	if (keyColl.maxSize != 0) {
    		minCapacity = Math.min(minCapacity, keyColl.maxSize);
    	}
        super.ensureCapacity(minCapacity);
    }

	@Override
    protected boolean doAdd(int index, E elem) {
    	// This method is also called by doAdd(E)
    	keyColl.checkElemAllowed(elem);

    	if (keyColl.maxSize != 0 && size() >= keyColl.maxSize) {
    		if (keyColl.movingWindow) {
    			if (index == 0) {
    				// the element inserted at position 0 will removed again due to the limited size
    				return false;
    			}
    			if (index == -1) {
    				index = size()-1;
    			}
   				doRemove(0);
   				index = index-1;
    		} else {
    			keyColl.errorMaxSize();
    		}
    	}

		if (keyColl.isSortedList()) {
			if (index == -1) {
				index = keyColl.binarySearchSorted(elem);
				if (index < 0) {
					index = -index-1;
				}
			}
			keyColl.addSorted(index, elem);
			if (forward == null) {
	    		super.doAdd(index, elem);
			}
		} else {
			keyColl.add(elem);
			if (index == -1) {
				// Element is already added to tableColl
				index = keyColl.size()-1;
			}
    		super.doAdd(index, elem);
		}

        if (DEBUG_CHECK) debugCheck();
        return true;
    }

	@Override
	protected boolean doAddAll(int index, E[] array) {
    	// delegate to doAdd()
		if (array.length == 0) {
			return false;
		}
		for (E elem: array) {
			int size = size();
			doAdd(index, elem);
			if (index != -1) {
				// If size has not changed, this is a list with max size,
				// so there is no need to increment the insert index
				if (size() != size) {
					index++;
				}
			}
		}
		return true;
	}

    @Override
    protected E doSet(int index, E elem) {
    	keyColl.checkElemAllowed(elem);

    	E remove = doGet(index);
    	if (keyColl.isSortedList()) {
        	keyColl.setSorted(index, elem, remove);
        	if (forward == null) {
        		super.doSet(index, elem);
        	} else {
        		forward.doSet(index, elem);
        	}

    	} else {
	    	keyColl.remove(remove);
	    	try {
		    	keyColl.add(elem);
		    	if (forward == null) {
		    		super.doSet(index, elem);
		    	}
	    	}
	    	catch (RuntimeException e) {
	    		keyColl.add(remove);
	    		throw e;
	    	}
    	}
        if (DEBUG_CHECK) debugCheck();
        return remove;
    }

    @Override
    protected void doSetAll(int index, E[] elems) {
    	// delegate to doSet()
        for (int i=0; i<elems.length; i++) {
            doSet(index+i, elems[i]);
        }
    }

    @Override
    protected E doRemove(int index) {
    	E removed = doGet(index);
		keyColl.remove(removed);
    	if (forward == null) {
    		super.doRemove(index);
    	}
        return removed;
    }

    @Override
	protected void doRemoveAll(int index, int len) {
    	if (forward != null) {
    		forward.doRemoveAll(index, len);
    	} else {
    		super.doRemoveAll(index, len);
    	}
	}

    @Override
    protected E doReSet(int index, E elem) {
    	if (forward != null) {
    		return forward.doReSet(index, elem);
    	} else {
    		return super.doReSet(index, elem);
    	}
    }

	@Override
    @SuppressWarnings("unchecked")
    public int indexOf(Object elem) {
    	if (keyColl.isSortedList()) {
    		return keyColl.indexOfSorted((E) elem);
    	} else {
    		return super.indexOf(elem);
    	}
    }

    /**
     * Find given key and return its index.
     *
     * @param keyIndex	key index
     * @param key   	key to find
     * @return      	index of key or -1 if not found
     */
    public int indexOfKey(int keyIndex, Object key) {
    	return indexOfKey(keyIndex, key, 0);
    }

    /**
     * Find given key and return its index.
     *
     * @param keyIndex	key index
     * @param key   	key to find
     * @param start		start index for search
     * @return      	index of key or -1 if not found
     */
    public int indexOfKey(int keyIndex, Object key, int start) {
    	int size = size();
    	for (int i=start; i<size; i++) {
    		Object elemKey = keyColl.getKey(keyIndex, doGet(i));
    		if (equalsElem(elemKey, key)) {
    			return i;
    		}
    	}
    	return -1;
    }

    /**
     * Checks whether the specified key exists in this list.
     *
     * @param keyIndex	key index
     * @param key 		key to look for
     * @return  		true if the key exists, otherwise false
     */
    public boolean containsKey(int keyIndex, Object key) {
        return indexOfKey(keyIndex, key) != -1;
    }

    /**
     * Returns value for given key.
     * If there are several values for this key, the first is returned.
     * If the key is not found, null is returned.
     *
     * @param keyIndex	key index
	 * @param key   	key to find
     * @return      	value of specified key or null
     */
    public E getByKey(int keyIndex, Object key) {
    	return (E) keyColl.getByKey(keyIndex, key);
    }

    /**
     * Returns a list with all elements with the specified key.
     *
     * @param keyIndex	key index
     * @param key   	key which elements must have
     * @return      	list with all keys (null if key is null)
     */
    public GapList<E> getAllByKey(int keyIndex, Object key) {
    	return keyColl.getAllByKey(keyIndex, key);
    }

    /**
     * Returns number of elements with specified key.
     *
     * @param keyIndex	key index
     * @param key   	key which elements must have
     * @return      	number of elements with key
     */
    public int getCountByKey(int keyIndex, Object key) {
    	return keyColl.getCountByKey(keyIndex, key);
    }

    /**
     * Removes element by key.
     * If there are duplicates, only one element is removed.
     *
     * @param keyIndex	key index
     * @param key   	key of element to remove
     * @return      	removed element or null if no element has been removed
     */
    protected E removeByKey(int keyIndex, Object key) {
    	Option<E> removed = keyColl.doRemoveByKey(keyIndex, key);
    	if (removed.hasValue()) {
	    	if (forward == null) {
	    		int index = super.indexOf(removed.getValue());
	    		if (index == -1) {
	    			keyColl.errorInvalidData();
	    		}
	    		super.doRemove(index);
	    	}
    	}
        if (DEBUG_CHECK) debugCheck();
        return removed.getValueOrNull();
    }

    protected E putByKey(int keyIndex, E elem) {
    	boolean add = true;
    	int index;
    	if (keyIndex == 0 && (keyColl.keyMaps == null || keyColl.keyMaps[0] == null)) {
    		index = indexOf(elem);
    	} else {
    		Object key = keyColl.getKey(keyIndex, elem);
    		index = indexOfKey(keyIndex, key);
    	}
    	if (index != -1) {
    		KeyMap keyMap = keyColl.getKeyMap(keyIndex);
    		if (elem != null) {
    			add = keyMap.allowDuplicates;
    		} else {
    			add = keyMap.allowDuplicatesNull;
    		}
    	}

    	E replaced = null;
    	if (add) {
    		doAdd(-1, elem);
    	} else {
    		replaced = doSet(index, elem);
    	}
        if (DEBUG_CHECK) debugCheck();
    	return replaced;
    }

    /**
     * Removes element by key.
     * If there are duplicates, all elements are removed.
     *
     * @param keyIndex 	key index
     * @param key   	key of element to remove
     * @return      	true if elements have been removed, false otherwise
     */
    protected GapList<E> removeAllByKey(int keyIndex, Object key) {
    	GapList<E> removeds = keyColl.removeAllByKey(keyIndex, key);
    	if (!removeds.isEmpty()) {
	    	if (forward == null) {
	    		if (!super.removeAll(removeds)) {
	    			keyColl.errorInvalidData();
	    		}
	    	}
    	}
        if (DEBUG_CHECK) debugCheck();
        return removeds;
    }

    /**
     * Returns list containing all distinct keys.
     *
     * @param keyIndex 	key index
     * @return 			list containing all distinct keys
     */
    public Set<?> getDistinctKeys(int keyIndex) {
    	return keyColl.getDistinctKeys(keyIndex);
    }

    @Override
    public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
    	// If this is a sorted list, it is obvious that binarySearch will work.
    	// The list can however also be sorted without been declared as being ordered,
    	// so we just try to do the binary search (as if Collections.binarySearch is called)
    	return super.binarySearch(index, len, key, comparator);
    }

    @Override
    public void sort(int index, int len, Comparator<? super E> comparator) {
    	// If this is sorted list, the comparator must be equal to the specified one
    	Comparator<?> sortComparator = keyColl.getSortComparator();
    	if (sortComparator != null) {
    		if (sortComparator != comparator) {
        		throw new IllegalArgumentException("Different comparator specified for sorted list");
    		}
    	} else {
        	super.sort(index, len, comparator);
    	}
    }

    //-- Element methods

	/**
	 * Returns all equal elements.
	 * The returned list is immutable.
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
	 * The returned set is immutable.
	 *
	 * @return		distinct elements
	 */
	protected Set<E> getDistinct() {
		return (Set<E>) getDistinctKeys(0);
	}

	/**
	 * Adds or replaces element.
	 * If there is no such element, the element is added.
	 * If there is such an element key and no duplicates
	 * are allowed, the existing element is replaced.
	 * If duplicates are allowed, the element is added.
	 *
	 * @param elem	element
	 * @return		element which has been replaced or null otherwise
	 */
	protected E put(E elem) {
		return putByKey(0, elem);
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
    	keyColl.invalidate(elem);
    	if (keyColl.isSortedList() && forward == null) {
    		int oldIndex = super.indexOf(elem);
    		int newIndex = keyColl.indexOfSorted(elem);
    		if (oldIndex != newIndex) {
    			super.doRemove(oldIndex);
    			if (oldIndex < newIndex) {
    				newIndex--;
    			}
    			super.doAdd(newIndex, elem);
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
    	elem = keyColl.doInvalidateKey(keyIndex, oldKey, newKey, elem);
    	if (keyColl.orderByKey == keyIndex && forward == null) {
    		super.doRemove(super.indexOf(elem));
    		int index = keyColl.indexOfSorted(elem);
    		super.doAdd(index, elem);
    	}
        if (DEBUG_CHECK) debugCheck();
    }

    //-- Key methods
	// The key methods can not be defined here.
	// Due to the generic type parameters, the methods cannot be overridden.

	//-- Invalidate

    //
//  public void invalidate(E elem) {
//  	int index = indexOf(elem);
//  	if (index == -1) {
//  		throw new IllegalArgumentException("Element not found: " + elem);
//  	}
//  	invalidate(index);
//  }
//
//  public void invalidate(int index) {
//  	E elem = doGet(index);
//      for (int i=0; i<keyMaps.length; i++) {
//  		Object key = invalidate(keyMaps[i], elem);
//  		if (key != null) {
//  			if (i == 0 && keyMaps[i].sortedKeys != null && keyMaps[i].sortedKeys != this) {
//  				// First key is sorted
//  				int idx = super.indexOf(elem);
//  				super.doRemove(idx);
//  				idx = doAddKey(keyMaps[i], -1, elem);
//  				super.doAdd(idx, elem);
//  			} else {
//  				// Not first or not sorted key
//  				doAddKey(keyMaps[i], -1, elem);
//  			}
//  		}
//      }
//      if (DEBUG_CHECK) debugCheck();
//  }
//
//  /**
//   * @param keyMap
//   * @param elem
//   * @return			null if key for keyMap and element is correct, else key which must be added to keymap
//   */
//  private Object invalidate(KeyMap keyMap, Object elem) {
//  	boolean allowDuplicates = (keyMap.duplicateMode == DuplicateMode.ALLOW);
//  	Object key = keyMap.mapper.getKey(elem);
//
//  	if (keyMap.unsortedKeys != null) {
//  		Iterator<Map.Entry> iter = keyMap.unsortedKeys.entrySet().iterator();
//  		while (iter.hasNext()) {
//  		    Map.Entry entry = iter.next();
//  		    if (equalsElem(elem, entry.getValue())) {
//  		    	if (equalsElem(key, entry.getKey())) {
//  		    		return null;
//  		    	}
//  		        iter.remove();
//  		        if (!allowDuplicates) {
//  		        	break;
//  		        }
//  		    }
//  		}
//  	} else {
//  		assert(keyMap.sortedKeys != null);
//  		for (int i=0; i<size(); i++) {
//  			if (equalsElem(elem, doGet(i))) {
//  				if (equalsElem(key, keyMap.sortedKeys.get(i))) {
//  					return null;
//  				}
//  				keyMap.sortedKeys.remove(i);
//  		        if (!allowDuplicates) {
//  		        	break;
//  		        }
//  			}
//  		}
//  	}
//  	return key;
//  }

//	// Fast doAddAll() implementation for window size
//	protected boolean doAddAll(int index, E[] array) {
//        checkIndexAdd(index);
//
//        int addSize = array.length;
//		if (addSize == 0) {
//			return false;
//		}
//		int overlap = size()+addSize - maxSize;
//		if (overlap > 0) {
//			if (index >= overlap) {
//				super.remove(0, overlap);
//			} else {
//				super.remove(0, index);
//			}
//			index = index - overlap;
//		}
//		if (index >= 0) {
//			for (int i=0; i<addSize; i++) {
//				super.doAdd(index+i, array[i]);
//			}
//		} else {
//			for (int i=0; i<addSize+index; i++) {
//				super.doAdd(0, array[i-index]);
//			}
//		}
//		return true;
//	}

}
