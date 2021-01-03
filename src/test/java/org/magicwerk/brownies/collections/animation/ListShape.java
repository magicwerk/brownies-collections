package org.magicwerk.brownies.collections.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.util.Duration;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.animation.ListChanges.AddChange;
import org.magicwerk.brownies.collections.animation.ListChanges.CapacityChange;
import org.magicwerk.brownies.collections.animation.ListChanges.MoveChange;
import org.magicwerk.brownies.collections.animation.ListChanges.Operation;
import org.magicwerk.brownies.collections.animation.ListChanges.RemoveChange;

public class ListShape extends Group {
	/**
	 * There are as many slots as the capacity of the list indicates.
	 */
	GapList<TextBox> slots;
	GapList<TextBox> boxes;
	List<Integer> list;
	Text text;
	String className;

	boolean showText = true;

	void setShowText(boolean showText) {
		this.showText = showText;
	}

	void init(List<Integer> list) {
		this.list = list;

		// Add margin around scene
		//Group root = (Group) scene.getRoot();
		setTranslateX(10);
		setTranslateY(10);

		// Text showing class and operation
		if (showText) {
			className = list.getClass().getSimpleName();
			text = new Text(className);
			text.setFont(AnimateCollections.font);
			text.setFontSmoothingType(FontSmoothingType.LCD);
			text.setLayoutX(0);
			text.setLayoutY(0);
			text.setTextOrigin(VPos.TOP);
			getChildren().add(text);
		}

		// Boxes visulizing the list
		slots = GapList.create();
		boxes = GapList.create();
		int capacity = ListChanges.getListCapacity(list);
		addSlots(capacity);

		if (list instanceof ArrayList) {
			for (int i = 0; i < list.size(); i++) {
				Integer elem = list.get(i);
				TextBox tb = getTextBox(elem);
				tb.setLayoutY(getY());
				tb.setLayoutX(getX(i));
				boxes.set(i, tb);
				getChildren().add(tb);
			}

		} else if (list instanceof GapList) {
			GapList gapList = (GapList) list;
			Object[] values = GapListModel.getGapListValues(gapList);
			for (int i = 0; i < values.length; i++) {
				if (values[i] == null) {
					continue;
				}
				TextBox tb = getTextBox((Integer) values[i]);
				tb.setLayoutY(getY());
				tb.setLayoutX(getX(i));
				boxes.set(i, tb);
				getChildren().add(tb);
			}

			int slotStart = GapListModel.getGapListSlotStart(gapList);
			TextBox slot = boxes.get(slotStart);
			getChildren().remove(slot);
			getChildren().add(slot);
			slot.getRectangle().setStrokeWidth(5);

			int gapSize = GapListModel.getGapListGapSize(gapList);
			if (gapSize > 0) {
				// There is a gap
				int gapStart = GapListModel.getGapListGapStart(gapList);
				if (gapStart + gapSize <= capacity) {
					// Gap consists of one part
					for (int i = gapStart; i < gapStart + gapSize; i++) {
						slot = slots.get(i);
						slot.setFill(AnimateCollections.gapSlotColor);
					}
				} else {
					// Gap consists of two parts
					for (int i = gapStart; i < capacity; i++) {
						slot = slots.get(i);
						slot.setFill(AnimateCollections.gapSlotColor);
					}
					for (int i = 0; i < (gapStart + gapSize) % capacity; i++) {
						slot = slots.get(i);
						slot.setFill(AnimateCollections.gapSlotColor);
					}
				}
			}
		} else {
			throw new AssertionError();
		}
	}

	List<TextBox> addSlots(int num) {
		List<TextBox> list = GapList.create();
		int size = slots.size();
		for (int i = 0; i < num; i++) {
			TextBox tb = new TextBox(null, AnimateCollections.boxSize, AnimateCollections.boxSize);
			tb.setLayoutY(getY());
			tb.setLayoutX(getX(size + i));
			list.add(tb);
			slots.add(tb);
			getChildren().add(0, tb);
			boxes.add(null);
		}
		return list;
	}

	TextBox getTextBox(int value) {
		String title = "" + value;
		TextBox tb = new TextBox(title, AnimateCollections.boxSize, AnimateCollections.boxSize);
		return tb;
	}

	double getX(int index) {
		return index * AnimateCollections.boxSize;
	}

