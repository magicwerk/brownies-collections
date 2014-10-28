package org.magicwerk.brownies.collections.primitive;
import org.magicwerk.brownies.collections.helper.ArraysHelper;
import org.magicwerk.brownies.collections.helper.primitive.FloatBinarySearch;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.BigList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.magicwerk.brownies.collections.helper.primitive.FloatMergeSort;

/**
 * FloatBigList is a list optimized for storing large number of elements.
 * It stores the elements in fixed size blocks and the blocks itself are maintained in a tree for fast access.
 * It also offers specialized methods for bulk processing of elements.
 * Also copying a FloatBigList is efficiently possible as its implemented using a copy-on-write approach.<p>
 *
 * <strong>Note that this implementation is not synchronized.</strong>
 * Due to data caching used for exploiting locality of reference, performance can decrease if FloatBigList is
 * accessed by several threads at different positions.<p>
 *
 * Note that the iterators provided are not fail-fast.<p>
 *
 * @author Thomas Mauch
 * @version $Id: FloatBigList.java 2531 2014-10-24 13:04:07Z origo $
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

    /** UID for serialization */
    private static final long serialVersionUID = 3715838828540564836L;

    /** Default block size */
    private static final int DEFAULT_BLOCK_SIZE = 1000;

    /** If two adjacent blocks both less than MERGE_THRESHOLD*blockSize elements, they are merged */
    private static final float MERGE_THRESHOLD = 0.35f;

    /**
	 * If an element is added to the list at the head or tail, the block is only filled until it
	 * has FILL_THRESHOLD*blockSize elements (so there is room for insertion without need to split).
	 */
    private static final float FILL_THRESHOLD = 0.95f;

    /** Set to true for debugging during developing */
    private static final boolean CHECK = false;

    // -- EMPTY --  
    // Cannot make a static reference to the non-static type E:  
    // public static FloatBigList EMPTY = FloatBigList.create().unmodifiableList();  
    // Syntax error:  
    // public static  FloatBigList EMPTY = FloatBigList.create().unmodifiableList();  
    /** Unmodifiable empty instance */
    
    private static final FloatBigList EMPTY = FloatBigList.create().unmodifiableList();

    /**
     * @return unmodifiable empty instance
     */

public static  FloatBigList EMPTY() {
    return EMPTY;
}

    /** Number of elements stored at maximum in a block */
    private int blockSize;

    /** Number of elements stored in this FloatBigList */
    private int size;

    /** The root node in the tree */
    private FloatBlockNode rootNode;

    /** Current node */
    private FloatBlockNode currNode;

    /** FloatBlock of current node */
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
        this.currFloatBlockStart = that.currFloatBlockStart;
        this.currFloatBlockEnd = that.currFloatBlockEnd;
        this.currNode = that.currNode;
        this.rootNode = that.rootNode;
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
    this(DEFAULT_BLOCK_SIZE);
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

    /**
     * Create new list with specified elements.
     *
     * @param coll      collection with element
     * @return          created list
     * @param        type of elements stored in the list
     */

