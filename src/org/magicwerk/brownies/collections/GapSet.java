package org.magicwerk.brownies.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import org.magicwerk.brownies.collections.KeyList.KeyMap;
import org.magicwerk.brownies.collections.KeyList.NullMode;
import org.magicwerk.brownies.collections.SetList.Builder;

public class GapSet<E> implements Set<E> {

    static class AnyComparator<K> implements Comparator<K> {
        public static final AnyComparator INSTANCE = new AnyComparator();

        @Override
        public int compare(K key1, K key2) {
            if (key1 == key2) {
                return 0;
            }
            int hash1 = key1.hashCode();
            int hash2 = key2.hashCode();
            // Prevent overflow
            int cmp = (hash1 < hash2 ? -1 : (hash1 > hash2 ? 1 : 0));
            if (cmp != 0) {
                return cmp;
            }
            // Objects have same hashcode
            if (key1.equals(key2)) {
                return 0;
            }
            // Objects have same hashcode, but are not equal, so use identity hash code to distinguish them
            hash1 = System.identityHashCode(key1);
            hash2 = System.identityHashCode(key2);
            return (hash1 < hash2 ? -1 : (hash1 > hash2 ? 1 : 0));
        }
    }

    // Not sorted

    public static <E> GapSet<E> create(boolean comparable) {
        return new GapSet<E>(comparable).init();
    }

    public static <E> GapSet<E> create(boolean comparable, int capacity) {
        return new GapSet<E>(comparable).init(capacity);
    }

    public static <E> GapSet<E> create(boolean comparable, Collection<? extends E> elements) {
        return new GapSet<E>(comparable).init(elements);
    }

    public static <E> GapSet<E> create(boolean comparable, E... elements) {
        return new GapSet<E>(comparable).init(elements);
    }

    // Sorted - Natural comparator

    public static <E> GapSet<E> createSorted() {
        return new GapSet<E>(KeyList.getNaturalComparator()).init();
    }

    public static <E> GapSet<E> createSorted(int capacity) {
        return new GapSet<E>(KeyList.getNaturalComparator()).init(capacity);
    }

    public static <E> GapSet<E> createSorted(Collection<? extends E> elements) {
        return new GapSet<E>(KeyList.getNaturalComparator()).init(elements);
    }

    public static <E> GapSet<E> createSorted(E... elements) {
        return new GapSet<E>(KeyList.getNaturalComparator()).init(elements);
    }

    // Sorted Explicit comparator

    public static <E> GapSet<E> createSorted(Comparator<? super E> comparator) {
        return new GapSet<E>(comparator).init();
    }

    public static <E> GapSet<E> createSorted(Comparator<? super E> comparator, int capacity) {
        return new GapSet<E>(comparator).init(capacity);
    }

    public static <E> GapSet<E> createSorted(Comparator<? super E> comparator, Collection<? extends E> elements) {
        return new GapSet<E>(comparator).init(elements);
    }

    public static <E> GapSet<E> createSorted(Comparator<? super E> comparator, E... elements) {
        return new GapSet<E>(comparator).init(elements);
    }


    private Comparator<? super E> comparator;
    private GapList<E> list;

    private GapSet(boolean comparable) {
        this(comparable ? KeyList.getNaturalComparator() : AnyComparator.INSTANCE);
    }

    private GapSet(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    private GapSet<E> init() {
        list = new GapList<E>();
        return this;
    }

    private GapSet<E> init(int capacity) {
        list = new GapList<E>(capacity);
        return this;
    }

    private GapSet<E> init(Collection<? extends E> elements) {
        list = new GapList<E>(elements);
        return this;
    }

    private GapSet<E> init(E... elements) {
        list = new GapList<E>(elements);
        return this;
    }

    private int indexOf(E elem) {
        int index = SortedLists.binarySearchGet(list, elem, comparator);
        if (index >= 0) {
            return index;
        } else {
            return -1;
        }
    }

    @Override
    public boolean add(E elem) {
        int index = 0;
        if (!list.isEmpty()) {
            if (comparator.compare(elem, list.getLast()) > 0) {
                index = -list.size() - 1;
            } else if (comparator.compare(elem, list.getFirst()) < 0) {
                index = -1;
            }
        }
        if (index == 0) {
            index = SortedLists.binarySearchAdd(list, elem, comparator);
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
        list.clear();
    }

    @Override
    public boolean contains(Object o) {
        return indexOf((E) o) != -1;
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
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public boolean remove(Object o) {
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
    	boolean changed = false;
		for (Iterator<?> i = iterator(); i.hasNext(); ) {
			if (!c.contains(i.next())) {
				i.remove();
				changed = true;
			}
		}
        return changed;
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

}
