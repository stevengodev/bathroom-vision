package com.foliaco.vision_bathroom.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.foliaco.vision_bathroom.exception.InvalidGoogleTokenException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Valida el ID Token emitido por Google (viene desde la app móvil).
 * Si el token es válido, retorna los datos del usuario de Google.
 */

@Slf4j
@Component
public class GoogleTokenValidator {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenValidator(@Value("${app.security.google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public GoogleIdToken.Payload verify(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            throw new InvalidGoogleTokenException("Google ID Token inválido o expirado");
        }

        return idToken.getPayload();
    }

}
