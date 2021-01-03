package org.magicwerk.brownies.collections.animation;

import java.util.List;

import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import org.magicwerk.brownies.collections.BigList;
import org.magicwerk.brownies.collections.GapList;

/**
 * BigListBlockShape visualizes a block of a BigList.
 */
public class BigListBlockShape extends Group {

	Rectangle rectangle;
	Text idLabel;
	Text idValue;
	Text refCountLabel;
	Text refCountValue;
	Text numElemsLabel;
	Text numElemsValue;
	ListShape blockList;

	public BigListBlockShape(BigList<?> list, int block) {
		GapList<?> bl = BigListModel.getBlock(list, block);
		int blockNumElems = bl.size();
		int blockRefCount = BigListModel.getBlockRefCount(list, block);

		int x1 = 10;
		int x2 = 100;
		int x3 = 160;
		int x4 = 310;
		int y0 = 20;
		int y1 = 50;
		int y2 = 20;

		this.rectangle = new Rectangle(520, 140);
		if (blockRefCount > 1) {
			rectangle.setFill(AnimateCollections.blockSharedColor);
		} else {
			rectangle.setFill(AnimateCollections.blockPrivateColor);
		}
		rectangle.setStrokeWidth(1);
		rectangle.setStroke(AnimateCollections.marginColor);
		rectangle.setArcHeight(15);
		rectangle.setArcWidth(15);

		this.idLabel = createText(x1, y0, "Block:");
		this.idValue = createText(x2, y0, "#"+AnimateCollections.getObjId(bl));

		this.refCountLabel = new Text("RefCount:");
		refCountLabel.setTranslateX(x3);
		refCountLabel.setTranslateY(y0);
		refCountLabel.setTextAlignment(TextAlignment.LEFT);
		refCountLabel.setFill(AnimateCollections.textColor);
		refCountLabel.setTextOrigin(VPos.CENTER);
		refCountLabel.setFont(AnimateCollections.font);
		refCountLabel.setFontSmoothingType(FontSmoothingType.LCD);

		this.refCountValue = new Text(""+blockRefCount);
		refCountValue.setTranslateX(x4);
		refCountValue.setTranslateY(y0);
		refCountValue.setTextAlignment(TextAlignment.LEFT);
		refCountValue.setFill(AnimateCollections.textColor);
		refCountValue.setTextOrigin(VPos.CENTER);
		refCountValue.setFont(AnimateCollections.font);
		refCountValue.setFontSmoothingType(FontSmoothingType.LCD);

		this.numElemsLabel = new Text("NumElems:");
		numElemsLabel.setTranslateX(x3);
		numElemsLabel.setTranslateY(y1);
		numElemsLabel.setTextAlignment(TextAlignment.LEFT);
		numElemsLabel.setFill(AnimateCollections.textColor);
		numElemsLabel.setTextOrigin(VPos.CENTER);
		numElemsLabel.setFont(AnimateCollections.font);
		numElemsLabel.setFontSmoothingType(FontSmoothingType.LCD);

		this.numElemsValue = new Text(""+blockNumElems);
		numElemsValue.setTranslateX(x4);
		numElemsValue.setTranslateY(y1);
		numElemsValue.setTextAlignment(TextAlignment.LEFT);
		numElemsValue.setFill(AnimateCollections.textColor);
		numElemsValue.setTextOrigin(VPos.CENTER);
		numElemsValue.setFont(AnimateCollections.font);
		numElemsValue.setFontSmoothingType(FontSmoothingType.LCD);

		this.blockList = new ListShape();
		blockList.setTranslateX(0);
		blockList.setLayoutY(y2);
		blockList.setShowText(false);
		blockList.init((List<Integer>) bl);

		this.getChildren().addAll(rectangle, idLabel, idValue, refCountLabel, refCountValue, numElemsLabel, numElemsValue, blockList);

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

}