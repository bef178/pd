/***************************************
 * 2010-05-12
 */

/**
 *  get nearest utf8 character head from @p
 */
const byte * utf8_glyphHead(const byte * p) {
    while ((*p & 0xC0) == 0x80) {
        ++p;
    }
    return p;
}

bool utf8_isGlyphHead(const byte * p) {
    return (*p & 0xC0) != 0x80;
}

int utf8_glyphSize(const byte * utf8) {
    assert(utf8_isGlyphHead(utf8));
    int size = 0;
    while (getBit(utf8, size) && size < 8) {
        ++size;
    }
    if (size == 1 || size > 6) {
        size = 0;  // invalid utf8 head
    } else if (size == 0) {
        ++size;
    }
    return size;
}

/**
 * convert 1 utf8 character to 1 ucs4 character
 * return the size of this @utf8 character
 */
int utf8_toUcs4(const byte * utf8, int32 * ucs4) {
    assert(utf8 != NULL && ucs4 != NULL);

    int size = utf8_glyphSize(utf8);
    switch (size) {
        case 0:
            break;
        case 1:
            *ucs4 = *utf8;
            break;
        default: {
            int32 ch = 0;
            // parse the head
            for (int i = size + 1; i < 8; ++i) {
                ch <<= 1;
                if (getBit(utf8, i)) {
                    ch |= 1;
                }
            }
            // parse the rest
            for (int i = size - 1; i > 0; --i) {
                ++utf8;
                ch <<= 6;
                ch |= *utf8 & 0x3F;
            }
            *ucs4 = ch;
            break;
        }
    }
    return size;
}
