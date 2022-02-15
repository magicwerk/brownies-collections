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
package org.magicwerk.brownies.collections;

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

import org.magicwerk.brownies.collections.helper.MergeSort;

/**
 * BigList is a list optimized for storing large number of elements.
 * It stores the elements in fixed size blocks and the blocks itself are maintained in a tree for fast access.
 * It also offers specialized methods for bulk processing of elements.
 * Also copying a BigList is efficiently possible as its implemented using a copy-on-write approach.<p>
 *
 * <strong>Note that this implementation is not synchronized.</strong>
 * Due to data caching used for exploiting locality of reference, performance can decrease if BigList is
 * accessed by several threads at different positions.<p>
 *
 * Note that the iterators provided are not fail-fast.<p>
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class BigList<E> extends IList<E> {

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
	// public static BigList<E> EMPTY = BigList.create().unmodifiableList();
	// Syntax error:
	// public static <EE> BigList<EE> EMPTY = BigList.create().unmodifiableList();

	/** Unmodifiable empty instance */
	@SuppressWarnings("rawtypes")
	private static final BigList EMPTY = BigList.create().unmodifiableList();

	/**
	 * @return unmodifiable empty instance
	 */
	@SuppressWarnings("unchecked")
	public static <EE> BigList<EE> EMPTY() {
		return EMPTY;
	}

	/** Number of elements stored at maximum in a block */
	private int blockSize;
	/** Number of elements stored in this BigList */
	private int size;

	/** The root node in the tree */
	private BlockNode<E> rootNode;
	/** Current node */
	private BlockNode<E> currNode;
	/** Block of current node */
	/** Start index of current block */
	private int currBlockStart;
	/** End index of current block */
	private int currBlockEnd;
	/** Modify value which must be applied before this block is not current any more */
	private int currModify;

	/**
	 * Constructor used internally, e.g. for ImmutableBigList.
	 *
	 * @param copy true to copy all instance values from source,
	 *             if false nothing is done
	 * @param that list to copy
	 */
	protected BigList(boolean copy, BigList<E> that) {
		if (copy) {
			this.blockSize = that.blockSize;
			this.currBlockStart = that.currBlockStart;
			this.currBlockEnd = that.currBlockEnd;
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
	 * @param <E>       type of elements stored in the list
	 */
	// This separate method is needed as the varargs variant creates the list with specific size
	public static <E> BigList<E> create() {
		return new BigList<E>();
	}

	/**
	 * Create new list with specified elements.
	 *
	 * @param coll      collection with element
	 * @return          created list
	 * @param <E>       type of elements stored in the list
	 */
	public static <E> BigList<E> create(Collection<? extends E> coll) {
		return new BigList<E>((coll != null) ? coll : Collections.emptyList());
	}

	/**
	 * Create new list with specified elements.
	 *
	 * @param elems 	array with elements
	 * @return 			created list
	 * @param <E> 		type of elements stored in the list
	 */
	public static <E> BigList<E> create(E... elems) {
		BigList<E> list = new BigList<E>();
		if (elems != null) {
			for (E elem : elems) {
				list.add(elem);
			}
		}
		return list;
	}

	/**
	 * Default constructor.
	 * The default block size is used.
	 */
	public BigList() {
		this(DEFAULT_BLOCK_SIZE);
	}

	/**
	 * Constructor.
	 *
	 * @param blockSize block size
	 */
	public BigList(int blockSize) {
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
	@SuppressWarnings("unchecked")
	public BigList(Collection<? extends E> coll) {
		if (coll instanceof BigList) {
			doAssign((BigList<E>) coll);
			doClone((BigList<E>) coll);

		} else {
			blockSize = DEFAULT_BLOCK_SIZE;

			addBlock(0, new Block<E>());

			for (Object obj : coll.toArray()) {
				add((E) obj);
			}
			assert (size() == coll.size());
		}
	}

	/**
	 * Returns block size used for this BigList.
	 *
	 * @return block size used for this BigList
	 */
	public int blockSize() {
		return blockSize;
	}

	/**
	 * Internal constructor.
	 *
	 * @param blockSize			default block size
	 * @param firstBlockSize	block size of first block
	 */
	private BigList(int blockSize, int firstBlockSize) {
		doInit(blockSize, firstBlockSize);
	}

	/**
	 * Initialize BigList.
	 *
	 * @param blockSize			default block size
	 * @param firstBlockSize	block size of first block
	 */
	private void doInit(int blockSize, int firstBlockSize) {
		this.blockSize = blockSize;

		// First block will grow until it reaches blockSize
		Block<E> block;
		if (firstBlockSize <= 1) {
			block = new Block<E>();
		} else {
			block = new Block<E>(firstBlockSize);
		}
		addBlock(0, block);
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
	@SuppressWarnings("unchecked")
	public BigList<E> copy() {
		return (BigList<E>) clone();
	}

	@Override
	public BigList<E> crop() {
		return (BigList<E>) super.crop();
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
		if (this instanceof ImmutableBigList) {
			BigList<E> list = new BigList<>(false, null);
			list.doClone(this);
			return list;
		} else {
			return super.clone();
		}
	}

	@Override
	protected void doAssign(IList<E> that) {
		BigList<E> list = (BigList<E>) that;
		this.blockSize = list.blockSize;
		this.currBlockEnd = list.currBlockEnd;
		this.currBlockStart = list.currBlockStart;
		this.currNode = list.currNode;
		this.rootNode = list.rootNode;
		this.size = list.size;
	}

	@Override
	protected void doClone(IList<E> that) {
		BigList<E> bigList = (BigList<E>) that;
		bigList.releaseBlock();
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
	private BlockNode<E> copy(BlockNode<E> node) {
		BlockNode<E> newNode = node.min();
		int index = newNode.block.size();
		BlockNode<E> newRoot = new BlockNode<E>(null, index, newNode.block.ref(), null, null);
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
	public E getDefaultElem() {
		return null;
	}

	@Override
	protected void finalize() {
		// This list will be garbage collected, so unref all referenced blocks.
		// As it is not reachable by any live objects, if is safe to access it from the GC thread without synchronization
		BlockNode<E> node = rootNode.min();
		while (node != null) {
			node.block.unref();
			node = node.next();
		}
	}

	@Override
	public Spliterator<E> spliterator() {
		return Spliterators.spliterator(this, Spliterator.ORDERED);

		// Note that BigList does not support RandomAccessSpliterator which is used per default.
		// Reason is that BigList.get() will be called on the same list from different threads in parallel.
		// As get() modifies cache information, this will result in MT issues.
		//if (this instanceof RandomAccess) {
		//  return new AbstractList.RandomAccessSpliterator<>(this);
		//} else {
		//	return Spliterators.spliterator(this, Spliterator.ORDERED);
		//}
	}

	@Override
	public int size() {
		return size;
	}

	/**
	 * As BigList grows and shrinks automatically, the term capacity does not really make sense.
	 * Therefore always -1 is returned.
	 */
	@Override
	public int capacity() {
		return -1;
	}

	@Override
	protected E doGet(int index) {
		int pos = getBlockIndex(index, false, 0);
		return currNode.block.doGet(pos);
	}

	@Override
	protected E doSet(int index, E elem) {
		int pos = getBlockIndex(index, true, 0);
		E oldElem = currNode.block.doGet(pos);
		currNode.block.doSet(pos, elem);
		return oldElem;
	}

	@Override
	protected E doReSet(int index, E elem) {
		int pos = getBlockIndex(index, true, 0);
		E oldElem = currNode.block.doGet(pos);
		currNode.block.doSet(pos, elem);
		return oldElem;
	}

	/**
	 * Release current block and apply modification if pending.
	 */
	private void releaseBlock() {
		if (currModify != 0) {
			int modify = currModify;
			currModify = 0;
			modify(currNode, modify);
		}
		currNode = null;
	}

	/**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currBlock to remember this last used block.
	 *
	 * @param index		list index (0 <= index <= size())
	 * @param write		true if the block is needed for a write operation (set, add, remove)
	 * @param modify	modify instruction (N>0: N elements are added, N<0: N elements are removed, 0 no change)
	 * @return			relative index within block
	 */
	private int getBlockIndex(int index, boolean write, int modify) {
		// Determine block where specified index is located and store it in currBlock
		if (currNode != null) {
			if (index >= currBlockStart && (index < currBlockEnd || index == currBlockEnd && size == index)) {
				// currBlock is already set correctly
				if (write) {
					if (currNode.block.isShared()) {
						currNode.block.unref();
						currNode.setBlock(new Block<E>(currNode.block));
					}
				}
				currModify += modify;
				return index - currBlockStart;
			}
			releaseBlock();
		}

		if (index == size) {
			if (currNode == null || currBlockEnd != size) {
				currNode = rootNode.max();
				currBlockEnd = size;
				currBlockStart = size - currNode.block.size();
			}
			if (modify != 0) {
				currNode.relPos += modify;
				BlockNode<E> leftNode = currNode.getLeftSubTree();
				if (leftNode != null) {
					leftNode.relPos -= modify;
				}
			}

		} else if (index == 0) {
			if (currNode == null || currBlockStart != 0) {
				currNode = rootNode.min();
				currBlockEnd = currNode.block.size();
				currBlockStart = 0;
			}
			if (modify != 0) {
				rootNode.relPos += modify;
			}
		}

		if (currNode == null) {
			doGetBlock(index, modify);
		}

		assert (index >= currBlockStart && index <= currBlockEnd);

		if (write) {
			if (currNode.block.isShared()) {
				currNode.block.unref();
				currNode.setBlock(new Block<E>(currNode.block));
			}
		}

		return index - currBlockStart;
	}

	/**
	 * @return true if there is only the root block, false otherwise
	 */
	private boolean isOnlyRootBlock() {
		return rootNode.left == null && rootNode.right == null;
	}

	/**
	 * Determine node/block for the specified index.
	 * The fields currNode, currBlockStart, and currBlockEnd are set.
	 * During the traversing the tree node, the nodes relative positions are changed according to the modify instruction.
	 *
	 * @param index		list index for which block must be determined
	 * @param modify	modify instruction (N>0: N elements are added, N<0: N elements are removed, 0 no change)
	 */
	private void doGetBlock(int index, int modify) {
		currNode = rootNode;
		currBlockEnd = rootNode.relPos;

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

				int leftIndex = currBlockEnd - currNode.block.size();
				assert (leftIndex >= 0);
				if (index >= leftIndex && index < currBlockEnd) {
					// Correct node has been found
					if (modify != 0) {
						BlockNode<E> leftNode = currNode.getLeftSubTree();
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
				BlockNode<E> nextNode;
				if (index < currBlockEnd) {
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
								BlockNode<E> left = currNode.getLeftSubTree();
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
				currBlockEnd += nextNode.relPos;
				currNode = nextNode;
			}
		}
		currBlockStart = currBlockEnd - currNode.block.size();
	}

	/**
	 * Adds a new element to the list.
	 *
	 * @param index  the index to add before
	 * @param obj  the element to add
	 */
	private void addBlock(int index, Block<E> obj) {
		if (rootNode == null) {
			rootNode = new BlockNode<E>(null, index, obj, null, null);
		} else {
			rootNode = rootNode.insert(index, obj);
			rootNode.parent = null;
		}
	}

	@Override
	protected boolean doAdd(int index, E element) {
		if (index == -1) {
			index = size;
		}
		// Insert
		int pos = getBlockIndex(index, true, 1);

		// If there is still place in the current block: insert in current block
		int maxSize = (index == size || index == 0) ? (int) (blockSize * FILL_THRESHOLD) : blockSize;
		// The second part of the condition is a work around to handle the case of insertion as position 0 correctly
		// where blockSize() is 2 (the new block would then be added after the current one)
		if (currNode.block.size() < maxSize || (currNode.block.size() == 1 && currNode.block.size() < blockSize)) {
			currNode.block.doAdd(pos, element);
			currBlockEnd++;

		} else {
			// No place any more in current block
			Block<E> newBlock = new Block<E>(blockSize);
			if (index == size) {
				// Insert new block at tail
				newBlock.doAdd(0, element);
				// Subtract 1 because getBlockIndex() has already added 1
				modify(currNode, -1);
				addBlock(size + 1, newBlock);
				BlockNode<E> lastNode = currNode.next();
				currNode = lastNode;
				currBlockStart = currBlockEnd;
				currBlockEnd++;

			} else if (index == 0) {
				// Insert new block at head
				newBlock.doAdd(0, element);
				// Subtract 1 because getBlockIndex() has already added 1
				modify(currNode, -1);
				addBlock(1, newBlock);
				BlockNode<E> firstNode = currNode.previous();
				currNode = firstNode;
				currBlockStart = 0;
				currBlockEnd = 1;

			} else {
				// Split block for insert
				int nextBlockLen = blockSize / 2;
				int blockLen = blockSize - nextBlockLen;
				GapList.transferRemove(currNode.block, blockLen, nextBlockLen, newBlock, 0, 0);

				// Subtract 1 more because getBlockIndex() has already added 1
				modify(currNode, -nextBlockLen - 1);
				addBlock(currBlockEnd - nextBlockLen, newBlock);

				if (pos < blockLen) {
					// Insert element in first block
					currNode.block.doAdd(pos, element);
					currBlockEnd = currBlockStart + blockLen + 1;
					modify(currNode, 1);
				} else {
					// Insert element in second block
					currNode = currNode.next();
					modify(currNode, 1);
					currNode.block.doAdd(pos - blockLen, element);
					currBlockStart += blockLen;
					currBlockEnd++;
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
	private void modify(BlockNode<E> node, int modify) {
		if (node == currNode) {
			modify += currModify;
			currModify = 0;
		} else {
			releaseBlock();
		}
		if (modify == 0) {
			return;
		}

		if (node.relPos < 0) {
			// Left node
			BlockNode<E> leftNode = node.getLeftSubTree();
			if (leftNode != null) {
				leftNode.relPos -= modify;
			}
			BlockNode<E> pp = node.parent;
			assert (pp.getLeftSubTree() == node);
			boolean parentRight = true;
			while (true) {
				BlockNode<E> p = pp.parent;
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
			BlockNode<E> leftNode = node.getLeftSubTree();
			if (leftNode != null) {
				leftNode.relPos -= modify;
			}
			BlockNode<E> parent = node.parent;
			if (parent != null) {
				assert (parent.getRightSubTree() == node);
				boolean parentLeft = true;
				while (true) {
					BlockNode<E> p = parent.parent;
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

	private BlockNode<E> doRemove(BlockNode<E> node) {
		BlockNode<E> p = node.parent;
		BlockNode<E> newNode = node.removeSelf();
		BlockNode<E> n = newNode;
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
	protected boolean doAddAll(int index, IList<? extends E> list) {
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

		int addPos = getBlockIndex(index, true, 0);
		Block<E> addBlock = currNode.block;
		int space = blockSize - addBlock.size();

		int addLen = list.size();
		if (addLen <= space) {
			// All elements can be added to current block
			currNode.block.addAll(addPos, list);
			modify(currNode, addLen);
			size += addLen;
			currBlockEnd += addLen;

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
					Block<E> nextBlock = new Block<E>(blockSize);
					int add = Math.min(todo, blockSize);
					for (int i = 0; i < add; i++) {
						nextBlock.add(i, list.get(done + i));
					}
					done += add;
					todo -= add;
					addBlock(size + done, nextBlock);
					currNode = currNode.next();
				}

				size += addLen;
				currBlockEnd = size;
				currBlockStart = currBlockEnd - currNode.block.size();

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
					Block<E> nextBlock = new Block<E>(blockSize);
					int add = Math.min(todo, blockSize);
					for (int i = 0; i < add; i++) {
						nextBlock.add(i, list.get(addLen - done - add + i));
					}
					done += add;
					todo -= add;
					addBlock(0, nextBlock);
					currNode = currNode.previous();
				}

				size += addLen;
				currBlockStart = 0;
				currBlockEnd = currNode.block.size();

			} else {
				// Add elements in the middle

				// Split first block to remove tail elements if necessary
				GapList<E> list2 = GapList.create(); // TODO avoid unnecessary copy
				list2.addAll(list);
				int remove = currNode.block.size() - addPos;
				if (remove > 0) {
					list2.addAll(currNode.block.getAll(addPos, remove));
					currNode.block.remove(addPos, remove);
					modify(currNode, -remove);
					size -= remove;
					currBlockEnd -= remove;
				}

				// Calculate how many blocks we need for the elements
				int numElems = currNode.block.size() + list2.size();
				int numBlocks = (numElems - 1) / blockSize + 1;
				assert (numBlocks > 1);

				int has = currNode.block.size();
				int should = numElems / numBlocks;
				int listPos = 0;
				if (has < should) {
					// Elements must be added to first block
					int add = should - has;
					List<? extends E> sublist = list2.getAll(0, add);
					listPos += add;

					currNode.block.addAll(addPos, sublist);
					modify(currNode, add);
					assert (currNode.block.size() == should);
					numElems -= should;
					numBlocks--;
					size += add;
					currBlockEnd += add;

				} else if (has > should) {
					// Elements must be moved from first to second block
					Block<E> nextBlock = new Block<E>(blockSize);
					int move = has - should;
					nextBlock.addAll(currNode.block.getAll(currNode.block.size() - move, move));
					currNode.block.remove(currNode.block.size() - move, move);
					modify(currNode, -move);
					assert (currNode.block.size() == should);
					numElems -= should;
					numBlocks--;
					currBlockEnd -= move;

					should = numElems / numBlocks;
					int add = should - move;
					assert (add >= 0);
					List<? extends E> sublist = list2.getAll(0, add);
					nextBlock.addAll(move, sublist);
					listPos += add;
					assert (nextBlock.size() == should);
					numElems -= should;

					numBlocks--;
					size += add;
					addBlock(currBlockEnd, nextBlock);
					currNode = currNode.next();
					assert (currNode.block == nextBlock);
					assert (currNode.block.size() == add + move);
					currBlockStart = currBlockEnd;
					currBlockEnd += add + move;

				} else {
					// Block already has the correct size
					numElems -= should;
					numBlocks--;
				}
				if (CHECK)
					check();

				while (numBlocks > 0) {
					int add = numElems / numBlocks;
					assert (add > 0);
					List<? extends E> sublist = list2.getAll(listPos, add);
					listPos += add;

					Block<E> nextBlock = new Block<E>();
					nextBlock.addAll(sublist);
					assert (nextBlock.size() == add);
					numElems -= add;
					addBlock(currBlockEnd, nextBlock);
					currNode = currNode.next();
					assert (currNode.block == nextBlock);
					assert (currNode.block.size() == add);
					currBlockStart = currBlockEnd;
					currBlockEnd += add;
					size += add;
					numBlocks--;
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
		currBlockStart = 0;
		currBlockEnd = 0;
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
		int startPos = getBlockIndex(index, true, 0);
		BlockNode<E> startNode = currNode;
		@SuppressWarnings("unused")
		int endPos = getBlockIndex(index + len - 1, true, 0);
		BlockNode<E> endNode = currNode;

		if (startNode == endNode) {
			// Delete from single block
			getBlockIndex(index, true, -len);
			currNode.block.remove(startPos, len);
			if (currNode.block.isEmpty()) {
				BlockNode<E> oldCurrNode = currNode;
				releaseBlock();
				BlockNode<E> node = doRemove(oldCurrNode);
				merge(node);
			} else {
				currBlockEnd -= len;
				merge(currNode);
			}
			size -= len;
		} else {
			// Delete from start block
			if (CHECK)
				check();
			int startLen = startNode.block.size() - startPos;
			getBlockIndex(index, true, -startLen);
			startNode.block.remove(startPos, startLen);
			assert (startNode == currNode);
			if (currNode.block.isEmpty()) {
				releaseBlock();
				doRemove(startNode);
				startNode = null;
			}
			len -= startLen;
			size -= startLen;

			while (len > 0) {
				currNode = null;
				getBlockIndex(index, true, 0);
				int s = currNode.block.size();
				if (s <= len) {
					modify(currNode, -s);
					BlockNode<E> oldCurrNode = currNode;
					releaseBlock();
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
			releaseBlock();
			if (CHECK)
				check();
			getBlockIndex(index, false, 0);
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
	private void merge(BlockNode<E> node) {
		if (node == null) {
			return;
		}

		final int minBlockSize = Math.max((int) (blockSize * MERGE_THRESHOLD), 1);
		if (node.block.size() >= minBlockSize) {
			return;
		}

		BlockNode<E> oldCurrNode = node;
		BlockNode<E> leftNode = node.previous();
		if (leftNode != null && leftNode.block.size() < minBlockSize) {
			// Merge with left block
			int len = node.block.size();
			int dstSize = leftNode.getBlock().size();
			for (int i = 0; i < len; i++) {
				leftNode.block.add(null);
			}
			GapList.transferCopy(node.block, 0, len, leftNode.block, dstSize, len);
			assert (leftNode.block.size() <= blockSize);

			modify(leftNode, +len);
			modify(oldCurrNode, -len);
			releaseBlock();
			doRemove(oldCurrNode);

		} else {
			BlockNode<E> rightNode = node.next();
			if (rightNode != null && rightNode.block.size() < minBlockSize) {
				// Merge with right block
				int len = node.block.size();
				for (int i = 0; i < len; i++) {
					rightNode.block.add(0, null);
				}
				GapList.transferCopy(node.block, 0, len, rightNode.block, 0, len);
				assert (rightNode.block.size() <= blockSize);

				modify(rightNode, +len);
				modify(oldCurrNode, -len);
				releaseBlock();
				doRemove(oldCurrNode);
			}
		}
	}

	@Override
	protected E doRemove(int index) {
		int pos = getBlockIndex(index, true, -1);
		E oldElem = currNode.block.doRemove(pos);
		currBlockEnd--;

		final int minBlockSize = Math.max(blockSize / 3, 1);
		if (currNode.block.size() < minBlockSize) {
			if (currNode.block.size() == 0) {
				if (!isOnlyRootBlock()) {
					BlockNode<E> oldCurrNode = currNode;
					releaseBlock();
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
	public BigList<E> unmodifiableList() {
		// Naming as in java.util.Collections#unmodifiableList
		if (this instanceof ImmutableBigList) {
			return this;
		} else {
			return new ImmutableBigList<E>(this);
		}
	}

	@Override
	protected void doEnsureCapacity(int minCapacity) {
		if (isOnlyRootBlock()) {
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

		if (isOnlyRootBlock()) {
			rootNode.block.trimToSize();
		} else {
			BigList<E> newList = new BigList<E>(blockSize);
			BlockNode<E> node = rootNode.min();
			while (node != null) {
				newList.addAll(node.block);
				remove(0, node.block.size());
				node = node.next();
			}
			doAssign(newList);
		}
	}

	@Override
	protected IList<E> doCreate(int capacity) {
		if (capacity <= blockSize) {
			return new BigList<E>(this.blockSize);
		} else {
			return new BigList<E>(this.blockSize, capacity);
		}
	}

	@Override
	public void sort(int index, int len, Comparator<? super E> comparator) {
		checkRange(index, len);

		if (isOnlyRootBlock()) {
			rootNode.block.sort(index, len, comparator);
		} else {
			MergeSort.sort(this, comparator, index, index + len);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
		checkRange(index, len);

		if (isOnlyRootBlock()) {
			return rootNode.block.binarySearch(key, comparator);
		} else {
			return Collections.binarySearch((List<K>) this, key, comparator);
		}
	}

	// --- Serialization ---

	/**
	 * Serialize a BigList object.
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
			oos.writeObject(doGet(i));
		}
	}

	/**
	 * Deserialize a BigList object.
	 *
	 * @param ois  input stream for serialization
	 * @throws 	   IOException if serialization fails
	 * @throws 	   ClassNotFoundException if serialization fails
	 */
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		int blockSize = ois.readInt();
		int size = ois.readInt();
		int firstBlockSize = (size <= blockSize) ? size : -1;
		doInit(blockSize, firstBlockSize);

		for (int i = 0; i < size; i++) {
			add((E) ois.readObject());
		}
	}

	// --- Debug checks ---

	private void checkNode(BlockNode<E> node) {
		assert ((node.block.size() > 0 || node == rootNode) && node.block.size() <= blockSize);
		BlockNode<E> child = node.getLeftSubTree();
		assert (child == null || child.parent == node);
		child = node.getRightSubTree();
		assert (child == null || child.parent == node);
	}

	private void checkHeight(BlockNode<E> node) {
		BlockNode<E> left = node.getLeftSubTree();
		BlockNode<E> right = node.getRightSubTree();
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
			assert (currBlockStart >= 0 && currBlockEnd <= size && currBlockStart <= currBlockEnd);
			assert (currBlockStart + currNode.block.size() == currBlockEnd);
		}

		if (rootNode == null) {
			assert (size == 0);
			return;
		}

		checkHeight(rootNode);

		BlockNode<E> oldCurrNode = currNode;
		int oldCurrModify = currModify;
		if (currModify != 0) {
			currNode = null;
			currModify = 0;
			modify(oldCurrNode, oldCurrModify);
		}

		BlockNode<E> node = rootNode;
		checkNode(node);
		int index = node.relPos;
		while (node.left != null) {
			node = node.left;
			checkNode(node);
			assert (node.relPos < 0);
			index += node.relPos;
		}
		Block<E> block = node.getBlock();
		assert (block.size() == index);
		int lastIndex = index;

		while (lastIndex < size()) {
			node = rootNode;
			index = node.relPos;
			int searchIndex = lastIndex + 1;
			while (true) {
				checkNode(node);
				block = node.getBlock();
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
			block = node.getBlock();
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

	// --- Block ---

	/**
	 * A block stores in maximum blockSize number of elements.
	 * The first block in a BigList will grow until reaches this limit, all other blocks are directly
	 * allocated with a capacity of blockSize.
	 * A block maintains a reference count which allows a block to be shared among different BigList
	 * instances with a copy-on-write approach.
	 */
	@SuppressWarnings("serial")
	static class Block<T> extends GapList<T> {
		private AtomicInteger refCount = new AtomicInteger(1);

		public Block() {
		}

		public Block(int capacity) {
			super(capacity);
		}

		public Block(Block<T> that) {
			super(that.capacity());
			addAll(that);
		}

		/**
		 * @return true if block is shared by several BigList instances
		 */
		public boolean isShared() {
			return refCount.get() > 1;
		}

		/**
		 * Increment reference count as block is used by one BigList instance more.
		 */
		public Block<T> ref() {
			refCount.incrementAndGet();
			return this;
		}

		/**
		 * Decrement reference count as block is no longer used by one BigList instance.
		 */
		public void unref() {
			refCount.decrementAndGet();
		}

	}

	// --- BlockNode ---

	/**
	 * Implements an AVLNode storing a Block.
	 * The nodes don't know the index of the object they are holding. They do know however their
	 * position relative to their parent node. This allows to calculate the index of a node while traversing the tree.
	 * There is a faedelung flag for both the left and right child
	 * to indicate if they are a child (false) or a link as in linked list (true).
	 */
	static class BlockNode<E> {
		/** Pointer to parent node (null for root) */
		BlockNode<E> parent;
		/** The left child node or the predecessor if {@link #leftIsPrevious}.*/
		BlockNode<E> left;
		/** Flag indicating that left reference is not a subtree but the predecessor. */
		boolean leftIsPrevious;
		/** The right child node or the successor if {@link #rightIsNext}. */
		BlockNode<E> right;
		/** Flag indicating that right reference is not a subtree but the successor. */
		boolean rightIsNext;
		/** How many levels of left/right are below this one. */
		int height;
		/** Relative position of node relative to its parent, root holds absolute position. */
		int relPos;
		/** The stored block */
		Block<E> block;

		/**
		 * Constructs a new node.
		 *
		 * @param parent			parent node (null for root)
		 * @param relativePosition  the relative position of the node (absolute position for root)
		 * @param block				the block to store
		 * @param rightFollower 	the node following this one
		 * @param leftFollower 		the node leading this one
		 */
		private BlockNode(BlockNode<E> parent, int relPos, Block<E> block, BlockNode<E> rightFollower, BlockNode<E> leftFollower) {
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
		private Block<E> getBlock() {
			return block;
		}

		/**
		 * Sets block to store by this node.
		 *
		 * @param block  the block to store
		 */
		private void setBlock(Block<E> block) {
			this.block = block;
		}

		/**
		 * Gets the next node in the list after this one.
		 *
		 * @return the next node
		 */
		private BlockNode<E> next() {
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
		private BlockNode<E> previous() {
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
		private BlockNode<E> insert(int index, Block<E> obj) {
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
		private BlockNode<E> insertOnLeft(int relIndex, Block<E> obj) {
			if (getLeftSubTree() == null) {
				int pos;
				if (relPos >= 0) {
					pos = -relPos;
				} else {
					pos = -block.size();
				}
				setLeft(new BlockNode<E>(this, pos, obj, this, left), null);
			} else {
				setLeft(left.insert(relIndex, obj), null);
			}
			if (relPos >= 0) {
				relPos += obj.size();
			}
			BlockNode<E> ret = balance();
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
		private BlockNode<E> insertOnRight(int relIndex, Block<E> obj) {
			if (getRightSubTree() == null) {
				setRight(new BlockNode<E>(this, obj.size(), obj, right, this), null);
			} else {
				setRight(right.insert(relIndex, obj), null);
			}
			if (relPos < 0) {
				relPos -= obj.size();
			}
			BlockNode<E> ret = balance();
			recalcHeight();
			return ret;
		}

		/**
		 * Gets the left node, returning null if its a faedelung.
		 */
		private BlockNode<E> getLeftSubTree() {
			return leftIsPrevious ? null : left;
		}

		/**
		 * Gets the right node, returning null if its a faedelung.
		 */
		private BlockNode<E> getRightSubTree() {
			return rightIsNext ? null : right;
		}

		/**
		 * Gets the rightmost child of this node.
		 *
		 * @return the rightmost child (greatest index)
		 */
		private BlockNode<E> max() {
			return getRightSubTree() == null ? this : right.max();
		}

		/**
		 * Gets the leftmost child of this node.
		 *
		 * @return the leftmost child (smallest index)
		 */
		private BlockNode<E> min() {
			return getLeftSubTree() == null ? this : left.min();
		}

		private BlockNode<E> removeMax() {
			if (getRightSubTree() == null) {
				return removeSelf();
			}
			setRight(right.removeMax(), right.right);
			recalcHeight();
			return balance();
		}

		private BlockNode<E> removeMin(int size) {
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
		private BlockNode<E> removeSelf() {
			BlockNode<E> p = parent;
			BlockNode<E> n = doRemoveSelf();
			if (n != null) {
				assert (p != n);
				n.parent = p;
			}
			return n;
		}

		private BlockNode<E> doRemoveSelf() {
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
				final BlockNode<E> rightMin = right.min();
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
				final BlockNode<E> leftMax = left.max();
				block = leftMax.block;
				if (rightIsNext) {
					right = leftMax.right;
				}
				final BlockNode<E> leftPrevious = left.left;
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
		private BlockNode<E> balance() {
			switch (heightRightMinusLeft()) {
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
		private int getOffset(BlockNode<E> node) {
			if (node == null) {
				return 0;
			}
			return node.relPos;
		}

		/**
		 * Sets the relative position.
		 */
		private int setOffset(BlockNode<E> node, int newOffest) {
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
			height = Math.max(
					getLeftSubTree() == null ? -1 : getLeftSubTree().height,
					getRightSubTree() == null ? -1 : getRightSubTree().height) + 1;
		}

		/**
		 * Returns the height of the node or -1 if the node is null.
		 */
		private int getHeight(final BlockNode<E> node) {
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
		private BlockNode<E> rotateLeft() {
			assert (!rightIsNext);
			final BlockNode<E> newTop = right; // can't be faedelung!
			final BlockNode<E> movedNode = getRightSubTree().getLeftSubTree();

			final int newTopPosition = relPos + getOffset(newTop);
			final int myNewPosition = -newTop.relPos;
			final int movedPosition = getOffset(newTop) + getOffset(movedNode);

			BlockNode<E> p = this.parent;
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
		private BlockNode<E> rotateRight() {
			assert (!leftIsPrevious);
			final BlockNode<E> newTop = left; // can't be faedelung
			final BlockNode<E> movedNode = getLeftSubTree().getRightSubTree();

			final int newTopPosition = relPos + getOffset(newTop);
			final int myNewPosition = -newTop.relPos;
			final int movedPosition = getOffset(newTop) + getOffset(movedNode);

			BlockNode<E> p = this.parent;
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
		private void setLeft(BlockNode<E> node, BlockNode<E> previous) {
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
		private void setRight(BlockNode<E> node, BlockNode<E> next) {
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
			return new StringBuilder()
					.append("BlockNode(")
					.append(relPos)
					.append(',')
					.append(getRightSubTree() != null)
					.append(',')
					.append(block)
					.append(',')
					.append(getRightSubTree() != null)
					.append(", height ")
					.append(height)
					.append(" )")
					.toString();
		}
	}

	// --- ImmutableBigList ---

	/**
	 * An immutable version of a BigList.
	 * Note that the client cannot change the list,
	 * but the content may change if the underlying list is changed.
	 */
	protected static class ImmutableBigList<E> extends BigList<E> {

		/** UID for serialization */
		private static final long serialVersionUID = -1352274047348922584L;

		/**
		 * Private constructor used internally.
		 *
		 * @param that  list to create an immutable view of
		 */
		protected ImmutableBigList(BigList<E> that) {
			super(true, that);
		}

		@Override
		protected boolean doAdd(int index, E elem) {
			error();
			return false;
		}

		@Override
		protected E doSet(int index, E elem) {
			error();
			return null;
		}

		@Override
		protected E doReSet(int index, E elem) {
			error();
			return null;
		}

		@Override
		protected E doRemove(int index) {
			error();
			return null;
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
