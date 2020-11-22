package com.example

import java.time.LocalDateTime

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.grpc.GrpcServiceException
import akka.grpc.scaladsl.Metadata
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import io.grpc.Status
import org.slf4j.LoggerFactory

class GrpcServiceImpl(context: ActorContext[Nothing]) extends GrpcServicePowerApi {

  private val log = LoggerFactory.getLogger(s"GrpcService")

  implicit val materializer: ActorSystem[Nothing] = context.system

  private val connectorNameHeaderKey = "X-Connector-Name"

  override def connect(in: Source[MessageFromConnector, NotUsed], metadata: Metadata): Source[MessageToConnector, NotUsed] = {

    val connectorName = metadata
      .getText(connectorNameHeaderKey.toLowerCase)
      .getOrElse(throw new GrpcServiceException(Status.INVALID_ARGUMENT))

    val messagesToConnectorSource = Source.queue[MessageToConnector](1000, OverflowStrategy.backpressure)
    val (messagesToConnectorQueue, sourceToReturn) = messagesToConnectorSource.preMaterialize()

    in.runForeach(messageFromConnector => {
      log.info(s"Received ${messageFromConnector.payload} from ${connectorName}")
      messagesToConnectorQueue.offer(MessageToConnector(s"Pong to ${connectorName} at ${LocalDateTime.now()}"))
    })

    sourceToReturn

  }

}
