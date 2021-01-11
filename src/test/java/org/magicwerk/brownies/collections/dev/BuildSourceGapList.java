package org.magicwerk.brownies.collections.dev;

import org.magicwerk.brownies.collections.dev.BuildSource.Builder;
import org.magicwerk.brownies.collections.dev.BuildSource.FileBuilder;
import org.magicwerk.brownies.collections.dev.RefactorVisitor.RefactorMethod;
import org.magicwerk.brownies.core.files.PathTools;
import org.magicwerk.brownies.core.reflect.ClassTools;
import org.magicwerk.brownies.core.regex.RegexTools;
import org.magicwerk.brownies.dev.sources.JavaParserTools;

import com.github.javaparser.ast.CompilationUnit;

/**
 * Create source files IntGapList, etc. out of file GapList
 * in package org.magicwerk.brownies.colletions.primitive
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class BuildSourceGapList extends FileBuilder {

	public BuildSourceGapList(Builder builder) {
		super(builder);
	}

	@Override
	public void build() {
		String srcClass = "org.magicwerk.brownies.collections.GapList";

		String srcFile = PathTools.getPath(srcDir, ClassTools.getPathFromClass(srcClass)) + ".java";
		String src = readFile(srcFile);

		CompilationUnit cu = JavaParserTools.getCompilationUnit(src);

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

		cu.accept(visitor, null);
		src = visitor.toString();
		src = processClass(src);
		src = applyTemplate(src);
		String javaFile = applyTemplate("{NAME}GapList.java");

		String dstFile = PathTools.getPath(
				srcDir,
				ClassTools.getPathFromClass(ClassTools.getPackageName(srcClass)),
				"primitive",
				javaFile);

		setFile(dstFile);
		setFileContent(src);
	}

	String processClass(String src) {
		src = substitute("\\<E\\>", src, "");
		src = substitute("\\<EE\\>", src, "");
		src = substitute("E\\[\\]", src, "{PRIMITIVE}[]");
		src = substitute("GapList", src, "{NAME}GapList");
		src = substitute("IList", src, "I{NAME}List");
		src = substitute("Object\\[", src, "{PRIMITIVE}[");
		src = substitute("@SuppressWarnings\\(\".*\"\\)", src, "");
		src = substitute("package org.magicwerk.brownies.collections;", src,
				"package org.magicwerk.brownies.collections.primitive;\n" +
						"import org.magicwerk.brownies.collections.helper.ArraysHelper;\n" +
						"import org.magicwerk.brownies.collections.GapList;");

		// for mappedList()
		src = substitute("{NAME}GapList\\<R\\>", src, "GapList<R>");
		src = substitute("Function\\<E, R\\>", src, "Function<{WRAPPER}, R>");

		if ("char".equals(BuildSourceGapList.this.builder.getPrimitiveType())) {
			String add = "   // Special string methods \r\n" +
					"\r\n" +
					"	public static CharGapList create(String str) {\r\n" +
					"		return new CharGapList(str);\r\n" +
					"	}\r\n" +
					"\r\n" +
					"	public CharGapList(String str) {\r\n" +
					"		init(str);\r\n" +
					"	}\r\n" +
					"\r\n" +
					"	public void init(String str) {\r\n" +
					"		char[] array = str.toCharArray();\r\n" +
					"		init(array, array.length);\r\n" +
					"	}\r\n";
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
		src = substitute("(?<!alues ==? )null", src, "{DEFAULT}");
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
		src = substitute("(coll != {DEFAULT})", src, "(coll != null)");
		src = substitute("(coll != \\({PRIMITIVE}\\) 0)", src, "(coll != null)");
		return src;
	}

}
