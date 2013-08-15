package org.magicwerk.brownies.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;

import org.magicwerk.brownies.collections.KeyList.KeyMap;
import org.magicwerk.brownies.collections.KeyList.NullMode;
import org.magicwerk.brownies.collections.SetList.Builder;
import org.magicwerk.brownies.collections.helper.AnyComparator;
import org.magicwerk.brownies.collections.helper.NaturalComparator;
import org.magicwerk.brownies.collections.helper.SortedLists;
import org.magicwerk.brownies.collections.primitive.BooleanObjGapList;
import org.magicwerk.brownies.collections.primitive.ByteObjGapList;
import org.magicwerk.brownies.collections.primitive.CharObjGapList;
import org.magicwerk.brownies.collections.primitive.DoubleObjGapList;
import org.magicwerk.brownies.collections.primitive.FloatObjGapList;
import org.magicwerk.brownies.collections.primitive.IntObjGapList;
import org.magicwerk.brownies.collections.primitive.LongObjGapList;
import org.magicwerk.brownies.collections.primitive.ShortObjGapList;

public class GapSet<E> implements NavigableSet<E> {

    /** Unmodifiable empty instance */
    @SuppressWarnings("rawtypes")
    private static final GapSet EMPTY = GapSet.create().unmodifiableSet();

    /**
     * @return unmodifiable empty instance
     */
    @SuppressWarnings("unchecked")
    public static <EE> GapSet<EE> EMPTY() {
        return EMPTY;
    }

    /**
     * An immutable version of a GapList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableGapSet<E> extends GapSet<E> {
    	public ImmutableGapSet(GapSet<E> set) {
			super(set.comparator, set.list);
		}

		@Override
		public boolean add(E elem) {
			error();
			return false;
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			error();
			return false;
		}

		@Override
		public void clear() {
			error();
		}

		@Override
		public boolean remove(Object o) {
			error();
			return false;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			error();
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			error();
			return false;
		}

		@Override
		public E pollFirst() {
			error();
			return null;
		}

		@Override
		public E pollLast() {
			error();
			return null;
		}

        private void error() {
            throw new UnsupportedOperationException("set is immutable");
        }
    }

    // Natural comparator

    public static <E> GapSet<E> create() {
        return new GapSet<E>(NaturalComparator.INSTANCE).init();
    }

    public static <E> GapSet<E> create(int capacity) {
        return new GapSet<E>(NaturalComparator.INSTANCE).init(capacity);
    }

    public static <E> GapSet<E> create(Collection<? extends E> elements) {
        return new GapSet<E>(NaturalComparator.INSTANCE).init(elements);
    }

    public static <E> GapSet<E> create(E... elements) {
        return new GapSet<E>(NaturalComparator.INSTANCE).init(elements);
    }

    // Explicit types

    public static <E> GapSet<E> create(Class<E> type) {
        return new GapSet<E>(type).init();
    }

    public static <E> GapSet<E> create(Class<E> type, int capacity) {
        return new GapSet<E>(type).init(capacity);
    }

    public static <E> GapSet<E> create(Class<E> type, Collection<? extends E> elements) {
        return new GapSet<E>(type).init(elements);
    }

    public static <E> GapSet<E> create(Class<E> type, E... elements) {
        return new GapSet<E>(type).init(elements);
    }

    // Explicit comparator

    public static <E> GapSet<E> create(Comparator<? super E> comparator) {
        return new GapSet<E>(comparator).init();
    }

    public static <E> GapSet<E> create(Comparator<? super E> comparator, int capacity) {
        return new GapSet<E>(comparator).init(capacity);
    }

    public static <E> GapSet<E> create(Comparator<? super E> comparator, Collection<? extends E> elements) {
        return new GapSet<E>(comparator).init(elements);
    }

    public static <E> GapSet<E> create(Comparator<? super E> comparator, E... elements) {
        return new GapSet<E>(comparator).init(elements);
    }


    private Comparator<? super E> comparator;
    private GapList<E> list;

    private GapSet(Comparator<? super E> comparator) {
        this.comparator = comparator;
        this.list = new GapList<E>(false, null);
    }

    private GapSet(Class<E> type) {
        this.comparator = null;
        this.list = (GapList<E>) createWrapperList(type);
    }

    private GapSet(Comparator<? super E> comparator, GapList<E> list) {
    	this.comparator = comparator;
    	this.list = list;
    }

    /**
     * Create a GapList wrapping a primitive GapList, e.g. IntObjGapList.
     *
     * @param type	primitive type for GapList
     * @return		create wrapping GapList
     * @throws 		IllegalArgumentException if no primitive type is specified
     */
    static GapList<?> createWrapperList(Class<?> type) {
    	if (type == int.class) {
    		return new IntObjGapList();
    	} else if (type == long.class) {
        	return new LongObjGapList();
    	} else if (type == double.class) {
        	return new DoubleObjGapList();
    	} else if (type == float.class) {
        	return new FloatObjGapList();
    	} else if (type == boolean.class) {
        	return new BooleanObjGapList();
    	} else if (type == byte.class) {
        	return new ByteObjGapList();
    	} else if (type == char.class) {
        	return new CharObjGapList();
    	} else if (type == short.class) {
        	return new ShortObjGapList();
    	} else {
    		throw new IllegalArgumentException("Wrapper type expected: " + type);
    	}
    }

    private GapSet<E> init() {
        list.init();
        return this;
    }

    private GapSet<E> init(int capacity) {
        list.init(capacity);
        return this;
    }

    private GapSet<E> init(Collection<? extends E> elements) {
        list.init(elements);
        return this;
    }

    private GapSet<E> init(E... elements) {
        list.init(elements);
        return this;
    }

    private int indexOf(E elem) {
    	int index = list.binarySearch(elem, comparator);
        if (index >= 0) {
            return index;
        } else {
            return -1;
        }
    }

    public GapList<E> unmodifiableList() {
    	return list.unmodifiableList();
    }

    public GapSet<E> unmodifiableSet() {
    	return new ImmutableGapSet<E>(this);
    }

    private int compare(E elem1, E elem2) {
    	if (comparator == null) {
    		return ((Comparable) elem1).compareTo(elem2);
    	} else {
    		return comparator.compare(elem1, elem2);
    	}
    }

    @Override
    public boolean add(E elem) {
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
		return new GapSet<E>(comparator, reverse).unmodifiableSet();
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
        	return EMPTY();
        }
		return new GapSet<E>(comparator, list.get(fromIndex, toIndex-fromIndex)).unmodifiableSet();
	}

	@Override
	public NavigableSet<E> headSet(E toElement, boolean inclusive) {
		return subSet(list.peekFirst(), true, toElement, inclusive);
	}

	@Override
	public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
		return subSet(fromElement, inclusive, list.peekLast(), true);
	}

}
