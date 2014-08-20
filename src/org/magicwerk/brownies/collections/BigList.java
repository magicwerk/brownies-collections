package org.magicwerk.brownies.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

import org.magicwerk.brownies.collections.helper.InternalSort;

/**
 * The first block (GapList) used grows dynamcically, all others
 * are allocated with fixed size. This is necessary to prevent starving
 * because of GC usage.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class BigList<T>
    extends IList<T>
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

    /** The root node in the AVL tree */
    private BlockNode<Block<T>> root;



	//private int currBlockIndex;
	/** Index of first element in currBlock for the whole BigList */
	private int currBlockStart;
	/** Index of last element in currBlock for the whole BigList */
	private int currBlockEnd;
	private BlockNode<Block<T>> currNode;
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
            //this.blocks = that.blocks TODO
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
		currBlock = new Block<T>();
		add1(0, currBlock);
		currNode = root; // TODO
	}

    public BigList(BigList<T> that) {
    	doClone(that);
    }

    public BigList(Collection<T> that) {
        blockSize = BLOCK_SIZE;

		//blocks = new RangeList<Block<T>>();
		currBlock = new Block<T>();
		add1(0, currBlock);
		currNode = root; // TODO

        for (Object elem: that.toArray()) {
            add((T) elem);
        }
        assert(size() == that.size());
    }

    @Override
    protected void doAssign(IList<T> that) {
    	//FIXME
    }

	@Override
	protected void doClone(IList<T> that) {
//		BigList<T> bigList = (BigList<T>) that;
//        blockSize = bigList.blockSize;
//
//        int size = 0;
//		blocks = new RangeList<Block<T>>();
//		AVLNode<Block<T>> node = bigList.blocks.root.min();
//		while (node != null) {
//			Block<T> block = node.getValue();
//			block.ref();
//			size += block.size();
//			blocks.add(size, block);
//		}
//		assert(size == bigList.size);
//
//		currNode = blocks.root;
//		currBlock = currNode.getValue();
//		currBlockStart = 0;
//		currBlockEnd = currBlock.size();
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
    int[] endIndex = new int[1];// FIXME

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

		endIndex[0] = 0;
        currNode = access(index, modify, endIndex);
        currBlock = currNode.getValue();
        currBlockEnd = endIndex[0];
        currBlockStart = currBlockEnd - currBlock.size();

        c++;	// FIXME
        if (!(index >= currBlockStart && index <= currBlockEnd)) {
        	System.out.printf("%s: %s, %s, %s\n", c, index, currBlockStart, currBlockEnd);
        }
        assert(index >= currBlockStart && index <= currBlockEnd);

        if (write) {
			if (currBlock.isShared()) {
				currBlock.unref();
				currBlock = new Block<T>(currBlock);
				currNode.setValue(currBlock);
			}
	    }

        return index - currBlockStart;
	}

	static int c;

	void checkNode(BlockNode node) {
		BlockNode child = node.getLeftSubTree();
		assert(child == null || child.parent == node);
		child = node.getRightSubTree();
		assert(child == null || child.parent == node);
	}

	boolean checkTree() {
		if (true) {
			//return true;
		}
		BlockNode<Block<T>> node = root;
		checkNode(node);
		int index = node.relativePosition;
		while (node.left != null) {
			node = node.left;
			checkNode(node);
			assert(node.relativePosition < 0);
			index += node.relativePosition;
			assert((node.getValue()).size() > 0);
		}
		Block<T> block = node.getValue();
		assert(block.size() == index);
		int lastIndex = index;

		while (lastIndex < size()) {
			node = root;
			index = node.relativePosition;
			int searchIndex = lastIndex+1;
			while (true) {
				checkNode(node);
				block = node.getValue();
				assert(block.size() > 0);
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
			block = node.getValue();
			assert(block.size() == index-lastIndex);
			lastIndex = index;
		}
		assert(index == size());

		return true;
	}

	@Override
	protected boolean doAdd(int index, T element) {
		if (index == -1) {
			index = size;
		}
		if (index == size && root.max().getValue().size() == blockSize) {
			// Insert new block at tail
			Block<T> block = new Block<T>(blockSize);
			block.add(0, element);
			add1(size+1, block);
		} else if (index == 0 && root.min().getValue().size() == blockSize) {
			// Insert new block at head
			Block<T> block = new Block<T>(blockSize);
			block.add(0, element);
			add1(1, block);
			//blocks.root.relativePosition += 1;
			root.min().relativePosition = -blockSize;
		} else {
			int pos = getBlockIndex(index, true, 1);

			// There is still place in the current block: insert in current block
			if (currBlock.size() < blockSize) {
				//changeNode(currNode, 1);
				currBlock.add(pos, element);
				currBlockEnd++;
				BlockNode<Block<T>> left = currNode.getLeftSubTree();
				if (left != null) {
					assert(left.relativePosition < 0);
//					left.relativePosition--;
				}

			} else {
				// No place any more in current block: split block for insert
				Block<T> nextBlock = new Block<T>(blockSize);
				int nextBlockLen = blockSize/2;
				int blockLen = blockSize - nextBlockLen;
				nextBlock.values.init(nextBlockLen, null);
				GapList.copy(currBlock.values, blockLen, nextBlock.values, 0, nextBlockLen);
				currBlock.values.remove(blockLen, blockSize-blockLen);

				// Subtract 1 more because getBlockIndex() has already added 1
				modify(-nextBlockLen-1);
				add1(currBlockEnd-nextBlockLen, nextBlock);
				assert(checkTree());

				if (pos < blockLen) {
					// Insert element in first block
					currBlock.add(pos, element);
					currBlockEnd = currBlockStart+blockLen+1;
					modify(1);
				} else {
					// Insert element in second block
					currNode = currNode.next();
					modify(1);
					currBlock = nextBlock;
					currBlock.add(pos-blockLen, element);
					currBlockStart += blockLen;
					currBlockEnd++;
				}

				//				if (currNode.relativePosition < 0) {
//					currNode.relativePosition += (nextBlockLen+1);
//				} else {
//					currNode.relativePosition -= (nextBlockLen+1);
//				}
//
//				if (pos < blockLen) {
//					// Insert element in first block
//					blocks.add(currBlockEnd, nextBlock);
//					if (currNode.relativePosition > 0) {
//						currNode.relativePosition += 1;
//					} else {
//						currNode.relativePosition -= 1;
//					}
//					currBlock.add(pos, element);
//					currBlockEnd = currBlockStart+blockLen+1;
//				} else {
//					// Insert element in second block
//					blocks.add(currBlockEnd, nextBlock);
//					currNode = currNode.next();
//					if (currNode.relativePosition > 0) {
//						currNode.relativePosition += 1;
//					} else {
//						currNode.relativePosition -= 1;
//					}
//					currBlock = nextBlock;
//					currBlock.add(pos-blockLen, element);
//					currBlockStart += blockLen;
//					currBlockEnd++;
//				}
			}
		}
		size++;

		if (DUMP) dump();
		if (CHECK) check();
		assert(checkTree()); //TODO

		return true;
	}

	void modify(int modify) {
		if (currBlockEnd < root.relativePosition || currNode == root) {
			root.relativePosition += modify;
		}
		if (currNode.relativePosition < 0) {
			BlockNode<Block<T>> leftNode = currNode.getLeftSubTree();
			if (leftNode != null) {
				leftNode.relativePosition -= modify;
			}
			BlockNode<Block<T>> pp = currNode.parent;
			assert(pp.getLeftSubTree() == currNode);
			boolean parentRight = true;
			while (true) {
				BlockNode p = pp.parent;
				if (p == null) {
					break;
				}
				boolean pRight = (p.getLeftSubTree() == pp);
//				if (parentRight == true && pRight == false) {
				if (parentRight != pRight) {
					if (pp.relativePosition > 0) {
						pp.relativePosition += modify;
					} else {
						pp.relativePosition -= modify;
					}
				}
				pp = p;
				parentRight = pRight;
			}
		} else {
			if (currNode != root) {
				currNode.relativePosition += modify;
			}
			BlockNode<Block<T>> leftNode = currNode.getLeftSubTree();
			if (leftNode != null) {
				leftNode.relativePosition -= modify;
			}
			BlockNode<Block<T>> parent = currNode.parent;
			if (parent != null) {
				assert(parent.getRightSubTree() == currNode);
				boolean parentLeft = true;
				while (true) {
					BlockNode p = parent.parent;
					if (p == null) {
						break;
					}
					boolean pLeft = (p.getRightSubTree() == parent);
					//if (parentLeft == true && pLeft == false) {
					if (parentLeft != pLeft) {
						if (parent.relativePosition > 0) {
							parent.relativePosition += modify;
						} else {
							parent.relativePosition -= modify;
						}
					}
					parent = p;
					parentLeft = pLeft;
				}
			}
		}

//		if (node.relativePosition < 0) {
//			blocks.root.relativePosition += modify;
//			AVLNode<Block<T>> leftNode = node.getLeftSubTree();
//			if (leftNode != null) {
//				leftNode.relativePosition -= modify;
//			}
//		} else {
//			node.relativePosition += modify;
//			AVLNode<Block<T>> leftNode = node.getLeftSubTree();
//			if (leftNode != null) {
//				leftNode.relativePosition -= modify;
//			}
//		}
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

		int minBlockSize = Math.max(blockSize/3, 1);
		if (currBlock.size() < minBlockSize) {
			if (currBlock.size() == 0) {
				if (!isOnlyRootBlock()) {
					if (currNode == root) {
						root = currNode.removeSelf();
					} else {
						BlockNode oldCurrNode = currNode;
						BlockNode pp = currNode.parent;
						currNode = currNode.removeSelf();
						// Position have already been updated, so just unlink this node
						if (pp != null) {
							if (pp.left == oldCurrNode) {
								pp.left = currNode;
							} else {
								assert(pp.right == oldCurrNode);
								pp.right = currNode;
							}
						}
					}
				}
				currNode = root; // TODO
				currBlock = currNode.getValue();
				currBlockEnd = root.relativePosition;
				currBlockStart = currBlockEnd - currBlock.size();

			} else {
    			BlockNode<Block<T>> leftNode = currNode.previous();
    			// TODO performance
    			if (leftNode != null && leftNode.getValue().size() <= blockSize/3+1) {
    				// Merge with left block
    			    int len = currBlock.size();
    			    int dstSize = leftNode.getValue().size();
    	            for (int i=0; i<len; i++) {
    	                leftNode.getValue().values.add(null); // TODO Add method to GapList
    	            }
    				GapList.copy(currBlock.values, 0, leftNode.getValue().values, dstSize, len);
    				remove1(currBlockEnd);
    				currBlock = leftNode.getValue();
    				currBlockStart -= dstSize;

    			} else {
        			BlockNode<Block<T>> rightNode = currNode.next();
    				if (rightNode != null && rightNode.getValue().size() <= blockSize/3+1) {
        				// Merge with right block
        			    int len = rightNode.getValue().size();
        			    int dstSize = currBlock.values.size();
        	            for (int i=0; i<len; i++) {
        	            	currBlock.values.add(null); // TODO Add method to GapList
        	            }
        				GapList.copy(rightNode.getValue().values, 0, currBlock.values, dstSize, len);
        				remove1(currBlockEnd+len);
        				currBlockEnd += len;
    				}
    			}
			}
		}

		if (DUMP) dump();
		if (CHECK) check();
		checkTree();

		return oldElem;
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
		if (isOnlyRootBlock()) {
			if (minCapacity > blockSize) {
				minCapacity = blockSize;
			}
			currBlock.values.doEnsureCapacity(minCapacity);
		}
	}

	@Override
	public void trimToSize() {
		if (isOnlyRootBlock()) {
			currBlock.values.trimToSize();
		}
	}

	@Override
	public IList<T> doCreate(int capacity) {
		// TODO make sure if content fits in one block, array is allocated directly
		return new BigList(this.blockSize);
	}

	@Override
	public void sort(int index, int len, Comparator<? super T> comparator) {
    	checkRange(index, len);

    	if (isOnlyRootBlock()) {
    		currBlock.values.sort(index, len, comparator);
    	} else {
    		InternalSort.sort(this, comparator, index, index+len);
    	}
	}

	@Override
	public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
    	checkRange(index, len);

    	if (isOnlyRootBlock()) {
    		return currBlock.values.binarySearch(key, comparator);
    	} else {
    		return Collections.binarySearch((List<K>) this, key, comparator);
    	}
	}

	private boolean isOnlyRootBlock() {
		return root.left == null && root.right == null;
	}

    public BlockNode<Block<T>> access(final int index, int modify, int[] endIndex) {
        //checkInterval(index, 0, size() - 1);
        return root.access(index, modify, false, endIndex);
    }

    //-----------------------------------------------------------------------
    /**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */

    public void add1(int index, Block<T> obj) {
        //modCount++;
        //checkInterval(index, 0, size());
        if (root == null) {
            root = new BlockNode<Block<T>>(null, index, obj, null, null);
        } else {
            root = root.insert(index, obj);
        }
    }

    /**
     * Removes the element at the specified index.
     *
     * @param index  the index to remove
     * @return the previous object at that index
     */
    public void remove1(final int index) {
        root = root.remove(index);
    }

    //-----------------------------------------------------------------------
    /**
     * Implements an AVLNode which keeps the offset updated.
     * <p>
     * This node contains the real work.
     * TreeList is just there to implement {@link java.util.List}.
     * The nodes don't know the index of the object they are holding.  They
     * do know however their position relative to their parent node.
     * This allows to calculate the index of a node while traversing the tree.
     * <p>
     * The Faedelung calculation stores a flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    public static class BlockNode<E> {
    	BlockNode<E> parent;
        /** The left child node or the predecessor if {@link #leftIsPrevious}.*/
        public BlockNode<E> left;
        /** Flag indicating that left reference is not a subtree but the predecessor. */
        public boolean leftIsPrevious;
        /** The right child node or the successor if {@link #rightIsNext}. */
        public BlockNode<E> right;
        /** Flag indicating that right reference is not a subtree but the successor. */
        public boolean rightIsNext;
        /** How many levels of left/right are below this one. */
        public int height;
        /** The relative position, root holds absolute position. */
        public int relativePosition;
        /** The stored element. */
        private E value;

        /**
         * Constructs a new node with a relative position.
         *
         * @param relativePosition  the relative position of the node
         * @param obj  the value for the node
         * @param rightFollower the node with the value following this one
         * @param leftFollower the node with the value leading this one
         */
        private BlockNode(BlockNode<E> parent, final int relativePosition, final E obj,
                        final BlockNode<E> rightFollower, final BlockNode<E> leftFollower) {
        	this.parent = parent;
            this.relativePosition = relativePosition;
            value = obj;
            rightIsNext = true;
            leftIsPrevious = true;
            right = rightFollower;
            left = leftFollower;
        }

        /**
         * Gets the value.
         *
         * @return the value of this node
         */
        public E getValue() {
            return value;
        }

        /**
         * Sets the value.
         *
         * @param obj  the value to store
         */
        public void setValue(final E obj) {
            this.value = obj;
        }

        private BlockNode<E> access(final int index, int modify, boolean wasLeft, int[] idx) {
        	assert(index >= 0);
        	if (relativePosition == 0) {
        		if (modify != 0) {
        			relativePosition += modify;
        		}
        		return this;
        	}
        	if (idx[0] == 0) {
        		idx[0] = relativePosition; // root
        	}
        	BlockNode<E> leftNode = getLeftSubTree();
        	int leftIndex = idx[0]-((BigList.Block) getValue()).size();
        	assert(leftIndex >= 0);
        	if (index >= leftIndex && index < idx[0]) {
    			if (relativePosition > 0) {
    				relativePosition += modify;
    				if (leftNode != null) {
    					leftNode.relativePosition -= modify;
    				}
    			} else {
    				if (leftNode != null) {
    					leftNode.relativePosition -= modify;
    				}
    			}
        		return this;
        	}
        	if (index < idx[0]) {
        		// left
        		BlockNode<E> nextNode = getLeftSubTree();
        		if (nextNode == null || !wasLeft) {
        			if (relativePosition > 0) {
        				relativePosition += modify;
        			} else {
        				relativePosition -= modify;
        			}
        			wasLeft = true;
        		}
                if (nextNode == null) {
                	return this;
                }
                idx[0] += nextNode.relativePosition;
                return nextNode.access(index, modify, wasLeft, idx);
        	} else {
        		// right
        		BlockNode<E> nextNode = getRightSubTree();
        		if (nextNode == null || wasLeft) {
        			if (relativePosition > 0) {
        				relativePosition += modify;
       		        	BlockNode<E> left = getLeftSubTree();
       					if (left != null) {
       						left.relativePosition -= modify;
        				}
        			} else {
        				relativePosition -= modify;
        			}
        			wasLeft = false;
        		}
                if (nextNode == null) {
                	return this;
                }
                idx[0] += nextNode.relativePosition;
                return nextNode.access(index, modify, wasLeft, idx);
        	}
        }

        /**
         * Stores the node and its children into the array specified.
         *
         * @param array the array to be filled
         * @param index the index of this node
         */
        void toArray(final Object[] array, final int index) {
            array[index] = value;
            if (getLeftSubTree() != null) {
                left.toArray(array, index + left.relativePosition);
            }
            if (getRightSubTree() != null) {
                right.toArray(array, index + right.relativePosition);
            }
        }

        /**
         * Gets the next node in the list after this one.
         *
         * @return the next node
         */
        public BlockNode<E> next() {
            if (rightIsNext || right == null) {
                return right;
            }
            return right.min();
        }

        /**
         * @return null if this is the root node
         */
