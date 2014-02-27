package org.magicwerk.brownies.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

import org.magicwerk.brownies.collections.helper.InternalSort;
import org.magicwerk.brownies.collections.helper.TreeList;
import org.magicwerk.brownies.collections.helper.TreeList.AVLNode;

/**
 * The first block (GapList) used grows dynamcically, all others
 * are allocated with fixed size. This is necessary to prevent starving
 * because of GC usage.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class BigList<T>
    extends IGapList<T>
    implements List<T>, Deque<T> {

    /**
     * An immutable version of a GapList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableBigList<E> extends BigList<E> {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
        protected ImmutableBigList(BigList<E> that) {
            super(true, that);
        }

        @Override
        protected boolean doAdd(int index, E elem) {
        	error();
        	return false;
        }

        @Override
        protected boolean doAddAll(int index, E[] elems) {
        	error();
        	return false;
        }

        @Override
        protected E doSet(int index, E elem) {
        	error();
        	return null;
        }

        @Override
        protected void doSetAll(int index, E[] elems) {
        	error();
        }

        @Override
        protected E doReSet(int index, E elem) {
        	error();
        	return null;
        }

        @Override
        protected E doRemove(int index) {
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
    };

    /**
	 *
	 */
	@SuppressWarnings("serial")
	static class Block<T> implements Serializable {
		private GapList<T> values;
		private int refCount;

		public Block() {
			values = new GapList<T>();
			refCount = 1;
		}

		public Block(int capacity) {
			values = new GapList<T>(capacity);
			refCount = 1;
		}

		public Block(Block<T> that) {
			values = new GapList<T>(that.values.capacity());
			values.addAll(that.values);
			refCount = 1;
		}

		public boolean isShared() {
			return refCount > 1;
		}

		public void ref() {
			refCount++;
		}

		public void unref() {
			refCount--;
		}

		public int size() {
			return values.size();
		}

		public T get(int index) {
			return values.get(index);
		}

		public void set(int index, T elem) {
			values.set(index, elem);
		}

		public void add(int index, T elem) {
			values.add(index, elem);
		}

		public T remove(int index) {
			return values.remove(index);
		}
	}

	/** Set to true for debugging during developping */
	public static final boolean TRACE = false;
	public static final boolean CHECK = false;
	public static final boolean DUMP = false;

	/** Default block size */
	private static int BLOCK_SIZE = 100*1000;

	/** Number of elements stored at maximum in a block */
	private int blockSize;
	/** Number of elements stored in this BigList */
	private int size;
	/** List with block containing the elements */
	private TreeList<Block<T>> blocks = new TreeList<Block<T>>();
	//private int currBlockIndex;
	/** Index of first element in currBlock for the whole BigList */
	private int currBlockStart;
	/** Index of last element in currBlock for the whole BigList */
	private int currBlockEnd;
	private AVLNode<Block<T>> currNode;
	private Block<T> currBlock;

    /**
     * Constructor used internally, e.g. for ImmutableBigList.
     *
     * @param copy true to copy all instance values from source,
     *             if false nothing is done
     * @param that list to copy
     */
    protected BigList(boolean copy, BigList<T> that) {
        if (copy) {
            this.blocks = that.blocks;
            this.blockSize = that.blockSize;
            this.size = that.size;
            this.currNode = that.currNode;
            this.currBlock = that.currBlock;
            this.currBlockStart = that.currBlockStart;
            this.currBlockEnd = that.currBlockEnd;
        }
    }

	/**
	 * Create new list with specified elements.
	 *
	 * @param elems 	array with elements
	 * @return 			created list
	 * @param <E> 		type of elements stored in the list
	 */
	public static <E> BigList<E> create(E... elems) {
		// TOOD improve
		BigList<E> list = new BigList<E>();
		for (E elem: elems) {
			list.add(elem);
		}
		return list;
	}

	/**
	 * Default constructor.
	 * The default block size is used.
	 */
	public BigList() {
		this(BLOCK_SIZE);
	}

	/**
	 * Constructor.
	 *
	 * @param blockSize block size
	 */
	public BigList(int blockSize) {
		this.blockSize = blockSize;

		// First block will grow until it reaches blockSize
		blocks = new TreeList<Block<T>>();
//		currBlock = new Block<T>();
	}

    public BigList(BigList<T> that) {
    	initClone(that);
    }

    public BigList(Collection<T> that) {
        blockSize = BLOCK_SIZE;

		blocks = new TreeList<Block<T>>();
		currBlock = new Block<T>();
		blocks.add(0, currBlock);

        for (Object elem: that.toArray()) {
            add((T) elem);
        }
        assert(size() == that.size());
    }

	@Override
	protected void initClone(IGapList<T> that) {
		BigList<T> bigList = (BigList<T>) that;
        blockSize = bigList.blockSize;

        int size = 0;
		blocks = new TreeList<Block<T>>();
		AVLNode<Block<T>> node = bigList.blocks.root.min();
		while (node != null) {
			Block<T> block = node.getValue();
			block.ref();
			blocks.add(size, block);
			size += block.size();
		}
		assert(size == bigList.size);

		currNode = blocks.root;
		currBlock = currNode.getValue();
		currBlockStart = 0;
		currBlockEnd = currBlock.size();
	}

	@Override
	public T getDefaultElem() {
		return null;
	}

    @Override
    protected void finalize() {
    	// TODO
//    	for (Block<T> block: blocks) {
//    		block.unref();
//    	}
    }

    private void check() {
    	assert(currNode.getValue() == currBlock);
    	assert(currNode.relativePosition == currBlockEnd);
    	assert(currBlockStart >= 0 && currBlockEnd <= size && currBlockStart <= currBlockEnd);
    	assert(currBlockEnd - currBlockStart <= blockSize);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	protected T doGet(int index) {
		int pos = getBlockIndex(index);
		return currBlock.get(pos);
	}

	@Override
	protected T doSet(int index, T elem) {
		int pos = getBlockIndexWrite(index);
		T oldElem = currBlock.get(pos);
		currBlock.set(pos, elem);
		return oldElem;
	}

	@Override
	protected T doReSet(int index, T elem) {
		int pos = getBlockIndexWrite(index);
		T oldElem = currBlock.get(pos);
		currBlock.set(pos, elem);
		return oldElem;
	}

	/**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currBlock to remember this last used block.
	 * If the block is shared, a copy is made and used for modifications.
	 *
	 * @param index	list index
	 * @return		relative index within block
	 */
	private int getBlockIndexWrite(int index) {
		int i = getBlockIndex(index);
		if (currBlock.isShared()) {
			currBlock.unref();
			currBlock = new Block<T>(currBlock);
			currNode.setValue(currBlock);
		}
		return i;
	}

	/**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currBlock to remember this last used block.
	 *
	 * @param index	list index (0 <= index <= size())
	 * @return		relative index within block
	 */
	private int getBlockIndex(int index) {
		// Determine block where specified index is located and store it in currBlock
		if (index >= currBlockStart && index < currBlockEnd) {
			// currBlock is already set correctly
			return index - currBlockStart;
		}

        if (index == size) {
    		currNode = blocks.root.max();
    		currBlock = currNode.getValue();
            currBlockEnd = size;
            currBlockStart = size - currBlock.size();
            return index - currBlockStart;

        } else if (index == 0) {
        	currNode = blocks.root.min();
    		currBlock = currNode.getValue();
            currBlockEnd = currBlock.size();
            currBlockStart = 0;
            return 0;
        }

        int[] endIndex = new int[1];
        currNode = blocks.getIn(index, endIndex);
        currBlock = currNode.getValue();
        currBlockEnd = endIndex[0];
        currBlockStart = currBlockEnd - currBlock.size();
        return index - currBlockStart;
	}

	@Override
	protected boolean doAdd(int index, T element) {
		if (index == -1) {
			index = size;
		}
		int pos = getBlockIndexWrite(index);

		// Insert in current block
		if (currBlock.size() < blockSize) {
			currNode.relativePosition++;
			currBlock.add(pos, element);
			currBlockEnd++;

		} else {
			// TODO special case index 0 and index == size()
			Block<T> block2 = new Block<T>(blockSize);
			if (index == size || index == 0) {
				blocks.add(size, block2);
				block2.add(0, element);
			} else {
				int len = blockSize/2;
				currNode.relativePosition -= len;

				int len2 = currBlock.size()-len;
				for (int i=0; i<len2; i++) {
				    block2.values.add(null); // TODO Add method to GapList
				}
				GapList.copy(currBlock.values, len, block2.values, 0, len2);
				currBlock.values.remove(len, len2);
				blocks.add(currNode.relativePosition+len, block2);

				if (pos < len) {
					// Insert element in first block
					currBlockEnd -= len2;
					currBlock.add(pos, element);
					currNode.relativePosition++;
					currBlockEnd++;
				} else {
					// Insert element in second block
					currBlock = block2;
					currNode = currNode.next();
					assert(currNode.getValue() == currBlock);
					currBlockStart += len;
					pos -= len;
					block2.add(pos, element);
					currBlockEnd++;
				}
			}
		}
		size++;

		if (DUMP) dump();
		if (CHECK) check();

		return true;
	}

	@Override
	protected void doRemoveAll(int index, int len) {
//		// Remove whole blocks
//		while (index <= currBlockStart && index+len >= currBlockEnd) {
//			int currBlockSize = currBlock.size();
//			size -= currBlockSize;
//			len -= currBlockSize;
//			if (len == 0) {
//				assert(currBlockStart == 0);
//				currBlockEnd = 0;
//				return;
//			}
//			blocks.remove(currBlockIndex);
//			if (currBlockIndex < blocks.size()) {
//				currBlock = blocks.get(currBlockIndex);
//				currBlockEnd = currBlockStart + currBlock.size();
//			} else {
//				currBlockIndex--;
//				currBlock = blocks.get(currBlockIndex);
//				currBlockEnd = size;
//				currBlockStart = currBlockEnd - currBlock.size();
//			}
//		}

		// Remove remaining elements
		for (int i=0; i<len; i++) {
			doRemove(index);
		}
	}

	protected T doRemove(int index) {
		int pos = getBlockIndexWrite(index);
		T oldElem = currBlock.remove(pos);
		currBlockEnd--;
		size--;

		if (currBlock.size() < blockSize/3) {
			AVLNode<Block<T>> leftNode = currNode.previous();
			// TODO performance
			if (leftNode != null && leftNode.getValue().size() < blockSize/3) {
				// Merge with left block
			    int len = currBlock.size();
			    int dstSize = leftNode.getValue().size();
	            for (int i=0; i<len; i++) {
	                leftNode.getValue().values.add(null); // TODO Add method to GapList
	            }
				GapList.copy(currBlock.values, 0, leftNode.getValue().values, dstSize, len);
				blocks.remove(currBlockEnd);
				currBlock = leftNode.getValue();
				currBlockStart -= dstSize;

			} else if (currNode.next() != null && currNode.next().getValue().size() < blockSize/3) {
				// Merge with right block
			    int len = currNode.next().getValue().values.size();
			    int dstSize = currBlock.values.size();
	            for (int i=0; i<len; i++) {
	            	currBlock.values.add(null); // TODO Add method to GapList
	            }
				GapList.copy(currNode.next().getValue().values, 0, currBlock.values, dstSize, len);
				blocks.remove(currBlockEnd+len);
				currBlockEnd += len;
			}
		}

		if (DUMP) dump();
		if (CHECK) check();

		return oldElem;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		for (int i=0; i<size(); i++) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(get(i));
		}
		buf.append("]");
		return buf.toString();
	}

	private void dump() {
		//LOG.debug(PrintTools.print(blockEndIndex));
		//LOG.debug(PrintTools.print(blockModCount));
		//LOG.debug(toString());
	}

	/**/

    @Override
    public BigList<T> unmodifiableList() {
        // Naming as in java.util.Collections#unmodifiableList
        return new ImmutableBigList<T>(this);
    }

	@Override
	protected void doEnsureCapacity(int minCapacity) {
		if (blocks.size() == 1) {
			if (minCapacity > blockSize) {
				minCapacity = blockSize;
			}
			currBlock.values.doEnsureCapacity(minCapacity);
		}
	}

	@Override
	public void trimToSize() {
		if (blocks.size() == 1) {
			currBlock.values.trimToSize();
		}
	}

	@Override
	public IGapList<T> doCreate(int capacity) {
		// TODO make sure if content fits in one block, array is allocated directly
		return new BigList(this.blockSize);
	}

	@Override
	public void sort(int index, int len, Comparator<? super T> comparator) {
    	checkRange(index, len);

    	if (blocks.size() == 0) {
    		currBlock.values.sort(index, len, comparator);
    	} else {
    		InternalSort.sort(this, comparator, index, index+len);
    	}
	}

	@Override
	public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
    	checkRange(index, len);

    	if (blocks.size() == 1) {
    		return currBlock.values.binarySearch(key, comparator);
    	} else {
    		return Collections.binarySearch((List<K>) this, key, comparator);
    	}
	}

}
