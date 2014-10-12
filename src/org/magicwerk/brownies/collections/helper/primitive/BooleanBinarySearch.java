package org.magicwerk.brownies.collections.helper.primitive;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.magicwerk.brownies.collections.helper.ArraysHelper;
import org.magicwerk.brownies.collections.helper.NaturalComparator;
import org.magicwerk.brownies.collections.primitive.IBooleanList;


/**
 *
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class BooleanBinarySearch {
  /**
   * Searches the specified list for the specified object using the binary search algorithm. The
   * list must be sorted into ascending order according to the specified comparator (as by the
   * {@link Collections#sort(List, Comparator) Collections.sort(List, Comparator)} method), prior
   * to making this call. If it is not sorted, the results are undefined.
   *
   * <p>If there are elements in the list which compare as equal to the key, the choice of
   * {@link KeyPresentBehavior} decides which index is returned. If no elements compare as equal to
   * the key, the choice of {@link KeyAbsentBehavior} decides which index is returned.
   *
   * <p>This method runs in log(n) time on random-access lists, which offer near-constant-time
   * access to each list element.
   *
   * @param list the list to be searched.
   * @param key the value to be searched for.
   * @param comparator the comparator by which the list is ordered.
   * @param presentBehavior the specification for what to do if at least one element of the list
   *        compares as equal to the key.
   * @param absentBehavior the specification for what to do if no elements of the list compare as
   *        equal to the key.
   * @return the index determined by the {@code KeyPresentBehavior}, if the key is in the list;
   *         otherwise the index determined by the {@code KeyAbsentBehavior}.
   */
  public static int binarySearch(IBooleanList list, boolean key, int lower, int upper) {
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
            } else { // c == 0
              upper = middle;
            }
          }
          return lower;
      }
    }
    return ~lower;
  }
}
