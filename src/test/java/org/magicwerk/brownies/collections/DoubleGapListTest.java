package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.magictest.client.Trace;
import org.magicwerk.brownies.collections.primitive.DoubleGapList;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.magicwerk.brownies.tools.runner.JvmRunner;
import org.magicwerk.brownies.tools.runner.MemoryTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test of DoubleGapList.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
@Trace(traceClass = "org.magicwerk.brownies.collections.primitive.DoubleGapList")
public class DoubleGapListTest {
	/** Logger */
	private static Logger LOG = LoggerFactory.getLogger(DoubleGapListTest.class);

	public static void main(String[] args) {
		test();
		//testMemory(args);
	}

	static void test() {
		//testMemoryIntGapList();
		//testMemoryDoubleGapList();
	}

	static void testMemory(String[] args) {
		JvmRunner runner = new JvmRunner();
		runner.setRunnable((a) -> testMemoryIntGapList());
		runner.addJavaArgsRun("java.exe", "-Xmx256m");
		runner.addJavaArgsRun("C:\\Java\\JDK\\jdk1.7.0_10\\bin\\java.exe", "-Xmx256m");
		runner.addJavaArgsRun("C:\\Java\\JDK\\jdk1.7.0_10-x64\\bin\\java.exe", "-Xmx256m");
		runner.run(args);
	}

	static void testMemoryDoubleGapList() {
		Consumer<Integer> func1 = (size) -> {
			DoubleGapList list = new DoubleGapList(size);
			for (int i = 0; i < size; i++) {
				list.add(i, i);
			}
		};
		Consumer<Integer> func2 = (size) -> {
			List<Double> list = new ArrayList<Double>(size);
			for (int i = 0; i < size; i++) {
				list.add(i, (double) i);
			}
		};
		Consumer<Integer> func3 = (size) -> {
			List<Double> list = new ArrayList<Double>(size);
			for (int i = 0; i < size; i++) {
				list.add(i, null);
			}
		};
		doTestMemory(func1, "DoubleGapList");
		doTestMemory(func2, "ArrayList(numbers)");
		doTestMemory(func3, "ArrayList(null)");
	}

	static void testMemoryIntGapList() {
		Consumer<Integer> func1 = (size) -> {
			IntGapList list = new IntGapList(size);
			for (int i = 0; i < size; i++) {
				list.add(i, i);
			}
		};
		Consumer<Integer> func2 = (size) -> {
			List<Integer> list = new ArrayList<Integer>(size);
			for (int i = 0; i < size; i++) {
				list.add(i, i);
			}
		};
		Consumer<Integer> func3 = (size) -> {
			List<Integer> list = new ArrayList<Integer>(size);
			for (int i = 0; i < size; i++) {
				list.add(i, null);
			}
		};
		doTestMemory(func1, "IntGapList");
		doTestMemory(func2, "ArrayList(numbers)");
		//		doTestMemory(func3, "ArrayList(null)");
	}

	static void doTestMemory(Consumer<Integer> func, String text) {
		MemoryTester tester = new MemoryTester();
		int max = tester.run(func, 10000, 1000);
		LOG.info("--- {}: {} ---", text, max);
	}

	@Trace(parameters = Trace.ALL_PARAMS | Trace.THIS)
	public static void testEquals() {
		// Check correct handling of NaN values
		DoubleGapList list1 = DoubleGapList.create(1, Double.NaN, 3);
		DoubleGapList list2 = DoubleGapList.create(1, Double.NaN, 3);

		list1.equals(list2);

		list2.set(0, 0);
		list1.equals(list2);
	}

	@Trace(parameters = Trace.ALL_PARAMS | Trace.THIS, result = Trace.THIS)
	public static void testSort() {
		// Check correct handling of NaN values
		DoubleGapList list1 = DoubleGapList.create(1, Double.NaN, 3);

		list1.sort();
	}

}
