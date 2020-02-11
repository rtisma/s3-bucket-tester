.PHONY:

# Override this variable to 1, for debug mode
DEMO_MODE := 0
FORCE := 0

# Required System files
DOCKER_COMPOSE_EXE := $(shell which docker-compose)
CURL_EXE := $(shell which curl)
MVN_EXE := $(shell which mvn)

# Variables
DOCKERFILE_NAME := $(shell if [ $(DEMO_MODE) -eq 1 ]; then echo Dockerfile; else echo Dockerfile.dev; fi)
ROOT_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
MY_UID := $$(id -u)
MY_GID := $$(id -g)
THIS_USER := $(MY_UID):$(MY_GID)
ACCESS_TOKEN := f69b726d-d40f-4261-b105-1ec7e6bf04d5
PROJECT_NAME := $(shell echo $(ROOT_DIR) | sed 's/.*\///g')
PROJECT_VERSION := $(shell $(MVN_EXE) -f $(ROOT_DIR) help:evaluate -Dexpression=project.version -q -DforceStdout 2>&1  | tail -1)

# STDOUT Formatting
RED := $$(echo  "\033[0;31m")
YELLOW := $$(echo "\033[0;33m")
END := $$(echo  "\033[0m")
ERROR_HEADER :=  [ERROR]:
INFO_HEADER := "**************** "
DONE_MESSAGE := $(YELLOW)$(INFO_HEADER) "- done\n" $(END)

# Paths
DOCKER_DIR := $(ROOT_DIR)/docker
SCRATCH_DIR := $(DOCKER_DIR)/scratch/
IMPORTER_DIST_FILE := $(ROOT_DIR)/target/intermediate-song-importer-$(PROJECT_VERSION)-dist.tar.gz
RETRY_CMD := $(DOCKER_DIR)/retry-command.sh
SCORE_SERVER_LOGS_DIR := $(SCRATCH_DIR)/score-server-logs
SCORE_CLIENT_LOGS_DIR := $(SCRATCH_DIR)/score-client-logs
SONG_SERVER_LOGS_DIR  := $(SCRATCH_DIR)/song-server-logs
INTERMEDIDATE_SONG_SERVER_LOGS_DIR  := $(SCRATCH_DIR)/intermediate-song-server-logs
SONG_CLIENT_LOGS_DIR  := $(SCRATCH_DIR)/song-client-logs
SCORE_CLIENT_LOG_FILE := $(SCORE_CLIENT_LOGS_DIR)/client.log
SONG_CLIENT_OUTPUT_DIR := $(SCRATCH_DIR)/song-client-output
SCORE_CLIENT_OUTPUT_DIR := $(SCRATCH_DIR)/score-client-output
SONG_CLIENT_ANALYSIS_ID_FILE := $(SONG_CLIENT_OUTPUT_DIR)/analysisId.txt
SONG_CLIENT_SUBMIT_RESPONSE_FILE := $(SONG_CLIENT_OUTPUT_DIR)/submit_response.json


OUTPUT_DIRS := $(SONG_CLIENT_OUTPUT_DIR) $(SCORE_CLIENT_OUTPUT_DIR)
LOG_DIRS := $(SCORE_SERVER_LOGS_DIR) $(SCORE_CLIENT_LOGS_DIR) $(SONG_SERVER_LOGS_DIR) $(SONG_CLIENT_LOGS_DIR) $(INTERMEDIDATE_SONG_SERVER_LOGS_DIR)


# Commands
DOCKER_COMPOSE_CMD := echo "*********** DEMO_MODE = $(DEMO_MODE) **************" \
	&& echo "*********** FORCE = $(FORCE) **************" \
	&& DOCKERFILE_NAME=$(DOCKERFILE_NAME) MY_UID=$(MY_UID) MY_GID=$(MY_GID) \
	$(DOCKER_COMPOSE_EXE) -f $(ROOT_DIR)/docker-compose.yml
SONG_CLIENT_CMD := $(DOCKER_COMPOSE_CMD) run --rm -u $(THIS_USER) song-client bin/sing
IMPORTER_CMD := $(DOCKER_COMPOSE_CMD) run --rm intermediate-song-importer ./bin/intermediate-song-importer
SCORE_CLIENT_CMD := $(DOCKER_COMPOSE_CMD) run --rm -u $(THIS_USER) score-client bin/score-client
DC_UP_CMD := $(DOCKER_COMPOSE_CMD) up -d --build
MVN_CMD := $(MVN_EXE) -f $(ROOT_DIR)/pom.xml

