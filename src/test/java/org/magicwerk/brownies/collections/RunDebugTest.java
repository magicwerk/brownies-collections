package org.magicwerk.brownies.collections;

import java.util.HashMap;
import java.util.Map;

import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.exec.Exec;
import org.magicwerk.brownies.core.exec.Exec.ExecStatus;
import org.magicwerk.brownies.core.files.FileInfo;
import org.magicwerk.brownies.core.files.FilePath;
import org.magicwerk.brownies.core.files.FileTools;
import org.magicwerk.brownies.core.files.TextData;
import org.magicwerk.brownies.core.files.TextData.Type;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.regex.RegexReplacer;
import org.magicwerk.brownies.core.strings.StringFormatter;
import org.magicwerk.brownies.tools.dev.java.JavaDependency;
import org.magicwerk.brownies.tools.dev.tools.GradleTool;
import org.magicwerk.brownies.tools.dev.tools.JavaOptions;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;

import ch.qos.logback.classic.Logger;

/**
 * Run tests with DEBUG_CHECK enabled.
 * <p>
 * Before running, make sure that you executed 'gradle compileJava' as the class files are referenced (e.g. Brownies-Core/build/classes/java/main)
 * <p>
 * - Copy current sources from "src" into "build/tmp/runDebugTest" <br>
 * - Set constant DEBUG_CHECK to true <br>
 * - Check that assertions would fail on problems by running RunDebugTestCheck (expected to fail) <br>
 * - Execute all tests by running RunAllTest <br>
 */
