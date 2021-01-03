package org.magicwerk.brownies.collections.animation;

import java.util.ArrayList;

import org.magicwerk.brownies.core.ArrayTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;

public class ArrayListModel {

	public static int getArrayListCapacity(ArrayList<?> list) {
		Object array = ReflectTools.getAnyFieldValue(list, "elementData");
		int capacity = ArrayTools.getLength(array);
		return capacity;
	}

}