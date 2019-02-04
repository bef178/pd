##
# for java8
#

BUILD := $(shell dirname $(lastword $(MAKEFILE_LIST)))
BUILD := $(patsubst ./%,%,$(BUILD))

include $(BUILD)/utility.mk

########

ifndef SRC
SRC := ./src
endif
SRC := $(patsubst ./%,%,$(SRC))
SRC := $(patsubst %/,%,$(SRC))

SRC_FILES := $(foreach D,$(SRC),$(call find-java-files,$(D)))

ifndef OUT
OUT := ./out
endif
OUT := $(patsubst ./%,%,$(OUT))
OUT := $(patsubst %/,%,$(OUT))

LIB_FILES := $(foreach D,"./lib",$(call find-jar-files,$(D)))
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

$(info * goal [$(MAKECMDGOALS)])

########

.PHONY: class
class: $(SRC_FILES)
	@echo "compiling ..."
	@mkdir -p $(OUT)
	@javac -source 1.8 -target 1.8 \
		-deprecation \
		-classpath $(CLASSPATH) \
		-d $(OUT) $(SRC_FILES)
