package org.magicwerk.brownies.collections.primitive;
import org.magicwerk.brownies.collections.helper.ArraysHelper;
import org.magicwerk.brownies.collections.helper.primitive.BooleanBinarySearch;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.BigList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.magicwerk.brownies.collections.helper.primitive.BooleanMergeSort;

/**
 * BooleanBigList is a list optimized for storing large number of elements.
 * It stores the elements in fixed size blocks and the blocks itself are maintained in a tree for fast access.
 * It also offers specialized methods for bulk processing of elements.
 * Also copying a BooleanBigList is efficiently possible as its implemented using a copy-on-write approach.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * Due to data caching used for exploiting locality of reference, performance can decrease if BooleanBigList is
 * accessed by several threads at different positions.
 * </p>
 *
 * @author Thomas Mauch
 * @version $Id: BooleanBigList.java 2507 2014-10-15 00:08:21Z origo $
 */
/**
 *
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class BooleanBigList extends IBooleanList {
	public static IBooleanList of(boolean[] values) {
		return new ImmutableBooleanListArrayPrimitive(values);
	}

	public static IBooleanList of(Boolean[] values) {
		return new ImmutableBooleanListArrayWrapper(values);
	}

	public static IBooleanList of(List<Boolean> values) {
		return new ImmutableBooleanListList(values);
	}

    static class ImmutableBooleanListArrayPrimitive extends ImmutableBooleanList {
    	boolean[] values;

    	public ImmutableBooleanListArrayPrimitive(boolean[] values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		protected boolean doGet(int index) {
			return values[index];
		}
    }

    static class ImmutableBooleanListArrayWrapper extends ImmutableBooleanList {
    	Boolean[] values;

    	public ImmutableBooleanListArrayWrapper(Boolean[] values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		protected boolean doGet(int index) {
			return values[index];
		}
    }

    static class ImmutableBooleanListList extends ImmutableBooleanList {
    	List<Boolean> values;

    	public ImmutableBooleanListList(List<Boolean> values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.size();
		}

		@Override
		protected boolean doGet(int index) {
			return values.get(index);
		}
    }

    protected static abstract class ImmutableBooleanList extends IBooleanList {

    	//-- Readers

		@Override
		public int capacity() {
			return size();
		}

		@Override
		public int binarySearch(int index, int len, boolean key) {
			return BooleanBinarySearch.binarySearch(this, key, index, index+len);
		}

		@Override
		public IBooleanList unmodifiableList() {
			return this;
		}

		@Override
		protected boolean getDefaultElem() {
			return false;
		}

        /**
         * Throw exception if an attempt is made to change an immutable list.
         */
        private void error() {
            throw new UnsupportedOperationException("list is immutable");
        }

        //-- Writers

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

		@Override
		protected void doClone(IBooleanList that) {
			error();
		}

		@Override
		protected boolean doSet(int index, boolean elem) {
			error();
			return false;
		}

		@Override
		protected boolean doReSet(int index, boolean elem) {
			error();
			return false;
		}

		@Override
		protected boolean doAdd(int index, boolean elem) {
			error();
			return false;
		}

		@Override
		protected void doEnsureCapacity(int minCapacity) {
			error();
		}

		@Override
		public void trimToSize() {
			error();
		}

		@Override
		protected IBooleanList doCreate(int capacity) {
			error();
			return null;
		}

		@Override
		protected void doAssign(IBooleanList that) {
			error();
		}

		@Override
		protected boolean doRemove(int index) {
			error();
			return false;
		}

		@Override
		public void sort(int index, int len) {
			error();
		}
    }

    /** UID for serialization */
    private static final long serialVersionUID = 3715838828540564836L;

    /** Default block size */
    private static int BLOCK_SIZE = 1000;

    /** Set to true for debugging during developing */
    private static final boolean CHECK = true;

    // -- EMPTY --  
    // Cannot make a static reference to the non-static type E:  
    // public static BooleanBigList EMPTY = BooleanBigList.create().unmodifiableList();  
    // Syntax error:  
    // public static  BooleanBigList EMPTY = BooleanBigList.create().unmodifiableList();  
    /** Unmodifiable empty instance */
    
    private static final BooleanBigList EMPTY = BooleanBigList.create().unmodifiableList();

    /**
     * @return unmodifiable empty instance
     */

public static  BooleanBigList EMPTY() {
    return EMPTY;
}

    /** Number of elements stored at maximum in a block */
    private int blockSize;

    /** Number of elements stored in this BooleanBigList */
    private int size;

    /** The root node in the tree */
    private BooleanBlockNode root;

    /** Current node */
    private BooleanBlockNode currNode;

    /** BooleanBlock of current node */
    private BooleanBlock currBooleanBlock;

    /** Start index of current block */
    private int currBooleanBlockStart;

    /** End index of current block */
    private int currBooleanBlockEnd;

    /** Modify value which must be applied before this block is not current any more */
    private int currModify;

    /**
     * Constructor used internally, e.g. for ImmutableBooleanBigList.
     *
     * @param copy true to copy all instance values from source,
     *             if false nothing is done
     * @param that list to copy
     */
