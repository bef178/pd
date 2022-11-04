# Makefile

.PHONY: clean-build
clean-build: clean classes jar

.PHONY: classes
classes:
	@ \
	SRC=./src/java \
	OUT=./out/classes \
	make -f ./build/java8.mk
	@ \
	SRC=./src/test \
	OUT=./out/test-classes \
	LIB="./lib ./out/classes" \
	make -f ./build/java8.mk

.PHONY: jar
jar: ./out/pd.common.jar

./out/pd.common.jar: classes
	@ \
	JAR_TARGET_FILE=$@ \
	make -f ./build/jar.mk

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf ./out
