#-----------------------------------------------------------
# makefile for typedef java

TOP ?= .

SRC_DIR := $(TOP)/src/java
SRC_FILES := $(shell find -L $(SRC_DIR) -name "*.java")

LIB_DIR := $(TOP)/lib

OUT_TOP := $(TOP)/out
OUT_DIR := $(OUT_TOP)/java
OUT_OBJ_DIR := $(OUT_DIR)/obj
OUT_JAR := $(OUT_TOP)/typedef.jar

#-----------------------------------------------------------

.PHONY: jar
jar: $(OUT_JAR)

$(OUT_JAR): $(SRC_FILES)
	@echo "compiling ..."
	@-mkdir -p $(OUT_OBJ_DIR)
	@javac $(SRC_FILES) \
		-classpath $(LIB_DIR)/junit.jar \
		-d $(OUT_OBJ_DIR)
	@echo "packaging ..."
	@jar cf $@ -C $(OUT_OBJ_DIR) .

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf $(OUT_JAR)
	@rm -rf $(OUT_DIR)
