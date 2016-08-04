#-----------------------------------------------------------
# makefile for typedef cc

CC := gcc
CCFLAGS = -std=c99 -Werror $(addprefix -include ,$(HEADERS))

TOP := .
SRC_DIR := $(TOP)/src/cc

OUT_TOP := $(TOP)/out
OUT_DIR := $(OUT_TOP)/cc
OUT_CC := $(OUT_TOP)/typedef.a

HEADERS := $(SRC_DIR)/t/typedef/typedef.h
OBJECTS :=

#-----------------------------------------------------------
# lib

LOCAL_PACKAGE := t/typedef/fundamental
LOCAL_SRC_DIR := $(SRC_DIR)/$(LOCAL_PACKAGE)

HEADERS += $(shell find -L $(LOCAL_SRC_DIR) -name "*.h")
OBJECTS += $(patsubst $(SRC_DIR)/%.c,$(OUT_DIR)/%.o,$(shell find -L $(LOCAL_SRC_DIR) -name "*.c"))

#-----------------------------------------------------------
# basic types

LOCAL_PACKAGE := t/typedef/basic
LOCAL_SRC_DIR := $(SRC_DIR)/$(LOCAL_PACKAGE)

HEADERS += $(shell find -L $(LOCAL_SRC_DIR) -name "*.h")
OBJECTS += $(patsubst $(SRC_DIR)/%.c,$(OUT_DIR)/%.o,$(shell find -L $(LOCAL_SRC_DIR) -name "*.c"))

#-----------------------------------------------------------

OBJECTS := $(strip $(OBJECTS))

.PHONY: cc
cc: $(OUT_TOP)/typedef.a $(OUT_TOP)/typedef.o

$(OUT_TOP)/typedef.a: $(OBJECTS)
	ar cr $@ $^

$(OUT_TOP)/typedef.o: $(OBJECTS)
	ld -r $^ -o $@

$(OUT_DIR)/%.o : $(SRC_DIR)/%.c
	@mkdir -p $(@D)
	$(CC) $(CCFLAGS) -c $< -o $@

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf $(OUT_TOP)/typedef.a
	@rm -rf $(OUT_TOP)/typedef.o
	@rm -rf $(OUT_DIR)