protected BooleanBigList(boolean copy, BooleanBigList that){
    if (copy) {
        this.blockSize = that.blockSize;
        this.currBooleanBlock = that.currBooleanBlock;
        this.currBooleanBlockStart = that.currBooleanBlockStart;
        this.currBooleanBlockEnd = that.currBooleanBlockEnd;
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
     * @param        type of elements stored in the list
     */
// This separate method is needed as the varargs variant creates the list with specific size  
public static BooleanBigList create() {
    return new BooleanBigList();
}

    /**
     * Create new list with specified elements.
     *
     * @param coll      collection with element
     * @return          created list
     * @param        type of elements stored in the list
     */
public static BooleanBigList create(Collection<Boolean> coll) {
    return new BooleanBigList(coll);
}

    /**
	 * Create new list with specified elements.
	 *
	 * @param elems 	array with elements
	 * @return 			created list
	 * @param  		type of elements stored in the list
	 */
public static BooleanBigList create(boolean... elems) {
    BooleanBigList list = new BooleanBigList();
    for (boolean elem : elems) {
        list.add(elem);
    }
    return list;
}

    /**
	 * Default constructor.
	 * The default block size is used.
	 */
public BooleanBigList(){
    this(BLOCK_SIZE);
}

    /**
	 * Constructor.
	 *
	 * @param blockSize block size
	 */
public BooleanBigList(int blockSize){
    if (blockSize < 2) {
        throw new IndexOutOfBoundsException("Invalid blockSize: " + blockSize);
    }
    doInit(blockSize, -1);
}

    /**
     * Create new list with specified elements.
     *
     * @param coll      collection with element
     * @return          created list
     * @param        type of elements stored in the list
     */
public BooleanBigList(Collection<Boolean> that){
    if (that instanceof BooleanBigList) {
        doAssign((BooleanBigList) that);
        doClone((BooleanBigList) that);
    } else {
        blockSize = BLOCK_SIZE;
        currBooleanBlock = new BooleanBlock();
        addBooleanBlock(0, currBooleanBlock);
        for (Object obj : that.toArray()) {
            add((Boolean) obj);
        }
        assert (size() == that.size());
    }
}

    /**
	 * Initialize the list to be empty.
	 */
public void init() {
    clear();
}

    /**
     * Initialize the list to have the specified elements.
     *
     * @param elems	elements
     */
public void init(boolean... elems) {
    clear();
    for (boolean elem : elems) {
        add(elem);
    }
}

    /**
     * Initialize the list to have all elements in the specified collection.
     *
     * @param that	collection
     */
public void init(Collection<Boolean> that) {
    clear();
    addAll(that);
}

    /**
     * Returns block size used for this BooleanBigList.
     *
     * @return block size used for this BooleanBigList
     */
public int blockSize() {
    return blockSize;
}

    /**
	 * Internal constructor.
	 *
	 * @param blockSize			default block size
	 * @param firstBooleanBlockSize	block size of first block
	 */
private BooleanBigList(int blockSize, int firstBooleanBlockSize){
    doInit(blockSize, firstBooleanBlockSize);
}

    /**
	 * Initialize BooleanBigList.
	 *
	 * @param blockSize			default block size
	 * @param firstBooleanBlockSize	block size of first block
	 */
private void doInit(int blockSize, int firstBooleanBlockSize) {
    this.blockSize = blockSize;
    // First block will grow until it reaches blockSize   
    if (firstBooleanBlockSize <= 1) {
        currBooleanBlock = new BooleanBlock();
    } else {
        currBooleanBlock = new BooleanBlock(firstBooleanBlockSize);
    }
    addBooleanBlock(0, currBooleanBlock);
}

    /**
     * Returns a copy of this <tt>BooleanBigList</tt> instance.
     * The copy is realized by a copy-on-write approach so also really large lists can efficiently be copied.
     * This method is identical to clone() except that the result is casted to BooleanBigList.
     *
     * @return a copy of this <tt>BooleanBigList</tt> instance
	 */
@Override
public BooleanBigList copy() {
    return (BooleanBigList) super.copy();
}

    /**
     * Returns a shallow copy of this <tt>BooleanBigList</tt> instance
     * The copy is realized by a copy-on-write approach so also really large lists can efficiently be copied.
     *
     * @return a copy of this <tt>BooleanBigList</tt> instance
     */
// Only overridden to change Javadoc  
@Override
public Object clone() {
    return super.clone();
}

    @Override
protected void doAssign(IBooleanList that) {
    BooleanBigList list = (BooleanBigList) that;
    this.blockSize = list.blockSize;
    this.currBooleanBlock = list.currBooleanBlock;
    this.currBooleanBlockEnd = list.currBooleanBlockEnd;
    this.currBooleanBlockStart = list.currBooleanBlockStart;
    this.currNode = list.currNode;
    this.root = list.root;
    this.size = list.size;
}

    @Override
protected void doClone(IBooleanList that) {
    BooleanBigList bigList = (BooleanBigList) that;
    bigList.releaseBooleanBlock();
    root = copy(bigList.root);
    currNode = null;
    currModify = 0;
    if (CHECK)
        check();
}

    /**
	 * Create a copy of the specified node.
	 *
	 * @param node	source node
	 * @return		newly created copy of source
	 */
private BooleanBlockNode copy(BooleanBlockNode node) {
    BooleanBlockNode newNode = node.min();
    int index = newNode.block.size();
    BooleanBlockNode newRoot = new BooleanBlockNode(null, index, newNode.block.ref(), null, null);
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
public boolean getDefaultElem() {
    return false;
}

    @Override
protected void finalize() {
    // This list will be garbage collected, so unref all referenced blocks   
    BooleanBlockNode node = root.min();
    while (node != null) {
        node.block.unref();
        node = node.next();
    }
}

    @Override
public int size() {
    return size;
}

    /**
	 * {@inheritDoc}
	 * For BooleanBigList, always -1 is returned.
	 */
@Override
public int capacity() {
    return -1;
}

    @Override
protected boolean doGet(int index) {
    int pos = getBooleanBlockIndex(index, false, 0);
    return currBooleanBlock.values.doGet(pos);
}

    @Override
protected boolean doSet(int index, boolean elem) {
    int pos = getBooleanBlockIndex(index, true, 0);
    boolean oldElem = currBooleanBlock.values.doGet(pos);
    currBooleanBlock.values.doSet(pos, elem);
    return oldElem;
}

    @Override
protected boolean doReSet(int index, boolean elem) {
    int pos = getBooleanBlockIndex(index, true, 0);
    boolean oldElem = currBooleanBlock.values.doGet(pos);
    currBooleanBlock.values.doSet(pos, elem);
    return oldElem;
}

    /**
	 * Release current block and apply modification if pending.
	 */
private void releaseBooleanBlock() {
    if (currModify != 0) {
        int modify = currModify;
        currModify = 0;
        modify(currNode, modify);
    }
    currNode = null;
}

    /**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currBooleanBlock to remember this last used block.
	 *
	 * @param index	list index (0 <= index <= size())
	 * @return		relative index within block
	 */
private int getBooleanBlockIndex(int index, boolean write, int modify) {
    // Determine block where specified index is located and store it in currBooleanBlock   
    if (currNode != null) {
        if (index >= currBooleanBlockStart && (index < currBooleanBlockEnd || index == currBooleanBlockEnd && size == index)) {
            // currBooleanBlock is already set correctly   
            if (write) {
                if (currBooleanBlock.isShared()) {
                    currBooleanBlock.unref();
                    currBooleanBlock = new BooleanBlock(currBooleanBlock);
                    currNode.setBooleanBlock(currBooleanBlock);
                }
            }
            currModify += modify;
            return index - currBooleanBlockStart;
        }
        releaseBooleanBlock();
    }
    boolean done = false;
    if (index == size) {
        if (currNode == null || currBooleanBlockEnd != size) {
            currNode = root.max();
            currBooleanBlock = currNode.getBooleanBlock();
            currBooleanBlockEnd = size;
            currBooleanBlockStart = size - currBooleanBlock.size();
        }
        if (modify != 0) {
            currNode.relativePosition += modify;
            BooleanBlockNode leftNode = currNode.getLeftSubTree();
            if (leftNode != null) {
                leftNode.relativePosition -= modify;
            }
        }
        done = true;
    } else if (index == 0) {
        if (currNode == null || currBooleanBlockStart != 0) {
            currNode = root.min();
            currBooleanBlock = currNode.getBooleanBlock();
            currBooleanBlockEnd = currBooleanBlock.size();
            currBooleanBlockStart = 0;
        }
        if (modify != 0) {
            root.relativePosition += modify;
        }
        done = true;
    }
    if (!done) {
        // Reset currBooleanBlockEnd, it will be then set by access()   
        currBooleanBlockEnd = 0;
        currNode = doGetBooleanBlock(index, modify);
        currBooleanBlock = currNode.getBooleanBlock();
        currBooleanBlockStart = currBooleanBlockEnd - currBooleanBlock.size();
    }
    assert (index >= currBooleanBlockStart && index <= currBooleanBlockEnd);
    if (write) {
        if (currBooleanBlock.isShared()) {
            currBooleanBlock.unref();
            currBooleanBlock = new BooleanBlock(currBooleanBlock);
            currNode.setBooleanBlock(currBooleanBlock);
        }
    }
    return index - currBooleanBlockStart;
}

    /**
	 * @return true if there is only the root block, false otherwise
	 */
private boolean isOnlyRootBooleanBlock() {
    return root.left == null && root.right == null;
}

    private BooleanBlockNode doGetBooleanBlock(int index, int modify) {
    return root.access(this, index, modify, false);
}

    /**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */
private void addBooleanBlock(int index, BooleanBlock obj) {
    if (root == null) {
        root = new BooleanBlockNode(null, index, obj, null, null);
    } else {
        root = root.insert(index, obj);
        root.parent = null;
    }
}

    @Override
protected boolean doAdd(int index, boolean element) {
    if (index == -1) {
        index = size;
    }
    // Insert   
    int pos = getBooleanBlockIndex(index, true, 1);
    // If there is still place in the current block: insert in current block   
    int maxSize = (index == size || index == 0) ? blockSize * 9 / 10 : blockSize;
    // The second part of the condition is a work around to handle the case of insertion as position 0 correctly   
    // where blockSize() is 2 (the new block would then be added after the current one)   
    if (currBooleanBlock.size() < maxSize || (currBooleanBlock.size() == 1 && currBooleanBlock.size() < blockSize)) {
        currBooleanBlock.values.doAdd(pos, element);
        currBooleanBlockEnd++;
    } else {
        // No place any more in current block   
        BooleanBlock newBooleanBlock = new BooleanBlock(blockSize);
        if (index == size) {
            // Insert new block at tail   
            newBooleanBlock.values.doAdd(0, element);
            // Subtract 1 because getBooleanBlockIndex() has already added 1   
            modify(currNode, -1);
            addBooleanBlock(size + 1, newBooleanBlock);
            BooleanBlockNode lastNode = currNode.next();
            currNode = lastNode;
            currBooleanBlock = currNode.block;
            currBooleanBlockStart = currBooleanBlockEnd;
            currBooleanBlockEnd++;
        } else if (index == 0) {
            // Insert new block at head   
            newBooleanBlock.values.doAdd(0, element);
            // Subtract 1 because getBooleanBlockIndex() has already added 1   
            modify(currNode, -1);
            addBooleanBlock(1, newBooleanBlock);
            BooleanBlockNode firstNode = currNode.previous();
            currNode = firstNode;
            currBooleanBlock = currNode.block;
            currBooleanBlockStart = 0;
            currBooleanBlockEnd = 1;
        } else {
            // Split block for insert   
            int nextBooleanBlockLen = blockSize / 2;
            int blockLen = blockSize - nextBooleanBlockLen;
            newBooleanBlock.values.init(nextBooleanBlockLen, false);
            BooleanGapList.copy(currBooleanBlock.values, blockLen, newBooleanBlock.values, 0, nextBooleanBlockLen);
            currBooleanBlock.values.remove(blockLen, blockSize - blockLen);
            // Subtract 1 more because getBooleanBlockIndex() has already added 1   
            modify(currNode, -nextBooleanBlockLen - 1);
            addBooleanBlock(currBooleanBlockEnd - nextBooleanBlockLen, newBooleanBlock);
            if (pos < blockLen) {
                // Insert element in first block   
                currBooleanBlock.values.doAdd(pos, element);
                currBooleanBlockEnd = currBooleanBlockStart + blockLen + 1;
                modify(currNode, 1);
            } else {
                // Insert element in second block   
                currNode = currNode.next();
                modify(currNode, 1);
                currBooleanBlock = currNode.block;
                currBooleanBlock.values.doAdd(pos - blockLen, element);
                currBooleanBlockStart += blockLen;
                currBooleanBlockEnd++;
            }
        }
    }
    size++;
    if (CHECK)
        check();
    return true;
}

    /**
	 * Modify relativePosition of all nodes starting from the specified node.
	 *
	 * @param node		node whose position value must be changed
	 * @param modify	modify value (>0 for add, <0 for delete)
	 */
private void modify(BooleanBlockNode node, int modify) {
    if (node == currNode) {
        modify += currModify;
        currModify = 0;
    } else {
        releaseBooleanBlock();
    }
    if (modify == 0) {
        return;
    }
    if (node.relativePosition < 0) {
        // Left node   
        BooleanBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        BooleanBlockNode pp = node.parent;
        assert (pp.getLeftSubTree() == node);
        boolean parentRight = true;
        while (true) {
            BooleanBlockNode p = pp.parent;
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
        BooleanBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        BooleanBlockNode parent = node.parent;
        if (parent != null) {
            assert (parent.getRightSubTree() == node);
            boolean parentLeft = true;
            while (true) {
                BooleanBlockNode p = parent.parent;
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

    private BooleanBlockNode doRemove(BooleanBlockNode node) {
    BooleanBlockNode p = node.parent;
    BooleanBlockNode newNode = node.removeSelf();
    BooleanBlockNode n = newNode;
    while (p != null) {
        assert (p.left == node || p.right == node);
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
protected boolean doAddAll(int index, boolean[] array) {
    if (array.length == 0) {
        return false;
    }
    if (index == -1) {
        index = size;
    }
    if (CHECK)
        check();
    int oldSize = size;
    if (array.length == 1) {
        return doAdd(index, array[0]);
    }
    int addPos = getBooleanBlockIndex(index, true, 0);
    BooleanBlock addBooleanBlock = currBooleanBlock;
    int space = blockSize - addBooleanBlock.size();
    int addLen = array.length;
    if (addLen <= space) {
        // All elements can be added to current block   
        currBooleanBlock.values.addAll(addPos, array);
        modify(currNode, addLen);
        size += addLen;
        currBooleanBlockEnd += addLen;
    } else {
        if (index == size) {
            // Add elements at end   
            for (int i = 0; i < space; i++) {
                currBooleanBlock.values.add(addPos + i, array[i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                BooleanBlock nextBooleanBlock = new BooleanBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextBooleanBlock.values.add(i, array[done + i]);
                }
                done += add;
                todo -= add;
                addBooleanBlock(size + done, nextBooleanBlock);
                currNode = currNode.next();
            }
            size += addLen;
            currBooleanBlock = currNode.block;
            currBooleanBlockEnd = size;
            currBooleanBlockStart = currBooleanBlockEnd - currBooleanBlock.size();
        } else if (index == 0) {
            // Add elements at head   
            assert (addPos == 0);
            for (int i = 0; i < space; i++) {
                currBooleanBlock.values.add(addPos + i, array[addLen - space + i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                BooleanBlock nextBooleanBlock = new BooleanBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextBooleanBlock.values.add(i, array[addLen - done - add + i]);
                }
                done += add;
                todo -= add;
                addBooleanBlock(0, nextBooleanBlock);
                currNode = currNode.previous();
            }
            size += addLen;
            currBooleanBlock = currNode.block;
            currBooleanBlockStart = 0;
            currBooleanBlockEnd = currBooleanBlock.size();
        } else {
            // Add elements in the middle   
            // Split first block to remove tail elements if necessary   
            BooleanGapList list = BooleanGapList.create(array);
            int remove = currBooleanBlock.values.size() - addPos;
            if (remove > 0) {
                list.addAll(currBooleanBlock.values.getAll(addPos, remove));
                currBooleanBlock.values.remove(addPos, remove);
                modify(currNode, -remove);
                size -= remove;
                currBooleanBlockEnd -= remove;
            }
            // Calculate how many blocks we need for the elements   
            int numElems = currBooleanBlock.values.size() + list.size();
            int numBooleanBlocks = (numElems - 1) / blockSize + 1;
            assert (numBooleanBlocks > 1);
            int has = currBooleanBlock.values.size();
            int should = numElems / numBooleanBlocks;
            int listPos = 0;
            if (has < should) {
                // Elements must be added to first block   
                int add = should - has;
                IBooleanList sublist = list.getAll(0, add);
                listPos += add;
                currBooleanBlock.values.addAll(addPos, sublist);
                modify(currNode, add);
                assert (currBooleanBlock.values.size() == should);
                numElems -= should;
                numBooleanBlocks--;
                size += add;
                currBooleanBlockEnd += add;
            } else if (has > should) {
                // Elements must be moved from first to second block   
                BooleanBlock nextBooleanBlock = new BooleanBlock(blockSize);
                int move = has - should;
                nextBooleanBlock.values.addAll(currBooleanBlock.values.getAll(currBooleanBlock.values.size() - move, move));
                currBooleanBlock.values.remove(currBooleanBlock.values.size() - move, move);
                modify(currNode, -move);
                assert (currBooleanBlock.values.size() == should);
                numElems -= should;
                numBooleanBlocks--;
                currBooleanBlockEnd -= move;
                should = numElems / numBooleanBlocks;
                int add = should - move;
                assert (add >= 0);
                IBooleanList sublist = list.getAll(0, add);
                nextBooleanBlock.values.addAll(move, sublist);
                listPos += add;
                assert (nextBooleanBlock.values.size() == should);
                numElems -= should;
                numBooleanBlocks--;
                size += add;
                addBooleanBlock(currBooleanBlockEnd, nextBooleanBlock);
                currNode = currNode.next();
                currBooleanBlock = currNode.block;
                assert (currBooleanBlock == nextBooleanBlock);
                assert (currBooleanBlock.size() == add + move);
                currBooleanBlockStart = currBooleanBlockEnd;
                currBooleanBlockEnd += add + move;
            } else {
                // BooleanBlock already has the correct size   
                numElems -= should;
                numBooleanBlocks--;
            }
            if (CHECK)
                check();
            while (numBooleanBlocks > 0) {
                int add = numElems / numBooleanBlocks;
                assert (add > 0);
                IBooleanList sublist = list.getAll(listPos, add);
                listPos += add;
                BooleanBlock nextBooleanBlock = new BooleanBlock();
                nextBooleanBlock.values.addAll(sublist);
                assert (nextBooleanBlock.values.size() == add);
                numElems -= add;
                addBooleanBlock(currBooleanBlockEnd, nextBooleanBlock);
                currNode = currNode.next();
                currBooleanBlock = currNode.block;
                assert (currBooleanBlock == nextBooleanBlock);
                assert (currBooleanBlock.size() == add);
                currBooleanBlockStart = currBooleanBlockEnd;
                currBooleanBlockEnd += add;
                size += add;
                numBooleanBlocks--;
                if (CHECK)
                    check();
            }
        }
    }
    assert (oldSize + addLen == size);
    if (CHECK)
        check();
    return true;
}

    @Override
protected void doClear() {
    root = null;
    currBooleanBlock = null;
    currBooleanBlockStart = 0;
    currBooleanBlockEnd = 0;
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
    int startPos = getBooleanBlockIndex(index, true, 0);
    BooleanBlockNode startNode = currNode;
    int endPos = getBooleanBlockIndex(index + len - 1, true, 0);
    BooleanBlockNode endNode = currNode;
    if (startNode == endNode) {
        // Delete from single block   
        getBooleanBlockIndex(index, true, -len);
        currBooleanBlock.values.remove(startPos, len);
        if (currBooleanBlock.values.isEmpty()) {
            BooleanBlockNode oldCurrNode = currNode;
            releaseBooleanBlock();
            BooleanBlockNode node = doRemove(oldCurrNode);
            merge(node);
        } else {
            currBooleanBlockEnd -= len;
            merge(currNode);
        }
        size -= len;
    } else {
        // Delete from start block   
        if (CHECK)
            check();
        int startLen = startNode.block.size() - startPos;
        getBooleanBlockIndex(index, true, -startLen);
        // TODO should that be modify?   
        startNode.block.values.remove(startPos, startLen);
        assert (startNode == currNode);
        if (currBooleanBlock.values.isEmpty()) {
            releaseBooleanBlock();
            doRemove(startNode);
            startNode = null;
        }
        len -= startLen;
        size -= startLen;
        while (len > 0) {
            currNode = null;
            getBooleanBlockIndex(index, true, 0);
            int s = currBooleanBlock.size();
            if (s <= len) {
                modify(currNode, -s);
                BooleanBlockNode oldCurrNode = currNode;
                releaseBooleanBlock();
                doRemove(oldCurrNode);
                if (oldCurrNode == endNode) {
                    endNode = null;
                }
                len -= s;
                size -= s;
                if (CHECK)
                    check();
            } else {
                modify(currNode, -len);
                currBooleanBlock.values.remove(0, len);
                size -= len;
                break;
            }
        }
        releaseBooleanBlock();
        if (CHECK)
            check();
        getBooleanBlockIndex(index, false, 0);
        merge(currNode);
    }
    if (CHECK)
        check();
}

    /**
	 * Merge the specified node with the left or right neighbor if possible.
	 *
	 * @param node	candidate node for merge
	 */
private void merge(BooleanBlockNode node) {
    if (node == null) {
        return;
    }
    final int minBooleanBlockSize = Math.max(blockSize / 3, 1);
    if (node.block.values.size() >= minBooleanBlockSize) {
        return;
    }
    BooleanBlockNode oldCurrNode = node;
    BooleanBlockNode leftNode = node.previous();
    if (leftNode != null && leftNode.block.size() < minBooleanBlockSize) {
        // Merge with left block   
        int len = node.block.size();
        int dstSize = leftNode.getBooleanBlock().size();
        for (int i = 0; i < len; i++) {
            leftNode.block.values.add(false);
        }
        BooleanGapList.copy(node.block.values, 0, leftNode.block.values, dstSize, len);
        assert (leftNode.block.values.size() <= blockSize);
        modify(leftNode, +len);
        modify(oldCurrNode, -len);
        releaseBooleanBlock();
        doRemove(oldCurrNode);
    } else {
        BooleanBlockNode rightNode = node.next();
        if (rightNode != null && rightNode.block.size() < minBooleanBlockSize) {
            // Merge with right block   
            int len = node.block.size();
            for (int i = 0; i < len; i++) {
                rightNode.block.values.add(0, false);
            }
            BooleanGapList.copy(node.block.values, 0, rightNode.block.values, 0, len);
            assert (rightNode.block.values.size() <= blockSize);
            modify(rightNode, +len);
            modify(oldCurrNode, -len);
            releaseBooleanBlock();
            doRemove(oldCurrNode);
        }
    }
}

    protected boolean doRemove(int index) {
    int pos = getBooleanBlockIndex(index, true, -1);
    boolean oldElem = currBooleanBlock.values.doRemove(pos);
    currBooleanBlockEnd--;
    final int minBooleanBlockSize = Math.max(blockSize / 3, 1);
    if (currBooleanBlock.size() < minBooleanBlockSize) {
        if (currBooleanBlock.size() == 0) {
            if (!isOnlyRootBooleanBlock()) {
                BooleanBlockNode oldCurrNode = currNode;
                releaseBooleanBlock();
                doRemove(oldCurrNode);
            }
        } else if (index != 0 && index != size - 1) {
            merge(currNode);
        }
    }
    size--;
    if (CHECK)
        check();
    return oldElem;
}

    @Override
public BooleanBigList unmodifiableList() {
    // Naming as in java.util.Collections#unmodifiableList   
    return new ImmutableBooleanBigList(this);
}

    @Override
protected void doEnsureCapacity(int minCapacity) {
    if (isOnlyRootBooleanBlock()) {
        if (minCapacity > blockSize) {
            minCapacity = blockSize;
        }
        currBooleanBlock.values.doEnsureCapacity(minCapacity);
    }
}

    /**
     * Pack as many elements in the blocks as allowed.
     * An application can use this operation to minimize the storage of an instance.
     */
@Override
public void trimToSize() {
    doModify();
    if (isOnlyRootBooleanBlock()) {
        currBooleanBlock.values.trimToSize();
    } else {
        BooleanBigList newList = new BooleanBigList(blockSize);
        BooleanBlockNode node = root.min();
        while (node != null) {
            newList.addAll(node.block.values);
            remove(0, node.block.values.size());
            node = node.next();
        }
        doAssign(newList);
    }
}

    @Override
protected IBooleanList doCreate(int capacity) {
    if (capacity <= blockSize) {
        return new BooleanBigList(this.blockSize);
    } else {
        return new BooleanBigList(this.blockSize, capacity);
    }
}

    @Override
public void sort(int index, int len) {
    checkRange(index, len);
    if (isOnlyRootBooleanBlock()) {
        currBooleanBlock.values.sort(index, len);
    } else {
        BooleanMergeSort.sort(this, index, index + len);
    }
}

    @Override
public int binarySearch(int index, int len, boolean key) {
    checkRange(index, len);
    if (isOnlyRootBooleanBlock()) {
        return currBooleanBlock.values.binarySearch(key);
    } else {
        return BooleanBinarySearch.binarySearch(this, key, 0, size());
    }
}

    // --- Serialization ---  
/**
     * Serialize a BooleanBigList object.
     *
     * @serialData block size (int), number of elements (int), followed by all of its elements
     *             (each an <tt>Object</tt>) in the proper order
     * @param oos  output stream for serialization
     * @throws 	   IOException if serialization fails
     */
private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.writeInt(blockSize);
    int size = size();
    oos.writeInt(size);
    for (int i = 0; i < size; i++) {
        oos.writeBoolean(doGet(i));
    }
}

    /**
     * Deserialize a BooleanBigList object.
 	 *
     * @param ois  input stream for serialization
     * @throws 	   IOException if serialization fails
     * @throws 	   ClassNotFoundException if serialization fails
     */

private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    int blockSize = ois.readInt();
    int size = ois.readInt();
    int firstBooleanBlockSize = (size <= blockSize) ? size : -1;
    doInit(blockSize, firstBooleanBlockSize);
    for (int i = 0; i < size; i++) {
        add(ois.readBoolean());
    }
}

    // --- Debug checks ---  
private void checkNode(BooleanBlockNode node) {
    assert ((node.block.size() > 0 || node == root) && node.block.size() <= blockSize);
    BooleanBlockNode child = node.getLeftSubTree();
    assert (child == null || child.parent == node);
    child = node.getRightSubTree();
    assert (child == null || child.parent == node);
}

    private void checkHeight(BooleanBlockNode node) {
    BooleanBlockNode left = node.getLeftSubTree();
    BooleanBlockNode right = node.getRightSubTree();
    if (left == null) {
        if (right == null) {
            assert (node.height == 0);
        } else {
            assert (right.height == node.height - 1);
            checkHeight(right);
        }
    } else {
        if (right == null) {
            assert (left.height == node.height - 1);
        } else {
            assert (left.height == node.height - 1 || left.height == node.height - 2);
            assert (right.height == node.height - 1 || right.height == node.height - 2);
            assert (right.height == node.height - 1 || left.height == node.height - 1);
        }
        checkHeight(left);
    }
}

    private void check() {
    if (currNode != null) {
        assert (currNode.block == currBooleanBlock);
        assert (currBooleanBlockStart >= 0 && currBooleanBlockEnd <= size && currBooleanBlockStart <= currBooleanBlockEnd);
        assert (currBooleanBlockStart + currBooleanBlock.size() == currBooleanBlockEnd);
    }
    if (root == null) {
        assert (size == 0);
        return;
    }
    checkHeight(root);
    BooleanBlockNode oldCurrNode = currNode;
    int oldCurrModify = currModify;
    if (currModify != 0) {
        currNode = null;
        currModify = 0;
        modify(oldCurrNode, oldCurrModify);
    }
    BooleanBlockNode node = root;
    checkNode(node);
    int index = node.relativePosition;
    while (node.left != null) {
        node = node.left;
        checkNode(node);
        assert (node.relativePosition < 0);
        index += node.relativePosition;
    }
    BooleanBlock block = node.getBooleanBlock();
    assert (block.size() == index);
    int lastIndex = index;
    while (lastIndex < size()) {
        node = root;
        index = node.relativePosition;
        int searchIndex = lastIndex + 1;
        while (true) {
            checkNode(node);
            block = node.getBooleanBlock();
            assert (block.size() > 0);
            if (searchIndex > index - block.size() && searchIndex <= index) {
                break;
            } else if (searchIndex < index) {
                if (node.left != null && node.left.height < node.height) {
                    node = node.left;
                } else {
                    break;
                }
            } else {
                if (node.right != null && node.right.height < node.height) {
                    node = node.right;
                } else {
                    break;
                }
            }
            index += node.relativePosition;
        }
        block = node.getBooleanBlock();
        assert (block.size() == index - lastIndex);
        lastIndex = index;
    }
    assert (index == size());
    if (oldCurrModify != 0) {
        modify(oldCurrNode, -oldCurrModify);
    }
    currNode = oldCurrNode;
    currModify = oldCurrModify;
}

    // --- BooleanBlock ---  
    /**
	 * A block stores in maximum blockSize number of elements.
	 * The first block in a BooleanBigList will grow until reaches this limit, all other blocks are directly
	 * allocated with a capacity of blockSize.
	 * A block maintains a reference count which allows a block to be shared among different BooleanBigList
	 * instances with a copy-on-write approach.
	 */
    
    public static class BooleanBlock implements Serializable {

        private BooleanGapList values;

        private int refCount;

        public BooleanBlock(){
    values = new BooleanGapList();
    refCount = 1;
}

        public BooleanBlock(int capacity){
    values = new BooleanGapList(capacity);
    refCount = 1;
}

        public BooleanBlock(BooleanBlock that){
    values = new BooleanGapList(that.values.capacity());
    values.init(that.values.getArray(0, that.values.size()));
    refCount = 1;
}

        /**
		 * @return true if block is shared by several BooleanBigList instances
		 */
public boolean isShared() {
    return refCount > 1;
}

        /**
		 * Increment reference count as block is used by one BooleanBigList instance more.
		 */
public BooleanBlock ref() {
    refCount++;
    return this;
}

        /**
		 * Decrement reference count as block is no longer used by one BooleanBigList instance.
		 */
public void unref() {
    refCount--;
}

        /**
		 * @return number of elements stored in this block
		 */
public int size() {
    return values.size();
}

        @Override
public String toString() {
    return values.toString();
}
    }

    // --- BooleanBlockNode ---  
    /**
     * Implements an AVLNode storing a BooleanBlock.
     * The nodes don't know the index of the object they are holding. They do know however their
     * position relative to their parent node. This allows to calculate the index of a node while traversing the tree.
     * There is a faedelung flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    static class BooleanBlockNode {

        /** Pointer to parent node (null for root) */
        BooleanBlockNode parent;

        /** The left child node or the predecessor if {@link #leftIsPrevious}.*/
        BooleanBlockNode left;

        /** Flag indicating that left reference is not a subtree but the predecessor. */
        boolean leftIsPrevious;

        /** The right child node or the successor if {@link #rightIsNext}. */
        BooleanBlockNode right;

        /** Flag indicating that right reference is not a subtree but the successor. */
        boolean rightIsNext;

        /** How many levels of left/right are below this one. */
        int height;

        /** The relative position, root holds absolute position. */
        int relativePosition;

        /** The stored block */
        BooleanBlock block;

        /**
         * Constructs a new node.
         *
         * @param parent			parent node (null for root)
         * @param relativePosition  the relative position of the node (absolute position for root)
         * @param block				the block to store
         * @param rightFollower 	the node following this one
         * @param leftFollower 		the node leading this one
         */
private BooleanBlockNode(BooleanBlockNode parent, int relativePosition, BooleanBlock block, BooleanBlockNode rightFollower, BooleanBlockNode leftFollower){
    this.parent = parent;
    this.relativePosition = relativePosition;
    this.block = block;
    rightIsNext = true;
    leftIsPrevious = true;
    right = rightFollower;
    left = leftFollower;
}

        /**
         * Gets the block stored by this node.
         *
         * @return block stored by this node
         */
public BooleanBlock getBooleanBlock() {
    return block;
}

        /**
         * Sets block to store by this node.
         *
         * @param block  the block to store
         */
public void setBooleanBlock(BooleanBlock block) {
    this.block = block;
}

        /**
         * Retrieves node with specified index.
         *
         * @param list		reference to BooleanBigList using this node (used for updating currBooleanBlockEnd)
         * @param index		index to retrieve
         * @param modify	modification to apply during traversal to relative positions <br/>
         * 					>0: N elements are added at index, <0: N elements are deleted at index, 0: no change
         * @param wasLeft	last node was a left child
         * @return
         */
private BooleanBlockNode access(BooleanBigList list, int index, int modify, boolean wasLeft) {
    assert (index >= 0);
    if (relativePosition == 0) {
        if (modify != 0) {
            relativePosition += modify;
        }
        return this;
    }
    if (list.currBooleanBlockEnd == 0) {
        list.currBooleanBlockEnd = relativePosition;
    }
    int leftIndex = list.currBooleanBlockEnd - block.size();
    assert (leftIndex >= 0);
    if (index >= leftIndex && index < list.currBooleanBlockEnd) {
        // Correct node has been found   
        BooleanBlockNode leftNode = getLeftSubTree();
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
    // Further traversal needed to find the correct node   
    if (index < list.currBooleanBlockEnd) {
        // Travese the left node   
        BooleanBlockNode nextNode = getLeftSubTree();
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
        list.currBooleanBlockEnd += nextNode.relativePosition;
        return nextNode.access(list, index, modify, wasLeft);
    } else {
        // Traverse the right node   
        BooleanBlockNode nextNode = getRightSubTree();
        if (nextNode == null || wasLeft) {
            if (relativePosition > 0) {
                relativePosition += modify;
                BooleanBlockNode left = getLeftSubTree();
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
        list.currBooleanBlockEnd += nextNode.relativePosition;
        return nextNode.access(list, index, modify, wasLeft);
    }
}

        /**
         * Gets the next node in the list after this one.
         *
         * @return the next node
         */
public BooleanBlockNode next() {
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
public BooleanBlockNode previous() {
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
private BooleanBlockNode insert(int index, BooleanBlock obj) {
    assert (relativePosition != 0);
    final int indexRelativeToMe = index - relativePosition;
    if (indexRelativeToMe < 0) {
        return insertOnLeft(indexRelativeToMe, obj);
    } else {
        return insertOnRight(indexRelativeToMe, obj);
    }
}

        private BooleanBlockNode insertOnLeft(int indexRelativeToMe, BooleanBlock obj) {
    if (getLeftSubTree() == null) {
        int pos;
        if (relativePosition >= 0) {
            pos = -relativePosition;
        } else {
            pos = -block.size();
        }
        setLeft(new BooleanBlockNode(this, pos, obj, this, left), null);
    } else {
        setLeft(left.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition >= 0) {
        relativePosition += obj.size();
    }
    final BooleanBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        private BooleanBlockNode insertOnRight(int indexRelativeToMe, BooleanBlock obj) {
    if (getRightSubTree() == null) {
        setRight(new BooleanBlockNode(this, obj.size(), obj, right, this), null);
    } else {
        setRight(right.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition < 0) {
        relativePosition -= obj.size();
    }
    final BooleanBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        /**
         * Gets the left node, returning null if its a faedelung.
         */
public BooleanBlockNode getLeftSubTree() {
    return leftIsPrevious ? null : left;
}

        /**
         * Gets the right node, returning null if its a faedelung.
         */
public BooleanBlockNode getRightSubTree() {
    return rightIsNext ? null : right;
}

        /**
         * Gets the rightmost child of this node.
         *
         * @return the rightmost child (greatest index)
         */
public BooleanBlockNode max() {
    return getRightSubTree() == null ? this : right.max();
}

        /**
         * Gets the leftmost child of this node.
         *
         * @return the leftmost child (smallest index)
         */
public BooleanBlockNode min() {
    return getLeftSubTree() == null ? this : left.min();
}

        /**
         * Removes the node at a given position.
         *
         * @param index is the index of the element to be removed relative to the position of
         * the parent node of the current node.
         */
private BooleanBlockNode remove(int index) {
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

        private BooleanBlockNode removeMax() {
    if (getRightSubTree() == null) {
        return removeSelf();
    }
    setRight(right.removeMax(), right.right);
    recalcHeight();
    return balance();
}

        private BooleanBlockNode removeMin(int size) {
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
public BooleanBlockNode removeSelf() {
    BooleanBlockNode p = parent;
    BooleanBlockNode n = doRemoveSelf();
    if (n != null) {
        assert (p != n);
        n.parent = p;
    }
    return n;
}

        public BooleanBlockNode doRemoveSelf() {
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
        final BooleanBlockNode rightMin = right.min();
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
        final BooleanBlockNode leftMax = left.max();
        block = leftMax.block;
        if (rightIsNext) {
            right = leftMax.right;
        }
        final BooleanBlockNode leftPrevious = left.left;
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

        /**
         * Balances according to the AVL algorithm.
         */
private BooleanBlockNode balance() {
    switch(heightRightMinusLeft()) {
        case 1:
        case 0:
        case -1:
            return this;
        case -2:
            if (left.heightRightMinusLeft() > 0) {
                setLeft(left.rotateLeft(), null);
            }
            return rotateRight();
        case 2:
            if (right.heightRightMinusLeft() < 0) {
                setRight(right.rotateRight(), null);
            }
            return rotateLeft();
        default:
            throw new RuntimeException("tree inconsistent!");
    }
}

        /**
         * Gets the relative position.
         */
private int getOffset(BooleanBlockNode node) {
    if (node == null) {
        return 0;
    }
    return node.relativePosition;
}

        /**
         * Sets the relative position.
         */
private int setOffset(BooleanBlockNode node, int newOffest) {
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
    height = Math.max(getLeftSubTree() == null ? -1 : getLeftSubTree().height, getRightSubTree() == null ? -1 : getRightSubTree().height) + 1;
}

        /**
         * Returns the height of the node or -1 if the node is null.
         */
private int getHeight(final BooleanBlockNode node) {
    return node == null ? -1 : node.height;
}

        /**
         * Returns the height difference right - left
         */
private int heightRightMinusLeft() {
    return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
}

        /**
         * Rotate tree to the left using this node as center.
         *
         * @return node which will take the place of this node
         */
private BooleanBlockNode rotateLeft() {
    assert (!rightIsNext);
    final BooleanBlockNode newTop = right;
    // can't be faedelung!   
    final BooleanBlockNode movedNode = getRightSubTree().getLeftSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    BooleanBlockNode p = this.parent;
    setRight(movedNode, newTop);
    newTop.setLeft(this, null);
    newTop.parent = p;
    this.parent = newTop;
    setOffset(newTop, newTopPosition);
    setOffset(this, myNewPosition);
    setOffset(movedNode, movedPosition);
    assert (newTop.getLeftSubTree() == null || newTop.getLeftSubTree().relativePosition < 0);
    assert (newTop.getRightSubTree() == null || newTop.getRightSubTree().relativePosition > 0);
    return newTop;
}

        /**
         * Rotate tree to the right using this node as center.
         *
         * @return node which will take the place of this node
         */
private BooleanBlockNode rotateRight() {
    assert (!leftIsPrevious);
    final BooleanBlockNode newTop = left;
    // can't be faedelung   
    final BooleanBlockNode movedNode = getLeftSubTree().getRightSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    BooleanBlockNode p = this.parent;
    setLeft(movedNode, newTop);
    newTop.setRight(this, null);
    newTop.parent = p;
    this.parent = newTop;
    setOffset(newTop, newTopPosition);
    setOffset(this, myNewPosition);
    setOffset(movedNode, movedPosition);
    assert (newTop.getLeftSubTree() == null || newTop.getLeftSubTree().relativePosition < 0);
    assert (newTop.getRightSubTree() == null || newTop.getRightSubTree().relativePosition > 0);
    return newTop;
}

        /**
         * Sets the left field to the node, or the previous node if that is null
         *
         * @param node  the new left subtree node
         * @param previous  the previous node in the linked list
         */
private void setLeft(BooleanBlockNode node, BooleanBlockNode previous) {
    assert (node != this && previous != this);
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
private void setRight(BooleanBlockNode node, BooleanBlockNode next) {
    assert (node != this && next != this);
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
    return new StringBuilder().append("BooleanBlockNode(").append(relativePosition).append(',').append(getRightSubTree() != null).append(',').append(block).append(',').append(getRightSubTree() != null).append(", height ").append(height).append(" )").toString();
}
    }

    // --- ImmutableBooleanBigList ---  
    /**
     * An immutable version of a BooleanBigList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableBooleanBigList extends BooleanBigList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
protected ImmutableBooleanBigList(BooleanBigList that){
    super(true, that);
}

        @Override
protected boolean doAdd(int index, boolean elem) {
    error();
    return false;
}

        @Override
protected boolean doAddAll(int index, boolean[] elems) {
    error();
    return false;
}

        @Override
protected boolean doSet(int index, boolean elem) {
    error();
    return false;
}

        @Override
protected void doSetAll(int index, boolean[] elems) {
    error();
}

        @Override
protected boolean doReSet(int index, boolean elem) {
    error();
    return false;
}

        @Override
protected boolean doRemove(int index) {
    error();
    return false;
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
    }
}
