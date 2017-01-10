#ifndef _INCLUDED_LISTENTRY_T
#define _INCLUDED_LISTENTRY_T

#include "ListHead_t.h"

typedef struct {
    ListHead_t * listHead;
    void * data;
} ListEntry_t;

ListEntry_t * ListEntry_pick(void * data);

void ListEntry_drop(ListEntry_t * asThis);

ListEntry_t * ListEntry_removeNext(ListEntry_t * asThis);

ListEntry_t * ListEntry_getByLink(ListHead_t * link);

#endif // _INCLUDED_LISTENTRY_T
