package com.example.client.grpcclient;

import com.example.client.grpcclient.GrpcServiceGrpc;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import com.example.client.grpcclient.MessageFromConnector;
import com.example.client.grpcclient.MessageToConnector;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
public class GrpcClient {

    final private Logger log = LoggerFactory.getLogger(GrpcClient.class);

    @Value( "${settings.grpc.host}" )
    private String GRPC_HOST;

    @Value( "${settings.grpc.port}" )
    private int GRPC_PORT;

    private StreamObserver<MessageFromConnector> messageFromConnectorStreamObserver = null;

    public void connect() {
        log.info("Attempting to connect to Grpc Service");

        final ManagedChannel channel = ManagedChannelBuilder
                .forAddress(GRPC_HOST, GRPC_PORT)
                .usePlaintext() // FIXME
                .build();

        final GrpcServiceGrpc.GrpcServiceStub stub = GrpcServiceGrpc
            .newStub(channel)
            .withCallCredentials(new CredentialsProvider());

        messageFromConnectorStreamObserver =
            stub.connect(new StreamObserver<MessageToConnector>() {

                @Override
                public void onNext(final MessageToConnector messageToConnector) {
                    log.info("Received message from server {}", messageToConnector);
                }

                @SneakyThrows
                @Override
                public void onError(Throwable throwable) {
                    log.error("onError", throwable);
                    messageFromConnectorStreamObserver = null;
                    channel.shutdown();
                    channel.awaitTermination(10000, TimeUnit.MILLISECONDS);
                    connectWithDelay();
                }

                @Override
                public void onCompleted() {
                    log.warn("onCompleted - the stream is never supposed to be completed");
                }

            });

    }

    private void sendMessageFromConnector(final MessageFromConnector messageFromConnector) {
        if (messageFromConnectorStreamObserver != null) {
            log.info("Sending messages from connector {}", messageFromConnector);
            messageFromConnectorStreamObserver.onNext(messageFromConnector);
        } else {
            log.warn("Currently there is no established Grpc connection. Message could not be sent and will be lost {}", messageFromConnector);
        }
    }

    @Scheduled(
        fixedRate = 5000,
        initialDelay = 10000
    )
    private void sendPing() {
        log.info("Time to send ping");
        final MessageFromConnector ping = MessageFromConnector.newBuilder()
            .setPayload(String.format("Ping at %s", LocalDateTime.now()))
            .build();
        sendMessageFromConnector(ping);
    }

    private void connectWithDelay() {
        log.info("GrpcService#connectWithDelay");
        final TimerTask task = new TimerTask() {
            public void run() {
                connect();
            }
        };
        final Timer timer = new Timer("GrpcClient#connectWithDelay");
        timer.schedule(task, 5000);
    }

    public static class CredentialsProvider extends CallCredentials {

        @Override
        public void applyRequestMetadata(final RequestInfo requestInfo, final Executor executor, final MetadataApplier metadataApplier) {
            executor.execute(() -> {
                try {
                    final Metadata headers = new Metadata();
                    final Metadata.Key<String> connectorName = Metadata.Key.of("X-Connector-Name", Metadata.ASCII_STRING_MARSHALLER);
                    headers.put(connectorName, "Connector-1");
                    metadataApplier.apply(headers);
                } catch(final Throwable ex) {
                    metadataApplier.fail(Status.UNAUTHENTICATED.withCause(ex));
                }
            });
        }

        @Override
        public void thisUsesUnstableApi() {}

    }

}
