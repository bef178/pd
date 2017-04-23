#-----------------------------------------------------------
# makefile for cc

CC := gcc
CCFLAGS = -std=c99 -Werror $(addprefix -include ,$(HEADERS))

PACKAGE := libcliff

TOP := .
SRC := $(TOP)/src/$(subst .,/,$(PACKAGE))

OUT := $(TOP)/out
OUT_CC := $(OUT)/cc
OUT_TARGET_A := $(OUT)/$(PACKAGE).a
OUT_TARGET_O := $(OUT)/$(PACKAGE).o
OUT_TARGET := $(OUT_TARGET_A) $(OUT_TARGET_O)

HEADERS := $(SRC)/libcliff.h
OBJECTS :=

#-----------------------------------------------------------
# lib

LOCAL_SRC_DIR := $(SRC)/primitive

#HEADERS += $(shell find -L $(LOCAL_SRC_DIR) -name "*.h")
HEADERS += $(addprefix $(LOCAL_SRC_DIR)/,logd.h byte.h bytes.h)
OBJECTS += $(patsubst $(SRC)/%.c,$(OUT_CC)/%.o,$(shell find -L $(LOCAL_SRC_DIR) -name "*.c"))

#-----------------------------------------------------------
# abstract data type

LOCAL_SRC_DIR := $(SRC)/adt

#HEADERS += $(shell find -L $(LOCAL_SRC_DIR) -name "*.h")
HEADERS += $(addprefix $(LOCAL_SRC_DIR)/,Blob.h KeyValue.h list/List.h HashMap.h)
OBJECTS += $(patsubst $(SRC)/%.c,$(OUT_CC)/%.o,$(shell find -L $(LOCAL_SRC_DIR) -name "*.c"))

#-----------------------------------------------------------

OBJECTS := $(strip $(OBJECTS))

.PHONY: cc
cc: $(OUT_TARGET)

$(OUT_TARGET_A): $(OBJECTS)
	@echo "archiving ..."
	@ar cr $@ $^

$(OUT_TARGET_O): $(OBJECTS)
	@echo "linking ..."
	@ld -r $^ -o $@

$(OUT_CC)/%.o : $(SRC)/%.c
	@-mkdir -p $(@D)
	@$(CC) $(CCFLAGS) -c $< -o $@

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf $(OUT_TARGET)
	@rm -rf $(OUT_CC)
