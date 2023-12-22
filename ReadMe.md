# High Performance Collections for Java


Brownies-Collections complements the Java Collections Framework by providing general purpose implementations of the List interface which are both fast and powerful and introducing collections which feature the concept of keys and collections.

-    GapList is the fastest known implementation for reasonably sized lists as it combines the strengths of both ArrayList and LinkedList. It is implemented to offer both efficient random access to elements by index (as ArrayList does) and at the same time efficient adding and removing elements to and from beginning and end (as LinkedList does). It also exploits the locality of reference and access patterns in applications to further improve performance.

-    BigList is a list optimized for handling large number of elements. It stores the elements in fixed size blocks which are maintained in a tree for fast access and split or merged as needed. Copying a BigList is very efficient as it is implemented using a copy-on-write approach.

-    There are specialized List implementations for primitive data types. As the storage is realized using arrays of primitives, memory is saved. These classes are named IntGapList/IntBigList, etc.

-    There are also additional classes which allow you to access the primitive data through the standard List interface. With this approach you can save memory without changing the interface to your collections. These classes are named IntObjGapList/IntObjBigList, etc.

-    Both GapList and BigList have been designed to be used as drop-in replacement for ArrayList, LinkedList, or ArrayDeque by implementing all their interfaces and offering all their methods.

-    All list implementations (GapList, BigList, KeyList, etc.) inherit many powerful methods provided through the common abstract class IList. This often makes it possible to replace the costly and clumsy use of stream() with a single method.

-    There are additional collections featuring the integration of keys and constraints in an orthogonal and declarative way to increase developer productivity. These features are offered by classes implementing the Collection interface (classes KeyCollection, Key1Collection, Key2Collection), the Set interface (classes KeySet, Key1Set, Key2Set), and the List interface (classes KeyList, Key1List, Key2List).


See [magicwerk.org](http://www.magicwerk.org/page-collections-overview.html) for more information,
including the full [documentation](http://www.magicwerk.org/page-collections-documentation.html) with benchmarks.



## Download

Get the library from Maven Central.

Gradle:

```
api 'org.magicwerk.brownies:brownies-collections:0.9.22' 
```

Maven:

```
<dependency>
	<groupId>org.magicwerk.brownies</groupId>
	<artifactId>brownies-collections</artifactId>
	<version>0.9.22</version>
</dependency>
```
