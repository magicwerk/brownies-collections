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
import org.magicwerk.brownies.collections.helper.primitive.ByteBinarySearch;
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
import org.magicwerk.brownies.collections.helper.primitive.ByteMergeSort;

/**
 * ByteBigList is a list optimized for storing large number of elements.
 * It stores the elements in fixed size blocks and the blocks itself are maintained in a tree for fast access.
 * It also offers specialized methods for bulk processing of elements.
 * Also copying a ByteBigList is efficiently possible as its implemented using a copy-on-write approach.<p>
 *
 * <strong>Note that this implementation is not synchronized.</strong>
 * Due to data caching used for exploiting locality of reference, performance can decrease if ByteBigList is
 * accessed by several threads at different positions.<p>
 *
 * Note that the iterators provided are not fail-fast.<p>
 *
 * @author Thomas Mauch
 */
public class ByteBigList extends IByteList {

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
    // public static ByteBigList EMPTY = ByteBigList.create().unmodifiableList();
    // Syntax error:
    // public static  ByteBigList EMPTY = ByteBigList.create().unmodifiableList();
    /**
     * Unmodifiable empty instance
     */
    private static final ByteBigList EMPTY = ByteBigList.create().unmodifiableList();

    /**
     * @return unmodifiable empty instance
     */
    public static ByteBigList EMPTY() {
        return EMPTY;
    }

    /**
     * Number of elements stored at maximum in a block
     */
    private int blockSize;

    /**
     * Number of elements stored in this ByteBigList
     */
    private int size;

    /**
     * The root node in the tree
     */
    private ByteBlockNode rootNode;

    /**
     * Current node
     */
    private ByteBlockNode currNode;

    /**
     * ByteBlock of current node
     */
    /**
     * Start index of current block
     */
    private int currByteBlockStart;

    /**
     * End index of current block
     */
    private int currByteBlockEnd;

    /**
     * Modify value which must be applied before this block is not current any more
     */
    private int currModify;

