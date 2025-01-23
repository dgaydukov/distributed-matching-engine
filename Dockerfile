FROM java:21
ADD /target/zd-1.0.jar zd-1.0.jar
ENTRYPOINT ["java","-jar","zd-1.0.jar"]