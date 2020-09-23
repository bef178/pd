##
# for java8
#

BUILD := $(shell dirname $(lastword $(MAKEFILE_LIST)))
include $(BUILD)/utility.mk

ifndef OUT
OUT := ./out
endif

ifndef SRC
SRC := ./src
endif

SRC_FILES := $(foreach D,$(SRC),$(call find-java-files,$(D)))

ifndef LIB
LIB := ./lib
endif

LIB_FILES := $(foreach D,$(LIB),$(call find-jar-files,$(D)))
ifeq ($(LIB_FILES),)
  ifeq ($(CLASSPATH),)
    CLASSPATH := ""
  endif
else
  ifeq ($(CLASSPATH),)
    CLASSPATH := $(LIB_FILES)
  else
    CLASSPATH += $(LIB_FILES)
  endif
  CLASSPATH := $(shell echo $(CLASSPATH)| sed "s/ \+/:/g")
endif

########

ifeq ($(MAKECMDGOALS),)
$(info * goal [(default)])
else
$(info * goal [$(MAKECMDGOALS)])
endif

########

JAVAC := /usr/lib/jvm/java-8-openjdk-amd64/bin/javac

.PHONY: classes
classes: $(SRC_FILES)
	@echo "compiling ..."
	@mkdir -p $(OUT)
	@$(JAVAC) -source 1.8 -target 1.8 \
		-deprecation \
		-classpath $(CLASSPATH) \
		-d $(OUT) $(SRC_FILES)
