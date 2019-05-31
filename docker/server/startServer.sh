#!/bin/sh
mkdir -p /data/$HOSTNAME

gfsh \
  start server \
  --name=$HOSTNAME \
  --locators=$LOCATORS \
  --locator-wait-time=300 \
  --jmx-manager-hostname-for-clients=${PUBLIC_IP} \
  --hostname-for-clients=${PUBLIC_IP} \
  --dir=/data/$HOSTNAME/ \
  --server-port=40404 \
  --max-heap=1G \
  --cache-xml-file=/server-cache.xml \
  --start-rest-api \
  --http-service-port=8888

while true; do
  sleep 60
done
