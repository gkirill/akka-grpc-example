FROM openjdk:15
COPY ./target/scala-2.13/grpc_service-assembly-0.1.0-SNAPSHOT.jar /etc/project/grpc_service-assembly-0.1.0-SNAPSHOT.jar
CMD [ "sh", "-c", "java -Dgrpc.service.port=$GRPC_PORT -jar /etc/project/grpc_service-assembly-0.1.0-SNAPSHOT.jar" ]
