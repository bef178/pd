/***************************************
 * predefined.h
 *
 * as supplementary to the language specification
 * fake because it should be embedded in compiler & hardware architecture, just make it work
 *
 *  provides:
 *      int16/int32/int64
 *      byte/word
 */

#ifndef _INCLUDED_PREDEFINED
#define _INCLUDED_PREDEFINED

#include <stdint.h>

// 在编译器内部定义16/32/64位int作为基准类型
typedef int16_t int16;
typedef int32_t int32;
typedef int64_t int64;

// 此处应定义int与字长相等，当然这颠覆了C的常识性概念，暂不取
// #if WORD_SIZE == 32
// typedef int32 int;
// #elif WORD_SIZE == 64
// typedef int64 int;
// #endif

// unsigned这个修饰概念本质上是对相同比特序列的不同解释
// unsigned在运算上的细微差别将带来不必要的复杂性
// unsigned用于内存操纵是合适的
// byte是流的最小单元
// word代表计算机字长，是指针与地址的本质
#ifndef WORD_SIZE
#define WORD_SIZE   __WORDSIZE
// assert WORD_SIZE == sizeof(void *)
#endif
typedef uint8_t byte;
#if WORD_SIZE == 32
typedef uint32_t word;
#elif WORD_SIZE == 64
typedef uint64_t word;
#endif

// 每种类型应该描述自身
// size_t描述了用途而不是类型的特征，弃之

// additional keyword
#define interface
#define package
#define import
#define _type
#define _method

// 一些语言特性
//
// assert -x == ~x + 1;
//
// int8_t c = -1;
// assert (unsigned) c == 255;
// assert (unsigned int) c == 4294967295;

#endif /* _INCLUDED_PREDEFINED */
