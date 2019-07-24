# Notes
## Run Gemfire/Geode and PGSQL

```bash
docker/start-docker-machine.sh
```


## Create Gemfire Regions

```bash

destroy region --name Suspect
destroy region --name PoS
destroy region --name Transaction

create region --type=PARTITION  --name=Suspect
create region --type=PARTITION  --name=PoS
create region --type=PARTITION  --name=Transaction

```

## Run Console
```bash
cd TransactionsConsole
./gradlew clean assemble
cf push
```

## Run Emulator

```bash
cd TransactionEmulator
./gradlew clean assemble
cf push
# Setup PoS
curl https://yrampuria-transactions-emulator.apps.pcfone.io/emulator/setup

# Setup Transactions
curl https://yrampuria-transactions-emulator.apps.pcfone.io/emulator/post-transactions

#Setup suspect txn
curl https://yrampuria-transactions-emulator.apps.pcfone.io/emulator/post-suspect

```

## Run Cluster Service

```bash
cd ClusterService
./gradlew clean assemble
cf push

# Train model
curl https://yrampuria-clustering-service.apps.pcfone.io/clustering/train

# Check model output
curl https://yrampuria-clustering-service.apps.pcfone.io/clustering/model.pmml.xml

```

## Register Enrich App 

```bash
cd Enrich-processor
./gradlew clean assemble
cf push

# Dataflow : Register Enrich App

app register --name  enrich --type processor --force --uri https://yrampuria-repository.apps.pcfone.io/enricher-processor-1.0.0.BUILD-SNAPSHOT.jar  


```

## Import SCDF Apps

```bash
app import --uri file:///Users/yrampuria/workspace/workshops/scb-may2019/FraudDetection-Microservices/scripts/scdf-stream-apps.properties  --local
app import --uri https://dataflow.spring.io/rabbitmq-maven-latest

```

## SCDF Stream

```bash
fromgem = gemfire --region-name=Transaction --host-addresses=34.87.29.150:10334 | enrich | log

eval = :fromgem.enrich > pmml --modelLocation=https://yrampuria-clustering-service.apps.pcfone.io/clustering/model.pmml.xml --inputs='field_0=payload.distance.doubleValue(),field_1=payload.value.doubleValue()'  --inputType='application/x-spring-tuple' --outputType='application/json' | log

result = :eval.pmml > filter --expression=payload._output.result.toString().equals('2')  | gemfire --region-name=Suspect --host-addresses=34.87.29.150:10334 --keyExpression=payload.id.toString()

```

OR in SCDF Shell

```bash

stream create --name fromgem  --definition "gemfire --region-name=Transaction --host-addresses=34.87.29.150:10334 | enrich | log"
stream deploy --name fromgem --propertiesFile scripts/fromgem-deploy.properties

stream create --deploy --name eval --definition ":fromgem.enrich > pmml --modelLocation=https://yrampuria-clustering-service.apps.pcfone.io/clustering/model.pmml.xml --inputs='field_0=payload.distance.doubleValue(),field_1=payload.value.doubleValue()'  --inputType='application/x-spring-tuple' --outputType='application/json' | log"

stream create --deploy --name result --definition ":eval.pmml > filter --expression=payload._output.result.toString().equals('2')  | gemfire --region-name=Suspect --host-addresses=34.87.29.150:10334 --keyExpression=payload.id.toString()"
```

stream create --name eval2 --definition ":fromgem.enrich > log" 