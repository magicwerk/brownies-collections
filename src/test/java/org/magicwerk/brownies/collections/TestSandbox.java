package org.magicwerk.brownies.collections;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Function;

import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.ExceptionTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.serialize.SerializeTools;

import ch.qos.logback.classic.Logger;

public class TestSandbox {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		test();
	}

	static void test() {
		testKey1ListSerializable();
	}

	static class Name implements Serializable {
		String name;

		public Name(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	static void testKey1ListSerializable() {
		Key1List<Name, String> list;
		list = new Key1List.Builder<Name, String>().withKey1Map(Name::getName).build();
		doSerializeFail(list);
		list = new Key1List.Builder<Name, String>().withKey1Map((Function<Name, String> & Serializable) Name::getName).build();
		doSerialize(list);
	}

	static void doSerializeFail(Object obj) {
		try {
			doSerialize(obj);
			CheckTools.check(false);
		} catch (RuntimeException e) {
			if (ExceptionTools.getBaseCause(e) instanceof NotSerializableException) {
				// ignore
				System.out.println(e);
			} else {
				throw e;
			}
		}
	}

	static void doSerialize(Object obj) {
		byte[] data = SerializeTools.toBinary(obj);
		Object obj2 = SerializeTools.fromBinary(data);
		CheckTools.check(obj.equals(obj2));
	}

	static void testBug091() {
		LOG.info("Bug 0.9.1");
		GapList<Integer> list1 = GapList.create(1, 2);
		GapList<Integer> list2 = GapList.create(1, 2, 3);
		list1.addAll(list2);
	}

	static void testBug09MikaelGrev() {
		LOG.info("Bug 0.9 MikaelGrev");
		String[] objs = new String[129];
		Arrays.fill(objs, "Hello, world");

		GapList<Object> nonNullList = new GapList<Object>(128);
		nonNullList.addAll(Arrays.asList(objs));

		System.out.println(nonNullList.get(0)); // Prints null, but shouldn't.
	}

}
