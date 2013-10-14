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
package org.magicwerk.brownies.collections;

import java.util.Collection;

import org.magicwerk.brownies.collections.exceptions.DuplicateKeyException;

/**
 * Implements a Set based on a Collection.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class KeyCollectionAsSet<E> extends CollectionAsSet<E> {

    public KeyCollectionAsSet(KeyCollectionImpl<E> coll, boolean immutable) {
    	super(coll, immutable);
    }

	@Override
	public boolean add(E e) {
		checkMutable();
		try {
			return coll.add(e);
		}
		catch (DuplicateKeyException ex) {
			return false;
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		checkMutable();
		boolean changed = false;
		for (E e: c) {
			if (add(e)) {
				changed = true;
			}
		}
		return changed;
	}

}