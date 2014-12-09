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

import java.io.Serializable;

import org.magicwerk.brownies.collections.function.IFunction;

/**
 * Identity mapper.
 *
 * @param <E>	element type
 *
 * @author Thomas Mauch
 * @version $Id$
 */
@SuppressWarnings("serial")
public class IdentMapper<E> implements IFunction<E, E>, Serializable {

	/** Singleton instance */
    @SuppressWarnings("rawtypes")
	public static final IdentMapper INSTANCE = new IdentMapper();


    /**
     * Prevent construction.
     */
    private IdentMapper() {
    }

    @Override
    public E apply(E v) {
        return v;
    }
}