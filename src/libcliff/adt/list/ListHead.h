/**
 * ListHead is really not a good name.
 * It is actually "link head"; but linux kernel uses list_head so it inherts.
 *
 * TODO i should compose a very fore-most processor:
 *  1. create a .h file
 *      x. include-guard
 *      x. interface macro/typedef
 *      x. forward declaration of interface struct/variable/function
 *      x. relevant comments
 *  2. modify current .c file
 *      x. remove interface macro/typedef
 *      x. add forward declaration of non-interface function
 */

#ifndef _INCLUDED_ADT_LISTHEAD
#define _INCLUDED_ADT_LISTHEAD

#define MEMBER_OFFSET(structName__, memberName__)   \
    (word) &(((structName__ *) 0)->memberName__)

#define ListHead_getContainer(memberAddr__, structName__, memberName__)    \
    (structName__ *)((word) (memberAddr__) - MEMBER_OFFSET(structName__, memberName__))

typedef struct list_head ListHead;

ListHead * ListHead_malloc();

void ListHead_free(ListHead * caller);

void ListHead_insertNext(ListHead * caller, ListHead * futureNext);

ListHead * ListHead_removeNext(ListHead * caller);

ListHead * ListHead_offset(ListHead * caller, int n);

#endif // _INCLUDED_ADT_LISTHEAD
