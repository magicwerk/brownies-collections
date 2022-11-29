package org.magicwerk.brownies.collections;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.commons.collections4.list.TreeList;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.magicwerk.brownies.collections.primitive.IntBigList;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.magicwerk.brownies.collections.primitive.IntObjBigList;
import org.magicwerk.brownies.collections.primitive.IntObjGapList;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.CollectionTools;
import org.magicwerk.brownies.core.MathTools;
import org.magicwerk.brownies.core.ObjectTools;
import org.magicwerk.brownies.core.StringTools;
import org.magicwerk.brownies.core.collections.GridSelection;
import org.magicwerk.brownies.core.files.FilePath;
import org.magicwerk.brownies.core.files.FileTools;
import org.magicwerk.brownies.core.function.IFormatter;
import org.magicwerk.brownies.core.function.Predicates;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.reflect.ClassTools;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.core.time.DurationTools;
import org.magicwerk.brownies.core.types.ByteSizeType.ByteSizeTypeFormatter;
import org.magicwerk.brownies.core.types.Type;
import org.magicwerk.brownies.core.validator.NumberFormatter;
import org.magicwerk.brownies.core.values.Table;
import org.magicwerk.brownies.html.CssStyle;
import org.magicwerk.brownies.html.HtmlConst;
import org.magicwerk.brownies.html.HtmlDoclet;
import org.magicwerk.brownies.html.HtmlDocument;
import org.magicwerk.brownies.html.HtmlReport;
import org.magicwerk.brownies.html.HtmlTable;
import org.magicwerk.brownies.html.HtmlTools;
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
import org.magicwerk.brownies.tools.dev.tools.JavaTools.JavaVersion;
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
		boolean fast = false;
		runBenchmarkOp(fast);
		runBenchmarkCopy(fast);
	}

	void runBenchmarkOp(boolean fast) {
		Options opts = configure(fast);
		opts.includeMethod(ListTest.class, "testGet");
		opts.includeMethod(ListTest.class, "testAdd");
		opts.includeMethod(ListTest.class, "testRemove");

		opts.setResultFile("output/ListTestPerformance.json");
		opts.setLogFile("output/ListTestPerformance.log");

		opts.setJavaVersion(JavaVersion.JAVA_8);
		//opts.setJavaVersion(JavaVersion.JAVA_11);

		//opts.setUseGcProfiler(true);

		JmhRunner runner = new JmhRunner();
		runner.setVerbose(true);
		runner.runJmh(opts);
	}

	Options configure(boolean fast) {
		Options opts = new Options();
		if (fast) {
			opts.setWarmupIterations(0);
			opts.setMeasurementIterations(1);
			opts.setRunTimeMillis(100);
		} else {
			opts.setWarmupIterations(25);
			opts.setMeasurementIterations(25);
			opts.setRunTimeMillis(100);
		}
		opts.setJvmArgs(jvmArgs);
		return opts;
	}

	void runBenchmarkCopy(boolean fast) {
		Options opts = configure(fast);
		opts.includeMethod(ListTest.class, "testCopy");

		opts.setResultFile("output/ListTestCopyPerformance.json");
		opts.setLogFile("output/ListTestCopyPerformance.log");
		JmhRunner runner = new JmhRunner();
		runner.runJmh(opts);
	}

	// Benchmarks

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
			if (type.equals("ArrayList") && size == 1000000) {
				throw new RuntimeException();
			}
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
		String classifier = "java8";
		IList<String> names = GapList.create("ListTestPerformance", "ListTestCopyPerformance");
		IList<FilePath> files = names.mappedList(n -> {
			if (classifier != null) {
				n = n + "-" + classifier;
			}
			return FilePath.of("output/" + n + ".json");
		});

		ShowBenchmark sb = new ShowBenchmark();
		sb.benchmarks = benchmarks;
		sb.files = files;
		sb.showTables();
	}

	public static class ShowBenchmark {

		static Type<Double> FactorNumberType = Type.builder(Double.class).with(new NumberFormatter<Double>(2)).toType();
		static IFormatter<Double> timeFormatter = s -> DurationTools.formatSeconds(s);
		static Type<Double> TimeNumberType = Type.builder(Double.class).with(timeFormatter).toType();
		static ByteSizeTypeFormatter byteSizeFormatter = new ByteSizeTypeFormatter(false);
		static IFormatter<Double> sizeFormatter = d -> byteSizeFormatter.format(d.longValue());
		static Type<Double> SizeNumberType = Type.builder(Double.class).with(sizeFormatter).toType();

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

		// Configuration
		IList<FilePath> files;

		// State
		IList<String> benchmarks;
		IList<String> sizes;
		IList<String> types;
		IList<String> ops;

		//

		/** Setter for {@link #files} */
		public ShowBenchmark setFiles(IList<FilePath> files) {
			this.files = files;
			return this;
		}

		public void showTables() {
			IList<Result> rs = readBenchmarks();

			HtmlReport report = new HtmlReport();
			report.add(StyleResource.INSTANCE);

			renderObjectSizeTable(report);
			renderBenchmarkTables(report, rs);

			report.setHtmlFile("output/tables.html");
			report.showHtml();
		}

		/**
		 * Read benchmark files.
		 */
		IList<Result> readBenchmarks() {
			IList<BenchmarkTrial> brs = GapList.create();
			for (FilePath file : files) {
				String text = FileTools.readFile().setFile(file).readText();
				BenchmarkJsonResult br = new BenchmarkJsonParser().parse(text);
				brs.addAll(br.getResults());
			}
			IList<Result> rs = brs.mappedList(Result::new);
			return rs;
		}

		void renderBenchmarkTables(HtmlReport report, IList<Result> rs) {
			benchmarks = CollectionTools.getDistinct(rs.mappedList(r -> r.benchmark));
			sizes = CollectionTools.getDistinct(rs.mappedList(r -> r.size));
			types = CollectionTools.getDistinct(rs.mappedList(r -> r.type));
			ops = CollectionTools.getDistinct(rs.mappedList(r -> r.op));

			for (String size : sizes) {
				IList<Result> rs2 = rs.filteredList(r -> r.size.equals(size));
				Table tab = getTable(rs2);
				HtmlTable ht = renderTable(tab);
				ht.setId("benchmark-performance-" + size);
				report.add(ht);
			}
		}

		void renderObjectSizeTable(HtmlReport report) {
			Table tab = getObjectSizeTable();
			HtmlTable ht = renderTable(tab);
			ht.setId("benchmark-memory");
			report.add(ht);
		}

		HtmlTable renderTable(Table tab) {
			String colBest = "#99ff99";
			String colGood = "#88aaff";
			String colModerate = "#ffff99";
			String colBad = "#ff9999";
			double valRed = 10;
			double valYellow = 2;
			double valBlue = 1;

			LOG.info("{}", tab);

			ConditionalFormatter cf = new ConditionalFormatter();
			cf.add(c -> (double) c.getValue() > valRed, t -> getCssStyle(colBad).getAttribute());
			cf.add(c -> (double) c.getValue() > valYellow, t -> getCssStyle(colModerate).getAttribute());
			cf.add(c -> (double) c.getValue() > valBlue, t -> getCssStyle(colGood).getAttribute());
			cf.add(Predicates.allow(), t -> getCssStyle(colBest).getAttribute());

			HtmlFormatters hf = new HtmlFormatters();
			for (int r = 0; r < tab.getNumRows(); r += 2) {
				hf.addFormatter(GridSelection.Region(r + 1, 1, r + 1, tab.getNumCols() - 1), cf);
			}

			HtmlTableFormatter htf = new HtmlTableFormatter();
			htf.setFormatters(hf);
			HtmlTable ht = htf.format(tab);

			boolean removeFactorRows = true;
			Element e = ht.getElement();
			int rows = HtmlTools.getTableNumRows(e);
			for (int r = 1; r < rows; r++) {
				IList<Element> src = HtmlTools.getTableRowCells(e, r + 1);
				IList<Element> dst = HtmlTools.getTableRowCells(e, r);
				copyFormatting(src, dst);

				if (removeFactorRows) {
					HtmlTools.removeTableRow(e, r + 1);
					rows--;
				} else {
					r++;
				}
			}

			return ht;
		}

		CssStyle getCssStyle(String bgColor) {
			String alignRight = "right";
			return new CssStyle().setBackgroundColor(bgColor).setTextAlign(alignRight).set("width", "84px");
		}

		/** Apply formatting by copying the style attribute. */
		void copyFormatting(IList<Element> srcElems, IList<Element> dstElems) {
			BiConsumer<Element, Element> handler = (src, dst) -> {
				Attribute attr = src.getAttribute(HtmlConst.ATTR_STYLE);
				if (attr != null) {
					dst.setAttribute(attr.clone());
				}
			};
			copyFormatting(srcElems, dstElems, handler);
		}

		void copyFormatting(IList<Element> srcElems, IList<Element> dstElems, BiConsumer<Element, Element> handler) {
			CheckTools.check(srcElems.size() == dstElems.size());
			for (int i = 0; i < srcElems.size(); i++) {
				handler.accept(srcElems.get(i), dstElems.get(i));
			}
		}

		Table getTable(IList<Result> rs) {
			Table tab = getTableHeader(rs);

			for (String benchmark : benchmarks) {
				for (String op : ops) {
					IList<Double> times = GapList.create();
					for (String type : types) {
						Result r = rs.getIf(
								n -> ObjectTools.equals(n.benchmark, benchmark) && ObjectTools.equals(n.op, op) && ObjectTools.equals(n.type, type));
						if (r != null) {
							double time = 1.0 / r.score;
							times.add(time);
						}
					}
					if (times.isEmpty()) {
						continue;
					}

					String name = (op != null) ? benchmark + " " + op : benchmark;

					// Add row with times
					IList<Object> row = GapList.create();
					row.add(name);
					row.addAll(times);
					tab.addRowElems(row);

					// Add row with normalized factors
					normalizeNumbers(times);
					row.clear();
					row.add(name);
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

		Table getTableHeader(IList<Result> trs) {
			Table tab = new Table();

			Result tr = trs.getFirst();
			tab.addCol("Size= " + tr.size, Type.STRING_TYPE);

			for (String type : types) {
				tab.addCol(type, TimeNumberType); // e.g. "GapList"
			}
			return tab;
		}

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

		// Charts - NOT USED

		void showChart(IList<Result> trs) {
			Table tab = getTable(trs);
			LOG.info("{}", tab);

			HtmlDocument doc = getChartDoc(tab);
			HtmlReport report = new HtmlReport();
			report.setDoc(doc);
			report.showHtml();
		}

		HtmlDocument getChartDoc(Table tab) {
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

	}

}
