/***************************************
 * logger.h
 * tanghao
 */

#ifndef LOGGER_H_
#define LOGGER_H_

extern const char * ERROR_INVALID_ARGUMENT;
extern const char * ERROR_MEMORY_ALLOCATION;

#define logd(msg)   logger(stdout, null, msg, __FILE__, __LINE__)

interface void logger(FILE * file, const char * prefix, const char * msg,
        const char * filepath, const int fileline);

#endif /* LOGGER_H_ */
