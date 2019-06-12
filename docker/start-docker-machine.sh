#!/usr/bin/env bash

# export GOOGLE_PROJECT=pa-yrampuria
# export GOOGLE_ZONE=asia-southeast1-a
SCRIPT_ROOT=$(cd `dirname $0`; pwd)



export DOCKER_MACHINE_NAME=fds

docker-machine create \
  --driver google \
  --google-disk-size "40" \
  --google-disk-type "pd-standard" \
  --google-open-port 5432/tcp \
  --google-open-port 8888/tcp \
  --google-open-port 40404/tcp \
  --google-open-port 7575/tcp \
  --google-open-port 7070/tcp \
  --google-open-port 8080/tcp \
  --google-open-port 1099/tcp \
  --google-open-port 10334/tcp \
  $* \
  $DOCKER_MACHINE_NAME
echo "Machine Created Successfully"  
export DOCKER_MACHINE_IP=$(docker-machine ip $DOCKER_MACHINE_NAME)
eval $(docker-machine env $DOCKER_MACHINE_NAME)

echo "Running gemfire and postgredb services"
docker-compose -f $SCRIPT_ROOT/docker-compose.yml up -d --build --force-recreate


cat <<EOF
Service up and running. 

You can now register gemfire and postgres as CUPS on PCF with following commands: 

cf cups gpdb -p '{"URL":"jdbc:postgresql://$DOCKER_MACHINE_IP:5432/gemfire?user=pivotal&password=pivotal"}'

cf cups gemfire -p '{"locatorHost":"$DOCKER_MACHINE_IP","locatorPort":"10334",
"RestEndpoint":"http://$DOCKER_MACHINE_IP:8888/geode/v1/"}'

Initialize Postgres database via:

(cd $PROJECT_HOME/scripts; PGPASSWORD=pivotal psql -h $DOCKER_MACHINE_IP -d gemfire -U pivotal -f model_postgres.sql)
EOF
