//
// This file has been copied from
// http://thomas.baudel.name/Visualisation/VisuTri/inplacestablesort.html
//
package org.magicwerk.brownies.collections.helper.primitive;
import org.magicwerk.brownies.collections.primitive.IIntList;




/**
 * This class implements a stable in-place merge sort.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class IntMergeSort {
	IIntList list;
	

	public static <E> void sort(IIntList list) {
		IntMergeSort sort = new IntMergeSort(list);
		sort.sort();
	}

	public static <E> void sort(IIntList list, int from, int to) {
		IntMergeSort sort = new IntMergeSort(list);
		sort.sort(from, to);
	}

	private IntMergeSort(IIntList list) {
		this.list = list;
		
	}

	private void sort() {
		sort(0, list.size());
	}

	private void sort(int from, int to) {
		if (to - from < 12) {
			insertSort(from, to);
			return;
		}
		int middle = (from + to) / 2;
		sort(from, middle);
		sort(middle, to);
		merge(from, middle, to, middle - from, to - middle);
	}

	private int compare(int idx1, int idx2) {
		int val1 = list.get(idx1);
		int val2 = list.get(idx2);
		return (val1<val2 ? -1 : (val1==val2 ? 0 : 1));
	}

	private void swap(int idx1, int idx2) {
		int val = list.get(idx1);
		list.set(idx1, list.get(idx2));
		list.set(idx2, val);
	}

	private int lower(int from, int to, int val) {
		int len = to - from, half;
		while (len > 0) {
			half = len / 2;
			int mid = from + half;
			if (compare(mid, val) < 0) {
				from = mid + 1;
				len = len - half - 1;
			} else
				len = half;
		}
		return from;
	}

	private int upper(int from, int to, int val) {
		int len = to - from, half;
		while (len > 0) {
			half = len / 2;
			int mid = from + half;
			if (compare(val, mid) < 0)
				len = half;
			else {
				from = mid + 1;
				len = len - half - 1;
			}
		}
		return from;
	}

	private void insertSort(int from, int to) {
		if (to > from + 1) {
			for (int i = from + 1; i < to; i++) {
				for (int j = i; j > from; j--) {
					if (compare(j, j - 1) < 0)
						swap(j, j - 1);
					else
						break;
				}
			}
		}
	}

	private int gcd(int m, int n) {
		while (n != 0) {
			int t = m % n;
			m = n;
			n = t;
		}
		return m;
	}

	private void rotate(int from, int mid, int to) {
		/*
		 * a less sophisticated but costlier version:
		 * reverse(from, mid-1); reverse(mid, to-1); reverse(from, to-1);
		 *
		 * 	private void reverse(int from, int to) {
		 *    while (from < to) {
		 *	    swap(from++, to--);
		 *    }
		 *  }
		 */
		if (from == mid || mid == to) {
			return;
		}
		int n = gcd(to - from, mid - from);
		while (n-- != 0) {
			int val = list.get(from + n);
			int shift = mid - from;
			int p1 = from + n, p2 = from + n + shift;
			while (p2 != from + n) {
				list.set(p1, list.get(p2));
				p1 = p2;
				if (to - p2 > shift) {
					p2 += shift;
				} else {
					p2 = from + (shift - (to - p2));
				}
			}
			list.set(p1, val);
		}
	}

	private void merge(int from, int pivot, int to, int len1, int len2) {
		if (len1 == 0 || len2 == 0) {
			return;
		}
		if (len1 + len2 == 2) {
			if (compare(pivot, from) < 0) {
				swap(pivot, from);
			}
			return;
		}
		int first_cut, second_cut;
		int len11, len22;
		if (len1 > len2) {
			len11 = len1 / 2;
			first_cut = from + len11;
			second_cut = lower(pivot, to, first_cut);
			len22 = second_cut - pivot;
		} else {
			len22 = len2 / 2;
			second_cut = pivot + len22;
			first_cut = upper(from, pivot, second_cut);
			len11 = first_cut - from;
		}
		rotate(first_cut, pivot, second_cut);
		int newMid = first_cut + len22;
		merge(from, first_cut, newMid, len11, len22);
		merge(newMid, second_cut, to, len1 - len11, len2 - len22);
	}

}
