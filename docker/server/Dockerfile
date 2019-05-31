FROM apachegeode/geode

EXPOSE 8888
EXPOSE 40404

ADD startServer.sh /startServer.sh
ADD server-cache.xml /server-cache.xml
CMD ["/startServer.sh"]