	double getY() {
		return 50;
	}

	/**
	 * Create transition animation from the current to the specified list.
	 *
	 * @param newList
	 * @return
	 */
	Animation transition(List newList) {
		ListChanges lc = ListChanges.build(list, newList);

		SequentialTransition st = new SequentialTransition();
		st.getChildren().add(getAnimation(lc));

		addGapListAnimation(st, newList, lc);

		this.list = newList;
		return st;
	}

	void addGapListAnimation(SequentialTransition st, List newList, ListChanges lc) {
		Animation textShowOp = getTextTransition(text, className + "." + lc.getOperationDesc());
		Animation textHideOp = getTextTransition(text, className);

		Animation gapOut = null;
		Animation startOut = null;
		Animation gapIn = null;
		Animation startIn = null;

		if (newList instanceof GapList) {
			GapList oldGapList = (GapList) list;
			int oldStart = GapListModel.getGapListSlotStart(oldGapList);
			TextBox startOutBox = boxes.get(oldStart);

			GapList gapList = (GapList) newList;
			int newStart = GapListModel.getGapListSlotStart(gapList);
			TextBox startInBox = boxes.get(newStart);

			getChildren().remove(startInBox);
			getChildren().add(startInBox);

			gapOut = getGapTransition(gapList, false);
			startOut = getStartTransition(startOutBox, false);
			gapIn = getGapTransition(gapList, true);
			startIn = getStartTransition(startInBox, true);
		}
		st.getChildren().add(0, getParallelTransition(gapOut, startOut, textShowOp));
		st.getChildren().add(getParallelTransition(gapIn, startIn, textHideOp));
	}

	ParallelTransition getParallelTransition(Animation... children) {
		ParallelTransition pt = new ParallelTransition();
		for (Animation child: children) {
			if (child != null) {
				pt.getChildren().add(child);
			}
		}
		return pt;
	}

	/**
	 * Create animation for change on list.
	 *
	 * @param changes
	 * @return
	 */
	Animation getAnimation(ListChanges changes) {
		if (changes.getOperation() == Operation.ADD) {
			return getAnimationAdd(changes);
		} else if (changes.getOperation() == Operation.REMOVE) {
			return getAnimationRemove(changes);
		} else {
			throw new AssertionError();
		}
	}

	Animation getTransitionCapacity(int add) {
		List<TextBox> newBoxes = addSlots(add);
		for (TextBox newBox : newBoxes) {
			newBox.setOpacity(0);
		}

		ParallelTransition groupAppearTransition = getParallelTransition(newBoxes,
				box -> getFadeTransition(box, true));

		SequentialTransition st = new SequentialTransition();
		st.getChildren().addAll(
				new PauseTransition(Duration.millis(1000)),
				groupAppearTransition);
		return st;
	}

	Animation getGapTransition(GapList list, boolean becomeGap) {
		List<TextBox> gaps = GapList.create();
		if (!becomeGap) {
			for (int i = 0; i < slots.size(); i++) {
				TextBox slot = slots.get(i);
				if (slot.getFill().equals(AnimateCollections.gapSlotColor)) {
					gaps.add(slot);
				}
			}

		} else {
			int gapSize = GapListModel.getGapListGapSize(list);
			if (gapSize > 0) {
				// There is a gap
				int capacity = GapListModel.getGapListCapacity(list);
				int gapStart = GapListModel.getGapListGapStart(list);
				if (gapStart + gapSize <= capacity) {
					// Gap consists of one part
					for (int i = gapStart; i < gapStart + gapSize; i++) {
						TextBox slot = slots.get(i);
						gaps.add(slot);
					}
				} else {
					// Gap consists of two parts
					for (int i = gapStart; i < capacity; i++) {
						TextBox slot = slots.get(i);
						gaps.add(slot);
					}
					for (int i = 0; i < (gapStart + gapSize) % capacity; i++) {
						TextBox slot = slots.get(i);
						gaps.add(slot);
					}
				}
			}
		}
		ParallelTransition transition = getParallelTransition(gaps,
				box -> getGapTransition(box, becomeGap));
		return transition;
	}

