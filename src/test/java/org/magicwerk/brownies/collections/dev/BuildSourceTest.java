package org.magicwerk.brownies.collections.dev;

import org.magicwerk.brownies.collections.dev.BuildSource.FileBuilder;
import org.magicwerk.brownies.core.files.PathTools;
import org.magicwerk.brownies.core.reflect.ClassTools;

/**
 * Create test sources.
 *
 * @author Thomas Mauch
 */
public class BuildSourceTest extends FileBuilder {

	String testClass;
	String testName;

	public BuildSourceTest(String testClass) {
		this.testClass = testClass;
		testName = testClass + "GapListTest";
	}

	@Override
	public void build() {
		String srcClass = "org.magicwerk.brownies.collections.GapListTest";
		String dstClass = "org.magicwerk.brownies.collections." + testName;

		String srcFile = PathTools.getPath(testDir, ClassTools.getPathFromClass(srcClass)) + ".java";
		String src = readFile(srcFile);

		src = processClass(src);
		src = applyTemplate(src);

		String dstFile = PathTools.getPath(testDir, ClassTools.getPathFromClass(dstClass)) + ".java";

		setFile(dstFile);
		setFileContent(src);
	}

	String processClass(String src) {
		// Replace GapList, but not calls to class GapLists
		src = substitute("GapList(?!s)", src, testClass);
		src = substitute(testClass + "Test", src, testName);

		String regex = "(?s)Begin test buffer.*End test buffer";
		src = substitute(regex, src, "");

		//		String imp = "import org.magicwerk.brownies.collections.function.Mapper;";
		//		src = substitute(imp, src, imp + "\nimport org.magicwerk.brownies.collections.primitive.IntObjGapList;");
		//		src = substitute("<Integer>", src, "");

		src = "// ---\r\n" +
				"// --- DO NOT EDIT\r\n" +
				"// --- AUTOMATICALLY GENERATED FILE\r\n" +
				"// ---\r\n" +
				"\r\n" + src;
		return src;
	}

}
