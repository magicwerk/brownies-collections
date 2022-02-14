package org.magicwerk.brownies.collections;

import org.magicwerk.brownies.test.MagicTestRunner;

/**
 * Test runner.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class Test {

	public static void main(String[] args) {
		new Test().run();
	}

	void run() {
		runMagicTest();
	}

	void runMagicTest() {
		MagicTestRunner mtr = new MagicTestRunner();
		mtr.setShowActualReport(true);
		mtr.setShowReferenceReport(false);

		String clazz = "org.magicwerk.brownies.collections.BigListGapListTest";

		// MagicTest

		IList<String> magicTestArgs = GapList.create(
				"-run",
				//"-save",
				"-loglevel", "trace",
				//"-delete",
				//"-missing", clazz
				//"-method", "org.magicwerk.brownies.core.context.ContextTest.test0Ok",
				"-class", clazz
		//"-package", "org.magicwerk.brownies.core.values.io"
		//"org.magicwerk.brownies.core.strings.*Test"
		// Unknown class
		//"org.magicwerk.brownies.core.**.*Test"
		);

		//mtr.runMagicTestEclipse(magicTestArgs);
		//mtr.runMagicTestForkEclipse(magicTestArgs);
		//mtr.runMagicTestForkJar(magicTestArgs);

		// MagicTestNG

		IList<String> magicTestNgArgs = GapList.create(
				"-testclass", clazz,
				"-verbose", "10"
		//"testng.xml"
		//"C:\\Windows\\TEMP\\testng-eclipse-839192392\\testng-customsuite.xml"
		);

		mtr.runMagicTestNgEclipse(magicTestNgArgs);
		//mtr.runMagicTestNgForkEclipse(magicTestNgArgs);
		//mtr.runMagicTestNgForkJar(magicTestNgArgs);
	}
}
