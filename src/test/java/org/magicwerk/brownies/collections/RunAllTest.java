package org.magicwerk.brownies.collections;

import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.magictest.MagicTest;
import org.magicwerk.brownies.collections.guava.BigListTestGuava;
import org.magicwerk.brownies.collections.guava.GapListTestGuava;
import org.magicwerk.brownies.collections.guava.IntObjBigListTestGuava;
import org.magicwerk.brownies.collections.guava.IntObjGapListTestGuava;
import org.magicwerk.brownies.collections.guava.Key1CollectionTestGuavaMap;
import org.magicwerk.brownies.collections.guava.KeyCollectionTestGuava;
import org.magicwerk.brownies.collections.guava.KeyCollectionTestGuavaSet;
import org.magicwerk.brownies.collections.guava.KeyListTestGuava;
import org.magicwerk.brownies.collections.guava.KeySetTestGuava;
import org.magicwerk.brownies.core.ThreadTools;
import org.magicwerk.brownies.core.reflect.ClassTools;

/**
 * Run all tests for Brownies-Collections.
 * <p>
 * Execute this test class with JUnit to run <br>
 * - All MagicTests in org.magicwerk.brownies.collections <br>
 * - All Guava JUnit tests in org.magicwerk.brownies.collections.guava <br>
 * - Test done by GapListTestCorrectness.testRelease
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class RunAllTest {

	public static void main(String[] args) {
		doTestAll();
	}

	/**
	 * Run all tests in this package as JUnit test.
	 */
	@org.junit.Test
	public void testAll() {
		doTestAll();
	}

	/**
	 * Run all tests in this package.
	 */
	static void doTestAll() {
		boolean success = true;

		// Run MagicTests
		boolean run = MagicTest.runPackage(ClassTools.getPackageName(ThreadTools.getCurrentClass()));
		if (!run) {
			success = false;
		}

		// Run Guava tests in JUnit
		Computer computer = new Computer();
		JUnitCore jUnitCore = new JUnitCore();
		Class<?> guavaTests[] = new Class<?>[] {
				GapListTestGuava.class,
				BigListTestGuava.class,
				IntObjGapListTestGuava.class,
				IntObjBigListTestGuava.class,
				Key1CollectionTestGuavaMap.class,
				KeyCollectionTestGuava.class,
				KeyCollectionTestGuavaSet.class,
				KeyListTestGuava.class,
				KeySetTestGuava.class
		};
		for (Class<?> test : guavaTests) {
			Result result = jUnitCore.run(computer, test);
			System.out.printf("Tests %s: run %d, failures %d\n", test.getSimpleName(), result.getRunCount(), result.getFailureCount());
			if (result.getFailureCount() > 0) {
				success = false;
			}
		}

		// Run compare tests
		ListCompareTest.test();

		// Run tests for coverage
		GapListTestCorrectness.testRelease();

		if (success) {
			System.out.println("Test successful");
		} else {
			System.out.println("Test failed - check output");
			throw new IllegalArgumentException();
		}
	}
}
