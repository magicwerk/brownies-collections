package org.magicwerk.brownies.collections;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import org.magicwerk.brownies.core.CollectionTools;
import org.magicwerk.brownies.core.MathTools;
import org.magicwerk.brownies.core.ObjectTools;
import org.magicwerk.brownies.core.StringTools;
import org.magicwerk.brownies.core.files.FilePath;
import org.magicwerk.brownies.core.files.FileTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.print.PrintTools;
import org.magicwerk.brownies.core.reflect.ClassTools;
import org.magicwerk.brownies.core.strings.StringPrinter;
import org.magicwerk.brownies.core.types.Type;
import org.magicwerk.brownies.core.values.Table;
import org.magicwerk.brownies.html.HtmlDoclet;
import org.magicwerk.brownies.html.HtmlReport;
import org.magicwerk.brownies.html.HtmlTable;
import org.magicwerk.brownies.html.StyleResource;
import org.magicwerk.brownies.html.content.HtmlChartCreator;
import org.magicwerk.brownies.html.content.HtmlChartCreator.ChartType;
import org.magicwerk.brownies.html.content.HtmlTableFormatter;
import org.magicwerk.brownies.test.JmhRunner.BenchmarkJsonParser;
import org.magicwerk.brownies.test.JmhRunner.BenchmarkJsonResult;
import org.magicwerk.brownies.test.JmhRunner.BenchmarkJsonResult.BenchmarkTrial;
import org.slf4j.Logger;

public class GeneralPerformanceReport {

	public static class BenchmarkFile {
		String classifier;
		FilePath file;

		public BenchmarkFile(String classifier, FilePath file) {
			this.classifier = classifier;
			this.file = file;
		}
	}

	static final Logger LOG = LogbackTools.getLogger();

	static final String CLASSIFIER = "classifier";

	/** Result of benchmark run (representing one line of text in result output) */
	static class Result {
		String classifier;
		String benchmark;
		double score;

		Result(BenchmarkTrial bt) {
			classifier = getClassifier(bt);
			benchmark = getBenchmark(bt);
			score = bt.getScore();
		}

		String getClassifier(BenchmarkTrial bt) {
			return bt.getParams().get(CLASSIFIER);
		}

		String getBenchmark(BenchmarkTrial bt) {
			TreeMap<String, String> params = new TreeMap<>(bt.getParams());
			params.remove(CLASSIFIER);
			StringPrinter buf = new StringPrinter().setElemMarker("-");

			String title = ClassTools.getLocalNameByDot(bt.getBenchmark());
			title = StringTools.removeHeadIf(title, "test_");
			buf.add(title);

			params.values().forEach(v -> buf.add(v));
			return buf.toString();
		}

	}

	// Configuration
	IList<BenchmarkFile> files;
	Comparator<String> sortClassifiers;
	Comparator<String> sortBenchmarks;

	// State
	HtmlReport report;
	IList<GeneralPerformanceReport.Result> rs;
	IList<String> classifiers;
	IList<String> benchmarks;

	//

	/** Setter for {@link #files} */
	public GeneralPerformanceReport setFiles(IList<BenchmarkFile> files) {
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
		renderTable(rs, "");
	}

	void renderTable(IList<GeneralPerformanceReport.Result> rs, String size) {
		Table tab = getTable(rs);
		HtmlTable ht = renderTable(tab);
		ht.setId("benchmark-performance-" + size);
		report.add(ht);
	}

	void renderCharts() {
		renderChart(rs, "");
	}

	void renderChart(IList<GeneralPerformanceReport.Result> rs, String size) {
		Table tab = getTable(rs);
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
		creator.setChartType(ChartType.COLUMN);
		creator.setValueAxisLog(false);

		HtmlDoclet hd = creator.getChart();
		return hd;
	}

	HtmlTable renderTable(Table tab) {
		HtmlTableFormatter htf = new HtmlTableFormatter();
		HtmlTable ht = htf.format(tab);
		return ht;
	}

	Table getTable(IList<GeneralPerformanceReport.Result> rs) {
		Table tab = getTableHeader(rs);

		for (String benchmark : benchmarks) {
			IList<Object> row = GapList.create();
			row.add(benchmark);

			IList<Double> scores = GapList.create();
			for (String classifier : classifiers) {
				GeneralPerformanceReport.Result r = rs.getIf(
						n -> ObjectTools.equals(n.classifier, classifier) && ObjectTools.equals(n.benchmark, benchmark));
				scores.add(r.score);
			}

			row.addAll(scores);
			tab.addRowElems(row);
		}
		return tab;
	}

	static void normalizeNumbers(List<Double> vals) {
		double min = MathTools.min(vals);
		for (int i = 0; i < vals.size(); i++) {
			vals.set(i, vals.get(i) / min);
		}
	}

	Table getTableHeader(IList<GeneralPerformanceReport.Result> trs) {
		Table tab = new Table();

		tab.addCol("Benchmark", Type.STRING_TYPE);

		for (String classifier : classifiers) {
			tab.addCol(classifier, Type.DOUBLE_TYPE);
		}
		return tab;
	}

	/**
	 * Read benchmark files.
	 */
	void readBenchmarks() {
		rs = doReadBenchmarks();

		classifiers = CollectionTools.getDistinct(rs.map(r -> r.classifier));
		classifiers.sort(sortClassifiers);
		benchmarks = CollectionTools.getDistinct(rs.map(r -> r.benchmark));
		benchmarks.sort(sortBenchmarks);
	}

	IList<GeneralPerformanceReport.Result> doReadBenchmarks() {
		IList<GeneralPerformanceReport.Result> rs = GapList.create();
		for (BenchmarkFile file : files) {
			String text = FileTools.readFile().setFile(file.file).readText();
			BenchmarkJsonResult br = new BenchmarkJsonParser().parse(text);
			for (BenchmarkTrial bt : br.getResults()) {
				Result r = new GeneralPerformanceReport.Result(bt);
				r.classifier = file.classifier;
				rs.add(r);
			}
		}
		return rs;
	}

}
