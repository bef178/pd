#ifndef _INCLUDED_HASH
#define _INCLUDED_HASH

interface word hash(const byte * bytes, const int size);

interface word rehash(word hashCode);

interface word hash_bkdr(const byte * bytes, const int size);

interface word hash_djb(const byte * bytes, const int size);

#endif // _INCLUDED_HASH
