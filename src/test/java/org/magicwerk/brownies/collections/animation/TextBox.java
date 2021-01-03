package org.magicwerk.brownies.collections.animation;

import javafx.beans.property.StringProperty;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * A TextBox is used to display a slot in the list.
 */
class TextBox extends Group {
	private Text text;
	private Rectangle rectangle;
	private Rectangle clip;

	TextBox(String str, double width, double height) {
		this.text = new Text(str);
		text.setTextAlignment(TextAlignment.CENTER);
		text.setFill(AnimateCollections.textColor);
		text.setTextOrigin(VPos.CENTER);
		text.setFont(AnimateCollections.font);
		text.setFontSmoothingType(FontSmoothingType.LCD);

		this.rectangle = new Rectangle(width, height);
		if (str == null) {
			rectangle.setFill(AnimateCollections.emptySlotColor);
		} else {
			rectangle.setFill(AnimateCollections.usedSlotColor);
		}
		rectangle.setStrokeWidth(1);
		rectangle.setStroke(AnimateCollections.marginColor);
		rectangle.setArcHeight(15);
		rectangle.setArcWidth(15);

		this.clip = new Rectangle(width, height);
		text.setClip(clip);

		this.getChildren().addAll(rectangle, text);
	}

	void setFill(Paint fill) {
		rectangle.setFill(fill);
	}

	Paint getFill() {
		return rectangle.getFill();
	}

	Rectangle getRectangle() {
		return rectangle;
	}

	@Override
	protected void layoutChildren() {
		final double w = rectangle.getWidth();
		final double h = rectangle.getHeight();

		clip.setWidth(w);
		clip.setHeight(h);
		clip.setLayoutX(0);
		clip.setLayoutY(-h / 2);

		text.setWrappingWidth(w * 0.9);
		text.setLayoutX(w / 2 - text.getLayoutBounds().getWidth() / 2);
		text.setLayoutY(h / 2);
	}

	@Override
	public String toString() {
		return "TextBox [text=" + text.getText() + "]";
	}

}