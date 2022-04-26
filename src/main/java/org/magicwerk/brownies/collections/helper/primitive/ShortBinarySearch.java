// ---
// --- DO NOT EDIT
// --- AUTOMATICALLY GENERATED FILE
// ---
/*
 * Copyright 2014 by Thomas Mauch
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
package org.magicwerk.brownies.collections.helper.primitive;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.magicwerk.brownies.collections.helper.ArraysHelper;
import org.magicwerk.brownies.collections.helper.NaturalComparator;
import org.magicwerk.brownies.collections.primitive.IShortList;

/**
 * Binary search for primitive type short.
 *
 * @author Thomas Mauch
 */
public class ShortBinarySearch {

    /**
     *  Searches the specified list for the specified object using the binary search algorithm. The
     *  list must be sorted into ascending order according to the specified comparator (as by the
     *  {@link Collections#sort(List, Comparator) Collections.sort(List, Comparator)} method), prior
     *  to making this call. If it is not sorted, the results are undefined.
     *
     *  <p>This method runs in log(n) time on random-access lists, which offer near-constant-time
     *  access to each list element.
     *
     *  @param list 	the list to be searched.
     *  @param key 	the value to be searched for.
     *  @param lower 	lower bound of range to search
     *  @param upper 	upper bound of range to search
     *  @return 		the index of the search key, if it is contained in the list;
     * 	       		otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.
     */
    public static int binarySearch(IShortList list, short key, int lower, int upper) {
        while (lower <= upper) {
            int middle = (lower + upper) >>> 1;
            int c = ArraysHelper.compare(key, list.get(middle));
            if (c < 0) {
                upper = middle - 1;
            } else if (c > 0) {
                lower = middle + 1;
            } else {
                // Of course, we have to use binary search to find the precise breakpoint...
                // Everything between lower and upper inclusive compares at <= 0.
                while (lower < upper) {
                    middle = (lower + upper) >>> 1;
                    c = ArraysHelper.compare(list.get(middle), key);
                    if (c < 0) {
                        lower = middle + 1;
                    } else {
                        // c == 0
                        upper = middle;
                    }
                }
                return lower;
            }
        }
        return ~lower;
    }
}
