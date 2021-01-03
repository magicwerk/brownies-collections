package org.magicwerk.brownies.collections.sandbox;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.magicwerk.brownies.collections.KeyListImpl;

public class AsMap {
    /**
     * Returns a view on the MapList as Map.
     * Note that a class cannot implement both List and Map.
     * Reason is the conflicting definition of the remove() method:  <br/>
     * - Map:  V remove(Object key) <br/>
     * - List: boolean remove(Object obj) <br/>
     *
     * @return view of this MapList as Map
     */
//    public Map<K,E> asMap() {
//    	return asMap(this, 0);
//    }
//
//    static <K,E> Map asMap(final KeyListImpl keyList, final int keyIndex) {
//        return new Map<K,E>() {
//
//            @Override
//            public int size() {
//                return keyList.size();
//            }
//
//            @Override
//            public boolean isEmpty() {
//                return keyList.isEmpty();
//            }
//
//            @Override
//            public boolean containsKey(Object key) {
//                return keyList.getByKey(keyIndex, (K) key) != null;
//            }
//
//            @Override
//            public boolean containsValue(Object value) {
//                return keyList.contains(value);
//            }
//
//            @Override
//            public E get(Object key) {
//                return (E) keyList.getByKey(keyIndex, (K) key);
//            }
//
//            @Override
//            public E put(K key, E value) {
//                throw new UnsupportedOperationException();
//            	//if (!getMapper().getKey(value).equals(key)) {
////            		throw new IllegalArgumentException("Key is not equal to key created by mapper");
//            	//}
////                return MapList.this.add(value); TODO
//            }
//
//            @Override
//            public E remove(Object key) {
//                return (E) keyList.removeByKey(keyIndex, (K) key);
//            }
//
//            @Override
//            public void putAll(Map<? extends K, ? extends E> m) {
//                throw new UnsupportedOperationException();
//                //for (E value: m.values()) {
//                    //MapList.this.put(value);
//                //} TODO
//            }
//
//            @Override
//            public void clear() {
//            	keyList.clear();
//            }
//
//            @Override
//            public Set<K> keySet() {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public Collection<E> values() {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public Set<java.util.Map.Entry<K, E>> entrySet() {
//                throw new UnsupportedOperationException();
//            }
//
//        };
//    }


}
