package org.magicwerk.brownies.collections.dev;

import java.util.regex.Pattern;

import org.magicwerk.brownies.collections.dev.BuildSource.Builder;
import org.magicwerk.brownies.collections.dev.BuildSource.FileBuilder;
import org.magicwerk.brownies.collections.dev.RefactorVisitor.RefactorMethod;
import org.magicwerk.brownies.core.files.PathTools;
import org.magicwerk.brownies.core.reflect.ClassTools;
import org.magicwerk.brownies.core.regex.RegexReplacer;
import org.magicwerk.brownies.core.regex.RegexTools;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;

/**
 * Create source files IIntList, etc. out of file IList
 * in package org.magicwerk.brownies.collections.primitive
 *
 * @author Thomas Mauch
 */
public class BuildSourceIList extends FileBuilder {

	/**
	 * @see BuildSourceIList
	 */
	public BuildSourceIList(Builder builder) {
		super(builder);
	}

	@Override
	public void build() {
		String srcClass = "org.magicwerk.brownies.collections.IList";
		String src = readJavaFile(srcClass);
		CompilationUnit cu = parseJavaSource(src);

		visitor = new RefactorVisitor();
		visitor.addRemoveTypes("Iter", "ListIter");
		visitor.addRemoveMethods("iterator", "listIterator", "descendingIterator");
		addRefactor("void move", "(fill\\(.*,) null\\)", "{1} {DEFAULT})");
		addRefactor("toArray", "array\\[size\\] = null", "array[size] = {DEFAULT}");
		visitor.addRefactorMethods(new RefactorMethod() {
			@Override
			public boolean match(MethodSource method) {
				return (method.getName().equals("toArray") && method.getMethodDecl().getModifiers().contains(Modifier.staticModifier()));
			}

			@Override
			public boolean refactor(MethodSource method) {
				String refSrc = "	static Object[] toArray(Collection<?> coll) {\r\n" + "    	Object[] values = coll.toArray();\r\n"
						+ "    	// as in ArrayList: toArray() might (incorrectly) not return Object[] (see bug 6260652)\r\n"
						+ "    	if (values.getClass() != Object[].class) {\r\n" + "    		values = Arrays.copyOf(values, values.length, Object[].class);\r\n"
						+ "	    }\r\n" + "    	return values;\r\n" + "	}\r\n" + "";
				String newSrc = "	static {PRIMITIVE}[] toArray(Collection<{WRAPPER}> coll) { \r\n" + "      Object[] values = coll.toArray(); \r\n"
						+ "       {PRIMITIVE}[] v = new {PRIMITIVE}[values.length]; \r\n" + "       for (int i=0; i<values.length; i++) { \r\n"
						+ "        v[i] = ({WRAPPER}) values[i]; \r\n" + "       }       return v; \r\n" + "}";
				method.setSource(replaceChecked(refSrc, method.getSource(), newSrc));
				return false;
			}
		});
		visitor.addRefactorMethods(new RefactorMethod() {
			@Override
			public boolean match(MethodSource method) {
				return method.getName().equals("equals");
			}

			@Override
			public boolean refactor(MethodSource method) {
				String refSrc = "    @Override public boolean equals(Object obj) {\r\n" + "    	if (obj == this) {\r\n" + "    		return true;\r\n"
						+ "    	}\r\n" + "    	if (!(obj instanceof List<?>)) {\r\n" + "    		return false;\r\n" + "    	}\r\n"
						+ "    	@SuppressWarnings(\"unchecked\")\r\n" + "		List<E> list = (List<E>) obj;\r\n" + "    	int size = size();\r\n"
						+ "    	if (size != list.size()) {\r\n" + "    		return false;\r\n" + "    	}\r\n" + "    	for (int i = 0; i < size; i++) {\r\n"
						+ "    		if (!equalsElem(doGet(i), list.get(i))) {\r\n" + "    			return false;\r\n" + "    		}\r\n" + "    	}\r\n"
						+ "    	return true;\r\n" + "    }\r\n" + "";
				String newSrc = "    @Override public boolean equals(Object obj) {\r\n" + "    	if (obj == this) {\r\n" + "    		return true;\r\n"
						+ "    	}\r\n" + "    	if (obj instanceof {NAME}ObjGapList) { \r\n" + "    		obj = (({NAME}ObjGapList) obj).list; \r\n"
						+ "    	} else if (obj instanceof {NAME}ObjBigList) { \r\n" + "    		obj = (({NAME}ObjBigList) obj).list; \r\n" + "    	}\r\n"
						+ " 		if (!(obj instanceof I{NAME}List)) {\r\n" + "    		return false;\r\n" + "    	}\r\n"
						+ "    	@SuppressWarnings(\"unchecked\")\r\n" + "		I{NAME}List list = (I{NAME}List) obj;\r\n" + "    	int size = size();\r\n"
						+ "    	if (size != list.size()) {\r\n" + "    		return false;\r\n" + "    	}\r\n" + "    	for (int i=0; i<size; i++) {\r\n"
						+ "    		if (!equalsElem(doGet(i), list.get(i))) {\r\n" + "    			return false;\r\n" + "    		}\r\n" + "    	}\r\n"
						+ "    	return true;\r\n" + "    }\r\n" + "";
				method.setSource(replaceChecked(refSrc, method.getSource(), newSrc));
				return false;
			}
		});
		visitor.addRefactorMethods(new RefactorMethod() {
			@Override
			public boolean match(MethodSource method) {
				return method.getName().equals("equalsElem");
			}

			@Override
			public boolean refactor(MethodSource method) {
				if ("double".equals(BuildSourceIList.this.builder.getPrimitiveType())) {
					method.setBody("{ // as in Double.equals\nreturn Double.doubleToLongBits(elem1) == Double.doubleToLongBits(elem2); }");
				} else if ("float".equals(BuildSourceIList.this.builder.getPrimitiveType())) {
					method.setBody("{ // as in Float.equals\nreturn Float.floatToIntBits(elem1) == Float.floatToIntBits(elem2); }");
				} else {
					method.setBody("{ return elem1 == elem2; }");
				}
				method.setSource(processMethod(method.getSource()));
				return false;
			}
		});
		visitor.addRefactorMethods(new RefactorMethod() {
			// TODO use same hashcode as wrappers
			@Override
			public boolean match(MethodSource method) {
				return method.getName().equals("hashCodeElem");
			}

			@Override
			public boolean refactor(MethodSource method) {
				if ("boolean".equals(BuildSourceIList.this.builder.getPrimitiveType())) {
					method.setBody("{ // as in Boolean.hashCode\nreturn (elem ? 1231 : 1237); }");
				} else {
					method.setBody("{ return (int) elem; }");
				}
				method.setSource(processMethod(method.getSource()));
				return false;
			}
		});
		// Remove second remove() method
		visitor.addRefactorMethods(new RefactorMethod() {
			final int[] removeCount = new int[1];

			@Override
			public boolean match(MethodSource method) {
				if (method.getName().equals("remove")) {
					removeCount[0]++;
					if (removeCount[0] == 2) {
						return true;
					}
				}
				return false;
			}

			@Override
			public boolean refactor(MethodSource method) {
				String src = method.getSource();
				src = substitute("remove\\(Object", src, "removeElem({PRIMITIVE}");
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

		// Support for:
		// - remove method
		// - rename method
		// - change comment
		// - change code
		// - change comment + code

		cu.accept(visitor, null);
		src = visitor.toString();
		src = processClass(src);
		src = applyTemplate(src);
		String javaFile = applyTemplate("I{NAME}List.java");

		String dstFile = PathTools.getPath(srcDir, ClassTools.getPathFromClass(ClassTools.getParentPackage(srcClass)), "primitive", javaFile);

		setFile(dstFile);
		setFileContent(src);
	}

	String processClass(String src) {
		src = substitute(" extends AbstractList\\<E\\>", src, "");
		src = substitute("(?s)implements.*?Deque.*?\\{", src, "implements Cloneable, Serializable '{'");
		src = substitute("class IList\\<E\\>", src, "class I{NAME}List");
		src = substitute("IList(?!(\\<R\\>)? list)", src, "I{NAME}List");
		src = substitute("\\(I{NAME}List\\<R\\>\\) doCreate\\(", src, "(IList<R>) new GapList<R>(");
		src = substitute("@Override", src, "");
		src = substitute("package org.magicwerk.brownies.collections;", src, "package org.magicwerk.brownies.collections.primitive;\n" + "\n"
				+ "import org.magicwerk.brownies.collections.IList;\n" + "import org.magicwerk.brownies.collections.GapList;\n");
		src = substitute("I{NAME}List\\<\\?\\>", src, "I{NAME}List");
		src = substitute("I{NAME}List\\<{WRAPPER}>", src, "I{NAME}List");
		src = substitute("I{NAME}List\\<E\\>", src, "I{NAME}List");
		src = substitute("checkNonNull\\({PRIMITIVE} obj\\)", src, "checkNonNull(Object obj)");
		src = substitute("\\(obj == (false|0|\\().*\\)", src, "(obj == null)");

		if ("char".equals(BuildSourceIList.this.builder.getPrimitiveType())) {
			src = src.replace("implements Cloneable, Serializable", "implements Cloneable, Serializable, CharSequence");

			Pattern p = Pattern.compile("(public String toString\\(\\) \\{)(.*)(public boolean isEmpty\\(\\))", Pattern.DOTALL);
			src = new RegexReplacer().setPattern(p).setFormat("{1}\nreturn new String(toArray());\n}\n\n{3}").replace(src);

			//@formatter:off
			String add = ""
					+ "	// Implementation of CharSequence \r\n" + "\r\n" 
							+ "	@Override\r\n" + "	public int length() {\r\n" 
							+ "		return size();\r\n"
							+ "	}\r\n" + "\r\n" + "	@Override\r\n" 
							+ "	public char charAt(int index) {\r\n" 
							+ "		return get(index);\r\n" + "	}\r\n" + "\r\n"
							+ "	@Override\r\n" 
							+ "	public CharSequence subSequence(int start, int end) {\r\n" 
							+ "		return getAll(start, end-start);\r\n"
							+ "	}\r\n";
			//@formatter:on
			int pos = src.lastIndexOf('}');
			src = src.substring(0, pos) + add + src.substring(pos);
		}

		// for IReadOnlyList
		src = substitute("E\\[", src, "{PRIMITIVE}[");
		src = substitute("E elem", src, "{PRIMITIVE} elem");
		src = substitute("Object\\[\\] array", src, "{PRIMITIVE}[] array");
		src = substitute("IReadOnlyList", src, "IReadOnly{NAME}List");
		src = substitute("\\<E\\>", src, "");
		src = substitute("List list2", src, "List<{WRAPPER}> list2");

		src = substitute("I{NAME}List<RR>", src, "IList<R>");

		return src;
	}

	String processMethod(String src) {
		src = substitute("mappedList = doCreate", src, "mappedList = new GapList");
		src = substitute("IList\\<\\? extends E\\>", src, "I{NAME}List");
		src = substitute("IList\\<E\\>", src, "I{NAME}List");

		src = substitute("<R> IList\\<R\\> map\\(Function\\<E, R\\> func", src, "<R> IList<RR> map(Function<{WRAPPER},R> func");

		src = substitute("<R> IList\\<R\\> mapFilter\\(Function\\<E, R\\> func, Predicate\\<R\\> filter", src,
				"<R> IList<RR> mapFilter(Function<{WRAPPER},R> func, Predicate<R> filter");

		src = substitute("<R> IList\\<R\\> filterMap\\(Predicate\\<E\\> filter, Function\\<E, R\\> func", src,
				"<R> IList<RR> filterMap(Predicate<{WRAPPER}> filter, Function<{WRAPPER},R> func");

		//src = substitute("map\\(UnaryOperator\\<E\\> op", src, "map(UnaryOperator<{WRAPPER}> op");

		src = substitute("E ", src, "{PRIMITIVE} ");
		src = substitute("E\\[", src, "{PRIMITIVE}[");
		src = substitute("E\\.\\.\\.", src, "{PRIMITIVE}...");
		src = substitute("\\<E\\>", src, "");
		src = substitute("\\<T\\>", src, "");
		src = substitute("\\(T\\)", src, "");
		src = substitute("T\\[", src, "{PRIMITIVE}[");
		src = substitute("Collection\\<\\?\\>", src, "Collection<{WRAPPER}>");
		src = substitute("\\? extends E", src, "{WRAPPER}");
		src = substitute("\\? super E\\> dst", src, "{WRAPPER}> dst");
		src = substitute("\\? super E\\> predicate", src, "{WRAPPER}> predicate");
		src = substitute("null", src, "{DEFAULT}");
		src = substitute("list == " + RegexTools.regexForLiteral(src), src, "list == null");
		src = substitute("list = {DEFAULT}", src, "list = null");
		src = substitute("removed == {DEFAULT_REGEX}", src, "removed == null");
		src = substitute("removed = {DEFAULT_REGEX}", src, "removed = null");
		src = substitute("if \\(list != {DEFAULT}\\)", src, "if (list != null)");
		src = substitute("if \\(list != \\({PRIMITIVE}\\) 0\\)", src, "if (list != null)");
		src = substitute("Object", src, "{PRIMITIVE}");
		src = substitute("{PRIMITIVE} clone", src, "Object clone");
		src = substitute("equals\\({PRIMITIVE} obj\\)", src, "equals(Object obj)");
		src = substitute(", Comparator\\<\\? super E\\> comparator", src, "");
		src = substitute("Comparator\\<\\? super E\\> comparator", src, "");
		src = substitute(", comparator", src, "");
		src = substitute(", Comparator\\<\\? super K\\> comparator", src, "");
		src = substitute("\\<K\\>", src, "");
		src = substitute("K key", src, "{PRIMITIVE} key");
		src = substitute("UnaryOperator", src, "UnaryOperator<{WRAPPER}>");
		src = substitute("Set ", src, "Set<{WRAPPER}> ");

		// for IReadOnlyList
		//src = substituteNested("(?s)class IReadOnly.*?unmodifiableList.*?\\}", "return (.*?);", src, "return null;");
		//src = substituteNested("(?s)class IReadOnly.*?doCreate.*?\\}", "return (.*?);", src, "return null;");
		src = substituteNested("(?s)(unmodifiableList|doCreate).*error.*?\\}", "return .*?;", src, "return null;");
		src = substitute("coll.toArray\\(\\)", src, "toArray(coll)");
		src = substitute("\\(E\\)", src, "");

		return src;
	}

}
