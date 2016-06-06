/***************************************
 * logger.c
 * tanghao
 */

interface const char * ERROR_INVALID_ARGUMENT = "E: invalid argument";
interface const char * ERROR_MEMORY_ALLOCATION = "E: fail to allocate memory";

interface void logger(FILE * file, const char * prefix, const char * msg,
        const char * filepath, const int fileline) {
    int len = (prefix != NULL) ? fprintf(file, "%s", prefix) : 0;
    if (len < 8) {
        fputc('\t', file);
    }
    fprintf(file, "\t\"%s\":%d\t%s\n", filepath, fileline, msg);
}
