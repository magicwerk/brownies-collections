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
public class BuildSourceMergeSort extends FileBuilder {

	/**
	 * @see BuildSourceMergeSort
	 */
	public BuildSourceMergeSort(Builder builder) {
		super(builder);
	}

	@Override
	public void build() {
		String srcClass = "org.magicwerk.brownies.collections.helper.MergeSort";
		String dstClass = "org.magicwerk.brownies.collections.helper.primitive.{NAME}MergeSort";
		dstClass = applyTemplate(dstClass);

		String srcFile = PathTools.getPath(srcDir, ClassTools.getPathFromClass(srcClass)) + ".java";
		String src = readFile(srcFile);

		src = processClass(src);
		src = applyTemplate(src);
		String javaFile = applyTemplate("{NAME}MergeSort.java");

		String dstFile = PathTools.getPath(
				srcDir,
				ClassTools.getPathFromClass(ClassTools.getPackageName(dstClass)),
				javaFile);

		setFile(dstFile);
		setFileContent(src);
	}

	String processClass(String src) {
		src = substitute("package org.magicwerk.brownies.collections.helper;", src,
				"package org.magicwerk.brownies.collections.helper.primitive;\n" +
						"import org.magicwerk.brownies.collections.primitive.I{NAME}List;");
		src = substitute("import java.util..*?;", src, "");
		src = substitute("MergeSort", src, "{NAME}MergeSort");
		src = substitute("{NAME}MergeSort\\<E\\>", src, "{NAME}MergeSort");
		src = substitute("List\\<E\\>", src, "I{NAME}List");
		src = substitute("Comparator\\<\\? super E\\> comparator;", src, "");
		src = substitute(", Comparator\\<\\? super E\\> comparator", src, "");
		src = substitute(", comparator", src, "");
		src = substitute("E val", src, "{PRIMITIVE} val");

		if ("boolean".equals(BuildSourceMergeSort.this.builder.getPrimitiveType())) {
			src = substitute("return comparator.compare\\(list.get\\(idx1\\), list.get\\(idx2\\)\\);", src,
					"{PRIMITIVE} val1 = list.get(idx1);\n" +
							"\t\t{PRIMITIVE} val2 = list.get(idx2);\n" +
							"\t\treturn (val1==val2) ? 0 : ((val1) ? 1 : -1);");
		} else {
			src = substitute("return comparator.compare\\(list.get\\(idx1\\), list.get\\(idx2\\)\\);", src,
					"{PRIMITIVE} val1 = list.get(idx1);\n" +
							"\t\t{PRIMITIVE} val2 = list.get(idx2);\n" +
							"\t\treturn (val1<val2 ? -1 : (val1==val2 ? 0 : 1));");
		}

		src = substitute("(?s)if \\(comparator.*?\\= comparator;", src, "");
		return src;
	}

}
