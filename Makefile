# Makefile

.PHONY: clean-build
clean-build: clean classes jar

.PHONY: classes
classes:
	@ \
	SRC=./src/java \
	OUT=./out/classes \
	make -f ./build/java8.mk classes

.PHONY: jar
jar: ./out/pd.core.jar

./out/pd.core.jar: classes
	@ \
	JAR_FILE=$@ \
	JAR_ROOT=./out/classes \
	JAVA_PACKAGES=pd.fenc,pd.json,pd.log,pd.net,pd.time,pd.util \
	make -f ./build/jar.mk

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf ./out
