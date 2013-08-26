package org.magicwerk.brownies.collections.function;

/**
 * The Handler interface is used to customize the behavior
 * of onAttach() and onDetach(). These methods cannot be
 * overridden as the concrete instances are created by builders.
 */
public interface Trigger<E> {
    /**
     * Handle element.
     *
     * @param elem element to handle
     */
    public void handle(E elem);
}