# java.mk

PRODUCT := $(OUT)/$(PACKAGE).jar

OBJ := $(OUT)/obj

LIB_FILES := $(strip $(shell echo "$(LIB_FILES)" | sed "s/[ \t]\+/ /g"))
ifeq ($(LIB_FILES),)
  ifeq ($(CLASSPATH),)
    CLASSPATH := ""
  endif
else
  ifeq ($(CLASSPATH),)
    CLASSPATH := $(LIB_FILES)
  else
    CLASSPATH += $(LIB_FILES)
  endif
  CLASSPATH := $(shell echo $(CLASSPATH)| sed "s/ \+/:/g")
endif

#ABS_MANIFEST := $(shell dirname $(abspath $(lastword $(MAKEFILE_LIST))))/manifest.mf
#MANIFEST := $(ABS_MANIFEST:$(PWD)/%=./%)
MANIFEST := ./$(shell dirname $(lastword $(MAKEFILE_LIST)))/manifest.mf

########

define compile
	@echo "compiling ..."
	@mkdir -p $(OBJ)
	@javac -source 1.8 -target 1.8 \
		-deprecation \
		-classpath $(CLASSPATH) \
		-d $(OBJ) \
		$(SRC_FILES)
endef

define package
	@echo "packaging ..."
	@jar cfm $@ $(MANIFEST) -C $(OBJ) .
endef

.PHONY: jar
jar: $(PRODUCT)

$(PRODUCT): $(SRC_FILES) $(LIB_FILES)
	@echo "making [$@] ..."
	$(call compile)
	$(call package)

.PHONY: classes
classes: $(SRC_FILES) $(LIB_FILES)
	$(call compile)

.PHONY: ls-args
ls-args:
	@echo arguments:
	@echo "  PACKAGE=[$(PACKAGE)]"
	@echo "  SRC_FILES=[$(SRC_FILES)]"
	@echo "  LIB_FILES=[$(LIB_FILES)]"
	@echo "  OUT=[$(OUT)]"

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf $(OUT)/*