	// Add: fade in new elements, capacity, move existing, move new elements to place
	Animation getAnimationAdd(ListChanges lc) {
		AddChange<Integer> add = lc.getAddChange();
		List<MoveChange> moves = StreamTools.getList(lc.getChanges().stream().filter(c -> c instanceof MoveChange), MoveChange.class);
		CapacityChange capacity = StreamTools.getValueIf(lc.getChanges().stream().filter(c -> c instanceof CapacityChange), CapacityChange.class);

		int addIndex = add.getIndex();
		int addElem = add.getElem();

		if (capacity != null) {
			boxes.addMult(capacity.getAddCapacity(), null);
		}
		GapList<TextBox> newBoxes = boxes.copy();

		List<TextBox> boxesToMove = GapList.create();
		for (int i = 0; i < moves.size(); i++) {
			MoveChange move = moves.get(i);
			int fromIndex = move.getFromIndex();
			int toIndex = move.getToIndex();
			boxesToMove.add(boxes.get(fromIndex));
			newBoxes.set(toIndex, boxes.get(fromIndex));
		}

		TextBox addBox = getTextBox(addElem);
		addBox.setLayoutX(getX(addIndex));
		addBox.setLayoutY(getY()+AnimateCollections.moveY);
		newBoxes.set(addIndex, addBox);
		addBox.setOpacity(0);

		boxes.setAll(0, newBoxes);

		// Move affected elements to end of list so they are drawn on top
		getChildren().add(addBox);
		for (int i = 0; i < boxesToMove.size(); i++) {
			TextBox b = boxesToMove.get(i);
			getChildren().remove(b);
			getChildren().add(b);
		}

		SequentialTransition st = new SequentialTransition();

		if (capacity != null) {
			st.getChildren().add(getTransitionCapacity(capacity.getAddCapacity()));
		}

		Transition newFadeInTransition = getFadeTransition(addBox, true);
		st.getChildren().add(newFadeInTransition);

		if (!boxesToMove.isEmpty()) {
			ParallelTransition groupActiveTransition = getParallelTransition(boxesToMove,
					box -> getActiveTransition(box, true));

			ParallelTransition groupMoveRightTransition = new ParallelTransition();
			for (int i = 0; i < boxesToMove.size(); i++) {
				TextBox box = boxesToMove.get(i);
				MoveChange move = moves.get(i);
				Transition t = getMoveXTransition(box, move.getToIndex() - move.getFromIndex());
				groupMoveRightTransition.getChildren().add(t);
			}

			ParallelTransition groupInactiveTransition = getParallelTransition(boxesToMove,
					box -> getActiveTransition(box, false));

			st.getChildren().addAll(
					groupActiveTransition,
					groupMoveRightTransition,
					groupInactiveTransition);
		}

		Transition newActiveTransition = getActiveTransition(addBox, true);
		Transition newMoveInListTransition = getMoveYTransition(addBox, true);
		Transition newInactiveTransition = getActiveTransition(addBox, false);

		st.getChildren().addAll(
				newActiveTransition,
				newMoveInListTransition,
				newInactiveTransition);
		return st;
	}

	// Remove: show element to remove as active, move elements out, fade them out, move existing elements
	Animation getAnimationRemove(ListChanges lc) {
		RemoveChange remove = lc.getRemoveChange();
		List<MoveChange> moves = StreamTools.getList(lc.getChanges().stream().filter(c -> c instanceof MoveChange), MoveChange.class);
		int removeIndex = remove.getIndex();

		GapList<TextBox> newBoxes = boxes.copy();

		TextBox removeBox = boxes.get(removeIndex);

		List<TextBox> boxesToMove = GapList.create();
		for (int i = 0; i < moves.size(); i++) {
			MoveChange move = moves.get(i);
			int fromIndex = move.getFromIndex();
			int toIndex = move.getToIndex();
			boxesToMove.add(boxes.get(fromIndex));
			newBoxes.set(toIndex, boxes.get(fromIndex));
		}

		boxes.setAll(0, newBoxes);

		// Move affected elements to end of list so they are drawn on top
		for (int i = 0; i < boxesToMove.size(); i++) {
			TextBox b = boxesToMove.get(i);
			getChildren().remove(b);
			getChildren().add(b);
		}

		SequentialTransition st = new SequentialTransition();

		Transition removeActiveTransition = getActiveTransition(removeBox, true);
		Transition removeMoveOutTransition = getMoveYTransition(removeBox, false);
		Transition removeDisappearTransition = getFadeTransition(removeBox, false);
		st.getChildren().addAll(
				removeActiveTransition,
				removeMoveOutTransition,
				removeDisappearTransition);

		if (!boxesToMove.isEmpty()) {
			ParallelTransition groupActiveTransition = getParallelTransition(boxesToMove,
					box -> getActiveTransition(box, true));

			ParallelTransition groupMoveRightTransition = new ParallelTransition();
			for (int i = 0; i < boxesToMove.size(); i++) {
				TextBox box = boxesToMove.get(i);
				MoveChange move = moves.get(i);
				Transition t = getMoveXTransition(box, move.getToIndex() - move.getFromIndex());
				groupMoveRightTransition.getChildren().add(t);
			}

			ParallelTransition groupInactiveTransition = getParallelTransition(boxesToMove,
					box -> getActiveTransition(box, false));

			st.getChildren().addAll(
					groupActiveTransition,
					groupMoveRightTransition,
					groupInactiveTransition);
		}

		return st;
	}

