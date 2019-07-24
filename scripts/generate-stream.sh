#!/usr/bin/env bash

cat <<EOF
On SCDF Dashboard use: 

fromgem = gemfire --region-name=Transaction --host-addresses=${DOCKER_MACHINE_IP}:10334 | enrich | log

eval = :fromgem.enrich > pmml --modelLocation=https://yrampuria-clustering-service.apps.pcfone.io/clustering/model.pmml.xml --inputs='field_0=payload.distance.doubleValue(),field_1=payload.value.doubleValue()'  --inputType='application/x-spring-tuple' --outputType='application/json' | log

result = :eval.pmml > filter --expression=payload._output.result.toString().equals('2')  | gemfire --region-name=Suspect --host-addresses=${DOCKER_MACHINE_IP}:10334 --keyExpression=payload.id.toString()



On SCDF Client use: 


stream create --name fromgem  --definition "gemfire --region-name=Transaction --host-addresses=${DOCKER_MACHINE_IP}:10334 | enrich | log"
stream deploy --name fromgem --propertiesFile scripts/fromgem-deploy.properties

stream create --deploy --name eval --definition ":fromgem.enrich > pmml --modelLocation=https://yrampuria-clustering-service.apps.pcfone.io/clustering/model.pmml.xml --inputs='field_0=payload.distance.doubleValue(),field_1=payload.value.doubleValue()'  --inputType='application/x-spring-tuple' --outputType='application/json' | log"

stream create --deploy --name result --definition ":eval.pmml > filter --expression=payload._output.result.toString().equals('2')  | gemfire --region-name=Suspect --host-addresses=${DOCKER_MACHINE_IP}:10334 --keyExpression=payload.id.toString()"
EOF
