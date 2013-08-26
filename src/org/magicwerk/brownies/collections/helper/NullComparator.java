package org.magicwerk.brownies.collections.helper;

import java.util.Comparator;

/**
 * A comparator which can handle null values.
 */
public class NullComparator<K> implements Comparator<K> {
    private Comparator<K> comparator;
    private boolean nullsFirst;

    public NullComparator(Comparator<K> comparator, boolean nullsFirst) {
        this.comparator = comparator;
        this.nullsFirst = nullsFirst;
    }

    @Override
    public int compare(K key1, K key2) {
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