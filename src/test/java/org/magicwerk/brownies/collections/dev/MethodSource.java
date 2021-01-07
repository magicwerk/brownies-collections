package org.magicwerk.brownies.collections.dev;

import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.StringTools;

import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.MethodDeclaration;

public class MethodSource {

	public static MethodSource parseMethod(String src) {
		final MethodSource[] method = new MethodSource[1];
		BodyDeclaration md = JavaParserTools.getBodyDeclaration(src);
		RefactorVisitor visitor = new RefactorVisitor() {
			@Override
			public void visit(final MethodDeclaration md, final Object arg) {
				method[0] = getMethod(md);
			}

			@Override
			public void visit(final ConstructorDeclaration cd, final Object arg) {
				method[0] = getMethod(cd);
			}
		};
		md.accept(visitor, null);
		return method[0];
	}

	final BodyDeclaration bodyDecl;
	boolean delete;
	String name;
	String doc;
	String header;
	String body;

	public MethodSource(BodyDeclaration decl, String name, String doc, String header, String body) {
		this.bodyDecl = decl;
		this.name = name;
		this.doc = doc;
		this.header = header;
		this.body = body;
	}

	public ConstructorDeclaration getConstructorDecl() {
		return (ConstructorDeclaration) CheckTools.checkGet(bodyDecl, bodyDecl instanceof ConstructorDeclaration, "Not a constructor");
	}

	public MethodDeclaration getMethodDecl() {
		return (MethodDeclaration) CheckTools.checkGet(bodyDecl, bodyDecl instanceof MethodDeclaration, "Not a method");
	}

	public boolean isDelete() {
		return delete;
	}

	public void delete() {
		delete = true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDoc() {
		return doc;
	}

	public void setDoc(String doc) {
		this.doc = doc;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getSourceDoc() {
		return StringTools.add(doc, header, body);
	}

	public void setSourceDoc(String source) {
		MethodSource method = parseMethod(source);
		name = method.getName();
		doc = method.getDoc();
		header = method.getHeader();
		body = method.getBody();
	}

	public String getSource() {
		return StringTools.add(header, body);
	}

	public void setSource(String source) {
		MethodSource method = parseMethod(source);
		name = method.getName();
		header = method.getHeader();
		body = method.getBody();
	}

}
