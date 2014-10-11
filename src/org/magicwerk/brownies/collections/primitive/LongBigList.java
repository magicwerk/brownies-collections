package org.magicwerk.brownies.collections.primitive;
import org.magicwerk.brownies.collections.helper.ArraysHelper;
import org.magicwerk.brownies.collections.helper.primitive.BinarySearch;
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
import org.magicwerk.brownies.collections.helper.primitive.LongMergeSort;

/**
 * The first block (LongGapList) used grows dynamcically, all others
 * are allocated with fixed size. This is necessary to prevent starving
 * because of GC usage.
 *
 * @author Thomas Mauch
 * @version $Id: LongBigList.java 2477 2014-10-08 23:47:35Z origo $
 */
public class LongBigList extends ILongList {
	public static IIntList of(int[] values) {
		return new ImmutableIntListArrayInt(values);
	}

	public static IIntList of(Integer[] values) {
		return new ImmutableIntListArrayInteger(values);
	}

	public static IIntList of(List<Integer> values) {
		return new ImmutableIntListListInteger(values);
	}

    static class ImmutableIntListArrayInt extends ImmutableIntList {
    	int[] values;

    	public ImmutableIntListArrayInt(int[] values) {
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

    static class ImmutableIntListArrayInteger extends ImmutableIntList {
    	Integer[] values;

    	public ImmutableIntListArrayInteger(Integer[] values) {
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

    static class ImmutableIntListListInteger extends ImmutableIntList {
    	List<Integer> values;

    	public ImmutableIntListListInteger(List<Integer> values) {
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
			return BinarySearch.binarySearch(this, key, index, index+len);
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
     * An immutable version of a LongBigList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableLongBigList extends LongBigList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
protected ImmutableLongBigList(LongBigList that){
    super(true, that);
}

        @Override
protected boolean doAdd(int index, long elem) {
    error();
    return false;
}

        @Override
protected boolean doAddAll(int index, long[] elems) {
    error();
    return false;
}

        @Override
protected long doSet(int index, long elem) {
    error();
    return 0;
}

        @Override
protected void doSetAll(int index, long[] elems) {
    error();
}

        @Override
protected long doReSet(int index, long elem) {
    error();
    return 0;
}

        @Override
protected long doRemove(int index) {
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
    
    public static class LongBlock implements Serializable {

        private LongGapList values;

        private int refCount;

        public LongBlock(){
    values = new LongGapList();
    refCount = 1;
}

        public LongBlock(int capacity){
    values = new LongGapList(capacity);
    refCount = 1;
}

        public LongBlock(LongBlock that){
    values = new LongGapList(that.values.capacity());
    values.init(that.values.getArray(0, that.values.size()));
    refCount = 1;
}

        public boolean isShared() {
    return refCount > 1;
}

        public LongBlock ref() {
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

    /** Number of elements stored in this LongBigList */
    private int size;

    /** The root node in the tree */
    private LongBlockNode root;

    /** Current node */
    private LongBlockNode currNode;

    /** LongBlock of current node */
    private LongBlock currLongBlock;

    /** Start index of current block */
    private int currLongBlockStart;

    /** End index of current block */
    private int currLongBlockEnd;

    /** Modify value which must be applied before this block is not current any more */
    private int currModify;

    /**
     * Constructor used internally, e.g. for ImmutableLongBigList.
     *
     * @param copy true to copy all instance values from source,
     *             if false nothing is done
     * @param that list to copy
     */
protected LongBigList(boolean copy, LongBigList that){
    if (copy) {
        this.blockSize = that.blockSize;
        this.currLongBlock = that.currLongBlock;
        this.currLongBlockStart = that.currLongBlockStart;
        this.currLongBlockEnd = that.currLongBlockEnd;
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
// This separate method is needed as the varargs variant creates the LongGapList with specific size  
public static LongBigList create() {
    return new LongBigList();
}

    /**
     * Create new list with specified elements.
     *
     * @param coll      collection with element
     * @return          created list
     * @param        type of elements stored in the list
     */
public static LongBigList create(Collection<Long> coll) {
    return new LongBigList(coll);
}

    /**
	 * Create new list with specified elements.
	 *
	 * @param elems 	array with elements
	 * @return 			created list
	 * @param  		type of elements stored in the list
	 */
public static LongBigList create(long... elems) {
    LongBigList list = new LongBigList();
    for (long elem : elems) {
        list.add(elem);
    }
    return list;
}

    /**
	 * Default constructor.
	 * The default block size is used.
	 */
public LongBigList(){
    this(BLOCK_SIZE);
}

    /**
	 * Constructor.
	 *
	 * @param blockSize block size
	 */
public LongBigList(int blockSize){
    doInit(blockSize, -1);
}

    public LongBigList(Collection<Long> that){
    if (that instanceof LongBigList) {
        doAssign((LongBigList) that);
        doClone((LongBigList) that);
    } else {
        blockSize = BLOCK_SIZE;
        currLongBlock = new LongBlock();
        addLongBlock(0, currLongBlock);
        for (long elem : that.toArray()) {
            add((E) elem);
        }
        assert (size() == that.size());
    }
}

    public void init() {
    clear();
}

    public void init(long... elems) {
    clear();
    for (long elem : elems) {
        add(elem);
    }
}

    public void init(Collection<Long> that) {
    clear();
    addAll(that);
}

    /**
     * Returns block size used for this LongBigList.
     *
     * @return block size used for this LongBigList
     */
public int blockSize() {
    return blockSize;
}

    //---  
private LongBigList(int blockSize, int firstLongBlockSize){
    doInit(blockSize, firstLongBlockSize);
}

    void doInit(int blockSize, int firstLongBlockSize) {
    this.blockSize = blockSize;
    // First block will grow until it reaches blockSize   
    if (firstLongBlockSize <= 1) {
        currLongBlock = new LongBlock();
    } else {
        currLongBlock = new LongBlock(firstLongBlockSize);
    }
    addLongBlock(0, currLongBlock);
}

    @Override
public LongBigList copy() {
    return (LongBigList) super.copy();
}

    @Override
protected void doAssign(ILongList that) {
    LongBigList list = (LongBigList) that;
    this.blockSize = list.blockSize;
    this.currLongBlock = list.currLongBlock;
    this.currLongBlockEnd = list.currLongBlockEnd;
    this.currLongBlockStart = list.currLongBlockStart;
    this.currNode = list.currNode;
    this.root = list.root;
    this.size = list.size;
}

    @Override
protected void doClone(ILongList that) {
    LongBigList bigList = (LongBigList) that;
    bigList.check();
    bigList.releaseLongBlock();
    root = copy(bigList.root);
    currNode = null;
    currModify = 0;
    check();
}

    private LongBlockNode copy(LongBlockNode node) {
    LongBlockNode newNode = node.min();
    int index = newNode.block.size();
    LongBlockNode newRoot = new LongBlockNode(null, index, newNode.block.ref(), null, null);
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
public long getDefaultElem() {
    return 0;
}

    @Override
protected void finalize() {
    // This list will be garbage collected, so unref all referenced blocks   
    LongBlockNode node = root.min();
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
protected long doGet(int index) {
    int pos = getLongBlockIndex(index, false, 0);
    return currLongBlock.values.doGet(pos);
}

    @Override
protected long doSet(int index, long elem) {
    int pos = getLongBlockIndex(index, true, 0);
    long oldElem = currLongBlock.values.doGet(pos);
    currLongBlock.values.doSet(pos, elem);
    return oldElem;
}

    @Override
protected long doReSet(int index, long elem) {
    int pos = getLongBlockIndex(index, true, 0);
    long oldElem = currLongBlock.values.doGet(pos);
    currLongBlock.values.doSet(pos, elem);
    return oldElem;
}

    private void releaseLongBlock() {
    if (currModify != 0) {
        int modify = currModify;
        currModify = 0;
        modify(currNode, modify);
    }
    currNode = null;
}

    /**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currLongBlock to remember this last used block.
	 *
	 * @param index	list index (0 <= index <= size())
	 * @return		relative index within block
	 */
private int getLongBlockIndex(int index, boolean write, int modify) {
    // Determine block where specified index is located and store it in currLongBlock   
    if (currNode != null) {
        if (index >= currLongBlockStart && (index < currLongBlockEnd || index == currLongBlockEnd && size == index)) {
            // currLongBlock is already set correctly   
            if (write) {
                if (currLongBlock.isShared()) {
                    currLongBlock.unref();
                    currLongBlock = new LongBlock(currLongBlock);
                    currNode.setLongBlock(currLongBlock);
                }
            }
            currModify += modify;
            return index - currLongBlockStart;
        }
        releaseLongBlock();
    }
    boolean done = false;
    if (index == size) {
        if (currNode == null || currLongBlockEnd != size) {
            currNode = root.max();
            currLongBlock = currNode.getLongBlock();
            currLongBlockEnd = size;
            currLongBlockStart = size - currLongBlock.size();
        }
        if (modify != 0) {
            currNode.relativePosition += modify;
            LongBigList.LongBlockNode leftNode = currNode.getLeftSubTree();
            if (leftNode != null) {
                leftNode.relativePosition -= modify;
            }
        }
        done = true;
    } else if (index == 0) {
        if (currNode == null || currLongBlockStart != 0) {
            currNode = root.min();
            currLongBlock = currNode.getLongBlock();
            currLongBlockEnd = currLongBlock.size();
            currLongBlockStart = 0;
        }
        if (modify != 0) {
            root.relativePosition += modify;
        }
        done = true;
    }
    if (!done) {
        // Reset currLongBlockEnd, it will be then set by access()   
        currLongBlockEnd = 0;
        currNode = access(index, modify);
        currLongBlock = currNode.getLongBlock();
        currLongBlockStart = currLongBlockEnd - currLongBlock.size();
    }
    assert (index >= currLongBlockStart && index <= currLongBlockEnd);
    if (write) {
        if (currLongBlock.isShared()) {
            currLongBlock.unref();
            currLongBlock = new LongBlock(currLongBlock);
            currNode.setLongBlock(currLongBlock);
        }
    }
    return index - currLongBlockStart;
}

    void checkNode(LongBlockNode node) {
    assert ((node.block.size() > 0 || node == root) && node.block.size() <= blockSize);
    LongBlockNode child = node.getLeftSubTree();
    assert (child == null || child.parent == node);
    child = node.getRightSubTree();
    assert (child == null || child.parent == node);
}

    void checkHeight(LongBlockNode node) {
    LongBlockNode left = node.getLeftSubTree();
    LongBlockNode right = node.getRightSubTree();
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
    if (true) {
        return;
    }
    //TODO   
    if (currNode != null) {
        assert (currNode.block == currLongBlock);
        assert (currLongBlockStart >= 0 && currLongBlockEnd <= size && currLongBlockStart <= currLongBlockEnd);
        assert (currLongBlockStart + currLongBlock.size() == currLongBlockEnd);
    }
    if (root == null) {
        assert (size == 0);
        return;
    }
    checkHeight(root);
    LongBlockNode oldCurrNode = currNode;
    int oldCurrModify = currModify;
    if (currModify != 0) {
        currNode = null;
        currModify = 0;
        modify(oldCurrNode, oldCurrModify);
    }
    LongBlockNode node = root;
    checkNode(node);
    int index = node.relativePosition;
    while (node.left != null) {
        node = node.left;
        checkNode(node);
        assert (node.relativePosition < 0);
        index += node.relativePosition;
    }
    LongBlock block = node.getLongBlock();
    assert (block.size() == index);
    int lastIndex = index;
    while (lastIndex < size()) {
        node = root;
        index = node.relativePosition;
        int searchIndex = lastIndex + 1;
        while (true) {
            checkNode(node);
            block = node.getLongBlock();
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
        block = node.getLongBlock();
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
protected boolean doAdd(int index, long element) {
    if (index == -1) {
        index = size;
    }
    // Insert   
    int pos = getLongBlockIndex(index, true, 1);
    // If there is still place in the current block: insert in current block   
    int maxSize = (index == size || index == 0) ? blockSize * 9 / 10 : blockSize;
    if (currLongBlock.size() < maxSize) {
        currLongBlock.values.doAdd(pos, element);
        currLongBlockEnd++;
    } else {
        // No place any more in current block   
        LongBlock nextLongBlock = new LongBlock(blockSize);
        if (index == size) {
            // Insert new block at tail   
            nextLongBlock.values.doAdd(0, element);
            modify(currNode, -1);
            addLongBlock(size + 1, nextLongBlock);
            LongBlockNode lastNode = currNode.next();
            currNode = lastNode;
            currLongBlock = currNode.block;
            currLongBlockStart = currLongBlockEnd;
            currLongBlockEnd++;
        } else if (index == 0) {
            // Insert new block at head   
            nextLongBlock.values.doAdd(0, element);
            modify(currNode, -1);
            addLongBlock(1, nextLongBlock);
            LongBlockNode firstNode = currNode.previous();
            currNode = firstNode;
            currLongBlock = currNode.block;
            currLongBlockStart = 0;
            currLongBlockEnd = 1;
        } else {
            // Split block for insert   
            int nextLongBlockLen = blockSize / 2;
            int blockLen = blockSize - nextLongBlockLen;
            nextLongBlock.values.init(nextLongBlockLen, null);
            LongGapList.copy(currLongBlock.values, blockLen, nextLongBlock.values, 0, nextLongBlockLen);
            currLongBlock.values.remove(blockLen, blockSize - blockLen);
            // Subtract 1 more because getLongBlockIndex() has already added 1   
            modify(currNode, -nextLongBlockLen - 1);
            addLongBlock(currLongBlockEnd - nextLongBlockLen, nextLongBlock);
            if (pos < blockLen) {
                // Insert element in first block   
                currLongBlock.values.doAdd(pos, element);
                currLongBlockEnd = currLongBlockStart + blockLen + 1;
                modify(currNode, 1);
            } else {
                // Insert element in second block   
                currNode = currNode.next();
                modify(currNode, 1);
                currLongBlock = currNode.block;
                currLongBlock.values.doAdd(pos - blockLen, element);
                currLongBlockStart += blockLen;
                currLongBlockEnd++;
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
private void modify(LongBlockNode node, int modify) {
    if (node == currNode) {
        modify += currModify;
        currModify = 0;
    } else {
        releaseLongBlock();
    }
    if (modify == 0) {
        return;
    }
    if (node.relativePosition < 0) {
        // Left node   
        LongBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        LongBlockNode pp = node.parent;
        assert (pp.getLeftSubTree() == node);
        boolean parentRight = true;
        while (true) {
            LongBlockNode p = pp.parent;
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
        LongBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        LongBlockNode parent = node.parent;
        if (parent != null) {
            assert (parent.getRightSubTree() == node);
            boolean parentLeft = true;
            while (true) {
                LongBlockNode p = parent.parent;
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

    private LongBlockNode doRemove(LongBlockNode node) {
    LongBlockNode p = node.parent;
    LongBlockNode newNode = node.removeSelf();
    LongBlockNode n = newNode;
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
protected boolean doAddAll(int index, long[] array) {
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
    int addPos = getLongBlockIndex(index, true, 0);
    LongBlock addLongBlock = currLongBlock;
    int space = blockSize - addLongBlock.size();
    int addLen = array.length;
    if (addLen <= space) {
        // All elements can be added to current block   
        currLongBlock.values.addAll(addPos, array);
        modify(currNode, addLen);
        size += addLen;
        currLongBlockEnd += addLen;
    } else {
        if (index == size) {
            // Add elements at end   
            for (int i = 0; i < space; i++) {
                currLongBlock.values.add(addPos, array[i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                LongBlock nextLongBlock = new LongBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextLongBlock.values.add(i, array[done + i]);
                }
                done += add;
                todo -= add;
                addLongBlock(size + done, nextLongBlock);
            }
            size += addLen;
            currNode = currNode.next();
            currLongBlock = currNode.block;
            currLongBlockStart = currLongBlockEnd + space;
            currLongBlockEnd = currLongBlockStart + addLen - space;
        } else if (index == 0) {
            // Add elements at head   
            assert (addPos == 0);
            for (int i = 0; i < space; i++) {
                currLongBlock.values.add(addPos, array[addLen - space + i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                LongBlock nextLongBlock = new LongBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextLongBlock.values.add(i, array[done + i]);
                }
                done += add;
                todo -= add;
                addLongBlock(0, nextLongBlock);
            }
            size += addLen;
            currNode = currNode.previous();
            currLongBlock = currNode.block;
            currLongBlockStart = 0;
            currLongBlockEnd = addLen - space;
        } else {
            // Add elements to several blocks   
            // Handle first block   
            LongGapList list = LongGapList.create(array);
            int remove = currLongBlock.values.size() - addPos;
            if (remove > 0) {
                list.addAll(currLongBlock.values.getAll(addPos, remove));
                currLongBlock.values.remove(addPos, remove);
                modify(currNode, -remove);
                size -= remove;
                currLongBlockEnd -= remove;
            }
            int s = currLongBlock.values.size() + list.size();
            int numLongBlocks = (s - 1) / blockSize + 1;
            assert (numLongBlocks > 1);
            int has = currLongBlock.values.size();
            int should = s / numLongBlocks;
            int start = 0;
            int end = 0;
            if (has < should) {
                // Elements must be added to first block   
                int add = should - has;
                ILongList sublist = list.getAll(0, add);
                currLongBlock.values.addAll(addPos, sublist);
                modify(currNode, add);
                start += add;
                assert (currLongBlock.values.size() == should);
                s -= should;
                numLongBlocks--;
                size += add;
                currLongBlockEnd += add;
                end = currLongBlockEnd;
            } else if (has > should) {
                // Elements must be moved from first to second block   
                LongBlock nextLongBlock = new LongBlock(blockSize);
                int move = has - should;
                nextLongBlock.values.addAll(currLongBlock.values.getAll(currLongBlock.values.size() - move, move));
                currLongBlock.values.remove(currLongBlock.values.size() - move, move);
                modify(currNode, -move);
                assert (currLongBlock.values.size() == should);
                s -= should;
                numLongBlocks--;
                currLongBlockEnd -= move;
                end = currLongBlockEnd;
                should = s / numLongBlocks;
                int add = should - move;
                assert (add >= 0);
                ILongList sublist = list.getAll(0, add);
                nextLongBlock.values.addAll(move, sublist);
                start += add;
                assert (nextLongBlock.values.size() == should);
                s -= should;
                numLongBlocks--;
                size += add;
                end += add;
                addLongBlock(end, nextLongBlock);
            } else {
                s -= should;
                numLongBlocks--;
            }
            check();
            while (numLongBlocks > 0) {
                int add = s / numLongBlocks;
                assert (add > 0);
                ILongList sublist = list.getAll(start, add);
                LongBlock nextLongBlock = new LongBlock();
                nextLongBlock.values.init(sublist);
                start += add;
                assert (nextLongBlock.values.size() == add);
                s -= add;
                end += add;
                addLongBlock(end, nextLongBlock);
                size += add;
                numLongBlocks--;
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
    currLongBlock = null;
    currLongBlockStart = 0;
    currLongBlockEnd = 0;
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
    int startPos = getLongBlockIndex(index, true, 0);
    LongBlockNode startNode = currNode;
    int endPos = getLongBlockIndex(index + len - 1, true, 0);
    LongBlockNode endNode = currNode;
    if (startNode == endNode) {
        // Delete from single block   
        getLongBlockIndex(index, true, -len);
        currLongBlock.values.remove(startPos, len);
        if (currLongBlock.values.isEmpty()) {
            LongBlockNode oldCurrNode = currNode;
            releaseLongBlock();
            LongBlockNode node = doRemove(oldCurrNode);
            merge(node);
        } else {
            currLongBlockEnd -= len;
            merge(currNode);
        }
        size -= len;
    } else {
        // Delete from start block   
        check();
        int startLen = startNode.block.size() - startPos;
        getLongBlockIndex(index, true, -startLen);
        // TODO should that be modify?   
        startNode.block.values.remove(startPos, startLen);
        assert (startNode == currNode);
        if (currLongBlock.values.isEmpty()) {
            releaseLongBlock();
            doRemove(startNode);
            startNode = null;
        }
        len -= startLen;
        size -= startLen;
        //check();   
        while (len > 0) {
            currNode = null;
            getLongBlockIndex(index, true, 0);
            int s = currLongBlock.size();
            if (s <= len) {
                modify(currNode, -s);
                LongBlockNode oldCurrNode = currNode;
                releaseLongBlock();
                doRemove(oldCurrNode);
                if (oldCurrNode == endNode) {
                    endNode = null;
                }
                len -= s;
                size -= s;
                check();
            } else {
                modify(currNode, -len);
                currLongBlock.values.remove(0, len);
                size -= len;
                break;
            }
        }
        releaseLongBlock();
        check();
        getLongBlockIndex(index, false, 0);
        merge(currNode);
    }
    if (DUMP)
        dump();
    if (CHECK)
        check();
}

    void merge(LongBlockNode node) {
    if (node == null) {
        return;
    }
    final int minLongBlockSize = Math.max(blockSize / 3, 1);
    if (node.block.values.size() >= minLongBlockSize) {
        return;
    }
    LongBlockNode oldCurrNode = node;
    LongBlockNode leftNode = node.previous();
    if (leftNode != null && leftNode.block.size() < minLongBlockSize) {
        // Merge with left block   
        int len = node.block.size();
        int dstSize = leftNode.getLongBlock().size();
        for (int i = 0; i < len; i++) {
            leftNode.block.values.add(null);
        }
        LongGapList.copy(node.block.values, 0, leftNode.block.values, dstSize, len);
        assert (leftNode.block.values.size() <= blockSize);
        modify(leftNode, +len);
        modify(oldCurrNode, -len);
        releaseLongBlock();
        doRemove(oldCurrNode);
    } else {
        LongBlockNode rightNode = node.next();
        if (rightNode != null && rightNode.block.size() < minLongBlockSize) {
            // Merge with right block   
            int len = node.block.size();
            for (int i = 0; i < len; i++) {
                rightNode.block.values.add(0, null);
            }
            LongGapList.copy(node.block.values, 0, rightNode.block.values, 0, len);
            assert (rightNode.block.values.size() <= blockSize);
            modify(rightNode, +len);
            modify(oldCurrNode, -len);
            releaseLongBlock();
            doRemove(oldCurrNode);
        }
    }
}

    protected long doRemove(int index) {
    int pos = getLongBlockIndex(index, true, -1);
    long oldElem = currLongBlock.values.doRemove(pos);
    currLongBlockEnd--;
    final int minLongBlockSize = Math.max(blockSize / 3, 1);
    if (currLongBlock.size() < minLongBlockSize) {
        if (currLongBlock.size() == 0) {
            if (!isOnlyRootLongBlock()) {
                LongBlockNode oldCurrNode = currNode;
                releaseLongBlock();
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
public LongBigList unmodifiableList() {
    // Naming as in java.util.Collections#unmodifiableList   
    return new ImmutableLongBigList(this);
}

    @Override
protected void doEnsureCapacity(int minCapacity) {
    if (isOnlyRootLongBlock()) {
        if (minCapacity > blockSize) {
            minCapacity = blockSize;
        }
        currLongBlock.values.doEnsureCapacity(minCapacity);
    }
}

    @Override
public void trimToSize() {
    if (isOnlyRootLongBlock()) {
        currLongBlock.values.trimToSize();
    }
}

    @Override
protected ILongList doCreate(int capacity) {
    if (capacity <= blockSize) {
        return new LongBigList(this.blockSize);
    } else {
        return new LongBigList(this.blockSize, capacity);
    }
}

    @Override
public void sort(int index, int len) {
    checkRange(index, len);
    if (isOnlyRootLongBlock()) {
        currLongBlock.values.sort(index, len);
    } else {
        LongMergeSort.sort(this, index, index + len);
    }
}

    @Override
public int binarySearch(int index, int len, long key) {
    checkRange(index, len);
    if (isOnlyRootLongBlock()) {
        return currLongBlock.values.binarySearch(key);
    } else {
        return Collections.binarySearch((ILongList) this, key);
    }
}

    private boolean isOnlyRootLongBlock() {
    return root.left == null && root.right == null;
}

    public LongBlockNode access(final int index, int modify) {
    return root.access(index, modify, false);
}

    //-----------------------------------------------------------------------  
/**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */
public void addLongBlock(int index, LongBlock obj) {
    if (root == null) {
        root = new LongBlockNode(null, index, obj, null, null);
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
public void removeLongBlock(int index) {
    root = root.remove(index);
}

    // --- Serialization ---  
/**
     * Serialize a LongBigList object.
     *
     * @serialData The length of the array backing the <tt>LongGapList</tt>
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
        oos.writeLong(doGet(i));
    }
}

    /**
     * Deserialize a LongBigList object.
 	 *
     * @param ois  input stream for serialization
     * @throws 	   IOException if serialization fails
     * @throws 	   ClassNotFoundException if serialization fails
     */

private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    int blockSize = ois.readInt();
    int size = ois.readInt();
    int firstLongBlockSize = (size <= blockSize) ? size : -1;
    doInit(blockSize, firstLongBlockSize);
    for (int i = 0; i < size; i++) {
        add(ois.readLong());
    }
}

    //-----------------------------------------------------------------------  
    /**
     * Implements an AVLNode which keeps the offset updated.
     * <p>
     * This node contains the real work.
     * TreeList is just there to implement {@link java.util.ILongList}.
     * The nodes don't know the index of the object they are holding.  They
     * do know however their position relative to their parent node.
     * This allows to calculate the index of a node while traversing the tree.
     * <p>
     * The Faedelung calculation stores a flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    class LongBlockNode {

        LongBlockNode parent;

        /** The left child node or the predecessor if {@link #leftIsPrevious}.*/
        LongBlockNode left;

        /** Flag indicating that left reference is not a subtree but the predecessor. */
        boolean leftIsPrevious;

        /** The right child node or the successor if {@link #rightIsNext}. */
        LongBlockNode right;

        /** Flag indicating that right reference is not a subtree but the successor. */
        boolean rightIsNext;

        /** How many levels of left/right are below this one. */
        int height;

        /** The relative position, root holds absolute position. */
        int relativePosition;

        /** The stored block */
        LongBlock block;

        /**
         * Constructs a new node with a relative position.
         *
         * @param relativePosition  the relative position of the node
         * @param obj  the value for the node
         * @param rightFollower the node with the value following this one
         * @param leftFollower the node with the value leading this one
         */
private LongBlockNode(LongBlockNode parent, final int relativePosition, final LongBlock block, final LongBlockNode rightFollower, final LongBlockNode leftFollower){
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
public LongBlock getLongBlock() {
    return block;
}

        /**
         * Sets the value.
         *
         * @param obj  the value to store
         */
public void setLongBlock(LongBlock obj) {
    this.block = obj;
}

        private LongBlockNode access(final int index, int modify, boolean wasLeft) {
    assert (index >= 0);
    if (relativePosition == 0) {
        if (modify != 0) {
            relativePosition += modify;
        }
        return this;
    }
    if (currLongBlockEnd == 0) {
        currLongBlockEnd = relativePosition;
    }
    LongBlockNode leftNode = getLeftSubTree();
    int leftIndex = currLongBlockEnd - block.size();
    assert (leftIndex >= 0);
    if (index >= leftIndex && index < currLongBlockEnd) {
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
    if (index < currLongBlockEnd) {
        // left   
        LongBlockNode nextNode = getLeftSubTree();
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
        currLongBlockEnd += nextNode.relativePosition;
        return nextNode.access(index, modify, wasLeft);
    } else {
        // right   
        LongBlockNode nextNode = getRightSubTree();
        if (nextNode == null || wasLeft) {
            if (relativePosition > 0) {
                relativePosition += modify;
                LongBlockNode left = getLeftSubTree();
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
        currLongBlockEnd += nextNode.relativePosition;
        return nextNode.access(index, modify, wasLeft);
    }
}

        /**
         * Gets the next node in the list after this one.
         *
         * @return the next node
         */
public LongBlockNode next() {
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
public LongBlockNode previous() {
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
private LongBlockNode insert(int index, LongBlock obj) {
    assert (relativePosition != 0);
    final int indexRelativeToMe = index - relativePosition;
    if (indexRelativeToMe < 0) {
        return insertOnLeft(indexRelativeToMe, obj);
    } else {
        return insertOnRight(indexRelativeToMe, obj);
    }
}

        private LongBlockNode insertOnLeft(int indexRelativeToMe, LongBlock obj) {
    if (getLeftSubTree() == null) {
        int pos;
        if (relativePosition >= 0) {
            pos = -relativePosition;
        } else {
            pos = -block.size();
        }
        setLeft(new LongBlockNode(this, pos, obj, this, left), null);
    } else {
        setLeft(left.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition >= 0) {
        relativePosition += obj.size();
    }
    final LongBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        private LongBlockNode insertOnRight(int indexRelativeToMe, LongBlock obj) {
    if (getRightSubTree() == null) {
        setRight(new LongBlockNode(this, obj.size(), obj, right, this), null);
    } else {
        setRight(right.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition < 0) {
        relativePosition -= obj.size();
    }
    final LongBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        //-----------------------------------------------------------------------  
/**
         * Gets the left node, returning null if its a faedelung.
         */
public LongBlockNode getLeftSubTree() {
    return leftIsPrevious ? null : left;
}

        /**
         * Gets the right node, returning null if its a faedelung.
         */
public LongBlockNode getRightSubTree() {
    return rightIsNext ? null : right;
}

        /**
         * Gets the rightmost child of this node.
         *
         * @return the rightmost child (greatest index)
         */
public LongBlockNode max() {
    return getRightSubTree() == null ? this : right.max();
}

        /**
         * Gets the leftmost child of this node.
         *
         * @return the leftmost child (smallest index)
         */
public LongBlockNode min() {
    return getLeftSubTree() == null ? this : left.min();
}

        /**
         * Removes the node at a given position.
         *
         * @param index is the index of the element to be removed relative to the position of
         * the parent node of the current node.
         */
private LongBlockNode remove(final int index) {
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

        private LongBlockNode removeMax() {
    if (getRightSubTree() == null) {
        return removeSelf();
    }
    setRight(right.removeMax(), right.right);
    if (relativePosition < 0) {
    }
    recalcHeight();
    return balance();
}

        private LongBlockNode removeMin(int size) {
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
public LongBlockNode removeSelf() {
    LongBlockNode p = parent;
    LongBlockNode n = doRemoveSelf();
    if (n != null) {
        assert (p != n);
        n.parent = p;
    }
    return n;
}

        public LongBlockNode doRemoveSelf() {
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
        final LongBlockNode rightMin = right.min();
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
        final LongBlockNode leftMax = left.max();
        block = leftMax.block;
        if (rightIsNext) {
            right = leftMax.right;
        }
        final LongBlockNode leftPrevious = left.left;
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
private LongBlockNode balance() {
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
private int getOffset(final LongBlockNode node) {
    if (node == null) {
        return 0;
    }
    return node.relativePosition;
}

        /**
         * Sets the relative position.
         */
private int setOffset(final LongBlockNode node, final int newOffest) {
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
private int getHeight(final LongBlockNode node) {
    return node == null ? -1 : node.height;
}

        /**
         * Returns the height difference right - left
         */
private int heightRightMinusLeft() {
    return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
}

        private LongBlockNode rotateLeft() {
    assert (!rightIsNext);
    final LongBlockNode newTop = right;
    // can't be faedelung!   
    final LongBlockNode movedNode = getRightSubTree().getLeftSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    LongBlockNode p = this.parent;
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

        private LongBlockNode rotateRight() {
    assert (!leftIsPrevious);
    final LongBlockNode newTop = left;
    // can't be faedelung   
    final LongBlockNode movedNode = getLeftSubTree().getRightSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    LongBlockNode p = this.parent;
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
private void setLeft(final LongBlockNode node, final LongBlockNode previous) {
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
private void setRight(final LongBlockNode node, final LongBlockNode next) {
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
