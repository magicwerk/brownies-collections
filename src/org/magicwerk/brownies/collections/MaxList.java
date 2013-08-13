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

/**
 * A MaxList is a list with a fixed size.
 * If you add new elements and the list then contains too many elements,
 * the first n elements at the head of the list are removed.
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @see GapList
 * @param <E> type of elements stored in the list
 */
public class MaxList<E> extends GapList<E> {
	/** Generated serial version UID. */
	private static final long serialVersionUID = -3811092972773788082L;

	/** Maximum size of list */
	private int maxSize;


	/**
	 * Constructor.
	 *
	 * @param maxSize	maximum size of list
	 */
	public MaxList(int maxSize) {
		super(maxSize);

		this.maxSize = maxSize;
	}

	/**
	 * Copy constructor.
	 *
	 * @param that	source object to copy
	 */
	public MaxList(MaxList<E> that) {
		super(that);

		this.maxSize = that.maxSize;
	}

	/**
	 * Returns maximum size of list.
	 *
	 * @return	maximum size of list
	 */
	public int maxSize() {
		return maxSize;
	}

    @Override
    public void ensureCapacity(int minCapacity) {
        // Make sure that we never allocate more slots than needed.
        // The add methods make sure that we never use to many slots.
        super.ensureCapacity(Math.min(minCapacity, maxSize));
    }

	@Override
    protected boolean doAdd(int index, E elem) {
        if (size() < maxSize) {
            return super.doAdd(index, elem);
        } else {
            if (index > 0) {
                doRemove(0);
                return super.doAdd(index-1, elem);
            }
        }
        return false;
    }

	@Override
	protected boolean doAddAll(int index, E[] array) {
        checkIndexAdd(index);

        int addSize = array.length;
		if (addSize == 0) {
			return false;
		}
		int overlap = size()+addSize - maxSize;
		if (overlap > 0) {
			if (index >= overlap) {
				super.remove(0, overlap);
			} else {
				super.remove(0, index);
			}
			index = index - overlap;
		}
		if (index >= 0) {
			for (int i=0; i<addSize; i++) {
				super.doAdd(index+i, array[i]);
			}
		} else {
			for (int i=0; i<addSize+index; i++) {
				super.doAdd(0, array[i-index]);
			}
		}
		return true;
	}

}
