// ---
// --- DO NOT EDIT
// --- AUTOMATICALLY GENERATED FILE
// ---
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
package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.helper.ArraysHelper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.Arrays;
import java.util.Collection;


import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;




/**
 * ShortGapList is a variation of GapList which uses primitive
 * values for storage. This allows to save memory and improve execution
 * speed. Use ShortObjGapList to combine these features with the
 * ability to access it using the standard List interface.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * </p>
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @see	    org.magicwerk.brownies.collections.GapList
 * @see	    org.magicwerk.brownies.collections.primitive.ShortObjGapList
 */

public class ShortGapList implements Cloneable, Serializable {


    /*
     * Helper variables to enable code for debugging.
     * As the variables are declared as "static final boolean", the compiler
     * will be able to detect unused branches and will not compile the
     * code (the same approach is used for the assert statement).
     */
    /** If true the invariants the ShortGapList are checked for debugging */
    private static final boolean DEBUG_CHECK = false;
    /** If true the calls to some methods are traced out for debugging */
    private static final boolean DEBUG_TRACE = false;
    /** If true the internal state of the ShortGapList is traced out for debugging */
    private static final boolean DEBUG_DUMP = false;


    // -- EMPTY --

    // Cannot make a static reference to the non-static type short:
    // public static ShortGapList EMPTY = ShortGapList.create().unmodifiableList();
    // Syntax error:
    // public static  ShortGapList EMPTY = ShortGapList.create().unmodifiableList();

    /** Unmodifiable empty instance */
    @SuppressWarnings("rawtypes")
    private static final ShortGapList EMPTY = ShortGapList.create().unmodifiableList();

    /**
     * @return unmodifiable empty instance
     */
    @SuppressWarnings("unchecked")
    public static  ShortGapList EMPTY() {
        return EMPTY;
    }


    /**
     * An immutable version of a ShortGapList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableShortGapList extends ShortGapList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
        protected ImmutableShortGapList(ShortGapList that) {
            super(true, that);
        }

        
        protected boolean doAdd(int index, short elem) {
        	error();
        	return false;
        }

        
        protected boolean doAddAll(int index, short[] elems) {
        	error();
        	return false;
        }

        
        protected short doSet(int index, short elem) {
        	error();
        	return (short)0;
        }

        
        protected void doSetAll(int index, short[] elems) {
        	error();
        }

        
        protected short doReSet(int index, short elem) {
        	error();
        	return (short)0;
        }

        
        protected short doRemove(int index) {
        	error();
        	return (short)0;
        }

        
        protected void doRemoveAll(int index, int len) {
        	error();
        }

        
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
	private short[] values;
	/** Number of elements stored in this ShortGapList */
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
     * @param        type of elements stored in the list
     */
    // This separate method is needed as the varargs variant creates the ShortGapList with specific size
    public static  ShortGapList create() {
        return new ShortGapList();
    }

    /**
     * Create new list with specified capacity.
     *
     * @param capacity  capacity
     * @return          created list
     * @param        type of elements stored in the list
     */
    public static  ShortGapList create(int capacity) {
        return new ShortGapList(capacity);
    }

    /**
     * Create new list with specified elements.
     *
     * @param coll      collection with element
     * @return          created list
     * @param        type of elements stored in the list
     */
	public static  ShortGapList create(Collection<Short> coll) {
		return new ShortGapList(coll);
	}

