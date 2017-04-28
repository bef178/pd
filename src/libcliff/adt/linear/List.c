/**
 * cyclic doubly linked list with head node
 * accepting only non-NULL value
 *
 * List强调位序，数据的先后位置是有意义的，因而其主要操作都依赖于位序
 */

typedef struct {
    ListHead * link;
    void * data;
} Node;

Node * Node_malloc(void * data) {
    Node * p = mem_pick(sizeof(Node));
    p->link = ListHead_malloc();
    p->data = data;
    return p;
}

void Node_free(Node * caller) {
    ListHead_free(caller->link);
    mem_drop(caller);
}

Node * Node_containerOf(ListHead * p) {
    return GET_CONTAINER(Node, link, p);
}

void Node_insertNext(Node * caller, Node * futureNext) {
    ListHead_insertNext(caller->link, futureNext->link);
}

Node * Node_removeNext(Node * caller) {
    ListHead * p = ListHead_removeNext(caller->link);
    return Node_containerOf(p);
}

Node * Node_offset(Node * caller, int n) {
    ListHead * p = ListHead_offset(caller->link, n);
    if (p != NULL) {
        return Node_containerOf(p);
    }
    return NULL;
}

// ---------------------------------------------------------

interface typedef struct list {
    int size;
    Node * head;
} List;

int betterIndex(int size, int index) {
    index = index % size;
    if (size > 6) {
        if (index >= 0) {
            if (index > size / 2 + 1) {
                index -= size + 1;
            }
        } else {
            if (index < -size / 2 - 1) {
                index += size + 1;
            }
        }
    }
    return index;
}

interface List * List_malloc() {
    List * p = mem_pick(sizeof(List));
    p->size = 0;
    p->head = Node_malloc(NULL);
    return p;
}

interface void List_free(List * caller) {
    assert(List_isEmpty(caller));
    Node_free(caller->head);
    caller->head = NULL;
    mem_drop(caller);
}

interface void List_clear(List * caller) {
    while (!List_isEmpty(caller)) {
        Node * p = Node_removeNext(caller->head);
        --caller->size;
        Node_free(p);
    }
}

interface int List_size(List * caller) {
    return caller->size;
}

interface bool List_isEmpty(List * caller) {
    return List_size(caller) == 0;
}

interface void * List_get(List * caller, int index) {
    index = betterIndex(caller->size, index);
    index = index >= 0 ? index + 1 : index;
    return Node_offset(caller->head, index)->data;
}

interface void * List_set(List * caller, int index, void * data) {
    assert(data != NULL);
    index = betterIndex(caller->size, index);
    index = index >= 0 ? index + 1 : index;
    Node * p = Node_offset(caller->head, index);
    void * old = p->data;
    p->data = data;
    return old;
}

interface void List_insert(List * caller, int index, void * data) {
    assert(data != NULL);
    index = betterIndex(caller->size, index);
    Node * p = Node_offset(caller->head, index);
    Node * futureNext = Node_malloc(data);
    Node_insertNext(p, futureNext);
    ++caller->size;
}

interface void * List_remove(List * caller, int index) {
    index = betterIndex(caller->size, index);
    Node * p = Node_offset(caller->head, index);
    p = Node_removeNext(p);
    --caller->size;
    void * data = p->data;
    Node_free(p);
    return data;
}

/**
 * negative for not found
 */
interface int List_indexOf(List * caller, int start, void * data, compare_f * compare) {
    start = betterIndex(caller->size, start);
    start = start >= 0 ? start + 1 : start;
    Node * p = Node_offset(caller->head, start);
    while (p != NULL && p != caller->head) {
        if (compare(p->data, data) == 0) {
            return start;
        }
        p = Node_offset(p, 1);
        ++start;
    }
    return -1;
}
