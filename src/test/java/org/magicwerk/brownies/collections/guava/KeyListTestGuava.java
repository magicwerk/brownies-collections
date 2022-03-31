package org.magicwerk.brownies.collections.guava;

import java.util.List;

import org.magicwerk.brownies.collections.KeyList;

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Test KeyList with the test suite provided by Guava.
 * Run this test by executing the whole test class using JUnit.
 * This test is defined as JUnit3 test but can also be run with JUnit4.
 *
 * @author Thomas Mauch
 */
public class KeyListTestGuava extends TestCase {

	// this method must be named suite()
	public static Test suite() {
		return ListTestSuiteBuilder.using(
				// This class is responsible for creating the collection
				// And providing data, which can be put into the collection
				// Here we use a abstract generator which will create strings
				// which will be put into the collection
				new TestStringListGenerator() {
					@Override
					protected List<String> create(String[] elements) {
						// Fill here your collection with the given elements
						KeyList<String> list = new KeyList.Builder<String>().withNull(true).withContent(elements).build();
						return list;
					}
				})
				// The name of the test suite
				.named("KeyList Test")
				// Here we give a hit what features our collection supports
				.withFeatures(ListFeature.GENERAL_PURPOSE,
						CollectionFeature.ALLOWS_NULL_VALUES,
						CollectionFeature.SERIALIZABLE,
						//CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
						CollectionSize.ANY)
				.createTestSuite();
	}

}
