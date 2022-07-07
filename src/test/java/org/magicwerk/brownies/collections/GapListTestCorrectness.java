package org.magicwerk.brownies.collections;

import static org.magicwerk.brownies.collections.TestHelper.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;

import ch.qos.logback.classic.Logger;

/**
 * Test correctness of GapList.
 *
 * @author Thomas Mauch
 */
public class GapListTestCorrectness {
	/** Logger */
	private static Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		testManual();
		//testAllStatesAdd();
		//test();
	}

	static void testManual() {
		IList<Object> l = initState5();
		LOG.info("---");
		l.add(5, 99);
	}

	static void test() {
		testRelease();

		//testEnsureCapacity();
		//testCoverage();
		//testCompareArrayList();
		//testMisc();
	}

	// Run this tests before a release is made
	//@Test(groups = { "slow" })
	public static void testRelease() {
		// Change declaration in GapList.java for debugging:
		//GapList.DEBUG_CHECK = true;
		//GapList.DEBUG_TRACE = false;
		//GapList.DEBUG_DUMP = false;

		testCorrectnes();

		testAllStatesAdd();
		testAllStatesRemove();
		testAllStatesEnsureCapacity();

		//CollectionsTestCompare.testCompareAllMethods();
	}

	// Set GapList.DEFAULT_CAPACITY = 8 to get 100% test coverage of
	// doEnsureCapacity(), doAdd(), doRemove()
	static void testCoverage() {
		testAllStatesEnsureCapacity();
		testAllStatesAdd();
		testAllStatesRemove();
	}

	static void testMisc() {
		//showStates();
		//testCompareMethods();
		//testMemory();
		//testPerformance();
		//testArrayList();
		//testAddAll();
		//testToArray();
		//testRemoveMult();
	}

	static GapList[] getStates() {
		GapList<Object>[] states = new GapList[16];
		states[0] = initState0();
		states[1] = initState1();
		states[2] = initState2();
		states[3] = initState3();
		states[4] = initState4();
		states[5] = initState5();
		states[6] = initState6();
		states[7] = initState7();
		states[8] = initState8();
		states[9] = initState9();
		states[10] = initState10();
		states[11] = initState11();
		states[12] = initState12();
		states[13] = initState13();
		states[14] = initState14();
		states[15] = initState15();
		return states;
	}

	static void testAllStatesEnsureCapacity() {
		GapList<Object>[] states = getStates();
		for (int i = 0; i < states.length; i++) {
			GapList<Object> state = states[i];
			//int oldS = state.debugState();
			//assert(oldS == i);
			ArrayList<Object> oldState = new ArrayList<Object>(state);
			state.ensureCapacity(100);
			ArrayList<Object> newState = new ArrayList<Object>(state);
			assert (newState.equals(oldState));
			//int newS = state.debugState();
			//System.out.printf("%d -> %d\n", oldS, newS);
		}
	}

	static void testAllStatesAdd() {
		GapList<Object>[] states = getStates();
		for (int i = 0; i < states.length; i++) {
			GapList<Object> state = states[i];
			//int oldS = state.debugState();
			//assert(oldS == i);
			for (int j = 0; j <= state.size(); j++) {
				System.out.printf("add %d / %d\n", j, state.size());
				GapList<Object> gapList = ReflectTools.cloneDeep(state);
				ArrayList<Object> arrayList = new ArrayList<Object>(state);
				gapList.add(j, 99);
				arrayList.add(j, 99);
				assert (arrayList.equals(gapList));
				//int newS = state.debugState();
				//System.out.printf("S%d -> %d\n", oldS, newS);
			}
		}
	}

	static void testAllStatesRemove() {
		GapList<Object>[] states = getStates();
		for (int i = 0; i < states.length; i++) {
			GapList<Object> state = states[i];
			//int oldS = state.debugState();
			//assert(oldS == i);
			for (int j = 0; j < state.size(); j++) {
				System.out.printf("remove %d / %d\n", j, state.size());
				GapList<Object> gapList = ReflectTools.cloneDeep(state);
				ArrayList<Object> arrayList = new ArrayList<Object>(state);
				gapList.remove(j);
				arrayList.remove(j);
				assert (arrayList.equals(gapList));
				//int newS = state.debugState();
				//System.out.printf("S%d -> %d\n", oldS, newS);
			}
		}

		{
			// R0-1
			GapList<Object> state = states[9];
			GapList<Object> gapList = ReflectTools.cloneDeep(state);
			ArrayList<Object> arrayList = new ArrayList<Object>(state);
			gapList.remove(2);
			arrayList.remove(2);
			gapList.remove(2);
			arrayList.remove(2);
			assert (arrayList.equals(gapList));
		}
		{
			// R1-1
			GapList<Object> state = states[5];
			GapList<Object> gapList = ReflectTools.cloneDeep(state);
			ArrayList<Object> arrayList = new ArrayList<Object>(state);
			gapList.remove(0);
			arrayList.remove(0);
			gapList.remove(0);
			arrayList.remove(0);
			assert (arrayList.equals(gapList));
		}
		{
			// R1-2
			GapList<Object> state = states[9];
			GapList<Object> gapList = ReflectTools.cloneDeep(state);
			ArrayList<Object> arrayList = new ArrayList<Object>(state);
			gapList.remove(0);
			arrayList.remove(0);
			gapList.remove(0);
			arrayList.remove(0);
			assert (arrayList.equals(gapList));
		}
		{
			// R6a
			GapList<Object> state = states[9];
			GapList<Object> gapList = ReflectTools.cloneDeep(state);
			gapList.set(2, 3);
			gapList.add(3, 7);
			ArrayList<Object> arrayList = new ArrayList<Object>(gapList);
			gapList.remove(1);
			arrayList.remove(1);
			assert (arrayList.equals(gapList));
		}
		{
			// R5a
			GapList<Object> state = states[9];
			GapList<Object> gapList = ReflectTools.cloneDeep(state);
			gapList.addArray(new Object[] { 2, 9, 9, 9, 9 });
			gapList.remove(3, 2);
			ArrayList<Object> arrayList = new ArrayList<Object>(gapList);
			gapList.remove(4);
			arrayList.remove(4);
			assert (arrayList.equals(gapList));
		}

	}

	static void showStates() {
		GapList<Object>[] states = getStates();
		showState(states[0], "S0");
		showState(states[1], "S1");
		showState(states[2], "S2");
		showState(states[3], "S3");
		showState(states[4], "S4");
		showState(states[5], "S5");
		showState(states[6], "S6");
		showState(states[7], "S7");
		showState(states[8], "S8");
		showState(states[9], "S9");
		showState(states[10], "S10");
		showState(states[11], "S11");
		showState(states[12], "S12");
		showState(states[13], "S13");
		showState(states[14], "S14");
		showState(states[15], "S15");
	}

	static void showState(GapList<Object> list, String state) {
		String str = list.toString();
		GapList<String> fields = GapList.create(printField(list, "size"), printField(list, "start"), printField(list, "end"), printField(list, "gapStart"),
				printField(list, "gapSize"), printField(list, "gapIndex"));
		System.out.println("- " + StringUtils.join(fields, ", "));
	}

	static String printField(GapList<Object> list, String state) {
		return state + ": " + ReflectTools.getAnyFieldValue(list, state);
	}

	static void testCorrectnes() {
		GapList<Object> c;
		GapList<Object>[] states = new GapList[16];

		// State
		states[0] = initState0();
		check(states[0], "S0");
		states[1] = initState1();
		check(states[1], "S1");
		states[2] = initState2();
		check(states[2], "S2");
		states[3] = initState3();
		check(states[3], "S3");
		states[4] = initState4();
		check(states[4], "S4");
		states[5] = initState5();
		check(states[5], "S5");
		states[6] = initState6();
		check(states[6], "S6");
		states[7] = initState7();
		check(states[7], "S7");
		states[8] = initState8();
		check(states[8], "S8");
		states[9] = initState9();
		check(states[9], "S9");
		states[10] = initState10();
		check(states[10], "S10");
		states[11] = initState11();
		check(states[11], "S11");
		states[12] = initState12();
		check(states[12], "S12");
		states[13] = initState13();
		check(states[13], "S13");
		states[14] = initState14();
		check(states[14], "S14");
		states[15] = initState15();
		check(states[15], "S15");

		// Test add
		LOG.info("Test add");
		c = initState0();
		testAdd(c);
		c = initState1();
		testAdd(c);
		c = initState2();
		testAdd(c);
		c = initState3();
		testAdd(c);
		c = initState4();
		testAdd(c);
		c = initState5();
		testAdd(c);
		c = initState6();
		testAdd(c);
		c = initState8();
		testAdd(c);
		c = initState7();
		testAdd(c);
		c = initState9();
		testAdd(c);
		c = initState10();
		testAdd(c);
		c = initState11();
		testAdd(c);
		c = initState12();
		testAdd(c);
		c = initState13();
		testAdd(c);
		c = initState14();
		testAdd(c);
		c = initState15();
		testAdd(c);

		// Test remove
		LOG.info("Test remove");
		c = initState1();
		testRemove(c);
		c = initState2();
		testRemove(c);
		c = initState3();
		testRemove(c);
		c = initState4();
		testRemove(c);
		c = initState5();
		testRemove(c);
		c = initState6();
		testRemove(c);
		c = initState8();
		testRemove(c);
		c = initState7();
		testRemove(c);
		c = initState9();
		testRemove(c);
		c = initState10();
		testRemove(c);
		c = initState11();
		testRemove(c);
		c = initState12();
		testRemove(c);
		c = initState13();
		testRemove(c);
		c = initState14();
		testRemove(c);
		c = initState15();
		testRemove(c);

		//		// Add
		//		LOG.info("Test add");
		//		c = new GapList<Object>(); testCaseA1(c); check(c);
		//		c = new GapList<Object>(); testCaseA2(c); check(c);
		//		c = new GapList<Object>(); testCaseA5(c); check(c);
		//		c = new GapList<Object>(); testCaseA5b(c); check(c);
		//		c = new GapList<Object>(); testCaseA6(c); check(c);
		//		c = new GapList<Object>(); testCaseA7(c); check(c);
		//		c = new GapList<Object>(); testCaseA7b(c); check(c);
		//		c = new GapList<Object>(); testCaseA7c(c); check(c);
		//		c = new GapList<Object>(); testCaseA8(c); check(c);
		//		c = new GapList<Object>(); testCaseA8b(c); check(c);
		//		c = new GapList<Object>(); testCaseA8c(c); check(c);
		//		c = new GapList<Object>(); testCaseA9(c); check(c);
		//		c = new GapList<Object>(); testCaseA10(c); check(c);
		//		c = new GapList<Object>(); testCaseA12(c); check(c);
		//		c = new GapList<Object>(); testCaseA13(c); check(c);
		//		c = new GapList<Object>(); testCaseA14(c); check(c);
		//		c = new GapList<Object>(); testCaseA15(c); check(c);
		//		c = new GapList<Object>(); testCaseA15b(c); check(c);
		//		c = new GapList<Object>(); testCaseA16(c); check(c);
		//		c = new GapList<Object>(); testCaseA16b(c); check(c);
		//		c = new GapList<Object>(); testCaseA16c(c); check(c);
		//		c = new GapList<Object>(); testCaseA17(c); check(c);
		//		c = new GapList<Object>(); testCaseA18(c); check(c);
		//
		//		// Remove
		//		LOG.info("Test remove");
		//		c = new GapList<Object>(); testCaseR1(c); check(c);
		//		c = new GapList<Object>(); testCaseR2(c); check(c);
		//		c = new GapList<Object>(); testCaseR10(c); check(c);
		//		c = new GapList<Object>(); testCaseR10b(c); check(c);
		//		c = new GapList<Object>(); testCaseR10c(c); check(c);
		//		c = new GapList<Object>(); testCaseR11(c); check(c);
		//		c = new GapList<Object>(); testCaseR11b(c); check(c);
		//
	}

	static void testGapMove() {
		int size = 10;
		GapList<Integer> list = new GapList<Integer>(size);
		for (int i = 0; i < size; i++) {
			list.add(i);
		}
		System.out.println("GAP");

		//		for (int i=2; i<10; i++) {
		//			list.add(i, 10*i);
		//		}
		list.add(2, 20);
		list.add(2, 25);
		//		list.add(3, 30);
	}

	//	static void testAddAll2() {
	//		Timer t = new Timer();
	//		for (int i=0; i<1; i++) {
	//			doTestAddAll();
	//		}
	//		t.printElapsed();
	//	}
	//
	//	static void doTestAddAll2() {
	//		//ArrayList<Object> list = allocArrayListSize(85*1024*1024);
	//
	//		// 2.2 s
	////		int size = 17*1024*1024;
	////		ArrayList<Object> al1 = allocArrayListSize(size);
	////		ArrayList<Object> al2 = allocArrayListSize(size);
	////		al1.addAll(al2);
	//
	//		int size = 17*1024*1024;	// 3.1 s
	//		GapList<Object> ml1 = allocGapListSize(size);
	//		GapList<Object> ml2 = allocGapListSize(size);
	//		//ml1.addAll((Collection<Object>) ml2);	// 3.1 s
	//		ml1.addAll(ml2);	// 2.8 s
	//
	//		//int size = 21*1024*1024;	// 3.6 s
	//		//MagicList<Object> ml1 = allocMagicListSize(size);
	//		//MagicList<Object> ml2 = allocMagicListSize(size);
	//		//ml1.addAll(ml2);
	//	}

	//	static void testRemoveMult() {
	//		Timer t = new Timer();
	//		for (int i=0; i<1; i++) {
	//			doTestRemoveMult();
	//		}
	//		t.printElapsed();
	//	}
	//
	//	static void doTestRemoveMult() {
	//		int size = 1024*1024;
	//		int delSize = 1024;
	//
	//		// 3.4 s
	//		//ArrayList<Object> al1 = allocArrayListSize(size);
	//		//removeMult(al1, 0, delSize);
	//		//System.out.println(al1.size());
	//
	//		// 0.05 s
	//		GapList<Object> ml1 = allocGapListSize(size);
	//		ml1.remove(0, delSize);
	//		System.out.println(ml1.size());
	//
	//	}

	//	static void testToArray() {
	//		Timer t = new Timer();
	//		for (int i=0; i<1000; i++) {
	//			doTestToArray();
	//		}
	//		t.printElapsed();
	//	}
	//
	//	static void doTestToArray() {
	//		int size = 10*1024*1024;
	//		// 3.4 s
	//		//ArrayList<Object> al1 = allocArrayListSize(size);
	//		//removeMult(al1, 0, delSize);
	//		//System.out.println(al1.size());
	//
	//		// 0.05 s
	//		GapList<Object> ml1 = allocGapListSize(size);
	//		Object[] a = ml1.toArray();
	//	}

	static void removeMult(List<?> list, int fromIndex, int toIndex) {
		for (int i = toIndex - 1; i >= fromIndex; i--) {
			list.remove(i);
		}
	}

	static Random random = new Random(5);

	static void testCompareLinkedList() {
		GapList<Integer> ml = new GapList<Integer>();
		LinkedList<Integer> ll = new LinkedList<Integer>();

		// List
		ml.add(1);
		ll.add(1);
		checkEquals(ml, ll);

		ml.add(2);
		ll.add(2);
		checkEquals(ml, ll);

		ml.remove();
		ll.remove();
		checkEquals(ml, ll);

		ml.remove();
		ll.remove();
		checkEquals(ml, ll);

		// List
		ml.offer(1);
		ll.offer(1);
		checkEquals(ml, ll);

		ml.offer(2);
		ll.offer(2);
		checkEquals(ml, ll);

		ml.poll();
		ll.poll();
		checkEquals(ml, ll);

		ml.poll();
		ll.poll();
		checkEquals(ml, ll);
	}

	static void testRemoveFirst(GapList<Object>[] cs) {
		for (int i = 0; i < cs.length; i++) {
			GapList<Object> c = cs[i];
			if (c.size() > 0) {
				LOG.info("S" + i + ": remove first");
				GapList<Object> c2 = new GapList<Object>(cs[i]);
				c2.removeFirst();
			}
		}
	}

	static void testRemoveLast(GapList<Object>[] cs) {
		for (int i = 0; i < cs.length; i++) {
			GapList<Object> c = cs[i];
			if (c.size() > 0) {
				LOG.info("S" + i + ": remove last");
				GapList<Object> c2 = new GapList<Object>(cs[i]);
				c2.removeLast();
				check(c2);
			}
		}
	}

	static void testRemoveWithoutGap(GapList<Object>[] states) {
		GapList<Object> state;

		LOG.info("S1: remove without gap");
		state = new GapList<Object>(states[1]);
		state.remove(2);
		check(state);

		LOG.info("S2: remove without gap");
		state = new GapList<Object>(states[2]);
		state.remove(2);
		check(state);

		LOG.info("S3: remove without gap");
		state = new GapList<Object>(states[3]);
		state.remove(2);
		check(state);

		LOG.info("S4: remove without gap");
		state = new GapList<Object>(states[4]);
		state.remove(2);
		check(state);

		LOG.info("S9: remove without gap");
		state = new GapList<Object>(states[9]);
		state.remove(1);
		check(state);

		LOG.info("S9: remove without gap");
		state = new GapList<Object>(states[9]);
		state.remove(4);
		check(state);

		LOG.info("S10: remove without gap");
		state = new GapList<Object>(states[10]);
		state.remove(2);
		check(state);

		LOG.info("S10: remove without gap");
		state = new GapList<Object>(states[10]);
		state.remove(5);
		check(state);
	}

	static void testAdd(GapList<Object> c) {
		if (c.size() == 10) {
			return;
		}
		for (int i = 0; i <= c.size(); i++) {
			GapList<Object> c2 = (GapList<Object>) c.clone();
			double d;
			if (i == 0) {
				if (c.size() == 0) {
					d = 1.0;
				} else {
					d = ((Double) c.get(0)) - 0.5;
				}
			} else if (i == c.size()) {
				d = ((Double) c.get(i - 1)) + 0.5;
			} else {
				d = (((Double) c.get(i)) + ((Double) c.get(i - 1))) / 2.0;
			}
			c2.add(i, d);
			check(c2);
			testAdd(c2);
		}
	}

	static void testRemove(GapList<Object> c) {
		for (int i = 0; i < c.size(); i++) {
			GapList<Object> c2 = (GapList<Object>) c.clone();
			c2.remove(i);
			check(c);
			testRemove(c2);
		}
	}

	static void testEnsureCapacity() {
		for (int state = 0; state <= 15; state++) {
			String method = "initState" + state;
			GapList<Object> list = (GapList) ReflectTools.invokeMethod(GapListTestCorrectness.class, method, null);

			//int state0 = list.debugState();
			ArrayList<Object> copy = new ArrayList<>(list);
			int capacity = list.capacity();
			list.ensureCapacity(2 * capacity);
			//int state1 = list.debugState();
			ArrayList<Object> copy2 = new ArrayList<>(list);
			CheckTools.check(copy.equals(copy2));
			//System.out.println(state0 + " - " + state1);

			while (list.size() > 0) {
				list.remove(0);
			}
		}
	}

	// Init GapList with capacity of 8 to all 16 possible states

	static final int LIST_CAPACITY = 8;

	static GapList<Object> initState0() {
		LOG.info("S0");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		return c;
	}

	static GapList<Object> initState1() {
		LOG.info("S1");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.add(7, 8d);
		return c;
	}

	static GapList<Object> initState2() {
		LOG.info("S2");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		return c;
	}

	static GapList<Object> initState3() {
		LOG.info("S3");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.add(7, 8d);
		c.remove(0);
		c.remove(0);
		c.remove(0);
		c.remove(0);
		return c;
	}

	static GapList<Object> initState4() {
		LOG.info("S4");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.remove(0);
		c.remove(0);
		return c;
	}

	static GapList<Object> initState5() {
		LOG.info("S5");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0, 3d);
		c.add(1, 4d);
		c.add(2, 5d);
		c.add(3, 6d);
		c.add(0, 2d);
		c.add(0, 1d);
		return c;
	}

	static GapList<Object> initState6() {
		LOG.info("S6");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.add(7, 8d);
		c.remove(2);
		c.remove(2);
		c.remove(2);
		c.remove(2);
		return c;
	}

	static GapList<Object> initState7() {
		LOG.info("S7");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0, 3d);
		c.add(1, 4d);
		c.add(2, 0d);
		c.add(3, 0d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.remove(2);
		c.remove(2);
		c.add(0, 2d);
		c.add(0, 1d);
		return c;
	}

	static GapList<Object> initState8() {
		LOG.info("S8");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0, 0d);
		c.add(1, 0d);
		c.add(2, 1d);
		c.add(3, 2d);
		c.add(4, 3d);
		c.add(5, 4d);
		c.add(6, 5d);
		c.add(7, 6d);
		c.remove(0);
		c.remove(0);
		c.remove(2);
		c.remove(2);
		c.add(7d);
		c.add(8d);
		return c;
	}

	static GapList<Object> initState9() {
		LOG.info("S9");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0d);
		c.add(0d);
		c.add(0d);
		c.add(0d);
		c.add(1d);
		c.add(2d);
		c.remove(0, 4);
		c.add(3d);
		c.add(4d);
		c.add(5d);
		c.add(6d);
		c.add(7d);
		c.add(8d);
		c.remove(2, 4);
		return c;
	}

	static GapList<Object> initState10() {
		LOG.info("S10");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.remove(2);
		c.remove(2);
		return c;
	}

	static GapList<Object> initState11() {
		LOG.info("S11");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.add(7, 8d);
		c.remove(0);
		c.remove(0);
		c.remove(2);
		c.remove(2);
		return c;
	}

	static GapList<Object> initState12() {
		LOG.info("S12");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0d);
		c.add(0d);
		c.add(0d);
		c.add(0d);
		c.add(0d);
		c.add(1d);
		c.add(2d);
		c.remove(0, 5);
		c.add(3d);
		c.add(4d);
		c.add(5d);
		c.add(6d);
		c.remove(2, 2);
		return c;
	}

	static GapList<Object> initState13() {
		LOG.info("S13");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.remove(4);
		c.remove(3);
		c.remove(0);
		return c;
	}

	static GapList<Object> initState14() {
		LOG.info("S14");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0, 3d);
		c.add(1, 4d);
		c.add(2, 0d);
		c.add(3, 5d);
		c.add(4, 6d);
		c.remove(2);
		c.add(0, 2d);
		c.add(0, 1d);
		return c;
	}

	static GapList<Object> initState15() {
		LOG.info("S15");
		GapList<Object> c = new GapList<Object>(LIST_CAPACITY);
		c.add(0, 0d);
		c.add(1, 0d);
		c.add(2, 0d);
		c.add(3, 1d);
		c.add(4, 2d);
		c.add(5, 0d);
		c.add(6, 3d);
		c.add(7, 4d);
		c.remove(0);
		c.remove(0);
		c.remove(0);
		c.remove(2);
		c.add(4, 5d);
		c.add(5, 6d);
		return c;
	}

	//

	static void testCaseA1(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 4.5);
	}

	static void testCaseA2(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.add(7, 8d);
		c.remove(0);
		c.remove(0);
		c.add(0, 0.5);
	}

	static void testCaseA5(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(2, 2.5);
	}

	static void testCaseA5b(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(2, 2.5);
	}

	static void testCaseA6(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(2, 3d);
		c.add(6, 6d);
		c.remove(3);
		c.add(0, 0.5);
	}

	static void testCaseA7(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 4d);
		c.add(3, 5d);
		c.add(2, 3d);
		c.add(1, 1.5);
	}

	static void testCaseA7b(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.add(0, 0.5);
		c.remove(4);
		c.remove(4);
		c.remove(4);
		c.add(1, 0.75);
	}

	static void testCaseA7c(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(2, 3d);
		c.add(6, 6d);
		c.remove(3);
		c.add(1, 1.5);
	}

	static void testCaseA8(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 4d);
		c.add(3, 5d);
		c.add(2, 3d);
		c.add(4, 4.5);
	}

	static void testCaseA8b(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(2, 3d);
		c.add(6, 6d);
		c.remove(3);
		c.add(4, 4.5);
	}

	static void testCaseA8c(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(2, 3d);
		c.add(6, 6d);
		c.remove(3);
		c.add(6, 6.5);
	}

	static void testCaseA9(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.add(7, 8d);
		c.remove(0);
		c.remove(0);
		c.add(3, 5.5);
	}

	static void testCaseA10(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.add(7, 8d);
		c.remove(5);
		c.remove(4);
		c.remove(0);
		c.remove(0);
		c.add(1, 3.5);
	}

	static void testCaseA12(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.add(7, 8d);
		c.remove(5);
		c.remove(4);
		c.remove(0);
		c.remove(0);
		c.add(3, 7.5);
	}

	static void testCaseA13(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.add(7, 8d);
		c.remove(7);
		c.remove(6);
		c.remove(0);
		c.remove(0);
		c.add(1, 3.5);
	}

	static void testCaseA14(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.add(7, 8d);
		c.remove(7);
		c.remove(6);
		c.remove(0);
		c.remove(0);
		c.add(3, 5.5);
	}

	static void testCaseA15(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.remove(0);
		c.add(6, 8d);
		c.add(7, 8.5);
	}

	static void testCaseA15b(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.add(7, 8d);
		c.remove(0);
		c.remove(0);
		c.remove(0);
		c.add(5, 9d);
		c.add(6, 10d);
	}

	static void testCaseA16(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(2, 3d);
		c.remove(2);
		c.add(0, 0.5);
	}

	static void testCaseA16b(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(2, 3d);
		c.remove(2);
		c.add(0, 0.5);
		c.add(0, 0.2);
	}

	static void testCaseA16c(GapList<Object> c) {
		c.add(0, 1d);
		c.add(1, 2d);
		c.add(2, 3d);
		c.add(3, 4d);
		c.add(4, 5d);
		c.add(5, 6d);
		c.add(6, 7d);
		c.remove(0);
		c.add(0, 1d);
		c.remove(6);
		c.add(0, 0.5);
	}

	static void testCaseS13A20() {
		GapList<Object> c = initState14();
		c.add(5, 5.5);
		check(c);
	}

	static void check(GapList<Object> c) {
		check(c, null);
	}

	static void check(GapList<Object> c, String s) {
		if (s != null) {
			System.out.println(s);
			//c.dump(); TODO
		}

		Double lastVal = null;
		for (int i = 0; i < c.size(); i++) {
			Double val = (Double) c.get(i);
			if (val == null) {
				throw new IllegalArgumentException();
			}
			if (lastVal != null && val <= lastVal) {
				throw new IllegalArgumentException();
			}
			lastVal = val;
		}
	}

}
