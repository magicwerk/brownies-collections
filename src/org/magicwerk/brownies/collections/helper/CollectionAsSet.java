package org.magicwerk.brownies.collections.helper;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Implements a Set based on a Collection.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class CollectionAsSet<K> implements Set<K> {
    Collection<K> coll;
    boolean immutable;

    public CollectionAsSet(Collection<K> coll, boolean immutable) {
    	if (coll == null) {
    		throw new IllegalArgumentException("Collection may not be null");
    	}
        this.coll = coll;
        this.immutable = immutable;
    }

    @Override
    public boolean equals(Object obj) {
    	return coll.equals(obj);
    }

    @Override
    public int hashCode() {
    	return coll.hashCode();
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
		return coll.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends K> c) {
		checkMutable();
		return coll.addAll(c);
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