
[[ $# -lt 1 ]] && echo "No external hostnamem provided" && exit 1
export CFDEV_HOST=$1
cat startup.gfsh.tmpl | sed "s/\$CFDEV_HOST/$CFDEV_HOST/g" > startup.gfsh
$GEODE_HOME/bin/gfsh run --file startup.gfsh
