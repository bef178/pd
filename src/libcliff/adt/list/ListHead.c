interface ListHead * ListHead_malloc() {
    return (ListHead *) mem_pick(sizeof(ListHead));
}

interface void ListHead_free(ListHead * caller) {
    mem_drop(caller);
}

// self-express circular doubly linked list
interface void ListHead_init(ListHead * caller) {
    caller->next = caller;
    caller->prev = caller;
}

/**
 * simply connect the two nodes, both joints should be NULL
 */
interface void ListHead_attachNext(ListHead * caller, ListHead * futureNext) {
    if (futureNext != NULL) {
        caller->next = futureNext;
        futureNext->prev = caller;
    }
}

/**
 * simply disconnect the two nodes, both joints will be NULL
 * return the detached node
 */
interface ListHead * ListHead_detachNext(ListHead * caller) {
    ListHead * asNext = caller->next;
    if (asNext != NULL) {
        caller->next = NULL;
        assert(asNext->prev == caller);
        asNext->prev = NULL;
    }
    return asNext;
}

/**
 * the guest node should be clean
 */
interface void ListHead_insertNext(ListHead * caller, ListHead * futureNext) {
    assert(futureNext != NULL);

    futureNext->next = caller->next;
    futureNext->prev = caller;

    if (caller->next != NULL) {
        caller->next->prev = futureNext;
    }
    caller->next = futureNext;
}

interface ListHead * ListHead_removeNext(ListHead * caller) {
    ListHead * asNext = caller->next;
    if (asNext != NULL) {
        asNext->next->prev = caller;
        caller->next = asNext->next;
        asNext->next = NULL;
        asNext->prev = NULL;
    }
    return asNext;
}

/**
 * the guest node will be clean
 * note if caller is the only element, the self-express list will be gone
 */
interface void ListHead_remove(ListHead * caller) {
    if (caller->prev != NULL) {
        caller->prev->next = caller->next;
    }
    if (caller->next != NULL) {
        caller->next->prev = caller->prev;
    }
    caller->next = NULL;
    caller->prev = NULL;
}
