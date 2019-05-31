#!/bin/sh


mkdir -p /data/$HOSTNAME

gfsh start server --name=$HOSTNAME --locators=locator[10334] --dir=/data/$HOSTNAME/ --server-port=40404 --max-heap=1G --cache-xml-file=/server-cache.xml --J=-Dgemfire.start-dev-rest-api=true --J=-Dgemfire.http-service-port=8888

while true; do
  sleep 60
done
