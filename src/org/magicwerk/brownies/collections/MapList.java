/*
 * Copyright 2012 by Thomas Mauch
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.magicwerk.brownies.collections.KeyList.Handler;
import org.magicwerk.brownies.collections.KeyList.NullMode;
import org.magicwerk.brownies.collections.SetList.Builder;


/**
 * Think about MapList as of a Map where you can also access the
 * elements by index. Also the key is automatically extracted
 * from the element. Typically the elements are in the order
 * specified by the list, but you can also let them order
 * automatically like in TreeMap.
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @see SetList
 * @param <E> type of elements stored in the list
 * @param <K> type of key
 */
public class MapList<E, K> extends KeyList<E> {

	public static class EntryMapper<E, K> implements Mapper<Entry<E, K>, K> {
		@Override
		public K getKey(Entry<E, K> entry) {
			return entry.getKey();
		}
	}

	public static class Entry<E, K> {

		public static <E, K> Mapper<E, K> getMapper() {
			return (Mapper<E, K>) new EntryMapper<E, K>();
		}

		E elem;
		K key;

		public Entry(E elem, K key) {
			this.elem = elem;
			this.key = key;
		}

		public K getKey() {
			return key;
		}

		public E getElem() {
			return elem;
		}

		public void setElem(E elem) {
			this.elem = elem;
		}
	}


	/**
     * Builder to construct MapList instances.
     */
    public static class Builder<E, K> extends KeyList.Builder<E> {

        /**
         * Constructor.
         *
         * @param mapper mapper to use
         */
        public Builder(Mapper<E, K> mapper) {
        	withKey((Mapper<E, Object>) mapper);
        }

        /**
         * Internal constructor
         *
         * @param mapList   mapList to customize
         */
        Builder(MapList<E, K> mapList) {
            this.keyList = mapList;
        }

        /**
         * Build MapList with specified options.
         *
         * @return  MapList with specified options
         */
        public MapList<E, K> build() {
            return (MapList<E, K>) build();
        }

        // --- Methods overridden to change return type

		@Override
        public Builder<E, K> withInsertTrigger(Handler<E> handler) {
            return (Builder<E, K>) super.withInsertTrigger(handler);
        }

		@Override
        public Builder<E, K> withDeleteTrigger(Handler<E> handler) {
            return (Builder<E, K>) super.withDeleteTrigger(handler);
        }

		@Override
		public Builder<E, K> withCapacity(int capacity) {
			return (Builder<E, K>) super.withCapacity(capacity);
		}

		@Override
		public Builder<E, K> withElements(Collection<? extends E> elements) {
			return (Builder<E, K>) super.withElements(elements);
		}

		@Override
		public Builder<E, K> withElements(E... elements) {
			return (Builder<E, K>) super.withElements(elements);
		}

    }


    /** UID for serialization. */
    private static final long serialVersionUID = -927503847084195522L;


    /**
     * Default constructor.
     * Internal use in builder and child classes only.
     */
    protected MapList(boolean ignore) {
    }

    /**
     * Copy constructor.
     * Internal use in copy() and crop() only.
     *
     * @param that source list
     */
    MapList(MapList<E, K> that) {
        super(that);
    }

    /**
     * Create builder for this class.
     * Internal use in child classes only.
     *
     * @param mapper 	mapper to use
     * @return builder for this class
     */
    protected Builder<E,K> getBuilder(Mapper<E, K> mapper) {
        return builder(mapper);
    }

    public static <E, K> MapList.Builder<E, K> builder(Mapper<E, K> mapper) {
        return new MapList.Builder<E, K>(mapper);
    }

    @Override
    public MapList<E, K> copy() {
        MapList<E, K> copy = new MapList<E, K>(this);
        copy.initCopy(this);
        return copy;
    }

    /**
     * Returns a copy this list but without elements.
     * The new list will use the same comparator, ordering, etc.
     *
     * @return  an empty copy of this instance
     */
    public MapList<E, K> crop() {
        MapList<E, K> copy = new MapList<E, K>(this);
        copy.initCrop(this);
        return copy;
    }

    public Mapper<E, K> getMapper() {
        return (Mapper<E, K>) getKeyMap(0).mapper;
    }

    /**
     * Returns a view on the MapList as Map.
     * Note that a class cannot implement both List and Map.
     * Reason is the conflicting definition of the remove() method:  <br/>
     * - Map:  V remove(Object key) <br/>
     * - List: boolean remove(Object obj) <br/>
     *
     * @return view of this MapList as Map
     */
    public Map<K, E> asMap() {
    	return asMap(0);

    }
    public Map<K, E> asMap(final int keyIndex) {
        return new Map<K, E>() {

            @Override
            public int size() {
                return MapList.this.size();
            }

            @Override
            public boolean isEmpty() {
                return MapList.this.isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                return MapList.this.getByKey(keyIndex, (K) key) != null;
            }

            @Override
            public boolean containsValue(Object value) {
                return MapList.this.contains(value);
            }

            @Override
            public E get(Object key) {
                return MapList.this.getByKey(keyIndex, (K) key);
            }

            @Override
            public E put(K key, E value) {
                throw new UnsupportedOperationException();
            	//if (!getMapper().getKey(value).equals(key)) {
//            		throw new IllegalArgumentException("Key is not equal to key created by mapper");
            	//}
//                return MapList.this.add(value); TODO
            }

            @Override
            public E remove(Object key) {
                return MapList.this.removeByKey(keyIndex, (K) key);
            }

            @Override
            public void putAll(Map<? extends K, ? extends E> m) {
                throw new UnsupportedOperationException();
                //for (E value: m.values()) {
                    //MapList.this.put(value);
                //} TODO
            }

            @Override
            public void clear() {
                MapList.this.clear();
            }

            @Override
            public Set<K> keySet() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Collection<E> values() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<java.util.Map.Entry<K, E>> entrySet() {
                throw new UnsupportedOperationException();
            }

        };
    }

}
