#ifndef _INCLUDED_BLOB_T
#define _INCLUDED_BLOB_T

// 已经 immutable，故可以暴露内部
// 是结构而非对象，故应该暴露内部
interface typedef struct {
    const int n;
    const byte a[0];
} Blob_t;

interface int Blob_compare(const Blob_t * asThis, const Blob_t * blob);

interface void Blob_drop(Blob_t * asThis);

interface Blob_t * Blob_pick(const byte * a, const int n);

#endif // _INCLUDED_BLOB_T
