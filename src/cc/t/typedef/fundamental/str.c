/***************************************
 * i/o string
 * 串与流意义不同，但经常同时出现，合并于一个文件中
 *
 * string-wise
 *
 * minimal complete subset of string operations:
 * concatenate, compare, copy, length, substring
 */

/**
 * return the length of the c-string
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
	assert (i >= 0 && i < n);
	assert (j >= i && j < n);

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

/**
 * trim 'white spaces' from head
 * returns at where the trimmed string @s 'should' start
 */
byte * str_trimFore(byte * s) {
	assert(s != NULL);
	while (byte_isWhiteSpace(*s)) {
		++s;
	}
	return s;
}

/**
 * trim 'white spaces' from tail
 * indicate where '\0' should be, instead of write it to string @s
 * returns at where the trimmed string @s 'should' end
 */
byte * str_trimHind(byte * s) {
	assert(s != NULL);
	byte * p = s;
	while (*p != EOS) {
		++p;
	}
	while (--p >= s && byte_isWhiteSpace(*p)) {
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
