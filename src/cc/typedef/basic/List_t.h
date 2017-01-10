#ifndef _INCLUDED_LIST_T
#define _INCLUDED_LIST_T

interface typedef struct {
    int size; // as an accelerator for internal use
    ListEntry_t * head;
} List_t;

interface bool List_isEmpty(List_t * asThis);

bool List_isIndexForAdd(List_t * asThis, int index);

bool List_isIndexForGet(List_t * asThis, int index);

int List_betterIndex(List_t * asThis, int index);

ListHead_t * List_getPrevLink(List_t * asThis, int index);

interface List_t * List_pick();

interface void List_drop(List_t * asThis);

interface bool List_isEmpty(List_t * asThis);

interface int List_size(List_t * asThis);

interface void List_clear(List_t * asThis);

interface void * List_get(List_t * asThis, int index);

interface void * List_set(List_t * asThis, int index, void * data);

interface void List_insert(List_t * asThis, int index, void * data);

interface void * List_remove(List_t * asThis, int index);

interface typedef struct {
    int index;
    ListEntry_t * node;
} ListSearchResult_t;

/**
 * NULL for not found
 */
interface ListSearchResult_t * List_search(List_t * asThis, void * data,
        int startIndex, compare_fp dataCmpr);

// straight insertion sort
interface void List_sort(List_t * asThis, compare_fp dataCmpr);

#endif // _INCLUDED_LIST_T
