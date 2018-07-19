# java.mk

JAR := $(OUT)/$(JAR)

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

define ls-args
	@echo arguments:
	@echo "  OPTIONS=[$(OPTIONS)]"
	@echo "  JAR=[$(JAR)]"
	@echo "  SRC_FILES=[$(SRC_FILES)]"
	@echo "  LIB_FILES=[$(LIB_FILES)]"
	@echo "  OUT=[$(OUT)]"
endef

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
jar: $(JAR)

$(JAR): $(SRC_FILES) $(LIB_FILES)
	$(if $(filter ls-args, $(OPTIONS)), $(call ls-args), )
	@echo "making [$(JAR)] ..."
	$(call compile)
	$(call package)

.PHONY: class
class: $(SRC_FILES) $(LIB_FILES)
	$(if $(filter ls-args, $(OPTIONS)), $(call ls-args), )
	$(call compile)

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf $(OUT)/*
