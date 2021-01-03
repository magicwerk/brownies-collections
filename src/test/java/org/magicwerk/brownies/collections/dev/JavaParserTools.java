package org.magicwerk.brownies.collections.dev;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.visitor.MyDumpVisitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.StreamTools;
import org.magicwerk.brownies.core.exceptions.WrapperException;

/**
 *
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class JavaParserTools {

    /**
     * Parse given Java source file and return compilation unit.
     *
     * @param src source code
     * @return created compilation unit
     * @throw RuntimeException if the source file cannot be read or parsed
     */
    public static CompilationUnit getCompilationUnit(String src) {
    	Reader reader = null;
    	try {
            reader = StreamTools.getReader(src);
            CompilationUnit cu = JavaParser.parse(reader);
            return cu;
        } catch (ParseException e) {
			throw new WrapperException(e);
		}
        finally {
            StreamTools.close(reader);
        }
    }

	/**
	 * Parse given Java source file and return compilation unit.
	 *
	 * @param srcFile source file
	 * @return created compilation unit
	 * @throw RuntimeException if the source file cannot be read or parsed
	 */
	public static CompilationUnit readCompilationUnit(String srcFile) {
		FileInputStream in = null;
		try {
			in = new FileInputStream(srcFile);
			CompilationUnit cu = JavaParser.parse(in);
			return cu;
        }
		catch (ParseException e) {
			throw new WrapperException(e);
		}
		catch (IOException e) {
			throw new RuntimeException("Cannot read " + srcFile, e);
		}
		finally {
			StreamTools.close(in);
		}
	}

    public static MethodDeclaration getMethodDeclaration(String src) {
    	return CheckTools.checkTypeOf(getBodyDeclaration(src), MethodDeclaration.class);
    }

    public static BodyDeclaration getBodyDeclaration(String src) {
    	try {
    		// The method parseBodyDeclaration seems not able to parse a method.
    		// So we wrap the method in a class, parse it and extract the method
    		String typeSrc = "class X { " + src + "}";
   			ClassOrInterfaceDeclaration cd = (ClassOrInterfaceDeclaration) JavaParser.parseBodyDeclaration(typeSrc);
    		return cd.getMembers().get(0);
        }
        catch (Exception e) {
           throw new RuntimeException(e);
        }
    }

	/**
	 * @param cu
	 * @param name	use null to get the only or default type
	 * @return
	 */
	public static TypeDeclaration getType(CompilationUnit cu, String name) {
		List<TypeDeclaration> types = cu.getTypes();
		if (types == null) {
			return null;
		}
		if (name == null) {
			if (types.size() == 1) {
				return types.get(0);
			}
		}
		for (TypeDeclaration type: types) {
			if (name == null) {
				if (ModifierSet.isPublic(type.getModifiers())) {
					return type;
				}
			} else {
				if (type.getName().equals(name)) {
					return type;
				}
			}
		}
		return null;
	}

	/**
	 * Return all constructors and methods declared by class or interface.
	 *
	 * @param ctype class or interface type
	 * @return	list with all constructors and methods
	 */
	public static IList<BodyDeclaration> getBehaviors(ClassOrInterfaceDeclaration ctype) {
		IList<BodyDeclaration> behaviors = GapList.create();
		List<BodyDeclaration> members = ctype.getMembers();
		for (BodyDeclaration member: members) {
			if (member instanceof MethodDeclaration || member instanceof ConstructorDeclaration) {
				behaviors.add(member);
			}
		}
		return behaviors;
	}

	/**
	 * Return all methods declared by class or interface.
	 *
	 * @param ctype class or interface type
	 * @return	list with all methods
	 */
	public static IList<MethodDeclaration> getMethods(ClassOrInterfaceDeclaration ctype) {
		IList<MethodDeclaration> methods = GapList.create();
		List<BodyDeclaration> members = ctype.getMembers();
		for (BodyDeclaration member: members) {
			if (member instanceof MethodDeclaration) {
				MethodDeclaration method = (MethodDeclaration) member;
				methods.add(method);
			}
		}
		return methods;
	}

	/**
	 * Return all constructors declared by class.
	 *
	 * @param ctype class type
	 * @return	list with all constructors
	 */
	public static IList<ConstructorDeclaration> getConstructors(ClassOrInterfaceDeclaration ctype) {
		IList<ConstructorDeclaration> constructors = GapList.create();
		List<BodyDeclaration> members = ctype.getMembers();
		for (BodyDeclaration member: members) {
			if (member instanceof ConstructorDeclaration) {
				ConstructorDeclaration constructor = (ConstructorDeclaration) member;
				constructors.add(constructor);
			}
		}
		return constructors;
	}

	/**
	 * Returns the method with the specified name.
	 * If there is none, null is returned. If there is more than one, an exception is thrown.
	 *
	 * @param ctype	class type
	 * @param name	method name
	 * @return		method
	 */
	public static MethodDeclaration getMethod(ClassOrInterfaceDeclaration ctype, String name) {
		return getMethods(ctype, name).getSingleOrEmpty();
	}

	/**
	 * Returns all method with the specified name.
	 *
	 * @param ctype	class type
	 * @param name	method name
	 * @return		method
	 */
	public static IList<MethodDeclaration> getMethods(ClassOrInterfaceDeclaration ctype, String name) {
		IList<MethodDeclaration> methods = GapList.create();
		List<BodyDeclaration> members = ctype.getMembers();
		for (BodyDeclaration member: members) {
			if (member instanceof MethodDeclaration) {
				MethodDeclaration method = (MethodDeclaration) member;
				if (method.getName().equals(name)) {
					methods.add(method);
				}
			}
		}
		return methods;
	}

	public static FieldDeclaration getField(ClassOrInterfaceDeclaration ctype, String name) {
		List<BodyDeclaration> members = ctype.getMembers();
		for (BodyDeclaration member: members) {
			if (member instanceof FieldDeclaration) {
				FieldDeclaration field = (FieldDeclaration) member;
				List<VariableDeclarator> variables = field.getVariables();
				CheckTools.check(variables.size() == 1, "Multiple field declaration not supported (e.g. int a,b)");
				if (variables.get(0).getId().getName().equals(name)) {
					return field;
				}
			}
		}
		return null;
	}


	/**
	 * Return all field declared by class or interface.
	 *
	 * @param ctype class or interface type
	 * @return	list with all fields
	 */
	public static List<FieldDeclaration> getFields(ClassOrInterfaceDeclaration ctype) {
		List<FieldDeclaration> fields = GapList.create();
		List<BodyDeclaration> members = ctype.getMembers();
		for (BodyDeclaration member: members) {
			if (member instanceof FieldDeclaration) {
				FieldDeclaration field = (FieldDeclaration) member;
				fields.add(field);
			}
		}
		return fields;
	}

	public static String getSource(Node md, boolean includeComments) {
		MyDumpVisitor dv = new MyDumpVisitor().setIncludeComments(includeComments);
		md.accept(dv, null);
		return dv.getSource();
	}

}
