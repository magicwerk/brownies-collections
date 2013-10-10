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
package org.magicwerk.brownies.collections.helper;

import java.util.Comparator;

/**
 * Reverse comparator.
 *
 * @param <T> type of object to compare
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class ReverseComparator<T> implements Comparator<T> {
	/** Comparator to reverse */
	private Comparator<T> comparator;

	/**
	 * Create reverse comparator.
	 *
	 * @param comparator	comparator which must be reversed
	 */
	public ReverseComparator(Comparator<T> comparator) {
		this.comparator = comparator;
	}

    @Override
    public int compare(T key1, T key2) {
    	return comparator.compare(key2, key1);
    }

}
