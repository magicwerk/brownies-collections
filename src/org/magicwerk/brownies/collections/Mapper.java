package org.magicwerk.brownies.collections;

/**
 * Interface to determine a map value out of a given value.
 *
 * @author Thomas Mauch
 * @version $Id$
 * 
 * @param <E> type of elements stored in the list
 * @param <K> type of key
 */
public interface Mapper<E, K> {
    /**
     * Return key for given value.
     *
     * @param v value to determine key for
     * @return  determined key value
     */
    public K getKey(E v);
}

/*
 Note that Mapper cannot be a nested type of MapList with JDK 6.
 The following error will appear on compiling which seems not yet to be fixed:
 http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6292765
 
An exception has occurred in the compiler (1.6.0_35).
Please file a bug at the Java Developer Connection (http://java.sun.com/webapps/bugreport)
after checking the Bug Parade for duplicates. Include your program and the following diagnostic
in your report. Thank you.

java.lang.NullPointerException
        at com.sun.tools.javac.comp.Check.checkCompatibleConcretes(Check.java:1213)
        at com.sun.tools.javac.comp.Check.checkCompatibleSupertypes(Check.java:1565)
        at com.sun.tools.javac.comp.Attr.attribClassBody(Attr.java:2674)
        at com.sun.tools.javac.comp.Attr.attribClass(Attr.java:2628)
        at com.sun.tools.javac.comp.Attr.attribClass(Attr.java:2564)
        at com.sun.tools.javac.main.JavaCompiler.attribute(JavaCompiler.java:1045)
        at com.sun.tools.javac.main.JavaCompiler.compile2(JavaCompiler.java:768)
        at com.sun.tools.javac.main.JavaCompiler.compile(JavaCompiler.java:730)
        at com.sun.tools.javac.main.Main.compile(Main.java:353)
        at com.sun.tools.javac.main.Main.compile(Main.java:279)
        at com.sun.tools.javac.main.Main.compile(Main.java:270)
        at com.sun.tools.javac.Main.compile(Main.java:69)
        at com.sun.tools.javac.Main.main(Main.java:54)
*/
 