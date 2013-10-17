/*
 * Copyright 2013 by Thomas Mauch
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

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

import org.magicwerk.brownies.collections.KeyCollectionImpl.BuilderImpl;
import org.magicwerk.brownies.collections.function.Predicate;
import org.magicwerk.brownies.collections.function.Trigger;


/**
 * KeyList implements a list.
 * It can provide fast access to its elements like a Set.
 * The elements allowed in the list can be constraint (null/duplicate values).
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @param <E> type of elements stored in the list
 */
@SuppressWarnings("serial")
public class KeyList<E> extends KeyListImpl<E> {

    /**
     * Builder to construct KeyList instances.
     */
    public static class Builder<E> extends BuilderImpl<E> {
        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Private constructor used if extending KeyList.
         *
         * @param keyList	key list
         */
        Builder(KeyList<E> keyList) {
        	this.keyList = keyList;
        }

        // -- Constraint

        @Override
        public Builder<E> withNull(boolean allowNull) {
        	return (Builder<E>) super.withNull(allowNull);
        }

        @Override
        public Builder<E> withConstraint(Predicate<E> constraint) {
        	return (Builder<E>) super.withConstraint(constraint);
        }

        // -- Triggers

        @Override
        public Builder<E> withInsertTrigger(Trigger<E> trigger) {
        	return (Builder<E>) super.withInsertTrigger(trigger);
        }

        @Override
        public Builder<E> withDeleteTrigger(Trigger<E> trigger) {
        	return (Builder<E>) super.withDeleteTrigger(trigger);
        }

        //-- Content

        @Override
        public Builder<E> withCapacity(int capacity) {
        	return (Builder<E>) super.withCapacity(capacity);
        }

        @Override
        public Builder<E> withElements(Collection<? extends E> elements) {
        	return (Builder<E>) super.withElements(elements);
        }

        @Override
        public Builder<E> withElements(E... elements) {
        	return (Builder<E>) super.withElements(elements);
        }

        @Override
        public Builder<E> withMaxSize(int maxSize) {
        	return (Builder<E>) super.withMaxSize(maxSize);
        }

        @Override
        public Builder<E> withWindowSize(int maxSize) {
        	return (Builder<E>) super.withWindowSize(maxSize);
        }

        //-- Element key

        @Override
        public Builder<E> withElem() {
        	return (Builder<E>) super.withElem();
        }

        @Override
        public Builder<E> withElemOrderBy(boolean orderBy) {
        	return (Builder<E>) super.withElemOrderBy(orderBy);
        }

        @Override
        public Builder<E> withElemOrderBy(Class<?> type) {
        	return (Builder<E>) super.withElemOrderBy(type);
        }

        @Override
        public Builder<E> withElemNull(boolean allowNull) {
        	return (Builder<E>) super.withElemNull(allowNull);
        }

        @Override
        public Builder<E> withElemDuplicates(boolean allowDuplicates) {
        	return (Builder<E>) super.withElemDuplicates(allowDuplicates);
        }

        @Override
        public Builder<E> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
        	return (Builder<E>) super.withElemDuplicates(allowDuplicates, allowDuplicatesNull);
        }

        @Override
        public Builder<E> withElemSort(boolean sort) {
        	return (Builder<E>) super.withElemSort(sort);
        }

        @Override
        public Builder<E> withElemSort(Comparator<? super E> comparator) {
        	return (Builder<E>) super.withElemSort(comparator);
        }

        @Override
        public Builder<E> withElemSort(Comparator<? super E> comparator, boolean sortNullsFirst) {
        	return (Builder<E>) super.withElemSort(comparator, sortNullsFirst);
        }

        @Override
        public Builder<E> withPrimaryElem() {
        	return (Builder<E>) super.withPrimaryElem();
        }

        @Override
        public Builder<E> withUniqueElem() {
        	return (Builder<E>) super.withUniqueElem();
        }

        /**
         * Create collection with specified options.
         *
         * @return created collection
         */
        public KeyList<E> build() {
        	if (keyColl == null) {
               	keyColl = new KeyCollectionImpl<E>();
        	}
        	build(keyColl, true);
        	KeyList<E> list = new KeyList<E>();
        	init(keyColl, list);
        	return list;
        }
    }

    /**
     * Private constructor used by builder.
     */
    private KeyList() {
    }

    /**
     * @return builder to use in extending classes
     */
    protected Builder<E> getBuilder() {
    	return new Builder<E>(this);
    }

    @Override
    public Object clone() {
    	return copy();
    }

    @Override
    public KeyList<E> copy() {
        KeyList<E> copy = new KeyList<E>();
        copy.initCopy(this);
        return copy;
    }

    @Override
    public KeyList<E> crop() {
        KeyList<E> crop = new KeyList<E>();
        crop.initCrop(this);
        return crop;
    }

    //-- Element methods

    @Override
	public GapList<E> getAll(E elem) {
		return super.getAll(elem);
	}

    @Override
	public int getCount(E elem) {
		return super.getCount(elem);
	}

    @Override
	public GapList<E> removeAll(E elem) {
		return super.removeAll(elem);
	}

    @Override
	public Set<E> getDistinct() {
		return super.getDistinct();
	}

}