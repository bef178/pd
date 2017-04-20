package libcliff.adt;

import java.util.Random;

/**
 * +---+                         +---+                                   +---+
 * | x | ----------------------> | x | --------------------------------> | x |
 * +---+     +---+               +---+     +---+                         +---+
 * | x | --> | x | ------------> | x | --> | x | ----------------------> | x |
 * +---+     +---+     +---+     +---+     +---+     +---+     +---+     +---+
 * | x | --> | x | --> | x | --> | x | --> | x | --> | x | --> | x | --> | x |
 * +---+     +---+     +---+     +---+     +---+     +---+     +---+     +---+
 */
public class SkipList<E extends Comparable<E>> {

	private class Node {

		E element;

		Node[] forward;

		@SuppressWarnings("unchecked")
		public Node(E element, int height) {
			assert height > 0 && height <= maxHeight;
			this.element = element;
			this.forward = new SkipList.Node[height];
		}
	}

	private static final int DEFAULT_MAX_HEIGHT = 16;
	private static final int DEFAULT_PROBABILITY_PERCENTAGE = 50;

	private final Random random = new Random(System.nanoTime());

	private final int maxHeight;
	private final int probalityPercentage;

	private final Node header;
	private int height;

	public SkipList() {
		this(DEFAULT_MAX_HEIGHT, DEFAULT_PROBABILITY_PERCENTAGE);
	}

	public SkipList(int maxHeight, int probalityPercentage) {
		assert maxHeight > 0;
		assert probalityPercentage >= 0 && probalityPercentage <= 100;
		this.maxHeight = maxHeight;
		this.probalityPercentage = probalityPercentage;
		this.header = new Node(null, maxHeight);
		this.height = 0;
	}

	public E find(E element) {
		assert element != null;

		if (isEmpty()) {
			return null;
		}

		Node[] prev = findPrev(element);
		assert prev[0] != null;
		Node p = prev[0].forward[0];
		if (p != null && p.element.compareTo(element) == 0) {
			return p.element;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Node[] findPrev(E element) {
		Node[] prev = new SkipList.Node[maxHeight];

		Node p = header;
		for (int i = height - 1; i >= 0; --i) {
			while (p.forward[i] != null
					&& p.forward[i].element.compareTo(element) < 0) {
				p = p.forward[i];
			}
			prev[i] = p;
		}
		return prev;
	}

	/**
	 * @return [1, maxHeight]
	 */
	private int findRandomHeight() {
		int height = 1;
		while (random.nextInt(100) < probalityPercentage) {
			++height;
		}
		return height < maxHeight ? height : maxHeight;
	}

	public E insert(E element) {
		assert element != null;

		Node[] prev = findPrev(element);
		if (!isEmpty()) {
			assert prev[0] != null;
			Node p = prev[0].forward[0];
			if (p != null && p.element.compareTo(element) == 0) {
				E t = p.element;
				p.element = element;
				return t;
			}
		}

		int h = findRandomHeight();

		if (h > height) {
			for (int i = height; i < h; ++i) {
				prev[i] = header;
			}
			height = h;
		}

		Node node = new Node(element, h);
		while (h-- > 0) {
			node.forward[h] = prev[h].forward[h];
			prev[h].forward[h] = node;
		}
		return null;
	}

	public boolean isEmpty() {
		return height == 0;
	}

	public E remove(E element) {
		assert element != null;

		if (isEmpty()) {
			return null;
		}

		Node[] prev = findPrev(element);
		assert prev[0] != null;
		Node p = prev[0].forward[0];
		if (p == null || p.element.compareTo(element) != 0) {
			return null;
		}

		for (int i = 0; i < height; ++i) {
			if (prev[i].forward[i] != p) {
				break;
			}
			prev[i].forward[i] = p.forward[i];
		}

		while (height > 0 && header.forward[height - 1] == null) {
			--height;
		}

		return p.element;
	}
}
