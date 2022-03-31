package org.magicwerk.brownies.collections.guava;

import java.util.Collection;

import org.magicwerk.brownies.collections.KeyCollection;

import com.google.common.collect.testing.CollectionTestSuiteBuilder;
import com.google.common.collect.testing.TestStringCollectionGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test KeyCollection with the test suite provided by Guava.
 * Run this test by executing the whole test class using JUnit.
 * This test is defined as JUnit3 test but can also be run with JUnit4.
 *
 * @author Thomas Mauch
 */
public class KeyCollectionTestGuava extends TestCase {

	// this method must be named suite()
	public static Test suite() {
		TestSuite test = new TestSuite("KeyCollection");
		test.addTest(suite1());
		test.addTest(suite2());
		return test;
	}

	/**
	 * @return test suite for testing key collection without count feature enabled
	 */
	static Test suite1() {
		return CollectionTestSuiteBuilder.using(
				// This class is responsible for creating the collection
				// And providing data, which can be put into the collection
				// Here we use a abstract generator which will create strings
				// which will be put into the collection
				new TestStringCollectionGenerator() {
					@Override
					protected Collection<String> create(String[] elements) {
						// Fill here your collection with the given elements
						KeyCollection<String> coll = new KeyCollection.Builder<String>().withNull(true).withContent(elements).build();
						assert (coll.size() == elements.length);
						return coll;
					}
				})
				// The name of the test suite
				.named("KeyCollection Test")
				// Here we give a hit what features our collection supports
				.withFeatures(
						CollectionFeature.GENERAL_PURPOSE,
						CollectionFeature.ALLOWS_NULL_VALUES,
						//CollectionFeature.SERIALIZABLE,
						//CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
						CollectionSize.ANY)
				.createTestSuite();
	}

	/**
	 * @return test suite for testing key collection with count feature enabled
	 */
	static Test suite2() {
		return CollectionTestSuiteBuilder.using(
				// This class is responsible for creating the collection
				// And providing data, which can be put into the collection
				// Here we use a abstract generator which will create strings
				// which will be put into the collection
				new TestStringCollectionGenerator() {
					@Override
					protected Collection<String> create(String[] elements) {
						// Fill here your collection with the given elements
						KeyCollection<String> coll = new KeyCollection.Builder<String>().withElemCount(true).withNull(true).withContent(elements).build();
						assert (coll.size() == elements.length);
						return coll;
					}
				})
				// The name of the test suite
				.named("KeyCollection[count] Test")
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
