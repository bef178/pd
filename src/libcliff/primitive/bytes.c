/***************************************
 * byte-stream manipulator
 * 串与流意义不同，但经常同时出现，合并于一个文件中
 */

interface void * mem_pick(int size) {
    assert(size >= 0);
    void * p = calloc(1, size);
    assert(p != NULL);
    return p;
}

interface void mem_drop(void * p) {
    if (p != NULL) {
        free(p);
    }
}

interface void mem_reset(void * p, int size) {
    assert(p != NULL);
    assert(size > 0);
    byte * q = (byte *)p;
    while (size--) {
        *q++ = 0;
    }
}

interface bool mem_resize(void ** pp, int size) {
    assert(size > 0);
    void * q = realloc(*pp, size);
    if (q != NULL) {
        *pp = q;
        return true;
    }
    return false;
}

interface int mem_compare(const byte * p1, const byte * p2, int n) {
    assert(p1 != NULL);
    assert(p2 != NULL);
    assert(n >= 0);

    const word * w1 = (const word *) p1;
    const word * w2 = (const word *) p2;
    while (n >= sizeof(word)) {
        if (*w1 != *w2) {
            return *w1 < *w2 ? -1 : 1;
        }
        ++w1;
        ++w2;
        n -= sizeof(word);
    }

    p1 = (const byte *) w1;
    p2 = (const byte *) w2;
    while (n > 0 && *p1 == *p2) {
        ++p1;
        ++p2;
        --n;
    }
    return n == 0 ? 0 : (*p1 < *p2 ? -1 : 1);
}

interface void mem_copy(byte * t, const byte * s, int n) {
    assert(t != NULL);
    assert(s != NULL);
    assert(n >= 0);
    // XXX fail in case of overlapping

    const word * src = (const word *) s;
    word * dst = (word *) t;
    while (n >= sizeof(word)) {
        *dst++ = *src++;
        n -= sizeof(word);
    }

    s = (const byte *) src;
    t = (byte *) dst;
    while (n-- > 0) {
        *t++ = *s++;
    }
}

/***************************************
 *  hash stuff
 */

static word hash_bkdr(const byte * bytes, const int size) {
    // BKDR hash seed: 31, 131, 1313, 13131, 131313, ...
    // static const int SEED = 31;

    word hashCode = 0;

    const word * pow = (const word *) bytes;
    const word * eow = pow + size / sizeof(word);
    while (pow < eow) {
        hashCode = (hashCode << 5) - hashCode + *pow++;
    }

    const byte * pob = (const byte *) eow;
    const byte * eob = bytes + size;
    while (pob < eob) {
        hashCode = (hashCode << 5) - hashCode + *pob++;
    }

    return hashCode;
}

static word hash_djb(const byte * bytes, const int size) {
    // DJB hash seed: 33
    // static const int SEED = 33;

    word hashCode = 5381;

    const word * pow = (const word *) bytes;
    const word * eow = pow + size / sizeof(word);
    while (pow < eow) {
        hashCode = (hashCode << 5) + hashCode + *pow++;
    }

    const byte * pob = (const byte *) eow;
    const byte * eob = bytes + size;
    while (pob < eob) {
        hashCode = (hashCode << 5) + hashCode + *pob++;
    }

    return hashCode;
}

interface word mem_hash(const byte * p, const int size) {
    assert(p != NULL && size >= 0);
    if (size == 0) {
        return 0;
    }
    return hash_djb(p, size);
}

/**
 * a supplemental hash function to defends against poor quality hash function
 * from jdk6
 */
interface word mem_rehash(word hashCode) {
    hashCode ^= (hashCode >> 20) ^ (hashCode >> 12);
    return hashCode ^ (hashCode >> 7) ^ (hashCode >> 4);
}

/**
 * minimal complete subset of string operations:
 * concatenate, compare, copy, length, substring
 */

interface int str_concatenate(byte * t, const byte * s) {
    assert(t != NULL);
    assert(s != NULL);

    int n = str_length(t);
    t += n;
    while ((*t++ = *s++) != EOS) {
        ++n;
    }
    return n;
}

interface int str_compare(const byte * s1, const byte * s2) {
    if (s1 == s2) {
        return 0;
    }

    if (s1 == NULL) {
        return -1;
    } else if (s2 == NULL) {
        return 1;
    }

    while (*s1 != EOS && *s1 == *s2) {
        ++s1;
        ++s2;
    }
    return *s1 - *s2;
}

interface int str_copy(byte * t, const byte * s) {
    assert(t != NULL);
    assert(s != NULL);
    int n = 0;
    while ((*t++ = *s++) != EOS) {
        ++n;
    }
    return n;
}

interface const byte * str_search(const byte * s, const byte c) {
    assert(s != NULL);
    while (*s != EOS) {
        if (*s == c) {
            return s;
        }
    }
    return NULL;
}

interface int str_length(const byte * s) {
    assert(s != NULL);
    const byte * p = s;
    while (*s != EOS) {
        ++s;
    }
    return (int)(s - p);
}

interface int str_substring(byte * t, const byte * s, int i, int j) {
    assert(s != NULL);
    assert(t != NULL);

    int n = str_length(s);
    if (i < 0) {
        i += n;
    }
    if (j < 0) {
        j += n;
    }
    assert(i >= 0 && i < n);
    assert(j >= i && j < n);

    s += i;
    j -= i;
    int size = 0;
    while (j-- && *s != EOS) {
        *t++ = *s++;
        ++size;
    }
    *t = EOS;
    return size;
}

interface byte * str_trimFore(byte * s) {
    assert(s != NULL);
    while (isWhiteSpace(*s)) {
        ++s;
    }
    return s;
}

interface byte * str_trimHind(byte * s) {
    assert(s != NULL);
    byte * p = s;
    while (*p != EOS) {
        ++p;
    }
    while (--p >= s && isWhiteSpace(*p)) {
        // dummy
    }
    return ++p;
}

/***************************************
 * 2008-01-30
 *
 * KMP algorithm
 */

// i |  0  1  2  3  4  5  6  7  8  |  0  1  2  3  4 |
// p |  a  b  a  a  b  c  a  b  a  |  a  a  a  b  c |
// r | -1  0 -1  1  0  2 -1  0 -1  | -1 -1 -1  2  0 |

static int * str_kmpRevise(const byte * p, const int n) {
    assert(p != NULL && *p != EOS);

    int * r = mem_pick(sizeof(int) * n);
    if (r == NULL) {
        perror("ERR MEM ALLOCATION");
        exit(1);
    }

    int k = -1;
    r[0] = -1;
    for (int i = 1; i < n; i++) {
        if (k != -1 && p[i - 1] != p[k]) {
            k = r[k];
        }
        ++k;
        if (p[i] == p[k]) {
            r[i] = r[k];
        } else {
            r[i] = k;
        }
    }

    return r;
}

interface const byte * str_kmp(const byte * s, const byte * p) {
    assert(s != NULL && p != NULL);

    if (*p == EOS) {
        return s;
    } else if (*s == EOS) {
        return NULL;
    }

    size_t n = str_length(p);
    int * r = str_kmpRevise(p, n);

    int i = 0;
    int j = 0;
    while (s[i] != EOS) {
        if (j == -1 || s[i] == p[j]) {
            ++i;
            ++j;
            if (p[j] == EOS) {
                mem_drop(r);
                return s + i - n;
            }
        } else {
            j = r[j];
        }
    }
    mem_drop(r);
    return NULL;
}
