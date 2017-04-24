// FIXME unimplemented
int KeyValue_compare(void *, void *);

int HashMap_alignCapacity(int requiredCapacity) {
    static const int MAX_CAPACITY = ~(-1U >> 1);
    if (requiredCapacity < 16) {
        return 16;
    } else if (requiredCapacity > MAX_CAPACITY) {
        return MAX_CAPACITY;
    }
    int capacity = 16;
    while (capacity < requiredCapacity) {
        capacity <<= 1;
    }
    return capacity;
}

List * HashMap_findSlot(HashMap * caller, Blob * key) {
    word hashCode = mem_hash(key->a, key->n);
    hashCode = mem_rehash(hashCode) & (caller->capacity - 1);
    return caller->slots[hashCode];
}

interface HashMap * HashMap_malloc(int capacity) {
    capacity = HashMap_alignCapacity(capacity);
    HashMap * p = mem_pick(sizeof(HashMap) + capacity * sizeof(List *));
    p->capacity = capacity;
    for (int i = 0; i < capacity; ++i) {
        p->slots[i] = List_malloc();
    }
    return p;
}

interface void HashMap_free(HashMap * caller) {
    for (int i = 0; i < caller->capacity; ++i) {
        List * slot = caller->slots[i];
        while (!List_isEmpty(slot)) {
            KeyValue * entry = List_remove(slot, 0);
            KeyValue_free(entry);
        }
        List_free(slot);
    }
    mem_drop(caller);
}

interface void HashMap_clear(HashMap * caller) {
    for (int i = 0; i < caller->capacity; ++i) {
        List * slot = caller->slots[i];
        while (!List_isEmpty(slot)) {
            KeyValue * entry = List_remove(slot, 0);
            KeyValue_free(entry);
        }
    }

}

interface void * HashMap_get(HashMap * caller, Blob * key) {
    assert(key != NULL);

    List * slot = HashMap_findSlot(caller, key);
    KeyValue * temp = KeyValue_malloc(key, NULL);
    int i = List_indexOf(slot, 0, temp, KeyValue_compare);
    KeyValue_free(temp);
    temp = NULL;
    if (i < 0) {
        return NULL;
    }

    temp = List_get(slot, i);
    return temp->value;
}

/**
 * return the replaced v if exists
 */
interface void * HashMap_put(HashMap * caller, Blob * key, void * value) {
    assert(key != NULL);
    assert(value != NULL);

    List * slot = HashMap_findSlot(caller, key);
    KeyValue * entry = KeyValue_malloc(key, value);
    int i = List_indexOf(slot, 0, entry, KeyValue_compare);
    if (i >= 0) {
        KeyValue_free(entry);
        entry = List_get(slot, i);
        void * origValue = entry->value;
        entry->value = value;
        return origValue;
    } else {
        // insert
        List_insert(slot, -1, entry);
        ++caller->size;
        return NULL;
    }
}

interface void * HashMap_remove(HashMap * caller, Blob * key) {
    assert(key != NULL);

    List * slot = HashMap_findSlot(caller, key);
    int i = -1;
    {
        KeyValue * entry = KeyValue_malloc(key, NULL);
        i = List_indexOf(slot, 0, entry, KeyValue_compare);
        KeyValue_free(entry);
        entry = NULL;
    }
    if (i >= 0) {
        // remove
        KeyValue * entry = List_remove(slot, i);
        --caller->size;
        void * origValue = entry->value;
        KeyValue_free(entry);
        entry = NULL;
        return origValue;
    }
    return NULL;
}

interface int HashMap_size(HashMap * caller) {
    return caller->size;
}
