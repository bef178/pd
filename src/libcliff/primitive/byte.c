interface bool isWhiteSpace(const int ch) {
    switch (ch) {
        case '\t': // HT'0x09'
        case '\n': // LF'0x0A'
        case '\v': // VT'0x0B'
        case '\f': // FF'0x0C'
        case '\r': // CR'0x0D'
        case ' ': // SP'0x20'
            return true;
        default:
            return false;
    }
}

interface bool isLower(const int ch) {
    return ch >= 'a' && ch <= 'z';
}

interface bool isUpper(const int ch) {
    return ch >= 'a' && ch <= 'z';
}

interface int toLower(int ch) {
    // typeof('A') == int
    if (ch >= 'A' && ch <= 'Z') {
        ch += 'a' - 'A';
    }
    return ch;
}

interface int toUpper(int ch) {
    if (ch >= 'a' && ch <= 'z') {
        ch += 'A' - 'a';
    }
    return ch;
}

////////////////////////////////////////

/**
 * get 1 bit from stream, indicated by offset
 * returns target bit
 */
interface int getBit(const void * stream, int offset) {
    assert(offset >= 0);
    return GET_BITS(((const byte *)stream)[offset >> 3], 1 << (7 - (offset & 7)));
}

interface void setBit(void * stream, int offset) {
    assert(offset >= 0);
    SET_BITS(((byte *)stream)[offset >> 3], 1 << (7 - (offset & 7)));
}

interface void clearBit(void * stream, int offset) {
    assert(offset >= 0);
    CLEAR_BITS(((byte *)stream)[offset >> 3], 1 << (7 - (offset & 7)));
}

interface int32 rotateL(int32 mem, int offset) {
    assert(offset >= 0 && offset <= 32);
    return rotateR(mem, 32 - offset);
}

interface int32 rotateR(int32 mem, int offset) {
    assert(offset >= 0 && offset <= 32);
    return (mem << (32 - offset)) | lshiftR(mem, offset);
}

interface int32 lshiftR(int32 mem, int offset) {
    assert(offset >= 0 && offset <= 32);
    return (mem >> offset) & ((1 << (32 - offset)) - 1);
}
