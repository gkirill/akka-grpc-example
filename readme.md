A simple example of running a Grpc service with Akka Http and a Grpc client with Java and Spring Boot. This example uses a 2-way stream - the client connects to the service and sends a ping messages, the service responds with a pong. This example uses Grpc metadata which can be used for example for authentication (it would need to be properly implemented). 

The only thing that is not implemented in this example is TLS communication between client and server, it would need to be implemented for serious usage. 

To run it you need docker, docker-compose and SBT installed, with that you would need to do the following:

From service directory run

`sbt assembly`

From client directory run 

`./mvnw package`

From root directory run

`docker-compose up`