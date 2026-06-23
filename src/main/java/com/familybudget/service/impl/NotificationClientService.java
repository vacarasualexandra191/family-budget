package com.familybudget.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationClientService {

    private static final String NOTIFICATION_SERVICE_URL = "http://NOTIFICATION-SERVICE/api/notifications";

    private final RestTemplate restTemplate;

    public void notify(String recipientUsername, String type, String message) {
        try {
            Map<String, String> payload = Map.of(
                    "recipientUsername", recipientUsername,
                    "type", type,
                    "message", message
            );
            restTemplate.postForObject(NOTIFICATION_SERVICE_URL, payload, Map.class);
            log.info("Notificare trimisa cu succes catre notification-service: tip={}, destinatar={}", type, recipientUsername);
        } catch (RestClientException ex) {
            log.warn("Nu s-a putut trimite notificarea (notification-service indisponibil): {}", ex.getMessage());
        }
    }
}
