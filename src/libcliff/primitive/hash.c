interface word hash(const byte * bytes, const int size) {
    assert(bytes != NULL && size >= 0);
    return size == 0 ? 0 : hash_djb(bytes, size);
}

/**
 * a supplemental hash function to defends against poor quality hash function
 * from jdk6
 */
interface word rehash(word hashCode) {
    hashCode ^= (hashCode >> 20) ^ (hashCode >> 12);
    return hashCode ^ (hashCode >> 7) ^ (hashCode >> 4);
}

interface word hash_bkdr(const byte * bytes, const int size) {
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

interface word hash_djb(const byte * bytes, const int size) {
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
