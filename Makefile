.PHONY: all
all: java

.PHONY: java
java:
	make -f build/java.mk

.PHONY: clean
clean:
	make clean -f build/java.mk
