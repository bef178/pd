#ifndef _INCLUDED_MEM
#define _INCLUDED_MEM

interface int mem_compare(const byte * m1, const byte * m2, int n);
interface void mem_copy(byte * t, const byte * s, int n);
interface void mem_drop(void * mem);
interface void * mem_pick(int size);
interface void mem_reset(void * mem, int size);
interface bool mem_resize(void ** mem, int size);

interface word mem_hash(const void * mem, const int size);
interface word mem_rehash(word hashCode);

#endif // _INCLUDED_MEM
