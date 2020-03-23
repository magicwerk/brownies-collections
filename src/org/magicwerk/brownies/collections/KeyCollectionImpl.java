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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.magicwerk.brownies.collections.exceptions.DuplicateKeyException;
import org.magicwerk.brownies.collections.exceptions.KeyException;
import org.magicwerk.brownies.collections.helper.BigLists;
import org.magicwerk.brownies.collections.helper.GapLists;
import org.magicwerk.brownies.collections.helper.IdentMapper;
import org.magicwerk.brownies.collections.helper.NaturalComparator;
import org.magicwerk.brownies.collections.helper.NullComparator;
import org.magicwerk.brownies.collections.helper.Option;
import org.magicwerk.brownies.collections.helper.SortedLists;

/**
 * Add:
 * - validation fails: null / constraint
 * - duplicate not allowed (mode replace)
 *
 * Triggers:
 * - triggers are called after the add/remove operation has finished
 * - if an exception is thrown in the trigger, the change already made to the collection is not undone
 *
 * @author Thomas Mauch
 * @version $Id$
 *
 * @see GapList
 * @param <E> type of elements stored in the list
 */
@SuppressWarnings("serial")
public class KeyCollectionImpl<E> implements Collection<E>, Serializable, Cloneable {

	/**
	 * Implementation of builder.
	 */
	public static class BuilderImpl<E> {

		public static class KeyMapBuilder<E, K> {
			/** True to order collection by this key map */
			Boolean orderBy;
			// -- sorted list
			/** Primitive class to use for list storage */
			Class<?> primitiveListType;
			// -- mapper
			Function<E, K> mapper;
			// -- null
			Boolean allowNull;
			// -- duplicates
			Boolean allowDuplicates;
			boolean allowDuplicatesNull;
			// -- sorted list
			/** True to sort */
			Boolean sort;
			/** Comparator to use for sorting (null for natural comparator) */
			Comparator<?> comparator;
			/** The specified comparator can handle null values */
			boolean comparatorSortsNull;
			/** Determine whether null values appear first or last */
			boolean sortNullsFirst;
		}

		// KeyList to build
		KeyCollectionImpl<E> keyColl;
		KeyListImpl<E> keyList;
		// -- constraint
		boolean allowNullElem = true;
		Predicate<E> constraint;
		// -- triggers
		Consumer<E> beforeInsertTrigger;
		Consumer<E> afterInsertTrigger;
		Consumer<E> beforeDeleteTrigger;
		Consumer<E> afterDeleteTrigger;
		// -- keys
		GapList<KeyMapBuilder<E, Object>> keyMapBuilders = GapList.create();
		// -- content
		Collection<? extends E> collection;
		E[] array;
		int capacity;
		int maxSize;
		Boolean movingWindow;
		boolean setBehavior;
		/** True to count only number of occurrences of equal elements */
		boolean count;
		/** True to store list data in a BigList instance, false for a GapList instance (only used for KeyList, Key1List, Key2List) */
		boolean useBigList;

		// Interface

		/**
		 * Specifies whether null elements are allowed or not.
		 * A null element will have null keys.
		 * This method does not implicitly create an element set, where as {@link #withElemNull} does.
		 *
		 * @param allowNull true to allow null elements (default), false to disallow
		 * @return          this (fluent interfaces)
		 */
		protected BuilderImpl<E> withNull(boolean allowNull) {
			this.allowNullElem = allowNull;
			if (hasElemMapBuilder()) {
				getKeyMapBuilder(0).allowNull = allowNull;
			}
			return this;
		}

		/**
		 * Specify element constraint.
		 *
		 * @param constraint	constraint element must satisfy, null for none (default)
		 * @return 				this (fluent interface)
		 */
		protected BuilderImpl<E> withConstraint(Predicate<E> constraint) {
			this.constraint = constraint;
			return this;
		}

		// -- Triggers

		/**
		 * Specify insert trigger.
		 *
		 * @param trigger	insert trigger method, null for none (default)
		 * @return			this (fluent interface)
		 */
		protected BuilderImpl<E> withBeforeInsertTrigger(Consumer<E> trigger) {
			this.beforeInsertTrigger = trigger;
			return this;
		}

		/**
		 * Specify insert trigger.
		 *
		 * @param trigger	insert trigger method, null for none (default)
		 * @return			this (fluent interface)
		 */
		protected BuilderImpl<E> withAfterInsertTrigger(Consumer<E> trigger) {
			this.afterInsertTrigger = trigger;
			return this;
		}

		/**
		 * Specify delete trigger.
		 *
		 * @param trigger	delete trigger method, null for none (default)
		 * @return			this (fluent interface)
		 */
		protected BuilderImpl<E> withBeforeDeleteTrigger(Consumer<E> trigger) {
			this.beforeDeleteTrigger = trigger;
			return this;
		}

		/**
		 * Specify delete trigger.
		 *
		 * @param trigger	delete trigger method, null for none (default)
		 * @return			this (fluent interface)
		 */
		protected BuilderImpl<E> withAfterDeleteTrigger(Consumer<E> trigger) {
			this.afterDeleteTrigger = trigger;
			return this;
		}

		//-- Content

		/**
		 * Specify initial capacity.
		 *
		 * @param capacity	initial capacity
		 * @return			this (fluent interface)
		 */
		protected BuilderImpl<E> withCapacity(int capacity) {
			this.capacity = capacity;
			return this;
		}

		/**
		 * Specify elements added to the collection upon creation.
		 *
		 * @param elements	initial elements
		 * @return			this (fluent interface)
		 */
		protected BuilderImpl<E> withContent(Collection<? extends E> elements) {
			this.collection = elements;
			return this;
		}

		/**
		 * Specify elements added to the collection upon creation.
		 *
		 * @param elements	initial elements
		 * @return			this (fluent interface)
		 */
		protected BuilderImpl<E> withContent(E... elements) {
			this.array = elements;
			return this;
		}

		/**
		 * Specify maximum size of collection.
		 * If an attempt is made to add more elements, an exception is thrown.
		 *
		 * @param maxSize	maximum size
		 * @return			this (fluent interface)
		 */
		protected BuilderImpl<E> withMaxSize(int maxSize) {
			if (movingWindow != null) {
				throw new IllegalArgumentException("maximum or window size alreay set");
			}
			this.maxSize = maxSize;
			this.movingWindow = false;
			return this;
		}

		/**
		 * Specify maximum window size of collection.
		 * If an attempt is made to add and additional element, the first element is removed.
		 *
		 * @param maxSize	maximum window size
		 * @return			this (fluent interface)
		 */
		protected BuilderImpl<E> withWindowSize(int maxSize) {
			if (movingWindow != null) {
				throw new IllegalArgumentException("maximum or window size alreay set");
			}
			this.maxSize = maxSize;
			this.movingWindow = true;
			return this;
		}

		//-- Element key

		/**
		 * Specifies that the collection only counts the number of occurrences
		 * of equal elements, but does not store the elements themselves.
		 *
		 * @param count	true to count only number of occurrences (default is false)
		 * @return		this (fluent interface)
		 */
		protected BuilderImpl<E> withSetBehavior(boolean setBehavior) {
			this.setBehavior = setBehavior;
			return this;
		}

		/**
		 * Specifies that the collection only counts the number of occurrences
		 * of equal elements, but does not store the elements themselves.
		 *
		 * @param count	true to count only number of occurrences (default is false)
		 * @return		this (fluent interface)
		 */
		protected BuilderImpl<E> withElemCount(boolean count) {
			this.count = count;
			return this;
		}

		/**
		 * Add element map (with ident mapper).
		 *
		 * @return			this (fluent interface)
		 */
		protected BuilderImpl<E> withElemSet() {
			return withKeyMap(0, IdentMapper.INSTANCE);
		}

		/**
		 * Specifies that the collection will have the order of the element set.
		 * The element set must be sorted, if no sort order has been defined,
		 * the natural comparator will be used. If the set allows null
		 * values, the used comparator will sort them last.
		 *
		 * @param orderBy	if true the collection will have the order of the element set
		 * 					(default is false, only one key map or the element set can have the order by option set)
		 * @return			this (fluent interface)
		 */
		protected BuilderImpl<E> withOrderByElem(boolean orderBy) {
			return withOrderByKey(0, orderBy);
		}

		/**
		 * Specifies that the list will have the order of the element set.
		 * The set will store values of the primitive type specified like <code>int</code>.
		 * The set will be sorted using the natural comparator and no null values are allowed.
		 *
		 * @param type	primitive type to use for key map
		 * 				(only one key map or the element set can have the order by option set)
		 * @return		this (fluent interface)
		 */
		// only for KeyList (if the element is a primitive, no keys can be extracted from it so Key1List and Key2List does not make sense)
		protected BuilderImpl<E> withOrderByElem(Class<?> type) {
			return withOrderByKey(0, type);
		}

		/**
		 * Specifies whether null elements are allowed or not.
		 * A null element will have null keys.
		 * This method does implicitly create an element set, where as {@link #withNull} does not.
		 *
		 * @param allowNull true to allow null elements, false to disallow (default is true)
		 * @return          this (fluent interfaces)
		 */
		protected BuilderImpl<E> withElemNull(boolean allowNull) {
			return withKeyNull(0, allowNull);
		}

		/**
		 * Specify whether duplicates are allowed or not.
		 * This method does implicitly create an element set.
		 *
		 * @param allowDuplicates   true to allow duplicates (default is true)
		 * @return              	this (fluent interfaces)
		 */
		protected BuilderImpl<E> withElemDuplicates(boolean allowDuplicates) {
			return withElemDuplicates(allowDuplicates, allowDuplicates);
		}

		/**
		 * Specify whether duplicates are allowed or not.
		 * This method does implicitly create an element set.
		 *
		 * @param allowDuplicates		true to allow duplicates (default is true)
		 * @param allowDuplicatesNull	true to allow duplicate null values (default is true)
		 * @return						this (fluent interfaces)
		 */
		protected BuilderImpl<E> withElemDuplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
			return withKeyDuplicates(0, allowDuplicates, allowDuplicatesNull);
		}

		/**
		 * Specify that the element set should be sorted using the natural comparator.
		 * If the collection supports null values, they are sorted last.
		 * This method does implicitly create an element set.
		 * Note that this does not automatically sort the collection itself, call a withOrderBy method for this.
		 *
		 * @param sort    true to sorted, false for unsorted (default is false)
		 * @return        this (fluent interface)
		 */
		protected BuilderImpl<E> withElemSort(boolean sort) {
			return withKeySort(0, sort);
		}

