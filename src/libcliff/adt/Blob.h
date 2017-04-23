#ifndef _INCLUDED_ADT_BLOB
#define _INCLUDED_ADT_BLOB

// 已经 immutable，故可以暴露内部
// 是结构而非对象，故应该暴露内部
interface typedef struct {
    const int n;
    const byte a[0];
} Blob;

interface Blob * Blob_malloc(const byte * a, const int n);

interface void Blob_free(Blob * caller);

interface int Blob_compare(const Blob * caller, const Blob * another);

#endif // _INCLUDED_ADT_BLOB
