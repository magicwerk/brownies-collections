package org.magicwerk.brownies.collections.dev;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.StreamTools;

// http://code.google.com/p/javaparser/
// http://qdox.codehaus.org/

/**
 *
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class MethodSourceTest {

	public static void main(String[] args) {
		test();
	}

	static void test() {
		String src1 =
				"	/** Method */ \n" +
				"	public static void main(String[] args) { \n" +
				"		test(); \n" +
				"	}";
		String src2 =
				"	public static void main(String[] args) { \n" +
				"		test(); \n" +
				"	}";
		MethodSource m = new MethodSource(null, "name", "doc", "header", "body");
		m.setSource(src2);
		System.out.println(m.getSource());
	}
}
