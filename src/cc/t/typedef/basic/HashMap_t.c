// TODO this is a unimplemented function
int MapEntry_compare(void *, void *);

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
			MapEntry_t * entry = List_remove(slot, 0);
			MapEntry_drop(entry);
		}
	}

}

interface void HashMap_drop(HashMap_t * asThis) {
	assert(asThis != NULL);
	for (int i = 0; i < asThis->capacity; ++i) {
		List_t * slot = asThis->slots[i];
		while (!List_isEmpty(slot)) {
			MapEntry_t * entry = List_remove(slot, 0);
			MapEntry_drop(entry);
		}
		List_drop(slot);
	}
	mem_drop(asThis);
	asThis = NULL;
}

List_t * HashMap_findSlot(HashMap_t * asThis, Blob_t * key) {
	word hashCode = mem_hash(key->data, key->size);
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
	MapEntry_t * entry = MapEntry_pick(key, value);
	listSearchResult_t * r = List_search(slot, entry, 0, MapEntry_compare);
	if (r != NULL) {
		MapEntry_drop(entry);
		entry = r->node->data;
		void * origValue = entry->value;
		entry->value = value;
		mem_drop(r);
		r = NULL;
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
	listSearchResult_t * r = NULL;
	{
		MapEntry_t * entry = MapEntry_pick(key, NULL);
		r = List_search(slot, entry, 0, MapEntry_compare);
		MapEntry_drop(entry);
		entry = NULL;
	}
	if (r != NULL) {
		// remove
		MapEntry_t * entry = List_remove(slot, r->index);
		--asThis->size;
		void * origValue = entry->value;
		MapEntry_drop(entry);
		entry = NULL;
		mem_drop(r);
		r = NULL;
		return origValue;
	}
	return NULL;
}

interface void * HashMap_get(HashMap_t * asThis, Blob_t * key) {
	assert(asThis != NULL);
	assert(key != NULL);

	List_t * slot = HashMap_findSlot(asThis, key);
	MapEntry_t * temp = MapEntry_pick(key, NULL);
	listSearchResult_t * r = List_search(slot, temp, 0, MapEntry_compare);
	MapEntry_drop(temp);
	temp = NULL;
	if (r == NULL) {
		return NULL;
	} else {
		MapEntry_t * entry =
				List_search(slot, temp, 0, &MapEntry_compare)->node->data;
		return entry->value;
	}
}

interface int HashMap_size(HashMap_t * asThis) {
	assert(asThis != NULL);
	return asThis->size;
}