		/**
		 * Set comparator to use for sorting the element set.
		 * If the collection allows null values, the comparator must be able to compare null values.
		 * If the comparator does not support null values, use withElemSort(Comparator, boolean) to
		 * explicitly specify how null values should be sorted.
		 * This method does implicitly create an element set.
		 * Note that this does not automatically sort the collection itself, call a withOrderBy method for this.
		 *
		 * @param comparator    comparator to use for sorting (null for natural comparator)
		 * @return              this (fluent interface)
		 */
		protected BuilderImpl<E> withElemSort(Comparator<? super E> comparator) {
			return withKeySort(0, comparator);
		}

		/**
		 * Set comparator to use for sorting the element set.
		 * This method should be used if the collection can contain null values, but the comparator
		 * is not able to handle them. The parameter sortNullsFirst determine how the null values
		 * should be sorted.
		 * This method does implicitly create an element set.
		 *
		 * @param comparator           comparator to use for sorting
		 * @param sortNullsFirst	   true to sort null values first, false for last
		 * @return                     this (fluent interface)
		 */
		protected BuilderImpl<E> withElemSort(Comparator<? super E> comparator, boolean sortNullsFirst) {
			return withKeySort(0, comparator, sortNullsFirst);
		}

		/**
		 * Specify the element to be a primary key.
		 * This is identical to calling
		 * withElemNull(false) and withElemDuplicates(false).
		 *
		 * @return	this (fluent interface)
		 */
		protected BuilderImpl<E> withPrimaryElem() {
			return withPrimaryKeyMap(0, null);
		}

		/**
		 * Specify the element to be a unique key.
		 * This is identical to calling
		 * withElemNull(true) and withElemDuplicates(false, true).
		 *
		 * @return	this (fluent interface)
		 */
		protected BuilderImpl<E> withUniqueElem() {
			return withUniqueKeyMap(0, null);
		}

		//

		@SuppressWarnings({ "rawtypes", "unchecked" })
		protected BuilderImpl<E> withKeyMap(int keyIndex, Function mapper) {
			if (mapper == null) {
				throw new IllegalArgumentException("Mapper may not be null");
			}
			KeyMapBuilder<?, ?> kmb = getKeyMapBuilder(keyIndex);
			if (kmb.mapper != null) {
				throw new IllegalArgumentException("Mapper already set");
			}
			kmb.mapper = mapper;
			return this;
		}

		protected BuilderImpl<E> withOrderByKey(int keyIndex, boolean orderBy) {
			KeyMapBuilder<?, ?> kmb = getKeyMapBuilder(keyIndex);
			if (kmb.orderBy != null) {
				throw new IllegalArgumentException("Order by already set");
			}
			kmb.orderBy = orderBy;
			return this;
		}

		protected BuilderImpl<E> withOrderByKey(int keyIndex, Class<?> type) {
			if (type == null) {
				throw new IllegalArgumentException("Order by type may not be null");
			}
			if (!type.isPrimitive()) {
				throw new IllegalArgumentException("Class type must be primitive");
			}
			KeyMapBuilder<?, ?> kmb = getKeyMapBuilder(keyIndex);
			if (kmb.orderBy != null) {
				throw new IllegalArgumentException("Order by already set");
			}
			kmb.orderBy = true;
			kmb.primitiveListType = type;
			return this;
		}

		/**
		 * Specifies that the list will store its elements as primitive type.
		 *
		 * @param type	primitive type to use for list
		 * @return		this (fluent interface)
		 */
		// only for KeyList.withListType
		protected BuilderImpl<E> withListType(Class<?> type) {
			if (type == null) {
				throw new IllegalArgumentException("Class type may not be null");
			}
			if (!type.isPrimitive()) {
				throw new IllegalArgumentException("Class type must be primitive");
			}
			KeyMapBuilder<?, ?> kmb = getKeyMapBuilder(0);
			kmb.primitiveListType = type;
			return this;
		}

		/**
		 * Specify whether list should be stored in an instance of BigList or GapList.
		 *
		 * @param big	true to store list content in an instance of BigList, false for GapList
		 * @return		this (fluent interface)
		 */
		// only for KeyList / Key1List / Key2List
		protected BuilderImpl<E> withListBig(boolean big) {
			this.useBigList = big;
			return this;
		}

		protected BuilderImpl<E> withKeyNull(int keyIndex, boolean allowNull) {
			KeyMapBuilder<?, ?> kmb = getKeyMapBuilder(keyIndex);
			if (kmb.allowNull != null) {
				throw new IllegalArgumentException("AllowNull already set");
			}
			kmb.allowNull = allowNull;
			if (keyIndex == 0) {
				allowNullElem = allowNull;
			}
			return this;
		}

		protected BuilderImpl<E> withKeyDuplicates(int keyIndex, boolean allowDuplicates, boolean allowDuplicatesNull) {
			KeyMapBuilder<?, ?> kmb = getKeyMapBuilder(keyIndex);
			if (kmb.allowDuplicates != null) {
				throw new IllegalArgumentException("AllowDuplicates already set");
			}
			kmb.allowDuplicates = allowDuplicates;
			kmb.allowDuplicatesNull = allowDuplicatesNull;
			return this;
		}

		protected BuilderImpl<E> withKeySort(int keyIndex, boolean sort) {
			KeyMapBuilder<?, ?> kmb = getKeyMapBuilder(keyIndex);
			if (kmb.sort != null) {
				throw new IllegalArgumentException("Sort already set");
			}
			kmb.sort = sort;
			kmb.comparator = null;
			kmb.comparatorSortsNull = false;
			kmb.sortNullsFirst = false;
			return this;
		}

		protected BuilderImpl<E> withKeySort(int keyIndex, Comparator<?> comparator) {
			KeyMapBuilder<?, ?> kmb = getKeyMapBuilder(keyIndex);
			if (kmb.sort != null) {
				throw new IllegalArgumentException("Sort already set");
			}
			kmb.sort = true;
			kmb.comparator = comparator;
			kmb.comparatorSortsNull = true;
			kmb.sortNullsFirst = false;
			return this;
		}

		protected BuilderImpl<E> withKeySort(int keyIndex, Comparator<?> comparator, boolean sortNullsFirst) {
			KeyMapBuilder<?, ?> kmb = getKeyMapBuilder(keyIndex);
			if (kmb.sort != null) {
				throw new IllegalArgumentException("Sort already set");
			}
			kmb.sort = true;
			kmb.comparator = comparator;
			kmb.comparatorSortsNull = false;
			kmb.sortNullsFirst = sortNullsFirst;
			return this;
		}

		@SuppressWarnings("rawtypes")
		protected BuilderImpl<E> withPrimaryKeyMap(int keyIndex, Function mapper) {
			if (mapper != null) {
				// mapper may be null if called by withPrimaryElem()
				withKeyMap(keyIndex, mapper);
			}
			withKeyNull(keyIndex, false);
			withKeyDuplicates(keyIndex, false, false);
			return this;
		}

		@SuppressWarnings("rawtypes")
		protected BuilderImpl<E> withUniqueKeyMap(int keyIndex, Function mapper) {
			if (mapper != null) {
				withKeyMap(keyIndex, mapper);
			}
			withKeyNull(keyIndex, true);
			withKeyDuplicates(keyIndex, false, true);
			return this;
		}

		// -- Key1
		//
		// Just define methods which can be overridden so Javadoc can be inherited.
		// All methods with generic type parameters must explicitly be defined in concrete classes.

		/**
		 * Specifies that the collection will have the order of the key map.
		 * The key map must be sorted, if no sort order has been defined,
		 * the natural comparator will be used. If the map allows null
		 * values, the used comparator will sort them last.
		 *
		 * @param orderBy	if true the collection will have the order of the key map
		 * 					(default is false, only one key map or the element set can have the order by option set)
		 * @return			this (fluent interface)
		 */
		protected BuilderImpl<E> withOrderByKey1(boolean orderBy) {
			return withOrderByKey(1, orderBy);
		}

		/**
		 * Specifies that the list will have the order of the key map.
		 * The key map will store values of the primitive type specified like <code>int</code>.
		 * The key map will be sorted using the natural comparator and no null values are allowed.
		 *
		 * @param type	primitive type to use for key map
		 * 				(only one key map or the element set can have the order by option set)
		 * @return		this (fluent interface)
		 */
		// only for Key1List / Key2List
		protected BuilderImpl<E> withOrderByKey1(Class<?> type) {
			return withOrderByKey(1, type);
		}

		/**
		 * Specify whether null elements are allowed or not.
		 * A null element will have a null key.
		 *
		 * @param allowNull true to allow null elements, false to disallow
		 * @return          this (fluent interfaces)
		 */
		protected BuilderImpl<E> withKey1Null(boolean allowNull) {
			return withKeyNull(1, allowNull);
		}

		/**
		 * Specify whether duplicates are allowed or not.
		 *
		 * @param allowDuplicates   true to allow duplicates
		 * @return              	this (fluent interfaces)
		 */
		protected BuilderImpl<E> withKey1Duplicates(boolean allowDuplicates) {
			return withKeyDuplicates(1, allowDuplicates, allowDuplicates);
		}

