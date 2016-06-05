#-----------------------------------------------------------
# makefile for typedef cc

CC := gcc
CCFLAGS = -std=c99 -Werror $(addprefix -include ,$(HEADERS))

TOP_DIR := .
SRC_TOP := $(TOP_DIR)/src/cc
OBJ_TOP := $(TOP_DIR)/bin/cc

HEADERS := $(SRC_TOP)/τ/typedef/typedef.h
OBJECTS :=

#-----------------------------------------------------------
# lib

MODULE := τ/typedef/fundamental

SRC_DIR := $(SRC_TOP)/$(MODULE)
OBJ_DIR := $(OBJ_TOP)/$(MODULE)

INCS := $(shell find -L $(SRC_DIR) -name "*.h")
SRCS := $(shell find -L $(SRC_DIR) -name "*.c")
OBJS := $(patsubst $(SRC_DIR)/%.c,$(OBJ_DIR)/%.o,$(SRCS))

HEADERS += $(INCS)
OBJECTS += $(OBJS)

$(OBJ_DIR)/%.o : $(SRC_DIR)/%.c
	@mkdir -p $(@D)
	$(CC) $(CCFLAGS) -c $< -o $@

#-----------------------------------------------------------
# basic types

MODULE := τ/typedef/basic

SRC_DIR := $(SRC_TOP)/$(MODULE)
OBJ_DIR := $(OBJ_TOP)/$(MODULE)

INCS := $(shell find -L $(SRC_DIR) -name "*.h")
SRCS := $(shell find -L $(SRC_DIR) -name "*.c")
OBJS := $(patsubst $(SRC_DIR)/%.c,$(OBJ_DIR)/%.o,$(SRCS))

HEADERS += $(INCS)
OBJECTS += $(OBJS)

$(OBJ_DIR)/%.o : $(SRC_DIR)/%.c
	@mkdir -p $(@D)
	$(CC) $(CCFLAGS) -c $< -o $@

#-----------------------------------------------------------

OBJECTS := $(strip $(OBJECTS))

.PHONY : all clean

all : $(OBJ_TOP)/typedef.a

$(OBJ_TOP)/typedef.a : $(OBJECTS)
	ar cr $@ $^

$(OBJ_TOP)/typedef.o : $(OBJECTS)
	ld -r $^ -o $@

clean:
	@rm -rf $(OBJ_TOP)/*
