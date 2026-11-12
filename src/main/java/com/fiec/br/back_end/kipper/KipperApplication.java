package com.fiec.br.back_end.kipper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
// Permite rodar tarefas em segundo plano (@Async), usado no envio das notificações push
@EnableAsync
public class KipperApplication {
    public static void main(String[] args) {
        SpringApplication.run(KipperApplication.class, args);
    }
}
