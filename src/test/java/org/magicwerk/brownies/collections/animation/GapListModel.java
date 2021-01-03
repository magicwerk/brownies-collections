package org.magicwerk.brownies.collections.animation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.core.strings.StringFormatter;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.core.ArrayTools;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.CollectionTools;
import org.magicwerk.brownies.core.ObjectTools;

public class GapListModel {

	public static int getGapListCapacity(GapList<?> list) {
		return list.capacity();
	}

	public static <T> T[] getGapListValues(GapList<T> list) {
		Object values = ReflectTools.getAnyFieldValue(list, "values");
		return (T[]) values;
	}

	static <T> int indexOf(T[] values, T value) {
		for (int i=0; i<values.length; i++) {
			if (ObjectTools.equals(values[i], value)) {
				return i;
			}
		}
		return -1;
	}

	public static int getGapListSlotStart(GapList<?> list) {
		return (int) ReflectTools.getAnyFieldValue(list, "start");
	}

	public static int getGapListSlotEnd(GapList<?> list) {
		return (int) ReflectTools.getAnyFieldValue(list, "end");
	}

	public static int getGapListGapStart(GapList<?> list) {
		return (int) ReflectTools.getAnyFieldValue(list, "gapStart");
	}

	public static int getGapListGapSize(GapList<?> list) {
		return (int) ReflectTools.getAnyFieldValue(list, "gapSize");
	}

}