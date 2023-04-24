package org.magicwerk.brownies.collections.dev;

import org.magicwerk.brownies.collections.dev.BuildSource.Builder;
import org.magicwerk.brownies.collections.dev.BuildSource.FileBuilder;
import org.magicwerk.brownies.core.files.PathTools;
import org.magicwerk.brownies.core.reflect.ClassTools;

/**
 * Create source files IntMergeSort, etc.
 * in package org.magicwerk.brownies.collections.helper.primitive
 *
 * @author Thomas Mauch
 */
public class BuildSourceBinarySearch extends FileBuilder {

	/**
	 * @see BuildSourceBinarySearch
	 */
	public BuildSourceBinarySearch(Builder builder) {
		super(builder);
	}

	@Override
	public void build() {
		String dstClass = "org.magicwerk.brownies.collections.helper.primitive.{NAME}BinarySearch";
		dstClass = applyTemplate(dstClass);

		String srcFile = PathTools.getPath(testDir, "org/magicwerk/brownies/collections/dev/BinarySearch.java.tpl");
		String src = readFile(srcFile);

		src = processClass(src);
		src = applyTemplate(src);
		String javaFile = applyTemplate("{NAME}BinarySearch.java");

		String dstFile = PathTools.getPath(
				srcDir,
				ClassTools.getPathFromClass(ClassTools.getParentPackage(dstClass)),
				javaFile);

		setFile(dstFile);
		setFileContent(src);
	}

	String processClass(String src) {
		return src;
	}

}
