#include <memory.h>

// TODO test this threshold
static const int SIZE_THRESHOLD = 32;

static int mem_compareLarge(const byte * m1, const byte * m2, int n) {
	const word * w1 = (const word *) m1;
	const word * w2 = (const word *) m2;
	while (n >= sizeof(word) && *w1 == *w2) {
		++w1;
		++w2;
		n -= sizeof(word);
	}

	if (n >= sizeof(word)) {
		return *w1 < *w2 ? -1 : 1;
	}

	m1 = (const byte *) w1;
	m2 = (const byte *) w2;
	while (n != 0 && *m1 == *m2) {
		++m1;
		++m2;
		--n;
	}
	return n == 0 ? 0 : (*m1 < *m2 ? -1 : 1);
}

interface int mem_compare(const byte * m1, const byte * m2, int n) {
	assert(m1 != NULL);
	assert(m2 != NULL);

	if (n < SIZE_THRESHOLD) {
		while (n != 0 && *m1 == *m2) {
			++m1;
			++m2;
			--n;
		}
		return n == 0 ? 0 : (*m1 < *m2 ? -1 : 1);
	} else {
		return mem_compareLarge(m1, m2, n);
	}

}

static void mem_copyLarge(byte * t, const byte * s, int n) {
	const word * sWord = (const word *) s;
	word * tWord = (word *) t;
	while (n >= sizeof(word)) {
		*tWord++ = *sWord++;
		n -= sizeof(word);
	}

	s = (const byte *) sWord;
	t = (byte *) tWord;
	while (n-- > 0) {
		*t++ = *s++;
	}
}

interface void mem_copy(byte * t, const byte * s, int n) {
	assert(t != NULL);
	assert(s != NULL);

	if (n < SIZE_THRESHOLD) {
		while (n-- > 0) {
			*t++ = *s++;
		}
	} else {
		mem_copyLarge(t, s, n);
	}
}

interface void mem_drop(void * mem) {
	if (mem != NULL) {
		free(mem);
		mem = NULL;
	}
}

interface void * mem_pick(int size) {
	assert(size >= 0);
	void * mem = calloc(1, size);
	assert(mem != NULL);
	return mem;
}

interface void mem_reset(void * mem, int size) {
	assert(mem != NULL);
	assert(size > 0);
	memset(mem, 0, size);
}

interface bool mem_resize(void ** mem, int size) {
	assert(size > 0);
	void * p = realloc(*mem, size);
	if (p != NULL) {
		*mem = p;
		return 1;
	}
	return 0;
}

// ---- hash stuff

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

interface word mem_hash(const void * mem, const int size) {
	assert(mem != NULL && size >= 0);
	if (size == 0) {
		return 0;
	}
	const byte * bytes = (const byte *) mem;
	return hash_djb(bytes, size);
}

/**
 * a supplemental hash function to defends against poor quality hash function
 * this is from jdk6
 */
interface word mem_rehash(word hashCode) {
	hashCode ^= (hashCode >> 20) ^ (hashCode >> 12);
	return hashCode ^ (hashCode >> 7) ^ (hashCode >> 4);
}
