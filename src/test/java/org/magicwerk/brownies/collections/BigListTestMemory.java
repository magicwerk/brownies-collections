package org.magicwerk.brownies.collections;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.collections4.list.TreeList;
import org.magicwerk.brownies.collections.TestFactories.ArrayListFactory;
import org.magicwerk.brownies.collections.TestFactories.BigListFactory;
import org.magicwerk.brownies.collections.TestFactories.CollectionFactory;
import org.magicwerk.brownies.collections.TestFactories.Factory;
import org.magicwerk.brownies.collections.TestFactories.FastTableFactory;
import org.magicwerk.brownies.collections.TestFactories.GapListFactory;
import org.magicwerk.brownies.collections.TestFactories.IntBigListFactory;
import org.magicwerk.brownies.collections.TestFactories.LinkedListFactory;
import org.magicwerk.brownies.collections.TestFactories.TreeListFactory;
import org.magicwerk.brownies.collections.primitive.IntBigList;
import org.magicwerk.brownies.collections.primitive.IntObjBigList;
import org.magicwerk.brownies.core.SystemTools;
import org.magicwerk.brownies.core.ThreadTools;
import org.magicwerk.brownies.core.TypeTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.core.strings.StringFormatter;
import org.magicwerk.brownies.tools.runner.JvmRunner;
import org.magicwerk.brownies.tools.runner.MemoryTester;
import org.slf4j.Logger;

import javolution.util.FastTable;

/**
 * Test performance of BigList.
 *
 * @author Thomas Mauch
 */
public class BigListTestMemory {
	/** Logger */
	private static Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		//runJava(args);

