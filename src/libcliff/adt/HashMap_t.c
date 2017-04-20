// TODO this is a unimplemented function
int KeyValue_compare(void *, void *);

interface HashMap_t * HashMap_pick(int capacity) {
    capacity = HashMap_alignCapacity(capacity);
    HashMap_t * asThis = mem_pick(
            sizeof(HashMap_t) + capacity * sizeof(List_t *));
    asThis->capacity = capacity;
    for (int i = 0; i < capacity; ++i) {
        asThis->slots[i] = List_pick();
    }
    return asThis;
}

interface void HashMap_clear(HashMap_t * asThis) {
    assert(asThis != NULL);
    for (int i = 0; i < asThis->capacity; ++i) {
        List_t * slot = asThis->slots[i];
        while (!List_isEmpty(slot)) {
            KeyValue_t * entry = List_remove(slot, 0);
            KeyValue_free(entry);
        }
    }

}

interface void HashMap_drop(HashMap_t * asThis) {
    assert(asThis != NULL);
    for (int i = 0; i < asThis->capacity; ++i) {
        List_t * slot = asThis->slots[i];
        while (!List_isEmpty(slot)) {
            KeyValue_t * entry = List_remove(slot, 0);
            KeyValue_free(entry);
        }
        List_drop(slot);
    }
    mem_drop(asThis);
    asThis = NULL;
}

List_t * HashMap_findSlot(HashMap_t * asThis, Blob_t * key) {
    word hashCode = mem_hash(key->a, key->n);
    hashCode = mem_rehash(hashCode) & (asThis->capacity - 1);
    return asThis->slots[hashCode];
}

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

/**
 * return the replaced v if exists
 */
interface void * HashMap_put(HashMap_t * asThis, Blob_t * key, void * value) {
    assert(asThis != NULL);
    assert(key != NULL);
    assert(value != NULL);

    List_t * slot = HashMap_findSlot(asThis, key);
    KeyValue_t * entry = KeyValue_malloc(key, value);
    int i = List_search(slot, entry, 0, KeyValue_compare);
    if (i >= 0) {
        KeyValue_free(entry);
        entry = List_get(slot, i);
        void * origValue = entry->value;
        entry->value = value;
        return origValue;
    } else {
        // insert
        List_insert(slot, -1, entry);
        ++asThis->size;
        return NULL;
    }
}

interface void * HashMap_remove(HashMap_t * asThis, Blob_t * key) {
    assert(asThis != NULL);
    assert(key != NULL);

    List_t * slot = HashMap_findSlot(asThis, key);
    int i = -1;
    {
        KeyValue_t * entry = KeyValue_malloc(key, NULL);
        i = List_search(slot, entry, 0, KeyValue_compare);
        KeyValue_free(entry);
        entry = NULL;
    }
    if (i >= 0) {
        // remove
        KeyValue_t * entry = List_remove(slot, i);
        --asThis->size;
        void * origValue = entry->value;
        KeyValue_free(entry);
        entry = NULL;
        return origValue;
    }
    return NULL;
}

interface void * HashMap_get(HashMap_t * asThis, Blob_t * key) {
    assert(asThis != NULL);
    assert(key != NULL);

    List_t * slot = HashMap_findSlot(asThis, key);
    KeyValue_t * temp = KeyValue_malloc(key, NULL);
    int i = List_search(slot, temp, 0, KeyValue_compare);
    KeyValue_free(temp);
    temp = NULL;
    if (i < 0) {
        return NULL;
    }

    temp = List_get(slot, i);
    return temp->value;
}

interface int HashMap_size(HashMap_t * asThis) {
    assert(asThis != NULL);
    return asThis->size;
}
