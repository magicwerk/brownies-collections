package org.magicwerk.brownies.collections.primitive;
import org.magicwerk.brownies.collections.helper.ArraysHelper;
import org.magicwerk.brownies.collections.helper.primitive.ShortBinarySearch;
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
import org.magicwerk.brownies.collections.helper.primitive.ShortMergeSort;

/**
 * The first block (ShortGapList) used grows dynamcically, all others
 * are allocated with fixed size. This is necessary to prevent starving
 * because of GC usage.
 *
 * @author Thomas Mauch
 * @version $Id: ShortBigList.java 2492 2014-10-11 15:18:58Z origo $
 */
public class ShortBigList extends IShortList {
	public static IShortList of(short[] values) {
		return new ImmutableShortListArrayPrimitive(values);
	}

	public static IShortList of(Short[] values) {
		return new ImmutableShortListArrayWrapper(values);
	}

	public static IShortList of(List<Short> values) {
		return new ImmutableShortListList(values);
	}

    static class ImmutableShortListArrayPrimitive extends ImmutableShortList {
    	short[] values;

    	public ImmutableShortListArrayPrimitive(short[] values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		protected short doGet(int index) {
			return values[index];
		}
    }

    static class ImmutableShortListArrayWrapper extends ImmutableShortList {
    	Short[] values;

    	public ImmutableShortListArrayWrapper(Short[] values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		protected short doGet(int index) {
			return values[index];
		}
    }

    static class ImmutableShortListList extends ImmutableShortList {
    	List<Short> values;

    	public ImmutableShortListList(List<Short> values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.size();
		}

		@Override
		protected short doGet(int index) {
			return values.get(index);
		}
    }

    protected static abstract class ImmutableShortList extends IShortList {

    	//-- Readers

		@Override
		public int capacity() {
			return size();
		}

		@Override
		public int binarySearch(int index, int len, short key) {
			return ShortBinarySearch.binarySearch(this, key, index, index+len);
		}

		@Override
		public IShortList unmodifiableList() {
			return this;
		}

