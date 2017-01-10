#-----------------------------------------------------------
# makefile for typedef cc

CC := gcc
CCFLAGS = -std=c99 -Werror $(addprefix -include ,$(HEADERS))

PACKAGE := cc.typedef

TOP := .
SRC := $(TOP)/src/$(subst .,/,$(PACKAGE))

OUT := $(TOP)/out
OUT_CC := $(OUT)/cc
OUT_TARGET_A := $(OUT)/$(PACKAGE).a
OUT_TARGET_O := $(OUT)/$(PACKAGE).o
OUT_TARGET := $(OUT_TARGET_A) $(OUT_TARGET_O)

HEADERS := $(SRC)/typedef.h
OBJECTS :=

#-----------------------------------------------------------
# lib

LOCAL_SRC_DIR := $(SRC)/primitive

HEADERS += $(shell find -L $(LOCAL_SRC_DIR) -name "*.h")
OBJECTS += $(patsubst $(SRC)/%.c,$(OUT_CC)/%.o,$(shell find -L $(LOCAL_SRC_DIR) -name "*.c"))

#-----------------------------------------------------------
# basic types

LOCAL_SRC_DIR := $(SRC)/basic

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
