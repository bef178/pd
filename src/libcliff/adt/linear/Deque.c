/**
 * 双端循环队列(数组)
 * head: 如果队非空，则从这里出队；总是指向一个有值的slot
 * tail = head + size: 如果队非满，则从这里入队；总是指向一个无值的slot
 */

interface typedef struct deque {
    const int capacity;
    int size;
    int head;
    void * slots[0];
} Deque;

interface Deque * Deque_malloc(int capacity) {
    void * p = mem_pick(sizeof(Deque) + capacity * WORD_SIZE);
    word memberOffset = (word) &(((Deque *) 0)->capacity);
    *(int *) ((word) p + memberOffset) = capacity;
    return (Deque *) p;
}

interface void Deque_free(Deque * caller) {
    mem_drop(caller);
}

interface int Deque_size(Deque * caller) {
    return caller->size;
}

interface bool Deque_isEmpty(Deque * caller) {
    return caller->size == 0;
}

interface bool Deque_isFull(Deque * caller) {
    return caller->size == caller->capacity;
}

interface void Deque_clear(Deque * caller) {
    int capacity = caller->capacity;
    mem_reset(caller, sizeof(Deque) + capacity * WORD_SIZE);
    word memberOffset = MEMBER_OFFSET(Deque, capacity);
    *(int *) ((word)caller + memberOffset) = capacity;
}

interface int Deque_capacity(Deque * caller) {
    return caller->capacity;
}

interface void Deque_pushHead(Deque * caller, void * d) {
    assert(!Deque_isFull(caller));
    if (!Deque_isEmpty(caller)) {
        caller->head--;
        if (caller->head < 0) {
            caller->head += caller->capacity;
        }
    }
    caller->slots[caller->head] = d;
    caller->size++;
}

interface void Deque_pushTail(Deque * caller, void * d) {
    assert(!Deque_isFull(caller));
    int tail = caller->head + caller->size;
    if (tail >= caller->capacity) {
        tail -= caller->capacity;
    }
    caller->slots[tail] = d;
    caller->size++;
}

interface void * Deque_pullHead(Deque * caller) {
    assert(!Deque_isEmpty(caller));
    void * d = caller->slots[caller->head];
    caller->slots[caller->head] = NULL;
    caller->head++;
    if (caller->head >= caller->capacity) {
        caller->head -= caller->capacity;
    }
    caller->size--;
    return d;
}

interface void * Deque_pullTail(Deque * caller) {
    assert(!Deque_isEmpty(caller));
    int tail = caller->head + caller->size - 1;
    if (tail >= caller->capacity) {
        tail -= caller->capacity;
    }
    void * d = caller->slots[tail];
    caller->slots[tail] = NULL;
    caller->size--;
    return d;
}

interface void * Deque_peekHead(Deque * caller) {
    assert(!Deque_isEmpty(caller));
    return caller->slots[caller->head];
}

interface void * Deque_peekTail(Deque * caller) {
    assert(!Deque_isEmpty(caller));
    int tail = caller->head + caller->size - 1;
    if (tail >= caller->capacity) {
        tail -= caller->capacity;
    }
    return caller->slots[tail];
}

// as stack
interface void Deque_push(Deque * caller, void * d) {
    Deque_pushHead(caller, d);
}

// as stack
interface void * Deque_pull(Deque * caller) {
    return Deque_pullHead(caller);
}

// as queue & stack
interface void * Deque_peek(Deque * caller) {
    return Deque_peekHead(caller);
}

// as queue
interface void Deque_enqueue(Deque * caller, void * d) {
    Deque_pushTail(caller, d);
}

// as queue
interface void * Deque_dequeue(Deque * caller) {
    return Deque_pullHead(caller);
}
