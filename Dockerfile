FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/memory_hole.jar /memory_hole/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/memory_hole/app.jar"]
