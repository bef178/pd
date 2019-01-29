package pd.adt;

import java.util.Iterator;

/**
 * doubly linked list with head node
 */
public class LinkList<T extends Object> implements Iterable<T> {

    /**
     * Ideally, an iterator should subscribe the modification message of its
     * host. So the host should maintain a collection of existing iterator. The
     * question is what's the collection. It may be a list, which is under
     * construction. Or it may have a linked chain, or even weak references,
     * but still costs too much.<br/>
     * <br/>
     * Finally, the iterator turns out to be a very simple version that does not
     * check concurrent modification.
     *
     * @author tanghao
     */
    private class LinkIterator implements Iterator<T> {

        private boolean readyToRemove = false;

        private Node current;

        private LinkIterator(Node node) {
            assert node != null;
            current = node;
        }

        @Override
        public boolean hasNext() {
            return !current.isEnd();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new IndexOutOfBoundsException();
            }
            readyToRemove = true;
            return current.offset(1).data();
        }

        @Override
        public void remove() {
            if (!readyToRemove) {
                throw new IllegalStateException("cannot remove");
            }
            current = current.offset(-1);
            current.removeNext();
            readyToRemove = false;
        }
    }

    /**
     * Provides motion, detection, connection methods.<br/>
     */
    private class Node {

        private Node next;

        private Node prev;

        private T data;

        private Node() {
            // private dummy
        };

        /**
         * a clean attach action checking preconditions
         */
        public void enlinkNext(Node futureNext) {
            this.next = futureNext;
            if (futureNext != null) {
                futureNext.prev = this;
            }
        }

        public T data() {
            return this.data;
        }

        public void data(T data) {
            assert data != null;
            this.data = data;
        }

        /**
         * *return* the delinked Node
         */
        public Node delinkNext() {
            Node asNext = this.next;
            if (asNext != null) {
                this.next = null;
                assert asNext.prev == this;
                asNext.prev = null;
            }
            return asNext;
        }

        public void insertNext(Node futureNext) {
            assert futureNext != null;
            Node asNext = this.delinkNext();
            this.enlinkNext(futureNext);
            futureNext.enlinkNext(asNext);
        }

        protected boolean isEnd() {
            return this == head || this == null;
        }

        /**
         * *return* the nth Link from this Link (this as 0), to the
         * right/fore/positive direction; null if it reaches null
         */
        public Node offset(int n) {
            Node link = this;
            if (n >= 0) {
                while (n-- > 0 && link != null) {
                    link = link.next;
                }
            } else {
                while (n++ < 0 && link != null) {
                    link = link.prev;
                }
            }
            return link;
        }

        public Node removeNext() {
            Node removed = this.delinkNext();
            if (removed != null) {
                this.enlinkNext(removed.delinkNext());
            }
            return removed;
        }
    }

    private final Node head;

    private int size;

    public LinkList() {
        head = new Node();
        head.enlinkNext(head);
        size = 0;
    }

    public void clear() {
        if (size == 0) {
            return;
        }
        head.delinkNext();
        head.enlinkNext(head);
        size = 0;
    }

    public boolean contains(T element) {
        assert element != null;
        return indexOf(element) >= 0;
    }

    private void failIfIndexOutOfBounds(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException(this.getClass().getName()
                    + ": size:" + size + " index:" + index);
        }
    }

    public T get(int index) {
        failIfIndexOutOfBounds(index);
        return head.offset(index + 1).data();
    }

    public int indexOf(T data) {
        assert data != null;
        int index = -1;
        Node link = head;
        while (!link.isEnd()) {
            link = link.offset(1);
            ++index;
            if (link.data().equals(data)) {
                return index;
            }
        }
        return -1;
    }

    public void insert(int index, T data) {
        failIfIndexOutOfBounds(index);
        Node node = new Node();
        node.data(data);
        head.offset(index).insertNext(node);
        ++size;
    }

    public void insert(T data) {
        Node node = new Node();
        node.data(data);
        head.offset(-1).insertNext(node);
        ++size;
    }

    public boolean isEmpty() {
        return head.isEnd();
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkIterator(head);
    }

    public T remove(int index) {
        failIfIndexOutOfBounds(index);
        Node removed = head.offset(index).removeNext();
        --size;
        return removed.data();
    }

    public int size() {
        return size;
    }
}
