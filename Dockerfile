FROM java:8-alpine
MAINTAINER Dmitri Sotnikov <dmitri.sotnikov@gmail.com>

ADD target/uberjar/memory-hole.jar /app/memory-hole.jar
COPY entrypoint.sh /app/entrypoint.sh
EXPOSE 3000

ENTRYPOINT [ "/app/entrypoint.sh" ]
CMD [ "memory-hole" ]

