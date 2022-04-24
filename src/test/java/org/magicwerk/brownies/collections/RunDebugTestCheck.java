package org.magicwerk.brownies.collections;

import org.magicwerk.brownies.core.reflect.ReflectTools;

/**
 * Test for DEBUG_CHECK enabled.
 *
 * @author Thomas Mauch
 */
public class RunDebugTestCheck {

	public static void main(String[] args) {
		new RunDebugTestCheck().run();
	}

	/**
	 * This test will fail if GapList contains DEBUG_CHECK = true and assertions are enabled.
	 */
	void run() {
		GapList<String> list = GapList.create();
		list.add("b");
		ReflectTools.setAnyBeanValue(list, "size", 2);
		list.add("c");
	}

}