#############################################################
# Internal Targets
#############################################################
$(SCORE_CLIENT_LOG_FILE):
	@mkdir -p $(SCORE_CLIENT_LOGS_DIR)
	@touch $(SCORE_CLIENT_LOGS_DIR)/client.log
	@chmod 777 $(SCORE_CLIENT_LOG_FILE)

_ping_score_server:
	@echo $(YELLOW)$(INFO_HEADER) "Pinging score-server on http://localhost:8087" $(END)
	@$(RETRY_CMD) curl  \
		-XGET \
		-H 'Authorization: Bearer f69b726d-d40f-4261-b105-1ec7e6bf04d5' \
		'http://localhost:8087/download/ping'
	@echo ""

_ping_song_server:
	@echo $(YELLOW)$(INFO_HEADER) "Pinging song-server on http://localhost:8080" $(END)
	@$(RETRY_CMD) curl --connect-timeout 5 \
		--max-time 10 \
		--retry 5 \
		--retry-delay 0 \
		--retry-max-time 40 \
		--retry-connrefuse \
		'http://localhost:8080/isAlive'
	@echo ""

_ping_intermediate_song_server:
	@echo $(YELLOW)$(INFO_HEADER) "Pinging intermediate-song-server on http://localhost:9080" $(END)
	@$(RETRY_CMD) curl --connect-timeout 5 \
		--max-time 10 \
		--retry 5 \
		--retry-delay 0 \
		--retry-max-time 40 \
		--retry-connrefuse \
		'http://localhost:9080/isAlive'
	@echo ""


_setup-object-storage: 
	@echo $(YELLOW)$(INFO_HEADER) "Setting up bucket oicr.icgc.test and heliograph" $(END)
	@if  $(DOCKER_COMPOSE_CMD) run aws-cli --endpoint-url http://object-storage:9000 s3 ls s3://oicr.icgc.test ; then \
		echo $(YELLOW)$(INFO_HEADER) "Bucket already exists. Skipping creation..." $(END); \
	else \
		$(DOCKER_COMPOSE_CMD) run aws-cli --endpoint-url http://object-storage:9000 s3 mb s3://oicr.icgc.test; \
	fi
	@$(DOCKER_COMPOSE_CMD) run aws-cli --endpoint-url http://object-storage:9000 s3 sync /score-data/ s3://oicr.icgc.test/data/

_destroy-object-storage:
	@echo $(YELLOW)$(INFO_HEADER) "Removing bucket oicr.icgc.test" $(END)
	@if  $(DOCKER_COMPOSE_CMD) run aws-cli --endpoint-url http://object-storage:9000 s3 ls s3://oicr.icgc.test ; then \
		$(DOCKER_COMPOSE_CMD) run aws-cli --endpoint-url http://object-storage:9000 s3 rb s3://oicr.icgc.test --force; \
	else \
		echo $(YELLOW)$(INFO_HEADER) "Bucket does not exist. Skipping..." $(END); \
	fi

_setup: init-log-dirs init-output-dirs $(SCORE_CLIENT_LOG_FILE)

#############################################################
# Help
#############################################################

# Help menu, displaying all available targets
help:
	@echo
	@echo "**************************************************************"
	@echo "**************************************************************"
	@echo "To dry-execute a target run: make -n <target> "
	@echo
	@echo "Available Targets: "
	@grep '^[A-Za-z][A-Za-z0-9_-]\+:.*' $(ROOT_DIR)/Makefile | sed 's/:.*//' | sed 's/^/\t/'
	@echo

intellij-importer-config: 
	@echo
	@echo
	@echo $(YELLOW)$(INFO_HEADER) In IntelliJ, configure the docker run profile with the following parameters to allow interactive debug on port 5005 $(END)
	@echo "$(YELLOW)Image ID:$(END)               $(PROJECT_NAME)_intermediate-song-importer:latest"
	@echo "$(YELLOW)Command:$(END)                bin/intermediate-song-importer run -p test -d /intermediate-song-importer/input"
	@echo "$(YELLOW)Bind Mounts:$(END)            $(DOCKER_DIR)/intermediate-song-importer-data:/intermediate-song-importer/input $(DOCKER_DIR)/intermediate-song-importer-config:/root/.intermediate-song-importer"
	@echo "$(YELLOW)Environment Variables:$(END)  JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n"
	@echo "$(YELLOW)Run Options:$(END)            --rm --network $(PROJECT_NAME)_default"
	@echo
	@echo
	@echo $(YELLOW)$(INFO_HEADER) After configuring docker run profile, configure debug profile with port forwarding 5005:5005 $(END)

