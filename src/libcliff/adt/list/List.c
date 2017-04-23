/**
 * List.c
 *
 * List强调位序，数据的先后位置是有意义的，因而其主要操作都依赖于位序
 *
 * cyclic doubly linked list with head node
 * accepting only non-NULL value
 */

bool List_isValidToGet(List * caller, int index) {
    return index < caller->size && index > -caller->size - 1;
}

bool List_isValidToInsert(List * caller, int futureIndex) {
    return futureIndex <= caller->size && futureIndex >= -caller->size - 1;
}

int List_toBetterIndex(List * caller, int index) {
    if (caller->size > 6) {
        if (index >= 0) {
            if (index > caller->size / 2 + 1) {
                index -= caller->size + 1;
            }
        } else {
            if (index < -caller->size / 2 - 1) {
                index += caller->size + 1;
            }
        }
    }
    return index;
}

ListHead * List_getPrevListHead(List * caller, int index) {
    assert(caller != NULL);
    index = List_toBetterIndex(caller, index);
    ListHead * head = caller->head->listHead;
    ListHead * p = head;
    if (index >= 0) {
        // needn't check: p->next != NULL && p->next != head
        while (index-- != 0) {
            p = p->next;
        }
    } else {
        while (index++ != 0) {
            p = p->prev;
        }
    }
    return p;
}

interface List * List_malloc() {
    List * p = mem_pick(sizeof(List));
    p->head = ListEntry_malloc(NULL);
    ListHead_init(p->head->listHead);
    return p;
}

interface void List_free(List * caller) {
    assert(List_isEmpty(caller));
    ListEntry_free(caller->head);
    caller->head = NULL;
    mem_drop(caller);
}

interface void List_clear(List * caller) {
    ListEntry * head = caller->head;
    while (!List_isEmpty(caller)) {
        ListEntry * entry = ListEntry_removeNext(caller->head);
        --caller->size;
        ListEntry_free(entry);
    }
}

interface void * List_get(List * caller, int index) {
    assert(List_isValidToGet(caller, index));
    ListHead * p = List_getPrevListHead(caller, index);
    return ListEntry_getByListHead(p->next)->data;
}

/**
 * negative for not found
 */
interface int List_indexOf(List * caller, void * data, int start, compare_fp compare) {
    assert(List_isValidToGet(caller, start));
    assert(data != NULL);

    ListHead * head = caller->head->listHead;
    ListHead * p = List_getPrevListHead(caller, start);
    ListEntry * entry = null;
    while (p->next != NULL && p->next != head) {
        entry = ListEntry_getByListHead(p->next);
        if (compare(entry->data, data) == 0) {
            return start;
        }
        p = p->next;
        ++start;
    }
    return -1;
}

interface void List_insert(List * caller, int index, void * data) {
    assert(List_isValidToInsert(caller, index));
    assert(data != NULL);
    ListHead * p = List_getPrevListHead(caller, index);
    ListEntry * entry = ListEntry_malloc(data);
    ListHead_insertNext(p, entry->listHead);
    ++caller->size;
}

interface bool List_isEmpty(List * caller) {
    ListHead * head = caller->head->listHead;
    return head->next == NULL || head->next == head;
}

interface void * List_remove(List * caller, int index) {
    assert(List_isValidToGet(caller, index));

    ListHead * p = List_getPrevListHead(caller, index);
    p = ListHead_removeNext(p);
    --caller->size;
    ListEntry * entry = ListEntry_getByListHead(p);
    void * origData = entry->data;
    ListEntry_free(entry);
    entry = NULL;
    return origData;
}

interface void * List_set(List * caller, int index, void * data) {
    assert(List_isValidToGet(caller, index));
    assert(data != NULL);

    ListHead * p = List_getPrevListHead(caller, index);
    ListEntry * entry = ListEntry_getByListHead(p->next);
    void * origData = entry->data;
    entry->data = data;
    return origData;
}

interface int List_size(List * caller) {
    ListHead * head = caller->head->listHead;
    ListHead * p = head;
    int size = 0;
    while (p->next != NULL && p->next != head) {
        p = p->next;
        ++size;
    }
    caller->size = size;
    return size;
}

// straight insertion sort
interface void List_sort(List * caller, compare_fp compare) {
    List * temp = List_malloc();
    ListHead * head = temp->head->listHead;
    while (!List_isEmpty(caller)) {
        ListEntry * entry = ListEntry_removeNext(caller->head);
        --caller->size;
        ListHead * p = head->next;
        while (p != NULL && p != head) {
            ListEntry * t = ListEntry_getByListHead(p);
            if (compare(entry->data, t->data) < 0) {
                break;
            }
            p = p->next;
        }
        ListHead_insertNext(p->prev, entry->listHead);
        ++temp->size;
    }

    head = caller->head->listHead;
    head->next = NULL;
    head->prev = NULL;
    ListHead_insertNext(temp->head->listHead->prev, caller->head->listHead);
    ListHead_removeNext(caller->head->listHead);
    caller->size = temp->size;

    temp->size = 0;
    ListHead_init(temp->head->listHead);
    List_free(temp);
    temp = NULL;
}
