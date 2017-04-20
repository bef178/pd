/**
 * 此为简单容器，须在外部事先析构key/value
 */
interface void KeyValue_free(KeyValue_t * asThis) {
    if (asThis == NULL) {
        return;
    }
    asThis->key = NULL;
    asThis->value = NULL;
    mem_drop(asThis);
    asThis = NULL;
}

/**
 * 浅拷贝，不接受空键
 */
interface KeyValue_t * KeyValue_malloc(void * key, void * value) {
    assert(key != NULL);
    KeyValue_t * entry = mem_pick(sizeof(KeyValue_t));
    entry->key = key;
    entry->value = value;
    return entry;
}
