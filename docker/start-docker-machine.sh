#!/usr/bin/env bash

# export GOOGLE_PROJECT=pa-yrampuria
# export GOOGLE_ZONE=asia-southeast1-a


docker-machine create \
  --driver google \
  --google-disk-size "40" \
  --google-disk-type "pd-standard" \
  --google-open-port 5432/tcp \
  --google-open-port 8888/tcp \
  --google-open-port 40404/tcp \
  --google-open-port 7575/tcp \
  $* \
  gp-services
echo "Machine Created Successfully"  
eval $(docker-machine env gp-services)

echo "Running gemfire and postgredb services"
docker-compose up -d
DOCKER_MACHINE_IP=$(docker-machine ip gp-services)

cat <<EOF
Service up and running. 

You can now register gemfire and postgres as CUPS on PCF with following commands: 

cf cups gpdb -p '{"URL":"jdbc:postgresql://$DOCKER_MACHINE_IP:5432/gemfire?user=pivotal&password=pivotal"}'

cf cups gemfire -p '{"locatorHost":"$DOCKER_MACHINE_IP","locatorPort":"10334", "RestEndpoint":"http://$DOCKER_MACHINE_IP:8888/gemfire-api/v1/"}'
EOF
