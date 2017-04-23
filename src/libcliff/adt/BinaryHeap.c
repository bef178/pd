/**
 * BinaryHeap.c
 *
 * 大顶堆
 * TANG Hao
 * 2009-09-01
 */

interface typedef struct {
    const int capacity;
    int size;
    const compare_fp compare;
    void * slots[0];
} BinaryHeap;

static void BinaryHeap_filterDn(BinaryHeap * asThis, int index) {
    assert(asThis != NULL);
    assert(index >= 0);

    while (index < asThis->size) {
        int i = 2 * index + 1; // l-child
        int j = 2 * index + 2; // r-child

        int t = index;
        if (i < asThis->size
                && asThis->compare(asThis->slots[i], asThis->slots[t]) > 0) {
            t = i;
        }
        if (j < asThis->size
                && asThis->compare(asThis->slots[j], asThis->slots[t]) > 0) {
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

static void BinaryHeap_filterUp(BinaryHeap * asThis, int index) {
    assert(asThis != NULL);
    assert(index >= 0);

    while (index != 0) {
        int t = (index - 1) / 2; // parent
        if (asThis->compare(asThis->slots[t], asThis->slots[index]) >= 0) {
            break;
        }
        void * data = asThis->slots[index];
        asThis->slots[index] = asThis->slots[t];
        asThis->slots[t] = data;
        index = t;
    }
}

interface BinaryHeap * BinaryHeap_pick(int capacity, compare_fp compare) {
    BinaryHeap * asThis = mem_pick(sizeof(BinaryHeap) + capacity);
    word memberOffset = (word) &(((BinaryHeap *) 0)->capacity);
    *(int *) ((word) asThis + memberOffset) = capacity;
    memberOffset = (word) &(((BinaryHeap *) 0)->compare);
    *(compare_fp *) ((word) asThis + memberOffset) = compare;
    return asThis;
}

interface void BinaryHeap_drop(BinaryHeap * asThis) {
    if (asThis != NULL) {
        mem_drop(asThis);
        asThis = NULL;
    }
}

interface bool BinaryHeap_isFull(BinaryHeap * asThis) {
    return asThis->size == asThis->capacity;
}

interface bool BinaryHeap_isEmpty(BinaryHeap * asThis) {
    return asThis->size == 0;
}

interface void BinaryHeap_insert(BinaryHeap * asThis, void * data) {
    assert(asThis != NULL);
    assert(data != NULL);
    assert(!BinaryHeap_isFull(asThis));
    asThis->slots[asThis->size++] = data;
    BinaryHeap_filterUp(asThis, asThis->size - 1);
}

interface void * BinaryHeap_remove(BinaryHeap * asThis) {
    assert(asThis != NULL);
    assert(!BinaryHeap_isEmpty(asThis));
    void * data = asThis->slots[0];
    asThis->slots[0] = asThis->slots[--asThis->size];
    BinaryHeap_filterDn(asThis, 0);
    return data;
}
