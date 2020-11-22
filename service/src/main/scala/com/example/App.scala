package com.example

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.{Http, HttpConnectionContext}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

object App {

  private val log = LoggerFactory.getLogger(s"App")

  def apply(): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => {

      implicit val system: ActorSystem[Nothing] = context.system
      import system.executionContext

      val futureBinding = Http()
        .newServerAt("0.0.0.0", ConfigFactory.load().getInt("grpc.service.port"))
        .bind(GrpcServicePowerApiHandler(new GrpcServiceImpl(context)))

      futureBinding.onComplete {
        case Success(binding) =>
          val address = binding.localAddress
          log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
        case Failure(ex) =>
          log.error("Failed to bind HTTP endpoint, terminating system", ex)
          system.terminate()
      }

      Behaviors.empty

    })

  def main(args: Array[String]): Unit = {
    ActorSystem[Nothing](App(), "grpc-service")
  }

}
