package org.magicwerk.brownies.collections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

import org.magicwerk.brownies.collections.helper.MergeSort;

/**
 * The first block (GapList) used grows dynamcically, all others
 * are allocated with fixed size. This is necessary to prevent starving
 * because of GC usage.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class BigList<E> extends IList<E> {

    /**
     * An immutable version of a BigList.
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
        protected void doClear() {
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
			values.init(that.values);
			refCount = 1;
		}

		public boolean isShared() {
			return refCount > 1;
		}

		public Block<T> ref() {
			refCount++;
			return this;
		}

		public void unref() {
			refCount--;
		}

		public int size() {
			return values.size();
		}

		public String toString() {
			return values.toString();
		}
	}

	/** Set to true for debugging during developping */
	public static final boolean TRACE = false;
	public static final boolean CHECK = true;
	public static final boolean DUMP = false;

	/** Default block size */
	private static int BLOCK_SIZE = 1000;

	/** Number of elements stored at maximum in a block */
	private int blockSize;
	/** Number of elements stored in this BigList */
	private int size;

    /** The root node in the tree */
    private BlockNode<E> root;
	/** Current node */
	private BlockNode<E> currNode;
	/** Block of current node */
	private Block<E> currBlock;
	/** Start index of current block */
	private int currBlockStart;
	/** End index of current block */
	private int currBlockEnd;
	/** Modify value which must be applied before this block is not current any more */
	private int currModify;

    /**
     * Constructor used internally, e.g. for ImmutableBigList.
     *
     * @param copy true to copy all instance values from source,
     *             if false nothing is done
     * @param that list to copy
     */
    protected BigList(boolean copy, BigList<E> that) {
        if (copy) {
            this.blockSize = that.blockSize;
            this.currBlock = that.currBlock;
            this.currBlockStart = that.currBlockStart;
            this.currBlockEnd = that.currBlockEnd;
            this.currNode = that.currNode;
            this.root = that.root;
            this.size = that.size;
        }
    }

	// --- Static methods ---

    /**
     * Create new list.
     *
     * @return          created list
     * @param <E>       type of elements stored in the list
     */
    // This separate method is needed as the varargs variant creates the GapList with specific size
    public static <E> BigList<E> create() {
        return new BigList<E>();
    }

    /**
     * Create new list with specified elements.
     *
     * @param coll      collection with element
     * @return          created list
     * @param <E>       type of elements stored in the list
     */
	public static <E> BigList<E> create(Collection<? extends E> coll) {
		return new BigList<E>(coll);
	}

	/**
	 * Create new list with specified elements.
	 *
	 * @param elems 	array with elements
	 * @return 			created list
	 * @param <E> 		type of elements stored in the list
	 */
	public static <E> BigList<E> create(E... elems) {
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
		doInit(blockSize, -1);
	}

    public BigList(Collection<? extends E> that) {
    	if (that instanceof BigList) {
    		doAssign((BigList<E>) that);
        	doClone((BigList<E>) that);

    	}  else {
	        blockSize = BLOCK_SIZE;

			currBlock = new Block<E>();
			addBlock(0, currBlock);

	        for (Object elem: that.toArray()) {
	            add((E) elem);
	        }
	        assert(size() == that.size());
    	}
    }

    public void init() {
    	clear();
    }

    public void init(E... elems) {
    	clear();
        for (E elem: elems) {
            add(elem);
        }
    }

    public void init(Collection<? extends E> that) {
    	clear();
    	addAll(that);
    }

    /**
     * Returns block size used for this BigList.
     *
     * @return block size used for this BigList
     */
    public int blockSize() {
    	return blockSize;
    }

    //---

	private BigList(int blockSize, int firstBlockSize) {
		doInit(blockSize, firstBlockSize);
	}

	void doInit(int blockSize, int firstBlockSize) {
		this.blockSize = blockSize;

		// First block will grow until it reaches blockSize
		if (firstBlockSize <= 1) {
			currBlock = new Block<E>();
		} else {
			currBlock = new Block<E>(firstBlockSize);
		}
		addBlock(0, currBlock);
	}

	@Override
    public BigList<E> copy() {
	    return (BigList<E>) super.copy();
	}

    @Override
    protected void doAssign(IList<E> that) {
		BigList<E> list = (BigList<E>) that;
        this.blockSize = list.blockSize;
        this.currBlock = list.currBlock;
        this.currBlockEnd = list.currBlockEnd;
        this.currBlockStart = list.currBlockStart;
        this.currNode = list.currNode;
        this.root = list.root;
        this.size = list.size;
    }

	@Override
	protected void doClone(IList<E> that) {
		BigList<E> bigList = (BigList<E>) that;
		bigList.check();
		bigList.releaseBlock();
		root = copy(bigList.root);
        currNode = null;
        currModify = 0;
        check();
	}

	private BlockNode<E> copy(BlockNode<E> node) {
		BlockNode<E> newNode = node.min();
		int index = newNode.block.size();
       	BlockNode<E> newRoot = new BlockNode<E>(null, index, newNode.block.ref(), null, null);
       	while (true) {
       		newNode = newNode.next();
       		if (newNode == null) {
       	       	return newRoot;
       		}
       		index += newNode.block.size();
       		newRoot = newRoot.insert(index, newNode.block.ref());
       		newRoot.parent = null;
       	}
	}

	@Override
	public E getDefaultElem() {
		return null;
	}

    @Override
    protected void finalize() {
    	// This list will be garbage collected, so unref all referenced blocks
		BlockNode<E> node = root.min();
       	while (node != null) {
       		node.block.unref();
       		node = node.next();
       	}
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
	protected E doGet(int index) {
		int pos = getBlockIndex(index, false, 0);
		return currBlock.values.doGet(pos);
	}

	@Override
	protected E doSet(int index, E elem) {
		int pos = getBlockIndex(index, true, 0);
		E oldElem = currBlock.values.doGet(pos);
		currBlock.values.doSet(pos, elem);
		return oldElem;
	}

	@Override
	protected E doReSet(int index, E elem) {
		int pos = getBlockIndex(index, true, 0);
		E oldElem = currBlock.values.doGet(pos);
		currBlock.values.doSet(pos, elem);
		return oldElem;
	}

	private void releaseBlock() {
		if (currModify != 0) {
			int modify = currModify;
			currModify = 0;
			modify(currNode, modify);
		}
		currNode = null;
	}

	/**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currBlock to remember this last used block.
	 *
	 * @param index	list index (0 <= index <= size())
	 * @return		relative index within block
	 */
	private int getBlockIndex(int index, boolean write, int modify) {
		// Determine block where specified index is located and store it in currBlock
		if (currNode != null) {
			if (index >= currBlockStart && (index < currBlockEnd || index == currBlockEnd && size == index)) {
				// currBlock is already set correctly
		        if (write) {
					if (currBlock.isShared()) {
						currBlock.unref();
						currBlock = new Block<E>(currBlock);
						currNode.setBlock(currBlock);
					}
			    }
				currModify += modify;
				return index - currBlockStart;
			}
			releaseBlock();
		}

		boolean done = false;
		if (index == size) {
        	if (currNode == null || currBlockEnd != size) {
        		currNode = root.max();
        		currBlock = currNode.getBlock();
        		currBlockEnd = size;
        		currBlockStart = size - currBlock.size();
        	}
        	if (modify != 0) {
        		currNode.relativePosition += modify;
        		BigList<E>.BlockNode<E> leftNode = currNode.getLeftSubTree();
        		if (leftNode != null) {
        			leftNode.relativePosition -= modify;
        		}
        	}
			done = true;

        } else if (index == 0) {
        	if (currNode == null || currBlockStart != 0) {
        		currNode = root.min();
        		currBlock = currNode.getBlock();
        		currBlockEnd = currBlock.size();
        		currBlockStart = 0;
        	}
        	if (modify != 0) {
        		root.relativePosition += modify;
        	}
			done = true;
		}

		if (!done) {
			// Reset currBlockEnd, it will be then set by access()
			currBlockEnd = 0;
	        currNode = access(index, modify);
	        currBlock = currNode.getBlock();
	        currBlockStart = currBlockEnd - currBlock.size();
		}

        assert(index >= currBlockStart && index <= currBlockEnd);

        if (write) {
			if (currBlock.isShared()) {
				currBlock.unref();
				currBlock = new Block<E>(currBlock);
				currNode.setBlock(currBlock);
			}
	    }

        return index - currBlockStart;
	}

	void checkNode(BlockNode<E> node) {
		assert((node.block.size() > 0 || node == root) && node.block.size() <= blockSize);
		BlockNode<E> child = node.getLeftSubTree();
		assert(child == null || child.parent == node);
		child = node.getRightSubTree();
		assert(child == null || child.parent == node);
	}

	void checkHeight(BlockNode<E> node) {
		BlockNode<E> left = node.getLeftSubTree();
		BlockNode<E> right = node.getRightSubTree();
		if (left == null) {
			if (right == null) {
				assert(node.height == 0);
			} else {
				assert(right.height == node.height-1);
				checkHeight(right);
			}
		} else {
			if (right == null) {
				assert(left.height == node.height-1);
			} else {
				assert(left.height == node.height-1 || left.height == node.height-2);
				assert(right.height == node.height-1 || right.height == node.height-2);
				assert(right.height == node.height-1 || left.height == node.height-1);
			}
			checkHeight(left);
		}
	}

	void check() {
		if (true) {return; } //TODO

		if (currNode != null) {
			assert(currNode.block == currBlock);
			assert(currBlockStart >= 0 && currBlockEnd <= size && currBlockStart <= currBlockEnd);
			assert(currBlockStart + currBlock.size() == currBlockEnd);
		}

		if (root == null) {
			assert(size == 0);
			return;
		}

    	checkHeight(root);

    	BlockNode<E> oldCurrNode = currNode;
    	int oldCurrModify = currModify;
    	if (currModify != 0) {
    		currNode = null;
    		currModify = 0;
    		modify(oldCurrNode, oldCurrModify);
    	}

		BlockNode<E> node = root;
		checkNode(node);
		int index = node.relativePosition;
		while (node.left != null) {
			node = node.left;
			checkNode(node);
			assert(node.relativePosition < 0);
			index += node.relativePosition;
		}
		Block<E> block = node.getBlock();
		assert(block.size() == index);
		int lastIndex = index;

		while (lastIndex < size()) {
			node = root;
			index = node.relativePosition;
			int searchIndex = lastIndex+1;
			while (true) {
				checkNode(node);
				block = node.getBlock();
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
			block = node.getBlock();
			assert(block.size() == index-lastIndex);
			lastIndex = index;
		}
		assert(index == size());

    	if (oldCurrModify != 0) {
    		modify(oldCurrNode, -oldCurrModify);
    	}
		currNode = oldCurrNode;
		currModify = oldCurrModify;
	}

	@Override
	protected boolean doAdd(int index, E element) {
		if (index == -1) {
			index = size;
		}
		// Insert
		int pos = getBlockIndex(index, true, 1);

		// If there is still place in the current block: insert in current block
		int maxSize = (index == size || index == 0) ? blockSize*9/10 : blockSize;
		// The second part of the condition is a work around to handle the case of insertion as position 0 correctly
		// where blockSize() is 2 (the new block would then be added after the current one)
		if (currBlock.size() < maxSize || (currBlock.size() == 1 && currBlock.size() < blockSize)) {
			currBlock.values.doAdd(pos, element);
			currBlockEnd++;

		} else {
			// No place any more in current block
			Block<E> newBlock = new Block<E>(blockSize);
			if (index == size) {
				// Insert new block at tail
				newBlock.values.doAdd(0, element);
				modify(currNode, -1);
				addBlock(size+1, newBlock);
				BlockNode<E> lastNode = currNode.next();
				currNode = lastNode;
				currBlock = currNode.block;
				currBlockStart = currBlockEnd;
				currBlockEnd++;

			} else if (index == 0) {
				// Insert new block at head
				newBlock.values.doAdd(0, element);
				modify(currNode, -1);
				addBlock(1, newBlock);
				BlockNode<E> firstNode = currNode.previous();
				currNode = firstNode;
				currBlock = currNode.block;
				currBlockStart = 0;
				currBlockEnd = 1;

			} else {
				// Split block for insert
				int nextBlockLen = blockSize/2;
				int blockLen = blockSize - nextBlockLen;
				newBlock.values.init(nextBlockLen, null);
				GapList.copy(currBlock.values, blockLen, newBlock.values, 0, nextBlockLen);
				currBlock.values.remove(blockLen, blockSize-blockLen);

				// Subtract 1 more because getBlockIndex() has already added 1
				modify(currNode, -nextBlockLen-1);
				addBlock(currBlockEnd-nextBlockLen, newBlock);

				if (pos < blockLen) {
					// Insert element in first block
					currBlock.values.doAdd(pos, element);
					currBlockEnd = currBlockStart+blockLen+1;
					modify(currNode, 1);
				} else {
					// Insert element in second block
					currNode = currNode.next();
					modify(currNode, 1);
					currBlock = currNode.block;
					currBlock.values.doAdd(pos-blockLen, element);
					currBlockStart += blockLen;
					currBlockEnd++;
				}
			}
		}
		size++;

		if (DUMP) dump();
		if (CHECK) check();

		return true;
	}

	/**
	 * Modify relativePosition of all nodes starting from the specified node.
	 *
	 * @param node
	 * @param modify
	 */
	private void modify(BlockNode<E> node, int modify) {
		if (node == currNode) {
			modify += currModify;
			currModify = 0;
		} else {
			releaseBlock();
		}
		if (modify == 0) {
			return;
		}

		if (node.relativePosition < 0) {
			// Left node
			BlockNode<E> leftNode = node.getLeftSubTree();
			if (leftNode != null) {
				leftNode.relativePosition -= modify;
			}
			BlockNode<E> pp = node.parent;
			assert(pp.getLeftSubTree() == node);
			boolean parentRight = true;
			while (true) {
				BlockNode<E> p = pp.parent;
				if (p == null) {
					break;
				}
				boolean pRight = (p.getLeftSubTree() == pp);
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
			if (parentRight) {
				root.relativePosition += modify;
			}
		} else {
			// Right node
			node.relativePosition += modify;
			BlockNode<E> leftNode = node.getLeftSubTree();
			if (leftNode != null) {
				leftNode.relativePosition -= modify;
			}
			BlockNode<E> parent = node.parent;
			if (parent != null) {
				assert(parent.getRightSubTree() == node);
				boolean parentLeft = true;
				while (true) {
					BlockNode<E> p = parent.parent;
					if (p == null) {
						break;
					}
					boolean pLeft = (p.getRightSubTree() == parent);
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
				if (!parentLeft) {
					root.relativePosition += modify;
				}
			}
		}
	}

	private BlockNode<E> doRemove(BlockNode<E> node) {
		BlockNode<E> p = node.parent;
		BlockNode<E> newNode = node.removeSelf();
		BlockNode<E> n = newNode;
		while (p != null) {
			assert(p.left == node || p.right == node);
			if (p.left == node) {
				p.left = newNode;
			} else {
				p.right = newNode;
			}
			node = p;
			node.recalcHeight();
			newNode = node.balance();
			p = newNode.parent;
		}
		root = newNode;
		return n;
	}

	@Override
	protected boolean doAddAll(int index, E[] array) {
		if (array.length == 0) {
			return false;
		}
		if (index == -1) {
		    index = size;
		}
		check();
		int oldSize = size;

		if (array.length == 1) {
			return doAdd(index, array[0]);
		}

		int addPos = getBlockIndex(index, true, 0);
		Block<E> addBlock = currBlock;
		int space = blockSize - addBlock.size();

		int addLen = array.length;
		if (addLen <= space) {
			// All elements can be added to current block
			currBlock.values.addAll(addPos, array);
			modify(currNode, addLen);
			size += addLen;
			currBlockEnd += addLen;

		} else {
			if (index == size) {
				// Add elements at end
				for (int i=0; i<space; i++) {
					currBlock.values.add(addPos, array[i]);
				}
				modify(currNode, space);

				int done = space;
				int todo = addLen-space;
				while (todo > 0) {
					Block<E> nextBlock = new Block<E>(blockSize);
					int add = Math.min(todo, blockSize);
					for(int i=0; i<add; i++) {
						nextBlock.values.add(i, array[done+i]);
					}
					done += add;
					todo -= add;
					addBlock(size+done, nextBlock);
				}

				size += addLen;
				currNode = currNode.next();
				currBlock = currNode.block;
				currBlockStart = currBlockEnd+space;
				currBlockEnd = currBlockStart+addLen-space;

			} else if (index == 0) {
				// Add elements at head
				assert(addPos == 0);
				for (int i=0; i<space; i++) {
					currBlock.values.add(addPos, array[addLen-space+i]);
				}
				modify(currNode, space);

				int done = space;
				int todo = addLen-space;
				while (todo > 0) {
					Block<E> nextBlock = new Block<E>(blockSize);
					int add = Math.min(todo, blockSize);
					for(int i=0; i<add; i++) {
						nextBlock.values.add(i, array[done+i]);
					}
					done += add;
					todo -= add;
					addBlock(0, nextBlock);
				}

				size += addLen;
				currNode = currNode.previous();
				currBlock = currNode.block;
				currBlockStart = 0;
				currBlockEnd = addLen-space;

			} else {
				// Add elements to several blocks
				// Handle first block
				GapList<E> list = GapList.create(array);
				int remove = currBlock.values.size()-addPos;
				if (remove > 0) {
					list.addAll(currBlock.values.getAll(addPos, remove));
					currBlock.values.remove(addPos, remove);
					modify(currNode, -remove);
					size -= remove;
					currBlockEnd -= remove;
				}

				int s = currBlock.values.size() + list.size();
				int numBlocks = (s-1)/blockSize+1;
				assert(numBlocks > 1);
				int has = currBlock.values.size();
				int should = s / numBlocks;
				int start = 0;
				int end = 0;
				if (has < should) {
					// Elements must be added to first block
					int add = should-has;
					List<E> sublist = list.getAll(0, add);
					currBlock.values.addAll(addPos, sublist);
					modify(currNode, add);
					start += add;
					assert(currBlock.values.size() == should);
					s -= should;
					numBlocks--;
					size += add;
					currBlockEnd += add;
					end = currBlockEnd;

				} else if (has > should) {
					// Elements must be moved from first to second block
					Block<E> nextBlock = new Block<E>(blockSize);
					int move = has-should;
					nextBlock.values.addAll(currBlock.values.getAll(currBlock.values.size()-move, move));
					currBlock.values.remove(currBlock.values.size()-move, move);
					modify(currNode, -move);
					assert(currBlock.values.size() == should);
					s -= should;
					numBlocks--;
					currBlockEnd -= move;
					end = currBlockEnd;

					should = s / numBlocks;
					int add = should-move;
					assert(add >= 0);
					List<E> sublist = list.getAll(0, add);
					nextBlock.values.addAll(move, sublist);
					start += add;
					assert(nextBlock.values.size() == should);
					s -= should;

					numBlocks--;
					size += add;
					end += add;
					addBlock(end, nextBlock);

				} else {
					s -= should;
					numBlocks--;
				}
				check();

				while (numBlocks > 0) {
					int add = s / numBlocks;
					assert(add > 0);
					List<E> sublist = list.getAll(start, add);
					Block<E> nextBlock = new Block<E>();
					nextBlock.values.init(sublist);
					start += add;
					assert(nextBlock.values.size() == add);
					s -= add;
					end += add;
					addBlock(end, nextBlock);
					size += add;
					numBlocks--;
					check();
				}
			}
		}

		assert(oldSize + addLen == size);
		check();

		return true;
	}

	@Override
	protected void doClear() {
		root = null;
		currBlock = null;
		currBlockStart = 0;
		currBlockEnd = 0;
		currModify = 0;
		currNode = null;
		size = 0;

		doInit(blockSize, 0);
	}

	@Override
	protected void doRemoveAll(int index, int len) {
		// Handle special cases
		if (len == 0) {
			return;
		}
		if (index == 0 && len == size) {
			doClear();
			return;
		}
		if (len == 1) {
			doRemove(index);
			return;
		}

		// Remove range
		int l = len;
		int startPos = getBlockIndex(index, true, 0);
		BlockNode<E> startNode = currNode;
		int endPos = getBlockIndex(index+len-1, true, 0);
		BlockNode<E> endNode = currNode;

		if (startNode == endNode) {
			// Delete from single block
			getBlockIndex(index, true, -len);
			currBlock.values.remove(startPos, len);
			if (currBlock.values.isEmpty()) {
				BlockNode<E> oldCurrNode = currNode;
				releaseBlock();
				BlockNode<E> node = doRemove(oldCurrNode);
				merge(node);
			} else {
				currBlockEnd -= len;
				merge(currNode);
			}
			size -= len;
		} else {
			// Delete from start block
			check();
			int startLen = startNode.block.size()-startPos;
			getBlockIndex(index, true, -startLen); // TODO should that be modify?
			startNode.block.values.remove(startPos, startLen);
			assert(startNode == currNode);
			if (currBlock.values.isEmpty()) {
				releaseBlock();
				doRemove(startNode);
				startNode = null;
			}
			len -= startLen;
			size -= startLen;
			//check();

			while (len > 0) {
				currNode = null;
				getBlockIndex(index, true, 0);
				int s = currBlock.size();
				if (s <= len) {
					modify(currNode, -s);
					BlockNode<E> oldCurrNode = currNode;
					releaseBlock();
					doRemove(oldCurrNode);
					if (oldCurrNode == endNode) {
						endNode = null;
					}
					len -= s;
					size -= s;
					check();
				} else {
					modify(currNode, -len);
					currBlock.values.remove(0, len);
					size -= len;
					break;
				}
			}
			releaseBlock();
			check();
			getBlockIndex(index, false, 0);
			merge(currNode);
		}

		if (DUMP) dump();
		if (CHECK) check();
	}

	void merge(BlockNode<E> node) {
		if (node == null) {
			return;
		}

		final int minBlockSize = Math.max(blockSize/3, 1);
		if (node.block.values.size() >= minBlockSize) {
			return;
		}

		BlockNode<E> oldCurrNode = node;
		BlockNode<E> leftNode = node.previous();
		if (leftNode != null && leftNode.block.size() < minBlockSize) {
			// Merge with left block
		    int len = node.block.size();
		    int dstSize = leftNode.getBlock().size();
            for (int i=0; i<len; i++) {
                leftNode.block.values.add(null); // TODO Add method to GapList
            }
			GapList.copy(node.block.values, 0, leftNode.block.values, dstSize, len);
			assert(leftNode.block.values.size() <= blockSize);

			modify(leftNode, +len);
			modify(oldCurrNode, -len);
			releaseBlock();
			doRemove(oldCurrNode);

		} else {
			BlockNode<E> rightNode = node.next();
			if (rightNode != null && rightNode.block.size() < minBlockSize) {
				// Merge with right block
			    int len = node.block.size();
	            for (int i=0; i<len; i++) {
	            	rightNode.block.values.add(0, null);
	            }
				GapList.copy(node.block.values, 0, rightNode.block.values, 0, len);
				assert(rightNode.block.values.size() <= blockSize);

				modify(rightNode, +len);
				modify(oldCurrNode, -len);
				releaseBlock();
				doRemove(oldCurrNode);
			}
		}
	}

	protected E doRemove(int index) {
		int pos = getBlockIndex(index, true, -1);
		E oldElem = currBlock.values.doRemove(pos);
		currBlockEnd--;

		final int minBlockSize = Math.max(blockSize/3, 1);
		if (currBlock.size() < minBlockSize) {
			if (currBlock.size() == 0) {
				if (!isOnlyRootBlock()) {
	    			BlockNode<E> oldCurrNode = currNode;
    				releaseBlock();
					doRemove(oldCurrNode);
				}
			} else if (index != 0 && index != size-1) {
				merge(currNode);
			}
		}
		size--;

		if (DUMP) dump();
		if (CHECK) check();

		return oldElem;
	}

	private void dump() {
	}

	/**/

    @Override
    public BigList<E> unmodifiableList() {
        // Naming as in java.util.Collections#unmodifiableList
        return new ImmutableBigList<E>(this);
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
	protected IList<E> doCreate(int capacity) {
		if (capacity <= blockSize) {
			return new BigList<E>(this.blockSize);
		} else {
			return new BigList<E>(this.blockSize, capacity);
		}
	}

	@Override
	public void sort(int index, int len, Comparator<? super E> comparator) {
    	checkRange(index, len);

    	if (isOnlyRootBlock()) {
    		currBlock.values.sort(index, len, comparator);
    	} else {
    		MergeSort.sort(this, comparator, index, index+len);
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

    public BlockNode<E> access(final int index, int modify) {
        return root.access(index, modify, false);
    }

    //-----------------------------------------------------------------------
    /**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */

    public void addBlock(int index, Block<E> obj) {
        if (root == null) {
            root = new BlockNode<E>(null, index, obj, null, null);
        } else {
            root = root.insert(index, obj);
            root.parent = null;
        }
    }

    /**
     * Removes the element at the specified index.
     *
     * @param index  the index to remove
     * @return the previous object at that index
     */
    public void removeBlock(int index) {
        root = root.remove(index);
    }

	// --- Serialization ---

    /**
     * Serialize a BigList object.
     *
     * @serialData The length of the array backing the <tt>GapList</tt>
     *             instance is emitted (int), followed by all of its elements
     *             (each an <tt>Object</tt>) in the proper order.
     * @param oos  output stream for serialization
     * @throws 	   IOException if serialization fails
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeInt(blockSize);
	    int size = size();
        oos.writeInt(size);

        for (int i=0; i<size; i++) {
        	oos.writeObject(doGet(i));
        }
    }

    /**
     * Deserialize a BigList object.
 	 *
     * @param ois  input stream for serialization
     * @throws 	   IOException if serialization fails
     * @throws 	   ClassNotFoundException if serialization fails
     */
    @SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        int blockSize = ois.readInt();
        int size = ois.readInt();
        int firstBlockSize = (size <= blockSize) ? size : -1;
        doInit(blockSize, firstBlockSize);

        for (int i=0; i<size; i++) {
            add((E) ois.readObject());
        }
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
    class BlockNode<E> {
    	BlockNode<E> parent;
        /** The left child node or the predecessor if {@link #leftIsPrevious}.*/
        BlockNode<E> left;
        /** Flag indicating that left reference is not a subtree but the predecessor. */
        boolean leftIsPrevious;
        /** The right child node or the successor if {@link #rightIsNext}. */
        BlockNode<E> right;
        /** Flag indicating that right reference is not a subtree but the successor. */
        boolean rightIsNext;
        /** How many levels of left/right are below this one. */
        int height;
        /** The relative position, root holds absolute position. */
        int relativePosition;
        /** The stored block */
        Block<E> block;

        /**
         * Constructs a new node with a relative position.
         *
         * @param relativePosition  the relative position of the node
         * @param obj  the value for the node
         * @param rightFollower the node with the value following this one
         * @param leftFollower the node with the value leading this one
         */
        private BlockNode(BlockNode<E> parent, final int relativePosition, final Block<E> block,
                        final BlockNode<E> rightFollower, final BlockNode<E> leftFollower) {
        	this.parent = parent;
            this.relativePosition = relativePosition;
            this.block = block;
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
        public Block<E> getBlock() {
            return block;
        }

        /**
         * Sets the value.
         *
         * @param obj  the value to store
         */
        public void setBlock(Block<E> obj) {
            this.block = obj;
        }

        private BlockNode<E> access(final int index, int modify, boolean wasLeft) {
        	assert(index >= 0);
        	if (relativePosition == 0) {
        		if (modify != 0) {
        			relativePosition += modify;
        		}
        		return this;
        	}
        	if (currBlockEnd == 0) {
        		currBlockEnd = relativePosition; // root
        	}
        	BlockNode<E> leftNode = getLeftSubTree();
        	int leftIndex = currBlockEnd-block.size();
        	assert(leftIndex >= 0);
        	if (index >= leftIndex && index < currBlockEnd) {
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
        	if (index < currBlockEnd) {
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
                currBlockEnd += nextNode.relativePosition;
                return nextNode.access(index, modify, wasLeft);
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
                currBlockEnd += nextNode.relativePosition;
                return nextNode.access(index, modify, wasLeft);
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
        private BlockNode<E> insert(int index, Block<E> obj) {
        	assert(relativePosition != 0);
            final int indexRelativeToMe = index - relativePosition;

            if (indexRelativeToMe < 0) {
                return insertOnLeft(indexRelativeToMe, obj);
            } else {
            	return insertOnRight(indexRelativeToMe, obj);
            }
        }

        private BlockNode<E> insertOnLeft(int indexRelativeToMe, Block<E> obj) {
            if (getLeftSubTree() == null) {
            	int pos;
            	if (relativePosition >= 0) {
            		pos = -relativePosition;
            	} else {
            		pos = -block.size();
            	}
                setLeft(new BlockNode<E>(this, pos, obj, this, left), null);
            } else {
                setLeft(left.insert(indexRelativeToMe, obj), null);
            }
            if (relativePosition >= 0) {
                relativePosition += obj.size();
            }
            final BlockNode<E> ret = balance();
            recalcHeight();
            return ret;
        }

        private BlockNode<E> insertOnRight(int indexRelativeToMe, Block<E> obj) {
            if (getRightSubTree() == null) {
                setRight(new BlockNode<E>(this, obj.size(), obj, right, this), null);
            } else {
                setRight(right.insert(indexRelativeToMe, obj), null);
            }
            if (relativePosition < 0) {
                relativePosition -= obj.size();
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
                //relativePosition++;
            }
            recalcHeight();
            return balance();
        }

        private BlockNode<E> removeMin(int size) {
            if (getLeftSubTree() == null) {
                return removeSelf();
            }
            setLeft(left.removeMin(size), left.left);
            if (relativePosition > 0) {
                relativePosition -= size;
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
        		assert(p != n);
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
                	left.relativePosition += relativePosition;
                }
                left.max().setRight(null, right);
                return left;
            }
            if (getLeftSubTree() == null) {
            	if (relativePosition < 0) {
                    right.relativePosition += relativePosition - (relativePosition < 0 ? 0 : 1);
            	}
                right.min().setLeft(null, left);
                return right;
            }

            if (heightRightMinusLeft() > 0) {
                // more on the right, so delete from the right
                final BlockNode<E> rightMin = right.min();
                block = rightMin.block;
                int bs = block.size();
                if (leftIsPrevious) {
                    left = rightMin.left;
                }
                right = right.removeMin(bs);
                relativePosition += bs;
           		left.relativePosition -= bs;
            } else {
                // more on the left or equal, so delete from the left
                final BlockNode<E> leftMax = left.max();
                block = leftMax.block;
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
                } else {
                	if (left.relativePosition == 0) {
                		left.relativePosition = -1;
                	}
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

            BlockNode<E> p = this.parent;
            setRight(movedNode, newTop);
            newTop.setLeft(this, null);
            newTop.parent = p;
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

            BlockNode<E> p = this.parent;
            setLeft(movedNode, newTop);
            newTop.setRight(this, null);
            newTop.parent = p;
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
        	assert(node != this && previous != this);
            leftIsPrevious = node == null;
            if (leftIsPrevious) {
            	left = previous;
            } else {
            	left = node;
            	left.parent = this;
            }
            recalcHeight();
        }

        /**
         * Sets the right field to the node, or the next node if that is null
         *
         * @param node  the new right subtree node
         * @param next  the next node in the linked list
         */
        private void setRight(final BlockNode<E> node, final BlockNode<E> next) {
        	assert(node != this && next != this);
            rightIsNext = node == null;
            if (rightIsNext) {
            	right = next;
            } else {
            	right = node;
            	right.parent = this;
            }
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
                .append(getRightSubTree() != null)
                .append(',')
                .append(block)
                .append(',')
                .append(getRightSubTree() != null)
                .append(", height ")
                .append(height)
                .append(" )")
                .toString();
        }
    }

}
