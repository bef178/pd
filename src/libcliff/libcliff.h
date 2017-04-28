#ifndef _INCLUDED_LIBCLIFF
#define _INCLUDED_LIBCLIFF

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>

#include "predefined.h"

#ifndef NULL
#define NULL ((void *)0)
#endif

#define null NULL

// ISO/IEC 9899:1999
typedef _Bool bool;
#define false   (0)
#define true    (!!1)

#define EOS     ('\0')

#define MEMBER_OFFSET(structName__, memberName__)   \
    (word) &(((structName__ *) 0)->memberName__)

#define GET_CONTAINER(memberAddr__, structName__, memberName__)    \
    (structName__ *)((word) (memberAddr__) - MEMBER_OFFSET(structName__, memberName__))

// 很多容器需要将相应的操作传递给容器内对象，因而需要传入对象的回调函数。这些回调函数相当于'接口'。若相应的函数指针为null，则不操作。
// 结构构造时需要不同的参数，因此不能定义pick接口
typedef int (* compare_fp)(void *, void *);

#endif /* _INCLUDED_LIBCLIFF */
