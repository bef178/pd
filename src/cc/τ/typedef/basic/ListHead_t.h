/**
 * interface header for ListHead_t
 *
 * TODO i should compose a very fore-most processor:
 *   1. bring interface macro, interface struct, interface function declaration, relevant comment to a same name .h file
 *   2. include that .h after macro & struct in .c file
 *   3. generate in-file function declaration
 */

#ifndef _INCLUDED_LISTHEAD_T
#define _INCLUDED_LISTHEAD_T

#define MEMBER_OFFSET(structName__, memberName__)	\
 	(word) &(((structName__ *) 0)->memberName__)

// find the object addr via a member addr
#define STRUCT_ENTRY(structName__, memberName__, memberAddr__)	\
	(structName__ *)((word) (memberAddr__) - MEMBER_OFFSET(structName__, memberName__))

#define ListHead_containerOf(asThis__, memberNameOfThisInStruct__, structName__)	\
	STRUCT_ENTRY(structName__, memberNameOfThisInStruct__, asThis__)

typedef struct ListHead {
	struct ListHead * next;
	struct ListHead * prev;
} ListHead_t;

interface ListHead_t * ListHead_pick();

// self-express circular doubly linked list
interface void ListHead_init(ListHead_t * asThis);

interface void ListHead_drop(ListHead_t * asThis);

/**
 * simply connect the two nodes, both joints should be NULL
 */
interface void ListHead_attachNext(ListHead_t * asThis, ListHead_t * asNext);

/**
 * simply disconnect the two nodes, both joints will be NULL
 * return the detached node
 */
interface ListHead_t * ListHead_detachNext(ListHead_t * asThis);

/**
 * the guest node should be clean
 */
interface void ListHead_insertNext(ListHead_t * asThis, ListHead_t * asNext);

interface ListHead_t * ListHead_removeNext(ListHead_t * asThis);

/**
 * the guest node will be clean
 * note if asThis is the only element, the self-express list will be gone
 */
interface void ListHead_remove(ListHead_t * asThis);

#endif // _INCLUDED_LISTHEAD_T
