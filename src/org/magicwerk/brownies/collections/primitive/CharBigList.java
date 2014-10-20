package org.magicwerk.brownies.collections.primitive;
import org.magicwerk.brownies.collections.helper.ArraysHelper;
import org.magicwerk.brownies.collections.helper.primitive.CharBinarySearch;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.BigList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.magicwerk.brownies.collections.helper.primitive.CharMergeSort;

/**
 * CharBigList is a list optimized for storing large number of elements.
 * It stores the elements in fixed size blocks and the blocks itself are maintained in a tree for fast access.
 * It also offers specialized methods for bulk processing of elements.
 * Also copying a CharBigList is efficiently possible as its implemented using a copy-on-write approach.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * Due to data caching used for exploiting locality of reference, performance can decrease if CharBigList is
 * accessed by several threads at different positions.
 * </p>
 *
 * @author Thomas Mauch
 * @version $Id: CharBigList.java 2522 2014-10-17 12:08:38Z origo $
 */
/**
 *
 *
 * @author Thomas Mauch
 * @version $Id: CharBigList.java 2522 2014-10-17 12:08:38Z origo $
 */
public class CharBigList extends ICharList {
	public static ICharList of(char[] values) {
		return new ImmutableCharListArrayPrimitive(values);
	}

	public static ICharList of(Character[] values) {
		return new ImmutableCharListArrayWrapper(values);
	}

	public static ICharList of(List<Character> values) {
		return new ImmutableCharListList(values);
	}

    static class ImmutableCharListArrayPrimitive extends ImmutableCharList {
    	char[] values;

    	public ImmutableCharListArrayPrimitive(char[] values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		protected char doGet(int index) {
			return values[index];
		}
    }

    static class ImmutableCharListArrayWrapper extends ImmutableCharList {
    	Character[] values;

    	public ImmutableCharListArrayWrapper(Character[] values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		protected char doGet(int index) {
			return values[index];
		}
    }

    static class ImmutableCharListList extends ImmutableCharList {
    	List<Character> values;

    	public ImmutableCharListList(List<Character> values) {
    		this.values = values;
    	}

		@Override
		public int size() {
			return values.size();
		}

		@Override
		protected char doGet(int index) {
			return values.get(index);
		}
    }

    protected static abstract class ImmutableCharList extends ICharList {

    	//-- Readers

		@Override
		public int capacity() {
			return size();
		}

		@Override
		public int binarySearch(int index, int len, char key) {
			return CharBinarySearch.binarySearch(this, key, index, index+len);
		}

		@Override
		public ICharList unmodifiableList() {
			return this;
		}

