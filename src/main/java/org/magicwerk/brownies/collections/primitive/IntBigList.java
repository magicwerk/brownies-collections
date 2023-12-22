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
 * $Id$
 */
package org.magicwerk.brownies.collections.primitive;
import org.magicwerk.brownies.collections.helper.ArraysHelper;
import org.magicwerk.brownies.collections.helper.primitive.IntBinarySearch;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.BigList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import org.magicwerk.brownies.collections.helper.primitive.IntMergeSort;

/**
 * IntBigList is a list optimized for storing large number of elements.
 * It stores the elements in fixed size blocks and the blocks itself are maintained in a tree for fast access.
 * It also offers specialized methods for bulk processing of elements.
 * Also copying a IntBigList is efficiently possible as its implemented using a copy-on-write approach.<p>
 *
 * <strong>Note that this implementation is not synchronized.</strong>
 * Due to data caching used for exploiting locality of reference, performance can decrease if IntBigList is
 * accessed by several threads at different positions.<p>
 *
 * Note that the iterators provided are not fail-fast.<p>
 *
 * @author Thomas Mauch
 */
public class IntBigList extends IIntList {

    /**
     * UID for serialization
     */
    private static final long serialVersionUID = 3715838828540564836L;

    /**
     * Default block size
     */
    private static final int DEFAULT_BLOCK_SIZE = 1000;

    /**
     * If two adjacent blocks both less than MERGE_THRESHOLD*blockSize elements, they are merged
     */
    private static final float MERGE_THRESHOLD = 0.35f;

    /**
     * If an element is added to the list at the head or tail, the block is only filled until it
     * has FILL_THRESHOLD*blockSize elements (so there is room for insertion without need to split).
     */
    private static final float FILL_THRESHOLD = 0.95f;

    /**
     * Set to true for debugging during developing
     */
    private static final boolean CHECK = false;

    // -- EMPTY --
    // Cannot make a static reference to the non-static type E:
    // public static IntBigList EMPTY = IntBigList.create().unmodifiableList();
    // Syntax error:
    // public static  IntBigList EMPTY = IntBigList.create().unmodifiableList();
    /**
     * Unmodifiable empty instance
     */
    
    private static final IntBigList EMPTY = IntBigList.create().unmodifiableList();

    /**
 * @return unmodifiable empty instance
 */

public static  IntBigList EMPTY() {
    return EMPTY;
}

    /**
     * Number of elements stored at maximum in a block
     */
    private int blockSize;

    /**
     * Number of elements stored in this IntBigList
     */
    private int size;

    /**
     * The root node in the tree
     */
    private IntBlockNode rootNode;

    /**
     * Current node
     */
    private IntBlockNode currNode;

    /**
     * IntBlock of current node
     */
    /**
     * Start index of current block
     */
    private int currIntBlockStart;

    /**
     * End index of current block
     */
    private int currIntBlockEnd;

    /**
     * Modify value which must be applied before this block is not current any more
     */
    private int currModify;

    /**
 * Constructor used internally, e.g. for ImmutableIntBigList.
 *
 * @param copy true to copy all instance values from source,
 *             if false nothing is done
 * @param that list to copy
 */
protected IntBigList(boolean copy, IntBigList that) {
    if (copy) {
        this.blockSize = that.blockSize;
        this.currIntBlockStart = that.currIntBlockStart;
        this.currIntBlockEnd = that.currIntBlockEnd;
        this.currNode = that.currNode;
        this.rootNode = that.rootNode;
        this.size = that.size;
    }
}

    // This separate method is needed as the varargs variant creates the list with specific size
public static /**
 * Create new list.
 *
 * @return          created list
 * @param        type of elements stored in the list
 */
IntBigList create() {
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
    return new IntBigList((coll != null) ? coll : Collections.emptyList());
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
    if (elems != null) {
        for (int elem : elems) {
            list.add(elem);
        }
    }
    return list;
}

    /**
 * Default constructor.
 * The default block size is used.
 */
public IntBigList() {
    this(DEFAULT_BLOCK_SIZE);
}

