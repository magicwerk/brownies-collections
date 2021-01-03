package org.magicwerk.brownies.collections.ext;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the List interface using a collection of arrays of
 * sizes 1, 2, 3, 4, and so on.  The main advantages of this over an
 * implementation like ArrayList is that there is never more than O(sqrt(size())
 * space being used to store anything other than the List elements themselves.
 * Insertions and removals take O(size() - i) amortized time.
 *
 * This provides a space-efficient implementation of an ArrayList.
 * The total space used beyond what is required to store elements is O(sqrt(n))
 * @author morin
 *
 * @param <T> the type of objects stored in this list
 */
public class RootishArrayStack<T> extends AbstractList<T> {
	/**
	 * The type of objects stored in this list
	 */
	Factory<T> f;

	/*
	 * The blocks that contains the list elements
	 */
	List<T[]> blocks;

	/**
	 * The number of elements in the list
	 */
	int n;

	/**
	 * Convert a list index i into a block number
	 * @param i
	 * @return the index of the block that contains list
	 *         element i
	 */
	 protected static int i2b(int i) {
		double db = (-3.0 + Math.sqrt(9 + 8*i)) / 2.0;
		int b = (int)Math.ceil(db);
		return b;
	}

	protected void grow() {
		blocks.add(f.newArray(blocks.size()+1));
	}

	protected void shrink() {
		int r = blocks.size();
		while (r > 0 && (r-2)*(r-1)/2 >= n) {
			blocks.remove(blocks.size()-1);
			r--;
		}
	}

	public T get(int i) {
		if (i < 0 || i > n - 1) throw new IndexOutOfBoundsException();
		int b = i2b(i);
		int j = i - b*(b+1)/2;
		return blocks.get(b)[j];
	}

	public T set(int i, T x) {
		if (i < 0 || i > n - 1) throw new IndexOutOfBoundsException();
		int b = i2b(i);
		int j = i - b*(b+1)/2;
		T y = blocks.get(b)[j];
		blocks.get(b)[j] = x;
		return y;
	}

	public void add(int i, T x) {
		if (i < 0 || i > n) throw new IndexOutOfBoundsException();
		int r = blocks.size();
		if (r*(r+1)/2 < n + 1) grow();
		n++;
		for (int j = n-1; j > i; j--)
			set(j, get(j-1));
		set(i, x);
	}

	public T remove(int i) {
		if (i < 0 || i > n - 1) throw new IndexOutOfBoundsException();
		T x = get(i);
		for (int j = i; j < n-1; j++)
			set(j, get(j+1));
		n--;
		int r = blocks.size();
		if ((r-2)*(r-1)/2 >= n)	shrink();
		return x;
	}

	public int size() {
		return n;
	}

	public RootishArrayStack(Class<T> t) {
		f = new Factory<T>(t);
		n = 0;
		blocks = new ArrayList<T[]>();
	}

	public void clear() {
		blocks.clear();
		n = 0;
	}
}


/**
 * An ugly little class for allocating objects and arrays of generic
 * type T.  This is needed to work around limitations of Java generics.
 */
class Factory<T> {
	Class<T> t;

	/**
	 * Return the type associated with this factory
	 * @return
	 */
	public Class<T> type() {
		return t;
	}

	/**
	 * Constructor - creates a factory for creating objects and
	 * arrays of type t(=T)
	 * @param t0
	 */
	public Factory(Class<T> t0) {
		t = t0;
	}

	/**
	 * Allocate a new array of objects of type T.
	 * @param n the size of the array to allocate
	 * @return the array allocated
	 */
	@SuppressWarnings({"unchecked"})
	protected T[] newArray(int n) {
		return (T[])Array.newInstance(t, n);
	}

	/**
	 *
	 * @return
	 */
	public T newInstance() {
		T x;
		try {
			x = t.newInstance();
		} catch (Exception e) {
			x = null;
		}
		return x;
	}
}

