package org.magicwerk.brownies.collections;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.magicwerk.brownies.collections.primitive.DoubleGapList;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.CollectionTools;
import org.magicwerk.brownies.core.MathTools;
import org.magicwerk.brownies.core.PrintTools;
import org.magicwerk.brownies.core.TypeTools;
import org.magicwerk.brownies.core.collections.CrossList;
import org.magicwerk.brownies.core.collections.Grid;
import org.magicwerk.brownies.core.collections.PivotTable;
import org.magicwerk.brownies.core.collections.PivotTableCreator;
import org.magicwerk.brownies.core.files.FileTools;
import org.magicwerk.brownies.core.logback.LogbackTools;
import org.magicwerk.brownies.core.types.Type;
import org.magicwerk.brownies.core.values.Record;
import org.magicwerk.brownies.core.values.Table;
import org.magicwerk.brownies.core.values.TableTools;
import org.magicwerk.brownies.core.values.io.FieldDelimTableReader;
import org.magicwerk.brownies.core.values.io.FieldDelimTableWriter;
import org.magicwerk.brownies.core.values.io.TableWriter;
import org.magicwerk.brownies.html.CssColor;
import org.magicwerk.brownies.html.HtmlBlock;
import org.magicwerk.brownies.html.HtmlDiv;
import org.magicwerk.brownies.html.HtmlDocument;
import org.magicwerk.brownies.html.HtmlTable;
import org.magicwerk.brownies.html.HtmlTools;
import org.magicwerk.brownies.html.HtmlTr;
import org.magicwerk.brownies.html.content.HtmlTableFormatter;

import ch.qos.logback.classic.Logger;

public class PerformanceReport {

	static String Release_090_J170_40 = "0.258\r\n" + "0.074\r\n" + "0.046\r\n" + "\r\n" + "0.235\r\n" + "0.068\r\n" + "0.045\r\n" + "\r\n" + "1.329\r\n"
			+ "1.649\r\n" + "2143.11\r\n" + "\r\n" + "0.121\r\n" + "0.063\r\n" + "0.126\r\n" + "\r\n" + "0.158\r\n" + "34.336\r\n" + "0.118\r\n" + "\r\n"
			+ "50.464\r\n" + "18.777\r\n" + "111.431\r\n" + "\r\n" + "5.957\r\n" + "16.258\r\n" + "80.75\r\n" + "\r\n" + "1.611\r\n" + "26.991\r\n"
			+ "60.61\r\n" + "\r\n" + "0.671\r\n" + "12.855\r\n" + "0.399\r\n";

	static String Release_090_J160_45 = "0.234\r\n" + "0.221\r\n" + "0.076\r\n" + "\r\n" + "0.237\r\n" + "0.206\r\n" + "0.076\r\n" + "\r\n" + "1.641\r\n"
			+ "2.102\r\n" + "2147.591\r\n" + "\r\n" + "0.124\r\n" + "0.113\r\n" + "0.130\r\n" + "\r\n" + "0.159\r\n" + "103.793\r\n" + "0.137\r\n" + "\r\n"
			+ "79.958\r\n" + "52.943\r\n" + "106.815\r\n" + "\r\n" + "7.988\r\n" + "50.553\r\n" + "82.171\r\n" + "\r\n" + "2.207\r\n" + "85.157\r\n"
			+ "62.789\r\n" + "\r\n" + "0.933\r\n" + "35.041\r\n" + "0.406\r\n";

	static String Release_090_J160_Published = "0.458\r\n" + "0.564\r\n" + "0.157\r\n" + "\r\n" + "0.440\r\n" + "0.443\r\n" + "0.153\r\n" + "\r\n" + "3.575\r\n"
			+ "4.036\r\n" + "7680.000\r\n" + "\r\n" + "2.100\r\n" + "1.500\r\n" + "1.300\r\n" + "\r\n" + "2.500\r\n" + "6259.000\r\n" + "1.300\r\n" + "\r\n"
			+ "5.200\r\n" + "3.000\r\n" + "25.400\r\n" + "\r\n" + "396.000\r\n" + "2743.000\r\n" + "13711.000\r\n" + "\r\n" + "55.000\r\n" + "4624.000\r\n"
			+ "11437.000\r\n" + "\r\n" + "11.000\r\n" + "2169.000\r\n" + "6.000\r\n";

