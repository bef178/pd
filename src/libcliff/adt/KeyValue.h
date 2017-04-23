#ifndef _INCLUDED_ADT_KEYVALUE
#define _INCLUDED_ADT_KEYVALUE

interface typedef struct {
    void * key;
    void * value;
} KeyValue;

interface KeyValue * KeyValue_malloc(void * key, void * value);

interface void KeyValue_free(KeyValue * caller);

#endif // _INCLUDED_ADT_KEYVALUE
