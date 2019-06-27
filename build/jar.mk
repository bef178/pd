##
# for jar
#

ifndef MANIFEST_FILE
BUILD := $(shell dirname $(lastword $(MAKEFILE_LIST)))
MANIFEST_FILE := $(BUILD)/manifest.mf
endif

JAR_RELATIVE := $(shell echo $(JAVA_PACKAGES) | sed "s/,/ /g" | sed "s/\./\//g")

.PHONY: jar
jar: $(MANIFEST_FILE) $(addprefix $(JAR_ROOT)/,$(JAR_RELATIVE))
	@echo "packaging [$(JAR_FILE)] ..."
	@jar cfm $(JAR_FILE) $(MANIFEST_FILE) $(addprefix -C $(JAR_ROOT) ,$(JAR_RELATIVE))
