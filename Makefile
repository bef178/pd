# Makefile

.PHONY: clean-build
clean-build: clean classes jars

.PHONY: classes
classes:
	@ OUT=./out/classes \
		make -f ./build/java8.mk classes

.PHONY: jars
jars: ./out/pd.core.jar

./out/pd.core.jar: classes
	@ JAR_FILE=$@ \
	JAR_ROOT=./out/classes \
	JAVA_PACKAGES=pd.ctype,pd.encoding,pd.io,pd.log,pd.net,pd.time \
		make -f ./build/jar.mk

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf ./out/*
