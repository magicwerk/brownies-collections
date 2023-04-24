package org.magicwerk.brownies.collections;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.jdom2.Attribute;
import org.jdom2.Element;
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
import org.magicwerk.brownies.core.print.PrintTools;
import org.magicwerk.brownies.core.reflect.ClassTools;
import org.magicwerk.brownies.core.time.DurationTools;
import org.magicwerk.brownies.core.types.ByteSizeType.ByteSizeTypeFormatter;
import org.magicwerk.brownies.core.types.Type;
import org.magicwerk.brownies.core.validator.NumberFormatter;
import org.magicwerk.brownies.core.values.Table;
import org.magicwerk.brownies.html.CssStyle;
import org.magicwerk.brownies.html.HtmlConst;
import org.magicwerk.brownies.html.HtmlDoclet;
import org.magicwerk.brownies.html.HtmlReport;
import org.magicwerk.brownies.html.HtmlTable;
import org.magicwerk.brownies.html.HtmlTools;
import org.magicwerk.brownies.html.StyleResource;
import org.magicwerk.brownies.html.content.HtmlChartCreator;
import org.magicwerk.brownies.html.content.HtmlChartCreator.ChartType;
import org.magicwerk.brownies.html.content.HtmlFormatters;
import org.magicwerk.brownies.html.content.HtmlFormatters.ConditionalFormatter;
import org.magicwerk.brownies.html.content.HtmlTableFormatter;
import org.magicwerk.brownies.test.JmhRunner.BenchmarkJsonParser;
import org.magicwerk.brownies.test.JmhRunner.BenchmarkJsonResult;
import org.magicwerk.brownies.test.JmhRunner.BenchmarkJsonResult.BenchmarkTrial;
import org.slf4j.Logger;

public class ListTestPerformanceReport {

	static final Logger LOG = LogbackTools.getLogger();

	static Type<Double> FactorNumberType = Type.builder(Double.class).with(new NumberFormatter<Double>(2)).toType();
	static IFormatter<Double> timeFormatter = s -> DurationTools.formatSeconds(s);
	static Type<Double> TimeNumberType = Type.builder(Double.class).with(timeFormatter).toType();
	static ByteSizeTypeFormatter byteSizeFormatter = new ByteSizeTypeFormatter(false);
	static IFormatter<Double> sizeFormatter = d -> byteSizeFormatter.format(d.longValue());
	static Type<Double> SizeNumberType = Type.builder(Double.class).with(sizeFormatter).toType();

	/** Result of benchmark run (representing one line of text in result output */
	static class Result {
		String benchmark;
		double score;
		/** Benchmark value {@literal @Param} type */
		String type;
		/** Benchmark value {@literal @Param} op */
		String op;
		/** Benchmark value {@literal @Param} size */
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
	Comparator<String> sortBenchmarks;

	// State
	HtmlReport report;
	IList<ListTestPerformanceReport.Result> rs;
	IList<String> benchmarks;
	IList<String> sizes;
	IList<String> types;
	IList<String> ops;

	//

	/** Setter for {@link #files} */
	public ListTestPerformanceReport setFiles(IList<FilePath> files) {
		this.files = files;
		return this;
	}

	HtmlReport createHtmlReport() {
		HtmlReport report = new HtmlReport();
		report.add(StyleResource.INSTANCE);
		report.setHtmlFile("output/benchmarks.html");
		return report;
	}

	public void showTables() {
		report = createHtmlReport();
		readBenchmarks();
		renderTables();
		report.showHtml();
	}

	public void showCharts() {
		report = createHtmlReport();
		readBenchmarks();
		renderCharts();
		report.showHtml();
	}

	void renderTables() {
		if (sizes.isEmpty()) {
			renderTable(rs, "");
		} else {
			for (String size : sizes) {
				IList<ListTestPerformanceReport.Result> rs2 = rs.filter(r -> r.size.equals(size));
				renderTable(rs2, size);
			}
		}
	}

	void renderTable(IList<ListTestPerformanceReport.Result> rs, String size) {
		Table tab = getTable(rs, true);
		HtmlTable ht = renderTable(tab);
		ht.setId("benchmark-performance-" + size);
		report.add(ht);
	}

	void renderCharts() {
		if (sizes.isEmpty()) {
			renderChart(rs, "");
		} else {
			for (String size : sizes) {
				IList<ListTestPerformanceReport.Result> rs2 = rs.filter(r -> r.size.equals(size));
				renderChart(rs, size);
			}
		}
	}

	void renderChart(IList<ListTestPerformanceReport.Result> rs, String size) {
		Table tab = getTable(rs, false);
		LOG.info("{}", tab);
		HtmlDoclet doc = getChartDoc(tab);
		report.add(doc);
	}

	HtmlDoclet getChartDoc(Table tab) {
		String title = PrintTools.toString(tab.getColName(0), "");

		HtmlChartCreator creator = new HtmlChartCreator();
		creator.setTitle(title);
		creator.setWidth("960px");
		creator.setHeight("720px");
		creator.setTable(tab);
		creator.setChartType(ChartType.LINE);
		creator.setValueAxisLog(true);

		HtmlDoclet hd = creator.getChart();
		return hd;
	}

	HtmlTable renderTable(Table tab) {
		String colBest = "#99ff99";
		String colGood = "#88aaff";
		String colModerate = "#ffff99";
		String colBad = "#ff9999";
		double valRed = 10;
		double valYellow = 2;
		double valBlue = 1;

		ListTestPerformance.LOG.info("{}", tab);

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

	Table getTable(IList<ListTestPerformanceReport.Result> rs, boolean addNormalizedRows) {
		Table tab = getTableHeader(rs);

		for (String benchmark : benchmarks) {
			for (String op : ops) {
				IList<Double> times = GapList.create();
				for (String type : types) {
					ListTestPerformanceReport.Result r = rs.getIf(
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

				if (addNormalizedRows) {
					// Add row with normalized factors (used to set the appropriate colors)
					normalizeNumbers(times);
					row.clear();
					row.add(name);
					row.addAll(times);
					tab.addRowElems(row);
				}
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

	Table getTableHeader(IList<ListTestPerformanceReport.Result> trs) {
		Table tab = new Table();

		ListTestPerformanceReport.Result tr = trs.getFirst();
		tab.addCol("Size= " + tr.size, Type.STRING_TYPE);

		for (String type : types) {
			tab.addCol(type, TimeNumberType); // e.g. "GapList"
		}
		return tab;
	}

	/**
	 * Read benchmark files.
	 */
	void readBenchmarks() {
		rs = doReadBenchmarks();

		benchmarks = CollectionTools.getDistinct(rs.map(r -> r.benchmark));
		benchmarks.sort(sortBenchmarks);

		sizes = CollectionTools.getDistinct(rs.mapFilter(r -> r.size, v -> v != null));
		types = CollectionTools.getDistinct(rs.mapFilter(r -> r.type, v -> v != null));
		ops = CollectionTools.getDistinct(rs.mapFilter(r -> r.op, v -> v != null));
	}

	IList<ListTestPerformanceReport.Result> doReadBenchmarks() {
		IList<BenchmarkTrial> brs = GapList.create();
		for (FilePath file : files) {
			String text = FileTools.readFile().setFile(file).readText();
			BenchmarkJsonResult br = new BenchmarkJsonParser().parse(text);
			brs.addAll(br.getResults());
		}
		IList<ListTestPerformanceReport.Result> rs = brs.map(ListTestPerformanceReport.Result::new);
		return rs;
	}

}
