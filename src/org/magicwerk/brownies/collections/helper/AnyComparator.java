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

import java.util.Comparator;

/**
 * The AnyComparator can compare elements of any object type,
 * even if they do not implement Comparable. As the comparison
 * bases on the identity hash code, order is not stable
 * after serialization.
 *
 * @param <T>	element type
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class AnyComparator<T> implements Comparator<T> {
	/**
	 * Singleton instance.
	 */
	public static final AnyComparator INSTANCE = new AnyComparator();

	/**
	 * Prevent construction.
	 */
	private AnyComparator() {
	}

    @Override
    public int compare(T key1, T key2) {
        if (key1 == key2) {
            return 0;
        }
        int hash1 = key1.hashCode();
        int hash2 = key2.hashCode();
        // Prevent overflow
        int cmp = (hash1 < hash2 ? -1 : (hash1 > hash2 ? 1 : 0));
        if (cmp != 0) {
            return cmp;
        }
        // Objects have same hashcode
        if (key1.equals(key2)) {
            return 0;
        }
        // Objects have same hashcode, but are not equal, so use identity hash code to distinguish them
        hash1 = System.identityHashCode(key1);
        hash2 = System.identityHashCode(key2);
        return (hash1 < hash2 ? -1 : (hash1 > hash2 ? 1 : 0));
    }
}
