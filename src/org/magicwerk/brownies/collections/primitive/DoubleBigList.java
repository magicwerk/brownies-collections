package org.magicwerk.brownies.collections.primitive;
import org.magicwerk.brownies.collections.helper.ArraysHelper;
import org.magicwerk.brownies.collections.helper.primitive.DoubleBinarySearch;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.BigList;

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
import org.magicwerk.brownies.collections.helper.primitive.DoubleMergeSort;

/**
 * The first block (DoubleGapList) used grows dynamcically, all others
 * are allocated with fixed size. This is necessary to prevent starving
 * because of GC usage.
 *
 * @author Thomas Mauch
 * @version $Id: DoubleBigList.java 2493 2014-10-12 00:40:31Z origo $
 */
public class DoubleBigList extends IDoubleList {
	public static IDoubleList of(double[] values) {
		return new ImmutableDoubleListArrayPrimitive(values);
	}

	public static IDoubleList of(Double[] values) {
		return new ImmutableDoubleListArrayWrapper(values);
	}

	public static IDoubleList of(List<Double> values) {
		return new ImmutableDoubleListList(values);
	}

    static class ImmutableDoubleListArrayPrimitive extends ImmutableDoubleList {
    	double[] values;

    	public ImmutableDoubleListArrayPrimitive(double[] values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		protected double doGet(int index) {
			return values[index];
		}
    }

    static class ImmutableDoubleListArrayWrapper extends ImmutableDoubleList {
    	Double[] values;

    	public ImmutableDoubleListArrayWrapper(Double[] values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		protected double doGet(int index) {
			return values[index];
		}
    }

    static class ImmutableDoubleListList extends ImmutableDoubleList {
    	List<Double> values;

    	public ImmutableDoubleListList(List<Double> values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.size();
		}

		@Override
		protected double doGet(int index) {
			return values.get(index);
		}
    }

    protected static abstract class ImmutableDoubleList extends IDoubleList {

    	//-- Readers

		@Override
		public int capacity() {
			return size();
		}

		@Override
		public int binarySearch(int index, int len, double key) {
			return DoubleBinarySearch.binarySearch(this, key, index, index+len);
		}

		@Override
		public IDoubleList unmodifiableList() {
			return this;
		}

