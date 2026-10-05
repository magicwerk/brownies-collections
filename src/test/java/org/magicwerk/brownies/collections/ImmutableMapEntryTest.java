package org.magicwerk.brownies.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;

public class ImmutableMapEntryTest {
    @Test
    public void equalityIsCompatibleWithJdkEntries() {
        Map.Entry<String, Integer> entry = new ImmutableMapEntry<>("key", 42);
        Map.Entry<String, Integer> other = new SimpleImmutableEntry<>("key", 42);
        assertTrue(entry.equals(other));
        assertTrue(other.equals(entry));
        assertEquals(other.hashCode(), entry.hashCode());
        assertEquals(entry, new ImmutableMapEntry<>("key", 42));
        assertFalse(entry.equals(new SimpleImmutableEntry<>("other", 42)));
        assertFalse(entry.equals(new SimpleImmutableEntry<>("key", 43)));
        assertFalse(entry.equals(null));
        assertFalse(entry.equals("key"));
    }

    @Test
    public void nullKeysAndValuesFollowEntryContract() {
        Map.Entry<String, Integer> entry = new ImmutableMapEntry<>(null, null);
        assertEquals(new SimpleImmutableEntry<>(null, null), entry);
        assertEquals(entry, new SimpleImmutableEntry<>(null, null));
        assertEquals(0, entry.hashCode());
        assertEquals(new SimpleImmutableEntry<>(null, 42).hashCode(),
                new ImmutableMapEntry<>(null, 42).hashCode());
        assertEquals(new SimpleImmutableEntry<>("key", null).hashCode(),
                new ImmutableMapEntry<>("key", null).hashCode());
    }

    @Test
    public void keyCollectionMapEntriesMatchJdkEntries() {
        Key1Collection<String, Integer> collection = new Key1Collection.Builder<String, Integer>()
                .withKey1Map(String::length).withKey1Duplicates(false).build();
        collection.add("abc");
        Map<Integer, String> map = collection.asMap1();
        assertEquals(Collections.singletonMap(3, "abc").entrySet(), map.entrySet());
        assertEquals(map.entrySet(), Collections.singletonMap(3, "abc").entrySet());
        assertEquals(Collections.singletonMap(3, "abc").entrySet().iterator().next().hashCode(),
                map.entrySet().iterator().next().hashCode());
    }
}
