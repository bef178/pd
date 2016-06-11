.PHONY: all
all:
	make -f build/cc.mk
	make -f build/jar.mk

.PHONY: clean
clean:
	make clean -f build/cc.mk
	make clean -f build/jar.mk
