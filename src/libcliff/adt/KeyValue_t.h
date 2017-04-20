#ifndef _INCLUDED_KEYVALUE_T
#define _INCLUDED_KEYVALUE_T

interface typedef struct {
    void * key;
    void * value;
} KeyValue_t;

interface void KeyValue_free(KeyValue_t * asThis);

interface KeyValue_t * KeyValue_malloc(void * key, void * value);

#endif // _INCLUDED_KEYVALUE_T
