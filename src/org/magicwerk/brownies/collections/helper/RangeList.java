/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.magicwerk.brownies.collections.helper;

import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javax.xml.soap.Node;

import org.magicwerk.brownies.collections.BigList;
import org.magicwerk.brownies.collections.BigList.Block;

/**
 * A <code>List</code> implementation that is optimised for fast insertions and
 * removals at any index in the list.
 * <p>
 * This list implementation utilises a tree structure internally to ensure that
 * all insertions and removals are O(log n). This provides much faster performance
 * than both an <code>ArrayList</code> and a <code>LinkedList</code> where elements
 * are inserted and removed repeatedly from anywhere in the list.
 * <p>
 * The following relative performance statistics are indicative of this class:
 * <pre>
 *              get  add  insert  iterate  remove
 * TreeList       3    5       1       2       1
 * ArrayList      1    1      40       1      40
 * LinkedList  5800    1     350       2     325
 * </pre>
 * <code>ArrayList</code> is a good general purpose list implementation.
 * It is faster than <code>TreeList</code> for most operations except inserting
 * and removing in the middle of the list. <code>ArrayList</code> also uses less
 * memory as <code>TreeList</code> uses one object per entry.
 * <p>
 * <code>LinkedList</code> is rarely a good choice of implementation.
 * <code>TreeList</code> is almost always a good replacement for it, although it
 * does use slightly more memory.
 *
 * @since 3.1
 * @version $Id: TreeList.java 1533984 2013-10-20 21:12:51Z tn $
 */
//Based on org.apache.commons.collections4.list.TreeList
public class RangeList<E> {

    /** The root node in the AVL tree */
    public AVLNode<E> root;

    /** The current size of the list */
    private int size;

    public AVLNode<E> access(final int index, int modify, int[] endIndex) {
        //checkInterval(index, 0, size() - 1);
        return root.access(index, modify, false, endIndex);
    }

    /**
     * Gets the current size of the list.
     *
     * @return the current size
     */
    //@Override
    public int size() {
        return size;
    }

    //-----------------------------------------------------------------------
    /**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */

    public void add(final int index, final E obj) {
        //modCount++;
        //checkInterval(index, 0, size());
        if (root == null) {
            root = new AVLNode<E>(index, obj, null, null);
        } else {
            root = root.insert(index, obj);
        }
        size++;
    }

    /**
     * Removes the element at the specified index.
     *
     * @param index  the index to remove
     * @return the previous object at that index
     */
    //@Override
    public void remove(final int index) {
        //modCount++;
        //checkInterval(index, 0, size() - 1);
        //final E result = get(index);
        root = root.remove(index);
        size--;
        //return result;
    }

    //-----------------------------------------------------------------------
    /**
     * Implements an AVLNode which keeps the offset updated.
     * <p>
     * This node contains the real work.
     * TreeList is just there to implement {@link java.util.List}.
     * The nodes don't know the index of the object they are holding.  They
     * do know however their position relative to their parent node.
     * This allows to calculate the index of a node while traversing the tree.
     * <p>
     * The Faedelung calculation stores a flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    public static class AVLNode<E> {
        /** The left child node or the predecessor if {@link #leftIsPrevious}.*/
        public AVLNode<E> left;
        /** Flag indicating that left reference is not a subtree but the predecessor. */
        public boolean leftIsPrevious;
        /** The right child node or the successor if {@link #rightIsNext}. */
        public AVLNode<E> right;
        /** Flag indicating that right reference is not a subtree but the successor. */
        public boolean rightIsNext;
        /** How many levels of left/right are below this one. */
        public int height;
        /** The relative position, root holds absolute position. */
        public int relativePosition;
        /** The stored element. */
        private E value;

