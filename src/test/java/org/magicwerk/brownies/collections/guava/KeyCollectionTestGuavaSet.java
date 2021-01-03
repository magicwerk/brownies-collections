package org.magicwerk.brownies.collections.guava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.magicwerk.brownies.collections.KeyCollection;

import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;

/**
 * Test KeyCollection as set with the test suite provided by Guava.
 * Run this test by executing the whole test class using JUnit.
 * This test is defined as JUnit3 test but can also be run with JUnit4.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class KeyCollectionTestGuavaSet extends TestCase {

	public static void main(String[] args) {
		test();
	}

	static void test() {
		Set<String> set = new HashSet<String>();
		set.add("a");
		set.add("b");
		List<String> list = new ArrayList<String>();
		list.addAll(set);
		assert (set.equals(list));

		//		String[] elements = new String[] {};
		//		KeyCollection<String> coll = new KeyCollection.Builder<String>().withNull(true).withElemDuplicates(false).withElements(elements).build();
		//		assert(coll.size() == elements.length);
		//		Set<String> set = coll.asSet();
		//		elements = new String[] { "d", "e", "d", "e" };
		//		boolean b = set.addAll(Arrays.asList(elements));
		//		System.out.println(set + " " + b);
	}

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
