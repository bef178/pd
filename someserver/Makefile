# Makefile

.PHONY: clean-build
clean-build: clean classes jars

.PHONY: classes
classes:
	@ OUT=./out/classes \
		make -f ../build/java11.mk classes

.PHONY: jars
jars: ./out/someserver.jar

./out/someserver.jar: classes
	@ JAR_FILE=$@ \
	JAR_ROOT=./out/classes \
	JAVA_PACKAGES=pd.net.serv \
		make -f ../build/jar.mk

.PHONY: clean
clean:
	@echo "cleaning ..."
	@rm -rf ./out/*
