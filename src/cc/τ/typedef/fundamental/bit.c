/**
 * get 1 bit from @stream, indicated by @offset
 * returns target bit
 */
bool bit_get(const void * stream, int offset) {
	assert(offset >= 0);
	return GET_BITS(((const byte *)stream)[offset >> 3], 1 << (7 - (offset & 7)));
}

void bit_set(void * stream, int offset) {
	assert(offset >= 0);
	SET_BITS(((byte *)stream)[offset >> 3], 1 << (7 - (offset & 7)));
}

void bit_clear(void * stream, int offset) {
	assert(offset >= 0);
	CLEAR_BITS(((byte *)stream)[offset >> 3], 1 << (7 - (offset & 7)));
}

uint32 bit_rotl32(uint32 mem, int offset) {
	assert(offset >= 0 && offset <= 32);
	return (mem << offset) | (mem >> (32 - offset));
}
