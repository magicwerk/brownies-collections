/*
 * Copyright 2014 by Thomas Mauch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: ShortBigList.java 2964 2015-10-18 22:43:57Z origo $
 */
package org.magicwerk.brownies.collections.primitive;
import org.magicwerk.brownies.collections.helper.ArraysHelper;
import org.magicwerk.brownies.collections.helper.primitive.ShortBinarySearch;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.BigList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.magicwerk.brownies.collections.helper.primitive.ShortMergeSort;

/**
 * ShortBigList is a list optimized for storing large number of elements.
 * It stores the elements in fixed size blocks and the blocks itself are maintained in a tree for fast access.
 * It also offers specialized methods for bulk processing of elements.
 * Also copying a ShortBigList is efficiently possible as its implemented using a copy-on-write approach.<p>
 *
 * <strong>Note that this implementation is not synchronized.</strong>
 * Due to data caching used for exploiting locality of reference, performance can decrease if ShortBigList is
 * accessed by several threads at different positions.<p>
 *
 * Note that the iterators provided are not fail-fast.<p>
 *
 * @author Thomas Mauch
 * @version $Id: ShortBigList.java 2964 2015-10-18 22:43:57Z origo $
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
    // public static ShortBigList EMPTY = ShortBigList.create().unmodifiableList();  
    // Syntax error:  
    // public static  ShortBigList EMPTY = ShortBigList.create().unmodifiableList();  
    /** Unmodifiable empty instance */
    
    private static final ShortBigList EMPTY = ShortBigList.create().unmodifiableList();

    /**
     * @return unmodifiable empty instance
     */

public static  ShortBigList EMPTY() {
    return EMPTY;
}

    /** Number of elements stored at maximum in a block */
    private int blockSize;

    /** Number of elements stored in this ShortBigList */
    private int size;

    /** The root node in the tree */
    private ShortBlockNode rootNode;

    /** Current node */
    private ShortBlockNode currNode;

    /** ShortBlock of current node */
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
        this.currShortBlockStart = that.currShortBlockStart;
        this.currShortBlockEnd = that.currShortBlockEnd;
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
    return new ShortBigList((coll != null) ? coll : Collections.emptyList());
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
    if (elems != null) {
        for (short elem : elems) {
            list.add(elem);
        }
    }
    return list;
}

    /**
	 * Default constructor.
	 * The default block size is used.
	 */
