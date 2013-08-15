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
 * $Id: TypeTools.java 1775 2013-07-10 22:53:57Z origo $
 */
package org.magicwerk.brownies.collections.helper;

import java.util.Comparator;

public class NaturalComparator<T> implements Comparator<T> {
	public static final NaturalComparator INSTANCE = new NaturalComparator();

	private NaturalComparator() {
	}

    public int compare(T o1, T o2) {
        return ((Comparable) o1).compareTo(o2);
    }
}
