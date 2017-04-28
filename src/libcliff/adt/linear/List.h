#ifndef _INCLUDED_ADT_LIST
#define _INCLUDED_ADT_LIST

typedef struct list List;

List * List_malloc();

void List_free(List * caller);

void List_clear(List * caller);

int List_size(List * caller);

bool List_isEmpty(List * caller);

void * List_get(List * caller, int index);

void * List_set(List * caller, int index, void * data);

void List_insert(List * caller, int index, void * data);

void * List_remove(List * caller, int index);

int List_indexOf(List * caller, int start, void * data, compare_f * compare);

#endif // _INCLUDED_ADT_LIST
