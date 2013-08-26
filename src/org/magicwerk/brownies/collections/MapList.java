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
import java.util.Map;
import java.util.Set;

import org.magicwerk.brownies.collections.KeyList.Builder;
import org.magicwerk.brownies.collections.function.Mapper;
import org.magicwerk.brownies.collections.function.Predicate;
import org.magicwerk.brownies.collections.function.Trigger;


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
    public static class Builder<E, K> extends BuilderBase<E> {
        /**
         * Constructor.
         *
         * @param mapper mapper to use
         */
        public Builder(Mapper<E,K> mapper) {
        	newKeyMapBuilder((Mapper<E, Object>) mapper);
        }

        /**
         * Internal constructor
         *
         * @param mapList   mapList to customize
         */
        Builder(MapList<E,K> mapList, Mapper<E,K> mapper) {
            this.keyList = mapList;

            newKeyMapBuilder((Mapper<E, Object>) mapper);
        }

        /**
         * Allow null elements.
         *
         * @return this (fluent interface)
         */
        public Builder<E,K> withNull() {
        	return withNull(true);
        }

        /**
         * Specify whether null elements are allowed.
         *
         * @param allowNullElem	true to allow null elements
         * @return 				this (fluent interface)
         */
        public Builder<E,K> withNull(boolean allowNullElem) {
        	endKeyMapBuilder();
        	this.allowNullElem = allowNullElem;
        	return this;
        }

        /**
         * Specify element constraint.
         *
         * @param constraint	constraint element must satisfy
         * @return 				this (fluent interface)
         */
        public Builder<E,K> withConstraint(Predicate<E> constraint) {
        	endKeyMapBuilder();
        	this.constraint = constraint;
        	return this;
        }

        /**
         * Specify insert trigger.
         *
         * @param trigger	insert trigger method
         * @return			this (fluent interface)
         */
        public Builder<E,K> withInsertTrigger(Trigger<E> trigger) {
        	endKeyMapBuilder();
            this.insertTrigger = trigger;
            return this;
        }

        /**
         * Specify delete trigger.
         *
         * @param trigger	delete trigger method
         * @return			this (fluent interface)
         */
        public Builder<E,K> withDeleteTrigger(Trigger<E> trigger) {
        	endKeyMapBuilder();
            this.deleteTrigger = trigger;
            return this;
        }

        //-- Capacity / Elements

        public Builder<E,K> withCapacity(int capacity) {
        	endKeyMapBuilder();
            this.capacity = capacity;
            return this;
        }

        public Builder<E,K> withElements(Collection<? extends E> elements) {
        	endKeyMapBuilder();
            this.collection = elements;
            return this;
        }

        public Builder<E,K> withElements(E... elements) {
        	endKeyMapBuilder();
            this.array = elements;
            return this;
        }

        //-- Keys

        /**
         * Determines whether null elements are allowed or not.
         * A null element will have a null key.
         *
         * @param nullable  true to allow null elements, false to disallow
         * @return          this (for use in fluent interfaces)
         */
        public Builder<E,K> withKeyNull(boolean nullable) {
            return withKeyNull(nullable ? NullMode.NORMAL : NullMode.NONE);
        }

        public Builder<E,K> withKeyNull(NullMode nullMode) {
        	getKeyMapBuilder().nullMode = nullMode;
            return this;
        }

        /**
         * Determines whether duplicates are allowed or not.
         *
         * @param duplicateMode duplicate mode
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E,K> withKeyDuplicates(DuplicateMode duplicateMode) {
        	getKeyMapBuilder().duplicateMode = duplicateMode;
            return this;
        }

        /**
         * Determines whether list should be sorted or not.
         *
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E,K> withKeySort() {
            return withKeySort(true);
        }

        /**
         * Determines that list should be sorted.
         *
         * @param sort    true to sort list, otherwise false
         * @return        this (for use in fluent interfaces)
         */
        public Builder<E,K> withKeySort(boolean sort) {
        	getKeyMapBuilder().sort = sort;
            return this;
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator    comparator to use for sorting
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E,K> withKeyComparator(Comparator<? super K> comparator) {
            return withKeyComparator(comparator, false);
        }

        /**
         * Set comparator to use for sorting.
         *
         * @param comparator            comparator to use for sorting
         * @param comparatorSortsNull   true if comparator sorts null, false if not
         * @return                      this (for use in fluent interfaces)
         */
        public Builder<E,K> withKeyComparator(Comparator<? super K> comparator, boolean comparatorSortsNull) {
        	KeyMapBuilder<E, Object> kmb = getKeyMapBuilder();
        	kmb.comparator = comparator;
            kmb.comparatorSortsNull = comparatorSortsNull;
            return this;
        }

        /**
         * Determines that nulls are sorted first.
         *
         * @return  this (for use in fluent interfaces)
         */
        public Builder<E,K> withKeyNullsFirst() {
            return withKeyNullsFirst(true);
        }

        /**
         * Determines whether nulls are sorted first or last.
         *
         * @param nullsFirst    true to sort nulls first, false to sort nulls last
         * @return              this (for use in fluent interfaces)
         */
        public Builder<E,K> withKeyNullsFirst(boolean nullsFirst) {
        	getKeyMapBuilder().sortNullsFirst = nullsFirst;
            return this;
        }

        /**
         * @return created MapList
         */
        public MapList<E,K> build() {
        	if (keyList == null) {
               	keyList = new MapList<E,K>(false);
        	}
        	build(keyList);
        	return (MapList<E,K>) keyList;
        }
    }

    /** UID for serialization. */
    private static final long serialVersionUID = -927503847084195522L;


    /**
     * Default constructor.
     * Internal use in builder and child classes only.
     *
     * @param ignore ignored parameter for unique method signature
     */
    protected MapList(boolean ignore) {
    	super(ignore);
    }

    /**
     * Copy constructor.
     * Internal use in copy() and crop() only.
     *
     * @param that source list
     */
    MapList(MapList<E,K> that) {
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
        return new MapList.Builder<E, K>(mapper);
    }

    //-- Key methods

    public Mapper<E,K> getMapper() {
    	return (Mapper<E,K>) keyMaps[0].mapper;
    }

    public K getKey(E elem) {
    	return getMapper().getKey(elem);
    }

    public boolean containsKey(K key) {
    	return super.containsKey(0, key);
    }

    public int indexOfKey(K key) {
    	return super.indexOfKey(0, key);
    }

	public int getCountDistinctKeys() {
		return super.getCountDistinctKeys(0);
	}

	public GapList<K> getDistinctKeys() {
		return (GapList<K>) super.getDistinctKeys(0);
	}

	public E getByKey(K key) {
		return super.getByKey(0, key);
	}

	public GapList<E> getAllByKey(K key) {
		return super.getAllByKey(0, key);
	}

	public int getCountByKey(K key) {
		return super.getCountByKey(0, key);
	}

	public E removeByKey(K key) {
		return super.removeByKey(0, key);
	}

	public GapList<E> removeAllByKey(K key) {
		return super.removeAllByKey(0, key);
	}

	public E put(E elem) {
		K key = getMapper().getKey(elem);
		int index = indexOfKey(0, key);
		E oldElem;
		if (index == -1) {
			oldElem = null;
			add(elem);
		} else {
			oldElem = get(index);
			set(index, elem);
		}
		return oldElem;
	}

    @Override
    public MapList<E,K> copy() {
        MapList<E,K> copy = new MapList<E,K>(this);
        copy.initCopy(this);
        return copy;
    }

    /**
     * Returns a copy this list but without elements.
     * The new list will use the same comparator, ordering, etc.
     *
     * @return  an empty copy of this instance
     */
    public MapList<E,K> crop() {
        MapList<E,K> copy = new MapList<E,K>(this);
        copy.initCrop(this);
        return copy;
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
    public Map<K,E> asMap() {
    	return asMap(this, 0);
    }

    static <K,E> Map asMap(final KeyList keyList, final int keyIndex) {
        return new Map<K,E>() {

            @Override
            public int size() {
                return keyList.size();
            }

            @Override
            public boolean isEmpty() {
                return keyList.isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                return keyList.getByKey(keyIndex, (K) key) != null;
            }

            @Override
            public boolean containsValue(Object value) {
                return keyList.contains(value);
            }

            @Override
            public E get(Object key) {
                return (E) keyList.getByKey(keyIndex, (K) key);
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
                return (E) keyList.removeByKey(keyIndex, (K) key);
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
            	keyList.clear();
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
