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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implements a Map based on a key map in a KeyCollection or KeyList.
 *
 * @author Thomas Mauch
 */
@SuppressWarnings("serial")
public class KeyCollectionAsMap<K, E> implements Map<K, E>, Serializable {
	/** Reference to KeyCollectionImpl containing data (exactly one of the fields coll and list is set) */
	KeyCollectionImpl<E> coll;
	/** Reference to KeyListImpl containing data (exactly one of the fields coll and list is set) */
	KeyListImpl<E> list;
	int keyIndex;
	boolean immutable;

	public KeyCollectionAsMap(KeyCollectionImpl<E> coll, int keyIndex, boolean immutable) {
		if (coll == null) {
			throw new IllegalArgumentException("Collection may not be null");
		}
		coll.checkAsMap(keyIndex);

		this.coll = coll;
		this.keyIndex = keyIndex;
		this.immutable = immutable;
	}

	public KeyCollectionAsMap(KeyListImpl<E> list, int keyIndex, boolean immutable) {
		if (list == null) {
			throw new IllegalArgumentException("List may not be null");
		}
		list.keyColl.checkAsMap(keyIndex);

		this.list = list;
		this.keyIndex = keyIndex;
		this.immutable = immutable;
	}

	@Override
	public boolean equals(Object o) {
		// Copied from AbstractMap
		if (o == this) {
			return true;
		}
		if (!(o instanceof Map)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Map<K, E> m = (Map<K, E>) o;
		if (m.size() != size()) {
			return false;
		}
		try {
			Iterator<Entry<K, E>> i = entrySet().iterator();
			while (i.hasNext()) {
				Entry<K, E> e = i.next();
				K key = e.getKey();
				E value = e.getValue();
				if (value == null) {
					if (!(m.get(key) == null && m.containsKey(key)))
						return false;
				} else {
					if (!value.equals(m.get(key)))
						return false;
				}
			}
		} catch (ClassCastException unused) {
			return false;
		} catch (NullPointerException unused) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		if (coll != null) {
			return coll.hashCode();
		} else {
			return list.hashCode();
		}
	}

	@Override
	public String toString() {
		if (coll != null) {
			return coll.toString();
		} else {
			return list.toString();
		}
	}

	// Map: read methods

	@Override
	public int size() {
		if (coll != null) {
			return coll.size();
		} else {
			return list.size();
		}
	}

	@Override
	public boolean isEmpty() {
		if (coll != null) {
			return coll.isEmpty();
		} else {
			return list.isEmpty();
		}
	}

	@Override
	public boolean containsKey(Object key) {
		if (coll != null) {
			return coll.containsKey(keyIndex, key);
		} else {
			return list.containsKey(keyIndex, key);
		}
	}

	@Override
	public boolean containsValue(Object value) {
		if (coll != null) {
			return coll.contains(value);
		} else {
			return list.contains(value);
		}
	}

	@Override
	public E get(Object key) {
		if (coll != null) {
			return coll.getByKey(keyIndex, key);
		} else {
			return list.getByKey(keyIndex, key);
		}
	}

	// Map: iterator methods

	/**
	 * {@inheritDoc}
	 * <p><i>
	 * Note that the returned set is immutable.
	 * </i></p>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Set<K> keySet() {
		if (coll != null) {
			return new CollectionAsSet(coll.getDistinctKeys(keyIndex), true, false);
		} else {
			return new CollectionAsSet(list.getDistinctKeys(keyIndex), true, false);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p><i>
	 * Note that the returned set is immutable.
	 * </i></p>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Set<Entry<K, E>> entrySet() {
		if (coll != null) {
			Set<K> keys = (Set<K>) coll.getDistinctKeys(keyIndex);
			List<Entry<K, E>> entries = new GapList(keys.size());
			for (K key : keys) {
				E elem = coll.getByKey(keyIndex, key);
				entries.add(new ImmutableMapEntry<K, E>(key, elem));
			}
			return new CollectionAsSet(entries, true, false);
		} else {
			Set<K> keys = (Set<K>) list.getDistinctKeys(keyIndex);
			List<Entry<K, E>> entries = new GapList(keys.size());
			for (K key : keys) {
				E elem = list.getByKey(keyIndex, key);
				entries.add(new ImmutableMapEntry<K, E>(key, elem));
			}
			return new CollectionAsSet(entries, true, false);
		}
	}

	@Override
	public Collection<E> values() {
		if (coll != null) {
			if (immutable) {
				return Collections.unmodifiableCollection(coll);
			} else {
				return coll;
			}
		} else {
			if (immutable) {
				return Collections.unmodifiableCollection(list);
			} else {
				return list;
			}
		}
	}

	// Map: write methods

	void checkMutable() {
		if (immutable) {
			throw new UnsupportedOperationException("Map is immutable");
		}
	}

	@Override
	public void clear() {
		checkMutable();
		if (coll != null) {
			coll.clear();
		} else {
			list.clear();
		}
	}

	@Override
	public E put(K key, E elem) {
		checkMutable();
		if (coll != null) {
			if (!GapList.equalsElem(key, coll.getKey(keyIndex, elem))) {
				KeyCollectionImpl.errorInvalidData();
			}
			return coll.putByKey(keyIndex, elem, true);
		} else {
			if (!GapList.equalsElem(key, list.keyColl.getKey(keyIndex, elem))) {
				KeyCollectionImpl.errorInvalidData();
			}
			return list.putByKey(keyIndex, elem, true);
		}
	}

	@Override
	public E remove(Object key) {
		checkMutable();
		if (coll != null) {
			return coll.removeByKey(keyIndex, key);
		} else {
			return list.removeByKey(keyIndex, key);
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends E> map) {
		for (Entry<? extends K, ? extends E> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

}
