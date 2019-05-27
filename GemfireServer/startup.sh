$GEODE_HOME/bin/gfsh run --file startup.gfsh



export CFDEV_HOST=$(dig +short  host.dev.cfdev.sh)
cat startup.gfsh.tmpl | sed "s/\$CFDEV_HOST/$CFDEV_HOST/g" > startup.gfsh
$GEODE_HOME/bin/gfsh run --file startup.gfsh