#############################################################
#  Cleaning targets
#############################################################

# Kills running services and removes created files/directories
clean-docker:
	@echo $(YELLOW)$(INFO_HEADER) "Destroying running docker services" $(END)
	@$(DOCKER_COMPOSE_CMD) down -v

# Delete all objects from object storage
clean-objects: _destroy-object-storage

clean-log-dirs:
	@echo $(YELLOW)$(INFO_HEADER) "Cleaning log directories" $(END);
	@rm -rf $(OUTPUT_DIRS)

clean-output-dirs:
	@echo $(YELLOW)$(INFO_HEADER) "Cleaning output directories" $(END);
	@rm -rf $(LOG_DIRS)

# Clean everything. Kills all services, maven cleans and removes generated files/directories
clean: clean-docker clean-log-dirs clean-output-dirs

#############################################################
#  Building targets
#############################################################

init-output-dirs:
	@echo $(YELLOW)$(INFO_HEADER) "Initializing output directories" $(END);
	@mkdir -p $(OUTPUT_DIRS)

init-log-dirs:
	@echo $(YELLOW)$(INFO_HEADER) "Initializing log directories" $(END);
	@mkdir -p $(LOG_DIRS)

# Package the cli using maven. Affected by DEMO_MODE
package: 
	@if [ $(DEMO_MODE) -eq 0 ] && [ ! -e $(IMPORTER_DIST_FILE) ] ; then \
		echo $(YELLOW)$(INFO_HEADER) "Running maven package" $(END); \
		$(MVN_CMD) package -DskipTests; \
	elif [ $(DEMO_MODE) -ne 0 ]; then \
		echo $(YELLOW)$(INFO_HEADER) "Skipping maven package since DEMO_MODE=$(DEMO_MODE)" $(END); \
	elif [ $(FORCE) -eq 1 ]; then \
		echo $(YELLOW)$(INFO_HEADER) "Forcefully runnint maven package since FORCE=$(FORCE)" $(END); \
		$(MVN_CMD) package -DskipTests; \
	else \
		echo $(YELLOW)$(INFO_HEADER) "Skipping maven package since files exist: $(IMPORTER_DIST_FILE)" $(END); \
	fi

#############################################################
#  Docker targets
#############################################################

# Start ego, song, score and object-storage. Affected by DEMO_MODE
#start-services: _setup package 

build-importer: 
	@echo $(YELLOW)$(INFO_HEADER) "Forcefully building importer container" $(END)
	@$(MVN_CMD) package -DskipTests
	@$(DOCKER_COMPOSE_CMD) build intermediate-song-importer

start-services: _setup build-importer
	@echo $(YELLOW)$(INFO_HEADER) "Starting all services: ego, score, song, interemediate-song and object-storage" $(END)
	@$(DC_UP_CMD) ego-api score-server song-server intermediate-song-server object-storage 
	@$(MAKE) _ping_song_server
	@$(MAKE) _ping_intermediate_song_server
	@$(MAKE) _ping_score_server
	@$(MAKE) _setup-object-storage
	@echo $(YELLOW)$(INFO_HEADER) Succesfully started services! $(END)

#############################################################
#  Logging Targets
#############################################################
show-song-server-logs:
	@echo $(YELLOW)$(INFO_HEADER) "Showing logs for song-server" $(END)
	@$(DOCKER_COMPOSE_CMD) logs song-server
	@echo $(DONE_MESSAGE)

show-intermediate-song-server-logs:
	@echo $(YELLOW)$(INFO_HEADER) "Showing logs for song-server" $(END)
	@$(DOCKER_COMPOSE_CMD) logs song-server
	@echo $(DONE_MESSAGE)

show-score-server-logs:
	@echo $(YELLOW)$(INFO_HEADER) "Showing logs for score-server" $(END)
	@$(DOCKER_COMPOSE_CMD) logs score-server
	@echo $(DONE_MESSAGE)


#############################################################
#  Client targets
#############################################################

refresh-intermediate-song-db:
	@$(DOCKER_COMPOSE_CMD) exec intermediate-song-db sh -c "psql -U postgres song < /refresh/refresh-song-db.sql"

build:
	@$(MVN_CMD) clean package -DskipTests
	@tar zxvf target/*.tar.gz -C /tmp
	@mv /tmp/intermediate-song-importer* target/dist
	@echo "Build complete! Start by running:  ./target/dist/bin/intermediate-song-importer --help"

test: start-services build-importer refresh-intermediate-song-db
	@$(IMPORTER_CMD) run -p test -d /intermediate-song-importer/input




