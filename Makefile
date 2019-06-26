# Makefile

clean-build: clean class jar

.PHONY: class
class:
	@make -f ./build/java8.mk class

define make-jar
	PKG_SUBDIR="$1" \
	TARGET_FILE=./out/pd.$@.jar \
	make -f ./build/jar.mk
endef

.PHONY: jar adt cprime geography geometry io net encoding

jar: adt cprime geography geometry io net encoding

adt cprime geography geometry io net encoding:
	@$(call make-jar,pd/$@)

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf ./out/*
