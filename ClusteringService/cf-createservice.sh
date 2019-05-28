cf cups gpdb -p '{"URL":"jdbc:postgresql://host.dev.cfdev.sh:5432/gemfire?user=pivotal&password=pivotal"}'
cf create-service p.redis cache-small redis 
cf create-service p-rabbitmq standard rabbit
