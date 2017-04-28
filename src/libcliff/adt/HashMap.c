interface typedef struct hash_map {
    const int capacity;
    int size;
    compare_f * const key_compare;
    hash_f * const key_hash;
    List * slots[0];
} HashMap;

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

List * HashMap_findSlot(HashMap * caller, void * key) {
    word hashCode = caller->key_hash(key, sizeof(void *));
    hashCode = mem_rehash(hashCode) & (caller->capacity - 1);
    return caller->slots[hashCode];
}

int defKeyCompare(void * key1, void * key2) {
    return key1 - key2;
}

word defKeyHash(void * key) {
}

interface HashMap * HashMap_malloc(int capacity, compare_f * key_compare, hash_f * key_hash) {
    assert(capacity > 0);
    capacity = HashMap_alignCapacity(capacity);
    HashMap * p = mem_pick(sizeof(HashMap) + capacity * sizeof(List *));
    MEMBER_SET(HashMap, p, int, capacity, capacity);
    if (key_compare == NULL) {
        key_compare = &defKeyCompare;
    }
    MEMBER_SET(HashMap, p, compare_f *, key_compare, key_compare);
    if (key_hash == NULL) {
        key_hash = &mem_hash;
    }
    MEMBER_SET(HashMap, p, hash_f *, key_hash, key_hash);
    for (int i = 0; i < capacity; ++i) {
        p->slots[i] = List_malloc();
    }
    return p;
}

interface void HashMap_free(HashMap * caller) {
    HashMap_clear(caller);
    for (int i = 0; i < caller->capacity; ++i) {
        List_free(caller->slots[i]);
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

interface int HashMap_size(HashMap * caller) {
    return caller->size;
}

interface void * HashMap_get(HashMap * caller, void * key) {
    assert(key != NULL);
    List * slot = HashMap_findSlot(caller, key);
    for (int i = 0; i < List_size(slot); i++) {
        KeyValue * entry = List_get(slot, i);
        if (caller->key_compare(entry->key, key) == 0) {
            return entry->value;
        }
    }
    return NULL;
}

interface void * HashMap_put(HashMap * caller, void * key, void * value) {
    assert(key != NULL);
    assert(value != NULL);
    List * slot = HashMap_findSlot(caller, key);
    for (int i = 0; i < List_size(slot); i++) {
        KeyValue * entry = List_get(slot, i);
        if (caller->key_compare(entry->key, key) == 0) {
            void * old = entry->value;
            entry->value = old;
            return old;
        }
    }
    List_insert(slot, -1, KeyValue_malloc(key, value));
    ++caller->size;
    return NULL;
}

interface void * HashMap_remove(HashMap * caller, void * key) {
    assert(key != NULL);
    List * slot = HashMap_findSlot(caller, key);
    for (int i = 0; i < List_size(slot); i++) {
        KeyValue * entry = List_get(slot, i);
        if (caller->key_compare(entry->key, key) == 0) {
            void * old = entry->value;
            KeyValue_free(entry);
            List_remove(slot, i);
            return old;
        }
    }
    return NULL;
}
