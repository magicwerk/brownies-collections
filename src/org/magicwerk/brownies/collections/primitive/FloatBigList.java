package org.magicwerk.brownies.collections.primitive;
import org.magicwerk.brownies.collections.helper.ArraysHelper;
import org.magicwerk.brownies.collections.helper.primitive.FloatBinarySearch;
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
import org.magicwerk.brownies.collections.helper.primitive.FloatMergeSort;

/**
 * The first block (FloatGapList) used grows dynamcically, all others
 * are allocated with fixed size. This is necessary to prevent starving
 * because of GC usage.
 *
 * @author Thomas Mauch
 * @version $Id: FloatBigList.java 2493 2014-10-12 00:40:31Z origo $
 */
public class FloatBigList extends IFloatList {
	public static IFloatList of(float[] values) {
		return new ImmutableFloatListArrayPrimitive(values);
	}

	public static IFloatList of(Float[] values) {
		return new ImmutableFloatListArrayWrapper(values);
	}

	public static IFloatList of(List<Float> values) {
		return new ImmutableFloatListList(values);
	}

    static class ImmutableFloatListArrayPrimitive extends ImmutableFloatList {
    	float[] values;

    	public ImmutableFloatListArrayPrimitive(float[] values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		protected float doGet(int index) {
			return values[index];
		}
    }

    static class ImmutableFloatListArrayWrapper extends ImmutableFloatList {
    	Float[] values;

    	public ImmutableFloatListArrayWrapper(Float[] values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		protected float doGet(int index) {
			return values[index];
		}
    }

    static class ImmutableFloatListList extends ImmutableFloatList {
    	List<Float> values;

    	public ImmutableFloatListList(List<Float> values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.size();
		}

		@Override
		protected float doGet(int index) {
			return values.get(index);
		}
    }

    protected static abstract class ImmutableFloatList extends IFloatList {

    	//-- Readers

		@Override
		public int capacity() {
			return size();
		}

		@Override
		public int binarySearch(int index, int len, float key) {
			return FloatBinarySearch.binarySearch(this, key, index, index+len);
		}

		@Override
		public IFloatList unmodifiableList() {
			return this;
		}

