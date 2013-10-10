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

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.primitive.BooleanObjGapList;
import org.magicwerk.brownies.collections.primitive.ByteObjGapList;
import org.magicwerk.brownies.collections.primitive.CharObjGapList;
import org.magicwerk.brownies.collections.primitive.DoubleObjGapList;
import org.magicwerk.brownies.collections.primitive.FloatObjGapList;
import org.magicwerk.brownies.collections.primitive.IntObjGapList;
import org.magicwerk.brownies.collections.primitive.LongObjGapList;
import org.magicwerk.brownies.collections.primitive.ShortObjGapList;

/**
 * Helper class to create wrapper list objects wrapping primitive GapLists.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class GapLists {
    /**
     * Create a GapList wrapping a primitive GapList, e.g. IntObjGapList.
     *
     * @param type	primitive type for GapList
     * @return		create wrapping GapList
     * @throws 		IllegalArgumentException if no primitive type is specified
     */
    public static GapList<?> createWrapperList(Class<?> type) {
    	if (type == int.class) {
    		return new IntObjGapList();
    	} else if (type == long.class) {
        	return new LongObjGapList();
    	} else if (type == double.class) {
        	return new DoubleObjGapList();
    	} else if (type == float.class) {
        	return new FloatObjGapList();
    	} else if (type == boolean.class) {
        	return new BooleanObjGapList();
    	} else if (type == byte.class) {
        	return new ByteObjGapList();
    	} else if (type == char.class) {
        	return new CharObjGapList();
    	} else if (type == short.class) {
        	return new ShortObjGapList();
    	} else {
    		throw new IllegalArgumentException("Primitive type expected: " + type);
    	}
    }

}
