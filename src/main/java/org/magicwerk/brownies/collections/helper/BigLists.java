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
package org.magicwerk.brownies.collections.helper;

import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.primitive.BooleanObjBigList;
import org.magicwerk.brownies.collections.primitive.ByteObjBigList;
import org.magicwerk.brownies.collections.primitive.CharObjBigList;
import org.magicwerk.brownies.collections.primitive.DoubleObjBigList;
import org.magicwerk.brownies.collections.primitive.FloatObjBigList;
import org.magicwerk.brownies.collections.primitive.IntObjBigList;
import org.magicwerk.brownies.collections.primitive.LongObjBigList;
import org.magicwerk.brownies.collections.primitive.ShortObjBigList;

/**
 * Helper class to create wrapper list objects wrapping primitive BigLists.
 *
 * @author Thomas Mauch
 */
public class BigLists {
    /**
     * Create a BigList wrapping a primitive BigList, e.g. an IntObjBigList wrapping an IntBigList.
     *
     * @param type	primitive type for BigList
     * @return		created wrapping BigList
     * @throws 		IllegalArgumentException if no primitive type is specified
     */
    public static IList<?> createWrapperList(Class<?> type) {
    	if (type == int.class) {
    		return new IntObjBigList();
    	} else if (type == long.class) {
        	return new LongObjBigList();
    	} else if (type == double.class) {
        	return new DoubleObjBigList();
    	} else if (type == float.class) {
        	return new FloatObjBigList();
    	} else if (type == boolean.class) {
        	return new BooleanObjBigList();
    	} else if (type == byte.class) {
        	return new ByteObjBigList();
    	} else if (type == char.class) {
        	return new CharObjBigList();
    	} else if (type == short.class) {
        	return new ShortObjBigList();
    	} else {
    		throw new IllegalArgumentException("Primitive type expected: " + type);
    	}
    }

}
