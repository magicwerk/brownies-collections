package org.magicwerk.brownies.collections.dev;

import org.magicwerk.brownies.collections.dev.BuildSource.Builder;
import org.magicwerk.brownies.collections.dev.BuildSource.FileBuilder;
import org.magicwerk.brownies.core.files.PathTools;
import org.magicwerk.brownies.core.reflect.ClassTools;

/**
 * Create source files IntObjGapList, etc.
 * in package org.magicwerk.brownies.colletions.primitive
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class BuildSourceObjGapList extends FileBuilder {

	/**
	 * @see BuildSourceObjGapList
	 */
	public BuildSourceObjGapList(Builder builder) {
		super(builder);
	}

	@Override
	public void build() {
		String srcClass = "org.magicwerk.brownies.collections.dev.ObjGapList";
		String dstClass = "org.magicwerk.brownies.collections.ObjGapList";

		String srcFile = PathTools.getPath(testDir, ClassTools.getPathFromClass(srcClass)) + ".java.tpl";
		String src = readFile(srcFile);

		src = applyTemplate(src);
		String javaFile = applyTemplate("{NAME}ObjGapList.java");

		String dstFile = PathTools.getPath(
				srcDir,
				ClassTools.getPathFromClass(ClassTools.getPackageName(dstClass)),
				"primitive",
				javaFile);

		setFile(dstFile);
		setFileContent(src);
	}

}
