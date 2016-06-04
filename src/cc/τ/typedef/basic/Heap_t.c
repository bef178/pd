/**
 * Heap_t.c
 *
 * 大顶堆
 * TANG Hao
 * 2009-09-01
 */

interface typedef struct {
	const int capacity;
	int size;
	const compare_fp cmpr;
	void * slots[0];
} Heap_t;

static void Heap_filterDn(Heap_t * asThis, int index) {
	assert(asThis != NULL);
	assert(index >= 0);

	while (index < asThis->size) {
		int i = 2 * index + 1; // l-child
		int j = 2 * index + 2; // r-child

		int t = index;
		if (i < asThis->size
				&& asThis->cmpr(asThis->slots[i], asThis->slots[t]) > 0) {
			t = i;
		}
		if (j < asThis->size
				&& asThis->cmpr(asThis->slots[j], asThis->slots[t]) > 0) {
			t = j;
		}
		if (t == index) {
			break;
		}
		// swap slot[t] and slot[index]
		void * data = asThis->slots[index];
		asThis->slots[index] = asThis->slots[t];
		asThis->slots[t] = data;
		index = t;
	}
}

static void Heap_filterUp(Heap_t * asThis, int index) {
	assert(asThis != NULL);
	assert(index >= 0);

	while (index != 0) {
		int t = (index - 1) / 2; // parent
		if (asThis->cmpr(asThis->slots[t], asThis->slots[index]) >= 0) {
			break;
		}
		void * data = asThis->slots[index];
		asThis->slots[index] = asThis->slots[t];
		asThis->slots[t] = data;
		index = t;
	}
}

interface Heap_t * Heap_pick(int capacity, compare_fp cmpr) {
	Heap_t * asThis = mem_pick(sizeof(Heap_t) + capacity);
	word memberOffset = (word) &(((Heap_t *) 0)->capacity);
	*(int *) ((word) asThis + memberOffset) = capacity;
	memberOffset = (word) &(((Heap_t *) 0)->cmpr);
	*(compare_fp *) ((word) asThis + memberOffset) = cmpr;
	return asThis;
}

interface void Heap_drop(Heap_t * asThis) {
	if (asThis != NULL) {
		mem_drop(asThis);
		asThis = NULL;
	}
}

interface bool Heap_isFull(Heap_t * asThis) {
	return asThis->size == asThis->capacity;
}

interface bool Heap_isEmpty(Heap_t * asThis) {
	return asThis->size == 0;
}

interface void Heap_insert(Heap_t * asThis, void * data) {
	assert(asThis != NULL);
	assert(data != NULL);
	assert(!Heap_isFull(asThis));
	asThis->slots[asThis->size++] = data;
	Heap_filterUp(asThis, asThis->size - 1);
}

interface void * Heap_remove(Heap_t * asThis) {
	assert(asThis != NULL);
	assert(!Heap_isEmpty(asThis));
	void * data = asThis->slots[0];
	asThis->slots[0] = asThis->slots[--asThis->size];
	Heap_filterDn(asThis, 0);
	return data;
}
