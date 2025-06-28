package com.patient_service.grpc;

import notification.NotificationRequest;
import notification.NotificationResponse;
import notification.NotificationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationGrpcClient.class);
    private final NotificationServiceGrpc.NotificationServiceBlockingStub blockingStub;

    public NotificationGrpcClient(
            @Value("${notification.service.address:localhost}") String serverAddress,
            @Value("${notification.service.grpc.port:9002}") int serverPort) {

        log.info("Connecting to Notification Service GRPC service at {}:{}", serverAddress, serverPort);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();

        blockingStub = NotificationServiceGrpc.newBlockingStub(channel);
    }

    public NotificationResponse sendNotification(String to, String subject, String body) {
        NotificationRequest request = NotificationRequest.newBuilder()
                .setTo(to)
                .setSubject(subject)
                .setBody(body)
                .build();

        NotificationResponse response = blockingStub.sendNotification(request);
        log.info("Received response from Notification service via GRPC: {}", response);
        return response;
    }
}
