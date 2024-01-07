package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.collections4.list.TreeList;
import org.magicwerk.brownies.collections.primitive.IntBigList;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.magicwerk.brownies.collections.primitive.IntObjBigList;
import org.magicwerk.brownies.collections.primitive.IntObjGapList;
import org.magicwerk.brownies.core.FuncTools;
import org.magicwerk.brownies.core.FuncTools.MapMode;
import org.magicwerk.brownies.core.files.FilePath;
import org.magicwerk.brownies.core.function.Predicates;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.core.types.Type;
import org.magicwerk.brownies.core.values.Table;
import org.magicwerk.brownies.html.HtmlTable;
import org.magicwerk.brownies.test.JavaEnvironment.JavaVersion;
import org.magicwerk.brownies.test.TestTools;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner;
import org.magicwerk.brownies.tools.dev.jvm.JmhRunner.Options;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.slf4j.Logger;

/**
 * Class {@link ListTestPerformance} evaluates the performance of various {@link List} implementations using JMH.
 */
public class ListTestPerformance {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new ListTestPerformance().run();
	}

	void run() {
		//runBenchmarks();
		showBenchmark();
	}

	//

	IList<String> benchmarks = GapList.create("Get", "Add", "Remove", "Copy");
	IList<String> jvmArgs = GapList.create("-Xmx4g", "-Xms4g", "-XX:+UseG1GC");

	void runBenchmarks() {
		boolean fast = true;
		//runBenchmarkOp(fast);
		runBenchmarkCopy(fast);
	}

	/** Run benchmarks, results in ListTestPerformance.json / ListTestPerformance.log */
	void runBenchmarkOp(boolean fast) {
		Options opts = configure();
		opts.includeMethod(ListTest.class, "testGet");
		opts.includeMethod(ListTest.class, "testAdd");
		opts.includeMethod(ListTest.class, "testRemove");

		opts.setResultFile(FilePath.of("output/ListTestPerformance.json"));
		opts.setLogFile(FilePath.of("output/ListTestPerformance.log"));

		JmhRunner runner = new JmhRunner();
		runner.setVerbose(true);
		runner.runJmh(opts);
	}

	Options configure() {
		Options opts = new Options();
		opts.setWarmupIterations(25);
		opts.setMeasurementIterations(25);
		opts.setRunTimeMillis(100);
		opts.setJvmArgs(jvmArgs);

		opts.setJavaVersions(GapList.create(TestTools.createJdkTools(JavaVersion.JAVA_8)));
		//opts.setJavaVersion(JavaVersion.JAVA_11);

		//opts.setUseGcProfiler(true);

		return opts;
	}

	/** Run benchmarks, results in ListTestCopyPerformance.json / ListTestCopyPerformance.log */
	void runBenchmarkCopy(boolean fast) {
		Options opts = configure();
		opts.includeMethod(ListTest.class, "testCopy");

		opts.setResultFile(FilePath.of("output/ListTestCopyPerformance.json"));
		opts.setLogFile(FilePath.of("output/ListTestCopyPerformance.log"));
		JmhRunner runner = new JmhRunner();
		runner.setFastMode(fast);
		runner.runJmh(opts);
	}

	// Benchmarks

	public static class ListTest {

		// Benchmarks executed by runBenchmarkOp() (with params "op", "type", "size")

		@Benchmark
		public Object testGet(GetListState state) {
			List<Integer> list = state.list();
			int pos = state.pos();
			//LOG.info("get {} (size= {})", pos, list.size());
			list.get(pos);
			return state;
		}

		@Benchmark
		public Object testAdd(AddListState state) {
			List<Integer> list = state.list();
			int pos = state.pos();
			//LOG.info("add {} (size= {})", pos, list.size());
			list.add(pos, pos);
			return state;
		}

		@Benchmark
		public Object testRemove(RemoveListState state) {
			List<Integer> list = state.list();
			int pos = state.pos();
			//LOG.info("remove {} (size= {})", pos, list.size());
			list.remove(pos);
			return state;
		}

		// Benchmarks executed by runBenchmarkCopy() (with params "type", "size")

		@Benchmark
		public Object testCopy(CopyListState state) {
			List<Integer> list = state.list();
			state.copy(list);
			return state;
		}

	}

	// States

	@State(Scope.Benchmark)
	public abstract static class ListState {

		//@Param({ "ArrayList" })
		@Param({ "ArrayList", "LinkedList", "GapList", "BigList", "TreeList" })
		//@Param({ "ArrayList", "GapList", "BigList", "GlueList" })
		String type;

		//@Param({ "1000000" })
		@Param({ "100", "10000", "1000000" })
		int size;

		List<Integer> sourceList;
		List<Integer> list;

		@Setup(Level.Trial)
		public void setupTrial() {
			// This setup is called before a trial (a sequence of all warmup / measurement iterations)
			//LOG.info("### setupTrial.start pid={}, type={}, op={}, size={}", SystemTools.getProcessId(), type, op, size);

			sourceList = initList(type, size);
			list = initList(type);
			list.addAll(sourceList);

			//LOG.info("### setupTrial.end");
		}

		List<Integer> initList(String type, int size) {
			List<Integer> list = initList(type);
			for (int i = 0; i < size; i++) {
				list.add(i);
			}
			return list;
		}

		List<Integer> initList(String type) {
			Supplier newOp = getNewOperation(type);
			List<Integer> list = (List<Integer>) newOp.get();
			return list;
		}

		List<Integer> list() {
			return list;
		}

	}

	@State(Scope.Benchmark)
	public abstract static class ListOpState extends ListState {

		final int step = 1;

		//@Param({ "Random" })
		@Param({ "First", "Last", "Mid", "Iter", "Random" })
		String op;

		Random r;
		int[] indexes;
		int pos;

		public ListOpState() {
			//LOG.info("ListOpState");
		}

		@Override
		@Setup(Level.Trial)
		public void setupTrial() {
			super.setupTrial();

			r = new Random(0);
			initIndexes(op, size);
		}

		@Setup(Level.Iteration)
		public void setupIteration() {
			// This setup is called before each warmup / measurement iteration
			//LOG.info("setupIteration.start");

			pos = 0;
			reset();

			//LOG.info("setupIteration.end");
		}

		@TearDown(Level.Iteration)
		public void teardownIteration() {
			// This teardown is called after each warmup / measurement iteration
			//LOG.info("teardownIteration");
		}

		abstract void initIndexes(String op, int size);

		int pos() {
			return indexes[pos++ % size];
		}

		@Override
		List<Integer> list() {
			if (pos == size) {
				reset();
				pos = 0;
			}
			return list;
		}

		void reset() {
			list = initList(type, size);
		}

	}

	@State(Scope.Benchmark)
	public static class GetListState extends ListOpState {
		public GetListState() {
			//LOG.info("GetListState");
		}

		@Override
		void reset() {
		}

		@Override
		void initIndexes(String op, int size) {
			indexes = new int[size];
			if ("First".equals(op)) {
				for (int i = 0; i < size; i++) {
					indexes[i] = 0;
				}
			} else if ("Last".equals(op)) {
				for (int i = 0; i < size; i++) {
					indexes[i] = size - 1;
				}
			} else if ("Mid".equals(op)) {
				for (int i = 0; i < size; i++) {
					indexes[i] = size / 2;
				}
			} else if ("Iter".equals(op)) {
				int pos = (size / 2) - (size * step / 2);
				for (int i = 0; i < size; i++) {
					indexes[i] = pos;
					pos += step;
				}
			} else if ("Random".equals(op)) {
				for (int i = 0; i < size; i++) {
					indexes[i] = r.nextInt(size);
				}
			} else {
				throw new AssertionError();
			}
		}
	}

	@State(Scope.Benchmark)
	public static class AddListState extends ListOpState {
		public AddListState() {
			//LOG.info("AddListState");
		}

		@Override
		void initIndexes(String op, int size) {
			indexes = new int[size];
			if ("First".equals(op)) {
				for (int i = 0; i < size; i++) {
					indexes[i] = 0;
				}
			} else if ("Last".equals(op)) {
				for (int i = 0; i < size; i++) {
					indexes[i] = size - 1 + i;
				}
			} else if ("Mid".equals(op)) {
				for (int i = 0; i < size; i++) {
					indexes[i] = size / 2;
				}
			} else if ("Iter".equals(op)) {
				int pos = (size / 2) - (size * step / 2);
				for (int i = 0; i < size; i++) {
					indexes[i] = pos;
					pos += step;
				}
			} else if ("Random".equals(op)) {
				for (int i = 0; i < size; i++) {
					indexes[i] = r.nextInt(size + i);
				}
			} else {
				throw new AssertionError();
			}
		}
	}

	@State(Scope.Benchmark)
	public static class RemoveListState extends ListOpState {
		public RemoveListState() {
			//LOG.info("RemoveListState");
		}

		@Override
		void initIndexes(String op, int size) {
			indexes = new int[size];
			if ("First".equals(op)) {
				for (int i = 0; i < size; i++) {
					indexes[i] = 0;
				}
			} else if ("Last".equals(op)) {
				for (int i = 0; i < size; i++) {
					indexes[i] = size - 1 - i;
				}
			} else if ("Mid".equals(op)) {
				for (int i = 0; i < size; i++) {
					indexes[i] = (size - i) / 2;
				}
			} else if ("Iter".equals(op)) {
				int pos = (size / 2) - (size * step / 2);
				for (int i = 0; i < size; i++) {
					if (pos >= size - i) {
						pos = 0;
					}
					indexes[i] = pos;
					pos += step;
				}
			} else if ("Random".equals(op)) {
				for (int i = 0; i < size; i++) {
					indexes[i] = r.nextInt(size - i);
				}
			} else {
				throw new AssertionError();
			}
		}
	}

	@State(Scope.Benchmark)
	public static class CopyListState extends ListState {
		UnaryOperator<?> copyOp;

		public CopyListState() {
			//LOG.info("CopyListState");
		}

		@Override
		@Setup(Level.Trial)
		public void setupTrial() {
			super.setupTrial();

			copyOp = getCopyOperation(type);
		}

		List<?> copy(List<?> list) {
			return (List<?>) ((UnaryOperator) copyOp).apply(list);
		}
	}

	//

	static Supplier<?> getNewOperation(String type) {
		if ("ArrayList".equals(type)) {
			return () -> new ArrayList();
		} else if ("LinkedList".equals(type)) {
			return () -> new LinkedList();
		} else if ("GapList".equals(type)) {
			return () -> new GapList();
		} else if ("IntGapList".equals(type)) {
			return () -> new IntGapList();
		} else if ("IntObjGapList".equals(type)) {
			return () -> new IntObjGapList();
		} else if ("BigList".equals(type)) {
			return () -> new BigList();
		} else if ("IntBigList".equals(type)) {
			return () -> new IntBigList();
		} else if ("IntObjBigList".equals(type)) {
			return () -> new IntObjBigList();
		} else if ("TreeList".equals(type)) {
			return () -> new TreeList();
			//} else if ("GlueList".equals(type)) {
			//return () -> new GlueList();
		} else {
			throw new AssertionError();
		}
	}

	static UnaryOperator<?> getCopyOperation(String type) {
		if ("ArrayList".equals(type)) {
			return (ArrayList l) -> (ArrayList) l.clone();
		} else if ("LinkedList".equals(type)) {
			return (LinkedList l) -> (LinkedList) l.clone();
		} else if ("GapList".equals(type)) {
			return (GapList l) -> l.copy();
		} else if ("BigList".equals(type)) {
			return (BigList l) -> l.copy();
		} else if ("TreeList".equals(type)) {
			return (TreeList l) -> new TreeList<>(l);
		} else {
			throw new AssertionError();
		}
	}

	//

	void showBenchmark() {
		ListTestPerformanceReport sb = new MyListTestPerformanceReport();

		//sb.showCharts();

		// Create tables as shown on http://www.magicwerk.org/page-collections-documentation.html
		sb.showTables();
	}

	/**
	 * Add additional table with object size to report of ListTestPerformanceReport.
	 */
	public static class MyListTestPerformanceReport extends ListTestPerformanceReport {

		MyListTestPerformanceReport() {
			String classifier = null;
			//String classifier = "java8";
			IList<String> names = GapList.create("ListTestPerformance", "ListTestCopyPerformance");

			files = names.map(n -> {
				if (classifier != null) {
					n = n + "-" + classifier;
				}
				return FilePath.of("output/" + n + ".json");
			});

			Function<String, Integer> f = s -> FuncTools.map(s, MapMode.ERROR,
					Predicates.is("Get"), 1,
					Predicates.is("Add"), 2,
					Predicates.is("Remove"), 3,
					Predicates.is("Copy"), 4);
			sortBenchmarks = Comparator.comparing(s -> f.apply(s));
		}

		@Override
		void renderTables() {
			// object size table
			renderObjectSizeTable();

			// benchmark performance table: 240 results: 3 sizes * 5 types * 16 operations
			super.renderTables();
		}

		void renderObjectSizeTable() {
			Table tab = getObjectSizeTable();
			HtmlTable ht = renderTable(tab);
			ht.setId("benchmark-memory");
			report.add(ht);
		}

		/** Create a table containing the size in bytes of different collections per number of elements */
		Table getObjectSizeTable() {
			IList<String> types = GapList.create("ArrayList", "LinkedList", "GapList", "IntObjGapList", "BigList", "IntObjBigList", "TreeList");
			IList<Integer> sizes = GapList.create(100, 100 * 100, 100 * 100 * 100);

			Table tab = new Table();
			tab.addCol("Size", Type.intType);
			for (String type : types) {
				tab.addCol(type, SizeNumberType); // e.g. "GapList"
			}

			for (int size : sizes) {
				IList<Double> vals = GapList.create();
				for (String type : types) {
					Supplier newOp = getNewOperation(type);
					List<Integer> list = (List<Integer>) newOp.get();
					for (int i = 0; i < size; i++) {
						list.add(i);
					}
					long objSize = ReflectTools.getObjectSize(list);
					vals.add((double) objSize);
				}

				// Add row with sizes
				IList<Object> row = GapList.create();
				row.add(size);
				row.addAll(vals);
				tab.addRowElems(row);

				// Add row with normalized factors
				normalizeNumbers(vals);
				row.clear();
				row.add(size);
				row.addAll(vals);
				tab.addRowElems(row);
			}

			return tab;
		}

	}

}
