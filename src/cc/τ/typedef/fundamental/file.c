#define LF 0x0A
#define CR 0x0D

/**
 *  Read 1 line from @fp to @buffer, excluding the line terminator, which will be consumed.
 *  Line terminators are LF, CR, or CR followed by LF.
 *  Read @size - 1 bytes at most.
 *  returns actual bytes read
 */
interface int file_getLine(FILE * fp, byte * buffer, const int size) {
	assert(fp != NULL && buffer != NULL);
	assert(size > 0);

	int n = size - 1;

	bool b = true;
	while (n > 0 && b) {
		int c = fgetc(fp);
		switch (c) {
		case CR:
			c = fgetc(fp);
			if (c != LF && c != EOF) {
				ungetc(c, fp);
			}
			// fall through
		case EOF:
			// fall through
		case LF:
			b = false;
			break;
		default:
			*buffer++ = c;
			--n;
			break;
		}
	}
	*buffer = EOS;
	return size - 1 - n;
}
