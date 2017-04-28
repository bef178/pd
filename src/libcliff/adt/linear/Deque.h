#ifndef _INCLUDED_ADT_DEQUE
#define _INCLUDED_ADT_DEQUE

typedef struct deque Deque;

Deque * Deque_malloc(int capacity);

void Deque_free(Deque * caller);

int Dque_size(Deque * caller);

bool Deque_isEmpty(Deque * caller);

bool Deque_isFull(Deque * caller);

void Deque_clear(Deque * caller);

int Deque_capacity(Deque * caller);

void Deque_pushHead(Deque * caller, void * d);

void Deque_pushTail(Deque * caller, void * d);

void * Deque_pullHead(Deque * caller);

void * Deque_pullTail(Deque * caller);

void * Deque_peekHead(Deque * caller);

void * Deque_peekTail(Deque * caller);

void Deque_push(Deque * caller, void * d);

void * Deque_pull(Deque * caller);

void * Deque_peek(Deque * caller);

void Deque_enqueue(Deque * caller, void * d);

void * Deque_dequeue(Deque * caller);

#endif // _INCLUDED_ADT_DEQUE
