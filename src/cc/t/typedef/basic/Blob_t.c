/**
 * immutable 'Blob_t'
 */

// 因为已经immutable了，所以可以暴露内部结构
// 因为是数据结构而不是对象，所以应该暴露内部结构

interface int Blob_compare(const Blob_t * asThis, const Blob_t * blob) {
	assert(asThis != NULL);
	if (blob == NULL) {
		return 1;
	}
	if (asThis == blob) {
		return 0;
	}
	int compare_result = mem_compare(asThis->data, blob->data, (asThis->size <= blob->size ? asThis->size : blob->size));
	if (compare_result != 0) {
		return compare_result;
	}
	if (asThis->size != blob->size) {
		// do not directly minus in case it turns to unsigned
		return asThis->size < blob->size ? -1 : 1;
	}
	return 0;
}

interface void Blob_drop(Blob_t * asThis) {
	if (asThis == NULL) {
		return;
	}
	mem_drop(asThis);
	asThis = NULL;
}

interface Blob_t * Blob_pick(const byte * data, const int size) {
	assert(size >= 0);
	// ugly but enables the immutability of Blob_t
	void * mem = mem_pick(sizeof(Blob_t) + size);

	word memberOffset = (word) &(((Blob_t *) 0)->size);
	*(word *) ((word) mem + memberOffset) = size;

	memberOffset = (word) &(((Blob_t *) 0)->data);
	mem_copy((void *) ((word) mem + memberOffset), data, size); // must deep-copy
	return (Blob_t *) mem;
}
