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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Implements a Map based on a KeyCollection key.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class KeyCollectionAsMap<E, K> implements Map<K, E> {
	KeyCollectionImpl<E> coll;
	int keyIndex;
	boolean immutable;

	public KeyCollectionAsMap(KeyCollectionImpl<E> coll, int keyIndex, boolean immutable) {
		if (coll == null) {
			throw new IllegalArgumentException("Collection may not be null");
		}
		this.coll = coll;
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
		return coll.hashCode();
	}

	@Override
	public String toString() {
		return coll.toString();
	}

	// Map: read methods

	@Override
	public int size() {
		return coll.size();
	}

	@Override
	public boolean isEmpty() {
		return coll.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return coll.containsKey(keyIndex, key);
	}

	@Override
	public boolean containsValue(Object value) {
		return coll.contains(value);
	}

	@Override
	public E get(Object key) {
		return coll.getByKey(keyIndex, key);
	}

	// Map: iterator methods

	/**
	 * {@inheritDoc}
	 * <p><i>
	 * Note that the returned set is immutable.
	 * </i></p>
	 */
	@Override
	public Set<K> keySet() {
		return new CollectionAsSet(coll.getDistinctKeys(keyIndex), true);
	}

	static class MapEntry<K, E> implements Entry<K, E> {
		K key;
		E elem;

		MapEntry(K key, E elem) {
			this.key = key;
			this.elem = elem;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public E getValue() {
			return elem;
		}

		@Override
		public E setValue(E value) {
			throw new AssertionError();
		}

		@Override
		public String toString() {
			return "MapEntry [key=" + key + ", elem=" + elem + "]";
		}

	}

	/**
	 * {@inheritDoc}
	 * <p><i>
	 * Note that the returned set is immutable.
	 * </i></p>
	 */
	@Override
	public Set<Entry<K, E>> entrySet() {
		List<K> keys = (List<K>) coll.getDistinctKeys(keyIndex);
		List<Entry<K, E>> entries = GapList.create(keys.size());
		for (K key : keys) {
			E elem = coll.getByKey(keyIndex, key);
			entries.add(new MapEntry<K, E>(key, elem));
		}
		return new CollectionAsSet(entries, true);
	}

	@Override
	public Collection<E> values() {
		if (immutable) {
			return Collections.unmodifiableCollection(coll);
		} else {
			return coll;
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
		coll.clear();
	}

	@Override
	public E put(K key, E elem) {
		checkMutable();
		if (!GapList.equalsElem(key, coll.getKey(keyIndex, elem))) {
			coll.errorInvalidData();
		}
		if (coll.containsKey(1, key)) {
			E oldElem = coll.removeByKey(keyIndex, key);
			coll.add(elem);
			return oldElem;
		} else {
			coll.add(elem);
			return null;
		}
	}

	@Override
	public E remove(Object key) {
		checkMutable();
		return coll.removeByKey(keyIndex, key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends E> map) {
		for (Entry<? extends K, ? extends E> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

}
