package cofh.thermaldynamics.multiblock.listtypes;

import cofh.lib.util.helpers.MathHelper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class ListWrapper<T> implements Iterable<T> {

    LinkedList<T> list;
    Object[] array;
    int cursor;
    SortType type;


    public void setList(LinkedList<T> list, SortType type) {
        this.list = list;
        array = null;
        this.type = type;
        cursor = 0;
    }

    @Override
    public Iterator<T> iterator() {
        if (type == SortType.NORMAL)
            return list.iterator();
        else if (type == SortType.REVERSE)
            return list.descendingIterator();
        else if (type == SortType.ROUNDROBIN) {
            cursor++;
            if (cursor >= list.size()) cursor = 0;
            if (cursor == 0)
                return list.iterator();

            return new RRobinIter();
        } else if (type == SortType.SHUFFLE) {
            if (list.size() == 1) {
                return list.listIterator();
            }
            if (array == null || list.size() != array.length) {
                array = list.toArray();
            }

            return new ShuffleIter();
        }

        return list.iterator();
    }

    public class RRobinIter implements Iterator<T> {

        public ListIterator<T> tListIterator;

        public RRobinIter() {
            tListIterator = list.listIterator(cursor + 1);
        }

        @Override
        public boolean hasNext() {
            return tListIterator.nextIndex() != cursor;
        }

        @Override
        public T next() {
            if (!tListIterator.hasNext()) tListIterator = list.listIterator(0);
            return tListIterator.next();
        }

        @Override
        public void remove() {

        }
    }


    public static enum SortType {
        NORMAL,
        REVERSE,
        SHUFFLE,
        ROUNDROBIN
    }

    private class ShuffleIterBuilding implements Iterator<T> {
        ListIterator<T> listIterator;

        public ShuffleIterBuilding() {
            array = new Object[list.size()];
            listIterator = list.listIterator();
        }

        @Override
        public boolean hasNext() {
            return listIterator.hasNext();
        }

        @Override
        public T next() {
            int i = listIterator.nextIndex();

            T next = listIterator.next();
            int j = MathHelper.RANDOM.nextInt(i + 1);
            array[i] = array[j];
            array[j] = next;

            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
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
