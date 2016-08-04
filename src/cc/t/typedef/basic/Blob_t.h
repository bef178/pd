#ifndef _INCLUDED_BLOB_T
#define _INCLUDED_BLOB_T

// 因为已经immutable了，所以可以暴露内部结构
// 因为是数据结构而不是对象，所以应该暴露内部结构
interface typedef struct Blob {
	const int size;
	const byte data[0];
} Blob_t;

interface int Blob_compare(const Blob_t * asThis, const Blob_t * blob);

interface void Blob_drop(Blob_t * asThis);

interface Blob_t * Blob_pick(const byte * data, const int size);

#endif // _INCLUDED_BLOB_T
