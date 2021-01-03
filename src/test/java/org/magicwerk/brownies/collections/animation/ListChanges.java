package org.magicwerk.brownies.collections.animation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.magicwerk.brownies.core.reflect.ReflectTools;
import org.magicwerk.brownies.core.strings.StringFormatter;
import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.core.ArrayTools;
import org.magicwerk.brownies.core.CheckTools;
import org.magicwerk.brownies.core.CollectionTools;
import org.magicwerk.brownies.core.ObjectTools;

public class ListChanges {

	static class Change {
	}

	static class AddChange<T> extends Change {
		int index;
		int logIndex = -1;
		T elem;

		AddChange(int index, T elem) {
			this.index = index;
			this.elem = elem;
		}

		public int getIndex() {
			return index;
		}

		public T getElem() {
			return elem;
		}

		public Change setLogIndex(int logIndex) {
			this.logIndex = logIndex;
			return this;
		}

		@Override
		public String toString() {
			int i = (logIndex != -1) ? logIndex : index;
			return StringFormatter.format("add({0}, {1})", i, elem);
		}

	}

	static class RemoveChange extends Change {
		int index;
		int logIndex = -1;

		RemoveChange(int index) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

		public Change setLogIndex(int logIndex) {
			this.logIndex = logIndex;
			return this;
		}

		@Override
		public String toString() {
			int i = (logIndex != -1) ? logIndex : index;
			return StringFormatter.format("remove({0})", i);
		}

	}

	static class MoveChange extends Change {
		int fromIndex;
		int toIndex;

		MoveChange(int fromIndex, int toIndex) {
			this.fromIndex = fromIndex;
			this.toIndex = toIndex;
		}

		public int getFromIndex() {
			return fromIndex;
		}

		public int getToIndex() {
			return toIndex;
		}
	}

	static class CapacityChange extends Change {
		int addCapacity;

		CapacityChange(int addCapacity) {
			this.addCapacity = addCapacity;
		}

		public int getAddCapacity() {
			return addCapacity;
		}
	}

	enum Operation {
		ADD,
		REMOVE
	}

	// Fields


	Operation operation;
	List<Change> changes = GapList.create();

	public Operation getOperation() {
		return operation;
	}

	public String getOperationDesc() {
		if (operation == Operation.ADD) {
			return getAddChange().toString();
		} else if (operation == Operation.REMOVE) {
			return getRemoveChange().toString();
		} else {
			throw new AssertionError();
		}
	}

	public AddChange getAddChange() {
		AddChange<Integer> ac = StreamTools.getValue(getChanges().stream().filter(c -> c instanceof AddChange), AddChange.class);
		return ac;
	}

	public RemoveChange getRemoveChange() {
		RemoveChange rc = StreamTools.getValue(getChanges().stream().filter(c -> c instanceof RemoveChange), RemoveChange.class);
		return rc;
	}

	public List<Change> getChanges() {
		return changes;
	}

	private ListChanges() {
	}

	public static <T> ListChanges build(List<T> oldList, List<T> newList) {
		ListChanges lc = new ListChanges();
		if (oldList instanceof ArrayList && newList instanceof ArrayList) {
			lc.doBuild((ArrayList<T>) oldList, (ArrayList<T>) newList);
		} else if (oldList instanceof GapList && newList instanceof GapList) {
			lc.doBuild((GapList<T>) oldList, (GapList<T>) newList);
		} else {
			throw new AssertionError();
		}
		return lc;
	}

	<T> void doBuild(ArrayList<T> oldList, ArrayList<T> newList) {
		// Capacity
		int oldCapacity = ArrayListModel.getArrayListCapacity(oldList);
		int newCapacity = ArrayListModel.getArrayListCapacity(newList);
		if (newCapacity != oldCapacity) {
			changes.add(new CapacityChange(newCapacity-oldCapacity));
		}

		// Remove
		List<T> removed = CollectionTools.minus(oldList, newList);
		for (T elem: removed) {
			int index = oldList.indexOf(elem);
			changes.add(new RemoveChange(index));
		}

		// Move
		// TODO
		List<T> intersect = GapList.create(CollectionTools.intersect(new LinkedHashSet(oldList), new LinkedHashSet(newList)));
		for (T elem: intersect) {
			int oldIndex = oldList.indexOf(elem);
			int newIndex = newList.indexOf(elem);
			if (oldIndex != newIndex) {
				changes.add(new MoveChange(oldIndex, newIndex));
			}
		}

		// Add
		List<T> added = CollectionTools.minus(newList, oldList);
		for (T elem: added) {
			int index = newList.indexOf(elem);
			changes.add(new AddChange(index, elem));
		}

		CheckTools.checkOneTrue(!removed.isEmpty(), !added.isEmpty());
		operation = (removed.isEmpty()) ? Operation.ADD : Operation.REMOVE;
	}

	<T> void doBuild(GapList<T> oldList, GapList<T> newList) {
		T[] oldValues = GapListModel.getGapListValues(oldList);
		T[] newValues = GapListModel.getGapListValues(newList);

		// Capacity
		int oldCapacity = GapListModel.getGapListCapacity(oldList);
		int newCapacity = GapListModel.getGapListCapacity(newList);
		if (newCapacity != oldCapacity) {
			changes.add(new CapacityChange(newCapacity-oldCapacity));
		}

		// Remove
		List<T> removed = CollectionTools.minus(oldList, newList);
		for (T elem: removed) {
			int index = indexOf(oldValues, elem);
			int logIndex = oldList.indexOf(elem);
			changes.add(new RemoveChange(index).setLogIndex(logIndex));
		}

		// Move
		// TODO
		List<T> intersect = GapList.create(CollectionTools.intersect(new LinkedHashSet(oldList), new LinkedHashSet(newList)));
		for (T elem: intersect) {
			int oldIndex = indexOf(oldValues, elem);
			int newIndex = indexOf(newValues, elem);
			if (oldIndex != newIndex) {
				changes.add(new MoveChange(oldIndex, newIndex));
			}
		}

		// Add
		List<T> added = CollectionTools.minus(newList, oldList);
		for (T elem: added) {
			int index = indexOf(newValues, elem);
			int logIndex = newList.indexOf(elem);
			changes.add(new AddChange(index, elem).setLogIndex(logIndex));
		}

		CheckTools.checkOneTrue(!removed.isEmpty(), !added.isEmpty());
		operation = (removed.isEmpty()) ? Operation.ADD : Operation.REMOVE;
	}
	
	static <T> int indexOf(T[] values, T value) {
		for (int i=0; i<values.length; i++) {
			if (ObjectTools.equals(values[i], value)) {
				return i;
			}
		}
		return -1;
	}

	public static int getListCapacity(List<Integer> list) {
		if (list instanceof ArrayList) {
			return ArrayListModel.getArrayListCapacity((ArrayList<?>) list);
		} else if (list instanceof GapList) {
			return GapListModel.getGapListCapacity((GapList<?>) list);
		} else {
			throw new AssertionError();
		}
	}

}