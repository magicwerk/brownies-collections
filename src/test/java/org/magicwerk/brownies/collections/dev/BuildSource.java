package org.magicwerk.brownies.collections.dev;

import java.util.EnumSet;

import org.apache.commons.lang3.StringUtils;
import org.magicwerk.brownies.collections.dev.RefactorVisitor.RefactorMethod;
import org.magicwerk.brownies.core.diff.DiffState;
import org.magicwerk.brownies.core.diff.StringDiffTools;
import org.magicwerk.brownies.core.files.FileTools;
import org.magicwerk.brownies.core.files.PathTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ClassTools;
import org.magicwerk.brownies.core.reflect.ReflectTypes;
import org.magicwerk.brownies.core.regex.RegexBuilder;
import org.magicwerk.brownies.core.regex.RegexReplacer;
import org.magicwerk.brownies.core.regex.RegexTools;
import org.magicwerk.brownies.core.strings.StringFormat;
import org.magicwerk.brownies.core.strings.StringFormatParsers;
import org.magicwerk.brownies.core.strings.StringFormatter;
import org.magicwerk.brownies.core.strings.matcher.NestedStringMatcher;
import org.magicwerk.brownies.core.strings.matcher.RegexStringMatcher;
import org.magicwerk.brownies.javassist.sources.JavaParserReader;
import org.slf4j.Logger;

import com.github.javaparser.ast.CompilationUnit;

/**
 * Create Java source files for brownies-collections.
 * <p>
 * - Brownies-Collections (src/main/java): <br>
 * collections\primitive\IBooleanList.java <br>
 * collections\primitive\BooleanGapList.java <br>
 * collections\primitive\BooleanObjGapList.java <br>
 * collections\primitive\BooleanBigList.java <br>
 * collections\helper\primitive\BooleanMergeSort.java <br>
 * collections\helper\primitive\BooleanBinarySearch.java <br>
 * collections\primitive\BooleanObjBigList.java <br>
 * <p>
 * - Brownies-Collections-Test (src/test/java): <br>
 * collections\BigListGapListTest.java <br>
 * <p>
 * - Manifest (src/main/resources): <br>
 * META-INF/MANIFEST.MF <br>
 */
public class BuildSource {

	static final JavaParserReader javaParserReader = new JavaParserReader();

	static abstract class FileBuilder {
		String srcDir;
		String testDir;
		Builder builder;
		RefactorVisitor visitor;
		String file;
		String fileContent;

		public FileBuilder() {
			this.srcDir = FileTools.getAbsolutePath("src/main/java");
			this.testDir = FileTools.getAbsolutePath("src/test/java");
		}

		public FileBuilder(Builder builder) {
			this();
			this.builder = builder;
		}

		public Builder getBuilder() {
			return builder;
		}

		public String getFile() {
			return file;
		}

		public void setFile(String file) {
			this.file = file;
		}

		public String getFileContent() {
			return fileContent;
		}

		public void setFileContent(String fileContent) {
			this.fileContent = fileContent;
		}

		abstract public void build();

		public void addRefactor(final String matchHeader, final String matchText, final String substText) {
			visitor.addRefactorMethods(new RefactorMethod() {
				@Override
				public boolean match(MethodSource method) {
					return RegexTools.find(matchHeader, method.getHeader());
				}

				@Override
				public boolean refactor(MethodSource method) {
					String src = method.getSource();
					src = substitute(matchText, src, substText);
					method.setSource(src);
					return true;
				}
			});
		}

		String applyTemplate(String str) {
			if (builder != null) {
				//			StringMapper sm = new StringMapper(
				//				"PRIMITIVE", builder.getPrimitiveType(),
				//				"WRAPPER", builder.getWrapperType(),
				//				"DEFAULT", builder.getWrapperType(),
				//				"NAME", builder.getDefaultValue());
				//			str = StringFormatter.format(str, sm);

				str = str.replace("{PRIMITIVE}", builder.getPrimitiveType());
				str = str.replace("{WRAPPER}", builder.getWrapperType());
				str = str.replace("{NAME}", builder.getTypeName());
				str = str.replace("{DEFAULT}", builder.getDefaultValue());
				str = str.replace("{DEFAULT_REGEX}", RegexBuilder.regexForLiteral(builder.getDefaultValue()));
			}
			return str;
		}

		StringFormat applyFormat(String str) {
			str = str.replace("{PRIMITIVE}", "'{PRIMITIVE}'");
			str = str.replace("{WRAPPER}", "'{WRAPPER}'");
			str = str.replace("{NAME}", "'{NAME}'");
			str = str.replace("{DEFAULT}", "'{DEFAULT}'");
			return new StringFormat(str, StringFormatParsers.MessageFormatParser);
		}

		String substitute(String regex, String input, String message) {
			return substitute(regex, input, message, false);
		}

		String substitute(String regex, String input, String message, boolean debug) {
			regex = applyTemplate(regex);
			//message = RegexTools.getLiteralMessageFormat(message);
			StringFormat format = applyFormat(message);

			String src = new RegexReplacer().setPattern(regex).setFormat(format).replace(input);
			src = applyTemplate(src);
			if (debug && !src.equals(input)) {
				String sd = getAnnotatedStringDiff(input, src);
				LOG.info("Change: {}", sd);
			}
			return src;
		}

		static String getAnnotatedStringDiff(String s0, String s1) {
			return StringDiffTools.getAnnotatedLinesDiff(s0, s1, EnumSet.of(DiffState.DELETED, DiffState.ADDED));
		}

