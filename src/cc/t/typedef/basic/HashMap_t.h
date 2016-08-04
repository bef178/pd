#ifndef _INCLUDED_HASHMAP_T
#define _INCLUDED_HASHMAP_T

typedef struct HashMap {
	int capacity; // actually numSlots
	int size;
	List_t * slots[0];
} HashMap_t;

interface HashMap_t * HashMap_pick(int capacity);

interface void HashMap_clear(HashMap_t * asThis);

interface void HashMap_drop(HashMap_t * asThis);

List_t * HashMap_findSlot(HashMap_t * asThis, Blob_t * key);

int HashMap_alignCapacity(int requiredCapacity);

/**
 * return the replaced v if exists
 */
interface void * HashMap_put(HashMap_t * asThis, Blob_t * key, void * value);

interface void * HashMap_remove(HashMap_t * asThis, Blob_t * key);

interface void * HashMap_get(HashMap_t * asThis, Blob_t * key);

interface int HashMap_size(HashMap_t * asThis);

#endif // _INCLUDED_HASHMAP_T