    /**
 * Constructor.
 *
 * @param blockSize block size
 */
public IntBigList(int blockSize) {
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

public IntBigList(Collection<Integer> coll) {
    if (coll instanceof IntBigList) {
        doAssign((IntBigList) coll);
        doClone((IntBigList) coll);
    } else {
        blockSize = DEFAULT_BLOCK_SIZE;
        addIntBlock(0, new IntBlock());
        for (Object obj : coll.toArray()) {
            add((Integer) obj);
        }
        assert (size() == coll.size());
    }
}

    /**
 * Returns block size used for this IntBigList.
 *
 * @return block size used for this IntBigList
 */
public int blockSize() {
    return blockSize;
}

    /**
 * Internal constructor.
 *
 * @param blockSize			default block size
 * @param firstIntBlockSize	block size of first block
 */
private IntBigList(int blockSize, int firstIntBlockSize) {
    doInit(blockSize, firstIntBlockSize);
}

    /**
 * Initialize IntBigList.
 *
 * @param blockSize			default block size
 * @param firstIntBlockSize	block size of first block
 */
private void doInit(int blockSize, int firstIntBlockSize) {
    this.blockSize = blockSize;
    // First block will grow until it reaches blockSize
    IntBlock block;
    if (firstIntBlockSize <= 1) {
        block = new IntBlock();
    } else {
        block = new IntBlock(firstIntBlockSize);
    }
    addIntBlock(0, block);
}

    /**
 * Returns a shallow copy of this list.
 * The new list will contain the same elements as the source list, i.e. the elements themselves are not copied.
 * The copy is realized by a copy-on-write approach so also really large lists can efficiently be copied.
 * This returned list will be modifiable, i.e. an unmodifiable list will become modifiable again.
 * This method is identical to clone() except that it returns an object with the exact type.
 *
 * @return a modifiable copy of this list
 */
@Override

public IntBigList copy() {
    return (IntBigList) clone();
}

    @Override
public IntBigList crop() {
    return (IntBigList) super.crop();
}

    /**
 * Returns a shallow copy of this list.
 * The new list will contain the same elements as the source list, i.e. the elements themselves are not copied.
 * The copy is realized by a copy-on-write approach so also really large lists can efficiently be copied.
 * This returned list will be modifiable, i.e. an unmodifiable list will become modifiable again.
 * It is advised to use copy() which is identical except that it returns an object with the exact type.
 *
 * @return a modifiable copy of this list
 */
@Override
public Object clone() {
    if (this instanceof ImmutableIntBigList) {
        IntBigList list = new IntBigList(false, null);
        list.doClone(this);
        return list;
    } else {
        return super.clone();
    }
}

    @Override
protected void doAssign(IIntList that) {
    IntBigList list = (IntBigList) that;
    this.blockSize = list.blockSize;
    this.currIntBlockEnd = list.currIntBlockEnd;
    this.currIntBlockStart = list.currIntBlockStart;
    this.currNode = list.currNode;
    this.rootNode = list.rootNode;
    this.size = list.size;
}

    @Override
protected void doClone(IIntList that) {
    IntBigList bigList = (IntBigList) that;
    bigList.releaseIntBlock();
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
    // This list will be garbage collected, so unref all referenced blocks.
    // As it is not reachable by any live objects, if is safe to access it from the GC thread without synchronization
    IntBlockNode node = rootNode.min();
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
 * As IntBigList grows and shrinks automatically, the term capacity does not really make sense.
 * Therefore always -1 is returned.
 */
@Override
public int capacity() {
    return -1;
}

    @Override
protected int doGet(int index) {
    int pos = getIntBlockIndex(index, false, 0);
    return currNode.block.doGet(pos);
}

    @Override
protected int doSet(int index, int elem) {
    int pos = getIntBlockIndex(index, true, 0);
    int oldElem = currNode.block.doGet(pos);
    currNode.block.doSet(pos, elem);
    return oldElem;
}

    @Override
protected int doReSet(int index, int elem) {
    int pos = getIntBlockIndex(index, true, 0);
    int oldElem = currNode.block.doGet(pos);
    currNode.block.doSet(pos, elem);
    return oldElem;
}

    /**
 * Release current block and apply modification if pending.
 */
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
 * @param index		list index (0 <= index <= size())
 * @param write		true if the block is needed for a write operation (set, add, remove)
 * @param modify	modify instruction (N>0: N elements are added, N<0: N elements are removed, 0 no change)
 * @return			relative index within block
 */
private int getIntBlockIndex(int index, boolean write, int modify) {
    // Determine block where specified index is located and store it in currIntBlock
    if (currNode != null) {
        if (index >= currIntBlockStart && (index < currIntBlockEnd || index == currIntBlockEnd && size == index)) {
            // currIntBlock is already set correctly
            if (write) {
                if (currNode.block.isShared()) {
                    currNode.block.unref();
                    currNode.setIntBlock(new IntBlock(currNode.block));
                }
            }
            currModify += modify;
            return index - currIntBlockStart;
        }
        releaseIntBlock();
    }
    return getIntBlockIndex2(index, write, modify);
}

    private int getIntBlockIndex2(int index, boolean write, int modify) {
    if (index == size) {
        if (currNode == null || currIntBlockEnd != size) {
            currNode = rootNode.max();
            currIntBlockEnd = size;
            currIntBlockStart = size - currNode.block.size();
        }
        if (modify != 0) {
            currNode.relPos += modify;
            IntBlockNode leftNode = currNode.getLeftSubTree();
            if (leftNode != null) {
                leftNode.relPos -= modify;
            }
        }
    } else if (index == 0) {
        if (currNode == null || currIntBlockStart != 0) {
            currNode = rootNode.min();
            currIntBlockEnd = currNode.block.size();
            currIntBlockStart = 0;
        }
        if (modify != 0) {
            rootNode.relPos += modify;
        }
    }
    if (currNode == null) {
        doGetIntBlock(index, modify);
    }
    assert (index >= currIntBlockStart && index <= currIntBlockEnd);
    if (write) {
        if (currNode.block.isShared()) {
            currNode.block.unref();
            currNode.setIntBlock(new IntBlock(currNode.block));
        }
    }
    return index - currIntBlockStart;
}

    /**
 * @return true if there is only the root block, false otherwise
 */
private boolean isOnlyRootIntBlock() {
    return rootNode.left == null && rootNode.right == null;
}

    /**
 * Determine node/block for the specified index.
 * The fields currNode, currIntBlockStart, and currIntBlockEnd are set.
 * During the traversing the tree node, the nodes relative positions are changed according to the modify instruction.
 *
 * @param index		list index for which block must be determined
 * @param modify	modify instruction (N>0: N elements are added, N<0: N elements are removed, 0 no change)
 */
private void doGetIntBlock(int index, int modify) {
    currNode = rootNode;
    currIntBlockEnd = rootNode.relPos;
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
            int leftIndex = currIntBlockEnd - currNode.block.size();
            assert (leftIndex >= 0);
            if (index >= leftIndex && index < currIntBlockEnd) {
                // Correct node has been found
                if (modify != 0) {
                    IntBlockNode leftNode = currNode.getLeftSubTree();
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
            IntBlockNode nextNode;
            if (index < currIntBlockEnd) {
                // Traverse the left node
                nextNode = currNode.getLeftSubTree();
                wasLeft = doGetIntBlockLeft(modify, nextNode, wasLeft);
                if (nextNode == null) {
                    break;
                }
            } else {
                // Traverse the right node
                nextNode = currNode.getRightSubTree();
                wasLeft = doGetIntBlockRight(modify, nextNode, wasLeft);
                if (nextNode == null) {
                    break;
                }
            }
            currIntBlockEnd += nextNode.relPos;
            currNode = nextNode;
        }
    }
    currIntBlockStart = currIntBlockEnd - currNode.block.size();
}

    private boolean doGetIntBlockLeft(int modify, IntBlockNode nextNode, boolean wasLeft) {
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
    return wasLeft;
}

    private boolean doGetIntBlockRight(int modify, IntBlockNode nextNode, boolean wasLeft) {
    if (modify != 0) {
        if (nextNode == null || wasLeft) {
            if (currNode.relPos > 0) {
                currNode.relPos += modify;
                IntBlockNode left = currNode.getLeftSubTree();
                if (left != null) {
                    left.relPos -= modify;
                }
            } else {
                currNode.relPos -= modify;
            }
            wasLeft = false;
        }
    }
    return wasLeft;
}

    /**
 * Adds a new element to the list.
 *
 * @param index  the index to add before
 * @param obj  the element to add
 */
private void addIntBlock(int index, IntBlock obj) {
    if (rootNode == null) {
        rootNode = new IntBlockNode(null, index, obj, null, null);
    } else {
        rootNode = rootNode.insert(index, obj);
        rootNode.parent = null;
    }
}

    @Override
protected boolean doAdd(int index, int element) {
    if (index == -1) {
        index = size;
    }
    // Insert
    int pos = getIntBlockIndex(index, true, 1);
    // If there is still place in the current block: insert in current block
    int maxSize = (index == size || index == 0) ? (int) (blockSize * FILL_THRESHOLD) : blockSize;
    // The second part of the condition is a work around to handle the case of insertion as position 0 correctly
    // where blockSize() is 2 (the new block would then be added after the current one)
    if (currNode.block.size() < maxSize || (currNode.block.size() == 1 && currNode.block.size() < blockSize)) {
        currNode.block.doAdd(pos, element);
        currIntBlockEnd++;
    } else {
        // No place any more in current block
        IntBlock newIntBlock = new IntBlock(blockSize);
        if (index == size) {
            // Insert new block at tail
            newIntBlock.doAdd(0, element);
            // Subtract 1 because getIntBlockIndex() has already added 1
            modify(currNode, -1);
            addIntBlock(size + 1, newIntBlock);
            IntBlockNode lastNode = currNode.next();
            currNode = lastNode;
            currIntBlockStart = currIntBlockEnd;
            currIntBlockEnd++;
        } else if (index == 0) {
            // Insert new block at head
            newIntBlock.doAdd(0, element);
            // Subtract 1 because getIntBlockIndex() has already added 1
            modify(currNode, -1);
            addIntBlock(1, newIntBlock);
            IntBlockNode firstNode = currNode.previous();
            currNode = firstNode;
            currIntBlockStart = 0;
            currIntBlockEnd = 1;
        } else {
            // Split block for insert
            doAddSplitIntBlock(index, element, pos, newIntBlock);
        }
    }
    size++;
    if (CHECK)
        check();
    return true;
}

    private void doAddSplitIntBlock(int index, int element, int pos, IntBlock newIntBlock) {
    int nextIntBlockLen = blockSize / 2;
    int blockLen = blockSize - nextIntBlockLen;
    IntGapList.transferRemove(currNode.block, blockLen, nextIntBlockLen, newIntBlock, 0, 0);
    // Subtract 1 more because getIntBlockIndex() has already added 1
    modify(currNode, -nextIntBlockLen - 1);
    addIntBlock(currIntBlockEnd - nextIntBlockLen, newIntBlock);
    if (pos < blockLen) {
        // Insert element in first block
        currNode.block.doAdd(pos, element);
        currIntBlockEnd = currIntBlockStart + blockLen + 1;
        modify(currNode, 1);
    } else {
        // Insert element in second block
        currNode = currNode.next();
        modify(currNode, 1);
        currNode.block.doAdd(pos - blockLen, element);
        currIntBlockStart += blockLen;
        currIntBlockEnd++;
    }
}

    /**
 * Modify relativePosition of all nodes starting from the specified node.
 *
 * @param node		node whose position value must be changed
 * @param modify	modify value (>0 for add, <0 for delete)
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
    if (node.relPos < 0) {
        modifyLeftNode(node, modify);
    } else {
        modifyRightNode(node, modify);
    }
}

    private void modifyLeftNode(IntBlockNode node, int modify) {
    IntBlockNode leftNode = node.getLeftSubTree();
    if (leftNode != null) {
        leftNode.relPos -= modify;
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
}

    private void modifyRightNode(IntBlockNode node, int modify) {
    node.relPos += modify;
    IntBlockNode leftNode = node.getLeftSubTree();
    if (leftNode != null) {
        leftNode.relPos -= modify;
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
    rootNode = newNode;
    return n;
}

    @Override
protected boolean doAddAll(int index, IIntListable list) {
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
    int addPos = getIntBlockIndex(index, true, 0);
    IntBlock addIntBlock = currNode.block;
    int space = blockSize - addIntBlock.size();
    int addLen = list.size();
    if (addLen <= space) {
        // All elements can be added to current block
        currNode.block.doAddAll(addPos, list);
        modify(currNode, addLen);
        size += addLen;
        currIntBlockEnd += addLen;
    } else {
        if (index == size) {
            // Add elements at end
            doAddAllTail(list, addPos, addLen, space);
        } else if (index == 0) {
            // Add elements at head
            doAddAllHead(list, addPos, addLen, space);
        } else {
            // Add elements in the middle
            doAddAllMiddle(list, addPos);
        }
    }
    assert (oldSize + addLen == size);
    if (CHECK)
        check();
    return true;
}

    private void doAddAllTail(IIntListable list, int addPos, int addLen, int space) {
    for (int i = 0; i < space; i++) {
        currNode.block.add(addPos + i, list.get(i));
    }
    modify(currNode, space);
    int done = space;
    int todo = addLen - space;
    while (todo > 0) {
        IntBlock nextIntBlock = new IntBlock(blockSize);
        int add = Math.min(todo, blockSize);
        for (int i = 0; i < add; i++) {
            nextIntBlock.add(i, list.get(done + i));
        }
        done += add;
        todo -= add;
        addIntBlock(size + done, nextIntBlock);
        currNode = currNode.next();
    }
    size += addLen;
    currIntBlockEnd = size;
    currIntBlockStart = currIntBlockEnd - currNode.block.size();
}

    private void doAddAllHead(IIntListable list, int addPos, int addLen, int space) {
    assert (addPos == 0);
    for (int i = 0; i < space; i++) {
        currNode.block.add(addPos + i, list.get(addLen - space + i));
    }
    modify(currNode, space);
    int done = space;
    int todo = addLen - space;
    while (todo > 0) {
        IntBlock nextIntBlock = new IntBlock(blockSize);
        int add = Math.min(todo, blockSize);
        for (int i = 0; i < add; i++) {
            nextIntBlock.add(i, list.get(addLen - done - add + i));
        }
        done += add;
        todo -= add;
        addIntBlock(0, nextIntBlock);
        currNode = currNode.previous();
    }
    size += addLen;
    currIntBlockStart = 0;
    currIntBlockEnd = currNode.block.size();
}

    // method is not changed right now.
private // To have good performance, it would have to be guaranteed that escape analysis is able to perform scalar replacement. As this is not trivial,
void doAddAllMiddle(IIntListable list, int addPos) {
    // Split first block to remove tail elements if necessary
    // TODO avoid unnecessary copy
    IntGapList list2 = IntGapList.create();
    list2.doAddAll(-1, list);
    int remove = currNode.block.size() - addPos;
    if (remove > 0) {
        list2.addAll(currNode.block.getAll(addPos, remove));
        currNode.block.remove(addPos, remove);
        modify(currNode, -remove);
        size -= remove;
        currIntBlockEnd -= remove;
    }
    // Calculate how many blocks we need for the elements
    int numElems = currNode.block.size() + list2.size();
    int numIntBlocks = (numElems - 1) / blockSize + 1;
    assert (numIntBlocks > 1);
    int has = currNode.block.size();
    int should = numElems / numIntBlocks;
    int listPos = 0;
    if (has < should) {
        // Elements must be added to first block
        int add = should - has;
        IIntList sublist = list2.getAll(0, add);
        listPos += add;
        currNode.block.addAll(addPos, sublist);
        modify(currNode, add);
        assert (currNode.block.size() == should);
        numElems -= should;
        numIntBlocks--;
        size += add;
        currIntBlockEnd += add;
    } else if (has > should) {
        // Elements must be moved from first to second block
        IntBlock nextIntBlock = new IntBlock(blockSize);
        int move = has - should;
        nextIntBlock.addAll(currNode.block.getAll(currNode.block.size() - move, move));
        currNode.block.remove(currNode.block.size() - move, move);
        modify(currNode, -move);
        assert (currNode.block.size() == should);
        numElems -= should;
        numIntBlocks--;
        currIntBlockEnd -= move;
        should = numElems / numIntBlocks;
        int add = should - move;
        assert (add >= 0);
        IIntList sublist = list2.getAll(0, add);
        nextIntBlock.addAll(move, sublist);
        listPos += add;
        assert (nextIntBlock.size() == should);
        numElems -= should;
        numIntBlocks--;
        size += add;
        addIntBlock(currIntBlockEnd, nextIntBlock);
        currNode = currNode.next();
        assert (currNode.block == nextIntBlock);
        assert (currNode.block.size() == add + move);
        currIntBlockStart = currIntBlockEnd;
        currIntBlockEnd += add + move;
    } else {
        // IntBlock already has the correct size
        numElems -= should;
        numIntBlocks--;
    }
    if (CHECK)
        check();
    while (numIntBlocks > 0) {
        int add = numElems / numIntBlocks;
        assert (add > 0);
        IIntList sublist = list2.getAll(listPos, add);
        listPos += add;
        IntBlock nextIntBlock = new IntBlock();
        nextIntBlock.addAll(sublist);
        assert (nextIntBlock.size() == add);
        numElems -= add;
        addIntBlock(currIntBlockEnd, nextIntBlock);
        currNode = currNode.next();
        assert (currNode.block == nextIntBlock);
        assert (currNode.block.size() == add);
        currIntBlockStart = currIntBlockEnd;
        currIntBlockEnd += add;
        size += add;
        numIntBlocks--;
        if (CHECK)
            check();
    }
}

    @Override
protected void doClear() {
    finalize();
    rootNode = null;
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
    int startPos = getIntBlockIndex(index, true, 0);
    IntBlockNode startNode = currNode;
    
    int endPos = getIntBlockIndex(index + len - 1, true, 0);
    IntBlockNode endNode = currNode;
    if (startNode == endNode) {
        // Delete from single block
        getIntBlockIndex(index, true, -len);
        currNode.block.remove(startPos, len);
        if (currNode.block.isEmpty()) {
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
        doRemoveAll2(index, len, startPos, startNode, endNode);
    }
    if (CHECK)
        check();
}

    private void doRemoveAll2(int index, int len, int startPos, IntBlockNode startNode, IntBlockNode endNode) {
    if (CHECK)
        check();
    int startLen = startNode.block.size() - startPos;
    getIntBlockIndex(index, true, -startLen);
    startNode.block.remove(startPos, startLen);
    assert (startNode == currNode);
    if (currNode.block.isEmpty()) {
        releaseIntBlock();
        doRemove(startNode);
        startNode = null;
    }
    len -= startLen;
    size -= startLen;
    while (len > 0) {
        currNode = null;
        getIntBlockIndex(index, true, 0);
        int s = currNode.block.size();
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
            if (CHECK)
                check();
        } else {
            modify(currNode, -len);
            currNode.block.remove(0, len);
            size -= len;
            break;
        }
    }
    releaseIntBlock();
    if (CHECK)
        check();
    getIntBlockIndex(index, false, 0);
    merge(currNode);
}

    /**
 * Merge the specified node with the left or right neighbor if possible.
 *
 * @param node	candidate node for merge
 */
private void merge(IntBlockNode node) {
    if (node == null) {
        return;
    }
    final int minIntBlockSize = Math.max((int) (blockSize * MERGE_THRESHOLD), 1);
    if (node.block.size() >= minIntBlockSize) {
        return;
    }
    IntBlockNode oldCurrNode = node;
    IntBlockNode leftNode = node.previous();
    if (leftNode != null && leftNode.block.size() < minIntBlockSize) {
        // Merge with left block
        int len = node.block.size();
        int dstSize = leftNode.getIntBlock().size();
        for (int i = 0; i < len; i++) {
            leftNode.block.add(0);
        }
        IntGapList.transferCopy(node.block, 0, len, leftNode.block, dstSize, len);
        assert (leftNode.block.size() <= blockSize);
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
                rightNode.block.add(0, 0);
            }
            IntGapList.transferCopy(node.block, 0, len, rightNode.block, 0, len);
            assert (rightNode.block.size() <= blockSize);
            modify(rightNode, +len);
            modify(oldCurrNode, -len);
            releaseIntBlock();
            doRemove(oldCurrNode);
        }
    }
}

