.PHONY: all
all: cc java

.PHONY: cc
cc:
	make -f build/cc.mk

.PHONY: java
java:
	make -f build/java.mk

.PHONY: clean
clean:
	make clean -f build/cc.mk
	make clean -f build/java.mk