public FloatBigList(Collection<Float> coll){
    if (coll instanceof FloatBigList) {
        doAssign((FloatBigList) coll);
        doClone((FloatBigList) coll);
    } else {
        blockSize = DEFAULT_BLOCK_SIZE;
        addFloatBlock(0, new FloatBlock());
        for (Object obj : coll.toArray()) {
            add((Float) obj);
        }
        assert (size() == coll.size());
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
public void init(float... elems) {
    clear();
    for (float elem : elems) {
        add(elem);
    }
}

    /**
     * Initialize the list to have all elements in the specified collection.
     *
     * @param that	collection
     */
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

    /**
	 * Internal constructor.
	 *
	 * @param blockSize			default block size
	 * @param firstFloatBlockSize	block size of first block
	 */
private FloatBigList(int blockSize, int firstFloatBlockSize){
    doInit(blockSize, firstFloatBlockSize);
}

    /**
	 * Initialize FloatBigList.
	 *
	 * @param blockSize			default block size
	 * @param firstFloatBlockSize	block size of first block
	 */
private void doInit(int blockSize, int firstFloatBlockSize) {
    this.blockSize = blockSize;
    // First block will grow until it reaches blockSize   
    FloatBlock block;
    if (firstFloatBlockSize <= 1) {
        block = new FloatBlock();
    } else {
        block = new FloatBlock(firstFloatBlockSize);
    }
    addFloatBlock(0, block);
}

    /**
     * Returns a copy of this <tt>FloatBigList</tt> instance.
     * The copy is realized by a copy-on-write approach so also really large lists can efficiently be copied.
     * This method is identical to clone() except that the result is casted to FloatBigList.
     *
     * @return a copy of this <tt>FloatBigList</tt> instance
	 */
@Override
public FloatBigList copy() {
    return (FloatBigList) super.copy();
}

    /**
     * Returns a shallow copy of this <tt>FloatBigList</tt> instance
     * The copy is realized by a copy-on-write approach so also really large lists can efficiently be copied.
     *
     * @return a copy of this <tt>FloatBigList</tt> instance
     */
// Only overridden to change Javadoc  
@Override
public Object clone() {
    return super.clone();
}

    @Override
protected void doAssign(IFloatList that) {
    FloatBigList list = (FloatBigList) that;
    this.blockSize = list.blockSize;
    this.currFloatBlockEnd = list.currFloatBlockEnd;
    this.currFloatBlockStart = list.currFloatBlockStart;
    this.currNode = list.currNode;
    this.rootNode = list.rootNode;
    this.size = list.size;
}

    @Override
protected void doClone(IFloatList that) {
    FloatBigList bigList = (FloatBigList) that;
    bigList.releaseFloatBlock();
    rootNode = copy(bigList.rootNode);
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
    FloatBlockNode node = rootNode.min();
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
	 * For FloatBigList, always -1 is returned.
	 */
@Override
public int capacity() {
    return -1;
}

    @Override
protected float doGet(int index) {
    int pos = getFloatBlockIndex(index, false, 0);
    return currNode.block.doGet(pos);
}

    @Override
protected float doSet(int index, float elem) {
    int pos = getFloatBlockIndex(index, true, 0);
    float oldElem = currNode.block.doGet(pos);
    currNode.block.doSet(pos, elem);
    return oldElem;
}

    @Override
protected float doReSet(int index, float elem) {
    int pos = getFloatBlockIndex(index, true, 0);
    float oldElem = currNode.block.doGet(pos);
    currNode.block.doSet(pos, elem);
    return oldElem;
}

    /**
	 * Release current block and apply modification if pending.
	 */
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
	 * @param index		list index (0 <= index <= size())
	 * @param write		true if the block is needed for a write operation (set, add, remove)
	 * @param modify	modify instruction (N>0: N elements are added, N<0: N elements are removed, 0 no change)
	 * @return			relative index within block
	 */
private int getFloatBlockIndex(int index, boolean write, int modify) {
    // Determine block where specified index is located and store it in currFloatBlock   
    if (currNode != null) {
        if (index >= currFloatBlockStart && (index < currFloatBlockEnd || index == currFloatBlockEnd && size == index)) {
            // currFloatBlock is already set correctly   
            if (write) {
                if (currNode.block.isShared()) {
                    currNode.block.unref();
                    currNode.setFloatBlock(new FloatBlock(currNode.block));
                }
            }
            currModify += modify;
            return index - currFloatBlockStart;
        }
        releaseFloatBlock();
    }
    if (index == size) {
        if (currNode == null || currFloatBlockEnd != size) {
            currNode = rootNode.max();
            currFloatBlockEnd = size;
            currFloatBlockStart = size - currNode.block.size();
        }
        if (modify != 0) {
            currNode.relPos += modify;
            FloatBlockNode leftNode = currNode.getLeftSubTree();
            if (leftNode != null) {
                leftNode.relPos -= modify;
            }
        }
    } else if (index == 0) {
        if (currNode == null || currFloatBlockStart != 0) {
            currNode = rootNode.min();
            currFloatBlockEnd = currNode.block.size();
            currFloatBlockStart = 0;
        }
        if (modify != 0) {
            rootNode.relPos += modify;
        }
    }
    if (currNode == null) {
        doGetFloatBlock(index, modify);
    }
    assert (index >= currFloatBlockStart && index <= currFloatBlockEnd);
    if (write) {
        if (currNode.block.isShared()) {
            currNode.block.unref();
            currNode.setFloatBlock(new FloatBlock(currNode.block));
        }
    }
    return index - currFloatBlockStart;
}

    /**
	 * @return true if there is only the root block, false otherwise
	 */
private boolean isOnlyRootFloatBlock() {
    return rootNode.left == null && rootNode.right == null;
}

    /**
     * Determine node/block for the specified index.
     * The fields currNode, currFloatBlockStart, and currFloatBlockEnd are set.
     * During the traversing the tree node, the nodes relative positions are changed according to the modify instruction.
     *
     * @param index		list index for which block must be determined
     * @param modify	modify instruction (N>0: N elements are added, N<0: N elements are removed, 0 no change)
     */
private void doGetFloatBlock(int index, int modify) {
    currNode = rootNode;
    currFloatBlockEnd = rootNode.relPos;
    if (currNode.relPos == 0) {
        // Empty tree   
        if (modify != 0) {
            currNode.relPos += modify;
        }
    } else {
        // Traverse non-empty tree until right node has been found   
        boolean wasLeft = false;
        while (true) {
            assert (index >= 0);
            int leftIndex = currFloatBlockEnd - currNode.block.size();
            assert (leftIndex >= 0);
            if (index >= leftIndex && index < currFloatBlockEnd) {
                // Correct node has been found   
                if (modify != 0) {
                    FloatBlockNode leftNode = currNode.getLeftSubTree();
                    if (currNode.relPos > 0) {
                        currNode.relPos += modify;
                        if (leftNode != null) {
                            leftNode.relPos -= modify;
                        }
                    } else {
                        if (leftNode != null) {
                            leftNode.relPos -= modify;
                        }
                    }
                }
                break;
            }
            // Further traversal needed to find the correct node   
            FloatBlockNode nextNode;
            if (index < currFloatBlockEnd) {
                // Traverse the left node   
                nextNode = currNode.getLeftSubTree();
                if (modify != 0) {
                    if (nextNode == null || !wasLeft) {
                        if (currNode.relPos > 0) {
                            currNode.relPos += modify;
                        } else {
                            currNode.relPos -= modify;
                        }
                        wasLeft = true;
                    }
                }
                if (nextNode == null) {
                    break;
                }
            } else {
                // Traverse the right node   
                nextNode = currNode.getRightSubTree();
                if (modify != 0) {
                    if (nextNode == null || wasLeft) {
                        if (currNode.relPos > 0) {
                            currNode.relPos += modify;
                            FloatBlockNode left = currNode.getLeftSubTree();
                            if (left != null) {
                                left.relPos -= modify;
                            }
                        } else {
                            currNode.relPos -= modify;
                        }
                        wasLeft = false;
                    }
                }
                if (nextNode == null) {
                    break;
                }
            }
            currFloatBlockEnd += nextNode.relPos;
            currNode = nextNode;
        }
    }
    currFloatBlockStart = currFloatBlockEnd - currNode.block.size();
}

    /**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */
private void addFloatBlock(int index, FloatBlock obj) {
    if (rootNode == null) {
        rootNode = new FloatBlockNode(null, index, obj, null, null);
    } else {
        rootNode = rootNode.insert(index, obj);
        rootNode.parent = null;
    }
}

    @Override
protected boolean doAdd(int index, float element) {
    if (index == -1) {
        index = size;
    }
    // Insert   
    int pos = getFloatBlockIndex(index, true, 1);
    // If there is still place in the current block: insert in current block   
    int maxSize = (index == size || index == 0) ? (int) (blockSize * FILL_THRESHOLD) : blockSize;
    // The second part of the condition is a work around to handle the case of insertion as position 0 correctly   
    // where blockSize() is 2 (the new block would then be added after the current one)   
    if (currNode.block.size() < maxSize || (currNode.block.size() == 1 && currNode.block.size() < blockSize)) {
        currNode.block.doAdd(pos, element);
        currFloatBlockEnd++;
    } else {
        // No place any more in current block   
        FloatBlock newFloatBlock = new FloatBlock(blockSize);
        if (index == size) {
            // Insert new block at tail   
            newFloatBlock.doAdd(0, element);
            // Subtract 1 because getFloatBlockIndex() has already added 1   
            modify(currNode, -1);
            addFloatBlock(size + 1, newFloatBlock);
            FloatBlockNode lastNode = currNode.next();
            currNode = lastNode;
            currFloatBlockStart = currFloatBlockEnd;
            currFloatBlockEnd++;
        } else if (index == 0) {
            // Insert new block at head   
            newFloatBlock.doAdd(0, element);
            // Subtract 1 because getFloatBlockIndex() has already added 1   
            modify(currNode, -1);
            addFloatBlock(1, newFloatBlock);
            FloatBlockNode firstNode = currNode.previous();
            currNode = firstNode;
            currFloatBlockStart = 0;
            currFloatBlockEnd = 1;
        } else {
            // Split block for insert   
            int nextFloatBlockLen = blockSize / 2;
            int blockLen = blockSize - nextFloatBlockLen;
            newFloatBlock.init(nextFloatBlockLen, 0);
            FloatGapList.copy(currNode.block, blockLen, newFloatBlock, 0, nextFloatBlockLen);
            currNode.block.remove(blockLen, blockSize - blockLen);
            // Subtract 1 more because getFloatBlockIndex() has already added 1   
            modify(currNode, -nextFloatBlockLen - 1);
            addFloatBlock(currFloatBlockEnd - nextFloatBlockLen, newFloatBlock);
            if (pos < blockLen) {
                // Insert element in first block   
                currNode.block.doAdd(pos, element);
                currFloatBlockEnd = currFloatBlockStart + blockLen + 1;
                modify(currNode, 1);
            } else {
                // Insert element in second block   
                currNode = currNode.next();
                modify(currNode, 1);
                currNode.block.doAdd(pos - blockLen, element);
                currFloatBlockStart += blockLen;
                currFloatBlockEnd++;
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
    if (node.relPos < 0) {
        // Left node   
        FloatBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relPos -= modify;
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
                if (pp.relPos > 0) {
                    pp.relPos += modify;
                } else {
                    pp.relPos -= modify;
                }
            }
            pp = p;
            parentRight = pRight;
        }
        if (parentRight) {
            rootNode.relPos += modify;
        }
    } else {
        // Right node   
        node.relPos += modify;
        FloatBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relPos -= modify;
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
                    if (parent.relPos > 0) {
                        parent.relPos += modify;
                    } else {
                        parent.relPos -= modify;
                    }
                }
                parent = p;
                parentLeft = pLeft;
            }
            if (!parentLeft) {
                rootNode.relPos += modify;
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
    rootNode = newNode;
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
    if (CHECK)
        check();
    int oldSize = size;
    if (array.length == 1) {
        return doAdd(index, array[0]);
    }
    int addPos = getFloatBlockIndex(index, true, 0);
    FloatBlock addFloatBlock = currNode.block;
    int space = blockSize - addFloatBlock.size();
    int addLen = array.length;
    if (addLen <= space) {
        // All elements can be added to current block   
        currNode.block.addAll(addPos, array);
        modify(currNode, addLen);
        size += addLen;
        currFloatBlockEnd += addLen;
    } else {
        if (index == size) {
            // Add elements at end   
            for (int i = 0; i < space; i++) {
                currNode.block.add(addPos + i, array[i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                FloatBlock nextFloatBlock = new FloatBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextFloatBlock.add(i, array[done + i]);
                }
                done += add;
                todo -= add;
                addFloatBlock(size + done, nextFloatBlock);
                currNode = currNode.next();
            }
            size += addLen;
            currFloatBlockEnd = size;
            currFloatBlockStart = currFloatBlockEnd - currNode.block.size();
        } else if (index == 0) {
            // Add elements at head   
            assert (addPos == 0);
            for (int i = 0; i < space; i++) {
                currNode.block.add(addPos + i, array[addLen - space + i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                FloatBlock nextFloatBlock = new FloatBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextFloatBlock.add(i, array[addLen - done - add + i]);
                }
                done += add;
                todo -= add;
                addFloatBlock(0, nextFloatBlock);
                currNode = currNode.previous();
            }
            size += addLen;
            currFloatBlockStart = 0;
            currFloatBlockEnd = currNode.block.size();
        } else {
            // Add elements in the middle   
            // Split first block to remove tail elements if necessary   
            FloatGapList list = FloatGapList.create(array);
            int remove = currNode.block.size() - addPos;
            if (remove > 0) {
                list.addAll(currNode.block.getAll(addPos, remove));
                currNode.block.remove(addPos, remove);
                modify(currNode, -remove);
                size -= remove;
                currFloatBlockEnd -= remove;
            }
            // Calculate how many blocks we need for the elements   
            int numElems = currNode.block.size() + list.size();
            int numFloatBlocks = (numElems - 1) / blockSize + 1;
            assert (numFloatBlocks > 1);
            int has = currNode.block.size();
            int should = numElems / numFloatBlocks;
            int listPos = 0;
            if (has < should) {
                // Elements must be added to first block   
                int add = should - has;
                IFloatList sublist = list.getAll(0, add);
                listPos += add;
                currNode.block.addAll(addPos, sublist);
                modify(currNode, add);
                assert (currNode.block.size() == should);
                numElems -= should;
                numFloatBlocks--;
                size += add;
                currFloatBlockEnd += add;
            } else if (has > should) {
                // Elements must be moved from first to second block   
                FloatBlock nextFloatBlock = new FloatBlock(blockSize);
                int move = has - should;
                nextFloatBlock.addAll(currNode.block.getAll(currNode.block.size() - move, move));
                currNode.block.remove(currNode.block.size() - move, move);
                modify(currNode, -move);
                assert (currNode.block.size() == should);
                numElems -= should;
                numFloatBlocks--;
                currFloatBlockEnd -= move;
                should = numElems / numFloatBlocks;
                int add = should - move;
                assert (add >= 0);
                IFloatList sublist = list.getAll(0, add);
                nextFloatBlock.addAll(move, sublist);
                listPos += add;
                assert (nextFloatBlock.size() == should);
                numElems -= should;
                numFloatBlocks--;
                size += add;
                addFloatBlock(currFloatBlockEnd, nextFloatBlock);
                currNode = currNode.next();
                assert (currNode.block == nextFloatBlock);
                assert (currNode.block.size() == add + move);
                currFloatBlockStart = currFloatBlockEnd;
                currFloatBlockEnd += add + move;
            } else {
                // FloatBlock already has the correct size   
                numElems -= should;
                numFloatBlocks--;
            }
            if (CHECK)
                check();
            while (numFloatBlocks > 0) {
                int add = numElems / numFloatBlocks;
                assert (add > 0);
                IFloatList sublist = list.getAll(listPos, add);
                listPos += add;
                FloatBlock nextFloatBlock = new FloatBlock();
                nextFloatBlock.addAll(sublist);
                assert (nextFloatBlock.size() == add);
                numElems -= add;
                addFloatBlock(currFloatBlockEnd, nextFloatBlock);
                currNode = currNode.next();
                assert (currNode.block == nextFloatBlock);
                assert (currNode.block.size() == add);
                currFloatBlockStart = currFloatBlockEnd;
                currFloatBlockEnd += add;
                size += add;
                numFloatBlocks--;
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
    rootNode = null;
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
    int startPos = getFloatBlockIndex(index, true, 0);
    FloatBlockNode startNode = currNode;
     int endPos = getFloatBlockIndex(index + len - 1, true, 0);
    FloatBlockNode endNode = currNode;
    if (startNode == endNode) {
        // Delete from single block   
        getFloatBlockIndex(index, true, -len);
        currNode.block.remove(startPos, len);
        if (currNode.block.isEmpty()) {
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
        if (CHECK)
            check();
        int startLen = startNode.block.size() - startPos;
        getFloatBlockIndex(index, true, -startLen);
        startNode.block.remove(startPos, startLen);
        assert (startNode == currNode);
        if (currNode.block.isEmpty()) {
            releaseFloatBlock();
            doRemove(startNode);
            startNode = null;
        }
        len -= startLen;
        size -= startLen;
        while (len > 0) {
            currNode = null;
            getFloatBlockIndex(index, true, 0);
            int s = currNode.block.size();
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
                if (CHECK)
                    check();
            } else {
                modify(currNode, -len);
                currNode.block.remove(0, len);
                size -= len;
                break;
            }
        }
        releaseFloatBlock();
        if (CHECK)
            check();
        getFloatBlockIndex(index, false, 0);
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
private void merge(FloatBlockNode node) {
    if (node == null) {
        return;
    }
    final int minFloatBlockSize = Math.max((int) (blockSize * MERGE_THRESHOLD), 1);
    if (node.block.size() >= minFloatBlockSize) {
        return;
    }
    FloatBlockNode oldCurrNode = node;
    FloatBlockNode leftNode = node.previous();
    if (leftNode != null && leftNode.block.size() < minFloatBlockSize) {
        // Merge with left block   
        int len = node.block.size();
        int dstSize = leftNode.getFloatBlock().size();
        for (int i = 0; i < len; i++) {
            leftNode.block.add(0);
        }
        FloatGapList.copy(node.block, 0, leftNode.block, dstSize, len);
        assert (leftNode.block.size() <= blockSize);
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
                rightNode.block.add(0, 0);
            }
            FloatGapList.copy(node.block, 0, rightNode.block, 0, len);
            assert (rightNode.block.size() <= blockSize);
            modify(rightNode, +len);
            modify(oldCurrNode, -len);
            releaseFloatBlock();
            doRemove(oldCurrNode);
        }
    }
}

    protected float doRemove(int index) {
    int pos = getFloatBlockIndex(index, true, -1);
    float oldElem = currNode.block.doRemove(pos);
    currFloatBlockEnd--;
    final int minFloatBlockSize = Math.max(blockSize / 3, 1);
    if (currNode.block.size() < minFloatBlockSize) {
        if (currNode.block.size() == 0) {
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
    if (CHECK)
        check();
    return oldElem;
}

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
        rootNode.block.doEnsureCapacity(minCapacity);
    }
}

    /**
     * Pack as many elements in the blocks as allowed.
     * An application can use this operation to minimize the storage of an instance.
     */
@Override
public void trimToSize() {
    doModify();
    if (isOnlyRootFloatBlock()) {
        rootNode.block.trimToSize();
    } else {
        FloatBigList newList = new FloatBigList(blockSize);
        FloatBlockNode node = rootNode.min();
        while (node != null) {
            newList.addAll(node.block);
            remove(0, node.block.size());
            node = node.next();
        }
        doAssign(newList);
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
        currNode.block.sort(index, len);
    } else {
        FloatMergeSort.sort(this, index, index + len);
    }
}

    
@Override
public int binarySearch(int index, int len, float key) {
    checkRange(index, len);
    if (isOnlyRootFloatBlock()) {
        return rootNode.block.binarySearch(key);
    } else {
        return FloatBinarySearch.binarySearch(this, key, 0, size());
    }
}

    // --- Serialization ---  
/**
     * Serialize a FloatBigList object.
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

    // --- Debug checks ---  
private void checkNode(FloatBlockNode node) {
    assert ((node.block.size() > 0 || node == rootNode) && node.block.size() <= blockSize);
    FloatBlockNode child = node.getLeftSubTree();
    assert (child == null || child.parent == node);
    child = node.getRightSubTree();
    assert (child == null || child.parent == node);
}

    private void checkHeight(FloatBlockNode node) {
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

    private void check() {
    if (currNode != null) {
        assert (currFloatBlockStart >= 0 && currFloatBlockEnd <= size && currFloatBlockStart <= currFloatBlockEnd);
        assert (currFloatBlockStart + currNode.block.size() == currFloatBlockEnd);
    }
    if (rootNode == null) {
        assert (size == 0);
        return;
    }
    checkHeight(rootNode);
    FloatBlockNode oldCurrNode = currNode;
    int oldCurrModify = currModify;
    if (currModify != 0) {
        currNode = null;
        currModify = 0;
        modify(oldCurrNode, oldCurrModify);
    }
    FloatBlockNode node = rootNode;
    checkNode(node);
    int index = node.relPos;
    while (node.left != null) {
        node = node.left;
        checkNode(node);
        assert (node.relPos < 0);
        index += node.relPos;
    }
    FloatBlock block = node.getFloatBlock();
    assert (block.size() == index);
    int lastIndex = index;
    while (lastIndex < size()) {
        node = rootNode;
        index = node.relPos;
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
            index += node.relPos;
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

    // --- FloatBlock ---  
    /**
	 * A block stores in maximum blockSize number of elements.
	 * The first block in a FloatBigList will grow until reaches this limit, all other blocks are directly
	 * allocated with a capacity of blockSize.
	 * A block maintains a reference count which allows a block to be shared among different FloatBigList
	 * instances with a copy-on-write approach.
	 */
    
    public static class FloatBlock extends FloatGapList {

        private int refCount = 1;

        public FloatBlock(){
}

        public FloatBlock(int capacity){
    super(capacity);
}

        public FloatBlock(FloatBlock that){
    super(that.capacity());
    addAll(that);
}

        /**
		 * @return true if block is shared by several FloatBigList instances
		 */
public boolean isShared() {
    return refCount > 1;
}

        /**
		 * Increment reference count as block is used by one FloatBigList instance more.
		 */
public FloatBlock ref() {
    refCount++;
    return this;
}

        /**
		 * Decrement reference count as block is no longer used by one FloatBigList instance.
		 */
public void unref() {
    refCount--;
}
    }

    // --- FloatBlockNode ---  
    /**
     * Implements an AVLNode storing a FloatBlock.
     * The nodes don't know the index of the object they are holding. They do know however their
     * position relative to their parent node. This allows to calculate the index of a node while traversing the tree.
     * There is a faedelung flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    static class FloatBlockNode {

        /** Pointer to parent node (null for root) */
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

        /** Relative position of node relative to its parent, root holds absolute position. */
        int relPos;

        /** The stored block */
        FloatBlock block;

        /**
         * Constructs a new node.
         *
         * @param parent			parent node (null for root)
         * @param relativePosition  the relative position of the node (absolute position for root)
         * @param block				the block to store
         * @param rightFollower 	the node following this one
         * @param leftFollower 		the node leading this one
         */
private FloatBlockNode(FloatBlockNode parent, int relPos, FloatBlock block, FloatBlockNode rightFollower, FloatBlockNode leftFollower){
    this.parent = parent;
    this.relPos = relPos;
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
private FloatBlock getFloatBlock() {
    return block;
}

        /**
         * Sets block to store by this node.
         *
         * @param block  the block to store
         */
private void setFloatBlock(FloatBlock block) {
    this.block = block;
}

        /**
         * Gets the next node in the list after this one.
         *
         * @return the next node
         */
private FloatBlockNode next() {
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
private FloatBlockNode previous() {
    if (leftIsPrevious || left == null) {
        return left;
    }
    return left.max();
}

        /**
         * Inserts new node holding specified block at the position index.
         *
         * @param index 	index of the position relative to the position of the parent node
         * @param obj 		object to store in the position
         * @return			this node or node replacing this node in the tree (if tree must be rebalanced)
         */
private FloatBlockNode insert(int index, FloatBlock obj) {
    assert (relPos != 0);
    int relIndex = index - relPos;
    if (relIndex < 0) {
        return insertOnLeft(relIndex, obj);
    } else {
        return insertOnRight(relIndex, obj);
    }
}

        /**
         * Inserts new node holding specified block on the node's left side.
         *
         * @param index 	index of the position relative to the position of the parent node
         * @param obj 		object to store in the position
         * @return			this node or node replacing this node in the tree (if tree must be rebalanced)
         */
private FloatBlockNode insertOnLeft(int relIndex, FloatBlock obj) {
    if (getLeftSubTree() == null) {
        int pos;
        if (relPos >= 0) {
            pos = -relPos;
        } else {
            pos = -block.size();
        }
        setLeft(new FloatBlockNode(this, pos, obj, this, left), null);
    } else {
        setLeft(left.insert(relIndex, obj), null);
    }
    if (relPos >= 0) {
        relPos += obj.size();
    }
    FloatBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        /**
         * Inserts new node holding specified block on the node's right side.
         *
         * @param index 	index of the position relative to the position of the parent node
         * @param obj 		object to store in the position
         * @return			this node or node replacing this node in the tree (if tree must be rebalanced)
         */
private FloatBlockNode insertOnRight(int relIndex, FloatBlock obj) {
    if (getRightSubTree() == null) {
        setRight(new FloatBlockNode(this, obj.size(), obj, right, this), null);
    } else {
        setRight(right.insert(relIndex, obj), null);
    }
    if (relPos < 0) {
        relPos -= obj.size();
    }
    FloatBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        /**
         * Gets the left node, returning null if its a faedelung.
         */
private FloatBlockNode getLeftSubTree() {
    return leftIsPrevious ? null : left;
}

        /**
         * Gets the right node, returning null if its a faedelung.
         */
private FloatBlockNode getRightSubTree() {
    return rightIsNext ? null : right;
}

        /**
         * Gets the rightmost child of this node.
         *
         * @return the rightmost child (greatest index)
         */
private FloatBlockNode max() {
    return getRightSubTree() == null ? this : right.max();
}

        /**
         * Gets the leftmost child of this node.
         *
         * @return the leftmost child (smallest index)
         */
private FloatBlockNode min() {
    return getLeftSubTree() == null ? this : left.min();
}

        private FloatBlockNode removeMax() {
    if (getRightSubTree() == null) {
        return removeSelf();
    }
    setRight(right.removeMax(), right.right);
    recalcHeight();
    return balance();
}

        private FloatBlockNode removeMin(int size) {
    if (getLeftSubTree() == null) {
        return removeSelf();
    }
    setLeft(left.removeMin(size), left.left);
    if (relPos > 0) {
        relPos -= size;
    }
    recalcHeight();
    return balance();
}

        /**
         * Removes this node from the tree.
         *
         * @return the node that replaces this one in the parent (can be null)
         */
private FloatBlockNode removeSelf() {
    FloatBlockNode p = parent;
    FloatBlockNode n = doRemoveSelf();
    if (n != null) {
        assert (p != n);
        n.parent = p;
    }
    return n;
}

        private FloatBlockNode doRemoveSelf() {
    if (getRightSubTree() == null && getLeftSubTree() == null) {
        return null;
    }
    if (getRightSubTree() == null) {
        if (relPos > 0) {
            left.relPos += relPos + (relPos > 0 ? 0 : 1);
        } else {
            left.relPos += relPos;
        }
        left.max().setRight(null, right);
        return left;
    }
    if (getLeftSubTree() == null) {
        if (relPos < 0) {
            right.relPos += relPos - (relPos < 0 ? 0 : 1);
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
        relPos += bs;
        left.relPos -= bs;
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
            if (left.relPos == 0) {
                left.relPos = -1;
            }
        }
    }
    recalcHeight();
    return this;
}

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
private int getOffset(FloatBlockNode node) {
    if (node == null) {
        return 0;
    }
    return node.relPos;
}

        /**
         * Sets the relative position.
         */
private int setOffset(FloatBlockNode node, int newOffest) {
    if (node == null) {
        return 0;
    }
    final int oldOffset = getOffset(node);
    node.relPos = newOffest;
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

        /**
         * Rotate tree to the left using this node as center.
         *
         * @return node which will take the place of this node
         */
private FloatBlockNode rotateLeft() {
    assert (!rightIsNext);
    final FloatBlockNode newTop = right;
    // can't be faedelung!   
    final FloatBlockNode movedNode = getRightSubTree().getLeftSubTree();
    final int newTopPosition = relPos + getOffset(newTop);
    final int myNewPosition = -newTop.relPos;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    FloatBlockNode p = this.parent;
    setRight(movedNode, newTop);
    newTop.setLeft(this, null);
    newTop.parent = p;
    this.parent = newTop;
    setOffset(newTop, newTopPosition);
    setOffset(this, myNewPosition);
    setOffset(movedNode, movedPosition);
    assert (newTop.getLeftSubTree() == null || newTop.getLeftSubTree().relPos < 0);
    assert (newTop.getRightSubTree() == null || newTop.getRightSubTree().relPos > 0);
    return newTop;
}

        /**
         * Rotate tree to the right using this node as center.
         *
         * @return node which will take the place of this node
         */
private FloatBlockNode rotateRight() {
    assert (!leftIsPrevious);
    final FloatBlockNode newTop = left;
    // can't be faedelung   
    final FloatBlockNode movedNode = getLeftSubTree().getRightSubTree();
    final int newTopPosition = relPos + getOffset(newTop);
    final int myNewPosition = -newTop.relPos;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    FloatBlockNode p = this.parent;
    setLeft(movedNode, newTop);
    newTop.setRight(this, null);
    newTop.parent = p;
    this.parent = newTop;
    setOffset(newTop, newTopPosition);
    setOffset(this, myNewPosition);
    setOffset(movedNode, movedPosition);
    assert (newTop.getLeftSubTree() == null || newTop.getLeftSubTree().relPos < 0);
    assert (newTop.getRightSubTree() == null || newTop.getRightSubTree().relPos > 0);
    return newTop;
}

        /**
         * Sets the left field to the node, or the previous node if that is null
         *
         * @param node  the new left subtree node
         * @param previous  the previous node in the linked list
         */
private void setLeft(FloatBlockNode node, FloatBlockNode previous) {
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
private void setRight(FloatBlockNode node, FloatBlockNode next) {
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
    return new StringBuilder().append("FloatBlockNode(").append(relPos).append(',').append(getRightSubTree() != null).append(',').append(block).append(',').append(getRightSubTree() != null).append(", height ").append(height).append(" )").toString();
}
    }

    // --- ImmutableFloatBigList ---  
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
}
