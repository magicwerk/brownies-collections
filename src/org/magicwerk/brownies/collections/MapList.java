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
public class MapList<E, K> extends KeyList<E, K> {

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
    public static class Builder<E, K> extends KeyList.Builder<E, K> {

        /**
         * Constructor.
         *
         * @param mapper mapper to use
         */
        public Builder(Mapper<E, K> mapper) {
        	this.mapper = mapper;
        }

        /**
         * Internal constructor
         *
         * @param mapList   mapList to customize
         * @param mapper	mapper to use
         */
        Builder(MapList<E, K> mapList, Mapper<E, K> mapper) {
            this.keyList = mapList;
            this.mapper = mapper;
        }

        /**
         * Determines that null elements are allowed.
         * A null element will have a null key.
         *
         * @return  this (fluent interface)
         */
        public Builder<E, K> withNullElem() {
            return withNullElem(true);
        }

        /**
         * Determines whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param nullElem      true to allow null elements, false otherwise
         * @return              this (fluent interface)
         */
        public Builder<E, K> withNullElem(boolean nullElem) {
            this.allowNullElem = nullElem;
            if (nullElem) {
                this.nullMode = NullMode.NORMAL;
            }
            return this;
        }

        /**
         * Build MapList with specified options.
         *
         * @return  MapList with specified options
         */
        public MapList<E, K> build() {
            return (MapList<E, K>) doBuild();
        }

        // --- Methods overridden to change return type

		@Override
        public Builder<E, K> withAttachHandler(Handler<E> handler) {
            return withAttachHandler(handler);
        }

		@Override
        public Builder<E, K> withDetachHandler(Handler<E> handler) {
            return withDetachHandler(handler);
        }

		@Override
		public Builder<E, K> withCapacity(int capacity) {
			return (Builder<E, K>) super.withCapacity(capacity);
		}

		@Override
		public Builder<E, K> withDuplicates(DuplicateMode mode) {
			return (Builder<E, K>) super.withDuplicates(mode);
		}

		@Override
		public Builder<E, K> withElements(Collection<? extends E> elements) {
			return (Builder<E, K>) super.withElements(elements);
		}

		@Override
		public Builder<E, K> withElements(E... elements) {
			return (Builder<E, K>) super.withElements(elements);
		}

		@Override
		public Builder<E, K> withNull(NullMode nullMode) {
			return (Builder<E, K>) super.withNull(nullMode);
		}

		@Override
		public Builder<E, K> withSort() {
			return (Builder<E, K>) super.withSort();
		}

		@Override
		public Builder<E, K> withComparator(Comparator<? super K> comparator) {
			return (Builder<E, K>) super.withComparator(comparator);
		}
    }


    /** UID for serialization. */
    private static final long serialVersionUID = -927503847084195522L;

    // MapList constructors

    public MapList(Mapper<E, K> mapper) {
    	getBuilder(mapper).build();
    }

    public MapList(Mapper<E, K> mapper, int capacity) {
    	getBuilder(mapper).withCapacity(capacity).build();
    }

    public MapList(Mapper<E, K> mapper, Collection<? extends E> elements) {
    	getBuilder(mapper).withElements(elements).build();
    }

    public MapList(Mapper<E, K> mapper, E... elements) {
    	getBuilder(mapper).withElements(elements).build();

    }

    // Create MapList

    public static <E, K> MapList<E, K> create(Mapper<E, K> mapper) {
    	return new Builder<E, K>(mapper).build();
    }

    public static <E, K> MapList<E, K> create(Mapper<E, K> mapper, int capacity) {
    	return new Builder<E, K>(mapper).withCapacity(capacity).build();
    }

    public static <E, K> MapList<E, K> create(Mapper<E, K> mapper, Collection<? extends E> elements) {
    	return new Builder<E, K>(mapper).withElements(elements).build();
    }

    public static <E, K> MapList<E, K> create(Mapper<E, K> mapper, E... elements) {
    	return new Builder<E, K>(mapper).withElements(elements).build();

    }

    // Create HashMap

    public static <E, K> MapList<E, K> createHashMap(Mapper<E, K> mapper) {
    	return new Builder<E, K>(mapper).withNull(NullMode.NORMAL).build();
    }

    public MapList<E, K> createHashMap(Mapper<E, K> mapper, int capacity) {
    	return new Builder<E, K>(mapper).withNull(NullMode.NORMAL).withCapacity(capacity).build();
    }

    public MapList<E, K> createHashMap(Mapper<E, K> mapper, Collection<? extends E> elements) {
    	return new Builder<E, K>(mapper).withNull(NullMode.NORMAL).withElements(elements).build();
    }

    public MapList<E, K> createHashMap(Mapper<E, K> mapper, E... elements) {
    	return new Builder<E, K>(mapper).withNull(NullMode.NORMAL).withElements(elements).build();
    }

    // Create TreeMap

    public MapList<E, K> createTreeMap(Mapper<E, K> mapper) {
    	return new Builder<E, K>(mapper).withSort().withNull(NullMode.NORMAL).build();
    }

    public MapList<E, K> createTreeMap(Mapper<E, K> mapper, int capacity) {
    	return new Builder<E, K>(mapper).withSort().withNull(NullMode.NORMAL).withCapacity(capacity).build();
    }

    public MapList<E, K> createTreeMap(Mapper<E, K> mapper, Collection<? extends E> elements) {
    	return new Builder<E, K>(mapper).withSort().withNull(NullMode.NORMAL).withElements(elements).build();
    }

    public MapList<E, K> createTreeMap(Mapper<E, K> mapper, E... elements) {
    	return new Builder<E, K>(mapper).withSort().withNull(NullMode.NORMAL).withElements(elements).build();
    }

    // TreeMap with comparator

    public MapList<E, K> createTreeMap(Mapper<E, K> mapper, Comparator<? super K> comparator) {
    	return new Builder<E, K>(mapper).withComparator(comparator).withNull(NullMode.NORMAL).build();
    }

    public MapList<E, K> createTreeMap(Mapper<E, K> mapper, Comparator<? super K> comparator, int capacity) {
    	return new Builder<E, K>(mapper).withComparator(comparator).withNull(NullMode.NORMAL).withCapacity(capacity).build();
    }

    public MapList<E, K> createTreeMap(Mapper<E, K> mapper, Comparator<? super K> comparator, Collection<? extends E> elements) {
    	return new Builder<E, K>(mapper).withComparator(comparator).withNull(NullMode.NORMAL).withElements(elements).build();
    }

    public MapList<E, K> createTreeMap(Mapper<E, K> mapper, Comparator<? super K> comparator, E... elements) {
    	return new Builder<E, K>(mapper).withComparator(comparator).withNull(NullMode.NORMAL).withElements(elements).build();
    }

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
        return new Builder<E,K>(this, mapper);
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

    @Override
    public Mapper<E, K> getMapper() {
        return mapper;
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
                return MapList.this.getByKey((K) key) != null;
            }

            @Override
            public boolean containsValue(Object value) {
                return MapList.this.contains(value);
            }

            @Override
            public E get(Object key) {
                return MapList.this.getByKey((K) key);
            }

            @Override
            // Note that the key is ignored (it is generated out of the value)
            public E put(K key, E value) {
                return MapList.this.put(value);
            }

            @Override
            public E remove(Object key) {
                return MapList.this.removeByKey((K) key);
            }

            @Override
            public void putAll(Map<? extends K, ? extends E> m) {
                for (E value: m.values()) {
                    MapList.this.put(value);
                }
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
