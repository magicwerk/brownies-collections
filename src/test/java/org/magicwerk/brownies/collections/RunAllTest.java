package org.magicwerk.brownies.collections;

import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.magictest.MagicTest;
import org.magictest.client.Test;
import org.magicwerk.brownies.collections.guava.BigListTestGuava;
import org.magicwerk.brownies.collections.guava.CollectionAsSetTestGuava;
import org.magicwerk.brownies.collections.guava.GapListTestGuava;
import org.magicwerk.brownies.collections.guava.IntObjBigListTestGuava;
import org.magicwerk.brownies.collections.guava.IntObjGapListTestGuava;
import org.magicwerk.brownies.collections.guava.Key1CollectionTestGuavaMap;
import org.magicwerk.brownies.collections.guava.KeyCollectionTestGuava;
import org.magicwerk.brownies.collections.guava.KeyCollectionTestGuavaSet;
import org.magicwerk.brownies.collections.guava.KeyListTestGuava;
import org.magicwerk.brownies.collections.guava.KeySetTestGuava;
import org.magicwerk.brownies.core.RunTools;
import org.magicwerk.brownies.core.ThreadTools;
import org.magicwerk.brownies.core.Timer;
import org.magicwerk.brownies.core.files.FilePath;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.slf4j.Logger;

/**
 * Run all tests for Brownies-Collections.
 * <p>
 * - All MagicTests in org.magicwerk.brownies.collections <br>
 * - All Guava JUnit tests in org.magicwerk.brownies.collections.guava <br>
 * - Test done by GapListTestCorrectness.testRelease
 * <br>
 * Depending on the execution environment, the number of tests differ:
 * - If run as main application, all tests are executed, including the tests typically run by MagicTest
 * - If run as MagicTest, only the tests not executed by MagicTest are executed
 *
 * @author Thomas Mauch
 */
public class RunAllTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	static boolean runTestAll;

	public static void main(String[] args) {
		new RunAllTest().run();
	}

	void run() {
		// Typically you will use RunDebugTest to run tests with assertions enabled in a separate source tree.
		// For debugging, it can however be handy to work with the actual sources.
		// In this case, you can uncomment the line below which will change the actual sources.
		// Note that a recompile must be triggered after calling enableDebugCheck(), so enableDebugCheck()/runAllTests()
		// must be executed as two different applications run with a manual recompile between.

		//enableDebugCheck(true);

		runAllTests();
	}

	void enableDebugCheck(boolean enabled) {
		RunDebugTest.enableDebugCheck(FilePath.of("."), enabled);
	}

	void runAllTests() {
		try {
			runTestAll = true;
			runTests(true);
		} finally {
			runTestAll = false;
		}
	}

	/**
	 * Run all tests in this package as MagicTest.
	 */
	@Test
	public void testNonMagicTests() {
		if (runTestAll) {
			return;
		}
		runTests(false);
	}

	void runTests(boolean includeMagicTests) {
		Timer t = new Timer();
		boolean success = true;

		if (includeMagicTests) {
			boolean run = runMagicTests();
			if (!run) {
				success = false;
			}
		}

		boolean run = runNonMagicTests();
		if (!run) {
			success = false;
		}

		String time = t.elapsedString();
		if (success) {
			LOG.info("Test successful ({})", time);
		} else {
			LOG.info("Test failed ({}) - check output", time);
			throw new IllegalArgumentException();
		}
	}

	static boolean runMagicTests() {
		return MagicTest.runPackage(ReflectTools.getPackageName(ThreadTools.getCurrentClass()));
	}

	/**
	 * Run all tests in this package.
	 */
	boolean runNonMagicTests() {
		boolean success = true;
		success = runJUnitTests() && success;
		success = runCompareTests() && success;
		success = runCoverageTests() && success;
		return success;
	}

	boolean runCompareTests() {
		return RunTools.runBoolLog(() -> ListCompareTest.test());
	}

	boolean runCoverageTests() {
		return RunTools.runBoolLog(() -> GapListTestCorrectness.testRelease());
	}

	boolean runJUnitTests() {
		Class<?> guavaTests[] = new Class<?>[] {
				BigListTestGuava.class,
				CollectionAsSetTestGuava.class,
				GapListTestGuava.class,
				IntObjGapListTestGuava.class,
				IntObjBigListTestGuava.class,
				Key1CollectionTestGuavaMap.class,
				KeyCollectionTestGuava.class,
				KeyCollectionTestGuavaSet.class,
				KeyListTestGuava.class,
				KeySetTestGuava.class
		};

		boolean success = true;
		Computer computer = new Computer();
		JUnitCore jUnitCore = new JUnitCore();
		for (Class<?> test : guavaTests) {
			Result result = jUnitCore.run(computer, test);
			LOG.info("Tests {}: run {}, failures {}\n", test.getSimpleName(), result.getRunCount(), result.getFailureCount());
			if (result.getFailureCount() > 0) {
				success = false;
			}
		}
		return success;
	}
}
