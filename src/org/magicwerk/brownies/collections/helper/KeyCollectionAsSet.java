package org.magicwerk.brownies.collections.helper;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.magicwerk.brownies.collections.KeyCollectionImpl;
import org.magicwerk.brownies.collections.exceptions.DuplicateKeyException;

/**
 * Implements a Set based on a Collection.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class KeyCollectionAsSet<K> extends CollectionAsSet<K> {

    public KeyCollectionAsSet(KeyCollectionImpl<K> coll, boolean immutable) {
    	super(coll, immutable);
    }

	@Override
	public boolean add(K e) {
		checkMutable();
		try {
			return coll.add(e);
		}
		catch (DuplicateKeyException ex) {
			return false;
		}
	}

}