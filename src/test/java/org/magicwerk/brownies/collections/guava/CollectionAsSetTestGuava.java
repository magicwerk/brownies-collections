package org.magicwerk.brownies.collections.guava;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.magicwerk.brownies.collections.CollectionAsSet;
import org.magicwerk.brownies.collections.GapList;

import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test {@link CollectionAsSet} with the test suite provided by Guava.
 * Run this test by executing the whole test class using JUnit.
 *
 * @author Thomas Mauch
 */
public class CollectionAsSetTestGuava extends TestCase {

	// this method must be named suite()
	public static Test suite() {
		return new CollectionAsSetTestGuava().allTests();
	}

	public Test allTests() {
		TestSuite suite = new TestSuite("CollectionAsSet");
		suite.addTest(testSet());
		return suite;
	}

	public Test testSet() {
		return SetTestSuiteBuilder.using(
				// This class is responsible for creating the collection and providing data, which can be put into the collection.
				// Here we use a abstract generator which will create strings which will be put into the collection
				new TestStringSetGenerator() {
					@Override
					protected Set<String> create(String[] elements) {
						// There are 2 tests in SetCreationTester which test that duplicate are removed during creation.
						// As CollectionAsSet can only reject invalid collections, this behavior is simulated here
						Set<String> set = new HashSet<>();
						for (String e : elements) {
							set.add(e);
						}
						Collection<String> list = GapList.create(set);
						return new CollectionAsSet<>(list, false, false);
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
