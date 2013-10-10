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
 * A NullComparator extends an existing comparator so it can handle null values.
 *
 * @param <T>	element type
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class NullComparator<T> implements Comparator<T> {
    private Comparator<T> comparator;
    private boolean nullsFirst;

    /**
     * Constructor.
     *
     * @param comparator	comparator to extend
     * @param nullsFirst	true to sort nulls first, false to sort null last
     */
    public NullComparator(Comparator<T> comparator, boolean nullsFirst) {
        this.comparator = comparator;
        this.nullsFirst = nullsFirst;
    }

    @Override
    public int compare(T key1, T key2) {
        if (key1 != null && key2 != null) {
            return comparator.compare(key1, key2);
        }
        if (key1 == null) {
            if (key2 == null) {
                return 0;
            } else {
                return nullsFirst ? -1 : 1;
            }
        } else {
            assert(key2 == null);
            return nullsFirst ? 1 : -1;
        }
    }
}