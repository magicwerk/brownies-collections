// ---
// --- DO NOT EDIT
// --- AUTOMATICALLY GENERATED FILE
// ---
/*
 * Copyright 2013 by Thomas Mauch
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
package org.magicwerk.brownies.collections.primitive;

import org.magicwerk.brownies.collections.primitive.DoubleGapList;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.helper.NaturalComparator;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * DoubleObjGapList implements the List interface and uses an instance
 * of DoubleGapList for storage. It therefore allows to use the advantages
 * of primitive collections like saved memory and improved execution
 * speed when standard list collections are expected.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * </p>
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @see	    org.magicwerk.brownies.collections.GapList
 * @see	    org.magicwerk.brownies.collections.primitive.DoubleGapList
 */
public class DoubleObjGapList extends IList<Double> {

	DoubleGapList list;

	static Double[] toWrapper(double[] elems) {
		Double[] e = new Double[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static double[] toPrimitive(Double[] elems) {
		double[] e = new double[elems.length];
		for (int i = 0; i < e.length; i++) {
			e[i] = elems[i];
		}
		return e;
	}

	static double[] toPrimitive(GapList<? extends Double> list2) {
		double[] e = new double[list2.size()];
		for (int i = 0; i < e.length; i++) {
			e[i] = list2.get(i);
		}
		return e;
	}

	static double[] toPrimitive(Collection<? extends Double> list) {
		double[] e = new double[list.size()];
		Iterator<? extends Double> iter = list.iterator();
		for (int i = 0; i < e.length; i++) {
			e[i] = iter.next();
		}
		return e;
	}

	public static DoubleObjGapList create() {
		return new DoubleObjGapList();
	}

	public static DoubleObjGapList create(Double... elems) {
		DoubleObjGapList list = new DoubleObjGapList();
		list.init(elems);
		return list;
	}

	public static DoubleObjGapList create(Collection<? extends Double> elems) {
		return new DoubleObjGapList(elems);
	}

	public DoubleObjGapList() {
		init();
	}

	public void init() {
		list = new DoubleGapList();
	}

	public DoubleObjGapList(int capacity) {
		list = new DoubleGapList(capacity);
	}

	public void init(Double... elems) {
		list = DoubleGapList.create(toPrimitive(elems));
	}

	public DoubleObjGapList(Collection<? extends Double> elems) {
		init(elems);
	}

	public void init(Collection<? extends Double> elems) {
		list = DoubleGapList.create(toPrimitive(elems));
	}

	@Override
	protected void doClone(IList<Double> that) {
		list = (DoubleGapList) ((DoubleObjGapList)that).list.clone();
	}

	@Override
	protected void doAssign(IList<Double> that) {
		DoubleObjGapList list = (DoubleObjGapList) that;
        this.list = list.list;
	}

	@Override
	public DoubleObjGapList copy() {
		return (DoubleObjGapList) clone();
	}

	@Override
	public Double getDefaultElem() {
		return list.getDefaultElem();
	}

    @Override
    public IList<Double> doCreate(int capacity) {
    	if (capacity == -1) {
    		capacity = GapList.DEFAULT_CAPACITY;
    	}
    	return new DoubleObjGapList(capacity);
    }

	@Override
	public int size() {
		return list.size();
	}

	/**
	 * Returns capacity of this GapList.
	 * Note that two GapLists are considered equal even if they have a distinct capacity.
	 * Also the capacity can be changed by operations like clone() etc.
	 *
	 * @return capacity of this GapList
	 */
	public int capacity() {
		return list.capacity();
	}

	@Override
	public Double get(int index) {
		return list.get(index);
	}

	@Override
	protected Double doGet(int index) {
		return list.doGet(index);
	}

	@Override
	protected void doGetAll(Object[] elems, int index, int len) {
		list.doGetAll(toPrimitive((Double[]) elems), index, len);
	}

	@Override
	protected boolean doAdd(int index, Double elem) {
		return list.doAdd(index, elem);
	}

	@Override
	protected boolean doAddAll(int index, Double[] elem) {
		return list.doAddAll(index, toPrimitive(elem));
	}

	@Override
	protected Double doSet(int index, Double elem) {
		return list.doSet(index, elem);
	}

	@Override
	protected void doSetAll(int index, Double[] elem) {
		list.doSetAll(index, toPrimitive(elem));
	}

	@Override
	protected Double doRemove(int index) {
		return list.doRemove(index);
	}

	@Override
	protected void doRemoveAll(int index, int len) {
		list.doRemoveAll(index, len);
	}

	@Override
	protected Double doReSet(int index, Double elem) {
		return list.doReSet(index, elem);
	}

	@Override
    public void move(int srcIndex, int dstIndex, int len) {
    	// Use correct default value
    	list.move(srcIndex, dstIndex, len);
    }

	@Override
	protected void doEnsureCapacity(int minCapacity) {
		list.doEnsureCapacity(minCapacity);
	}

	@Override
	public void trimToSize() {
		list.trimToSize();
	}

	@Override
	public boolean equals(Object obj) {
		return list.equals(obj);
	}

	@Override
	public int hashCode() {
		return list.hashCode();
	}

	@Override
	public String toString() {
		return list.toString();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public int indexOf(Object elem) {
		if (elem == null || elem.getClass() != Double.class) {
			return -1;
		}
		return list.indexOf((Double) elem);
	}

	@Override
	public int lastIndexOf(Object elem) {
		if (elem == null || elem.getClass() != Double.class) {
			return -1;
		}
		return list.lastIndexOf((Double) elem);
	}

	@Override
	public boolean remove(Object elem) {
		if (elem == null || elem.getClass() != Double.class) {
			return false;
		}
		int index = list.indexOf((Double) elem);
		if (index == -1) {
			return false;
		}
		list.remove(index);
		return true;
	}

	@Override
	public boolean contains(Object elem) {
		if (elem == null || elem.getClass() != Double.class) {
			return false;
		}
		return list.contains((Double) elem);
	}

	@Override
	public boolean containsAny(Collection<?> coll) {
		return list.containsAny((Collection<Double>) coll);
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
		return list.containsAll((Collection<Double>) coll);
	}

	@Override
	public boolean removeAll(Collection<?> coll) {
		return list.removeAll((Collection<Double>) coll);
	}

	@Override
	public boolean removeAll(IList<?> coll) {
		return list.removeAll((Collection<Double>) coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll((Collection<Double>) coll);
	}

	@Override
	public boolean retainAll(IList<?> coll) {
		return list.retainAll((Collection<Double>) coll);
	}

	@Override
	public Object[] toArray() {
		double[] elems = list.toArray();
		return toWrapper(elems);
	}

	@Override
	public Object[] toArray(int index, int len) {
		double[] elems = list.toArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public <T> T[] toArray(T[] array) {
		int size = list.size();
		if (array.length < size) {
			array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
		}
		for (int i = 0; i < size; i++) {
			array[i] = (T) (Double) list.get(i);
		}
		if (array.length > size) {
			array[size] = null;
		}
		return array;
	}

	@Override
	public boolean addAll(Collection<? extends Double> coll) {
		return list.addAll((Collection<Double>) coll);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Double> coll) {
		return list.addAll(index, (Collection<Double>) coll);
	}

	@Override
	public boolean addAll(Double... elems) {
		double[] e = toPrimitive(elems);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, Double... elems) {
		double[] e = toPrimitive(elems);
		return list.addAll(index, e);
	}

	@Override
	public boolean addAll(IList<? extends Double> list2) {
		double[] e = toPrimitive(list2);
		return list.addAll(e);
	}

	@Override
	public boolean addAll(int index, IList<? extends Double> list2) {
		double[] e = toPrimitive(list2);
		return list.addAll(index, e);
	}

	@Override
	public boolean removeFirstOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Double.class) {
			return false;
		}
		return list.removeFirstOccurrence((Double) elem);
	}

	@Override
	public boolean removeLastOccurrence(Object elem) {
		if (elem == null || elem.getClass() != Double.class) {
			return false;
		}
		return list.removeLastOccurrence((Double) elem);
	}

	@Override
	public GapList<Double> getAll(int index, int len) {
		double[] elems = list.getArray(index, len);
		return GapList.create(toWrapper(elems));
	}

	@Override
	public Double[] getArray(int index, int len) {
		double[] elems = list.getArray(index, len);
		return toWrapper(elems);
	}

	@Override
	public void setAll(int index, IList<? extends Double> list2) {
		double[] e = toPrimitive(list2);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Collection<? extends Double> coll) {
		double[] e = toPrimitive(coll);
		list.setAll(index, e);
	}

	@Override
	public void setAll(int index, Double... elems) {
		double[] e = toPrimitive(elems);
		list.setAll(index, e);
	}

	@Override
	public void sort(int index, int len, Comparator comparator) {
		if (comparator != null && comparator != NaturalComparator.INSTANCE()) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		list.sort(index, len);
	}

	@Override
	public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
		if (comparator != null && comparator != NaturalComparator.INSTANCE()) {
			throw new IllegalArgumentException("Only natural comparator (null) allowed");
		}
		return list.binarySearch(index, len, (Double) key);
	}

    public DoubleObjGapList unmodifiableList() {
        return new ImmutableDoubleObjGapList(this);
    }

    /**
     * An immutable version of a GapList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableDoubleObjGapList extends DoubleObjGapList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
        protected ImmutableDoubleObjGapList(DoubleObjGapList that) {
            super(that);
        }

        @Override
        protected boolean doAdd(int index, Double elem) {
        	error();
        	return false;
        }

        @Override
        protected boolean doAddAll(int index, Double[] elems) {
        	error();
        	return false;
        }

        @Override
        protected Double doSet(int index, Double elem) {
        	error();
        	return null;
        }

        @Override
        protected void doSetAll(int index, Double[] elems) {
        	error();
        }

        @Override
        protected Double doReSet(int index, Double elem) {
        	error();
        	return null;
        }

        @Override
        protected Double doRemove(int index) {
        	error();
        	return null;
        }

        @Override
        protected void doRemoveAll(int index, int len) {
        	error();
        }

        @Override
        protected void doModify() {
        	error();
        }

        /**
         * Throw exception if an attempt is made to change an immutable list.
         */
        private void error() {
            throw new UnsupportedOperationException("list is immutable");
        }
    }

}
