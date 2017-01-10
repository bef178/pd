#ifndef _INCLUDE_LOG
#define _INCLUDE_LOG

extern const char * ERROR_INVALID_ARGUMENT;
extern const char * ERROR_OOM;

#define logd(msg)   log(stdout, NULL, msg, __FILE__, __LINE__)
#define loge(msg)   log(stderr, NULL, msg, __FILE__, __LINE__)

interface void logMessage(FILE * file, const char * prefix, const char * msg,
        const char * filepath, const int fileline);

#endif // _INCLUDE_LOG
