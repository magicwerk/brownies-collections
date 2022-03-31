package org.magicwerk.brownies.collections.guava;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

import org.magicwerk.brownies.collections.Key1List;
import org.magicwerk.brownies.collections.Key2List;
import org.magicwerk.brownies.collections.KeyList;

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
		TestSuite test = new TestSuite("KeyList");
		test.addTest(suiteKeyList());
		test.addTest(suiteKey1List());
		test.addTest(suiteKey2List());
		return test;
	}

	// this method must be named suite()
	public static Test suiteKeyList() {
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

	// this method must be named suite()
	public static Test suiteKey1List() {
		return ListTestSuiteBuilder.using(
				// This class is responsible for creating the collection
				// And providing data, which can be put into the collection
				// Here we use a abstract generator which will create strings
				// which will be put into the collection
				new TestStringListGenerator() {
					@Override
					protected List<String> create(String[] elements) {
						// Fill here your collection with the given elements
						Key1List<String, Integer> list = new Key1List.Builder<String, Integer>()
								.withKey1Map((Function<String, Integer> & Serializable) (s) -> s.length())
								.withContent(elements).build();
						return list;
					}
				})
				// The name of the test suite
				.named("Key1List Test")
				// Here we give a hit what features our collection supports
				.withFeatures(ListFeature.GENERAL_PURPOSE,
						CollectionFeature.ALLOWS_NULL_VALUES,
						CollectionFeature.SERIALIZABLE,
						//CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
						CollectionSize.ANY)
				.createTestSuite();
	}

	// this method must be named suite()
	public static Test suiteKey2List() {
		return ListTestSuiteBuilder.using(
				// This class is responsible for creating the collection
				// And providing data, which can be put into the collection
				// Here we use a abstract generator which will create strings
				// which will be put into the collection
				new TestStringListGenerator() {
					@Override
					protected List<String> create(String[] elements) {
						// Fill here your collection with the given elements
						Key2List<String, Integer, Integer> list = new Key2List.Builder<String, Integer, Integer>()
								.withKey1Map((Function<String, Integer> & Serializable) (s) -> s.length())
								.withKey2Map((Function<String, Integer> & Serializable) (s) -> s.length())
								.withContent(elements).build();
						return list;
					}
				})
				// The name of the test suite
				.named("Key2List Test")
				// Here we give a hit what features our collection supports
				.withFeatures(ListFeature.GENERAL_PURPOSE,
						CollectionFeature.ALLOWS_NULL_VALUES,
						CollectionFeature.SERIALIZABLE,
						//CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
						CollectionSize.ANY)
				.createTestSuite();
	}

}
