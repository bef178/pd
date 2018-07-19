# Makefile

define find-files
$(shell find -L $2 -type f -iname $1 -and -not -name ".*" 2>/dev/null)
endef

define find-jar-files
$(call find-files, "*.jar", $1)
endef

define find-java-files
$(call find-files, "*.java", $1)
endef

PACKAGE := libjava

LIB_FILES := $(call find-jar-files, ./lib)

OUT := ./out

TARGETS := primitive geography geometry
SPECIAL_TARGETS := adt io

####

define usage
	@echo "usage:"
	@echo "  make [GOALS]"
	@echo "  make [OPTIONS] <GOALS>"
	@echo "  OPTIONS: ls-args"
	@echo "  GOALS: jar class clean"
endef

define compile
	@ JAR=$(PACKAGE).$@.jar OPTIONS="$(strip $(OPTIONS))" \
		OUT=$(OUT)/$@ \
		LIB_FILES="$(strip $(LIB_FILES) $1)" \
		SRC_FILES="$(call find-java-files, ./src/$(PACKAGE)/$@)" \
		make -f ./build/java.mk $(GOALS)
endef

define get-jar
	$(OUT)/$(strip $1)/$(PACKAGE).$(strip $1).jar
endef

.PHONY: jar class
jar class: $(TARGETS) $(SPECIAL_TARGETS)

.PHONY: $(TARGETS)
$(TARGETS): ls-args
	$(call compile)

.PHONY: adt io
adt io: ls-args primitive
	$(call compile, $(call get-jar, primitive))

.PHONY: ls-args
ls-args:
	@echo >/dev/null
	$(eval OPTIONS :=)
	$(if $(findstring ls-args, $(MAKECMDGOALS)), $(eval OPTIONS += ls-args), )
	$(eval GOALS :=)
	$(if $(findstring class, $(MAKECMDGOALS)), $(eval GOALS += class), )
	$(if $(findstring jar, $(MAKECMDGOALS)), $(eval GOALS := $(filter-out class, $(GOALS)) jar), )
	$(if $(OPTIONS), $(if $(filter-out ls-args, $(MAKECMDGOALS)), , $(call usage)), )

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf $(OUT)/*
