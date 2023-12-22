/*
 * Copyright 2023 by Thomas Mauch
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
 */
package org.magicwerk.brownies.collections;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

/**
 * Class {@link IListAsDeque} implements a {@link Deque} based on a {@link IList}.
 *
 * @author Thomas Mauch
 */
public class IListAsDeque<E> implements Deque<E> {

	// As IList implements all method of Deque but cannot implement the interface itself, this class simply delegates all method calls

	IList<E> list;

	public IListAsDeque(IList<E> list) {
		this.list = list;
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		return list.toArray(array);
	}

	@Override
	public boolean containsAll(Collection<?> coll) {
		return list.containsAll(coll);
	}

	@Override
	public boolean addAll(Collection<? extends E> coll) {
		return list.addAll(coll);
	}

	@Override
	public boolean removeAll(Collection<?> coll) {
		return list.removeAll(coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll) {
		return list.retainAll(coll);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public void addFirst(E elem) {
		list.addFirst(elem);
	}

	@Override
	public void addLast(E elem) {
		list.addLast(elem);
	}

	@Override
	public boolean offerFirst(E elem) {
		return list.offerFirst(elem);
	}

	@Override
	public boolean offerLast(E elem) {
		return list.offerLast(elem);
	}

	@Override
	public E removeFirst() {
		return list.removeFirst();
	}

	@Override
	public E removeLast() {
		return list.removeLast();
	}

	@Override
	public E pollFirst() {
		return list.pollFirst();
	}

	@Override
	public E pollLast() {
		return list.pollLast();
	}

	@Override
	public E getFirst() {
		return list.getFirst();
	}

	@Override
	public E getLast() {
		return list.getLast();
	}

	@Override
	public E peekFirst() {
		return list.peekFirst();
	}

	@Override
	public E peekLast() {
		return list.peekLast();
	}

	@Override
	public boolean removeFirstOccurrence(Object elem) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeLastOccurrence(Object elem) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean add(E elem) {
		return list.add(elem);
	}

	@Override
	public boolean offer(E elem) {
		return list.offer(elem);
	}

	@Override
	public E remove() {
		return list.remove();
	}

	@Override
	public E poll() {
		return list.poll();
	}

	@Override
	public E element() {
		return list.element();
	}

	@Override
	public E peek() {
		return list.peek();
	}

	@Override
	public void push(E elem) {
		list.push(elem);
	}

	@Override
	public E pop() {
		return list.pop();
	}

	@Override
	public boolean remove(Object elem) {
		return list.remove(elem);
	}

	@Override
	public boolean contains(Object elem) {
		return list.contains(elem);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public Iterator<E> iterator() {
		return list.iterator();
	}

	@Override
	public Iterator<E> descendingIterator() {
		return list.descendingIterator();
	}

}