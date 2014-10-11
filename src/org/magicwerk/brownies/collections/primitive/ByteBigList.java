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
import org.magicwerk.brownies.collections.helper.primitive.ByteMergeSort;

/**
 * The first block (ByteGapList) used grows dynamcically, all others
 * are allocated with fixed size. This is necessary to prevent starving
 * because of GC usage.
 *
 * @author Thomas Mauch
 * @version $Id: ByteBigList.java 2477 2014-10-08 23:47:35Z origo $
 */
public class ByteBigList extends IByteList {
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
     * An immutable version of a ByteBigList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableByteBigList extends ByteBigList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
protected ImmutableByteBigList(ByteBigList that){
    super(true, that);
}

        @Override
protected boolean doAdd(int index, byte elem) {
    error();
    return false;
}

        @Override
protected boolean doAddAll(int index, byte[] elems) {
    error();
    return false;
}

        @Override
protected byte doSet(int index, byte elem) {
    error();
    return (byte) 0;
}

        @Override
protected void doSetAll(int index, byte[] elems) {
    error();
}

        @Override
protected byte doReSet(int index, byte elem) {
    error();
    return (byte) 0;
}

        @Override
protected byte doRemove(int index) {
    error();
    return (byte) 0;
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
    
    public static class ByteBlock implements Serializable {

        private ByteGapList values;

        private int refCount;

        public ByteBlock(){
    values = new ByteGapList();
    refCount = 1;
}

        public ByteBlock(int capacity){
    values = new ByteGapList(capacity);
    refCount = 1;
}

        public ByteBlock(ByteBlock that){
    values = new ByteGapList(that.values.capacity());
    values.init(that.values.getArray(0, that.values.size()));
    refCount = 1;
}

        public boolean isShared() {
    return refCount > 1;
}

        public ByteBlock ref() {
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

    /** Number of elements stored in this ByteBigList */
    private int size;

    /** The root node in the tree */
    private ByteBlockNode root;

    /** Current node */
    private ByteBlockNode currNode;

    /** ByteBlock of current node */
    private ByteBlock currByteBlock;

    /** Start index of current block */
    private int currByteBlockStart;

    /** End index of current block */
    private int currByteBlockEnd;

    /** Modify value which must be applied before this block is not current any more */
    private int currModify;

    /**
     * Constructor used internally, e.g. for ImmutableByteBigList.
     *
     * @param copy true to copy all instance values from source,
     *             if false nothing is done
     * @param that list to copy
     */
protected ByteBigList(boolean copy, ByteBigList that){
    if (copy) {
        this.blockSize = that.blockSize;
        this.currByteBlock = that.currByteBlock;
        this.currByteBlockStart = that.currByteBlockStart;
        this.currByteBlockEnd = that.currByteBlockEnd;
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
// This separate method is needed as the varargs variant creates the ByteGapList with specific size  
public static ByteBigList create() {
    return new ByteBigList();
}

    /**
     * Create new list with specified elements.
     *
     * @param coll      collection with element
     * @return          created list
     * @param        type of elements stored in the list
     */
public static ByteBigList create(Collection<Byte> coll) {
    return new ByteBigList(coll);
}

    /**
	 * Create new list with specified elements.
	 *
	 * @param elems 	array with elements
	 * @return 			created list
	 * @param  		type of elements stored in the list
	 */
public static ByteBigList create(byte... elems) {
    ByteBigList list = new ByteBigList();
    for (byte elem : elems) {
        list.add(elem);
    }
    return list;
}

    /**
	 * Default constructor.
	 * The default block size is used.
	 */
public ByteBigList(){
    this(BLOCK_SIZE);
}

    /**
	 * Constructor.
	 *
	 * @param blockSize block size
	 */
public ByteBigList(int blockSize){
    doInit(blockSize, -1);
}

    public ByteBigList(Collection<Byte> that){
    if (that instanceof ByteBigList) {
        doAssign((ByteBigList) that);
        doClone((ByteBigList) that);
    } else {
        blockSize = BLOCK_SIZE;
        currByteBlock = new ByteBlock();
        addByteBlock(0, currByteBlock);
        for (byte elem : that.toArray()) {
            add((E) elem);
        }
        assert (size() == that.size());
    }
}

    public void init() {
    clear();
}

    public void init(byte... elems) {
    clear();
    for (byte elem : elems) {
        add(elem);
    }
}

    public void init(Collection<Byte> that) {
    clear();
    addAll(that);
}

    /**
     * Returns block size used for this ByteBigList.
     *
     * @return block size used for this ByteBigList
     */
public int blockSize() {
    return blockSize;
}

    //---  
private ByteBigList(int blockSize, int firstByteBlockSize){
    doInit(blockSize, firstByteBlockSize);
}

    void doInit(int blockSize, int firstByteBlockSize) {
    this.blockSize = blockSize;
    // First block will grow until it reaches blockSize   
    if (firstByteBlockSize <= 1) {
        currByteBlock = new ByteBlock();
    } else {
        currByteBlock = new ByteBlock(firstByteBlockSize);
    }
    addByteBlock(0, currByteBlock);
}

    @Override
public ByteBigList copy() {
    return (ByteBigList) super.copy();
}

    @Override
protected void doAssign(IByteList that) {
    ByteBigList list = (ByteBigList) that;
    this.blockSize = list.blockSize;
    this.currByteBlock = list.currByteBlock;
    this.currByteBlockEnd = list.currByteBlockEnd;
    this.currByteBlockStart = list.currByteBlockStart;
    this.currNode = list.currNode;
    this.root = list.root;
    this.size = list.size;
}

    @Override
protected void doClone(IByteList that) {
    ByteBigList bigList = (ByteBigList) that;
    bigList.check();
    bigList.releaseByteBlock();
    root = copy(bigList.root);
    currNode = null;
    currModify = 0;
    check();
}

    private ByteBlockNode copy(ByteBlockNode node) {
    ByteBlockNode newNode = node.min();
    int index = newNode.block.size();
    ByteBlockNode newRoot = new ByteBlockNode(null, index, newNode.block.ref(), null, null);
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
public byte getDefaultElem() {
    return (byte) 0;
}

    @Override
protected void finalize() {
    // This list will be garbage collected, so unref all referenced blocks   
    ByteBlockNode node = root.min();
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
protected byte doGet(int index) {
    int pos = getByteBlockIndex(index, false, 0);
    return currByteBlock.values.doGet(pos);
}

    @Override
protected byte doSet(int index, byte elem) {
    int pos = getByteBlockIndex(index, true, 0);
    byte oldElem = currByteBlock.values.doGet(pos);
    currByteBlock.values.doSet(pos, elem);
    return oldElem;
}

    @Override
protected byte doReSet(int index, byte elem) {
    int pos = getByteBlockIndex(index, true, 0);
    byte oldElem = currByteBlock.values.doGet(pos);
    currByteBlock.values.doSet(pos, elem);
    return oldElem;
}

    private void releaseByteBlock() {
    if (currModify != 0) {
        int modify = currModify;
        currModify = 0;
        modify(currNode, modify);
    }
    currNode = null;
}

    /**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currByteBlock to remember this last used block.
	 *
	 * @param index	list index (0 <= index <= size())
	 * @return		relative index within block
	 */
private int getByteBlockIndex(int index, boolean write, int modify) {
    // Determine block where specified index is located and store it in currByteBlock   
    if (currNode != null) {
        if (index >= currByteBlockStart && (index < currByteBlockEnd || index == currByteBlockEnd && size == index)) {
            // currByteBlock is already set correctly   
            if (write) {
                if (currByteBlock.isShared()) {
                    currByteBlock.unref();
                    currByteBlock = new ByteBlock(currByteBlock);
                    currNode.setByteBlock(currByteBlock);
                }
            }
            currModify += modify;
            return index - currByteBlockStart;
        }
        releaseByteBlock();
    }
    boolean done = false;
    if (index == size) {
        if (currNode == null || currByteBlockEnd != size) {
            currNode = root.max();
            currByteBlock = currNode.getByteBlock();
            currByteBlockEnd = size;
            currByteBlockStart = size - currByteBlock.size();
        }
        if (modify != 0) {
            currNode.relativePosition += modify;
            ByteBigList.ByteBlockNode leftNode = currNode.getLeftSubTree();
            if (leftNode != null) {
                leftNode.relativePosition -= modify;
            }
        }
        done = true;
    } else if (index == 0) {
        if (currNode == null || currByteBlockStart != 0) {
            currNode = root.min();
            currByteBlock = currNode.getByteBlock();
            currByteBlockEnd = currByteBlock.size();
            currByteBlockStart = 0;
        }
        if (modify != 0) {
            root.relativePosition += modify;
        }
        done = true;
    }
    if (!done) {
        // Reset currByteBlockEnd, it will be then set by access()   
        currByteBlockEnd = 0;
        currNode = access(index, modify);
        currByteBlock = currNode.getByteBlock();
        currByteBlockStart = currByteBlockEnd - currByteBlock.size();
    }
    assert (index >= currByteBlockStart && index <= currByteBlockEnd);
    if (write) {
        if (currByteBlock.isShared()) {
            currByteBlock.unref();
            currByteBlock = new ByteBlock(currByteBlock);
            currNode.setByteBlock(currByteBlock);
        }
    }
    return index - currByteBlockStart;
}

    void checkNode(ByteBlockNode node) {
    assert ((node.block.size() > 0 || node == root) && node.block.size() <= blockSize);
    ByteBlockNode child = node.getLeftSubTree();
    assert (child == null || child.parent == node);
    child = node.getRightSubTree();
    assert (child == null || child.parent == node);
}

    void checkHeight(ByteBlockNode node) {
    ByteBlockNode left = node.getLeftSubTree();
    ByteBlockNode right = node.getRightSubTree();
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
        assert (currNode.block == currByteBlock);
        assert (currByteBlockStart >= 0 && currByteBlockEnd <= size && currByteBlockStart <= currByteBlockEnd);
        assert (currByteBlockStart + currByteBlock.size() == currByteBlockEnd);
    }
    if (root == null) {
        assert (size == 0);
        return;
    }
    checkHeight(root);
    ByteBlockNode oldCurrNode = currNode;
    int oldCurrModify = currModify;
    if (currModify != 0) {
        currNode = null;
        currModify = 0;
        modify(oldCurrNode, oldCurrModify);
    }
    ByteBlockNode node = root;
    checkNode(node);
    int index = node.relativePosition;
    while (node.left != null) {
        node = node.left;
        checkNode(node);
        assert (node.relativePosition < 0);
        index += node.relativePosition;
    }
    ByteBlock block = node.getByteBlock();
    assert (block.size() == index);
    int lastIndex = index;
    while (lastIndex < size()) {
        node = root;
        index = node.relativePosition;
        int searchIndex = lastIndex + 1;
        while (true) {
            checkNode(node);
            block = node.getByteBlock();
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
        block = node.getByteBlock();
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
protected boolean doAdd(int index, byte element) {
    if (index == -1) {
        index = size;
    }
    // Insert   
    int pos = getByteBlockIndex(index, true, 1);
    // If there is still place in the current block: insert in current block   
    int maxSize = (index == size || index == 0) ? blockSize * 9 / 10 : blockSize;
    if (currByteBlock.size() < maxSize) {
        currByteBlock.values.doAdd(pos, element);
        currByteBlockEnd++;
    } else {
        // No place any more in current block   
        ByteBlock nextByteBlock = new ByteBlock(blockSize);
        if (index == size) {
            // Insert new block at tail   
            nextByteBlock.values.doAdd(0, element);
            modify(currNode, -1);
            addByteBlock(size + 1, nextByteBlock);
            ByteBlockNode lastNode = currNode.next();
            currNode = lastNode;
            currByteBlock = currNode.block;
            currByteBlockStart = currByteBlockEnd;
            currByteBlockEnd++;
        } else if (index == 0) {
            // Insert new block at head   
            nextByteBlock.values.doAdd(0, element);
            modify(currNode, -1);
            addByteBlock(1, nextByteBlock);
            ByteBlockNode firstNode = currNode.previous();
            currNode = firstNode;
            currByteBlock = currNode.block;
            currByteBlockStart = 0;
            currByteBlockEnd = 1;
        } else {
            // Split block for insert   
            int nextByteBlockLen = blockSize / 2;
            int blockLen = blockSize - nextByteBlockLen;
            nextByteBlock.values.init(nextByteBlockLen, null);
            ByteGapList.copy(currByteBlock.values, blockLen, nextByteBlock.values, 0, nextByteBlockLen);
            currByteBlock.values.remove(blockLen, blockSize - blockLen);
            // Subtract 1 more because getByteBlockIndex() has already added 1   
            modify(currNode, -nextByteBlockLen - 1);
            addByteBlock(currByteBlockEnd - nextByteBlockLen, nextByteBlock);
            if (pos < blockLen) {
                // Insert element in first block   
                currByteBlock.values.doAdd(pos, element);
                currByteBlockEnd = currByteBlockStart + blockLen + 1;
                modify(currNode, 1);
            } else {
                // Insert element in second block   
                currNode = currNode.next();
                modify(currNode, 1);
                currByteBlock = currNode.block;
                currByteBlock.values.doAdd(pos - blockLen, element);
                currByteBlockStart += blockLen;
                currByteBlockEnd++;
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
private void modify(ByteBlockNode node, int modify) {
    if (node == currNode) {
        modify += currModify;
        currModify = 0;
    } else {
        releaseByteBlock();
    }
    if (modify == 0) {
        return;
    }
    if (node.relativePosition < 0) {
        // Left node   
        ByteBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        ByteBlockNode pp = node.parent;
        assert (pp.getLeftSubTree() == node);
        boolean parentRight = true;
        while (true) {
            ByteBlockNode p = pp.parent;
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
        ByteBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        ByteBlockNode parent = node.parent;
        if (parent != null) {
            assert (parent.getRightSubTree() == node);
            boolean parentLeft = true;
            while (true) {
                ByteBlockNode p = parent.parent;
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

    private ByteBlockNode doRemove(ByteBlockNode node) {
    ByteBlockNode p = node.parent;
    ByteBlockNode newNode = node.removeSelf();
    ByteBlockNode n = newNode;
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
protected boolean doAddAll(int index, byte[] array) {
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
    int addPos = getByteBlockIndex(index, true, 0);
    ByteBlock addByteBlock = currByteBlock;
    int space = blockSize - addByteBlock.size();
    int addLen = array.length;
    if (addLen <= space) {
        // All elements can be added to current block   
        currByteBlock.values.addAll(addPos, array);
        modify(currNode, addLen);
        size += addLen;
        currByteBlockEnd += addLen;
    } else {
        if (index == size) {
            // Add elements at end   
            for (int i = 0; i < space; i++) {
                currByteBlock.values.add(addPos, array[i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                ByteBlock nextByteBlock = new ByteBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextByteBlock.values.add(i, array[done + i]);
                }
                done += add;
                todo -= add;
                addByteBlock(size + done, nextByteBlock);
            }
            size += addLen;
            currNode = currNode.next();
            currByteBlock = currNode.block;
            currByteBlockStart = currByteBlockEnd + space;
            currByteBlockEnd = currByteBlockStart + addLen - space;
        } else if (index == 0) {
            // Add elements at head   
            assert (addPos == 0);
            for (int i = 0; i < space; i++) {
                currByteBlock.values.add(addPos, array[addLen - space + i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                ByteBlock nextByteBlock = new ByteBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextByteBlock.values.add(i, array[done + i]);
                }
                done += add;
                todo -= add;
                addByteBlock(0, nextByteBlock);
            }
            size += addLen;
            currNode = currNode.previous();
            currByteBlock = currNode.block;
            currByteBlockStart = 0;
            currByteBlockEnd = addLen - space;
        } else {
            // Add elements to several blocks   
            // Handle first block   
            ByteGapList list = ByteGapList.create(array);
            int remove = currByteBlock.values.size() - addPos;
            if (remove > 0) {
                list.addAll(currByteBlock.values.getAll(addPos, remove));
                currByteBlock.values.remove(addPos, remove);
                modify(currNode, -remove);
                size -= remove;
                currByteBlockEnd -= remove;
            }
            int s = currByteBlock.values.size() + list.size();
            int numByteBlocks = (s - 1) / blockSize + 1;
            assert (numByteBlocks > 1);
            int has = currByteBlock.values.size();
            int should = s / numByteBlocks;
            int start = 0;
            int end = 0;
            if (has < should) {
                // Elements must be added to first block   
                int add = should - has;
                IByteList sublist = list.getAll(0, add);
                currByteBlock.values.addAll(addPos, sublist);
                modify(currNode, add);
                start += add;
                assert (currByteBlock.values.size() == should);
                s -= should;
                numByteBlocks--;
                size += add;
                currByteBlockEnd += add;
                end = currByteBlockEnd;
            } else if (has > should) {
                // Elements must be moved from first to second block   
                ByteBlock nextByteBlock = new ByteBlock(blockSize);
                int move = has - should;
                nextByteBlock.values.addAll(currByteBlock.values.getAll(currByteBlock.values.size() - move, move));
                currByteBlock.values.remove(currByteBlock.values.size() - move, move);
                modify(currNode, -move);
                assert (currByteBlock.values.size() == should);
                s -= should;
                numByteBlocks--;
                currByteBlockEnd -= move;
                end = currByteBlockEnd;
                should = s / numByteBlocks;
                int add = should - move;
                assert (add >= 0);
                IByteList sublist = list.getAll(0, add);
                nextByteBlock.values.addAll(move, sublist);
                start += add;
                assert (nextByteBlock.values.size() == should);
                s -= should;
                numByteBlocks--;
                size += add;
                end += add;
                addByteBlock(end, nextByteBlock);
            } else {
                s -= should;
                numByteBlocks--;
            }
            check();
            while (numByteBlocks > 0) {
                int add = s / numByteBlocks;
                assert (add > 0);
                IByteList sublist = list.getAll(start, add);
                ByteBlock nextByteBlock = new ByteBlock();
                nextByteBlock.values.init(sublist);
                start += add;
                assert (nextByteBlock.values.size() == add);
                s -= add;
                end += add;
                addByteBlock(end, nextByteBlock);
                size += add;
                numByteBlocks--;
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
    currByteBlock = null;
    currByteBlockStart = 0;
    currByteBlockEnd = 0;
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
    int startPos = getByteBlockIndex(index, true, 0);
    ByteBlockNode startNode = currNode;
    int endPos = getByteBlockIndex(index + len - 1, true, 0);
    ByteBlockNode endNode = currNode;
    if (startNode == endNode) {
        // Delete from single block   
        getByteBlockIndex(index, true, -len);
        currByteBlock.values.remove(startPos, len);
        if (currByteBlock.values.isEmpty()) {
            ByteBlockNode oldCurrNode = currNode;
            releaseByteBlock();
            ByteBlockNode node = doRemove(oldCurrNode);
            merge(node);
        } else {
            currByteBlockEnd -= len;
            merge(currNode);
        }
        size -= len;
    } else {
        // Delete from start block   
        check();
        int startLen = startNode.block.size() - startPos;
        getByteBlockIndex(index, true, -startLen);
        // TODO should that be modify?   
        startNode.block.values.remove(startPos, startLen);
        assert (startNode == currNode);
        if (currByteBlock.values.isEmpty()) {
            releaseByteBlock();
            doRemove(startNode);
            startNode = null;
        }
        len -= startLen;
        size -= startLen;
        //check();   
        while (len > 0) {
            currNode = null;
            getByteBlockIndex(index, true, 0);
            int s = currByteBlock.size();
            if (s <= len) {
                modify(currNode, -s);
                ByteBlockNode oldCurrNode = currNode;
                releaseByteBlock();
                doRemove(oldCurrNode);
                if (oldCurrNode == endNode) {
                    endNode = null;
                }
                len -= s;
                size -= s;
                check();
            } else {
                modify(currNode, -len);
                currByteBlock.values.remove(0, len);
                size -= len;
                break;
            }
        }
        releaseByteBlock();
        check();
        getByteBlockIndex(index, false, 0);
        merge(currNode);
    }
    if (DUMP)
        dump();
    if (CHECK)
        check();
}

    void merge(ByteBlockNode node) {
    if (node == null) {
        return;
    }
    final int minByteBlockSize = Math.max(blockSize / 3, 1);
    if (node.block.values.size() >= minByteBlockSize) {
        return;
    }
    ByteBlockNode oldCurrNode = node;
    ByteBlockNode leftNode = node.previous();
    if (leftNode != null && leftNode.block.size() < minByteBlockSize) {
        // Merge with left block   
        int len = node.block.size();
        int dstSize = leftNode.getByteBlock().size();
        for (int i = 0; i < len; i++) {
            leftNode.block.values.add(null);
        }
        ByteGapList.copy(node.block.values, 0, leftNode.block.values, dstSize, len);
        assert (leftNode.block.values.size() <= blockSize);
        modify(leftNode, +len);
        modify(oldCurrNode, -len);
        releaseByteBlock();
        doRemove(oldCurrNode);
    } else {
        ByteBlockNode rightNode = node.next();
        if (rightNode != null && rightNode.block.size() < minByteBlockSize) {
            // Merge with right block   
            int len = node.block.size();
            for (int i = 0; i < len; i++) {
                rightNode.block.values.add(0, null);
            }
            ByteGapList.copy(node.block.values, 0, rightNode.block.values, 0, len);
            assert (rightNode.block.values.size() <= blockSize);
            modify(rightNode, +len);
            modify(oldCurrNode, -len);
            releaseByteBlock();
            doRemove(oldCurrNode);
        }
    }
}

    protected byte doRemove(int index) {
    int pos = getByteBlockIndex(index, true, -1);
    byte oldElem = currByteBlock.values.doRemove(pos);
    currByteBlockEnd--;
    final int minByteBlockSize = Math.max(blockSize / 3, 1);
    if (currByteBlock.size() < minByteBlockSize) {
        if (currByteBlock.size() == 0) {
            if (!isOnlyRootByteBlock()) {
                ByteBlockNode oldCurrNode = currNode;
                releaseByteBlock();
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
public ByteBigList unmodifiableList() {
    // Naming as in java.util.Collections#unmodifiableList   
    return new ImmutableByteBigList(this);
}

    @Override
protected void doEnsureCapacity(int minCapacity) {
    if (isOnlyRootByteBlock()) {
        if (minCapacity > blockSize) {
            minCapacity = blockSize;
        }
        currByteBlock.values.doEnsureCapacity(minCapacity);
    }
}

    @Override
public void trimToSize() {
    if (isOnlyRootByteBlock()) {
        currByteBlock.values.trimToSize();
    }
}

    @Override
protected IByteList doCreate(int capacity) {
    if (capacity <= blockSize) {
        return new ByteBigList(this.blockSize);
    } else {
        return new ByteBigList(this.blockSize, capacity);
    }
}

    @Override
public void sort(int index, int len) {
    checkRange(index, len);
    if (isOnlyRootByteBlock()) {
        currByteBlock.values.sort(index, len);
    } else {
        ByteMergeSort.sort(this, index, index + len);
    }
}

    @Override
public int binarySearch(int index, int len, byte key) {
    checkRange(index, len);
    if (isOnlyRootByteBlock()) {
        return currByteBlock.values.binarySearch(key);
    } else {
        return Collections.binarySearch((IByteList) this, key);
    }
}

    private boolean isOnlyRootByteBlock() {
    return root.left == null && root.right == null;
}

    public ByteBlockNode access(final int index, int modify) {
    return root.access(index, modify, false);
}

    //-----------------------------------------------------------------------  
/**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */
public void addByteBlock(int index, ByteBlock obj) {
    if (root == null) {
        root = new ByteBlockNode(null, index, obj, null, null);
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
public void removeByteBlock(int index) {
    root = root.remove(index);
}

    // --- Serialization ---  
/**
     * Serialize a ByteBigList object.
     *
     * @serialData The length of the array backing the <tt>ByteGapList</tt>
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
        oos.writeByte(doGet(i));
    }
}

    /**
     * Deserialize a ByteBigList object.
 	 *
     * @param ois  input stream for serialization
     * @throws 	   IOException if serialization fails
     * @throws 	   ClassNotFoundException if serialization fails
     */

private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    int blockSize = ois.readInt();
    int size = ois.readInt();
    int firstByteBlockSize = (size <= blockSize) ? size : -1;
    doInit(blockSize, firstByteBlockSize);
    for (int i = 0; i < size; i++) {
        add(ois.readByte());
    }
}

    //-----------------------------------------------------------------------  
    /**
     * Implements an AVLNode which keeps the offset updated.
     * <p>
     * This node contains the real work.
     * TreeList is just there to implement {@link java.util.IByteList}.
     * The nodes don't know the index of the object they are holding.  They
     * do know however their position relative to their parent node.
     * This allows to calculate the index of a node while traversing the tree.
     * <p>
     * The Faedelung calculation stores a flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    class ByteBlockNode {

        ByteBlockNode parent;

        /** The left child node or the predecessor if {@link #leftIsPrevious}.*/
        ByteBlockNode left;

        /** Flag indicating that left reference is not a subtree but the predecessor. */
        boolean leftIsPrevious;

        /** The right child node or the successor if {@link #rightIsNext}. */
        ByteBlockNode right;

        /** Flag indicating that right reference is not a subtree but the successor. */
        boolean rightIsNext;

        /** How many levels of left/right are below this one. */
        int height;

        /** The relative position, root holds absolute position. */
        int relativePosition;

        /** The stored block */
        ByteBlock block;

        /**
         * Constructs a new node with a relative position.
         *
         * @param relativePosition  the relative position of the node
         * @param obj  the value for the node
         * @param rightFollower the node with the value following this one
         * @param leftFollower the node with the value leading this one
         */
private ByteBlockNode(ByteBlockNode parent, final int relativePosition, final ByteBlock block, final ByteBlockNode rightFollower, final ByteBlockNode leftFollower){
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
public ByteBlock getByteBlock() {
    return block;
}

        /**
         * Sets the value.
         *
         * @param obj  the value to store
         */
public void setByteBlock(ByteBlock obj) {
    this.block = obj;
}

        private ByteBlockNode access(final int index, int modify, boolean wasLeft) {
    assert (index >= 0);
    if (relativePosition == 0) {
        if (modify != 0) {
            relativePosition += modify;
        }
        return this;
    }
    if (currByteBlockEnd == 0) {
        currByteBlockEnd = relativePosition;
    }
    ByteBlockNode leftNode = getLeftSubTree();
    int leftIndex = currByteBlockEnd - block.size();
    assert (leftIndex >= 0);
    if (index >= leftIndex && index < currByteBlockEnd) {
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
    if (index < currByteBlockEnd) {
        // left   
        ByteBlockNode nextNode = getLeftSubTree();
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
        currByteBlockEnd += nextNode.relativePosition;
        return nextNode.access(index, modify, wasLeft);
    } else {
        // right   
        ByteBlockNode nextNode = getRightSubTree();
        if (nextNode == null || wasLeft) {
            if (relativePosition > 0) {
                relativePosition += modify;
                ByteBlockNode left = getLeftSubTree();
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
        currByteBlockEnd += nextNode.relativePosition;
        return nextNode.access(index, modify, wasLeft);
    }
}

        /**
         * Gets the next node in the list after this one.
         *
         * @return the next node
         */
public ByteBlockNode next() {
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
public ByteBlockNode previous() {
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
private ByteBlockNode insert(int index, ByteBlock obj) {
    assert (relativePosition != 0);
    final int indexRelativeToMe = index - relativePosition;
    if (indexRelativeToMe < 0) {
        return insertOnLeft(indexRelativeToMe, obj);
    } else {
        return insertOnRight(indexRelativeToMe, obj);
    }
}

        private ByteBlockNode insertOnLeft(int indexRelativeToMe, ByteBlock obj) {
    if (getLeftSubTree() == null) {
        int pos;
        if (relativePosition >= 0) {
            pos = -relativePosition;
        } else {
            pos = -block.size();
        }
        setLeft(new ByteBlockNode(this, pos, obj, this, left), null);
    } else {
        setLeft(left.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition >= 0) {
        relativePosition += obj.size();
    }
    final ByteBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        private ByteBlockNode insertOnRight(int indexRelativeToMe, ByteBlock obj) {
    if (getRightSubTree() == null) {
        setRight(new ByteBlockNode(this, obj.size(), obj, right, this), null);
    } else {
        setRight(right.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition < 0) {
        relativePosition -= obj.size();
    }
    final ByteBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        //-----------------------------------------------------------------------  
/**
         * Gets the left node, returning null if its a faedelung.
         */
public ByteBlockNode getLeftSubTree() {
    return leftIsPrevious ? null : left;
}

        /**
         * Gets the right node, returning null if its a faedelung.
         */
public ByteBlockNode getRightSubTree() {
    return rightIsNext ? null : right;
}

        /**
         * Gets the rightmost child of this node.
         *
         * @return the rightmost child (greatest index)
         */
public ByteBlockNode max() {
    return getRightSubTree() == null ? this : right.max();
}

        /**
         * Gets the leftmost child of this node.
         *
         * @return the leftmost child (smallest index)
         */
public ByteBlockNode min() {
    return getLeftSubTree() == null ? this : left.min();
}

        /**
         * Removes the node at a given position.
         *
         * @param index is the index of the element to be removed relative to the position of
         * the parent node of the current node.
         */
private ByteBlockNode remove(final int index) {
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

        private ByteBlockNode removeMax() {
    if (getRightSubTree() == null) {
        return removeSelf();
    }
    setRight(right.removeMax(), right.right);
    if (relativePosition < 0) {
    }
    recalcHeight();
    return balance();
}

        private ByteBlockNode removeMin(int size) {
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
public ByteBlockNode removeSelf() {
    ByteBlockNode p = parent;
    ByteBlockNode n = doRemoveSelf();
    if (n != null) {
        assert (p != n);
        n.parent = p;
    }
    return n;
}

        public ByteBlockNode doRemoveSelf() {
    if (getRightSubTree() == null && getLeftSubTree() == null) {
        return (byte) 0;
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
        final ByteBlockNode rightMin = right.min();
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
        final ByteBlockNode leftMax = left.max();
        block = leftMax.block;
        if (rightIsNext) {
            right = leftMax.right;
        }
        final ByteBlockNode leftPrevious = left.left;
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
private ByteBlockNode balance() {
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
private int getOffset(final ByteBlockNode node) {
    if (node == null) {
        return 0;
    }
    return node.relativePosition;
}

        /**
         * Sets the relative position.
         */
private int setOffset(final ByteBlockNode node, final int newOffest) {
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
private int getHeight(final ByteBlockNode node) {
    return node == null ? -1 : node.height;
}

        /**
         * Returns the height difference right - left
         */
private int heightRightMinusLeft() {
    return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
}

        private ByteBlockNode rotateLeft() {
    assert (!rightIsNext);
    final ByteBlockNode newTop = right;
    // can't be faedelung!   
    final ByteBlockNode movedNode = getRightSubTree().getLeftSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    ByteBlockNode p = this.parent;
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

        private ByteBlockNode rotateRight() {
    assert (!leftIsPrevious);
    final ByteBlockNode newTop = left;
    // can't be faedelung   
    final ByteBlockNode movedNode = getLeftSubTree().getRightSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    ByteBlockNode p = this.parent;
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
private void setLeft(final ByteBlockNode node, final ByteBlockNode previous) {
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
private void setRight(final ByteBlockNode node, final ByteBlockNode next) {
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
