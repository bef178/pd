#ifndef _INCLUDED_MEM_STR
#define _INCLUDED_MEM_STR

interface void * mem_pick(int size);
interface void mem_drop(void * mem);
interface void mem_reset(void * mem, int size);
interface bool mem_resize(void ** mem, int size);
interface int mem_compare(const byte * m1, const byte * m2, int n);
interface void mem_copy(byte * t, const byte * s, int n);

interface word mem_hash(const void * mem, const int size);
interface word mem_rehash(word hashCode);

interface int str_concatenate(byte * t, const byte * s);
interface int str_compare(const byte * s1, const byte * s2);
interface int str_copy(byte * t, const byte * s);
interface const byte * str_search(const byte * s, const byte c);
interface int str_length(const byte * s);
interface int str_substring(byte * t, const byte * s, int i, int j);

/**
 * trim 'white spaces' from head
 * returns at where the trimmed string @s 'should' start
 */
interface byte * str_trimFore(byte * s);

/**
 * trim 'white spaces' from tail
 * indicate where '\0' should be, instead of write it to string @s
 * returns at where the trimmed string @s 'should' end
 */
interface byte * str_trimHind(byte * s);

interface const byte * str_kmp(const byte * s, const byte * p);

#endif // _INCLUDED_MEM_STR
