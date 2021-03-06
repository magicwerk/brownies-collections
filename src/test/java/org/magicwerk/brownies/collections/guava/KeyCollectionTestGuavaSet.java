package org.magicwerk.brownies.collections.guava;

import java.util.Arrays;
import java.util.Set;

import org.magicwerk.brownies.collections.KeyCollection;

import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test KeyCollection as set with the test suite provided by Guava.
 * Run this test by executing the whole test class using JUnit.
 * This test is defined as JUnit3 test but can also be run with JUnit4.
 *
 * @author Thomas Mauch
 */
public class KeyCollectionTestGuavaSet extends TestCase {

	// this method must be named suite()
	public static Test suite() {
		return new KeyCollectionTestGuavaSet().allTests();
	}

	public Test allTests() {
		TestSuite suite = new TestSuite("KeyCollection as Set");
		suite.addTest(testSet());
		return suite;
	}

	public Test testSet() {
		return SetTestSuiteBuilder.using(
				// This class is responsible for creating the collection
				// And providing data, which can be put into the collection
				// Here we use a abstract generator which will create strings
				// which will be put into the collection
				new TestStringSetGenerator() {
					@Override
					protected Set<String> create(String[] elements) {
						// Note that you should not add elements with withElements(elements)
						// as duplicated could be added
						KeyCollection<String> coll = new KeyCollection.Builder<String>().withNull(true).withElemDuplicates(false).build();
						Set<String> set = coll.asSet();
						set.addAll(Arrays.asList(elements));
						return set;
					}
				})
				// The name of the test suite
				.named("KeyCollection Set Test")
				// Here we give a hit what features our collection supports
				.withFeatures(
						CollectionFeature.GENERAL_PURPOSE,
						CollectionFeature.ALLOWS_NULL_VALUES,
						//CollectionFeature.SERIALIZABLE,
						//CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
						CollectionSize.ANY)
				.createTestSuite();
	}

}
