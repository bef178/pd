/**
 * 浅拷贝，不接受空键
 */
interface KeyValue * KeyValue_malloc(void * key, void * value) {
    assert(key != NULL);
    KeyValue * entry = mem_pick(sizeof(KeyValue));
    entry->key = key;
    entry->value = value;
    return entry;
}

/**
 * 此为简单容器，须在外部事先析构key/value
 */
interface void KeyValue_free(KeyValue * caller) {
    caller->key = NULL;
    caller->value = NULL;
    mem_drop(caller);
}
