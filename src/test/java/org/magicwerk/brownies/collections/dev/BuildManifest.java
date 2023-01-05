package org.magicwerk.brownies.collections.dev;

import java.util.jar.Attributes;
import java.util.regex.Pattern;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.collections.MapTree;
import org.magicwerk.brownies.core.collections.TreeTools;
import org.magicwerk.brownies.core.exec.Exec;
import org.magicwerk.brownies.core.exec.Exec.ExecStatus;
import org.magicwerk.brownies.core.files.FileInfo;
import org.magicwerk.brownies.core.files.FileInfo.FileType;
import org.magicwerk.brownies.core.files.FilePath;
import org.magicwerk.brownies.core.files.FileTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ClassTools;
import org.magicwerk.brownies.core.reflect.JavaBuildConst;
import org.magicwerk.brownies.core.regex.RegexTools;
import org.magicwerk.brownies.core.strings.StringPrinter;
import org.magicwerk.brownies.core.types.ManifestValues;

import ch.qos.logback.classic.Logger;

/**
 * Create file META-INF/MANIFEST.MF.
 */
public class BuildManifest {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	// Manifest file
	static final String MANIFEST_MF = "MANIFEST.MF";
	static final String META_INF = "META-INF";

	// Java Module info
	static final String Automatic_Module_Name = "Automatic-Module-Name";

	// JAR info
	static final String Specification_Title = Attributes.Name.SPECIFICATION_TITLE.toString();
	static final String Specification_Version = Attributes.Name.SPECIFICATION_TITLE.toString();
	static final String Specification_Vendor = Attributes.Name.SPECIFICATION_TITLE.toString();
	static final String Implementation_Title = Attributes.Name.SPECIFICATION_TITLE.toString();
	static final String Implementation_Version = Attributes.Name.SPECIFICATION_TITLE.toString();
	static final String Implementation_Vendor = Attributes.Name.SPECIFICATION_TITLE.toString();

	// OSGI Bundle info
	static final String Bundle_ManifestVersion = "Bundle-ManifestVersion";
	static final String Bundle_SymbolicName = "Bundle-SymbolicName";
	static final String Bundle_Name = "Bundle-Name";
	static final String Bundle_Version = "Bundle-Version";
	static final String Bundle_Description = "Bundle-Description";
	static final String Bundle_License = "Bundle-License";
	static final String Bundle_DocURL = "Bundle-DocURL";
	static final String Export_Package = "Export-Package";

	public static void main(String[] args) {
		new BuildManifest().run();
	}

	void run() {
		// Fields which match content in build.gradle / pom.xml
		String version = getMavenVersion();
		//LOG.info("mavenVersion: {}", version);

		// Additional data for MANIFEST.FM
		String url = "http://www.magicwerk.org/collections";
		String description = "Brownies Collections contains high-performance collections complementing the Java Collections Framework.";
		String bundleManifestVersion = "2";
		String name = "Brownies-Collections";
		String pkgName = "org.magicwerk.brownies.collections";
		String vendor = "magicwerk.org";
		String license = "https://www.apache.org/licenses/LICENSE-2.0.txt";

		FilePath dir = FilePath.of(JavaBuildConst.JAVA_SOURCE_DIR);
		IList<String> pkgs = getNonEmptyPackages(dir);
		String exportPackage = getExportPackage(pkgs, version);
		ManifestValues mv = new ManifestValues();

		mv.write(Automatic_Module_Name, pkgName);

		mv.write(Specification_Title, name);
		mv.write(Specification_Version, version);
		mv.write(Specification_Vendor, vendor);
		mv.write(Implementation_Title, name);
		mv.write(Implementation_Version, version);
		mv.write(Implementation_Vendor, vendor);

		mv.write(Bundle_ManifestVersion, bundleManifestVersion);
		mv.write(Bundle_SymbolicName, pkgName);
		mv.write(Bundle_Name, name);
		mv.write(Bundle_Version, version);
		mv.write(Bundle_Description, description);
		mv.write(Bundle_License, license);
		mv.write(Bundle_DocURL, url);

		mv.write(Export_Package, exportPackage);
		String text = mv.write();

		FilePath file = FilePath.of(JavaBuildConst.JAVA_RESOURCE_DIR).get(META_INF).get(MANIFEST_MF);
		FileTools.writeFile().setFile(file).setText(text).write();
	}

	static final Pattern MAVEN_REFERENCE_PATTERN = Pattern.compile("(?m)(?<=^mavenReference: )(.*)(?=\\R)");
	static final Pattern MAVEN_VERSION_PATTERN = Pattern.compile(".*:(.*)");

	String getMavenVersion() {
		String cmd = "gradle printMavenReference";
		ExecStatus status = new Exec().setUseShell(true).setPrintOutput(true).setArgLine(cmd).setThrowOnError(true).execute();
		String out = status.getMessage();
		String ref = RegexTools.get(MAVEN_REFERENCE_PATTERN, out);
		return RegexTools.get(MAVEN_VERSION_PATTERN, ref);
	}

	String getExportPackage(IList<String> pkgs, String version) {
		String pkgVer = ";version=\"" + version + "\"";
		StringPrinter buf = new StringPrinter().setElemMarker(",");
		for (String pkg : pkgs) {
			buf.add(pkg + pkgVer);
		}
		return buf.toString();
	}

	IList<String> getNonEmptyPackages(FilePath dir) {
		MapTree<String, FileInfo> tree = FileTools.listFiles().setDir(dir).setRecursive(true).setEnterFilter(fi -> !fi.getName().equals(META_INF)).listTree();
		IList<String> pkgs = GapList.create();
		TreeTools.traverse(tree, n -> {
			FileInfo fi = n.getValue();
			if (fi.getType() != FileType.DIRECTORY) {
				return;
			}

			if (n.getChildValues().containsIf(f -> f.getType() == FileType.FILE)) {
				FilePath fp = fi.getPath();
				fp = dir.relativize(fp);
				String pkg = ClassTools.getClassFromPath(fp.getPath());
				pkgs.add(pkg);
			}
		});
		return pkgs;
	}

}
