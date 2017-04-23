ListEntry * ListEntry_malloc(void * data) {
    ListEntry * p = mem_pick(sizeof(ListEntry));
    p->listHead = ListHead_malloc();
    p->data = data;
    return p;
}

void ListEntry_drop(ListEntry * caller) {
    ListHead_free(caller->listHead);
    mem_drop(caller);
}

ListEntry * ListEntry_removeNext(ListEntry * caller) {
    ListHead * p = ListHead_removeNext(caller->listHead);
    return ListEntry_getByListHead(p);
}

ListEntry * ListEntry_getByListHead(ListHead * p) {
    return ListHead_containerOf(p, listHead, ListEntry);
}
