package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.list.TreeList;
import org.magicwerk.brownies.collections.ext.CircularArrayList;
import org.magicwerk.brownies.collections.ext.DualArrayDeque;
import org.magicwerk.brownies.collections.ext.DualRootishArrayDeque;
import org.magicwerk.brownies.collections.ext.RootishArrayStack;
import org.magicwerk.brownies.collections.ext.TList;
import org.magicwerk.brownies.collections.primitive.IntBigList;
import org.magicwerk.brownies.collections.primitive.IntGapList;

import javolution.util.FastTable;

/**
 *
 *
 * @author Thomas Mauch
 */
public class TestFactories {

	/**
	 * Interface to create lists.
	 *
	 * @author Thomas Mauch
	 */
	public static abstract class Factory {
		/**
		 * @return type of collection class
		 */
		abstract Class<?> getType();

		public String getName() {
			return getType().getSimpleName();
		}
	}

	public static abstract class PrimitiveFactory extends Factory {
		/**
		 * Create collection with specified number of elements.
		 * The collection will grow as needed.
		 */
		abstract Object create(int size);

		/**
		 * Create collection with specified number of elements.
		 * The collection's capacity will be set accordingly before the elements are added.
		 */
		abstract Object createSize(int size);

		/**
		 * Create collection with specified content.
		 */
		abstract Object copy(Object that);
	}

	/**
	 * A ContentFactory is responsible to fill a collection with test values. 
	 */
	public static abstract class ContentFactory {
		/**
		 * Fill collection with the specified number of values.
		 * 
		 * @param coll	collection to fill
		 * @param size	number of elements to add
		 */
		abstract void fill(Collection<Object> coll, int size);
	}

	static Random random = new Random(0);

	public static class SingletonContentFactory extends ContentFactory {
		@Override
		void fill(Collection<Object> coll, int size) {
			Integer obj = 0;
			for (int i = 0; i < size; i++) {
				coll.add(obj);
			}
		}
	}

	public static class DistinctContentFactory extends ContentFactory {
		@Override
		void fill(Collection<Object> coll, int size) {
			for (int i = 0; i < size; i++) {
				Object obj = i;
				coll.add(obj);
			}
		}
	}

	/**
	 * Fills collection with random values by adding the elements at the end.
	 */
	public static class RandomContentFactory extends ContentFactory {
		@Override
		void fill(Collection<Object> coll, int size) {
			for (int i = 0; i < size; i++) {
				Object obj = random.nextInt(size);
				coll.add(obj);
			}
		}
	}

	/**
	 * Fills collection with random values by adding the elements at random locations.
	 */
	public static class RandomRandomContentFactory extends ContentFactory {
		@Override
		void fill(Collection<Object> coll, int size) {
			List<Object> list = (List<Object>) coll;
			for (int i = 0; i < size; i++) {
				int index = (i == 0) ? 0 : random.nextInt(i);
				Object obj = random.nextInt(size);
				list.add(index, obj);
			}
		}
	}

	/**
	 * Interface to create lists.
	 *
	 * @author Thomas Mauch
	 */
	public static abstract class CollectionFactory extends Factory {
		ContentFactory contentFactory = new SingletonContentFactory();

		public ContentFactory getContentFactory() {
			return contentFactory;
		}

		public void setContenFactory(ContentFactory contentFactory) {
			this.contentFactory = contentFactory;
		}

		Collection<?> create(int size, boolean preSize, ContentFactory contentFactory) {
			Collection<Object> coll;
			if (preSize) {
				coll = (Collection<Object>) createSize(size);
			} else {
				coll = (Collection<Object>) create(size);
			}
			contentFactory.fill(coll, size);
			return coll;
		}

		/**
		 * @return type of collection class
		 */
		@Override
		abstract Class<?> getType();

		/**
		 * Create collection with specified number of elements.
		 * The collection will grow as needed.
		 */
		abstract Collection<?> create(int size);

		/**
		 * Create collection with specified number of elements.
		 * The collection's capacity will be set accordingly before the elements are added.
		 */
		abstract Collection<?> createSize(int size);

		/**
		 * Create collection with specified content.
		 */
		abstract Collection<?> copy(Collection<?> that);
	}

	public static class GapListFactory extends CollectionFactory {
		@Override
		public Class<?> getType() {
			return GapList.class;
		}

		@Override
		public List create(int size) {
			return allocGapList(size);
		}

		@Override
		public List createSize(int size) {
			return allocGapListSize(size);
		}

		@Override
		public List copy(Collection that) {
			return ((GapList) that).copy();
		}

		GapList<Object> allocGapList(int size) {
			GapList<Object> list = new GapList<Object>();
			getContentFactory().fill(list, size);
			return list;
		}

		GapList<Object> allocGapListSize(int size) {
			GapList<Object> list = new GapList<Object>(size);
			getContentFactory().fill(list, size);
			return list;
		}
	}

