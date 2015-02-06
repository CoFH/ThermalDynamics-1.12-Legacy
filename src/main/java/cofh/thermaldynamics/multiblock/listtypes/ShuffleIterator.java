package cofh.thermaldynamics.multiblock.listtypes;

import cofh.lib.util.helpers.MathHelper;

import java.util.ArrayList;
import java.util.Iterator;

public class ShuffleIterator<T> implements Iterator<T> {
    ArrayList<T> list;
    int i = 0;
    int j = -1;
    T next;


    public ShuffleIterator(ArrayList<T> list) {
        this.list = list;
    }

    @Override
    public boolean hasNext() {
        if (list.size() >= i)
            return false;
        next = list.get(i);
        return true;
    }

    @Override
    public T next() {
        if (i > 0 && i < list.size()) {
            j = MathHelper.RANDOM.nextInt(i + 1);
            if (j != i) list.set(i, list.set(j, next));
        }
        i++;
        return next;
    }

    @Override
    public void remove() {
        if (j >= 0 && j < list.size())
            list.remove(j);
    }
}
