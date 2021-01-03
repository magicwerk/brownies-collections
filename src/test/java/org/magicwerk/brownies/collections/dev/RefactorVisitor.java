package org.magicwerk.brownies.collections.dev;

import java.util.Iterator;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.core.regex.RegexTools;

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.DumpVisitor;

public class RefactorVisitor extends DumpVisitor {

	public static class RefactorMethod {

		/**
		 * @param method
		 * @return			true if refactoring should be done
		 */
		public boolean match(MethodSource method) {
			return false;
		}

		/**
		 * @param md
		 * @return		true if processing should continue, false to stop
		 */
		public boolean refactor(MethodSource method) {
			return true;
		}
	}

	private GapList<String> removeTypes = GapList.create();
	private GapList<String> removeMethods = GapList.create();
	private GapList<RefactorMethod> refactorMethods = GapList.create();

	public RefactorVisitor addRemoveTypes(String... types) {
		removeTypes.addArray(types);
		return this;
	}

	public RefactorVisitor addRemoveMethods(String... methods) {
		removeMethods.addArray(methods);
		return this;
	}

	public RefactorVisitor addRefactorMethods(RefactorMethod... methods) {
		refactorMethods.addArray(methods);
		return this;
	}

	@Override
	public void visit(final ClassOrInterfaceDeclaration n, final Object arg) {
		if (removeTypes.contains(n.getName())) {
			return;
		}
		//SourcePrinter oldPrinter = printer;
		//printer = new SourcePrinter();
		super.visit(n, arg);
		//String src = printer.getSource();
		//System.out.println("TYPE: " + src);
		//oldPrinter.print(src);
		//printer = oldPrinter;
	}

	@Override
	public void visit(final ConstructorDeclaration n, final Object arg) {
		if (removeMethods.contains(n.getName())) {
			return;
		}

		MethodSource method = getMethod(n);
		for (RefactorMethod rm : refactorMethods) {
			if (rm.match(method)) {
				if (!rm.refactor(method)) {
					break;
				}
			}
		}
		if (!method.isDelete()) {
			oldPrinter.print(method.getSourceDoc());
		}
		printer = oldPrinter;
	}

	@Override
	public void visit(final MethodDeclaration n, final Object arg) {
		if (removeMethods.contains(n.getName())) {
			return;
		}

		MethodSource method = getMethod(n);
		for (RefactorMethod rm : refactorMethods) {
			if (rm.match(method)) {
				if (!rm.refactor(method)) {
					break;
				}
			}
		}
		if (!method.isDelete()) {
			oldPrinter.print(method.getSourceDoc());
		}
		printer = oldPrinter;
	}

	MethodSource getMethod(ConstructorDeclaration n) {
		beginPrint();
		printDoc(n, null);
		String doc = endPrint();

		beginPrint();
		printHeader(n, null);
		String header = endPrint();

		beginPrint();
		printBody(n, null);
		String body = endPrint();

		MethodSource method = new MethodSource(n, n.getName(), doc, header, body);
		return method;
	}

	MethodSource getMethod(MethodDeclaration n) {
		beginPrint();
		printDoc(n, null);
		String doc = endPrint();

		beginPrint();
		printHeader(n, null);
		String header = endPrint();

		beginPrint();
		printBody(n, null);
		String body = endPrint();

		MethodSource method = new MethodSource(n, n.getName(), doc, header, body);
		return method;
	}

	SourcePrinter oldPrinter;

	void beginPrint() {
		oldPrinter = printer;
		printer = new SourcePrinter();
	}

	String endPrint() {
		String src = printer.getSource();
		printer = oldPrinter;
		return src;
	}

	protected String changeMethodSrc(String name, String src) {
		return src;
	}

	public static String replaceBody(String src, String body) {
		String header = RegexTools.get("(?s)^(.*?)\\{.*\\}", src);
		return header + " {\n" + body + "}\n";
	}

	// Source for these methods has been taken from
	// public void visit(final MethodDeclaration n, final Object arg)

	void printDoc(final MethodDeclaration n, final Object arg) {
		printJavaComment(n.getComment(), arg);
		printJavadoc(n.getJavaDoc(), arg);
	}

	void printHeader(final MethodDeclaration n, final Object arg) {
		printMemberAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers());

		printTypeParameters(n.getTypeParameters(), arg);
		if (n.getTypeParameters() != null) {
			printer.print(" ");
		}

		n.getType().accept(this, arg);
		printer.print(" ");
		printer.print(n.getName());

		printer.print("(");
		if (n.getParameters() != null) {
			for (final Iterator<Parameter> i = n.getParameters().iterator(); i.hasNext();) {
				final Parameter p = i.next();
				p.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}
		printer.print(")");

		for (int i = 0; i < n.getArrayCount(); i++) {
			printer.print("[]");
		}

		if (n.getThrows() != null) {
			printer.print(" throws ");
			for (final Iterator<NameExpr> i = n.getThrows().iterator(); i.hasNext();) {
				final NameExpr name = i.next();
				name.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}
	}

	void printBody(final MethodDeclaration n, final Object arg) {
		if (n.getBody() == null) {
			printer.print(";");
		} else {
			printer.print(" ");
			n.getBody().accept(this, arg);
		}
	}

	// Source for these methods has been taken from
	// public void visit(final ConstructorDeclaration n, final Object arg)

	void printDoc(final ConstructorDeclaration n, final Object arg) {
		printJavaComment(n.getComment(), arg);
		printJavadoc(n.getJavaDoc(), arg);
	}

	void printHeader(final ConstructorDeclaration n, final Object arg) {
		printMemberAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers());

		printTypeParameters(n.getTypeParameters(), arg);
		if (n.getTypeParameters() != null) {
			printer.print(" ");
		}
		printer.print(n.getName());

		printer.print("(");
		if (n.getParameters() != null) {
			for (final Iterator<Parameter> i = n.getParameters().iterator(); i.hasNext();) {
				final Parameter p = i.next();
				p.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}
		printer.print(")");

		if (n.getThrows() != null) {
			printer.print(" throws ");
			for (final Iterator<NameExpr> i = n.getThrows().iterator(); i.hasNext();) {
				final NameExpr name = i.next();
				name.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}
	}

	void printBody(final ConstructorDeclaration n, final Object arg) {
		n.getBlock().accept(this, arg);
	}

}
