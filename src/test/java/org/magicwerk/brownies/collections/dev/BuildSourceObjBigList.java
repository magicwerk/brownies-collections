package org.magicwerk.brownies.collections.dev;

import org.magicwerk.brownies.collections.dev.BuildSource.Builder;
import org.magicwerk.brownies.collections.dev.BuildSource.FileBuilder;
import org.magicwerk.brownies.core.files.PathTools;
import org.magicwerk.brownies.core.reflect.ClassTools;

/**
 * Create source files IntObjBigList, etc.
 * in package org.magicwerk.brownies.collections.primitive
 *
 * @author Thomas Mauch
 * @version $Id: BuildSourceObjGapList.java 2477 2014-10-08 23:47:35Z origo $
 */
public class BuildSourceObjBigList extends FileBuilder {

	/**
	 * @see BuildSourceObjBigList
	 */
	public BuildSourceObjBigList(Builder builder) {
		super(builder);
	}

	@Override
	public void build() {
		String srcClass = "org.magicwerk.brownies.collections.dev.ObjGapList";
		String dstClass = "org.magicwerk.brownies.collections.ObjBigList";

		String srcFile = PathTools.getPath(testDir, ClassTools.getPathFromClass(srcClass)) + ".java.tpl";
		String src = readFile(srcFile);

		src = processClass(src);
		src = applyTemplate(src);
		String javaFile = applyTemplate("{NAME}ObjBigList.java");

		String dstFile = PathTools.getPath(
				srcDir,
				ClassTools.getPathFromClass(ClassTools.getParentPackage(dstClass)),
				"primitive",
				javaFile);

		setFile(dstFile);
		setFileContent(src);
	}

	String processClass(String src) {
		src = substitute("GapList", src, "BigList");
		src = substitute("(?s)(doCreate.*?\\{)(.*?\\})(.*?)\\(capacity\\)", src, "{1}{3}()");
		return src;
	}
}