		@Override
		protected float getDefaultElem() {
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
		protected void doClone(IFloatList that) {
			error();
		}

		@Override
		protected float doSet(int index, float elem) {
			error();
			return 0;
		}

		@Override
		protected float doReSet(int index, float elem) {
			error();
			return 0;
		}

		@Override
		protected boolean doAdd(int index, float elem) {
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
		protected IFloatList doCreate(int capacity) {
			error();
			return null;
		}

		@Override
		protected void doAssign(IFloatList that) {
			error();
		}

		@Override
		protected float doRemove(int index) {
			error();
			return 0;
		}

		@Override
		public void sort(int index, int len) {
			error();
		}
    }

    /**
     * An immutable version of a FloatBigList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableFloatBigList extends FloatBigList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
protected ImmutableFloatBigList(FloatBigList that){
    super(true, that);
}

        @Override
protected boolean doAdd(int index, float elem) {
    error();
    return false;
}

        @Override
protected boolean doAddAll(int index, float[] elems) {
    error();
    return false;
}

        @Override
protected float doSet(int index, float elem) {
    error();
    return 0;
}

        @Override
protected void doSetAll(int index, float[] elems) {
    error();
}

        @Override
protected float doReSet(int index, float elem) {
    error();
    return 0;
}

        @Override
protected float doRemove(int index) {
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
    
    public static class FloatBlock implements Serializable {

        private FloatGapList values;

        private int refCount;

        public FloatBlock(){
    values = new FloatGapList();
    refCount = 1;
}

        public FloatBlock(int capacity){
    values = new FloatGapList(capacity);
    refCount = 1;
}

        public FloatBlock(FloatBlock that){
    values = new FloatGapList(that.values.capacity());
    values.init(that.values.getArray(0, that.values.size()));
    refCount = 1;
}

        public boolean isShared() {
    return refCount > 1;
}

        public FloatBlock ref() {
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

    /** Number of elements stored in this FloatBigList */
    private int size;

    /** The root node in the tree */
    private FloatBlockNode root;

    /** Current node */
    private FloatBlockNode currNode;

    /** FloatBlock of current node */
    private FloatBlock currFloatBlock;

    /** Start index of current block */
    private int currFloatBlockStart;

    /** End index of current block */
    private int currFloatBlockEnd;

    /** Modify value which must be applied before this block is not current any more */
    private int currModify;

    /**
     * Constructor used internally, e.g. for ImmutableFloatBigList.
     *
     * @param copy true to copy all instance values from source,
     *             if false nothing is done
     * @param that list to copy
     */
protected FloatBigList(boolean copy, FloatBigList that){
    if (copy) {
        this.blockSize = that.blockSize;
        this.currFloatBlock = that.currFloatBlock;
        this.currFloatBlockStart = that.currFloatBlockStart;
        this.currFloatBlockEnd = that.currFloatBlockEnd;
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
// This separate method is needed as the varargs variant creates the FloatGapList with specific size  
public static FloatBigList create() {
    return new FloatBigList();
}

    /**
     * Create new list with specified elements.
     *
     * @param coll      collection with element
     * @return          created list
     * @param        type of elements stored in the list
     */
public static FloatBigList create(Collection<Float> coll) {
    return new FloatBigList(coll);
}

    /**
	 * Create new list with specified elements.
	 *
	 * @param elems 	array with elements
	 * @return 			created list
	 * @param  		type of elements stored in the list
	 */
public static FloatBigList create(float... elems) {
    FloatBigList list = new FloatBigList();
    for (float elem : elems) {
        list.add(elem);
    }
    return list;
}

    /**
	 * Default constructor.
	 * The default block size is used.
	 */
public FloatBigList(){
    this(BLOCK_SIZE);
}

    /**
	 * Constructor.
	 *
	 * @param blockSize block size
	 */
public FloatBigList(int blockSize){
    if (blockSize < 2) {
        throw new IndexOutOfBoundsException("Invalid blockSize: " + blockSize);
    }
    doInit(blockSize, -1);
}

    public FloatBigList(Collection<Float> that){
    if (that instanceof FloatBigList) {
        doAssign((FloatBigList) that);
        doClone((FloatBigList) that);
    } else {
        blockSize = BLOCK_SIZE;
        currFloatBlock = new FloatBlock();
        addFloatBlock(0, currFloatBlock);
        for (Object obj : that.toArray()) {
            add((Float) obj);
        }
        assert (size() == that.size());
    }
}

    public void init() {
    clear();
}

    public void init(float... elems) {
    clear();
    for (float elem : elems) {
        add(elem);
    }
}

    public void init(Collection<Float> that) {
    clear();
    addAll(that);
}

    /**
     * Returns block size used for this FloatBigList.
     *
     * @return block size used for this FloatBigList
     */
public int blockSize() {
    return blockSize;
}

    //---  
private FloatBigList(int blockSize, int firstFloatBlockSize){
    doInit(blockSize, firstFloatBlockSize);
}

    void doInit(int blockSize, int firstFloatBlockSize) {
    this.blockSize = blockSize;
    // First block will grow until it reaches blockSize   
    if (firstFloatBlockSize <= 1) {
        currFloatBlock = new FloatBlock();
    } else {
        currFloatBlock = new FloatBlock(firstFloatBlockSize);
    }
    addFloatBlock(0, currFloatBlock);
}

    @Override
public FloatBigList copy() {
    return (FloatBigList) super.copy();
}

    @Override
protected void doAssign(IFloatList that) {
    FloatBigList list = (FloatBigList) that;
    this.blockSize = list.blockSize;
    this.currFloatBlock = list.currFloatBlock;
    this.currFloatBlockEnd = list.currFloatBlockEnd;
    this.currFloatBlockStart = list.currFloatBlockStart;
    this.currNode = list.currNode;
    this.root = list.root;
    this.size = list.size;
}

    @Override
protected void doClone(IFloatList that) {
    FloatBigList bigList = (FloatBigList) that;
    bigList.check();
    bigList.releaseFloatBlock();
    root = copy(bigList.root);
    currNode = null;
    currModify = 0;
    check();
}

    private FloatBlockNode copy(FloatBlockNode node) {
    FloatBlockNode newNode = node.min();
    int index = newNode.block.size();
    FloatBlockNode newRoot = new FloatBlockNode(null, index, newNode.block.ref(), null, null);
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
public float getDefaultElem() {
    return 0;
}

    @Override
protected void finalize() {
    // This list will be garbage collected, so unref all referenced blocks   
    FloatBlockNode node = root.min();
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
protected float doGet(int index) {
    int pos = getFloatBlockIndex(index, false, 0);
    return currFloatBlock.values.doGet(pos);
}

    @Override
protected float doSet(int index, float elem) {
    int pos = getFloatBlockIndex(index, true, 0);
    float oldElem = currFloatBlock.values.doGet(pos);
    currFloatBlock.values.doSet(pos, elem);
    return oldElem;
}

    @Override
protected float doReSet(int index, float elem) {
    int pos = getFloatBlockIndex(index, true, 0);
    float oldElem = currFloatBlock.values.doGet(pos);
    currFloatBlock.values.doSet(pos, elem);
    return oldElem;
}

    private void releaseFloatBlock() {
    if (currModify != 0) {
        int modify = currModify;
        currModify = 0;
        modify(currNode, modify);
    }
    currNode = null;
}

    /**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currFloatBlock to remember this last used block.
	 *
	 * @param index	list index (0 <= index <= size())
	 * @return		relative index within block
	 */
private int getFloatBlockIndex(int index, boolean write, int modify) {
    // Determine block where specified index is located and store it in currFloatBlock   
    if (currNode != null) {
        if (index >= currFloatBlockStart && (index < currFloatBlockEnd || index == currFloatBlockEnd && size == index)) {
            // currFloatBlock is already set correctly   
            if (write) {
                if (currFloatBlock.isShared()) {
                    currFloatBlock.unref();
                    currFloatBlock = new FloatBlock(currFloatBlock);
                    currNode.setFloatBlock(currFloatBlock);
                }
            }
            currModify += modify;
            return index - currFloatBlockStart;
        }
        releaseFloatBlock();
    }
    boolean done = false;
    if (index == size) {
        if (currNode == null || currFloatBlockEnd != size) {
            currNode = root.max();
            currFloatBlock = currNode.getFloatBlock();
            currFloatBlockEnd = size;
            currFloatBlockStart = size - currFloatBlock.size();
        }
        if (modify != 0) {
            currNode.relativePosition += modify;
            FloatBlockNode leftNode = currNode.getLeftSubTree();
            if (leftNode != null) {
                leftNode.relativePosition -= modify;
            }
        }
        done = true;
    } else if (index == 0) {
        if (currNode == null || currFloatBlockStart != 0) {
            currNode = root.min();
            currFloatBlock = currNode.getFloatBlock();
            currFloatBlockEnd = currFloatBlock.size();
            currFloatBlockStart = 0;
        }
        if (modify != 0) {
            root.relativePosition += modify;
        }
        done = true;
    }
    if (!done) {
        // Reset currFloatBlockEnd, it will be then set by access()   
        currFloatBlockEnd = 0;
        currNode = access(index, modify);
        currFloatBlock = currNode.getFloatBlock();
        currFloatBlockStart = currFloatBlockEnd - currFloatBlock.size();
    }
    assert (index >= currFloatBlockStart && index <= currFloatBlockEnd);
    if (write) {
        if (currFloatBlock.isShared()) {
            currFloatBlock.unref();
            currFloatBlock = new FloatBlock(currFloatBlock);
            currNode.setFloatBlock(currFloatBlock);
        }
    }
    return index - currFloatBlockStart;
}

    void checkNode(FloatBlockNode node) {
    assert ((node.block.size() > 0 || node == root) && node.block.size() <= blockSize);
    FloatBlockNode child = node.getLeftSubTree();
    assert (child == null || child.parent == node);
    child = node.getRightSubTree();
    assert (child == null || child.parent == node);
}

    void checkHeight(FloatBlockNode node) {
    FloatBlockNode left = node.getLeftSubTree();
    FloatBlockNode right = node.getRightSubTree();
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
        assert (currNode.block == currFloatBlock);
        assert (currFloatBlockStart >= 0 && currFloatBlockEnd <= size && currFloatBlockStart <= currFloatBlockEnd);
        assert (currFloatBlockStart + currFloatBlock.size() == currFloatBlockEnd);
    }
    if (root == null) {
        assert (size == 0);
        return;
    }
    checkHeight(root);
    FloatBlockNode oldCurrNode = currNode;
    int oldCurrModify = currModify;
    if (currModify != 0) {
        currNode = null;
        currModify = 0;
        modify(oldCurrNode, oldCurrModify);
    }
    FloatBlockNode node = root;
    checkNode(node);
    int index = node.relativePosition;
    while (node.left != null) {
        node = node.left;
        checkNode(node);
        assert (node.relativePosition < 0);
        index += node.relativePosition;
    }
    FloatBlock block = node.getFloatBlock();
    assert (block.size() == index);
    int lastIndex = index;
    while (lastIndex < size()) {
        node = root;
        index = node.relativePosition;
        int searchIndex = lastIndex + 1;
        while (true) {
            checkNode(node);
            block = node.getFloatBlock();
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
        block = node.getFloatBlock();
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
protected boolean doAdd(int index, float element) {
    if (index == -1) {
        index = size;
    }
    // Insert   
    int pos = getFloatBlockIndex(index, true, 1);
    // If there is still place in the current block: insert in current block   
    int maxSize = (index == size || index == 0) ? blockSize * 9 / 10 : blockSize;
    // The second part of the condition is a work around to handle the case of insertion as position 0 correctly   
    // where blockSize() is 2 (the new block would then be added after the current one)   
    if (currFloatBlock.size() < maxSize || (currFloatBlock.size() == 1 && currFloatBlock.size() < blockSize)) {
        currFloatBlock.values.doAdd(pos, element);
        currFloatBlockEnd++;
    } else {
        // No place any more in current block   
        FloatBlock newFloatBlock = new FloatBlock(blockSize);
        if (index == size) {
            // Insert new block at tail   
            newFloatBlock.values.doAdd(0, element);
            // Subtract 1 because getFloatBlockIndex() has already added 1   
            modify(currNode, -1);
            addFloatBlock(size + 1, newFloatBlock);
            FloatBlockNode lastNode = currNode.next();
            currNode = lastNode;
            currFloatBlock = currNode.block;
            currFloatBlockStart = currFloatBlockEnd;
            currFloatBlockEnd++;
        } else if (index == 0) {
            // Insert new block at head   
            newFloatBlock.values.doAdd(0, element);
            // Subtract 1 because getFloatBlockIndex() has already added 1   
            modify(currNode, -1);
            addFloatBlock(1, newFloatBlock);
            FloatBlockNode firstNode = currNode.previous();
            currNode = firstNode;
            currFloatBlock = currNode.block;
            currFloatBlockStart = 0;
            currFloatBlockEnd = 1;
        } else {
            // Split block for insert   
            int nextFloatBlockLen = blockSize / 2;
            int blockLen = blockSize - nextFloatBlockLen;
            newFloatBlock.values.init(nextFloatBlockLen, 0);
            FloatGapList.copy(currFloatBlock.values, blockLen, newFloatBlock.values, 0, nextFloatBlockLen);
            currFloatBlock.values.remove(blockLen, blockSize - blockLen);
            // Subtract 1 more because getFloatBlockIndex() has already added 1   
            modify(currNode, -nextFloatBlockLen - 1);
            addFloatBlock(currFloatBlockEnd - nextFloatBlockLen, newFloatBlock);
            if (pos < blockLen) {
                // Insert element in first block   
                currFloatBlock.values.doAdd(pos, element);
                currFloatBlockEnd = currFloatBlockStart + blockLen + 1;
                modify(currNode, 1);
            } else {
                // Insert element in second block   
                currNode = currNode.next();
                modify(currNode, 1);
                currFloatBlock = currNode.block;
                currFloatBlock.values.doAdd(pos - blockLen, element);
                currFloatBlockStart += blockLen;
                currFloatBlockEnd++;
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
private void modify(FloatBlockNode node, int modify) {
    if (node == currNode) {
        modify += currModify;
        currModify = 0;
    } else {
        releaseFloatBlock();
    }
    if (modify == 0) {
        return;
    }
    if (node.relativePosition < 0) {
        // Left node   
        FloatBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        FloatBlockNode pp = node.parent;
        assert (pp.getLeftSubTree() == node);
        boolean parentRight = true;
        while (true) {
            FloatBlockNode p = pp.parent;
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
        FloatBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        FloatBlockNode parent = node.parent;
        if (parent != null) {
            assert (parent.getRightSubTree() == node);
            boolean parentLeft = true;
            while (true) {
                FloatBlockNode p = parent.parent;
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

    private FloatBlockNode doRemove(FloatBlockNode node) {
    FloatBlockNode p = node.parent;
    FloatBlockNode newNode = node.removeSelf();
    FloatBlockNode n = newNode;
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
protected boolean doAddAll(int index, float[] array) {
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
    int addPos = getFloatBlockIndex(index, true, 0);
    FloatBlock addFloatBlock = currFloatBlock;
    int space = blockSize - addFloatBlock.size();
    int addLen = array.length;
    if (addLen <= space) {
        // All elements can be added to current block   
        currFloatBlock.values.addAll(addPos, array);
        modify(currNode, addLen);
        size += addLen;
        currFloatBlockEnd += addLen;
    } else {
        if (index == size) {
            // Add elements at end   
            for (int i = 0; i < space; i++) {
                currFloatBlock.values.add(addPos, array[i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                FloatBlock nextFloatBlock = new FloatBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextFloatBlock.values.add(i, array[done + i]);
                }
                done += add;
                todo -= add;
                addFloatBlock(size + done, nextFloatBlock);
            }
            size += addLen;
            currNode = currNode.next();
            currFloatBlock = currNode.block;
            currFloatBlockStart = currFloatBlockEnd + space;
            currFloatBlockEnd = currFloatBlockStart + addLen - space;
        } else if (index == 0) {
            // Add elements at head   
            assert (addPos == 0);
            for (int i = 0; i < space; i++) {
                currFloatBlock.values.add(addPos, array[addLen - space + i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                FloatBlock nextFloatBlock = new FloatBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextFloatBlock.values.add(i, array[done + i]);
                }
                done += add;
                todo -= add;
                addFloatBlock(0, nextFloatBlock);
            }
            size += addLen;
            currNode = currNode.previous();
            currFloatBlock = currNode.block;
            currFloatBlockStart = 0;
            currFloatBlockEnd = addLen - space;
        } else {
            // Add elements to several blocks   
            // Handle first block   
            FloatGapList list = FloatGapList.create(array);
            int remove = currFloatBlock.values.size() - addPos;
            if (remove > 0) {
                list.addAll(currFloatBlock.values.getAll(addPos, remove));
                currFloatBlock.values.remove(addPos, remove);
                modify(currNode, -remove);
                size -= remove;
                currFloatBlockEnd -= remove;
            }
            int s = currFloatBlock.values.size() + list.size();
            int numFloatBlocks = (s - 1) / blockSize + 1;
            assert (numFloatBlocks > 1);
            int has = currFloatBlock.values.size();
            int should = s / numFloatBlocks;
            int start = 0;
            int end = 0;
            if (has < should) {
                // Elements must be added to first block   
                int add = should - has;
                IFloatList sublist = list.getAll(0, add);
                currFloatBlock.values.addAll(addPos, sublist);
                modify(currNode, add);
                start += add;
                assert (currFloatBlock.values.size() == should);
                s -= should;
                numFloatBlocks--;
                size += add;
                currFloatBlockEnd += add;
                end = currFloatBlockEnd;
            } else if (has > should) {
                // Elements must be moved from first to second block   
                FloatBlock nextFloatBlock = new FloatBlock(blockSize);
                int move = has - should;
                nextFloatBlock.values.addAll(currFloatBlock.values.getAll(currFloatBlock.values.size() - move, move));
                currFloatBlock.values.remove(currFloatBlock.values.size() - move, move);
                modify(currNode, -move);
                assert (currFloatBlock.values.size() == should);
                s -= should;
                numFloatBlocks--;
                currFloatBlockEnd -= move;
                end = currFloatBlockEnd;
                should = s / numFloatBlocks;
                int add = should - move;
                assert (add >= 0);
                IFloatList sublist = list.getAll(0, add);
                nextFloatBlock.values.addAll(move, sublist);
                start += add;
                assert (nextFloatBlock.values.size() == should);
                s -= should;
                numFloatBlocks--;
                size += add;
                end += add;
                addFloatBlock(end, nextFloatBlock);
            } else {
                s -= should;
                numFloatBlocks--;
            }
            check();
            FloatBlockNode node = currNode;
            while (numFloatBlocks > 0) {
                int add = s / numFloatBlocks;
                assert (add > 0);
                IFloatList sublist = list.getAll(start, add);
                FloatBlock nextFloatBlock = new FloatBlock();
                nextFloatBlock.values.clear();
                nextFloatBlock.values.addAll(sublist);
                start += add;
                assert (nextFloatBlock.values.size() == add);
                s -= add;
                addFloatBlock(end, nextFloatBlock);
                assert (node.next().block == nextFloatBlock);
                node = node.next();
                end += add;
                size += add;
                numFloatBlocks--;
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
    currFloatBlock = null;
    currFloatBlockStart = 0;
    currFloatBlockEnd = 0;
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
    int startPos = getFloatBlockIndex(index, true, 0);
    FloatBlockNode startNode = currNode;
    int endPos = getFloatBlockIndex(index + len - 1, true, 0);
    FloatBlockNode endNode = currNode;
    if (startNode == endNode) {
        // Delete from single block   
        getFloatBlockIndex(index, true, -len);
        currFloatBlock.values.remove(startPos, len);
        if (currFloatBlock.values.isEmpty()) {
            FloatBlockNode oldCurrNode = currNode;
            releaseFloatBlock();
            FloatBlockNode node = doRemove(oldCurrNode);
            merge(node);
        } else {
            currFloatBlockEnd -= len;
            merge(currNode);
        }
        size -= len;
    } else {
        // Delete from start block   
        check();
        int startLen = startNode.block.size() - startPos;
        getFloatBlockIndex(index, true, -startLen);
        // TODO should that be modify?   
        startNode.block.values.remove(startPos, startLen);
        assert (startNode == currNode);
        if (currFloatBlock.values.isEmpty()) {
            releaseFloatBlock();
            doRemove(startNode);
            startNode = null;
        }
        len -= startLen;
        size -= startLen;
        //check();   
        while (len > 0) {
            currNode = null;
            getFloatBlockIndex(index, true, 0);
            int s = currFloatBlock.size();
            if (s <= len) {
                modify(currNode, -s);
                FloatBlockNode oldCurrNode = currNode;
                releaseFloatBlock();
                doRemove(oldCurrNode);
                if (oldCurrNode == endNode) {
                    endNode = null;
                }
                len -= s;
                size -= s;
                check();
            } else {
                modify(currNode, -len);
                currFloatBlock.values.remove(0, len);
                size -= len;
                break;
            }
        }
        releaseFloatBlock();
        check();
        getFloatBlockIndex(index, false, 0);
        merge(currNode);
    }
    if (DUMP)
        dump();
    if (CHECK)
        check();
}

    void merge(FloatBlockNode node) {
    if (node == null) {
        return;
    }
    final int minFloatBlockSize = Math.max(blockSize / 3, 1);
    if (node.block.values.size() >= minFloatBlockSize) {
        return;
    }
    FloatBlockNode oldCurrNode = node;
    FloatBlockNode leftNode = node.previous();
    if (leftNode != null && leftNode.block.size() < minFloatBlockSize) {
        // Merge with left block   
        int len = node.block.size();
        int dstSize = leftNode.getFloatBlock().size();
        for (int i = 0; i < len; i++) {
            leftNode.block.values.add(0);
        }
        FloatGapList.copy(node.block.values, 0, leftNode.block.values, dstSize, len);
        assert (leftNode.block.values.size() <= blockSize);
        modify(leftNode, +len);
        modify(oldCurrNode, -len);
        releaseFloatBlock();
        doRemove(oldCurrNode);
    } else {
        FloatBlockNode rightNode = node.next();
        if (rightNode != null && rightNode.block.size() < minFloatBlockSize) {
            // Merge with right block   
            int len = node.block.size();
            for (int i = 0; i < len; i++) {
                rightNode.block.values.add(0, 0);
            }
            FloatGapList.copy(node.block.values, 0, rightNode.block.values, 0, len);
            assert (rightNode.block.values.size() <= blockSize);
            modify(rightNode, +len);
            modify(oldCurrNode, -len);
            releaseFloatBlock();
            doRemove(oldCurrNode);
        }
    }
}

    protected float doRemove(int index) {
    int pos = getFloatBlockIndex(index, true, -1);
    float oldElem = currFloatBlock.values.doRemove(pos);
    currFloatBlockEnd--;
    final int minFloatBlockSize = Math.max(blockSize / 3, 1);
    if (currFloatBlock.size() < minFloatBlockSize) {
        if (currFloatBlock.size() == 0) {
            if (!isOnlyRootFloatBlock()) {
                FloatBlockNode oldCurrNode = currNode;
                releaseFloatBlock();
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
public FloatBigList unmodifiableList() {
    // Naming as in java.util.Collections#unmodifiableList   
    return new ImmutableFloatBigList(this);
}

    @Override
protected void doEnsureCapacity(int minCapacity) {
    if (isOnlyRootFloatBlock()) {
        if (minCapacity > blockSize) {
            minCapacity = blockSize;
        }
        currFloatBlock.values.doEnsureCapacity(minCapacity);
    }
}

    @Override
public void trimToSize() {
    if (isOnlyRootFloatBlock()) {
        currFloatBlock.values.trimToSize();
    }
}

    @Override
protected IFloatList doCreate(int capacity) {
    if (capacity <= blockSize) {
        return new FloatBigList(this.blockSize);
    } else {
        return new FloatBigList(this.blockSize, capacity);
    }
}

    @Override
public void sort(int index, int len) {
    checkRange(index, len);
    if (isOnlyRootFloatBlock()) {
        currFloatBlock.values.sort(index, len);
    } else {
        FloatMergeSort.sort(this, index, index + len);
    }
}

    @Override
public int binarySearch(int index, int len, float key) {
    checkRange(index, len);
    if (isOnlyRootFloatBlock()) {
        return currFloatBlock.values.binarySearch(key);
    } else {
        return FloatBinarySearch.binarySearch(this, key, 0, size());
    }
}

    private boolean isOnlyRootFloatBlock() {
    return root.left == null && root.right == null;
}

    public FloatBlockNode access(final int index, int modify) {
    return root.access(this, index, modify, false);
}

    //-----------------------------------------------------------------------  
/**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */
public void addFloatBlock(int index, FloatBlock obj) {
    if (root == null) {
        root = new FloatBlockNode(null, index, obj, null, null);
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
public void removeFloatBlock(int index) {
    root = root.remove(index);
}

    // --- Serialization ---  
/**
     * Serialize a FloatBigList object.
     *
     * @serialData The length of the array backing the <tt>FloatGapList</tt>
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
        oos.writeFloat(doGet(i));
    }
}

    /**
     * Deserialize a FloatBigList object.
 	 *
     * @param ois  input stream for serialization
     * @throws 	   IOException if serialization fails
     * @throws 	   ClassNotFoundException if serialization fails
     */

private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    int blockSize = ois.readInt();
    int size = ois.readInt();
    int firstFloatBlockSize = (size <= blockSize) ? size : -1;
    doInit(blockSize, firstFloatBlockSize);
    for (int i = 0; i < size; i++) {
        add(ois.readFloat());
    }
}

    //-----------------------------------------------------------------------  
    /**
     * Implements an AVLNode which keeps the offset updated.
     * <p>
     * This node contains the real work.
     * TreeList is just there to implement {@link java.util.IFloatList}.
     * The nodes don't know the index of the object they are holding.  They
     * do know however their position relative to their parent node.
     * This allows to calculate the index of a node while traversing the tree.
     * <p>
     * The Faedelung calculation stores a flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    static class FloatBlockNode {

        FloatBlockNode parent;

        /** The left child node or the predecessor if {@link #leftIsPrevious}.*/
        FloatBlockNode left;

        /** Flag indicating that left reference is not a subtree but the predecessor. */
        boolean leftIsPrevious;

        /** The right child node or the successor if {@link #rightIsNext}. */
        FloatBlockNode right;

        /** Flag indicating that right reference is not a subtree but the successor. */
        boolean rightIsNext;

        /** How many levels of left/right are below this one. */
        int height;

        /** The relative position, root holds absolute position. */
        int relativePosition;

        /** The stored block */
        FloatBlock block;

        /**
         * Constructs a new node with a relative position.
         *
         * @param relativePosition  the relative position of the node
         * @param obj  the value for the node
         * @param rightFollower the node with the value following this one
         * @param leftFollower the node with the value leading this one
         */
private FloatBlockNode(FloatBlockNode parent, final int relativePosition, final FloatBlock block, final FloatBlockNode rightFollower, final FloatBlockNode leftFollower){
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
public FloatBlock getFloatBlock() {
    return block;
}

        /**
         * Sets the value.
         *
         * @param obj  the value to store
         */
public void setFloatBlock(FloatBlock obj) {
    this.block = obj;
}

        private FloatBlockNode access(FloatBigList list, int index, int modify, boolean wasLeft) {
    assert (index >= 0);
    if (relativePosition == 0) {
        if (modify != 0) {
            relativePosition += modify;
        }
        return this;
    }
    if (list.currFloatBlockEnd == 0) {
        list.currFloatBlockEnd = relativePosition;
    }
    FloatBlockNode leftNode = getLeftSubTree();
    int leftIndex = list.currFloatBlockEnd - block.size();
    assert (leftIndex >= 0);
    if (index >= leftIndex && index < list.currFloatBlockEnd) {
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
    if (index < list.currFloatBlockEnd) {
        // left   
        FloatBlockNode nextNode = getLeftSubTree();
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
        list.currFloatBlockEnd += nextNode.relativePosition;
        return nextNode.access(list, index, modify, wasLeft);
    } else {
        // right   
        FloatBlockNode nextNode = getRightSubTree();
        if (nextNode == null || wasLeft) {
            if (relativePosition > 0) {
                relativePosition += modify;
                FloatBlockNode left = getLeftSubTree();
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
        list.currFloatBlockEnd += nextNode.relativePosition;
        return nextNode.access(list, index, modify, wasLeft);
    }
}

        /**
         * Gets the next node in the list after this one.
         *
         * @return the next node
         */
public FloatBlockNode next() {
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
public FloatBlockNode previous() {
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
private FloatBlockNode insert(int index, FloatBlock obj) {
    assert (relativePosition != 0);
    final int indexRelativeToMe = index - relativePosition;
    if (indexRelativeToMe < 0) {
        return insertOnLeft(indexRelativeToMe, obj);
    } else {
        return insertOnRight(indexRelativeToMe, obj);
    }
}

        private FloatBlockNode insertOnLeft(int indexRelativeToMe, FloatBlock obj) {
    if (getLeftSubTree() == null) {
        int pos;
        if (relativePosition >= 0) {
            pos = -relativePosition;
        } else {
            pos = -block.size();
        }
        setLeft(new FloatBlockNode(this, pos, obj, this, left), null);
    } else {
        setLeft(left.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition >= 0) {
        relativePosition += obj.size();
    }
    final FloatBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        private FloatBlockNode insertOnRight(int indexRelativeToMe, FloatBlock obj) {
    if (getRightSubTree() == null) {
        setRight(new FloatBlockNode(this, obj.size(), obj, right, this), null);
    } else {
        setRight(right.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition < 0) {
        relativePosition -= obj.size();
    }
    final FloatBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        //-----------------------------------------------------------------------  
/**
         * Gets the left node, returning null if its a faedelung.
         */
public FloatBlockNode getLeftSubTree() {
    return leftIsPrevious ? null : left;
}

        /**
         * Gets the right node, returning null if its a faedelung.
         */
public FloatBlockNode getRightSubTree() {
    return rightIsNext ? null : right;
}

        /**
         * Gets the rightmost child of this node.
         *
         * @return the rightmost child (greatest index)
         */
public FloatBlockNode max() {
    return getRightSubTree() == null ? this : right.max();
}

        /**
         * Gets the leftmost child of this node.
         *
         * @return the leftmost child (smallest index)
         */
public FloatBlockNode min() {
    return getLeftSubTree() == null ? this : left.min();
}

        /**
         * Removes the node at a given position.
         *
         * @param index is the index of the element to be removed relative to the position of
         * the parent node of the current node.
         */
private FloatBlockNode remove(final int index) {
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

        private FloatBlockNode removeMax() {
    if (getRightSubTree() == null) {
        return removeSelf();
    }
    setRight(right.removeMax(), right.right);
    if (relativePosition < 0) {
    }
    recalcHeight();
    return balance();
}

        private FloatBlockNode removeMin(int size) {
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
public FloatBlockNode removeSelf() {
    FloatBlockNode p = parent;
    FloatBlockNode n = doRemoveSelf();
    if (n != null) {
        assert (p != n);
        n.parent = p;
    }
    return n;
}

        public FloatBlockNode doRemoveSelf() {
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
        final FloatBlockNode rightMin = right.min();
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
        final FloatBlockNode leftMax = left.max();
        block = leftMax.block;
        if (rightIsNext) {
            right = leftMax.right;
        }
        final FloatBlockNode leftPrevious = left.left;
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
private FloatBlockNode balance() {
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
private int getOffset(final FloatBlockNode node) {
    if (node == null) {
        return 0;
    }
    return node.relativePosition;
}

        /**
         * Sets the relative position.
         */
private int setOffset(final FloatBlockNode node, final int newOffest) {
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
private int getHeight(final FloatBlockNode node) {
    return node == null ? -1 : node.height;
}

        /**
         * Returns the height difference right - left
         */
private int heightRightMinusLeft() {
    return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
}

        private FloatBlockNode rotateLeft() {
    assert (!rightIsNext);
    final FloatBlockNode newTop = right;
    // can't be faedelung!   
    final FloatBlockNode movedNode = getRightSubTree().getLeftSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    FloatBlockNode p = this.parent;
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

        private FloatBlockNode rotateRight() {
    assert (!leftIsPrevious);
    final FloatBlockNode newTop = left;
    // can't be faedelung   
    final FloatBlockNode movedNode = getLeftSubTree().getRightSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    FloatBlockNode p = this.parent;
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
private void setLeft(final FloatBlockNode node, final FloatBlockNode previous) {
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
private void setRight(final FloatBlockNode node, final FloatBlockNode next) {
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
