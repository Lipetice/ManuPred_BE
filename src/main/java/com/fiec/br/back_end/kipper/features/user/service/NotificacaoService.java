package com.fiec.br.back_end.kipper.features.user.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.fiec.br.back_end.kipper.features.user.model.entities.Users;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class NotificacaoService {

    /**
     * Envia a notificação push em segundo plano (@Async). Antes o envio acontecia dentro da
     * requisição, então o app esperava o Firebase responder (às vezes mais de 1 segundo por
     * notificação) para ver o chamado atualizado. Agora a resposta volta na hora.
     */
    @Async
    public void enviar(Users destinatario, String titulo, String corpo, UUID chamadoId) {
        if (destinatario == null || destinatario.getFcmToken() == null
                || destinatario.getFcmToken().isBlank()) {
            log.debug("Usuário sem fcmToken, pulando notificação");
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase não inicializado; notificação para {} não enviada.", destinatario.getEmail());
            return;
        }

        try {
            Message.Builder builder = Message.builder()
                    .setToken(destinatario.getFcmToken())
                    .setNotification(Notification.builder()
                            .setTitle(titulo)
                            .setBody(corpo)
                            .build());
            // Envia o ID do chamado no data payload para o app abrir a tela certa
            if (chamadoId != null) {
                builder.putData("chamadoId", chamadoId.toString());
            }
            String response = FirebaseMessaging.getInstance().send(builder.build());
            log.info("Notificação enviada para {}: {}", destinatario.getEmail(), response);
        } catch (Exception e) {
            log.error("Falha ao enviar notificação para {}: {}",
                    destinatario.getEmail(), e.getMessage());
        }
    }
}
