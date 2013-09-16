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
import org.magicwerk.brownies.collections.function.Predicate;
import org.magicwerk.brownies.collections.function.Trigger;
import org.magicwerk.brownies.collections.helper.IdentMapper;
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
 * @version $Id$
 *
 * @see GapList
 * @param <E> type of elements stored in the list
 */
public class TableListImpl<E> extends GapList<E> {

    TableCollectionImpl<E> tableImpl;

    /** If true the invariants the GapList are checked for debugging */
    private static final boolean DEBUG_CHECK = true;

    /**
     * Private method to check invariant of GapList.
     * It is only used for debugging.
     */
    private void debugCheck() {
    	assert(super.size() == tableImpl.size());
    }

    TableListImpl() {
    	super(false, null);
    }

    // TODO clone?

	/**
	 * Initialize object for crop() operation.
	 *
	 * @param that source object
	 */
	@SuppressWarnings("unchecked")
    void initCrop(TableListImpl<E> that) {
	    // GapList
	    init(new Object[DEFAULT_CAPACITY], 0);

	    // TableCollection
	    tableImpl = new TableCollectionImpl();
	    tableImpl.initCopy(that.tableImpl);
	}

    /**
     * Initialize object for copy() operation.
     *
     * @param that source object
     */
    void initCopy(TableListImpl<E> that) {
        // GapList
        init(that.toArray(), that.size());

	    // TableCollection
	    tableImpl = new TableCollectionImpl();
	    tableImpl.initCrop(that.tableImpl);
    }

    @Override
    public void clear() {
    	tableImpl.clear();
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

    @Override
    protected boolean doAdd(int index, E elem) {
    	tableImpl.checkElemAllowed(elem);

    	tableImpl.add(elem);
    	int addIndex = index;
    	if (tableImpl.isSortedList()) {
    		addIndex = tableImpl.binarySearchSorted(elem);
    		assert(addIndex >= 0);
    		if (index != -1 && addIndex != index) {
    			throw new IllegalArgumentException("Invalid index for sorted list: " + index);
    		}
    	} else {
    		if (addIndex == -1) {
    			// Element is already added to tableImpl
    			addIndex = tableImpl.size()-1;
    		}
    	}
        super.doAdd(addIndex, elem);
        if (DEBUG_CHECK) debugCheck();
        return true;
    }

    @Override
    protected E doSet(int index, E elem) {
    	tableImpl.checkElemAllowed(elem);

    	E remove = doGet(index);
    	// TODO this will result in add/remove instead of set for sorted lists
    	tableImpl.remove(remove);
    	tableImpl.add(elem);
    	int addIndex = index;
    	if (tableImpl.isSortedList()) {
    		addIndex = tableImpl.binarySearchSorted(elem);
    		assert(addIndex >= 0);
    		if (addIndex != index) {
    			throw new IllegalArgumentException("Invalid index for sorted list: " + index);
    		}
    	}
        super.doSet(addIndex, elem);
        if (DEBUG_CHECK) debugCheck();
        return remove;
    }

    @Override
    protected E doRemove(int index) {
    	E removed = super.doGet(index);
		tableImpl.remove(removed);
    	if (!tableImpl.isSortedList()) {
    		super.doRemove(index);
    	}
        return removed;
    }

    @Override
    public int indexOf(Object elem) {
    	if (tableImpl.isSortedList()) {
    		return tableImpl.indexOfSorted(elem);
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
    		K elemKey = (K) tableImpl.getKey(keyIndex, doGet(i));
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
    	return tableImpl.getByKey(keyIndex, key);
    }

    /**
     * Returns a list with all elements with the specified key.
     *
     * @param key   key which elements must have
     * @return      list with all keys (null if key is null)
     */
    @SuppressWarnings("unchecked")
    public GapList<E> getAllByKey(int keyIndex, Object key) {
    	return tableImpl.getAllByKey(keyIndex, key);
    }

    /**
     * Returns number of elements with specified key.
     *
     * @param key   key which elements must have
     * @return      number of elements with key
     */
    public int getCountByKey(int keyIndex, Object key) {
    	return tableImpl.getCountByKey(keyIndex, key);
    }

    /**
     * Removes element by key.
     * If there are duplicates, only one element is removed.
     *
     * @param key   key of element to remove
     * @return      removed element or null if no element has been removed
     */
    protected E removeByKey(int keyIndex, Object key) {
    	E removed = (E) tableImpl.removeByKey(keyIndex, key);
    	if (!tableImpl.isSortedList()) {
    		if (!super.remove(removed)) {
    			tableImpl.errorInvalidData();
    		}
    	}
        if (DEBUG_CHECK) debugCheck();
        return removed;
    }

    /**
     * Removes element by key.
     * If there are duplicates, all elements are removed.
     *
     * @param key   key of element to remove
     * @return      true if elements have been removed, false otherwise
     */
    protected GapList<E> removeAllByKey(int keyIndex, Object key) {
    	GapList<E> removeds = tableImpl.removeAllByKey(keyIndex, key);
    	if (!tableImpl.isSortedList()) {
    		if (!super.removeAll(removeds)) {
    			tableImpl.errorInvalidData();
    		}
    	}
        if (DEBUG_CHECK) debugCheck();
        return removeds;
    }

    /**
     * Returns list containing all distinct keys.
     *
     * @return list containing all distinct keys
     */
    public GapList<?> getAllDistinctKeys(int keyIndex) {
    	return tableImpl.getAllDistinctKeys(keyIndex);
    }

    /**
     * Returns count of distinct keys.
     *
     * @return count of distinct keys
     */
    public int getCountDistinctKeys(int keyIndex) {
    	return tableImpl.getCountDistinctKeys(keyIndex);
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
    	Comparator sortComparator = tableImpl.getSortComparator();
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
