ListEntry_t * ListEntry_pick(void * data) {
    ListEntry_t * asThis = mem_pick(sizeof(ListEntry_t));
    asThis->listHead = ListHead_pick();
    asThis->data = data;
    return asThis;
}

void ListEntry_drop(ListEntry_t * asThis) {
    if (asThis == NULL) {
        return;
    }
    assert(asThis->listHead->next == NULL && asThis->listHead->prev == NULL);
    ListHead_drop(asThis->listHead);
    asThis->listHead = NULL;
    mem_drop(asThis);
    asThis = NULL;
}

ListEntry_t * ListEntry_removeNext(ListEntry_t * asThis) {
    assert(asThis != NULL);
    ListHead_t * p = ListHead_removeNext(asThis->listHead);
    return ListEntry_getByLink(p);
}

ListEntry_t * ListEntry_getByLink(ListHead_t * link) {
    return ListHead_containerOf(link, listHead, ListEntry_t);
}
