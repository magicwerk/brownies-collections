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
     * Returns key for given value.
     *
     * @param v value to determine key for
     * @return  determined key value
     */
    public K getKey(E v);
}
