#ifndef _INCLUDE_BIT
#define _INCLUDE_BIT

#define GET_BITS(flags, f)	((flags) & (f))
#define SET_BITS(flags, f)	do { (flags) |= (f); } while(0)
#define CLEAR_BITS(flags, f)	do { (flags) &= ~(f); } while(0)

/**
 * get 1 bit from @stream, indicated by @offset
 * returns target bit
 */
bool bit_get(const void * stream, int offset);

void bit_set(void * stream, int offset);

void bit_clear(void * stream, int offset);

uint32 bit_rotl32(uint32 mem, int offset);

#endif // _INCLUDE_BIT
