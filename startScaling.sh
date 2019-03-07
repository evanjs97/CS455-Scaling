#!/bin/bash

TEST_HOME="$( cd "$( dirname "$0" )" && pwd )"

SERVER="santa-fe"
PORT="3007"
NUM_THREADS="50"
BATCH_SIZE="10"
BATCH_TIME="2"
MESSAGE_RATE="5"
CLIENTS_PER_MACHINE=1

gnome-terminal --geometry=132x43 -e "ssh -t ${SERVER} 'cd ${TEST_HOME}/build/classes/java/main; java cs455.scaling.server.Server ${PORT} ${NUM_THREADS} ${BATCH_SIZE} ${BATCH_TIME};bash;'"

SCRIPT="cd ${TEST_HOME}/build/classes/java/main; java cs455.scaling.client.Client ${SERVER} ${PORT} ${MESSAGE_RATE};"
COMMAND="gnome-terminal"
sleep 3
for i in `cat machine_list`; do
	for j in `seq 1 ${CLIENTS_PER_MACHINE}`; do
      		echo 'logging into '${i}
       		OPTIONS='--tab -e "ssh -t '$i' '$SCRIPT'"'
        	COMMAND+=" $OPTIONS"
    done
done

eval $COMMAND &
