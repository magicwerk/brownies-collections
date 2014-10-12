package org.magicwerk.brownies.collections.primitive;
import org.magicwerk.brownies.collections.helper.ArraysHelper;
import org.magicwerk.brownies.collections.helper.primitive.IntBinarySearch;
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
import org.magicwerk.brownies.collections.helper.primitive.IntMergeSort;

/**
 * The first block (IntGapList) used grows dynamcically, all others
 * are allocated with fixed size. This is necessary to prevent starving
 * because of GC usage.
 *
 * @author Thomas Mauch
 * @version $Id: IntBigList.java 2493 2014-10-12 00:40:31Z origo $
 */
public class IntBigList extends IIntList {
	public static IIntList of(int[] values) {
		return new ImmutableIntListArrayPrimitive(values);
	}

	public static IIntList of(Integer[] values) {
		return new ImmutableIntListArrayWrapper(values);
	}

	public static IIntList of(List<Integer> values) {
		return new ImmutableIntListList(values);
	}

    static class ImmutableIntListArrayPrimitive extends ImmutableIntList {
    	int[] values;

    	public ImmutableIntListArrayPrimitive(int[] values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		protected int doGet(int index) {
			return values[index];
		}
    }

    static class ImmutableIntListArrayWrapper extends ImmutableIntList {
    	Integer[] values;

    	public ImmutableIntListArrayWrapper(Integer[] values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		protected int doGet(int index) {
			return values[index];
		}
    }

    static class ImmutableIntListList extends ImmutableIntList {
    	List<Integer> values;

    	public ImmutableIntListList(List<Integer> values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.size();
		}

		@Override
		protected int doGet(int index) {
			return values.get(index);
		}
    }

    protected static abstract class ImmutableIntList extends IIntList {

    	//-- Readers

		@Override
		public int capacity() {
			return size();
		}

		@Override
		public int binarySearch(int index, int len, int key) {
			return IntBinarySearch.binarySearch(this, key, index, index+len);
		}

		@Override
		public IIntList unmodifiableList() {
			return this;
		}

