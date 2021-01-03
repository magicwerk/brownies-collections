package org.magicwerk.brownies.collections.sandbox;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;

import org.magicwerk.brownies.collections.GapList;

public class SortedListAsSet<E> implements NavigableSet<E> {

	private GapList<E> list;
    private Comparator<? super E> comparator;
    private boolean immutable;

    public SortedListAsSet(GapList<E> list, Comparator<? super E> comparator, boolean immutable) {
    	this.list = list;
    	this.comparator = comparator;
    	this.immutable = immutable;
    }

    private int compare(E elem1, E elem2) {
    	if (comparator == null) {
    		return ((Comparable) elem1).compareTo(elem2);
    	} else {
    		return comparator.compare(elem1, elem2);
    	}
    }

    // --- Write operations

    private void checkMutable() {
    	if (immutable) {
    		throw new UnsupportedOperationException("Set is immutable");
    	}
    }

    @Override
    public boolean add(E elem) {
    	checkMutable();
        int index = 0;
        if (!list.isEmpty()) {
            if (compare(elem, list.getLast()) > 0) {
                index = -list.size() - 1;
            } else if (compare(elem, list.getFirst()) < 0) {
                index = -1;
            }
        }
        if (index == 0) {
            index = list.binarySearch(elem, comparator);
        }
        if (index >= 0) {
            return false;
        } else {
            index = -index - 1;
            list.add(index, elem);
            return true;
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
    	checkMutable();
        boolean changed = false;
        for (E e: c) {
            if (add(e)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void clear() {
    	checkMutable();
        list.clear();
    }

    @Override
    public boolean remove(Object o) {
    	checkMutable();
        int index = indexOf((E) o);
        if (index == -1) {
            return false;
        } else {
            list.remove(index);
            return true;
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
    	checkMutable();
    	boolean changed = false;
    	if (c.size() < size()) {
    		for (Iterator<?> i = c.iterator(); i.hasNext(); ) {
    			if (remove(i.next())) {
    				changed = true;
    			}
    		}
    	} else {
    		for (Iterator<?> i = iterator(); i.hasNext(); ) {
    			if (c.contains(i.next())) {
    				i.remove();
    				changed = true;
    			}
    		}
    	}
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
    	checkMutable();
    	boolean changed = false;
		for (Iterator<?> i = iterator(); i.hasNext(); ) {
			if (!c.contains(i.next())) {
				i.remove();
				changed = true;
			}
		}
        return changed;
    }

    //--- Methods from Set

    @Override
    public Iterator<E> iterator() {
    	if (immutable) {
    		return list.unmodifiableList().iterator();
    	} else {
    		return list.iterator();
    	}
    }

    @Override
    public boolean contains(Object o) {
        return indexOf((E) o) != -1;
    }

    private int indexOf(E elem) {
    	int index = list.binarySearch(elem, comparator);
        if (index >= 0) {
            return index;
        } else {
            return -1;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e: c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    // Methods from SortedSet

	@Override
	public Comparator<? super E> comparator() {
		return comparator;
	}

	@Override
	public SortedSet<E> subSet(E fromElement, E toElement) {
		return subSet(fromElement, true, toElement, false);
	}

	@Override
	public SortedSet<E> headSet(E toElement) {
		return subSet(list.peekFirst(), true, toElement, false);
	}

	@Override
	public SortedSet<E> tailSet(E fromElement) {
		return subSet(fromElement, true, list.peekLast(), true);
	}

	@Override
	public E first() {
		return list.getFirst();
	}

	@Override
	public E last() {
		return list.getLast();
	}

	// Methods from NavigableSet

	@Override
	public E lower(E e) {
        int index = list.binarySearch(e, comparator);
        if (index >= 0) {
            index--;
        } else {
            index = -index-1;
        }
        if (index >= 0) {
        	return list.get(index);
        } else {
        	return null;
        }
	}

	@Override
	public E floor(E e) {
        int index = list.binarySearch(e, comparator);
        if (index >= 0) {
            ;
        } else {
            index = -index-1;
        }
        if (index >= 0) {
        	return list.get(index);
        } else {
        	return null;
        }
	}

	@Override
	public E ceiling(E e) {
        int index = list.binarySearch(e, comparator);
        if (index >= 0) {
            ;
        } else {
            index = -index-1;
        }
        if (index < list.size()) {
        	return list.get(index);
        } else {
        	return null;
        }
	}

	@Override
	public E higher(E e) {
        int index = list.binarySearch(e, comparator);
        if (index >= 0) {
            index++;
        } else {
            index = -index-1;
        }
        if (index < list.size()) {
        	return list.get(index);
        } else {
        	return null;
        }
	}

	@Override
	public E pollFirst() {
		return list.pollFirst();
	}

	@Override
	public E pollLast() {
		return list.pollLast();
	}

	@Override
	public NavigableSet<E> descendingSet() {
		GapList<E> reverse = list.copy();
		reverse.reverse();
		return new SortedListAsSet<E>(reverse.unmodifiableList(), comparator, immutable);
	}

	@Override
	public Iterator<E> descendingIterator() {
		return list.descendingIterator();
	}

	@Override
	public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int fromIndex = list.binarySearch(fromElement, comparator);
        if (fromIndex >= 0) {
        	if (!fromInclusive) {
        		fromIndex--;
        	}
        } else {
        	fromIndex = -fromIndex-1;
        }
        int toIndex = list.binarySearch(toElement, comparator);
        if (toIndex >= 0) {
        	if (toInclusive) {
        		toIndex++;
        	}
        } else {
        	toIndex = -toIndex-1;
        }
        if (fromIndex >= toIndex) {
        	return new SortedListAsSet<E>((GapList<E>) GapList.EMPTY(), comparator, immutable);
        }
		//return new SortedListAsSet<E>(list.getAll(fromIndex, toIndex-fromIndex).unmodifiableList(), comparator, immutable);
        return null; //FIXME
	}

	@Override
	public NavigableSet<E> headSet(E toElement, boolean inclusive) {
		return subSet(list.peekFirst(), true, toElement, inclusive);
	}

	@Override
	public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
		return subSet(fromElement, inclusive, list.peekLast(), true);
	}

	@Override
    public boolean equals(Object obj) {
    	if (obj == this) {
    		return true;
    	}
    	if (!(obj instanceof Set)) {
    		return false;
		}
		Set<?> set = (Set<?>) obj;
		if (set.size() != size()) {
			return false;
		}
        try {
            return containsAll(set);
        } catch (ClassCastException unused)   {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    @Override
    public int hashCode() {
    	return list.hashCode();
    }
}