public class RunDebugTest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new RunDebugTest().run();
	}

	FilePath buildDir = FilePath.of("build/tmp/runDebugTest");
	FilePath testAllJar = buildDir.get("build/libs/runDebugTest-test-all.jar");

	void run() {
		compile();
		prepareBuild();
		enableDebugCheck();
		build();
		assertDebugTestFails();
		runTests();
	}

	void assertDebugTestFails() {
		runApplication("org.magicwerk.brownies.collections.RunDebugTestCheck", false);
	}

	void runApplication(String mainClass, boolean result) {
		LOG.info("-- run {}", mainClass);

		JavaOptions jo = new JavaOptions();
		jo.setMainClass(mainClass);
		jo.setClassPath(testAllJar.getPath());
		jo.setJvmArgs("-ea");

		JavaTool jt = new JavaTool();
		jt.setPrintOutput(true);
		ExecStatus status = jt.run(jo);
		CheckTools.check(status.isOk() == result);
	}

	void runTests() {
		runApplication("org.magicwerk.brownies.collections.RunAllTest", true);
	}

	void prepareBuild() {
		GradleTool gradleTool = new GradleTool();
		IList<String> deps = gradleTool.getDependencies("testCompileClasspath").getDescendantValues(false);
		Map<String, String> versionMap = getVersionMap(deps);
		String buildGradle = StringFormatter.formatMap(buildGradleTemplate, versionMap);

		FileTools.cleanDir().setDir(buildDir).setCreateDirs(true).clean();
		FileTools.copyFile().copyDirIntoDir(FilePath.of("src"), buildDir);
		FileTools.writeFile().setFile(buildDir.get("build.gradle")).setText(buildGradle).write();
		FileTools.writeFile().setFile(buildDir.get("settings.gradle")).setText("").write();
	}

	Map<String, String> getVersionMap(IList<String> deps) {
		Map<String, String> versionMap = new HashMap<>();
		for (String dep : deps) {
			JavaDependency dp = JavaDependency.parseReference(dep);
			String name = dp.getArtifactId() + "_version";
			versionMap.put(name, dp.getVersion());
		}
		return versionMap;
	}

	void compile() {
		String cmd = "gradle compileJava";
		new Exec().setUseShell(true).setDirectory("..").setArgLine(cmd).setPrintOutput(true).setThrowOnError(true).execute();
	}

	void build() {
		String cmd = "gradle shadowTestJar";
		new Exec().setUseShell(true).setDirectory(buildDir).setArgLine(cmd).setPrintOutput(true).setThrowOnError(true).execute();
	}

	void enableDebugCheck() {
		enableDebugCheck(buildDir, true);
	}

	public static void enableDebugCheck(FilePath baseDir, boolean enabled) {
		RegexReplacer replacer = new RegexReplacer().setPattern("DEBUG_CHECK = (true|false)").setFormat("DEBUG_CHECK = " + enabled);
		String fileSet = baseDir.get("src/main/java/**/*.java").getPath();
		IList<FileInfo> files = FileTools.listFiles().setFileSet(fileSet).setFileTypeProvider(fi -> Type.TEXT).list();
		for (FileInfo file : files) {
			String text = file.getText().getText();
			String newText = replacer.replace(text);
			if (!newText.equals(text)) {
				LOG.info("Setting DEBUG_CHECK to {} in {}", enabled, file);
				file.setData(new TextData(newText));
				FileTools.writeFile(file);
			}
		}
	}

	static final String buildGradleTemplate =
			// The jintellitype dependency is contained as constant as it contains a classifier which is not returned by the Gradle dependency output
			// @formatter:off
			  "plugins {{\r\n"
			+ "  id 'java-library'\r\n"
			+ "	 id 'com.github.johnrengelman.shadow' version '8.1.1'\r\n"
			+ ""
			+ "}\r\n"
			+ "import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar\r\n"
			+ "\r\n"
			+ "repositories {{\r\n"
			+ "	 mavenLocal()\r\n"
			+ "	 mavenCentral()\r\n"
			+ "}\r\n"
			+ "\r\n"
			+ "// Use 'gradle dependencies --configuration testCompileClasspath' to see dependencies\r\n"
			+ "dependencies {{\r\n"
			+ "  testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Core/build/classes/java/main\"))\r\n"
			+ "  testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Javassist/build/classes/java/main\"))\r\n"
			+ "	 testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Jdom/build/classes/java/main\"))\r\n"
			+ "	 testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Html/build/classes/java/main\"))\r\n"
			+ "	 testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Swt/build/classes/java/main\"))\r\n"
			+ "	 testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Test/build/classes/java/main\"))\r\n"
			+ "	 testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Tools/build/classes/java/main\"))\r\n"
			+ "	 testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Dev/build/classes/java/main\"))\r\n"
			+ "	 testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/MagicTest-NG/build/classes/java/main\"))\r\n"
			+ "	 testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/MagicTest/build/classes/java/main\"))\r\n"
			+ " \r\n"
			+ "  testImplementation \"junit:junit:{junit_version}\"\r\n"
			+ "  testImplementation \"com.google.guava:guava-testlib:{guava-testlib_version}\"\r\n"
			+ "	 testImplementation \"com.github.javaparser:javaparser-core:{javaparser-core_version}\"	\r\n"
			+ "	\r\n"
			+ "	 testImplementation 'com.melloware:jintellitype:1.3.9:dll-x64' \r\n"
			+ "	 testImplementation 'org.apache.commons:commons-collections4:{commons-collections4_version}'\r\n"
			+ "	 testImplementation 'org.javolution:javolution-core-java:{javolution-core-java_version}'\r\n"
			+ "	 testImplementation 'it.unimi.dsi:fastutil:{fastutil_version}'\r\n"
			+ "	\r\n"
			+ "	 testImplementation 'org.slf4j:slf4j-api:{slf4j-api_version}'\r\n"
			+ "  testImplementation 'ch.qos.logback:logback-classic:{logback-classic_version}'\r\n"
			+ "  testImplementation 'ch.qos.logback:logback-core:{logback-core_version}'\r\n"
			+ "	 testImplementation 'org.apache.commons:commons-lang3:{commons-lang3_version}'\r\n"
			+ "	 testImplementation 'de.schlichtherle.truezip:truezip-file:{truezip-file_version}'\r\n"
			+ "	 testImplementation 'de.schlichtherle.truezip:truezip-path:{truezip-path_version}'\r\n"
			+ "	 testImplementation 'de.schlichtherle.truezip:truezip-driver-zip:{truezip-driver-zip_version}'\r\n"
			+ "	 testImplementation 'org.javassist:javassist:{javassist_version}'\r\n"
			+ "	 testImplementation 'org.jdom:jdom2:{jdom2_version}' \r\n"
			+ "	 testImplementation 'org.openjdk.jmh:jmh-core:{jmh-core_version}'\r\n"
			+ "}\r\n"
			+ "\r\n"
			+ "	shadowJar {{\r\n"
			+ "   mergeServiceFiles()\r\n"
			+ "	}\r\n"
			+ "\r\n"
			+ "	task shadowTestJar(type: ShadowJar) {{\r\n"
			+ "   mergeServiceFiles()\r\n"
			+ " \r\n"
			+ "	  exclude('META-INF/INDEX.LIST', 'META-INF/*.SF', 'META-INF/*.DSA', 'META-INF/*.RSA')\r\n"
			+ "\r\n"
			+ "  archiveClassifier.set(\"test-all\")\r\n"
			+ "  from sourceSets.main.output, sourceSets.test.output\r\n"
			+ "  configurations = [project.configurations.testRuntimeClasspath]\r\n"
			+ "	}\r\n";
		// @formatter:on

}
