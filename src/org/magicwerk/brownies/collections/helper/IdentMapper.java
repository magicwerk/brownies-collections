package org.magicwerk.brownies.collections.helper;

import org.magicwerk.brownies.collections.function.Mapper;

/**
 * Identity mapper.
 */
public class IdentMapper<E> implements Mapper<E, E> {

    public static final IdentMapper INSTANCE = new IdentMapper();

    IdentMapper() {
    }

    @Override
    public E getKey(E v) {
        return v;
    }
}