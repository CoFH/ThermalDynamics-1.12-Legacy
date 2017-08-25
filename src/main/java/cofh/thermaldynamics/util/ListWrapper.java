package cofh.thermaldynamics.util;

import cofh.core.util.helpers.MathHelper;
import cofh.thermaldynamics.duct.item.DuctUnitItem;
import cofh.thermaldynamics.duct.item.GridItem;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.multiblock.RouteCache;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.stream.IntStream;

public class ListWrapper<T> implements Iterable<T> {

	public SortType type;
	LinkedList<T> list;
	Object[] array;
	int cursor;

	public void setList(LinkedList<T> list, SortType type) {

		this.list = list;
		array = null;
		this.type = type;
		cursor = 0;
	}

	@Override
	public Iterator<T> iterator() {

		if (list.size() <= 1) {
			return list.listIterator();
		}
		if (type == SortType.NORMAL) {
			return list.iterator();
		} else if (type == SortType.REVERSE) {
			return list.descendingIterator();
		} else if (type == SortType.ROUNDROBIN) {
			return new RRobinIter();
		} else if (type == SortType.SHUFFLE) {
			if (array == null || list.size() != array.length) {
				array = list.toArray();
			}
			return new ShuffleIter();
		}

		return list.iterator();
	}

	public void advanceCursor() {

		cursor++;
		if (cursor >= list.size()) {
			cursor = 0;
		}
	}

	public T peekRR() {

		if (cursor + 1 >= list.size()) {
			return list.get(0);
		} else {
			return list.get(cursor + 1);
		}
	}

	public int size() {

		return list.size();
	}

	public enum SortType {

		NORMAL, REVERSE, SHUFFLE, ROUNDROBIN
	}

	public class RRobinIter implements Iterator<T> {

		final int stopCursor = cursor - 1;
		public ListIterator<T> tListIterator;

		public RRobinIter() {

			tListIterator = list.listIterator(cursor);
		}

		@Override
		public boolean hasNext() {

			if (!tListIterator.hasNext()) {
				if (stopCursor < 0) {
					return false;
				} else {
					tListIterator = list.listIterator(0);
				}
			}
			return tListIterator.nextIndex() != stopCursor;
		}

		@Override
		public T next() {

			return tListIterator.next();
		}

		@Override
		public void remove() {

		}
	}

	private class ShuffleIter implements Iterator<T> {

		int i;

		public ShuffleIter() {

			super();
		}

		@Override
		public boolean hasNext() {

			return i < array.length;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T next() {

			Object t = array[i];
			int j = MathHelper.RANDOM.nextInt(array.length - i) + i;
			array[i] = array[j];
			array[j] = t;

			i++;
			return (T) t;
		}

		@Override
		public void remove() {

			throw new UnsupportedOperationException();
		}
	}
}
