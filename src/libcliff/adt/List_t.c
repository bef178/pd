/**
 * List_t.c
 *
 * List强调位序，数据的先后位置是有意义的，因而其主要操作都依赖于位序
 *
 * cyclic doubly linked list with head node
 * accepting only non-NULL value
 * tanghao
 */

bool List_isIndexForAdd(List_t * asThis, int index) {
    return index <= asThis->size && index >= -asThis->size - 1;
}

bool List_isIndexForGet(List_t * asThis, int index) {
    return index < asThis->size && index >= -asThis->size;
}

int List_betterIndex(List_t * asThis, int index) {
    if (asThis->size > 6) {
        if (index >= 0) {
            if (index > asThis->size / 2 + 1) {
                index -= asThis->size + 1;
            }
        } else {
            if (index < -asThis->size / 2 - 1) {
                index += asThis->size + 1;
            }
        }
    }
    return index;
}

ListHead_t * List_getPrevLink(List_t * asThis, int index) {
    assert(asThis != NULL);
    index = List_betterIndex(asThis, index);
    ListHead_t * head = asThis->head->listHead;
    ListHead_t * p = head;
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

interface List_t * List_pick() {
    List_t * asThis = mem_pick(sizeof(List_t));
    asThis->head = ListEntry_pick(NULL);
    ListHead_init(asThis->head->listHead);
    return asThis;
}

interface void List_drop(List_t * asThis) {
    if (asThis == NULL) {
        return;
    }
    assert(List_isEmpty(asThis));
    ListEntry_drop(asThis->head);
    asThis->head = NULL;
    mem_drop(asThis);
    asThis = NULL;
}

interface bool List_isEmpty(List_t * asThis) {
    assert(asThis != NULL);
    ListHead_t * head = asThis->head->listHead;
    return head->next == NULL || head->next == head;
}

interface int List_size(List_t * asThis) {
    assert(asThis != NULL);
    ListHead_t * head = asThis->head->listHead;
    ListHead_t * p = head;
    int size = 0;
    while (p->next != NULL && p->next != head) {
        p = p->next;
        ++size;
    }
    asThis->size = size;
    return size;
}

interface void List_clear(List_t * asThis) {
    assert(asThis != NULL);
    ListEntry_t * head = asThis->head;
    while (!List_isEmpty(asThis)) {
        ListEntry_t * node = ListEntry_removeNext(asThis->head);
        --asThis->size;
        ListEntry_drop(node);
        node = NULL;
    }
}

interface void * List_get(List_t * asThis, int index) {
    assert(asThis != NULL);
    assert(List_isIndexForGet(asThis, index));
    ListHead_t * p = List_getPrevLink(asThis, index);
    return ListEntry_getByLink(p->next)->data;
}

interface void * List_set(List_t * asThis, int index, void * data) {
    assert(asThis != NULL);
    assert(List_isIndexForGet(asThis, index));
    assert(data != NULL);

    ListHead_t * p = List_getPrevLink(asThis, index);
    ListEntry_t * node = ListEntry_getByLink(p->next);
    void * origData = node->data;
    node->data = data;
    return origData;
}

interface void List_insert(List_t * asThis, int index, void * data) {
    assert(asThis != NULL);
    assert(List_isIndexForAdd(asThis, index));
    assert(data != NULL);
    ListHead_t * p = List_getPrevLink(asThis, index);
    ListEntry_t * node = ListEntry_pick(data);
    ListHead_insertNext(p, node->listHead);
    ++asThis->size;
}

interface void * List_remove(List_t * asThis, int index) {
    assert(asThis != NULL);
    assert(List_isIndexForGet(asThis, index));

    ListHead_t * p = List_getPrevLink(asThis, index);
    p = ListHead_removeNext(p);
    --asThis->size;
    ListEntry_t * node = ListEntry_getByLink(p);
    void * origData = node->data;
    ListEntry_drop(node);
    node = NULL;
    return origData;
}

/**
 * negative for not found
 */
interface int List_search(List_t * asThis, void * data, int startIndex,
        compare_fp dataCmpr) {
    assert(asThis != NULL);
    assert(List_isIndexForGet(asThis, startIndex));
    assert(data != NULL);

    ListHead_t * head = asThis->head->listHead;
    ListHead_t * p = List_getPrevLink(asThis, startIndex);
    ListEntry_t * node = null;
    while (p->next != NULL && p->next != head) {
        node = ListEntry_getByLink(p->next);
        if (dataCmpr(node->data, data) == 0) {
            return startIndex;
        }
        p = p->next;
        ++startIndex;
    }
    return -1;
}

// straight insertion sort
interface void List_sort(List_t * asThis, compare_fp dataCmpr) {
    assert(asThis != NULL);

    List_t * temp = List_pick();
    ListHead_t * head = temp->head->listHead;
    while (!List_isEmpty(asThis)) {
        ListEntry_t * node = ListEntry_removeNext(asThis->head);
        --asThis->size;
        ListHead_t * p = head->next;
        while (p != NULL && p != head) {
            ListEntry_t * t = ListEntry_getByLink(p);
            if (dataCmpr(node->data, t->data) < 0) {
                break;
            }
            p = p->next;
        }
        ListHead_insertNext(p->prev, node->listHead);
        ++temp->size;
    }

    head = asThis->head->listHead;
    head->next = NULL;
    head->prev = NULL;
    ListHead_insertNext(temp->head->listHead->prev, asThis->head->listHead);
    ListHead_removeNext(asThis->head->listHead);
    asThis->size = temp->size;

    temp->size = 0;
    ListHead_init(temp->head->listHead);
    List_drop(temp);
    temp = NULL;
}