		String substituteNested(String regex1, String regex2, String input, String message) {
			regex1 = applyTemplate(regex1);
			regex2 = applyTemplate(regex2);
			//message = RegexTools.getLiteralMessageFormat(message);
			StringFormat format = applyFormat(message);

			RegexStringMatcher m1 = new RegexStringMatcher().setPattern(regex1);
			RegexStringMatcher m2 = new RegexStringMatcher().setPattern(regex2);
			NestedStringMatcher nsm = new NestedStringMatcher(m1, m2);
			String src = new RegexReplacer().setMatcher(nsm).setFormat(format).replace(input);
			src = applyTemplate(src);
			return src;
		}

		String replaceChecked(String refStr, String str, String newStr) {
			String refStr2 = StringUtils.normalizeSpace(refStr);
			String str2 = StringUtils.normalizeSpace(str);
			if (!str2.equals(refStr2)) {
				String msg = StringFormatter.format("String replace failed because of unexpected input:\n" + "Expected: {}\n" + "Found: {}", refStr, str);
				throw new IllegalArgumentException(msg);
			}
			newStr = applyTemplate(newStr);
			return newStr;
		}

		String readJavaFile(String srcClass) {
			String srcFile = PathTools.getPath(srcDir, ClassTools.getPathFromClass(srcClass)) + ".java";
			String src = readFile(srcFile);
			return src;
		}

		CompilationUnit parseJavaSource(String src) {
			CompilationUnit cu = javaParserReader.parseCompilationUnit(src);
			return cu;
		}

		String readFile(String file) {
			return FileTools.readFile().setFile(file).readText();
		}

	}

	/**
	 * Create source files for one primitive type, e.g. IIntList, IntGapList, and IntObjGapList
	 */
	static class Builder {
		String primitiveType;
		String wrapperType;
		String defaultValue;

		public Builder(String primitiveType, String wrapperType, String defaultValue) {
			this.primitiveType = primitiveType;
			this.wrapperType = wrapperType;
			this.defaultValue = defaultValue;
		}

		/**
		 * @return primitive type, e.g. "int"
		 */
		String getPrimitiveType() {
			return primitiveType;
		}

		/**
		 * @return primitive type, e.g. "Integer"
		 */
		String getWrapperType() {
			return wrapperType;
		}

		/**
		 * @return name, i.e. capitalized primitive type, e.g. "Int"
		 */
		String getTypeName() {
			return StringUtils.capitalize(primitiveType);
		}

		String getDefaultValue() {
			return defaultValue;
		}

		public void build() {
			// Interface
			FileBuilder builderIList = new BuildSourceIList(this);
			builderIList.build();
			writeFile(builderIList.getFile(), builderIList.getFileContent());

			// GapList
			FileBuilder builderGapList = new BuildSourceGapList(this);
			builderGapList.build();
			writeFile(builderGapList.getFile(), builderGapList.getFileContent());

			FileBuilder builderObjGapList = new BuildSourceObjGapList(this);
			builderObjGapList.build();
			writeFile(builderObjGapList.getFile(), builderObjGapList.getFileContent());

			// BigList
			FileBuilder builderBigList = new BuildSourceBigList(this);
			builderBigList.build();
			writeFile(builderBigList.getFile(), builderBigList.getFileContent());

			FileBuilder builderMergeSort = new BuildSourceMergeSort(this);
			builderMergeSort.build();
			writeFile(builderMergeSort.getFile(), builderMergeSort.getFileContent());

			FileBuilder builderBinarySearch = new BuildSourceBinarySearch(this);
			builderBinarySearch.build();
			writeFile(builderBinarySearch.getFile(), builderBinarySearch.getFileContent());

			FileBuilder builderObjBigList = new BuildSourceObjBigList(this);
			builderObjBigList.build();
			writeFile(builderObjBigList.getFile(), builderObjBigList.getFileContent());
		}
	}

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new BuildSource().run();
	}

	void run() {
		// Generate source in in src/main/java, src/test/java
		buildSource();
		// Generate manifest in src/main/resources
		new BuildManifest().run();
	}

	/**
	 * Generate Java source files for primitive types.
	 */
	void buildSource() {
		// Generate source files
		for (String primitiveType : ReflectTypes.VALUE_TYPES) {
			Class<?> primitiveClass = ReflectTypes.getPrimitiveClass(primitiveType);
			String wrapperType = ReflectTypes.getWrapperFromPrimitive(primitiveClass).getSimpleName();
			String defaultValue = ReflectTypes.getPrimitiveDefaultValue(primitiveClass);
			Builder builder = new Builder(primitiveType, wrapperType, defaultValue);
			builder.build();
		}

		// Generate test files
		FileBuilder buildTest;

		//		buildTest = new BuildSourceTest("IntObjGapList");
		//		buildTest.build();
		//		writeFile(buildTest.getFile(), buildTest.getFileContent());

		buildTest = new BuildSourceTest("BigList");
		buildTest.build();
		writeFile(buildTest.getFile(), buildTest.getFileContent());
	}

	static void writeFile(String file, String src) {
		LOG.info("Write file {}", file);
		//		CompilationUnit cu = javaParserReader.parseCompilationUnit(src);
		//		src = javaParserReader.print(cu);
		FileTools.writeFile().setFile(file).setText(src).write();
	}

}
