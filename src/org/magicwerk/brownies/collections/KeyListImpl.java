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

import org.magicwerk.brownies.collections.helper.CollectionAsSet;


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
public class KeyListImpl<E> extends GapList<E> {

    KeyCollectionImpl keyColl;
    GapList<E> forward;

    /** If true the invariants the GapList are checked for debugging */
    private static final boolean DEBUG_CHECK = true;


    /*
     * GapList offers static create() methods which are public.
     * As TableListImpl extends GapList, it also inherits these methods.
     * As they cannot be hidden, the will throw an UnsupportedOperationException.
     * A cleaner solution would be that TableListImpl does not extend GapList.
     */

    /**
     * Do not use. Use Builder instead.
     */
    public static <E> KeyListImpl<E> create() {
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

    /**
     * Private method to check invariant of GapList.
     * It is only used for debugging.
     */
    private void debugCheck() {
    	if (forward != null) {
    		assert(super.size() == 0);
    	} else {
    		assert(super.size() == keyColl.size());
    	}
    }

    KeyListImpl() {
    	super(false, null);
    }

	/**
	 * Initialize object for crop() operation.
	 *
	 * @param that source object
	 */
	@SuppressWarnings("unchecked")
    void initCrop(KeyListImpl<E> that) {
	    // GapList
	    init(new Object[DEFAULT_CAPACITY], 0);

	    // TableCollection
	    keyColl = new KeyCollectionImpl();
	    keyColl.initCopy(that.keyColl);
	}

    /**
     * Initialize object for copy() operation.
     *
     * @param that source object
     */
    void initCopy(KeyListImpl<E> that) {
        // GapList
        init(that.toArray(), that.size());

	    // TableCollection
	    keyColl = new KeyCollectionImpl();
	    keyColl.initCrop(that.keyColl);
    }

    public Set<E> asSet() {
    	return new CollectionAsSet(this, false);
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

    @Override
    public void clear() {
    	keyColl.clear();
    	if (forward == null) {
    		super.clear();
    	}
    }

    /**
     * Returns a copy this list but without elements.
     * The new list will use the same comparator, ordering, etc.
     *
     * @return  an empty copy of this instance
     */
    public KeyListImpl<E> crop() {
    	// Derived classes must implement
    	throw new UnsupportedOperationException();
    }

    @Override
    protected boolean doAdd(int index, E elem) {
    	keyColl.checkElemAllowed(elem);

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
			doAdd(index, elem);
			if (index != -1) {
			    index++;
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
     * @param key   key to find
     * @return      index of key or -1 if not found
     */
    public <K> int indexOfKey(int keyIndex, K key) {
    	return indexOfKey(keyIndex, key, 0);
    }
    /**
     * Find given key and return its index.
     *
     * @param key   key to find
     * @return      index of key or -1 if not found
     */
    public <K> int indexOfKey(int keyIndex, K key, int start) {
    	int size = size();
    	for (int i=start; i<size; i++) {
    		K elemKey = (K) keyColl.getKey(keyIndex, doGet(i));
    		if (equalsElem(elemKey, key)) {
    			return i;
    		}
    	}
    	return -1;
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
     * Returns value for given key.
     * If there are several values for this key, the first is returned.
     * If the key is not found, null is returned.
     *
     * @param key   key to find
     * @return      value of specified key or null
     */
    public E getByKey(int keyIndex, Object key) {
    	return (E) keyColl.getByKey(keyIndex, key);
    }

    /**
     * Returns a list with all elements with the specified key.
     *
     * @param key   key which elements must have
     * @return      list with all keys (null if key is null)
     */
    @SuppressWarnings("unchecked")
    public GapList<E> getAllByKey(int keyIndex, Object key) {
    	return keyColl.getAllByKey(keyIndex, key);
    }

    /**
     * Returns number of elements with specified key.
     *
     * @param key   key which elements must have
     * @return      number of elements with key
     */
    public int getCountByKey(int keyIndex, Object key) {
    	return keyColl.getCountByKey(keyIndex, key);
    }

    /**
     * Removes element by key.
     * If there are duplicates, only one element is removed.
     *
     * @param key   key of element to remove
     * @return      removed element or null if no element has been removed
     */
    protected E removeByKey(int keyIndex, Object key) {
    	// TODO what about if null has been removed -> return Option
    	E removed = (E) keyColl.removeByKey(keyIndex, key);
    	if (!keyColl.isSortedList()) {
    		int index = super.indexOf(removed);
    		if (index == -1) {
    			keyColl.errorInvalidData();
    		}
    		super.doRemove(index);
    	}
        if (DEBUG_CHECK) debugCheck();
        return removed;
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
    public GapList<?> getDistinctKeys(int keyIndex) {
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
    	Comparator sortComparator = keyColl.getSortComparator();
    	if (sortComparator != null) {
    		if (sortComparator != comparator) {
        		throw new IllegalArgumentException("Different comparator specified for sorted list");
    		}
    	} else {
        	super.sort(index, len, comparator);
    	}
    }

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
}
