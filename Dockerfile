FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/yuggoth.jar /yuggoth/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/yuggoth/app.jar"]
