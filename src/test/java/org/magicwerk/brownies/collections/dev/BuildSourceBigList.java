package org.magicwerk.brownies.collections.dev;

import org.apache.commons.lang3.StringUtils;
import org.magicwerk.brownies.collections.dev.BuildSource.Builder;
import org.magicwerk.brownies.collections.dev.BuildSource.FileBuilder;
import org.magicwerk.brownies.collections.dev.RefactorVisitor.RefactorMethod;
import org.magicwerk.brownies.core.files.FileTools;
import org.magicwerk.brownies.core.files.PathTools;
import org.magicwerk.brownies.core.reflect.ClassTools;
import org.magicwerk.brownies.core.regex.RegexTools;

import com.github.javaparser.ast.CompilationUnit;

/**
 * Create source files IntBigList, etc. out of file BigList
 * in package org.magicwerk.brownies.collections.primitive
 *
 * @author Thomas Mauch
 */
public class BuildSourceBigList extends FileBuilder {

	public BuildSourceBigList(Builder builder) {
		super(builder);
	}

	@Override
	public void build() {
		String srcClass = "org.magicwerk.brownies.collections.BigList";

		String src = readJavaFile(srcClass);
		CompilationUnit cu = parseJavaSource(src);

		visitor = new RefactorVisitor();
		addRefactor("int binarySearch", ", Comparator\\<\\? super K\\> comparator", "");
		addRefactor("int binarySearch", ", \\(Comparator\\<Object\\>\\) comparator", "");
		addRefactor(" sort", ", Comparator\\<\\? super E\\> comparator", "");

		visitor.addRefactorMethods(new RefactorMethod() {
			@Override
			public boolean match(MethodSource method) {
				return method.getName().equals("readObject");
			}

			@Override
			public boolean refactor(MethodSource method) {
				String src = method.getSource();
				src = substitute("new Object", src, "new {PRIMITIVE}");
				src = substitute(RegexTools.regexForLiteral("(E) ois.readObject()"), src, "ois.read{NAME}()");
				src = substitute(RegexTools.regexForLiteral("(E)"), src, "(int)");
				method.setSource(src);
				return false;
			}
		});
		visitor.addRefactorMethods(new RefactorMethod() {
			@Override
			public boolean match(MethodSource method) {
				return method.getName().equals("writeObject");
			}

			@Override
			public boolean refactor(MethodSource method) {
				String src = method.getSource();
				src = substitute(RegexTools.regexForLiteral("oos.writeObject"), src, "oos.write{NAME}");
				method.setSource(src);
				return false;
			}
		});
		visitor.addRefactorMethods(new RefactorMethod() {
			@Override
			public boolean match(MethodSource method) {
				return true;
			}

			@Override
			public boolean refactor(MethodSource method) {
				method.setSource(processMethod(method.getSource()));
				return true;
			}
		});
		visitor.addRemoveMethods("spliterator");

		cu.accept(visitor, null);
		src = visitor.toString();
		src = processClass(src);
		src = insertTemplate(src);
		src = applyTemplate(src);
		String javaFile = applyTemplate("{NAME}BigList.java");

		String dstFile = PathTools.getPath(
				srcDir,
				ClassTools.getPathFromClass(ClassTools.getPackageName(srcClass)),
				"primitive",
				javaFile);

		setFile(dstFile);
		setFileContent(src);
	}

	String insertTemplate(String src) {
		String tplPath = PathTools.getPath(testDir, "org/magicwerk/brownies/collections/dev/PrimitiveList.java.tpl");
		String tplFile = FileTools.readFile().setFile(tplPath).readText();
		tplFile = StringUtils.replace(tplFile, "{", "'{'");
		tplFile = StringUtils.replace(tplFile, "}", "'}'");

		src = substitute("(?s)(extends I{NAME}List \\{\n)", src, "{1}" + tplFile);
		return src;
	}

