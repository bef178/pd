#-----------------------------------------------------------
# makefile for java

PACKAGE := libjava

TOP ?= .

SRC := $(TOP)/src
SRC_FILES := $(shell find -L $(SRC) -name "*.java")

LIB := $(TOP)/lib

OUT := $(TOP)/out
OUT_OBJ := $(OUT)/java/obj
OUT_TARGET := $(OUT)/$(PACKAGE).jar

#-----------------------------------------------------------

.PHONY: java
java: $(OUT_TARGET)

$(OUT_TARGET): $(SRC_FILES)
	@echo "compiling ..."
	@-mkdir -p $(OUT_OBJ)
	@javac -source 1.8 -target 1.8 $(SRC_FILES) \
		-classpath $(LIB)/junit.jar \
		-d $(OUT_OBJ)
	@echo "packaging ..."
	@jar cfm $@ $(TOP)/build/manifest.mf -C $(OUT_OBJ) .

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf $(OUT_TARGET)
	@rm -rf $(OUT)/java
