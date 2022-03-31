package org.magicwerk.brownies.collections.guava;

import java.util.List;

import org.magicwerk.brownies.collections.primitive.IntObjGapList;

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.TestListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Test IntObjGapList with the test suite provided by Guava.
 * Run this test by executing the whole test class using JUnit.
 * This test is defined as JUnit3 test but can also be run with JUnit4.
 *
 * @author Thomas Mauch
 */
public class IntObjGapListTestGuava extends TestCase {

	// this method must be named suite()
	public static Test suite() {
		return ListTestSuiteBuilder.using(
				// This class is responsible for creating the collection
				// And providing data, which can be put into the collection
				// Here we use a abstract generator which will create strings
				// which will be put into the collection
				new TestIntegerListGenerator() {
					@Override
					protected List<Integer> create(Integer[] elements) {
						// Fill here your collection with the given elements
						return IntObjGapList.create(elements);
					}
				})
				// The name of the test suite
				.named("IntObjGapList Test")
				// Here we give a hit what features our collection supports
				.withFeatures(ListFeature.GENERAL_PURPOSE,
						//CollectionFeature.ALLOWS_NULL_VALUES,
						CollectionFeature.SERIALIZABLE,
						//CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
						CollectionSize.ANY)
				.createTestSuite();
	}

}

/**
 * Sample generator for testing collections with Integers.
 * This class is also used by IntObjBigListTestGuava.
 */
abstract class TestIntegerListGenerator implements TestListGenerator<Integer> {
	@Override
	public SampleElements<Integer> samples() {
		return new SampleElements.Ints();
	}

	@Override
	public List<Integer> create(Object... elements) {
		Integer[] array = new Integer[elements.length];
		int i = 0;
		for (Object e : elements) {
			array[i++] = (Integer) e;
		}
		return create(array);
	}

	/**
	 * Creates a new collection containing the given elements; implement this
	 * method instead of {@link #create(Object...)}.
	 */
	protected abstract List<Integer> create(Integer[] elements);

	@Override
	public Integer[] createArray(int length) {
		return new Integer[length];
	}

	/** Returns the original element list, unchanged. */
	@Override
	public List<Integer> order(List<Integer> insertionOrder) {
		return insertionOrder;
	}
}
