#ifndef _INCLUDED_ADT_LIST
#define _INCLUDED_ADT_LIST

/**
 * interface header for ListHead
 *
 * TODO i should compose a very fore-most processor:
 *   1. bring interface macro, interface struct, interface function declaration, relevant comment to a same name .h file
 *   2. include that .h after macro & struct in .c file
 *   3. generate in-file function declaration
 */

// ---------------------------------------------------------

#define MEMBER_OFFSET(structName__, memberName__)   \
    (word) &(((structName__ *) 0)->memberName__)

// find the object addr via a member addr
#define STRUCT_ENTRY(structName__, memberName__, memberAddr__)  \
    (structName__ *)((word) (memberAddr__) - MEMBER_OFFSET(structName__, memberName__))

#define ListHead_containerOf(asThis__, memberNameOfThisInStruct__, structName__)    \
    STRUCT_ENTRY(structName__, memberNameOfThisInStruct__, asThis__)

typedef struct list_head {
    struct list_head * next;
    struct list_head * prev;
} ListHead;

interface ListHead * ListHead_malloc();

interface void ListHead_free(ListHead * caller);

interface void ListHead_init(ListHead * caller);

interface void ListHead_attachNext(ListHead * caller, ListHead * futureNext);

interface ListHead * ListHead_detachNext(ListHead * caller);

interface void ListHead_insertNext(ListHead * caller, ListHead * futureNext);

interface ListHead * ListHead_removeNext(ListHead * caller);

interface void ListHead_remove(ListHead * caller);

// ---------------------------------------------------------

typedef struct {
    ListHead * listHead;
    void * data;
} ListEntry;

ListEntry * ListEntry_malloc(void * data);

void ListEntry_free(ListEntry * caller);

ListEntry * ListEntry_removeNext(ListEntry * caller);

ListEntry * ListEntry_getByListHead(ListHead * p);

// ---------------------------------------------------------

interface typedef struct {
    int size; // as an accelerator for internal use
    ListEntry * head;
} List;

interface List * List_malloc();

interface void List_free(List * caller);

interface void List_clear(List * caller);

interface void * List_get(List * caller, int index);

interface int List_indexOf(List * caller, void * data, int start, compare_fp compare);

interface void List_insert(List * caller, int index, void * data);

interface bool List_isEmpty(List * caller);

interface void * List_remove(List * caller, int index);

interface void * List_set(List * caller, int index, void * data);

interface int List_size(List * caller);

interface void List_sort(List * caller, compare_fp compare);

#endif // _INCLUDED_ADT_LIST
