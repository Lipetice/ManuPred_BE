package com.fiec.br.back_end.kipper.features.user.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.fiec.br.back_end.kipper.features.user.model.entities.Users;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificacaoService {

    public void enviar(Users destinatario, String titulo, String corpo) {
        if (destinatario == null || destinatario.getFcmToken() == null
                || destinatario.getFcmToken().isBlank()) {
            log.info("Usuário sem fcmToken, pulando notificação");
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(destinatario.getFcmToken())
                    .setNotification(Notification.builder()
                            .setTitle(titulo)
                            .setBody(corpo)
                            .build())
                    .build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Notificação enviada para {}: {}", destinatario.getEmail(), response);
        } catch (Exception e) {
            log.error("Falha ao enviar notificação para {}: {}",
                    destinatario.getEmail(), e.getMessage());
        }
    }
}