        /**
         * Constructs a new node with a relative position.
         *
         * @param relativePosition  the relative position of the node
         * @param obj  the value for the node
         * @param rightFollower the node with the value following this one
         * @param leftFollower the node with the value leading this one
         */
        private AVLNode(final int relativePosition, final E obj,
                        final AVLNode<E> rightFollower, final AVLNode<E> leftFollower) {
            this.relativePosition = relativePosition;
            value = obj;
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
        public E getValue() {
            return value;
        }

        /**
         * Sets the value.
         *
         * @param obj  the value to store
         */
        public void setValue(final E obj) {
            this.value = obj;
        }

        private AVLNode<E> access(final int index, int modify, boolean wasLeft, int[] idx) {
        	assert(index >= 0);
        	if (relativePosition == 0) {
        		if (modify != 0) {
        			relativePosition += modify;
        		}
        		return this;
        	}
        	if (idx[0] == 0) {
        		idx[0] = relativePosition; // root
        	}
        	AVLNode<E> leftNode = getLeftSubTree();
        	int leftIndex = idx[0]-((BigList.Block) getValue()).size();
        	assert(leftIndex >= 0);
        	if (index >= leftIndex && index < idx[0]) {
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
        	if (index < idx[0]) {
        		// left
        		AVLNode<E> nextNode = getLeftSubTree();
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
                idx[0] += nextNode.relativePosition;
                return nextNode.access(index, modify, wasLeft, idx);
        	} else {
        		// right
        		AVLNode<E> nextNode = getRightSubTree();
        		if (nextNode == null || wasLeft) {
        			if (relativePosition > 0) {
        				relativePosition += modify;
       		        	AVLNode<E> left = getLeftSubTree();
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
                idx[0] += nextNode.relativePosition;
                return nextNode.access(index, modify, wasLeft, idx);
        	}
        }

        /**
         * Stores the node and its children into the array specified.
         *
         * @param array the array to be filled
         * @param index the index of this node
         */
        void toArray(final Object[] array, final int index) {
            array[index] = value;
            if (getLeftSubTree() != null) {
                left.toArray(array, index + left.relativePosition);
            }
            if (getRightSubTree() != null) {
                right.toArray(array, index + right.relativePosition);
            }
        }

        /**
         * Gets the next node in the list after this one.
         *
         * @return the next node
         */
        public AVLNode<E> next() {
            if (rightIsNext || right == null) {
                return right;
            }
            return right.min();
        }

        public AVLNode<E> parent() {
        	AVLNode prev = this;
        	AVLNode next = this;
        	while (true) {
        		if (prev != null) {
        			prev = prev.previous();
        			if (prev != null && prev.getRightSubTree() == this) {
        				return prev;
        			}
        		}
        		if (next != null) {
	        		next = next.next();
	        		if (next != null && next.getLeftSubTree() == this) {
	        			return next;
	        		}
        		}
        		if (prev == null && next == null) {
        			return null;
        		}
        	}
        }

        /**
         * Gets the node in the list before this one.
         *
         * @return the previous node
         */
        public AVLNode<E> previous() {
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
        private AVLNode<E> insert(final int index, final E obj) {
        	assert(relativePosition != 0);
            final int indexRelativeToMe = index - relativePosition;

            if (indexRelativeToMe < 0) {
                return insertOnLeft(indexRelativeToMe, obj);
            } else {
            	return insertOnRight(indexRelativeToMe, obj);
            }
        }

        private AVLNode<E> insertOnLeft(final int indexRelativeToMe, final E obj) {
            if (getLeftSubTree() == null) {
            	int pos;
            	if (relativePosition >= 0) {
            		pos = -relativePosition;
            	} else {
            		Block b = (Block) value;
            		pos = -b.size();
            		//pos = -1;
            	}
                setLeft(new AVLNode<E>(pos, obj, this, left), null);
            } else {
                setLeft(left.insert(indexRelativeToMe, obj), null);
            }
            if (relativePosition >= 0) {
        		Block b = (Block) obj;
                relativePosition += b.size();
            }
            final AVLNode<E> ret = balance();
            recalcHeight();
            return ret;
        }

        private AVLNode<E> insertOnRight(final int indexRelativeToMe, final E obj) {
            if (getRightSubTree() == null) {
            	Block b = (Block) obj;
                setRight(new AVLNode<E>(b.size(), obj, right, this), null);
            } else {
                setRight(right.insert(indexRelativeToMe, obj), null);
            }
            if (relativePosition < 0) {
        		Block b = (Block) obj;
                relativePosition -= b.size();
            }
            final AVLNode<E> ret = balance();
            recalcHeight();
            return ret;
        }

        //-----------------------------------------------------------------------
        /**
         * Gets the left node, returning null if its a faedelung.
         */
        public AVLNode<E> getLeftSubTree() {
            return leftIsPrevious ? null : left;
        }

        /**
         * Gets the right node, returning null if its a faedelung.
         */
        public AVLNode<E> getRightSubTree() {
            return rightIsNext ? null : right;
        }

        /**
         * Gets the rightmost child of this node.
         *
         * @return the rightmost child (greatest index)
         */
        public AVLNode<E> max() {
            return getRightSubTree() == null ? this : right.max();
        }

        /**
         * Gets the leftmost child of this node.
         *
         * @return the leftmost child (smallest index)
         */
        public AVLNode<E> min() {
            return getLeftSubTree() == null ? this : left.min();
        }

        /**
         * Removes the node at a given position.
         *
         * @param index is the index of the element to be removed relative to the position of
         * the parent node of the current node.
         */
        private AVLNode<E> remove(final int index) {
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

        private AVLNode<E> removeMax() {
            if (getRightSubTree() == null) {
                return removeSelf();
            }
            setRight(right.removeMax(), right.right);
            if (relativePosition < 0) {
                relativePosition++;
            }
            recalcHeight();
            return balance();
        }

        private AVLNode<E> removeMin() {
            if (getLeftSubTree() == null) {
                return removeSelf();
            }
            setLeft(left.removeMin(), left.left);
            if (relativePosition > 0) {
                relativePosition--;
            }
            recalcHeight();
            return balance();
        }

        /**
         * Removes this node from the tree.
         *
         * @return the node that replaces this one in the parent
         */
        private AVLNode<E> removeSelf() {
            if (getRightSubTree() == null && getLeftSubTree() == null) {
                return null;
            }
            if (getRightSubTree() == null) {
                if (relativePosition > 0) {
                    left.relativePosition += relativePosition + (relativePosition > 0 ? 0 : 1);
                }
                left.max().setRight(null, right);
                return left;
            }
            if (getLeftSubTree() == null) {
                right.relativePosition += relativePosition - (relativePosition < 0 ? 0 : 1);
                right.min().setLeft(null, left);
                return right;
            }

            if (heightRightMinusLeft() > 0) {
                // more on the right, so delete from the right
                final AVLNode<E> rightMin = right.min();
                value = rightMin.value;
                if (leftIsPrevious) {
                    left = rightMin.left;
                }
                right = right.removeMin();
                if (relativePosition < 0) {
                    relativePosition++;
                }
            } else {
                // more on the left or equal, so delete from the left
                final AVLNode<E> leftMax = left.max();
                value = leftMax.value;
                if (rightIsNext) {
                    right = leftMax.right;
                }
                final AVLNode<E> leftPrevious = left.left;
                left = left.removeMax();
                if (left == null) {
                    // special case where left that was deleted was a double link
                    // only occurs when height difference is equal
                    left = leftPrevious;
                    leftIsPrevious = true;
                }
                if (relativePosition > 0) {
                    relativePosition--;
                }
            }
            recalcHeight();
            return this;
        }

        //-----------------------------------------------------------------------
        /**
         * Balances according to the AVL algorithm.
         */
        private AVLNode<E> balance() {
            switch (heightRightMinusLeft()) {
                case 1 :
                case 0 :
                case -1 :
                    return this;
                case -2 :
                    if (left.heightRightMinusLeft() > 0) {
                        setLeft(left.rotateLeft(), null);
                    }
                    return rotateRight();
                case 2 :
                    if (right.heightRightMinusLeft() < 0) {
                        setRight(right.rotateRight(), null);
                    }
                    return rotateLeft();
                default :
                    throw new RuntimeException("tree inconsistent!");
            }
        }

        /**
         * Gets the relative position.
         */
        private int getOffset(final AVLNode<E> node) {
            if (node == null) {
                return 0;
            }
            return node.relativePosition;
        }

        /**
         * Sets the relative position.
         */
        private int setOffset(final AVLNode<E> node, final int newOffest) {
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
            height = Math.max(
                getLeftSubTree() == null ? -1 : getLeftSubTree().height,
                getRightSubTree() == null ? -1 : getRightSubTree().height) + 1;
        }

        /**
         * Returns the height of the node or -1 if the node is null.
         */
        private int getHeight(final AVLNode<E> node) {
            return node == null ? -1 : node.height;
        }

        /**
         * Returns the height difference right - left
         */
        private int heightRightMinusLeft() {
            return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
        }

        private AVLNode<E> rotateLeft() {
        	assert(!rightIsNext);
            final AVLNode<E> newTop = right; // can't be faedelung!
            final AVLNode<E> movedNode = getRightSubTree().getLeftSubTree();

            final int newTopPosition = relativePosition + getOffset(newTop);
            final int myNewPosition = -newTop.relativePosition;
            final int movedPosition = getOffset(newTop) + getOffset(movedNode);

            setRight(movedNode, newTop);
            newTop.setLeft(this, null);

            setOffset(newTop, newTopPosition);
            setOffset(this, myNewPosition);
            setOffset(movedNode, movedPosition);

            assert(newTop.getLeftSubTree() == null || newTop.getLeftSubTree().relativePosition < 0);
            assert(newTop.getRightSubTree() == null || newTop.getRightSubTree().relativePosition > 0);
            return newTop;
        }

        private AVLNode<E> rotateRight() {
        	assert(!leftIsPrevious);
            final AVLNode<E> newTop = left; // can't be faedelung
            final AVLNode<E> movedNode = getLeftSubTree().getRightSubTree();

            final int newTopPosition = relativePosition + getOffset(newTop);
            final int myNewPosition = -newTop.relativePosition;
            final int movedPosition = getOffset(newTop) + getOffset(movedNode);

            setLeft(movedNode, newTop);
            newTop.setRight(this, null);

            setOffset(newTop, newTopPosition);
            setOffset(this, myNewPosition);
            setOffset(movedNode, movedPosition);

            assert(newTop.getLeftSubTree() == null || newTop.getLeftSubTree().relativePosition < 0);
            assert(newTop.getRightSubTree() == null || newTop.getRightSubTree().relativePosition > 0);
            return newTop;
        }

        /**
         * Sets the left field to the node, or the previous node if that is null
         *
         * @param node  the new left subtree node
         * @param previous  the previous node in the linked list
         */
        private void setLeft(final AVLNode<E> node, final AVLNode<E> previous) {
            leftIsPrevious = node == null;
            left = leftIsPrevious ? previous : node;
            recalcHeight();
        }

        /**
         * Sets the right field to the node, or the next node if that is null
         *
         * @param node  the new left subtree node
         * @param next  the next node in the linked list
         */
        private void setRight(final AVLNode<E> node, final AVLNode<E> next) {
            rightIsNext = node == null;
            right = rightIsNext ? next : node;
            recalcHeight();
        }

        /**
         * Used for debugging.
         */
        @Override
        public String toString() {
            return new StringBuilder()
                .append("AVLNode(")
                .append(relativePosition)
                .append(',')
                .append(left != null)
                .append(',')
                .append(value)
                .append(',')
                .append(getRightSubTree() != null)
                .append(", faedelung ")
                .append(rightIsNext)
                .append(" )")
                .toString();
        }
    }

}
