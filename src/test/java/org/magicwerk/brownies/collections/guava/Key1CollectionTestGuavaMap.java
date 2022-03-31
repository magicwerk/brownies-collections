package org.magicwerk.brownies.collections.guava;

import static com.google.common.collect.testing.Helpers.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.magicwerk.brownies.collections.Key1Collection;

import com.google.common.collect.testing.AbstractTester;
import com.google.common.collect.testing.FeatureSpecificTestSuiteBuilder;
import com.google.common.collect.testing.Helpers;
import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.OneSizeTestContainerGenerator;
import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.TestMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import com.google.common.collect.testing.testers.MapClearTester;
import com.google.common.collect.testing.testers.MapContainsKeyTester;
import com.google.common.collect.testing.testers.MapContainsValueTester;
import com.google.common.collect.testing.testers.MapCreationTester;
import com.google.common.collect.testing.testers.MapEqualsTester;
import com.google.common.collect.testing.testers.MapGetTester;
import com.google.common.collect.testing.testers.MapHashCodeTester;
import com.google.common.collect.testing.testers.MapIsEmptyTester;
import com.google.common.collect.testing.testers.MapRemoveTester;
import com.google.common.collect.testing.testers.MapSerializationTester;
import com.google.common.collect.testing.testers.MapSizeTester;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test Key1Collection with the test suite provided by Guava.
 * Run this test by executing the whole test class using JUnit.
 * This test is defined as JUnit3 test but can also be run with JUnit4.
 *
 * @author Thomas Mauch
 */
public class Key1CollectionTestGuavaMap extends TestCase {

	// This method must be named suite() to be picked up by JUnit
	// The name has been changed to ignore this test.
	public static Test suiteIGNORE() {
		return new Key1CollectionTestGuavaMap().allTests();
	}

	// Provide trivial test to prohibit warning about no tests
	public void testEmpty() {
	}

	public Test allTests() {
		TestSuite suite = new TestSuite("Key1Collection as Map");
		suite.addTest(testMap());
		return suite;
	}

	@SuppressWarnings("serial")
	static class TestEntry implements Serializable {
		static class TestEntryMapper implements Function<TestEntry, String>, Serializable {
			@Override
			public String apply(TestEntry e) {
				return e.key;
			}
		};

		static Function<TestEntry, String> MAPPER = new TestEntryMapper();

		private String val;
		private String key;

		public TestEntry(String val, String key) {
			this.val = val;
			this.key = key;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((val == null) ? 0 : val.hashCode());
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestEntry other = (TestEntry) obj;
			if (val == null) {
				if (other.val != null)
					return false;
			} else if (!val.equals(other.val))
				return false;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TestEntry [val=" + val + ", key=" + key + "]";
		}

	}

	public Test testMap() {
		//		return MapTestSuiteBuilder
		//				.using(new TestMapGenerator<String,TestEntry>() {

		final TestMapGenerator<String, TestEntry> tmg = new TestMapGenerator<String, TestEntry>() {

			@Override
			public SampleElements<Entry<String, TestEntry>> samples() {
				return new SampleElements<Entry<String, TestEntry>>(
						Helpers.mapEntry("a", new TestEntry("a1", "a")),
						Helpers.mapEntry("b", new TestEntry("b2", "b")),
						Helpers.mapEntry("c", new TestEntry("c3", "c")),
						// This entry is used for non-existent entries
						//			    		Helpers.mapEntry("d", new TestEntry("d4", "d")),
						Helpers.mapEntry((String) null, new TestEntry("d4", null)),
						Helpers.mapEntry("e", new TestEntry("e4", "e")));
			}

			@Override
			public Map<String, TestEntry> create(Object... entries) {
				Key1Collection<TestEntry, String> coll = new Key1Collection.Builder<TestEntry, String>().withElemNull(true).withKey1Map(TestEntry.MAPPER)
						.withKey1Null(true).build();
				Map<String, TestEntry> map = coll.asMap1();

				//			    Map<String,TestEntry> map = new HashMap<String,TestEntry>();
				for (Object o : entries) {
					Entry<String, TestEntry> e = (Entry<String, TestEntry>) o;
					String key = e.getKey();
					TestEntry val = e.getValue();
					if (!val.key.equals(key)) {
						val = new TestEntry(val.val, key);
					}
					map.put(key, val);
				}
				return map;
			}

			@Override
			public Entry<String, TestEntry>[] createArray(int length) {
				return new Entry[length];
			}

			@Override
			public Iterable<Entry<String, TestEntry>> order(List<Entry<String, TestEntry>> insertionOrder) {
				return orderEntriesByKey(insertionOrder);
			}

			@Override
			public String[] createKeyArray(int length) {
				return new String[length];
			}

			@Override
			public TestEntry[] createValueArray(int length) {
				return new TestEntry[length];
			}
		};

		MapTestSuiteBuilder<String, TestEntry> mtsb = new MapTestSuiteBuilder<String, TestEntry>() {
			{
				usingGenerator(tmg)
						.named("Key1Collection asMap")
						.withFeatures(
								MapFeature.GENERAL_PURPOSE,
								MapFeature.ALLOWS_NULL_KEYS,
								MapFeature.ALLOWS_NULL_VALUES,
								MapFeature.RESTRICTS_KEYS,
								//CollectionFeature.KNOWN_ORDER,
								CollectionFeature.SERIALIZABLE,
								CollectionSize.ANY);
			}

			// copied from MapTestSuiteBuilder
			@SuppressWarnings("unchecked") // Class parameters must be raw.
			@Override
			protected List<Class<? extends AbstractTester>> getTesters() {
				return Arrays.<Class<? extends AbstractTester>>asList(
						MapClearTester.class,
						MapContainsKeyTester.class,
						MapContainsValueTester.class,
						MapCreationTester.class,
						MapEqualsTester.class,
						MapGetTester.class,
						MapHashCodeTester.class,
						MapIsEmptyTester.class,
						//MapPutTester.class,
						//MapPutAllTester.class,
						MapRemoveTester.class,
						MapSerializationTester.class,
						MapSizeTester.class);
			}

			@Override
			protected List<TestSuite> createDerivedSuites(
					FeatureSpecificTestSuiteBuilder<?, ? extends OneSizeTestContainerGenerator<Map<String, TestEntry>, Map.Entry<String, TestEntry>>> parentBuilder) {
				return new ArrayList<>();
			}
		};
		return mtsb.createTestSuite();
	}

}
