
lazy val akkaHttpVersion = "10.2.1"
lazy val akkaVersion    = "2.6.10"

enablePlugins(AkkaGrpcPlugin)
akkaGrpcCodeGeneratorSettings += "server_power_apis"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.13.3"
    )),
    name := "grpc_service",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http2-support"       % akkaHttpVersion,

      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "com.typesafe.akka" %% "akka-discovery"           % akkaVersion,
      "ch.qos.logback"    % "logback-classic"           % "1.2.3",
    )
  )