    /**
     * Constructor used internally, e.g. for ImmutableByteBigList.
     *
     * @param copy true to copy all instance values from source,
     *             if false nothing is done
     * @param that list to copy
     */
    protected ByteBigList(boolean copy, ByteBigList that) {
        if (copy) {
            this.blockSize = that.blockSize;
            this.currByteBlockStart = that.currByteBlockStart;
            this.currByteBlockEnd = that.currByteBlockEnd;
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
    ByteBigList create() {
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
        return new ByteBigList((coll != null) ? coll : Collections.emptyList());
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
        if (elems != null) {
            for (byte elem : elems) {
                list.add(elem);
            }
        }
        return list;
    }

    /**
     * Default constructor.
     * The default block size is used.
     */
    public ByteBigList() {
        this(DEFAULT_BLOCK_SIZE);
    }

    /**
     * Constructor.
     *
     * @param blockSize block size
     */
    public ByteBigList(int blockSize) {
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
    public ByteBigList(Collection<Byte> coll) {
        if (coll instanceof ByteBigList) {
            doAssign((ByteBigList) coll);
            doClone((ByteBigList) coll);
        } else {
            blockSize = DEFAULT_BLOCK_SIZE;
            addByteBlock(0, new ByteBlock());
            for (Object obj : coll.toArray()) {
                add((Byte) obj);
            }
            assert (size() == coll.size());
        }
    }

    /**
     * Returns block size used for this ByteBigList.
     *
     * @return block size used for this ByteBigList
     */
    public int blockSize() {
        return blockSize;
    }

    /**
     * Internal constructor.
     *
     * @param blockSize			default block size
     * @param firstByteBlockSize	block size of first block
     */
    private ByteBigList(int blockSize, int firstByteBlockSize) {
        doInit(blockSize, firstByteBlockSize);
    }

    /**
     * Initialize ByteBigList.
     *
     * @param blockSize			default block size
     * @param firstByteBlockSize	block size of first block
     */
    private void doInit(int blockSize, int firstByteBlockSize) {
        this.blockSize = blockSize;
        // First block will grow until it reaches blockSize
        ByteBlock block;
        if (firstByteBlockSize <= 1) {
            block = new ByteBlock();
        } else {
            block = new ByteBlock(firstByteBlockSize);
        }
        addByteBlock(0, block);
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
    public ByteBigList copy() {
        return (ByteBigList) clone();
    }

    @Override
    public ByteBigList crop() {
        return (ByteBigList) super.crop();
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
        if (this instanceof ImmutableByteBigList) {
            ByteBigList list = new ByteBigList(false, null);
            list.doClone(this);
            return list;
        } else {
            return super.clone();
        }
    }

    @Override
    protected void doAssign(IByteList that) {
        ByteBigList list = (ByteBigList) that;
        this.blockSize = list.blockSize;
        this.currByteBlockEnd = list.currByteBlockEnd;
        this.currByteBlockStart = list.currByteBlockStart;
        this.currNode = list.currNode;
        this.rootNode = list.rootNode;
        this.size = list.size;
    }

    @Override
    protected void doClone(IByteList that) {
        ByteBigList bigList = (ByteBigList) that;
        bigList.releaseByteBlock();
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
        // This list will be garbage collected, so unref all referenced blocks.
        // As it is not reachable by any live objects, if is safe to access it from the GC thread without synchronization
        ByteBlockNode node = rootNode.min();
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
     * As ByteBigList grows and shrinks automatically, the term capacity does not really make sense.
     * Therefore always -1 is returned.
     */
    @Override
    public int capacity() {
        return -1;
    }

    @Override
    protected byte doGet(int index) {
        int pos = getByteBlockIndex(index, false, 0);
        return currNode.block.doGet(pos);
    }

    @Override
    protected byte doSet(int index, byte elem) {
        int pos = getByteBlockIndex(index, true, 0);
        byte oldElem = currNode.block.doGet(pos);
        currNode.block.doSet(pos, elem);
        return oldElem;
    }

    @Override
    protected byte doReSet(int index, byte elem) {
        int pos = getByteBlockIndex(index, true, 0);
        byte oldElem = currNode.block.doGet(pos);
        currNode.block.doSet(pos, elem);
        return oldElem;
    }

    /**
     * Release current block and apply modification if pending.
     */
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
     * @param index		list index (0 <= index <= size())
     * @param write		true if the block is needed for a write operation (set, add, remove)
     * @param modify	modify instruction (N>0: N elements are added, N<0: N elements are removed, 0 no change)
     * @return			relative index within block
     */
    private int getByteBlockIndex(int index, boolean write, int modify) {
        // Determine block where specified index is located and store it in currByteBlock
        if (currNode != null) {
            if (index >= currByteBlockStart && (index < currByteBlockEnd || index == currByteBlockEnd && size == index)) {
                // currByteBlock is already set correctly
                if (write) {
                    if (currNode.block.isShared()) {
                        currNode.block.unref();
                        currNode.setByteBlock(new ByteBlock(currNode.block));
                    }
                }
                currModify += modify;
                return index - currByteBlockStart;
            }
            releaseByteBlock();
        }
        return getByteBlockIndex2(index, write, modify);
    }

    private int getByteBlockIndex2(int index, boolean write, int modify) {
        if (index == size) {
            if (currNode == null || currByteBlockEnd != size) {
                currNode = rootNode.max();
                currByteBlockEnd = size;
                currByteBlockStart = size - currNode.block.size();
            }
            if (modify != 0) {
                currNode.relPos += modify;
                ByteBlockNode leftNode = currNode.getLeftSubTree();
                if (leftNode != null) {
                    leftNode.relPos -= modify;
                }
            }
        } else if (index == 0) {
            if (currNode == null || currByteBlockStart != 0) {
                currNode = rootNode.min();
                currByteBlockEnd = currNode.block.size();
                currByteBlockStart = 0;
            }
            if (modify != 0) {
                rootNode.relPos += modify;
            }
        }
        if (currNode == null) {
            doGetByteBlock(index, modify);
        }
        assert (index >= currByteBlockStart && index <= currByteBlockEnd);
        if (write) {
            if (currNode.block.isShared()) {
                currNode.block.unref();
                currNode.setByteBlock(new ByteBlock(currNode.block));
            }
        }
        return index - currByteBlockStart;
    }

    /**
     * @return true if there is only the root block, false otherwise
     */
    private boolean isOnlyRootByteBlock() {
        return rootNode.left == null && rootNode.right == null;
    }

    /**
     * Determine node/block for the specified index.
     * The fields currNode, currByteBlockStart, and currByteBlockEnd are set.
     * During the traversing the tree node, the nodes relative positions are changed according to the modify instruction.
     *
     * @param index		list index for which block must be determined
     * @param modify	modify instruction (N>0: N elements are added, N<0: N elements are removed, 0 no change)
     */
    private void doGetByteBlock(int index, int modify) {
        currNode = rootNode;
        currByteBlockEnd = rootNode.relPos;
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
                int leftIndex = currByteBlockEnd - currNode.block.size();
                assert (leftIndex >= 0);
                if (index >= leftIndex && index < currByteBlockEnd) {
                    // Correct node has been found
                    if (modify != 0) {
                        ByteBlockNode leftNode = currNode.getLeftSubTree();
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
                ByteBlockNode nextNode;
                if (index < currByteBlockEnd) {
                    // Traverse the left node
                    nextNode = currNode.getLeftSubTree();
                    wasLeft = doGetByteBlockLeft(modify, nextNode, wasLeft);
                    if (nextNode == null) {
                        break;
                    }
                } else {
                    // Traverse the right node
                    nextNode = currNode.getRightSubTree();
                    wasLeft = doGetByteBlockRight(modify, nextNode, wasLeft);
                    if (nextNode == null) {
                        break;
                    }
                }
                currByteBlockEnd += nextNode.relPos;
                currNode = nextNode;
            }
        }
        currByteBlockStart = currByteBlockEnd - currNode.block.size();
    }

    private boolean doGetByteBlockLeft(int modify, ByteBlockNode nextNode, boolean wasLeft) {
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

    private boolean doGetByteBlockRight(int modify, ByteBlockNode nextNode, boolean wasLeft) {
        if (modify != 0) {
            if (nextNode == null || wasLeft) {
                if (currNode.relPos > 0) {
                    currNode.relPos += modify;
                    ByteBlockNode left = currNode.getLeftSubTree();
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
    private void addByteBlock(int index, ByteBlock obj) {
        if (rootNode == null) {
            rootNode = new ByteBlockNode(null, index, obj, null, null);
        } else {
            rootNode = rootNode.insert(index, obj);
            rootNode.parent = null;
        }
    }

    @Override
    protected boolean doAdd(int index, byte element) {
        if (index == -1) {
            index = size;
        }
        // Insert
        int pos = getByteBlockIndex(index, true, 1);
        // If there is still place in the current block: insert in current block
        int maxSize = (index == size || index == 0) ? (int) (blockSize * FILL_THRESHOLD) : blockSize;
        // The second part of the condition is a work around to handle the case of insertion as position 0 correctly
        // where blockSize() is 2 (the new block would then be added after the current one)
        if (currNode.block.size() < maxSize || (currNode.block.size() == 1 && currNode.block.size() < blockSize)) {
            currNode.block.doAdd(pos, element);
            currByteBlockEnd++;
        } else {
            // No place any more in current block
            ByteBlock newByteBlock = new ByteBlock(blockSize);
            if (index == size) {
                // Insert new block at tail
                newByteBlock.doAdd(0, element);
                // Subtract 1 because getByteBlockIndex() has already added 1
                modify(currNode, -1);
                addByteBlock(size + 1, newByteBlock);
                ByteBlockNode lastNode = currNode.next();
                currNode = lastNode;
                currByteBlockStart = currByteBlockEnd;
                currByteBlockEnd++;
            } else if (index == 0) {
                // Insert new block at head
                newByteBlock.doAdd(0, element);
                // Subtract 1 because getByteBlockIndex() has already added 1
                modify(currNode, -1);
                addByteBlock(1, newByteBlock);
                ByteBlockNode firstNode = currNode.previous();
                currNode = firstNode;
                currByteBlockStart = 0;
                currByteBlockEnd = 1;
            } else {
                // Split block for insert
                doAddSplitByteBlock(index, element, pos, newByteBlock);
            }
        }
        size++;
        if (CHECK)
            check();
        return true;
    }

    private void doAddSplitByteBlock(int index, byte element, int pos, ByteBlock newByteBlock) {
        int nextByteBlockLen = blockSize / 2;
        int blockLen = blockSize - nextByteBlockLen;
        ByteGapList.transferRemove(currNode.block, blockLen, nextByteBlockLen, newByteBlock, 0, 0);
        // Subtract 1 more because getByteBlockIndex() has already added 1
        modify(currNode, -nextByteBlockLen - 1);
        addByteBlock(currByteBlockEnd - nextByteBlockLen, newByteBlock);
        if (pos < blockLen) {
            // Insert element in first block
            currNode.block.doAdd(pos, element);
            currByteBlockEnd = currByteBlockStart + blockLen + 1;
            modify(currNode, 1);
        } else {
            // Insert element in second block
            currNode = currNode.next();
            modify(currNode, 1);
            currNode.block.doAdd(pos - blockLen, element);
            currByteBlockStart += blockLen;
            currByteBlockEnd++;
        }
    }

    /**
     * Modify relativePosition of all nodes starting from the specified node.
     *
     * @param node		node whose position value must be changed
     * @param modify	modify value (>0 for add, <0 for delete)
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
        if (node.relPos < 0) {
            modifyLeftNode(node, modify);
        } else {
            modifyRightNode(node, modify);
        }
    }

    private void modifyLeftNode(ByteBlockNode node, int modify) {
        ByteBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relPos -= modify;
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

    private void modifyRightNode(ByteBlockNode node, int modify) {
        node.relPos += modify;
        ByteBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relPos -= modify;
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
        rootNode = newNode;
        return n;
    }

    @Override
    protected boolean doAddAll(int index, IByteList list) {
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
        int addPos = getByteBlockIndex(index, true, 0);
        ByteBlock addByteBlock = currNode.block;
        int space = blockSize - addByteBlock.size();
        int addLen = list.size();
        if (addLen <= space) {
            // All elements can be added to current block
            currNode.block.addAll(addPos, list);
            modify(currNode, addLen);
            size += addLen;
            currByteBlockEnd += addLen;
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

    private void doAddAllTail(IByteList list, int addPos, int addLen, int space) {
        for (int i = 0; i < space; i++) {
            currNode.block.add(addPos + i, list.get(i));
        }
        modify(currNode, space);
        int done = space;
        int todo = addLen - space;
        while (todo > 0) {
            ByteBlock nextByteBlock = new ByteBlock(blockSize);
            int add = Math.min(todo, blockSize);
            for (int i = 0; i < add; i++) {
                nextByteBlock.add(i, list.get(done + i));
            }
            done += add;
            todo -= add;
            addByteBlock(size + done, nextByteBlock);
            currNode = currNode.next();
        }
        size += addLen;
        currByteBlockEnd = size;
        currByteBlockStart = currByteBlockEnd - currNode.block.size();
    }

    private void doAddAllHead(IByteList list, int addPos, int addLen, int space) {
        assert (addPos == 0);
        for (int i = 0; i < space; i++) {
            currNode.block.add(addPos + i, list.get(addLen - space + i));
        }
        modify(currNode, space);
        int done = space;
        int todo = addLen - space;
        while (todo > 0) {
            ByteBlock nextByteBlock = new ByteBlock(blockSize);
            int add = Math.min(todo, blockSize);
            for (int i = 0; i < add; i++) {
                nextByteBlock.add(i, list.get(addLen - done - add + i));
            }
            done += add;
            todo -= add;
            addByteBlock(0, nextByteBlock);
            currNode = currNode.previous();
        }
        size += addLen;
        currByteBlockStart = 0;
        currByteBlockEnd = currNode.block.size();
    }

    // method is not changed right now.
    private // To have good performance, it would have to be guaranteed that escape analysis is able to perform scalar replacement. As this is not trivial,
    void doAddAllMiddle(IByteList list, int addPos) {
        // Split first block to remove tail elements if necessary
        // TODO avoid unnecessary copy
        ByteGapList list2 = ByteGapList.create();
        list2.addAll(list);
        int remove = currNode.block.size() - addPos;
        if (remove > 0) {
            list2.addAll(currNode.block.getAll(addPos, remove));
            currNode.block.remove(addPos, remove);
            modify(currNode, -remove);
            size -= remove;
            currByteBlockEnd -= remove;
        }
        // Calculate how many blocks we need for the elements
        int numElems = currNode.block.size() + list2.size();
        int numByteBlocks = (numElems - 1) / blockSize + 1;
        assert (numByteBlocks > 1);
        int has = currNode.block.size();
        int should = numElems / numByteBlocks;
        int listPos = 0;
        if (has < should) {
            // Elements must be added to first block
            int add = should - has;
            IByteList sublist = list2.getAll(0, add);
            listPos += add;
            currNode.block.addAll(addPos, sublist);
            modify(currNode, add);
            assert (currNode.block.size() == should);
            numElems -= should;
            numByteBlocks--;
            size += add;
            currByteBlockEnd += add;
        } else if (has > should) {
            // Elements must be moved from first to second block
            ByteBlock nextByteBlock = new ByteBlock(blockSize);
            int move = has - should;
            nextByteBlock.addAll(currNode.block.getAll(currNode.block.size() - move, move));
            currNode.block.remove(currNode.block.size() - move, move);
            modify(currNode, -move);
            assert (currNode.block.size() == should);
            numElems -= should;
            numByteBlocks--;
            currByteBlockEnd -= move;
            should = numElems / numByteBlocks;
            int add = should - move;
            assert (add >= 0);
            IByteList sublist = list2.getAll(0, add);
            nextByteBlock.addAll(move, sublist);
            listPos += add;
            assert (nextByteBlock.size() == should);
            numElems -= should;
            numByteBlocks--;
            size += add;
            addByteBlock(currByteBlockEnd, nextByteBlock);
            currNode = currNode.next();
            assert (currNode.block == nextByteBlock);
            assert (currNode.block.size() == add + move);
            currByteBlockStart = currByteBlockEnd;
            currByteBlockEnd += add + move;
        } else {
            // ByteBlock already has the correct size
            numElems -= should;
            numByteBlocks--;
        }
        if (CHECK)
            check();
        while (numByteBlocks > 0) {
            int add = numElems / numByteBlocks;
            assert (add > 0);
            IByteList sublist = list2.getAll(listPos, add);
            listPos += add;
            ByteBlock nextByteBlock = new ByteBlock();
            nextByteBlock.addAll(sublist);
            assert (nextByteBlock.size() == add);
            numElems -= add;
            addByteBlock(currByteBlockEnd, nextByteBlock);
            currNode = currNode.next();
            assert (currNode.block == nextByteBlock);
            assert (currNode.block.size() == add);
            currByteBlockStart = currByteBlockEnd;
            currByteBlockEnd += add;
            size += add;
            numByteBlocks--;
            if (CHECK)
                check();
        }
    }

    @Override
    protected void doClear() {
        finalize();
        rootNode = null;
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
        int startPos = getByteBlockIndex(index, true, 0);
        ByteBlockNode startNode = currNode;
        int endPos = getByteBlockIndex(index + len - 1, true, 0);
        ByteBlockNode endNode = currNode;
        if (startNode == endNode) {
            // Delete from single block
            getByteBlockIndex(index, true, -len);
            currNode.block.remove(startPos, len);
            if (currNode.block.isEmpty()) {
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
            doRemoveAll2(index, len, startPos, startNode, endNode);
        }
        if (CHECK)
            check();
    }

    private void doRemoveAll2(int index, int len, int startPos, ByteBlockNode startNode, ByteBlockNode endNode) {
        if (CHECK)
            check();
        int startLen = startNode.block.size() - startPos;
        getByteBlockIndex(index, true, -startLen);
        startNode.block.remove(startPos, startLen);
        assert (startNode == currNode);
        if (currNode.block.isEmpty()) {
            releaseByteBlock();
            doRemove(startNode);
            startNode = null;
        }
        len -= startLen;
        size -= startLen;
        while (len > 0) {
            currNode = null;
            getByteBlockIndex(index, true, 0);
            int s = currNode.block.size();
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
                if (CHECK)
                    check();
            } else {
                modify(currNode, -len);
                currNode.block.remove(0, len);
                size -= len;
                break;
            }
        }
        releaseByteBlock();
        if (CHECK)
            check();
        getByteBlockIndex(index, false, 0);
        merge(currNode);
    }

    /**
     * Merge the specified node with the left or right neighbor if possible.
     *
     * @param node	candidate node for merge
     */
    private void merge(ByteBlockNode node) {
        if (node == null) {
            return;
        }
        final int minByteBlockSize = Math.max((int) (blockSize * MERGE_THRESHOLD), 1);
        if (node.block.size() >= minByteBlockSize) {
            return;
        }
        ByteBlockNode oldCurrNode = node;
        ByteBlockNode leftNode = node.previous();
        if (leftNode != null && leftNode.block.size() < minByteBlockSize) {
            // Merge with left block
            int len = node.block.size();
            int dstSize = leftNode.getByteBlock().size();
            for (int i = 0; i < len; i++) {
                leftNode.block.add((byte) 0);
            }
            ByteGapList.transferCopy(node.block, 0, len, leftNode.block, dstSize, len);
            assert (leftNode.block.size() <= blockSize);
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
                    rightNode.block.add(0, (byte) 0);
                }
                ByteGapList.transferCopy(node.block, 0, len, rightNode.block, 0, len);
                assert (rightNode.block.size() <= blockSize);
                modify(rightNode, +len);
                modify(oldCurrNode, -len);
                releaseByteBlock();
                doRemove(oldCurrNode);
            }
        }
    }

    @Override
    protected void doRelease(int index) {
    }

    @Override
    protected byte doRemove(int index) {
        int pos = getByteBlockIndex(index, true, -1);
        byte oldElem = currNode.block.doRemove(pos);
        currByteBlockEnd--;
        final int minByteBlockSize = Math.max(blockSize / 3, 1);
        if (currNode.block.size() < minByteBlockSize) {
            if (currNode.block.size() == 0) {
                if (!isOnlyRootByteBlock()) {
                    ByteBlockNode oldCurrNode = currNode;
                    releaseByteBlock();
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
    public ByteBigList unmodifiableList() {
        // Naming as in java.util.Collections#unmodifiableList
        if (this instanceof ImmutableByteBigList) {
            return this;
        } else {
            return new ImmutableByteBigList(this);
        }
    }

    @Override
    protected void doEnsureCapacity(int minCapacity) {
        if (isOnlyRootByteBlock()) {
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
        if (isOnlyRootByteBlock()) {
            rootNode.block.trimToSize();
        } else {
            ByteBigList newList = new ByteBigList(blockSize);
            ByteBlockNode node = rootNode.min();
            while (node != null) {
                newList.addAll(node.block);
                remove(0, node.block.size());
                node = node.next();
            }
            doAssign(newList);
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
            rootNode.block.sort(index, len);
        } else {
            ByteMergeSort.sort(this, index, index + len);
        }
    }

    @Override
    public int binarySearch(int index, int len, byte key) {
        checkRange(index, len);
        if (isOnlyRootByteBlock()) {
            return rootNode.block.binarySearch(key);
        } else {
            return ByteBinarySearch.binarySearch(this, key, 0, size());
        }
    }

    /**
     * Serialize a ByteBigList object.
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

    private void checkNode(ByteBlockNode node) {
        assert ((node.block.size() > 0 || node == rootNode) && node.block.size() <= blockSize);
        ByteBlockNode child = node.getLeftSubTree();
        assert (child == null || child.parent == node);
        child = node.getRightSubTree();
        assert (child == null || child.parent == node);
    }

    private void checkHeight(ByteBlockNode node) {
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

    private void check() {
        if (currNode != null) {
            assert (currByteBlockStart >= 0 && currByteBlockEnd <= size && currByteBlockStart <= currByteBlockEnd);
            assert (currByteBlockStart + currNode.block.size() == currByteBlockEnd);
        }
        if (rootNode == null) {
            assert (size == 0);
            return;
        }
        checkHeight(rootNode);
        ByteBlockNode oldCurrNode = currNode;
        int oldCurrModify = currModify;
        if (currModify != 0) {
            currNode = null;
            currModify = 0;
            modify(oldCurrNode, oldCurrModify);
        }
        ByteBlockNode node = rootNode;
        checkNode(node);
        int index = node.relPos;
        while (node.left != null) {
            node = node.left;
            checkNode(node);
            assert (node.relPos < 0);
            index += node.relPos;
        }
        ByteBlock block = node.getByteBlock();
        assert (block.size() == index);
        int lastIndex = index;
        while (lastIndex < size()) {
            node = rootNode;
            index = node.relPos;
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
                index += node.relPos;
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

    // --- ByteBlock ---
    /**
     * A block stores in maximum blockSize number of elements.
     * The first block in a ByteBigList will grow until reaches this limit, all other blocks are directly
     * allocated with a capacity of blockSize.
     * A block maintains a reference count which allows a block to be shared among different ByteBigList
     * instances with a copy-on-write approach.
     */
    static class ByteBlock extends ByteGapList {

        private AtomicInteger refCount = new AtomicInteger(1);

        public ByteBlock() {
        }

        public ByteBlock(int capacity) {
            super(capacity);
        }

        public ByteBlock(ByteBlock that) {
            super(that.capacity());
            addAll(that);
        }

        /**
         * @return true if block is shared by several ByteBigList instances
         */
        public boolean isShared() {
            return refCount.get() > 1;
        }

        /**
         * Increment reference count as block is used by one ByteBigList instance more.
         */
        public ByteBlock ref() {
            refCount.incrementAndGet();
            return this;
        }

        /**
         * Decrement reference count as block is no longer used by one ByteBigList instance.
         */
        public void unref() {
            refCount.decrementAndGet();
        }
    }

    // --- ByteBlockNode ---
    /**
     * Implements an AVLNode storing a ByteBlock.
     * The nodes don't know the index of the object they are holding. They do know however their
     * position relative to their parent node. This allows to calculate the index of a node while traversing the tree.
     * There is a faedelung flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    static class ByteBlockNode {

        /**
         * Pointer to parent node (null for root)
         */
        ByteBlockNode parent;

        /**
         * The left child node or the predecessor if {@link #leftIsPrevious}.
         */
        ByteBlockNode left;

        /**
         * Flag indicating that left reference is not a subtree but the predecessor.
         */
        boolean leftIsPrevious;

        /**
         * The right child node or the successor if {@link #rightIsNext}.
         */
        ByteBlockNode right;

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
        ByteBlock block;

        /**
         * Constructs a new node.
         *
         * @param parent			parent node (null for root)
         * @param relativePosition  the relative position of the node (absolute position for root)
         * @param block				the block to store
         * @param rightFollower 	the node following this one
         * @param leftFollower 		the node leading this one
         */
        private ByteBlockNode(ByteBlockNode parent, int relPos, ByteBlock block, ByteBlockNode rightFollower, ByteBlockNode leftFollower) {
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
        private ByteBlock getByteBlock() {
            return block;
        }

        /**
         * Sets block to store by this node.
         *
         * @param block  the block to store
         */
        private void setByteBlock(ByteBlock block) {
            this.block = block;
        }

        /**
         * Gets the next node in the list after this one.
         *
         * @return the next node
         */
        private ByteBlockNode next() {
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
        private ByteBlockNode previous() {
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
        private ByteBlockNode insert(int index, ByteBlock obj) {
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
        private ByteBlockNode insertOnLeft(int relIndex, ByteBlock obj) {
            if (getLeftSubTree() == null) {
                int pos;
                if (relPos >= 0) {
                    pos = -relPos;
                } else {
                    pos = -block.size();
                }
                setLeft(new ByteBlockNode(this, pos, obj, this, left), null);
            } else {
                setLeft(left.insert(relIndex, obj), null);
            }
            if (relPos >= 0) {
                relPos += obj.size();
            }
            ByteBlockNode ret = balance();
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
        private ByteBlockNode insertOnRight(int relIndex, ByteBlock obj) {
            if (getRightSubTree() == null) {
                setRight(new ByteBlockNode(this, obj.size(), obj, right, this), null);
            } else {
                setRight(right.insert(relIndex, obj), null);
            }
            if (relPos < 0) {
                relPos -= obj.size();
            }
            ByteBlockNode ret = balance();
            recalcHeight();
            return ret;
        }

        /**
         * Gets the left node, returning null if its a faedelung.
         */
        private ByteBlockNode getLeftSubTree() {
            return leftIsPrevious ? null : left;
        }

        /**
         * Gets the right node, returning null if its a faedelung.
         */
        private ByteBlockNode getRightSubTree() {
            return rightIsNext ? null : right;
        }

        /**
         * Gets the rightmost child of this node.
         *
         * @return the rightmost child (greatest index)
         */
        private ByteBlockNode max() {
            return getRightSubTree() == null ? this : right.max();
        }

        /**
         * Gets the leftmost child of this node.
         *
         * @return the leftmost child (smallest index)
         */
        private ByteBlockNode min() {
            return getLeftSubTree() == null ? this : left.min();
        }

        private ByteBlockNode removeMax() {
            if (getRightSubTree() == null) {
                return removeSelf();
            }
            setRight(right.removeMax(), right.right);
            recalcHeight();
            return balance();
        }

        private ByteBlockNode removeMin(int size) {
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
        private ByteBlockNode removeSelf() {
            ByteBlockNode p = parent;
            ByteBlockNode n = doRemoveSelf();
            if (n != null) {
                assert (p != n);
                n.parent = p;
            }
            return n;
        }

        private ByteBlockNode doRemoveSelf() {
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
                final ByteBlockNode rightMin = right.min();
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
        private int getOffset(ByteBlockNode node) {
            if (node == null) {
                return 0;
            }
            return node.relPos;
        }

        /**
         * Sets the relative position.
         */
        private int setOffset(ByteBlockNode node, int newOffest) {
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
        private int getHeight(final ByteBlockNode node) {
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
        private ByteBlockNode rotateLeft() {
            assert (!rightIsNext);
            // can't be faedelung!
            final ByteBlockNode newTop = right;
            final ByteBlockNode movedNode = getRightSubTree().getLeftSubTree();
            final int newTopPosition = relPos + getOffset(newTop);
            final int myNewPosition = -newTop.relPos;
            final int movedPosition = getOffset(newTop) + getOffset(movedNode);
            ByteBlockNode p = this.parent;
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
        private ByteBlockNode rotateRight() {
            assert (!leftIsPrevious);
            // can't be faedelung
            final ByteBlockNode newTop = left;
            final ByteBlockNode movedNode = getLeftSubTree().getRightSubTree();
            final int newTopPosition = relPos + getOffset(newTop);
            final int myNewPosition = -newTop.relPos;
            final int movedPosition = getOffset(newTop) + getOffset(movedNode);
            ByteBlockNode p = this.parent;
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
        private void setLeft(ByteBlockNode node, ByteBlockNode previous) {
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
        private void setRight(ByteBlockNode node, ByteBlockNode next) {
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
            return new StringBuilder().append("ByteBlockNode(").append(relPos).append(',').append(getRightSubTree() != null).append(',').append(block).append(',').append(getRightSubTree() != null).append(", height ").append(height).append(" )").toString();
        }
    }

    // --- ImmutableByteBigList ---
    /**
     * An immutable version of a ByteBigList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableByteBigList extends ByteBigList {

        /**
         * UID for serialization
         */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
        protected ImmutableByteBigList(ByteBigList that) {
            super(true, that);
        }

        @Override
        protected boolean doAdd(int index, byte elem) {
            error();
            return false;
        }

        @Override
        protected byte doSet(int index, byte elem) {
            error();
            return (byte) 0;
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
}
