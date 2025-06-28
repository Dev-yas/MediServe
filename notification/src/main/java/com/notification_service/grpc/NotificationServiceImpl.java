package com.notification_service.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import notification.NotificationRequest;
import notification.NotificationResponse;
import notification.NotificationServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@GrpcService
public class NotificationServiceImpl extends NotificationServiceGrpc.NotificationServiceImplBase {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Override
    public void sendNotification(NotificationRequest request, StreamObserver<NotificationResponse> responseObserver) {
        System.out.println("Received notification request to: " + request.getTo());

        boolean success = true;
        String message = "";

        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(request.getTo());
            email.setSubject(request.getSubject());
            email.setText(request.getBody());
            email.setFrom(fromEmail);  // Very important!

            mailSender.send(email);
            message = "Notification sent to " + request.getTo();
        } catch (Exception e) {
            success = false;
            message = "Failed to send notification: " + e.getMessage();
            e.printStackTrace();  // Optional: for debugging
        }

        NotificationResponse response = NotificationResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
