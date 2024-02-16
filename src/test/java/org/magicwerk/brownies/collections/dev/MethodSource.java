package org.magicwerk.brownies.collections.dev;

import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.objects.Single;
import org.magicwerk.brownies.core.strings.StringPrinter;
import org.magicwerk.brownies.javassist.sources.JavaParserReader;

import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

public class MethodSource {

	static final JavaParserReader javaParserReader = new JavaParserReader();

	public static MethodSource parseMethod(String src) {
		Single<MethodSource> method = new Single<>();
		Single<Boolean> visiting = new Single<>(false);
		BodyDeclaration<?> md = javaParserReader.parseBodyDeclaration(src);
		RefactorVisitor visitor = new RefactorVisitor() {
			@Override
			public void visit(final MethodDeclaration md, final Void arg) {
				if (!visiting.get()) {
					visiting.set(true);
					method.set(getMethod(md));
					visiting.set(false);
				} else {
					super.visit(md, arg);
				}
			}

			@Override
			public void visit(final ConstructorDeclaration cd, final Void arg) {
				if (!visiting.get()) {
					visiting.set(true);
					method.set(getMethod(cd));
					visiting.set(false);
				} else {
					super.visit(cd, arg);
				}
			}
		};
		md.accept(visitor, null);
		return method.get();
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
		return (ConstructorDeclaration) CheckTools.checkValid(bodyDecl, bodyDecl instanceof ConstructorDeclaration, "Not a constructor");
	}

	public MethodDeclaration getMethodDecl() {
		return (MethodDeclaration) CheckTools.checkValid(bodyDecl, bodyDecl instanceof MethodDeclaration, "Not a method");
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
		return new StringPrinter().addAllIf(doc, header, body).toString();
	}

	public void setSourceDoc(String source) {
		MethodSource method = parseMethod(source);
		name = method.getName();
		doc = method.getDoc();
		header = method.getHeader();
		body = method.getBody();
	}

	public String getSource() {
		return new StringPrinter().addAllIf(header, body).toString();
	}

	public void setSource(String source) {
		MethodSource method = parseMethod(source);
		name = method.getName();
		header = method.getHeader();
		body = method.getBody();
	}

}
