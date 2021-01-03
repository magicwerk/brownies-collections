package org.magicwerk.brownies.collections.sandbox;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.ImmutableDescriptor;

/**
 * A comparator which can handle null values.
 */
public class MapAsSet<K> implements Set<K> {
    private Map<K, K> map;
    private boolean immutable;

    public MapAsSet(Map<K, K> map, boolean immutable) {
        this.map = map;
        this.immutable = immutable;
    }

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public Object[] toArray() {
		return map.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return map.keySet().toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<K> iterator() {
		if (immutable) {
			return Collections.unmodifiableSet(map.keySet()).iterator();
		} else {
			return map.keySet().iterator();
		}
	}

	// -- write operations

    private void checkMutable() {
    	if (immutable) {
    		throw new UnsupportedOperationException("Set is immutable");
    	}
    }

	@Override
	public boolean add(K e) {
		checkMutable();
		if (map.containsKey(e)) {
			return false;
		}
		map.put(e, e);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends K> c) {
		checkMutable();
		boolean changed = false;
    	for (K e: c) {
    		if (add(e)) {
    			changed = true;
    		}
    	}
    	return changed;
	}

	@Override
	public void clear() {
		checkMutable();
		map.clear();
	}

	@Override
	public boolean remove(Object o) {
		checkMutable();
		if (!map.containsKey(o)) {
			return false;
		}
		map.remove(o);
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		checkMutable();
		boolean changed = false;
    	for (Object e: c) {
    		if (remove(e)) {
    			changed = true;
    		}
    	}
    	return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		checkMutable();
		boolean changed = false;
		for (Iterator<?> i = map.keySet().iterator(); i.hasNext(); ) {
			if (!c.contains(i.next())) {
				i.remove();
				changed = true;
			}
		}
        return changed;
	}

}