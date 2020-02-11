#!/bin/bash

command_path=$1

if [ ! -e ${command_path} ]; then
	echo "ERROR: the path ${command_path} dne"
	exit 1
fi

${command_path} config set source -p test -a f69b726d-d40f-4261-b105-1ec7e6bf04d5 -u http://localhost:8080
${command_path} config set target -p test -a f69b726d-d40f-4261-b105-1ec7e6bf04d5 -u http://localhost:9080 -dn song -dh localhost -dp 9432 -du postgres -dw password


