package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.collections4.list.TreeList;
import org.magicwerk.brownies.collections.primitive.IntBigList;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.magicwerk.brownies.collections.primitive.IntObjBigList;
import org.magicwerk.brownies.collections.primitive.IntObjGapList;
import org.magicwerk.brownies.core.CollectionTools;
import org.magicwerk.brownies.core.MathTools;
import org.magicwerk.brownies.core.StringTools;
import org.magicwerk.brownies.core.collections.GridSelection;
import org.magicwerk.brownies.core.files.FileTools;
import org.magicwerk.brownies.core.function.IFormatter;
import org.magicwerk.brownies.core.function.Predicates;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ClassTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.core.types.Type;
import org.magicwerk.brownies.core.validator.NumberFormatter;
import org.magicwerk.brownies.core.values.Table;
import org.magicwerk.brownies.html.CssStyle;
import org.magicwerk.brownies.html.HtmlDoclet;
import org.magicwerk.brownies.html.HtmlDocument;
import org.magicwerk.brownies.html.HtmlReport;
import org.magicwerk.brownies.html.HtmlTable;
import org.magicwerk.brownies.html.StyleResource;
import org.magicwerk.brownies.html.content.HtmlChartCreator;
import org.magicwerk.brownies.html.content.HtmlChartCreator.ChartType;
import org.magicwerk.brownies.html.content.HtmlFormatters;
import org.magicwerk.brownies.html.content.HtmlFormatters.ConditionalFormatter;
import org.magicwerk.brownies.html.content.HtmlTableFormatter;
import org.magicwerk.brownies.test.JmhRunner;
import org.magicwerk.brownies.test.JmhRunner.BenchmarkJsonParser;
import org.magicwerk.brownies.test.JmhRunner.BenchmarkJsonResult;
import org.magicwerk.brownies.test.JmhRunner.BenchmarkJsonResult.BenchmarkTrial;
import org.magicwerk.brownies.test.JmhRunner.Options;
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
 * 
 * @author Thomas Mauch
 */
