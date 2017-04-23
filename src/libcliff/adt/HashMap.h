#ifndef _INCLUDED_ADT_HASHMAP
#define _INCLUDED_ADT_HASHMAP

typedef struct HashMap {
    int capacity; // actually numSlots
    int size;
    List * slots[0];
} HashMap;

interface HashMap * HashMap_malloc(int capacity);

interface void HashMap_free(HashMap * caller);

interface void HashMap_clear(HashMap * caller);

interface void * HashMap_get(HashMap * caller, Blob * key);

interface void * HashMap_put(HashMap * caller, Blob * key, void * value);

interface void * HashMap_remove(HashMap * caller, Blob * key);

interface int HashMap_size(HashMap * caller);

#endif // _INCLUDED_ADT_HASHMAP
