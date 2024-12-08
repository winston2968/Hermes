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
MAIN_HERMES_SERVER = hermes.ServerHermes
MAIN_HERMES_CLIENT = hermes.ClientHermes

# Compilation command
compile:
	find $(SRC_DIR) -name "*java" > sources.txt 
	javac -d bin -cp lib/* @sources.txt
	rm sources.txt

# Run Chatty server
run-chatty-server: compile
	java -cp "$(BIN_DIR):$(JARS)" $(MAIN_SERVER)

# Run Chatty client 
run-chatty-client: compile
	java -cp "$(BIN_DIR):$(JARS)" $(MAIN_CLIENT)

# Run hermes server
run-hermes-server: compile
	java -cp "$(BIN_DIR):$(JARS)" $(MAIN_HERMES_SERVER)

# Run hermes client
run-hermes-client: compile
	java -cp "$(BIN_DIR):$(JARS)" $(MAIN_HERMES_CLIENT)

# Run client window
run-hermes-clientGX: compile
	java -cp "$(BIN_DIR):$(JARS)" hermes.ClientHermesGX

# Clean compiled files
clean:
	rm -rf $(BIN_DIR)/*
