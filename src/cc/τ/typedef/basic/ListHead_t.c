/**
 * ListHead_t.c
 */

interface ListHead_t * ListHead_pick() {
	return (ListHead_t *) mem_pick(sizeof(ListHead_t));
}

// self-express circular doubly linked list
interface void ListHead_init(ListHead_t * asThis) {
	asThis->next = asThis;
	asThis->prev = asThis;
}

interface void ListHead_drop(ListHead_t * asThis) {
	if (asThis == NULL) {
		return;
	}
	mem_drop(asThis);
	asThis = NULL;
}

/**
 * simply connect the two nodes, both joints should be NULL
 */
interface void ListHead_attachNext(ListHead_t * asThis, ListHead_t * asNext) {
	assert(asThis != NULL && asThis->next == NULL);
	if (asNext != NULL) {
		assert(asNext->prev == NULL);
		asThis->next = asNext;
		asNext->prev = asThis;
	}
}

/**
 * simply disconnect the two nodes, both joints will be NULL
 * return the detached node
 */
interface ListHead_t * ListHead_detachNext(ListHead_t * asThis) {
	assert(asThis != NULL);
	ListHead_t * asNext = asThis->next;
	if (asNext != NULL) {
		asThis->next = NULL;
		asNext->prev = NULL;
	}
	return asNext;
}

/**
 * the guest node should be clean
 */
interface void ListHead_insertNext(ListHead_t * asThis, ListHead_t * asNext) {
	assert(asThis != NULL);
	assert(asNext != NULL && asNext->next == NULL && asNext->prev == NULL);

	asNext->next = asThis->next;
	asNext->prev = asThis;

	if (asNext->next != NULL) {
		asNext->next->prev = asNext;
	}
	asThis->next = asNext;
}

interface ListHead_t * ListHead_removeNext(ListHead_t * asThis) {
	assert(asThis != NULL);
	ListHead_t * asNext = asThis->next;
	if (asNext != NULL) {
		asNext->next->prev = asThis;
		asThis->next = asNext->next;
		asNext->next = NULL;
		asNext->prev = NULL;
	}
	return asNext;
}

/**
 * the guest node will be clean
 * note if asThis is the only element, the self-express list will be gone
 */
interface void ListHead_remove(ListHead_t * asThis) {
	assert(asThis != NULL);

	if (asThis->prev != NULL) {
		asThis->prev->next = asThis->next;
	}
	if (asThis->next != NULL) {
		asThis->next->prev = asThis->prev;
	}
	asThis->next = NULL;
	asThis->prev = NULL;
}
