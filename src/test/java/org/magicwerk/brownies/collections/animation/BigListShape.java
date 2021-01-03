package org.magicwerk.brownies.collections.animation;

import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import org.magicwerk.brownies.collections.BigList;
import org.magicwerk.brownies.core.CheckTools;

/**
 * A JavaFX group visualizing a BigList.
 */
class BigListShape extends Group {

	BigList<?> list;
	Rectangle rectangle;
	Text idLabel;
	Text idValue;
	Text numBlocksLabel;
	Text numBlockValue;
	Text numElemsLabel;
	Text numElemsValue;
	BigListBlockShape block1;
	BigListBlockShape block2;

	public BigListShape(BigList<?> list) {
		this.list = list;

		update();
	}

	public void update() {
		int numBlocks = BigListModel.getNumBlocks(list);
		int numElems = BigListModel.getNumBlocks(list);
		CheckTools.check(numBlocks <= 2, "Max 2 blocks can be displayed");

		getChildren().clear();

		int x1 = 10;
		int x2 = 100;
		int x3 = 160;
		int x4 = 310;
		int y0 = 20;
		int y1 = 50;
		int y2 = 20;

		this.rectangle = new Rectangle(1090, 230);
		rectangle.setFill(AnimateCollections.usedSlotColor);
		rectangle.setStrokeWidth(1);
		rectangle.setStroke(AnimateCollections.marginColor);
		rectangle.setArcHeight(15);
		rectangle.setArcWidth(15);

		this.idLabel = createText(x1, y0, "BigList:");
		this.idValue = createText(x2, y0, "#"+AnimateCollections.getObjId(list));

		this.numBlocksLabel = createText(x3, y0, "NumBlocks:");
		this.numBlockValue = createText(x4, y0, ""+numBlocks);

		this.numElemsLabel = createText(x3, y1, "NumElems:");
		this.numElemsValue = createText(x4, y1, ""+numElems);

		this.block1 = new BigListBlockShape(list, 0);
		block1.setLayoutX(10);
		block1.setLayoutY(80);

		block2 = null;
		if (numBlocks == 2) {
			block2 = new BigListBlockShape(list, 1);
			block2.setLayoutX(550);
			block2.setLayoutY(80);
		}
		addChildren(rectangle, idLabel, idValue, numBlocksLabel, numBlockValue, numElemsLabel, numElemsValue, block1, block2);
	}

	Text createText(double x, double y, String str) {
		Text text = new Text(x, y, str);
		text.setTextAlignment(TextAlignment.LEFT);
		text.setFill(AnimateCollections.textColor);
		text.setTextOrigin(VPos.CENTER);
		text.setFont(AnimateCollections.font);
		text.setFontSmoothingType(FontSmoothingType.LCD);
		return text;
	}

	void addChildren(Node... elements) {
		for (Node element: elements) {
			if (element != null) {
				getChildren().add(element);
			}
		}
	}
}