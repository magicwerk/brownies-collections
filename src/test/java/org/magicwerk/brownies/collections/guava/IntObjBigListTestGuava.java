package org.magicwerk.brownies.collections.guava;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import org.magicwerk.brownies.collections.primitive.IntObjBigList;

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;

/**
 * Test IntObjBigList with the test suite provided by Guava.
 * Run this test by executing the whole test class using JUnit.
 * This test is defined as JUnit3 test but can also be run with JUnit4.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class IntObjBigListTestGuava extends TestCase {

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
						return IntObjBigList.create(elements);
					}
				})
				// The name of the test suite
				.named("IntObjBigList Test")
				// Here we give a hit what features our collection supports
				.withFeatures(ListFeature.GENERAL_PURPOSE,
						//CollectionFeature.ALLOWS_NULL_VALUES,
						CollectionFeature.SERIALIZABLE,
						//CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
						CollectionSize.ANY)
				.createTestSuite();
	}

}
