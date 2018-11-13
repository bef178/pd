package libjava.adt;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

/**
 * 二叉堆。必须传入与元素匹配的比较器。数值越小的元素越优先。
 */
public final class MinHeap<E> {

    private static final int STEP = 256;
    private final Comparator<? super E> cmpr;
    private E[] array = null;
    private int size = 0; // next position/index

    @SuppressWarnings("unchecked")
    public MinHeap(Comparator<? super E> c) {
        assert c != null;
        cmpr = c;
        array = (E[]) new Object[STEP];
    }

    public void clear() {
        size = 0;
    }

    private boolean filter(int index) {
        boolean isFiltered = false;

        // try filter up
        int parentIndex = (index - 1) >> 1;
        while (parentIndex >= 0
                && parentIndex != getPrior(parentIndex, index)) {
            isFiltered = true;
            swapElement(parentIndex, index);
            index = parentIndex;
            parentIndex = (index - 1) >> 1;
        }

        if (isFiltered) {
            return true;
        }

        // try filter down
        int childIndex = getPrior((index << 1) + 1, (index << 1) + 2);
        while (childIndex >= 0 && childIndex == getPrior(index, childIndex)) {
            isFiltered = true;
            swapElement(index, childIndex);
            index = childIndex;
            childIndex = getPrior((index << 1) + 1, (index << 1) + 2);
        }

        return isFiltered;
    }

    public int find(E element) {
        assert element != null;
        for (int i = 0; i < size; ++i) {
            if (array[i].equals(element)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return the index of:<br/>
     *  &emsp;the prior one if both legal<br/>
     *  &emsp;the legal one if either legal<br/>
     *  &emsp;negative if neither legal<br/>
     */
    private int getPrior(int i, int j) {
        if (isIndexInBounds(i)) {
            if (isIndexInBounds(j)) {
                // both legal
                return cmpr.compare(array[i], array[j]) <= 0 ? i : j;
            } else {
                // only i legal
                return i;
            }
        } else {
            if (isIndexInBounds(j)) {
                // only j legal
                return j;
            } else {
                // neither legal
                return -1;
            }
        }
    }

    public void insert(E element) {
        assert element != null;

        if (size == array.length) {
            array = Arrays.copyOf(array, size + STEP);
        }
        array[size] = element;
        filter(size++);
    }

    private boolean isIndexInBounds(int index) {
        return index >= 0 && index < size;
    }

    public Iterator<E> iterator() {
        return Arrays.asList(array).iterator();
    }

    public E remove() {
        if (size <= 0) {
            return null;
        }

        E element = array[0];
        array[0] = array[--size];
        array[size] = null; // avoid memory leak
        filter(0);
        return element;
    }

    public int size() {
        return size;
    }

    private void swapElement(int i, int j) {
        E t = array[j];
        array[j] = array[i];
        array[i] = t;
    }

    public boolean update(E element) {
        assert element != null;

        for (int i = 0; i < size; ++i) {
            if (array[i] == element) {
                filter(i);
                return true;
            }
        }
        return false;
    }
}