	public static class IntGapListFactory extends PrimitiveFactory {
		@Override
		public String getName() {
			return "IntGapList";
		}

		@Override
		public Class<?> getType() {
			return int.class;
		}

		@Override
		public IntGapList create(int size) {
			return allocGapList(size);
		}

		@Override
		public IntGapList createSize(int size) {
			return allocGapListSize(size);
		}

		@Override
		public IntGapList copy(Object that) {
			IntGapList list = (IntGapList) that;
			return list.copy();
		}

		public static IntGapList allocGapList(int size) {
			Integer obj = new Integer(0);
			IntGapList l = new IntGapList();
			for (int i = 0; i < size; i++) {
				l.add(i, obj);
			}
			return l;
		}

		public static IntGapList allocGapListSize(int size) {
			Integer obj = new Integer(0);
			IntGapList l = new IntGapList(size);
			for (int i = 0; i < size; i++) {
				l.add(i, obj);
			}
			return l;
		}
	}

	public static class IntBigListFactory extends PrimitiveFactory {
		public static final int BLOCK_SIZE = 1000;

		int blockSize;

		public IntBigListFactory() {
			this.blockSize = BLOCK_SIZE;
		}

		public IntBigListFactory(int blockSize) {
			this.blockSize = blockSize;
		}

		@Override
		public String getName() {
			return "IntBigList " + blockSize;
		}

		@Override
		public Class<?> getType() {
			return int.class;
		}

		@Override
		public IntBigList create(int size) {
			return allocBigList(size);
		}

		@Override
		public IntBigList createSize(int size) {
			return allocBigList(size);
		}

		@Override
		public IntBigList copy(Object that) {
			IntBigList list = (IntBigList) that;
			return list.copy();
		}

		public static IntBigList allocBigList(int size) {
			return allocBigList(size, BLOCK_SIZE);
		}

		public static IntBigList allocBigList(int size, int blockSize) {
			int val = 0;
			IntBigList l = new IntBigList(blockSize);
			for (int i = 0; i < size; i++) {
				l.add(i, val);
			}
			return l;
		}
	}

	public static class TreeListFactory extends CollectionFactory {
		@Override
		public Class<?> getType() {
			return TreeList.class;
		}

		@Override
		public Collection<?> create(int size) {
			return allocTreeList(size);
		}

		@Override
		public Collection<?> createSize(int size) {
			return allocTreeList(size);
		}

		@Override
		public List copy(Collection that) {
			return new TreeList(that);
		}

		public static TreeList<Object> allocTreeList(int size) {
			Integer obj = new Integer(0);
			TreeList<Object> l = new TreeList<Object>();
			for (int i = 0; i < size; i++) {
				l.add(i, obj);
			}
			return l;
		}

	}

	public static class BigListFactory extends CollectionFactory {
		public static final int BLOCK_SIZE = 1000;

		int blockSize;

		public BigListFactory() {
			this.blockSize = BLOCK_SIZE;
		}

		public BigListFactory(int blockSize) {
			this.blockSize = blockSize;
		}

		@Override
		public String getName() {
			return "BigList " + blockSize;
		}

		@Override
		public Class<?> getType() {
			return BigList.class;
		}

		@Override
		public List create(int size) {
			return allocBigList(size, blockSize);
		}

		@Override
		public List createSize(int size) {
			return allocBigList(size);
		}

		@Override
		public List copy(Collection that) {
			return ((BigList) that).copy();
		}

		public static BigList<Object> allocBigList(int size) {
			return allocBigList(size, BLOCK_SIZE);
		}

		public static BigList<Object> allocBigList(int size, int blockSize) {
			Integer obj = new Integer(0);
			BigList<Object> l = new BigList<Object>(blockSize);
			for (int i = 0; i < size; i++) {
				l.add(i, obj);
			}
			return l;
		}
	}

	static class ArrayListFactory extends CollectionFactory {
		@Override
		public Class<?> getType() {
			return ArrayList.class;
		}

		@Override
		public List create(int size) {
			return allocArrayList(size);
		}

		@Override
		public List createSize(int size) {
			return allocArrayListSize(size);
		}

		@Override
		public List copy(Collection that) {
			return (List) ((ArrayList) that).clone();
		}

		public static ArrayList<Object> allocArrayList(int size) {
			Integer obj = new Integer(0);
			ArrayList<Object> l = new ArrayList<Object>();
			for (int i = 0; i < size; i++) {
				l.add(i, obj);
			}
			return l;
		}

		public static ArrayList<Object> allocArrayListSize(int size) {
			Integer obj = new Integer(0);
			ArrayList<Object> l = new ArrayList<Object>(size);
			for (int i = 0; i < size; i++) {
				l.add(i, obj);
			}
			return l;
		}
	}

