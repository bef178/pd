package libjava.adt.forked;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class BinaryTree<E> {

    class Node {

        public Node parent = null;
        public Node prev = null;
        public Node next = null;
        public E value = null;

        public void clear() {
            parent = prev = next = null;
            value = null;
        }

        /**
         * find the true root (whose parent is null) of this tree
         */
        public Node findRoot() {
            Node root = this;
            while (root.parent != null) {
                root = root.parent;
            }
            return root;
        }

        public boolean isAncestorOf(Node node) {
            assert node != null;
            node = node.parent;
            while (node != this && node != null) {
                node = node.parent;
            }
            if (node == this) {
                return true;
            }
            return false;
        }

        /**
         * return the delinked node
         */
        public Node linkL(Node asL) {
            Node kept = this.prev;
            if ((this.prev = asL) != null) {
                asL.parent = this;
            }
            if (kept != null) {
                kept.parent = null;
            }
            return kept;
        }

        /**
         * @return the delinked node
         */
        public Node linkR(Node asR) {
            Node kept = this.next;
            if ((this.next = asR) != null) {
                asR.parent = this;
            }
            if (kept != null) {
                kept.parent = null;
            }
            return kept;
        }

        public int depthAsRoot() {
            int ldepth = (prev == null) ? 0 : prev.depthAsRoot();
            int rdepth = (next == null) ? 0 : next.depthAsRoot();
            return Math.max(ldepth, rdepth) + 1;
        }

        public int depthAsLeaf(Node asRoot) {
            Node node = this;
            int depth = 1;
            while (node.parent != null && node != asRoot) {
                node = node.parent;
                ++depth;
            }
            return (node == asRoot) ? depth : -1;
        }

    }

    public Node root = null;

    /**
     * find the depth of this tree
     */
    public int depth() {
        return (root == null) ? 0 : root.depthAsRoot();
    }

    /**
     * find the depth of node in this tree
     *
     * @return 0 if being root<br/>
     *         negative if not in tree<br/>
     */
    public int depth(Node node) {
        if (isEmpty() || node == null) {
            return -1;
        }
        return node.depthAsLeaf(root);
    }

    public boolean isEmpty() {
        return root == null;
    }

    /**
     * calculate tree size
     */
    public int size() {
        int size = 0;
        Queue<Node> queue = new LinkedList<Node>();
        Node node = root;
        if (node != null) {
            queue.add(node);
        }
        while (!queue.isEmpty()) {
            node = queue.poll();
            ++size;
            if (node.prev != null) {
                queue.add(node.prev);
            }
            if (node.next != null) {
                queue.add(node.next);
            }
        }
        return size;
    }

    public Collection<Node> toCollection() {
        Collection<Node> nodes = new LinkedList<BinaryTree<E>.Node>();
        Queue<Node> queue = new LinkedList<Node>();
        Node node = root;
        if (node != null) {
            queue.add(node);
        }
        while (!queue.isEmpty()) {
            node = queue.poll();
            nodes.add(node);
            if (node.prev != null) {
                queue.add(node.prev);
            }
            if (node.next != null) {
                queue.add(node.next);
            }
        }
        return nodes;
    }
}
