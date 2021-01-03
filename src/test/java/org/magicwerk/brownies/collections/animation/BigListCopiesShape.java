package org.magicwerk.brownies.collections.animation;

import javafx.scene.Group;
import javafx.scene.Node;

import org.magicwerk.brownies.collections.BigList;

/**
 * A JavaFX group visualizing a BigList and its copy.
 */
class BigListCopiesShape extends Group {

	BigList<Integer> bigListOrig;
	BigList<Integer> bigListCopy;
	BigListShape bigListShapeOrig;
	BigListShape bigListShapeCopy;

	public BigListCopiesShape(BigList<Integer> list) {
		this.bigListOrig = list;

		update();
	}

	public BigList<Integer> getCopy() {
		return bigListCopy;
	}

	public void setCopy(BigList<Integer> copy) {
		this.bigListCopy = copy;
	}

	public void update() {
		getChildren().clear();

		bigListShapeOrig = new BigListShape(bigListOrig);
		if (bigListCopy != null) {
			bigListShapeCopy = new BigListShape(bigListCopy);
			bigListShapeCopy.setTranslateY(250);
		} else {
			bigListShapeCopy = null;
		}

		addChildren(bigListShapeOrig, bigListShapeCopy);
	}

	void addChildren(Node... elements) {
		for (Node element: elements) {
			if (element != null) {
				getChildren().add(element);
			}
		}
	}
}