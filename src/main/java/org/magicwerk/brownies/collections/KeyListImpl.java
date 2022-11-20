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

import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

import org.magicwerk.brownies.collections.helper.Option;

/**
 * A KeyList add key handling features to GapList.
 *
 * @author Thomas Mauch
 *
 * @see GapList
 * @param <E> type of elements stored in the list
 */
@SuppressWarnings("serial")
public abstract class KeyListImpl<E> extends IList<E> {

	/**
	 * Key collection used for key storage (never null).
	 */
	KeyCollectionImpl<E> keyColl;
	/**
	 * List where the list content of this KeyListImpl is stored (never null).
	 * If this is list sorted by element (key 0), keyColl.keyMaps[0].keysList will also reference this list.
	 */
	IList<E> list;

	/** If true the invariants are checked for debugging */
	private static final boolean DEBUG_CHECK = false;

	/**
	 * Private method to check invariant of KeyListImpl.
	 * It is only used for debugging.
	 */
	private void debugCheck() {
		keyColl.debugCheck();
		//list.debugCheck();
		assert (keyColl.keyList == this);
		assert (list.size() == keyColl.size() || (keyColl.size() == 0 && keyColl.keyMaps == null));
	}

	/**
	 * Constructor.
	 */
	KeyListImpl() {
	}

	protected KeyListImpl(boolean copy, KeyListImpl<E> that) {
		if (copy) {
			doAssign(that);
		}
	}

	@Override
	protected void doAssign(IList<E> that) {
		KeyListImpl<E> list = (KeyListImpl<E>) that;
		this.keyColl = list.keyColl;
		this.list = list.list;
	}

