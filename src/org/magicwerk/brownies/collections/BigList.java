package org.magicwerk.brownies.collections;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * The first block (GapList) used grows dynamcically, all others
 * are allocated with fixed size. This is necessary to prevent starving
 * because of GC usage.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class BigList<T>
    extends IGapList<T>
    implements List<T>, Deque<T> {

    /**
	 *
	 * @param <T>
	 */
	@SuppressWarnings("serial")
	static class BigListBlock<T> {
		private GapList<T> values;
		private int refCount;

		public BigListBlock() {
			values = new GapList<T>();
			refCount = 1;
		}

		public BigListBlock(int capacity) {
			values = new GapList<T>(capacity);
			refCount = 1;
		}

		public BigListBlock(BigListBlock<T> that) {
			values = new GapList<T>(that.values.capacity());
			values.addAll(that.values);
			refCount = 1;
		}

		public boolean isShared() {
			return refCount > 1;
		}

		public void ref() {
			refCount++;
		}

		public void unref() {
			refCount--;
		}

		public int size() {
			return values.size();
		}

		public T get(int index) {
			return values.get(index);
		}

		public void set(int index, T elem) {
			values.set(index, elem);
		}

		public void add(int index, T elem) {
			values.add(index, elem);
		}

		public T remove(int index) {
			return values.remove(index);
		}
	}

	/** Set to true for debugging during developping */
	public static final boolean TRACE = false;
	public static final boolean CHECK = false;
	public static final boolean DUMP = false;

	/** Default block size */
	private static int BLOCK_SIZE = 100*1000;

	/** List with block containing the elements */
	private GapList<BigListBlock<T>> blocks = new GapList<BigListBlock<T>>();
	/** Number of elements stored at maximum in a block */
	private int blockSize;
	/** Number of elements stored in this BigList */
	private int size;
	/** Index of current block */
	private transient BigListBlock<T> currBlock;
	/** Index of current block */
	private transient int currBlockIndex;
	/** Index of first element in currBlock for the whole BigList */
	private transient int currBlockStart;
	/** Index of last element in currBlock for the whole BigList */
	private transient int currBlockEnd;

	/**
	 * Default constructor.
	 * The default block size is used.
	 */
	public BigList() {
		this(BLOCK_SIZE);
	}

	/**
	 * Constructor.
	 *
	 * @param blockSize block size
	 */
	public BigList(int blockSize) {
		this.blockSize = blockSize;

		// First block will grow until it reaches blockSize
		blocks = new GapList<BigListBlock<T>>();
		currBlock = new BigListBlock<T>();
		blocks.add(currBlock);
	}

    public BigList(BigList<T> that) {
        blockSize = that.blockSize;

		blocks = new GapList<BigListBlock<T>>(that.blocks);
		for (BigListBlock block: blocks) {
			block.ref();
		}
		size = that.size;

		currBlock = blocks.get(0);
		currBlockIndex = 0;
		currBlockStart = 0;
		currBlockEnd = currBlock.size();
    }

    public BigList(Collection<T> that) {
        blockSize = BLOCK_SIZE;

		blocks = new GapList<BigListBlock<T>>();
		currBlock = new BigListBlock<T>();
		blocks.add(currBlock);

        for (Object elem: that.toArray()) {
            add((T) elem);
        }
        assert(size() == that.size());
    }

    private void check() {
    	assert(currBlockStart >= 0 && currBlockEnd <= size && currBlockStart <= currBlockEnd);
    	assert(currBlockEnd - currBlockStart <= blockSize);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public T get(int index) {
		if (index < 0 || index >= size) {
			throw new IllegalArgumentException();
		}
		int pos = getBlockIndex(index);
		return currBlock.get(pos);
	}

	@Override
	public T set(int index, T element) {
		if (index < 0 || index >= size) {
			throw new IllegalArgumentException();
		}

		int pos = getBlockIndexWrite(index);
		BigListBlock<T> block = blocks.get(currBlockIndex);
		T oldElem = block.get(pos);
		block.set(pos, element);
		return oldElem;
	}

	/**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currBlock to remember this last used block.
	 * If the block is shared, a copy is made and used for modifications.
	 *
	 * @param index
	 * @return
	 */
	private int getBlockIndexWrite(int index) {
		int i = getBlockIndex(index);
		if (currBlock.isShared()) {
			currBlock.unref();
			currBlock = new BigListBlock<T>(currBlock);
			blocks.set(currBlockIndex, currBlock);
		}
		return i;
	}

	/**
	 * Returns index in block where the element with specified index is located.
	 * This method also sets currBlock to remember this last used block.
	 *
	 * @param index
	 * @return
	 */
	private int getBlockIndex(int index) {
		// Determine block where specified index is located and store it in currBlock
		if (index >= currBlockStart && index < currBlockEnd) {
			// currBlock is already set correctly
			return index - currBlockStart;
		}

        // Add at end
        if (index == size) {
        	if (currBlockEnd == index) {
                if (currBlockEnd-currBlockStart == blockSize) {
                	currBlock = new BigListBlock<T>(blockSize);
                    blocks.addLast(currBlock);
                    currBlockIndex = blocks.size()-1;
                    currBlockEnd = size;
                    currBlockStart = size;
                    return 0;
                } else {
                	return index - currBlockStart;
                }
        	}
            currBlockIndex = blocks.size()-1;
    		currBlock = blocks.get(currBlockIndex);
            currBlockEnd = size;
            currBlockStart = size - currBlock.size();
            return index - currBlockStart;

        } else if (index == 0) {
        	if (currBlockStart == 0) {
                if (currBlockEnd-currBlockStart == blockSize) {
                	currBlock = new BigListBlock<T>(blockSize);
                    blocks.addFirst(currBlock);
                    currBlockIndex = 0;
                    currBlockEnd = 0;
                    currBlockStart = 0;
                    return 0;
                } else {
                	return 0;
                }
        	}
            currBlockIndex = 0;
    		currBlock = blocks.get(0);
            currBlockEnd = currBlock.size();
            currBlockStart = 0;
            return 0;
        }

		boolean up;
		if (index < currBlockStart) {
		    if (index < currBlockStart/2) {
		        // search from start to current block
                currBlockIndex = 0;
	    		currBlock = blocks.get(currBlockIndex);
		        currBlockStart = 0;
		        currBlockEnd = currBlock.size();
                up = true;
		    } else {
		        // search from current block to start
		        up = false;
		    }
		} else {
		    int right = size - currBlockEnd;
		    if (index < currBlockEnd+right/2) {
		        // search from current block to end
		        up = true;
		    } else {
		        // search from end to current block
		        currBlockIndex = blocks.size()-1;
	    		currBlock = blocks.get(currBlockIndex);
		        currBlockEnd = size;
		        currBlockStart = size - currBlock.size();
		        up = false;
		    }
		}

		while (true) {
	        if (index >= currBlockStart && index < currBlockEnd) {
	            return index - currBlockStart;
	        }
		    if (up) {
		        currBlockStart = currBlockEnd;
		        currBlockIndex++;
	    		currBlock = blocks.get(currBlockIndex);
                currBlockEnd += currBlock.size();
		    } else {
		        currBlockEnd = currBlockStart;
		        currBlockIndex--;
	    		currBlock = blocks.get(currBlockIndex);
                currBlockStart -= currBlock.size();
		    }
		}
	}

	@Override
	protected boolean doAdd(int index, T element) {
		int pos = getBlockIndexWrite(index);

		// Insert in current block
		if (currBlock.size() < blockSize) {
			currBlock.add(pos, element);
			currBlockEnd++;

		} else {
			BigListBlock<T> block2 = new BigListBlock<T>(blockSize);
			blocks.add(currBlockIndex+1, block2);
			int len = blockSize/2;
			int len2 = currBlock.size()-len;
			for (int i=0; i<len2; i++) {
			    block2.values.add(null); // TODO Add method to GapList
			}
			GapList.copy(currBlock.values, len, block2.values, 0, len2);
			currBlock.values.remove(len, len2);

			if (pos < len) {
				// Insert element in first block
				currBlockEnd -= len2;
				currBlock.add(pos, element);
				currBlockEnd++;
			} else {
				// Insert element in second block
				currBlockIndex++;
				currBlock = blocks.get(currBlockIndex);
				currBlockStart += len;
				pos -= len;
				block2.add(pos, element);
				currBlockEnd++;
			}
		}
		size++;

		if (DUMP) dump();
		if (CHECK) check();

		return true;
	}

	@Override
	protected void doRemoveAll(int index, int len) {
		// Remove whole blocks
		while (index <= currBlockStart && index+len >= currBlockEnd) {
			int currBlockSize = currBlock.size();
			size -= currBlockSize;
			len -= currBlockSize;
			blocks.remove(currBlockIndex);
			if (currBlockIndex < blocks.size()) {
				currBlock = blocks.get(currBlockIndex);
				currBlockEnd = currBlockStart + currBlock.size();
			} else {
				currBlockIndex--;
				currBlock = blocks.get(currBlockIndex);
				currBlockEnd = size;
				currBlockStart = currBlockEnd - currBlock.size();
			}
		}

		// Remove remaining elements
		for (int i=0; i<len; i++) {
			doRemove(index);
		}
	}

	protected T doRemove(int index) {
		int pos = getBlockIndexWrite(index);
		T oldElem = currBlock.remove(pos);
		currBlockEnd--;
		size--;

		if (currBlock.size() < blockSize/3) {
			BigListBlock<T> leftBlock;
			if (currBlockIndex > 0 && blocks.get(currBlockIndex-1).size() < blockSize/3) {
				// Merge with left block
			    int len = currBlock.size();
			    int dstSize = blocks.get(currBlockIndex-1).size();
	            for (int i=0; i<len; i++) {
	                blocks.get(currBlockIndex-1).values.add(null); // TODO Add method to GapList
	            }
				GapList.copy(currBlock.values, 0, blocks.get(currBlockIndex-1).values, dstSize, len);
				blocks.remove(currBlockIndex);
				currBlockIndex--;
				currBlock = blocks.get(currBlockIndex);
				currBlockStart -= dstSize;
			} else if (currBlockIndex < blocks.size()-1 && blocks.get(currBlockIndex+1).size() < blockSize/3) {
				// Merge with right block
			    int len = blocks.get(currBlockIndex+1).values.size();
			    int dstSize = currBlock.values.size();
	            for (int i=0; i<len; i++) {
	            	currBlock.values.add(null); // TODO Add method to GapList
	            }
				GapList.copy(blocks.get(currBlockIndex+1).values, 0, currBlock.values, dstSize, len);
				blocks.remove(currBlockIndex+1);
				currBlockEnd += len;
			}
		}

		if (DUMP) dump();
		if (CHECK) check();

		return oldElem;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		for (int i=0; i<size(); i++) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(get(i));
		}
		buf.append("]");
		return buf.toString();
	}

	private void dump() {
		//LOG.debug(PrintTools.print(blockEndIndex));
		//LOG.debug(PrintTools.print(blockModCount));
		//LOG.debug(toString());
	}

    public Object[] toArray() {
        Object[] array = new Object[size];
        for (int i=0; i<size(); i++) {
            array[i] = get(i);
        }
        return array;
    }

    public void sort(Comparator<? super T> comparator) {
    	if (size < blockSize) {
    		assert(blocks.size() == 1);
    		blocks.get(0).values.sort(comparator);
    	} else {
    		try {
    			//LOG.debug("normal sort");
    			T[] data = (T[]) toArray();
    			Arrays.sort(data, comparator);
    			for (int i=0; i<size; i++) {
    				set(i, data[i]);
    			}
    		}
    		catch (OutOfMemoryError e) {
    			//LOG.debug("inplace sort");
    			new InternalSort<T>(this, comparator).sort();
    		}
    	}
    }

	/**/

	@Override
	public Iterator<T> descendingIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IGapList<T> unmodifiableList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initClone(IGapList<T> that) {
		// TODO Auto-generated method stub

	}

	@Override
	public int capacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected T doGet(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected T doSet(int index, T elem) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected T doReSet(int index, T elem) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doEnsureCapacity(int minCapacity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void trimToSize() {
		// TODO Auto-generated method stub

	}

	@Override
	protected <T> void doGetAll(T[] array, int index, int len) {
		// TODO Auto-generated method stub

	}

	@Override
	public IGapList<T> doCreate(int capacity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sort(int index, int len, Comparator<? super T> comparator) {
		// TODO Auto-generated method stub

	}

	@Override
	public <K> int binarySearch(int index, int len, K key, Comparator<? super K> comparator) {
		// TODO Auto-generated method stub
		return 0;
	}

}