	/**
	 * Create new list with specified elements.
	 *
	 * @param elems 	array with elements
	 * @return 			created list
	 * @param  		type of elements stored in the list
	 */
	public static  ShortGapList create(short... elems) {
		return new ShortGapList(elems);
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
    protected ShortGapList(boolean copy, ShortGapList that) {
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
	public ShortGapList() {
		init();
	}

	/**
	 * Construct a list with specified capacity.
	 *
	 * @param capacity	capacity to use
	 */
	public ShortGapList(int capacity) {
		init(capacity);
	}

	/**
	 * Copy constructor.
	 *
	 * @param that	source object to copy
	 */
	public ShortGapList(Collection<Short> that) {
		init(that);
	}

	/**
	 * Copy constructor.
	 *
	 * @param that	source object to copy
	 */
	public ShortGapList(short... that) {
		init(that);
	}

	/**
	 * Initialize the list to be empty.
	 */
	public void init() {
		init(new short[DEFAULT_CAPACITY], 0);
	}

	/**
     * Initialize the list to be empty with specified capacity.
     *
	 * @param capacity capacity
	 */
	public void init(int capacity) {
		init(new short[capacity], 0);
	}

	/**
	 * Initialize the list to contain the specified elements only.
	 *
	 * @param coll collection with elements
	 */
	public void init(Collection<Short> coll) {
		short[] array = toArray(coll);
		init(array, array.length);
	}

	/**
     * Initialize the list to contain the specified elements only.
     *
	 * @param elems array with elements
	 */
	public void init(short... elems) {
		short[] array = elems.clone();
		init(array, array.length);
	}

	/**
	 * Copies the collection values into an array.
	 *
	 * @param coll   collection of values
	 * @return       array containing the collection values
	 */
	static short[] toArray(Collection<Short> coll) { 
       Object[] values = coll.toArray(); 
       short[] v = new short[values.length]; 
       for (int i=0; i<values.length; i++) { 
        v[i] = (Short) values[i]; 
       }       return v;   }

    /**
     * Returns a shallow copy of this <tt>ShortGapList</tt> instance
     * (the new list will contain the same elements as the source list, i.e. the elements themselves are not copied).
     * This method is identical to clone() except that the result is casted to ShortGapList.
     *
     * @return a clone of this instance
     * @see #clone
     */
	@SuppressWarnings("unchecked")
    public ShortGapList copy() {
	    return (ShortGapList) clone();
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
    public ShortGapList unmodifiableList() {
        // Naming as in java.util.Collections#unmodifiableList
        return new ImmutableShortGapList(this);
    }

    /**
     * Returns a shallow copy of this <tt>ShortGapList</tt> instance
     * (The elements themselves are not copied).
     * The capacity of the list will be set to the number of elements,
     * so after calling clone(), size and capacity are equal.
     *
     * @return a clone of this <tt>ShortGapList</tt> instance
     */
	@SuppressWarnings("unchecked")
    
    public Object clone() {
		try {
			ShortGapList list = (ShortGapList) super.clone();
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
	 * Normalize data of ShortGapList so the elements are found
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
	void init(short[] values, int size) {
		this.values = (short[]) values;
		this.size = size;

		start = 0;
		end = 0;
		gapSize = 0;
		gapStart = 0;
		gapIndex = 0;

		if (DEBUG_CHECK) debugCheck();
	}

	
	public void clear() {
		doRemoveAll(0, size());
	}

	
	public int size() {
		return size;
	}

	/**
	 * Returns capacity of this ShortGapList.
	 * Note that two GapLists are considered equal even if they have a distinct capacity.
	 * Also the capacity can be changed by operations like clone() etc.
	 *
	 * @return capacity of this ShortGapList
	 */
	public int capacity() {
		return values.length;
	}

    
    public short get(int index) {
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
     * Helper method for getting an element from the ShortGapList.
     * This is the only method which really gets an element.
     * Override if you need to validity checks before getting.
     *
     * @param index index of element to return
     * @return      the element at the specified position in this list
     */
    protected short doGet(int index) {
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

	
	public short set(int index, short elem) {
		checkIndex(index);

		return doSet(index, elem);
	}

    /**
     * Helper method for setting an element in the ShortGapList.
     * This is the only method which really sets an element.
     * Override if you need to validity checks before setting.
     *
     * @param index index where element will be placed
     * @param elem  element to set
     * @return      old element which was at the position
     */
    protected short doSet(int index, short elem) {
        assert (index >= 0 && index < size);

        int physIdx = physIndex(index);
        short oldElem = values[physIdx];
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
    protected short doReSet(int index, short elem) {
        assert (index >= 0 && index < size);

        int physIdx = physIndex(index);
        short oldElem = values[physIdx];
        values[physIdx] = elem;
        return oldElem;
    }

    /**
     * This method is called internally before elements are allocated or freed.
     * Override if you need to validity checks.
     */
    protected void doModify() {
    }

    
	public boolean add(short elem) {
		if (DEBUG_TRACE) {
			debugLog("add: " + elem);
			if (DEBUG_DUMP) debugDump();
		}
		return doAdd(-1, elem);
	}

	
	public void add(int index, short elem)	{
		if (DEBUG_TRACE) {
			debugLog("add: " + index + ", " + elem);
			if (DEBUG_DUMP) debugDump();
		}
		checkIndexAdd(index);
		doAdd(index, elem);
	}

	/**
	 * Helper method for adding an element to the ShortGapList.
	 * This is the only method which really adds an element.
	 * Override if you need to validity checks before adding.
	 *
	 * @param index	index where element should be added
	 *              (-1 means it is up to the implementation to choose the index)
	 * @param elem	element to add
	 * @return      true if element has been added (ShortGapList.add() will always return true)
	 */
	protected boolean doAdd(int index, short elem) {
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
	 * filled with (short)0.
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
	 * filled with (short)0.
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

		// Write (short)0 into array slots which are not used anymore
		// This is necessary to allow GC to reclaim non used objects.
		if (src <= dst) {
			int start = src;
			int end = (dst < src+len) ? dst : src+len;
			assert(end-start <= len);
			for (int i=start; i<end; i++) {
				values[i] = (short)0;
			}
		} else {
			int start = (src > dst+len) ? src : dst+len;
			int end = src+len;
			assert(end-start <= len);
			for (int i=start; i<end; i++) {
				values[i] = (short)0;
			}
		}

		if (DEBUG_TRACE) {
			if (DEBUG_DUMP) {
				debugLog(debugPrint(values));
			}
		}
	}

	
	public short remove(int index) {
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
	protected short doRemove(int index) {
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

		short removed = values[physIdx];
		values[physIdx] = (short)0;
		size--;

		if (DEBUG_DUMP) debugDump();
		if (DEBUG_CHECK) debugCheck();
		return removed;
	}

    /**
     * Increases the capacity of this <tt>ShortGapList</tt> instance, if
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

		short[] newValues = null;
		if (start == 0) {
			newValues = Arrays.copyOf(values, newCapacity);
		} else if (start > 0) {
			int grow = newCapacity-values.length;
			newValues = (short []) new short[newCapacity];
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
     * Trims the capacity of this <tt>ShortGapList</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>ShortGapList</tt> instance.
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

    
    public boolean equals(Object obj) {
    	if (obj == this) {
    		return true;
    	}
    	if (obj instanceof ShortObjGapList) { obj = ((ShortObjGapList) obj).list; }
 if (!(obj instanceof ShortGapList)) {
    		return false;
    	}
    	@SuppressWarnings("unchecked")
		ShortGapList list = (ShortGapList) obj;
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

    
    public int hashCode() {
    	int hashCode = 1;
    	int size = size();
    	for (int i=0; i<size; i++) {
    		short elem = doGet(i);
    		hashCode = 31*hashCode + hashCodeElem(elem);
    	}
    	return hashCode;
    }

	
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

	
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Helper function to check two elements stored in the ShortGapList
	 * for equality.
	 *
	 * @param elem1	first element
	 * @param elem2	second element
	 * @return		true if the elements are equal, otherwise false
	 */
	static boolean equalsElem(short val1, short val2) {
return val1 == val2;
	}

	/**
	 * Helper method to calculate hash code of a element stored in
	 * the ShortGapList.
	 *
	 * @param elem	element
	 * @return		hash code for element
	 */
	static int hashCodeElem(short val) {
return (int) val;
	}

	
	public int indexOf(short elem) {
		int size = size();
		for (int i=0; i<size; i++) {
			if (equalsElem(doGet(i), elem)) {
				return i;
			}
		}
		return -1;
	}

	
	public int lastIndexOf(short elem) {
		for (int i=size-1; i>=0; i--) {
			if (equalsElem(doGet(i), elem)) {
				return i;
			}
		}
		return -1;
	}

	
	

	
	public boolean contains(short elem) {
		return indexOf(elem) != -1;
	}

	/**
	 * Returns true if any of the elements of the specified collection is contained in the list.
	 *
	 * @param coll collection with elements to be contained
	 * @return     true if any element is contained, false otherwise
	 */
	public boolean containsAny(Collection<Short> coll) {
	    // Note that the signature has been chosen as in List:
	    // - boolean addAll(Collection<Short> c);
	    // - boolean containsAll(Collection<Short> c);
	    for (short elem: coll) {
	        if (contains(elem)) {
	            return true;
	        }
	    }
	    return false;
	}

	
	public boolean containsAll(Collection<Short> coll) {
	    // Note that this method is already implemented in AbstractCollection.
		// It has been duplicated so the method is also available in the primitive classes.
	    for (short elem: coll) {
	        if (!contains(elem)) {
	            return false;
	        }
	    }
	    return true;
	}

	
    public boolean removeAll(Collection<Short> coll) {
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
    public boolean removeAll(ShortGapList coll) {
    	// There is a special implementation accepting a ShortGapList
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

	
    public boolean retainAll(Collection<Short> coll) {
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
    public boolean retainAll(ShortGapList coll) {
    	// There is a special implementation accepting a ShortGapList
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

	
	public short[] toArray() {
	    int size = size();
		short[] array = new short[size];
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
	public short[] toArray(int index, int len) {
		short[] array = new short[len];
		doGetAll(array, index, len);
        return array;
	}

	@SuppressWarnings("unchecked")
	
	public  short[] toArray(short[] array) {
	    int size = size();
        if (array.length < size) {
        	array = (short[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
        }
        doGetAll(array, 0, size);
        if (array.length > size) {
        	array[size] = (short)0;
        }
        return array;
	}

	/**
	 * Helper method to fill the specified elements in an array.
	 *
	 * @param array	array to store the list elements
	 * @param index	index of first element to copy
	 * @param len	number of elements to copy
	 * @param  type of elements stored in the list
	 */
	protected  void doGetAll(short[] array, int index, int len) {
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
     * @throws NullPointerException if the specified collection is (short)0
     */
	
	public boolean addAll(Collection<Short> coll) {
        // ArrayList.addAll() also first creates an array containing the
        // collection elements. This guarantees that the list's capacity
        // must only be increased once.
        @SuppressWarnings("unchecked")
        short[] array = (short[]) toArray(coll);
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
     * @throws NullPointerException if the specified collection is (short)0
     */
    
    public boolean addAll(int index, Collection<Short> coll) {
        checkIndexAdd(index);

        // ArrayList.addAll() also first creates an array containing the
        // collection elements. This guarantees that the list's capacity
        // must only be increased once.
        @SuppressWarnings("unchecked")
        short[] array = (short[]) toArray(coll);
        return doAddAll(index, array);
    }

    /**
     * Adds all specified elements into this list.
     *
     * @param elems elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     */
	public boolean addAll(short... elems) {
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
    public boolean addAll(int index, short... elems) {
        checkIndexAdd(index);

        return doAddAll(index, elems);
    }

    /**
     * Adds all of the elements in the specified list into this list.
     *
     * @param list collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified list is (short)0
     */
    @SuppressWarnings("unchecked")
    public boolean addAll(ShortGapList list) {
        return doAddAll(-1, (short[]) list.toArray());
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
     * @throws NullPointerException if the specified collection is (short)0
     */
	@SuppressWarnings("unchecked")
    public boolean addAll(int index, ShortGapList list) {
		checkIndexAdd(index);

		return doAddAll(index, (short[]) list.toArray());
	}

    /**
     * Helper method for adding multiple elements to the ShortGapList.
     * It still calls doAdd() for adding each element.
     *
     * @param index index where element should be added
     *              (-1 is valid for adding at the end)
     * @param array array with elements to add
     * @return      true if elements have been added, false otherwise
     */
	protected boolean doAddAll(int index, short[] array) {
        ensureCapacity(size() + array.length);

		if (array.length == 0) {
			return false;
		}
		for (short elem: array) {
			doAdd(index, elem);
			if (index != -1) {
			    index++;
			}
		}
		return true;
	}

	// Iterators

	
	

    
	

    
	

	// List operations

    
    public short getFirst() {
    	return doGet(0);
    }

    
    public short getLast() {
    	return doGet(size()-1);
    }

    
    public void addFirst(short elem) {
    	add(0, elem);
    }

    
    public void addLast(short elem) {
    	add(elem);
    }

    
    public short removeFirst() {
    	return doRemove(0);
    }

    
    public short removeLast() {
    	return doRemove(size()-1);
    }

    // Queue operations

    
    public short peek() {
        if (size() == 0) {
            return (short)0;
        }
        return getFirst();
    }

    
    public short element() {
        return getFirst();
    }

    
    public short poll() {
        if (size() == 0) {
            return (short)0;
        }
        return removeFirst();
    }

	
    public short remove() {
        return removeFirst();
    }

	
    public boolean offer(short elem) {
        return add(elem);
    }

    // Deque operations

	
	

	
	public boolean offerFirst(short elem) {
        addFirst(elem);
        return true;
	}

	
	public boolean offerLast(short elem) {
        addLast(elem);
        return true;
	}

	
	public short peekFirst() {
        if (size() == 0) {
            return (short)0;
        }
        return getFirst();
	}

	
	public short peekLast() {
        if (size() == 0) {
            return (short)0;
        }
        return getLast();
	}

	
	public short pollFirst() {
        if (size() == 0) {
            return (short)0;
        }
        return removeFirst();
	}

	
	public short pollLast() {
        if (size() == 0) {
            return (short)0;
        }
        return removeLast();
	}

	
	public short pop() {
        return removeFirst();
	}

	
	public void push(short elem) {
        addFirst(elem);
	}

	
	public boolean removeFirstOccurrence(short elem) {
		int index = indexOf(elem);
		if (index == -1) {
			return false;
		}
		doRemove(index);
		return true;
	}

	
	public boolean removeLastOccurrence(short elem) {
		int index = lastIndexOf(elem);
		if (index == -1) {
			return false;
		}
		doRemove(index);
		return true;
	}

	// --- Serialization ---

    /**
     * Serialize a ShortGapList object.
     *
     * @serialData The length of the array backing the <tt>ShortGapList</tt>
     *             instance is emitted (int), followed by all of its elements
     *             (each an <tt>short</tt>) in the proper order.
     * @param oos  output stream for serialization
     * @throws 	   IOException if serialization fails
     */
    private void writeshort(ObjectOutputStream oos) throws IOException {
        // Write out array length
	    int size = size();
        oos.writeInt(size);

        // Write out all elements in the proper order.
        for (int i=0; i<size; i++) {
        	oos.writeShort(doGet(i));
        }
    }

    /**
     * Deserialize a ShortGapList object.
 	 *
     * @param ois  input stream for serialization
     * @throws 	   IOException if serialization fails
     * @throws 	   ClassNotFoundException if serialization fails
     */
    @SuppressWarnings("unchecked")
	private void readshort(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // Read in array length and allocate array
        size = ois.readInt();
        values = (short[]) new short[size];

        // Read in all elements in the proper order.
        for (int i=0; i<size; i++) {
            values[i] = (short) ois.readShort();
        }
    }

    // --- Static bulk methods working with two GapLists ---

    /**
     * Moves elements from one ShortGapList to another.
     *
     * @param src		source list
     * @param srcIndex	index of first element in source list
     * @param dst		destination list
     * @param dstIndex	index of first element in source list
     * @param len		number of elements to move
     * @param  		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public static  void move(ShortGapList src, int srcIndex, ShortGapList dst, int dstIndex, int len) {
        if (src == dst) {
            src.move(srcIndex, dstIndex, len);

        } else {
            src.checkRange(srcIndex, len);
            dst.checkRange(dstIndex, len);

    		for (int i=0; i<len; i++) {
    			short elem = src.doReSet(srcIndex+i, (short)0);
    			dst.doSet(dstIndex+i, elem);
    		}
        }
    }

    /**
     * Copies elements from one ShortGapList to another.
     *
     * @param src		source list
     * @param srcIndex	index of first element in source list
     * @param dst		destination list
     * @param dstIndex	index of first element in source list
     * @param len		number of elements to copy
     * @param  		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public static  void copy(ShortGapList src, int srcIndex, ShortGapList dst, int dstIndex, int len) {
        if (src == dst) {
            src.copy(srcIndex, dstIndex, len);

        } else {
            src.checkRange(srcIndex, len);
            dst.checkRange(dstIndex, len);

    		for (int i=0; i<len; i++) {
    			short elem = src.doGet(srcIndex+i);
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
     * @param  		type of elements stored in the list
     * @throws 			IndexOutOfBoundsException if the ranges are invalid
     */
    public static  void swap(ShortGapList src, int srcIndex, ShortGapList dst, int dstIndex, int len) {
        if (src == dst) {
            src.swap(srcIndex, dstIndex, len);

        } else {
        	src.checkRange(srcIndex, len);
        	dst.checkRange(dstIndex, len);

        	if (src != dst) {
        		for (int i=0; i<len; i++) {
            		short swap = src.doGet(srcIndex+i);
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
     * @return      ShortGapList containing the specified range of elements from list
     */
    public ShortGapList getAll(int index, int len) {
        checkRange(index, len);

        ShortGapList list = new ShortGapList(len);
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
     * @return      ShortGapList containing the specified range of elements from list
     */
    public short[] getArray(int index, int len) {
        checkRange(index, len);

        @SuppressWarnings("unchecked")
        short[] array = (short[]) new short[len];
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
    public void setAll(int index, ShortGapList list) {
    	// There is a special implementation accepting a ShortGapList
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
    public void setAll(int index, Collection<Short> coll) {
        checkRange(index, coll.size());

        // In contrary to addAll() there is no need to first create an array
        // containing the collection elements, as the list will not grow.
        int i = 0;
        Iterator<Short> iter = coll.iterator();
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
    public void setAll(int index, short... elems) {
        checkRange(index, elems.length);

        doSetAll(index, elems);
    }

    /**
     * Replaces the specified elements.
     *
     * @param index index of first element to set
     * @param elems elements to set
     */
    protected void doSetAll(int index, short[] elems) {
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
	public void init(int len, short elem) {
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
	public void resize(int len, short elem) {
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
    public void fill(short elem) {
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
    public void fill(int index, int len, short elem) {
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
     * The elements which are moved away are set to (short)0.
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

    	// Set elements to (short)0 after the move operation
    	if (srcIndex < dstIndex) {
    		int fill = Math.min(len, dstIndex-srcIndex);
    		fill(srcIndex, fill, (short)0);
    	} else if (srcIndex > dstIndex) {
    		int fill = Math.min(len, srcIndex-dstIndex);
    		fill(srcIndex+len-fill, fill, (short)0);
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
    		short swap = doGet(pos1);
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
    		short swap = doGet(index1+i);
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
            short elem = doGet(index+start);
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
     * 						((short)0 means the elements natural ordering should be used)
     *
     * @see Arrays#sort
     */
    public void sort() {
    	sort(0, size());
    }

    /**
     * Sort specified elements in the list using the specified comparator.
     *
     * @param index			index of first element to sort
     * @param len			number of elements to sort
     * @param comparator	comparator to use for sorting
     * 						((short)0 means the elements natural ordering should be used)
     *
     * @see Arrays#sort
     */
    public void sort(int index, int len) {
    	checkRange(index, len);

    	normalize();
    	ArraysHelper.sort(values, index, index+len);
    }

    /*
    Question:
       Why is the signature of method binarySearch
           public  int binarySearch(short key)
       and not
           public int binarySearch(short key)
       as you could expect?

    Answer:
       This allows to use the binarySearch method not only with keys of
       the type stored in the ShortGapList, but also with any other type you
       are prepared to handle in you Comparator.
       So if we have a class Name and its comparator as defined in the
       following code snippets, both method calls are possible:

       new ShortGapList<Name>().binarySearch(new Name("a"), new NameComparator());
       new ShortGapList<Name>().binarySearch("a), new NameComparator());

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

       static class NameComparator implements Comparator<short> {
           
           public int compare(short o1, short o2) {
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
     *                      A <tt>(short)0</tt> value indicates that the elements'
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
    public  int binarySearch(short key) {
    	return binarySearch(0, size(), key);
    }

    /**
     * Searches the specified range for an object using the binary
     * search algorithm.
     *
     * @param index         index of first element to search
     * @param len           number of elements to search
     * @param key           the value to be searched for
     * @param comparator    the comparator by which the list is ordered.
     *                      A <tt>(short)0</tt> value indicates that the elements'
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
    public  int binarySearch(int index, int len, short key) {
    	checkRange(index, len);

    	normalize();
    	return ArraysHelper.binarySearch((short[]) values, index, index+len, key);
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
	 * Private method to check invariant of ShortGapList.
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

		// Check that gap positions contain (short)0 values
		if (gapSize > 0) {
			for (int i=gapStart; i<gapStart+gapSize; i++) {
				int pos = (i % values.length);
				assert(values[pos] == (short)0);
			}
		}

		// Check that all end positions contain (short)0 values
		if (end != start) {
			for (int i=end; i<start; i++) {
				int pos = (i % values.length);
				assert(values[pos] == (short)0);
			}
		}
	}

	/**
	 * Private method to determine state of ShortGapList.
	 * It is only used for debugging.
	 *
	 * @return	state in which ShortGapList is
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
	 * Private method to dump fields of ShortGapList.
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
	private String debugPrint(short[] values) {
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

    // 

    // 

}
