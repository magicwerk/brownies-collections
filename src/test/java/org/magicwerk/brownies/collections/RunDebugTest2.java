package org.magicwerk.brownies.collections;

import org.magicwerk.brownies.core.reflect.ReflectTools;


/**
 * Run test with DEBUG_CHECK.
 * This test does not end with "Test" but with "Test2" so Maven will not recognize it.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class RunDebugTest2 {

	public static void main(String[] args) {
		doTestDebug();
	}

    /**
     * This test will fail if GapList contains DEBUG_CHECK = true
     * and assertions are enabled.
     */
    @org.junit.Test
    public void testDebug() {
    	doTestDebug();
    }

    static void doTestDebug() {
    	GapList<String> list = GapList.create();
    	list.add("b");
    	ReflectTools.setAnyBeanValue(list, "size", 2);
    	list.add("c");
    }

}
