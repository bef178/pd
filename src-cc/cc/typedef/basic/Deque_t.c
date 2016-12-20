/**
 * Deque_t.c
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
} Deque_t;

Deque_t * Deque_pick(int capacity) {
    void * asThis = mem_pick(sizeof(Deque_t) + capacity * WORD_SIZE);
    word memberOffset = (word) &(((Deque_t *) 0)->capacity);
    *(int *) ((word) asThis + memberOffset) = capacity;
    return (Deque_t *)asThis;
}

void Deque_drop(Deque_t * asThis) {
    if (asThis != NULL) {
        mem_drop(asThis);
        asThis = NULL;
    }
}

int Deque_capacity(Deque_t * asThis) {
    assert(asThis != NULL);
    return asThis->capacity;
}

int Deque_size(Deque_t * asThis) {
    assert(asThis != NULL);
    return asThis->size;
}

bool Deque_isEmpty(Deque_t * asThis) {
    return Deque_size(asThis) == 0;
}

bool Deque_isFull(Deque_t * asThis) {
    return Deque_size(asThis) == Deque_capacity(asThis);
}

void Deque_clear(Deque_t * asThis) {
    assert(asThis != NULL);
    int capacity = asThis->capacity;
    mem_reset(asThis, sizeof(Deque_t) + capacity * WORD_SIZE);
    word memberOffset = (word) &(((Deque_t *) 0)->capacity);
    *(int *) ((word)asThis + memberOffset) = capacity;
}

void Deque_pushHead(Deque_t * asThis, void * entry) {
    assert(asThis != NULL);
    assert(!Deque_isFull(asThis));
    asThis->head--;
    if (asThis->head < 0) {
        asThis->head += asThis->capacity;
    }
    asThis->slots[asThis->head] = entry;
    asThis->size++;
}

void Deque_pushTail(Deque_t * asThis, void * entry) {
    assert(asThis != NULL);
    assert(!Deque_isFull(asThis));
    int tail = asThis->head + asThis->size;
    if (tail >= asThis->capacity) {
        tail -= asThis->capacity;
    }
    asThis->slots[tail] = entry;
    asThis->size++;
}

void * Deque_pullHead(Deque_t * asThis) {
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

void * Deque_pullTail(Deque_t * asThis) {
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

void * Deque_peekHead(Deque_t * asThis) {
    assert(asThis != NULL);
    assert(!Deque_isEmpty(asThis));
    return asThis->slots[asThis->head];
}

void * Deque_peekTail(Deque_t * asThis) {
    assert(asThis != NULL);
    assert(!Deque_isEmpty(asThis));
    int tail = asThis->head + asThis->size - 1;
    if (tail >= asThis->capacity) {
        tail -= asThis->capacity;
    }
    return asThis->slots[tail];
}

// as queue & stack
void * Deque_peek(Deque_t * asThis) {
    return Deque_peekHead(asThis);
}

// as queue
void Deque_enqueue(Deque_t * asThis, void * entry) {
    Deque_pushTail(asThis, entry);
}

// as queue
void * Deque_dequeue(Deque_t * asThis) {
    return Deque_pullHead(asThis);
}

// as stack
void Deque_push(Deque_t * asThis, void * entry) {
    Deque_pushHead(asThis, entry);
}

// as stack
void * Deque_pull(Deque_t * asThis) {
    return Deque_pullHead(asThis);
}
