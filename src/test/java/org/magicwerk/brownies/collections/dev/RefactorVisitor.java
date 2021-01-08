package org.magicwerk.brownies.collections.dev;

import java.util.Optional;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.core.regex.RegexTools;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.printer.PrettyPrintVisitor;
import com.github.javaparser.printer.PrettyPrinterConfiguration;

public class RefactorVisitor extends PrettyPrintVisitor {

	public RefactorVisitor() {
		super(new PrettyPrinterConfiguration());
	}

	public static class RefactorMethod {

		/**
		 * @return			true if refactoring should be done
		 */
		public boolean match(MethodSource method) {
			return false;
		}

		/**
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
	public void visit(final ClassOrInterfaceDeclaration n, final Void arg) {
		if (removeTypes.contains(n.getName().getId())) {
			return;
		}
		super.visit(n, arg);
	}

	@Override
	public void visit(final ConstructorDeclaration n, final Void arg) {
		if (removeMethods.contains(n.getName().getId())) {
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
			printer.print(method.getSourceDoc());
		}
	}

	@Override
	public void visit(final MethodDeclaration n, final Void arg) {
		if (removeMethods.contains(n.getName().getId())) {
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
			printer.print(method.getSourceDoc());
		}
	}

	MethodSource getMethod(ConstructorDeclaration n) {
		PrettyPrintVisitor visitor = new RefactorPrintVisitor(true, false);
		printDoc(visitor, n.getComment());
		String doc = visitor.toString();

		visitor = new RefactorPrintVisitor(false, false);
		printHeader(visitor, n);
		String header = visitor.toString();

		visitor = new RefactorPrintVisitor(false, true);
		printBody(visitor, n);
		String body = visitor.toString();

		MethodSource method = new MethodSource(n, n.getName().getId(), doc, header, body);
		return method;
	}

	static class RefactorPrintVisitor extends PrettyPrintVisitor {

		boolean printJavadoc;
		boolean printBody;

		RefactorPrintVisitor(boolean printJavadoc, boolean printBody) {
			super(new PrettyPrinterConfiguration());
			this.printJavadoc = printJavadoc;
			this.printBody = printBody;
		}

		void print(String str) {
			printer.print(str);
		}

		@Override
		public void visit(final BlockStmt n, final Void arg) {
			if (printBody) {
				super.visit(n, arg);
			}
		}

		@Override
		protected void printComment(final Optional<Comment> comment, final Void arg) {
			if (!printJavadoc) {
				printJavadoc = true;
			} else {
				super.printComment(comment, arg);
			}
		}

	}

	MethodSource getMethod(MethodDeclaration n) {
		RefactorPrintVisitor visitor = new RefactorPrintVisitor(true, false);
		printDoc(visitor, n.getComment());
		String doc = visitor.toString();

		visitor = new RefactorPrintVisitor(false, false);
		printHeader(visitor, n);
		String header = visitor.toString();

		visitor = new RefactorPrintVisitor(false, true);
		printBody(visitor, n);
		String body = visitor.toString();

		MethodSource method = new MethodSource(n, n.getName().getId(), doc, header, body);
		return method;
	}

	void printDoc(PrettyPrintVisitor visitor, Optional<Comment> n) {
		n.ifPresent(nn -> nn.accept(visitor, null));
	}

	void printHeader(PrettyPrintVisitor visitor, MethodDeclaration n) {
		Optional<BlockStmt> body = n.getBody();
		n.setBody(new BlockStmt());
		n.accept(visitor, null);
		n.setBody(body.orElse(null));
	}

	void printBody(RefactorPrintVisitor visitor, MethodDeclaration n) {
		if (n.getBody().isPresent()) {
			n.getBody().get().accept(visitor, null);
		} else {
			visitor.print(";");
		}
	}

	void printHeader(PrettyPrintVisitor visitor, ConstructorDeclaration n) {
		BlockStmt body = n.getBody();
		n.setBody(new BlockStmt());
		n.accept(visitor, null);
		n.setBody(body);
	}

	void printBody(PrettyPrintVisitor visitor, ConstructorDeclaration n) {
		n.getBody().accept(visitor, null);
	}

	protected String changeMethodSrc(String name, String src) {
		return src;
	}

	public static String replaceBody(String src, String body) {
		String header = RegexTools.get("(?s)^(.*?)\\{.*\\}", src);
		return header + " {\n" + body + "}\n";
	}

}
