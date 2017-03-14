FROM frolvlad/alpine-glibc:alpine-3.4

MAINTAINER Dmitri Sotnikov <dmitri.sotnikov@gmail.com>
RUN apk update && apk add git vim tree
RUN apk upgrade libssl1.0 --update-cache
RUN apk add wget ca-certificates
RUN apk add tar
RUN apk add curl
RUN apk add bash
RUN apk add openjdk8

WORKDIR /root

RUN mkdir -p /root/bin && wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein && \
	chmod +x lein && \
	mv lein bin
RUN git clone https://github.com/yogthos/memory-hole.git

EXPOSE 3000
WORKDIR /root/memory-hole
ENV LEIN_ROOT "yes"
RUN ~/bin/lein deps
RUN ~/bin/lein uberjar

ENV APP=target/uberjar/memory-hole.jar

CMD ~/bin/lein run migrate && java -jar $APP