	@Override
	@SuppressWarnings("unchecked")
	public KeyListImpl<E> copy() {
		return (KeyListImpl<E>) clone();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object clone() {
		KeyListImpl<E> copy = (KeyListImpl<E>) super.clone();
		copy.initCopy(this);
		return copy;
	}

	/**
	 * Returns a copy this list but without elements.	 
	 * The new list will have the same type as this list and use the same comparator, ordering, etc.
	 * 
	 * @return  an empty copy of this list
	 */
	@Override
	@SuppressWarnings("unchecked")
	public KeyListImpl<E> crop() {
		KeyListImpl<E> copy = (KeyListImpl<E>) super.clone();
		copy.initCrop(this);
		return copy;
	}

	/**
	 * Returns the elements stored in the KeyList as simple IList without constraints etc.
	 * The KeyList itself becomes empty, it is equal to a list created by calling crop().
	 * This method is useful if you need performance or safety of a KeyList just while building the result,
	 * but you then want to return a simple IList without constraints and the KeyList is not needed any longer. 
	 *
	 * @return IList with elements
	 */
	public IList<E> unwrap() {
		IList<E> l = list;
		this.initCrop(this);
		return l;
	}

	/**
	 * Initialize object for crop() operation.
	 *
	 * @param that source object
	 */
	@SuppressWarnings("unchecked")
	void initCrop(KeyListImpl<E> that) {
		// KeyCollectionImpl
		keyColl = new KeyCollectionImpl<E>();
		keyColl.initCrop(that.keyColl);
		keyColl.keyList = this;

		// List
		if (that.keyColl.keyMaps != null && that.keyColl.keyMaps[0] != null && that.list == that.keyColl.keyMaps[0].keysList) {
			list = (IList<E>) keyColl.keyMaps[0].keysList;
		} else {
			list = that.list.doCreate(-1);
		}

		if (DEBUG_CHECK)
			debugCheck();
	}

	/**
	 * Initialize object for copy() operation.
	 *
	 * @param that source object
	 */
	@SuppressWarnings("unchecked")
	void initCopy(KeyListImpl<E> that) {
		// KeyCollectionImpl
		keyColl = new KeyCollectionImpl<E>();
		keyColl.initCopy(that.keyColl);
		if (keyColl.keyList != null) {
			keyColl.keyList = this;
		}

		// List
		if (that.keyColl.keyMaps != null && that.keyColl.keyMaps[0] != null && that.list == that.keyColl.keyMaps[0].keysList) {
			list = (IList<E>) keyColl.keyMaps[0].keysList;
		} else {
			list = that.list.doCreate(-1);
			list.addAll(that.list);
		}

		if (DEBUG_CHECK)
			debugCheck();
	}

	@Override
	protected void doClone(IList<E> that) {
		// overridden as not used, initCopy() and initCrop() are used instead
	}

	/**
	 * Returns a Set view of the element set.
	 *
	 * @return set view
	 * @throws IllegalArgumentException if the element set cannot be viewed as Set
	 */
	public Set<E> asSet() {
		return new CollectionAsSet<E>(this, false, true);
	}

	//-- Read

	/**
	 * Determines whether this list is sorted or not.
	 *
	 * @return true if this a sorted list, false if not
	 */
	public boolean isSorted() {
		return keyColl.isSorted();
	}

	@Override
	public int capacity() {
		return list.capacity();
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public E get(int index) {
		return list.get(index);
	}

	@Override
	protected E doGet(int index) {
		return list.doGet(index);
	}

	@Override
	protected <T> void doGetAll(T[] array, int index, int len) {
		list.doGetAll(array, index, len);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If the list defines maps for the keys, they are used to speed up the lookup operation.
	 * So if the list defines a primary key, performance will be similar to a lookup in a set.
	 */
	@Override
	public boolean contains(Object elem) {
		if (keyColl.keyMaps != null) {
			return keyColl.contains(elem);
		} else {
			return super.contains(elem);
		}
	}

	/**
	 * Determines whether calling contains() will be fast, i.e. it can use some sort of key lookup instead of traversing through all elements.
	 *
	 * @return	true if calling contains() will be fast, otherwise false
	 */
	boolean isContainsFast() {
		if (keyColl.keyMaps != null) {
			return keyColl.isContainsFast();
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If the list is sorted, this is used to speed up the remove operation.
	 * If the list defines unsorted map, this does not improve performance.
	 */
	@Override
	public boolean remove(Object elem) {
		return super.remove(elem);
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
	 * Adds element if allowed and returns true.
	 * If the element cannot be added (constraint violation like duplicated key), false is returned.
	 *
	 * @param elem 	element to add
	 * @return		true if element has been added, false otherwise
	 */
	public boolean addIf(E elem) {
		try {
			return super.add(elem);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 * <p><i>
	 * Note that the behavior of the operation depends on the defined constraints.
	 * </i></p>
	 */
	// This method is only overwritten to change the Javadoc comment.
	@Override
	public void add(int index, E elem) {
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
		list.clear();
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

		// Handle maximum size and window
		if (keyColl.maxSize != 0 && size() >= keyColl.maxSize) {
			if (keyColl.movingWindow) {
				if (index == 0) {
					// the element inserted at position 0 will be removed again due to the limited size
					return false;
				}
				if (index == -1) {
					index = size();
				}
				doRemove(0);
				index = index - 1;
			} else {
				KeyCollectionImpl.errorMaxSize();
			}
		}

		// Add element
		if (keyColl.isSorted()) {
			// Sorted list
			if (index == -1) {
				index = keyColl.binarySearchSorted(elem);
				if (index < 0) {
					index = -index - 1;
				}
			}
			keyColl.addSorted(index, elem);
			// If list is sorted by element, keyColl points to list so no explicit call to its add method is needed
			if (!keyColl.isSortedByElem()) {
				list.doAdd(index, elem);
			}
		} else {
			// Unsorted list
			keyColl.addUnsorted(elem);
			if (index == -1) {
				// Element is already added to keyColl
				index = list.size();
			}
			list.doAdd(index, elem);
		}

		if (DEBUG_CHECK)
			debugCheck();
		return true;
	}

	@Override
	protected E doSet(int index, E elem) {
		keyColl.checkElemAllowed(elem);

		E remove = doGet(index);
		if (keyColl.isSorted()) {
			keyColl.setSorted(index, elem, remove);

		} else {
			keyColl.remove(remove);
			try {
				keyColl.add(elem);
			} catch (RuntimeException e) {
				keyColl.add(remove);
				throw e;
			}
		}
		list.doSet(index, elem);
		if (DEBUG_CHECK)
			debugCheck();
		return remove;
	}

	@Override
	protected E doRemove(int index) {
		E removed = list.get(index);
		keyColl.remove(removed);
		if (!keyColl.isSortedByElem()) {
			list.remove(index);
		}
		if (DEBUG_CHECK)
			debugCheck();
		return removed;
	}

	@Override
	protected void doRemoveAll(int index, int len) {
		if (keyColl.isSortedByElem()) {
			for (int i = 0; i < len; i++) {
				E removed = list.get(index);
				keyColl.remove(removed);
			}
		} else {
			for (int i = 0; i < len; i++) {
				E removed = list.get(index + i);
				keyColl.remove(removed);
			}
			list.doRemoveAll(index, len);
		}
		if (DEBUG_CHECK)
			debugCheck();
	}

	@Override
	protected E doReSet(int index, E elem) {
		return list.doReSet(index, elem);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If the list is sorted, this is used to speed up the lookup operation.
	 * If the list defines unsorted map, this does not improve performance.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public int indexOf(Object elem) {
		if (keyColl.isSorted()) {
			return keyColl.indexOfSorted((E) elem);
		} else {
			return super.indexOf(elem);
		}
	}

	/**
	 * Determines whether calling indexOf() will be fast, i.e. it can use some sort of key lookup instead of traversing through all elements.
	 *
	 * @return	true if calling indexOf() will be fast, otherwise false
	 */
	boolean isIndexOfFast() {
		return keyColl.isSorted();
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
		for (int i = start; i < size; i++) {
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
	 * Returns mapper for specified key map.
	 *
	 * @param keyIndex 	key index
	 * @return      	mapper for specified key map
	 */
	public Function<E, Object> getKeyMapper(int keyIndex) {
		return keyColl.getKeyMapper(keyIndex);
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
		return keyColl.getByKey(keyIndex, key);
	}

	/**
	 * Returns a list with all elements with the specified key.
	 *
	 * @param keyIndex	key index
	 * @param key   	key which elements must have
	 * @return      	list with all elements
	 */
	public IList<E> getAllByKey(int keyIndex, Object key) {
		IList<E> list = doCreate(-1);
		keyColl.getAllByKey(keyIndex, key, list);
		return list;
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
			int index = list.indexOf(removed.getValue());
			if (index == -1) {
				KeyCollectionImpl.errorInvalidData();
			}
			list.doRemove(index);
		}
		if (DEBUG_CHECK)
			debugCheck();
		return removed.getValueOrNull();
	}

	/**
	 * Put element by key into list.
	 *
	 * @param keyIndex	key index
	 * @param elem		element to put
	 * @param replace	true to replace an existing element with the same key, false to let the element unchanged
	 * @return			replaced element or null if no element has been replaced
	 */
	protected E putByKey(int keyIndex, E elem, boolean replace) {
		int index;
		if (keyIndex == 0 && (keyColl.keyMaps == null || keyColl.keyMaps[0] == null)) {
			index = indexOf(elem);
		} else {
			Object key = keyColl.getKey(keyIndex, elem);
			index = indexOfKey(keyIndex, key);
		}
		E replaced = null;
		if (index == -1) {
			doAdd(-1, elem);
		} else {
			if (replace) {
				replaced = doSet(index, elem);
			}
		}
		if (DEBUG_CHECK)
			debugCheck();
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
	protected IList<E> removeAllByKey(int keyIndex, Object key) {
		IList<E> removeds = doCreate(-1);
		keyColl.removeAllByKey(keyIndex, key, removeds);
		if (!removeds.isEmpty()) {
			if (!keyColl.isSortedByElem()) {
				if (!list.removeAll(removeds)) {
					KeyCollectionImpl.errorInvalidData();
				}
			}
		}
		if (DEBUG_CHECK)
			debugCheck();
		return removeds;
	}

	/**
	 * Returns list containing all keys in element order.
	 *
	 * @param keyIndex	key index
	 * @return 			list containing all keys
	 */
	protected IList<Object> getAllKeys(int keyIndex) {
		Function<E, Object> mapper = keyColl.getKeyMap(keyIndex).mapper;
		GapList<Object> list = GapList.create();
		for (E obj : this) {
			list.add(mapper.apply(obj));
		}
		return list;
	}

	/**
	 * Returns set containing all distinct keys.
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
		return list.binarySearch(index, len, key, comparator);
	}

	@Override
	public void sort(int index, int len, Comparator<? super E> comparator) {
		if (keyColl.isSorted()) {
			// If this is sorted list, the comparator must be equal to the specified one
			if (keyColl.isSortedByElem()) {
				if (GapList.equalsElem(comparator, keyColl.getElemSortComparator())) {
					return;
				}
			}
			throw new IllegalArgumentException("Different comparator specified for sorted list");
		} else {
			list.sort(index, len, comparator);
		}
	}

	//-- Element methods

	@Override
	public IList<E> getAll(E elem) {
		if (keyColl.hasElemSet()) {
			return getAllByKey(0, elem);
		} else {
			return list.getAll(elem);
		}
	}

	@Override
	public int count(E elem) {
		if (keyColl.hasElemSet()) {
			return getCountByKey(0, elem);
		} else {
			return list.count(elem);
		}
	}

	@Override
	public IList<E> removeAll(E elem) {
		if (keyColl.hasElemSet()) {
			return removeAllByKey(0, elem);
		} else {
			return list.removeAll(elem);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<E> getDistinct() {
		if (keyColl.hasElemSet()) {
			return (Set<E>) getDistinctKeys(0);
		} else {
			return super.getDistinct();
		}
	}

	/**
	 * Adds or replaces element.
	 * If there is no such element, the element is added.
	 * If there is such an element, the element is replaced.
	 * So said simply, it is a shortcut for the following code:
	 * <pre>
	 * if (contains(elem)) {
	 *   remove(elem);
	 * }
	 * add(elem);
	 * </pre>
	 * However the method is atomic in the sense that all or none operations are executed.
	 * So if there is already such an element, but adding the new one fails due to a constraint violation,
	 * the old element remains in the list.
	 *
	 * @param elem	element
	 * @return		element which has been replaced or null otherwise
	 */
	protected E put(E elem) {
		// If contains() is fast but indexOf() slow, we check first whether the element is contained and then call
		// add if it not (we do not use the index for adding, so we can skip this costly operation)
		if (!isIndexOfFast() && isContainsFast()) {
			if (!contains(elem)) {
				add(elem);
				return null;
			}
		}

		int index = indexOf(elem);
		if (index != -1) {
			return set(index, elem);
		} else {
			add(elem);
			return null;
		}
	}

	/**
	 * Invalidate element, i.e. all keys of the element are extracted
	 * again and stored in the key maps. Old key values are removed if needed.
	 * You must call an invalidate method if an element's key value has changed after adding it to the collection.
	 *
	 * @param elem element to invalidate
	 */
	protected void invalidate(E elem) {
		keyColl.invalidate(elem);
		if (keyColl.isSorted() && !keyColl.isSortedByElem()) {
			int oldIndex = super.indexOf(elem);
			int newIndex = keyColl.indexOfSorted(elem);
			if (oldIndex != newIndex) {
				list.doRemove(oldIndex);
				if (oldIndex < newIndex) {
					newIndex--;
				}
				list.doAdd(newIndex, elem);
			}
		}
		if (DEBUG_CHECK)
			debugCheck();
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
		if (keyColl.orderByKey == keyIndex && list != null) {
			list.doRemove(super.indexOf(elem));
			int index = keyColl.indexOfSorted(elem);
			list.doAdd(index, elem);
		}
		if (DEBUG_CHECK)
			debugCheck();
	}

	@Override
	protected E getDefaultElem() {
		return null;
	}

	@Override
	protected void doEnsureCapacity(int minCapacity) {
		list.doEnsureCapacity(minCapacity);
	}

	@Override
	public void trimToSize() {
		list.trimToSize();
	}

	@Override
	protected IList<E> doCreate(int capacity) {
		return list.doCreate(capacity);
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