	static class TListFactory extends CollectionFactory {
		@Override
		public Class<?> getType() {
			return TList.class;
		}

		@Override
		public Collection<?> create(int size) {
			return allocTList(size);
		}

		@Override
		public Collection<?> createSize(int size) {
			return create(size);
		}

		@Override
		public List copy(Collection that) {
			return new TList(that);
		}

		public static TList allocTList(int size) {
			Integer obj = new Integer(0);
			TList list = new TList();
			for (int i = 0; i < size; i++) {
				list.add(obj);
			}
			return list;
		}

	}

	static class RootishArrayStackFactory extends CollectionFactory {
		@Override
		public Class<?> getType() {
			return RootishArrayStack.class;
		}

		@Override
		public Collection<?> create(int size) {
			return allocRootishArrayStack(size);
		}

		@Override
		public Collection<?> createSize(int size) {
			return create(size);
		}

		@Override
		public List copy(Collection that) {
			throw new UnsupportedOperationException();
		}

		public static RootishArrayStack<Object> allocRootishArrayStack(int size) {
			Integer obj = new Integer(0);
			RootishArrayStack<Object> list = new RootishArrayStack<Object>(Object.class);
			for (int i = 0; i < size; i++) {
				list.add(obj);
			}
			return list;
		}

	}

	public static class CircularArrayListFactory extends CollectionFactory {
		@Override
		public Class<?> getType() {
			return CircularArrayList.class;
		}

		@Override
		public List create(int size) {
			return allocCircularArrayList(size);
		}

		@Override
		public List createSize(int size) {
			return allocCircularArrayListSize(size);
		}

		@Override
		public List copy(Collection that) {
			return new CircularArrayList(that);
		}

		static CircularArrayList allocCircularArrayList(int size) {
			Integer obj = new Integer(0);
			CircularArrayList l = new CircularArrayList();
			for (int i = 0; i < size; i++) {
				l.add(i, obj);
			}
			return l;
		}

		static CircularArrayList allocCircularArrayListSize(int size) {
			Integer obj = new Integer(0);
			CircularArrayList l = new CircularArrayList(size);
			for (int i = 0; i < size; i++) {
				l.add(i, obj);
			}
			return l;
		}
	}

	static class LinkedListFactory extends CollectionFactory {
		@Override
		public Class<?> getType() {
			return LinkedList.class;
		}

		@Override
		public Collection<?> create(int size) {
			return allocLinkedList(size);
		}

		@Override
		public Collection<?> createSize(int size) {
			return create(size);
		}

		@Override
		public List copy(Collection that) {
			return (List) ((LinkedList) that).clone();
		}

		public static LinkedList<Object> allocLinkedList(int size) {
			Integer obj = new Integer(0);
			LinkedList<Object> l = new LinkedList<Object>();
			for (int i = 0; i < size; i++) {
				l.add(i, obj);
			}
			return l;
		}
	}

	public static class FastTableFactory extends CollectionFactory {
		@Override
		public Class<?> getType() {
			return FastTable.class;
		}

		@Override
		public List create(int size) {
			Integer obj = new Integer(0);
			FastTable<Object> l = new FastTable<Object>();
			for (int i = 0; i < size; i++) {
				l.add(i, obj);
			}
			return l;
		}

		@Override
		public List createSize(int size) {
			return create(size);
		}

		@Override
		public List copy(Collection that) {
			FastTable ft = new FastTable();
			ft.addAll(that);
			return ft;
		}

		public static FastTable<Object> allocFastTable(int size) {
			Integer obj = new Integer(0);
			FastTable<Object> l = new FastTable<Object>();
			for (int i = 0; i < size; i++) {
				l.add(i, obj);
			}
			return l;
		}
	}

	public static class DualRootishArrayDequeFactory extends CollectionFactory {
		@Override
		public Class<?> getType() {
			return DualRootishArrayDeque.class;
		}

		@Override
		public List create(int size) {
			Integer obj = new Integer(0);
			List l = new DualRootishArrayDeque<Object>(Object.class);
			for (int i = 0; i < size; i++) {
				l.add(i, obj);
			}
			return l;
		}

		@Override
		public List createSize(int size) {
			return create(size);
		}

		@Override
		public List copy(Collection that) {
			throw new UnsupportedOperationException();
		}

	}

	public static class DualArrayDequeFactory extends CollectionFactory {
		@Override
		public Class<?> getType() {
			return DualArrayDeque.class;
		}

		@Override
		public List create(int size) {
			Integer obj = new Integer(0);
			List l = new DualArrayDeque<Object>(Object.class);
			for (int i = 0; i < size; i++) {
				l.add(i, obj);
			}
			return l;
		}

		@Override
		public List createSize(int size) {
			return create(size);
		}

		@Override
		public List copy(Collection that) {
			throw new UnsupportedOperationException();
		}
	}

}
