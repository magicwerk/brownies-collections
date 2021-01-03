package org.magicwerk.brownies.collections;

import org.magictest.MagicTest;
import org.magictest.ng.MagicTestNG;

/**
 * Test runner.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class Test {

    public static void main(String[] args) {
    	test();
        //testNg();
    	//testMagicTestNg();
    }

    public static void test() {
        new MagicTest().run(new String[] {
              "-run",
              //"-save",
              "-loglevel", "trace",
              //"-method", "org.magicwerk.brownies.collections.GapListTest.testInitMult",
              "-class", "org.magicwerk.brownies.collections.IntGapListTest",
              //"-package", "org.magicwerk.brownies.collections"
        });
    }

    @org.testng.annotations.Test
    static void testNg() {
        String[] args = new String[] {
                //"C:\\Users\\Thomas\\AppData\\Local\\Temp\\testng-eclipse-490876144\\testng-customsuite.xml"
                "-testclass", "org.magicwerk.brownies.collections.BigListTest"
        };
        org.magictest.ng.MagicTestNG.main(args);
        //org.testng.TestNG.main(args);
    }

    static void testSource() {
        new MagicTest().run(new String[] {
                "-source", "org.magicwerk.brownies.collections.GapSet"
        });
    }

    static void testMagicTestNg() {
        String[] args = new String[] {
                //"C:\\Users\\Thomas\\AppData\\Local\\Temp\\testng-eclipse-490876144\\testng-customsuite.xml"
        		//"testng.xml"
        		"-testclass",
               		"org.magicwerk.brownies.collections.KeyListTest"
        };
        MagicTestNG.main(args);
    }

}
