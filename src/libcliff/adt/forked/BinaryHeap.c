/**
 * BinaryHeap.c
 *
 * since 2009-09-01
 */

interface typedef struct {
    const int capacity;
    int size;
    compare_f * const compare;
    void * slots[0];
} BinaryHeap;

interface BinaryHeap * BinaryHeap_malloc(int capacity, compare_f * compare) {
    BinaryHeap * p = mem_pick(sizeof(BinaryHeap) + capacity);
    word memberOffset = MEMBER_OFFSET(BinaryHeap, capacity);
    *(int *) ((word) p + memberOffset) = capacity;
    memberOffset = MEMBER_OFFSET(BinaryHeap, compare);
    *(compare_f **) ((word) p + memberOffset) = compare;
    //p->compare = compare;
    return p;
}

interface void BinaryHeap_free(BinaryHeap * caller) {
    mem_drop(caller);
}

void BinaryHeap_filterDn(BinaryHeap * caller, int index) {
    assert(index >= 0);
    while (index < caller->size) {
        int i = 2 * index + 1; // l-child
        int j = 2 * index + 2; // r-child

        int t = index;
        if (i < caller->size
                && caller->compare(caller->slots[i], caller->slots[t]) < 0) {
            t = i;
        }
        if (j < caller->size
                && caller->compare(caller->slots[j], caller->slots[t]) < 0) {
            t = j;
        }
        if (t == index) {
            break;
        }
        void * data = caller->slots[index];
        caller->slots[index] = caller->slots[t];
        caller->slots[t] = data;
        index = t;
    }
}

void BinaryHeap_filterUp(BinaryHeap * caller, int index) {
    assert(index >= 0);
    while (index != 0) {
        int t = (index - 1) / 2; // parent
        if (caller->compare(caller->slots[t], caller->slots[index]) <= 0) {
            break;
        }
        void * data = caller->slots[index];
        caller->slots[index] = caller->slots[t];
        caller->slots[t] = data;
        index = t;
    }
}

interface bool BinaryHeap_isEmpty(BinaryHeap * caller) {
    return caller->size == 0;
}

interface bool BinaryHeap_isFull(BinaryHeap * caller) {
    return caller->size == caller->capacity;
}

interface void BinaryHeap_insert(BinaryHeap * caller, void * data) {
    assert(!BinaryHeap_isFull(caller));
    assert(data != NULL);
    caller->slots[caller->size++] = data;
    BinaryHeap_filterUp(caller, caller->size - 1);
}

interface void * BinaryHeap_remove(BinaryHeap * caller) {
    assert(!BinaryHeap_isEmpty(caller));
    void * data = caller->slots[0];
    caller->slots[0] = caller->slots[--caller->size];
    BinaryHeap_filterDn(caller, 0);
    return data;
}