	static final Logger LOG = LogbackTools.getConsoleLogger();

	public static void main(String[] args) {
		run();
	}

	static void run() {
		PerformanceReport pr = new PerformanceReport();
		pr.load();
		//pr.init();
		//pr.save();
		pr.writeReport();
	}

	public static List<String> parseStrings(String str) {
		String[] strs = str.split("[\t\r\n ]+");
		return GapList.create(strs);
	}

	public static List<Object> parseValues(String str) {
		List<String> strs = parseStrings(str);
		GapList<Object> vals = new GapList<>(strs.size());
		for (String s : strs) {
			vals.add(TypeTools.parse(s));
		}
		return vals;
	}

	public static class RunInfo {
		String java;
		String vmArgs;
		String release;
		String run;
		String type;
		double time;

		public String getJava() {
			return java;
		}

		public RunInfo setJava(String java) {
			this.java = java;
			return this;
		}

		public String getVmArgs() {
			return vmArgs;
		}

		public RunInfo setVmArgs(String vmArgs) {
			this.vmArgs = vmArgs;
			return this;
		}

		public String getRelease() {
			return release;
		}

		public RunInfo setRelease(String release) {
			this.release = release;
			return this;
		}

		public String getRun() {
			return run;
		}

		public RunInfo setRun(String run) {
			this.run = run;
			return this;
		}

		public String getType() {
			return type;
		}

		public RunInfo setType(String type) {
			this.type = type;
			return this;
		}

		public double getTime() {
			return time;
		}

		public RunInfo setTime(double time) {
			this.time = time;
			return this;
		}

	}

	static String file = "testdata/performance.csv";
	static String REPORT_FILE = "testdata/performance.html";

	static String JAVA = "Java";
	static String VMARGS = "VmArgs";
	static String RELEASE = "Release";
	static String RUN = "Run";
	static String TYPE = "Type";
	static String TIME = "Time";

	Table table;

	public PerformanceReport() {
	}

	static String JAVA_PUB = "1.6.0";
	static String RELEASE_PUB = "0.9.0-pub";
	static String RELEASE_090 = "0.9.0";
	static String JAVA_160_45 = "1.6.0_45";
	static String JAVA_170_40 = "1.7.0_40";

	public void deleteRelease(String release) {
		PivotTableCreator.deleteRecords(table, Record.create(RELEASE, release));
	}

	public void init() {
		table = new Table();

		//deleteRecords(table, Record.create(RELEASE, RELEASE_PUB));
		//deleteRecords(table, Record.create(RELEASE, RELEASE_090));

		Table t = new Table();
		addToTable(t, Release_090_J160_Published, JAVA_PUB, RELEASE_PUB);
		addToTable(t, Release_090_J160_45, JAVA_160_45, RELEASE_090);
		addToTable(t, Release_090_J170_40, JAVA_170_40, RELEASE_090);
		TableTools.addTable(t, table, false);
		table = t;
	}

	static void addToTable(Table table, String valsStr, String java, String release) {
		List<Object> vals = parseValues(valsStr);
		GapList<String> runs = GapList.create("Get last", "Get first", "Get random", "Add last", "Add first", "Add random", "Add near 0.1", "Add near 0.01",
				"Add iter 2.0");
		GapList<String> types = GapList.create("GapList", "ArrayList", "LinkedList");
		CrossList<String> cl = new CrossList<String>();
		cl.add(runs);
		cl.add(types);
		assert (cl.size() == vals.size());

		for (int i = 0; i < cl.size(); i++) {
			List<String> l = cl.get(i);
			Record rec = Record.create(JAVA, java, RELEASE, release, RUN, l.get(0), TYPE, l.get(1), TIME, vals.get(i));
			TableTools.addRecord(table, rec, true);
		}
	}

