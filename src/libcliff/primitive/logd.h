#ifndef _INCLUDE_LOGD
#define _INCLUDE_LOGD

extern const char * ERROR_INVALID_ARGUMENT;
extern const char * ERROR_OOM;

#define logd(msg)   logMessage(stdout, NULL, msg, __FILE__, __LINE__)
#define loge(msg)   logMessage(stderr, NULL, msg, __FILE__, __LINE__)

interface void logMessage(FILE * file, const char * prefix, const char * msg,
        const char * filepath, const int fileline);

#endif // _INCLUDE_LOGD
