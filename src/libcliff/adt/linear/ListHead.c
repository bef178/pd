interface typedef struct list_head {
    struct list_head * next;
    struct list_head * prev;
} ListHead;

void ListHead_enlinkNext(ListHead * caller, ListHead * futureNext);

interface ListHead * ListHead_malloc() {
    ListHead * p = (ListHead *) mem_pick(sizeof(ListHead));
    ListHead_enlinkNext(p, p);
    return p;
}

interface void ListHead_free(ListHead * caller) {
    mem_drop(caller);
}

/**
 * simply connect the two nodes, both joints should be NULL
 */
void ListHead_enlinkNext(ListHead * caller, ListHead * futureNext) {
    caller->next = futureNext;
    if (futureNext != NULL) {
        futureNext->prev = caller;
    }
}

/**
 * simply disconnect the two nodes, both joints will be NULL
 * return the delinked node
 */
ListHead * ListHead_delinkNext(ListHead * caller) {
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
    // futureNext->next = caller->next;
    // futureNext->prev = caller;
    // if (caller->next != NULL) {
    //     caller->next->prev = futureNext;
    // }
    // caller->next = futureNext;
    ListHead * asNext = ListHead_delinkNext(caller);
    ListHead_enlinkNext(caller, futureNext);
    ListHead_enlinkNext(futureNext, asNext);
}

interface ListHead * ListHead_removeNext(ListHead * caller) {
    ListHead * asNext = caller->next;
    if (asNext != NULL) {
        // caller->next = asNext->next;
        // if (asNext->next != null) {
        //     asNext->next->prev = caller;
        // }
        // asNext->next = NULL;
        // asNext->prev = NULL;
        ListHead_enlinkNext(caller, ListHead_delinkNext(asNext));
    }
    return asNext;
}

interface ListHead * ListHead_offset(ListHead * caller, int n) {
    ListHead * p = caller;
    if (n >= 0) {
        while (n-- > 0 && p != NULL) {
            p = p->next;
        }
    } else {
        while (n++ < 0 && p != NULL) {
            p = p->prev;
        }
    }
    return p;
}
