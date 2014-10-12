package org.magicwerk.brownies.collections.primitive;
import org.magicwerk.brownies.collections.helper.ArraysHelper;
import org.magicwerk.brownies.collections.helper.primitive.CharBinarySearch;
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
import org.magicwerk.brownies.collections.helper.primitive.CharMergeSort;

/**
 * The first block (CharGapList) used grows dynamcically, all others
 * are allocated with fixed size. This is necessary to prevent starving
 * because of GC usage.
 *
 * @author Thomas Mauch
 * @version $Id: CharBigList.java 2493 2014-10-12 00:40:31Z origo $
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

    ;

    /**
	 *
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

        public boolean isShared() {
    return refCount > 1;
}

        public CharBlock ref() {
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
// This separate method is needed as the varargs variant creates the CharGapList with specific size  
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

    public CharBigList(Collection<Character> that){
    if (that instanceof CharBigList) {
        doAssign((CharBigList) that);
        doClone((CharBigList) that);
    } else {
        blockSize = BLOCK_SIZE;
        currCharBlock = new CharBlock();
        addCharBlock(0, currCharBlock);
        for (Object obj : that.toArray()) {
            add((Character) obj);
        }
        assert (size() == that.size());
    }
}

    public void init() {
    clear();
}

    public void init(char... elems) {
    clear();
    for (char elem : elems) {
        add(elem);
    }
}

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

    //---  
private CharBigList(int blockSize, int firstCharBlockSize){
    doInit(blockSize, firstCharBlockSize);
}

    void doInit(int blockSize, int firstCharBlockSize) {
    this.blockSize = blockSize;
    // First block will grow until it reaches blockSize   
    if (firstCharBlockSize <= 1) {
        currCharBlock = new CharBlock();
    } else {
        currCharBlock = new CharBlock(firstCharBlockSize);
    }
    addCharBlock(0, currCharBlock);
}

    @Override
public CharBigList copy() {
    return (CharBigList) super.copy();
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
    bigList.check();
    bigList.releaseCharBlock();
    root = copy(bigList.root);
    currNode = null;
    currModify = 0;
    check();
}

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
        currNode = access(index, modify);
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

    void checkNode(CharBlockNode node) {
    assert ((node.block.size() > 0 || node == root) && node.block.size() <= blockSize);
    CharBlockNode child = node.getLeftSubTree();
    assert (child == null || child.parent == node);
    child = node.getRightSubTree();
    assert (child == null || child.parent == node);
}

    void checkHeight(CharBlockNode node) {
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

    void check() {
    //if (true) {return; } //TODO   
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
                currCharBlock.values.add(addPos, array[i]);
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
            }
            size += addLen;
            currNode = currNode.next();
            currCharBlock = currNode.block;
            currCharBlockStart = currCharBlockEnd + space;
            currCharBlockEnd = currCharBlockStart + addLen - space;
        } else if (index == 0) {
            // Add elements at head   
            assert (addPos == 0);
            for (int i = 0; i < space; i++) {
                currCharBlock.values.add(addPos, array[addLen - space + i]);
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
                addCharBlock(0, nextCharBlock);
            }
            size += addLen;
            currNode = currNode.previous();
            currCharBlock = currNode.block;
            currCharBlockStart = 0;
            currCharBlockEnd = addLen - space;
        } else {
            // Add elements to several blocks   
            // Handle first block   
            CharGapList list = CharGapList.create(array);
            int remove = currCharBlock.values.size() - addPos;
            if (remove > 0) {
                list.addAll(currCharBlock.values.getAll(addPos, remove));
                currCharBlock.values.remove(addPos, remove);
                modify(currNode, -remove);
                size -= remove;
                currCharBlockEnd -= remove;
            }
            int s = currCharBlock.values.size() + list.size();
            int numCharBlocks = (s - 1) / blockSize + 1;
            assert (numCharBlocks > 1);
            int has = currCharBlock.values.size();
            int should = s / numCharBlocks;
            int start = 0;
            int end = 0;
            if (has < should) {
                // Elements must be added to first block   
                int add = should - has;
                ICharList sublist = list.getAll(0, add);
                currCharBlock.values.addAll(addPos, sublist);
                modify(currNode, add);
                start += add;
                assert (currCharBlock.values.size() == should);
                s -= should;
                numCharBlocks--;
                size += add;
                currCharBlockEnd += add;
                end = currCharBlockEnd;
            } else if (has > should) {
                // Elements must be moved from first to second block   
                CharBlock nextCharBlock = new CharBlock(blockSize);
                int move = has - should;
                nextCharBlock.values.addAll(currCharBlock.values.getAll(currCharBlock.values.size() - move, move));
                currCharBlock.values.remove(currCharBlock.values.size() - move, move);
                modify(currNode, -move);
                assert (currCharBlock.values.size() == should);
                s -= should;
                numCharBlocks--;
                currCharBlockEnd -= move;
                end = currCharBlockEnd;
                should = s / numCharBlocks;
                int add = should - move;
                assert (add >= 0);
                ICharList sublist = list.getAll(0, add);
                nextCharBlock.values.addAll(move, sublist);
                start += add;
                assert (nextCharBlock.values.size() == should);
                s -= should;
                numCharBlocks--;
                size += add;
                end += add;
                addCharBlock(end, nextCharBlock);
            } else {
                s -= should;
                numCharBlocks--;
            }
            check();
            CharBlockNode node = currNode;
            while (numCharBlocks > 0) {
                int add = s / numCharBlocks;
                assert (add > 0);
                ICharList sublist = list.getAll(start, add);
                CharBlock nextCharBlock = new CharBlock();
                nextCharBlock.values.clear();
                nextCharBlock.values.addAll(sublist);
                start += add;
                assert (nextCharBlock.values.size() == add);
                s -= add;
                addCharBlock(end, nextCharBlock);
                assert (node.next().block == nextCharBlock);
                node = node.next();
                end += add;
                size += add;
                numCharBlocks--;
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
    int l = len;
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
        //check();   
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
                check();
            } else {
                modify(currNode, -len);
                currCharBlock.values.remove(0, len);
                size -= len;
                break;
            }
        }
        releaseCharBlock();
        check();
        getCharBlockIndex(index, false, 0);
        merge(currNode);
    }
    if (DUMP)
        dump();
    if (CHECK)
        check();
}

    void merge(CharBlockNode node) {
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

    @Override
public void trimToSize() {
    if (isOnlyRootCharBlock()) {
        currCharBlock.values.trimToSize();
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

    private boolean isOnlyRootCharBlock() {
    return root.left == null && root.right == null;
}

    public CharBlockNode access(final int index, int modify) {
    return root.access(this, index, modify, false);
}

    //-----------------------------------------------------------------------  
/**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */
public void addCharBlock(int index, CharBlock obj) {
    if (root == null) {
        root = new CharBlockNode(null, index, obj, null, null);
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
public void removeCharBlock(int index) {
    root = root.remove(index);
}

    // --- Serialization ---  
/**
     * Serialize a CharBigList object.
     *
     * @serialData The length of the array backing the <tt>CharGapList</tt>
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

    //-----------------------------------------------------------------------  
    /**
     * Implements an AVLNode which keeps the offset updated.
     * <p>
     * This node contains the real work.
     * TreeList is just there to implement {@link java.util.ICharList}.
     * The nodes don't know the index of the object they are holding.  They
     * do know however their position relative to their parent node.
     * This allows to calculate the index of a node while traversing the tree.
     * <p>
     * The Faedelung calculation stores a flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    static class CharBlockNode {

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
         * Constructs a new node with a relative position.
         *
         * @param relativePosition  the relative position of the node
         * @param obj  the value for the node
         * @param rightFollower the node with the value following this one
         * @param leftFollower the node with the value leading this one
         */
private CharBlockNode(CharBlockNode parent, final int relativePosition, final CharBlock block, final CharBlockNode rightFollower, final CharBlockNode leftFollower){
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
public CharBlock getCharBlock() {
    return block;
}

        /**
         * Sets the value.
         *
         * @param obj  the value to store
         */
public void setCharBlock(CharBlock obj) {
    this.block = obj;
}

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
    CharBlockNode leftNode = getLeftSubTree();
    int leftIndex = list.currCharBlockEnd - block.size();
    assert (leftIndex >= 0);
    if (index >= leftIndex && index < list.currCharBlockEnd) {
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
    if (index < list.currCharBlockEnd) {
        // left   
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
        // right   
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

        //-----------------------------------------------------------------------  
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

        /**
         * Removes the node at a given position.
         *
         * @param index is the index of the element to be removed relative to the position of
         * the parent node of the current node.
         */
private CharBlockNode remove(final int index) {
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

        private CharBlockNode removeMax() {
    if (getRightSubTree() == null) {
        return removeSelf();
    }
    setRight(right.removeMax(), right.right);
    if (relativePosition < 0) {
    }
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

        //-----------------------------------------------------------------------  
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
private int getOffset(final CharBlockNode node) {
    if (node == null) {
        return 0;
    }
    return node.relativePosition;
}

        /**
         * Sets the relative position.
         */
private int setOffset(final CharBlockNode node, final int newOffest) {
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
private void setLeft(final CharBlockNode node, final CharBlockNode previous) {
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
private void setRight(final CharBlockNode node, final CharBlockNode next) {
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
