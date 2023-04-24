package org.magicwerk.brownies.collections;

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
import org.magicwerk.brownies.core.strings.StringPrinter;
import org.magicwerk.brownies.tools.dev.tools.GradleTool;
import org.magicwerk.brownies.tools.dev.tools.JavaOptions;
import org.magicwerk.brownies.tools.dev.tools.JavaTool;

import ch.qos.logback.classic.Logger;

/**
 * Run tests with DEBUG_CHECK enabled.
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
		checkDependencies();
		prepareBuild();
		enableDebugCheck();
		build();
		assertDebugTestFails();
		runTests();
	}

	void checkDependencies() {
		GradleTool gradleTool = new GradleTool();
		IList<String> deps = gradleTool.getDependencies("testCompileClasspath");
		// TODO check dependencies against text in buildGradle
		LOG.info("Check that dependencies are still up-to-date:\n{}", StringPrinter.formatLines(deps));
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
		FileTools.cleanDir().setDir(buildDir).setCreateDirs(true).clean();
		FileTools.copyFile().copyDirIntoDir(FilePath.of("src"), buildDir);
		FileTools.writeFile().setFile(buildDir.get("build.gradle")).setText(buildGradle).write();
		FileTools.writeFile().setFile(buildDir.get("settings.gradle")).setText("").write();
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

	static final String buildGradle = "plugins {\r\n"
			+ "    id 'java-library'\r\n"
			+ "	   id 'com.github.johnrengelman.shadow' version '6.1.0'\r\n"
			+ ""
			+ "}\r\n"
			+ "import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar\r\n"
			+ "\r\n"
			+ "repositories {\r\n"
			+ "	mavenLocal()\r\n"
			+ "	mavenCentral()\r\n"
			+ "}\r\n"
			+ "\r\n"
			+ "// Use 'gradle dependencies --configuration testCompileClasspath' to see dependencies\r\n"
			+ "dependencies {\r\n"
			+ "	testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Core/build/classes/java/main\"))\r\n"
			+ "	testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Javassist/build/classes/java/main\"))\r\n"
			+ "	testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Jdom/build/classes/java/main\"))\r\n"
			+ "	testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Html/build/classes/java/main\"))\r\n"
			+ "	testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Swt/build/classes/java/main\"))\r\n"
			+ "	testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Test/build/classes/java/main\"))\r\n"
			+ "	testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/Brownies-Tools/build/classes/java/main\"))\r\n"
			+ "	testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/MagicTest-NG/build/classes/java/main\"))\r\n"
			+ "	testImplementation(files(\"C:/dev/Java/Sources/magicwerk.origo/Java/Brownies/MagicTest/build/classes/java/main\"))\r\n"
			+ " \r\n"
			+ " testImplementation \"junit:junit:4.8.2\"\r\n"
			+ " testImplementation \"com.google.guava:guava-testlib:31.1-jre\"\r\n"
			+ "	testImplementation \"com.github.javaparser:javaparser-core:3.24.8\"	\r\n"
			+ "	\r\n"
			+ "	testImplementation 'com.melloware:jintellitype:1.3.9:dll-x64' \r\n"
			+ "	testImplementation 'org.apache.commons:commons-collections4:4.0'\r\n"
			+ "	testImplementation 'org.javolution:javolution-core-java:6.0.0'\r\n"
			+ "	testImplementation 'it.unimi.dsi:fastutil:7.0.9'\r\n"
			+ "	\r\n"
			+ "	testImplementation 'org.slf4j:slf4j-api:1.7.36'\r\n"
			+ " testImplementation 'ch.qos.logback:logback-classic:1.2.11'\r\n"
			+ " testImplementation 'ch.qos.logback:logback-core:1.2.11'\r\n"
			+ "	testImplementation 'org.apache.commons:commons-lang3:3.12.0'\r\n"
			+ "	testImplementation 'de.schlichtherle.truezip:truezip-file:7.7.10'\r\n"
			+ "	testImplementation 'de.schlichtherle.truezip:truezip-path:7.7.10'\r\n"
			+ "	testImplementation 'de.schlichtherle.truezip:truezip-driver-zip:7.7.10'\r\n"
			+ "	testImplementation 'org.javassist:javassist:3.29.2-GA'\r\n"
			+ "	testImplementation 'org.jdom:jdom2:2.0.6' \r\n"
			+ "	testImplementation 'org.openjdk.jmh:jmh-core:1.36'\r\n"
			+ "}\r\n"
			+ "\r\n"
			+ "	shadowJar {\r\n"
			+ "   mergeServiceFiles()\r\n"
			+ "	}\r\n"
			+ "\r\n"
			+ "	task shadowTestJar(type: ShadowJar) {\r\n"
			+ "   mergeServiceFiles()\r\n"
			+ " \r\n"
			+ "	  exclude('META-INF/INDEX.LIST', 'META-INF/*.SF', 'META-INF/*.DSA', 'META-INF/*.RSA')\r\n"
			+ "\r\n"
			+ "  archiveClassifier.set(\"test-all\")\r\n"
			+ "  from sourceSets.main.output, sourceSets.test.output\r\n"
			+ "  configurations = [project.configurations.testRuntimeClasspath]\r\n"
			+ "	}\r\n";

}
