/**
 * *returns* the length of the concatenated c-string
 */
interface int str_concatenate(byte * t, const byte * s);

interface int str_compare(const byte * s1, const byte * s2);

interface int str_copy(byte * t, const byte * s);

interface const byte * str_search(const byte * s, const byte c);

interface int str_length(const byte * s);

interface int str_substring(byte * t, const byte * s, int i, int j);

interface const byte * str_kmp(const byte * s, const byte * p);

/**
 * trim 'white spaces' from head
 * returns at where the trimmed string @s 'should' start
 */
byte * str_trimFore(byte * s);

/**
 * trim 'white spaces' from tail
 * indicate where '\0' should be, instead of write it to string @s
 * returns at where the trimmed string @s 'should' end
 */
byte * str_trimHind(byte * s);
