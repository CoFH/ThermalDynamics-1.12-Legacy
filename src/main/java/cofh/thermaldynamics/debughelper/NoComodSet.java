package cofh.thermaldynamics.debughelper;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NoComodSet<E> extends AbstractSet<E> {

    public final Set<E> set;

    int modCount = 0;

    public NoComodSet() {

        this(new HashSet<E>());
    }

    public NoComodSet(Set<E> set) {

        this.set = set;
    }

    public boolean add(E aMultiBlock) {

        updateModCount();
        return set.add(aMultiBlock);
    }

    public void updateModCount() {

        modCount++;
    }

    @Override
    public void clear() {

        updateModCount();
        set.clear();
    }

    @Override
    public boolean contains(Object o) {

        return set.contains(o);
    }

    @Override
    public boolean remove(Object o) {

        updateModCount();
        return set.remove(o);
    }


    @Override
    @SuppressWarnings("NullableProblems")
    public Iterator<E> iterator() {

        return new NoComodIterator(set.iterator());
    }

    @Override
    public int size() {

        return set.size();
    }

    public class NoComodIterator implements Iterator<E> {

        private final Iterator<E> iterator;
        int expectedModCount;

        public NoComodIterator(Iterator<E> iterator) {

            this.iterator = iterator;
            expectedModCount = modCount;
        }

        @Override
        public boolean hasNext() {
            return expectedModCount == modCount && iterator.hasNext();
        }

        @Override
        public E next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
