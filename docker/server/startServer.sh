#!/bin/sh
echo "Hostname : $HOSTNAME"
echo "Data Dir: /data/$HOSTNAME"

mkdir -p /data/$HOSTNAME

gfsh \
  start server \
  --name=$HOSTNAME \
  --locators=$LOCATORS \
  --locator-wait-time=120 \
  --jmx-manager-hostname-for-clients=${PUBLIC_IP} \
  --hostname-for-clients=${PUBLIC_IP} \
  --dir=/data/$HOSTNAME/ \
  --max-heap=1G \
  --cache-xml-file=/server-cache.xml \
  --start-rest-api \
  --http-service-port=8080

while true; do
  sleep 60
done
