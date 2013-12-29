/**
 * <p>
 * This packages contains collections extending the Java Collections Framework.
 * </p><p>
 * The class GapList combines the strengths of both ArrayList and LinkedList. It is implemented to offer both efficient random access to elements by index (as ArrayList does) and at the same time efficient adding and removing elements to and from beginning and end (as LinkedList does). It also exploits the locality of reference often seen in applications to further improve performance, e.g. for iterating over the list.
 * GapList has been designed to be used as drop-in replacement for both ArrayList and LinkedList by offering all their methods. It has a high test coverage and also passes the Guava testsuite for lists. Additionally many more helpful methods are available.
 * </p><p>
 * Additionally there are List implementations for all primitive data types. As the storage is realized using arrays of primitives, memory is saved and execution speed increased. For each primitive list class like IntGapList there is also wrapper class IntObjGapList which allows you to access the primitive data through the standard List interface.
 * </p><p>
 * To increase developer productivity, keys and constraints have been added to collections in an orthogonal and declarative way. These features offered by classes implementing the Collection (classes KeyCollection, Key1Collection, Key2Collection) and the List interface (classes KeyList, Key1List, Key2List).
 * </p>
 */
package org.magicwerk.brownies.collections;