    @Override
protected int doRemove(int index) {
    int pos = getIntBlockIndex(index, true, -1);
    int oldElem = currNode.block.doRemove(pos);
    currIntBlockEnd--;
    final int minIntBlockSize = Math.max(blockSize / 3, 1);
    if (currNode.block.size() < minIntBlockSize) {
        if (currNode.block.size() == 0) {
            if (!isOnlyRootIntBlock()) {
                IntBlockNode oldCurrNode = currNode;
                releaseIntBlock();
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
public IntBigList unmodifiableList() {
    if (this instanceof ImmutableIntBigList) {
        return this;
    } else {
        return new ImmutableIntBigList(this);
    }
}

    @Override
public IntBigList immutableList() {
    if (this instanceof ImmutableIntBigList) {
        return this;
    } else {
        return new ImmutableIntBigList(copy());
    }
}

    @Override
protected void doEnsureCapacity(int minCapacity) {
    if (isOnlyRootIntBlock()) {
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
    if (isOnlyRootIntBlock()) {
        rootNode.block.trimToSize();
    } else {
        IntBigList newList = new IntBigList(blockSize);
        IntBlockNode node = rootNode.min();
        while (node != null) {
            newList.addAll(node.block);
            remove(0, node.block.size());
            node = node.next();
        }
        doAssign(newList);
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
        rootNode.block.sort(index, len);
    } else {
        IntMergeSort.sort(this, index, index + len);
    }
}

    
@Override
public int binarySearch(int index, int len, int key) {
    checkRange(index, len);
    if (isOnlyRootIntBlock()) {
        return rootNode.block.binarySearch(key);
    } else {
        return IntBinarySearch.binarySearch(this, key, 0, size());
    }
}

    /**
 * Serialize a IntBigList object.
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

    private void checkNode(IntBlockNode node) {
    assert ((node.block.size() > 0 || node == rootNode) && node.block.size() <= blockSize);
    IntBlockNode child = node.getLeftSubTree();
    assert (child == null || child.parent == node);
    child = node.getRightSubTree();
    assert (child == null || child.parent == node);
}

    private void checkHeight(IntBlockNode node) {
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

    private void check() {
    if (currNode != null) {
        assert (currIntBlockStart >= 0 && currIntBlockEnd <= size && currIntBlockStart <= currIntBlockEnd);
        assert (currIntBlockStart + currNode.block.size() == currIntBlockEnd);
    }
    if (rootNode == null) {
        assert (size == 0);
        return;
    }
    checkHeight(rootNode);
    IntBlockNode oldCurrNode = currNode;
    int oldCurrModify = currModify;
    if (currModify != 0) {
        currNode = null;
        currModify = 0;
        modify(oldCurrNode, oldCurrModify);
    }
    IntBlockNode node = rootNode;
    checkNode(node);
    int index = node.relPos;
    while (node.left != null) {
        node = node.left;
        checkNode(node);
        assert (node.relPos < 0);
        index += node.relPos;
    }
    IntBlock block = node.getIntBlock();
    assert (block.size() == index);
    int lastIndex = index;
    while (lastIndex < size()) {
        node = rootNode;
        index = node.relPos;
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
            index += node.relPos;
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

    // --- IntBlock ---
    /**
     * A block stores in maximum blockSize number of elements.
     * The first block in a IntBigList will grow until reaches this limit, all other blocks are directly
     * allocated with a capacity of blockSize.
     * A block maintains a reference count which allows a block to be shared among different IntBigList
     * instances with a copy-on-write approach.
     */
    
    static class IntBlock extends IntGapList {

        private AtomicInteger refCount = new AtomicInteger(1);

        public IntBlock() {
}

        public IntBlock(int capacity) {
    super(capacity);
}

        public IntBlock(IntBlock that) {
    super(that.capacity());
    addAll(that);
}

        /**
 * @return true if block is shared by several IntBigList instances
 */
public boolean isShared() {
    return refCount.get() > 1;
}

        /**
 * Increment reference count as block is used by one IntBigList instance more.
 */
public IntBlock ref() {
    refCount.incrementAndGet();
    return this;
}

        /**
 * Decrement reference count as block is no longer used by one IntBigList instance.
 */
public void unref() {
    refCount.decrementAndGet();
}
    }

    // --- IntBlockNode ---
    /**
     * Implements an AVLNode storing a IntBlock.
     * The nodes don't know the index of the object they are holding. They do know however their
     * position relative to their parent node. This allows to calculate the index of a node while traversing the tree.
     * There is a faedelung flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    static class IntBlockNode {

        /**
         * Pointer to parent node (null for root)
         */
        IntBlockNode parent;

        /**
         * The left child node or the predecessor if {@link #leftIsPrevious}.
         */
        IntBlockNode left;

        /**
         * Flag indicating that left reference is not a subtree but the predecessor.
         */
        boolean leftIsPrevious;

        /**
         * The right child node or the successor if {@link #rightIsNext}.
         */
        IntBlockNode right;

        /**
         * Flag indicating that right reference is not a subtree but the successor.
         */
        boolean rightIsNext;

        /**
         * How many levels of left/right are below this one.
         */
        int height;

        /**
         * Relative position of node relative to its parent, root holds absolute position.
         */
        int relPos;

        /**
         * The stored block
         */
        IntBlock block;

        /**
 * Constructs a new node.
 *
 * @param parent			parent node (null for root)
 * @param relativePosition  the relative position of the node (absolute position for root)
 * @param block				the block to store
 * @param rightFollower 	the node following this one
 * @param leftFollower 		the node leading this one
 */
private IntBlockNode(IntBlockNode parent, int relPos, IntBlock block, IntBlockNode rightFollower, IntBlockNode leftFollower) {
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
private IntBlock getIntBlock() {
    return block;
}

        /**
 * Sets block to store by this node.
 *
 * @param block  the block to store
 */
private void setIntBlock(IntBlock block) {
    this.block = block;
}

        /**
 * Gets the next node in the list after this one.
 *
 * @return the next node
 */
private IntBlockNode next() {
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
private IntBlockNode previous() {
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
private IntBlockNode insert(int index, IntBlock obj) {
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
private IntBlockNode insertOnLeft(int relIndex, IntBlock obj) {
    if (getLeftSubTree() == null) {
        int pos;
        if (relPos >= 0) {
            pos = -relPos;
        } else {
            pos = -block.size();
        }
        setLeft(new IntBlockNode(this, pos, obj, this, left), null);
    } else {
        setLeft(left.insert(relIndex, obj), null);
    }
    if (relPos >= 0) {
        relPos += obj.size();
    }
    IntBlockNode ret = balance();
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
private IntBlockNode insertOnRight(int relIndex, IntBlock obj) {
    if (getRightSubTree() == null) {
        setRight(new IntBlockNode(this, obj.size(), obj, right, this), null);
    } else {
        setRight(right.insert(relIndex, obj), null);
    }
    if (relPos < 0) {
        relPos -= obj.size();
    }
    IntBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        /**
 * Gets the left node, returning null if its a faedelung.
 */
private IntBlockNode getLeftSubTree() {
    return leftIsPrevious ? null : left;
}

        /**
 * Gets the right node, returning null if its a faedelung.
 */
private IntBlockNode getRightSubTree() {
    return rightIsNext ? null : right;
}

        /**
 * Gets the rightmost child of this node.
 *
 * @return the rightmost child (greatest index)
 */
private IntBlockNode max() {
    return getRightSubTree() == null ? this : right.max();
}

        /**
 * Gets the leftmost child of this node.
 *
 * @return the leftmost child (smallest index)
 */
private IntBlockNode min() {
    return getLeftSubTree() == null ? this : left.min();
}

        private IntBlockNode removeMax() {
    if (getRightSubTree() == null) {
        return removeSelf();
    }
    setRight(right.removeMax(), right.right);
    recalcHeight();
    return balance();
}

        private IntBlockNode removeMin(int size) {
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
private IntBlockNode removeSelf() {
    IntBlockNode p = parent;
    IntBlockNode n = doRemoveSelf();
    if (n != null) {
        assert (p != n);
        n.parent = p;
    }
    return n;
}

        private IntBlockNode doRemoveSelf() {
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
        final IntBlockNode rightMin = right.min();
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
private int getOffset(IntBlockNode node) {
    if (node == null) {
        return 0;
    }
    return node.relPos;
}

        /**
 * Sets the relative position.
 */
private int setOffset(IntBlockNode node, int newOffest) {
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
private int getHeight(final IntBlockNode node) {
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
private IntBlockNode rotateLeft() {
    assert (!rightIsNext);
    // can't be faedelung!
    final IntBlockNode newTop = right;
    final IntBlockNode movedNode = getRightSubTree().getLeftSubTree();
    final int newTopPosition = relPos + getOffset(newTop);
    final int myNewPosition = -newTop.relPos;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    IntBlockNode p = this.parent;
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
private IntBlockNode rotateRight() {
    assert (!leftIsPrevious);
    // can't be faedelung
    final IntBlockNode newTop = left;
    final IntBlockNode movedNode = getLeftSubTree().getRightSubTree();
    final int newTopPosition = relPos + getOffset(newTop);
    final int myNewPosition = -newTop.relPos;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    IntBlockNode p = this.parent;
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
private void setLeft(IntBlockNode node, IntBlockNode previous) {
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
private void setRight(IntBlockNode node, IntBlockNode next) {
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
    return new StringBuilder().append("IntBlockNode(").append(relPos).append(',').append(getRightSubTree() != null).append(',').append(block).append(',').append(getRightSubTree() != null).append(", height ").append(height).append(" )").toString();
}
    }

    // --- ImmutableIntBigList ---
    /**
     * An immutable version of a IntBigList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableIntBigList extends IntBigList {

        /**
         * UID for serialization
         */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
 * Private constructor used internally.
 *
 * @param that  list to create an immutable view of
 */
protected ImmutableIntBigList(IntBigList that) {
    super(true, that);
}

        @Override
protected boolean doAdd(int index, int elem) {
    error();
    return false;
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
}
