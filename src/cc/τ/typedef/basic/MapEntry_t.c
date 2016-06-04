/**
 * MapEntry_t.c
 *
 * 键-值对。不接受空键。
 */

// 此为简单容器，须在外部事先析构key/value
interface void MapEntry_drop(MapEntry_t * asThis) {
	if (asThis == NULL) {
		return;
	}
	asThis->key = NULL;
	asThis->value = NULL;
	mem_drop(asThis);
	asThis = NULL;
}

// 浅拷贝
interface MapEntry_t * MapEntry_pick(void * key, void * value) {
	assert(key != NULL);
	MapEntry_t * entry = mem_pick(sizeof(MapEntry_t));
	entry->key = key;
	entry->value = value;
	return entry;
}
