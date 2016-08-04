#ifndef _INCLUDED_MAPENTRY_T
#define _INCLUDED_MAPENTRY_T

interface typedef struct MapEntry {
	void * key;
	void * value;
} MapEntry_t;

interface void MapEntry_drop(MapEntry_t * asThis);

interface MapEntry_t * MapEntry_pick(void * key, void * value);

#endif // _INCLUDED_MAPENTRY_T