		/**
		 * Specify whether duplicates are allowed or not.
		 *
		 * @param allowDuplicates		true to allow duplicates
		 * @param allowDuplicatesNull	true to allow duplicate null values
		 * @return						this (fluent interfaces)
		 */
		protected BuilderImpl<E> withKey1Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
			return withKeyDuplicates(1, allowDuplicates, allowDuplicatesNull);
		}

		/**
		 * Set comparator to use for sorting the key map.
		 * Note that this does not automatically sort the list itself, call a withOrderBy method for this.
		 *
		 * @param sort	  true to sort key map
		 * @return        this (fluent interface)
		 */
		// The other overloaded methods also named withKey1Sort must defined directly in the concrete classes due to the generic types
		protected BuilderImpl<E> withKey1Sort(boolean sort) {
			return withKeySort(1, sort);
		}

		// -- Key2
		//
		// Just define methods which can be overridden so Javadoc can be inherited.
		// All methods with generic type parameters must explicitly be defined in concrete classes.

		/**
		 * Specifies that the collection will have the order of the key map.
		 * The key map must be sorted, if no sort order has been defined,
		 * the natural comparator will be used. If the map allows null
		 * values, the used comparator will sort them last.
		 *
		 * @param orderBy	if true the collection will have the order of the key map
		 * 					(default is false, only one key map or the element set can have the order by option set)
		 * @return			this (fluent interface)
		 */
		protected BuilderImpl<E> withOrderByKey2(boolean orderBy) {
			return withOrderByKey(2, orderBy);
		}

		/**
		 * Specifies that the list will have the order of the key map.
		 * The key map will store values of the primitive type specified like <code>int</code>.
		 * The key map will be sorted using the natural comparator and no null values are allowed.
		 *
		 * @param type	primitive type to use for key map
		 * 				(only one key map or the element set can have the order by option set)
		 * @return		this (fluent interface)
		 */
		protected BuilderImpl<E> withOrderByKey2(Class<?> type) {
			return withOrderByKey(2, type);
		}

		/**
		 * Specify whether null elements are allowed or not.
		 * A null element will have a null key.
		 *
		 * @param allowNull true to allow null elements, false to disallow
		 * @return          this (fluent interfaces)
		 */
		protected BuilderImpl<E> withKey2Null(boolean allowNull) {
			return withKeyNull(2, allowNull);
		}

		/**
		 * Specify whether duplicates are allowed or not.
		 *
		 * @param allowDuplicates   true to allow duplicates
		 * @return              	this (fluent interfaces)
		 */
		protected BuilderImpl<E> withKey2Duplicates(boolean allowDuplicates) {
			return withKeyDuplicates(2, allowDuplicates, allowDuplicates);
		}

		/**
		 * Specify whether duplicates are allowed or not.
		 *
		 * @param allowDuplicates		true to allow duplicates
		 * @param allowDuplicatesNull	true to allow duplicate null values
		 * @return						this (fluent interfaces)
		 */
		protected BuilderImpl<E> withKey2Duplicates(boolean allowDuplicates, boolean allowDuplicatesNull) {
			return withKeyDuplicates(2, allowDuplicates, allowDuplicatesNull);
		}

		/**
		 * Set comparator to use for sorting the key map.
		 * Note that this does not automatically sort the list itself, call a withOrderBy method for this.
		 *
		 * @param sort    true to sort key map
		 * @return        this (fluent interface)
		 */
		// The other overloaded methods also named withKey2Sort must defined directly in the concrete classes due to the generic types
		protected BuilderImpl<E> withKey2Sort(boolean sort) {
			return withKeySort(2, sort);
		}

		//-- Implementation

		/**
		 * @param numKeys	number of keys
		 */
		void initKeyMapBuilder(int numKeys) {
			assert (numKeys >= 0);
			// add 1 for elem key
			keyMapBuilders.initMult(numKeys + 1, null);
		}

		boolean hasElemMapBuilder() {
			return keyMapBuilders.size() > 0 && keyMapBuilders.get(0) != null;
		}

		KeyMapBuilder<E, Object> getKeyMapBuilder(int index) {
			int size = keyMapBuilders.size();
			for (int i = size; i <= index; i++) {
				keyMapBuilders.add(i, null);
			}
			KeyMapBuilder kmb = keyMapBuilders.get(index);
			if (kmb == null) {
				kmb = new KeyMapBuilder();
				keyMapBuilders.set(index, kmb);
			}
			return kmb;
		}

		boolean isTrue(Boolean b) {
			return b != null && b;
		}

		boolean isFalse(Boolean b) {
			return !(b != null && !b);
		}

		/**
		 * Initialize KeyMap.
		 *
		 * @param keyMapBuild 	key map builder to use for initialization
		 * @param list   		true if a KeyListImpl is built up, false for KeyCollectionImpl
		 */
		KeyMap buildKeyMap(KeyMapBuilder keyMapBuilder, boolean list) {
			KeyMap<E, Object> keyMap = new KeyMap<E, Object>();
			keyMap.mapper = keyMapBuilder.mapper;
			keyMap.allowNull = isFalse(keyMapBuilder.allowNull);
			keyMap.allowDuplicates = isFalse(keyMapBuilder.allowDuplicates);
			keyMap.allowDuplicatesNull = isFalse(keyMapBuilder.allowDuplicatesNull);

			if (isTrue(keyMapBuilder.sort) || isTrue(keyMapBuilder.orderBy)) {
				if (keyMapBuilder.comparator == null) {
					if (keyMap.allowNull) {
						keyMap.comparator = new NullComparator(NaturalComparator.INSTANCE(), keyMapBuilder.sortNullsFirst);
					} else {
						keyMap.comparator = NaturalComparator.INSTANCE();
					}
				} else {
					if (!keyMapBuilder.comparatorSortsNull && keyMap.allowNull) {
						keyMap.comparator = new NullComparator(keyMapBuilder.comparator, keyMapBuilder.sortNullsFirst);
					} else {
						keyMap.comparator = keyMapBuilder.comparator;
					}
				}
			}

			if (list && isTrue(keyMapBuilder.orderBy)) {
				if (keyMapBuilder.primitiveListType == null) {
					if (useBigList) {
						keyMap.keysList = new BigList<Object>();
					} else {
						keyMap.keysList = new GapList<Object>();
					}
				} else {
					if (keyMapBuilder.comparator != null && keyMapBuilder.comparator != NaturalComparator.INSTANCE()) {
						throw new IllegalArgumentException("Only natural comparator supported for list type");
					}
					if (isTrue(keyMapBuilder.allowNull)) {
						throw new IllegalArgumentException("Null values are not supported for primitive list type");
					}
					keyMap.comparator = NaturalComparator.INSTANCE();
					if (useBigList) {
						keyMap.keysList = (IList<Object>) BigLists.createWrapperList(keyMapBuilder.primitiveListType);
					} else {
						keyMap.keysList = (IList<Object>) GapLists.createWrapperList(keyMapBuilder.primitiveListType);
					}
				}
			} else if (keyMap.comparator != null) {
				keyMap.keysMap = new TreeMap(keyMap.comparator);
			} else {
				keyMap.keysMap = new HashMap();
			}

			return keyMap;
		}

		/**
		 * Initialize KeyCollectionImpl.
		 *
		 * @param keyColl collection to initialize
		 * @param list    true if a KeyListImpl is built up, false for KeyCollectionImpl
		 */
		void build(KeyCollectionImpl keyColl, boolean list) {
			keyColl.setBehavior = setBehavior;
			keyColl.allowNullElem = allowNullElem;
			keyColl.constraint = constraint;
			keyColl.beforeInsertTrigger = beforeInsertTrigger;
			keyColl.afterInsertTrigger = afterInsertTrigger;
			keyColl.beforeDeleteTrigger = beforeDeleteTrigger;
			keyColl.afterDeleteTrigger = afterDeleteTrigger;
			keyColl.maxSize = maxSize;
			keyColl.movingWindow = isTrue(movingWindow);

			int orderByKey = -1;
			int size = keyMapBuilders.size();
			if (size == 1) {
				KeyMapBuilder kmb = keyMapBuilders.get(0);
				if (kmb == null) {
					if (!list) {
						withElemSet();
					} else {
						size = 0;
					}
				} else {
					// If primitiveListType is only set to define a list type, no key map must be created
					if (list && kmb.primitiveListType != null && kmb.orderBy == null && kmb.mapper == null && kmb.allowDuplicates == null
							&& kmb.allowNull == null && kmb.sort == null) {
						size = 0;
					}
				}
			}
			if (size > 0) {
				keyColl.keyMaps = new KeyMap[size];
				for (int i = 0; i < size; i++) {
					KeyMapBuilder kmb = keyMapBuilders.get(i);
					if (kmb == null) {
						if (i != 0) {
							throw new IllegalArgumentException("Key " + i + " is not defined");
						}
					} else {
						if (isTrue(kmb.orderBy)) {
							if (orderByKey != -1) {
								throw new IllegalArgumentException("Only one order by key allowed");
							}
							orderByKey = i;
						}
						if (kmb.mapper == null) {
							if (i == 0) {
								kmb.mapper = IdentMapper.INSTANCE;
							} else {
								throw new IllegalArgumentException("No mapper for key " + i + " defined");
							}
						}
						keyColl.keyMaps[i] = buildKeyMap(kmb, list);
						if (i == 0) {
							keyColl.keyMaps[i].count = count;
						}
					}
				}
			}

			// KeyCollectionImpl must have a defined order,
			// KeyListImpl will use the list order
			if (orderByKey == -1 && !list) {
				if (keyColl.keyMaps != null) {
					if (keyColl.keyMaps[0] != null) {
						orderByKey = 0;
					} else {
						assert (keyColl.keyMaps[1] != null);
						orderByKey = 1;
					}
				}
			}
			keyColl.orderByKey = orderByKey;
		}

		/**
		 * This method is called if a KeyCollection, Key1Collection, Key2Collection is initialized.
		 *
		 * @param keyColl
		 * @param keyList
		 */
		void init(KeyCollectionImpl keyColl) {
			if (collection != null) {
				keyColl.addAll(collection);
			} else if (array != null) {
				keyColl.addAll(Arrays.asList(array));
			}
		}

		/**
		 * This method is called if a KeyList, Key1List, Key2List is initialized.
		 *
		 * @param keyColl	KeyCollectionImpl which has already been initialized
		 * @param keyList	KeyListImpl to initialize
		 */
		void init(KeyCollectionImpl keyColl, KeyListImpl keyList) {
			keyList.keyColl = keyColl;
			keyColl.keyList = keyList;
			if (keyColl.orderByKey == 0) {
				keyList.list = keyColl.keyMaps[0].keysList;
				if (keyList.list == null) {
					keyList.list = initList();
				}
				if (collection != null) {
					keyColl.addAll(collection);
				} else if (array != null) {
					keyColl.addAll(Arrays.asList(array));
				}
			} else {
				keyList.list = initList();
				if (collection != null) {
					keyList.ensureCapacity(capacity);
					keyList.addAll(collection);
				} else if (array != null) {
					keyList.ensureCapacity(capacity);
					keyList.addArray(array);
				} else if (capacity != 0) {
					keyList.ensureCapacity(capacity);
				}
			}
		}

		IList<?> initList() {
			Class<?> primitiveListType = null;
			KeyMapBuilder<?, ?> kmb = keyMapBuilders.get(0);
			if (kmb != null) {
				primitiveListType = kmb.primitiveListType;
			}

			if (primitiveListType == null) {
				if (useBigList) {
					return new BigList<Object>();
				} else {
					return new GapList<Object>();
				}
			} else {
				if (useBigList) {
					return BigLists.createWrapperList(primitiveListType);
				} else {
					return GapLists.createWrapperList(primitiveListType);
				}
			}
		}
	}

	static class KeyMap<E, K> implements Serializable {
		/** A mapper to extract keys out of element. For the element itself , this is always an IdentMapper. */
		Function<E, K> mapper;
		/** True to allow null keys */
		boolean allowNull;
		/** True to allow duplicate values if they are not null */
		boolean allowDuplicates;
		/** True to allow duplicate null values */
		boolean allowDuplicatesNull;
		/** Comparator to use for sorting (if null, elements are not sorted) */
		Comparator<K> comparator;
		/**
		 * Key storage if not sorted. The values are single elements or a list of elements.
		 * Note that we cannot use TreeMap as K may not be comparable.
		 * One of keysMap or keysList is used.
		 */
		Map<K, Object> keysMap;
		/** Key storage if this is a KeyListImpl sorted by this key map, otherwise null */
		IList<K> keysList;
		/**
		 * True to count only number of occurrences of equal elements
		 * (can only be set on keyMap[0] storing the elements).
		 */
		boolean count;

		KeyMap() {
		}

		KeyMap<E, K> copy() {
			KeyMap<E, K> copy = new KeyMap<E, K>();
			copy.mapper = mapper;
			copy.allowNull = allowNull;
			copy.allowDuplicates = allowDuplicates;
			copy.allowDuplicatesNull = allowDuplicatesNull;
			copy.comparator = comparator;
			copy.count = count;
			if (keysMap != null) {
				if (keysMap instanceof HashMap) {
					copy.keysMap = (Map) ((HashMap) keysMap).clone();
				} else {
					copy.keysMap = (Map) ((TreeMap) keysMap).clone();
				}

				// The map cannot only contain simple value, but also instances of KeyMapList
				// for duplicates which must be also be cloned
				for (Object obj : copy.keysMap.entrySet()) {
					Map.Entry entry = (Map.Entry) obj;
					Object val = entry.getValue();
					if (val instanceof KeyMapList) {
						val = new KeyMapList((KeyMapList) val);
						entry.setValue(val);
					}
				}
			} else {
				copy.keysList = keysList.copy();
			}
			return copy;
		}

		KeyMap<E, K> crop() {
			KeyMap<E, K> copy = new KeyMap<E, K>();
			copy.mapper = mapper;
			copy.allowNull = allowNull;
			copy.allowDuplicates = allowDuplicates;
			copy.allowDuplicatesNull = allowDuplicatesNull;
			copy.comparator = comparator;
			copy.count = count;
			if (keysMap != null) {
				if (keysMap instanceof HashMap) {
					copy.keysMap = new HashMap<K, Object>();
				} else {
					copy.keysMap = new TreeMap<K, Object>();
				}
			} else {
				copy.keysList = new GapList<K>();
			}
			return copy;
		}

		boolean isPrimaryMap() {
			return !allowDuplicates && !allowDuplicatesNull && !allowNull;
		}

		K getKey(E elem) {
			if (elem == null) {
				return null;
			}
			return mapper.apply(elem);
		}

		boolean containsKey(Object key) {
			if (key == null) {
				if (!allowNull) {
					return false;
				}
			}
			if (keysMap != null) {
				return keysMap.containsKey(key);
			} else {
				return keysList.binarySearch(key, (Comparator<Object>) comparator) >= 0;
			}
		}

		boolean containsValue(Object key, Object value) {
			if (keysMap == null) {
				return keysList.contains(value);
			}

			assert (count == false);
			Object obj = keysMap.get(key);
			if (obj == null && value == null) {
				return keysMap.containsKey(key);
			}

			if (obj instanceof KeyMapList) {
				GapList<E> list = (GapList<E>) obj;
				if (list.contains(value)) {
					return true;
				}
			} else {
				if (Objects.equals(obj, value)) {
					return true;
				}
			}
			return false;
		}

		boolean containsValue(Object value) {
			if (keysMap == null) {
				return keysList.contains(value);
			}

			assert (count == false);
			for (Object obj : keysMap.values()) {
				if (obj instanceof KeyMapList) {
					GapList<E> list = (GapList<E>) obj;
					if (list.contains(value)) {
						return true;
					}
				} else {
					if (Objects.equals(obj, value)) {
						return true;
					}
				}
			}
			return false;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		Option<E> getContainedKey(Object key) {
			if (key == null) {
				if (!allowNull) {
					return Option.EMPTY();
				}
			}
			if (keysMap != null) {
				Object val = keysMap.get(key);
				if (val != null) {
					return new Option(val);
				} else if (keysMap.containsKey(key)) {
					return new Option(val);
				}
			} else {
				int index = keysList.binarySearch(key, (Comparator<Object>) comparator);
				if (index >= 0) {
					return new Option(keysList.get(index));
				}
			}
			return Option.EMPTY();
		}

		@SuppressWarnings("unchecked")
		Option<E> getContainedValue(Object value) {
			assert (count == false);
			for (Map.Entry<?, ?> entry : keysMap.entrySet()) {
				if ((entry.getValue() == null && value == null) || (entry.getValue() != null && entry.getValue().equals(value))) {
					return new Option(entry.getValue());
				}
			}
			return Option.EMPTY();
		}

		Iterator<E> iteratorValues(KeyCollectionImpl<E> keyColl) {
			assert (keysMap != null);
			if (count) {
				return new KeyMapCountIter<E, K>(keyColl, this, keysMap);
			} else {
				return new KeyMapIter<E, K>(keyColl, this, keysMap);
			}
		}

		static class KeyMapIter<E, K> implements Iterator<E> {

			KeyCollectionImpl<E> keyColl;
			KeyMap<E, K> keyMap;
			Iterator<Object> mapIter;
			Iterator<E> listIter;
			boolean hasElem;
			E elem;

			public KeyMapIter(KeyCollectionImpl<E> tableColl, KeyMap<E, K> keyMap, Map<K, Object> map) {
				this.keyColl = tableColl;
				this.keyMap = keyMap;
				this.mapIter = map.values().iterator();
			}

			@Override
			public boolean hasNext() {
				boolean hasNext = false;
				if (listIter != null) {
					hasNext = listIter.hasNext();
				}
				if (!hasNext) {
					hasNext = mapIter.hasNext();
				}
				return hasNext;
			}

			@Override
			public E next() {
				// Reset hasElem so it is false if a call to Iterator.next()
				// fails with NoSuchElementException
				hasElem = false;

				boolean hasNext = false;
				elem = null;
				if (listIter != null) {
					if (listIter.hasNext()) {
						hasNext = true;
						elem = listIter.next();
					} else {
						listIter = null;
					}
				}
				if (!hasNext) {
					// This call can fail with NoSuchElementException
					Object o = mapIter.next();
					if (o instanceof KeyMapList) {
						listIter = ((KeyMapList<E>) o).iterator();
						elem = listIter.next();
					} else {
						elem = (E) o;
					}
				}
				hasElem = true;
				return elem;
			}

			@Override
			public void remove() {
				if (!hasElem) {
					throw new IllegalStateException("No current element to remove");
				}
				hasElem = false;

				if (listIter != null) {
					listIter.remove();
				} else {
					mapIter.remove();
				}
				keyColl.remove(elem, keyMap);
			}
		}

		static class KeyMapCountIter<E, K> implements Iterator<E> {

			KeyCollectionImpl<E> keyColl;
			KeyMap<E, K> keyMap;
			Map<K, Object> map;
			Iterator<Entry<K, Object>> mapIter;
			E elem;
			int count;
			boolean hasElem;

			public KeyMapCountIter(KeyCollectionImpl<E> keyColl, KeyMap<E, K> keyMap, Map<K, Object> map) {
				this.keyColl = keyColl;
				this.keyMap = keyMap;
				this.map = map;
				this.mapIter = map.entrySet().iterator();
			}

			@Override
			public boolean hasNext() {
				boolean hasNext = false;
				if (count > 0) {
					hasNext = true;
				}
				if (!hasNext) {
					hasNext = mapIter.hasNext();
				}
				return hasNext;
			}

			@Override
			public E next() {
				// Reset hasElem so it is false if a call to Iterator.next()
				// fails with NoSuchElementException
				hasElem = false;

				if (count > 0) {
					count--;
				} else {
					// This call can fail with NoSuchElementException
					Entry<K, Object> o = mapIter.next();
					elem = (E) o.getKey();
					count = (Integer) o.getValue();
					count--;
				}

				hasElem = true;
				return elem;
			}

			@Override
			public void remove() {
				if (!hasElem) {
					throw new IllegalStateException("No current element to remove");
				}
				hasElem = false;

				Integer val = (Integer) map.get(elem);
				if (val == 1) {
					mapIter.remove();
				} else {
					map.put((K) elem, val - 1);
				}
			}
		}

		/**
		 * Add element to key map.
		 *
		 * @param key	key of element
		 * @param elem	element
		 */
		void add(K key, E elem) {
			if (key == null) {
				if (!allowNull) {
					errorNullKey();
				}
			}
			if (keysMap != null) {
				// Keys not sorted
				Object newElem = (count ? 1 : elem);
				int oldSize = keysMap.size();
				Object oldElem = keysMap.put(key, newElem);
				boolean hasOldElem;
				if (oldElem != null) {
					hasOldElem = true;
				} else {
					if (key == null) {
						hasOldElem = (keysMap.size() == oldSize);
					} else {
						hasOldElem = false;
					}
				}

				if (!hasOldElem) {
					// There was no old element, so it was correct to just add the new one

				} else {
					if (!(allowDuplicates || (key == null && allowDuplicatesNull))) {
						// Revert change and raise error
						keysMap.put(key, oldElem);
						errorDuplicateKey(key);
					}

					if (count) {
						if (oldElem != null) {
							Integer val = (Integer) oldElem;
							keysMap.put(key, val + 1);
						}
					} else {
						GapList<E> list;
						if (oldElem instanceof KeyMapList) {
							list = (GapList<E>) oldElem;
							list.add(elem);
						} else {
							list = new KeyMapList<E>();
							list.addArray((E) oldElem, elem);
						}
						keysMap.put(key, list);
					}
				}

			} else {
				// Sorted keys
				int addIndex = 0;
				if (!keysList.isEmpty()) {
					if (comparator.compare(key, keysList.getLast()) > 0) {
						addIndex = -keysList.size() - 1;
					} else if (comparator.compare(key, keysList.getFirst()) < 0) {
						addIndex = -1;
					}
				}
				if (addIndex == 0) {
					addIndex = SortedLists.binarySearchAdd(keysList, key, comparator);
				}
				boolean add = false;
				if (addIndex < 0) {
					// New key
					addIndex = -addIndex - 1;
					add = true;
				} else {
					// Existing key
					if (allowDuplicates || (key == null && allowDuplicatesNull)) {
						add = true;
					}
				}
				if (!add) {
					errorDuplicateKey(key);
				}
				keysList.doAdd(addIndex, key);
			}
		}

		/**
		 * Remove element from key map.
		 *
		 * @param key			key of object to remove
		 * @param matchValue	true if value must match to remove entry
		 * @param value			value of object to remove
		 * @param keyColl		key collection which stores object
		 * @return				removed object
		 */
		Option<E> remove(Object key, boolean matchValue, Object value, KeyCollectionImpl keyColl) {
			// If list cannot contain null, handle null explicitly to prevent NPE
			if (key == null) {
				if (!allowNull) {
					return Option.EMPTY();
				}
			}

			if (keysMap != null) {
				// Collection or unsorted list
				if (!keysMap.containsKey(key)) {
					return Option.EMPTY();
				}
				if (count) {
					assert (!matchValue || key == value);
					Integer val = (Integer) keysMap.get(key);
					if (val == 1) {
						keysMap.remove(key);
					} else {
						keysMap.put((K) key, val - 1);
					}
					return new Option(key);
				} else {
					E elem = null;
					Object obj = keysMap.get(key);
					if (obj instanceof KeyMapList) {
						GapList<E> list = (GapList<E>) obj;
						if (matchValue) {
							if (!list.remove(value)) {
								return Option.EMPTY();
							} else {
								elem = (E) value;
							}
						} else {
							elem = list.removeFirst();
						}
						if (list.isEmpty()) {
							keysMap.remove(key);
						}
					} else {
						elem = (E) keysMap.remove(key);
					}
					return new Option(elem);
				}
			} else {
				// Sorted list
				int index = keysList.binarySearch(key, (Comparator<Object>) comparator);
				E elem = null;
				if (index < 0) {
					return Option.EMPTY();
				}
				elem = (E) keyColl.keyList.doGet(index);
				keysList.remove(index);
				return new Option(elem);
			}
		}

		/**
		 * Removes element by key.
		 * If there are duplicates, all elements are removed.
		 *
		 * @param keyMap	key map
		 * @param key   	key of element to remove
		 * @param coll      collection to store all removed elements, null to not store them
		 */
		private void doRemoveAllByKey(K key, KeyCollectionImpl<E> keyColl, Collection<E> coll) {
			// If list cannot contain null, handle null explicitly to prevent NPE
			if (key == null) {
				if (!allowNull) {
					return;
				}
			}
			if (keysMap != null) {
				// Collection or unsorted list
				if (!keysMap.containsKey(key)) {
					return;
				}
				Object obj = keysMap.remove(key);
				if (coll != null) {
					if (obj instanceof KeyMapList) {
						coll.addAll((GapList<E>) obj);
					} else {
						coll.add((E) obj);
					}
				}

			} else {
				// Sorted list
				int index = SortedLists.binarySearchGet(keysList, key, comparator);
				if (index < 0) {
					return;
				}
				int start = index;
				while (true) {
					index++;
					if (index == keysList.size()) {
						break;
					}
					if (!GapList.equalsElem(keysList.get(index), key)) {
						break;
					}
				}
				if (coll != null) {
					coll.addAll(keyColl.keyList.list.getAll(start, index - start));
				}
				keysList.remove(start, index - start);
			}
		}

		Set<K> getDistinctKeys() {
			if (keysMap != null) {
				Set<K> set = keysMap.keySet();
				if (comparator != null) {
					TreeSet treeSet = new TreeSet(comparator);
					treeSet.addAll(set);
					return treeSet;
				} else {
					return new HashSet(set);
				}
			} else {
				K lastKey = null;
				TreeSet<K> set = new TreeSet<K>(comparator);
				for (int i = 0; i < keysList.size(); i++) {
					K key = keysList.get(i);
					boolean add = false;
					if (set.isEmpty()) {
						add = true;
					} else {
						if (key != null) {
							add = !key.equals(lastKey);
						} else {
							add = (lastKey != null);
						}
					}
					if (add) {
						set.add(key);
						lastKey = key;
					}
				}
				return set;
			}
		}
	}

	/**
	 * List type used to store multiple elements.
	 * We need this distinct type to distinguish it from a normal GapList
	 * in a KeyCollection&lt;GapList&lt;String&gt;&gt;.
	 */
	static class KeyMapList<E> extends GapList<E> {
		public KeyMapList() {
			super();
		}

		public KeyMapList(KeyMapList that) {
			super(that);
		}
	}

	//-- KeyCollection --

	/** If true the invariants are checked for debugging */
	private static final boolean DEBUG_CHECK = false;

	/**
	 * Size of collection.
	 * The size is cached, as the key maps do not know the size if duplicates are allowed.
	 */
	int size;
	/** Maximum absolute or windows size, 0 if this list has no size restriction */
	int maxSize;
	/** If maxSize is >0, this boolean indicates whether the size is for window (true) or absolute (false) */
	boolean movingWindow;
	/**
	 * Maps for element and all defined keys.
	 * keyMaps may be null for a KeyListImpl without keys.
	 * Index 0 is reserved for the elem key using an IdentMapper.
	 * If there is no elem key, keyMaps[0] contains null.
	 */
	KeyMap<E, Object>[] keyMaps;
	/**
	 * Index of key map which defines order
	 * (-1 for no order, only possible for KeyList).
	 * If an order key is defined for a KeyList, it must be implemented as KeyMap.keysList.
	 */
	int orderByKey;
	/**
	 * True to allow null elements, false to reject them.
	 */
	boolean allowNullElem;
	/**
	 * true if collections implements {@link Set}, false for @{link Collection} (behavior of {@link #add} changes)
	 */
	boolean setBehavior;
	/**
	 * All elements in the list must fulfill this predicate, if null, all elements are allowed
	 */
	Predicate<E> constraint;
	// -- handlers
	Consumer<E> beforeInsertTrigger;
	Consumer<E> afterInsertTrigger;
	Consumer<E> beforeDeleteTrigger;
	Consumer<E> afterDeleteTrigger;
	/**
	 * Back pointer to KeyListImpl if this object is used to implement a KeyList, Key1List, Key2List.
	 * Otherwise null if it is part of a KeyCollection, Key1Collection, Key2Collection.
	 */
	KeyListImpl<E> keyList;

	//

	/**
	 * Private constructor.
	 */
	KeyCollectionImpl() {
	}

	/**
	 * Initialize object for copy() operation.
	 *
	 * @param that source object
	 */
	void initCopy(KeyCollectionImpl<E> that) {
		size = that.size;
		// keyList is copied later
		keyList = that.keyList;
		// Copy keyMaps
		if (that.keyMaps != null) {
			keyMaps = new KeyMap[that.keyMaps.length];
			for (int i = 0; i < keyMaps.length; i++) {
				if (that.keyMaps[i] != null) {
					keyMaps[i] = that.keyMaps[i].copy();
				}
			}
		}
		maxSize = that.maxSize;
		movingWindow = that.movingWindow;
		allowNullElem = that.allowNullElem;
		constraint = that.constraint;
		orderByKey = that.orderByKey;
		beforeInsertTrigger = that.beforeInsertTrigger;
		afterInsertTrigger = that.afterInsertTrigger;
		beforeDeleteTrigger = that.beforeDeleteTrigger;
		afterDeleteTrigger = that.afterDeleteTrigger;
	}

	/**
	 * Initialize object for crop() operation.
	 *
	 * @param that source object
	 */
	void initCrop(KeyCollectionImpl<E> that) {
		size = 0;
		// keyList is copied later
		keyList = that.keyList;
		// Copy keyMaps
		if (that.keyMaps != null) {
			keyMaps = new KeyMap[that.keyMaps.length];
			for (int i = 0; i < keyMaps.length; i++) {
				if (that.keyMaps[i] != null) {
					keyMaps[i] = that.keyMaps[i].crop();
				}
			}
		}
		maxSize = that.maxSize;
		movingWindow = that.movingWindow;
		allowNullElem = that.allowNullElem;
		constraint = that.constraint;
		orderByKey = that.orderByKey;
		beforeInsertTrigger = that.beforeInsertTrigger;
		afterInsertTrigger = that.afterInsertTrigger;
		beforeDeleteTrigger = that.beforeDeleteTrigger;
		afterDeleteTrigger = that.afterDeleteTrigger;
	}

	/**
	 * Private method to check invariant of KeyCollectionImpl.
	 * It is only used for debugging.
	 */
	void debugCheck() {
		if (keyMaps != null) {
			for (KeyMap<E, ?> keyMap : keyMaps) {
				if (keyMap != null) {
					doDebugCheck(keyMap);
				}
			}
		}
	}

	private void doDebugCheck(KeyMap keyMap) {
		if (keyMap.keysMap != null) {
			int count = 0;
			if (keyMap.count) {
				for (Object val : keyMap.keysMap.values()) {
					count += ((Integer) val);
				}
			} else {
				for (Object obj : keyMap.keysMap.values()) {
					if (obj instanceof KeyMapList) {
						count += ((KeyMapList) obj).size();
					} else {
						count++;
					}
				}
			}
			assert (count == size());
		} else if (keyMap.keysList != null) {
			assert (keyMap.keysList.size() == size());
			IList<?> copy = keyMap.keysList.copy();
			copy.sort(keyMap.comparator);
			assert (copy.equals(keyMap.keysList));
		} else {
			assert (false);
		}
	}

	// for KeyListImpl

	Object getKey(int keyIndex, E elem) {
		return keyMaps[keyIndex].getKey(elem);
	}

	/**
	 * Determines whether this list is sorted or not.
	 *
	 * @return true if this a sorted list, false if not
	 */
	public boolean isSorted() {
		return orderByKey != -1;
	}

	boolean isSortedByElem() {
		return orderByKey == 0;
	}

	Comparator getElemSortComparator() {
		Comparator comparator = keyMaps[orderByKey].comparator;
		if (comparator instanceof NaturalComparator) {
			return null;
		}
		return comparator;
	}

	boolean hasElemSet() {
		return keyMaps != null && keyMaps[0] != null;
	}

	/**
	 * Check whether index is valid for the sorted list.
	 */
	void checkIndex(int loIndex, int hiIndex, E elem) {
		KeyMap keyMap = keyMaps[orderByKey];
		Object key = keyMap.getKey(elem);
		IList<Object> list = keyMap.keysList;
		Comparator<Object> comp = keyMap.comparator;
		if (loIndex >= 0) {
			int cmp = comp.compare(list.doGet(loIndex), key);
			if (cmp == 0) {
				if (elem != null) {
					if (!keyMap.allowDuplicates) {
						cmp = 1;
					}
				} else {
					if (!keyMap.allowDuplicatesNull) {
						cmp = 1;
					}
				}
			}
			if (cmp > 0) {
				errorInvalidIndex();
			}
		}
		if (hiIndex < list.size()) {
			int cmp = comp.compare(key, list.doGet(hiIndex));
			if (cmp == 0) {
				if (elem != null) {
					if (!keyMap.allowDuplicates) {
						errorDuplicateKey(key);
					}
				} else {
					if (!keyMap.allowDuplicatesNull) {
						errorDuplicateKey(key);
					}
				}
			}
			if (cmp > 0) {
				errorInvalidIndex();
			}
		}
	}

	/**
	 * Called from KeyListImpl.doAdd() to add element to sorted list.
	 * It calls beforeInsert() and afterInsert() as needed.
	 *
	 * @param index	index where element will be added
	 * @param elem	element to add
	 */
	void addSorted(int index, E elem) {
		// Check whether index is correct for adding element in a sorted list
		checkIndex(index - 1, index, elem);

		beforeInsert(elem);

		// Index is correct
		KeyMap keyMap = keyMaps[orderByKey];
		Object key = keyMap.getKey(elem);
		IList<Object> list = keyMap.keysList;

		if (doAdd(elem, keyMap)) {
			size++;
		}
		list.doAdd(index, key);

		afterInsert(elem);
	}

	/**
	 * Called from KeyListImpl.doAdd() to add element to sorted list.
	 * It calls beforeInsert() and afterInsert() as needed.
	 *
	 * @param elem	element to add
	 */
	void addUnsorted(E elem) {
		beforeInsert(elem);
		if (doAdd(elem, null)) {
			size++;
		}
		afterInsert(elem);
	}

	// Called from KeyListImpl.doSet
	void setSorted(int index, E elem, E oldElem) {
		// Check whether index is correct for setting element in a sorted list
		checkIndex(index - 1, index + 1, elem);

		// Index is correct
		KeyMap keyMap = keyMaps[orderByKey];
		Object key = keyMap.getKey(elem);
		IList<Object> list = keyMap.keysList;

		beforeDelete(oldElem);
		beforeInsert(elem);
		doRemove(oldElem, keyMap);
		try {
			doAdd(elem, keyMap);
		} catch (RuntimeException e) {
			// adding failed due to violated constraint, so roll back change
			doAdd(oldElem, keyMap);
			throw e;
		}
		list.doSet(index, key);
		afterDelete(elem);
		afterInsert(elem);
	}

	int binarySearchSorted(E elem) {
		KeyMap<E, Object> keyMap = keyMaps[orderByKey];
		Object key = keyMap.getKey(elem);
		int index = keyMap.keysList.binarySearch(key, keyMap.comparator);
		if (index >= 0) {
			index++;
			while (index < keyMap.keysList.size()) {
				if (keyMap.comparator.compare(keyMap.keysList.get(index), key) != 0) {
					break;
				}
				index++;
			}
		}
		return index;
	}

	int indexOfSorted(E elem) {
		KeyMap<E, Object> keyMap = keyMaps[orderByKey];
		Object key = keyMap.getKey(elem);
		int index = keyMap.keysList.binarySearch(key, keyMap.comparator);
		return (index < 0) ? -1 : index;
	}

	//

	/**
	 * Checks whether element is allowed in collection (null/constraint).
	 * The constraint is also checked if the element is null.
	 *
	 * @param elem element to check
	 * @throws IllegalArgumentException if the element is not allowed
	 */
	void checkElemAllowed(E elem) {
		if (elem == null) {
			if (!allowNullElem) {
				errorNullElement();
			}
		}
		if (constraint != null) {
			if (!constraint.test(elem)) {
				errorConstraintElement();
			}
		}
	}

	static void errorNullElement() {
		throw new KeyException("Constraint violation: null element not allowed");
	}

	static void errorConstraintElement() {
		throw new KeyException("Constraint violation: element not allowed");
	}

	static void errorNullKey() {
		throw new KeyException("Constraint violation: null key not allowed");
	}

	static void errorMaxSize() {
		throw new KeyException("Constraint violation: maximum size reached");
	}

	static void errorDuplicateKey(Object key) {
		throw new DuplicateKeyException(key);
	}

	static void errorInvalidData() {
		throw new IllegalStateException("Invalid data: call update() on change of key data");
	}

	static void errorInvalidIndex() {
		throw new IllegalStateException("Invalid index for sorted list");
	}

	static void errorInvalidateNotSupported() {
		throw new IllegalStateException("Invalidate is not support if elemCount is true");
	}

	static void errorInvalidSetBehavior() {
		throw new IllegalStateException("Invalid configuration: setBehavior must be true");
	}

	static void errorInvaliDuplicates() {
		throw new IllegalStateException("Invalid configuration: duplicates are not allowed");
	}

	/**
	 * This method is called before a new element is added.
	 * If the addition should not happen, an exception can be thrown.
	 *
	 * @param elem	element to insert
	 */
	private void beforeInsert(E elem) {
		if (beforeInsertTrigger != null) {
			beforeInsertTrigger.accept(elem);
		}
	}

	/**
	 * This method is called after a new element has been added.
	 *
	 * @param elem	element which has been inserted
	 */
	private void afterInsert(E elem) {
		if (afterInsertTrigger != null) {
			afterInsertTrigger.accept(elem);
		}
	}

	/**
	 * This method is called before an existing element is removed.
	 * If the deletion should not happen, an exception can be thrown.
	 *
	 * @param elem	element to delete
	 */
	private void beforeDelete(E elem) {
		if (beforeDeleteTrigger != null) {
			beforeDeleteTrigger.accept(elem);
		}
	}

	/**
	 * This method is called after an existing element has been removed.
	 *
	 * @param elem	element which has been deleted
	 */
	private void afterDelete(E elem) {
		if (afterDeleteTrigger != null) {
			afterDeleteTrigger.accept(elem);
		}
	}

	@Override
	public boolean add(E elem) {
		// This method is also used by addAll()
		checkAddElem(elem);
		beforeInsert(elem);

		try {
			if (doAdd(elem, null)) {
				size++;
			}
		} catch (DuplicateKeyException ex) {
			if (setBehavior) {
				return false;
			}
			throw ex;
		}

		if (DEBUG_CHECK)
			debugCheck();
		afterInsert(elem);
		return true;
	}

	@Override
	public boolean remove(Object elem) {
		return remove(elem, null);
	}

	void checkAddElem(E elem) {
		checkElemAllowed(elem);
		if (maxSize != 0 && size >= maxSize) {
			errorMaxSize();
		}
	}

	/**
	 * Remove element.
	 *
	 * @param elem		element to remove
	 * @param ignore	KeyMap to ignore (null to remove element from all key maps)
	 * @return			true if element has been removed
	 */
	boolean remove(Object elem, KeyMap ignore) {
		beforeDelete((E) elem);
		Option<E> removed = doRemove(elem, ignore);
		if (removed.hasValue() || ignore != null) {
			size--;
		}
		if (DEBUG_CHECK)
			debugCheck();
		afterDelete((E) elem);
		return removed.hasValue();
	}

	/**
	 * Adds or replaces element.
	 * If there is no such element, the element is added.
	 * If there is such an element, the element is replaced.
	 * So said simply, it is a shortcut for the following code:
	 * <pre>
	 * if (contains(elem)) {
	 *   remove(elem);
	 * }
	 * add(elem);
	 * </pre>
	 * However the method is atomic in the sense that all or none operations are executed.
	 * So if there is already such an element, but adding the new one fails due to a constraint violation,
	 * the old element remains in the list.
	 *
	 * @param elem	element
	 * @return		element which has been replaced or null otherwise
	 */
	protected E put(E elem) {
		return putByKey(0, elem);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean contains(Object o) {
		if (keyMaps[0] != null) {
			return keyMaps[0].containsKey(o);
		} else {
			// Try to use containsKey if a map is defined (fast)
			for (int i = 1; i < keyMaps.length; i++) {
				KeyMap<E, Object> km = keyMaps[i];
				if (km == null) {
					continue;
				}
				try {
					Object key = km.getKey((E) o);
					if (km.isPrimaryMap()) {
						return km.containsKey(key);
					} else {
						return km.containsValue(key, o);
					}
				} catch (Exception e) {
					// This can happen if an object of a wrong type is passed where applying the mapper function to extract the key fails 
				}
			}
			// Otherwise use containsValue (slow)
			return keyMaps[1].containsValue(o);
		}
	}

	@Override
	public Iterator<E> iterator() {
		return keyMaps[orderByKey].iteratorValues(this);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean added = false;
		for (E e : c) {
			if (add(e)) {
				added = true;
			}
		}
		return added;
	}

	/**
	 * Returns all elements contained in this collection as list.
	 *
	 * @return all elements contained in this collection as list
	 */
	public GapList<E> toList() {
		GapList<E> list = new GapList(size());
		for (E e : this) {
			list.add(e);
		}
		return list;
	}

	@Override
	public Object[] toArray() {
		GapList<Object> list = new GapList(size());
		for (E e : this) {
			list.add(e);
		}
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		GapList<Object> list = new GapList(size());
		for (E e : this) {
			list.add(e);
		}
		return list.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object e : c) {
			if (!contains(e)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		if (c.size() < size()) {
			for (Iterator<?> i = c.iterator(); i.hasNext();) {
				if (remove(i.next())) {
					changed = true;
				}
			}
		} else {
			for (Iterator<?> i = iterator(); i.hasNext();) {
				if (c.contains(i.next())) {
					i.remove();
					changed = true;
				}
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = false;
		for (Iterator<?> i = iterator(); i.hasNext();) {
			if (!c.contains(i.next())) {
				i.remove();
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		boolean first = true;
		for (Iterator<E> iter = iterator(); iter.hasNext();) {
			if (!first) {
				buf.append(", ");
			} else {
				first = false;
			}
			buf.append(iter.next());
		}
		buf.append("]");
		return buf.toString();
	}

	/**
	 * Remove element.
	 * It does not adjust the size and does not call triggers.
	 *
	 * @param elem		element to remove
	 * @param ignore	KeyMap to ignore (null to remove element from all key maps)
	 * @return			optional with element which has been removed
	 */
	Option<E> doRemove(Object elem, KeyMap ignore) {
		Option<E> removed = Option.EMPTY();
		boolean first = true;
		if (keyMaps != null) {
			for (int i = 0; i < keyMaps.length; i++) {
				if (keyMaps[i] != null && keyMaps[i] != ignore) {
					Object key = keyMaps[i].getKey((E) elem);
					Option<E> obj = keyMaps[i].remove(key, true, elem, this);
					if (first) {
						if (!obj.hasValue()) {
							return Option.EMPTY();
						} else {
							removed = obj;
						}
						first = false;
					} else {
						if (!obj.hasValue() || !obj.getValue().equals(removed.getValue())) {
							errorInvalidData();
						}
					}
				}
			}
		}
		return removed;
	}

	/**
	 * Returns a copy of this collection with all its elements.
	 * The new collection will use the same comparator, ordering, etc.
	 *
	 * @return  a copy of this collection
	 */
	public KeyCollectionImpl copy() {
		try {
			KeyCollectionImpl copy = (KeyCollectionImpl) super.clone();
			copy.initCopy(this);
			if (DEBUG_CHECK)
				copy.debugCheck();
			return copy;
		} catch (CloneNotSupportedException e) {
			// This shouldn't happen, since we are Cloneable
			throw new AssertionError(e);
		}
	}

	/**
	 * Returns a copy of this collection but without elements.
	 * The new collection will use the same comparator, ordering, etc.
	 *
	 * @return  an empty copy of this collection
	 */
	public KeyCollectionImpl crop() {
		try {
			KeyCollectionImpl copy = (KeyCollectionImpl) super.clone();
			copy.initCrop(this);
			if (DEBUG_CHECK)
				copy.debugCheck();
			return copy;
		} catch (CloneNotSupportedException e) {
			// This shouldn't happen, since we are Cloneable
			throw new AssertionError(e);
		}
	}

	@Override
	protected Object clone() {
		return copy();
	}

	/**
	 * Initialize this object after the bitwise copy has been made
	 * by Object.clone().
	 *
	 * @param that	source object
	 */
	protected void initClone(Object that) {
	}

	/**
	 * Returns a Set view of the element set.
	 *
	 * @return set view
	 * @throws IllegalArgumentException if the element set cannot be viewed as Set
	 */
	public Set<E> asSet() {
		return new KeyCollectionAsSet(this, false);
	}

	@Override
	public void clear() {
		if (keyMaps != null) {
			for (KeyMap<E, Object> keyMap : keyMaps) {
				if (keyMap != null) {
					doClear(keyMap);
				}
			}
		}
		size = 0;
	}

	private void doClear(KeyMap<E, ?> keyMap) {
		if (keyMap.keysMap != null) {
			keyMap.keysMap.clear();
		} else {
			keyMap.keysList.clear();
		}
	}

	/**
	 * Add element.
	 * This method does not change size, check whether the element may be added or calls any triggers,
	 * so this must be done by the calling method.
	 *
	 * @param elem		element to add
	 * @param ignore	KeyMap to ignore (null to add element to all key maps)
	 * @return			true if element has been added, false if not (no key maps)
	 */
	boolean doAdd(E elem, KeyMap ignore) {
		if (keyMaps == null) {
			return false;
		}
		RuntimeException error = null;
		int i = 0;
		try {
			for (i = 0; i < keyMaps.length; i++) {
				if (keyMaps[i] != null && keyMaps[i] != ignore) {
					Object key = keyMaps[i].getKey(elem);
					keyMaps[i].add(key, elem);
				}
			}
		} catch (RuntimeException e) {
			error = e;
		}

		// If an error occurred, roll back changes
		if (error != null) {
			for (i--; i >= 0; i--) {
				if (keyMaps[i] != null) {
					Object key = keyMaps[i].getKey(elem);
					keyMaps[i].remove(key, true, elem, this);
				}
			}
			if (error != null) {
				if (DEBUG_CHECK)
					debugCheck();
				throw error;
			}
		}
		return true;
	}

	/**
	 * Checks whether the specified key exists in this list.
	 *
	 * @param keyIndex  key index
	 * @param key 		key to look for
	 * @return  		true if the key exists, otherwise false
	 */
	protected <K> boolean containsKey(int keyIndex, K key) {
		return getKeyMap(keyIndex).containsKey(key);
	}

	/**
	 * Returns set containing all distinct keys.
	 *
	 * @param keyIndex	key index
	 * @return 			list containing all distinct keys
	 */
	protected Set<?> getDistinctKeys(int keyIndex) {
		return getKeyMap(keyIndex).getDistinctKeys();
	}

	/**
	 * Returns list containing all keys in element order.
	 *
	 * @param keyIndex	key index
	 * @return 			list containing all keys
	 */
	protected IList<?> getAllKeys(int keyIndex) {
		Function mapper = getKeyMap(keyIndex).mapper;
		GapList<Object> list = GapList.create();
		for (Object obj : this) {
			list.add(mapper.apply(obj));
		}
		return list;
	}

	/**
	 * Check whether there is a KeyMap (can also be the elements set) with specified index.
	 *
	 * @param keyIndex 	key map index
	 * @throws IllegalArgumentException if there is no such key map
	 */
	void checkKeyMap(int keyIndex) {
		if (keyMaps == null || keyIndex >= keyMaps.length || keyIndex < 0 || keyMaps[keyIndex] == null) {
			throw new IllegalArgumentException("Invalid key index: " + keyIndex);
		}
	}

	/**
	 * Check whether the key map can be viewed as Map.
	 *
	 * @param keyIndex 	key map index
	 * @throws IllegalArgumentException if the key map cannot be viewed as Map
	 */
	void checkAsMap(int keyIndex) {
		if (keyMaps == null || keyIndex >= keyMaps.length || keyIndex <= 0 || keyMaps[keyIndex] == null) {
			throw new IllegalArgumentException("Invalid key index: " + keyIndex);
		}
		if (keyMaps[keyIndex].allowDuplicates || keyMaps[keyIndex].allowDuplicatesNull) {
			throw new IllegalArgumentException("Key map must not allow duplicates");
		}
	}

	/**
	 * Check whether the element set can be viewed as Set.
	 *
	 * @param keyIndex 	key map index
	 * @throws IllegalArgumentException if the element set cannot be viewed as Set
	 */
	void checkAsSet() {
		if (keyMaps == null || keyMaps[0] == null) {
			throw new IllegalArgumentException("No element set");
		}
		if (keyMaps[0].allowDuplicates || keyMaps[0].allowDuplicatesNull) {
			throw new IllegalArgumentException("Element set must not allow duplicates");
		}
	}

	KeyMap<E, Object> getKeyMap(int keyIndex) {
		checkKeyMap(keyIndex);
		return keyMaps[keyIndex];
	}

	/**
	 * Returns mapper for specified key map.
	 *
	 * @param keyIndex 	key index
	 * @return      	mapper for specified key map
	 */
	protected Function<E, Object> getKeyMapper(int keyIndex) {
		return getKeyMap(keyIndex).mapper;
	}

	/**
	 * Returns value for specified key.
	 * If there are several values for this key, the first is returned.
	 * If the key is not found, null is returned.
	 *
	 * @param keyIndex 	key index
	 * @param key   	key to find
	 * @return      	value of specified key or null
	 */
	protected E getByKey(int keyIndex, Object key) {
		return getByKey(getKeyMap(keyIndex), key);
	}

	private <K> E getByKey(KeyMap<E, K> keyMap, K key) {
		// Handle null key if not allowed to prevent NPE
		if (key == null) {
			if (!keyMap.allowNull) {
				return null;
			}
		}

		if (keyMap.keysMap != null) {
			// not sorted
			Object obj = keyMap.keysMap.get(key);
			if (obj instanceof KeyMapList) {
				GapList<E> list = (GapList<E>) obj;
				return list.getFirst();
			} else {
				return (E) obj;
			}

		} else {
			// sorted
			int index = SortedLists.binarySearchGet(keyMap.keysList, key, keyMap.comparator);
			if (index >= 0) {
				return keyList.doGet(index);
			} else {
				return null;
			}
		}
	}

	/**
	 * Returns a collection with all elements with the specified key.
	 *
	 * @param keyIndex	key index
	 * @param key   	key which elements must have
	 * @return      	list with all elements
	 */
	protected Collection<E> getAllByKey(int keyIndex, Object key) {
		Collection<E> coll = crop();
		getAllByKey(keyIndex, key, coll);
		return coll;
	}

	/**
	 * Fill the collection with all elements with the specified key.
	 *
	 * @param keyIndex	key index
	 * @param key   	key which elements must have
	 * @param coll      collection with all elements
	 */
	protected void getAllByKey(int keyIndex, Object key, Collection<E> coll) {
		doGetAllByKey(getKeyMap(keyIndex), key, coll);
	}

	private <K> void doGetAllByKey(KeyMap<E, K> keyMap, K key, Collection<E> coll) {
		// Handle null key if not allowed to prevent NPE
		if (key == null) {
			if (!keyMap.allowNull) {
				return;
			}
		}

		if (keyMap.keysMap != null) {
			// not sorted
			Object obj = keyMap.keysMap.get(key);
			if (obj == null) {
				;
			} else if (obj instanceof KeyMapList) {
				coll.addAll((GapList<E>) obj);
			} else {
				coll.add((E) obj);
			}

		} else {
			// sorted
			// - keyMap.keysList: contains sorted keys
			// - keyList: contains elements, sorted by key
			int index = SortedLists.binarySearchGet(keyMap.keysList, key, keyMap.comparator);
			if (index >= 0) {
				while (true) {
					coll.add(keyList.doGet(index));
					index++;
					if (index == keyMap.keysList.size()) {
						break;
					}
					if (!GapList.equalsElem(keyMap.keysList.get(index), key)) {
						break;
					}
				}
			}
		}
	}

	/**
	 * Returns number of elements with specified key.
	 *
	 * @param keyIndex 	key index
	 * @param key   	key which elements must have
	 * @return      	number of elements with key (-1 if key is null)
	 */
	protected int getCountByKey(int keyIndex, Object key) {
		return getCountByKey(getKeyMap(keyIndex), key);
	}

	private <K> int getCountByKey(KeyMap<E, K> keyMap, K key) {
		// Handle null key if not allowed to prevent NPE
		if (key == null) {
			if (!keyMap.allowNull) {
				return 0;
			}
		}

		if (keyMap.keysMap != null) {
			// not sorted
			if (keyMap.count) {
				Integer val = (Integer) keyMap.keysMap.get(key);
				if (val == null) {
					return 0;
				} else {
					return val;
				}
			} else {
				Object obj = keyMap.keysMap.get(key);
				if (obj == null) {
					return 0;
				} else if (obj instanceof KeyMapList) {
					GapList<E> list = (GapList<E>) obj;
					return list.size();
				} else {
					return 1;
				}
			}
		} else {
			// sorted
			int index = SortedLists.binarySearchGet(keyMap.keysList, key, keyMap.comparator);
			if (index >= 0) {
				int count = 0;
				while (true) {
					count++;
					index++;
					if (index == keyMap.keysList.size()) {
						break;
					}
					if (!GapList.equalsElem(keyMap.keysList.get(index), key)) {
						break;
					}
				}
				return count;
			} else {
				return 0;
			}
		}
	}

	/**
	 * Invalidate element, i.e. all keys of the element are extracted
	 * again and stored in the key maps. Old key values are removed
	 * if needed.
	 * You must call an invalidate method if an element's key value has changed after adding it to the collection.
	 *
	 * @param elem element to invalidate
	 */
	protected void invalidate(E elem) {
		if (keyMaps != null) {
			for (int i = 0; i < keyMaps.length; i++) {
				if (keyMaps[i] != null) {
					if (i == 0 && keyMaps[0].count) {
						errorInvalidateNotSupported();
					}
					Option<Object> key = invalidate(keyMaps[i], elem);
					if (key.hasValue()) {
						keyMaps[i].add(key.getValue(), elem);
					}
				}
			}
		}
		if (DEBUG_CHECK)
			debugCheck();
	}

	/**
	 * Invalidate key value of element.
	 * You must call an invalidate method if an element's key value has changed after adding it to the collection.
	 *
	 * @param keyIndex	key index
	 * @param oldKey	old key value
	 * @param newKey	new key value
	 * @param elem		element to invalidate (can be null if there are no duplicates with this key)
	 */
	protected void invalidateKey(int keyIndex, Object oldKey, Object newKey, E elem) {
		doInvalidateKey(keyIndex, oldKey, newKey, elem);
	}

	E doInvalidateKey(int keyIndex, Object oldKey, Object newKey, E elem) {
		KeyMap keyMap = getKeyMap(keyIndex);
		Option<Object> removed;
		if (elem == null) {
			removed = keyMap.remove(oldKey, false, null, this);
		} else {
			removed = keyMap.remove(oldKey, true, elem, this);
		}
		if (!removed.hasValue()) {
			errorInvalidData();
		}
		keyMap.add(newKey, removed.getValue());
		if (DEBUG_CHECK)
			debugCheck();
		return (E) removed.getValue();
	}

	/**
	 * @param keyMap	key map
	 * @param elem		elem to invalidate
	 * @return			null if key for keyMap and element is correct, else key which must be added to keymap
	 */
	private Option<Object> invalidate(KeyMap keyMap, Object elem) {
		boolean allowDuplicates = keyMap.allowDuplicates;
		Object key = keyMap.getKey(elem);

		if (keyMap.keysMap != null) {
			Iterator<Map.Entry> iter = keyMap.keysMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = iter.next();
				if (GapList.equalsElem(elem, entry.getValue())) {
					if (GapList.equalsElem(key, entry.getKey())) {
						return Option.EMPTY();
					}
					iter.remove();
					if (!allowDuplicates) {
						break;
					}
				}
			}
		} else {
			assert (keyMap.keysList != null);
			for (int i = 0; i < keyMap.keysList.size(); i++) {
				if (GapList.equalsElem(elem, keyList.doGet(i))) {
					if (GapList.equalsElem(key, keyMap.keysList.get(i))) {
						return Option.EMPTY();
					}
					keyMap.keysList.remove(i);
					if (!allowDuplicates) {
						break;
					}
				}
			}
		}
		return new Option(key);
	}

	protected E removeByKey(int keyIndex, Object key) {
		return doRemoveByKey(keyIndex, key).getValueOrNull();
	}

	/**
	 * Removes element by key.
	 * It adjusts the size, but does not call triggers.
	 * If there are duplicates, only one element is removed.
	 *
	 * @param keyIndex	index of key map
	 * @param key   	key of element to remove
	 * @return      	option with removed element as value or option without value if no element has been removed
	 */
	protected Option<E> doRemoveByKey(int keyIndex, Object key) {
		checkKeyMap(keyIndex);
		Option<E> removed = keyMaps[keyIndex].remove(key, false, null, this);
		if (removed.hasValue()) {
			E elem = removed.getValue();
			try {
				beforeDelete(elem);
			} catch (RuntimeException e) {
				keyMaps[keyIndex].add(key, elem);
				throw e;
			}
			for (int i = 0; i < keyMaps.length; i++) {
				if (i != keyIndex && keyMaps[i] != null) {
					E value = removed.getValue();
					Object k = keyMaps[i].getKey(value);
					keyMaps[i].remove(k, true, value, this);
				}
			}
			size--;
			afterDelete(elem);
		}
		if (DEBUG_CHECK)
			debugCheck();
		return removed;
	}

	/**
	 * Removes element by key.
	 * If there are duplicates, all elements are removed.
	 *
	 * @param keyIndex	key index
	 * @param key   	key of element to remove
	 * @return      	true if elements have been removed, false otherwise
	 */
	protected Collection<E> removeAllByKey(int keyIndex, Object key) {
		Collection<E> removeds = crop();
		removeAllByKey(keyIndex, key, removeds);
		return removeds;
	}

	@SuppressWarnings("unchecked")
	protected void removeAllByKey(int keyIndex, Object key, Collection<E> removeds) {
		checkKeyMap(keyIndex);
		keyMaps[keyIndex].doRemoveAllByKey(key, this, removeds);

		for (E elem : removeds) {
			try {
				beforeDelete(elem);
			} catch (RuntimeException e) {
				for (E elem2 : removeds) {
					keyMaps[keyIndex].add(key, elem2);
				}
				throw e;
			}
			for (int i = 0; i < keyMaps.length; i++) {
				if (i != keyIndex && keyMaps[i] != null) {
					Object k = keyMaps[i].getKey(elem);
					keyMaps[i].doRemoveAllByKey(k, this, null);
				}
			}
			afterDelete(elem);
			size--;
		}
		if (DEBUG_CHECK)
			debugCheck();
	}

	protected E putByKey(int keyIndex, E elem) {
		// Try to remove element
		Option<E> removed;
		if (keyIndex == 0) {
			removed = doRemove(elem, null);
			if (removed.hasValue()) {
				size--;
			}
		} else {
			Object key = getKey(keyIndex, elem);
			removed = doRemoveByKey(keyIndex, key);
		}
		if (removed.hasValue()) {
			try {
				beforeDelete(removed.getValue());
			} catch (RuntimeException e) {
				doAdd(removed.getValue(), null);
				throw e;
			}
		}

		try {
			beforeInsert(elem);
		} catch (RuntimeException e) {
			size++;
			doAdd(removed.getValue(), null);
			throw e;
		}

		// Add new element
		try {
			checkAddElem(elem);
			doAdd(elem, null);
			size++;
		} catch (RuntimeException e) {
			size++;
			doAdd(removed.getValue(), null);
			throw e;
		}

		// Call after triggers
		if (removed.hasValue()) {
			afterDelete(removed.getValue());
		}
		afterInsert(elem);

		if (DEBUG_CHECK)
			debugCheck();
		return removed.getValueOrNull();
	}

	// As in AbstractCollection
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Collection)) {
			return false;
		}
		Collection c = (Collection) o;
		if (c.size() != size()) {
			return false;
		}
		try {
			return containsAll(c);
		} catch (ClassCastException unused) {
			return false;
		} catch (NullPointerException unused) {
			return false;
		}
	}

	// As in AbstractCollection
	@Override
	public int hashCode() {
		int h = 0;
		Iterator<E> i = iterator();
		while (i.hasNext()) {
			E obj = i.next();
			if (obj != null) {
				h += obj.hashCode();
			}
		}
		return h;
	}

	//-- Element methods

	/**
	 * Returns all equal elements.
	 *
	 * @param elem	element
	 * @return		all equal elements (never null)
	 */
	protected Collection<E> getAll(E elem) {
		return getAllByKey(0, elem);
	}

	/**
	 * Returns the number of equal elements.
	 *
	 * @param elem	element
	 * @return		number of equal elements
	 */
	protected int getCount(E elem) {
		return getCountByKey(0, elem);
	}

	/**
	 * Removes all equal elements.
	 *
	 * @param elem	element
	 * @return		removed equal elements (never null)
	 */
	protected Collection<E> removeAll(E elem) {
		return removeAllByKey(0, elem);
	}

	/**
	 * Returns all distinct elements in the same order as in the collection.
	 *
	 * @return		distinct elements
	 */
	protected Set<E> getDistinct() {
		return (Set<E>) getDistinctKeys(0);
	}

	//-- Key methods
	// The key methods can not be defined here.
	// Due to the generic type parameters, the methods cannot be overridden.

}
