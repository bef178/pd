##
# for jar
#

ifndef MANIFEST_FILE
BUILD := $(shell dirname $(lastword $(MAKEFILE_LIST)))
MANIFEST_FILE := $(BUILD)/manifest.mf
endif

ifndef JAR_TARGET_FILE
$(error Not defined JAR_TARGET_FILE. Stop.)
endif

ifndef JAR_SRC
JAR_SRC := ./out/classes
endif

ifndef JAR_INCLUDES
JAR_INCLUDES_PATHS := .
else ifeq ($(JAR_INCLUDES),*)
JAR_INCLUDES_PATHS := .
else
JAR_INCLUDES_PATHS := $(shell echo $(JAR_INCLUDES) | sed -e "s/,/ /g" -e "s/\./\//g")
endif

.PHONY: jar
jar: $(MANIFEST_FILE) $(addprefix $(JAR_SRC)/,$(JAR_INCLUDES_PATHS))
	@echo "packaging ..."
	@jar cfm $(JAR_TARGET_FILE) $(MANIFEST_FILE) $(addprefix -C $(JAR_SRC) ,$(JAR_INCLUDES_PATHS))
