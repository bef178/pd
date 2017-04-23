/**
 * Deque.c
 *
 * 双端循环队列(数组)
 * head: 如果队非空，则从这里出队；总是指向一个有值的slot
 * tail = head + size: 如果队非满，则从这里入队；总是指向一个无值的slot
 */

interface typedef struct deque {
    const int capacity;
    int head;
    int size;
    void * slots[0];
} Deque;

Deque * Deque_pick(int capacity) {
    void * asThis = mem_pick(sizeof(Deque) + capacity * WORD_SIZE);
    word memberOffset = (word) &(((Deque *) 0)->capacity);
    *(int *) ((word) asThis + memberOffset) = capacity;
    return (Deque *)asThis;
}

void Deque_drop(Deque * asThis) {
    if (asThis != NULL) {
        mem_drop(asThis);
        asThis = NULL;
    }
}

int Deque_capacity(Deque * asThis) {
    assert(asThis != NULL);
    return asThis->capacity;
}

int Deque_size(Deque * asThis) {
    assert(asThis != NULL);
    return asThis->size;
}

bool Deque_isEmpty(Deque * asThis) {
    return Deque_size(asThis) == 0;
}

bool Deque_isFull(Deque * asThis) {
    return Deque_size(asThis) == Deque_capacity(asThis);
}

void Deque_clear(Deque * asThis) {
    assert(asThis != NULL);
    int capacity = asThis->capacity;
    mem_reset(asThis, sizeof(Deque) + capacity * WORD_SIZE);
    word memberOffset = (word) &(((Deque *) 0)->capacity);
    *(int *) ((word)asThis + memberOffset) = capacity;
}

void Deque_pushHead(Deque * asThis, void * entry) {
    assert(asThis != NULL);
    assert(!Deque_isFull(asThis));
    asThis->head--;
    if (asThis->head < 0) {
        asThis->head += asThis->capacity;
    }
    asThis->slots[asThis->head] = entry;
    asThis->size++;
}

void Deque_pushTail(Deque * asThis, void * entry) {
    assert(asThis != NULL);
    assert(!Deque_isFull(asThis));
    int tail = asThis->head + asThis->size;
    if (tail >= asThis->capacity) {
        tail -= asThis->capacity;
    }
    asThis->slots[tail] = entry;
    asThis->size++;
}

void * Deque_pullHead(Deque * asThis) {
    assert(asThis != NULL);
    assert(!Deque_isEmpty(asThis));
    void * entry = asThis->slots[asThis->head];
    asThis->slots[asThis->head] = NULL;
    asThis->head++;
    if (asThis->head >= asThis->capacity) {
        asThis->head -= asThis->capacity;
    }
    asThis->size--;
    return entry;
}

void * Deque_pullTail(Deque * asThis) {
    assert(asThis != NULL);
    assert(!Deque_isEmpty(asThis));
    int tail = asThis->head + asThis->size;
    if (tail >= asThis->capacity) {
        tail -= asThis->capacity;
    }
    void * entry = asThis->slots[tail];
    asThis->slots[tail] = NULL;
    asThis->size--;
    return entry;
}

void * Deque_peekHead(Deque * asThis) {
    assert(asThis != NULL);
    assert(!Deque_isEmpty(asThis));
    return asThis->slots[asThis->head];
}

void * Deque_peekTail(Deque * asThis) {
    assert(asThis != NULL);
    assert(!Deque_isEmpty(asThis));
    int tail = asThis->head + asThis->size - 1;
    if (tail >= asThis->capacity) {
        tail -= asThis->capacity;
    }
    return asThis->slots[tail];
}

// as queue & stack
void * Deque_peek(Deque * asThis) {
    return Deque_peekHead(asThis);
}

// as queue
void Deque_enqueue(Deque * asThis, void * entry) {
    Deque_pushTail(asThis, entry);
}

// as queue
void * Deque_dequeue(Deque * asThis) {
    return Deque_pullHead(asThis);
}

// as stack
void Deque_push(Deque * asThis, void * entry) {
    Deque_pushHead(asThis, entry);
}

// as stack
void * Deque_pull(Deque * asThis) {
    return Deque_pullHead(asThis);
}
