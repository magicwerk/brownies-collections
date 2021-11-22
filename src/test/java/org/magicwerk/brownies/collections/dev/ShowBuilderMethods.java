package org.magicwerk.brownies.collections.dev;

import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.brownies.collections.Key1Collection;
import org.magicwerk.brownies.collections.Key1List;
import org.magicwerk.brownies.collections.Key2Collection;
import org.magicwerk.brownies.collections.Key2List;
import org.magicwerk.brownies.collections.KeyCollection;
import org.magicwerk.brownies.collections.KeyList;
import org.magicwerk.brownies.core.CollectionTools;
import org.magicwerk.brownies.core.StringTools;
import org.magicwerk.brownies.core.collections.Grid;
import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.html.HtmlBlock;
import org.magicwerk.brownies.html.HtmlDocument;
import org.magicwerk.brownies.html.HtmlTable;
import org.magicwerk.brownies.html.ReportTools;
import org.magicwerk.brownies.html.content.HtmlTableFormatter;

/**
 * Show methods of all builder classes for key collections.
 *
 * @author Thomas Mauch
 * @version $Id$
 */
public class ShowBuilderMethods {

	static IList<Class<?>> classes = GapList.create(
			KeyCollection.Builder.class,
			Key1Collection.Builder.class,
			Key2Collection.Builder.class,
			KeyList.Builder.class,
			Key1List.Builder.class,
			Key2List.Builder.class);

	public static void main(String[] args) {
		new ShowBuilderMethods().run();
	}

	void run() {
		Grid<String> grid = getMethodsGrid();
		showMethods(grid);
	}

	Grid<String> getMethodsGrid() {
		List<String> allMethods = GapList.create();
		List<List<String>> allClassMethods = GapList.create();
		for (Class<?> clazz : classes) {
			List<String> methods = ReflectTools.getAllMethods(clazz).stream().filter(m -> Modifier.isPublic(m.getModifiers())).map(Executable::getName)
					.filter(s -> s.startsWith("with")).collect(Collectors.toList());
			allClassMethods.add(methods);
			allMethods = CollectionTools.union(allMethods, methods);
		}
		GapList<String> sortedMethods = GapList.create(allMethods);
		sortedMethods.sort(null);

		Grid<String> grid = new Grid<>();
		for (int r = 0; r < sortedMethods.size(); r++) {
			grid.add(0, r + 1, sortedMethods.get(r));
		}
		for (int c = 0; c < classes.size(); c++) {
			String className = classes.get(c).getName();
			className = StringTools.removeTail(className, "$Builder");
			className = StringTools.removeHead(className, "org.magicwerk.brownies.collections.");
			grid.add(c + 1, 0, className);

			List<String> classMethdods = allClassMethods.get(c);
			for (int r = 0; r < sortedMethods.size(); r++) {
				String method = sortedMethods.get(r);
				String str = "";
				if (classMethdods.contains(method)) {
					str = "X";
				}
				grid.add(c + 1, r + 1, str);
			}
		}

		return grid;
	}

	void showMethods(Grid<String> grid) {
		String htmlFile = "output/builders.html";

		HtmlDocument htmlDoc = createHtmlDoc(grid);
		ReportTools.showHtmlFile(htmlDoc, htmlFile);
	}

	HtmlDocument createHtmlDoc(Grid<String> grid) {
		//String title = "Title";
		HtmlDocument htmlDoc = new HtmlDocument();
		//htmlDoc.newHead().setTitleText(title).setStyleCss(css);
		HtmlBlock htmlBody = htmlDoc.getBody();

		HtmlTableFormatter formatter = new HtmlTableFormatter();
		HtmlTable tab = formatter.format(grid);
		htmlBody.addElem(tab);
		return htmlDoc;
	}

}