	String processClass(String src) {
		src = substitute("import java.util.Dequeu;", src, "");
		src = substitute("\\<E\\>", src, "");
		src = substitute("\\<EE\\>", src, "");
		src = substitute("E\\[\\]", src, "{PRIMITIVE}[]");
		src = substitute("BigList", src, "{NAME}BigList");
		src = substitute("return null", src, "return {DEFAULT}");
		src = substitute("GapList", src, "{NAME}GapList");
		src = substitute("{NAME}GapList<T>", src, "{NAME}GapList");
		src = substitute("Block", src, "{NAME}Block");
		src = substitute("{NAME}Block<T>", src, "{NAME}Block");
		src = substitute("MergeSort", src, "{NAME}MergeSort");
		src = substitute("import org.magicwerk.brownies.collections.helper.{NAME}MergeSort;", src,
				"import org.magicwerk.brownies.collections.helper.primitive.{NAME}MergeSort;");
		src = substitute("IList", src, "I{NAME}List");
		src = substitute("(\\b)List", src, "{1}I{NAME}List");
		src = substitute("Object\\[", src, "{PRIMITIVE}[");
		src = substitute("@SuppressWarnings\\(\".*\"\\)", src, "");
		// for mappedList()
		src = substitute("{NAME}BigList\\<R\\>", src, "BigList<R>");
		src = substitute("Mapper\\<E, R\\>", src, "Mapper<{WRAPPER}, R>");
		src = substitute("import java.util.I{NAME}List;", src, "import java.util.List;");
		src = substitute("values.init\\(that.values\\);", src, "values.init(that.values.getArray(0, that.values.size()));");
		src = substitute("I{NAME}List\\<{WRAPPER}\\>", src, "I{NAME}List");

		src = substitute("{PRIMITIVE} obj", src, "Object obj");
		src = substitute("add\\(\\(E\\) obj", src, "add(({WRAPPER}) obj");
		src = substitute("Collections.binarySearch\\(\\(I{NAME}List\\) this, key\\)", src, "{NAME}BinarySearch.binarySearch(this, key, 0, size())");
		src = substitute("add\\(null\\)", src, "add({DEFAULT})");
		src = substitute("add\\(0, null\\)", src, "add(0, {DEFAULT})");
		src = substitute("BlockLen, null", src, "BlockLen, {DEFAULT}");

		src = substituteNested("(?s)doRemoveSelf.*?\\}", "return .*?;", src, "return null;");

		src = substitute("package org.magicwerk.brownies.collections;", src,
				"package org.magicwerk.brownies.collections.primitive;\n" +
						"import org.magicwerk.brownies.collections.helper.ArraysHelper;\n" +
						"import org.magicwerk.brownies.collections.helper.primitive.{NAME}BinarySearch;\n" +
						"import org.magicwerk.brownies.collections.GapList;\n" +
						"import org.magicwerk.brownies.collections.BigList;");

		if ("char".equals(BuildSourceBigList.this.builder.getPrimitiveType())) {
			String add = "   // Special string methods \r\n" +
					"\r\n" +
					"	public static CharBigList create(String str) {\r\n" +
					"		return new CharBigList(str);\r\n" +
					"	}\r\n" +
					"\r\n" +
					"	public CharBigList(String str) {\r\n" +
					"		init(str);\r\n" +
					"	}\r\n" +
					"\r\n" +
					"	public void init(String str) {\r\n" +
					"		char[] array = str.toCharArray();\r\n" +
					"		initArray(array);\r\n" + // TODO array is copied twice
					"	}\r\n" +
					"\r\n";
			int pos = src.lastIndexOf('}');
			src = src.substring(0, pos) + add + src.substring(pos);
		}
		return src;
	}

	String processMethod(String src) {
		src = substitute("E ", src, "{PRIMITIVE} ");
		src = substitute("E\\[", src, "{PRIMITIVE}[");
		src = substitute("E\\.\\.\\.", src, "{PRIMITIVE}...");
		src = substitute("\\<E\\>", src, "");
		src = substitute("\\<T\\>", src, "");
		src = substitute("\\(T\\)", src, "");
		src = substitute("T\\[", src, "{PRIMITIVE}[");
		src = substitute("Collection\\<\\?\\>", src, "Collection<{WRAPPER}>");
		src = substitute("\\? extends E", src, "{WRAPPER}");
		//src = substitute("(?<!alues ==? )null", src, "{DEFAULT}");
		src = substitute("Object", src, "{PRIMITIVE}");
		src = substitute("{PRIMITIVE} clone", src, "Object clone");
		src = substitute("int clone", src, "Object clone");
		src = substitute("equals\\({PRIMITIVE} obj\\)", src, "equals(Object obj)");
		src = substitute("int\\[\\] newValues = 0", src, "{PRIMITIVE}[] newValues = null");
		src = substitute("Collection\\<\\? extends E\\>", src, "Collection<{WRAPPER}>");
		src = substitute(", comparator", src, "");
		src = substitute(", Comparator\\<\\? super K\\> comparator", src, "");
		src = substitute("\\<K\\>", src, "");
		src = substitute("K key", src, "{PRIMITIVE} key");
		src = substitute("Arrays\\.sort", src, "ArraysHelper.sort");
		src = substitute("Arrays\\.bin", src, "ArraysHelper.bin");
		src = substitute("if \\(elems != {DEFAULT}\\)", src, "if (elems != null)");
		src = substitute("if \\(elems != \\({PRIMITIVE}\\) 0\\)", src, "if (elems != null)");
		// return new BooleanBigList<>(false, null);
		src = substitute("\\<\\>\\(false, null\\)", src, "(false, null)");
		return src;
	}

}
