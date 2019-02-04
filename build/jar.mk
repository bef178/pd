##
# for jar
#

ifndef MANIFEST_FILE
BUILD := $(shell dirname $(lastword $(MAKEFILE_LIST)))
BUILD := $(patsubst ./%,%,$(BUILD))
MANIFEST_FILE := $(BUILD)/manifest.mf
endif

ifndef OUT
OUT := ./out
endif
OUT := $(patsubst ./%,%,$(OUT))
OUT := $(patsubst %/,%,$(OUT))

TARGET_FILE := $(patsubst ./%,%,$(TARGET_FILE))

.PHONY: jar
jar: $(MANIFEST_FILE) $(addprefix $(OUT)/,$(PKG_SUBDIR))
	@echo "packaging [$(TARGET_FILE)] ..."
	@jar cfm $(TARGET_FILE) $(MANIFEST_FILE) $(addprefix -C $(OUT) ,$(PKG_SUBDIR))
