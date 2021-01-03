package japa.parser.ast.visitor;

import japa.parser.ast.BlockComment;
import japa.parser.ast.LineComment;
import japa.parser.ast.body.JavadocComment;

/**
 * Extends the standard DumpVisitor so it can be controlled whether comments are written out or not.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class MyDumpVisitor extends DumpVisitor {

	boolean includeComments = true;

	public MyDumpVisitor setIncludeComments(boolean includeComments) {
		this.includeComments = includeComments;
		return this;
	}

	@Override public void visit(final JavadocComment n, final Object arg) {
		if (!includeComments) {
			return;
		}
		printJavaComment(n.getComment(), arg);
		printer.print("/**");
		printer.print(n.getContent());
		printer.printLn("*/");
	}

	@Override public void visit(final LineComment n, final Object arg) {
		if (!includeComments) {
			return;
		}
		printJavaComment(n.getComment(), arg);
		printer.print("//");
		String tmp = n.getContent();
		tmp = tmp.replace('\r', ' ');
		tmp = tmp.replace('\n', ' ');
		printer.printLn(tmp);
	}

	@Override public void visit(final BlockComment n, final Object arg) {
		if (!includeComments) {
			return;
		}
		printJavaComment(n.getComment(), arg);
		printer.print("/*");
		printer.print(n.getContent());
		printer.printLn("*/");
	}

}
