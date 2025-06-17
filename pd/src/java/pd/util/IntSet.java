package pd.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class IntSet<E> {

    private final IntNode<E>[] slots = new IntNode[64];

    private int size() {
        return (int) getAll(a -> true).count();
    }

    public boolean isEmpty() {
        for (IntNode<E> node : slots) {
            if (node != null) {
                return false;
            }
        }
        return true;
    }

    public boolean containsKey(int key) {
        IntNode<E> node = slots[getIndex(key)];
        while (node != null) {
            if (node.key == key) {
                return true;
            }
            node = node.next;
        }
        return false;
    }

    private int getIndex(int key) {
        // slots.length must be powers of 2
        return (slots.length - 1) & key;
    }

    private Stream<IntEntry<E>> getAll(Predicate<IntEntry<E>> predicate) {
        return Arrays.stream(slots).flatMap(node -> {
            List<IntEntry<E>> a = new LinkedList<>();
            while (node != null) {
                if (predicate.test(node)) {
                    a.add(node);
                }
                node = node.next;
            }
            return a.stream();
        });
    }

    public Set<IntEntry<E>> entrySet() {
        return getAll(a -> true).collect(Collectors.toSet());
    }

    public int[] keys() {
        return getAll(a -> true).mapToInt(a -> a.key).toArray();
    }

    public List<E> values() {
        return getAll(a -> true).map(a -> a.value).collect(Collectors.toList());
    }

    public E get(int key) {
        IntNode<E> node = slots[getIndex(key)];
        while (node != null) {
            if (node.key == key) {
                return node.value;
            }
            node = node.next;
        }
        return null;
    }

    public E set(int key, E value) {
        int slotIndex = getIndex(key);
        IntNode<E> node = slots[slotIndex];
        IntNode<E> prev = null;
        while (node != null) {
            if (node.key == key) {
                break;
            }
            prev = node;
            node = node.next;
        }
        if (node != null) {
            E old = node.value;
            node.value = value;
            return old;
        } else if (prev != null) {
            prev.next = new IntNode<>(key, value);
        } else {
            slots[slotIndex] = new IntNode<>(key, value);
        }
        return null;
    }

    public E remove(int key) {
        int slotIndex = getIndex(key);
        IntNode<E> node = slots[slotIndex];
        IntNode<E> prev = null;
        while (node != null) {
            if (node.key == key) {
                break;
            }
            prev = node;
            node = node.next;
        }
        if (node == null) {
            // not found
            return null;
        } else if (prev == null) {
            // first in slot
            slots[slotIndex] = node.next;
            return node.value;
        } else {
            prev.next = node.next;
            return node.value;
        }
    }
}
