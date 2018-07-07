# Makefile

define find-files
$(shell find -L $2 -type f -iname $1 -and -not -name ".*" 2>/dev/null)
endef

define find-jar-files
$(call find-files, "*.jar", $1)
endef

define find-java-files
$(call find-files, "*.java", $1)
endef

PACKAGE := libjava

LIB_FILES := $(call find-jar-files, ./lib)

OUT=./out

.PHONY: all
all: primitive geography geometry

.PHONY: primitive geography geometry
primitive geography geometry: ls-args
	@ PACKAGE=$(PACKAGE).$@ OUT=$(OUT)/$@ LIB_FILES="$(LIB_FILES)" \
		SRC_FILES="$(call find-java-files, ./src/$(PACKAGE)/$@)" \
		make -f ./build/java.mk $(ADDITIONAL) jar

.PHONY: ls-args
ls-args:
	@echo >/dev/null
	$(if $(findstring ls-args, $(MAKECMDGOALS)), $(eval ADDITIONAL += ls-args), )

.PHONY: clean
clean:
	@ OUT=./out \
		make -f ./build/java.mk $@