	/**
	 * Create a parallel transition for all listed text boxes.
	 *
	 * @param boxes
	 * @param consumer
	 * @return
	 */
	ParallelTransition getParallelTransition(List<TextBox> boxes, Function<TextBox, Transition> consumer) {
		ParallelTransition pt = new ParallelTransition();
		for (TextBox box : boxes) {
			Transition t = consumer.apply(box);
			pt.getChildren().add(t);
		}
		return pt;
	}

	Transition getMoveYTransition(TextBox box, boolean moveIn) {
		TranslateTransition moveYTransition = new TranslateTransition();
		moveYTransition.setDuration(Duration.millis(AnimateCollections.duration));
		double y = box.getTranslateY();
		if (moveIn) {
			moveYTransition.setFromY(y+0);
			moveYTransition.setToY(y-AnimateCollections.moveY);
		} else {
			moveYTransition.setFromY(y+0);
			moveYTransition.setToY(y+AnimateCollections.moveY);
		}
		moveYTransition.setNode(box);
		return moveYTransition;
	}

	Transition getMoveXTransition(TextBox box, int offset) {
		TranslateTransition tr = new TranslateTransition();
		tr.setDuration(Duration.millis(AnimateCollections.duration));
		double x = box.getTranslateX();
		tr.setFromX(x + 0);
		tr.setToX(x + offset * AnimateCollections.boxSize);
		tr.setNode(box);
		return tr;
	}

	Transition getFadeTransition(Node box, boolean fadeIn) {
		FadeTransition fadeTransition = new FadeTransition(Duration.millis(AnimateCollections.duration), box);
		if (fadeIn) {
			fadeTransition.setFromValue(0.0);
			fadeTransition.setToValue(1.0);
		} else {
			fadeTransition.setFromValue(1.0);
			fadeTransition.setToValue(0.0);
		}
		return fadeTransition;
	}

	Transition getActiveTransition(TextBox box, boolean becomeActive) {
		if (becomeActive) {
			return new FillTransition(Duration.millis(AnimateCollections.duration), box.getRectangle(), AnimateCollections.usedSlotColor, AnimateCollections.activeSlotColor);
		} else {
			return new FillTransition(Duration.millis(AnimateCollections.duration), box.getRectangle(), AnimateCollections.activeSlotColor, AnimateCollections.usedSlotColor);
		}
	}

	Transition getGapTransition(TextBox box, boolean becomeGap) {
		if (becomeGap) {
			return new FillTransition(Duration.millis(AnimateCollections.duration), box.getRectangle(), AnimateCollections.emptySlotColor, AnimateCollections.gapSlotColor);
		} else {
			return new FillTransition(Duration.millis(AnimateCollections.duration), box.getRectangle(), AnimateCollections.gapSlotColor, AnimateCollections.emptySlotColor);
		}
	}

	Animation getStartTransition(TextBox box, boolean becomeGap) {
		double width = (becomeGap) ? 5 : 1;
		final Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(AnimateCollections.duration),
				new KeyValue(box.getRectangle().strokeWidthProperty(), width)));
		return timeline;
	}

	Animation getTextTransition(Text text, String str) {
		if (text == null) {
			return null;
		}
		final Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(AnimateCollections.duration),
				new KeyValue(text.textProperty(), str)));
		return timeline;
	}

}