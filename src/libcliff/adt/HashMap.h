#ifndef _INCLUDED_ADT_HASHMAP
#define _INCLUDED_ADT_HASHMAP

typedef struct hash_map HashMap;

interface HashMap * HashMap_malloc(int capacity, compare_f * compare, hash_f * hash);

interface void HashMap_free(HashMap * caller);

interface void HashMap_clear(HashMap * caller);

interface int HashMap_size(HashMap * caller);

interface void * HashMap_get(HashMap * caller, void * key);

interface void * HashMap_put(HashMap * caller, void * key, void * value);

interface void * HashMap_remove(HashMap * caller, void * key);

#endif // _INCLUDED_ADT_HASHMAP
