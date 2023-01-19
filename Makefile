# Makefile

jar:

.PHONY: clean-build
clean-build: clean jar

.PHONY: classes
classes:
	@mvn compile

.PHONY: jar
jar:
	@mvn package

.PHONY: clean
clean:
	@echo "cleaning ..."
	@mvn clean
