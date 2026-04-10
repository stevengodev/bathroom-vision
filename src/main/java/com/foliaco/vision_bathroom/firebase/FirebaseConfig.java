package com.foliaco.vision_bathroom.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;


@Configuration
@Log4j2
public class FirebaseConfig {

    @Value("${app.firebase.credentials-path}")
    private String firebaseConfigPath;

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    @Bean
    public FirebaseApp firebaseApp() {
        try {
            ClassPathResource serviceAccount = new ClassPathResource(firebaseConfigPath);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                log.info("Firebase application has been initialized");
                return FirebaseApp.initializeApp(options);
            } else {
                log.info("Firebase application already initialized");
                return FirebaseApp.getInstance();
            }

        } catch (IOException e) {
            log.error("Error initializing Firebase", e);
            throw new RuntimeException(e); // importante lanzar para que Spring sepa que falló
        }
    }


}
