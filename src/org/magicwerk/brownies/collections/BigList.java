package org.magicwerk.brownies.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

import org.magicwerk.brownies.collections.helper.InternalSort;
import org.magicwerk.brownies.collections.helper.RangeList;
import org.magicwerk.brownies.collections.helper.RangeList.AVLNode;

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
	public static class Block<T> implements Serializable {
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

		public String toString() {
			return values.toString();
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
	private RangeList<Block<T>> blocks;
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
		blocks = new RangeList<Block<T>>();
		currBlock = new Block<T>();
		blocks.add(0, currBlock);
		currNode = blocks.root;
	}

    public BigList(BigList<T> that) {
    	initClone(that);
    }

    public BigList(Collection<T> that) {
        blockSize = BLOCK_SIZE;

		blocks = new RangeList<Block<T>>();
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
		blocks = new RangeList<Block<T>>();
		AVLNode<Block<T>> node = bigList.blocks.root.min();
		while (node != null) {
			Block<T> block = node.getValue();
			block.ref();
			size += block.size();
			blocks.add(size, block);
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
	public int capacity() {
		return -1;	// TODO
	}

	@Override
	protected T doGet(int index) {
		int pos = getBlockIndex(index, false, 0);
		return currBlock.get(pos);
	}

	@Override
	protected T doSet(int index, T elem) {
		int pos = getBlockIndex(index, true, 0);
		T oldElem = currBlock.get(pos);
		currBlock.set(pos, elem);
		return oldElem;
	}

	@Override
	protected T doReSet(int index, T elem) {
		int pos = getBlockIndex(index, true, 0);
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
//	private int getBlockIndexWrite2(int index) {
//		int i = getBlockIndex(index, false, 0);
//		return i;
//	}

	/**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currBlock to remember this last used block.
	 *
	 * @param index	list index (0 <= index <= size())
	 * @return		relative index within block
	 */
	private int getBlockIndex(int index, boolean write, int modify) {
		// Determine block where specified index is located and store it in currBlock
//		if (index >= currBlockStart && index < currBlockEnd) {
//			// currBlock is already set correctly
//			return index - currBlockStart;
//		}

//        if (index == size) {
//        	if (currBlockEnd != size) {
//        		currNode = blocks.root.max();
//        		currBlock = currNode.getValue();
//        		currBlockEnd = size;
//        		currBlockStart = size - currBlock.size();
//        	}
//        	return index - currBlockStart;
//
//        } else if (index == 0) {
//        	if (currBlockStart != 0) {
//        		currNode = blocks.root.min();
//        		currBlock = currNode.getValue();
//        		currBlockEnd = currBlock.size();
//        		currBlockStart = 0;
//        	}
//            return 0;
//        }

        int[] endIndex = new int[1];
//        currNode = blocks.getIn(index, endIndex);
        currNode = blocks.access(index, modify, endIndex);
        currBlock = currNode.getValue();
//        currBlockStart = endIndex[0];
//        currBlockEnd = currBlockStart + currBlock.size();
        currBlockEnd = endIndex[0];
        currBlockStart = currBlockEnd - currBlock.size();
        assert(index >= currBlockStart);

        if (write) {
			if (currBlock.isShared()) {
				currBlock.unref();
				currBlock = new Block<T>(currBlock);
				currNode.setValue(currBlock);
			}
	    }

        return index - currBlockStart;
	}

	void checkTree() {
		AVLNode node = blocks.root;
		int index = node.relativePosition;
		while (node.left != null) {
			node = node.left;
			index += node.relativePosition;
		}
		Block<T> block = (Block<T>) node.getValue();
		assert(block.size() == index);
		int lastIndex = index;

		while (lastIndex < size()) {
			node = blocks.root;
			index = node.relativePosition;
			int searchIndex = lastIndex+1;
			while (true) {
				block = (Block<T>) node.getValue();
				if (searchIndex > index-block.size() && searchIndex <= index) {
					break;
				} else if (searchIndex < index) {
					if (node.left != null && node.left.height<node.height) {
						node = node.left;
					} else {
						break;
					}
				} else {
					if (node.right != null && node.right.height<node.height) {
						node = node.right;
					} else {
						break;
					}
				}
				index += node.relativePosition;
			}
			block = (Block<T>) node.getValue();
			assert(block.size() == index-lastIndex);
			lastIndex = index;
		}
		assert(index == size());

//		while (true) {
//			if (node.right== null) {
//				break;
//			}
//            if (node.rightIsNext || node.right == null) {
//                node = node.right;
//                if (node == blocks.root) {
//                	index = node.relativePosition;
//                } else {
//                	if (node.relativePosition > 0) {
//                		index += node.relativePosition;
//                	} else {
//            			index -= node.left.relativePosition;
//                	}
//                }
//            } else {
//                node = node.right;
//                index += node.relativePosition;
//                while (node.getLeftSubTree() != null) {
//                	node = node.getLeftSubTree();
//                    index += node.relativePosition;
//                }
//            }
//
//           // if (node == null) {
//           // 	break;
//           // }
//
//			//node = node.next();
//			block = (Block<T>) node.getValue();
//			assert(block.size() == index-lastIndex);
//			lastIndex = index;
//		}
	}

	void changeNode(AVLNode node, int add) {
		AVLNode nextTop = node.nextTop();
		if (nextTop != null) {
			nextTop.relativePosition += add;
		}
		AVLNode prevTop = node.prevTop();
		if (prevTop != null) {
			if (prevTop != this.blocks.root) {
				prevTop.relativePosition -= add;
			}
		}
		if (node.relativePosition >= 0) {
			node.relativePosition += add;
		}
		AVLNode prevDown = node.prevDown();
		if (prevDown != null) {
			prevDown.relativePosition -= add;
		}
	}

	@Override
	protected boolean doAdd(int index, T element) {
		if (index == -1) {
			index = size;
		}
		if (index == size && blocks.root.max().getValue().size() == blockSize) {
			// Insert new block at tail
			Block<T> block = new Block<T>(blockSize);
			block.add(0, element);
			blocks.add(size+1, block);
		} else if (index == 0 && blocks.root.min().getValue().size() == blockSize) {
			// Insert new block at head
			Block<T> block = new Block<T>(blockSize);
			block.add(0, element);
			blocks.add(1, block);
			blocks.root.min().relativePosition = -blockSize;
		} else {
			int pos = getBlockIndex(index, true, 1);

			// Insert in current block
			if (currBlock.size() < blockSize) {
				//changeNode(currNode, 1);
				currBlock.add(pos, element);
				currBlockEnd++;

			} else {
				// Split block for insert
				Block<T> nextBlock = new Block<T>(blockSize);
				int nextBlockLen = blockSize/2;
				int blockLen = blockSize - nextBlockLen;
				nextBlock.values.init(nextBlockLen, null);
				GapList.copy(currBlock.values, blockLen, nextBlock.values, 0, nextBlockLen);
				currBlock.values.remove(nextBlockLen, blockSize-nextBlockLen);

				//assert(n.equals(currNode.previous()));

				if (currNode.relativePosition < 0) {
					currNode.relativePosition += (nextBlockLen+1);
				} else {
					currNode.relativePosition -= (nextBlockLen+1);
				}

				if (pos < blockLen) {
					// Insert element in first block
					blocks.add(currBlockEnd, nextBlock);
					if (currNode.relativePosition > 0) {
						currNode.relativePosition += 1;
					} else {
						currNode.relativePosition -= 1;
					}
					currBlock.add(pos, element);
					currBlockEnd = currBlockStart+blockLen+1;
				} else {
					// Insert element in second block
					blocks.add(currBlockEnd, nextBlock);
					currNode = currNode.next();
					if (currNode.relativePosition > 0) {
						currNode.relativePosition += 1;
					} else {
						currNode.relativePosition -= 1;
					}
					currBlock = nextBlock;
					currBlock.add(pos-blockLen, element);
					currBlockStart += blockLen;
					currBlockEnd++;
				}
			}
		}
		size++;

		if (DUMP) dump();
		if (CHECK) check();
		checkTree(); //TODO

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
		int pos = getBlockIndex(index, true, -1);
		T oldElem = currBlock.remove(pos);
		currBlockEnd--;
		size--;

		if (currBlock.size() >= blockSize/3) {
			if (currNode.relativePosition > 0) {
				currNode.relativePosition--;
				AVLNode left = blocks.root.previous();
				if (left != null) {
					left.relativePosition++;
				}
			} else {
				blocks.root.relativePosition--;
			}

		} else {
			AVLNode<Block<T>> leftNode = currNode.previous();
			// TODO performance
			if (leftNode != null && leftNode.getValue().size() <= blockSize/3+1) {
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

			} else if (currNode.next() != null && currNode.next().getValue().size() <= blockSize/3+1) {
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
