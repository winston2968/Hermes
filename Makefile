# ====================================
#	     Makefile for Hermes
# ====================================


# Compiled and sources files folders
SRC_DIR=src
BIN_DIR=bin
LIB_DIR=lib
JARS=$(LIB_DIR)/lanterna-3.1.1.jar

# Main class
MAIN_SERVER=chatty.ServerChatty
MAIN_CLIENT=chatty.ClientChatty 

# Compilation command
compile:
	find $(SRC_DIR) -name "*java" > sources.txt 
	javac -d bin -cp lib/lanterna-3.1.1.jar @sources.txt
	rm sources.txt

# Run server
run-server: compile
	java -cp "$(BIN_DIR):$(JARS)" $(MAIN_SERVER)

# Run client 
run-client: compile
	java -cp "$(BIN_DIR):$(JARS)" $(MAIN_CLIENT)

# Clean compiled files
clean:
	rm -rf $(BIN_DIR)/*
