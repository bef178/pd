package pd.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LinkedIntSet<E> {

    transient IntSet<E> m;

    transient LinkedList<IntEntry<E>> a;

    public int size() {
        return a.size();
    }

    public boolean isEmpty() {
        return a.isEmpty();
    }

    public boolean containsKey(int key) {
        return m.containsKey(key);
    }

    public List<IntEntry<E>> getAll(Predicate<IntEntry<E>> predicate) {
        return a.stream().filter(predicate).collect(Collectors.toList());
    }

    public Set<IntEntry<E>> entrySet() {
        return a.stream().map(a -> (IntEntry<E>) a).collect(Collectors.toSet());
    }

    public int[] keys() {
        return a.stream().mapToInt(a -> a.key).toArray();
    }

    public List<E> values() {
        return a.stream().map(a -> a.value).collect(Collectors.toList());
    }

    public E get(int key) {
        return m.get(key);
    }

    public E set(int key, E value) {
        E old = remove(key);
        m.set(key, value);
        a.add(new IntEntry<>(key, value));
        return old;
    }

    public E remove(int key) {
        E old = m.remove(key);
        a.removeIf(a -> a.key == key);
        return old;
    }
}
