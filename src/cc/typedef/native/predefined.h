/***************************************
 * predefined.h
 *
 * as supplementary to the language specification
 * fake because it should be embedded in compiler & hardware architecture, just make it work
 *
 * provides:
 *   int8/int16/int32/int64
 *   byte
 *   word
 *   int
 *   uint
 */

#ifndef _INCLUDED_PREDEFINED
#define _INCLUDED_PREDEFINED

#include <stdint.h>

// 在编译器内部定义8/16/32/64位int作为基准类型，不应该有short/long
typedef int8_t int8;
typedef int16_t int16;
typedef int32_t int32;
typedef int64_t int64;

// 亦不用unsigned这个概念，本质上是对相同比特序列的不同解释
typedef uint8_t uint8;
typedef uint16_t uint16;
typedef uint32_t uint32;
typedef uint64_t uint64;

// 相对于char，内存操纵用byte更合适
typedef uint8_t byte;

// word是源于汇编的概念，代表计算机字长，无符号，是指针与地址的本质
# if __WORDSIZE == 32
typedef uint32_t word;
# elif __WORDSIZE == 64
typedef uint64_t word;
# endif

// 此处定义int/uint应与字长相等
// 当然这改变了C的常识性概念，暂不取
// size_t是一个奇怪的类型，字面上描述了用途而不是类型的特征，弃之
typedef unsigned int uint;

// 故，最终给出的类型 u?int(8|16|32|64)?, byte, word

// additional keyword
#define interface
#define package
#define import
#define _type
#define _method

#endif /* _INCLUDED_PREDEFINED */
