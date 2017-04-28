#ifndef _INCLUDED_ADT_LISTHEAD
#define _INCLUDED_ADT_LISTHEAD

typedef struct list_head ListHead;

ListHead * ListHead_malloc();

void ListHead_free(ListHead * caller);

void ListHead_insertNext(ListHead * caller, ListHead * futureNext);

ListHead * ListHead_removeNext(ListHead * caller);

ListHead * ListHead_offset(ListHead * caller, int n);

#endif // _INCLUDED_ADT_LISTHEAD
