interface int Blob_compare(const Blob_t * asThis, const Blob_t * blob) {
    assert(asThis != NULL);
    if (blob == NULL) {
        return 1;
    }
    if (asThis == blob) {
        return 0;
    }
    int result = mem_compare(asThis->a, blob->a, (asThis->n <= blob->n ? asThis->n : blob->n));
    if (result != 0) {
        return result;
    }
    if (asThis->n != blob->n) {
        return asThis->n < blob->n ? -1 : 1;
    }
    return 0;
}

interface void Blob_drop(Blob_t * asThis) {
    if (asThis == NULL) {
        return;
    }
    mem_drop(asThis);
}

interface Blob_t * Blob_pick(const byte * a, const int n) {
    assert(n >= 0);

    // ugly but enables the immutability of Blob_t

    void * mem = mem_pick(sizeof(Blob_t) + n);

    word memberOffset = (word) &(((Blob_t *) 0)->n);
    *(word *) ((word) mem + memberOffset) = n;

    memberOffset = (word) &(((Blob_t *) 0)->a);
    mem_copy((void *) ((word) mem + memberOffset), a, n); // must deep-copy
    return (Blob_t *) mem;
}
