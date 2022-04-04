package org.magicwerk.brownies.collections;

import static org.magicwerk.brownies.collections.TestHelper.*;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.magicwerk.brownies.collections.TestHelper.Ticket;
import org.magicwerk.brownies.collections.helper.NaturalComparator;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.CollectionTools;
import org.magicwerk.brownies.core.ObjectTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.objects.Result;
import org.magicwerk.brownies.core.reflect.Access;
import org.magicwerk.brownies.core.reflect.ReflectImpl;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.core.regex.RegexTools;
import org.magicwerk.brownies.core.strings.StringFormatter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Test Brownies collections by comparing them with JDK classes like ArrayList, LinkedList, etc.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class CollectionsTestCompare {

	static final ReflectImpl REFLECT = new ReflectImpl();

	static final Logger LOG = LogbackTools.getConsoleLogger();

	static final Random random = new Random(5);

	public static void main(String[] args) {
		test();
	}

	static void test() {
		testCompareAllMethods();
		testCompareAddRemove();

		//testCreateStringBetween();
	}

	public static void testCreateStringBetween() {
		IList<String> strs = GapList.create();
		strs.addArray("1", "2");

		for (int n = 0; n < 20; n++) {
			for (int i = strs.size() - 1; i > 0; i--) {
				String s = createStringBetween(strs.get(i - 1), strs.get(i));
				strs.add(i, s);
			}
			IList copy = strs.copy();
			copy.sort(null);
			CheckTools.check(copy.equals(strs));
			StringFormatter.println("{}", strs);
		}
	}

	public static String createStringBetween(String before, String after) {
		if (ObjectTools.equals(before, after)) {
			return before;
		}

		int beforeLen = (before == null) ? 0 : before.length();
		int afterLen = (after == null) ? 0 : after.length();
		int len = Math.max(beforeLen, afterLen);
		BigInteger scale = BigInteger.valueOf(36);
		BigInteger beforeValue = BigInteger.valueOf(0);
		BigInteger afterValue = BigInteger.valueOf(0);

		for (int i = 0; i < len; i++) {
			int beforeCode = (i < beforeLen) ? char2code(before.charAt(i)) : 0;
			int afterCode = (i < afterLen) ? char2code(after.charAt(i)) : 0;
			beforeValue = beforeValue.multiply(scale).add(BigInteger.valueOf(beforeCode));
			afterValue = afterValue.multiply(scale).add(BigInteger.valueOf(afterCode));
		}

		BigInteger betweenValue = beforeValue.add(afterValue).divide(BigInteger.valueOf(2));
		if (betweenValue.equals(beforeValue)) {
			beforeValue = beforeValue.multiply(scale).add(BigInteger.valueOf(0));
			afterValue = afterValue.multiply(scale).add(BigInteger.valueOf(0));
			betweenValue = beforeValue.add(afterValue).divide(BigInteger.valueOf(2));
		}

		StringBuilder buf = new StringBuilder();
		while (!betweenValue.equals(BigInteger.ZERO)) {
			int code = betweenValue.mod(scale).intValue();
			buf.append(code2char(code));
			betweenValue = betweenValue.divide(scale);
		}
		buf.reverse();
		return buf.toString();
	}

	static char code2char(int n) {
		if (n < 10) {
			return (char) ('0' + n);
		} else if (n < 36) {
			return (char) ('A' + n - 10);
		} else {
			CheckTools.error("Invalid code: " + n);
			return (char) -1;
		}
	}

	static int char2code(char c) {
		if (c >= '0' && c <= '9') {
			return c - '0';
		} else if (c >= 'A' && c <= 'Z') {
			return c - 'A' + 10;
		} else {
			CheckTools.error("Invalid char: " + c);
			return -1;
		}
	}

	static void testCompareAllMethods() {
		IList<ListTestData> testDatas = getListTestData();
		for (int i = 0; i < testDatas.size(); i++) {
			if (i < 20) {
				continue; // FIXME
			}
			LOG.info("TestData {}", i);
			ListTestData testData = testDatas.get(i);
			TestCompareAllMethods tcam = new TestCompareAllMethods(testData.ilistProducer, testData.valueProducer);
			if (i >= 2) {
				tcam.allowNull = false;
			}
			tcam.run();
		}

		//		// Test GapLlist
		//		GapList<Object> gl = new GapList<Object>();
		//		for (int i=0; i<listSize; i++) {
		//			gl.add(i);
		//		}
		//		new TestCompareAllMethods(
		//				() -> gl.copy(),
		//				new IntegerProducer()
		//				).run();
		//
		//		// TestBigList
		//		BigList<Object> bl = new BigList<Object>(2);
		//		for (int i=0; i<listSize; i++) {
		//			bl.add(i);
		//		}
		//		new TestCompareAllMethods(
		//				() -> bl.copy(),
		//				new IntegerProducer()
		//				).run();
	}

	static class TestCompareAllMethods {

		static int numIters = 100 * 1000;

		IListProducer ilistProducer;
		ValueProducer valueProducer;
		boolean allowNull = true;

		TestCompareAllMethods(IListProducer ilistProducer, ValueProducer valueProducer) {
			this.ilistProducer = CheckTools.checkNonNull(ilistProducer);
			this.valueProducer = CheckTools.checkNonNull(valueProducer);
		}

		void run() {
			IList<Object> il = ilistProducer.create();

			// Test against ArrayList
			ArrayList<Object> al = new ArrayList<Object>(il);
			checkEquals(il, al);
			runCompareTest(il, al);

			// Test against LinkedList
			LinkedList<Object> ll = new LinkedList<Object>(il);
			checkEquals(il, ll);
			runCompareTest(il, ll);
		}

		void runCompareTest(IList<Object> gl, List<Object> l) {
			List<Method> methods = getMethods(l.getClass());
			for (int i = 0; i < methods.size(); i++) {
				//testMethod(methods.get(i), gl, l);
			}
			for (int i = 0; i < numIters; i++) {
				if (i % 1000 == 0) {
					LOG.info("Step {}", i / 1000);
				}
				int r = random.nextInt(methods.size());
				Method method = methods.get(r);
				r = random.nextInt(methods.size());
				invokeMethod(method, gl, l, r);
			}
		}

		static List<Method> getPublicMethods(Class<?> clazz) {
			IList<Executable> ms = REFLECT.getAccessedMethods(clazz, Access.PUBLIC);
			ms.filteredList(m -> m instanceof Method);
			return (List) ms;
		}

		static List<Method> getMethods(Class<?> clazz) {
			List<Method> methods = new ArrayList<Method>(getPublicMethods(clazz));
			for (int i = 0; i < methods.size(); i++) {
				if (methods.get(i).getName().equals("wait") || methods.get(i).getName().equals("notify") || methods.get(i).getName().equals("notifyAll")
						|| methods.get(i).getName().equals("getClass") || methods.get(i).getName().equals("iterator")
						|| methods.get(i).getName().equals("descendingIterator") || methods.get(i).getName().equals("listIterator")) {
					methods.remove(i);
					i--;
				}
			}
			return methods;
		}

		static void invokeMethod(GapList<Integer> list2) {
			GapList<Integer> list = new GapList<Integer>(list2);
			List<String> methods1 = getMethodSignatures(getPublicMethods(ArrayList.class));
			List<String> methods2 = getMethodSignatures(getPublicMethods(LinkedList.class));
			Set<String> ml = CollectionTools.union(new HashSet<String>(methods1), new HashSet<String>(methods2));
			ml.remove("wait(long)");
			ml.remove("wait(long,int)");
			List<String> mll = new ArrayList<String>(ml);
			//System.out.println(PrintTools.print(mll));

			int rnd = random.nextInt(mll.size());
			String m = mll.get(rnd);
			System.out.println("calling " + m);
			List<String> s = RegexTools.getAll("^(.*)(\\(.*\\))$", m);
			//System.out.println("s= " + PrintTools.print(s));

			Method method = null;
			List<Method> methods = getPublicMethods(GapList.class);
			for (int i = 0; i < methods.size(); i++) {
				if (methods.get(i).getName().equals(s.get(1))) {
					method = methods.get(i);
				}
			}
			assert (method != null);

			//Method method = methods.get(rnd);
		}

		void invokeMethod(Method method, IList<Object> ilist, List<Object> list, int index) {
			List<Object> params = getParams(method, ilist, index);
			if (params == null) {
				return;
			}

			List<Object> ilistCopy = (List<Object>) ilist.clone();
			Result ilistResult = invokeMethod(method, params, ilistCopy);
			List<Object> listCopy = CollectionTools.copy(list);
			Result listResult = invokeMethod(method, params, listCopy);
			//			try {
			//					checkEquals(ilistCopy, listCopy);
			if (ilistResult.isSuccess() && listResult.isSuccess()) {
				if (ilistResult.isVoid() && listResult.isVoid()) {
					// void methods have the same result
				} else {
					Object ilistValue = ilistResult.getValue();
					Object listValue = listResult.getValue();
					if (ilistValue instanceof List && listValue instanceof List) {
						assert (ilistValue.equals(listValue));
					} else {
						if (ilistValue instanceof Stream || ilistValue instanceof Spliterator) {
							// TODO: cannot compare
						} else {
							if (!ObjectTools.equals(ilistValue, listValue)) {
								assert (ObjectTools.equals(ilistValue, listValue));
							}
						}
					}
				}
			} else if (ilistResult.isError() && listResult.isError()) {
				//assert(glr.exception.getClass() == lr.exception.getClass());
				//assert(ExceptionUtils.getRootCause(glr.exception).getClass() == ExceptionUtils.getRootCause(lr.exception).getClass());
			} else {
				printCall(method, params, ilist, ilistResult);
				printCall(method, params, list, listResult);
				ilistResult = invokeMethod(method, params, ilistCopy);
				listResult = invokeMethod(method, params, listCopy);
				assert (false);
			}
		}

		void printCall(Method m, List<Object> params, Object on, Result result) {
			String s = result.toString();
			StringFormatter.println("{}: Calling {} with {} on {} returns {}", on.getClass().getSimpleName(), m.getName(), params, on, result);
		}

		/**
		 *
		 *
		 * @param method
		 * @param ilist
		 * @param n
		 * @return			parameters to use for method call, null if method should not be called
		 */
		List<Object> getParams(Method method, IList<Object> ilist, int n) {
			if ("sort".equals(method.getName())) {
				if (ilist instanceof KeyListImpl) {
					KeyListImpl kl = (KeyListImpl) ilist;
					if (kl.isSorted()) {
						return null;
					}
				}
			}
			return getParams(method.getParameterTypes(), n);
		}

		List<Object> getParams(Class<?>[] paramTypes, int n) {
			List<Object> params = GapList.create();
			for (int i = 0; i < paramTypes.length; i++) {
				params.add(getParams(paramTypes[i], n + i));
			}
			return params;
		}

		Object getParams(Class<?> paramType, int n) {
			Object param = null;
			if (paramType == int.class) {
				param = getIntParam(n);
			} else if (paramType == Object.class) {
				param = getObjectParam(n);
			} else if (paramType == Collection.class) {
				param = getCollectionParam(n);
			} else if (paramType == Comparator.class) {
				param = NaturalComparator.INSTANCE();
			} else if (paramType == Predicate.class) {
				// Collection introduces default method
				// public boolean java.util.ArrayList.removeIf(java.util.function.Predicate)
				Predicate<Object> predicate = (Object val) -> {
					return (Integer) val % 2 == 0;
				};
				param = predicate;
			} else if (paramType == Consumer.class) {
				// Iterable introduces default method
				// public void java.util.ArrayList.forEach(java.util.function.Consumer)
				Consumer<Object> consumer = (Object) -> {
				};
				param = consumer;
			} else if (paramType == UnaryOperator.class) {
				// List introduces default method
				// public void java.util.ArrayList.replaceAll(java.util.function.UnaryOperator)
				UnaryOperator<Object> uo = (Object val) -> {
					return (Integer) val + 1;
				};
				param = uo;
			} else if (paramType.isArray()) {
				param = getArrayParam(n);
			} else {
				throw new IllegalArgumentException("Invalid type: " + paramType);
			}
			return param;
		}

		static int getIntParam(int i) {
			return i - 1;
		}

		Object getObjectParam(int i) {
			if (i == 0 && allowNull) {
				return null;
			}
			return valueProducer.create(i);
		}

		List<Object> getCollectionParam(int i) {
			if (i == 0) {
				return null;
			}
			i--;
			List<Object> l = new ArrayList<Object>();
			for (int j = 0; j < i; j++) {
				Object value = valueProducer.create(i);
				l.add(value);
			}
			return l;
		}

		/// e.g. for List.toArray(Object[] array)
		Object[] getArrayParam(int i) {
			if (i == 0) {
				return null;
			}
			i--;
			Object[] ii = new Object[i];
			for (int j = 0; j < i; j++) {
				Object value = valueProducer.create(i);
				ii[j] = value;
			}
			return ii;
		}

		static List<String> getMethodSignatures(List<Method> methods) {
			List<String> signatures = new ArrayList<String>(methods.size());
			for (Method method : methods) {
				CheckTools.error();
				//String signature = method.getName() + ReflectTools.getJavaSignature(method, new MethodSignature());
				//signatures.add(signature);
			}
			return signatures;
		}

		static Result invokeMethod(Method method, List<Object> params, List<Object> list) {
			List<Method> listMethods = getPublicMethods(list.getClass());
			Method listMethod = null;
			for (Method lm : listMethods) {
				if (lm.getName().equals(method.getName()) && ObjectTools.equals(lm.getParameterTypes(), method.getParameterTypes())) {
					listMethod = lm;
					break;
				}
			}
			//System.out.println("calling " + listMethod);
			try {
				Object r = ReflectTools.invokeMethod(listMethod, list, params.toArray());
				if (method.getReturnType() == void.class) {
					return Result.valueVoid();
					//System.out.println(method.getName() + ": " + PrintTools.print(params));
				} else {
					return Result.value(r);
					//System.out.println(method.getName() + ": " + PrintTools.print(params) + " -> " + r);
				}
			} catch (Throwable t) {
				// Get exception cause: WrapperException -> InvocationTargetException -> real exception
				Throwable t2 = t.getCause().getCause();
				//t2.printStackTrace();
				return Result.error(t2);
				//System.out.println(method.getName() + ": " + PrintTools.print(params) + " throws " + t);
				//t.printStackTrace();
			}
		}
	}

	//

	interface ValueProducer {
		Object create(int index);
	}

	static class IntegerProducer implements ValueProducer {
		@Override
		public Object create(int index) {
			return Integer.valueOf(index);
		}
	}

	static class IntegerConstProducer implements ValueProducer {
		@Override
		public Object create(int index) {
			return Integer.valueOf(1);
		}
	}

	static class TicketProducer implements ValueProducer {
		@Override
		public Object create(int index) {
			return new Ticket(index, "extId" + index, "text" + index);
		}
	}

	static class TicketConstProducer implements ValueProducer {
		@Override
		public Object create(int index) {
			return new Ticket(1, "extId", "text");
		}
	}

	interface IListProducer {
		IList create();
	}

	static class ListTestData {
		IListProducer ilistProducer;
		ValueProducer valueProducer;

		ListTestData(IListProducer ilistProducer, ValueProducer valueProducer) {
			this.ilistProducer = ilistProducer;
			this.valueProducer = valueProducer;
		}
	}

	static IList<ListTestData> getListTestData() {
		IList<ListTestData> testDatas = GapList.create();
		testDatas.addArray(new ListTestData(() -> new GapList<Object>(), new IntegerProducer()),
				new ListTestData(() -> new BigList<Object>(), new IntegerProducer()));
		for (int n = 0; n < KeyCollectionsStructTest.KeyListImplsInteger.size(); n++) {
			KeyListImpl ilist = KeyCollectionsStructTest.KeyListImplsInteger.get(n);
			ListTestData testData = new ListTestData(() -> ilist.copy(), ilist.isSorted() ? new IntegerConstProducer() : new IntegerProducer());
			testDatas.add(testData);
		}
		for (int n = 0; n < KeyCollectionsStructTest.KeyListImplsTicket.size(); n++) {
			KeyListImpl ilist = KeyCollectionsStructTest.KeyListImplsTicket.get(n);
			ListTestData testData = new ListTestData(() -> ilist.copy(), ilist.isSorted() ? new TicketProducer() : new TicketProducer());
			testDatas.add(testData);
		}
		return testDatas;
	}

	static void testCompareAddRemove() {
		LOG.setLevel(Level.INFO);

		final int numSteps = 100;

		IList<ListTestData> testDatas = getListTestData();
		for (int i = 0; i < testDatas.size(); i++) {
			ListTestData testData = testDatas.get(i);
			IList ilist = testData.ilistProducer.create();
			if (ilist instanceof KeyListImpl && ((KeyListImpl) ilist).isSorted()) {
				continue;
			}
			LOG.info("Test {}: {}", i, ilist.getClass().getSimpleName());
			for (int s = 0; s < numSteps; s++) {
				LOG.info("Step {}", s);
				new TestCompareAddRemove(ilist, testData.valueProducer).run();
			}
		}
	}

	/**
	 * Implementation of test.
	 */
	static class TestCompareAddRemove {

		ValueProducer valueProducer;
		IList ilist;

		TestCompareAddRemove(IList ilist, ValueProducer valueProducer) {
			this.ilist = ilist;
			this.valueProducer = CheckTools.checkNonNull(valueProducer);
		}

		void run() {
			ArrayList<Integer> list = new ArrayList<Integer>();

			int size = 500;
			int minSize = size - size / 2;
			int maxSize = size + size / 2;
			int numIters = 1000;

			// Grow
			while (list.size() < size) {
				int op;
				if (list.size() == 0) {
					op = 0;
				} else {
					op = random.nextInt(2);
				}
				if (op == 0) {
					add(list, ilist);
				} else {
					set(list, ilist);
				}
			}

			// Modify
			for (int i = 0; i < numIters; i++) {
				int op;
				if (list.size() < minSize) {
					op = 0;
				} else if (list.size() > maxSize) {
					op = 2;
				} else {
					op = random.nextInt(3);
				}

				if (op == 0) {
					add(list, ilist);
				} else if (op == 1) {
					set(list, ilist);
				} else {
					remove(list, ilist);
				}
			}

			// Shrink
			while (list.size() > 0) {
				if (random.nextBoolean()) {
					remove(list, ilist);
				} else {
					set(list, ilist);
				}
			}
		}

		void add(List list, IList ilist) {
			boolean single = random.nextBoolean();
			int index = random.nextInt(list.size() + 1);
			if (single) {
				Object value = valueProducer.create(index);
				list.add(index, value);
				ilist.add(index, value);
			} else {
				int len = random.nextInt(list.size() + 1);
				ArrayList<Object> coll = new ArrayList<Object>(len);
				for (int i = 0; i < len; i++) {
					Object value = valueProducer.create(index + 1);
					coll.add(value);
				}
				list.addAll(index, coll);
				ilist.addAll(index, coll);
			}
			checkEquals(list, ilist);
		}

		void remove(List list, IList ilist) {
			boolean single = random.nextBoolean();
			int index = random.nextInt(list.size());
			if (single) {
				list.remove(index);
				ilist.remove(index);
			} else {
				int len = random.nextInt(list.size() - index + 1);
				list.subList(index, index + len).clear();
				ilist.remove(index, len);
			}
			checkEquals(list, ilist);
		}

		void set(List list, IList ilist) {
			boolean single = random.nextBoolean();
			int index = random.nextInt(list.size());
			if (single) {
				Object value = valueProducer.create(index);
				list.set(index, value);
				ilist.set(index, value);
			} else {
				int len = random.nextInt(list.size() - index + 1);
				ArrayList<Object> coll = new ArrayList<Object>(len);
				for (int i = 0; i < len; i++) {
					Object value = valueProducer.create(index + 1);
					coll.add(value);
				}
				for (int i = 0; i < len; i++) {
					list.set(index + i, coll.get(i));
				}
				ilist.setAll(index, coll);
			}
			checkEquals(list, ilist);
		}
	}

}
