#!/bin/sh

# default location for config is working directory
if [ -z "$CONFIG_EDN" ] ; then
	CONFIG_EDN=/app/config.edn
fi

if [ "$1" = 'memory-hole' ] ; then
	java ${JAVA_OPTS} -Dconf=${CONFIG_EDN} -jar /app/memory-hole.jar migrate
	exec java ${JAVA_OPTS} -Dconf=${CONFIG_EDN} -jar /app/memory-hole.jar "$@"
fi

exec "$@"
