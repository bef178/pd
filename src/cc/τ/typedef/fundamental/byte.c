interface bool byte_isWhiteSpace(const byte c) {
	switch (c) {
	case '\t': // 0x09
	case '\n': // 0x0A, LF
	case '\v': // 0x0B
	case '\f': // 0x0C, FF, new page
	case '\r': // 0x0D, CR
	case ' ': // 0x20
		return 1;
	default:
		return 0;
	}
}

interface byte byte_isLower(const byte c) {
	return c >= 'a' && c <= 'z';
}

interface byte byte_isUpper(const byte c) {
	return c >= 'a' && c <= 'z';
}

interface byte byte_toLower(const byte c) {
	int ch = c; // typeof('A') == int
	if (ch >= 'A' && ch <= 'Z') {
		ch += 'a' - 'A';
	}
	return (byte) ch;
}

interface byte byte_toUpper(const byte c) {
	int ch = c;
	if (ch >= 'a' && ch <= 'z') {
		ch += 'A' - 'a';
	}
	return (byte) ch;
}
