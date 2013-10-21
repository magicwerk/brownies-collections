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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * GapList combines the strengths of both ArrayList and LinkedList.
 * It is implemented to offer both efficient random access to elements
 * by index (as ArrayList does) and at the same time efficient adding
 * and removing elements to and from beginning and end (as LinkedList does).
 * It also exploits the locality of reference often seen in applications
 * to further improve performance, e.g. for iterating over the list.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * </p>
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @param <E> type of elements stored in the list
 * @see	    java.util.List
 * @see	    java.util.ArrayList
 * @see	    java.util.LinkedList
 */
public class GapList<E> extends AbstractList<E>
	implements
		// All interfaces of ArrayList
		List<E>, RandomAccess, Cloneable, Serializable,
		// Additional interfaces of LinkedList
		Deque<E> {


    /*
     * Helper variables to enable code for debugging.
     * As the variables are declared as "static final boolean", the compiler
     * will be able to detect unused branches and will not compile the
     * code (the same approach is used for the assert statement).
     */
    /** If true the invariants the GapList are checked for debugging */
    private static final boolean DEBUG_CHECK = false;
    /** If true the calls to some methods are traced out for debugging */
    private static final boolean DEBUG_TRACE = false;
    /** If true the internal state of the GapList is traced out for debugging */
    private static final boolean DEBUG_DUMP = false;


    // -- EMPTY --

    // Cannot make a static reference to the non-static type E:
    // public static GapList<E> EMPTY = GapList.create().unmodifiableList();
    // Syntax error:
    // public static <EE> GapList<EE> EMPTY = GapList.create().unmodifiableList();

    /** Unmodifiable empty instance */
    @SuppressWarnings("rawtypes")
    private static final GapList EMPTY = GapList.create().unmodifiableList();

    /**
     * @return unmodifiable empty instance
     */
    @SuppressWarnings("unchecked")
    public static <EE> GapList<EE> EMPTY() {
        return EMPTY;
    }


    /**
     * An immutable version of a GapList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableGapList<E> extends GapList<E> {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
        protected ImmutableGapList(GapList<E> that) {
            super(true, that);
        }

        @Override
        protected boolean doAdd(int index, E elem) {
        	error();
        	return false;
        }

        @Override
        protected boolean doAddAll(int index, E[] elems) {
        	error();
        	return false;
        }

        @Override
        protected E doSet(int index, E elem) {
        	error();
        	return null;
        }

        @Override
        protected void doSetAll(int index, E[] elems) {
        	error();
        }

        @Override
        protected E doReSet(int index, E elem) {
        	error();
        	return null;
        }

        @Override
        protected E doRemove(int index) {
        	error();
        	return null;
        }

        @Override
        protected void doRemoveAll(int index, int len) {
        	error();
        }

        @Override
        protected void doModify() {
        	error();
        }

        /**
         * Throw exception if an attempt is made to change an immutable list.
         */
        private void error() {
            throw new UnsupportedOperationException("list is immutable");
        }
    };

    /** UID for serialization */
    private static final long serialVersionUID = -4477005565661968383L;

    /** Default capacity for list */
    static final int DEFAULT_CAPACITY = 10;

	/** Array holding raw data */
	private E[] values;
	/** Number of elements stored in this GapList */
	private int size;
	/** Physical position of first element */
	private int start;
	/** Physical position after last element */
	private int end;
	/** Size of gap (0 if there is no gap) */
	private int gapSize;
	/** Logical index of first element after gap (ignored if gapSize=0) */
	private int gapIndex;
	/** Physical position of first slot in gap (ignored if gapSize=0) */
	private int gapStart;


	// --- Static methods ---

    /**
     * Create new list.
     *
     * @return          created list
     * @param <E>       type of elements stored in the list
     */
    // This separate method is needed as the varargs variant creates the GapList with specific size
    public static <E> GapList<E> create() {
        return new GapList<E>();
    }

    /**
     * Create new list with specified capacity.
     *
     * @param capacity  capacity
     * @return          created list
     * @param <E>       type of elements stored in the list
     */
    public static <E> GapList<E> create(int capacity) {
        return new GapList<E>(capacity);
    }

    /**
     * Create new list with specified elements.
     *
     * @param coll      collection with element
     * @return          created list
     * @param <E>       type of elements stored in the list
     */
	public static <E> GapList<E> create(Collection<? extends E> coll) {
		return new GapList<E>(coll);
	}

	/**
	 * Create new list with specified elements.
	 *
	 * @param elems 	array with elements
	 * @return 			created list
	 * @param <E> 		type of elements stored in the list
	 */
	public static <E> GapList<E> create(E... elems) {
		return new GapList<E>(elems);
	}

	/**
	 * Calculate index for physical access to an element.
	 *
	 * @param idx	logical index of element
	 * @return		physical index to access element in values[]
	 */
	private final int physIndex(int idx) {
		int physIdx = idx+start;
		if (idx >= gapIndex) {
			physIdx += gapSize;
		}
		if (physIdx >= values.length) {
			physIdx -= values.length;
		}
		return physIdx;
	}

	/**
	 * Calculate indexes for physical access to a range of elements.
	 * The method returns between one and three ranges of physical indexes.
	 *
	 * @param idx0  start index
	 * @param idx1	end index
	 * @return		array with physical start and end indexes (may contain 2, 4, or 6 elements)
	 */
	private int[] physIndex(int idx0, int idx1) {
		assert(idx0 >=0 && idx1 <= size && idx0 <= idx1);

		if (idx0 == idx1) {
			return new int[0];
		}

		// Decrement idx1 to make sure we get the physical index
		// of an existing position. We will increment the physical index
		// again before returning.
		idx1--;
		int pidx0 = physIndex(idx0);
		if (idx1 == idx0) {
			return new int[] {
				pidx0, pidx0+1
			};
		}

		int pidx1 = physIndex(idx1);
		if (pidx0 < pidx1) {
			if (gapSize > 0 && pidx0 < gapStart && pidx1 > gapStart) {
				assert(pidx0 < gapStart);
				assert(gapStart+gapSize < pidx1+1);

				return new int[] {
					pidx0, gapStart,
					gapStart+gapSize, pidx1+1
				};
			} else {
				return new int[] {
					pidx0, pidx1+1
				};
			}
		} else {
			assert(pidx0 > pidx1);
			assert(start != 0);
			if (gapSize > 0 && pidx1 > gapStart && gapStart > 0) {
				assert(pidx0 < values.length);
				assert(0 < gapStart);
				assert(gapStart+gapSize < pidx1+1);

				return new int[] {
					pidx0, values.length,
					0, gapStart,
					gapStart+gapSize, pidx1+1
				};
			} else if (gapSize > 0 && pidx0 < gapStart && gapStart+gapSize < values.length) {
				assert(pidx0 < gapStart);
				assert(gapStart+gapSize < values.length);
				assert(0 < pidx1+1);

				return new int[] {
					pidx0, gapStart,
					gapStart+gapSize, values.length,
					0, pidx1+1
				};
			} else {
				assert(pidx0 < values.length);
				assert(0 < pidx1+1);

				int end = values.length;
				if (gapSize > 0 && gapStart > pidx0) {
					end = gapStart;
				}
				int start = 0;
				if (gapSize > 0 &&  (gapStart+gapSize)%values.length < pidx1+1) {
					start = (gapStart+gapSize)%values.length;
				}

				return new int[] {
					pidx0, end,
					start, pidx1+1
				};
			}
		}
	}

    /**
     * Constructor used internally, e.g. for ImmutableGapList.
     *
     * @param copy true to copy all instance values from source,
     *             if false nothing is done
     * @param that list to copy
     */
    protected GapList(boolean copy, GapList<E> that) {
        if (copy) {
            this.values = that.values;
            this.size = that.size;
            this.start = that.start;
            this.end = that.end;
            this.gapSize = that.gapSize;
            this.gapIndex = that.gapIndex;
            this.gapStart = that.gapStart;
        }
    }

	/**
	 * Default constructor.
	 */
	public GapList() {
		init();
	}

	/**
	 * Construct a list with specified capacity.
	 *
	 * @param capacity	capacity to use
	 */
	public GapList(int capacity) {
		init(capacity);
	}

	/**
	 * Copy constructor.
	 *
	 * @param that	source object to copy
	 */
	public GapList(Collection<? extends E> that) {
		init(that);
	}

	/**
	 * Copy constructor.
	 *
	 * @param that	source object to copy
	 */
	public GapList(E... that) {
		init(that);
	}

	/**
	 * Initialize the list to be empty.
	 */
	public void init() {
		init(new Object[DEFAULT_CAPACITY], 0);
	}

	/**
     * Initialize the list to be empty with specified capacity.
     *
	 * @param capacity capacity
	 */
	public void init(int capacity) {
		init(new Object[capacity], 0);
	}

	/**
	 * Initialize the list to contain the specified elements only.
	 *
	 * @param coll collection with elements
	 */
	public void init(Collection<? extends E> coll) {
		Object[] array = toArray(coll);
		init(array, array.length);
	}

	/**
     * Initialize the list to contain the specified elements only.
     *
	 * @param elems array with elements
	 */
	public void init(E... elems) {
		Object[] array = elems.clone();
		init(array, array.length);
	}

	/**
	 * Copies the collection values into an array.
	 *
	 * @param coll   collection of values
	 * @return       array containing the collection values
	 */
	static Object[] toArray(Collection<?> coll) {
    	Object[] values = coll.toArray();
    	// toArray() might (incorrectly) not return Object[] (see bug 6260652)
    	if (values.getClass() != Object[].class) {
    		values = Arrays.copyOf(values, values.length, Object[].class);
	    }
    	return values;
	}

    /**
     * Returns a shallow copy of this <tt>GapList</tt> instance
     * (the new list will contain the same elements as the source list, i.e. the elements themselves are not copied).
     * This method is identical to clone() except that the result is casted to GapList.
     *
     * @return a clone of this instance
     * @see #clone
     */
	@SuppressWarnings("unchecked")
    public GapList<E> copy() {
	    return (GapList<E>) clone();
	}

    /**
     * Returns an unmodifiable view of this list. This method allows
     * modules to provide users with "read-only" access to internal lists.
     * Query operations on the returned list "read through" to the specified
     * list, and attempts to modify the returned list, whether direct or
     * via its iterator, result in an UnsupportedOperationException.
     *
     * @return an unmodifiable view of the specified list
     */
    public GapList<E> unmodifiableList() {
        // Naming as in java.util.Collections#unmodifiableList
        return new ImmutableGapList<E>(this);
    }

    /**
     * Returns a shallow copy of this <tt>GapList</tt> instance
     * (The elements themselves are not copied).
     * The capacity of the list will be set to the number of elements,
     * so after calling clone(), size and capacity are equal.
     *
     * @return a clone of this <tt>GapList</tt> instance
     */
	@SuppressWarnings("unchecked")
    @Override
    public Object clone() {
		try {
			GapList<E> list = (GapList<E>) super.clone();
			// Do not simply clone the array, but make sure its capacity
			// is equal to the size (as in ArrayList)
		    list.init(toArray(), size());
			if (DEBUG_CHECK) list.debugCheck();
		    return list;
		}
		catch (CloneNotSupportedException e) {
		    // This shouldn't happen, since we are Cloneable
		    throw new AssertionError(e);
		}
    }

	/**
	 * Normalize data of GapList so the elements are found
	 * from values[0] to values[size-1].
	 * This method can help to speed up operations like sort or
	 * binarySearch.
	 */
	private void normalize() {
		if (start == 0 && end == 0 && gapSize == 0 && gapStart == 0 && gapIndex == 0) {
			return;
		}
		init(toArray(), size());
	}

	/**
	 * Initialize all instance fields.
	 *
	 * @param values	new values array
	 * @param size		new size
	 */
	@SuppressWarnings("unchecked")
	void init(Object[] values, int size) {
		this.values = (E[]) values;
		this.size = size;

		start = 0;
		end = 0;
		gapSize = 0;
		gapStart = 0;
		gapIndex = 0;

		if (DEBUG_CHECK) debugCheck();
	}

	@Override
	public void clear() {
		doRemoveAll(0, size());
	}

	@Override
	public int size() {
		return size;
	}

	/**
	 * Returns capacity of this GapList.
	 * Note that two GapLists are considered equal even if they have a distinct capacity.
	 * Also the capacity can be changed by operations like clone() etc.
	 *
	 * @return capacity of this GapList
	 */
	public int capacity() {
		return values.length;
	}

    @Override
    public E get(int index) {
        // A note about the inlining capabilities of the Java HotSpot Performance Engine
        // (http://java.sun.com/developer/technicalArticles/Networking/HotSpot/inlining.html)
        // The JVM seems not able to inline the methods called within
        // this method, irrespective whether they are "private final" or not.
        // Also -XX:+AggressiveOpts seems not to help.
        // We therefore do inlining manually.

        // INLINE: checkIndex(index);
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Invalid index: " + index + " (size: " + size() + ")");
        }
        return doGet(index);
    }

    /**
     * Helper method for getting an element from the GapList.
     * This is the only method which really gets an element.
     * Override if you need to validity checks before getting.
     *
     * @param index index of element to return
     * @return      the element at the specified position in this list
     */
    protected E doGet(int index) {
        assert (index >= 0 && index < size);

        // INLINE: return values[physIndex(index)];
        int physIdx = index+start;
        if (index >= gapIndex) {
            physIdx += gapSize;
        }
        if (physIdx >= values.length) {
            physIdx -= values.length;
        }
        return values[physIdx];
    }

	@Override
	public E set(int index, E elem) {
		checkIndex(index);

		return doSet(index, elem);
	}

    /**
     * Helper method for setting an element in the GapList.
     * This is the only method which really sets an element.
     * Override if you need to validity checks before setting.
     *
     * @param index index where element will be placed
     * @param elem  element to set
     * @return      old element which was at the position
     */
    protected E doSet(int index, E elem) {
        assert (index >= 0 && index < size);

        int physIdx = physIndex(index);
        E oldElem = values[physIdx];
        values[physIdx] = elem;
        return oldElem;
    }

    /**
     * Sets an element at specified position.
     * This method is used internally if existing elements will be moved etc.
     * Override if you need to validity checks.
     *
     * @param index index where element will be placed
     * @param elem  element to set
     * @return      old element which was at the position
     */
    protected E doReSet(int index, E elem) {
        assert (index >= 0 && index < size);

        int physIdx = physIndex(index);
        E oldElem = values[physIdx];
        values[physIdx] = elem;
        return oldElem;
    }

    /**
     * This method is called internally before elements are allocated or freed.
     * Override if you need to validity checks.
     */
    protected void doModify() {
    }

    @Override
	public boolean add(E elem) {
		if (DEBUG_TRACE) {
			debugLog("add: " + elem);
			if (DEBUG_DUMP) debugDump();
		}
		return doAdd(-1, elem);
	}

	@Override
	public void add(int index, E elem)	{
		if (DEBUG_TRACE) {
			debugLog("add: " + index + ", " + elem);
			if (DEBUG_DUMP) debugDump();
		}
		checkIndexAdd(index);
		doAdd(index, elem);
	}

	/**
	 * Helper method for adding an element to the GapList.
	 * This is the only method which really adds an element.
	 * Override if you need to validity checks before adding.
	 *
	 * @param index	index where element should be added
	 *              (-1 means it is up to the implementation to choose the index)
	 * @param elem	element to add
	 * @return      true if element has been added (GapList.add() will always return true)
	 */
	protected boolean doAdd(int index, E elem) {
        ensureCapacity(size() + 1);

		if (index == -1) {
		    index = size;
		}
        assert(index >= 0 && index <= size);

        int physIdx;
		// Add at last position
		if (index == size && (end != start || size == 0)) {
			if (DEBUG_TRACE) debugLog("Case A0");
			physIdx = end;
			end++;
			if (end >= values.length) {
				end -= values.length;
			}

		// Add at first position
		} else if (index == 0 && (end != start || size == 0)) {
			if (DEBUG_TRACE) debugLog("Case A1");
			start--;
			if (start < 0) {
				start += values.length;
			}
			physIdx = start;
			if (gapSize > 0) {
				gapIndex++;
			}

		// Shrink gap
		} else if (gapSize > 0 && index == gapIndex) {
			if (DEBUG_TRACE) debugLog("Case A2");
			physIdx = gapStart+gapSize-1;
			if (physIdx >= values.length) {
				physIdx -= values.length;
			}
			gapSize--;

		// Add at other positions
		} else {
			physIdx = physIndex(index);

			if (gapSize == 0) {
				// Create new gap
				if (start < end && start > 0) {
					// S4: Space is at head and tail
					assert(debugState() == 4);
					int len1 = physIdx-start;
					int len2 = end-physIdx;
					if (len1 <= len2) {
						if (DEBUG_TRACE) debugLog("Case A3");
						moveData(start, 0, len1);
						gapSize = start-1;
						gapStart = len1;
						gapIndex = len1;
						start = 0;
						physIdx--;
					} else {
						if (DEBUG_TRACE) debugLog("Case A4");
						moveData(physIdx, values.length-len2, len2);
						gapSize = values.length-end-1;
						gapStart = physIdx+1;
						gapIndex = index+1;
						end = 0;
					}

				} else if (physIdx < end) {
					assert(debugState() == 2 || debugState() == 5);
					if (DEBUG_TRACE) debugLog("Case A5");
					int len = end-physIdx;
					int rightSize = (start-end+values.length)%values.length;
					moveData(physIdx, end+rightSize-len, len);
					end = start;
					gapSize = rightSize-1;
					gapStart = physIdx+1;
					gapIndex = index+1;

				} else {
					assert(debugState() == 3 || debugState() == 5);
					assert(physIdx > end);
					if (DEBUG_TRACE) debugLog("Case A6");
					int len = physIdx-start;
					int rightSize = start-end;
					moveData(start, end, len);
					start -= rightSize;
					end = start;
					gapSize = rightSize-1;
					gapStart = start+len;
					gapIndex = index;
					physIdx--;
				}
			} else {
				// Move existing gap
				boolean moveLeft;
				int gapEnd = (gapStart+gapSize-1) % values.length + 1;
				if (gapEnd < gapStart) {
					assert(debugState() == 9 || debugState() == 12);
					// Gap is at head and tail
					int len1 = physIdx-gapEnd;
					int len2 = gapStart-physIdx-1;
					if (len1 <= len2) {
						if (DEBUG_TRACE) debugLog("Case A7a");
						moveLeft = true;
					} else {
						if (DEBUG_TRACE) debugLog("Case A8a");
						moveLeft = false;
					}

				} else {
					assert(debugState() == 6 || debugState() == 7 || debugState() == 8 || debugState() == 9 || debugState() == 10 ||
						debugState() == 11 || debugState() == 12 || debugState() == 13 || debugState() == 14 || debugState() == 15);
					if (physIdx > gapStart) {
						if (DEBUG_TRACE) debugLog("Case A7b");
						moveLeft = true;
					} else  {
						if (DEBUG_TRACE) debugLog("Case A8b");
						moveLeft = false;
					}
				}
				if (moveLeft) {
					int src = gapStart+gapSize;
					int dst = gapStart;
					int len = physIdx-gapEnd;
					moveGap(src, dst, len);
					physIdx--;
					gapSize--;
					gapIndex = index;
					gapStart += len;
					if (gapStart >= values.length) {
						gapStart -= values.length;
					}

					if (index == 0) {
						start = physIdx;
						if ((gapStart + gapSize) % values.length == start) {
							end = gapStart;
							gapSize = 0;
						}
					}
				} else {
					int src = physIdx;
					int dst = physIdx+gapSize;
					int len = gapStart-physIdx;
					moveGap(src, dst, len);
					gapSize--;
					gapStart = physIdx+1;
					gapIndex = index+1;

					if (index == 0) {
						start = physIdx;
						end = physIdx;
					} else if (index == size) {
						if ((gapStart + gapSize) % values.length == start) {
							end = gapStart;
							gapSize = 0;
						}
					}

				}
			}
		}

		values[physIdx] = elem;
		size++;

		if (DEBUG_DUMP) debugDump();
		if (DEBUG_CHECK) debugCheck();

		return true;
	}

	/**
	 * Move a range of elements in the values array.
	 * The elements are first copied and the source range is then
	 * filled with null.
	 *
	 * @param src	start index of source range
	 * @param dst	start index of destination range
	 * @param len	number of elements to move
	 */
	private void moveGap(int src, int dst, int len) {
		if (DEBUG_TRACE) {
			debugLog("moveGap: " + src + "-" + src+len + " -> " + dst + "-" + dst+len);
		}

		if (src > values.length) {
			src -= values.length;
		}
		if (dst > values.length) {
			dst -= values.length;
		}
		assert(len >= 0);
		assert(src+len <= values.length);

		if (start >= src && start < src+len) {
			start += dst-src;
			if (start >= values.length) {
				start -= values.length;
			}
		}
		if (end >= src && end < src+len) {
			end += dst-src;
			if (end >= values.length) {
				end -= values.length;
			}
		}
		if (dst+len <= values.length) {
			moveData(src, dst, len);
		} else {
			// Destination range overlaps end of range so do the
			// move in two calls
			int len2 = dst+len - values.length;
			int len1 = len - len2;
			if (!(src <= len2 && len2 < dst)) {
				moveData(src+len1, 0, len2);
				moveData(src, dst, len1);
			} else {
				moveData(src, dst, len1);
				moveData(src+len1, 0, len2);
			}
		}
	}

	/**
	 * Move a range of elements in the values array.
	 * The elements are first copied and the source range is then
	 * filled with null.
	 *
	 * @param src	start index of source range
	 * @param dst	start index of destination range
	 * @param len	number of elements to move
	 */
	private void moveData(int src, int dst, int len) {
		if (DEBUG_TRACE) {
			debugLog("moveData: " + src + "-" + src+len + " -> " + dst + "-" + dst+len);
			if (DEBUG_DUMP) {
				debugLog(debugPrint(values));
			}
		}
		System.arraycopy(values, src, values, dst, len);

		// Write null into array slots which are not used anymore
		// This is necessary to allow GC to reclaim non used objects.
		if (src <= dst) {
			int start = src;
			int end = (dst < src+len) ? dst : src+len;
			assert(end-start <= len);
			for (int i=start; i<end; i++) {
				values[i] = null;
			}
		} else {
			int start = (src > dst+len) ? src : dst+len;
			int end = src+len;
			assert(end-start <= len);
			for (int i=start; i<end; i++) {
				values[i] = null;
			}
		}

		if (DEBUG_TRACE) {
			if (DEBUG_DUMP) {
				debugLog(debugPrint(values));
			}
		}
	}

	@Override
	public E remove(int index) {
		checkIndex(index);

		if (DEBUG_TRACE) {
			debugLog("remove: " + index);
			if (DEBUG_DUMP) debugDump();
		}
		return doRemove(index);
	}

	/**
	 * Helper method to remove an element.
     * This is the only method which really removes an element.
     * Override if you need to validity checks before removing.
	 *
	 * @param index	index of element to remove
	 * @return		removed element
	 */
	protected E doRemove(int index) {
		int physIdx;

		// Remove at last position
		if (index == size-1) {
			if (DEBUG_TRACE) debugLog("Case R0");

			end--;
			if (end < 0) {
				end += values.length;
			}
			physIdx = end;

			// Remove gap if it is followed by only one element
			if (gapSize > 0) {
				if (gapIndex == index) {
					end = gapStart;
					gapSize = 0;
				}
			}

		// Remove at first position
		} else if (index == 0) {
			if (DEBUG_TRACE) debugLog("Case R1");

			physIdx = start;
			start++;
			if (start >= values.length) {
				start -= values.length;
			}

			// Remove gap if if it is preceded by only one element
			if (gapSize > 0) {
				if (gapIndex == 1) {
					start += gapSize;
					if (start >= values.length) {
						start -= values.length;
					}
					gapSize = 0;
				} else {
					gapIndex--;
				}
			}

		} else {
			// Remove in middle of list
			physIdx = physIndex(index);

			// Create gap
			if (gapSize == 0) {
				if (DEBUG_TRACE) debugLog("Case R2");
				gapIndex = index;
				gapStart = physIdx;
				gapSize = 1;

			// Extend existing gap at tail
			} else if (index == gapIndex) {
				if (DEBUG_TRACE) debugLog("Case R3");
				gapSize++;

			// Extend existing gap at head
			} else if (index == gapIndex-1) {
				if (DEBUG_TRACE) debugLog("Case R4");
				gapStart--;
				if (gapStart < 0) {
					gapStart += values.length;
				}
				gapSize++;
				gapIndex--;

			} else {
				// Move existing gap
				assert(gapSize > 0);

				boolean moveLeft;
				int gapEnd = (gapStart+gapSize-1) % values.length + 1;
				if (gapEnd < gapStart) {
					// Gap is at head and tail: check where fewer
					// elements must be moved
					int len1 = physIdx-gapEnd;
					int len2 = gapStart-physIdx-1;
					if (len1 <= len2) {
						if (DEBUG_TRACE) debugLog("Case R5a");
						moveLeft = true;
					} else {
						if (DEBUG_TRACE) debugLog("Case R6a");
						moveLeft = false;
					}

				} else {
					if (physIdx > gapStart) {
						// Existing gap is left of insertion point
						if (DEBUG_TRACE) debugLog("Case R5b");
						moveLeft = true;
					} else  {
						// Existing gap is right of insertion point
						if (DEBUG_TRACE) debugLog("Case R6b");
						moveLeft = false;
					}
				}
				if (moveLeft) {
					int src = gapStart+gapSize;
					int dst = gapStart;
					int len = physIdx-gapEnd;
					moveGap(src, dst, len);
					gapStart += len;
					if (gapStart >= values.length) {
						gapStart -= values.length;
					}
					gapSize++;

				} else {
					int src = physIdx+1;
					int dst = physIdx+gapSize+1;
					int len = gapStart-physIdx-1;
					moveGap(src, dst, len);
					gapStart = physIdx;
					gapSize++;
				}
				gapIndex = index;
			}
		}

		E removed = values[physIdx];
		values[physIdx] = null;
		size--;

		if (DEBUG_DUMP) debugDump();
		if (DEBUG_CHECK) debugCheck();
		return removed;
	}

    /**
     * Increases the capacity of this <tt>GapList</tt> instance, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity
     */
	// Note: Provide this method to make transition from ArrayList as
	//       smooth as possible
    @SuppressWarnings("unchecked")
    public void ensureCapacity(int minCapacity) {
		if (DEBUG_TRACE) debugLog("ensureCapacity: " + minCapacity);

		doModify();

		// Note: Same behavior as in ArrayList.ensureCapacity()
		int oldCapacity = values.length;
		if (minCapacity <= oldCapacity) {
			return;	// do not shrink
		}
	    int newCapacity = (oldCapacity*3)/2 + 1;
	    if (newCapacity < minCapacity) {
	    	newCapacity = minCapacity;
    	}

		E[] newValues = null;
		if (start == 0) {
			newValues = Arrays.copyOf(values, newCapacity);
		} else if (start > 0) {
			int grow = newCapacity-values.length;
			newValues = (E []) new Object[newCapacity];
			System.arraycopy(values, 0, newValues, 0, start);
			System.arraycopy(values, start, newValues, start+grow, values.length-start);
			if (gapStart > start && gapSize > 0) {
				gapStart += grow;
			}
			start += grow;
		}
		if (end == 0 && size != 0) {
			end = values.length;
		}
		values = newValues;

		if (DEBUG_DUMP) debugDump();
		if (DEBUG_CHECK) debugCheck();
	}

    /**
     * Trims the capacity of this <tt>GapList</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>GapList</tt> instance.
     */
	// Note: Provide this method to make transition from ArrayList as
	//       smooth as possible
    @SuppressWarnings("unchecked")
	public void trimToSize() {
        doModify();

    	if (size < values.length) {
    		init(toArray(), size);
    	}
    }

    @Override
    public boolean equals(Object obj) {
    	if (obj == this) {
    		return true;
    	}
    	if (!(obj instanceof List<?>)) {
    		return false;
    	}
    	@SuppressWarnings("unchecked")
		List<E> list = (List<E>) obj;
    	int size = size();
    	if (size != list.size()) {
    		return false;
    	}
    	for (int i=0; i<size; i++) {
    		if (!equalsElem(doGet(i), list.get(i))) {
    			return false;
    		}
    	}
    	return true;
    }

    @Override
    public int hashCode() {
    	int hashCode = 1;
    	int size = size();
    	for (int i=0; i<size; i++) {
    		E elem = doGet(i);
    		hashCode = 31*hashCode + hashCodeElem(elem);
    	}
    	return hashCode;
    }

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		int size = size();
		for (int i=0; i<size; i++) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(doGet(i));
		}
		buf.append("]");
		return buf.toString();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Helper function to check two elements stored in the GapList
	 * for equality.
	 *
	 * @param elem1	first element
	 * @param elem2	second element
	 * @return		true if the elements are equal, otherwise false
	 */
	static boolean equalsElem(Object elem1, Object elem2) {
		if (elem1 == null) {
			if (elem2 == null) {
				return true;
			}
		} else {
			if (elem1.equals(elem2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Helper method to calculate hash code of a element stored in
	 * the GapList.
	 *
	 * @param elem	element
	 * @return		hash code for element
	 */
	static int hashCodeElem(Object elem) {
	    if (elem == null) {
	        return 0;
	    } else {
	        return elem.hashCode();
	    }
	}

	@Override
	public int indexOf(Object elem) {
		int size = size();
		for (int i=0; i<size; i++) {
			if (equalsElem(doGet(i), elem)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object elem) {
		for (int i=size-1; i>=0; i--) {
			if (equalsElem(doGet(i), elem)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean remove(Object elem) {
		int index = indexOf(elem);
		if (index == -1) {
			return false;
		}
		doRemove(index);
		return true;
	}

	@Override
	public boolean contains(Object elem) {
		return indexOf(elem) != -1;
	}

	/**
	 * Returns true if any of the elements of the specified collection is contained in the list.
	 *
	 * @param coll collection with elements to be contained
	 * @return     true if any element is contained, false otherwise
	 */
	public boolean containsAny(Collection<?> coll) {
	    // Note that the signature has been chosen as in List:
	    // - boolean addAll(Collection<? extends E> c);
	    // - boolean containsAll(Collection<?> c);
	    for (Object elem: coll) {
	        if (contains(elem)) {
	            return true;
	        }
	    }
	    return false;
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
	    // Note that this method is already implemented in AbstractCollection.
		// It has been duplicated so the method is also available in the primitive classes.
	    for (Object elem: coll) {
	        if (!contains(elem)) {
	            return false;
	        }
	    }
	    return true;
	}

	@Override
    public boolean removeAll(Collection<?> coll) {
	    // Note that this method is already implemented in AbstractCollection.
		// It has been duplicated so the method is also available in the primitive classes.
	    boolean modified = false;
	    int size = size();
		for (int i=0; i<size; i++) {
			if (coll.contains(doGet(i))) {
				doRemove(i);
				size--;
				i--;
				modified = true;
			}
		}
		return modified;
    }

    /**
     * @see #removeAll(Collection)
     */
    public boolean removeAll(GapList<?> coll) {
    	// There is a special implementation accepting a GapList
    	// so the method is also available in the primitive classes.
	    boolean modified = false;
	    int size = size();
		for (int i=0; i<size; i++) {
			if (coll.contains(doGet(i))) {
				doRemove(i);
				size--;
				i--;
				modified = true;
			}
		}
		return modified;
    }

	@Override
    public boolean retainAll(Collection<?> coll) {
	    // Note that this method is already implemented in AbstractCollection.
		// It has been duplicated so the method is also available in the primitive classes.
	    boolean modified = false;
	    int size = size();
		for (int i=0; i<size; i++) {
			if (!coll.contains(doGet(i))) {
				doRemove(i);
				size--;
				i--;
				modified = true;
			}
		}
		return modified;
    }

    /**
     * @see #retainAll(Collection)
     */
    public boolean retainAll(GapList<?> coll) {
    	// There is a special implementation accepting a GapList
    	// so the method is also available in the primitive classes.
	    boolean modified = false;
	    int size = size();
		for (int i=0; i<size; i++) {
			if (!coll.contains(doGet(i))) {
				doRemove(i);
				size--;
				i--;
				modified = true;
			}
		}
		return modified;
    }

	@Override
	public Object[] toArray() {
	    int size = size();
		Object[] array = new Object[size];
		doGetAll(array, 0, size);
        return array;
	}

	/**
	 * Returns an array containing the specified elements in this list.
	 *
	 * @param index	index of first element to copy
	 * @param len	number of elements to copy
	 * @return		array the specified elements
	 */
	public Object[] toArray(int index, int len) {
		Object[] array = new Object[len];
		doGetAll(array, index, len);
        return array;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] array) {
	    int size = size();
        if (array.length < size) {
        	array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
        }
        doGetAll(array, 0, size);
        if (array.length > size) {
        	array[size] = null;
        }
        return array;
	}

	/**
	 * Helper method to fill the specified elements in an array.
	 *
	 * @param array	array to store the list elements
	 * @param index	index of first element to copy
	 * @param len	number of elements to copy
	 * @param <T> type of elements stored in the list
	 */
	protected <T> void doGetAll(T[] array, int index, int len) {
		int[] physIdx = physIndex(index, index+len);
		int pos = 0;
        for (int i=0; i<physIdx.length; i+=2) {
        	int num = physIdx[i+1] - physIdx[i];
        	System.arraycopy(values, physIdx[i], array, pos, num);
        	pos += num;
        }
        assert(pos == len);
	}

    /**
     * Adds all of the elements in the specified collection into this list.
     * The new elements will appear in the list in the order that they
     * are returned by the specified collection's iterator.
     *
     * @param coll collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
	@Override
	public boolean addAll(Collection<? extends E> coll) {
        // ArrayList.addAll() also first creates an array containing the
        // collection elements. This guarantees that the list's capacity
        // must only be increased once.
        @SuppressWarnings("unchecked")
        E[] array = (E[]) toArray(coll);
        return doAddAll(-1, array);
	}

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (increases their indices).
     * The new elements will appear in the list in the order that they
     * are returned by the specified collection's iterator.
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param coll collection containing elements to be inserted into this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws NullPointerException if the specified collection is null
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> coll) {
        checkIndexAdd(index);

        // ArrayList.addAll() also first creates an array containing the
        // collection elements. This guarantees that the list's capacity
        // must only be increased once.
        @SuppressWarnings("unchecked")
        E[] array = (E[]) toArray(coll);
        return doAddAll(index, array);
    }

    /**
     * Adds all specified elements into this list.
     *
     * @param elems elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     */
	public boolean addAll(E... elems) {
		return doAddAll(-1, elems);
	}

    /**
     * Inserts the specified elements into this list,
     * starting at the specified position.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (increases their indices).
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param elems elements to be inserted into this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public boolean addAll(int index, E... elems) {
        checkIndexAdd(index);

        return doAddAll(index, elems);
    }

    /**
     * Adds all of the elements in the specified list into this list.
     *
     * @param list collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified list is null
     */
    @SuppressWarnings("unchecked")
    public boolean addAll(GapList<? extends E> list) {
        return doAddAll(-1, (E[]) list.toArray());
    }

    /**
     * Inserts all of the elements in the specified list into this
     * list, starting at the specified position.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (increases their indices).
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param list list containing elements to be inserted into this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws NullPointerException if the specified collection is null
     */
	@SuppressWarnings("unchecked")
    public boolean addAll(int index, GapList<? extends E> list) {
		checkIndexAdd(index);

		return doAddAll(index, (E[]) list.toArray());
	}

    /**
     * Helper method for adding multiple elements to the GapList.
     * It still calls doAdd() for adding each element.
     *
     * @param index index where element should be added
     *              (-1 is valid for adding at the end)
     * @param array array with elements to add
     * @return      true if elements have been added, false otherwise
     */
	protected boolean doAddAll(int index, E[] array) {
        ensureCapacity(size() + array.length);

		if (array.length == 0) {
			return false;
		}
		for (E elem: array) {
			doAdd(index, elem);
			if (index != -1) {
			    index++;
			}
		}
		return true;
	}

	// Iterators

	@Override
	public Iterator<E> iterator() {
		return new Iter(true);
	}

    @Override
	public ListIterator<E> listIterator() {
		return new ListIter(0);
	}

    @Override
	public ListIterator<E> listIterator(int index) {
		return new ListIter(index);
	}

	// List operations

    @Override
    public E getFirst() {
    	return doGet(0);
    }

    @Override
    public E getLast() {
    	return doGet(size()-1);
    }

    @Override
    public void addFirst(E elem) {
    	add(0, elem);
    }

    @Override
    public void addLast(E elem) {
    	add(elem);
    }

    @Override
    public E removeFirst() {
    	return doRemove(0);
    }

    @Override
    public E removeLast() {
    	return doRemove(size()-1);
    }

    // Queue operations

    @Override
    public E peek() {
        if (size() == 0) {
            return null;
        }
        return getFirst();
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E poll() {
        if (size() == 0) {
            return null;
        }
        return removeFirst();
    }

	@Override
    public E remove() {
        return removeFirst();
    }

	@Override
    public boolean offer(E elem) {
        return add(elem);
    }

    // Deque operations

	@Override
	public Iterator<E> descendingIterator() {
		return new Iter(false);
	}

	@Override
	public boolean offerFirst(E elem) {
        addFirst(elem);
        return true;
	}

	@Override
	public boolean offerLast(E elem) {
        addLast(elem);
        return true;
	}

	@Override
	public E peekFirst() {
        if (size() == 0) {
            return null;
        }
        return getFirst();
	}

	@Override
	public E peekLast() {
        if (size() == 0) {
            return null;
        }
        return getLast();
	}

	@Override
	public E pollFirst() {
        if (size() == 0) {
            return null;
        }
        return removeFirst();
	}

	@Override
	public E pollLast() {
        if (size() == 0) {
            return null;
        }
        return removeLast();
	}

	@Override
	public E pop() {
        return removeFirst();
	}

	@Override
	public void push(E elem) {
        addFirst(elem);
	}

	@Override
	public boolean removeFirstOccurrence(Object elem) {
		int index = indexOf(elem);
		if (index == -1) {
			return false;
		}
		doRemove(index);
		return true;
	}

	@Override
	public boolean removeLastOccurrence(Object elem) {
		int index = lastIndexOf(elem);
		if (index == -1) {
			return false;
		}
		doRemove(index);
		return true;
	}

	// --- Serialization ---

    /**
     * Serialize a GapList object.
     *
     * @serialData The length of the array backing the <tt>GapList</tt>
     *             instance is emitted (int), followed by all of its elements
     *             (each an <tt>Object</tt>) in the proper order.
     * @param oos  output stream for serialization
     * @throws 	   IOException if serialization fails
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        // Write out array length
	    int size = size();
        oos.writeInt(size);

        // Write out all elements in the proper order.
        for (int i=0; i<size; i++) {
        	oos.writeObject(doGet(i));
        }
    }

    /**
     * Deserialize a GapList object.
 	 *
     * @param ois  input stream for serialization
     * @throws 	   IOException if serialization fails
     * @throws 	   ClassNotFoundException if serialization fails
     */
    @SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // Read in array length and allocate array
        size = ois.readInt();
        values = (E[]) new Object[size];

        // Read in all elements in the proper order.
        for (int i=0; i<size; i++) {
            values[i] = (E) ois.readObject();
        }
    }

    // --- Static bulk methods working with two GapLists ---

    /**
     * Moves elements from one GapList to another.
     *
     * @param src		source list
     * @param srcIndex	index of first element in source list
     * @param dst		destination list
     * @param dstIndex	index of first element in source list
     * @param len		number of elements to move
     * @param <E> 		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public static <E> void move(GapList<? extends E> src, int srcIndex, GapList<E> dst, int dstIndex, int len) {
        if (src == dst) {
            src.move(srcIndex, dstIndex, len);

        } else {
            src.checkRange(srcIndex, len);
            dst.checkRange(dstIndex, len);

    		for (int i=0; i<len; i++) {
    			E elem = src.doReSet(srcIndex+i, null);
    			dst.doSet(dstIndex+i, elem);
    		}
        }
    }

    /**
     * Copies elements from one GapList to another.
     *
     * @param src		source list
     * @param srcIndex	index of first element in source list
     * @param dst		destination list
     * @param dstIndex	index of first element in source list
     * @param len		number of elements to copy
     * @param <E> 		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public static <E> void copy(GapList<? extends E> src, int srcIndex, GapList<E> dst, int dstIndex, int len) {
        if (src == dst) {
            src.copy(srcIndex, dstIndex, len);

        } else {
            src.checkRange(srcIndex, len);
            dst.checkRange(dstIndex, len);

    		for (int i=0; i<len; i++) {
    			E elem = src.doGet(srcIndex+i);
    			dst.doSet(dstIndex+i, elem);
    		}
    	}
    }

    /**
     * Swaps elements from two GapLists.
     *
     * @param src		first list
     * @param srcIndex	index of first element in first list
     * @param dst		second list
     * @param dstIndex	index of first element in second list
     * @param len		number of elements to swap
     * @param <E> 		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public static <E> void swap(GapList<E> src, int srcIndex, GapList<E> dst, int dstIndex, int len) {
        if (src == dst) {
            src.swap(srcIndex, dstIndex, len);

        } else {
        	src.checkRange(srcIndex, len);
        	dst.checkRange(dstIndex, len);

        	if (src != dst) {
        		for (int i=0; i<len; i++) {
            		E swap = src.doGet(srcIndex+i);
            		swap = dst.doSet(dstIndex+i, swap);
            		src.doSet(srcIndex+i, swap);
        		}
        	}
        }
    }


    // --- Bulk methods ---

    // -- Readers --

    /**
     * Returns specified range of elements from list.
     *
     * @param index index of first element to retrieve
     * @param len   number of elements to retrieve
     * @return      GapList containing the specified range of elements from list
     */
    public GapList<E> getAll(int index, int len) {
        checkRange(index, len);

        GapList<E> list = new GapList<E>(len);
        for (int i=0; i<len; i++) {
            list.add(doGet(index+i));
        }
        return list;
    }

    /**
     * Returns specified range of elements from list.
     *
     * @param index index of first element to retrieve
     * @param len   number of elements to retrieve
     * @return      GapList containing the specified range of elements from list
     */
    public E[] getArray(int index, int len) {
        checkRange(index, len);

        @SuppressWarnings("unchecked")
        E[] array = (E[]) new Object[len];
        for (int i=0; i<len; i++) {
            array[i] = doGet(index+i);
        }
        return array;
    }

    // -- Mutators --

    /**
     * Replaces the specified elements.
     *
     * @param index index of first element to set
     * @param list  list with elements to set
     */
    public void setAll(int index, GapList<? extends E> list) {
    	// There is a special implementation accepting a GapList
    	// so the method is also available in the primitive classes.
	    int size = list.size();
        checkRange(index, size);

        for (int i=0; i<size; i++) {
            doSet(index+i, list.get(i));
        }
    }

    /**
     * Replaces the specified elements.
     *
     * @param index index of first element to set
     * @param coll  collection with elements to set
     */
    public void setAll(int index, Collection<? extends E> coll) {
        checkRange(index, coll.size());

        // In contrary to addAll() there is no need to first create an array
        // containing the collection elements, as the list will not grow.
        int i = 0;
        Iterator<? extends E> iter = coll.iterator();
        while (iter.hasNext()) {
            doSet(index+i, iter.next());
            i++;
        }
    }

    /**
     * Replaces the specified elements.
     *
     * @param index index of first element to set
     * @param elems elements to set
     */
    public void setAll(int index, E... elems) {
        checkRange(index, elems.length);

        doSetAll(index, elems);
    }

    /**
     * Replaces the specified elements.
     *
     * @param index index of first element to set
     * @param elems elements to set
     */
    protected void doSetAll(int index, E[] elems) {
        for (int i=0; i<elems.length; i++) {
            doSet(index+i, elems[i]);
        }
    }

	/**
	 * Remove specified range of elements from list.
	 *
	 * @param index	index of first element to remove
	 * @param len	number of elements to remove
	 */
	public void remove(int index, int len) {
    	checkRange(index, len);

    	doRemoveAll(index, len);
	}

    /**
     * Remove specified range of elements from list.
     *
     * @param index index of first element to remove
     * @param len   number of elements to remove
     */
	protected void doRemoveAll(int index, int len) {
		if (len == size()) {
			doModify();
			init(values, 0);
		} else {
			for (int i=index+len-1; i>=index; i--) {
				doRemove(i);
			}
		}
	}

	/**
	 * Initializes the list so it will afterwards have a size of
	 * <code>len</code> and contain only the element <code>elem</code>.
	 * The list will grow or shrink as needed.
	 *
	 * @param len  length of list
	 * @param elem element which the list will contain
	 */
	public void init(int len, E elem) {
	    checkLength(len);

	    int size = size();
        if (len < size) {
            remove(len, size-len);
            fill(0, len, elem);
        } else {
            fill(0, size, elem);
            for (int i=size; i<len; i++) {
                add(elem);
            }
        }
        assert(size() == len);
	}

	/**
     * Resizes the list so it will afterwards have a size of
     * <code>len</code>. If the list must grow, the specified
     * element <code>elem</code> will be used for filling.
     *
     * @param len  length of list
     * @param elem element which will be used for extending the list
	 */
	public void resize(int len, E elem) {
	    checkLength(len);

	    int size = size();
        if (len < size) {
            remove(len, size-len);
        } else {
            for (int i=size; i<len; i++) {
                add(elem);
            }
        }
        assert(size() == len);
	}

    /**
     * Fill list.
     *
     * @param elem  element used for filling
     */
    // see java.util.Arrays#fill
    public void fill(E elem) {
    	int size = size();
        for (int i=0; i<size; i++) {
            doSet(i, elem);
        }
    }

    /**
     * Fill specified elements.
     *
     * @param index	index of first element to fill
     * @param len	number of elements to fill
     * @param elem	element used for filling
     */
    // see java.util.Arrays#fill
    public void fill(int index, int len, E elem) {
    	checkRange(index, len);

    	for (int i=0; i<len; i++) {
    		doSet(index+i, elem);
    	}
    }

    /**
     * Copy specified elements.
     * Source and destination ranges may overlap.
     *
     * @param srcIndex	index of first source element to copy
     * @param dstIndex	index of first destination element to copy
     * @param len		number of elements to copy
     */
    public void copy(int srcIndex, int dstIndex, int len) {
    	checkRange(srcIndex, len);
    	checkRange(dstIndex, len);

    	if (srcIndex < dstIndex) {
    		for (int i=len-1; i>=0; i--) {
    			doReSet(dstIndex+i, doGet(srcIndex+i));
    		}
    	} else if (srcIndex > dstIndex) {
    		for (int i=0; i<len; i++) {
    			doReSet(dstIndex+i, doGet(srcIndex+i));
    		}
    	}
    }

    /**
     * Move specified elements.
     * The elements which are moved away are set to null.
     * Source and destination ranges may overlap.
     *
     * @param srcIndex	index of first source element to move
     * @param dstIndex	index of first destination element to move
     * @param len		number of elements to move
     */
    public void move(int srcIndex, int dstIndex, int len) {
    	checkRange(srcIndex, len);
    	checkRange(dstIndex, len);

    	// Copy elements
    	if (srcIndex < dstIndex) {
    		for (int i=len-1; i>=0; i--) {
    			doReSet(dstIndex+i, doGet(srcIndex+i));
    		}
    	} else if (srcIndex > dstIndex) {
    		for (int i=0; i<len; i++) {
    			doReSet(dstIndex+i, doGet(srcIndex+i));
    		}
    	}

    	// Set elements to null after the move operation
    	if (srcIndex < dstIndex) {
    		int fill = Math.min(len, dstIndex-srcIndex);
    		fill(srcIndex, fill, null);
    	} else if (srcIndex > dstIndex) {
    		int fill = Math.min(len, srcIndex-dstIndex);
    		fill(srcIndex+len-fill, fill, null);
    	}
    }

    /**
     * Reverses the order of all elements in the specified list.
     */
    public void reverse() {
    	reverse(0, size());
    }

    /**
     * Reverses the order of the specified elements in the list.
     *
     * @param index	index of first element to reverse
     * @param len	number of elements to reverse
     */
    public void reverse(int index, int len) {
    	checkRange(index, len);

    	int pos1 = index;
		int pos2 = index+len-1;
    	int mid = len / 2;
    	for (int i=0; i<mid; i++) {
    		E swap = doGet(pos1);
    		swap = doReSet(pos2, swap);
    		doReSet(pos1, swap);
    		pos1++;
    		pos2--;
    	}
    }

    /**
     * Swap the specified elements in the list.
     *
     * @param index1	index of first element in first range to swap
     * @param index2	index of first element in second range to swap
     * @param len		number of elements to swap
     * @throws 			IndexOutOfBoundsException if the ranges overlap
     */
    public void swap(int index1, int index2, int len) {
    	checkRange(index1, len);
    	checkRange(index2, len);
    	if ((index1 < index2 && index1+len > index2) ||
    		index1 > index2 && index2+len > index1) {
    		throw new IllegalArgumentException("Swap ranges overlap");
    	}

    	for (int i=0; i<len; i++) {
    		E swap = doGet(index1+i);
    		swap = doReSet(index2+i, swap);
    		doReSet(index1+i, swap);
    	}
    }

    /**
     * Rotate specified elements in the list.
     * The distance argument can be positive or negative:
     * If it is positive, the elements are moved towards the end,
     * if negative, the elements are moved toward the beginning,
     * if distance is 0, the list is not changed.
     *
     * @param distance	distance to move the elements
     */
    public void rotate(int distance) {
    	rotate(0, size(), distance);
    }

    /**
     * Rotate specified elements in the list.
     * The distance argument can be positive or negative:
     * If it is positive, the elements are moved towards the end,
     * if negative, the elements are moved toward the beginning,
     * if distance is 0, the list is not changed.
     *
     * @param index		index of first element to rotate
     * @param len		number of elements to rotate
     * @param distance	distance to move the elements
     */
    public void rotate(int index, int len, int distance) {
    	checkRange(index, len);

    	int size = size();
        distance = distance % size;
        if (distance < 0) {
            distance += size;
        }
        if (distance == 0) {
            return;
        }

        int num = 0;
        for (int start=0; num != size; start++) {
            E elem = doGet(index+start);
            int i = start;
            do {
                i += distance;
                if (i >= len) {
                    i -= len;
                }
                elem = doReSet(index+i, elem);
                num++;
            } while (i != start);
        }
    }

    /**
     * Sort elements in the list using the specified comparator.
     *
     * @param comparator	comparator to use for sorting
     * 						(null means the elements natural ordering should be used)
     *
     * @see Arrays#sort
     */
    public void sort(Comparator<? super E> comparator) {
    	sort(0, size(), comparator);
    }

    /**
     * Sort specified elements in the list using the specified comparator.
     *
     * @param index			index of first element to sort
     * @param len			number of elements to sort
     * @param comparator	comparator to use for sorting
     * 						(null means the elements natural ordering should be used)
     *
     * @see Arrays#sort
     */
    public void sort(int index, int len, Comparator<? super E> comparator) {
    	checkRange(index, len);

    	normalize();
    	Arrays.sort(values, index, index+len, comparator);
    }

    /*
    Question:
       Why is the signature of method binarySearch
           public <K> int binarySearch(K key, Comparator<? super K> comparator)
       and not
           public int binarySearch(E key, Comparator<? super E> comparator)
       as you could expect?

    Answer:
       This allows to use the binarySearch method not only with keys of
       the type stored in the GapList, but also with any other type you
       are prepared to handle in you Comparator.
       So if we have a class Name and its comparator as defined in the
       following code snippets, both method calls are possible:

       new GapList<Name>().binarySearch(new Name("a"), new NameComparator());
       new GapList<Name>().binarySearch("a), new NameComparator());

       class Name {
           String name;

           public Name(String name) {
               this.name = name;
           }
           public String getName() {
               return name;
           }
           public String toString() {
               return name;
           }
       }

       static class NameComparator implements Comparator<Object> {
           @Override
           public int compare(Object o1, Object o2) {
               String s1;
               if (o1 instanceof String) {
                   s1 = (String) o1;
               } else {
                   s1 = ((Name) o1).getName();
               }
               String s2;
               if (o2 instanceof String) {
                   s2 = (String) o2;
               } else {
                   s2 = ((Name) o2).getName();
               }
               return s1.compareTo(s2);
           }
        }
    */

    /**
     * Searches the specified range for an object using the binary
     * search algorithm.
     *
     * @param key           the value to be searched for
     * @param comparator    the comparator by which the list is ordered.
     *                      A <tt>null</tt> value indicates that the elements'
     *                      {@linkplain Comparable natural ordering} should be used.
     * @return              index of the search key, if it is contained in the array;
     *                      otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *                      <i>insertion point</i> is defined as the point at which the
     *                      key would be inserted into the array: the index of the first
     *                      element greater than the key, or <tt>a.length</tt> if all
     *                      elements in the array are less than the specified key.  Note
     *                      that this guarantees that the return value will be &gt;= 0 if
     *                      and only if the key is found.
     *
     * @see Arrays#binarySearch
     */
    public <K> int binarySearch(K key, Comparator<? super K> comparator) {
    	return binarySearch(0, size(), key, comparator);
    }

    /**
     * Searches the specified range for an object using the binary
     * search algorithm.
     *
     * @param index         index of first element to search
     * @param len           number of elements to search
     * @param key           the value to be searched for
     * @param comparator    the comparator by which the list is ordered.
     *                      A <tt>null</tt> value indicates that the elements'
     *                      {@linkplain Comparable natural ordering} should be used.
     * @return              index of the search key, if it is contained in the array;
     *                      otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *                      <i>insertion point</i> is defined as the point at which the
     *                      key would be inserted into the array: the index of the first
     *                      element greater than the key, or <tt>a.length</tt> if all
     *                      elements in the array are less than the specified key.  Note
     *                      that this guarantees that the return value will be &gt;= 0 if
     *                      and only if the key is found.
     *
     * @see Arrays#binarySearch
     */
    @SuppressWarnings("unchecked")
    public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
    	checkRange(index, len);

    	normalize();
    	return Arrays.binarySearch((Object[]) values, index, index+len, key, (Comparator<Object>) comparator);
    }

    //--- Arguments check methods

    /**
     * Check that specified index is valid for getting/setting elements.
     *
     * @param index	index to check
     * @throws IndexOutOfBoundsException if index is invalid
     */
    protected void checkIndex(int index) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException("Invalid index: " + index + " (size: " + size() + ")");
		}
	}

    /**
     * Check that specified index is valid for adding elements.
     *
     * @param index	index to check
     * @throws IndexOutOfBoundsException if index is invalid
     */
	protected void checkIndexAdd(int index) {
		if (index < 0 || index > size()) {
			throw new IndexOutOfBoundsException("Invalid index: " + index + " (size: " + size() + ")");
		}
	}

	/**
	 * Check that specified range is valid.
	 *
	 * @param index	start index of range to check
	 * @param len	number of elements  in range to check
     * @throws IndexOutOfBoundsException if index is invalid
	 */
	protected void checkRange(int index, int len) {
		if (index < 0 || len < 0 || index+len > size()) {
			throw new IndexOutOfBoundsException("Invalid range: " + index + "/" + len + " (size: " + size() + ")");
		}
	}

    /**
     * Check that specified length is valid (>= 0).
     *
     * @param length length to check
     * @throws IndexOutOfBoundsException if length is invalid
     */
    protected void checkLength(int length) {
        if (length < 0) {
            throw new IndexOutOfBoundsException("Invalid length: " + length);
        }
    }

	// --- Helper methods for debugging ---

	/**
	 * Private method to check invariant of GapList.
	 * It is only used for debugging.
	 */
	private void debugCheck() {
		assert(size >= 0 && size <= values.length);
		assert(start >=0 && (start < values.length || values.length == 0));
		assert(end >= 0 && (end < values.length || values.length == 0));
		assert(values.length == 0 || (start+size+gapSize) % values.length == end);

		// Check that logical gap index is correct
		assert(gapSize >= 0);
		if (gapSize > 0) {
			assert(gapStart >= 0 && gapStart < values.length);
			// gap may not be at start or end
			assert(gapIndex > 0 && gapIndex < size);
			// gap start may not be the same as start or end
			assert(gapStart != start && gapStart != end);
			// check that logical and phyiscal gap index are correct
			assert(physIndex(gapIndex) == (gapStart+gapSize) % values.length);
		}

		// Check that gap positions contain null values
		if (gapSize > 0) {
			for (int i=gapStart; i<gapStart+gapSize; i++) {
				int pos = (i % values.length);
				assert(values[pos] == null);
			}
		}

		// Check that all end positions contain null values
		if (end != start) {
			for (int i=end; i<start; i++) {
				int pos = (i % values.length);
				assert(values[pos] == null);
			}
		}
	}

	/**
	 * Private method to determine state of GapList.
	 * It is only used for debugging.
	 *
	 * @return	state in which GapList is
	 */
	private int debugState() {
		if (size == 0) {
			return 0;
		} else if (size == values.length) {
			return 1;
		} else if (gapSize == 0) {
			if (start == 0) {
				return 2;
			} else if (end == 0) {
				return 3;
			} else if (start < end) {
				return 4;
			} else if (start > end) {
				return 5;
			}
		} else if (gapSize > 0) {
			if (start == end) {
				if (start == 0) {
					return 6;
				} else if (gapStart < start) {
					return 7;
				} else if (gapStart > start) {
					int gapEnd = (gapStart+gapSize) % values.length;
					if (gapEnd > gapStart) {
						return 8;
					} else if (gapEnd < gapStart) {
						return 9;
					}
				}
			} else if (start != end) {
				if (start == 0) {
					return 10;
				} else if (gapStart < start) {
					return 14; //
				} else if (gapStart > start) {
					int gapEnd = (gapStart+gapSize) % values.length;
					if (gapEnd < gapStart) {
						return 12;
					} else {
						if (end == 0) {
							return 11;
						} else if (end > start) {
							return 13;
						} else if (end < start) {
							return 15;
						}
					}
				}
			}
		}
		assert(false);
		return -1;
	}

	/**
	 * Private method to dump fields of GapList.
	 * It is only called if the code is run in development mode.
	 */
	private void debugDump() {
		debugLog("values: size= " +values.length + ", data= "+ debugPrint(values));
		debugLog("size=" + size + ", start=" + start + ", end=" + end +
				", gapStart=" + gapStart + ", gapSize=" + gapSize + ", gapIndex=" + gapIndex);
		debugLog(toString());
	}

	/**
	 * Print array values into string.
	 *
	 * @param values	array with values
	 * @return			string representing array values
	 */
	private String debugPrint(E[] values) {
		StringBuilder buf = new StringBuilder();
		buf.append("[ ");
		for (int i=0; i<values.length; i++) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(values[i]);
		}
		buf.append(" ]");
		return buf.toString();
	}


	/**
	 * Private method write logging output.
	 * It is only used for debugging.
	 *
	 * @param msg message to write out
	 */
	private void debugLog(String msg) {

	}

    // --- Start class Iter ---

    /**
     * Iterator supports forward and reverse iteration.
     */
    class Iter implements Iterator<E> {
    	/** true if iterator moves forward */
    	boolean forward;
    	/** current index */
    	int index;
    	/** index where element will be removed if remove() is called */
    	int remove;

    	/**
    	 * Constructor.
    	 *
    	 * @param forward	true for forward access
    	 */
    	public Iter(boolean forward) {
    		this.forward = forward;

    		if (forward) {
    			index = 0;
    		} else {
    			index = size()-1;
    		}
    		remove = -1;
    	}

		@Override
		public boolean hasNext() {
			if (forward) {
				return index != size();
			} else {
				return index != -1;
			}
		}

		@Override
		public E next() {
			if (forward) {
				if (index >= size()) {
					throw new NoSuchElementException();
				}
			} else {
				if (index < 0) {
					throw new NoSuchElementException();
				}
			}
			E elem = get(index);
			remove = index;
			if (forward) {
				index++;
			} else {
				index--;
			}
			return elem;
		}

		@Override
		public void remove() {
			if (remove == -1) {
				throw new IllegalStateException("No current element to remove");
			}
			GapList.this.remove(remove);
			if (index > remove) {
				index--;
			}
			remove = -1;
		}
    }

    // --- End class Iter ---

    // --- Start class ListIter ---

    /**
     * Iterator supports forward and reverse iteration.
     */
    class ListIter implements ListIterator<E> {
    	/** current index */
    	int index;
    	/** index where element will be removed if remove() is called */
    	int remove;

    	/**
    	 * Constructor.
    	 *
    	 * @param index	start index
    	 */
    	public ListIter(int index) {
    		checkIndexAdd(index);
   			this.index = index;
    		this.remove = -1;
    	}

		@Override
		public boolean hasNext() {
			return index < size();
		}

		@Override
		public boolean hasPrevious() {
			return index > 0;
		}

		@Override
		public E next() {
			if (index >= size()) {
				throw new NoSuchElementException();
			}
			E elem = GapList.this.get(index);
			remove = index;
			index++;
			return elem;
		}

		@Override
		public int nextIndex() {
			return index;
		}

		@Override
		public E previous() {
			if (index <= 0) {
				throw new NoSuchElementException();
			}
			index--;
			E elem = GapList.this.get(index);
			remove = index;
			return elem;
		}

		@Override
		public int previousIndex() {
			return index-1;
		}

		@Override
		public void remove() {
			if (remove == -1) {
				throw new IllegalStateException("No current element to remove");
			}
			GapList.this.remove(remove);
			if (index > remove) {
				index--;
			}
			remove = -1;
		}

		@Override
		public void set(E e) {
			if (remove == -1) {
				throw new IllegalStateException("No current element to set");
			}
			GapList.this.set(remove, e);
		}

		@Override
		public void add(E e) {
			GapList.this.add(index, e);
			index++;
			remove = -1;
		}
    }

    // --- End class ListIter ---

}