		@Override
		protected int getDefaultElem() {
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
		protected void doClone(IIntList that) {
			error();
		}

		@Override
		protected int doSet(int index, int elem) {
			error();
			return 0;
		}

		@Override
		protected int doReSet(int index, int elem) {
			error();
			return 0;
		}

		@Override
		protected boolean doAdd(int index, int elem) {
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
		protected IIntList doCreate(int capacity) {
			error();
			return null;
		}

		@Override
		protected void doAssign(IIntList that) {
			error();
		}

		@Override
		protected int doRemove(int index) {
			error();
			return 0;
		}

		@Override
		public void sort(int index, int len) {
			error();
		}
    }

    /**
     * An immutable version of a IntBigList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableIntBigList extends IntBigList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
protected ImmutableIntBigList(IntBigList that){
    super(true, that);
}

        @Override
protected boolean doAdd(int index, int elem) {
    error();
    return false;
}

        @Override
protected boolean doAddAll(int index, int[] elems) {
    error();
    return false;
}

        @Override
protected int doSet(int index, int elem) {
    error();
    return 0;
}

        @Override
protected void doSetAll(int index, int[] elems) {
    error();
}

        @Override
protected int doReSet(int index, int elem) {
    error();
    return 0;
}

        @Override
protected int doRemove(int index) {
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
    
    public static class IntBlock implements Serializable {

        private IntGapList values;

        private int refCount;

        public IntBlock(){
    values = new IntGapList();
    refCount = 1;
}

        public IntBlock(int capacity){
    values = new IntGapList(capacity);
    refCount = 1;
}

        public IntBlock(IntBlock that){
    values = new IntGapList(that.values.capacity());
    values.init(that.values.getArray(0, that.values.size()));
    refCount = 1;
}

        public boolean isShared() {
    return refCount > 1;
}

        public IntBlock ref() {
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

    /** Number of elements stored in this IntBigList */
    private int size;

    /** The root node in the tree */
    private IntBlockNode root;

    /** Current node */
    private IntBlockNode currNode;

    /** IntBlock of current node */
    private IntBlock currIntBlock;

    /** Start index of current block */
    private int currIntBlockStart;

    /** End index of current block */
    private int currIntBlockEnd;

    /** Modify value which must be applied before this block is not current any more */
    private int currModify;

    /**
     * Constructor used internally, e.g. for ImmutableIntBigList.
     *
     * @param copy true to copy all instance values from source,
     *             if false nothing is done
     * @param that list to copy
     */
protected IntBigList(boolean copy, IntBigList that){
    if (copy) {
        this.blockSize = that.blockSize;
        this.currIntBlock = that.currIntBlock;
        this.currIntBlockStart = that.currIntBlockStart;
        this.currIntBlockEnd = that.currIntBlockEnd;
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
// This separate method is needed as the varargs variant creates the IntGapList with specific size  
public static IntBigList create() {
    return new IntBigList();
}

    /**
     * Create new list with specified elements.
     *
     * @param coll      collection with element
     * @return          created list
     * @param        type of elements stored in the list
     */
public static IntBigList create(Collection<Integer> coll) {
    return new IntBigList(coll);
}

    /**
	 * Create new list with specified elements.
	 *
	 * @param elems 	array with elements
	 * @return 			created list
	 * @param  		type of elements stored in the list
	 */
public static IntBigList create(int... elems) {
    IntBigList list = new IntBigList();
    for (int elem : elems) {
        list.add(elem);
    }
    return list;
}

    /**
	 * Default constructor.
	 * The default block size is used.
	 */
public IntBigList(){
    this(BLOCK_SIZE);
}

    /**
	 * Constructor.
	 *
	 * @param blockSize block size
	 */
public IntBigList(int blockSize){
    if (blockSize < 2) {
        throw new IndexOutOfBoundsException("Invalid blockSize: " + blockSize);
    }
    doInit(blockSize, -1);
}

    public IntBigList(Collection<Integer> that){
    if (that instanceof IntBigList) {
        doAssign((IntBigList) that);
        doClone((IntBigList) that);
    } else {
        blockSize = BLOCK_SIZE;
        currIntBlock = new IntBlock();
        addIntBlock(0, currIntBlock);
        for (Object obj : that.toArray()) {
            add((Integer) obj);
        }
        assert (size() == that.size());
    }
}

    public void init() {
    clear();
}

    public void init(int... elems) {
    clear();
    for (int elem : elems) {
        add(elem);
    }
}

    public void init(Collection<Integer> that) {
    clear();
    addAll(that);
}

    /**
     * Returns block size used for this IntBigList.
     *
     * @return block size used for this IntBigList
     */
public int blockSize() {
    return blockSize;
}

    //---  
private IntBigList(int blockSize, int firstIntBlockSize){
    doInit(blockSize, firstIntBlockSize);
}

    void doInit(int blockSize, int firstIntBlockSize) {
    this.blockSize = blockSize;
    // First block will grow until it reaches blockSize   
    if (firstIntBlockSize <= 1) {
        currIntBlock = new IntBlock();
    } else {
        currIntBlock = new IntBlock(firstIntBlockSize);
    }
    addIntBlock(0, currIntBlock);
}

    @Override
public IntBigList copy() {
    return (IntBigList) super.copy();
}

    @Override
protected void doAssign(IIntList that) {
    IntBigList list = (IntBigList) that;
    this.blockSize = list.blockSize;
    this.currIntBlock = list.currIntBlock;
    this.currIntBlockEnd = list.currIntBlockEnd;
    this.currIntBlockStart = list.currIntBlockStart;
    this.currNode = list.currNode;
    this.root = list.root;
    this.size = list.size;
}

    @Override
protected void doClone(IIntList that) {
    IntBigList bigList = (IntBigList) that;
    bigList.check();
    bigList.releaseIntBlock();
    root = copy(bigList.root);
    currNode = null;
    currModify = 0;
    check();
}

    private IntBlockNode copy(IntBlockNode node) {
    IntBlockNode newNode = node.min();
    int index = newNode.block.size();
    IntBlockNode newRoot = new IntBlockNode(null, index, newNode.block.ref(), null, null);
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
public int getDefaultElem() {
    return 0;
}

    @Override
protected void finalize() {
    // This list will be garbage collected, so unref all referenced blocks   
    IntBlockNode node = root.min();
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
protected int doGet(int index) {
    int pos = getIntBlockIndex(index, false, 0);
    return currIntBlock.values.doGet(pos);
}

    @Override
protected int doSet(int index, int elem) {
    int pos = getIntBlockIndex(index, true, 0);
    int oldElem = currIntBlock.values.doGet(pos);
    currIntBlock.values.doSet(pos, elem);
    return oldElem;
}

    @Override
protected int doReSet(int index, int elem) {
    int pos = getIntBlockIndex(index, true, 0);
    int oldElem = currIntBlock.values.doGet(pos);
    currIntBlock.values.doSet(pos, elem);
    return oldElem;
}

    private void releaseIntBlock() {
    if (currModify != 0) {
        int modify = currModify;
        currModify = 0;
        modify(currNode, modify);
    }
    currNode = null;
}

    /**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currIntBlock to remember this last used block.
	 *
	 * @param index	list index (0 <= index <= size())
	 * @return		relative index within block
	 */
private int getIntBlockIndex(int index, boolean write, int modify) {
    // Determine block where specified index is located and store it in currIntBlock   
    if (currNode != null) {
        if (index >= currIntBlockStart && (index < currIntBlockEnd || index == currIntBlockEnd && size == index)) {
            // currIntBlock is already set correctly   
            if (write) {
                if (currIntBlock.isShared()) {
                    currIntBlock.unref();
                    currIntBlock = new IntBlock(currIntBlock);
                    currNode.setIntBlock(currIntBlock);
                }
            }
            currModify += modify;
            return index - currIntBlockStart;
        }
        releaseIntBlock();
    }
    boolean done = false;
    if (index == size) {
        if (currNode == null || currIntBlockEnd != size) {
            currNode = root.max();
            currIntBlock = currNode.getIntBlock();
            currIntBlockEnd = size;
            currIntBlockStart = size - currIntBlock.size();
        }
        if (modify != 0) {
            currNode.relativePosition += modify;
            IntBlockNode leftNode = currNode.getLeftSubTree();
            if (leftNode != null) {
                leftNode.relativePosition -= modify;
            }
        }
        done = true;
    } else if (index == 0) {
        if (currNode == null || currIntBlockStart != 0) {
            currNode = root.min();
            currIntBlock = currNode.getIntBlock();
            currIntBlockEnd = currIntBlock.size();
            currIntBlockStart = 0;
        }
        if (modify != 0) {
            root.relativePosition += modify;
        }
        done = true;
    }
    if (!done) {
        // Reset currIntBlockEnd, it will be then set by access()   
        currIntBlockEnd = 0;
        currNode = access(index, modify);
        currIntBlock = currNode.getIntBlock();
        currIntBlockStart = currIntBlockEnd - currIntBlock.size();
    }
    assert (index >= currIntBlockStart && index <= currIntBlockEnd);
    if (write) {
        if (currIntBlock.isShared()) {
            currIntBlock.unref();
            currIntBlock = new IntBlock(currIntBlock);
            currNode.setIntBlock(currIntBlock);
        }
    }
    return index - currIntBlockStart;
}

    void checkNode(IntBlockNode node) {
    assert ((node.block.size() > 0 || node == root) && node.block.size() <= blockSize);
    IntBlockNode child = node.getLeftSubTree();
    assert (child == null || child.parent == node);
    child = node.getRightSubTree();
    assert (child == null || child.parent == node);
}

    void checkHeight(IntBlockNode node) {
    IntBlockNode left = node.getLeftSubTree();
    IntBlockNode right = node.getRightSubTree();
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
        assert (currNode.block == currIntBlock);
        assert (currIntBlockStart >= 0 && currIntBlockEnd <= size && currIntBlockStart <= currIntBlockEnd);
        assert (currIntBlockStart + currIntBlock.size() == currIntBlockEnd);
    }
    if (root == null) {
        assert (size == 0);
        return;
    }
    checkHeight(root);
    IntBlockNode oldCurrNode = currNode;
    int oldCurrModify = currModify;
    if (currModify != 0) {
        currNode = null;
        currModify = 0;
        modify(oldCurrNode, oldCurrModify);
    }
    IntBlockNode node = root;
    checkNode(node);
    int index = node.relativePosition;
    while (node.left != null) {
        node = node.left;
        checkNode(node);
        assert (node.relativePosition < 0);
        index += node.relativePosition;
    }
    IntBlock block = node.getIntBlock();
    assert (block.size() == index);
    int lastIndex = index;
    while (lastIndex < size()) {
        node = root;
        index = node.relativePosition;
        int searchIndex = lastIndex + 1;
        while (true) {
            checkNode(node);
            block = node.getIntBlock();
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
        block = node.getIntBlock();
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
protected boolean doAdd(int index, int element) {
    if (index == -1) {
        index = size;
    }
    // Insert   
    int pos = getIntBlockIndex(index, true, 1);
    // If there is still place in the current block: insert in current block   
    int maxSize = (index == size || index == 0) ? blockSize * 9 / 10 : blockSize;
    // The second part of the condition is a work around to handle the case of insertion as position 0 correctly   
    // where blockSize() is 2 (the new block would then be added after the current one)   
    if (currIntBlock.size() < maxSize || (currIntBlock.size() == 1 && currIntBlock.size() < blockSize)) {
        currIntBlock.values.doAdd(pos, element);
        currIntBlockEnd++;
    } else {
        // No place any more in current block   
        IntBlock newIntBlock = new IntBlock(blockSize);
        if (index == size) {
            // Insert new block at tail   
            newIntBlock.values.doAdd(0, element);
            // Subtract 1 because getIntBlockIndex() has already added 1   
            modify(currNode, -1);
            addIntBlock(size + 1, newIntBlock);
            IntBlockNode lastNode = currNode.next();
            currNode = lastNode;
            currIntBlock = currNode.block;
            currIntBlockStart = currIntBlockEnd;
            currIntBlockEnd++;
        } else if (index == 0) {
            // Insert new block at head   
            newIntBlock.values.doAdd(0, element);
            // Subtract 1 because getIntBlockIndex() has already added 1   
            modify(currNode, -1);
            addIntBlock(1, newIntBlock);
            IntBlockNode firstNode = currNode.previous();
            currNode = firstNode;
            currIntBlock = currNode.block;
            currIntBlockStart = 0;
            currIntBlockEnd = 1;
        } else {
            // Split block for insert   
            int nextIntBlockLen = blockSize / 2;
            int blockLen = blockSize - nextIntBlockLen;
            newIntBlock.values.init(nextIntBlockLen, 0);
            IntGapList.copy(currIntBlock.values, blockLen, newIntBlock.values, 0, nextIntBlockLen);
            currIntBlock.values.remove(blockLen, blockSize - blockLen);
            // Subtract 1 more because getIntBlockIndex() has already added 1   
            modify(currNode, -nextIntBlockLen - 1);
            addIntBlock(currIntBlockEnd - nextIntBlockLen, newIntBlock);
            if (pos < blockLen) {
                // Insert element in first block   
                currIntBlock.values.doAdd(pos, element);
                currIntBlockEnd = currIntBlockStart + blockLen + 1;
                modify(currNode, 1);
            } else {
                // Insert element in second block   
                currNode = currNode.next();
                modify(currNode, 1);
                currIntBlock = currNode.block;
                currIntBlock.values.doAdd(pos - blockLen, element);
                currIntBlockStart += blockLen;
                currIntBlockEnd++;
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
private void modify(IntBlockNode node, int modify) {
    if (node == currNode) {
        modify += currModify;
        currModify = 0;
    } else {
        releaseIntBlock();
    }
    if (modify == 0) {
        return;
    }
    if (node.relativePosition < 0) {
        // Left node   
        IntBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        IntBlockNode pp = node.parent;
        assert (pp.getLeftSubTree() == node);
        boolean parentRight = true;
        while (true) {
            IntBlockNode p = pp.parent;
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
        IntBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        IntBlockNode parent = node.parent;
        if (parent != null) {
            assert (parent.getRightSubTree() == node);
            boolean parentLeft = true;
            while (true) {
                IntBlockNode p = parent.parent;
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

    private IntBlockNode doRemove(IntBlockNode node) {
    IntBlockNode p = node.parent;
    IntBlockNode newNode = node.removeSelf();
    IntBlockNode n = newNode;
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
protected boolean doAddAll(int index, int[] array) {
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
    int addPos = getIntBlockIndex(index, true, 0);
    IntBlock addIntBlock = currIntBlock;
    int space = blockSize - addIntBlock.size();
    int addLen = array.length;
    if (addLen <= space) {
        // All elements can be added to current block   
        currIntBlock.values.addAll(addPos, array);
        modify(currNode, addLen);
        size += addLen;
        currIntBlockEnd += addLen;
    } else {
        if (index == size) {
            // Add elements at end   
            for (int i = 0; i < space; i++) {
                currIntBlock.values.add(addPos, array[i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                IntBlock nextIntBlock = new IntBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextIntBlock.values.add(i, array[done + i]);
                }
                done += add;
                todo -= add;
                addIntBlock(size + done, nextIntBlock);
            }
            size += addLen;
            currNode = currNode.next();
            currIntBlock = currNode.block;
            currIntBlockStart = currIntBlockEnd + space;
            currIntBlockEnd = currIntBlockStart + addLen - space;
        } else if (index == 0) {
            // Add elements at head   
            assert (addPos == 0);
            for (int i = 0; i < space; i++) {
                currIntBlock.values.add(addPos, array[addLen - space + i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                IntBlock nextIntBlock = new IntBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextIntBlock.values.add(i, array[done + i]);
                }
                done += add;
                todo -= add;
                addIntBlock(0, nextIntBlock);
            }
            size += addLen;
            currNode = currNode.previous();
            currIntBlock = currNode.block;
            currIntBlockStart = 0;
            currIntBlockEnd = addLen - space;
        } else {
            // Add elements to several blocks   
            // Handle first block   
            IntGapList list = IntGapList.create(array);
            int remove = currIntBlock.values.size() - addPos;
            if (remove > 0) {
                list.addAll(currIntBlock.values.getAll(addPos, remove));
                currIntBlock.values.remove(addPos, remove);
                modify(currNode, -remove);
                size -= remove;
                currIntBlockEnd -= remove;
            }
            int s = currIntBlock.values.size() + list.size();
            int numIntBlocks = (s - 1) / blockSize + 1;
            assert (numIntBlocks > 1);
            int has = currIntBlock.values.size();
            int should = s / numIntBlocks;
            int start = 0;
            int end = 0;
            if (has < should) {
                // Elements must be added to first block   
                int add = should - has;
                IIntList sublist = list.getAll(0, add);
                currIntBlock.values.addAll(addPos, sublist);
                modify(currNode, add);
                start += add;
                assert (currIntBlock.values.size() == should);
                s -= should;
                numIntBlocks--;
                size += add;
                currIntBlockEnd += add;
                end = currIntBlockEnd;
            } else if (has > should) {
                // Elements must be moved from first to second block   
                IntBlock nextIntBlock = new IntBlock(blockSize);
                int move = has - should;
                nextIntBlock.values.addAll(currIntBlock.values.getAll(currIntBlock.values.size() - move, move));
                currIntBlock.values.remove(currIntBlock.values.size() - move, move);
                modify(currNode, -move);
                assert (currIntBlock.values.size() == should);
                s -= should;
                numIntBlocks--;
                currIntBlockEnd -= move;
                end = currIntBlockEnd;
                should = s / numIntBlocks;
                int add = should - move;
                assert (add >= 0);
                IIntList sublist = list.getAll(0, add);
                nextIntBlock.values.addAll(move, sublist);
                start += add;
                assert (nextIntBlock.values.size() == should);
                s -= should;
                numIntBlocks--;
                size += add;
                end += add;
                addIntBlock(end, nextIntBlock);
            } else {
                s -= should;
                numIntBlocks--;
            }
            check();
            IntBlockNode node = currNode;
            while (numIntBlocks > 0) {
                int add = s / numIntBlocks;
                assert (add > 0);
                IIntList sublist = list.getAll(start, add);
                IntBlock nextIntBlock = new IntBlock();
                nextIntBlock.values.clear();
                nextIntBlock.values.addAll(sublist);
                start += add;
                assert (nextIntBlock.values.size() == add);
                s -= add;
                addIntBlock(end, nextIntBlock);
                assert (node.next().block == nextIntBlock);
                node = node.next();
                end += add;
                size += add;
                numIntBlocks--;
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
    currIntBlock = null;
    currIntBlockStart = 0;
    currIntBlockEnd = 0;
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
    int startPos = getIntBlockIndex(index, true, 0);
    IntBlockNode startNode = currNode;
    int endPos = getIntBlockIndex(index + len - 1, true, 0);
    IntBlockNode endNode = currNode;
    if (startNode == endNode) {
        // Delete from single block   
        getIntBlockIndex(index, true, -len);
        currIntBlock.values.remove(startPos, len);
        if (currIntBlock.values.isEmpty()) {
            IntBlockNode oldCurrNode = currNode;
            releaseIntBlock();
            IntBlockNode node = doRemove(oldCurrNode);
            merge(node);
        } else {
            currIntBlockEnd -= len;
            merge(currNode);
        }
        size -= len;
    } else {
        // Delete from start block   
        check();
        int startLen = startNode.block.size() - startPos;
        getIntBlockIndex(index, true, -startLen);
        // TODO should that be modify?   
        startNode.block.values.remove(startPos, startLen);
        assert (startNode == currNode);
        if (currIntBlock.values.isEmpty()) {
            releaseIntBlock();
            doRemove(startNode);
            startNode = null;
        }
        len -= startLen;
        size -= startLen;
        //check();   
        while (len > 0) {
            currNode = null;
            getIntBlockIndex(index, true, 0);
            int s = currIntBlock.size();
            if (s <= len) {
                modify(currNode, -s);
                IntBlockNode oldCurrNode = currNode;
                releaseIntBlock();
                doRemove(oldCurrNode);
                if (oldCurrNode == endNode) {
                    endNode = null;
                }
                len -= s;
                size -= s;
                check();
            } else {
                modify(currNode, -len);
                currIntBlock.values.remove(0, len);
                size -= len;
                break;
            }
        }
        releaseIntBlock();
        check();
        getIntBlockIndex(index, false, 0);
        merge(currNode);
    }
    if (DUMP)
        dump();
    if (CHECK)
        check();
}

    void merge(IntBlockNode node) {
    if (node == null) {
        return;
    }
    final int minIntBlockSize = Math.max(blockSize / 3, 1);
    if (node.block.values.size() >= minIntBlockSize) {
        return;
    }
    IntBlockNode oldCurrNode = node;
    IntBlockNode leftNode = node.previous();
    if (leftNode != null && leftNode.block.size() < minIntBlockSize) {
        // Merge with left block   
        int len = node.block.size();
        int dstSize = leftNode.getIntBlock().size();
        for (int i = 0; i < len; i++) {
            leftNode.block.values.add(0);
        }
        IntGapList.copy(node.block.values, 0, leftNode.block.values, dstSize, len);
        assert (leftNode.block.values.size() <= blockSize);
        modify(leftNode, +len);
        modify(oldCurrNode, -len);
        releaseIntBlock();
        doRemove(oldCurrNode);
    } else {
        IntBlockNode rightNode = node.next();
        if (rightNode != null && rightNode.block.size() < minIntBlockSize) {
            // Merge with right block   
            int len = node.block.size();
            for (int i = 0; i < len; i++) {
                rightNode.block.values.add(0, 0);
            }
            IntGapList.copy(node.block.values, 0, rightNode.block.values, 0, len);
            assert (rightNode.block.values.size() <= blockSize);
            modify(rightNode, +len);
            modify(oldCurrNode, -len);
            releaseIntBlock();
            doRemove(oldCurrNode);
        }
    }
}

    protected int doRemove(int index) {
    int pos = getIntBlockIndex(index, true, -1);
    int oldElem = currIntBlock.values.doRemove(pos);
    currIntBlockEnd--;
    final int minIntBlockSize = Math.max(blockSize / 3, 1);
    if (currIntBlock.size() < minIntBlockSize) {
        if (currIntBlock.size() == 0) {
            if (!isOnlyRootIntBlock()) {
                IntBlockNode oldCurrNode = currNode;
                releaseIntBlock();
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
public IntBigList unmodifiableList() {
    // Naming as in java.util.Collections#unmodifiableList   
    return new ImmutableIntBigList(this);
}

    @Override
protected void doEnsureCapacity(int minCapacity) {
    if (isOnlyRootIntBlock()) {
        if (minCapacity > blockSize) {
            minCapacity = blockSize;
        }
        currIntBlock.values.doEnsureCapacity(minCapacity);
    }
}

    @Override
public void trimToSize() {
    if (isOnlyRootIntBlock()) {
        currIntBlock.values.trimToSize();
    }
}

    @Override
protected IIntList doCreate(int capacity) {
    if (capacity <= blockSize) {
        return new IntBigList(this.blockSize);
    } else {
        return new IntBigList(this.blockSize, capacity);
    }
}

    @Override
public void sort(int index, int len) {
    checkRange(index, len);
    if (isOnlyRootIntBlock()) {
        currIntBlock.values.sort(index, len);
    } else {
        IntMergeSort.sort(this, index, index + len);
    }
}

    @Override
public int binarySearch(int index, int len, int key) {
    checkRange(index, len);
    if (isOnlyRootIntBlock()) {
        return currIntBlock.values.binarySearch(key);
    } else {
        return IntBinarySearch.binarySearch(this, key, 0, size());
    }
}

    private boolean isOnlyRootIntBlock() {
    return root.left == null && root.right == null;
}

    public IntBlockNode access(final int index, int modify) {
    return root.access(this, index, modify, false);
}

    //-----------------------------------------------------------------------  
/**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */
public void addIntBlock(int index, IntBlock obj) {
    if (root == null) {
        root = new IntBlockNode(null, index, obj, null, null);
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
public void removeIntBlock(int index) {
    root = root.remove(index);
}

    // --- Serialization ---  
/**
     * Serialize a IntBigList object.
     *
     * @serialData The length of the array backing the <tt>IntGapList</tt>
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
        oos.writeInt(doGet(i));
    }
}

    /**
     * Deserialize a IntBigList object.
 	 *
     * @param ois  input stream for serialization
     * @throws 	   IOException if serialization fails
     * @throws 	   ClassNotFoundException if serialization fails
     */

private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    int blockSize = ois.readInt();
    int size = ois.readInt();
    int firstIntBlockSize = (size <= blockSize) ? size : -1;
    doInit(blockSize, firstIntBlockSize);
    for (int i = 0; i < size; i++) {
        add(ois.readInt());
    }
}

    //-----------------------------------------------------------------------  
    /**
     * Implements an AVLNode which keeps the offset updated.
     * <p>
     * This node contains the real work.
     * TreeList is just there to implement {@link java.util.IIntList}.
     * The nodes don't know the index of the object they are holding.  They
     * do know however their position relative to their parent node.
     * This allows to calculate the index of a node while traversing the tree.
     * <p>
     * The Faedelung calculation stores a flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    static class IntBlockNode {

        IntBlockNode parent;

        /** The left child node or the predecessor if {@link #leftIsPrevious}.*/
        IntBlockNode left;

        /** Flag indicating that left reference is not a subtree but the predecessor. */
        boolean leftIsPrevious;

        /** The right child node or the successor if {@link #rightIsNext}. */
        IntBlockNode right;

        /** Flag indicating that right reference is not a subtree but the successor. */
        boolean rightIsNext;

        /** How many levels of left/right are below this one. */
        int height;

        /** The relative position, root holds absolute position. */
        int relativePosition;

        /** The stored block */
        IntBlock block;

        /**
         * Constructs a new node with a relative position.
         *
         * @param relativePosition  the relative position of the node
         * @param obj  the value for the node
         * @param rightFollower the node with the value following this one
         * @param leftFollower the node with the value leading this one
         */
private IntBlockNode(IntBlockNode parent, final int relativePosition, final IntBlock block, final IntBlockNode rightFollower, final IntBlockNode leftFollower){
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
public IntBlock getIntBlock() {
    return block;
}

        /**
         * Sets the value.
         *
         * @param obj  the value to store
         */
public void setIntBlock(IntBlock obj) {
    this.block = obj;
}

        private IntBlockNode access(IntBigList list, int index, int modify, boolean wasLeft) {
    assert (index >= 0);
    if (relativePosition == 0) {
        if (modify != 0) {
            relativePosition += modify;
        }
        return this;
    }
    if (list.currIntBlockEnd == 0) {
        list.currIntBlockEnd = relativePosition;
    }
    IntBlockNode leftNode = getLeftSubTree();
    int leftIndex = list.currIntBlockEnd - block.size();
    assert (leftIndex >= 0);
    if (index >= leftIndex && index < list.currIntBlockEnd) {
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
    if (index < list.currIntBlockEnd) {
        // left   
        IntBlockNode nextNode = getLeftSubTree();
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
        list.currIntBlockEnd += nextNode.relativePosition;
        return nextNode.access(list, index, modify, wasLeft);
    } else {
        // right   
        IntBlockNode nextNode = getRightSubTree();
        if (nextNode == null || wasLeft) {
            if (relativePosition > 0) {
                relativePosition += modify;
                IntBlockNode left = getLeftSubTree();
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
        list.currIntBlockEnd += nextNode.relativePosition;
        return nextNode.access(list, index, modify, wasLeft);
    }
}

        /**
         * Gets the next node in the list after this one.
         *
         * @return the next node
         */
public IntBlockNode next() {
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
public IntBlockNode previous() {
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
private IntBlockNode insert(int index, IntBlock obj) {
    assert (relativePosition != 0);
    final int indexRelativeToMe = index - relativePosition;
    if (indexRelativeToMe < 0) {
        return insertOnLeft(indexRelativeToMe, obj);
    } else {
        return insertOnRight(indexRelativeToMe, obj);
    }
}

        private IntBlockNode insertOnLeft(int indexRelativeToMe, IntBlock obj) {
    if (getLeftSubTree() == null) {
        int pos;
        if (relativePosition >= 0) {
            pos = -relativePosition;
        } else {
            pos = -block.size();
        }
        setLeft(new IntBlockNode(this, pos, obj, this, left), null);
    } else {
        setLeft(left.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition >= 0) {
        relativePosition += obj.size();
    }
    final IntBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        private IntBlockNode insertOnRight(int indexRelativeToMe, IntBlock obj) {
    if (getRightSubTree() == null) {
        setRight(new IntBlockNode(this, obj.size(), obj, right, this), null);
    } else {
        setRight(right.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition < 0) {
        relativePosition -= obj.size();
    }
    final IntBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        //-----------------------------------------------------------------------  
/**
         * Gets the left node, returning null if its a faedelung.
         */
public IntBlockNode getLeftSubTree() {
    return leftIsPrevious ? null : left;
}

        /**
         * Gets the right node, returning null if its a faedelung.
         */
public IntBlockNode getRightSubTree() {
    return rightIsNext ? null : right;
}

        /**
         * Gets the rightmost child of this node.
         *
         * @return the rightmost child (greatest index)
         */
public IntBlockNode max() {
    return getRightSubTree() == null ? this : right.max();
}

        /**
         * Gets the leftmost child of this node.
         *
         * @return the leftmost child (smallest index)
         */
public IntBlockNode min() {
    return getLeftSubTree() == null ? this : left.min();
}

        /**
         * Removes the node at a given position.
         *
         * @param index is the index of the element to be removed relative to the position of
         * the parent node of the current node.
         */
private IntBlockNode remove(final int index) {
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

        private IntBlockNode removeMax() {
    if (getRightSubTree() == null) {
        return removeSelf();
    }
    setRight(right.removeMax(), right.right);
    if (relativePosition < 0) {
    }
    recalcHeight();
    return balance();
}

        private IntBlockNode removeMin(int size) {
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
public IntBlockNode removeSelf() {
    IntBlockNode p = parent;
    IntBlockNode n = doRemoveSelf();
    if (n != null) {
        assert (p != n);
        n.parent = p;
    }
    return n;
}

        public IntBlockNode doRemoveSelf() {
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
        final IntBlockNode rightMin = right.min();
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
        final IntBlockNode leftMax = left.max();
        block = leftMax.block;
        if (rightIsNext) {
            right = leftMax.right;
        }
        final IntBlockNode leftPrevious = left.left;
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
private IntBlockNode balance() {
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
private int getOffset(final IntBlockNode node) {
    if (node == null) {
        return 0;
    }
    return node.relativePosition;
}

        /**
         * Sets the relative position.
         */
private int setOffset(final IntBlockNode node, final int newOffest) {
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
private int getHeight(final IntBlockNode node) {
    return node == null ? -1 : node.height;
}

        /**
         * Returns the height difference right - left
         */
private int heightRightMinusLeft() {
    return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
}

        private IntBlockNode rotateLeft() {
    assert (!rightIsNext);
    final IntBlockNode newTop = right;
    // can't be faedelung!   
    final IntBlockNode movedNode = getRightSubTree().getLeftSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    IntBlockNode p = this.parent;
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

        private IntBlockNode rotateRight() {
    assert (!leftIsPrevious);
    final IntBlockNode newTop = left;
    // can't be faedelung   
    final IntBlockNode movedNode = getLeftSubTree().getRightSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    IntBlockNode p = this.parent;
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
private void setLeft(final IntBlockNode node, final IntBlockNode previous) {
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
private void setRight(final IntBlockNode node, final IntBlockNode next) {
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
