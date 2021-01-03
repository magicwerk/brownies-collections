package org.magicwerk.brownies.collections.dev;

import org.magicwerk.brownies.core.diff.StringDiff;
import org.magicwerk.brownies.core.files.FileTools;
import org.magicwerk.brownies.core.regex.RegexReplacer;

/**
 * Compare test results.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class CompareTestResults {

	public static void main(String[] args) {
		run();
	}

	static void run() {
		String dir = "magictest/org/magicwerk/brownies/collections/";

		String str = readFile(dir + "GapListTest.ref.xml");
		str = normalize(str);
		FileTools.writeFile().setFile(dir + "BigListGapListTest.ref.xml").setText(str).write();
	}

	static String normalize(String str) {
		str = substitute("name=\"org.magicwerk.brownies.collections.GapListTest\"", str,
				"name=\"org.magicwerk.brownies.collections.BigListGapListTest\"", false);
		str = substitute("<traceClass>org.magicwerk.brownies.collections.GapList</traceClass>", str,
				"<traceClass>org.magicwerk.brownies.collections.BigList</traceClass>", true);
		return str;
	}

	static void run1() {

		String dir = "magictest/org/magicwerk/brownies/collections/";

		String gapListTest = readFile(dir + "GapListTest.act.xml");
		String bigListGapListTest = readFile(dir + "BigListGapListTest.act.xml");

		gapListTest = normalize(gapListTest);
		//bigListGapListTest = normalize(bigListGapListTest);

		StringDiff diff = new StringDiff();
		String str = diff.setStringAsLines(gapListTest, bigListGapListTest).diffAsSingleString();
		System.out.println(str);
	}

	static String readFile(String file) {
		return FileTools.readFile().setFile(file).readText();
	}

	static String normalize1(String str) {
		str = substitute("<testClass .*?>", str, "<testClass/>", true);
		str = substitute("<runDate>.*?>", str, "<runDate/>", true);
		str = substitute("<runDuration>.*?>", str, "<runDuration/>", true);
		str = substitute("<traceClass>.*?>", str, "<traceClass/>", true);
		return str;
	}

	static String substitute(String pattern, String input, String message, boolean global) {
		RegexReplacer r = new RegexReplacer().setPattern(pattern).setFormat(message);
		if (!global) {
			r.setNumReplaces(1);
		}
		return r.replace(input);
	}
}
