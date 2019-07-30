# Makefile

.PHONY: clean-build
clean-build: clean classes jars

.PHONY: classes
classes:
	@ OUT=./out/classes \
		make -f ./build/java8.mk classes

.PHONY: jars
jars: ./out/pd.core.jar ./out/pd.oldfashion.jar

./out/pd.core.jar: classes
	@ JAR_FILE=$@ \
	JAR_ROOT=./out/classes \
	JAVA_PACKAGES=pd.ctype,pd.encoding,pd.io,pd.net \
		make -f ./build/jar.mk

./out/pd.oldfashion.jar: classes
	@ JAR_FILE=$@ \
	JAR_ROOT=./out/classes \
	JAVA_PACKAGES=pd.adt,pd.geography,pd.geometry \
		make -f ./build/jar.mk

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf ./out/*
