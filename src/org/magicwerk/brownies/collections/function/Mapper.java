package org.magicwerk.brownies.collections.function;

/**
 * Interface to determine a map value out of a given value.
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @param <E> type of elements stored in the list
 * @param <K> type of key
 */
public interface Mapper<E, K> {
    /**
     * Return key for given value.
     *
     * @param v value to determine key for
     * @return  determined key value
     */
    public K getKey(E v);
}
