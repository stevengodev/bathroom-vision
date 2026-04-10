package com.foliaco.vision_bathroom.firebase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.foliaco.vision_bathroom.dto.PushNotificationRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class NotificationServiceImpl implements NotificationService {

    private String notificationTopic;

    public NotificationServiceImpl(@Value("${app.notification.topic}") String notificationTopic) {
        this.notificationTopic = notificationTopic;
    }

    @Async("notificationExecutor")
    @Override
    public void notifyBathroomStatusChanged(PushNotificationRequest request) {
        Message message = Message.builder()
                .setTopic(notificationTopic)
                .setNotification(Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getBody())
                            .build())
                .putAllData(request.getData())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
            log.info("Notificación enviada");
        } catch (FirebaseMessagingException e) {
            log.error("Error enviando notificación FCM: {}", e.getMessage());
        }
    }
}