//        public BlockNode<E> parent() {
//        	BlockNode prev = this;
//        	BlockNode next = this;
//        	while (true) {
//        		if (prev != null) {
//        			prev = prev.previous();
//        			if (prev != null && prev.getRightSubTree() == this) {
//        				return prev;
//        			}
//        		}
//        		if (next != null) {
//	        		next = next.next();
//	        		if (next != null && next.getLeftSubTree() == this) {
//	        			return next;
//	        		}
//        		}
//        		if (prev == null && next == null) {
//        			return null;
//        		}
//        	}
//        }

        /**
         * Gets the node in the list before this one.
         *
         * @return the previous node
         */
        public BlockNode<E> previous() {
            if (leftIsPrevious || left == null) {
                return left;
            }
            return left.max();
        }

        /**
         * Inserts a node at the position index.
         *
         * @param index is the index of the position relative to the position of
         * the parent node.
         * @param obj is the object to be stored in the position.
         */
        private BlockNode<E> insert(final int index, final E obj) {
        	assert(relativePosition != 0);
            final int indexRelativeToMe = index - relativePosition;

            if (indexRelativeToMe < 0) {
                return insertOnLeft(indexRelativeToMe, obj);
            } else {
            	return insertOnRight(indexRelativeToMe, obj);
            }
        }

        private BlockNode<E> insertOnLeft(final int indexRelativeToMe, final E obj) {
            if (getLeftSubTree() == null) {
            	int pos;
            	if (relativePosition >= 0) {
            		pos = -relativePosition;
            	} else {
            		Block b = (Block) value;
            		pos = -b.size();
            	}
                setLeft(new BlockNode<E>(this, pos, obj, this, left), null);
            } else {
                setLeft(left.insert(indexRelativeToMe, obj), null);
            }
            if (relativePosition >= 0) {
        		Block b = (Block) obj;
                relativePosition += b.size();
            }
            final BlockNode<E> ret = balance();
            recalcHeight();
            return ret;
        }

        private BlockNode<E> insertOnRight(final int indexRelativeToMe, final E obj) {
            if (getRightSubTree() == null) {
            	Block b = (Block) obj;
                setRight(new BlockNode<E>(this, b.size(), obj, right, this), null);
            } else {
                setRight(right.insert(indexRelativeToMe, obj), null);
            }
            if (relativePosition < 0) {
        		Block b = (Block) obj;
                relativePosition -= b.size();
            }
            final BlockNode<E> ret = balance();
            recalcHeight();
            return ret;
        }

        //-----------------------------------------------------------------------
        /**
         * Gets the left node, returning null if its a faedelung.
         */
        public BlockNode<E> getLeftSubTree() {
            return leftIsPrevious ? null : left;
        }

        /**
         * Gets the right node, returning null if its a faedelung.
         */
        public BlockNode<E> getRightSubTree() {
            return rightIsNext ? null : right;
        }

        /**
         * Gets the rightmost child of this node.
         *
         * @return the rightmost child (greatest index)
         */
        public BlockNode<E> max() {
            return getRightSubTree() == null ? this : right.max();
        }

        /**
         * Gets the leftmost child of this node.
         *
         * @return the leftmost child (smallest index)
         */
        public BlockNode<E> min() {
            return getLeftSubTree() == null ? this : left.min();
        }

        /**
         * Removes the node at a given position.
         *
         * @param index is the index of the element to be removed relative to the position of
         * the parent node of the current node.
         */
        private BlockNode<E> remove(final int index) {
            final int indexRelativeToMe = index - relativePosition;

            if (indexRelativeToMe == 0) {
                return removeSelf();
            }
            if (indexRelativeToMe > 0) {
                setRight(right.remove(indexRelativeToMe), right.right);
                if (relativePosition < 0) {
                    relativePosition++;
                }
            } else {
                setLeft(left.remove(indexRelativeToMe), left.left);
                if (relativePosition > 0) {
                    relativePosition--;
                }
            }
            recalcHeight();
            return balance();
        }

        private BlockNode<E> removeMax() {
            if (getRightSubTree() == null) {
                return removeSelf();
            }
            setRight(right.removeMax(), right.right);
            if (relativePosition < 0) {
                relativePosition++;
            }
            recalcHeight();
            return balance();
        }

        private BlockNode<E> removeMin() {
            if (getLeftSubTree() == null) {
                return removeSelf();
            }
            setLeft(left.removeMin(), left.left);
            if (relativePosition > 0) {
                relativePosition--;
            }
            recalcHeight();
            return balance();
        }

        /**
         * Removes this node from the tree.
         *
         * @return the node that replaces this one in the parent (can be null)
         */
        public BlockNode<E> removeSelf() {
        	BlockNode<E> p = parent;
        	BlockNode<E> n = doRemoveSelf();
        	if (n != null) {
        		n.parent = p;
        	}
        	return n;
        }

        public BlockNode<E> doRemoveSelf() {
            if (getRightSubTree() == null && getLeftSubTree() == null) {
                return null;
            }
            if (getRightSubTree() == null) {
                if (relativePosition > 0) {
                    left.relativePosition += relativePosition + (relativePosition > 0 ? 0 : 1);
                } else {
                	left.relativePosition = relativePosition;
                }
                left.max().setRight(null, right);
                return left;
            }
            if (getLeftSubTree() == null) {
                //right.relativePosition += relativePosition - (relativePosition < 0 ? 0 : 1);
                right.min().setLeft(null, left);
                return right;
            }

            if (heightRightMinusLeft() > 0) {
                // more on the right, so delete from the right
                final BlockNode<E> rightMin = right.min();
                value = rightMin.value;
                if (leftIsPrevious) {
                    left = rightMin.left;
                }
                right = right.removeMin();
                if (relativePosition < 0) {
                    relativePosition++;
                }
            } else {
                // more on the left or equal, so delete from the left
                final BlockNode<E> leftMax = left.max();
                value = leftMax.value;
                if (rightIsNext) {
                    right = leftMax.right;
                }
                final BlockNode<E> leftPrevious = left.left;
                left = left.removeMax();
                if (left == null) {
                    // special case where left that was deleted was a double link
                    // only occurs when height difference is equal
                    left = leftPrevious;
                    leftIsPrevious = true;
                }
                if (relativePosition > 0) {
                    //relativePosition--;
                }
            }
            recalcHeight();
            return this;
        }

        //-----------------------------------------------------------------------
        /**
         * Balances according to the AVL algorithm.
         */
        private BlockNode<E> balance() {
            switch (heightRightMinusLeft()) {
                case 1 :
                case 0 :
                case -1 :
                    return this;
                case -2 :
                    if (left.heightRightMinusLeft() > 0) {
                        setLeft(left.rotateLeft(), null);
                    }
                    return rotateRight();
                case 2 :
                    if (right.heightRightMinusLeft() < 0) {
                        setRight(right.rotateRight(), null);
                    }
                    return rotateLeft();
                default :
                    throw new RuntimeException("tree inconsistent!");
            }
        }

        /**
         * Gets the relative position.
         */
        private int getOffset(final BlockNode<E> node) {
            if (node == null) {
                return 0;
            }
            return node.relativePosition;
        }

        /**
         * Sets the relative position.
         */
        private int setOffset(final BlockNode<E> node, final int newOffest) {
            if (node == null) {
                return 0;
            }
            final int oldOffset = getOffset(node);
            node.relativePosition = newOffest;
            return oldOffset;
        }

        /**
         * Sets the height by calculation.
         */
        private void recalcHeight() {
            height = Math.max(
                getLeftSubTree() == null ? -1 : getLeftSubTree().height,
                getRightSubTree() == null ? -1 : getRightSubTree().height) + 1;
        }

        /**
         * Returns the height of the node or -1 if the node is null.
         */
        private int getHeight(final BlockNode<E> node) {
            return node == null ? -1 : node.height;
        }

        /**
         * Returns the height difference right - left
         */
        private int heightRightMinusLeft() {
            return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
        }

        private BlockNode<E> rotateLeft() {
        	assert(!rightIsNext);
            final BlockNode<E> newTop = right; // can't be faedelung!
            final BlockNode<E> movedNode = getRightSubTree().getLeftSubTree();

            final int newTopPosition = relativePosition + getOffset(newTop);
            final int myNewPosition = -newTop.relativePosition;
            final int movedPosition = getOffset(newTop) + getOffset(movedNode);

            setRight(movedNode, newTop);
            newTop.setLeft(this, null);
            newTop.parent = this.parent;
            this.parent = newTop;

            setOffset(newTop, newTopPosition);
            setOffset(this, myNewPosition);
            setOffset(movedNode, movedPosition);

            assert(newTop.getLeftSubTree() == null || newTop.getLeftSubTree().relativePosition < 0);
            assert(newTop.getRightSubTree() == null || newTop.getRightSubTree().relativePosition > 0);
            return newTop;
        }

        private BlockNode<E> rotateRight() {
        	assert(!leftIsPrevious);
            final BlockNode<E> newTop = left; // can't be faedelung
            final BlockNode<E> movedNode = getLeftSubTree().getRightSubTree();

            final int newTopPosition = relativePosition + getOffset(newTop);
            final int myNewPosition = -newTop.relativePosition;
            final int movedPosition = getOffset(newTop) + getOffset(movedNode);

            setLeft(movedNode, newTop);
            newTop.setRight(this, null);
            newTop.parent = this.parent;
            this.parent = newTop;

            setOffset(newTop, newTopPosition);
            setOffset(this, myNewPosition);
            setOffset(movedNode, movedPosition);

            assert(newTop.getLeftSubTree() == null || newTop.getLeftSubTree().relativePosition < 0);
            assert(newTop.getRightSubTree() == null || newTop.getRightSubTree().relativePosition > 0);
            return newTop;
        }

        /**
         * Sets the left field to the node, or the previous node if that is null
         *
         * @param node  the new left subtree node
         * @param previous  the previous node in the linked list
         */
        private void setLeft(final BlockNode<E> node, final BlockNode<E> previous) {
            leftIsPrevious = node == null;
            left = leftIsPrevious ? previous : node;
            recalcHeight();
        }

        /**
         * Sets the right field to the node, or the next node if that is null
         *
         * @param node  the new right subtree node
         * @param next  the next node in the linked list
         */
        private void setRight(final BlockNode<E> node, final BlockNode<E> next) {
            rightIsNext = node == null;
            right = rightIsNext ? next : node;
            recalcHeight();
        }

        /**
         * Used for debugging.
         */
        @Override
        public String toString() {
            return new StringBuilder()
                .append("AVLNode(")
                .append(relativePosition)
                .append(',')
                .append(left != null)
                .append(',')
                .append(value)
                .append(',')
                .append(getRightSubTree() != null)
                .append(", faedelung ")
                .append(rightIsNext)
                .append(" )")
                .toString();
        }
    }

}
