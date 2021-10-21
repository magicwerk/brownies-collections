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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Implements a Set based on a Collection.
 * It supports both mutable and immutable sets.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class CollectionAsSet<K> implements Set<K> {
	Collection<K> coll;
	boolean immutable;

	public CollectionAsSet(Collection<K> coll, boolean immutable, boolean check) {
		if (coll == null) {
			throw new IllegalArgumentException("Collection may not be null");
		}
		if (check) {
			Set<K> set = new HashSet<>(coll);
			if (set.size() != coll.size()) {
				throw new IllegalArgumentException("Collection is not a set");
			}
		}
		this.coll = coll;
		this.immutable = immutable;
	}

	@Override
	public boolean equals(Object obj) {
		// like in AbstractSet.java
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Set)) {
			return false;
		}
		Collection<?> c = (Collection<?>) obj;
		if (c.size() != size()) {
			return false;
		}
		try {
			return containsAll(c);
		} catch (ClassCastException unused) {
			return false;
		} catch (NullPointerException unused) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		// like in AbstractSet.java
		int h = 0;
		Iterator<K> i = iterator();
		while (i.hasNext()) {
			K obj = i.next();
			if (obj != null) {
				h += obj.hashCode();
			}
		}
		return h;
	}

	@Override
	public String toString() {
		return coll.toString();
	}

	// Set: read methods

	@Override
	public int size() {
		return coll.size();
	}

	@Override
	public boolean isEmpty() {
		return coll.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return coll.contains(o);
	}

	@Override
	public Object[] toArray() {
		return coll.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return coll.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return coll.containsAll(c);
	}

	// Set: iterator methods

	@Override
	public Iterator<K> iterator() {
		if (immutable) {
			return Collections.unmodifiableCollection(coll).iterator();
		} else {
			return coll.iterator();
		}
	}

	// Set: write methods

	void checkMutable() {
		if (immutable) {
			throw new UnsupportedOperationException("Set is immutable");
		}
	}

	@Override
	public boolean add(K e) {
		checkMutable();
		if (coll.contains(e)) {
			return false;
		}
		return coll.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends K> c) {
		checkMutable();
		boolean changed = false;
		for (K e : c) {
			changed = add(e) || changed;
		}
		return changed;
	}

	@Override
	public void clear() {
		checkMutable();
		coll.clear();
	}

	@Override
	public boolean remove(Object o) {
		checkMutable();
		return coll.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		checkMutable();
		return coll.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		checkMutable();
		return coll.retainAll(c);
	}

}