		@Override
		protected short getDefaultElem() {
			return (short) 0;
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
		protected void doClone(IShortList that) {
			error();
		}

		@Override
		protected short doSet(int index, short elem) {
			error();
			return (short) 0;
		}

		@Override
		protected short doReSet(int index, short elem) {
			error();
			return (short) 0;
		}

		@Override
		protected boolean doAdd(int index, short elem) {
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
		protected IShortList doCreate(int capacity) {
			error();
			return null;
		}

		@Override
		protected void doAssign(IShortList that) {
			error();
		}

		@Override
		protected short doRemove(int index) {
			error();
			return (short) 0;
		}

		@Override
		public void sort(int index, int len) {
			error();
		}
    }

    /**
     * An immutable version of a ShortBigList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableShortBigList extends ShortBigList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
protected ImmutableShortBigList(ShortBigList that){
    super(true, that);
}

        @Override
protected boolean doAdd(int index, short elem) {
    error();
    return false;
}

        @Override
protected boolean doAddAll(int index, short[] elems) {
    error();
    return false;
}

        @Override
protected short doSet(int index, short elem) {
    error();
    return (short) 0;
}

        @Override
protected void doSetAll(int index, short[] elems) {
    error();
}

        @Override
protected short doReSet(int index, short elem) {
    error();
    return (short) 0;
}

        @Override
protected short doRemove(int index) {
    error();
    return (short) 0;
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
    
    public static class ShortBlock implements Serializable {

        private ShortGapList values;

        private int refCount;

        public ShortBlock(){
    values = new ShortGapList();
    refCount = 1;
}

        public ShortBlock(int capacity){
    values = new ShortGapList(capacity);
    refCount = 1;
}

        public ShortBlock(ShortBlock that){
    values = new ShortGapList(that.values.capacity());
    values.init(that.values.getArray(0, that.values.size()));
    refCount = 1;
}

        public boolean isShared() {
    return refCount > 1;
}

        public ShortBlock ref() {
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

    /** Number of elements stored in this ShortBigList */
    private int size;

    /** The root node in the tree */
    private ShortBlockNode root;

    /** Current node */
    private ShortBlockNode currNode;

    /** ShortBlock of current node */
    private ShortBlock currShortBlock;

    /** Start index of current block */
    private int currShortBlockStart;

    /** End index of current block */
    private int currShortBlockEnd;

    /** Modify value which must be applied before this block is not current any more */
    private int currModify;

    /**
     * Constructor used internally, e.g. for ImmutableShortBigList.
     *
     * @param copy true to copy all instance values from source,
     *             if false nothing is done
     * @param that list to copy
     */
protected ShortBigList(boolean copy, ShortBigList that){
    if (copy) {
        this.blockSize = that.blockSize;
        this.currShortBlock = that.currShortBlock;
        this.currShortBlockStart = that.currShortBlockStart;
        this.currShortBlockEnd = that.currShortBlockEnd;
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
// This separate method is needed as the varargs variant creates the ShortGapList with specific size  
public static ShortBigList create() {
    return new ShortBigList();
}

    /**
     * Create new list with specified elements.
     *
     * @param coll      collection with element
     * @return          created list
     * @param        type of elements stored in the list
     */
public static ShortBigList create(Collection<Short> coll) {
    return new ShortBigList(coll);
}

    /**
	 * Create new list with specified elements.
	 *
	 * @param elems 	array with elements
	 * @return 			created list
	 * @param  		type of elements stored in the list
	 */
public static ShortBigList create(short... elems) {
    ShortBigList list = new ShortBigList();
    for (short elem : elems) {
        list.add(elem);
    }
    return list;
}

    /**
	 * Default constructor.
	 * The default block size is used.
	 */
public ShortBigList(){
    this(BLOCK_SIZE);
}

    /**
	 * Constructor.
	 *
	 * @param blockSize block size
	 */
public ShortBigList(int blockSize){
    if (blockSize < 2) {
        throw new IndexOutOfBoundsException("Invalid blockSize: " + blockSize);
    }
    doInit(blockSize, -1);
}

    public ShortBigList(Collection<Short> that){
    if (that instanceof ShortBigList) {
        doAssign((ShortBigList) that);
        doClone((ShortBigList) that);
    } else {
        blockSize = BLOCK_SIZE;
        currShortBlock = new ShortBlock();
        addShortBlock(0, currShortBlock);
        for (Object obj : that.toArray()) {
            add((Short) obj);
        }
        assert (size() == that.size());
    }
}

    public void init() {
    clear();
}

    public void init(short... elems) {
    clear();
    for (short elem : elems) {
        add(elem);
    }
}

    public void init(Collection<Short> that) {
    clear();
    addAll(that);
}

    /**
     * Returns block size used for this ShortBigList.
     *
     * @return block size used for this ShortBigList
     */
public int blockSize() {
    return blockSize;
}

    //---  
private ShortBigList(int blockSize, int firstShortBlockSize){
    doInit(blockSize, firstShortBlockSize);
}

    void doInit(int blockSize, int firstShortBlockSize) {
    this.blockSize = blockSize;
    // First block will grow until it reaches blockSize   
    if (firstShortBlockSize <= 1) {
        currShortBlock = new ShortBlock();
    } else {
        currShortBlock = new ShortBlock(firstShortBlockSize);
    }
    addShortBlock(0, currShortBlock);
}

    @Override
public ShortBigList copy() {
    return (ShortBigList) super.copy();
}

    @Override
protected void doAssign(IShortList that) {
    ShortBigList list = (ShortBigList) that;
    this.blockSize = list.blockSize;
    this.currShortBlock = list.currShortBlock;
    this.currShortBlockEnd = list.currShortBlockEnd;
    this.currShortBlockStart = list.currShortBlockStart;
    this.currNode = list.currNode;
    this.root = list.root;
    this.size = list.size;
}

    @Override
protected void doClone(IShortList that) {
    ShortBigList bigList = (ShortBigList) that;
    bigList.check();
    bigList.releaseShortBlock();
    root = copy(bigList.root);
    currNode = null;
    currModify = 0;
    check();
}

    private ShortBlockNode copy(ShortBlockNode node) {
    ShortBlockNode newNode = node.min();
    int index = newNode.block.size();
    ShortBlockNode newRoot = new ShortBlockNode(null, index, newNode.block.ref(), null, null);
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
public short getDefaultElem() {
    return (short) 0;
}

    @Override
protected void finalize() {
    // This list will be garbage collected, so unref all referenced blocks   
    ShortBlockNode node = root.min();
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
protected short doGet(int index) {
    int pos = getShortBlockIndex(index, false, 0);
    return currShortBlock.values.doGet(pos);
}

    @Override
protected short doSet(int index, short elem) {
    int pos = getShortBlockIndex(index, true, 0);
    short oldElem = currShortBlock.values.doGet(pos);
    currShortBlock.values.doSet(pos, elem);
    return oldElem;
}

    @Override
protected short doReSet(int index, short elem) {
    int pos = getShortBlockIndex(index, true, 0);
    short oldElem = currShortBlock.values.doGet(pos);
    currShortBlock.values.doSet(pos, elem);
    return oldElem;
}

    private void releaseShortBlock() {
    if (currModify != 0) {
        int modify = currModify;
        currModify = 0;
        modify(currNode, modify);
    }
    currNode = null;
}

    /**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currShortBlock to remember this last used block.
	 *
	 * @param index	list index (0 <= index <= size())
	 * @return		relative index within block
	 */
private int getShortBlockIndex(int index, boolean write, int modify) {
    // Determine block where specified index is located and store it in currShortBlock   
    if (currNode != null) {
        if (index >= currShortBlockStart && (index < currShortBlockEnd || index == currShortBlockEnd && size == index)) {
            // currShortBlock is already set correctly   
            if (write) {
                if (currShortBlock.isShared()) {
                    currShortBlock.unref();
                    currShortBlock = new ShortBlock(currShortBlock);
                    currNode.setShortBlock(currShortBlock);
                }
            }
            currModify += modify;
            return index - currShortBlockStart;
        }
        releaseShortBlock();
    }
    boolean done = false;
    if (index == size) {
        if (currNode == null || currShortBlockEnd != size) {
            currNode = root.max();
            currShortBlock = currNode.getShortBlock();
            currShortBlockEnd = size;
            currShortBlockStart = size - currShortBlock.size();
        }
        if (modify != 0) {
            currNode.relativePosition += modify;
            ShortBlockNode leftNode = currNode.getLeftSubTree();
            if (leftNode != null) {
                leftNode.relativePosition -= modify;
            }
        }
        done = true;
    } else if (index == 0) {
        if (currNode == null || currShortBlockStart != 0) {
            currNode = root.min();
            currShortBlock = currNode.getShortBlock();
            currShortBlockEnd = currShortBlock.size();
            currShortBlockStart = 0;
        }
        if (modify != 0) {
            root.relativePosition += modify;
        }
        done = true;
    }
    if (!done) {
        // Reset currShortBlockEnd, it will be then set by access()   
        currShortBlockEnd = 0;
        currNode = access(index, modify);
        currShortBlock = currNode.getShortBlock();
        currShortBlockStart = currShortBlockEnd - currShortBlock.size();
    }
    assert (index >= currShortBlockStart && index <= currShortBlockEnd);
    if (write) {
        if (currShortBlock.isShared()) {
            currShortBlock.unref();
            currShortBlock = new ShortBlock(currShortBlock);
            currNode.setShortBlock(currShortBlock);
        }
    }
    return index - currShortBlockStart;
}

    void checkNode(ShortBlockNode node) {
    assert ((node.block.size() > 0 || node == root) && node.block.size() <= blockSize);
    ShortBlockNode child = node.getLeftSubTree();
    assert (child == null || child.parent == node);
    child = node.getRightSubTree();
    assert (child == null || child.parent == node);
}

    void checkHeight(ShortBlockNode node) {
    ShortBlockNode left = node.getLeftSubTree();
    ShortBlockNode right = node.getRightSubTree();
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
        assert (currNode.block == currShortBlock);
        assert (currShortBlockStart >= 0 && currShortBlockEnd <= size && currShortBlockStart <= currShortBlockEnd);
        assert (currShortBlockStart + currShortBlock.size() == currShortBlockEnd);
    }
    if (root == null) {
        assert (size == 0);
        return;
    }
    checkHeight(root);
    ShortBlockNode oldCurrNode = currNode;
    int oldCurrModify = currModify;
    if (currModify != 0) {
        currNode = null;
        currModify = 0;
        modify(oldCurrNode, oldCurrModify);
    }
    ShortBlockNode node = root;
    checkNode(node);
    int index = node.relativePosition;
    while (node.left != null) {
        node = node.left;
        checkNode(node);
        assert (node.relativePosition < 0);
        index += node.relativePosition;
    }
    ShortBlock block = node.getShortBlock();
    assert (block.size() == index);
    int lastIndex = index;
    while (lastIndex < size()) {
        node = root;
        index = node.relativePosition;
        int searchIndex = lastIndex + 1;
        while (true) {
            checkNode(node);
            block = node.getShortBlock();
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
        block = node.getShortBlock();
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
protected boolean doAdd(int index, short element) {
    if (index == -1) {
        index = size;
    }
    // Insert   
    int pos = getShortBlockIndex(index, true, 1);
    // If there is still place in the current block: insert in current block   
    int maxSize = (index == size || index == 0) ? blockSize * 9 / 10 : blockSize;
    // The second part of the condition is a work around to handle the case of insertion as position 0 correctly   
    // where blockSize() is 2 (the new block would then be added after the current one)   
    if (currShortBlock.size() < maxSize || (currShortBlock.size() == 1 && currShortBlock.size() < blockSize)) {
        currShortBlock.values.doAdd(pos, element);
        currShortBlockEnd++;
    } else {
        // No place any more in current block   
        ShortBlock newShortBlock = new ShortBlock(blockSize);
        if (index == size) {
            // Insert new block at tail   
            newShortBlock.values.doAdd(0, element);
            // Subtract 1 because getShortBlockIndex() has already added 1   
            modify(currNode, -1);
            addShortBlock(size + 1, newShortBlock);
            ShortBlockNode lastNode = currNode.next();
            currNode = lastNode;
            currShortBlock = currNode.block;
            currShortBlockStart = currShortBlockEnd;
            currShortBlockEnd++;
        } else if (index == 0) {
            // Insert new block at head   
            newShortBlock.values.doAdd(0, element);
            // Subtract 1 because getShortBlockIndex() has already added 1   
            modify(currNode, -1);
            addShortBlock(1, newShortBlock);
            ShortBlockNode firstNode = currNode.previous();
            currNode = firstNode;
            currShortBlock = currNode.block;
            currShortBlockStart = 0;
            currShortBlockEnd = 1;
        } else {
            // Split block for insert   
            int nextShortBlockLen = blockSize / 2;
            int blockLen = blockSize - nextShortBlockLen;
            newShortBlock.values.init(nextShortBlockLen, (short) 0);
            ShortGapList.copy(currShortBlock.values, blockLen, newShortBlock.values, 0, nextShortBlockLen);
            currShortBlock.values.remove(blockLen, blockSize - blockLen);
            // Subtract 1 more because getShortBlockIndex() has already added 1   
            modify(currNode, -nextShortBlockLen - 1);
            addShortBlock(currShortBlockEnd - nextShortBlockLen, newShortBlock);
            if (pos < blockLen) {
                // Insert element in first block   
                currShortBlock.values.doAdd(pos, element);
                currShortBlockEnd = currShortBlockStart + blockLen + 1;
                modify(currNode, 1);
            } else {
                // Insert element in second block   
                currNode = currNode.next();
                modify(currNode, 1);
                currShortBlock = currNode.block;
                currShortBlock.values.doAdd(pos - blockLen, element);
                currShortBlockStart += blockLen;
                currShortBlockEnd++;
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
private void modify(ShortBlockNode node, int modify) {
    if (node == currNode) {
        modify += currModify;
        currModify = 0;
    } else {
        releaseShortBlock();
    }
    if (modify == 0) {
        return;
    }
    if (node.relativePosition < 0) {
        // Left node   
        ShortBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        ShortBlockNode pp = node.parent;
        assert (pp.getLeftSubTree() == node);
        boolean parentRight = true;
        while (true) {
            ShortBlockNode p = pp.parent;
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
        ShortBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        ShortBlockNode parent = node.parent;
        if (parent != null) {
            assert (parent.getRightSubTree() == node);
            boolean parentLeft = true;
            while (true) {
                ShortBlockNode p = parent.parent;
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

    private ShortBlockNode doRemove(ShortBlockNode node) {
    ShortBlockNode p = node.parent;
    ShortBlockNode newNode = node.removeSelf();
    ShortBlockNode n = newNode;
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
protected boolean doAddAll(int index, short[] array) {
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
    int addPos = getShortBlockIndex(index, true, 0);
    ShortBlock addShortBlock = currShortBlock;
    int space = blockSize - addShortBlock.size();
    int addLen = array.length;
    if (addLen <= space) {
        // All elements can be added to current block   
        currShortBlock.values.addAll(addPos, array);
        modify(currNode, addLen);
        size += addLen;
        currShortBlockEnd += addLen;
    } else {
        if (index == size) {
            // Add elements at end   
            for (int i = 0; i < space; i++) {
                currShortBlock.values.add(addPos, array[i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                ShortBlock nextShortBlock = new ShortBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextShortBlock.values.add(i, array[done + i]);
                }
                done += add;
                todo -= add;
                addShortBlock(size + done, nextShortBlock);
            }
            size += addLen;
            currNode = currNode.next();
            currShortBlock = currNode.block;
            currShortBlockStart = currShortBlockEnd + space;
            currShortBlockEnd = currShortBlockStart + addLen - space;
        } else if (index == 0) {
            // Add elements at head   
            assert (addPos == 0);
            for (int i = 0; i < space; i++) {
                currShortBlock.values.add(addPos, array[addLen - space + i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                ShortBlock nextShortBlock = new ShortBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextShortBlock.values.add(i, array[done + i]);
                }
                done += add;
                todo -= add;
                addShortBlock(0, nextShortBlock);
            }
            size += addLen;
            currNode = currNode.previous();
            currShortBlock = currNode.block;
            currShortBlockStart = 0;
            currShortBlockEnd = addLen - space;
        } else {
            // Add elements to several blocks   
            // Handle first block   
            ShortGapList list = ShortGapList.create(array);
            int remove = currShortBlock.values.size() - addPos;
            if (remove > 0) {
                list.addAll(currShortBlock.values.getAll(addPos, remove));
                currShortBlock.values.remove(addPos, remove);
                modify(currNode, -remove);
                size -= remove;
                currShortBlockEnd -= remove;
            }
            int s = currShortBlock.values.size() + list.size();
            int numShortBlocks = (s - 1) / blockSize + 1;
            assert (numShortBlocks > 1);
            int has = currShortBlock.values.size();
            int should = s / numShortBlocks;
            int start = 0;
            int end = 0;
            if (has < should) {
                // Elements must be added to first block   
                int add = should - has;
                IShortList sublist = list.getAll(0, add);
                currShortBlock.values.addAll(addPos, sublist);
                modify(currNode, add);
                start += add;
                assert (currShortBlock.values.size() == should);
                s -= should;
                numShortBlocks--;
                size += add;
                currShortBlockEnd += add;
                end = currShortBlockEnd;
            } else if (has > should) {
                // Elements must be moved from first to second block   
                ShortBlock nextShortBlock = new ShortBlock(blockSize);
                int move = has - should;
                nextShortBlock.values.addAll(currShortBlock.values.getAll(currShortBlock.values.size() - move, move));
                currShortBlock.values.remove(currShortBlock.values.size() - move, move);
                modify(currNode, -move);
                assert (currShortBlock.values.size() == should);
                s -= should;
                numShortBlocks--;
                currShortBlockEnd -= move;
                end = currShortBlockEnd;
                should = s / numShortBlocks;
                int add = should - move;
                assert (add >= 0);
                IShortList sublist = list.getAll(0, add);
                nextShortBlock.values.addAll(move, sublist);
                start += add;
                assert (nextShortBlock.values.size() == should);
                s -= should;
                numShortBlocks--;
                size += add;
                end += add;
                addShortBlock(end, nextShortBlock);
            } else {
                s -= should;
                numShortBlocks--;
            }
            check();
            ShortBlockNode node = currNode;
            while (numShortBlocks > 0) {
                int add = s / numShortBlocks;
                assert (add > 0);
                IShortList sublist = list.getAll(start, add);
                ShortBlock nextShortBlock = new ShortBlock();
                nextShortBlock.values.clear();
                nextShortBlock.values.addAll(sublist);
                start += add;
                assert (nextShortBlock.values.size() == add);
                s -= add;
                addShortBlock(end, nextShortBlock);
                assert (node.next().block == nextShortBlock);
                node = node.next();
                end += add;
                size += add;
                numShortBlocks--;
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
    currShortBlock = null;
    currShortBlockStart = 0;
    currShortBlockEnd = 0;
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
    int startPos = getShortBlockIndex(index, true, 0);
    ShortBlockNode startNode = currNode;
    int endPos = getShortBlockIndex(index + len - 1, true, 0);
    ShortBlockNode endNode = currNode;
    if (startNode == endNode) {
        // Delete from single block   
        getShortBlockIndex(index, true, -len);
        currShortBlock.values.remove(startPos, len);
        if (currShortBlock.values.isEmpty()) {
            ShortBlockNode oldCurrNode = currNode;
            releaseShortBlock();
            ShortBlockNode node = doRemove(oldCurrNode);
            merge(node);
        } else {
            currShortBlockEnd -= len;
            merge(currNode);
        }
        size -= len;
    } else {
        // Delete from start block   
        check();
        int startLen = startNode.block.size() - startPos;
        getShortBlockIndex(index, true, -startLen);
        // TODO should that be modify?   
        startNode.block.values.remove(startPos, startLen);
        assert (startNode == currNode);
        if (currShortBlock.values.isEmpty()) {
            releaseShortBlock();
            doRemove(startNode);
            startNode = null;
        }
        len -= startLen;
        size -= startLen;
        //check();   
        while (len > 0) {
            currNode = null;
            getShortBlockIndex(index, true, 0);
            int s = currShortBlock.size();
            if (s <= len) {
                modify(currNode, -s);
                ShortBlockNode oldCurrNode = currNode;
                releaseShortBlock();
                doRemove(oldCurrNode);
                if (oldCurrNode == endNode) {
                    endNode = null;
                }
                len -= s;
                size -= s;
                check();
            } else {
                modify(currNode, -len);
                currShortBlock.values.remove(0, len);
                size -= len;
                break;
            }
        }
        releaseShortBlock();
        check();
        getShortBlockIndex(index, false, 0);
        merge(currNode);
    }
    if (DUMP)
        dump();
    if (CHECK)
        check();
}

    void merge(ShortBlockNode node) {
    if (node == null) {
        return;
    }
    final int minShortBlockSize = Math.max(blockSize / 3, 1);
    if (node.block.values.size() >= minShortBlockSize) {
        return;
    }
    ShortBlockNode oldCurrNode = node;
    ShortBlockNode leftNode = node.previous();
    if (leftNode != null && leftNode.block.size() < minShortBlockSize) {
        // Merge with left block   
        int len = node.block.size();
        int dstSize = leftNode.getShortBlock().size();
        for (int i = 0; i < len; i++) {
            leftNode.block.values.add((short) 0);
        }
        ShortGapList.copy(node.block.values, 0, leftNode.block.values, dstSize, len);
        assert (leftNode.block.values.size() <= blockSize);
        modify(leftNode, +len);
        modify(oldCurrNode, -len);
        releaseShortBlock();
        doRemove(oldCurrNode);
    } else {
        ShortBlockNode rightNode = node.next();
        if (rightNode != null && rightNode.block.size() < minShortBlockSize) {
            // Merge with right block   
            int len = node.block.size();
            for (int i = 0; i < len; i++) {
                rightNode.block.values.add(0, (short) 0);
            }
            ShortGapList.copy(node.block.values, 0, rightNode.block.values, 0, len);
            assert (rightNode.block.values.size() <= blockSize);
            modify(rightNode, +len);
            modify(oldCurrNode, -len);
            releaseShortBlock();
            doRemove(oldCurrNode);
        }
    }
}

    protected short doRemove(int index) {
    int pos = getShortBlockIndex(index, true, -1);
    short oldElem = currShortBlock.values.doRemove(pos);
    currShortBlockEnd--;
    final int minShortBlockSize = Math.max(blockSize / 3, 1);
    if (currShortBlock.size() < minShortBlockSize) {
        if (currShortBlock.size() == 0) {
            if (!isOnlyRootShortBlock()) {
                ShortBlockNode oldCurrNode = currNode;
                releaseShortBlock();
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
public ShortBigList unmodifiableList() {
    // Naming as in java.util.Collections#unmodifiableList   
    return new ImmutableShortBigList(this);
}

    @Override
protected void doEnsureCapacity(int minCapacity) {
    if (isOnlyRootShortBlock()) {
        if (minCapacity > blockSize) {
            minCapacity = blockSize;
        }
        currShortBlock.values.doEnsureCapacity(minCapacity);
    }
}

    @Override
public void trimToSize() {
    if (isOnlyRootShortBlock()) {
        currShortBlock.values.trimToSize();
    }
}

    @Override
protected IShortList doCreate(int capacity) {
    if (capacity <= blockSize) {
        return new ShortBigList(this.blockSize);
    } else {
        return new ShortBigList(this.blockSize, capacity);
    }
}

    @Override
public void sort(int index, int len) {
    checkRange(index, len);
    if (isOnlyRootShortBlock()) {
        currShortBlock.values.sort(index, len);
    } else {
        ShortMergeSort.sort(this, index, index + len);
    }
}

    @Override
public int binarySearch(int index, int len, short key) {
    checkRange(index, len);
    if (isOnlyRootShortBlock()) {
        return currShortBlock.values.binarySearch(key);
    } else {
        return ShortBinarySearch.binarySearch(this, key, 0, size());
    }
}

    private boolean isOnlyRootShortBlock() {
    return root.left == null && root.right == null;
}

    public ShortBlockNode access(final int index, int modify) {
    return root.access(this, index, modify, false);
}

    //-----------------------------------------------------------------------  
/**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */
public void addShortBlock(int index, ShortBlock obj) {
    if (root == null) {
        root = new ShortBlockNode(null, index, obj, null, null);
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
public void removeShortBlock(int index) {
    root = root.remove(index);
}

    // --- Serialization ---  
/**
     * Serialize a ShortBigList object.
     *
     * @serialData The length of the array backing the <tt>ShortGapList</tt>
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
        oos.writeShort(doGet(i));
    }
}

    /**
     * Deserialize a ShortBigList object.
 	 *
     * @param ois  input stream for serialization
     * @throws 	   IOException if serialization fails
     * @throws 	   ClassNotFoundException if serialization fails
     */

private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    int blockSize = ois.readInt();
    int size = ois.readInt();
    int firstShortBlockSize = (size <= blockSize) ? size : -1;
    doInit(blockSize, firstShortBlockSize);
    for (int i = 0; i < size; i++) {
        add(ois.readShort());
    }
}

    //-----------------------------------------------------------------------  
    /**
     * Implements an AVLNode which keeps the offset updated.
     * <p>
     * This node contains the real work.
     * TreeList is just there to implement {@link java.util.IShortList}.
     * The nodes don't know the index of the object they are holding.  They
     * do know however their position relative to their parent node.
     * This allows to calculate the index of a node while traversing the tree.
     * <p>
     * The Faedelung calculation stores a flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    static class ShortBlockNode {

        ShortBlockNode parent;

        /** The left child node or the predecessor if {@link #leftIsPrevious}.*/
        ShortBlockNode left;

        /** Flag indicating that left reference is not a subtree but the predecessor. */
        boolean leftIsPrevious;

        /** The right child node or the successor if {@link #rightIsNext}. */
        ShortBlockNode right;

        /** Flag indicating that right reference is not a subtree but the successor. */
        boolean rightIsNext;

        /** How many levels of left/right are below this one. */
        int height;

        /** The relative position, root holds absolute position. */
        int relativePosition;

        /** The stored block */
        ShortBlock block;

        /**
         * Constructs a new node with a relative position.
         *
         * @param relativePosition  the relative position of the node
         * @param obj  the value for the node
         * @param rightFollower the node with the value following this one
         * @param leftFollower the node with the value leading this one
         */
private ShortBlockNode(ShortBlockNode parent, final int relativePosition, final ShortBlock block, final ShortBlockNode rightFollower, final ShortBlockNode leftFollower){
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
public ShortBlock getShortBlock() {
    return block;
}

        /**
         * Sets the value.
         *
         * @param obj  the value to store
         */
public void setShortBlock(ShortBlock obj) {
    this.block = obj;
}

        private ShortBlockNode access(ShortBigList list, int index, int modify, boolean wasLeft) {
    assert (index >= 0);
    if (relativePosition == 0) {
        if (modify != 0) {
            relativePosition += modify;
        }
        return this;
    }
    if (list.currShortBlockEnd == 0) {
        list.currShortBlockEnd = relativePosition;
    }
    ShortBlockNode leftNode = getLeftSubTree();
    int leftIndex = list.currShortBlockEnd - block.size();
    assert (leftIndex >= 0);
    if (index >= leftIndex && index < list.currShortBlockEnd) {
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
    if (index < list.currShortBlockEnd) {
        // left   
        ShortBlockNode nextNode = getLeftSubTree();
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
        list.currShortBlockEnd += nextNode.relativePosition;
        return nextNode.access(list, index, modify, wasLeft);
    } else {
        // right   
        ShortBlockNode nextNode = getRightSubTree();
        if (nextNode == null || wasLeft) {
            if (relativePosition > 0) {
                relativePosition += modify;
                ShortBlockNode left = getLeftSubTree();
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
        list.currShortBlockEnd += nextNode.relativePosition;
        return nextNode.access(list, index, modify, wasLeft);
    }
}

        /**
         * Gets the next node in the list after this one.
         *
         * @return the next node
         */
public ShortBlockNode next() {
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
public ShortBlockNode previous() {
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
private ShortBlockNode insert(int index, ShortBlock obj) {
    assert (relativePosition != 0);
    final int indexRelativeToMe = index - relativePosition;
    if (indexRelativeToMe < 0) {
        return insertOnLeft(indexRelativeToMe, obj);
    } else {
        return insertOnRight(indexRelativeToMe, obj);
    }
}

        private ShortBlockNode insertOnLeft(int indexRelativeToMe, ShortBlock obj) {
    if (getLeftSubTree() == null) {
        int pos;
        if (relativePosition >= 0) {
            pos = -relativePosition;
        } else {
            pos = -block.size();
        }
        setLeft(new ShortBlockNode(this, pos, obj, this, left), null);
    } else {
        setLeft(left.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition >= 0) {
        relativePosition += obj.size();
    }
    final ShortBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        private ShortBlockNode insertOnRight(int indexRelativeToMe, ShortBlock obj) {
    if (getRightSubTree() == null) {
        setRight(new ShortBlockNode(this, obj.size(), obj, right, this), null);
    } else {
        setRight(right.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition < 0) {
        relativePosition -= obj.size();
    }
    final ShortBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        //-----------------------------------------------------------------------  
/**
         * Gets the left node, returning null if its a faedelung.
         */
public ShortBlockNode getLeftSubTree() {
    return leftIsPrevious ? null : left;
}

        /**
         * Gets the right node, returning null if its a faedelung.
         */
public ShortBlockNode getRightSubTree() {
    return rightIsNext ? null : right;
}

        /**
         * Gets the rightmost child of this node.
         *
         * @return the rightmost child (greatest index)
         */
public ShortBlockNode max() {
    return getRightSubTree() == null ? this : right.max();
}

        /**
         * Gets the leftmost child of this node.
         *
         * @return the leftmost child (smallest index)
         */
public ShortBlockNode min() {
    return getLeftSubTree() == null ? this : left.min();
}

        /**
         * Removes the node at a given position.
         *
         * @param index is the index of the element to be removed relative to the position of
         * the parent node of the current node.
         */
private ShortBlockNode remove(final int index) {
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

        private ShortBlockNode removeMax() {
    if (getRightSubTree() == null) {
        return removeSelf();
    }
    setRight(right.removeMax(), right.right);
    if (relativePosition < 0) {
    }
    recalcHeight();
    return balance();
}

        private ShortBlockNode removeMin(int size) {
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
public ShortBlockNode removeSelf() {
    ShortBlockNode p = parent;
    ShortBlockNode n = doRemoveSelf();
    if (n != null) {
        assert (p != n);
        n.parent = p;
    }
    return n;
}

        public ShortBlockNode doRemoveSelf() {
    if (getRightSubTree() == null && getLeftSubTree() == null) {
        return (short) 0;
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
        final ShortBlockNode rightMin = right.min();
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
        final ShortBlockNode leftMax = left.max();
        block = leftMax.block;
        if (rightIsNext) {
            right = leftMax.right;
        }
        final ShortBlockNode leftPrevious = left.left;
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
private ShortBlockNode balance() {
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
private int getOffset(final ShortBlockNode node) {
    if (node == null) {
        return 0;
    }
    return node.relativePosition;
}

        /**
         * Sets the relative position.
         */
private int setOffset(final ShortBlockNode node, final int newOffest) {
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
private int getHeight(final ShortBlockNode node) {
    return node == null ? -1 : node.height;
}

        /**
         * Returns the height difference right - left
         */
private int heightRightMinusLeft() {
    return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
}

        private ShortBlockNode rotateLeft() {
    assert (!rightIsNext);
    final ShortBlockNode newTop = right;
    // can't be faedelung!   
    final ShortBlockNode movedNode = getRightSubTree().getLeftSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    ShortBlockNode p = this.parent;
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

        private ShortBlockNode rotateRight() {
    assert (!leftIsPrevious);
    final ShortBlockNode newTop = left;
    // can't be faedelung   
    final ShortBlockNode movedNode = getLeftSubTree().getRightSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    ShortBlockNode p = this.parent;
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
private void setLeft(final ShortBlockNode node, final ShortBlockNode previous) {
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
private void setRight(final ShortBlockNode node, final ShortBlockNode next) {
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
