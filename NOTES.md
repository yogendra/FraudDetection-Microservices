# Notes

## Run Gemfire/Geode and PGSQL

```bash
docker/start-docker-machine.sh
```

(Optional) Fetch docker environment details and put in `.env-docker` file

```
docker-machine env fds > ~/.env-docker
direnv allow
```

## Create Gemfire Regions

Connect to gemfire running on the docker machine, delete any existing region and create regions again

```
gfsh -e "connect --locator=${DOCKER_MACHINE_IP}[10334]" \
    -e "list members" \
    -e "destroy region --name Suspect" \
    -e "destroy region --name PoS" \
    -e "destroy region --name Transaction" \
    -e "create region --type=PARTITION --name=Suspect" \
    -e "create region --type=PARTITION --name=PoS" \
    -e "create region --type=PARTITION --name=Transaction"

```

Open pulse

```
open http://$DOCKER_MACHINE_IP:8080/pulse
```

## Login to CF

```bash

cf api api.run.pcfone.io
cf login -s demo --sso

```

## Create scdf, redis, gpdb and gemfire service

```bash

cf create-service p-dataflow standard scdf

cf cups gpdb -p "{\"URL\":\"jdbc:postgresql://$DOCKER_MACHINE_IP:5432/gemfire?user=pivotal&password=pivotal\"}"

cf cups gemfire -p "{\"locatorHost\":\"$DOCKER_MACHINE_IP\",\"locatorPort\":\"10334\",
\"RestEndpoint\":\"http://$DOCKER_MACHINE_IP:8888/geode/v1/\"}"

cf create-service p-redis shared-vm redis

```

Export scdf dashboard URL

```
export SCDF_URL=$(cf service scdf | grep dashboard | awk {'print $2'}  | sed -E 's/\/instance.*//')
```

## Run Console

```bash
cd TransactionsConsole
./gradlew clean assemble
cf push
cd ..
```

## Run Emulator

```bash
cd TransactionsEmulator
./gradlew clean assemble
cf push
cd ..
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
cd ..
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
cd ..
# Dataflow : Register Enrich App

app register --name  enrich --type processor --force --uri https://yrampuria-repository.apps.pcfone.io/enricher-processor-1.0.0.BUILD-SNAPSHOT.jar


```

## Download client

```

```

### SCDF Shell Access on PCF

**Without Plugin**

1. Download shell client jar

   ```
   curl -L https://repo.spring.io/release/org/springframework/cloud/spring-cloud-dataflow-shell/2.1.2.RELEASE/spring-cloud-dataflow-shell-2.1.2.RELEASE.jar -o scdf-shell.jar

   ```

1. Use oauth token form `cf` to connect

   ```
   java -jar scdf-shell.jar \
       --dataflow.uri=https://dataflow-$(cf service scdf --guid).apps.pcfone.io/ \
       --dataflow.credentials-provider-command="cf oauth-token" \
       --dataflow.mode=skipper
   ```

**With Plugin**

1. Install dataflow plugin

   ```
   cf install-plugin -r CF-Community "spring-cloud-dataflow-for-pcf"

   ```

1. Access shell using

   ```
   cf dataflow-shell scdf
   ```

   =

## Import SCDF Apps

Cloud Stream App Starter: https://raw.githubusercontent.com/yogendra/FraudDetection-Microservices/master/scripts/scdf-stream-apps.properties
Or
Latest: https://dataflow.spring.io/rabbitmq-maven-latest

**From SCDF Dashboard**

1. Goto SCDF Dashboard
1. Goto App -> Add Application(s)
1. Choose "Bulk import application"
1. In URI, put either:
   - https://dataflow.spring.io/rabbitmq-maven-latest
   - https://raw.githubusercontent.com/yogendra/FraudDetection-Microservices/master/scripts/scdf-stream-apps.properties
1. Click on "Import"

**From SCDF Shell**

```bash
app import --uri https://raw.githubusercontent.com/yogendra/FraudDetection-Microservices/master/scripts/scdf-stream-apps.properties
```

Or

```bash
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


