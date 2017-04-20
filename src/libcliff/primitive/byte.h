#ifndef _INCLUDE_BYTE_BIT
#define _INCLUDE_BYTE_BIT

interface bool isWhiteSpace(const int ch);
interface bool isLower(const int ch);
interface bool isUpper(const int ch);
interface int toLower(int ch);
interface int toUpper(int ch);

#define GET_BITS(flags, f)      ((flags) & (f))
#define SET_BITS(flags, f)      do { (flags) |= (f); } while(0)
#define CLEAR_BITS(flags, f)    do { (flags) &= ~(f); } while(0)

/**
 * get 1 bit from @stream, indicated by @offset
 * returns target bit
 */
interface int getBit(const void * stream, int offset);
interface void setBit(void * stream, int offset);
interface void clearBit(void * stream, int offset);
interface int32 rotateL(int32 mem, int offset);
interface int32 rotateR(int32 mem, int offset);

// logical (compare to arithmetic) shift right
interface int32 lshiftR(int32 mem, int offset);

#endif // _INCLUDE_BYTE_BIT