		@Override
		protected char getDefaultElem() {
			return (char) 0;
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
		protected void doClone(ICharList that) {
			error();
		}

		@Override
		protected char doSet(int index, char elem) {
			error();
			return (char) 0;
		}

		@Override
		protected char doReSet(int index, char elem) {
			error();
			return (char) 0;
		}

		@Override
		protected boolean doAdd(int index, char elem) {
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
		protected ICharList doCreate(int capacity) {
			error();
			return null;
		}

		@Override
		protected void doAssign(ICharList that) {
			error();
		}

		@Override
		protected char doRemove(int index) {
			error();
			return (char) 0;
		}

		@Override
		public void sort(int index, int len) {
			error();
		}
    }

    /** UID for serialization */
    private static final long serialVersionUID = 3715838828540564836L;

    /** Default block size */
    private static int BLOCK_SIZE = 1000;

    /** Set to true for debugging during developing */
    private static final boolean CHECK = false;

    // -- EMPTY --  
    // Cannot make a static reference to the non-static type E:  
    // public static CharBigList EMPTY = CharBigList.create().unmodifiableList();  
    // Syntax error:  
    // public static  CharBigList EMPTY = CharBigList.create().unmodifiableList();  
    /** Unmodifiable empty instance */
    
    private static final CharBigList EMPTY = CharBigList.create().unmodifiableList();

    /**
     * @return unmodifiable empty instance
     */

public static  CharBigList EMPTY() {
    return EMPTY;
}

    /** Number of elements stored at maximum in a block */
    private int blockSize;

    /** Number of elements stored in this CharBigList */
    private int size;

    /** The root node in the tree */
    private CharBlockNode root;

    /** Current node */
    private CharBlockNode currNode;

    /** CharBlock of current node */
    private CharBlock currCharBlock;

    /** Start index of current block */
    private int currCharBlockStart;

    /** End index of current block */
    private int currCharBlockEnd;

    /** Modify value which must be applied before this block is not current any more */
    private int currModify;

    /**
     * Constructor used internally, e.g. for ImmutableCharBigList.
     *
     * @param copy true to copy all instance values from source,
     *             if false nothing is done
     * @param that list to copy
     */
protected CharBigList(boolean copy, CharBigList that){
    if (copy) {
        this.blockSize = that.blockSize;
        this.currCharBlock = that.currCharBlock;
        this.currCharBlockStart = that.currCharBlockStart;
        this.currCharBlockEnd = that.currCharBlockEnd;
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
// This separate method is needed as the varargs variant creates the list with specific size  
public static CharBigList create() {
    return new CharBigList();
}

    /**
     * Create new list with specified elements.
     *
     * @param coll      collection with element
     * @return          created list
     * @param        type of elements stored in the list
     */
public static CharBigList create(Collection<Character> coll) {
    return new CharBigList(coll);
}

    /**
	 * Create new list with specified elements.
	 *
	 * @param elems 	array with elements
	 * @return 			created list
	 * @param  		type of elements stored in the list
	 */
public static CharBigList create(char... elems) {
    CharBigList list = new CharBigList();
    for (char elem : elems) {
        list.add(elem);
    }
    return list;
}

    /**
	 * Default constructor.
	 * The default block size is used.
	 */
public CharBigList(){
    this(BLOCK_SIZE);
}

    /**
	 * Constructor.
	 *
	 * @param blockSize block size
	 */
public CharBigList(int blockSize){
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
public CharBigList(Collection<Character> coll){
    if (coll instanceof CharBigList) {
        doAssign((CharBigList) coll);
        doClone((CharBigList) coll);
    } else {
        blockSize = BLOCK_SIZE;
        currCharBlock = new CharBlock();
        addCharBlock(0, currCharBlock);
        for (Object obj : coll.toArray()) {
            add((Character) obj);
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
public void init(char... elems) {
    clear();
    for (char elem : elems) {
        add(elem);
    }
}

    /**
     * Initialize the list to have all elements in the specified collection.
     *
     * @param that	collection
     */
public void init(Collection<Character> that) {
    clear();
    addAll(that);
}

    /**
     * Returns block size used for this CharBigList.
     *
     * @return block size used for this CharBigList
     */
public int blockSize() {
    return blockSize;
}

    /**
	 * Internal constructor.
	 *
	 * @param blockSize			default block size
	 * @param firstCharBlockSize	block size of first block
	 */
private CharBigList(int blockSize, int firstCharBlockSize){
    doInit(blockSize, firstCharBlockSize);
}

    /**
	 * Initialize CharBigList.
	 *
	 * @param blockSize			default block size
	 * @param firstCharBlockSize	block size of first block
	 */
private void doInit(int blockSize, int firstCharBlockSize) {
    this.blockSize = blockSize;
    // First block will grow until it reaches blockSize   
    if (firstCharBlockSize <= 1) {
        currCharBlock = new CharBlock();
    } else {
        currCharBlock = new CharBlock(firstCharBlockSize);
    }
    addCharBlock(0, currCharBlock);
}

    /**
     * Returns a copy of this <tt>CharBigList</tt> instance.
     * The copy is realized by a copy-on-write approach so also really large lists can efficiently be copied.
     * This method is identical to clone() except that the result is casted to CharBigList.
     *
     * @return a copy of this <tt>CharBigList</tt> instance
	 */
@Override
public CharBigList copy() {
    return (CharBigList) super.copy();
}

    /**
     * Returns a shallow copy of this <tt>CharBigList</tt> instance
     * The copy is realized by a copy-on-write approach so also really large lists can efficiently be copied.
     *
     * @return a copy of this <tt>CharBigList</tt> instance
     */
// Only overridden to change Javadoc  
@Override
public Object clone() {
    return super.clone();
}

    @Override
protected void doAssign(ICharList that) {
    CharBigList list = (CharBigList) that;
    this.blockSize = list.blockSize;
    this.currCharBlock = list.currCharBlock;
    this.currCharBlockEnd = list.currCharBlockEnd;
    this.currCharBlockStart = list.currCharBlockStart;
    this.currNode = list.currNode;
    this.root = list.root;
    this.size = list.size;
}

    @Override
protected void doClone(ICharList that) {
    CharBigList bigList = (CharBigList) that;
    bigList.releaseCharBlock();
    root = copy(bigList.root);
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
private CharBlockNode copy(CharBlockNode node) {
    CharBlockNode newNode = node.min();
    int index = newNode.block.size();
    CharBlockNode newRoot = new CharBlockNode(null, index, newNode.block.ref(), null, null);
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
public char getDefaultElem() {
    return (char) 0;
}

    @Override
protected void finalize() {
    // This list will be garbage collected, so unref all referenced blocks   
    CharBlockNode node = root.min();
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
	 * For CharBigList, always -1 is returned.
	 */
@Override
public int capacity() {
    return -1;
}

    @Override
protected char doGet(int index) {
    int pos = getCharBlockIndex(index, false, 0);
    return currCharBlock.values.doGet(pos);
}

    @Override
protected char doSet(int index, char elem) {
    int pos = getCharBlockIndex(index, true, 0);
    char oldElem = currCharBlock.values.doGet(pos);
    currCharBlock.values.doSet(pos, elem);
    return oldElem;
}

    @Override
protected char doReSet(int index, char elem) {
    int pos = getCharBlockIndex(index, true, 0);
    char oldElem = currCharBlock.values.doGet(pos);
    currCharBlock.values.doSet(pos, elem);
    return oldElem;
}

    /**
	 * Release current block and apply modification if pending.
	 */
private void releaseCharBlock() {
    if (currModify != 0) {
        int modify = currModify;
        currModify = 0;
        modify(currNode, modify);
    }
    currNode = null;
}

    /**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currCharBlock to remember this last used block.
	 *
	 * @param index	list index (0 <= index <= size())
	 * @return		relative index within block
	 */
private int getCharBlockIndex(int index, boolean write, int modify) {
    // Determine block where specified index is located and store it in currCharBlock   
    if (currNode != null) {
        if (index >= currCharBlockStart && (index < currCharBlockEnd || index == currCharBlockEnd && size == index)) {
            // currCharBlock is already set correctly   
            if (write) {
                if (currCharBlock.isShared()) {
                    currCharBlock.unref();
                    currCharBlock = new CharBlock(currCharBlock);
                    currNode.setCharBlock(currCharBlock);
                }
            }
            currModify += modify;
            return index - currCharBlockStart;
        }
        releaseCharBlock();
    }
    boolean done = false;
    if (index == size) {
        if (currNode == null || currCharBlockEnd != size) {
            currNode = root.max();
            currCharBlock = currNode.getCharBlock();
            currCharBlockEnd = size;
            currCharBlockStart = size - currCharBlock.size();
        }
        if (modify != 0) {
            currNode.relativePosition += modify;
            CharBlockNode leftNode = currNode.getLeftSubTree();
            if (leftNode != null) {
                leftNode.relativePosition -= modify;
            }
        }
        done = true;
    } else if (index == 0) {
        if (currNode == null || currCharBlockStart != 0) {
            currNode = root.min();
            currCharBlock = currNode.getCharBlock();
            currCharBlockEnd = currCharBlock.size();
            currCharBlockStart = 0;
        }
        if (modify != 0) {
            root.relativePosition += modify;
        }
        done = true;
    }
    if (!done) {
        // Reset currCharBlockEnd, it will be then set by access()   
        currCharBlockEnd = 0;
        currNode = doGetCharBlock(index, modify);
        currCharBlock = currNode.getCharBlock();
        currCharBlockStart = currCharBlockEnd - currCharBlock.size();
    }
    assert (index >= currCharBlockStart && index <= currCharBlockEnd);
    if (write) {
        if (currCharBlock.isShared()) {
            currCharBlock.unref();
            currCharBlock = new CharBlock(currCharBlock);
            currNode.setCharBlock(currCharBlock);
        }
    }
    return index - currCharBlockStart;
}

    /**
	 * @return true if there is only the root block, false otherwise
	 */
private boolean isOnlyRootCharBlock() {
    return root.left == null && root.right == null;
}

    private CharBlockNode doGetCharBlock(int index, int modify) {
    return root.access(this, index, modify, false);
}

    /**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */
private void addCharBlock(int index, CharBlock obj) {
    if (root == null) {
        root = new CharBlockNode(null, index, obj, null, null);
    } else {
        root = root.insert(index, obj);
        root.parent = null;
    }
}

    @Override
protected boolean doAdd(int index, char element) {
    if (index == -1) {
        index = size;
    }
    // Insert   
    int pos = getCharBlockIndex(index, true, 1);
    // If there is still place in the current block: insert in current block   
    int maxSize = (index == size || index == 0) ? blockSize * 9 / 10 : blockSize;
    // The second part of the condition is a work around to handle the case of insertion as position 0 correctly   
    // where blockSize() is 2 (the new block would then be added after the current one)   
    if (currCharBlock.size() < maxSize || (currCharBlock.size() == 1 && currCharBlock.size() < blockSize)) {
        currCharBlock.values.doAdd(pos, element);
        currCharBlockEnd++;
    } else {
        // No place any more in current block   
        CharBlock newCharBlock = new CharBlock(blockSize);
        if (index == size) {
            // Insert new block at tail   
            newCharBlock.values.doAdd(0, element);
            // Subtract 1 because getCharBlockIndex() has already added 1   
            modify(currNode, -1);
            addCharBlock(size + 1, newCharBlock);
            CharBlockNode lastNode = currNode.next();
            currNode = lastNode;
            currCharBlock = currNode.block;
            currCharBlockStart = currCharBlockEnd;
            currCharBlockEnd++;
        } else if (index == 0) {
            // Insert new block at head   
            newCharBlock.values.doAdd(0, element);
            // Subtract 1 because getCharBlockIndex() has already added 1   
            modify(currNode, -1);
            addCharBlock(1, newCharBlock);
            CharBlockNode firstNode = currNode.previous();
            currNode = firstNode;
            currCharBlock = currNode.block;
            currCharBlockStart = 0;
            currCharBlockEnd = 1;
        } else {
            // Split block for insert   
            int nextCharBlockLen = blockSize / 2;
            int blockLen = blockSize - nextCharBlockLen;
            newCharBlock.values.init(nextCharBlockLen, (char) 0);
            CharGapList.copy(currCharBlock.values, blockLen, newCharBlock.values, 0, nextCharBlockLen);
            currCharBlock.values.remove(blockLen, blockSize - blockLen);
            // Subtract 1 more because getCharBlockIndex() has already added 1   
            modify(currNode, -nextCharBlockLen - 1);
            addCharBlock(currCharBlockEnd - nextCharBlockLen, newCharBlock);
            if (pos < blockLen) {
                // Insert element in first block   
                currCharBlock.values.doAdd(pos, element);
                currCharBlockEnd = currCharBlockStart + blockLen + 1;
                modify(currNode, 1);
            } else {
                // Insert element in second block   
                currNode = currNode.next();
                modify(currNode, 1);
                currCharBlock = currNode.block;
                currCharBlock.values.doAdd(pos - blockLen, element);
                currCharBlockStart += blockLen;
                currCharBlockEnd++;
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
private void modify(CharBlockNode node, int modify) {
    if (node == currNode) {
        modify += currModify;
        currModify = 0;
    } else {
        releaseCharBlock();
    }
    if (modify == 0) {
        return;
    }
    if (node.relativePosition < 0) {
        // Left node   
        CharBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        CharBlockNode pp = node.parent;
        assert (pp.getLeftSubTree() == node);
        boolean parentRight = true;
        while (true) {
            CharBlockNode p = pp.parent;
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
        CharBlockNode leftNode = node.getLeftSubTree();
        if (leftNode != null) {
            leftNode.relativePosition -= modify;
        }
        CharBlockNode parent = node.parent;
        if (parent != null) {
            assert (parent.getRightSubTree() == node);
            boolean parentLeft = true;
            while (true) {
                CharBlockNode p = parent.parent;
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

    private CharBlockNode doRemove(CharBlockNode node) {
    CharBlockNode p = node.parent;
    CharBlockNode newNode = node.removeSelf();
    CharBlockNode n = newNode;
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
protected boolean doAddAll(int index, char[] array) {
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
    int addPos = getCharBlockIndex(index, true, 0);
    CharBlock addCharBlock = currCharBlock;
    int space = blockSize - addCharBlock.size();
    int addLen = array.length;
    if (addLen <= space) {
        // All elements can be added to current block   
        currCharBlock.values.addAll(addPos, array);
        modify(currNode, addLen);
        size += addLen;
        currCharBlockEnd += addLen;
    } else {
        if (index == size) {
            // Add elements at end   
            for (int i = 0; i < space; i++) {
                currCharBlock.values.add(addPos + i, array[i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                CharBlock nextCharBlock = new CharBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextCharBlock.values.add(i, array[done + i]);
                }
                done += add;
                todo -= add;
                addCharBlock(size + done, nextCharBlock);
                currNode = currNode.next();
            }
            size += addLen;
            currCharBlock = currNode.block;
            currCharBlockEnd = size;
            currCharBlockStart = currCharBlockEnd - currCharBlock.size();
        } else if (index == 0) {
            // Add elements at head   
            assert (addPos == 0);
            for (int i = 0; i < space; i++) {
                currCharBlock.values.add(addPos + i, array[addLen - space + i]);
            }
            modify(currNode, space);
            int done = space;
            int todo = addLen - space;
            while (todo > 0) {
                CharBlock nextCharBlock = new CharBlock(blockSize);
                int add = Math.min(todo, blockSize);
                for (int i = 0; i < add; i++) {
                    nextCharBlock.values.add(i, array[addLen - done - add + i]);
                }
                done += add;
                todo -= add;
                addCharBlock(0, nextCharBlock);
                currNode = currNode.previous();
            }
            size += addLen;
            currCharBlock = currNode.block;
            currCharBlockStart = 0;
            currCharBlockEnd = currCharBlock.size();
        } else {
            // Add elements in the middle   
            // Split first block to remove tail elements if necessary   
            CharGapList list = CharGapList.create(array);
            int remove = currCharBlock.values.size() - addPos;
            if (remove > 0) {
                list.addAll(currCharBlock.values.getAll(addPos, remove));
                currCharBlock.values.remove(addPos, remove);
                modify(currNode, -remove);
                size -= remove;
                currCharBlockEnd -= remove;
            }
            // Calculate how many blocks we need for the elements   
            int numElems = currCharBlock.values.size() + list.size();
            int numCharBlocks = (numElems - 1) / blockSize + 1;
            assert (numCharBlocks > 1);
            int has = currCharBlock.values.size();
            int should = numElems / numCharBlocks;
            int listPos = 0;
            if (has < should) {
                // Elements must be added to first block   
                int add = should - has;
                ICharList sublist = list.getAll(0, add);
                listPos += add;
                currCharBlock.values.addAll(addPos, sublist);
                modify(currNode, add);
                assert (currCharBlock.values.size() == should);
                numElems -= should;
                numCharBlocks--;
                size += add;
                currCharBlockEnd += add;
            } else if (has > should) {
                // Elements must be moved from first to second block   
                CharBlock nextCharBlock = new CharBlock(blockSize);
                int move = has - should;
                nextCharBlock.values.addAll(currCharBlock.values.getAll(currCharBlock.values.size() - move, move));
                currCharBlock.values.remove(currCharBlock.values.size() - move, move);
                modify(currNode, -move);
                assert (currCharBlock.values.size() == should);
                numElems -= should;
                numCharBlocks--;
                currCharBlockEnd -= move;
                should = numElems / numCharBlocks;
                int add = should - move;
                assert (add >= 0);
                ICharList sublist = list.getAll(0, add);
                nextCharBlock.values.addAll(move, sublist);
                listPos += add;
                assert (nextCharBlock.values.size() == should);
                numElems -= should;
                numCharBlocks--;
                size += add;
                addCharBlock(currCharBlockEnd, nextCharBlock);
                currNode = currNode.next();
                currCharBlock = currNode.block;
                assert (currCharBlock == nextCharBlock);
                assert (currCharBlock.size() == add + move);
                currCharBlockStart = currCharBlockEnd;
                currCharBlockEnd += add + move;
            } else {
                // CharBlock already has the correct size   
                numElems -= should;
                numCharBlocks--;
            }
            if (CHECK)
                check();
            while (numCharBlocks > 0) {
                int add = numElems / numCharBlocks;
                assert (add > 0);
                ICharList sublist = list.getAll(listPos, add);
                listPos += add;
                CharBlock nextCharBlock = new CharBlock();
                nextCharBlock.values.addAll(sublist);
                assert (nextCharBlock.values.size() == add);
                numElems -= add;
                addCharBlock(currCharBlockEnd, nextCharBlock);
                currNode = currNode.next();
                currCharBlock = currNode.block;
                assert (currCharBlock == nextCharBlock);
                assert (currCharBlock.size() == add);
                currCharBlockStart = currCharBlockEnd;
                currCharBlockEnd += add;
                size += add;
                numCharBlocks--;
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
    root = null;
    currCharBlock = null;
    currCharBlockStart = 0;
    currCharBlockEnd = 0;
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
    int startPos = getCharBlockIndex(index, true, 0);
    CharBlockNode startNode = currNode;
    int endPos = getCharBlockIndex(index + len - 1, true, 0);
    CharBlockNode endNode = currNode;
    if (startNode == endNode) {
        // Delete from single block   
        getCharBlockIndex(index, true, -len);
        currCharBlock.values.remove(startPos, len);
        if (currCharBlock.values.isEmpty()) {
            CharBlockNode oldCurrNode = currNode;
            releaseCharBlock();
            CharBlockNode node = doRemove(oldCurrNode);
            merge(node);
        } else {
            currCharBlockEnd -= len;
            merge(currNode);
        }
        size -= len;
    } else {
        // Delete from start block   
        if (CHECK)
            check();
        int startLen = startNode.block.size() - startPos;
        getCharBlockIndex(index, true, -startLen);
        // TODO should that be modify?   
        startNode.block.values.remove(startPos, startLen);
        assert (startNode == currNode);
        if (currCharBlock.values.isEmpty()) {
            releaseCharBlock();
            doRemove(startNode);
            startNode = null;
        }
        len -= startLen;
        size -= startLen;
        while (len > 0) {
            currNode = null;
            getCharBlockIndex(index, true, 0);
            int s = currCharBlock.size();
            if (s <= len) {
                modify(currNode, -s);
                CharBlockNode oldCurrNode = currNode;
                releaseCharBlock();
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
                currCharBlock.values.remove(0, len);
                size -= len;
                break;
            }
        }
        releaseCharBlock();
        if (CHECK)
            check();
        getCharBlockIndex(index, false, 0);
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
private void merge(CharBlockNode node) {
    if (node == null) {
        return;
    }
    final int minCharBlockSize = Math.max(blockSize / 3, 1);
    if (node.block.values.size() >= minCharBlockSize) {
        return;
    }
    CharBlockNode oldCurrNode = node;
    CharBlockNode leftNode = node.previous();
    if (leftNode != null && leftNode.block.size() < minCharBlockSize) {
        // Merge with left block   
        int len = node.block.size();
        int dstSize = leftNode.getCharBlock().size();
        for (int i = 0; i < len; i++) {
            leftNode.block.values.add((char) 0);
        }
        CharGapList.copy(node.block.values, 0, leftNode.block.values, dstSize, len);
        assert (leftNode.block.values.size() <= blockSize);
        modify(leftNode, +len);
        modify(oldCurrNode, -len);
        releaseCharBlock();
        doRemove(oldCurrNode);
    } else {
        CharBlockNode rightNode = node.next();
        if (rightNode != null && rightNode.block.size() < minCharBlockSize) {
            // Merge with right block   
            int len = node.block.size();
            for (int i = 0; i < len; i++) {
                rightNode.block.values.add(0, (char) 0);
            }
            CharGapList.copy(node.block.values, 0, rightNode.block.values, 0, len);
            assert (rightNode.block.values.size() <= blockSize);
            modify(rightNode, +len);
            modify(oldCurrNode, -len);
            releaseCharBlock();
            doRemove(oldCurrNode);
        }
    }
}

    protected char doRemove(int index) {
    int pos = getCharBlockIndex(index, true, -1);
    char oldElem = currCharBlock.values.doRemove(pos);
    currCharBlockEnd--;
    final int minCharBlockSize = Math.max(blockSize / 3, 1);
    if (currCharBlock.size() < minCharBlockSize) {
        if (currCharBlock.size() == 0) {
            if (!isOnlyRootCharBlock()) {
                CharBlockNode oldCurrNode = currNode;
                releaseCharBlock();
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
public CharBigList unmodifiableList() {
    // Naming as in java.util.Collections#unmodifiableList   
    return new ImmutableCharBigList(this);
}

    @Override
protected void doEnsureCapacity(int minCapacity) {
    if (isOnlyRootCharBlock()) {
        if (minCapacity > blockSize) {
            minCapacity = blockSize;
        }
        currCharBlock.values.doEnsureCapacity(minCapacity);
    }
}

    /**
     * Pack as many elements in the blocks as allowed.
     * An application can use this operation to minimize the storage of an instance.
     */
@Override
public void trimToSize() {
    doModify();
    if (isOnlyRootCharBlock()) {
        currCharBlock.values.trimToSize();
    } else {
        CharBigList newList = new CharBigList(blockSize);
        CharBlockNode node = root.min();
        while (node != null) {
            newList.addAll(node.block.values);
            remove(0, node.block.values.size());
            node = node.next();
        }
        doAssign(newList);
    }
}

    @Override
protected ICharList doCreate(int capacity) {
    if (capacity <= blockSize) {
        return new CharBigList(this.blockSize);
    } else {
        return new CharBigList(this.blockSize, capacity);
    }
}

    @Override
public void sort(int index, int len) {
    checkRange(index, len);
    if (isOnlyRootCharBlock()) {
        currCharBlock.values.sort(index, len);
    } else {
        CharMergeSort.sort(this, index, index + len);
    }
}

    @Override
public int binarySearch(int index, int len, char key) {
    checkRange(index, len);
    if (isOnlyRootCharBlock()) {
        return currCharBlock.values.binarySearch(key);
    } else {
        return CharBinarySearch.binarySearch(this, key, 0, size());
    }
}

    // --- Serialization ---  
/**
     * Serialize a CharBigList object.
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
        oos.writeChar(doGet(i));
    }
}

    /**
     * Deserialize a CharBigList object.
 	 *
     * @param ois  input stream for serialization
     * @throws 	   IOException if serialization fails
     * @throws 	   ClassNotFoundException if serialization fails
     */

private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    int blockSize = ois.readInt();
    int size = ois.readInt();
    int firstCharBlockSize = (size <= blockSize) ? size : -1;
    doInit(blockSize, firstCharBlockSize);
    for (int i = 0; i < size; i++) {
        add(ois.readChar());
    }
}

    // --- Debug checks ---  
private void checkNode(CharBlockNode node) {
    assert ((node.block.size() > 0 || node == root) && node.block.size() <= blockSize);
    CharBlockNode child = node.getLeftSubTree();
    assert (child == null || child.parent == node);
    child = node.getRightSubTree();
    assert (child == null || child.parent == node);
}

    private void checkHeight(CharBlockNode node) {
    CharBlockNode left = node.getLeftSubTree();
    CharBlockNode right = node.getRightSubTree();
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
        assert (currNode.block == currCharBlock);
        assert (currCharBlockStart >= 0 && currCharBlockEnd <= size && currCharBlockStart <= currCharBlockEnd);
        assert (currCharBlockStart + currCharBlock.size() == currCharBlockEnd);
    }
    if (root == null) {
        assert (size == 0);
        return;
    }
    checkHeight(root);
    CharBlockNode oldCurrNode = currNode;
    int oldCurrModify = currModify;
    if (currModify != 0) {
        currNode = null;
        currModify = 0;
        modify(oldCurrNode, oldCurrModify);
    }
    CharBlockNode node = root;
    checkNode(node);
    int index = node.relativePosition;
    while (node.left != null) {
        node = node.left;
        checkNode(node);
        assert (node.relativePosition < 0);
        index += node.relativePosition;
    }
    CharBlock block = node.getCharBlock();
    assert (block.size() == index);
    int lastIndex = index;
    while (lastIndex < size()) {
        node = root;
        index = node.relativePosition;
        int searchIndex = lastIndex + 1;
        while (true) {
            checkNode(node);
            block = node.getCharBlock();
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
        block = node.getCharBlock();
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

    // --- CharBlock ---  
    /**
	 * A block stores in maximum blockSize number of elements.
	 * The first block in a CharBigList will grow until reaches this limit, all other blocks are directly
	 * allocated with a capacity of blockSize.
	 * A block maintains a reference count which allows a block to be shared among different CharBigList
	 * instances with a copy-on-write approach.
	 */
    
    public static class CharBlock implements Serializable {

        private CharGapList values;

        private int refCount;

        public CharBlock(){
    values = new CharGapList();
    refCount = 1;
}

        public CharBlock(int capacity){
    values = new CharGapList(capacity);
    refCount = 1;
}

        public CharBlock(CharBlock that){
    values = new CharGapList(that.values.capacity());
    values.init(that.values.getArray(0, that.values.size()));
    refCount = 1;
}

        /**
		 * @return true if block is shared by several CharBigList instances
		 */
public boolean isShared() {
    return refCount > 1;
}

        /**
		 * Increment reference count as block is used by one CharBigList instance more.
		 */
public CharBlock ref() {
    refCount++;
    return this;
}

        /**
		 * Decrement reference count as block is no longer used by one CharBigList instance.
		 */
public void unref() {
    refCount--;
}

        /**
		 * @return number of elements stored in this block
		 */
public int size() {
    return values.size();
}

        @Override
public String toString() {
    return values.toString();
}
    }

    // --- CharBlockNode ---  
    /**
     * Implements an AVLNode storing a CharBlock.
     * The nodes don't know the index of the object they are holding. They do know however their
     * position relative to their parent node. This allows to calculate the index of a node while traversing the tree.
     * There is a faedelung flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    static class CharBlockNode {

        /** Pointer to parent node (null for root) */
        CharBlockNode parent;

        /** The left child node or the predecessor if {@link #leftIsPrevious}.*/
        CharBlockNode left;

        /** Flag indicating that left reference is not a subtree but the predecessor. */
        boolean leftIsPrevious;

        /** The right child node or the successor if {@link #rightIsNext}. */
        CharBlockNode right;

        /** Flag indicating that right reference is not a subtree but the successor. */
        boolean rightIsNext;

        /** How many levels of left/right are below this one. */
        int height;

        /** The relative position, root holds absolute position. */
        int relativePosition;

        /** The stored block */
        CharBlock block;

        /**
         * Constructs a new node.
         *
         * @param parent			parent node (null for root)
         * @param relativePosition  the relative position of the node (absolute position for root)
         * @param block				the block to store
         * @param rightFollower 	the node following this one
         * @param leftFollower 		the node leading this one
         */
private CharBlockNode(CharBlockNode parent, int relativePosition, CharBlock block, CharBlockNode rightFollower, CharBlockNode leftFollower){
    this.parent = parent;
    this.relativePosition = relativePosition;
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
public CharBlock getCharBlock() {
    return block;
}

        /**
         * Sets block to store by this node.
         *
         * @param block  the block to store
         */
public void setCharBlock(CharBlock block) {
    this.block = block;
}

        /**
         * Retrieves node with specified index.
         *
         * @param list		reference to CharBigList using this node (used for updating currCharBlockEnd)
         * @param index		index to retrieve
         * @param modify	modification to apply during traversal to relative positions <br/>
         * 					>0: N elements are added at index, <0: N elements are deleted at index, 0: no change
         * @param wasLeft	last node was a left child
         * @return
         */
private CharBlockNode access(CharBigList list, int index, int modify, boolean wasLeft) {
    assert (index >= 0);
    if (relativePosition == 0) {
        if (modify != 0) {
            relativePosition += modify;
        }
        return this;
    }
    if (list.currCharBlockEnd == 0) {
        list.currCharBlockEnd = relativePosition;
    }
    int leftIndex = list.currCharBlockEnd - block.size();
    assert (leftIndex >= 0);
    if (index >= leftIndex && index < list.currCharBlockEnd) {
        // Correct node has been found   
        CharBlockNode leftNode = getLeftSubTree();
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
    // Further traversal needed to find the correct node   
    if (index < list.currCharBlockEnd) {
        // Travese the left node   
        CharBlockNode nextNode = getLeftSubTree();
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
        list.currCharBlockEnd += nextNode.relativePosition;
        return nextNode.access(list, index, modify, wasLeft);
    } else {
        // Traverse the right node   
        CharBlockNode nextNode = getRightSubTree();
        if (nextNode == null || wasLeft) {
            if (relativePosition > 0) {
                relativePosition += modify;
                CharBlockNode left = getLeftSubTree();
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
        list.currCharBlockEnd += nextNode.relativePosition;
        return nextNode.access(list, index, modify, wasLeft);
    }
}

        /**
         * Gets the next node in the list after this one.
         *
         * @return the next node
         */
public CharBlockNode next() {
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
public CharBlockNode previous() {
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
private CharBlockNode insert(int index, CharBlock obj) {
    assert (relativePosition != 0);
    final int indexRelativeToMe = index - relativePosition;
    if (indexRelativeToMe < 0) {
        return insertOnLeft(indexRelativeToMe, obj);
    } else {
        return insertOnRight(indexRelativeToMe, obj);
    }
}

        private CharBlockNode insertOnLeft(int indexRelativeToMe, CharBlock obj) {
    if (getLeftSubTree() == null) {
        int pos;
        if (relativePosition >= 0) {
            pos = -relativePosition;
        } else {
            pos = -block.size();
        }
        setLeft(new CharBlockNode(this, pos, obj, this, left), null);
    } else {
        setLeft(left.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition >= 0) {
        relativePosition += obj.size();
    }
    final CharBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        private CharBlockNode insertOnRight(int indexRelativeToMe, CharBlock obj) {
    if (getRightSubTree() == null) {
        setRight(new CharBlockNode(this, obj.size(), obj, right, this), null);
    } else {
        setRight(right.insert(indexRelativeToMe, obj), null);
    }
    if (relativePosition < 0) {
        relativePosition -= obj.size();
    }
    final CharBlockNode ret = balance();
    recalcHeight();
    return ret;
}

        /**
         * Gets the left node, returning null if its a faedelung.
         */
public CharBlockNode getLeftSubTree() {
    return leftIsPrevious ? null : left;
}

        /**
         * Gets the right node, returning null if its a faedelung.
         */
public CharBlockNode getRightSubTree() {
    return rightIsNext ? null : right;
}

        /**
         * Gets the rightmost child of this node.
         *
         * @return the rightmost child (greatest index)
         */
public CharBlockNode max() {
    return getRightSubTree() == null ? this : right.max();
}

        /**
         * Gets the leftmost child of this node.
         *
         * @return the leftmost child (smallest index)
         */
public CharBlockNode min() {
    return getLeftSubTree() == null ? this : left.min();
}

        private CharBlockNode removeMax() {
    if (getRightSubTree() == null) {
        return removeSelf();
    }
    setRight(right.removeMax(), right.right);
    recalcHeight();
    return balance();
}

        private CharBlockNode removeMin(int size) {
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
public CharBlockNode removeSelf() {
    CharBlockNode p = parent;
    CharBlockNode n = doRemoveSelf();
    if (n != null) {
        assert (p != n);
        n.parent = p;
    }
    return n;
}

        public CharBlockNode doRemoveSelf() {
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
        final CharBlockNode rightMin = right.min();
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
        final CharBlockNode leftMax = left.max();
        block = leftMax.block;
        if (rightIsNext) {
            right = leftMax.right;
        }
        final CharBlockNode leftPrevious = left.left;
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

        /**
         * Balances according to the AVL algorithm.
         */
private CharBlockNode balance() {
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
private int getOffset(CharBlockNode node) {
    if (node == null) {
        return 0;
    }
    return node.relativePosition;
}

        /**
         * Sets the relative position.
         */
private int setOffset(CharBlockNode node, int newOffest) {
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
private int getHeight(final CharBlockNode node) {
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
private CharBlockNode rotateLeft() {
    assert (!rightIsNext);
    final CharBlockNode newTop = right;
    // can't be faedelung!   
    final CharBlockNode movedNode = getRightSubTree().getLeftSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    CharBlockNode p = this.parent;
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

        /**
         * Rotate tree to the right using this node as center.
         *
         * @return node which will take the place of this node
         */
private CharBlockNode rotateRight() {
    assert (!leftIsPrevious);
    final CharBlockNode newTop = left;
    // can't be faedelung   
    final CharBlockNode movedNode = getLeftSubTree().getRightSubTree();
    final int newTopPosition = relativePosition + getOffset(newTop);
    final int myNewPosition = -newTop.relativePosition;
    final int movedPosition = getOffset(newTop) + getOffset(movedNode);
    CharBlockNode p = this.parent;
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
private void setLeft(CharBlockNode node, CharBlockNode previous) {
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
private void setRight(CharBlockNode node, CharBlockNode next) {
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
    return new StringBuilder().append("CharBlockNode(").append(relativePosition).append(',').append(getRightSubTree() != null).append(',').append(block).append(',').append(getRightSubTree() != null).append(", height ").append(height).append(" )").toString();
}
    }

    // --- ImmutableCharBigList ---  
    /**
     * An immutable version of a CharBigList.
     * Note that the client cannot change the list,
     * but the content may change if the underlying list is changed.
     */
    protected static class ImmutableCharBigList extends CharBigList {

        /** UID for serialization */
        private static final long serialVersionUID = -1352274047348922584L;

        /**
         * Private constructor used internally.
         *
         * @param that  list to create an immutable view of
         */
protected ImmutableCharBigList(CharBigList that){
    super(true, that);
}

        @Override
protected boolean doAdd(int index, char elem) {
    error();
    return false;
}

        @Override
protected boolean doAddAll(int index, char[] elems) {
    error();
    return false;
}

        @Override
protected char doSet(int index, char elem) {
    error();
    return (char) 0;
}

        @Override
protected void doSetAll(int index, char[] elems) {
    error();
}

        @Override
protected char doReSet(int index, char elem) {
    error();
    return (char) 0;
}

        @Override
protected char doRemove(int index) {
    error();
    return (char) 0;
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
	public static CharBigList create(String str) {
		return new CharBigList(str);
	}

	public CharBigList(String str) {
		init(str);
	}

	public void init(String str) {
		char[] array = str.toCharArray();
		init(array);
	}
}
