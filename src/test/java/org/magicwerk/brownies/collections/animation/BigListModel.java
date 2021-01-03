package org.magicwerk.brownies.collections.animation;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.magicwerk.brownies.collections.BigList;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.core.ObjectTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;

public class BigListModel {

	public static void main(String[] args) {
		test();
	}

	static void test() {
		int blockSize = 4;
		BigList<Integer> list = new BigList<Integer>(blockSize);
		print(list);
		list.addAll(Arrays.asList(1, 2, 3, 4, 5));
		print(list);

		System.out.println("-- copy");
		BigList<Integer> copy = list.copy();
		print(copy);

		copy.set(1, 8);
		print(list);
		print(copy);

		BigList<Integer> copy2 = list.copy();
		print(copy2);

		System.out.println("-- copy2");
		copy2.set(1, 9);
		print(list);
		print(copy);
		print(copy2);
	}

	static void print(BigList<?> list) {
		int numElems = getNumElems(list);
		int numBlocks = getNumBlocks(list);
		System.out.println("BigList: #" + ObjectTools.getIdentityHashCode(list));
		System.out.println("numElems: " + numElems);
		System.out.println("numBlocks: " + numBlocks);
		for (int i=0; i<numBlocks; i++) {
			int blockNumElems = getBlockNumElems(list, i);
			int blockRefCount = getBlockRefCount(list, i);
			Object block = getBlock(list, i);
			System.out.printf("Block: %d, #%s\n", i, ObjectTools.getIdentityHashCode(block));
			System.out.printf("  numElems: %s\n", blockNumElems);
			System.out.printf("  refCount: %s\n", blockRefCount);
			System.out.printf("  elems: %s\n", block);
		}
		System.out.println();
	}

	public static int getNumElems(BigList<?> list) {
		return list.size();
	}

	public static int getNumBlocks(BigList<?> list) {
		Object rootNode = ReflectTools.getAnyFieldValue(list, "rootNode");
		Object node = ReflectTools.invokeMethod("min", rootNode);
		int numBlocks = 0;
		while (node != null) {
			node = ReflectTools.invokeMethod("next", node);
			numBlocks++;
		}
		return numBlocks;
	}

	public static int getBlockNumElems(BigList<?> list, int block) {
		Object b = getBlock(list, block);
		int size = (Integer) ReflectTools.getAnyFieldValue(b, "size");
		return size;
	}

	public static int getBlockRefCount(BigList<?> list, int block) {
		Object b = getBlock(list, block);
		AtomicInteger refCount = (AtomicInteger) ReflectTools.getAnyFieldValue(b, "refCount");
		return refCount.get();
	}

	public static void setBlockRefCount(BigList<?> list, int block, int value) {
		Object b = getBlock(list, block);
		AtomicInteger refCount = (AtomicInteger) ReflectTools.getAnyFieldValue(b, "refCount");
		refCount.set(value);
	}

	static GapList<?> getBlock(BigList<?> list, int block) {
		Object rootNode = ReflectTools.getAnyFieldValue(list, "rootNode");
		Object node = ReflectTools.invokeMethod("min", rootNode);
		int i = 0;
		while (node != null) {
			if (i == block) {
				return (GapList<?>) ReflectTools.getAnyFieldValue(node, "block");
			}
			node = ReflectTools.invokeMethod("next", node);
			i++;
		}
		throw new IllegalArgumentException("Block index out of bounds: " + block);
	}

}