/*
 * Copyright 2010 by Thomas Mauch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */
package org.magicwerk.brownies.collections.helper;

import java.util.Arrays;

/**
 * The class ArraysHelper adds method for handling boolean arrays which are missing in java.util.Arrays, e.g. sort() and binarySearch().
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class ArraysHelper {

	public static int compare(int val1, int val2) {
		if (val1 < val2)
			return -1;
		else if (val1 > val2)
			return 1;
		else
			return 0;
	}

	public static int compare(long val1, long val2) {
		if (val1 < val2)
			return -1;
		else if (val1 > val2)
			return 1;
		else
			return 0;
	}

	public static int compare(double val1, double val2) {
		if (val1 < val2)
			return -1;
		else if (val1 > val2)
			return 1;
		else
			return 0;
	}

	public static int compare(float val1, float val2) {
		if (val1 < val2)
			return -1;
		else if (val1 > val2)
			return 1;
		else
			return 0;
	}

	public static int compare(boolean val1, boolean val2) {
		return (val1 == val2) ? 0 : ((val1) ? 1 : -1);
	}

	// --- Sort

	// We have to provide these delegate functions, as there is no method Array.sort(boolean[])

	public static void sort(int[] values, int fromIndex, int toIndex) {
		Arrays.sort(values, fromIndex, toIndex);
	}

	public static void sort(long[] values, int fromIndex, int toIndex) {
		Arrays.sort(values, fromIndex, toIndex);
	}

	public static void sort(double[] values, int fromIndex, int toIndex) {
		Arrays.sort(values, fromIndex, toIndex);
	}

	public static void sort(float[] values, int fromIndex, int toIndex) {
		Arrays.sort(values, fromIndex, toIndex);
	}

	public static void sort(boolean[] values, int fromIndex, int toIndex) {
		boolean reorder = false;
		int numFalse = 0;
		int numTrue = 0;
		for (int i = fromIndex; i < toIndex; i++) {
			if (values[i]) {
				numTrue++;
			} else {
				numFalse++;
				if (numTrue > 0) {
					reorder = true;
				}
			}
		}
		if (reorder) {
			for (int i = 0; i < numFalse; i++) {
				values[fromIndex + i] = false;
			}
			for (int i = 0; i < numTrue; i++) {
				values[fromIndex + numFalse + i] = true;
			}
		}
	}

	public static void sort(byte[] values, int fromIndex, int toIndex) {
		Arrays.sort(values, fromIndex, toIndex);
	}

	public static void sort(char[] values, int fromIndex, int toIndex) {
		Arrays.sort(values, fromIndex, toIndex);
	}

	public static void sort(short[] values, int fromIndex, int toIndex) {
		Arrays.sort(values, fromIndex, toIndex);
	}

	// --- Binary search

	// We have to provide these delegate functions, as there is no method Array.binarySearch(boolean[], boolean)

	public static int binarySearch(int[] values, int fromIndex, int toIndex, int key) {
		return Arrays.binarySearch(values, fromIndex, toIndex, key);
	}

	public static int binarySearch(long[] values, int fromIndex, int toIndex, long key) {
		return Arrays.binarySearch(values, fromIndex, toIndex, key);
	}

	public static int binarySearch(double[] values, int fromIndex, int toIndex, double key) {
		return Arrays.binarySearch(values, fromIndex, toIndex, key);
	}

	public static int binarySearch(float[] values, int fromIndex, int toIndex, float key) {
		return Arrays.binarySearch(values, fromIndex, toIndex, key);
	}

	public static int binarySearch(boolean[] values, int fromIndex, int toIndex, boolean key) {
		if (key) {
			if (values[toIndex - 1] == true) {
				return toIndex - 1;
			} else {
				return -toIndex - 1;
			}
		} else {
			if (values[fromIndex] == false) {
				return fromIndex;
			} else {
				return -fromIndex - 1;

			}
		}
	}

	public static int binarySearch(byte[] values, int fromIndex, int toIndex, byte key) {
		return Arrays.binarySearch(values, fromIndex, toIndex, key);
	}

	public static int binarySearch(char[] values, int fromIndex, int toIndex, char key) {
		return Arrays.binarySearch(values, fromIndex, toIndex, key);
	}

	public static int binarySearch(short[] values, int fromIndex, int toIndex, short key) {
		return Arrays.binarySearch(values, fromIndex, toIndex, key);
	}

}
