#-----------------------------------------------------------
# makefile for typedef cc

CC := gcc
CCFLAGS = -std=c99 -Werror $(addprefix -include ,$(HEADERS))

TOP := .
SRC := $(TOP)/src/cc

OUT := $(TOP)/out
OUT_CC := $(OUT)/cc
OUT_TARGET_A := $(OUT)/t.typedef.a
OUT_TARGET_O := $(OUT)/t.typedef.o
OUT_TARGET := $(OUT_TARGET_A) $(OUT_TARGET_O)

HEADERS := $(SRC)/t/typedef/typedef.h
OBJECTS :=

#-----------------------------------------------------------
# lib

LOCAL_PACKAGE := t/typedef/fundamental
LOCAL_SRC_DIR := $(SRC)/$(LOCAL_PACKAGE)

HEADERS += $(shell find -L $(LOCAL_SRC_DIR) -name "*.h")
OBJECTS += $(patsubst $(SRC)/%.c,$(OUT_CC)/%.o,$(shell find -L $(LOCAL_SRC_DIR) -name "*.c"))

#-----------------------------------------------------------
# basic types

LOCAL_PACKAGE := t/typedef/basic
LOCAL_SRC_DIR := $(SRC)/$(LOCAL_PACKAGE)

HEADERS += $(shell find -L $(LOCAL_SRC_DIR) -name "*.h")
OBJECTS += $(patsubst $(SRC)/%.c,$(OUT_CC)/%.o,$(shell find -L $(LOCAL_SRC_DIR) -name "*.c"))

#-----------------------------------------------------------

OBJECTS := $(strip $(OBJECTS))

.PHONY: cc
cc: $(OUT_TARGET)

$(OUT_TARGET_A): $(OBJECTS)
	ar cr $@ $^

$(OUT_TARGET_O): $(OBJECTS)
	ld -r $^ -o $@

$(OUT_CC)/%.o : $(SRC)/%.c
	@mkdir -p $(@D)
	$(CC) $(CCFLAGS) -c $< -o $@

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf $(OUT_TARGET)
	@rm -rf $(OUT_CC)
