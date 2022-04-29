# High Performance Collections for Java

Brownies Collections complements the Java Collections Framework. The Classes sections contains an illustration to give you an overview of all the available functionality.

-    GapList combines the strengths of both ArrayList and LinkedList. It is implemented to offer both efficient random access to elements by index (as ArrayList does) and at the same time efficient adding and removing elements to and from beginning and end (as LinkedList does). It also exploits the locality of reference often seen in applications to further improve performance, e.g. for iterating over the list.

-    BigList is a list optimized for storing large number of elements. It stores the elements in fixed size blocks so for adding and removing only few elements must be moved. Blocks are split or merged as needed and maintained in a tree for fast access. Copying a BigList is very efficient as it is implemented using a copy-on-write approach.

-    Both GapList and BigList have been designed to be used as drop-in replacement for ArrayList, LinkedList or ArrayDeque by offering all their methods. Additionally many more helpful methods are available provided by the common abstract class IList.

-    There are specialized List implementations for all primitive data types. As the storage is realized using arrays of primitives, memory is saved and execution speed increased. These classes are named IntGapList/IntBigList, LongGapList/LongBigList, etc.

-    For each primitive list class like IntGapList there is also wrapper which allows you to access the primitive data through the standard List interface. With this approach you can save memory and continue to use the code working with lists. These classes are named IntObjGapList/IntObjBigList, LongObjGapList/LongObjBigList, etc.

-    To increase developer productivity, keys and constraints have been added to collections in an orthogonal and declarative way. These features offered by classes implementing the Collection (classes KeyCollection, Key1Collection, Key2Collection), the Set interface (classes KeySet, Key1Set, Key2Set), and the List interface (classes KeyList, Key1List, Key2List).


See full documentation on [magicwerk.org](http://www.magicwerk.org/page-collections-overview.html)