		testMemorySize();
		//testMemoryCopy();
	}

	static void runJava(String[] args) {
		JvmRunner runner = new JvmRunner();
		runner.setRunnable((as) -> {
			String arg = (as.length == 0) ? "" : as[0];
			doRun(arg);
		});
		String java6 = "C:\\Java\\JDK\\jdk1.6.0_45\\bin\\java.exe";
		String java7 = "C:\\Java\\JDK\\jdk1.7.0_40\\bin\\java.exe";
		String java8 = "C:\\Java\\JDK\\jdk1.8.0_05\\bin\\java.exe";
		String java8_64 = "C:\\Java\\JDK\\jdk1.8.0_05-x64\\bin\\java.exe";

		//		String[] jvmArgs = new String[] { "-Xms1024m", "-Xmx1024m" };
		String[] jvmArgs = new String[] { "-Xms256m", "-Xmx256m", "-XX:+UseG1GC",
				//				"-XX:+PrintGC",
				//				"-XX:+PrintGCDetails",
				//				"-XX:+PrintGCTimeStamps"
		};

		runner.addJavaArgsRun(java8, jvmArgs);
		runner.addJavaArgsRun(java8_64, jvmArgs);

		// OOME
		//		tester.addRunner(new JavaRunner().setExecutable(java8).setJvmArgs(GapList.create(jvmArgs)).setArgs(GapList.create(ARRAY_LIST)));
		//		tester.addRunner(new JavaRunner().setExecutable(java8).setJvmArgs(GapList.create(jvmArgs)).setArgs(GapList.create(GAP_LIST)));
		//		tester.addRunner(new JavaRunner().setExecutable(java8).setJvmArgs(GapList.create(jvmArgs)).setArgs(GapList.create(BIG_LIST)));
		//		tester.addRunner(new JavaRunner().setExecutable(java8).setJvmArgs(GapList.create(jvmArgs)).setArgs(GapList.create(FAST_TABLE)));
		//		tester.addRunner(new JavaRunner().setExecutable(java8).setJvmArgs(GapList.create(jvmArgs)).setArgs(GapList.create(TREE_LIST)));
		runner.run(args);
	}

	static void doRun(String name) {
		LOG.info("Run with {}", SystemTools.getJvmArgsString());

		//testMemorySize();
		testMemorySizeInt();

		//testMemoryOOME(name);
	}

	static int size = 1000 * 1000;

	static Factory[] factories = new Factory[] { new ArrayListFactory(), new LinkedListFactory(), new GapListFactory(), new BigListFactory(1000),
			new IntBigListFactory(1000), new FastTableFactory(), new TreeListFactory() };

	static void testMemorySize() {
		int size = 1000 * 1000;

		StringFormatter.println("Size = " + size);
		for (Factory factory : factories) {
			Object coll;
			if (factory instanceof CollectionFactory) {
				coll = ((CollectionFactory) factory).create(size);
			} else {
				throw new AssertionError();
			}
			StringFormatter.println("{} = {}", factory.getName(), TypeTools.formatGrouped(ReflectTools.getObjectSize(coll)));
		}
	}

	static void testMemorySizeInt() {
		int size = 1000 * 1000;

		int[] array = new int[size];
		StringFormatter.println("Array = {}", ReflectTools.getObjectSize(array));

		IntBigList intBigList = new IntBigList();
		for (int i = 0; i < size; i++) {
			intBigList.add(i);
		}
		StringFormatter.println("IntBigList = {}", ReflectTools.getObjectSize(intBigList));

		testMemorySizeInt(new IntObjBigList());
		testMemorySizeInt(new BigList());
		testMemorySizeInt(new GapList());
		testMemorySizeInt(new ArrayList());
		testMemorySizeInt(new LinkedList());
		testMemorySizeInt(new TreeList());
		testMemorySizeInt(new FastTable());
	}

	static void testMemorySizeInt(List list) {
		for (int i = 0; i < size; i++) {
			list.add(i);
		}
		StringFormatter.println("{} = {}", list.getClass().getName(), ReflectTools.getObjectSize(list));
	}

	static void testMemoryCopy() {
		FastTableFactory f = new org.magicwerk.brownies.collections.TestFactories.FastTableFactory();
		FastTable l1 = f.createSize(100000);
		Collection l2 = f.copy(l1);
		List<Object> l = GapList.create();
		l.add(l1);
		l.add(l2);
		StringFormatter.println("{}", ReflectTools.getObjectSize(l));

		int size = 1000 * 1000;
		int copy = 3;

		StringFormatter.println("Size clone = " + size);
		for (Factory factory : factories) {
			CollectionFactory collFactory = (CollectionFactory) factory;
			Collection coll = (Collection) collFactory.create(size);
			List<Object> list = GapList.create();
			list.add(coll);
			for (int i = 1; i <= copy; i++) {
				if (i > 1) {
					list.add(collFactory.copy(coll));
				}
				StringFormatter.println("{}, {}, {}", factory.getName(), i, TypeTools.formatGrouped(ReflectTools.getObjectSize(list)));
			}
		}
	}

	static void testMemoryOOME() {
		ThreadTools.sleep(5 * 1000);

		//		List l = org.magicwerk.brownies.collections.TestFactories.BigListFactory.allocBigList(10*1000*1000);
		//		//List l = org.magicwerk.brownies.collections.TestFactories.FastTableFactory.allocFastTable(10*1000*1000);
		//		LOG.info("allocated");
		//		TimerTools.sleep(10*1000);

		//		testMemory("org.magicwerk.brownies.collections.TestFactories$ArrayListFactory", "allocArrayList");
		testMemory("org.magicwerk.brownies.collections.TestFactories$GapListFactory", "allocGapList");
		testMemory("org.magicwerk.brownies.collections.TestFactories$BigListFactory", "allocBigList");
		//		testMemory("org.magicwerk.brownies.collections.TestFactories$FastTableFactory", "allocFastTable");
		//		testMemory("org.magicwerk.brownies.collections.TestFactories$TreeListFactory", "allocTreeList");
	}

	static String ARRAY_LIST = "allocArrayList";
	static String GAP_LIST = "allocGapList";
	static String BIG_LIST = "allocBigList";
	static String FAST_TABLE = "allocFastTable";
	static String TREE_LIST = "allocTreeList";

	static void testMemoryOOME(String name) {
		if ("allocArrayList".equals(name)) {
			testMemory("org.magicwerk.brownies.collections.TestFactories$ArrayListFactory", ARRAY_LIST);
		} else if ("allocGapList".equals(name)) {
			testMemory("org.magicwerk.brownies.collections.TestFactories$GapListFactory", GAP_LIST);
		} else if ("allocBigList".equals(name)) {
			testMemory("org.magicwerk.brownies.collections.TestFactories$BigListFactory", BIG_LIST);
		} else if ("allocFastTable".equals(name)) {
			testMemory("org.magicwerk.brownies.collections.TestFactories$FastTableFactory", FAST_TABLE);
		} else if ("allocTreeList".equals(name)) {
			testMemory("org.magicwerk.brownies.collections.TestFactories$TreeListFactory", TREE_LIST);
		} else {
			throw new AssertionError();
		}
	}

	static void testMemory(final String className, final String methodName) {
		LOG.info("--- {} ---", methodName);

		Consumer<Integer> func = new Consumer<Integer>() {
			Executable method = ReflectTools.getAnyMethod(className, methodName);

			@Override
			public void accept(Integer size) {
				Object obj = ReflectTools.invokeMethod(method, null, size);
				LOG.info("size = {} -> bytes = {}", size, ReflectTools.getObjectSize(obj));
				ThreadTools.sleep(1 * 1000);
			}
		};
		MemoryTester tester = new MemoryTester();
		int max = tester.run(func, 16, 1000 * 1000);
		LOG.info("--- {}: {} ---", methodName, max);
	}

}