	public void addRun(RunInfo runInfo) {
		Record rec = Record.create(JAVA, runInfo.getJava(), VMARGS, runInfo.getVmArgs(), RUN, runInfo.getRun(), RELEASE, runInfo.getRelease(), TYPE,
				runInfo.getType(), TIME, 1000 * runInfo.getTime());
		TableTools.addRecord(table, rec, true);
	}

	void load() {
		try {
			FieldDelimTableReader tr = new FieldDelimTableReader();
			tr.setInputFile(file);
			table = tr.readTable();
			int col = table.indexOfCol("Time");
			table = TableTools.convertCol(table, col, Type.DOUBLE_TYPE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void save() {
		TableWriter tw = FieldDelimTableWriter.getCommaSeparatedWriter();
		String str = tw.formatTable(table);
		FileTools.writeFile().setFile(file).setText(str).write();
	}

	public void writeReport() {
		Table t = table;

		Grid<Object> g = createGrid(t, GapList.create(RUN, TYPE), GapList.create(RELEASE, JAVA), TIME);
		writeHtml(g);
	}

	static final int colorSteps = 7;
	static IList<CssColor> colors = getGradient(colorSteps);
	static DoubleGapList percs = DoubleGapList.create(0, 75, 85, 95, 105, 115, 125);

	static void writeHtml(Grid<Object> grid) {
		String css = "table { \n" + "  border-collapse:collapse;\n" + "}\n" + "table,tr,td { \n" + "  border: 1px solid black; \n" + "  padding: 2px; \n"
				+ "} \n" + ".right { \n" + "  text-align: right; \n" + "}";
		System.out.println(grid);
		HtmlDocument html = new HtmlDocument();
		html.getHead().setTitle("Colors").addStyleCss(css);
		HtmlBlock body = html.getBody();

		// Write colors legend
		body.newH1("Colors");
		HtmlTable t = body.newTable();
		HtmlTr tr = t.newTr();

		for (int i = 0; i < colors.size(); i++) {
			CssColor color = colors.get(i);
			tr.newTd(color.toHexRgb()).setStyle("background-color: " + color.toHexRgb());
		}

		// Write result grid
		body.newH1("Grid");
		HtmlTableFormatter formatter = new HtmlTableFormatter();
		HtmlTable tab = formatter.format(grid);
		body.addElem(tab);

		// Align all cells right (except the two first columns)
		int rows = HtmlTools.getTableNumRows(tab.getElement());
		int cols = HtmlTools.getTableNumCols(tab.getElement(), 0);
		for (int r = 0; r < rows; r++) {
			for (int c = 2; c < cols; c++) {
				Element cell = HtmlTools.getTableCell(tab.getElement(), r, c);
				cell.setAttribute("class", "right");
			}
		}

		HtmlTools.createHtmlFile(html, REPORT_FILE);
	}

	public static int getRange(DoubleGapList list, double val) {
		int i = list.binarySearch(val);
		if (i < 0) {
			i = -(i + 1) - 1;
		}
		return i;
	}

	static IList<CssColor> getGradient(int steps) {
		CheckTools.check(steps % 2 == 1);

		CssColor c0 = CssColor.GREEN;
		CssColor c1 = CssColor.YELLOW;
		CssColor c2 = CssColor.RED;

		IList<CssColor> colors = CssColor.getGradient(c0, c1, steps / 2 + 1);
		colors.removeLast();
		colors.addAll(CssColor.getGradient(c1, c2, steps / 2 + 1));
		return colors;
	}

	/**
	 * Create grid from a table.
	 *
	 * @param table
	 * @param rowKeys
	 * @param colKeys
	 * @param cellKey
	 * @return
	 */
	static Grid<Object> createGrid(Table table, List<String> rowKeys, List<String> colKeys, String cellKey) {
		PivotTable<Object> pt = new PivotTableCreator().setTable(table).setRowKeys(rowKeys).setColKeys(colKeys).setCellKeys(GapList.create(cellKey)).create();
		Grid<Object> grid = (Grid<Object>) pt.getGrid();

		final int startX = rowKeys.size();
		final int startY = colKeys.size();

		// Cells
		for (int x = startX; x < grid.getNumCols(); x++) {
			for (int y = startY; y < grid.getNumRows(); y++) {
				Object val = grid.getElem(y, x);
				String str;
				if (val instanceof Double) {
					str = String.format("%.6f", val);
				} else {
					str = PrintTools.toString(val);
				}
				grid.set(y, x, str);
			}
		}

		for (int c = 2; c < grid.getNumCols(); c += 2) {
			addPercentageColumn(grid, 2, 2, c, c + 1);
		}
		addCmpGapArrayListColumns(grid);

		return grid;
	}

	static void addCmpGapArrayListColumns(Grid<Object> grid) {
		for (int c = 0; c < grid.getNumCols(); c++) {
			if (!StringUtils.isEmpty((String) grid.getElem(0, c))) {
				addCmpGapArrayListColumn(grid, c);
			}
		}
	}

	static void addCmpGapArrayListColumn(Grid<Object> grid, int col) {
		grid.addCol(col + 1);
		for (int r = 2; r < grid.getNumRows(); r += 3) {
			double timeGapList = TypeTools.parseDouble((String) grid.getElem(r, col));
			double timeArrayList = TypeTools.parseDouble((String) grid.getElem(r + 1, col));
			double v = MathTools.round(timeGapList / timeArrayList * 100, 2);
			HtmlDiv div = new HtmlDiv();
			div.addText("" + v);
			int i = getRange(percs, v);
			CssColor c = colors.get(i);
			div.setStyle("background-color: " + c.toHexRgb());
			grid.set(r, col + 1, div.getElement());
		}
	}

	static void addPercentageColumn(Grid<Object> grid, int rows, int src1, int src2, int col) {
		grid.addCol(col);
		for (int r = rows; r < grid.getNumRows(); r++) {
			double v1 = TypeTools.parseDouble((String) grid.getElem(r, src1));
			double v2 = TypeTools.parseDouble((String) grid.getElem(r, src2));
			double v = MathTools.round(v2 / v1 * 100, 2);
			HtmlDiv div = new HtmlDiv();
			div.addText("" + v);
			int i = getRange(percs, v);
			CssColor c = colors.get(i);
			div.setStyle("background-color: " + c.toHexRgb());
			grid.set(r, col, div.getElement());
		}
	}

	static Grid<Object> createGrid(Table table, String rowKey, String colKey, String cellKey) {
		final int colorColumn = 3;

		List<String> rowValues = CollectionTools.getDistinct(table.getCol(rowKey, String.class));
		List<String> colValues = CollectionTools.getDistinct(table.getCol(colKey, String.class));

		Grid<Object> grid = new Grid<Object>(rowValues.size() + 1, colValues.size() + 2);

		// Column header
		for (int r = 0; r < rowValues.size(); r++) {
			grid.add(r + 1, 0, rowValues.get(r));
		}
		// Row header
		for (int c = 0; c < colValues.size(); c++) {
			grid.add(0, c + 1, colValues.get(c));
		}
		// Cells
		for (int r = 0; r < rowValues.size(); r++) {
			for (int c = 0; c < colValues.size(); c++) {
				Table tab = PivotTableCreator.getTable(table, rowKey, rowValues.get(r), colKey, colValues.get(c));
				assert (tab.getNumRows() == 1);
				Record rec = tab.getRow(0);
				Object val = rec.getValue(cellKey);
				grid.set(r + 1, c + 1, val);
			}
		}

		// Add percentage column
		for (int r = 1; r < grid.getNumRows(); r++) {
			Double v1 = (Double) grid.getElem(r, 1);
			//Double v2 = (Double) grid.get(2, r);// TODO
			Double v2 = 0.5;
			double v = MathTools.round(v2 / v1 * 100, 2);
			grid.add(r, colorColumn, v);
		}

		return grid;
	}

}