		@Override
		protected double getDefaultElem() {
			return 0;
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
		protected void doClone(IDoubleList that) {
			error();
		}

		@Override
		protected double doSet(int index, double elem) {
			error();
			return 0;
		}

		@Override
		protected double doReSet(int index, double elem) {
			error();
			return 0;
		}

		@Override
		protected boolean doAdd(int index, double elem) {
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
		protected IDoubleList doCreate(int capacity) {
			error();
			return null;
		}

		@Override
		protected void doAssign(IDoubleList that) {
			error();
		}

		@Override
		protected double doRemove(int index) {
			error();
			return 0;
		}

		@Override
		public void sort(int index, int len) {
			error();
		}
    }

    /**
     * An immutable version of a DoubleBigList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableDoubleBigList extends DoubleBigList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
protected ImmutableDoubleBigList(DoubleBigList that){
    super(true, that);
}

        @Override
protected boolean doAdd(int index, double elem) {
    error();
    return false;
}

        @Override
protected boolean doAddAll(int index, double[] elems) {
    error();
    return false;
}

        @Override
protected double doSet(int index, double elem) {
    error();
    return 0;
}

        @Override
protected void doSetAll(int index, double[] elems) {
    error();
}

        @Override
protected double doReSet(int index, double elem) {
    error();
    return 0;
}

        @Override
protected double doRemove(int index) {
    error();
    return 0;
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

    ;

    /**
	 *
	 */
    
    public static class DoubleBlock implements Serializable {

        private DoubleGapList values;

        private int refCount;

        public DoubleBlock(){
    values = new DoubleGapList();
    refCount = 1;
}

        public DoubleBlock(int capacity){
    values = new DoubleGapList(capacity);
    refCount = 1;
}

        public DoubleBlock(DoubleBlock that){
    values = new DoubleGapList(that.values.capacity());
    values.init(that.values.getArray(0, that.values.size()));
    refCount = 1;
}

        public boolean isShared() {
    return refCount > 1;
}

        public DoubleBlock ref() {
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

    /** Number of elements stored in this DoubleBigList */
    private int size;

    /** The root node in the tree */
    private DoubleBlockNode root;

    /** Current node */
    private DoubleBlockNode currNode;

    /** DoubleBlock of current node */
    private DoubleBlock currDoubleBlock;

    /** Start index of current block */
    private int currDoubleBlockStart;

    /** End index of current block */
    private int currDoubleBlockEnd;

    /** Modify value which must be applied before this block is not current any more */
    private int currModify;

    /**
     * Constructor used internally, e.g. for ImmutableDoubleBigList.
     *
     * @param copy true to copy all instance values from source,
     *             if false nothing is done
     * @param that list to copy
     */
protected DoubleBigList(boolean copy, DoubleBigList that){
    if (copy) {
        this.blockSize = that.blockSize;
        this.currDoubleBlock = that.currDoubleBlock;
        this.currDoubleBlockStart = that.currDoubleBlockStart;
        this.currDoubleBlockEnd = that.currDoubleBlockEnd;
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
// This separate method is needed as the varargs variant creates the DoubleGapList with specific size  
public static DoubleBigList create() {
    return new DoubleBigList();
}

    /**
     * Create new list with specified elements.
     *
     * @param coll      collection with element
     * @return          created list
     * @param        type of elements stored in the list
     */
public static DoubleBigList create(Collection<Double> coll) {
    return new DoubleBigList(coll);
}

    /**
	 * Create new list with specified elements.
	 *
	 * @param elems 	array with elements
	 * @return 			created list
	 * @param  		type of elements stored in the list
	 */
public static DoubleBigList create(double... elems) {
    DoubleBigList list = new DoubleBigList();
    for (double elem : elems) {
        list.add(elem);
    }
    return list;
}

    /**
	 * Default constructor.
	 * The default block size is used.
	 */
public DoubleBigList(){
    this(BLOCK_SIZE);
}

    /**
	 * Constructor.
	 *
	 * @param blockSize block size
	 */
public DoubleBigList(int blockSize){
    if (blockSize < 2) {
        throw new IndexOutOfBoundsException("Invalid blockSize: " + blockSize);
    }
    doInit(blockSize, -1);
}

    public DoubleBigList(Collection<Double> that){
    if (that instanceof DoubleBigList) {
        doAssign((DoubleBigList) that);
        doClone((DoubleBigList) that);
    } else {
        blockSize = BLOCK_SIZE;
        currDoubleBlock = new DoubleBlock();
        addDoubleBlock(0, currDoubleBlock);
        for (Object obj : that.toArray()) {
            add((Double) obj);
        }
        assert (size() == that.size());
    }
}

    public void init() {
    clear();
}

    public void init(double... elems) {
    clear();
    for (double elem : elems) {
        add(elem);
    }
}

    public void init(Collection<Double> that) {
    clear();
    addAll(that);
}

    /**
     * Returns block size used for this DoubleBigList.
     *
     * @return block size used for this DoubleBigList
     */
public int blockSize() {
    return blockSize;
}

    //---  
private DoubleBigList(int blockSize, int firstDoubleBlockSize){
    doInit(blockSize, firstDoubleBlockSize);
}

    void doInit(int blockSize, int firstDoubleBlockSize) {
    this.blockSize = blockSize;
    // First block will grow until it reaches blockSize   
    if (firstDoubleBlockSize <= 1) {
        currDoubleBlock = new DoubleBlock();
    } else {
        currDoubleBlock = new DoubleBlock(firstDoubleBlockSize);
    }
    addDoubleBlock(0, currDoubleBlock);
}

    @Override
public DoubleBigList copy() {
    return (DoubleBigList) super.copy();
}

    @Override
protected void doAssign(IDoubleList that) {
    DoubleBigList list = (DoubleBigList) that;
    this.blockSize = list.blockSize;
    this.currDoubleBlock = list.currDoubleBlock;
    this.currDoubleBlockEnd = list.currDoubleBlockEnd;
    this.currDoubleBlockStart = list.currDoubleBlockStart;
    this.currNode = list.currNode;
    this.root = list.root;
    this.size = list.size;
}

    @Override
protected void doClone(IDoubleList that) {
    DoubleBigList bigList = (DoubleBigList) that;
    bigList.check();
    bigList.releaseDoubleBlock();
    root = copy(bigList.root);
    currNode = null;
    currModify = 0;
    check();
}

    private DoubleBlockNode copy(DoubleBlockNode node) {
    DoubleBlockNode newNode = node.min();
    int index = newNode.block.size();
    DoubleBlockNode newRoot = new DoubleBlockNode(null, index, newNode.block.ref(), null, null);
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
public double getDefaultElem() {
    return 0;
}

    @Override
protected void finalize() {
    // This list will be garbage collected, so unref all referenced blocks   
    DoubleBlockNode node = root.min();
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
    return -1;
}

    @Override
protected double doGet(int index) {
    int pos = getDoubleBlockIndex(index, false, 0);
    return currDoubleBlock.values.doGet(pos);
}

    @Override
protected double doSet(int index, double elem) {
    int pos = getDoubleBlockIndex(index, true, 0);
    double oldElem = currDoubleBlock.values.doGet(pos);
    currDoubleBlock.values.doSet(pos, elem);
    return oldElem;
}

    @Override
protected double doReSet(int index, double elem) {
    int pos = getDoubleBlockIndex(index, true, 0);
    double oldElem = currDoubleBlock.values.doGet(pos);
    currDoubleBlock.values.doSet(pos, elem);
    return oldElem;
}

    private void releaseDoubleBlock() {
    if (currModify != 0) {
        int modify = currModify;
        currModify = 0;
        modify(currNode, modify);
    }
    currNode = null;
}

    /**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currDoubleBlock to remember this last used block.
	 *
	 * @param index	list index (0 <= index <= size())
	 * @return		relative index within block
	 */
private int getDoubleBlockIndex(int index, boolean write, int modify) {
    // Determine block where specified index is located and store it in currDoubleBlock   
    if (currNode != null) {
        if (index >= currDoubleBlockStart && (index < currDoubleBlockEnd || index == currDoubleBlockEnd && size == index)) {
            // currDoubleBlock is already set correctly   
            if (write) {
                if (currDoubleBlock.isShared()) {
                    currDoubleBlock.unref();
                    currDoubleBlock = new DoubleBlock(currDoubleBlock);
                    currNode.setDoubleBlock(currDoubleBlock);
                }
            }
            currModify += modify;
            return index - currDoubleBlockStart;
        }
        releaseDoubleBlock();
    }
    boolean done = false;
    if (index == size) {
        if (currNode == null || currDoubleBlockEnd != size) {
            currNode = root.max();
            currDoubleBlock = currNode.getDoubleBlock();
            currDoubleBlockEnd = size;
            currDoubleBlockStart = size - currDoubleBlock.size();
        }
        if (modify != 0) {
            currNode.relativePosition += modify;
            DoubleBlockNode leftNode = currNode.getLeftSubTree();
            if (leftNode != null) {
                leftNode.relativePosition -= modify;
            }
        }
        done = true;
    } else if (index == 0) {
        if (currNode == null || currDoubleBlockStart != 0) {
            currNode = root.min();
            currDoubleBlock = currNode.getDoubleBlock();
            currDoubleBlockEnd = currDoubleBlock.size();
            currDoubleBlockStart = 0;
        }
        if (modify != 0) {
            root.relativePosition += modify;
        }
        done = true;
    }
    if (!done) {
        // Reset currDoubleBlockEnd, it will be then set by access()   
        currDoubleBlockEnd = 0;
        currNode = access(index, modify);
        currDoubleBlock = currNode.getDoubleBlock();
        currDoubleBlockStart = currDoubleBlockEnd - currDoubleBlock.size();
    }
    assert (index >= currDoubleBlockStart && index <= currDoubleBlockEnd);
    if (write) {
        if (currDoubleBlock.isShared()) {
            currDoubleBlock.unref();
            currDoubleBlock = new DoubleBlock(currDoubleBlock);
            currNode.setDoubleBlock(currDoubleBlock);
        }
    }
    return index - currDoubleBlockStart;
}

    void checkNode(DoubleBlockNode node) {
    assert ((node.block.size() > 0 || node == root) && node.block.size() <= blockSize);
    DoubleBlockNode child = node.getLeftSubTree();
    assert (child == null || child.parent == node);
    child = node.getRightSubTree();
    assert (child == null || child.parent == node);
}

    void checkHeight(DoubleBlockNode node) {
    DoubleBlockNode left = node.getLeftSubTree();
    DoubleBlockNode right = node.getRightSubTree();
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

    void check() {
    //if (true) {return; } //TODO   
    if (currNode != null) {
        assert (currNode.block == currDoubleBlock);
        assert (currDoubleBlockStart >= 0 && currDoubleBlockEnd <= size && currDoubleBlockStart <= currDoubleBlockEnd);
        assert (currDoubleBlockStart + currDoubleBlock.size() == currDoubleBlockEnd);
    }
    if (root == null) {
        assert (size == 0);
        return;
    }
    checkHeight(root);
    DoubleBlockNode oldCurrNode = currNode;
    int oldCurrModify = currModify;
    if (currModify != 0) {
        currNode = null;
        currModify = 0;
        modify(oldCurrNode, oldCurrModify);
    }
    DoubleBlockNode node = root;
    checkNode(node);
    int index = node.relativePosition;
    while (node.left != null) {
        node = node.left;
        checkNode(node);
        assert (node.relativePosition < 0);
        index += node.relativePosition;
    }
    DoubleBlock block = node.getDoubleBlock();
    assert (block.size() == index);
    int lastIndex = index;
    while (lastIndex < size()) {
        node = root;
        index = node.relativePosition;
        int searchIndex = lastIndex + 1;
        while (true) {
            checkNode(node);
            block = node.getDoubleBlock();
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
        block = node.getDoubleBlock();
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

    @Override
protected boolean doAdd(int index, double element) {
    if (index == -1) {
        index = size;
    }
    // Insert   
    int pos = getDoubleBlockIndex(index, true, 1);
    // If there is still place in the current block: insert in current block   
    int maxSize = (index == size || index == 0) ? blockSize * 9 / 10 : blockSize;
    // The second part of the condition is a work around to handle the case of insertion as position 0 correctly   
    // where blockSize() is 2 (the new block would then be added after the current one)   
    if (currDoubleBlock.size() < maxSize || (currDoubleBlock.size() == 1 && currDoubleBlock.size() < blockSize)) {
        currDoubleBlock.values.doAdd(pos, element);
        currDoubleBlockEnd++;
    } else {
        // No place any more in current block   
        DoubleBlock newDoubleBlock = new DoubleBlock(blockSize);
        if (index == size) {
            // Insert new block at tail   
            newDoubleBlock.values.doAdd(0, element);
            // Subtract 1 because getDoubleBlockIndex() has already added 1   
            modify(currNode, -1);
            addDoubleBlock(size + 1, newDoubleBlock);
            DoubleBlockNode lastNode = currNode.next();
            currNode = lastNode;
            currDoubleBlock = currNode.block;
            currDoubleBlockStart = currDoubleBlockEnd;
            currDoubleBlockEnd++;
        } else if (index == 0) {
            // Insert new block at head   
            newDoubleBlock.values.doAdd(0, element);
            // Subtract 1 because getDoubleBlockIndex() has already added 1   
            modify(currNode, -1);
            addDoubleBlock(1, newDoubleBlock);
            DoubleBlockNode firstNode = currNode.previous();
            currNode = firstNode;
            currDoubleBlock = currNode.block;
            currDoubleBlockStart = 0;
            currDoubleBlockEnd = 1;
        } else {
            // Split block for insert   
            int nextDoubleBlockLen = blockSize / 2;
            int blockLen = blockSize - nextDoubleBlockLen;
            newDoubleBlock.values.init(nextDoubleBlockLen, 0);
            DoubleGapList.copy(currDoubleBlock.values, blockLen, newDoubleBlock.values, 0, nextDoubleBlockLen);
            currDoubleBlock.values.remove(blockLen, blockSize - blockLen);
            // Subtract 1 more because getDoubleBlockIndex() has already added 1   
            modify(currNode, -nextDoubleBlockLen - 1);
            addDoubleBlock(currDoubleBlockEnd - nextDoubleBlockLen, newDoubleBlock);
            if (pos < blockLen) {
                // Insert element in first block   
                currDoubleBlock.values.doAdd(pos, element);
                currDoubleBlockEnd = currDoubleBlockStart + blockLen + 1;
                modify(currNode, 1);
            } else {
                // Insert element in second block   
                currNode = currNode.next();
                modify(currNode, 1);
                currDoubleBlock = currNode.block;
                currDoubleBlock.values.doAdd(pos - blockLen, element);
                currDoubleBlockStart += blockLen;
                currDoubleBlockEnd++;
            }
        }
    }
    size++;
    if (DUMP)
        dump();
    if (CHECK)
        check();
    return true;
}

    /**
	 * Modify relativePosition of all nodes starting from the specified node.
	 *
	 * @param node
	 * @param modify
	 */
private void modify(DoubleBlockNode node, int modify) {
    if (node == currNode) {
        modify += currModify;
        currModify = 0;
    } else {
        releaseDoubleBlock();
    }
    if (modify == 0) {
        return;
    }
    if (node.relativePosition < 0) {
        // Left node   
        DoubleBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        DoubleBlockNode pp = node.parent;
        assert (pp.getLeftSubTree() == node);
        boolean parentRight = true;
        while (true) {
            DoubleBlockNode p = pp.parent;
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
        DoubleBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        DoubleBlockNode parent = node.parent;
        if (parent != null) {
            assert (parent.getRightSubTree() == node);
            boolean parentLeft = true;
            while (true) {
                DoubleBlockNode p = parent.parent;
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

    private DoubleBlockNode doRemove(DoubleBlockNode node) {
    DoubleBlockNode p = node.parent;
    DoubleBlockNode newNode = node.removeSelf();
    DoubleBlockNode n = newNode;
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
protected boolean doAddAll(int index, double[] array) {
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
    int addPos = getDoubleBlockIndex(index, true, 0);
    DoubleBlock addDoubleBlock = currDoubleBlock;
    int space = blockSize - addDoubleBlock.size();
    int addLen = array.length;
    if (addLen <= space) {
        // All elements can be added to current block   
        currDoubleBlock.values.addAll(addPos, array);
        modify(currNode, addLen);
        size += addLen;
        currDoubleBlockEnd += addLen;
    } else {
        if (index == size) {
            // Add elements at end   
            for (int i = 0; i < space; i++) {
                currDoubleBlock.values.add(addPos + i, array[i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                DoubleBlock nextDoubleBlock = new DoubleBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextDoubleBlock.values.add(i, array[done + i]);
                }
                done += add;
                todo -= add;
                addDoubleBlock(size + done, nextDoubleBlock);
                currNode = currNode.next();
            }
            size += addLen;
            currDoubleBlock = currNode.block;
            currDoubleBlockEnd = size;
            currDoubleBlockStart = currDoubleBlockEnd - currDoubleBlock.size();
        } else if (index == 0) {
            // Add elements at head   
            assert (addPos == 0);
            for (int i = 0; i < space; i++) {
                currDoubleBlock.values.add(addPos + i, array[addLen - space + i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                DoubleBlock nextDoubleBlock = new DoubleBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextDoubleBlock.values.add(i, array[addLen - done - add + i]);
                }
                done += add;
                todo -= add;
                addDoubleBlock(0, nextDoubleBlock);
                currNode = currNode.previous();
            }
            size += addLen;
            currDoubleBlock = currNode.block;
            currDoubleBlockStart = 0;
            currDoubleBlockEnd = currDoubleBlock.size();
        } else {
            // Add elements to several blocks   
            // Handle first block   
            DoubleGapList list = DoubleGapList.create(array);
            int remove = currDoubleBlock.values.size() - addPos;
            if (remove > 0) {
                list.addAll(currDoubleBlock.values.getAll(addPos, remove));
                currDoubleBlock.values.remove(addPos, remove);
                modify(currNode, -remove);
                size -= remove;
                currDoubleBlockEnd -= remove;
            }
            int s = currDoubleBlock.values.size() + list.size();
            int numDoubleBlocks = (s - 1) / blockSize + 1;
            assert (numDoubleBlocks > 1);
            int has = currDoubleBlock.values.size();
            int should = s / numDoubleBlocks;
            int start = 0;
            int end = 0;
            if (has < should) {
                // Elements must be added to first block   
                int add = should - has;
                IDoubleList sublist = list.getAll(0, add);
                currDoubleBlock.values.addAll(addPos, sublist);
                modify(currNode, add);
                start += add;
                assert (currDoubleBlock.values.size() == should);
                s -= should;
                numDoubleBlocks--;
                size += add;
                currDoubleBlockEnd += add;
                end = currDoubleBlockEnd;
            } else if (has > should) {
                // Elements must be moved from first to second block   
                DoubleBlock nextDoubleBlock = new DoubleBlock(blockSize);
                int move = has - should;
                nextDoubleBlock.values.addAll(currDoubleBlock.values.getAll(currDoubleBlock.values.size() - move, move));
                currDoubleBlock.values.remove(currDoubleBlock.values.size() - move, move);
                modify(currNode, -move);
                assert (currDoubleBlock.values.size() == should);
                s -= should;
                numDoubleBlocks--;
                currDoubleBlockEnd -= move;
                end = currDoubleBlockEnd;
                should = s / numDoubleBlocks;
                int add = should - move;
                assert (add >= 0);
                IDoubleList sublist = list.getAll(0, add);
                nextDoubleBlock.values.addAll(move, sublist);
                start += add;
                assert (nextDoubleBlock.values.size() == should);
                s -= should;
                numDoubleBlocks--;
                size += add;
                end += add;
                addDoubleBlock(end, nextDoubleBlock);
            } else {
                end = currDoubleBlockEnd;
                s -= should;
                numDoubleBlocks--;
            }
            check();
            DoubleBlockNode node = currNode;
            while (numDoubleBlocks > 0) {
                int add = s / numDoubleBlocks;
                assert (add > 0);
                IDoubleList sublist = list.getAll(start, add);
                DoubleBlock nextDoubleBlock = new DoubleBlock();
                nextDoubleBlock.values.clear();
                nextDoubleBlock.values.addAll(sublist);
                start += add;
                assert (nextDoubleBlock.values.size() == add);
                s -= add;
                addDoubleBlock(end, nextDoubleBlock);
                assert (node.next().block == nextDoubleBlock);
                node = node.next();
                end += add;
                size += add;
                numDoubleBlocks--;
                check();
            }
        }
    }
    assert (oldSize + addLen == size);
    check();
    return true;
}

    @Override
protected void doClear() {
    root = null;
    currDoubleBlock = null;
    currDoubleBlockStart = 0;
    currDoubleBlockEnd = 0;
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
    int startPos = getDoubleBlockIndex(index, true, 0);
    DoubleBlockNode startNode = currNode;
    int endPos = getDoubleBlockIndex(index + len - 1, true, 0);
    DoubleBlockNode endNode = currNode;
    if (startNode == endNode) {
        // Delete from single block   
        getDoubleBlockIndex(index, true, -len);
        currDoubleBlock.values.remove(startPos, len);
        if (currDoubleBlock.values.isEmpty()) {
            DoubleBlockNode oldCurrNode = currNode;
            releaseDoubleBlock();
            DoubleBlockNode node = doRemove(oldCurrNode);
            merge(node);
        } else {
            currDoubleBlockEnd -= len;
            merge(currNode);
        }
        size -= len;
    } else {
        // Delete from start block   
        check();
        int startLen = startNode.block.size() - startPos;
        getDoubleBlockIndex(index, true, -startLen);
        // TODO should that be modify?   
        startNode.block.values.remove(startPos, startLen);
        assert (startNode == currNode);
        if (currDoubleBlock.values.isEmpty()) {
            releaseDoubleBlock();
            doRemove(startNode);
            startNode = null;
        }
        len -= startLen;
        size -= startLen;
        //check();   
        while (len > 0) {
            currNode = null;
            getDoubleBlockIndex(index, true, 0);
            int s = currDoubleBlock.size();
            if (s <= len) {
                modify(currNode, -s);
                DoubleBlockNode oldCurrNode = currNode;
                releaseDoubleBlock();
                doRemove(oldCurrNode);
                if (oldCurrNode == endNode) {
                    endNode = null;
                }
                len -= s;
                size -= s;
                check();
            } else {
                modify(currNode, -len);
                currDoubleBlock.values.remove(0, len);
                size -= len;
                break;
            }
        }
        releaseDoubleBlock();
        check();
        getDoubleBlockIndex(index, false, 0);
        merge(currNode);
    }
    if (DUMP)
        dump();
    if (CHECK)
        check();
}

    void merge(DoubleBlockNode node) {
    if (node == null) {
        return;
    }
    final int minDoubleBlockSize = Math.max(blockSize / 3, 1);
    if (node.block.values.size() >= minDoubleBlockSize) {
        return;
    }
    DoubleBlockNode oldCurrNode = node;
    DoubleBlockNode leftNode = node.previous();
    if (leftNode != null && leftNode.block.size() < minDoubleBlockSize) {
        // Merge with left block   
        int len = node.block.size();
        int dstSize = leftNode.getDoubleBlock().size();
        for (int i = 0; i < len; i++) {
            leftNode.block.values.add(0);
        }
        DoubleGapList.copy(node.block.values, 0, leftNode.block.values, dstSize, len);
        assert (leftNode.block.values.size() <= blockSize);
        modify(leftNode, +len);
        modify(oldCurrNode, -len);
        releaseDoubleBlock();
        doRemove(oldCurrNode);
    } else {
        DoubleBlockNode rightNode = node.next();
        if (rightNode != null && rightNode.block.size() < minDoubleBlockSize) {
            // Merge with right block   
            int len = node.block.size();
            for (int i = 0; i < len; i++) {
                rightNode.block.values.add(0, 0);
            }
            DoubleGapList.copy(node.block.values, 0, rightNode.block.values, 0, len);
            assert (rightNode.block.values.size() <= blockSize);
            modify(rightNode, +len);
            modify(oldCurrNode, -len);
            releaseDoubleBlock();
            doRemove(oldCurrNode);
        }
    }
}

    protected double doRemove(int index) {
    int pos = getDoubleBlockIndex(index, true, -1);
    double oldElem = currDoubleBlock.values.doRemove(pos);
    currDoubleBlockEnd--;
    final int minDoubleBlockSize = Math.max(blockSize / 3, 1);
    if (currDoubleBlock.size() < minDoubleBlockSize) {
        if (currDoubleBlock.size() == 0) {
            if (!isOnlyRootDoubleBlock()) {
                DoubleBlockNode oldCurrNode = currNode;
                releaseDoubleBlock();
                doRemove(oldCurrNode);
            }
        } else if (index != 0 && index != size - 1) {
            merge(currNode);
        }
    }
    size--;
    if (DUMP)
        dump();
    if (CHECK)
        check();
    return oldElem;
}

    private void dump() {
}

    /**/
@Override
public DoubleBigList unmodifiableList() {
    // Naming as in java.util.Collections#unmodifiableList   
    return new ImmutableDoubleBigList(this);
}

    @Override
protected void doEnsureCapacity(int minCapacity) {
    if (isOnlyRootDoubleBlock()) {
        if (minCapacity > blockSize) {
            minCapacity = blockSize;
        }
        currDoubleBlock.values.doEnsureCapacity(minCapacity);
    }
}

    @Override
public void trimToSize() {
    if (isOnlyRootDoubleBlock()) {
        currDoubleBlock.values.trimToSize();
    }
}

    @Override
protected IDoubleList doCreate(int capacity) {
    if (capacity <= blockSize) {
        return new DoubleBigList(this.blockSize);
    } else {
        return new DoubleBigList(this.blockSize, capacity);
    }
}

    @Override
public void sort(int index, int len) {
    checkRange(index, len);
    if (isOnlyRootDoubleBlock()) {
        currDoubleBlock.values.sort(index, len);
    } else {
        DoubleMergeSort.sort(this, index, index + len);
    }
}

    @Override
public int binarySearch(int index, int len, double key) {
    checkRange(index, len);
    if (isOnlyRootDoubleBlock()) {
        return currDoubleBlock.values.binarySearch(key);
    } else {
        return DoubleBinarySearch.binarySearch(this, key, 0, size());
    }
}

    private boolean isOnlyRootDoubleBlock() {
    return root.left == null && root.right == null;
}

    public DoubleBlockNode access(final int index, int modify) {
    return root.access(this, index, modify, false);
}

    //-----------------------------------------------------------------------  
/**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */
public void addDoubleBlock(int index, DoubleBlock obj) {
    if (root == null) {
        root = new DoubleBlockNode(null, index, obj, null, null);
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
public void removeDoubleBlock(int index) {
    root = root.remove(index);
}

    // --- Serialization ---  
/**
     * Serialize a DoubleBigList object.
     *
     * @serialData The length of the array backing the <tt>DoubleGapList</tt>
     *             instance is emitted (int), followed by all of its elements
     *             (each an <tt>Object</tt>) in the proper order.
     * @param oos  output stream for serialization
     * @throws 	   IOException if serialization fails
     */
private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.writeInt(blockSize);
    int size = size();
    oos.writeInt(size);
    for (int i = 0; i < size; i++) {
        oos.writeDouble(doGet(i));
    }
}

    /**
     * Deserialize a DoubleBigList object.
 	 *
     * @param ois  input stream for serialization
     * @throws 	   IOException if serialization fails
     * @throws 	   ClassNotFoundException if serialization fails
     */

private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    int blockSize = ois.readInt();
    int size = ois.readInt();
    int firstDoubleBlockSize = (size <= blockSize) ? size : -1;
    doInit(blockSize, firstDoubleBlockSize);
    for (int i = 0; i < size; i++) {
        add(ois.readDouble());
    }
}

    //-----------------------------------------------------------------------  
    /**
     * Implements an AVLNode which keeps the offset updated.
     * <p>
     * This node contains the real work.
     * TreeList is just there to implement {@link java.util.IDoubleList}.
     * The nodes don't know the index of the object they are holding.  They
     * do know however their position relative to their parent node.
     * This allows to calculate the index of a node while traversing the tree.
     * <p>
     * The Faedelung calculation stores a flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    static class DoubleBlockNode {

        DoubleBlockNode parent;

        /** The left child node or the predecessor if {@link #leftIsPrevious}.*/
        DoubleBlockNode left;

        /** Flag indicating that left reference is not a subtree but the predecessor. */
        boolean leftIsPrevious;

        /** The right child node or the successor if {@link #rightIsNext}. */
        DoubleBlockNode right;

        /** Flag indicating that right reference is not a subtree but the successor. */
        boolean rightIsNext;

        /** How many levels of left/right are below this one. */
        int height;

        /** The relative position, root holds absolute position. */
        int relativePosition;

        /** The stored block */
        DoubleBlock block;

        /**
         * Constructs a new node with a relative position.
         *
         * @param relativePosition  the relative position of the node
         * @param obj  the value for the node
         * @param rightFollower the node with the value following this one
         * @param leftFollower the node with the value leading this one
         */
private DoubleBlockNode(DoubleBlockNode parent, final int relativePosition, final DoubleBlock block, final DoubleBlockNode rightFollower, final DoubleBlockNode leftFollower){
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
public DoubleBlock getDoubleBlock() {
    return block;
}

        /**
         * Sets the value.
         *
         * @param obj  the value to store
         */
public void setDoubleBlock(DoubleBlock obj) {
    this.block = obj;
}

        private DoubleBlockNode access(DoubleBigList list, int index, int modify, boolean wasLeft) {
    assert (index >= 0);
    if (relativePosition == 0) {
        if (modify != 0) {
            relativePosition += modify;
        }
        return this;
    }
    if (list.currDoubleBlockEnd == 0) {
        list.currDoubleBlockEnd = relativePosition;
    }
    DoubleBlockNode leftNode = getLeftSubTree();
    int leftIndex = list.currDoubleBlockEnd - block.size();
    assert (leftIndex >= 0);
    if (index >= leftIndex && index < list.currDoubleBlockEnd) {
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
    if (index < list.currDoubleBlockEnd) {
        // left   
        DoubleBlockNode nextNode = getLeftSubTree();
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
        list.currDoubleBlockEnd += nextNode.relativePosition;
        return nextNode.access(list, index, modify, wasLeft);
    } else {
        // right   
        DoubleBlockNode nextNode = getRightSubTree();
        if (nextNode == null || wasLeft) {
            if (relativePosition > 0) {
                relativePosition += modify;
                DoubleBlockNode left = getLeftSubTree();
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
        list.currDoubleBlockEnd += nextNode.relativePosition;
        return nextNode.access(list, index, modify, wasLeft);
    }
}

        /**
         * Gets the next node in the list after this one.
         *
         * @return the next node
         */
public DoubleBlockNode next() {
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
public DoubleBlockNode previous() {
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
private DoubleBlockNode insert(int index, DoubleBlock obj) {
    assert (relativePosition != 0);
    final int indexRelativeToMe = index - relativePosition;
    if (indexRelativeToMe < 0) {
        return insertOnLeft(indexRelativeToMe, obj);
    } else {
        return insertOnRight(indexRelativeToMe, obj);
    }
}

        private DoubleBlockNode insertOnLeft(int indexRelativeToMe, DoubleBlock obj) {
    if (getLeftSubTree() == null) {
        int pos;
        if (relativePosition >= 0) {
            pos = -relativePosition;
        } else {
            pos = -block.size();
        }
        setLeft(new DoubleBlockNode(this, pos, obj, this, left), null);
    } else {
        setLeft(left.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition >= 0) {
        relativePosition += obj.size();
    }
    final DoubleBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        private DoubleBlockNode insertOnRight(int indexRelativeToMe, DoubleBlock obj) {
    if (getRightSubTree() == null) {
        setRight(new DoubleBlockNode(this, obj.size(), obj, right, this), null);
    } else {
        setRight(right.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition < 0) {
        relativePosition -= obj.size();
    }
    final DoubleBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        //-----------------------------------------------------------------------  
/**
         * Gets the left node, returning null if its a faedelung.
         */
public DoubleBlockNode getLeftSubTree() {
    return leftIsPrevious ? null : left;
}

        /**
         * Gets the right node, returning null if its a faedelung.
         */
public DoubleBlockNode getRightSubTree() {
    return rightIsNext ? null : right;
}

        /**
         * Gets the rightmost child of this node.
         *
         * @return the rightmost child (greatest index)
         */
public DoubleBlockNode max() {
    return getRightSubTree() == null ? this : right.max();
}

        /**
         * Gets the leftmost child of this node.
         *
         * @return the leftmost child (smallest index)
         */
public DoubleBlockNode min() {
    return getLeftSubTree() == null ? this : left.min();
}

        /**
         * Removes the node at a given position.
         *
         * @param index is the index of the element to be removed relative to the position of
         * the parent node of the current node.
         */
private DoubleBlockNode remove(final int index) {
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

        private DoubleBlockNode removeMax() {
    if (getRightSubTree() == null) {
        return removeSelf();
    }
    setRight(right.removeMax(), right.right);
    if (relativePosition < 0) {
    }
    recalcHeight();
    return balance();
}

        private DoubleBlockNode removeMin(int size) {
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
public DoubleBlockNode removeSelf() {
    DoubleBlockNode p = parent;
    DoubleBlockNode n = doRemoveSelf();
    if (n != null) {
        assert (p != n);
        n.parent = p;
    }
    return n;
}

        public DoubleBlockNode doRemoveSelf() {
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
        final DoubleBlockNode rightMin = right.min();
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
        final DoubleBlockNode leftMax = left.max();
        block = leftMax.block;
        if (rightIsNext) {
            right = leftMax.right;
        }
        final DoubleBlockNode leftPrevious = left.left;
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
private DoubleBlockNode balance() {
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
private int getOffset(final DoubleBlockNode node) {
    if (node == null) {
        return 0;
    }
    return node.relativePosition;
}

        /**
         * Sets the relative position.
         */
private int setOffset(final DoubleBlockNode node, final int newOffest) {
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
private int getHeight(final DoubleBlockNode node) {
    return node == null ? -1 : node.height;
}

        /**
         * Returns the height difference right - left
         */
private int heightRightMinusLeft() {
    return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
}

        private DoubleBlockNode rotateLeft() {
    assert (!rightIsNext);
    final DoubleBlockNode newTop = right;
    // can't be faedelung!   
    final DoubleBlockNode movedNode = getRightSubTree().getLeftSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    DoubleBlockNode p = this.parent;
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

        private DoubleBlockNode rotateRight() {
    assert (!leftIsPrevious);
    final DoubleBlockNode newTop = left;
    // can't be faedelung   
    final DoubleBlockNode movedNode = getLeftSubTree().getRightSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    DoubleBlockNode p = this.parent;
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
private void setLeft(final DoubleBlockNode node, final DoubleBlockNode previous) {
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
private void setRight(final DoubleBlockNode node, final DoubleBlockNode next) {
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
    return new StringBuilder().append("AVLNode(").append(relativePosition).append(',').append(getRightSubTree() != null).append(',').append(block).append(',').append(getRightSubTree() != null).append(", height ").append(height).append(" )").toString();
}
    }
}