public ShortBigList(){
    this(DEFAULT_BLOCK_SIZE);
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

    /**
     * Create new list with specified elements.
     *
     * @param coll      collection with element
     */

public ShortBigList(Collection<Short> coll){
    if (coll instanceof ShortBigList) {
        doAssign((ShortBigList) coll);
        doClone((ShortBigList) coll);
    } else {
        blockSize = DEFAULT_BLOCK_SIZE;
        addShortBlock(0, new ShortBlock());
        for (Object obj : coll.toArray()) {
            add((Short) obj);
        }
        assert (size() == coll.size());
    }
}

    /**
     * Returns block size used for this ShortBigList.
     *
     * @return block size used for this ShortBigList
     */
public int blockSize() {
    return blockSize;
}

    /**
	 * Internal constructor.
	 *
	 * @param blockSize			default block size
	 * @param firstShortBlockSize	block size of first block
	 */
private ShortBigList(int blockSize, int firstShortBlockSize){
    doInit(blockSize, firstShortBlockSize);
}

    /**
	 * Initialize ShortBigList.
	 *
	 * @param blockSize			default block size
	 * @param firstShortBlockSize	block size of first block
	 */
private void doInit(int blockSize, int firstShortBlockSize) {
    this.blockSize = blockSize;
    // First block will grow until it reaches blockSize   
    ShortBlock block;
    if (firstShortBlockSize <= 1) {
        block = new ShortBlock();
    } else {
        block = new ShortBlock(firstShortBlockSize);
    }
    addShortBlock(0, block);
}

    /**
     * Returns a copy of this <tt>ShortBigList</tt> instance.
     * The copy is realized by a copy-on-write approach so also really large lists can efficiently be copied.
     * This method is identical to clone() except that the result is casted to ShortBigList.
     *
     * @return a copy of this <tt>ShortBigList</tt> instance
	 */
@Override
public ShortBigList copy() {
    return (ShortBigList) super.copy();
}

    /**
     * Returns a shallow copy of this <tt>ShortBigList</tt> instance
     * The copy is realized by a copy-on-write approach so also really large lists can efficiently be copied.
     *
     * @return a copy of this <tt>ShortBigList</tt> instance
     */
// Only overridden to change Javadoc  
@Override
public Object clone() {
    return super.clone();
}

    @Override
protected void doAssign(IShortList that) {
    ShortBigList list = (ShortBigList) that;
    this.blockSize = list.blockSize;
    this.currShortBlockEnd = list.currShortBlockEnd;
    this.currShortBlockStart = list.currShortBlockStart;
    this.currNode = list.currNode;
    this.rootNode = list.rootNode;
    this.size = list.size;
}

    @Override
protected void doClone(IShortList that) {
    ShortBigList bigList = (ShortBigList) that;
    bigList.releaseShortBlock();
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
    // This list will be garbage collected, so unref all referenced blocks.   
    // As it is not reachable by any live objects, if is safe to access it from   
    // the GC thread without synchronization   
    ShortBlockNode node = rootNode.min();
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
	 * As ShortBigList grows and shrinks automatically, the term capacity does not really make sense.
	 * Therefore always -1 is returned.
	 */
@Override
public int capacity() {
    return -1;
}

    @Override
protected short doGet(int index) {
    int pos = getShortBlockIndex(index, false, 0);
    return currNode.block.doGet(pos);
}

    @Override
protected short doSet(int index, short elem) {
    int pos = getShortBlockIndex(index, true, 0);
    short oldElem = currNode.block.doGet(pos);
    currNode.block.doSet(pos, elem);
    return oldElem;
}

    @Override
protected short doReSet(int index, short elem) {
    int pos = getShortBlockIndex(index, true, 0);
    short oldElem = currNode.block.doGet(pos);
    currNode.block.doSet(pos, elem);
    return oldElem;
}

    /**
	 * Release current block and apply modification if pending.
	 */
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
	 * @param index		list index (0 <= index <= size())
	 * @param write		true if the block is needed for a write operation (set, add, remove)
	 * @param modify	modify instruction (N>0: N elements are added, N<0: N elements are removed, 0 no change)
	 * @return			relative index within block
	 */
private int getShortBlockIndex(int index, boolean write, int modify) {
    // Determine block where specified index is located and store it in currShortBlock   
    if (currNode != null) {
        if (index >= currShortBlockStart && (index < currShortBlockEnd || index == currShortBlockEnd && size == index)) {
            // currShortBlock is already set correctly   
            if (write) {
                if (currNode.block.isShared()) {
                    currNode.block.unref();
                    currNode.setShortBlock(new ShortBlock(currNode.block));
                }
            }
            currModify += modify;
            return index - currShortBlockStart;
        }
        releaseShortBlock();
    }
    if (index == size) {
        if (currNode == null || currShortBlockEnd != size) {
            currNode = rootNode.max();
            currShortBlockEnd = size;
            currShortBlockStart = size - currNode.block.size();
        }
        if (modify != 0) {
            currNode.relPos += modify;
            ShortBlockNode leftNode = currNode.getLeftSubTree();
            if (leftNode != null) {
                leftNode.relPos -= modify;
            }
        }
    } else if (index == 0) {
        if (currNode == null || currShortBlockStart != 0) {
            currNode = rootNode.min();
            currShortBlockEnd = currNode.block.size();
            currShortBlockStart = 0;
        }
        if (modify != 0) {
            rootNode.relPos += modify;
        }
    }
    if (currNode == null) {
        doGetShortBlock(index, modify);
    }
    assert (index >= currShortBlockStart && index <= currShortBlockEnd);
    if (write) {
        if (currNode.block.isShared()) {
            currNode.block.unref();
            currNode.setShortBlock(new ShortBlock(currNode.block));
        }
    }
    return index - currShortBlockStart;
}

    /**
	 * @return true if there is only the root block, false otherwise
	 */
private boolean isOnlyRootShortBlock() {
    return rootNode.left == null && rootNode.right == null;
}

    /**
     * Determine node/block for the specified index.
     * The fields currNode, currShortBlockStart, and currShortBlockEnd are set.
     * During the traversing the tree node, the nodes relative positions are changed according to the modify instruction.
     *
     * @param index		list index for which block must be determined
     * @param modify	modify instruction (N>0: N elements are added, N<0: N elements are removed, 0 no change)
     */
private void doGetShortBlock(int index, int modify) {
    currNode = rootNode;
    currShortBlockEnd = rootNode.relPos;
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
            int leftIndex = currShortBlockEnd - currNode.block.size();
            assert (leftIndex >= 0);
            if (index >= leftIndex && index < currShortBlockEnd) {
                // Correct node has been found   
                if (modify != 0) {
                    ShortBlockNode leftNode = currNode.getLeftSubTree();
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
            ShortBlockNode nextNode;
            if (index < currShortBlockEnd) {
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
                            ShortBlockNode left = currNode.getLeftSubTree();
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
            currShortBlockEnd += nextNode.relPos;
            currNode = nextNode;
        }
    }
    currShortBlockStart = currShortBlockEnd - currNode.block.size();
}

    /**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */
private void addShortBlock(int index, ShortBlock obj) {
    if (rootNode == null) {
        rootNode = new ShortBlockNode(null, index, obj, null, null);
    } else {
        rootNode = rootNode.insert(index, obj);
        rootNode.parent = null;
    }
}

    @Override
protected boolean doAdd(int index, short element) {
    if (index == -1) {
        index = size;
    }
    // Insert   
    int pos = getShortBlockIndex(index, true, 1);
    // If there is still place in the current block: insert in current block   
    int maxSize = (index == size || index == 0) ? (int) (blockSize * FILL_THRESHOLD) : blockSize;
    // The second part of the condition is a work around to handle the case of insertion as position 0 correctly   
    // where blockSize() is 2 (the new block would then be added after the current one)   
    if (currNode.block.size() < maxSize || (currNode.block.size() == 1 && currNode.block.size() < blockSize)) {
        currNode.block.doAdd(pos, element);
        currShortBlockEnd++;
    } else {
        // No place any more in current block   
        ShortBlock newShortBlock = new ShortBlock(blockSize);
        if (index == size) {
            // Insert new block at tail   
            newShortBlock.doAdd(0, element);
            // Subtract 1 because getShortBlockIndex() has already added 1   
            modify(currNode, -1);
            addShortBlock(size + 1, newShortBlock);
            ShortBlockNode lastNode = currNode.next();
            currNode = lastNode;
            currShortBlockStart = currShortBlockEnd;
            currShortBlockEnd++;
        } else if (index == 0) {
            // Insert new block at head   
            newShortBlock.doAdd(0, element);
            // Subtract 1 because getShortBlockIndex() has already added 1   
            modify(currNode, -1);
            addShortBlock(1, newShortBlock);
            ShortBlockNode firstNode = currNode.previous();
            currNode = firstNode;
            currShortBlockStart = 0;
            currShortBlockEnd = 1;
        } else {
            // Split block for insert   
            int nextShortBlockLen = blockSize / 2;
            int blockLen = blockSize - nextShortBlockLen;
            ShortGapList.transferRemove(currNode.block, blockLen, nextShortBlockLen, newShortBlock, 0, 0);
            // Subtract 1 more because getShortBlockIndex() has already added 1   
            modify(currNode, -nextShortBlockLen - 1);
            addShortBlock(currShortBlockEnd - nextShortBlockLen, newShortBlock);
            if (pos < blockLen) {
                // Insert element in first block   
                currNode.block.doAdd(pos, element);
                currShortBlockEnd = currShortBlockStart + blockLen + 1;
                modify(currNode, 1);
            } else {
                // Insert element in second block   
                currNode = currNode.next();
                modify(currNode, 1);
                currNode.block.doAdd(pos - blockLen, element);
                currShortBlockStart += blockLen;
                currShortBlockEnd++;
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
    if (node.relPos < 0) {
        // Left node   
        ShortBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relPos -= modify;
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
        ShortBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relPos -= modify;
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
    rootNode = newNode;
    return n;
}

    @Override
protected boolean doAddAll(int index, IShortList list) {
    if (list.size() == 0) {
        return false;
    }
    if (index == -1) {
        index = size;
    }
    if (CHECK)
        check();
    int oldSize = size;
    if (list.size() == 1) {
        return doAdd(index, list.get(0));
    }
    int addPos = getShortBlockIndex(index, true, 0);
    ShortBlock addShortBlock = currNode.block;
    int space = blockSize - addShortBlock.size();
    int addLen = list.size();
    if (addLen <= space) {
        // All elements can be added to current block   
        currNode.block.addAll(addPos, list);
        modify(currNode, addLen);
        size += addLen;
        currShortBlockEnd += addLen;
    } else {
        if (index == size) {
            // Add elements at end   
            for (int i = 0; i < space; i++) {
                currNode.block.add(addPos + i, list.get(i));
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                ShortBlock nextShortBlock = new ShortBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextShortBlock.add(i, list.get(done + i));
                }
                done += add;
                todo -= add;
                addShortBlock(size + done, nextShortBlock);
                currNode = currNode.next();
            }
            size += addLen;
            currShortBlockEnd = size;
            currShortBlockStart = currShortBlockEnd - currNode.block.size();
        } else if (index == 0) {
            // Add elements at head   
            assert (addPos == 0);
            for (int i = 0; i < space; i++) {
                currNode.block.add(addPos + i, list.get(addLen - space + i));
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                ShortBlock nextShortBlock = new ShortBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextShortBlock.add(i, list.get(addLen - done - add + i));
                }
                done += add;
                todo -= add;
                addShortBlock(0, nextShortBlock);
                currNode = currNode.previous();
            }
            size += addLen;
            currShortBlockStart = 0;
            currShortBlockEnd = currNode.block.size();
        } else {
            // Add elements in the middle   
            // Split first block to remove tail elements if necessary   
            ShortGapList list2 = ShortGapList.create();
            // TODO avoid unnecessary copy   
            list2.addAll(list);
            int remove = currNode.block.size() - addPos;
            if (remove > 0) {
                list2.addAll(currNode.block.getAll(addPos, remove));
                currNode.block.remove(addPos, remove);
                modify(currNode, -remove);
                size -= remove;
                currShortBlockEnd -= remove;
            }
            // Calculate how many blocks we need for the elements   
            int numElems = currNode.block.size() + list2.size();
            int numShortBlocks = (numElems - 1) / blockSize + 1;
            assert (numShortBlocks > 1);
            int has = currNode.block.size();
            int should = numElems / numShortBlocks;
            int listPos = 0;
            if (has < should) {
                // Elements must be added to first block   
                int add = should - has;
                IShortList sublist = list2.getAll(0, add);
                listPos += add;
                currNode.block.addAll(addPos, sublist);
                modify(currNode, add);
                assert (currNode.block.size() == should);
                numElems -= should;
                numShortBlocks--;
                size += add;
                currShortBlockEnd += add;
            } else if (has > should) {
                // Elements must be moved from first to second block   
                ShortBlock nextShortBlock = new ShortBlock(blockSize);
                int move = has - should;
                nextShortBlock.addAll(currNode.block.getAll(currNode.block.size() - move, move));
                currNode.block.remove(currNode.block.size() - move, move);
                modify(currNode, -move);
                assert (currNode.block.size() == should);
                numElems -= should;
                numShortBlocks--;
                currShortBlockEnd -= move;
                should = numElems / numShortBlocks;
                int add = should - move;
                assert (add >= 0);
                IShortList sublist = list2.getAll(0, add);
                nextShortBlock.addAll(move, sublist);
                listPos += add;
                assert (nextShortBlock.size() == should);
                numElems -= should;
                numShortBlocks--;
                size += add;
                addShortBlock(currShortBlockEnd, nextShortBlock);
                currNode = currNode.next();
                assert (currNode.block == nextShortBlock);
                assert (currNode.block.size() == add + move);
                currShortBlockStart = currShortBlockEnd;
                currShortBlockEnd += add + move;
            } else {
                // ShortBlock already has the correct size   
                numElems -= should;
                numShortBlocks--;
            }
            if (CHECK)
                check();
            while (numShortBlocks > 0) {
                int add = numElems / numShortBlocks;
                assert (add > 0);
                IShortList sublist = list2.getAll(listPos, add);
                listPos += add;
                ShortBlock nextShortBlock = new ShortBlock();
                nextShortBlock.addAll(sublist);
                assert (nextShortBlock.size() == add);
                numElems -= add;
                addShortBlock(currShortBlockEnd, nextShortBlock);
                currNode = currNode.next();
                assert (currNode.block == nextShortBlock);
                assert (currNode.block.size() == add);
                currShortBlockStart = currShortBlockEnd;
                currShortBlockEnd += add;
                size += add;
                numShortBlocks--;
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
    finalize();
    rootNode = null;
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
    int startPos = getShortBlockIndex(index, true, 0);
    ShortBlockNode startNode = currNode;
     int endPos = getShortBlockIndex(index + len - 1, true, 0);
    ShortBlockNode endNode = currNode;
    if (startNode == endNode) {
        // Delete from single block   
        getShortBlockIndex(index, true, -len);
        currNode.block.remove(startPos, len);
        if (currNode.block.isEmpty()) {
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
        if (CHECK)
            check();
        int startLen = startNode.block.size() - startPos;
        getShortBlockIndex(index, true, -startLen);
        startNode.block.remove(startPos, startLen);
        assert (startNode == currNode);
        if (currNode.block.isEmpty()) {
            releaseShortBlock();
            doRemove(startNode);
            startNode = null;
        }
        len -= startLen;
        size -= startLen;
        while (len > 0) {
            currNode = null;
            getShortBlockIndex(index, true, 0);
            int s = currNode.block.size();
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
                if (CHECK)
                    check();
            } else {
                modify(currNode, -len);
                currNode.block.remove(0, len);
                size -= len;
                break;
            }
        }
        releaseShortBlock();
        if (CHECK)
            check();
        getShortBlockIndex(index, false, 0);
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
private void merge(ShortBlockNode node) {
    if (node == null) {
        return;
    }
    final int minShortBlockSize = Math.max((int) (blockSize * MERGE_THRESHOLD), 1);
    if (node.block.size() >= minShortBlockSize) {
        return;
    }
    ShortBlockNode oldCurrNode = node;
    ShortBlockNode leftNode = node.previous();
    if (leftNode != null && leftNode.block.size() < minShortBlockSize) {
        // Merge with left block   
        int len = node.block.size();
        int dstSize = leftNode.getShortBlock().size();
        for (int i = 0; i < len; i++) {
            leftNode.block.add((short) 0);
        }
        ShortGapList.transferCopy(node.block, 0, len, leftNode.block, dstSize, len);
        assert (leftNode.block.size() <= blockSize);
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
                rightNode.block.add(0, (short) 0);
            }
            ShortGapList.transferCopy(node.block, 0, len, rightNode.block, 0, len);
            assert (rightNode.block.size() <= blockSize);
            modify(rightNode, +len);
            modify(oldCurrNode, -len);
            releaseShortBlock();
            doRemove(oldCurrNode);
        }
    }
}

    protected short doRemove(int index) {
    int pos = getShortBlockIndex(index, true, -1);
    short oldElem = currNode.block.doRemove(pos);
    currShortBlockEnd--;
    final int minShortBlockSize = Math.max(blockSize / 3, 1);
    if (currNode.block.size() < minShortBlockSize) {
        if (currNode.block.size() == 0) {
            if (!isOnlyRootShortBlock()) {
                ShortBlockNode oldCurrNode = currNode;
                releaseShortBlock();
                doRemove(oldCurrNode);
            }
        } else if (index != 0 && index != size - 1) {
            // Do not merge if remove happens at head or tail.   
            // Reason: if removing continues, we can remove the whole block without merging   
            merge(currNode);
        }
    }
    size--;
    if (CHECK)
        check();
    return oldElem;
}

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
    if (isOnlyRootShortBlock()) {
        rootNode.block.trimToSize();
    } else {
        ShortBigList newList = new ShortBigList(blockSize);
        ShortBlockNode node = rootNode.min();
        while (node != null) {
            newList.addAll(node.block);
            remove(0, node.block.size());
            node = node.next();
        }
        doAssign(newList);
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
        rootNode.block.sort(index, len);
    } else {
        ShortMergeSort.sort(this, index, index + len);
    }
}

    
@Override
public int binarySearch(int index, int len, short key) {
    checkRange(index, len);
    if (isOnlyRootShortBlock()) {
        return rootNode.block.binarySearch(key);
    } else {
        return ShortBinarySearch.binarySearch(this, key, 0, size());
    }
}

    // --- Serialization ---  
/**
     * Serialize a ShortBigList object.
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

    // --- Debug checks ---  
private void checkNode(ShortBlockNode node) {
    assert ((node.block.size() > 0 || node == rootNode) && node.block.size() <= blockSize);
    ShortBlockNode child = node.getLeftSubTree();
    assert (child == null || child.parent == node);
    child = node.getRightSubTree();
    assert (child == null || child.parent == node);
}

    private void checkHeight(ShortBlockNode node) {
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

    private void check() {
    if (currNode != null) {
        assert (currShortBlockStart >= 0 && currShortBlockEnd <= size && currShortBlockStart <= currShortBlockEnd);
        assert (currShortBlockStart + currNode.block.size() == currShortBlockEnd);
    }
    if (rootNode == null) {
        assert (size == 0);
        return;
    }
    checkHeight(rootNode);
    ShortBlockNode oldCurrNode = currNode;
    int oldCurrModify = currModify;
    if (currModify != 0) {
        currNode = null;
        currModify = 0;
        modify(oldCurrNode, oldCurrModify);
    }
    ShortBlockNode node = rootNode;
    checkNode(node);
    int index = node.relPos;
    while (node.left != null) {
        node = node.left;
        checkNode(node);
        assert (node.relPos < 0);
        index += node.relPos;
    }
    ShortBlock block = node.getShortBlock();
    assert (block.size() == index);
    int lastIndex = index;
    while (lastIndex < size()) {
        node = rootNode;
        index = node.relPos;
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
            index += node.relPos;
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

    // --- ShortBlock ---  
    /**
	 * A block stores in maximum blockSize number of elements.
	 * The first block in a ShortBigList will grow until reaches this limit, all other blocks are directly
	 * allocated with a capacity of blockSize.
	 * A block maintains a reference count which allows a block to be shared among different ShortBigList
	 * instances with a copy-on-write approach.
	 */
    
    static class ShortBlock extends ShortGapList {

        private AtomicInteger refCount = new AtomicInteger(1);

        public ShortBlock(){
}

        public ShortBlock(int capacity){
    super(capacity);
}

        public ShortBlock(ShortBlock that){
    super(that.capacity());
    addAll(that);
}

        /**
		 * @return true if block is shared by several ShortBigList instances
		 */
public boolean isShared() {
    return refCount.get() > 1;
}

        /**
		 * Increment reference count as block is used by one ShortBigList instance more.
		 */
public ShortBlock ref() {
    refCount.incrementAndGet();
    return this;
}

        /**
		 * Decrement reference count as block is no longer used by one ShortBigList instance.
		 */
public void unref() {
    refCount.decrementAndGet();
}
    }

    // --- ShortBlockNode ---  
    /**
     * Implements an AVLNode storing a ShortBlock.
     * The nodes don't know the index of the object they are holding. They do know however their
     * position relative to their parent node. This allows to calculate the index of a node while traversing the tree.
     * There is a faedelung flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    static class ShortBlockNode {

        /** Pointer to parent node (null for root) */
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

        /** Relative position of node relative to its parent, root holds absolute position. */
        int relPos;

        /** The stored block */
        ShortBlock block;

        /**
         * Constructs a new node.
         *
         * @param parent			parent node (null for root)
         * @param relativePosition  the relative position of the node (absolute position for root)
         * @param block				the block to store
         * @param rightFollower 	the node following this one
         * @param leftFollower 		the node leading this one
         */
private ShortBlockNode(ShortBlockNode parent, int relPos, ShortBlock block, ShortBlockNode rightFollower, ShortBlockNode leftFollower){
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
private ShortBlock getShortBlock() {
    return block;
}

        /**
         * Sets block to store by this node.
         *
         * @param block  the block to store
         */
private void setShortBlock(ShortBlock block) {
    this.block = block;
}

        /**
         * Gets the next node in the list after this one.
         *
         * @return the next node
         */
private ShortBlockNode next() {
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
private ShortBlockNode previous() {
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
private ShortBlockNode insert(int index, ShortBlock obj) {
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
private ShortBlockNode insertOnLeft(int relIndex, ShortBlock obj) {
    if (getLeftSubTree() == null) {
        int pos;
        if (relPos >= 0) {
            pos = -relPos;
        } else {
            pos = -block.size();
        }
        setLeft(new ShortBlockNode(this, pos, obj, this, left), null);
    } else {
        setLeft(left.insert(relIndex, obj), null);
    }
    if (relPos >= 0) {
        relPos += obj.size();
    }
    ShortBlockNode ret = balance();
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
private ShortBlockNode insertOnRight(int relIndex, ShortBlock obj) {
    if (getRightSubTree() == null) {
        setRight(new ShortBlockNode(this, obj.size(), obj, right, this), null);
    } else {
        setRight(right.insert(relIndex, obj), null);
    }
    if (relPos < 0) {
        relPos -= obj.size();
    }
    ShortBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        /**
         * Gets the left node, returning null if its a faedelung.
         */
private ShortBlockNode getLeftSubTree() {
    return leftIsPrevious ? null : left;
}

        /**
         * Gets the right node, returning null if its a faedelung.
         */
private ShortBlockNode getRightSubTree() {
    return rightIsNext ? null : right;
}

        /**
         * Gets the rightmost child of this node.
         *
         * @return the rightmost child (greatest index)
         */
private ShortBlockNode max() {
    return getRightSubTree() == null ? this : right.max();
}

        /**
         * Gets the leftmost child of this node.
         *
         * @return the leftmost child (smallest index)
         */
private ShortBlockNode min() {
    return getLeftSubTree() == null ? this : left.min();
}

        private ShortBlockNode removeMax() {
    if (getRightSubTree() == null) {
        return removeSelf();
    }
    setRight(right.removeMax(), right.right);
    recalcHeight();
    return balance();
}

        private ShortBlockNode removeMin(int size) {
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
private ShortBlockNode removeSelf() {
    ShortBlockNode p = parent;
    ShortBlockNode n = doRemoveSelf();
    if (n != null) {
        assert (p != n);
        n.parent = p;
    }
    return n;
}

        private ShortBlockNode doRemoveSelf() {
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
        final ShortBlockNode rightMin = right.min();
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
private int getOffset(ShortBlockNode node) {
    if (node == null) {
        return 0;
    }
    return node.relPos;
}

        /**
         * Sets the relative position.
         */
private int setOffset(ShortBlockNode node, int newOffest) {
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
private int getHeight(final ShortBlockNode node) {
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
private ShortBlockNode rotateLeft() {
    assert (!rightIsNext);
    final ShortBlockNode newTop = right;
    // can't be faedelung!   
    final ShortBlockNode movedNode = getRightSubTree().getLeftSubTree();
    final int newTopPosition = relPos + getOffset(newTop);
    final int myNewPosition = -newTop.relPos;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    ShortBlockNode p = this.parent;
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
private ShortBlockNode rotateRight() {
    assert (!leftIsPrevious);
    final ShortBlockNode newTop = left;
    // can't be faedelung   
    final ShortBlockNode movedNode = getLeftSubTree().getRightSubTree();
    final int newTopPosition = relPos + getOffset(newTop);
    final int myNewPosition = -newTop.relPos;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    ShortBlockNode p = this.parent;
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
private void setLeft(ShortBlockNode node, ShortBlockNode previous) {
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
private void setRight(ShortBlockNode node, ShortBlockNode next) {
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
    return new StringBuilder().append("ShortBlockNode(").append(relPos).append(',').append(getRightSubTree() != null).append(',').append(block).append(',').append(getRightSubTree() != null).append(", height ").append(height).append(" )").toString();
}
    }

    // --- ImmutableShortBigList ---  
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
}
