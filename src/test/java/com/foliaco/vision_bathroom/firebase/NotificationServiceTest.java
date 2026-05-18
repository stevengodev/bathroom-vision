package com.foliaco.vision_bathroom.firebase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;

import com.foliaco.vision_bathroom.dto.PushNotificationRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl("bathroom-topic");
    }

    @Test
    @DisplayName("Debe enviar notificación correctamente")
    void shouldSendNotificationSuccessfully() throws Exception {

        PushNotificationRequest request = PushNotificationRequest.builder()
                .title("Baño fuera de servicio")
                .body("El baño del piso 2 está cerrado")
                .data(Map.of(
                        "bathroomId", "1",
                        "status", "OUT_OF_SERVICE"
                ))
                .build();

        FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);

        try (MockedStatic<FirebaseMessaging> firebaseMock =
                     mockStatic(FirebaseMessaging.class)) {

            firebaseMock.when(FirebaseMessaging::getInstance)
                    .thenReturn(firebaseMessaging);

            when(firebaseMessaging.send(any(Message.class)))
                    .thenReturn("message-id-123");

            service.notifyBathroomStatusChanged(request);

            verify(firebaseMessaging, times(1))
                    .send(any(Message.class));
        }
    }

    @Test
    @DisplayName("Debe manejar excepción al enviar notificación")
    void shouldHandleFirebaseException() throws Exception {

        PushNotificationRequest request = PushNotificationRequest.builder()
                .title("Error")
                .body("No se pudo enviar")
                .data(Map.of(
                        "test", "value"
                ))
                .build();

        FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);

        try (MockedStatic<FirebaseMessaging> firebaseMock =
                     mockStatic(FirebaseMessaging.class)) {

            firebaseMock.when(FirebaseMessaging::getInstance)
                    .thenReturn(firebaseMessaging);

            when(firebaseMessaging.send(any(Message.class)))
                    .thenThrow(mock(FirebaseMessagingException.class));

            assertDoesNotThrow(() ->
                    service.notifyBathroomStatusChanged(request));

            verify(firebaseMessaging, times(1))
                    .send(any(Message.class));
        }
    }

    @Test
    @DisplayName("Debe construir mensaje con datos correctos")
    void shouldBuildMessageCorrectly() throws Exception {

        PushNotificationRequest request = PushNotificationRequest.builder()
                .title("Título Test")
                .body("Mensaje Test")
                .data(Map.of(
                        "key1", "value1",
                        "key2", "value2"
                ))
                .build();

        FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);

        try (MockedStatic<FirebaseMessaging> firebaseMock =
                     mockStatic(FirebaseMessaging.class)) {

            firebaseMock.when(FirebaseMessaging::getInstance)
                    .thenReturn(firebaseMessaging);

            when(firebaseMessaging.send(any(Message.class)))
                    .thenReturn("ok");

            service.notifyBathroomStatusChanged(request);

        }
    }
}