public class ListTestPerformance {

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		new ListTestPerformance().run();
	}

	void run() {
		runBenchmark();
		//showBenchmark();
	}

	//

	void showBenchmark() {
		ShowBenchmark sb = new ShowBenchmark("../Brownies-Collections/output/ListTestPerformance.json");
		sb.benchmarks = benchmarks;
		sb.showTables();
	}

	static class ShowBenchmark {

		static class Result {
			String benchmark;
			double score;
			String type;
			String op;
			String size;

			Result(BenchmarkTrial bt) {
				benchmark = convertBenchmark(bt.getBenchmark());
				score = bt.getScore();
				Map<String, String> p = bt.getParams();
				type = p.get("type");
				op = p.get("op");
				size = p.get("size");
			}

			String convertBenchmark(String str) {
				String n = ClassTools.getLocalNameByDot(str);
				return StringTools.removeHead(n, "test");
			}
		}

		String jsonFile;
		IList<Result> rs;
		IList<String> benchmarks;
		IList<String> sizes;
		IList<String> types;
		IList<String> ops;

		ShowBenchmark(String jsonFile) {
			this.jsonFile = jsonFile;
		}

		void showTables() {
			HtmlReport report = new HtmlReport();
			report.add(StyleResource.INSTANCE);

			renderObjectSizeTable(report);
			renderBenchmarkTables(report);

			report.showHtml();
		}

		void renderBenchmarkTables(HtmlReport report) {
			String text = FileTools.readFile().setFile(jsonFile).readText();
			BenchmarkJsonResult br = new BenchmarkJsonParser().parse(text);
			IList<BenchmarkTrial> brs = br.getResults();

			rs = brs.mappedList(Result::new);
			if (benchmarks == null) {
				benchmarks = CollectionTools.getDistinct(rs.mappedList(r -> r.benchmark));
			}
			sizes = CollectionTools.getDistinct(rs.mappedList(r -> r.size));
			types = CollectionTools.getDistinct(rs.mappedList(r -> r.type));
			ops = CollectionTools.getDistinct(rs.mappedList(r -> r.op));

			for (String size : sizes) {
				IList<Result> rs2 = rs.filteredList(r -> r.size.equals(size));
				Table tab = getTable(rs2, true);
				HtmlTable ht = renderTable(tab);
				report.add(ht);
			}
		}

		void renderObjectSizeTable(HtmlReport report) {
			Table tab = getObjectSizeTable();
			HtmlTable ht = renderTable(tab);
			report.add(ht);
		}

		HtmlTable renderTable(Table tab) {
			String colBest = "#88ff88";
			String colGood = "#4488ff";
			String colModerate = "#ffff88";
			String colBad = "#ff8888";

			LOG.info("{}", tab);

			ConditionalFormatter cf = new ConditionalFormatter();
			cf.add(c -> (double) c.getValue() > 25, t -> new CssStyle().setBackgroundColor(colBad).getAttribute());
			cf.add(c -> (double) c.getValue() > 5, t -> new CssStyle().setBackgroundColor(colModerate).getAttribute());
			cf.add(c -> (double) c.getValue() > 1, t -> new CssStyle().setBackgroundColor(colGood).getAttribute());
			cf.add(Predicates.allow(), t -> new CssStyle().setBackgroundColor(colBest).getAttribute());

			HtmlFormatters hf = new HtmlFormatters();
			hf.addFormatter(GridSelection.Region(0, 1, tab.getNumRows() - 1, tab.getNumCols() - 1), cf);

			HtmlTableFormatter htf = new HtmlTableFormatter();
			htf.setFormatters(hf);
			return htf.format(tab);
		}

		void showChart2(IList<Result> trs) {
			Table tab = getTable(trs, false);
			LOG.info("{}", tab);

			HtmlDocument doc = getDoc(tab);
			HtmlReport report = new HtmlReport();
			report.setDoc(doc);
			report.showHtml();
		}

		HtmlDocument getDoc(Table tab) {
			HtmlDocument doc = new HtmlDocument();
			doc.getBody().addH1("Charts");

			HtmlChartCreator creator = new HtmlChartCreator();
			creator.setTitle("Chart");
			creator.setWidth("800px");
			creator.setHeight("400px");

			creator.setTable(tab);
			creator.setChartType(ChartType.LINE);
			HtmlDoclet chart = creator.getChart();
			doc.addResources(chart.getResources());
			doc.getBody().addElem(chart.getElement());
			return doc;
		}

		Table getTable(IList<Result> rs, boolean factor) {
			Table tab = getTableHeader(rs, factor);

			for (String benchmark : benchmarks) {
				for (String op : ops) {
					IList<Object> row = GapList.create();

					String name = benchmark + " " + op;
					row.add(name);

					IList<Double> times = GapList.create();
					for (String type : types) {
						Result r = rs.getIf(n -> n.benchmark.equals(benchmark) && n.op.equals(op) && n.type.equals(type));

						double time = 1.0 / r.score;
						times.add(time);
					}

					if (factor) {
						normalizeNumbers(times);
					}

					row.addAll(times);
					tab.addRowElems(row);
				}
			}
			return tab;
		}

		static void normalizeNumbers(List<Double> vals) {
			double min = MathTools.min(vals);
			for (int i = 0; i < vals.size(); i++) {
				vals.set(i, vals.get(i) / min);
			}
		}

		Table getTableHeader(IList<Result> trs, boolean factor) {
			Table tab = new Table();

			Result tr = trs.getFirst();
			tab.addCol("Size= " + tr.size, Type.STRING_TYPE);

			Type<Double> numberType = (factor) ? FactorNumberType : Type.DOUBLE_TYPE;
			for (String type : types) {
				tab.addCol(type, numberType); // e.g. "GapList"
			}
			return tab;
		}

		Table getObjectSizeTable() {
			IList<String> types = GapList.create("ArrayList", "LinkedList", "GapList", "IntObjGapList", "BigList", "IntObjBigList", "TreeList");
			IList<Integer> sizes = GapList.create(100, 100 * 100, 100 * 100 * 100);

			Table tab = new Table();
			tab.addCol("Size", Type.intType);
			for (String type : types) {
				tab.addCol(type, FactorNumberType); // e.g. "GapList"
			}

			for (int size : sizes) {
				IList<Object> row = GapList.create();
				row.add(size);

				IList<Double> vals = GapList.create();
				for (String type : types) {
					Supplier newOp = getNewOperation(type);
					List<Integer> list = (List<Integer>) newOp.get();
					for (int i = 0; i < size; i++) {
						list.add(i);
					}
					int objSize = ReflectTools.getObjectSize(list);
					vals.add((double) objSize);
				}

				normalizeNumbers(vals);

				row.addAll(vals);
				tab.addRowElems(row);
			}

			return tab;
		}

		static Type<Double> FactorNumberType = Type.builder(Double.class).with((IFormatter) new NumberFormatter(2)).toType();

	}

	//

	IList<String> benchmarks = GapList.create("Get", "Add", "Remove", "Copy");

	void runBenchmark() {
		Options opts = new Options();
		opts.includeMethod(ListTest.class, "testGet");
		opts.includeMethod(ListTest.class, "testAdd");
		opts.includeMethod(ListTest.class, "testRemove");
		//opts.includeMethod(ListTest.class, "testCopy");

		int numIter = ListState.numIter;
		opts.setJvmArgs(GapList.create("-Xmx4g", "-Xms4g", "-XX:+UseG1GC"));
		if (numIter != 0) {
			opts.setWarmupIterations(numIter);
			opts.setMeasurementIterations(numIter);
		} else {
			opts.setWarmupIterations(0);
			opts.setMeasurementIterations(1);
		}
		opts.setRunTimeMillis(ListState.runTimeMillis);
		opts.setResultFile("output/ListTestPerformance.json");
		opts.setLogFile("output/ListTestPerformance.log");
		JmhRunner runner = new JmhRunner();
		runner.runJmh(opts);
	}

	@State(Scope.Benchmark)
	public abstract static class ListState {

		static final boolean fast = false;

		static final int numIter = (fast) ? 0 : 5;
		static final int runTimeMillis = (fast) ? 100 : 1000;

		final int step = 1;

		//@Param({ "ArrayList" })
		@Param({ "ArrayList", "LinkedList", "GapList", "BigList", "TreeList" })
		String type;

		//@Param({ "Iter" })
		@Param({ "First", "Last", "Mid", "Iter", "Random" })
		String op;

		//@Param({ "100" })
		@Param({ "100", "10000", "1000000" })
		int size;

		Random r;
		List<Integer> sourceList;
		List<Integer> list;
		int[] indexes;
		int pos;

		public ListState() {
			//LOG.info("ListState");
		}

		@Setup(Level.Trial)
		public void setupTrial() {
			// This setup is called before a trial (a sequence of all warmup / measurement iterations)
			//LOG.info("### setupTrial.start pid={}, type={}, op={}, size={}", SystemTools.getProcessId(), type, op, size);

			sourceList = initList(type, size);
			list = initList(type);
			list.addAll(sourceList);

			r = new Random(0);
			initIndexes(op, size);

			//LOG.info("### setupTrial.end");
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

		abstract void initIndexes(String op, int size);

		int pos() {
			return indexes[pos++ % size];
		}

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
	public static class GetListState extends ListState {
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
	public static class AddListState extends ListState {

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
	public static class RemoveListState extends ListState {
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
		void initIndexes(String op, int batchSize) {
			// indexes are not needed, but use it to setup copyOp
			copyOp = getCopyOperation(type);
		}

		List<?> copy(List<?> list) {
			return (List<?>) ((UnaryOperator) copyOp).apply(list);
		}

		@Override
		void reset() {
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

	// Get

	public static class ListTest {

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

		@Benchmark
		public Object testCopy(CopyListState state) {
			List<Integer> list = state.list();
			state.copy(list);
			return state;
		}

	}

}
